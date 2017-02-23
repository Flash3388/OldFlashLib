package edu.flash3388.flashlib.communications;

public interface ReadInterface {
	void open();
	void close();
	boolean connect();
	boolean isOpened();
	boolean read(Packet packet);
	void setReadTimeout(long millis);
	long getTimeout();
	void setMaxBufferSize(int bytes);
	int getMaxBufferSize();
	void write(byte[] data);
	void write(byte[] data, int start, int length);
}
