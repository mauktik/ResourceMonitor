package resourceMonitor;

public interface ResourceMonitor 
{
	void collect();
	void aggregate(MonitorResults results);
	void stop();
}
