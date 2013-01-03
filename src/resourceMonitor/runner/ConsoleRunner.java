package resourceMonitor.runner;

import org.apache.commons.daemon.DaemonInitException;
import resourceMonitor.Main;

public class ConsoleRunner 
{	
	public static void main(String args[]) throws DaemonInitException, Exception
	{
		System.out.println("Creating resource monitor");
		Main main = new Main();
		System.out.println("Initializing resource monitor");
		main.init(null);
		System.out.println("Starting resource monitor");
		main.start();

        System.out.println("Resource monitor running. Press enter to exit...");
        System.in.read();
		
		System.out.println("Stopping resource monitor");
		main.stop();
		System.out.println("Destroying resource monitor");
		main.destroy();
	}

}