package edu.flash3388.flashlib.robot.rio;

import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;

import static edu.flash3388.flashlib.robot.Scheduler.*;
import static edu.flash3388.flashlib.util.FlashUtil.*;
import static edu.flash3388.flashlib.util.Log.*;
import static edu.flash3388.flashlib.robot.rio.FlashRioUtil.*;

public abstract class FlashRio extends SampleRobot {
	
	@Override
	protected void robotInit(){
		initFlashLib();
		initRobot();
	}
	@Override
	public void robotMain() {
		logTime("STARTING");
		LiveWindow.setEnabled(false);
		while(true){
			if(isDisabled()){
				saveLog();
				logTime("NEW STATE - DISABLED");
				disableScheduler(true);
				m_ds.InDisabled(true);
				disabledInit();
				
				while(isDisabled()){
					disabledPeriodic();
					delay(5);
				}
				m_ds.InDisabled(false);
				logTime("DISABLED - DONE");
			}else if(isAutonomous()){
				saveLog();
				logTime("NEW STATE - AUTONOMOUS");
				disableScheduler(false);
				m_ds.InAutonomous(true);
				autonomousInit();
				
				while(isEnabled() && isAutonomous()){
					runScheduler();
					autonomousPeriodic();
					delay(5);
				}
				m_ds.InAutonomous(false);
				logTime("AUTONOMOUS - DONE");
			}else if(isTest()){
				saveLog();
				logTime("NEW STATE - TEST");
				disableScheduler(false);
				m_ds.InTest(true);
				testInit();
				
				while(isEnabled() && isTest()){
					runScheduler();
					testPeriodic();
					delay(5);
				}
				m_ds.InTest(false);
				logTime("TEST - DONE");
			}else{
				saveLog();
				logTime("NEW STATE - TELEOP");
				disableScheduler(false);
				m_ds.InOperatorControl(true);
				teleopInit();
				
				while(isEnabled() && isOperatorControl()){
					runScheduler();
					teleopPeriodic();
					delay(3);
				}
				m_ds.InOperatorControl(false);
				logTime("TELEOP - DONE");
			}
		}
	}
	
	protected abstract void initRobot();
	protected abstract void teleopInit();
	protected abstract void teleopPeriodic();
	protected abstract void autonomousInit();
	protected abstract void autonomousPeriodic();
	protected abstract void disabledInit();
	protected abstract void disabledPeriodic();
	protected void testInit(){}
	protected void testPeriodic(){}
}
