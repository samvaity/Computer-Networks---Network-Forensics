package project2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;

public class project2 {
	// static = new ArrayList<byte[]>();
	static int countPackets = 0;
	static int task = 4;

	public static void main(String args[]) throws FileNotFoundException {
		/* Read Arguments */
		// int task = Integer.parseInt(args[0]);
		// int task = 2;
		List<PacketHeader> packets = readPcapFile();

		switch (task) {
		case 1:
			int[] packetInfo = commonUtil(packets, task, null, null);
			System.out.print(countPackets + " " + packetInfo[0] + " " + packetInfo[1] + " " + packetInfo[2] + " "
					+ packetInfo[3] + "\n");
			break;
		case 2:
		case 3:
		case 4:
			HashMap<String, PacketInformation> uplinkMap = new HashMap<String, PacketInformation>();
			HashMap<String, PacketInformation> downLinkMap = new HashMap<String, PacketInformation>();
			commonUtil(packets, task, uplinkMap, downLinkMap);
			print(uplinkMap, downLinkMap, task);
			break;

		/*
		 * HashMap<String, PacketInformation> uplinkMap1 = new HashMap<String,
		 * PacketInformation>(); HashMap<String, PacketInformation> downLinkMap1
		 * = new HashMap<String, PacketInformation>(); commonUtil(packets, task,
		 * uplinkMap1, downLinkMap1); print(uplinkMap1, downLinkMap1, task);
		 */
		// doTask3(uplinkMap1,downLinkMap1);

		/*
		 * HashMap<String, PacketInformation> uplinkMap2 = new HashMap<String,
		 * PacketInformation>(); HashMap<String, PacketInformation> downLinkMap2
		 * = new HashMap<String, PacketInformation>(); commonUtil(packets, task,
		 * uplinkMap2, downLinkMap2); print(uplinkMap2, downLinkMap2, task);
		 */
		default:
			break;
		}

	}

