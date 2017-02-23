package edu.flash3388.flashlib.robot.hid;

import edu.wpi.first.wpilibj.DriverStation;

public class StickRio extends Stick{

	private int stick, axisX, axisY;
	
	public StickRio(int stick, int axisX, int axisY){
		this.axisX = axisX;
		this.stick = stick;
		this.axisY = axisY;
	}
	
	@Override
	public double getX() {
		return DriverStation.getInstance().getStickAxis(stick, axisX);
	}
	@Override
	public double getY() {
		return DriverStation.getInstance().getStickAxis(stick, axisY);
	}
}
