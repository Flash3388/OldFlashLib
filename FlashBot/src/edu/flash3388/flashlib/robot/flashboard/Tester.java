package edu.flash3388.flashlib.robot.flashboard;

import java.util.Vector;

import com.ctre.CANTalon;

import edu.flash3388.flashlib.communications.Sendable;
import edu.flash3388.flashlib.communications.SendableData;
import edu.flash3388.flashlib.flashboard.FlashboardSendableType;
import edu.flash3388.flashlib.robot.rio.RioControllers;
import edu.flash3388.flashlib.util.FlashUtil;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.SpeedController;
import edu.wpi.first.wpilibj.Talon;
import edu.wpi.first.wpilibj.TalonSRX;
import edu.wpi.first.wpilibj.Victor;
import edu.wpi.first.wpilibj.VictorSP;

public class Tester extends Sendable{

	public static class Motor extends Sendable{
		private SpeedController controller;
		private CANTalon canTalonController;
		private boolean brakeMode = false;
		private boolean enabled = false;
		private boolean canTalon = false;
		private int channel = -1;
		private MotorData data;
		private boolean send = false;
		
		public Motor(SpeedController con){
			super("Tester-Motor", FlashboardSendableType.MOTOR);
			controller = con;
			if(con instanceof CANTalon){
				canTalonController = (CANTalon)con;
				canTalon = true;
				enableBrakeMode(false);
				channel = canTalonController.getDeviceID();
			}else if(con instanceof Talon){
				channel = ((Talon)con).getChannel();
			}else if(con instanceof Victor){
				channel = ((Victor)con).getChannel();
			}else if(con instanceof Victor){
				channel = ((VictorSP)con).getChannel();
			}else if(con instanceof Jaguar){
				channel = ((Jaguar)con).getChannel();
			}else if(con instanceof TalonSRX){
				channel = ((TalonSRX)con).getChannel();
			}
			data = new MotorData(this);
		}
		
		public void enableBrakeMode(boolean brakeMode){
			if(!canTalon) return;
			this.brakeMode = brakeMode;
			canTalonController.enableBrakeMode(brakeMode);
		}
		public boolean isBrakeMode(){
			return canTalon && brakeMode;
		}
		public double voltage(){
			if(!canTalon) return -1;
			return canTalonController.getOutputVoltage();
		}
		public double current(){
			if(!canTalon) return -1;
			return canTalonController.getOutputCurrent();
		}
		public double get(){
			return controller.get();
		}
		public void set(double speed){
			if(enabled)
				controller.set(speed);
		}
		public void enable(boolean en){
			enabled = en;
		}
		public void send(boolean send){
			this.send = send;
		}

		@Override
		public void newData(byte[] data) {
			if(data.length < 2 || !send) return;
			boolean e = data[0] == 1,
					b = data[1] == 1;
			if(e != enabled)
				enable(e);
			if(b != brakeMode)
				enableBrakeMode(b);
			if(data.length < 10) return;
			double d = FlashUtil.toDouble(data, 2);
			set(d);
		}
		@Override
		public SendableData dataForTransmition() {
			return send? data : null;
		}
	}
	private static class MotorData implements SendableData{

		private Motor motor;
		private byte[] bytes = new byte[31];//channel, enabled, cantalon, brakemode, get, current, voltage
		private byte[] lastBytes = new byte[31];
		
		public MotorData(Motor motor){
			this.motor = motor;
		}
		
		@Override
		public byte[] get() {
			return bytes;
		}
		@Override
		public boolean hasChanged() {
			FlashUtil.fillByteArray(motor.channel, bytes);
			bytes[4] = (byte) (motor.enabled ? 1 : 0);
			bytes[5] = (byte) (motor.canTalon ? 1 : 0);
			bytes[6] = (byte) (motor.brakeMode ? 1 : 0);
			FlashUtil.fillByteArray(motor.get(), 7, bytes);
			FlashUtil.fillByteArray(motor.current(), 15, bytes);
			FlashUtil.fillByteArray(motor.voltage(), 23, bytes);
			
			boolean changed = false;
			for(int i = 0; i < 31; i++){
				if(lastBytes[i] != bytes[i]){
					lastBytes[i] = bytes[i];
					if(!changed) changed = true;
				}
			}
			return changed;
		}
		@Override
		public void onConnection() {
		}
		@Override
		public void onConnectionLost() {
		}
	}
	private static class TesterData implements SendableData{
		byte[] bytes = {1};
		boolean update = true;
		Tester tester;
		boolean confirm = false;
		
		@Override
		public byte[] get() {
			confirm = false;
			return bytes;
		}
		@Override
		public boolean hasChanged() {
			return confirm;
		}
		@Override
		public void onConnection() {
			if(update){
				update = false;
				tester.start();
			}
		}
		@Override
		public void onConnectionLost() {
		}
	}
	
	private static Tester instance;
	
	private Vector<Motor> controllers = new Vector<Motor>();
	private TesterData data = new TesterData();
	private boolean send = false;
	
	private Tester(){
		super("Tester", FlashboardSendableType.TESTER);
		data.tester = this;
	}
	
	public void registerMotor(SpeedController controller){
		controllers.add(new Motor(controller));
	}
	public void registerMotors(SpeedController... controllers){
		for (SpeedController speedController : controllers) 
			this.controllers.add(new Motor(speedController));
	}
	public void registerMotors(RioControllers...controllers){
		for(RioControllers c : controllers){
			for(int i = 0; i < c.getControllerCount(); i++)
				this.controllers.addElement(new Motor(c.getController(i)));
		}
	}
	
	public Motor get(int index){
		return controllers.get(index);
	}
	public void set(double speed){
		for(Motor controller : controllers)
			controller.set(speed);
	}
	public void set(double speed, int motor){
		controllers.get(motor).set(speed);
	}
	public void enableBrakeMode(boolean enable){
		for(Motor c : controllers){
			if(c.brakeMode != enable)
				c.enableBrakeMode(enable);
		}
	}
	public void enable(boolean en, int motor){
		controllers.get(motor).enable(en);
	}
	@Override
	public void newData(byte[] data) {
		if(data.length < 1) return;
		this.data.confirm = true;
		send = data[0] == 1;
		for(Motor c : controllers)
			c.send(send);
	}
	@Override
	public SendableData dataForTransmition() {
		return data;
	}
	
	public void start(){
		for(Motor c : controllers)
			Flashboard.attach(c);
	}
	public static Tester getInstance(){
		return instance;
	}
	public static void init(){
		instance = new Tester();
	}
}
