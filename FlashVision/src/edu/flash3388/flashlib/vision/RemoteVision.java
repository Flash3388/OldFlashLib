package edu.flash3388.flashlib.vision;

import edu.flash3388.flashlib.communications.Sendable;
import edu.flash3388.flashlib.communications.SendableData;
import edu.flash3388.flashlib.util.FlashUtil;

public class RemoteVision extends Sendable implements Vision{

	private ProcessingParam param;
	private Analysis analysis;
	private VisionSendableData data;
	private boolean send = false, newAnalysis = false, stopping = false;
	private double camOffset, targetHeight, targetWidth;
	private long lastRec;
	
	public RemoteVision(boolean remote, double camOffset) {
		super(Type.Vision);
		data = new VisionSendableData(this);
		enableRemoteParameters(remote);
		this.camOffset = camOffset;
	}
	public RemoteVision(boolean remote){
		this(remote, 0);
	}
	public RemoteVision(){
		this(false);
	}

	@Override
	public void start(){
		if(param == null && data.isLocalParam())
			enableRemoteParameters(true);
		data.startRemote();
		send = true;
		lastRec = FlashUtil.millis();
	}
	@Override
	public void stop(){
		data.stopRemote();
		stopping = true;
	}
	public boolean isRunning(){
		return send;
	}
	public void enableRemoteParameters(boolean enable){
		if(enable && data.isLocalParam())
			data.setRemoteParam();
		else if(!enable && !data.isLocalParam())
			data.setRoborioParam();
	}
	@Override
	public void setParameters(ProcessingParam param){
		enableRemoteParameters(false);
		this.param = param;
		data.setParam(param);
		FlashUtil.getLog().log("Vision Prameters are set!!");
	}
	@Override
	public ProcessingParam getParameters(){
		return param;
	}
	@Override
	public Analysis getAnalysis(){
		newAnalysis = false;
		return analysis;
	}
	@Override
	public boolean hasNewAnalysis(){
		return newAnalysis;
	}
	
	@Override
	public void newData(byte[] data) {
		if(!send) return;
		if(stopping){
			stopping = false;
			send = false;
			return;
		}
		Analysis an = Analysis.fromBytes(data);
		
		if(an != null) {
			long t = FlashUtil.millis() - lastRec;
			if(t > 1000)
				FlashUtil.getLog().log("New analysis: "+t);
			lastRec = FlashUtil.millis();
			analysis = an;
			newAnalysis = true;
		}
	}
	@Override
	public SendableData dataForTransmition() {
		if(!send) return null;
		return data;
	}

	@Override
	public void setCameraOffsetAngle(double angle) {
		camOffset = angle;
	}
	@Override
	public double getCameraOffsetAngle() {
		return camOffset;
	}
	@Override
	public void setTargetHeight(double h) {
		targetHeight = h;
	}
	@Override
	public double getTargetHeight() {
		return targetHeight;
	}
	@Override
	public void setTargetWidth(double w) {
		targetWidth = w;
	}
	@Override
	public double getTargetWidth() {
		return targetWidth;
	}
}
