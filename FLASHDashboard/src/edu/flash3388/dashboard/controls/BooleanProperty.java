package edu.flash3388.dashboard.controls;


import edu.flash3388.dashboard.Displayble;
import edu.flash3388.flashlib.communications.SendableData;
import edu.flash3388.flashlib.util.Log;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class BooleanProperty extends Displayble{

	private static final int LABEL_WIDTH = 150;
	private static final int LABEL_HEIGHT = 10;
	
	private boolean value = false;
	private boolean changed = false;
	
	private Label label;
	private VBox node;
	private Runnable updater;
	
	public BooleanProperty(String name, int id) {
		super(name, id, Type.Boolean);
		node = new VBox();
		label = new Label(name + ": " + value);
		label.setTextFill(Color.RED);
		label.setPrefSize(LABEL_WIDTH, LABEL_HEIGHT);
		node.getChildren().add(label);
		
		updater = new Runnable(){
			@Override
			public void run() {
				label.setText(name + ": " + value);
				if(changed){
					changed = false;
					label.setTextFill(value? Color.GREEN : Color.RED);
				}
			}
		};
	}

	@Override
	public void newData(byte[] bytes) {
		synchronized(this){
			value = bytes[0] == 1;
			changed = true;
		}
	}
	@Override
	public SendableData dataForTransmition() {
		return null;
	}
	@Override
	protected Node getNode(){return node;}
	@Override
	public Runnable updateDisplay() {
		return updater;
	}

}
