package project2;

public class PacketTCP {
	public int ackNum;
	public int getAckNum() {
		return ackNum;
	}
	public void setAckNum(int ackNum) {
		this.ackNum = ackNum;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	public String data;
	public long timeStamp;
	
	public PacketTCP(String data,long timeStamp,int ackNum)
	{
		this.data = data;
		this.timeStamp = timeStamp;
		this.ackNum = ackNum;
	}
	public PacketTCP() {
		// TODO Auto-generated constructor stub
	}
	

}
