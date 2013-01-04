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
package resourceMonitor.parsers;

import java.io.File;
import resourceMonitor.FileReadHelper;

public final class ProcStatReader 
{
	// private Pattern procStatPattern = Pattern.compile("^\\d+ \\((.+?)\\) \\w -?\\d+ -?\\d+ -?\\d+ -?\\d+ -?\\d+ -?\\d+ -?\\d+ -?\\d+ (-?\\d+) -?\\d+ (-?\\d+) (-?\\d+) -?\\d+ -?\\d+ -?\\d+ -?\\d+ (-?\\d+) -?\\d+ (-?\\d+) (-?\\d+) (-?\\d+) -?\\d+ -?\\d+ -?\\d+ -?\\d+ -?\\d+ -?\\d+ -?\\d+ -?\\d+ -?\\d+ -?\\d+ -?\\d+ -?\\d+ -?\\d+");
	
	private File file;
	private long processId;
	private String processName;
	private long majorPageFaults;
	private long userModeTime;
	private long kernelModeTime;
	private long virtualMemorySize;
	private long residentMemorySize;
	private long threadCount;
	
	public ProcStatReader(File statFile) 
	{
		this.file = statFile;
	}
	
	public long getProcessId() { return this.processId; }
	public String getProcessName() { return this.processName; }
	public long getMajorPageFaults() { return this.majorPageFaults; }
	public long getUserModeTime() { return this.userModeTime; }
	public long getKernelModeTime() { return this.kernelModeTime; }
	public long getVirtualMemorySize() { return this.virtualMemorySize; }
	public long getResidentMemorySize() { return this.residentMemorySize; }
	public long getThreadCount() { return this.threadCount; }
	
	public boolean refresh()
	{
		return this.load(FileReadHelper.getFirstLine(this.file));		
	}
	
	private boolean load(String line)
	{
		if (line == null || line.length() == 0)
		{
			return false;
		}

		Token token = new Token(0, 0);
		Long longValue = null;
		String stringValue = null;
		
		// Parse process id
		if (((token = Token.getNextToken(line, 0, token)) == null) ||
			((longValue = Common.parseLong(line, token.start, token.end)) == null))
		{
			return false;
		}
		this.processId = longValue.longValue();
		
		// Parse process name
		if (((token = Token.getNextToken(line, token.end + 1, token)) == null) ||
			((stringValue = Common.parseString(line, token.start, token.end)) == null))
		{
			return false;
		}
		this.processName = stringValue;
		
		// Parse major page faults
		if (((token = Token.getNthNextToken(line, token.end + 1, 10, token)) == null) ||
			((longValue = Common.parseLong(line, token.start, token.end)) == null))
		{
			return false;
		}
		this.majorPageFaults = longValue.longValue();
		
		// Parse user mode time
		if (((token = Token.getNthNextToken(line, token.end + 1, 2, token)) == null) ||
			((longValue = Common.parseLong(line, token.start, token.end)) == null))
		{
			return false;
		}
		this.userModeTime = longValue.longValue();
		
		// Parse kernel mode time
		if (((token = Token.getNthNextToken(line, token.end + 1, 1, token)) == null) ||
			((longValue = Common.parseLong(line, token.start, token.end)) == null))
		{
			return false;
		}
		this.kernelModeTime = longValue.longValue();
		
		// Parse thread count
		if (((token = Token.getNthNextToken(line, token.end + 1, 5, token)) == null) ||
			((longValue = Common.parseLong(line, token.start, token.end)) == null))
		{
			return false;
		}
		this.threadCount = longValue.longValue();

		// Parse virtual memory size
		if (((token = Token.getNthNextToken(line, token.end + 1, 3, token)) == null) ||
			((longValue = Common.parseLong(line, token.start, token.end)) == null))
		{
			return false;
		}
		this.virtualMemorySize = longValue.longValue();
		
		// Parse resident memory size
		if (((token = Token.getNthNextToken(line, token.end + 1, 1, token)) == null) ||
			((longValue = Common.parseLong(line, token.start, token.end)) == null))
		{
			return false;
		}
		this.residentMemorySize = longValue.longValue();
		
		return true;
	}
}
