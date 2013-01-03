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
