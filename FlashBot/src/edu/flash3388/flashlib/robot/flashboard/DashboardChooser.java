package edu.flash3388.flashlib.robot.flashboard;

import java.util.Enumeration;
import java.util.Vector;

import edu.flash3388.flashlib.communications.Sendable;
import edu.flash3388.flashlib.communications.SendableData;
import edu.flash3388.flashlib.flashboard.FlashboardSendableType;
import edu.flash3388.flashlib.util.FlashUtil;

public class DashboardChooser<T> extends Sendable{

	public static class Option<T>{
		private String name;
		private T option;
		
		public Option(String name, T option){
			this.name = name;
			this.option = option;
		}
		
		public String getName(){
			return name;
		}
		public T getOption(){
			return option;
		}
	}
	@SuppressWarnings({"rawtypes", "unchecked"}) 
	private static class ChooserData implements SendableData{

		private DashboardChooser chooser;
		boolean changed = false, changedIndex = false;
		
		public ChooserData(DashboardChooser chooser){
			this.chooser = chooser;
		}
		@Override
		public byte[] get() {
			if(changedIndex && !changed){
				changedIndex = false;
				byte[] bytes = {1, 0, 0, 0, 0};
				FlashUtil.fillByteArray(chooser.selected, 1, bytes);
				return bytes;
			}
			changed = false;
			if(!changedIndex) changedIndex = true;
			String all = "";
			for(Enumeration<Option> opEnum = chooser.options.elements(); opEnum.hasMoreElements();)
				all += opEnum.nextElement().name + ":";
			all = all.substring(0, all.length() - 1);
			byte[] bytes = new byte[all.length() + 1];
			bytes[0] = 2;
			System.arraycopy(all.getBytes(), 0, bytes, 1, all.length());
			return bytes;
		}
		@Override
		public boolean hasChanged() {
			return changed || changedIndex;
		}
		@Override
		public void onConnection() {
			changed = true;
			chooser.selected = chooser.defaultIndex >= 0 ? chooser.defaultIndex : 0;
		}
		@Override
		public void onConnectionLost() {
			changed = false;
		}
	}
	
	private Vector<Option<T>> options = new Vector<Option<T>>();
	private int selected = 0, defaultIndex = -1;
	private ChooserData data;
	
	@SafeVarargs
	public DashboardChooser(String name, Option<T>...options) {
		super(name, FlashboardSendableType.CHOOSER);
		
		if(options != null){
			for(Option<T> o : options)
				this.options.addElement(o);
		}
		
		data = new ChooserData(this);
	}
	public DashboardChooser(String name) {
		this(name, (Option<T>[])null);
	}
	
	public DashboardChooser<T> addDefault(Option<T> option){
		select(options.size());
		defaultIndex = selected;
		this.options.addElement(option);
		data.changed = true;
		return this;
	}
	public DashboardChooser<T> addDefault(String name, T option){
		return this.addOption(new Option<T>(name, option));
	}
	public DashboardChooser<T> addOption(Option<T> option){
		this.options.addElement(option);
		data.changed = true;
		return this;
	}
	public DashboardChooser<T> addOption(String name, T option){
		return this.addOption(new Option<T>(name, option));
	}
	public DashboardChooser<T> remove(int index){
		if(index < 0) 
			throw new IllegalArgumentException("Index must be non-negative");
		options.remove(index);
		data.changed = true;
		return this;
	}
	public DashboardChooser<T> removeLast(){
		return remove(options.size()-1);
	}
	public T getSelected(){
		return selected >= 0 && selected < options.size() && options.size() > 0 ? options.get(selected).option : null;
	}
	public int getSelectedIndex(){
		return selected >= 0 && selected < options.size() && options.size() > 0 ? selected : -1;
	}
	public void select(int index){
		selected = index;
		data.changedIndex = true;
	}

	@Override
	public void newData(byte[] data) {
		if(data.length < 4) return;
		int sel = FlashUtil.toInt(data);
		if(sel >= 0 && sel < options.size() && sel != selected)
			selected = sel;
	}
	@Override
	public SendableData dataForTransmition() {
		return options.size() > 0 ? data : null;
	}
}
