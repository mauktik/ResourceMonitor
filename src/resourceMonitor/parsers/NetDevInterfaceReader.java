package resourceMonitor.parsers;

public class NetDevInterfaceReader 
{
	private String name;
	private long receiveBytes;
	private long receivePackets;
	private long receiveErrors;
	private long receiveDrops;
	private long transmitBytes;
	private long transmitPackets;
	private long transmitErrors;
	private long transmitDrops;
	
	public String getName() { return this.name; }
	public long getReceiveBytes() { return this.receiveBytes; }
	public long getReceivePackets() { return this.receivePackets; }
	public long getReceiveErrors() { return this.receiveErrors; }
	public long getReceiveDrops() { return this.receiveDrops; }
	public long getTransmitBytes() { return this.transmitBytes; }
	public long getTransmitPackets() { return this.transmitPackets; }
	public long getTransmitErrors() { return this.transmitErrors; }
	public long getTransmitDrops() { return this.transmitDrops; }

	public static NetDevInterfaceReader create(String line)
	{
		NetDevInterfaceReader reader = new NetDevInterfaceReader();
		return reader.load(line) ? reader : null;
	}
	
	private boolean load(String line)
	{
		if (line == null || line.length() == 0)
		{
			return false;
		}

		Token token = new Token(0, 0);
		Long longValue = null;
		String stringValue = null;
		
		// Parse name
		if (((token = Token.getNextToken(line, 0, token)) == null) ||
			((stringValue = Common.parseString(line, token.start, token.end)) == null) ||
			!stringValue.endsWith(":"))
		{
			return false;
		}
		this.name = stringValue.substring(0, stringValue.length()-1);
		
		// Parse receive bytes
		if (((token = Token.getNextToken(line, token.end + 1, token)) == null) ||
			((longValue = Common.parseLong(line, token.start, token.end)) == null))
		{
			return false;
		}
		this.receiveBytes = longValue.longValue();		

		// Parse receive packets
		if (((token = Token.getNextToken(line, token.end + 1, token)) == null) ||
			((longValue = Common.parseLong(line, token.start, token.end)) == null))
		{
			return false;
		}
		this.receivePackets = longValue.longValue();
		
		// Parse receive errors
		if (((token = Token.getNextToken(line, token.end + 1, token)) == null) ||
			((longValue = Common.parseLong(line, token.start, token.end)) == null))
		{
			return false;
		}
		this.receiveErrors = longValue.longValue();
		
		// Parse receive drops
		if (((token = Token.getNextToken(line, token.end + 1, token)) == null) ||
			((longValue = Common.parseLong(line, token.start, token.end)) == null))
		{
			return false;
		}
		this.receiveDrops = longValue.longValue();
		
		// Parse transmit bytes
		if (((token = Token.getNthNextToken(line, token.end + 1, 5, token)) == null) ||
			((longValue = Common.parseLong(line, token.start, token.end)) == null))
		{
			return false;
		}
		this.transmitBytes = longValue.longValue();	
		
		// Parse transmit packets
		if (((token = Token.getNextToken(line, token.end + 1, token)) == null) ||
			((longValue = Common.parseLong(line, token.start, token.end)) == null))
		{
			return false;
		}
		this.transmitPackets = longValue.longValue();			
		
		// Parse transmit errors
		if (((token = Token.getNextToken(line, token.end + 1, token)) == null) ||
			((longValue = Common.parseLong(line, token.start, token.end)) == null))
		{
			return false;
		}
		this.transmitErrors = longValue.longValue();
		
		// Parse transmit drops
		if (((token = Token.getNextToken(line, token.end + 1, token)) == null) ||
			((longValue = Common.parseLong(line, token.start, token.end)) == null))
		{
			return false;
		}
		this.transmitDrops = longValue.longValue();
		
		return true;
	}
}
