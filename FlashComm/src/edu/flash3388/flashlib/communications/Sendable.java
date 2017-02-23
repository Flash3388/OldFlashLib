package edu.flash3388.flashlib.communications;

public abstract class Sendable {
	public static class Type{
		public static final byte CHOOSER = 0x01;
		public static final byte ACTIVATABLE = 0x02;
		public static final byte JOYSTICK = 0x03;
		public static final byte CAM = 0x04;
		public static final byte DOUBLE = 0x05;
		public static final byte BOOLEAN = 0x06;
		public static final byte STRING = 0x07;
		public static final byte LOG = 0x08;
		public static final byte TESTER = 0x09;
		public static final byte MOTOR = 0x10;
		public static final byte VISION = 0x11;
		public static final byte DNAVX = 0x12;
		public static final byte RSHELL = 0x13;
		public static final byte PDP = 0x14;
		
		public static final int TYPE_DASHBOARD = 0x01;
		public static final int TYPE_SBC = 0x01 << 1;
		public static final int TYPE_BOTH = TYPE_DASHBOARD | TYPE_SBC;
		
		public final int type;
		public final byte value;
		private Type(byte val, int type){
			value = val;
			this.type = type;
		}
		
		public static final Type Pdp = new Type(PDP, TYPE_BOTH);
		public static final Type RemoteShell = new Type(RSHELL, TYPE_SBC);
		public static final Type DashboardNavx = new Type(DNAVX, TYPE_DASHBOARD);
		public static final Type Vision = new Type(VISION, TYPE_BOTH);
		public static final Type Motor = new Type(MOTOR, TYPE_DASHBOARD);
		public static final Type Tester = new Type(TESTER, TYPE_DASHBOARD);
		public static final Type Chooser = new Type(CHOOSER, TYPE_DASHBOARD);
		public static final Type Activatable = new Type(ACTIVATABLE, TYPE_BOTH);
		public static final Type Boolean = new Type(BOOLEAN, TYPE_BOTH);
		public static final Type Double = new Type(DOUBLE, TYPE_BOTH);
		public static final Type String = new Type(STRING, TYPE_BOTH);
		public static final Type Joystick = new Type(JOYSTICK, TYPE_BOTH);
		public static final Type Camera = new Type(CAM, TYPE_BOTH);
		public static final Type Log = new Type(LOG, TYPE_DASHBOARD);
	}
	
	private static int nextID = 0;
	private int id;
	private String name;
	private Type type;
	private boolean init = false, attached = false;
	
	protected Sendable(Type type){
		this("", nextID++, type);
	}
	protected Sendable(String name, Type type){
		this(name, nextID++, type);
	}
	protected Sendable(String name, int id, Type type){
		this.id = id;
		this.type = type;
		this.name = name;
	}
	
	boolean remoteInit(){
		return init;
	}
	void setRemoteInit(boolean b){
		init = b;
	}
	void setAttached(boolean attached){
		this.attached = attached;
	}
	public String getName(){
		return name;
	}
	public void setName(String name){
		this.name = name;
	}
	public int getID(){
		return id;
	}
	public Type getType(){
		return type;
	}
	public boolean attached(){
		return attached;
	}
	
	public abstract void newData(byte[] data);
	public abstract SendableData dataForTransmition();
}
