package edu.flash3388.flashlib.robot.hid;

import edu.flash3388.flashlib.communications.Sendable;
import edu.flash3388.flashlib.communications.SendableData;
import edu.flash3388.flashlib.robot.RobotFactory;
import edu.flash3388.flashlib.robot.ScheduledTask;
import edu.flash3388.flashlib.robot.flashboard.HIDSendableData;
import edu.wpi.first.wpilibj.DriverStation;

public class Joystick extends Sendable implements HID, ScheduledTask{

	
	private static Joystick head;
	private Joystick next;
	
	private static final int X = 0, Y = 1, Z = 2, THROTTLE = 3;
	
	private static DriverStation ds = DriverStation.getInstance();
	private Button[] buttons;
	private int stick_num;
	private DPad pov;
	private Stick stick;
	private HIDSendableData data;
	
	public Joystick(String name, int stick, int buttonCount){
		super(name, Type.Joystick);
		stick_num = stick;
		
		this.stick = RobotFactory.createStick(stick, X, Y);
		buttons = new Button[buttonCount];
		for(int i = 0; i < buttons.length; i++)
			buttons[i] = RobotFactory.createButton(stick, i+1);
		pov = RobotFactory.createDpad(stick);
		data = new HIDSendableData(this);
		
		next = head;
		head = this;
	}
	public Joystick(int stick, int buttonCount){
		this("Joystick"+stick, stick, buttonCount);
	}
	
	public double getX(){
		return stick.getX();
	}
	public double getY(){
		return stick.getY();
	}
	public double getZ(){
		return ds.getStickAxis(stick_num, Z);
	}
	public double getThrottle(){
		return ds.getStickAxis(stick_num, THROTTLE);
	}
	
	@Override
	public double getRawAxis(int axis){
		return ds.getStickAxis(stick_num, axis);
	}
	@Override
	public boolean getRawButton(int button){
		return ds.getStickButton(stick_num, (byte)button);
	}
	@Override
	public Button getButton(int button) {
		return buttons[button - 1];
	}
	@Override
	public Stick getStick(int index) {
		switch(index){
			case 0: return stick;
			default: return null;
		}
	}
	@Override
	public Stick getStick() {
		return stick;
	}
	@Override
	public int getButtonCount(){
		return buttons.length;
	}
	@Override
	public DPad getPOV(){
		return pov;
	}

	@Override
	public void newData(byte[] data) {}
	@Override
	public SendableData dataForTransmition() {
		return data;
	}
	
	public void refresh(){
		for(Button b : buttons)
			b.refresh();
		pov.refresh();
	}
	@Override
	public boolean run() {
		refresh();
		return true;
	}
	public static void refreshAll(){
		for(Joystick c = head; c != null; c = c.next)
			c.refresh();
	}
}
