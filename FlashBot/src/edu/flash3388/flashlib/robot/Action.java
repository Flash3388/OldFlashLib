package edu.flash3388.flashlib.robot;

import java.util.Enumeration;
import java.util.Vector;

import edu.flash3388.flashlib.util.FlashUtil;

/**
 * Represents an action to be executed by the robot. This class cannot be instantiated.
 * 
 * @author Tom Tzook
 */
public abstract class Action {
	
	public static final Action EMPTY = new InstantAction(){
		@Override
		protected void execute() {}
	};
	public static Action addRequirement(Action action, System sys){
		action.requires(sys);
		return action;
	}

	private Vector<System> requirements = new Vector<System>(2);
	private boolean initialized = false;
	private boolean canceled = false;
	private boolean running = false;
	private long timeout = -1;
	private long start_time = -1;
	
	public void start(){
		initialized = false;
		canceled = false;
		running = true;
		Scheduler.getInstance().add(this);
	}
	public void cancel(){
		if(running)
			canceled = true;
	}
	public void removed(){
		if(initialized){
			if(canceled)
				interrupted();
			else end();
		}
		initialized = false;
		canceled = false;
		running = false;
		start_time = -1;
	}
	protected boolean run(){
		if((RobotState.isRobotDisabled() && removeOnDisabled()) || isTimedOut())
			cancel();
		if(canceled)
			return false;
		if(!initialized){
			initialized = true;
			start_time = FlashUtil.millis();
			initialize();
		}
		execute();
		return !isFinished();
	}
	
	public boolean isCanceled(){
		return canceled;
	}
	public boolean isRunning(){
		return running;
	}
	public Enumeration<System> getRequirements(){
		return requirements.elements();
	}
	
	protected void requires(System... subsystems){
		for(System s : subsystems)
			requirements.add(s);
	}
	protected void resetRequirements(){
		requirements.clear();
	}
	protected void requires(System subsystem){
		requirements.addElement(subsystem);
	}
	protected void setTimeOut(long milliseconds){
		timeout = milliseconds;
	}
	protected long getTimeOut(){
		return timeout;
	}
	protected boolean removeOnDisabled(){
		return true;
	}
	protected boolean isTimedOut(){
		return start_time != -1 && timeout != -1 && (FlashUtil.millis() - start_time) 
				>= timeout;
	}
	
	protected void initialize(){ }
	protected boolean isFinished(){ return false;}
	protected void interrupted(){ end();}
	
	protected abstract void execute();
	protected abstract void end();
}
