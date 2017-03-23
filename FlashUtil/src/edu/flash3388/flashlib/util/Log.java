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
	
	private static final String EXTENSION = ".flog";
	private static final String ERROR_EXTENSION = ".elog";
	private static String parentDirectory = "";
	
	private Vector<LoggingInterface> loggingInterfaces = new Vector<LoggingInterface>(2);
	
	private String name;
	
	private Queue<String> logLines, errorLines;
	private File logFile, errorFile;
	private boolean closed = true;
	
	public Log(String directory, String name, boolean override){
		this.name = name;
		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("dd_MM_yyyy");
		directory += name + "/" + "log_" + dateFormat.format(date) + "/";
		File file = new File(directory);
		if(!file.exists())
			file.mkdirs();
		
		int counter = 0;
		logFile = new File(directory + name + EXTENSION);
		while(logFile.exists() && !override)
			logFile = new File(directory + name + (++counter) + EXTENSION);
		
		logLines = new Queue<String>(100);
		errorLines = new Queue<String>(100);
		try {
			System.out.println(name+"> Log file: "+logFile.getAbsolutePath());
			if(!logFile.exists())
				logFile.createNewFile();
			dateFormat = new SimpleDateFormat("hh:mm:ss");
			FileStream.writeLine(logFile.getAbsolutePath(), "Time: "+dateFormat.format(date));
			
			errorFile = new File(directory + name + (counter > 0? counter : "") + ERROR_EXTENSION);
			if(!errorFile.exists())
				errorFile.createNewFile();
			closed = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public Log(String name, boolean override){
		this(parentDirectory+"logs/", name, override);
	}
	public Log(String name){
		this(name, false);
	}
	
	@Override
	protected void finalize() throws Throwable{
		close();
		super.finalize();
	}
	
	public void write(String mess){
		if(isClosed()) return;
		logLines.enqueue(mess);
	}
	public void writeError(String mess){
		if(isClosed()) return;
		mess = (FlashUtil.secs()) + ": " + mess;
		errorLines.enqueue(mess);
	}
	public void writeError(String mess, String stacktrace){
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
		
		FileStream.appendLines(logFile.getAbsolutePath(), lines);
		
		lines = errorLines.toArray(new String[0]);
		errorLines.clear();
		
		FileStream.appendLines(errorFile.getAbsolutePath(), lines);
		
		closed = false;
	}
	public boolean isClosed(){
		return closed;
	}
	
	public void addLoggingInterface(LoggingInterface in){
		loggingInterfaces.addElement(in);
	}
	
	public void reportError(String error){
		String err = "ERROR\n\t" + 
					FlashUtil.secs() + " : " + error;
		String trace = getErrorStackTrace();
		writeError(error, trace);
		write(err);
		System.err.println(name + "> " + err);
		for(Enumeration<LoggingInterface> lEnum = loggingInterfaces.elements(); lEnum.hasMoreElements();)
			lEnum.nextElement().reportError(error);
	}
	public void reportWarning(String warning){
		String war = "WARNING\n\t" + 
				FlashUtil.secs() + " : " + warning;
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
	public void logTime(String msg, double time){
		msg = time + " : ---------->" + msg;
		write(msg);
		System.out.println(name + "> " + msg);
		for(Enumeration<LoggingInterface> lEnum = loggingInterfaces.elements(); lEnum.hasMoreElements();)
			lEnum.nextElement().log(msg);
	}
	public void logTime(String msg){
		logTime(msg, FlashUtil.secs());
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
