package resourceMonitor;

import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.daemon.*;
import org.apache.log4j.Logger;

public class Main implements Daemon 
{
	private static Logger logger = Logger.getLogger(Main.class.getName());
	private Timer monitoringTimer = null;
	private Timer uploadResultsTimer = null;
	private MonitoringTask monitoringTask = null;
	
	@Override
	public void destroy()
	{
		logger.debug("Executing destroy");
		this.monitoringTimer = null;
		this.uploadResultsTimer = null;
	}

	@Override
	public void init(DaemonContext arg0) throws DaemonInitException, Exception
	{
		logger.debug("Executing init");
		this.monitoringTimer = new Timer();
		this.uploadResultsTimer = new Timer();
	}

	@Override
	public void start() throws Exception
	{
		logger.debug("Executing start");
		ConcurrentLinkedQueue<AggregatedResults> resultQueue = new ConcurrentLinkedQueue<AggregatedResults>();
		Configuration configuration = new Configuration(new SystemInfo());
		MonitorResults results = new MonitorResults(resultQueue);
		
		logger.debug("Starting monitoring and update result timers and tasks");
		this.monitoringTask = new MonitoringTask(configuration, results);
		this.monitoringTimer.schedule(this.monitoringTask, 0l, (configuration.getCollectionPeriod()));
		this.uploadResultsTimer.schedule((new UploadResultsTask(configuration, resultQueue)), 0l, (configuration.getUploadResultsPeriod()));
	}

	@Override
	public void stop() throws Exception
	{
		logger.debug("Executing stop. Cancelling monitoring and update result timers");
		this.monitoringTimer.cancel();
		this.uploadResultsTimer.cancel();
		
		if (this.monitoringTask != null)
		{
			this.monitoringTask.stop();
		}
	}
}
