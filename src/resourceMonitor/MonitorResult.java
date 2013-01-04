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
