package edu.flash3388.dashboard.controls;

import edu.flash3388.dashboard.Displayble;
import edu.flash3388.flashlib.communications.SendableData;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class InputField extends Displayble{

	private static final int LABEL_WIDTH = 50;
	private static final int LABEL_HEIGHT = 10;
	
	private String value = "";
	private boolean changed = false;
	
	private Label label;
	private TextField field;
	private javafx.scene.control.Button button;
	private VBox node;
	private SendableData data = new SendableData(){
		@Override
		public byte[] get() {
			if(changed){
				changed = false;
				return value.getBytes();
			}
			return null;
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
	
	public InputField(String name, int id) {
		super(name, id, Type.String);
		
		node = new VBox();
		label = new Label(name);
		field = new TextField();
		field.setPrefSize(LABEL_WIDTH, LABEL_HEIGHT);
		button = new javafx.scene.control.Button();
		button.setPrefSize(LABEL_WIDTH / 3, LABEL_HEIGHT);
		HBox d = new HBox();
		d.getChildren().addAll(field, button);
		node.getChildren().addAll(label, d);
		
		button.setOnAction((ActionEvent e)->{
			value = field.getText();
			changed = true;
		});
		field.setOnKeyPressed((KeyEvent e)->{
			if(e.getCode() == KeyCode.ENTER){
				value = field.getText();
				changed = true;
			}
		});
	}

	@Override
	public void newData(byte[] bytes) {
	}
	@Override
	public SendableData dataForTransmition() {
		return data;
	}

	@Override
	protected Node getNode(){return node;}
	@Override
	public DisplayType getDisplayType(){
		return DisplayType.Manual;
	}
}
