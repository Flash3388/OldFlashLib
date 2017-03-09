package edu.flash3388.flashlib.robot.flashboard;

import edu.flash3388.flashlib.communications.Sendable;
import edu.flash3388.flashlib.communications.SendableData;
import edu.flash3388.flashlib.robot.devices.DoubleDataSource;
import edu.flash3388.flashlib.util.FlashUtil;

public class DoubleProperty extends Sendable{
	
	private static class DoubleData implements SendableData{

		double lastValue = 0.0, value = 0.0;
		boolean changed = false;
		DoubleDataSource src;
		byte[] bytes = new byte[8];
		
		public DoubleData(DoubleDataSource src){
			set(src);
		}
		
		public void set(DoubleDataSource src){
			lastValue = src.get();
			this.src = src;
		}
		@Override
		public byte[] get() {
			lastValue = value;
			FlashUtil.fillByteArray(lastValue, bytes);
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
	
	private DoubleData data;
	
	public DoubleProperty(String name, DoubleDataSource data) {
		super(name, Type.Double);
		this.data = new DoubleData(data);
	}
	public DoubleProperty(String name){
		this(name, new DoubleDataSource.VarDataSource());
	}

	public void set(DoubleDataSource src){
		data.set(src);
	}
	public DoubleDataSource get(){
		return data.src;
	}
	
	@Override
	public void newData(byte[] data) {}
	@Override
	public SendableData dataForTransmition() {
		return data;
	}

}
