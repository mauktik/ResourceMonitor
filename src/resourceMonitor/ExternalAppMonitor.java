package resourceMonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

public class ExternalAppMonitor implements ResourceMonitor 
{
	private static Logger logger = Logger.getLogger(ExternalAppMonitor.class);
	
	private Gson gson = new GsonBuilder().create();
	private Map<MapKey, MapValue> sampleMap = new HashMap<>();
	private Configuration configuration = null;
	private ExecutorService threadpool = null;
	private ServerSocket serverSocket = null;
	private Thread serverThread = null;
	private volatile boolean stopRequested = false;
	
    public ExternalAppMonitor(Configuration configuration) throws IOException
	{
		this.configuration = configuration;
		this.threadpool = Executors.newFixedThreadPool(configuration.getSystemInfo().getProcessorCount());
		this.serverSocket = new ServerSocket(configuration.getExternalAppMonitorPort());
		this.serverThread = new Thread(new ServerRunner(this.serverSocket, this));
		this.serverThread.start();
	}
	
	@Override
	public void collect() 
	{
	}

	@Override
	public void aggregate(MonitorResults results) 
	{
		Map<String, List<InstanceData>> aggregateData = new HashMap<>();
		
		synchronized (this.sampleMap) 
		{
		    Iterator<Entry<MapKey, MapValue>> iterator = this.sampleMap.entrySet().iterator();
		    
		    while (iterator.hasNext())
		    {
		    	Entry<MapKey, MapValue> pair = iterator.next();
		    	synchronized (pair.getValue()) 
		    	{
		    		if (!pair.getValue().collection.isEmpty())
		    		{
		    			if (!aggregateData.containsKey(pair.getKey().name))
		    			{
		    				aggregateData.put(pair.getKey().name, new ArrayList<InstanceData>());
		    			}
		    			aggregateData.get(pair.getKey().name).add(new InstanceData(pair.getKey().instance, pair.getValue().collection.getAverage()));
		    			pair.getValue().collection.clear();
		    		}
		    		
		    		if (this.shouldExpire(pair.getValue().lastAccessTime))
		    		{
		    			iterator.remove();
		    		}
				}
		    }
		}
		
		results.add(aggregateData);
	}
	
    @Override
	public void stop()
	{
		logger.debug("Stopping monitor");
		this.stopRequested = true;
		
		try
		{
			logger.debug("Waiting for server thread to stop");
			this.serverThread.wait();
		}
		catch (InterruptedException ex)
		{
			logger.error(String.format("Failed to wait for server thread to terminate. Exception = %s", ex.toString()));
		}
		
		try
		{
			logger.debug("Closing server socket");
			this.serverSocket.close();
		}
		catch (IOException ex)
		{
			logger.error(String.format("Failed to close the server socket. Exception = %s", ex.toString()));
		}
	}
	
	private void processClientRequestAsync(InputStream input)
	{
		String request = this.getClientRequestString(input);	
		this.threadpool.execute(new ClientRequestProcessingRunner(this, request));
	}
	
	private void processClientRequest(String request)
	{
		if (request == null || request.length() == 0)
		{
			logger.warn("Failed to convert client request to a string. Skipping.");
			return;
		}
		
		try
		{
			ClientRequest clientRequest = this.gson.fromJson(request, ClientRequest.class);
			
			if (clientRequest.version != 1)
			{
				logger.warn(String.format("Unsupported request object version %d", clientRequest.version));
				return;				
			}
			
			for (ClientRequestEntries entry : clientRequest.entries)
			{
				this.addSample(entry.name, entry.instance, entry.value);
			}
		}
		catch (JsonParseException ex)
		{
			logger.warn(String.format("Failed to parse client request. Exception = %s", ex.toString()));
			logger.debug(request);
		}
	}
	
	private void addSample(String name, String instance, long value)
	{
		MapValue mapValue = null;

		synchronized (this.sampleMap)
		{
			MapKey key = new MapKey(name, instance);
			if (!this.sampleMap.containsKey(key))
			{
				this.sampleMap.put(key, new MapValue());
			}
			mapValue = this.sampleMap.get(key);
		}
		
		synchronized (mapValue) 
		{
			mapValue.collection.addSample(value);
			mapValue.setAccessedNow();
		}
	}
	
	private String getClientRequestString(InputStream input)
	{
		try
		{
			BufferedReader reader = new BufferedReader(new InputStreamReader(input));
			StringBuilder stringBuffer = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null)
			{
				stringBuffer.append(line);
			}
			reader.close();
			return stringBuffer.toString();
		}
		catch (IOException ex)
		{
			logger.warn(String.format("Failed to read input stream. Exception = %s", ex.getMessage()));
			return "";
		}
	}	
	
	private boolean shouldExpire(long lastAccessTime)
	{
		return ((System.currentTimeMillis() - lastAccessTime) > this.configuration.getNoActivityExpiryPeriod());
	}
	
	private class ClientRequest
	{
		public long version;
		public List<ClientRequestEntries> entries;
	}
	
	public class ClientRequestEntries
	{
		public String name;
		public String instance;
		public long value;
	}	
	
	private class MapKey
	{
		public String name;
		public String instance;
		
		public MapKey(String name, String instance)
		{
			this.name = name;
			this.instance = instance;
		}
	}
	
	private final class MapValue
	{
		public SampleCollection collection;
		public long lastAccessTime;
	
		public MapValue()
		{
			this.collection = new SampleCollection(10);
			this.setAccessedNow();
		}
		
		public void setAccessedNow()
		{
			this.lastAccessTime = System.currentTimeMillis();
		}
	}
	
	private class ServerRunner implements Runnable
	{
		private ServerSocket server = null;
		private ExternalAppMonitor monitor = null;
		
		public ServerRunner(ServerSocket server, ExternalAppMonitor monitor)
		{
			this.server = server;
			this.monitor = monitor;	
		}

		@Override
		public void run() 
		{
			while (!this.monitor.stopRequested)
			{
				try
				{
					Socket client = server.accept();
					this.monitor.processClientRequestAsync(client.getInputStream());
					client.close();
				}
				catch (IOException e)
				{
					logger.error(String.format("Failed to accept socket connection. Exception = %s", e.toString()));
				}
			}
		}
	}
	
	private class ClientRequestProcessingRunner implements Runnable
	{
		private ExternalAppMonitor externalAppMonitor = null;
		private String request = null;
		
		public ClientRequestProcessingRunner(ExternalAppMonitor monitor, String request)
		{
			this.externalAppMonitor = monitor;
			this.request = request;
		}

		@Override
		public void run() 
		{
			this.externalAppMonitor.processClientRequest(this.request);
		}
	}
}
