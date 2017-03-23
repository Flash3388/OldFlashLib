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
	private Log powerLog;
	
	@Override
	protected void robotInit(){
		initFlashLib();
		initRobot();
		log = FlashUtil.getLog();
		powerLog = new Log("powerlog", true);
	}
	@Override
	public void robotMain() {
		log.logTime("STARTING");
		LiveWindow.setEnabled(false);
		powerLog.logTime("Starting Voltage: "+m_ds.getBatteryVoltage(), powerLogTime());
		while(true){
			if(isDisabled()){
				log.logTime("NEW STATE - DISABLED");
				powerLog.logTime("New State: Disabled >> Voltage: "+m_ds.getBatteryVoltage(),
						powerLogTime());
				log.saveLog();
				powerLog.saveLog();
				disableScheduler(true);
				m_ds.InDisabled(true);
				disabledInit();
				
				while(isDisabled()){
					disabledPeriodic();
					logLowVoltage();
					delay(5);
				}
				m_ds.InDisabled(false);
				log.logTime("DISABLED - DONE");
				powerLog.logTime("Done State: Disabled >> Voltage: "+m_ds.getBatteryVoltage(),
						powerLogTime());
			}else if(isAutonomous()){
				log.logTime("NEW STATE - AUTONOMOUS");
				powerLog.logTime("New State: Autonomous >> Voltage: "+m_ds.getBatteryVoltage(),
						powerLogTime());
				log.saveLog();
				powerLog.saveLog();
				disableScheduler(false);
				m_ds.InAutonomous(true);
				autonomousInit();
				
				while(isEnabled() && isAutonomous()){
					runScheduler();
					autonomousPeriodic();
					logLowVoltage();
					delay(5);
				}
				m_ds.InAutonomous(false);
				log.logTime("AUTONOMOUS - DONE");
				powerLog.logTime("Done State: Autonomous >> Voltage: "+m_ds.getBatteryVoltage(),
						powerLogTime());
			}else if(isTest()){
				log.logTime("NEW STATE - TEST");
				powerLog.logTime("New State: Test >> Voltage: "+m_ds.getBatteryVoltage(),
						powerLogTime());
				log.saveLog();
				powerLog.saveLog();
				disableScheduler(false);
				m_ds.InTest(true);
				testInit();
				
				while(isEnabled() && isTest()){
					runScheduler();
					testPeriodic();
					logLowVoltage();
					delay(5);
				}
				m_ds.InTest(false);
				log.logTime("TEST - DONE");
				powerLog.logTime("Done State: Test >> Voltage: "+m_ds.getBatteryVoltage(),
						powerLogTime());
			}else{
				log.logTime("NEW STATE - TELEOP");
				powerLog.logTime("New State: Teleop >> Voltage: "+m_ds.getBatteryVoltage(),
						powerLogTime());
				log.saveLog();
				powerLog.saveLog();
				disableScheduler(false);
				m_ds.InOperatorControl(true);
				teleopInit();
				
				while(isEnabled() && isOperatorControl()){
					runScheduler();
					teleopPeriodic();
					logLowVoltage();
					delay(3);
				}
				m_ds.InOperatorControl(false);
				log.logTime("TELEOP - DONE");
				powerLog.logTime("Done State: Teleop >> Voltage: "+m_ds.getBatteryVoltage(),
						powerLogTime());
			}
		}
	}
	
	private double powerLogTime(){
		double matchTime = m_ds.getMatchTime();
		return matchTime > 0? matchTime : FlashUtil.secs();
	}
	private void logLowVoltage(){
		double volts = m_ds.getBatteryVoltage();
		double matchTime = powerLogTime();
		boolean emergencySave = false;
		if(volts < 10){
			powerLog.logTime("Low Voltage: "+volts, matchTime);
			emergencySave = true;
		}
		if(m_ds.isBrownedOut()){
			powerLog.logTime("Browned Out", matchTime);
			emergencySave = true;
		}
		if(emergencySave){
			powerLog.save();
			log.save();
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
