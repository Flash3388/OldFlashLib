package edu.flash3388.dashboard.controls;

import edu.flash3388.dashboard.Dashboard;
import edu.flash3388.dashboard.Displayble;
import edu.flash3388.flashlib.communications.SendableData;
import edu.flash3388.flashlib.util.FlashUtil;
import edu.flash3388.gui.FlashFxUtils;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class Chooser extends Displayble{

	private ComboBox<String> box;
	private VBox container = new VBox();
	private SimpleIntegerProperty selected = new SimpleIntegerProperty(-1);
	private boolean changed = false, manual = false;
	private SendableData data = new SendableData(){
		@Override
		public byte[] get() {
			if(!changed) return null;
			changed = false;
			return FlashUtil.toByteArray(selected.get());
		}
		@Override
		public boolean hasChanged() {
			return changed;
		}
		@Override
		public void onConnection() {}
		@Override
		public void onConnectionLost() {}
	};
	
	public Chooser(String name, int id) {
		super(name, id, Type.Chooser);
		
		box = new ComboBox<String>();
		container.getChildren().addAll(new Label(name), box);
		container.setSpacing(5);
		box.valueProperty().addListener(new ChangeListener<String>(){
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				synchronized (selected) {
					selected.set(box.getSelectionModel().getSelectedIndex());
					if(!manual)
						changed = true;
				}
			}
		});
	}

	@Override
	public void newData(byte[] bytes) {
		if(bytes[0] == 1){
			int sel = FlashUtil.toInt(bytes, 1);
			FlashFxUtils.onFxThread(()->{
				manual = true;
				box.getSelectionModel().select(sel);
				manual = false;
			});
			return;
		}
		String s = new String(bytes, 1, bytes.length - 1);
		System.out.println(s);
		String[] str = s.split(":");
		FlashFxUtils.onFxThread(()->{
			box.getItems().clear();
			box.getItems().addAll(str);
		});
	}
	@Override
	public SendableData dataForTransmition() {
		return data;
	}
	@Override
	protected Node getNode(){return container;}
	@Override
	public DisplayType getDisplayType(){
		return DisplayType.Manual;
	}
}
