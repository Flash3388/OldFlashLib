package edu.flash3388.flashlib.robot.flashboard;

import edu.flash3388.flashlib.communications.Sendable;
import edu.flash3388.flashlib.communications.SendableData;
import edu.flash3388.flashlib.util.FlashUtil;

public class DashboardSlider extends Sendable{

	public static class SliderSendableData implements SendableData{

		private DashboardSlider slider;
		private boolean changed = false;
		private byte[] data = new byte[20];
		
		@Override
		public byte[] get() {
			changed = false;
			FlashUtil.fillByteArray(slider.minValue(), 0, data);
			FlashUtil.fillByteArray(slider.maxValue(), 8, data);
			FlashUtil.fillByteArray(slider.getTicks(), 16, data);
			return data;
		}
		@Override
		public boolean hasChanged() {
			return changed;
		}
		@Override
		public void onConnection() {
			changed = true;
		}
		@Override
		public void onConnectionLost() {
		}
	}
	
	private double max, min, value;
	private int ticks;
	private SliderSendableData data = new SliderSendableData();
	
	public DashboardSlider(String name, double min, double max, int ticks) {
		super(name, Type.String);
		data.slider = this;
		this.ticks = ticks;
		this.max = max;
		this.min = min;
		data.changed = true;
	}

	public double getTicks(){
		return ticks;
	}
	public double maxValue(){
		return max;
	}
	public double minValue(){
		return min;
	}
	public double doubleValue(){
		return value;
	}
	public void setMinValue(double min){
		this.min = min;
		data.changed = true;
	}
	public void setMaxValue(double max){
		this.max = max;
		data.changed = true;
	}
	public void setTicks(int ticks){
		this.ticks = ticks;
		data.changed = true;
	}
	
	@Override
	public void newData(byte[] data) {
		if(data.length < 8) return;
		value = FlashUtil.toDouble(data);
	}
	@Override
	public SendableData dataForTransmition() {
		return data;
	}
}
