package edu.flash3388.flashlib.robot.flashboard;

import edu.flash3388.flashlib.communications.SendableData;
import edu.flash3388.flashlib.robot.hid.HID;

public class HIDSendableData implements SendableData{
	
	private HID stick;
	private byte[] lastBytes = new byte[9], bytes = new byte[9];
	private boolean justConnected = false;
	
	public HIDSendableData(HID con){
		stick = con;
	}
	
	private void update(){
		for(int i = 0; i < 6; i++){
			double get = stick.getRawAxis(i);
			bytes[i] = (byte) ((get < 0)? get * 128 : get * 127);
		}
		bytes[6] = (byte) stick.getButtonCount();
		bytes[7] = bytes[8] = 0;
		for(int i = 0; i < stick.getButtonCount(); i++){
			int a = stick.getRawButton(i + 1)? 1 : 0, index = 7 + ((i >= bytes[6]/2)? 1 : 0);
			bytes[index] = (byte) ((a == 1) ? bytes[index] | (bytes[index] | (a << (i % (bytes[6]/2)))) : 
				bytes[index] & (bytes[index] | (a << (i % (bytes[6]/2)))));
		}
	}
	@Override
	public byte[] get() {
		for(int i = 0; i < 9; i++)
			lastBytes[i] = bytes[i];
		
		return bytes;
	}

	@Override
	public boolean hasChanged() {
		update();
		if(justConnected){
			justConnected = false;
			return true;
		}
		for(int i = 0; i < 6; i++){
			if(Math.abs(bytes[i] - lastBytes[i]) >= 0.08)
				return true;
		}
		if(bytes[7] != lastBytes[7] || bytes[8] != lastBytes[8])
			return true;
		return false;
	}
	
	@Override
	public void onConnection() {
		justConnected = true;
	}
	@Override
	public void onConnectionLost() {
		justConnected = false;
	}
}
