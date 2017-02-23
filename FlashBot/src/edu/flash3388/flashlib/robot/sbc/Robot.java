package edu.flash3388.flashlib.robot.sbc;

import static edu.flash3388.flashlib.util.Log.*;
import static edu.flash3388.flashlib.util.FlashUtil.*;
import static edu.flash3388.flashlib.robot.Scheduler.*;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;

import edu.flash3388.flashlib.communications.CommInfo;
import edu.flash3388.flashlib.communications.Communications;
import edu.flash3388.flashlib.communications.ReadInterface;
import edu.flash3388.flashlib.communications.UDPReadInterface;
import edu.flash3388.flashlib.robot.RobotFactory;
import edu.flash3388.flashlib.robot.ShellExecutor;
import edu.flash3388.flashlib.util.Log;
import edu.flash3388.flashlib.util.Properties;
import io.silverspoon.bulldog.core.io.IOPort;
import io.silverspoon.bulldog.core.io.bus.i2c.I2cBus;
import io.silverspoon.bulldog.core.io.bus.spi.SpiBus;
import io.silverspoon.bulldog.core.io.serial.SerialPort;
import io.silverspoon.bulldog.core.pin.Pin;
import io.silverspoon.bulldog.core.platform.Board;
import io.silverspoon.bulldog.core.platform.Platform;

import static edu.flash3388.flashlib.robot.FlashRoboUtil.*;

public abstract class Robot {
	
	public static final String PROP_USER_CLASS = "user.class";
	public static final String PROP_SHUTDOWN_ON_EXIT = "board.shutdown";
	public static final String PROP_COMM_PORT = "board.commport";
	public static final String PROP_COMM_TYPE = "board.commtype";
	
	private static final String NATIVE_LIBRARY_NAME = "";
	private static final String PROPERTIES_FILE = "robot.ini";
	
	private static Board board;
	private static ShellExecutor executor;
	private static Communications communications;
	private static Properties properties = new Properties();

	public static void main(String[] args){
		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		if(!NATIVE_LIBRARY_NAME.equals(""))
			System.loadLibrary(NATIVE_LIBRARY_NAME);
		
		setStart();
		Log.init();
		logTime("Initializing robot...");
		
		log("Loading settings...");
		File file = new File(PROPERTIES_FILE);
		if(file.exists())
			properties.loadFromFile(PROPERTIES_FILE);
		else{
			try {
				file.createNewFile();
			} catch (IOException e) {}
			loadDefaultSettings();
		}
		loadSettings(args);
		properties.saveToFile(PROPERTIES_FILE);
		printSettings();
		
		log("Setting up shutdown hook...");
		Runtime.getRuntime().addShutdownHook(new Thread(()->onShutdown()));
		log("Done");
		
		log("Initializing board...");
		board = Platform.createBoard();
		executor = new ShellExecutor();
		log("Done :: board-name="+getBoardName());
		
		log("Initializing FlashLib...");
		initFlashLib(FLASHBOARD_INIT | SCHEDULER_INIT, RobotFactory.ImplType.SBC);
		
		log("Initializing Communications...");
		ReadInterface inter = null;
		try {
			inter = setupCommInterface();
			if(inter == null)
				throw new Exception("Failure to initialize read interface (null)");
		} catch (Exception e) {
			reportError(e.getMessage());
			shutdown(1);
		}
		communications = new Communications("Robot", inter, true);
		communications.attach(executor);
		communications.start();
		log("Done");
		
		logTime("Initialization Done");
		saveLog();
		
		log("Loading user class...");
		Robot userClass = null;
		String userClassName = "";
		try {
			userClassName = properties.getProperty(PROP_USER_CLASS);
			if(userClassName == null || userClassName.equals(""))
				throw new ClassNotFoundException("User class missing! Must be set to "+PROP_USER_CLASS+" property");
			userClass = (Robot) Class.forName(userClassName).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			reportError(e.getMessage());
			shutdown(1);
		}
		log("User class instantiated: "+userClassName);
		
		logTime("Starting Robot");
		userClass.startRobot();
	}
	private static ReadInterface setupCommInterface() throws SocketException{
		int port = properties.getIntegerProperty(PROP_COMM_PORT);
		if(port <= 0) return null;
		String interfaceType = properties.getProperty(PROP_COMM_TYPE);
		if(interfaceType.equals("udp"))
			return new UDPReadInterface(port);
		return null;
	}
	private static int getPortByBoard(){
		String name = getBoardName();
		if(name.contains("Raspberry"))
			return CommInfo.ROBORIO2RASPBERRY_PORT_RASPBERRY;
		else if(name.contains("BeagleBone"))
			return CommInfo.ROBORIO2BEAGLEBONE_PORT_BEAGLEBONE;
		return 0;
	}
	private static void loadSettings(String[] args){
		for (int i = 0; i < args.length; i++) {
			String[] splits = args[i].split(":");
			if(splits.length == 2)
				properties.putProperty(splits[0], splits[1]);
		}
	}
	private static void loadDefaultSettings(){
		properties.putBooleanProperty(PROP_SHUTDOWN_ON_EXIT, false);
		properties.putIntegerProperty(PROP_COMM_PORT, getPortByBoard());
		properties.putProperty(PROP_COMM_TYPE, "udp");
	}
	private static void onShutdown(){
		logTime("Shuting down...");
		
		if(schedulerHasInstance())
			disableScheduler(true);
		if(communications != null){
			log("Closing communications...");
			communications.close();
			log("Done");
		}
		if(board != null){
			log("Shutting down board...");
			board.shutdown();
			log("Done");
		}
		properties.saveToFile(PROPERTIES_FILE);
		log("Settings saved");
		
		logTime("Shutdown successful");
		boolean shutdown = properties.getBooleanProperty(PROPERTIES_FILE);
		log("Board shutdown="+shutdown);
		saveLog();
		Log.getInstance().close();
		if(shutdown){
			try {
				Runtime.getRuntime().exec("shutdown -s -t 0");
			} catch (IOException e) {}
		}
	}
	
	public static void shutdown(int code){
		System.exit(code);
	}
	public static void shutdown(){
		shutdown(0);
	}
	public static void printSettings(){
		String[] keys = properties.keys(),
				 values = properties.values();
		String print = "Settings:\n";
		for (int i = 0; i < values.length; i++) 
			print += "\r"+keys[i]+"="+values[i];
		log(print);
	}
	public static void setProperty(String property, String value){
		properties.putProperty(property, value);
	}
	public static String getProperty(String property){
		return properties.getProperty(property);
	}
	public static Properties getProperties(){
		return properties;
	}
	
	public static ShellExecutor shell(){
		return executor;
	}
	public static Communications communications(){
		return communications;
	}
	public static Board board(){
		return board;
	}
	public static String getBoardName(){
		return board.getName();
	}
	public static String getBoardProperty(String propertyName){
		return board.getProperty(propertyName);
	}
	public static SerialPort getSerialPort(String name){
		return board.getSerialPort(name);
	}
	public static I2cBus getI2cBus(String name){
		return board.getI2cBus(name);
	}
	public static SpiBus getSpiBus(String name){
		return board.getSpiBus(name);
	}
	public static Pin getPin(String name){
		return board.getPin(name);
	}
	public static IOPort getIOPort(String name){
		return board.getIOPortByName(name);
	}
	
	protected abstract void startRobot();
}
