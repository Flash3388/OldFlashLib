package edu.flash3388.flashlib.robot.flashboard;

import edu.flash3388.flashlib.communications.Sendable;
import edu.flash3388.flashlib.communications.SendableData;

public class DashboardSlider extends Sendable{

	private double max, min, value;
	
	public DashboardSlider(String name) {
		super(name, Type.String);
	}

	@Override
	public void newData(byte[] data) {
	}
	@Override
	public SendableData dataForTransmition() {
		return null;
	}
}
