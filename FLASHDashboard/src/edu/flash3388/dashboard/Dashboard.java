package edu.flash3388.dashboard;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Vector;

import org.opencv.core.Core;
import org.opencv.core.Mat;

import edu.flash3388.dashboard.controls.CameraViewer;
import edu.flash3388.flashlib.communications.CameraClient;
import edu.flash3388.flashlib.communications.CommInfo;
import edu.flash3388.flashlib.communications.Communications;
import edu.flash3388.flashlib.communications.UDPReadInterface;
import edu.flash3388.flashlib.util.FlashUtil;
import edu.flash3388.flashlib.util.Properties;
import edu.flash3388.flashlib.vision.CvProcessing;
import edu.flash3388.flashlib.vision.CvRunner;
import edu.flash3388.flashlib.vision.ProcessingParam;
import edu.flash3388.gui.FlashFxUtils;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Dashboard extends Application {

	private static class UpdateTask implements Runnable{
		Vector<UpdateWrapper> updatables = new Vector<UpdateWrapper>();
		boolean stop = false;
		@Override
		public void run() {
			while (!stop) {
				for(Enumeration<UpdateWrapper> upenum = updatables.elements(); upenum.hasMoreElements();){
					UpdateWrapper up = upenum.nextElement();
					up.runnable.run();
					if(up.removeWhenFinished)
						updatables.remove(up);
				}
				FlashUtil.delay(3);
			}
		}
	}
	private static class UpdateWrapper{
		Runnable runnable;
		boolean removeWhenFinished;
	}
	
	
	public static final String EXT_VISION_PARAM = "imgproc";
	public static final String PROP_VISION_DEFAULT_PARAM = "vision.default";
	
	public static final String FOLDER_SAVES = "data/saves/";
	public static final String FOLDER_RESOURCE = "data/res/";
	public static final String FOLDER_DATA = "data/";
	
	private static final String SETTINGS_FILE = FOLDER_DATA+"dash.ini";
	private static final String REMOTE_HOSTS_FILE = FOLDER_DATA+"hosts.ini";
	
	private static Vector<Displayble> displayables = new Vector<Displayble>();
	private static Properties properties = new Properties();
	private static Stage primaryStage;
	private static Dashboard instance;
	private static UpdateTask updateTask;
	private static Thread updateThread;
	private static UDPReadInterface readInterface;
	private static Communications communications;
	private static CameraClient camClient;
	private static CameraViewer camViewer;
	private static CvRunner vision;
	
	private static BufferedImage[] visionImageNext = new BufferedImage[2];
	private static int visionImageIndex = 0;
	private static boolean visionParamLoadFailed = false;
	
	public static void main(String[] args) throws Exception{
		FlashUtil.setStart();
		Remote.initializeJSCH();
		FlashUtil.getLog().log("Loading opencv natives: "+Core.NATIVE_LIBRARY_NAME+" ...", "Dashboard");
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		FlashUtil.getLog().log("Done", "Dashboard");
		FlashUtil.getLog().log("Loading settings and properties...", "Dashboard");
		loadSettings();
		validateBasicSettings();
		FlashUtil.getLog().log("Done", "Dashboard");
		
		FlashUtil.getLog().log("Creating shutdown hook...", "Dashboard");
		Runtime.getRuntime().addShutdownHook(new Thread(()->close()));
		FlashUtil.getLog().log("Done", "Dashboard");
		
		FlashUtil.getLog().saveLog();
		boolean TESTING = false;
		if (!TESTING) {
		   initStart();
		   FlashUtil.getLog().log("Launching FX...", "Dashboard");
		   launch();
		}
		else test();
	}
	private static void test(){
		
	}
	private static void initStart(){
		updateTask = new UpdateTask();
		updateThread = new Thread(updateTask);
	    updateThread.start();
	    camViewer = new CameraViewer("RoboRIO-CamViewer", -1);
	    addDisplayable(camViewer);
		addRunnableForUpdate(()->{
			   if(readInterface == null){
				   try {
					InetAddress ad = InetAddress.getByName(CommInfo.ROBORIO_HOST);
					FlashUtil.getLog().log("Found host: "+CommInfo.ROBORIO_HOST, "Dashboard");
					readInterface = new UDPReadInterface(ad, CommInfo.FLASHBOARD_PORT_BOARD, CommInfo.FLASHBOARD_PORT_ROBORIO);
					communications = new Communications("RoboRIO", readInterface, false);
					communications.setSendableCreator(new FlashboardSendableCreator());
					communications.start();
					camClient = new CameraClient("RoboRIO CAMServer", CommInfo.FLASHBOARD_CAMERA_PORT_BOARD, ad, CommInfo.FLASHBOARD_CAMERA_PORT_ROBORIO, CameraClient.DEFUALT_MAX_BYTES);
					camClient.addListener(camViewer);
				   } catch (UnknownHostException | SocketException e) {}
			   }
		 });
		addRunnableForUpdate(()->{
			BufferedImage img = visionImageNext[visionImageIndex];
			visionImageNext[visionImageIndex] = null;
			visionImageIndex ^= 1;
			if(vision != null && img != null){
				Mat m = FlashFxUtils.bufferedImage2Mat(img);
				vision.newImage(m, 0);
			}
		});
		addRunnableForUpdate(()->{
			if(camViewer.updateTimeout())
				camViewer.setTimeoutDisplay();
		});
	}

	public static void updateParamDisplay(){
		Platform.runLater(()->{
			instance.controller.updateParam();
		});
	}
	public static Stage getPrimary(){
		return primaryStage;
	}
	
	public static Enumeration<Displayble> getDisplaybles(){
		return displayables.elements();
	}
	public static String getProperty(String prop){
		return properties.getProperty(prop);
	}
	public static void putProperty(String prop, String val){
		properties.putProperty(prop, val);
	}
	public static Properties getProperties(){
		return properties;
	}
	public static void addRunnableForUpdate(Runnable run){
		UpdateWrapper up = new UpdateWrapper();
		up.runnable = run;
		up.removeWhenFinished = false;
		updateTask.updatables.addElement(up);
	}
	public static void runLater(Runnable run){
		UpdateWrapper up = new UpdateWrapper();
		up.runnable = run;
		up.removeWhenFinished = true;
		updateTask.updatables.addElement(up);
	}
	public static void addDisplayable(Displayble d){
		displayables.addElement(d);
	}
	public static boolean communicationsConnected(){
		return communications != null && communications.isConnected();
	}
	public static boolean camConnected(){
		return camClient != null && camClient.isConnected();
	}
	public static CameraViewer getCamViewer(){
		return camViewer;
	}
	public static CvRunner getVision(){
		return vision;
	}
	public static void loadDefaultParameters(){
		if(vision == null || visionParamLoadFailed) return;
		String filename = getProperty(PROP_VISION_DEFAULT_PARAM);
		if(filename != null){
			FlashUtil.getLog().log("Loading default parameters: "+filename, "Dashboard");
			ProcessingParam param = ProcessingParam.loadFromFile(filename);
			if(param != null){
				vision.setParameters(param);
				FlashUtil.getLog().log("Done", "Dashboard");
			}else {
				visionParamLoadFailed = true;
				FlashUtil.getLog().log("Loading failed", "Dashboard");
			}
		}
	}
	public static boolean visionInitialized(){
		return vision != null;
	}
	public static void setForVision(BufferedImage image){
		if(vision != null)
			visionImageNext[1-visionImageIndex] = image;
	}
	protected static void setVision(CvRunner vision){
		Dashboard.vision = vision;
		CvProcessing.pipeline = camViewer;
	}
	private static void validateBasicSettings(){
		if(getProperty(PROP_VISION_DEFAULT_PARAM) == null)
			putProperty(PROP_VISION_DEFAULT_PARAM, "");
	}
	private static void loadSettings(){
		properties.loadFromFile(SETTINGS_FILE);
		String[] keys = properties.keys();
		String[] values = properties.values();
		String props = "Properties: \n";
		for (int i = 0; i < values.length; i++) 
			props += keys[i]+":"+values[i]+"\n";
		FlashUtil.getLog().log(props, "Dashboard");
		
		try {
			Remote.loadHosts(REMOTE_HOSTS_FILE);
		} catch (NullPointerException | IOException e) {
			FlashUtil.getLog().reportError(e.getMessage());
		}
	}
	private static void saveSettings(){
		properties.saveToFile(SETTINGS_FILE);
		Remote.saveHosts(REMOTE_HOSTS_FILE);
	}
	
	
	private MainController controller;
	@Override
	public void start(Stage primaryStage) throws Exception {
		Dashboard.primaryStage = primaryStage;
		Dashboard.instance = this;
		
		BorderPane root = new BorderPane();
		
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("WorldWindow.fxml"));
			loader.setRoot(root);
			root = loader.load();
			controller = loader.getController(); 
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Scene scene = new Scene(root, 1300, 680);
		primaryStage.setScene(scene);
		primaryStage.setResizable(true);
		primaryStage.setOnCloseRequest((e)->{
			controller.stop();
			System.exit(0);
		});
		primaryStage.setTitle("FLASHboard");
		
		primaryStage.show();
	}
	public static void close(){
		FlashUtil.getLog().log("Shutting down");
		updateTask.stop = true;
		if(visionInitialized()){
			FlashUtil.getLog().log("Stopping image processing...");
			vision.close();
		}
		if(camClient != null){
			FlashUtil.getLog().log("Stopping camera client...");
			camClient.stop();
		}
		if(communications != null){
			FlashUtil.getLog().log("Stopping communications...");
			communications.close();
		}
		Remote.closeSessions();
		saveSettings();
		FlashUtil.getLog().log("Settings saved");
		FlashUtil.getLog().close();
		Platform.exit();
	}
}
