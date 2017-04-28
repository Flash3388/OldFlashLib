package edu.flash3388.flashlib.robot.sbc;

import java.util.Arrays;

import edu.flash3388.flashlib.communications.Sendable;
import edu.flash3388.flashlib.communications.SendableData;

public final class SbcControlStation extends Sendable{

	public static final short MAX_CONTROLLERS = 3;
	public static final short CONTROLLER_AXES = 6;
	private static final byte CONTROLLER_DATA_SIZE = 100;
	
	private static class ControllerButtons{
		byte count;
		short buttons;
	}
	private static class ControlSendableData implements SendableData{

		boolean attached = false;
		SbcControlStation cs;
		
		ControlSendableData(SbcControlStation cs){
			this.cs = cs;
		}
		
		@Override
		public byte[] get() {
			return null;
		}
		@Override
		public boolean hasChanged() {
			return false;
		}

		@Override
		public void onConnection() {
			attached = true;
		}
		@Override
		public void onConnectionLost() {
			attached = false;
		}
	}
	private static class UpdateTask implements Runnable{

		private SbcControlStation cs;
		
		public UpdateTask(SbcControlStation cs){
			this.cs = cs;
		}
		
		@Override
		public void run() {
		}
	}
	
	private short[][] controllerAxes = new short[MAX_CONTROLLERS][CONTROLLER_AXES];
	private short[] controllerPovs = new short[MAX_CONTROLLERS];
	private ControllerButtons[] controllerButtons = new ControllerButtons[MAX_CONTROLLERS];
	private byte[][] controllersData = new byte[2][CONTROLLER_DATA_SIZE];
	private byte dataIndex = 0;
	
	private ControlSendableData senData;
	private UpdateTask upTask;
	private Thread csThread;
	
	SbcControlStation() {
		super("CS-"+SbcBot.getBoardName(), (byte)0x0);
		
		for (int i = 0; i < controllerButtons.length; i++)
			controllerButtons[i] = new ControllerButtons();
		senData = new ControlSendableData(this);
		upTask = new UpdateTask(this);
		csThread = new Thread(upTask, "CS-Update");
		csThread.setPriority((Thread.MAX_PRIORITY + Thread.MIN_PRIORITY) / 2);
		csThread.start();
	}

	private void updateControllers(){
		
	}
	
	@Override
	public void newData(byte[] data) {
		if(data.length < CONTROLLER_DATA_SIZE / 2) {
			return;
		}
		if(data.length != controllersData[1 - dataIndex].length)
			controllersData[1 - dataIndex] = Arrays.copyOf(data, data.length);
		else System.arraycopy(data, 0, controllersData, 0, data.length);
	}
	@Override
	public SendableData dataForTransmition() {
		return null;
	}
}
