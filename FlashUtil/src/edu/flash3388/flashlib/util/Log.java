package edu.flash3388.flashlib.util;

import java.io.File;
import java.io.FileWriter;
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
	
	public static enum LoggingType{
		Stream, Buffered
	}
	public static enum BufferedLogType{
		FlushAmount, FlushTime, FlushManual
	}
	
	public static final int MODE_DISABLED = 0x00;
	public static final int MODE_WRITE = 0x01 << 2;
	public static final int MODE_PRINT = 0x01 << 3;
	public static final int MODE_INTERFACES = 0x01 << 4;
	public static final int MODE_FULL = MODE_WRITE | MODE_PRINT | MODE_INTERFACES;
	
	private static final int BUFFER_SIZE = 50;
	private static final String EXTENSION = ".flog";
	private static final String ERROR_EXTENSION = ".elog";
	private static String parentDirectory = "";
	
	private Vector<LoggingInterface> loggingInterfaces = new Vector<LoggingInterface>(2);
	
	private String name;
	
	private FileWriter writerLog, writerErrorLog;
	private String[] logLines, errorLines;
	private File logFile, errorFile;
	private boolean closed = true;
	private int logMode, indexLog, indexErrorLog;
	private LoggingType type;
	
	public Log(String directory, String name, LoggingType type, boolean override, int logMode){
		this.name = name;
		this.logMode = logMode;
		this.type = type;
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
			
			if(type == LoggingType.Buffered){
				logLines = new String[BUFFER_SIZE];
				errorLines = new String[BUFFER_SIZE];
			}else{
				writerLog = new FileWriter(logFile);
				writerErrorLog = new FileWriter(errorFile);
			}
			
			closed = false;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public Log(String name, LoggingType type, boolean override, int logMode){
		this(parentDirectory+"logs/", name, type, override, logMode);
	}
	public Log(String name, LoggingType type, boolean override){
		this(parentDirectory+"logs/", name, type, override, MODE_FULL);
	}
	public Log(String name){
		this(name, LoggingType.Stream, false);
	}
	
	private synchronized void flushLogFile(){
		if(indexLog == 0) return;
		FileStream.appendLines(logFile.getAbsolutePath(), logLines);
		indexLog = 0;
	}
	private synchronized void flushErrorLogFile(){
		if(indexErrorLog == 0) return;
		FileStream.appendLines(errorFile.getAbsolutePath(), errorLines);
		indexErrorLog = 0;
	}
	
	public synchronized void write(String mess){
		if(isClosed()) return;
		if(type == LoggingType.Buffered){
			if(indexLog >= BUFFER_SIZE)
				flushLogFile();
			logLines[indexLog++] = mess;
		}else{
			try {
				writerLog.write(mess);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public synchronized void writeError(String mess){
		if(isClosed()) return;
		mess = (FlashUtil.secs()) + ": " + mess;
		if(type == LoggingType.Buffered){
			if(indexErrorLog >= BUFFER_SIZE)
				flushErrorLogFile();
			errorLines[indexErrorLog++] = mess;
		}else{
			try {
				writerErrorLog.write(mess);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public synchronized void writeError(String mess, String stacktrace){
		if(isClosed()) return;
		mess = (FlashUtil.secs()) + ": " + mess;
		if(type == LoggingType.Buffered){
			if(indexErrorLog >= BUFFER_SIZE)
				flushErrorLogFile();
			errorLines[indexErrorLog++] = mess;
		}else{
			try {
				writerErrorLog.write(mess);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public synchronized void close(){
		if(isClosed()) return;
		save();
		closed = true;
	}
	public synchronized void delete(){
		if(!isClosed())
			close();
		logFile.delete();
		errorFile.delete();
	}
	public synchronized void save(){
		if(isClosed() || isDisabled()) return;
		
		if(type == LoggingType.Buffered){
			flushLogFile();
			flushErrorLogFile();
		}else{
			try {
				writerLog.flush();
				writerErrorLog.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		closed = false;
		if((logMode & MODE_PRINT) != 0)
			System.out.println(name + "> " + FlashUtil.secs() + " : Log Saved");
	}
	
	public LoggingType getLoggingType(){
		return type;
	}
	public boolean isClosed(){
		return closed || (type == LoggingType.Stream && (writerLog == null || writerErrorLog == null));
	}
	public void disable(){
		setLoggingMode(MODE_DISABLED);
	}
	public boolean isDisabled(){
		return logMode == MODE_DISABLED;
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
		if(isDisabled()) return;
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
		if(isDisabled()) return;
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
	public void log(String msg){
		log(msg, getCallerClass());
	}
	public void log(String msg, Class<?> caller){
		log(msg, caller.getName());
	}
	public void log(String msg, String caller){
		if(isDisabled()) return;
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
	public void logTime(String msg){
		logTime(msg, FlashUtil.secs());
	}
	public void logTime(String msg, double time){
		if(isDisabled()) return;
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
