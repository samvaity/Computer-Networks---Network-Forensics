package project2;

import java.util.TreeMap;

public class PacketInformation {

	public int dataLength;

	public int getDataLength() {
		return dataLength;
	}

	public void setDataLength(int dataLength) {
		this.dataLength = dataLength;
	}

	public TreeMap<Integer, PacketTCP> getTotalPacketdata() {
		return totalPacketdata;
	}

	public void setTotalPacketdata(TreeMap<Integer, PacketTCP> totalPacketdata) {
		this.totalPacketdata = totalPacketdata;
	}

	public TreeMap<Integer, PacketTCP> totalPacketdata;

	public PacketInformation() {
		this.totalPacketdata = new TreeMap<Integer, PacketTCP>();
		this.dataLength = 0;

	}

}
