package nschat.routing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import nschat.exception.PacketFormatException;
import nschat.multicasting.SendingBuffer;
import nschat.tcp.Packet;
import nschat.tcp.SequenceNumberSet;
import nschat.tcp.Packet.PacketType;

/**
 * Distance vector routing protocol
 * @author Pieter Jan
 *
 */
public class BasicRoutingProtocol {

	private SequenceNumberSet set;
	private SendingBuffer sendingBuffer;
	private ForwardingTable forwardingTable;
	
	
	public BasicRoutingProtocol() {
		set = new SequenceNumberSet();
		sendingBuffer = new SendingBuffer();
		forwardingTable = new ForwardingTable();
	}

	//TODO Finish function
	public void receivePacket(Packet packet) {
		int rtt = getRTT(packet);
		byte[] bytes = packet.getData();
		List<BasicRoute> routes = getForwardingTable(bytes);
		
	
	}
	//TODO Finish function
	public void makeRoute(int dest, int rtt, int nextHop) {
		BasicRoute route = new BasicRoute(dest, rtt, nextHop);
	}
	
	public int getRTT(Packet packet) {
		byte[] old = sendingBuffer.get(set, packet.getAckNumber());
		Packet oldPacket;
		try {
			oldPacket = new Packet(old);
		} catch (PacketFormatException e) { 
			return -1;
		}
		long sendTime = oldPacket.getTimestamp();
		int rtt = (int) (System.currentTimeMillis() - sendTime);
		return rtt;
	}
	
	public void sendPacket() {
		short seq = set.get();
		
		Packet packet = new Packet(PacketType.ROUTING, (byte) 0, seq, (short) 0, null);
		Collection<BasicRoute> routes = forwardingTable.getRoutes();
		byte[] data = new byte[routes.size() * 12];
		
		int i = 0;
		for (BasicRoute route : routes) {
			System.arraycopy(route.getBytes(), 0, data, i * 12, 12);
			++i;
		}
		packet.setData(data);
		
		sendingBuffer.add(set, seq, packet.pack());
	}
	
	/*
	 * Returns a list of the received forwarding tables.
	 */
	private List<BasicRoute> getForwardingTable(byte[] bytes) {
		List<BasicRoute> basicRoutes = new ArrayList<BasicRoute>();
		
		for (byte[] chunk : getBytesChunk(bytes, 12)) {
			int dest = (chunk[0] << 24) + (chunk[1] << 16) + (chunk[2] << 8) + chunk[3];
			int cost = (chunk[4] << 24) + (chunk[5] << 16) + (chunk[6] << 8) + chunk[7];
			int hop = (chunk[8] << 24) + (chunk[9] << 16) + (chunk[10] << 8) + chunk[11];
			basicRoutes.add(new BasicRoute(dest, cost, hop));
		}
		return basicRoutes;
	}
	
	/*
	 * Divide the given byte array into chunks of the given chunk-size.
	 */
	private List<byte[]> getBytesChunk(byte[] bytes, int chunksize) {
		List<byte[]> bytesList = new ArrayList<byte[]>();
		
		int begin = 0;
		while (begin < bytes.length) {
			int end = Math.min(chunksize, begin + chunksize);
			bytesList.add(Arrays.copyOfRange(bytes, begin, end));
			begin += chunksize;
		}
		return bytesList;
	}
	

}
