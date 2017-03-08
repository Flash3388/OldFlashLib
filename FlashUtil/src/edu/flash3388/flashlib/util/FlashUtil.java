package edu.flash3388.flashlib.util;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class FlashUtil {
	
	private FlashUtil(){}
	private static long startTime = 0;
	
	//--------------------------------------------------------------------
	//--------------------------Time--------------------------------------
	//--------------------------------------------------------------------
	
	public static void delay(long ms){
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {}
	}
	public static void delay(double secs){
		delay((long)(secs * 1000));
	}
	public static long millis(){
		return startTime != 0? System.currentTimeMillis() - startTime : -1;
	}
	public static double secs(){
		return startTime != 0? millis() / 1000.0 : -1;
	}
	protected static void setStartTime(long time){
		if(startTime == 0)
			startTime = time;
	}
	public static void setStart(){
		setStartTime(System.currentTimeMillis());
	}
	
	//--------------------------------------------------------------------
	//--------------------------Arrays------------------------------------
	//--------------------------------------------------------------------
	
	public static void printArray(byte[] s){
		for(byte i : s)
			System.out.println((int)i);
	}
	public static void printArray(int[] s){
		for(int i : s)
			System.out.println(i);
	}
	public static void printArray(double[] s){
		for(double d : s)
			System.out.println(d);
	}
	public static void printArray(short[] s){
		for(short sh : s)
			System.out.println(sh);
	}
	public static void printArray(float[] s){
		for(float f : s)
			System.out.println(f);
	}
	public static void printArray(long[] s){
		for(float l : s)
			System.out.println(l);
	}
	public static <T> void printArray(T[] s){
		for(T f : s)
			System.out.println(f);
	}
	
	public static void shiftArrayL(int[] arr, int start, int end){
		if(start > end || end > arr.length || start > arr.length || start < 0 || end < 0)
			throw new IllegalArgumentException("Illegal shift arguments");
		for (int i = start; i < end; i++) 
			arr[i] = arr[i+1];
	}
	public static void shiftArrayL(double[] arr, int start, int end){
		if(start > end || end > arr.length || start > arr.length || start < 0 || end < 0)
			throw new IllegalArgumentException("Illegal shift arguments");
		for (int i = start; i < end; i++) 
			arr[i] = arr[i+1];
	}
	public static void shiftArrayL(float[] arr, int start, int end){
		if(start > end || end > arr.length || start > arr.length || start < 0 || end < 0)
			throw new IllegalArgumentException("Illegal shift arguments");
		for (int i = start; i < end; i++) 
			arr[i] = arr[i+1];
	}
	public static void shiftArrayL(long[] arr, int start, int end){
		if(start > end || end > arr.length || start > arr.length || start < 0 || end < 0)
			throw new IllegalArgumentException("Illegal shift arguments");
		for (int i = start; i < end; i++) 
			arr[i] = arr[i+1];
	}
	public static void shiftArrayL(byte[] arr, int start, int end){
		if(start > end || end > arr.length || start > arr.length || start < 0 || end < 0)
			throw new IllegalArgumentException("Illegal shift arguments");
		for (int i = start; i < end; i++) 
			arr[i] = arr[i+1];
	}
	public static void shiftArrayL(short[] arr, int start, int end){
		if(start > end || end > arr.length || start > arr.length || start < 0 || end < 0)
			throw new IllegalArgumentException("Illegal shift arguments");
		for (int i = start; i < end; i++) 
			arr[i] = arr[i+1];
	}
	public static <T> void shiftArrayL(T[] arr, int start, int end){
		if(start > end || end > arr.length || start > arr.length || start < 0 || end < 0)
			throw new IllegalArgumentException("Illegal shift arguments");
		for (int i = start; i < end; i++) 
			arr[i] = arr[i+1];
	}
	
	//--------------------------------------------------------------------
	//--------------------------Byte Arrays-------------------------------
	//--------------------------------------------------------------------
	
	public static byte[] fillByteArray(double value, byte[] bytes){
		return fillByteArray(value, 0, bytes);
	}
	public static byte[] fillByteArray(double value, int start, byte[] bytes){
		if(bytes.length < 8) throw new IllegalArgumentException("Bytes array must be 8 bytes long");
		
		long lng = Double.doubleToLongBits(value);
		for(int i = 0; i < 8; i++) 
			bytes[start + i] = (byte)((lng >> ((7 - i) * 8)) & 0xff);
		return bytes;
	}
	public static byte[] fillByteArray(int value, byte[] bytes){
		return fillByteArray(value, 0, bytes);
	}
	public static byte[] fillByteArray(int value, int start, byte[] bytes){
		if(bytes.length < 4) throw new IllegalArgumentException("Bytes array must be 4 bytes long");
		
		bytes[start + 3] = (byte) (value & 0xFF);   
		bytes[start + 2] = (byte) ((value >> 8) & 0xFF);   
		bytes[start + 1] = (byte) ((value >> 16) & 0xFF);   
		bytes[start] = (byte) ((value >> 24) & 0xFF);
		return bytes;
	}
	
	public static byte[] toByteArray(int value){
	    byte[] bytes = new byte[4];
	    fillByteArray(value, bytes);
	    return bytes;
	}
	public static int toInt(byte[] b){
	    return   b[3] & 0xFF |
	            (b[2] & 0xFF) << 8 |
	            (b[1] & 0xFF) << 16 |
	            (b[0] & 0xFF) << 24;
	}
	public static int toInt(byte[] b, int s){
	    return toInt(Arrays.copyOfRange(b, s, s + 4));
	}
	public static byte[] toByteArray(double value) {
	    byte[] bytes = new byte[8];
	    fillByteArray(value, bytes);
	    return bytes;
	}
	public static double toDouble(byte[] bytes) {
	    return ByteBuffer.wrap(bytes).getDouble();
	}
	public static double toDouble(byte[] b, int s) {
	    return toDouble(Arrays.copyOfRange(b, s, s + 8));
	}
	public static boolean equals(byte[] b1, byte[] b2){
		if(b1.length != b2.length) return false;
		for(int i = 0; i < b1.length; i++){
			if(b1[i] != b2[i])
				return false;
		}
		return true;
	}
	
	public static int toInt(String s){
		try{
			return Integer.parseInt(s);
		}catch(NumberFormatException e){}
		return 0;
	}
	public static double toDouble(String s){
		try{
			return Double.parseDouble(s);
		}catch(NumberFormatException e){}
		return 0;
	}
	public static float toFloat(String s){
		try{
			return Float.parseFloat(s);
		}catch(NumberFormatException e){}
		return 0;
	}
}
