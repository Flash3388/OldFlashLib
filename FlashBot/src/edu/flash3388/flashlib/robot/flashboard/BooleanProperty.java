package edu.flash3388.flashlib.robot.flashboard;

import edu.flash3388.flashlib.communications.Sendable;
import edu.flash3388.flashlib.communications.SendableData;
import edu.flash3388.flashlib.robot.devices.BooleanDataSource;

public class BooleanProperty extends Sendable{

	private static class BooleanData implements SendableData{

		boolean lastValue = false, value = false, changed = false;
		BooleanDataSource src;
		byte[] bytes = new byte[1];
		
		public BooleanData(BooleanDataSource src){
			set(src);
		}
		
		public void set(BooleanDataSource src){
			lastValue = src.get();
			this.src = src;
		}
		@Override
		public byte[] get() {
			lastValue = value;
			bytes[0] = (byte) (lastValue? 1 : 0);
			changed = false;
			return bytes;
		}
		@Override
		public boolean hasChanged() {
			return lastValue != (value = src.get()) || changed;
		}
		@Override
		public void onConnection() {
			changed = true;
		}
		@Override
		public void onConnectionLost() {
		}
	}
	
	private BooleanData data;
	
	public BooleanProperty(String name, BooleanDataSource data) {
		super(name, Type.Boolean);
		this.data = new BooleanData(data);
	}
	public BooleanProperty(String name){
		this(name, new BooleanDataSource.VarDataSource());
	}

	public void set(BooleanDataSource src){
		data.set(src);
	}
	public BooleanDataSource get(){
		return data.src;
	}
	
	@Override
	public void newData(byte[] data) {}
	@Override
	public SendableData dataForTransmition() {
		return data;
	}

}
