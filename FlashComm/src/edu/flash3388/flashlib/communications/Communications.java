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
		
		public CommTask(Communications comm){
			this.comm = comm;
		}
		
		@Override
		public void run() {
			try{
				while(!stop){
					Log.logTime(comm.logName+": Searching for remote dashboard");
					while(!comm.connect() && !stop);
					if(stop) break;
					
					comm.readInterface.setReadTimeout(READ_TIMEOUT);
					Log.logTime(comm.logName+": Connected");
					comm.resetAll();
					comm.lastRead = FlashUtil.millis();
					int timeouts = 0;
					while(comm.connected){
						comm.writeHandshake();
						
						comm.sendAll();
						comm.read();
						
						if(comm.lastRead != -1 && FlashUtil.millis() - comm.lastRead > CONNECTION_TIMEOUT){
							timeouts++;
							Log.logTime(comm.logName+": TIMEOUT " + timeouts);
							comm.lastRead = FlashUtil.millis();
						}
						if(timeouts >= 3){
							Log.logTime(comm.logName+": Connection lost");
							comm.connected = false;
							break;
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
	
	public static final long CONNECTION_TIMEOUT = 1000;
	public static final int READ_TIMEOUT = 20;
	public static final int MAX_REC_LENGTH = 100;
	public static final byte[] HANDSHAKE = {0x01, 0x00, 0x01};
	private static int instances = 0;
	
	private Vector<Sendable> sendables;
	
	private boolean connected = false, server;
	private long lastRead = -1;
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
		
		initializeConcurrency();
		Log.logTime(logName+": Initialized");
		
		sendables = new Vector<Sendable>();
		readInterface.setMaxBufferSize(MAX_REC_LENGTH);
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
		long start = FlashUtil.millis();
		while(FlashUtil.millis() - start < READ_TIMEOUT){
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
		}
	}
	private void resetAll(){
		Enumeration<Sendable> sendablesEnum = sendables.elements();
		while(sendablesEnum.hasMoreElements()){
			Sendable sen = sendablesEnum.nextElement();
			sen.setRemoteInit(false);
			SendableData data;
			if((data = sen.dataForTransmition()) != null)
				data.onConnection();
		}
	} 
	private void onDisconnect(){
		Enumeration<Sendable> sendablesEnum = sendables.elements();
		while(sendablesEnum.hasMoreElements()){
			Sendable sen = sendablesEnum.nextElement();
			SendableData data;
			if((data = sen.dataForTransmition()) != null)
				data.onConnectionLost();
		}
	} 
	private void sendAll(){
		Enumeration<Sendable> sendablesEnum = sendables.elements();
		while(sendablesEnum.hasMoreElements()){
			Sendable sen = sendablesEnum.nextElement();
			
			if(!sen.remoteInit()){
				byte[] bytes = sen.getName().getBytes();
				send(bytes, sen);
				sen.setRemoteInit(true);
				continue;
			}
			
			SendableData data = sen.dataForTransmition();
			byte[] dataB;
			
			if(data == null || !data.hasChanged() || (dataB = data.get()) == null) continue;
			send(dataB, sen);
		}
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
		}
	}
	public boolean detach(Sendable sendable){
		if(sendables.remove(sendable))
			sendable.setAttached(false);
		return !sendable.attached();
	}
	public boolean detach(int index){
		Sendable sen = sendables.get(index);
		if(sen != null) {
			sendables.remove(index);
			sen.setAttached(false);
		}
		return sen != null && !sen.attached();
	}
	public boolean detachByID(int id){
		Sendable sen = getByID(id);
		if(sen != null) {
			sendables.remove(sen);
			sen.setAttached(false);
		}
		return sen != null && !sen.attached();
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
	public void close(){
		disconnect();
		readInterface.close();
	}
	public boolean isConnected(){
		return connected;
	}
	public void setSendableCreator(SendableCreator creator){
		sendableCreator = creator;
	}
	public SendableCreator getSendableCreator(){
		return sendableCreator;
	}
	public void start(){
		if(!commThread.isAlive())
			commThread.start();
	}
	public void disposeComm() {
		disconnect();
		readInterface.close();
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
