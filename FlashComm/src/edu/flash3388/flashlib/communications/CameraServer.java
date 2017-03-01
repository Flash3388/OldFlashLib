package edu.flash3388.flashlib.communications;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

import edu.flash3388.flashlib.cams.Camera;
import edu.flash3388.flashlib.util.FlashUtil;

public class CameraServer {
	private static class Task implements Runnable{
		CameraServer server;
		
		public Task(CameraServer server){
			this.server = server;
		}
		
		@Override
		public void run() {
			try {
				byte bytes[] = new byte[1];
				DatagramPacket packet = new DatagramPacket(bytes, bytes.length);
				server.socket.receive(packet);
				server.sendAddress = packet.getAddress();
				server.sendPort = packet.getPort();
				
				byte[] checkBytes = FlashUtil.toByteArray(1);
				long period = (long) (1000 / (1.0 * 30)), lastCheck = System.currentTimeMillis();
				while(!server.stop){
					long t0 = System.currentTimeMillis();
					
					if(server.camera == null) continue;
					byte[] imageArray = server.camera.getData();
					if(imageArray == null) continue;
			        
			        server.socket.send(new DatagramPacket(imageArray, imageArray.length, server.sendAddress, server.sendPort));
			        
			        long dt = System.currentTimeMillis() - t0;

		            if (dt < period)
		            	FlashUtil.delay(dt);
		            
		            if(System.currentTimeMillis() - lastCheck > 2000){
		            	server.socket.send(new DatagramPacket(checkBytes, 4, server.sendAddress, server.sendPort));
				       
				        packet = new DatagramPacket(bytes, bytes.length);
						server.socket.receive(packet);
						server.sendAddress = packet.getAddress();
						server.sendPort = packet.getPort();
				        
		            	lastCheck = System.currentTimeMillis();
		            }
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private Thread runThread;
	private DatagramSocket socket;
	
	private InetAddress sendAddress;
	private int sendPort;
	private int port;
	private String name;
	
	private Camera camera;
	private boolean stop = false;
	
	public CameraServer(String name, int localPort, Camera camera){
		port = localPort;
		this.name = name;
		try {
			socket = new DatagramSocket(new InetSocketAddress(localPort));
		} catch (SocketException e) {
		}
		
		this.camera = camera;
		runThread = new Thread(new Task(this));
		runThread.start();
	}
	public CameraServer(int localPort, Camera camera){
		this("CamServer", localPort, camera);
	}
	
	public String getName(){
		return name;
	}
	public int getLocalPort(){
		return port;
	}
	public int getRemotePort(){
		return sendPort;
	}
	public InetAddress getRemoteAddress(){
		return sendAddress;
	}
	public Camera getCamera(){
		return camera;
	}
	public void stop(){
		stop = true;
		socket.close();
	}
}
