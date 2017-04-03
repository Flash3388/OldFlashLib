package edu.flash3388.flashlib.testing;

import edu.flash3388.flashlib.util.FlashUtil;
import edu.flash3388.flashlib.util.Log;

public class Main {

	public static void main(String[] args) {
		FlashUtil.setStart();
		Log.setParentDirectory("/home/tomtzook/frc");
		Log log = new Log("TestLog");
		
		log.log("Logging...");
		log.log("Logging...");
		log.logTime("Logging");
		log.reportError("Error");
		
		log.save();
		
		log.log("Logging...2");
		log.log("Logging...2");
		log.logTime("Logging2");
		
		log.close();
	}

}
