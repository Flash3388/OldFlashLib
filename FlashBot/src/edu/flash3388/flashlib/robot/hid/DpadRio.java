package edu.flash3388.flashlib.robot.hid;

import edu.wpi.first.wpilibj.DriverStation;

public class DpadRio extends DPad{

	private int stick;
	
	public DpadRio(int stick) {
		super(stick);
		this.stick = stick;
	}

	@Override
	public int get() {
		return DriverStation.getInstance().getStickPOV(stick, 0);
	}
	
	@Override
	public void refresh(){
		int degrees = DriverStation.getInstance().getStickPOV(stick, 0);
		Up.set(degrees);
		Down.set(degrees);
		Left.set(degrees);
		Right.set(degrees);
		POV.set(degrees);
	}
}
