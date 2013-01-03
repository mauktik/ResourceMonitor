package resourceMonitor;

import java.util.List;

public class MonitorResult
{
	public String name;
	public List<InstanceData> instanceData;
	
	public MonitorResult(String name, List<InstanceData> instanceData)
	{
		this.name = name;
		this.instanceData = instanceData;
	}
}
