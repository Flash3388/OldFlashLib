package edu.flash3388.flashlib.communications;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Vector;

import edu.flash3388.flashlib.util.FlashUtil;
import edu.flash3388.flashlib.util.Log;

public class Communications {
	private static class CommTask implements Runnable{
		boolean stop = false;
		private Communications comm;
		private long timeLastTimeout = -1;
		private int maxTimeouts = 3;
		
		public CommTask(Communications comm){
			this.comm = comm;
		}
		
		@Override
		public void run() {
			try{
				while(!stop){
					Log.logTime(comm.logName+": Searching for remote connection");
					while(!comm.connect() && !stop);
					if(stop) break;
					
					Log.logTime(comm.logName+": Connected");
					comm.resetAll();
					comm.updateClock();
					comm.lastRead = comm.readClock();
					int timeouts = 0;
					while(comm.isConnected()){
						comm.writeHandshake();
						
						comm.sendAll();
						comm.read();
						
						comm.updateClock();
						
						if(comm.connectionTimedout()){
							timeouts++;
							Log.logTime(comm.logName+": TIMEOUT " + timeouts);
							comm.lastRead = comm.readClock();
							timeLastTimeout = comm.readClock();
						}
						if(timeouts >= maxTimeouts){
							Log.logTime(comm.logName+": Connection lost");
							comm.connected = false;
							break;
						}
						if(timeLastTimeout != -1 && 
								comm.readClock() - timeLastTimeout > (comm.connectionTimeout*3)){
							timeouts = 0;
							timeLastTimeout = -1;
							Log.logTime(comm.logName+": Timeout Reset");
						}
						comm.writeHandshake();
						FlashUtil.delay(1);
					}
					comm.onDisconnect();
					Log.logTime(comm.logName+": Disconnected");
				}
			}catch(IOException e){
				Log.reportError(e.getMessage());
				comm.disconnect();
			}
		}
		public void stop(){
			stop = true;
		}
	}
	
	public static final int CONNECTION_TIMEOUT = 1000;
	public static final int READ_TIMEOUT = 20;
	public static final int MAX_REC_LENGTH = 100;
	public static final byte[] HANDSHAKE = {0x01, 0x00, 0x01};
	private static int instances = 0;
	
	private Vector<Sendable> sendables;
	
	private boolean connected = false, server;
	private long lastRead = -1, currentMillis = -1, readStart = -1;
	private int connectionTimeout;
	private int readTimeout;
	private Packet packet = new Packet();
	private ReadInterface readInterface;
	private SendableCreator sendableCreator;
	private String name, logName;
	
	private Thread commThread;
	private CommTask commTask;
	
	public Communications(String name, ReadInterface readIn, boolean server){
		instances++;
		this.name = name;
		this.server = server;
		this.readInterface = readIn;
		this.logName = name+"-Comm";
		
		setReadTimeout(READ_TIMEOUT);
		setConnectionTimeout(CONNECTION_TIMEOUT);
		
		initializeConcurrency();
		Log.logTime(logName+": Initialized");
		
		sendables = new Vector<Sendable>();
		setBufferSize(MAX_REC_LENGTH);
		readInterface.open();
	}
	public Communications(ReadInterface readIn, boolean server){
		this(""+instances, readIn, server);
	}
	
	private void initializeConcurrency(){
		commTask = new CommTask(this);
		commThread = new Thread(commTask, name+"-Communications");
	}
	private Packet receivePacket(){
		if(!readInterface.read(packet))
			return null;
		return packet;
	}
	private void read(){//ID|VALUE
		if(!connected) return;
		updateClock();
		readStart = currentMillis;
		while(!readTimedout()){
			Packet packet = receivePacket();
			if(packet == null || packet.length < 1)
				return;
			lastRead = FlashUtil.millis();
			if(isHandshake(packet.data, packet.length))
				continue;
			if(packet.length < 5)
				continue;
			
			int id = FlashUtil.toInt(packet.data);
			Sendable sen = getByID(id);
			if(sen != null)
				sen.newData(Arrays.copyOfRange(packet.data, 5, packet.length));
			else if(sendableCreator != null){
				String str = new String(packet.data, 5, packet.length - 5);
				sen = sendableCreator.create(str, id, packet.data[4]);
				if(sen != null){
					sendables.add(sen);
					sen.setAttached(true);
				}
			}
			updateClock();
		}
	}
	private void resetAll(){
		Enumeration<Sendable> sendablesEnum = sendables.elements();
		while(sendablesEnum.hasMoreElements())
			resetSendable(sendablesEnum.nextElement());
	} 
	private void resetSendable(Sendable sen){
		sen.setRemoteInit(false);
		SendableData data;
		if((data = sen.dataForTransmition()) != null)
			data.onConnection();
	}
	private void onDisconnect(){
		Enumeration<Sendable> sendablesEnum = sendables.elements();
		while(sendablesEnum.hasMoreElements())
			handleDisconnection(sendablesEnum.nextElement());
	} 
	private void handleDisconnection(Sendable sen){
		SendableData data;
		if((data = sen.dataForTransmition()) != null)
			data.onConnectionLost();
	}
	private void sendAll(){
		Enumeration<Sendable> sendablesEnum = sendables.elements();
		while(sendablesEnum.hasMoreElements())
			sendFromSendable(sendablesEnum.nextElement());
	}
	private void sendFromSendable(Sendable sen){
		if(!sen.remoteInit()){
			byte[] bytes = sen.getName().getBytes();
			send(bytes, sen);
			sen.setRemoteInit(true);
			return;
		}
		
		SendableData data = sen.dataForTransmition();
		byte[] dataB;
		
		if(data == null || !data.hasChanged() || (dataB = data.get()) == null) 
			return;
		send(dataB, sen);
	}
	private void updateClock(){
		currentMillis = FlashUtil.millis();
	}
	private boolean readTimedout(){
		if(currentMillis == -1)
			currentMillis = FlashUtil.millis();
		return readStart != -1 && currentMillis - readStart > readTimeout;
	}
	private boolean connectionTimedout(){
		if(currentMillis == -1)
			currentMillis = FlashUtil.millis();
		return lastRead != -1 && currentMillis - lastRead > connectionTimeout;
	}
	private long readClock(){
		return currentMillis;
	}
	private void writeHandshake(){
		write(HANDSHAKE);
	}
	private void write(byte[] bytes){
		if(!connected) return;
		readInterface.write(bytes);
	}
	
