package edu.flash3388.flashlib.communications;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;

public class TCPReadInterface implements ReadInterface{

	private static final byte SEPERATOR_START = (byte)'<';
	private static final byte SEPERATOR_END = (byte)'>';
	
	private ServerSocket serverSocket;
	private Socket socket;
	private int portOut;
	private InetAddress outInet;
	private boolean closed = false, server;
	private int maxBufferSize = 100, lastIndex = 0;
	private byte[] data = new byte[maxBufferSize], leftoverData = new byte[maxBufferSize];
	private OutputStream out;
	private InputStream in;
	
	public TCPReadInterface(CommInfo info) throws IOException{
		outInet = InetAddress.getByName(info.hostname);
		portOut = info.remotePort;
		socket = new Socket();
		socket.bind(new InetSocketAddress(InetAddress.getLocalHost(), info.localPort));
		server = false;
	}
	public TCPReadInterface(InetAddress remote, int localport, int remoteport) throws UnknownHostException, IOException{
		outInet = remote;
		portOut = remoteport;
		socket = new Socket();
		socket.bind(new InetSocketAddress(InetAddress.getLocalHost(), localport));
		server = false;
	}
	public TCPReadInterface(int localPort) throws IOException{
		this(InetAddress.getLocalHost(), localPort);
	}
	public TCPReadInterface(InetAddress localAddr, int localPort) throws IOException{
		serverSocket = new ServerSocket(localPort, 20, localAddr);
		server = true;
	}
	
	@Override
	public void open() {
	}
	@Override
	public void close(){
		try {
			if(server) 
				serverSocket.close();
			if(socket != null)
				socket.close();
		} catch (IOException e) {
		}
		closed = true;
	}
	@Override
	public boolean connect(){
		try {
			if(server)
				socket = serverSocket.accept();
			else 
				socket.connect(new InetSocketAddress(outInet, portOut), 200);
			
			out = socket.getOutputStream();
			in = socket.getInputStream();
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	@Override
	public boolean read(Packet packet) {
		if(!isOpened()) return false;
		try {
			int len = in.read(data);
			int start = findStartSeperator(data, 0, len);
			int end = findSeperatorEnd(data, 0, len);
			if(start > end || start == -1){
				byte[] bytes = new byte[leftoverData.length - lastIndex + end];
				System.arraycopy(leftoverData, lastIndex, bytes, 0, leftoverData.length - lastIndex);
				System.arraycopy(data, 0, bytes, leftoverData.length - lastIndex, end);
				packet.data = bytes;
				packet.length = bytes.length;
				System.arraycopy(data, end+1, leftoverData, 0, data.length - end + 1);
				lastIndex = end+1;
			}else if(end > start && start == 0){
				byte[] bytes = Arrays.copyOfRange(data, start+1, end);
				packet.data = bytes;
				packet.length = bytes.length;
				if(end < data.length){
					lastIndex = end+1;
					System.arraycopy(data, end+1, leftoverData, 0, data.length - end + 1);
				}else lastIndex = leftoverData.length-1;
			}
			return true;
		} catch (IOException e) {
			packet.length = 0;
			return false;
		}
	}
	@Override
	public void setReadTimeout(long millis) {
		try {
			if(socket != null)
				socket.setSoTimeout((int)millis);
		} catch (IOException e) {}
	}
	@Override
	public long getTimeout() {
		try {
			return socket != null? socket.getSoTimeout() : 0;
		} catch (IOException e) {}
		return -1;
	}
	@Override
	public void write(byte[] data) {
		write(data, 0, data.length);
	}
	@Override
	public void write(byte[] data, int start, int length) {
		if(!isOpened()) return;
		try {
			byte[] sendData = new byte[length + 2];
			sendData[0] = SEPERATOR_START;
			sendData[sendData.length-1] = SEPERATOR_END;
			System.arraycopy(data, start, sendData, 1, length);
			out.write(sendData);
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
	
	private int findStartSeperator(byte[] data, int start, int length){
		for (int i = start; i < length; i++) 
			if(data[i] == SEPERATOR_START) return i;
		return -1;
	}
	private int findSeperatorEnd(byte[] data, int start, int length){
		for (int i = start; i < length; i++) 
			if(data[i] == SEPERATOR_END) return i;
		return -1;
	}
}
