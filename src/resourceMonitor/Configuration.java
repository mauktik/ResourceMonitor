package resourceMonitor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import resourceMonitor.common.Tracer;

public class Configuration 
{	
	private static Tracer tracer = Tracer.getTracer(Configuration.class);
	private static String configurationFileName = "resourceMonitor.properties";
	
	private static int defaultCollectionPeriod = 1000 * 1;
	private static int defaultSamplesToAverage = 60;
	private static int defaultUploadResultsPeriod = 1000 * 60;
	private static int defaultNoActivityExpiryPeriod = 1000 * 60 * 15;
	private static int defaultExternalAppMonitorPort = 8071;
	private static String defaultUploadUrl = "http://127.0.0.1:8070";
	private static String defaultTenantId = "";
	
	private int collectionPeriod = defaultCollectionPeriod;
	private int samplesToAverage = defaultSamplesToAverage;
	private int uploadResultsPeriod = defaultUploadResultsPeriod;
	private int noActivityExpiryPeriod = defaultNoActivityExpiryPeriod;
	private int externalAppMonitorPort = defaultExternalAppMonitorPort;
	private URI uploadUrl = new URI(defaultUploadUrl);
	private String tenantId = defaultTenantId;
	private SystemInfo systemInfo = null;

	public Configuration(SystemInfo systemInfo) throws URISyntaxException
	{
		this.systemInfo = systemInfo;
		this.loadConfigurationFromFile();
	}
	
	public int getUploadResultsPeriod()
	{
		return this.uploadResultsPeriod;
	}
	
	public int getCollectionPeriod() 
	{
		return this.collectionPeriod;
	}
	
	public int getSamplesToAverage() 
	{
		return this.samplesToAverage;
	}
	
	public long getNoActivityExpiryPeriod()
	{
		return this.noActivityExpiryPeriod;
	}
	
	public int getExternalAppMonitorPort()
	{
		return this.externalAppMonitorPort;
	}

	public URI getUploadUri() 
	{
		return this.uploadUrl;
	}

	public String getTenantId() 
	{
		return this.tenantId;
	}
	
	public SystemInfo getSystemInfo()
	{
		return this.systemInfo;
	}

	private void loadConfigurationFromFile() throws URISyntaxException
	{
		Properties configFile = new Properties();
		
		try
		{
			InputStream configFileStream = this.getClass().getClassLoader().getResourceAsStream(Configuration.configurationFileName);
			
			if (configFileStream == null)
			{
				tracer.warn("Properties file '%s' not found. Using default values.", configurationFileName);
				return;
			}
			
			configFile.load(configFileStream);
			
			this.collectionPeriod = this.getConfigValueInt(configFile, "CollectionPeriod", defaultCollectionPeriod);
			this.samplesToAverage = this.getConfigValueInt(configFile, "SamplesToAverage", defaultSamplesToAverage);
			this.uploadResultsPeriod = this.getConfigValueInt(configFile, "UploadResultsPeriod", defaultUploadResultsPeriod);
			this.noActivityExpiryPeriod = this.getConfigValueInt(configFile, "NoActivityExpiryPeriod", defaultNoActivityExpiryPeriod);
			this.externalAppMonitorPort = this.getConfigValueInt(configFile, "ExternalAppMonitorPort", defaultExternalAppMonitorPort);
			this.uploadUrl = new URI(this.getConfigValueString(configFile, "UploadUrl", defaultUploadUrl));
			this.tenantId = this.getConfigValueString(configFile, "TenantID", defaultTenantId);
		}
		catch (IOException ex)
		{
			tracer.warn("Failed to read '%s' file. Using default values. Exception = %s", configurationFileName, ex.toString());
		}
	}
	
	private int getConfigValueInt(Properties configFile, String name, int defaultValue)
	{
		String value = configFile.getProperty(name);
		
		try
		{
			return Integer.parseInt(value);
		}
		catch (Exception ex)
		{
			tracer.warn("Configuration '%s' does not exist or does not have an integer value", name);
			return defaultValue;
		}
	}
	
	private String getConfigValueString(Properties configFile, String name, String defaultValue)
	{
		String value = configFile.getProperty(name);
		return (value != null) ? value : defaultValue;
	}
}
