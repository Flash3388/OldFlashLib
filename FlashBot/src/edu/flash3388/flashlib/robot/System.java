package edu.flash3388.flashlib.robot;

import edu.flash3388.flashlib.communications.Sendable;
import edu.flash3388.flashlib.communications.SendableData;

public abstract class System extends Sendable{
	
	private Action default_action;
	private Action current_action;
	
	protected System(String name, Type t){
		super(name, t);
		Scheduler.getInstance().registerSystem(this);
	}
	
	public void cancelCurrentAction(){
		if(hasCurrentAction() && getCurrentAction().isRunning())
			getCurrentAction().cancel();
	}
	public boolean hasCurrentAction(){
		return current_action != null;
	}
	public void setCurrentAction(Action action){
		current_action = action;
	}
	public Action getCurrentAction(){
		return current_action;
	}
	public void startDefaultAction(){
		if(default_action != null) 
			default_action.start();
	}
	protected void setDefaultAction(Action action){
		default_action = action;
	}
	
	@Override
	public void newData(byte[] bytes){}
	@Override
	public SendableData dataForTransmition(){return null;}
	
	protected abstract void initDefaultAction();

}
