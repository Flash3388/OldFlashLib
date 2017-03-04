package edu.flash3388.flashlib.robot;

import java.util.Enumeration;

import edu.flash3388.flashlib.robot.devices.BooleanDataSource;

public class ConditionalAction extends Action {

	private BooleanDataSource condition;
	private Action actionTrue, actionFalse, runAction;
	
	public ConditionalAction(BooleanDataSource condition, Action aTrue, Action aFalse){
		this.condition = condition;
		this.actionTrue = aTrue;
		this.actionFalse = aFalse;
		
		Enumeration<System> requirements = actionTrue.getRequirements();
		while (requirements.hasMoreElements()) {
			System system = requirements.nextElement();
			requires(system);
		}
	}
	
	@Override
	protected void initialize(){ 
		runAction = condition.get()? actionTrue : actionFalse;
		runAction.initialize();
	}
	@Override
	protected void execute() {
		runAction.execute();
	}
	@Override
	protected boolean isFinished(){ 
		return runAction.isFinished();
	}
	@Override
	protected void end() {
		runAction.end();
	}
	@Override
	protected void interrupted(){ 
		runAction.interrupted();
	}
}
