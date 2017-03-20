package edu.flash3388.flashlib.robot;

import java.io.IOException;

import edu.flash3388.flashlib.communications.Sendable;
import edu.flash3388.flashlib.communications.SendableData;
import edu.flash3388.flashlib.util.FlashUtil;

public class ShellExecutor extends Sendable{
	
	private static class ShellData implements SendableData{
		
		ShellExecutor executor;
		byte[] dataBytes = new byte[5];
		
		@Override
		public byte[] get() {
			Process remProc;
			synchronized (executor.remoteProcess) {
				remProc = executor.remoteProcess;
			}
			switch(executor.executionCode){
				case EXECUTE_PROGRESS:
					String s = ShellExecutor.readInputString(remProc);
					if(s == null) return null;
					byte[] bytes = new byte[1 + s.length()];
					bytes[0] = EXECUTE_PROGRESS;
					java.lang.System.arraycopy(s.getBytes(), 0, bytes, 1, s.length());
					return bytes;
				case FAILED_TO_EXECUTE:
				case EXECUTE_DONE:
					int exitCode = remProc.exitValue();
					dataBytes[0] = (byte) executor.executionCode;
					FlashUtil.fillByteArray(exitCode, 1, dataBytes);
					executor.executionCode = EXECUTE_IDLE;
					return dataBytes;
			}
		
			return null;
		}
		@Override
		public boolean hasChanged() {
			return executor.executionCode != EXECUTE_IDLE;
		}

		@Override
		public void onConnection() {}
		@Override
		public void onConnectionLost() {
			if(executor.executionCode == EXECUTE_PROGRESS){
				executor.remoteProcess.destroyForcibly();
				executor.executionCode = EXECUTE_DONE;
			}
		}
		
	}
	
	private static final byte FAILED_TO_EXECUTE = 0x05;
	private static final byte EXECUTE_DONE = 0x03;
	private static final byte EXECUTE_PROGRESS = 0x07;
	private static final byte EXECUTE_IDLE = 0x00;
	private static final byte EXECUTE_START = 0x01;
	private static final byte EXECUTE_STOP = 0xa;
	
	private Process remoteProcess;
	private int executionCode = EXECUTE_IDLE;
	private ShellData data = new ShellData();
	
	public ShellExecutor(){
		super("Shell", Type.RemoteShell);
		data.executor = this;
	}

	public static String readInputString(Process proc){
		try {
			int av = proc.getInputStream().available();
			if(av < 1) return null;
			byte[] b = new byte[av];
			proc.getInputStream().read(b);
			return new String(b);
		} catch (IOException e) {
			return null;
		}
	}
	public Process execute(String command) throws IOException{
		 return Runtime.getRuntime().exec(command);
	}
	@Override
	public void newData(byte[] data) {
		if(data.length < 1) return;
		if(data[0] == EXECUTE_START){
			try {
				String command = new String(data, 1, data.length - 1);
				remoteProcess = execute(command);
				executionCode = EXECUTE_PROGRESS;
			} catch (IOException e) {
				FlashUtil.getLog().reportError(e.getMessage());
			}
		}
		if(data[0] == EXECUTE_STOP){
			remoteProcess.destroyForcibly();
			executionCode = EXECUTE_DONE;
		}
	}
	@Override
	public SendableData dataForTransmition() {
		return data;
	}
}
