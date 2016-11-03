package project2;

public class ConnectionInformation {

	public String sourceIp;
	public String destIp;
	public StringBuilder sourcePort;
	public StringBuilder destPort;
	
	public ConnectionInformation()
	{
		
	}
	public ConnectionInformation(String sourceIp,String destIp,StringBuilder sourcePort,StringBuilder destPort)
	{
		this.sourceIp = sourceIp;
		this.destIp = destIp;
		
		this.sourcePort = sourcePort;
		this.destPort = destPort;
			
	}
	
	
}