	private void send(byte[] data, Sendable sendable){
		byte[] bytes = new byte[data.length + 5];
		FlashUtil.fillByteArray(sendable.getID(), bytes);
		bytes[4] = sendable.getType().value;
		System.arraycopy(data, 0, bytes, 5, data.length);
		write(bytes);
	}
	
	public void attach(Sendable... sendables){
		for (Sendable sendable : sendables) 
			attach(sendable);
	}
	public void attach(Sendable sendable){
		if(getByID(sendable.getID()) == null){
			sendables.add(sendable);
			sendable.setAttached(true);
			if(isConnected())
				resetSendable(sendable);
		}
	}
	public boolean detach(Sendable sendable){
		if(sendables.remove(sendable)){
			sendable.setAttached(false);
			if(isConnected())
				handleDisconnection(sendable);
		}
		return !sendable.attached();
	}
	public boolean detach(int index){
		Sendable sen = sendables.get(index);
		if(sen != null) {
			sendables.remove(index);
			sen.setAttached(false);
			if(isConnected())
				handleDisconnection(sen);
		}
		return sen != null && !sen.attached();
	}
	public boolean detachByID(int id){
		Sendable sen = getByID(id);
		if(sen != null) {
			sendables.remove(sen);
			sen.setAttached(false);
			if(isConnected())
				handleDisconnection(sen);
		}
		return sen != null && !sen.attached();
	}
	public void detachAll(){
		Enumeration<Sendable> sendablesEnum = sendables.elements();
		while (sendablesEnum.hasMoreElements())
			detach(sendablesEnum.nextElement());
	}
	public Sendable getByID(int id){
		Enumeration<Sendable> sendablesEnum = sendables.elements();
		while(sendablesEnum.hasMoreElements()){
			Sendable sen = sendablesEnum.nextElement();
			if(sen.getID() == id)
				return sen;
		}
		return null;
	}
	public String getName(){
		return name;
	}
	public ReadInterface getReadInterface(){
		return readInterface;
	}
	public boolean connect() throws IOException{
		if(connected) return true;
		connected = server? handshakeServer(readInterface, packet) : handshakeClient(readInterface, packet);
		return connected;
	}
	public void disconnect(){
		if(connected){
			commTask.stop();
			connected = false;
		}
	}
	public boolean isConnected(){
		return connected;
	}
	public void setMaxTimeoutsCount(int timeouts){
		commTask.maxTimeouts = timeouts;
	}
	public int getMaxTimeoutsCount(){
		return commTask.maxTimeouts;
	}
	public void setConnectionTimeout(int timeout){
		connectionTimeout = timeout;
	}
	public int getConnectionTimeout(){
		return connectionTimeout;
	}
	public void setReadTimeout(int timeout){
		readTimeout = timeout;
		readInterface.setReadTimeout(readTimeout);
	}
	public int getReadTimeout(){
		return readTimeout;
	}
	public void setSendableCreator(SendableCreator creator){
		sendableCreator = creator;
	}
	public void setBufferSize(int size){
		readInterface.setMaxBufferSize(size);
	}
	public SendableCreator getSendableCreator(){
		return sendableCreator;
	}
	public void start(){
		if(!commThread.isAlive())
			commThread.start();
	}
	public void close() {
		disconnect();
		readInterface.close();
		detachAll();
	}
	public void sendDataForSendable(Sendable sendable, byte[] data){
		if(getByID(sendable.getID()) == null)
			return;
		send(data, sendable);
	}
	
	private static boolean handshakeServer(ReadInterface readInterface, Packet packet){
		readInterface.setReadTimeout(READ_TIMEOUT * 4);
		readInterface.read(packet);
		if(!isHandshake(packet.data, packet.length))
			return false;
		
		readInterface.write(HANDSHAKE);
		readInterface.read(packet);
		if(!isHandshake(packet.data, packet.length))
			return false;
		return true;
	}
	private static boolean handshakeClient(ReadInterface readInterface, Packet packet){
		readInterface.setReadTimeout(READ_TIMEOUT);
		readInterface.write(HANDSHAKE);
		
		readInterface.read(packet);
		if(!isHandshake(packet.data, packet.length))
			return false;
		
		readInterface.write(HANDSHAKE);
		return true;
	}
	public static boolean isHandshake(byte[] bytes, int length){
		if(length != HANDSHAKE.length) return false;
		for(int i = 0; i < length; i++){
			if(bytes[i] != HANDSHAKE[i])
				return false;
		}
		return true;
	}
}
