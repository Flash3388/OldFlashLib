package edu.flash3388.flashlib.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

/**
 * Allows to log certain events in a file on the robot.
 * 
 * @author Tom Tzook.
 */
public class Log{

	public static final String DIRECTORY = "logs/";
	
	private static Log instance;
	private static Vector<LoggingInterface> loggingInterfaces = new Vector<LoggingInterface>(2);
	
	private String name = "log";
	private String extension = ".flog";
	private String directory = DIRECTORY + "log_";
	
	private File logFile, errorFile;
	private FileWriter writer;
	private FileWriter error_writer;
	private boolean closed = true;
	
	private Log(){
		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");
		directory += dateFormat.format(new Date()) + "/";
		File file = new File(directory);
		
		if(!file.exists())
			file.mkdirs();
		
		int counter = 1;
		logFile = new File(directory + name + extension);
		while(logFile.exists())
			logFile = new File(directory + name + (counter++) + extension);
		
		try {
			logFile.createNewFile();
			writer = new FileWriter(logFile);
			
			errorFile = new File(directory + name + "_ERROR" + extension);
			if(!errorFile.exists())
				errorFile.createNewFile();
			error_writer = new FileWriter(errorFile);
			closed = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void write(String mess){
		if(isClosed()) return;
		try {
			writer.write(mess + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeError(String mess){
		if(isClosed()) return;
		mess = (FlashUtil.secs()) + ": " + mess;
		try {
			error_writer.write(mess + "\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void close(){
		if(isClosed()) return;
		try {
			writer.flush();
			writer.close();
			error_writer.flush();
			error_writer.close();
			closed = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void delete(){
		if(!isClosed())
			close();
		logFile.delete();
		errorFile.delete();
	}
	public void save(){
		if(isClosed()) return;
		try {
			close();
			writer = new FileWriter(logFile);
			error_writer = new FileWriter(errorFile);
			closed = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public boolean isClosed(){
		return closed || error_writer == null || writer == null;
	}
	
	public static void reportError(String error){
		String err = ">>>>>>>>>>>ERROR\n" + 
					FlashUtil.secs() + " : " + error + 
					"\n--------------------------";
		if(logHasInstance()){
			getInstance().writeError(error);
			getInstance().write(err);
		}
		System.err.println(err);
		for(Enumeration<LoggingInterface> lEnum = loggingInterfaces.elements(); lEnum.hasMoreElements();)
			lEnum.nextElement().reportError(error);
	}
	public static void reportWarning(String warning){
		String war = ">>>>>>>>>>>WARNING\n" + 
				FlashUtil.secs() + " : " + warning + 
				"\n--------------------------";
		System.err.println(war);
		if(logHasInstance()){
			getInstance().writeError("WARNING - " +warning);
			getInstance().write(war);
		}
		for(Enumeration<LoggingInterface> lEnum = loggingInterfaces.elements(); lEnum.hasMoreElements();)
			lEnum.nextElement().reportWarning(warning);
	}
	public static void saveLog(){
		if(!logHasInstance()) return;
		getInstance().save();
		System.out.println(FlashUtil.secs() + " : ---------->Log Saved");
	}
	public static void log(String msg){
		log(msg, getCallerClass());
	}
	public static void log(String msg, Class<?> caller){
		log(msg, caller.getName());
	}
	public static void log(String msg, String caller){
		msg = caller+": "+msg;
		if(logHasInstance())
			getInstance().write(msg);
		System.out.println(msg);
		for(Enumeration<LoggingInterface> lEnum = loggingInterfaces.elements(); lEnum.hasMoreElements();)
			lEnum.nextElement().log(msg);
	}
	public static void logTime(String msg){
		msg = FlashUtil.secs() + " : ---------->" + msg;
		if(logHasInstance())
			getInstance().write(msg);
		System.out.println(msg);
		for(Enumeration<LoggingInterface> lEnum = loggingInterfaces.elements(); lEnum.hasMoreElements();)
			lEnum.nextElement().log(msg);
	}
	public static void init(){
		if(instance == null) 
			instance = new Log();
	}
	public static boolean logHasInstance(){
		return instance != null;
	}
	public static Log getInstance(){
		return instance;
	}
	private static String getCallerClass(){
		StackTraceElement[] traces = Thread.currentThread().getStackTrace();
		if(traces.length > 3)
			return traces[3].getClassName();
		return "";
	}
	
	public static void addLoggingInterface(LoggingInterface in){
		loggingInterfaces.addElement(in);
	}
}
