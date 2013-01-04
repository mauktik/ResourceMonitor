// ---------------------------------------------------------------------------
// Copyright 2010 Mauktik Gandhi
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