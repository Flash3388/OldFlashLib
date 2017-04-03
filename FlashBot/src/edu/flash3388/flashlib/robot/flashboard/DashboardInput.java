package edu.flash3388.flashlib.robot.flashboard;

import edu.flash3388.flashlib.communications.Sendable;
import edu.flash3388.flashlib.communications.SendableData;
import edu.flash3388.flashlib.util.FlashUtil;

public class DashboardInput extends Sendable{
	
	private String value;
	
	public DashboardInput(String name) {
		super(name, Type.String);
		value = "";
	}

	public boolean empty(){
		return value == null || value.equals("");
	}
	public int intValue(){
		return intValue(0);
	}
	public int intValue(int defaultVal){
		if(empty()) return defaultVal;
		return FlashUtil.toInt(value);
	}
	public double doubleValue(){
		return doubleValue(0);
	}
	public double doubleValue(double defaultVal){
		if(empty()) return defaultVal;
		return FlashUtil.toDouble(value);
	}
	public String stringValue(){
		return stringValue("");
	}
	public String stringValue(String defaultVal){
		if(empty()) return defaultVal;
		return value;
	}
	
	@Override
	public void newData(byte[] data) {
		String newV = new String(data);
		synchronized (value) {
			value = newV;
		}
	}
	@Override
	public SendableData dataForTransmition() {
		return null;
	}
}
