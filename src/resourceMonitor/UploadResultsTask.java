package resourceMonitor;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.AbstractQueue;
import java.util.List;
import java.util.TimeZone;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class UploadResultsTask extends TimerTask 
{
	private static Logger logger = Logger.getLogger(UploadResultsTask.class);
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	private final TimeZone utc = TimeZone.getTimeZone("UTC");
	private final HttpClient httpClient = new DefaultHttpClient();

	private AbstractQueue<AggregatedResults> queue = null;
	private Configuration configuration;
	
	public UploadResultsTask(Configuration configuration, AbstractQueue<AggregatedResults> queue)
	{
		this.configuration = configuration;
		this.queue = queue;
		this.dateFormat.setTimeZone(utc);
	}
	
	@Override
	public void run()
	{
		logger.debug("Running upload task");
		
		AggregatedResults results = null;
		try
		{
			while ((results = this.queue.poll()) != null)
			{
				logger.debug(String.format("Uploading results for %s", this.getDateString(results.dateTime)));
				if (!this.tryUploadResults(this.getJsonString(results)))
				{
					logger.warn("Upload results failed");
					break;
				}
			}
		}
		catch(Exception ex)
		{
			logger.warn(String.format("Uploading results failed. Exception = %s", ex.toString()));
		}

		if (results != null)
		{
			this.queue.add(results);
		}
	}
	
	private boolean tryUploadResults(String result) throws IOException
	{
		logger.info("Uploading results");
		logger.debug(result);
		
		HttpPost post = new HttpPost(this.configuration.getUploadUri());
		post.setHeader("Content-Type", "application/json");
		post.setEntity(new StringEntity(result));
		post.getParams().setParameter("tenantid", this.configuration.getTenantId());
		HttpResponse response = this.httpClient.execute(post);
		
		logger.debug(String.format("Upload results status response = %s", response.toString()));
		response.getEntity().getContent().close();
		return response.getStatusLine().getStatusCode() <= 200;
	}
	
	private String getJsonString(AggregatedResults results)
	{
		JsonObject json = new JsonObject();
		json.addProperty("Source", this.configuration.getSystemInfo().getMachineName());
		json.addProperty("Window", this.getDateString(results.dateTime));
		json.add("Guages", this.getJson(results.monitorResults));
		return json.toString();
	}
	
	private JsonElement getJson(List<MonitorResult> monitorResults)
	{
		JsonArray json = new JsonArray();
		for (MonitorResult result : monitorResults)
		{
			json.add(this.getJson(result));
		}
		return json;
	}
	
	private JsonElement getJson(MonitorResult monitorResult)
	{
		JsonArray instanceArray = new JsonArray();
		for (InstanceData data : monitorResult.instanceData)
		{
			JsonObject instanceData = new JsonObject();
			instanceData.addProperty("Instance", data.instance);
			instanceData.addProperty("Avg", data.value);
			instanceData.addProperty("Min", "0");
			instanceData.addProperty("Max", "0");
			
			instanceArray.add(instanceData);
		}
		
		JsonObject json = new JsonObject();
		json.addProperty("Name", monitorResult.name);
		json.add("Instances", instanceArray);
		return json;
	}
	
	private String getDateString(Date dateTime)
	{
		return String.format("%sZ", this.dateFormat.format(dateTime));
	}
}
