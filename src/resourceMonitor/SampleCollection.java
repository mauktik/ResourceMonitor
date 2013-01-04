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

public class SampleCollection 
{
	private long[] sampleValues;
	private int sampleCount;
	
	public SampleCollection(int initialSize) 
	{
		this.sampleValues = new long[initialSize];
		this.sampleCount = 0;
	}
	
	public void addSample(long value) 
	{
		this.growArrayIfNeeded();
		this.sampleValues[this.sampleCount] = value;
		this.sampleCount++;
	}
	
	public long getAverage() 
	{	
		if (this.sampleCount == 0) 
		{
			return 0;
		}
		
		long total = 0;
		for (int i = 0; i < this.sampleCount; ++i) 
		{
			total += this.sampleValues[i];
		}
		return (total / this.sampleCount);
	}
	
	public void clear() 
	{
		this.sampleCount = 0;
	}
	
	public boolean isEmpty()
	{
		return (this.sampleCount == 0);
	}
	
	private void growArrayIfNeeded()
	{
		if (this.sampleCount == this.sampleValues.length)
		{
			long[] newSampleValues = new long[this.sampleValues.length * 2];
			
			for (int i = 0; i < this.sampleValues.length; ++i)
			{
				newSampleValues[i] = this.sampleValues[i];
			}
			
			this.sampleValues = newSampleValues;
		}
	}
}
