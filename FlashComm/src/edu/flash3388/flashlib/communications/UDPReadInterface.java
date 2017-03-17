package edu.flash3388.flashlib.communications;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

import edu.flash3388.flashlib.util.FlashUtil;

public class UDPReadInterface implements ReadInterface{

	private DatagramSocket socket;
	private int portOut;
	private InetAddress outInet;
	private boolean closed = false;
	private int maxBufferSize = 50;
	private byte[] data = new byte[maxBufferSize];
	private boolean server = false;
	private long bytesRead = 0, readStart = -1;
	
	long counter = 0;
	long timestamp = -1;
	static final long INTERVAL = 1000;
	long limit = -1;
	
	public UDPReadInterface(CommInfo info) throws SocketException, UnknownHostException{
		outInet = InetAddress.getByName(info.hostname);
		socket = new DatagramSocket(info.localPort);
		portOut = info.remotePort;
		server = false;
	}
	public UDPReadInterface(InetAddress remote, int localport, int remoteport) throws SocketException{
		outInet = remote;
		socket = new DatagramSocket(localport);
		portOut = remoteport;
		server = false;
	}
	public UDPReadInterface(int localPort) throws SocketException{
		this(null, localPort);
	}
	public UDPReadInterface(InetAddress localAddr, int localPort) throws SocketException{
		socket = new DatagramSocket(localPort, localAddr);
		server = true;
	}
	
	@Override
	public void open() {
	}
	@Override
	public void close() {
		socket.close();
		closed = true;
	}
	@Override
	public boolean connect() {
		return false;
	}
	@Override
	public boolean read(Packet packet) {
		if(!isOpened()) return false;
		if(readStart < 0){
			readStart = FlashUtil.millis();
			timestamp = readStart;
		}
		if(limit != -1 && counter > limit){
			long now = FlashUtil.millis();
			if(timestamp + INTERVAL >= now)
				FlashUtil.delay(timestamp + INTERVAL - now);
			timestamp = now;
	        counter = 0;
		}
		try {
			DatagramPacket recp = new DatagramPacket(data, maxBufferSize);
			socket.receive(recp);
			
			if(server){
				outInet = recp.getAddress();
				portOut = recp.getPort();
			}
			
			packet.senderAddress = outInet;
			packet.senderPort = portOut;
			packet.data = recp.getData();
			packet.length = recp.getLength();
			bytesRead += packet.length;
			counter += packet.length;
			return true;
		} catch (IOException e) {
			packet.length = 0;
			return false;
		}
	}
	@Override
	public void setReadTimeout(long millis) {
		try {
			socket.setSoTimeout((int)millis);
		} catch (SocketException e) {}
	}
	@Override
	public long getTimeout() {
		try {
			return socket.getSoTimeout();
		} catch (SocketException e) {}
		return -1;
	}
	@Override
	public void write(byte[] data) {
		write(data, outInet, portOut);
	}
	@Override
	public void write(byte[] data, int start, int length) {
		if(!isOpened()) return;
		write(Arrays.copyOfRange(data, start, length + start));
	}
	public void write(byte[] data, InetAddress outInet, int portOut){
		if(!isOpened()) return;
		if(readStart < 0){
			readStart = FlashUtil.millis();
			timestamp = readStart;
		}
		if(limit != -1 && counter > limit){
			long now = FlashUtil.millis();
			if(timestamp + INTERVAL >= now)
				FlashUtil.delay(timestamp + INTERVAL - now);
			timestamp = now;
	        counter = 0;
		}
		try {
			socket.send(new DatagramPacket(data, data.length, outInet, portOut));
			bytesRead += data.length;
			counter += data.length;
		} catch (IOException e) {}
	}

	@Override
	public boolean isOpened() {
		return !closed;
	}
	@Override
	public void setMaxBufferSize(int bytes) {
		maxBufferSize = bytes;
		data = new byte[maxBufferSize];
	}
	@Override
	public int getMaxBufferSize() {
		return maxBufferSize;
	}
	
	public boolean boundAsServer(){
		return server;
	}
	public int getLocalPort(){
		return socket.getLocalPort();
	}
	public int getRemotePort(){
		return portOut;
	}
	public InetAddress getRemoteAddress(){
		return outInet;
	}
	public double getBandwithUsage(){
		if(readStart < 0) return 0;
		double secs = (FlashUtil.millis() - readStart) / 1000;
		double mbytes = bytesRead * 8 / 1e6;
		readStart = -1;
		bytesRead = 0;
		return (mbytes / secs);
	}
	public long getBytesPassed(){
		return bytesRead;
	}
	public long getMillisSinceReset(){
		return FlashUtil.millis() - readStart;
	}
	public void setBandwidthLimit(double mbps){
		limit = (long) (mbps * 1e6 / 8);
	}
	public void disableBandwidthLimit(){
		limit = -1;
	}
}
