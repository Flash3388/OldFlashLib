package edu.flash3388.flashlib.util;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import edu.flash3388.flashlib.io.FileStream;

/**
 * Allows to log certain events in a file on the robot.
 * 
 * @author Tom Tzook.
 */
public class Log{
	
	private static String parentDirectory = "";
	
	private Vector<LoggingInterface> loggingInterfaces = new Vector<LoggingInterface>(2);
	
	private String extension = ".flog";
	private String directory = parentDirectory+"logs/log_";
	private String name;
	
	private Queue<String> logLines, errorLines;
	private File logFile, errorFile;
	private boolean closed = true;
	
	public Log(String name){
		this.name = name;
		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");
		directory += dateFormat.format(new Date()) + "/";
		File file = new File(directory);
		if(!file.exists())
			file.mkdirs();
		
		int counter = 1;
		logFile = new File(directory + name + extension);
		while(logFile.exists())
			logFile = new File(directory + name + (counter++) + extension);
		
		logLines = new Queue<String>(100);
		errorLines = new Queue<String>(100);
		try {
			logFile.createNewFile();
			
			errorFile = new File(directory + name + counter + "_ERROR" + extension);
			if(!errorFile.exists())
				errorFile.createNewFile();
			closed = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void finalize() throws Throwable{
		close();
		super.finalize();
	}
	
	private void write(String mess){
		if(isClosed()) return;
		logLines.enqueue(mess);
	}
	private void writeError(String mess){
		if(isClosed()) return;
		mess = (FlashUtil.secs()) + ": " + mess;
		errorLines.enqueue(mess);
	}
	private void writeError(String mess, String stacktrace){
		if(isClosed()) return;
		mess = (FlashUtil.secs()) + ": " + mess;
		errorLines.enqueue(mess);
		errorLines.enqueue(stacktrace);
	}
	public void close(){
		if(isClosed()) return;
		save();
		closed = true;
	}
	public void delete(){
		if(!isClosed())
			close();
		logFile.delete();
		errorFile.delete();
	}
	public void save(){
		if(isClosed()) return;
		String[] lines = logLines.toArray(new String[0]);
		logLines.clear();
		
		FileStream.writeLines(logFile.getAbsolutePath(), lines);
		
		lines = errorLines.toArray(new String[0]);
		errorLines.clear();
		
		FileStream.writeLines(errorFile.getAbsolutePath(), lines);
		
		closed = false;
	}
	public boolean isClosed(){
		return closed;
	}
	
	public void addLoggingInterface(LoggingInterface in){
		loggingInterfaces.addElement(in);
	}
	
	public void reportError(String error){
		String err = "ERROR\n" + 
					FlashUtil.secs() + " : " + error + 
					"\n--------------------------";
		String trace = getErrorStackTrace();
		writeError(error, trace);
		write(err);
		System.err.println(name + "> " + err);
		for(Enumeration<LoggingInterface> lEnum = loggingInterfaces.elements(); lEnum.hasMoreElements();)
			lEnum.nextElement().reportError(error);
	}
	public void reportWarning(String warning){
		String war = "WARNING\n" + 
				FlashUtil.secs() + " : " + warning + 
				"\n--------------------------";
		System.err.println(name + "> " + war);
		writeError("WARNING - " +warning);
		write(war);
		for(Enumeration<LoggingInterface> lEnum = loggingInterfaces.elements(); lEnum.hasMoreElements();)
			lEnum.nextElement().reportWarning(warning);
	}
	public void saveLog(){
		save();
		System.out.println(name + "> " + FlashUtil.secs() + " : Log Saved");
	}
	public void log(String msg){
		log(msg, getCallerClass());
	}
	public void log(String msg, Class<?> caller){
		log(msg, caller.getName());
	}
	public void log(String msg, String caller){
		msg = caller+": "+msg;
		write(msg);
		System.out.println(name + "> " + msg);
		for(Enumeration<LoggingInterface> lEnum = loggingInterfaces.elements(); lEnum.hasMoreElements();)
			lEnum.nextElement().log(msg);
	}
	public void logTime(String msg){
		msg = FlashUtil.secs() + " : ---------->" + msg;
		write(msg);
		System.out.println(name + "> " + msg);
		for(Enumeration<LoggingInterface> lEnum = loggingInterfaces.elements(); lEnum.hasMoreElements();)
			lEnum.nextElement().log(msg);
	}
	
	
	private static String getCallerClass(){
		StackTraceElement[] traces = Thread.currentThread().getStackTrace();
		if(traces.length > 3)
			return traces[3].getClassName();
		return "";
	}
	private static String getErrorStackTrace(){
		StackTraceElement[] traces = Thread.currentThread().getStackTrace();
		String trace = "";
		for(int i = 3; i < traces.length; i++)
			trace += "\t"+traces[i].toString()+"\n";
		return trace;
	}
	public static void setParentDirectory(String directory){
		parentDirectory = directory;
		if(!parentDirectory.endsWith("/"))
			parentDirectory += "/";
	}
}
