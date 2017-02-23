package edu.flash3388.flashlib.robot;

import edu.flash3388.flashlib.robot.hid.Button;
import edu.flash3388.flashlib.robot.hid.DPad;
import edu.flash3388.flashlib.robot.hid.DpadButtonRio;
import edu.flash3388.flashlib.robot.hid.DpadRio;
import edu.flash3388.flashlib.robot.hid.POVButton;
import edu.flash3388.flashlib.robot.hid.RioButton;
import edu.flash3388.flashlib.robot.hid.RioTrigger;
import edu.flash3388.flashlib.robot.hid.Stick;
import edu.flash3388.flashlib.robot.hid.StickRio;
import edu.flash3388.flashlib.robot.hid.Triggers.Trigger;
import edu.flash3388.flashlib.util.Log;
import edu.flash3388.flashlib.util.LoggingInterface;
import edu.wpi.first.wpilibj.DriverStation;

public class RobotFactory {
	private RobotFactory(){}
	
	public static enum ImplType{
		SBC, RIO
	}
	
	private static ImplType type;
	
	protected static void setImplementationType(ImplType type){
		RobotFactory.type = type;
		
		if(type.equals(ImplType.RIO)){
			Log.addLoggingInterface(new LoggingInterface(){
				@Override
				public void reportError(String err) {
					DriverStation.reportError(err, false);
				}
				@Override
				public void reportWarning(String war) {
					DriverStation.reportWarning(war, false);
				}
				@Override
				public void log(String log) {}
			});
			RobotState.setImplementation(new RobotState(){
				@Override
				public boolean isDisabled() {
					return DriverStation.getInstance().isDisabled();
				}
				@Override
				public boolean isTeleop() {
					return DriverStation.getInstance().isOperatorControl();
				}
				
			});
		}
	}
	
	public static Stick createStick(int stick, int axisX, int axisY){
		switch(type){
			case RIO: return new StickRio(stick, axisX, axisY);
			case SBC: return null;
			default: return null;
		}
	}
	public static Button createButton(int stick, int button){
		switch(type){
			case RIO: return new RioButton(stick, button);
			case SBC: return null;
			default: return null;
		}
	}
	public static Button createButton(String name, int stick, int button){
		switch(type){
			case RIO: return new RioButton(name, stick, button);
			case SBC: return null;
			default: return null;
		}
	}
	public static DPad createDpad(int stick){
		switch(type){
			case RIO: return new DpadRio(stick);
			case SBC: return null;
			default: return null;
		}
	}
	public static POVButton createDpadButton(int stick, POVButton.Type t){
		switch(type){
			case RIO: return new DpadButtonRio(stick, t);
			case SBC: return null;
			default: return null;
		}
	}
	public static Trigger createTrigger(int stick, int trig){
		switch(type){
			case RIO: return new RioTrigger(stick, trig);
			case SBC: return null;
			default: return null;
		}
	}
}
