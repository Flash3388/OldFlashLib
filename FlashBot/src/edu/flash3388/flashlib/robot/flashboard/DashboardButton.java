package edu.flash3388.flashlib.robot.flashboard;

import java.util.Enumeration;
import java.util.Vector;

import edu.flash3388.flashlib.communications.Sendable;
import edu.flash3388.flashlib.communications.SendableData;
import edu.flash3388.flashlib.robot.Action;
import edu.flash3388.flashlib.util.Log;

public class DashboardButton extends Sendable{

	public static class ButtonData implements SendableData{
		private byte[] done = {1};
		
		@Override
		public byte[] get() {
			return done;
		}
		@Override
		public boolean hasChanged() {
			return true;
		}
		@Override
		public void onConnection() {
		}
		@Override
		public void onConnectionLost() {
		}
	}
	
	private Vector<Action> actions = new Vector<Action>(2);
	private boolean activated = false;
	private ButtonData data = new ButtonData();
	
	public DashboardButton(String name) {
		super(name, Type.Activatable);
	}

	public void whenPressed(Action action){
		actions.add(action);
	}
	
	@Override
	public void newData(byte[] data) {
		if(data[0] == 1){
			Log.log("Start");
			for(Enumeration<Action> eA = actions.elements(); eA.hasMoreElements();){
				Action a = eA.nextElement();
				if(!a.isRunning())
					a.start();
			}
			activated = true;
		}
	}
	@Override
	public SendableData dataForTransmition() {
		if(activated){
			for(Enumeration<Action> eA = actions.elements(); eA.hasMoreElements();){
				if(eA.nextElement().isRunning())
					return null;
			}
			activated = false;
			return data;
		}
		return null;
	}
}
