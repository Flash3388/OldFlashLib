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
	
	public static final int MODE_WRITE = 0x01;
	public static final int MODE_PRINT = 0x02;
	public static final int MODE_INTERFACES = 0x03;
	public static final int MODE_FULL = MODE_WRITE | MODE_PRINT | MODE_INTERFACES;
	
	private static final String EXTENSION = ".flog";
	private static final String ERROR_EXTENSION = ".elog";
	private static String parentDirectory = "";
	
	private Vector<LoggingInterface> loggingInterfaces = new Vector<LoggingInterface>(2);
	
	private String name;
	
	private Queue<String> logLines, errorLines;
	private File logFile, errorFile;
	private boolean closed = true, disable = true;
	private int logMode;
	
	public Log(String directory, String name, boolean override, int logMode){
		this.name = name;
		this.logMode = logMode;
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
			if((logMode & MODE_PRINT) != 0)
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
	public Log(String name, boolean override, int logMode){
		this(parentDirectory+"logs/", name, override, logMode);
	}
	public Log(String name, boolean override){
		this(parentDirectory+"logs/", name, override, MODE_FULL);
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
		if(isClosed() || isDisabled()) return;
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
	public void disable(boolean disable){
		this.disable = disable;
	}
	public boolean isDisabled(){
		return disable;
	}
	public void setLoggingMode(int mode){
		this.logMode = mode;
	}
	public int getLoggingMode(){
		return logMode;
	}
	
	public void addLoggingInterface(LoggingInterface in){
		loggingInterfaces.addElement(in);
	}
	
	public void reportError(String error){
		String err = "ERROR\n\t" + 
					FlashUtil.secs() + " : " + error;
		if((logMode & MODE_WRITE) != 0){
			String trace = getErrorStackTrace();
			writeError(error, trace);
			write(err);
		}
		if((logMode & MODE_PRINT) != 0)
			System.err.println(name + "> " + err);
		if((logMode & MODE_INTERFACES) != 0){
			for(Enumeration<LoggingInterface> lEnum = loggingInterfaces.elements(); lEnum.hasMoreElements();)
				lEnum.nextElement().reportError(error);
		}
	}
	public void reportWarning(String warning){
		String war = "WARNING\n\t" + 
				FlashUtil.secs() + " : " + warning;
		if((logMode & MODE_PRINT) != 0)
			System.err.println(name + "> " + war);
		if((logMode & MODE_WRITE) != 0){
			writeError("WARNING - " +warning);
			write(war);
		}
		if((logMode & MODE_INTERFACES) != 0){
			for(Enumeration<LoggingInterface> lEnum = loggingInterfaces.elements(); lEnum.hasMoreElements();)
				lEnum.nextElement().reportWarning(warning);
		}
	}
	public void saveLog(){
		save();
		if((logMode & MODE_PRINT) != 0)
			System.out.println(name + "> " + FlashUtil.secs() + " : Log Saved");
	}
	public void log(String msg){
		log(msg, getCallerClass());
	}
	public void log(String msg, Class<?> caller){
		log(msg, caller.getName());
	}
	public void log(String msg, String caller){
		if(disable) return;
		msg = caller+": "+msg;
		if((logMode & MODE_WRITE) != 0)
			write(msg);
		if((logMode & MODE_PRINT) != 0)
			System.out.println(name + "> " + msg);
		if((logMode & MODE_INTERFACES) != 0){
			for(Enumeration<LoggingInterface> lEnum = loggingInterfaces.elements(); lEnum.hasMoreElements();)
				lEnum.nextElement().log(msg);
		}
	}
	public void logTime(String msg, double time){
		if(disable) return;
		msg = time + " : ---------->" + msg;
		if((logMode & MODE_WRITE) != 0)
			write(msg);
		if((logMode & MODE_PRINT) != 0)
			System.out.println(name + "> " + msg);
		if((logMode & MODE_INTERFACES) != 0){
			for(Enumeration<LoggingInterface> lEnum = loggingInterfaces.elements(); lEnum.hasMoreElements();)
				lEnum.nextElement().log(msg);
		}
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
