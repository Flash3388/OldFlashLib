package edu.flash3388.flashlib.robot.flashboard;

import java.util.Vector;

import edu.flash3388.flashlib.communications.Sendable;
import edu.flash3388.flashlib.communications.SendableData;
import edu.flash3388.flashlib.util.Log;
import edu.flash3388.flashlib.util.LoggingInterface;

public class SendableLog extends Sendable{

	private static class LogData implements SendableData{

		private Vector<String> logs = new Vector<String>();
		private Vector<String> sentLogs = new Vector<String>();
		private boolean justConnected = false;
		private int sentIndex = 0;
		
		public void feed(String log){
			logs.addElement(log);
		}
		
		@Override
		public byte[] get() {
			String str = "";
			
			if(sentIndex >= sentLogs.size() || sentLogs.isEmpty()) justConnected = false;
			if(justConnected)
				str = sentLogs.elementAt(sentIndex++);
			else if(!logs.isEmpty()){
				String log = logs.elementAt(0);
				logs.removeElementAt(0);
				sentLogs.addElement(log);
				str = log;
			}
			
			return str.getBytes();
		}
		@Override
		public boolean hasChanged() {
			return !logs.isEmpty() || (justConnected && !sentLogs.isEmpty());
		}
		@Override
		public void onConnection() {
			justConnected = true;
			sentIndex = 0;
		}
		@Override
		public void onConnectionLost() {
			justConnected = false;
		}
	}
	
	private LogData data = new LogData();
	
	public SendableLog() {
		super(Type.Log);
		Log.addLoggingInterface(new LoggingInterface(){
			@Override
			public void log(String log) {
				data.feed(log);
			}
			@Override
			public void reportError(String err) {}
			@Override
			public void reportWarning(String war) {}
		});
	}

	@Override
	public void newData(byte[] data) {
	}
	@Override
	public SendableData dataForTransmition() {
		return data;
	}

}