	private static List<PacketHeader> readPcapFile() {

		PacketHeader packHead = null;
		List<PacketHeader> packets = new ArrayList<PacketHeader>();
		// HashMap<Long,byte[]> packets = new HashMap<Long,byte[]>();
		try {
			/*
			 * String next; BufferedReader reader = new BufferedReader(new
			 * InputStreamReader(System.in)); while (true) { next =
			 * reader.readLine(); if (next == null) { break; }
			 * data.append(next);
			 * 
			 * }
			 */
			Path path = Paths.get("D:\\Fall 2016\\CN\\Project2\\project2\\testdata\\task4.test2.pcap");

			byte[] byteData = Files.readAllBytes(path);

			int startIndex = 32, offset = 4;
			int timS = 24, ofime = 8;

			int packetStart = 0;
			byte[] headData = new byte[4];
			byte[] timeD = new byte[8];
			long timeStamp = 0;
			while (startIndex < byteData.length) {
				for (int i = startIndex, j = 0; i < startIndex + offset; i++, j++) {
					headData[j] = byteData[i];
				}
				for (int i = timS, j = 0; i < timS + ofime; i++, j++) {
					timeD[j] = byteData[i];
				}

				int packetLength = calculatePacketDataLength(headData);

				// update packet start;
				packetStart = startIndex + 8;

				byte[] packetData = Arrays.copyOfRange(byteData, packetStart, packetStart + packetLength);

				long l = calculateTimeStamp(Arrays.copyOfRange(timeD, 0, 4));
				l = l * 1000000;
				long m = calculateTimeStamp(Arrays.copyOfRange(timeD, 4, timeD.length));
				timeStamp = l + m;

				packHead = new PacketHeader(timeStamp, packetData);
				packets.add(packHead);
				countPackets++;

				// update to next packet length
				startIndex += 8 + packetLength + 8;
				timS += packetLength + 16;
				timeStamp = 0;

				// System.out.println(timeStamp);

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return packets;
	}

	private static long calculateTimeStamp(byte[] timeD) {
		long time = 0;
		for (int i = timeD.length - 1; i >= 0; i--) {
			time <<= 8;
			time |= (long) (timeD[i] & 0xFF);
		}
		return time;

	}

	// calculate the packet length
	private static int calculatePacketDataLength(byte[] headData) {
		int packetLength = 0;
		for (int i = headData.length - 1; i >= 0; i--) {
			packetLength <<= 8;
			packetLength |= (int) headData[i] & 0xFF;
		}
		return packetLength;
	}

	private static int[] commonUtil(List<PacketHeader> packets, int task, HashMap<String, PacketInformation> uplinkMap,
			HashMap<String, PacketInformation> downLinkMap) throws FileNotFoundException {

		int countIpPackets = 0, countTCP = 0, countUDP = 0;

		int[] result = new int[4];
		HashMap<String, Integer> tcpConnections = new HashMap<String, Integer>();
		for (int i = 0; i < packets.size(); i++) {
			byte[] temp = packets.get(i).getPacketByte();
			long packetTime = packets.get(i).timeStamp;
			if (temp[12] == 8 && temp[13] == 0) {
				countIpPackets++;
			}
			if (temp[23] == 6) {
				countTCP++;
				buildTCPConnection(temp, task, tcpConnections, uplinkMap, downLinkMap, packetTime);
			} else if (temp[23] == 17) {
				countUDP++;
			}
		}
		result[0] = countIpPackets++;
		result[1] = countTCP++;
		result[2] = countUDP++;
		result[3] = tcpConnections.size() / 2;

		return result;

	}

	private static void print(HashMap<String, PacketInformation> uplinkMap,
			HashMap<String, PacketInformation> downLinkMap, int task) throws FileNotFoundException {

		StringBuilder upLinkData = new StringBuilder();
		HashMap<String, String> conString = new HashMap<String, String>();
		TreeMap<Long, String> timeSortedData = new TreeMap<Long, String>();

		for (String conn : uplinkMap.keySet()) {
			int upLinkDataLength = uplinkMap.get(conn).dataLength;

			for (Integer s : uplinkMap.get(conn).totalPacketdata.keySet()) {
				upLinkData.append(uplinkMap.get(conn).totalPacketdata.get(s).data);
			}

			String rev = findReverse(conn);
			int downLinkDataLength = 0;
			if (downLinkMap.containsKey(rev)) {
				downLinkDataLength = downLinkMap.get(rev).dataLength;

				for (Integer s : downLinkMap.get(rev).totalPacketdata.keySet()) {

					upLinkData.append(downLinkMap.get(rev).totalPacketdata.get(s).data);

				}

				// System.out.println(upLinkData.toString());
				if (task == 3 || task == 4) {
					TreeMap<Integer, PacketTCP> upLinkSeq = uplinkMap.get(conn).totalPacketdata;
					TreeMap<Integer, PacketTCP> downLinkSeq = downLinkMap.get(rev).totalPacketdata;
					// System.out.println(downLinkSeq.get(key))
					doTask3(upLinkSeq, downLinkSeq, uplinkMap.get(conn), downLinkMap.get(rev), timeSortedData);

				}

			}

			conString.put(conn.replace("|", " ") + " " + upLinkDataLength + " " + downLinkDataLength,
					upLinkData.toString());
			upLinkData.setLength(0);

		}
		List<String> conList = new LinkedList<String>();
		for (String e : conString.keySet()) {
			conList.add(e);
		}
		Collections.sort(conList);

		StringBuilder totalData = new StringBuilder();
		if (task == 2) {
			for (String str : conList) {
				System.out.print(str + "\n");

				totalData.append(conString.get(str));
			}

			System.out.print(totalData.toString());
		}

		if (task == 3 || task == 4) {
			if (task == 4) {
				timeSortedData.put(Long.MAX_VALUE, "0\r\n\r\n");

				for (long ts : timeSortedData.keySet()) {

					System.out.print(timeSortedData.get(ts));
				}
			}
			if (task == 3) {
				for (long ts : timeSortedData.keySet()) {

					System.out.print(timeSortedData.get(ts) + "\n");
				}
			}
		}

	}

	private static void doTask3(TreeMap<Integer, PacketTCP> upLinkSeq, TreeMap<Integer, PacketTCP> downLinkSeq,
			PacketInformation upLinkpacketInformation, PacketInformation downLinkpacketInformation,
			TreeMap<Long, String> timeSortedData) {

		StringBuilder totalResponseData = new StringBuilder();
		long upLinkTimeStamp = 0;
		for (int i : upLinkSeq.keySet()) {
			String reqData = upLinkSeq.get(i).data;
			if (reqData.length() > 0) {
				int toMatch = i + reqData.length();
				upLinkTimeStamp = upLinkSeq.get(i).timeStamp;
				for (int j : downLinkSeq.keySet()) {
					int ack = downLinkSeq.get(j).ackNum;
					if (toMatch == ack) {
						// respData = ;
						// System.out.println(respData);
						if (downLinkSeq.get(j).data.length() > 0) {
							totalResponseData.append(downLinkSeq.get(j).data);
						}
					}

				}

				// System.out.println((totalResponseData.toString()));
				getSpecificFormatData(timeSortedData, upLinkTimeStamp, totalResponseData.toString(), reqData);
				totalResponseData.setLength(0);

				// System.out.println(reqData + totalResponseData.toString());
			}
		}

	}

	private static void getSpecificFormatData(TreeMap<Long, String> timeSortedData, long upLinkTimeStamp,
			String responseData, String reqData) {
		String[] reqStr = reqData.split("\\r?\\n");

		StringBuilder finalConnData = new StringBuilder();
		// System.out.println(responseData);
		String host = "", getType = "";
		boolean imageFound = false;
		for (String content : reqStr) {
			if (content.toLowerCase().startsWith("host:"))
				host = (content.substring(6, content.length()).trim().toLowerCase());
			else if (content.toLowerCase().startsWith("get"))
				getType = (content.substring(4, content.length() - 9).trim().toLowerCase());
			else if (content.toUpperCase().startsWith("post"))
				getType = (content.substring(5, content.length() - 9).trim().toLowerCase());
			else if (content.toUpperCase().startsWith("put"))
				getType = (content.substring(4, content.length() - 9).trim().toLowerCase());
			else if (content.toUpperCase().startsWith("delete"))
				getType = (content.substring(7, content.length() - 9).trim().toLowerCase());
			else if (content.toUpperCase().startsWith("head"))
				getType = (content.substring(5, content.length() - 9).trim().toLowerCase());
		}
		finalConnData.append(getType + " ");
		if (host != null) {
			finalConnData.append(host + " ");
		} else {
			host = "0";
		}
		if (getType.endsWith("gif") || getType.endsWith("jpeg") || getType.endsWith("jpg") || getType.endsWith("webp")
				|| getType.endsWith("png")) {
			imageFound = true;
		}
		String respCode = "0";
		int respContentCoding = 0;
		String[] responsiveData = responseData.split("\r\n\r\n", 2);
		String[] resStr = responsiveData[0].split("\\r?\\n");
		StringBuffer chunkData = new StringBuffer();
		String resp200OK = "";
		// String[] resStr = chunkedData[0];
		boolean isChunked = false;
		if (task == 3 || imageFound == true) {
			for (String response : resStr) {
				if (response.toLowerCase().startsWith("http/1.1")) {
					respCode = (response.substring(9, 13)).trim();
					resp200OK = (response.substring(9, 15)).trim();
				} else if (response.toLowerCase().trim().startsWith("content-length:")
						|| (response.toLowerCase().trim().equals("transfer-encoding: chunked"))) {
					if ((response.toLowerCase().equals("transfer-encoding: chunked"))) {
						isChunked = true;
					} else {
						isChunked = false;
					}
					if (isChunked) {

						respContentCoding = calculateTranferEncoding(responsiveData[1], chunkData);
					} else {
						respContentCoding = Integer.parseInt((response.substring(15, response.length())).trim());
					}
				}
			}
			if (task == 3) {
				finalConnData.append(respCode + " ");
				finalConnData.append(respContentCoding);
				// System.out.println("final map timestamp"+upLinkTimeStamp);
				timeSortedData.put(upLinkTimeStamp, finalConnData.toString());
			}
		}
		if (task == 4 && imageFound == true) {

			StringBuilder imageData = new StringBuilder();
			if (resp200OK.equals("200 OK")) {
				if (!isChunked) {
					imageData.append(Integer.toHexString(respContentCoding)).append("\r\n").append((responsiveData[1]))
							.append("\r\n");
				} else {
					imageData.append(Integer.toHexString(respContentCoding)).append("\r\n")
							.append((chunkData).toString()).append("\r\n");
				}

				timeSortedData.put(upLinkTimeStamp, imageData.toString());
				imageData.setLength(0);
			}
		}

	}

	private static int calculateTranferEncoding(String string, StringBuffer chunkData) {
		int value = 0;
		int temp = 0;
		while (!string.equals("0\r\n\r\n")) {
			int index = string.indexOf("\r\n");
			String str = string.substring(0, index);
			temp = Integer.parseInt(str, 16);
			value += temp;
			chunkData.append(string.substring(index + 2, index + 2 + temp).trim());
			string = string.substring(Integer.parseInt(str, 16) + index + 4);

		}
		return value;
	}

	private static String findReverse(String conn) {
		String[] tokens = conn.split("\\|");
		for (int start = 0, end = tokens.length - 1; start < end; start++, end--) {
			String temp = tokens[start];
			tokens[start] = tokens[end];
			tokens[end] = temp;
		}
		return String.join("|", tokens);

	}

	private static void buildTCPConnection(byte[] packet, int task, HashMap<String, Integer> tcpConnections,
			HashMap<String, PacketInformation> uplinkMap, HashMap<String, PacketInformation> downLinkMap,
			long packetTimeStamp) {

		StringBuilder tcpTuple = new StringBuilder();
		StringBuilder reverseTcpTuple = new StringBuilder();

		String sourceIpAddr = ((int) packet[26] & 0xFF) + "." + ((int) packet[27] & 0xFF) + "."
				+ ((int) packet[28] & 0xFF) + "." + ((int) packet[29] & 0xFF);
		String destIpAddr = ((int) packet[30] & 0xFF) + "." + ((int) packet[31] & 0xFF) + "."
				+ ((int) packet[32] & 0xFF) + "." + ((int) packet[33] & 0xFF);

		String sourcePort = covertToDecimal(new byte[] { packet[34], packet[35] });
		String destPort = covertToDecimal(new byte[] { packet[36], packet[37] });

		tcpTuple.append(sourceIpAddr);
		tcpTuple.append(" ");
		tcpTuple.append(sourcePort);
		tcpTuple.append("|");
		tcpTuple.append(destIpAddr);
		tcpTuple.append(" ");
		tcpTuple.append(destPort);

		reverseTcpTuple.append(destIpAddr);
		reverseTcpTuple.append(" ");
		reverseTcpTuple.append(destPort);
		reverseTcpTuple.append("|");
		reverseTcpTuple.append(sourceIpAddr);
		reverseTcpTuple.append(" ");
		reverseTcpTuple.append(sourcePort);

		if (task == 1) {

			int i = 0;
			if (!tcpConnections.containsKey(tcpTuple.toString())) {
				tcpConnections.put(tcpTuple.toString(), i);
				i++;
			}
			if (!tcpConnections.containsKey(reverseTcpTuple.toString())) {
				tcpConnections.put(reverseTcpTuple.toString(), i);
				i++;
			}

		}

		if (task == 2 || task == 3 || task == 4) {
			setUpLinkDownLinkUtil(packet, tcpTuple, reverseTcpTuple, sourcePort, destPort, uplinkMap, downLinkMap,
					packetTimeStamp);
		}

	}

	private static void setUpLinkDownLinkUtil(byte[] packet, StringBuilder tcpTuple, StringBuilder reverseTcpTuple,
			String sourcePort, String destPort, HashMap<String, PacketInformation> uplinkMap,
			HashMap<String, PacketInformation> downLinkMap, long packetTimeStamp) {

		String totalLength = covertToDecimal(new byte[] { packet[16], packet[17] });
		// System.out.println(totalLength);
		int ipHeaderLength = ((int) (4 * (packet[14] & 0x0F)));
		// System.out.println(ipHeaderLength);
		int dataOffset = ((int) (4 * (packet[46] & 0xFF) >> 4));
		// System.out.println("DO"+dataOffset);
		int actualDataLength = Integer.parseInt(totalLength.toString()) - ipHeaderLength - dataOffset;
		// System.out.println("Actual"+actualDataLength);
		String seqNum = covertToDecimal(new byte[] { packet[38], packet[39], packet[40], packet[41] });
		// System.out.println(seqNum);
		String data = "";
		try {
			data = new String(Arrays.copyOfRange(packet, packet.length - actualDataLength, packet.length),
					"ISO-8859-1");
		} catch (UnsupportedEncodingException e) {

			e.printStackTrace();
		}
		PacketTCP packetTcp = new PacketTCP();
		// String seqNum =covertToDecimal(new byte[] { packet[38],
		// packet[39],packet[40], packet[41]});
		String ackNum = covertToDecimal(new byte[] { packet[42], packet[43], packet[44], packet[45] });
		packetTcp.ackNum = (Integer.parseInt(ackNum));
		// packetTcp.timeStamp = packetTimeStamp;
		packetTcp.setTimeStamp(packetTimeStamp);
		packetTcp.data = data;

		if (sourcePort.toString().equals("80")) {
			if (!downLinkMap.containsKey(tcpTuple.toString())) {
				PacketInformation packetInfo = new PacketInformation();
				packetInfo.dataLength = packetInfo.getDataLength() + actualDataLength;
				packetInfo.totalPacketdata.put((Integer.parseInt(seqNum)), packetTcp);
				downLinkMap.put(tcpTuple.toString(), packetInfo);
			} else {
				PacketInformation packetInfo = downLinkMap.get(tcpTuple.toString());
				packetInfo.dataLength = packetInfo.getDataLength() + actualDataLength;
				packetInfo.totalPacketdata.put((Integer.parseInt(seqNum)), packetTcp);
				downLinkMap.put(tcpTuple.toString(), packetInfo);
			}

		} else if (destPort.toString().equals("80")) {
			if (!uplinkMap.containsKey(tcpTuple.toString())) {
				PacketInformation packetInfo = new PacketInformation();
				packetInfo.dataLength = packetInfo.getDataLength() + actualDataLength;
				packetInfo.totalPacketdata.put((Integer.parseInt(seqNum)), packetTcp);
				uplinkMap.put(tcpTuple.toString(), packetInfo);
			} else {
				PacketInformation packetInfo = uplinkMap.get(tcpTuple.toString());
				packetInfo.dataLength = packetInfo.getDataLength() + actualDataLength;
				packetInfo.totalPacketdata.put((Integer.parseInt(seqNum)), packetTcp);
				uplinkMap.put(tcpTuple.toString(), packetInfo);
			}

		}

	}

	private static String covertToDecimal(byte[] temp) {
		int dec = 0;
		StringBuilder str = new StringBuilder();

		for (int i = 0; i < temp.length; i++) {
			dec <<= 8;
			dec |= (int) temp[i] & 0xFF;
		}
		return str.append(dec).toString();

	}

}