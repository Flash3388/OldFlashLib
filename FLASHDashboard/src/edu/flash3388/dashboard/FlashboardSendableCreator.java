package edu.flash3388.dashboard;

import edu.flash3388.dashboard.controls.BooleanProperty;
import edu.flash3388.dashboard.controls.Button;
import edu.flash3388.dashboard.controls.Chooser;
import edu.flash3388.dashboard.controls.Controller;
import edu.flash3388.dashboard.controls.DoubleProperty;
import edu.flash3388.dashboard.controls.InputField;
import edu.flash3388.dashboard.controls.PDP;
import edu.flash3388.dashboard.controls.Tester;
import edu.flash3388.flashlib.communications.Sendable;
import edu.flash3388.flashlib.communications.Sendable.Type;
import edu.flash3388.flashlib.communications.SendableCreator;
import edu.flash3388.flashlib.vision.CvRunner;

public class FlashboardSendableCreator implements SendableCreator{

	private Sendable get(String name, int id, byte type) {
		switch(type){
			case Type.ACTIVATABLE: return new Button(name, id);
			case Type.BOOLEAN: return new BooleanProperty(name, id);
			case Type.DOUBLE: return new DoubleProperty(name, id);
			case Type.STRING: return new InputField(name, id);
			case Type.JOYSTICK: return new Controller(name, id);
			case Type.CHOOSER: return new Chooser(name, id);
			case Type.TESTER: Tester.init(id); return Tester.getInstance();
			case Type.MOTOR: if(Tester.getInstance() != null) return Tester.getInstance().addMotor(id);
							else return null;
			case Type.LOG: return new LogWindow.RemoteLog(id);
			case Type.VISION: return Dashboard.visionInitialized()? null : new CvRunner(name, id);
			case Type.PDP: return new PDP(name, id);
		}
		return null;
	}
	@Override
	public Sendable create(String name, int id, byte type) {
		Sendable s = get(name, id, type);
		if(s != null && s instanceof CvRunner)
			Dashboard.setVision((CvRunner)s);
		if(s != null && s instanceof Displayble)
			Dashboard.addDisplayable((Displayble)s);
		return s;
	}
}
