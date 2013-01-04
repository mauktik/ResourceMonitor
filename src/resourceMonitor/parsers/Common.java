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
package resourceMonitor.parsers;

public class Common 
{
	public static Long parseLong(String line, int start, int end)
	{
		if (line == null || line.length() == 0 || line.length() < start || line.length() < end)
		{
			return null;
		}
		
		boolean negative = false;
		if (line.charAt(start) == '+' || line.charAt(start) == '-')
		{
			negative = line.charAt(start) == '-';
			start++;
		}

		long value = 0;	
		for (int i = start; i <= end; ++i)
		{
			char ch = line.charAt(i);
			if (ch < '0' || ch > '9') 
			{ 
				return null;
			}
			
			value = value * 10 + Character.digit(ch, 10);
		}
		
		return negative ? -value : value;
	}
	
	public static String parseString(String line, int start, int end)
	{
		if (start > end || start >= line.length() || end >= line.length())
		{
			return null;
		}
		
		return line.substring(start, end + 1);
	}
}
