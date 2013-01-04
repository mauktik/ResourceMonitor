// ---------------------------------------------------------------------------
// Copyright 2012 Mauktik Gandhi
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ---------------------------------------------------------------------------
package resourceMonitor;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import resourceMonitor.common.Tracer;
import resourceMonitor.parsers.ProcStatReader;

public class ProcessResourceMonitor implements ResourceMonitor 
{
	private static Tracer tracer = Tracer.getTracer(ProcessResourceMonitor.class);
	
	private Configuration configuration = null;
	private Map<String, ProcessInfo> processInfoMap = new HashMap<String, ProcessInfo>();
	private File procRoot = new File("/proc");
	private Pattern cpuStatPattern = Pattern.compile("^cpu\\s+(\\d+) (\\d+) (\\d+) (\\d+) (\\d+) (\\d+) (\\d+) (\\d+) (\\d+) (\\d+)");
	private long lastTotalCpuTime = -1;
	private ProcessDirFilter processDirFilter = new ProcessDirFilter();
	
	public static String PageFaultsKey = "Process\\Page faults";	
	public static String UserModeTimeKey = "Process\\% Processor Time User Mode";
	public static String KernelModeTimeKey = "Process\\% Processor Time Kernel Mode";
	public static String TotalTimeKey = "Process\\% Processor Time";
	public static String VirtualMemorySizeKey = "Process\\Virtual memory size";
	public static String ResidentMemorySizeKey = "Process\\Resident memory size";
	public static String ThreadCountKey = "Process\\Thread count";
	
	public ProcessResourceMonitor(Configuration configuration)
	{
		tracer.debug("Creating ProcessResourceMonitor");
		this.configuration = configuration;
	}
	
	@Override
	public void collect() 
	{
		tracer.debug("Collecting process statistics");
		
		long totalCpuTime = this.getTotalCpuTime();
		long cpuTimeDelta = (this.lastTotalCpuTime != -1 && totalCpuTime != -1) ? (totalCpuTime - this.lastTotalCpuTime) : -1;
		this.lastTotalCpuTime = totalCpuTime;
		
		for(File file : procRoot.listFiles(processDirFilter))
		{
			if (!(file.isDirectory())) // && integerPattern.matcher(file.getName()).matches()))
			{
				tracer.debug("Skipping file or directory %s", file.toString());
				continue;
			}
			
			tracer.debug("Processing directory %s", file.toString());
			if (!this.processInfoMap.containsKey(file.getName()))
			{
				tracer.debug("Creating process info for new process");
				this.processInfoMap.put(file.getName(), new ProcessInfo(this.configuration));
			}
			
			ProcessInfo processInfo = this.processInfoMap.get(file.getName());
			this.collectProcessStat(processInfo, file, cpuTimeDelta);
		}
		
	}

	@Override
	public void aggregate(MonitorResults results) 
	{
		tracer.debug("Aggregating process results");
		Map<String, List<InstanceData>> aggregatedResults = new HashMap<String, List<InstanceData>>();
		
	    Iterator<Entry<String, ProcessInfo>> iterator = this.processInfoMap.entrySet().iterator();
	    while (iterator.hasNext())
	    {
	    	iterator.next().getValue().aggregate(aggregatedResults);
	    }
	    
	    results.add(aggregatedResults);
		this.pruneProcessInfoMap();
	}
	
	@Override
	public void stop()
	{
		tracer.debug("Stopping monitor");
	}
	
	private long getTotalCpuTime()
	{
		String line = FileReadHelper.getLineBeginningWith(new File(this.procRoot, "stat"), "cpu");
		Matcher matcher = this.cpuStatPattern.matcher(line);
		
		if (!matcher.find())
		{
			return -1;
		}
		
		return (this.getGroupValue(matcher, 1) + this.getGroupValue(matcher, 2) + this.getGroupValue(matcher, 3) +
				this.getGroupValue(matcher, 4) + this.getGroupValue(matcher, 5) + this.getGroupValue(matcher, 6) +
				this.getGroupValue(matcher, 7) + this.getGroupValue(matcher, 8));
	}
	
