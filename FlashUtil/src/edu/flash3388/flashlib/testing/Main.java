package edu.flash3388.flashlib.testing;

import java.util.Scanner;

import edu.flash3388.flashlib.math.Interpolation;
import edu.flash3388.flashlib.math.NewtonianInterpolation;
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
		
		/*Interpolation lerp = new NewtonianInterpolation(20);
		lerp.readFromFile("/home/tomtzook/frc/interpolation.ini");
		Scanner in = new Scanner(System.in);
		while(true){
			double d = in.nextDouble();
			System.out.println(d+"="+lerp.interpolate(d));
		}*/
	}

}
