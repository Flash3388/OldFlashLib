package edu.flash3388.flashlib.robot.flashboard;

import java.io.IOException;
import java.net.SocketException;

import edu.flash3388.flashlib.cams.Camera;
import edu.flash3388.flashlib.cams.CameraView;
import edu.flash3388.flashlib.communications.CameraServer;
import edu.flash3388.flashlib.communications.CommInfo;
import edu.flash3388.flashlib.communications.Communications;
import edu.flash3388.flashlib.communications.Sendable;
import edu.flash3388.flashlib.communications.UDPReadInterface;
import edu.flash3388.flashlib.util.FlashUtil;
import edu.flash3388.flashlib.vision.RemoteVision;

public class Flashboard {
	
	private static boolean instance = false;
	private static CameraView camViewer;
	private static CameraServer camServer;
	private static RemoteVision vision;
	private static Communications communications;
	
	public static void attach(Sendable sendable){
		if(!instance) return;
		communications.attach(sendable);
	}
	public static void attach(Sendable... sendables){
		if(!instance) return;
		for (Sendable sendable : sendables) 
			communications.attach(sendable);
	}
	public static boolean detach(Sendable sendable){
		if(!instance) return false;
		return communications.detach(sendable);
	}
	public static boolean detach(int index){
		if(!instance) return false;
		return communications.detach(index);
	}
	public static boolean detachByID(int id){
		if(!instance) return false;
		return communications.detachByID(id);
	}
	public static Sendable getByID(int id){
		if(!instance) return null;
		return communications.getByID(id);
	}
	
	public static boolean connect() throws IOException{
		if(!instance) return false;
		return communications.connect();
	}
	public static void disconnect(){
		if(!instance) return;
		communications.disconnect();
	}
	public static boolean isConnected(){
		if(!instance) return false;
		return communications.isConnected();
	}
	public static void start(){
		if(!instance) return;
		communications.start();
	}
	
	public static CameraView getCameraView(){
		return camViewer;
	}
	public static RemoteVision getVision(){
		return vision;
	}
	public static CameraServer getCameraServer(){
		return camServer;
	}
	
	public static void init(CommInfo info){
		if(!instance || info == null){
			try {
				communications = new Communications("Flashboard", new UDPReadInterface(info.localPort), true);
				vision = new RemoteVision();
				camViewer = new CameraView("Flashboard-CamViewer", null, new Camera[]{});
				camServer = new CameraServer("Flashboard", info.camPort, camViewer);
				communications.attach(vision);
				
				instance = true;
				FlashUtil.getLog().logTime("FLASHBoard: Initialized at port " + info.localPort);
			} catch (SocketException e) {
				FlashUtil.getLog().reportError(e.getMessage());
			}
		}
	}
	public static boolean flashboardInit(){
		return instance;
	}
}
