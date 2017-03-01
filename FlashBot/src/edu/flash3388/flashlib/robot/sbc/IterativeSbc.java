package edu.flash3388.flashlib.robot.sbc;

import edu.flash3388.flashlib.util.FlashUtil;
import edu.flash3388.flashlib.util.Log;

public abstract class IterativeSbc extends SbcBot{

	private boolean stop = false;
	
	@Override
	protected void startRobot() {
		robotInit();
		
		SbcState lastObsState = null;
		while (!stop) {
			SbcState state = currentState();
			if(lastObsState == null || state.value != lastObsState.value){
				Log.saveLog();
				Log.logTime("NEW STATE - "+state.toString());
				lastObsState = state;
				if(state.value == SbcState.DISABLED)
					disabledInit();
				else stateInit(state);
			}
			if(state.value == SbcState.DISABLED)
				disabledPeriodic();
			else statePeriodic(state);
			
			FlashUtil.delay(5);
		}
	}
	@Override
	protected void stopRobot(){
		stop = true;
		robotShutdown();
	}
	
	protected abstract void robotInit();
	protected abstract void robotShutdown();
	protected abstract void disabledInit();
	protected abstract void disabledPeriodic();
	protected abstract void stateInit(SbcState state);
	protected abstract void statePeriodic(SbcState state);
}