	private void collectProcessStat(ProcessInfo processInfo, File processDir, long totalCpuTimeDelta)
	{
		tracer.debug("Collecting process data from %s", processDir.toString());
		
		if (processInfo.statReader == null)
		{
			processInfo.statReader = new ProcStatReader(new File(processDir, "stat"));
		}

		if (!processInfo.statReader.refresh())
		{
			tracer.warn("Could not read contents of stat file for %s", processDir.toString());
			return;
		}
		
		processInfo.setProcessName(processInfo.statReader.getProcessName());
		processInfo.addSample(ProcessResourceMonitor.PageFaultsKey, processInfo.statReader.getMajorPageFaults());			
		processInfo.addSample(ProcessResourceMonitor.ThreadCountKey, processInfo.statReader.getThreadCount());
		processInfo.addSample(ProcessResourceMonitor.VirtualMemorySizeKey, processInfo.statReader.getVirtualMemorySize());
		processInfo.addSample(ProcessResourceMonitor.ResidentMemorySizeKey, this.toBytes(processInfo.statReader.getResidentMemorySize()));

		long userModeTime = processInfo.statReader.getUserModeTime();
		long kernelModeTime = processInfo.statReader.getKernelModeTime();
		if (totalCpuTimeDelta != -1 && processInfo.containsValue(UserModeTimeKey) && processInfo.containsValue(KernelModeTimeKey))
		{
			long lastUserModeTime = processInfo.getValue(ProcessResourceMonitor.UserModeTimeKey);
			long lastKernelModeTime = processInfo.getValue(ProcessResourceMonitor.UserModeTimeKey);
			
			processInfo.addSample(ProcessResourceMonitor.UserModeTimeKey, this.getCpuPercentage(userModeTime, lastUserModeTime, totalCpuTimeDelta));
			processInfo.addSample(ProcessResourceMonitor.KernelModeTimeKey, this.getCpuPercentage(kernelModeTime, lastKernelModeTime, totalCpuTimeDelta));
			processInfo.addSample(ProcessResourceMonitor.TotalTimeKey, this.getCpuPercentage((userModeTime + kernelModeTime), (lastKernelModeTime + lastUserModeTime), totalCpuTimeDelta));
		}

		processInfo.setValue(ProcessResourceMonitor.UserModeTimeKey, userModeTime);
		processInfo.setValue(ProcessResourceMonitor.KernelModeTimeKey, kernelModeTime);		
	}
	
	private void pruneProcessInfoMap()
	{
		tracer.debug("Pruning exited processes from process info map");
	    Iterator<Entry<String, ProcessInfo>> iterator = this.processInfoMap.entrySet().iterator();
	    
	    while (iterator.hasNext())
	    {
	    	Entry<String, ProcessInfo> pair = iterator.next();
	    	if (!(new File(procRoot, pair.getKey()).exists()))
	    	{
	    		tracer.info("Stopping to monitor exited process %s (%s)", pair.getKey(), pair.getValue().getProcessName());
	    		iterator.remove();
	    	}
	    }
	}
	
	private long getCpuPercentage(long newValue, long oldValue, long base)
	{
		return (long)(100 * (double)((newValue - oldValue) * 1000)/(double)(base * this.configuration.getCollectionPeriod()));
	}
	
	private long toBytes(long pages)
	{
		return this.configuration.getSystemInfo().getBytesPerPage() * pages;
	}
	
	private long getGroupValue(Matcher matcher, int group)
	{
		return Long.parseLong(matcher.group(group));
	}

	private class ProcessInfo
	{
		private Configuration configuration = null;
		private String processName = null;
		private Map<String, SampleCollection> infoTable = new HashMap<String, SampleCollection>();
		private Map<String, Long> valueTable = new HashMap<String, Long>();
		
		public ProcStatReader statReader = null;
		
		public ProcessInfo(Configuration configuration)
		{
			this.configuration = configuration;
		}
		
		public void setProcessName(String processName)
		{
			this.processName = processName;
		}
		
		public String getProcessName()
		{
			return this.processName;
		}
		
		public void setValue(String name, long value)
		{
			this.valueTable.put(name, value);
		}
		
		public long getValue(String name)
		{
			return this.valueTable.get(name);
		}
		
		public boolean containsValue(String name)
		{
			return this.valueTable.containsKey(name);
		}
		
		public void addSample(String name, long value)
		{
			if (!this.infoTable.containsKey(name))
			{
				this.infoTable.put(name, new SampleCollection(this.configuration.getSamplesToAverage() + 1));
			}
			
			this.infoTable.get(name).addSample(value);
		}
		
		public void aggregate(Map<String, List<InstanceData>> results)
		{			
		    Iterator<Entry<String, SampleCollection>> it = this.infoTable.entrySet().iterator();

		    while (it.hasNext())
		    {
		        Entry<String, SampleCollection> pair = (Entry<String, SampleCollection>)it.next();
		        
		        if (!pair.getValue().isEmpty())
		        {
		        	if (!results.containsKey(pair.getKey()))
		        	{
		        		results.put(pair.getKey(), new ArrayList<InstanceData>());
		        	}
		        	results.get(pair.getKey()).add(new InstanceData(this.processName, pair.getValue().getAverage()));
		        	pair.getValue().clear();
		        }
		    }
		}
	}
	
	public class ProcessDirFilter implements FilenameFilter
	{
		@Override
		public boolean accept(File dir, String name)
		{
			for (int i = 0; i < name.length(); ++i)
			{
				char ch = name.charAt(i);
				if (ch < '0' || ch > '9')
				{
					return false;
				}
			}
			
			return name.length() > 0;
		}
	}
}
