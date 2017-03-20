package edu.flash3388.flashlib.robot.rio;

import edu.wpi.first.wpilibj.SampleRobot;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;

import static edu.flash3388.flashlib.robot.Scheduler.*;
import static edu.flash3388.flashlib.util.FlashUtil.*;

import edu.flash3388.flashlib.util.FlashUtil;
import edu.flash3388.flashlib.util.Log;

import static edu.flash3388.flashlib.robot.rio.FlashRioUtil.*;

public abstract class FlashRio extends SampleRobot {
	
	private Log log;
	
	@Override
	protected void robotInit(){
		initFlashLib();
		initRobot();
		log = FlashUtil.getLog();
	}
	@Override
	public void robotMain() {
		log.logTime("STARTING");
		LiveWindow.setEnabled(false);
		while(true){
			if(isDisabled()){
				log.saveLog();
				log.logTime("NEW STATE - DISABLED");
				disableScheduler(true);
				m_ds.InDisabled(true);
				disabledInit();
				
				while(isDisabled()){
					disabledPeriodic();
					delay(5);
				}
				m_ds.InDisabled(false);
				log.logTime("DISABLED - DONE");
			}else if(isAutonomous()){
				log.saveLog();
				log.logTime("NEW STATE - AUTONOMOUS");
				disableScheduler(false);
				m_ds.InAutonomous(true);
				autonomousInit();
				
				while(isEnabled() && isAutonomous()){
					runScheduler();
					autonomousPeriodic();
					delay(5);
				}
				m_ds.InAutonomous(false);
				log.logTime("AUTONOMOUS - DONE");
			}else if(isTest()){
				log.saveLog();
				log.logTime("NEW STATE - TEST");
				disableScheduler(false);
				m_ds.InTest(true);
				testInit();
				
				while(isEnabled() && isTest()){
					runScheduler();
					testPeriodic();
					delay(5);
				}
				m_ds.InTest(false);
				log.logTime("TEST - DONE");
			}else{
				log.saveLog();
				log.logTime("NEW STATE - TELEOP");
				disableScheduler(false);
				m_ds.InOperatorControl(true);
				teleopInit();
				
				while(isEnabled() && isOperatorControl()){
					runScheduler();
					teleopPeriodic();
					delay(3);
				}
				m_ds.InOperatorControl(false);
				log.logTime("TELEOP - DONE");
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
