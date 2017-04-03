package edu.flash3388.dashboard.controls;

import edu.flash3388.dashboard.Displayble;
import edu.flash3388.flashlib.communications.SendableData;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

public class Button extends Displayble{

	private static final int WIDTH = 100;
	private static final int HEIGHT = 30;
	
	private javafx.scene.control.Button button;
	private HBox node;
	private byte[] bytes = new byte[1], press = {1};
	private boolean disabled = false;
	private Runnable updater;
	private SendableData data = new SendableData(){
		@Override
		public byte[] get() {
			if(bytes[0] == 1){
				bytes[0] = 0;
				return press;
			}
			return null;
		}
		@Override
		public boolean hasChanged() {
			return bytes[0] == 1;
		}
		@Override
		public void onConnection() {}
		@Override
		public void onConnectionLost() {}
	};
	
	public Button(String name, int id) {
		super(name, id, Type.Activatable);
		node = new HBox();
		node.setAlignment(Pos.TOP_CENTER);
		
		button = new javafx.scene.control.Button(name);
		button.setPrefSize(WIDTH, HEIGHT);
		button.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				press();
			}
		});
		node.getChildren().add(button);
		button.setAlignment(Pos.TOP_CENTER);
		
		updater = new Runnable(){
			@Override
			public void run() {
				if(disabled && !button.isDisabled())
					button.setDisable(true);
				else if(!disabled && button.isDisabled())
					button.setDisable(false);
			}
		};
	}

	private void press(){
		bytes[0] = 1;
		disabled = true;
	}
	@Override
	public void newData(byte[] bytes) {
		if(bytes[0] == 1){
			disabled = false;
		}
	}
	@Override
	public SendableData dataForTransmition() {
		return data;
	}
	@Override
	protected Node getNode(){return node;}
	@Override
	public Runnable updateDisplay() {
		return updater;
	}
	@Override
	public DisplayType getDisplayType(){
		return DisplayType.Manual;
	}
}
