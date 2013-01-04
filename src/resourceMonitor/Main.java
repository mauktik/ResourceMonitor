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
