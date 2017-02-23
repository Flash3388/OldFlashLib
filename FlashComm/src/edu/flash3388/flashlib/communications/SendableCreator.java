package edu.flash3388.flashlib.communications;

public interface SendableCreator {
	Sendable create(String name, int id, byte type);
}
