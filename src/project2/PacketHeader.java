package project2;

public class PacketHeader {
	public long timeStamp;
	public byte[] packetData;
	public PacketHeader(long timeStamp, byte[] packetData) {
		this.timeStamp = timeStamp;
		this.packetData = packetData;
	}
	public long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}
	public byte[] getPacketByte() {
		return packetData;
	}
	public void setPacketByte(byte[] packetByte) {
		this.packetData = packetByte;
	}

}
