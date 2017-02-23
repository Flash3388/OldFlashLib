package edu.flash3388.flashlib.robot.hid;

import edu.wpi.first.wpilibj.DriverStation;

public class DpadButtonRio extends POVButton{

	public DpadButtonRio(String name, int stick, Type t) {
		super(name, stick, t);
	}
	public DpadButtonRio(int stick, Type t) {
		this("", stick, t);
	}

	@Override
	public void refresh(){
		set(DriverStation.getInstance().getStickPOV(getJoystick(), getNumber()));
	}
}
