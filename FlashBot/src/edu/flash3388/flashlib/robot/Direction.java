package edu.flash3388.flashlib.robot;

public class Direction{
	private static final int FORWARD = 1;
	private static final int BACKWARD = -1;
	private static final int RIGHT = 1;
	private static final int LEFT = -1;
	private static final int NORMAL = 1;
	private static final int REVERSED = -1;
	private static final int NONE = 0;
	
	public final int value;
	private Direction(int val){
		value = val;
	}
	
	public static final Direction Forward = new Direction(FORWARD);
	public static final Direction Backward = new Direction(BACKWARD);
	public static final Direction Right = new Direction(RIGHT);
	public static final Direction Left = new Direction(LEFT);
	public static final Direction Normal = new Direction(NORMAL);
	public static final Direction Reversed = new Direction(REVERSED);
	public static final Direction None = new Direction(NONE);
}
