package util;
import com.amazonaws.services.sqs.model.Message;

public class InternalMsg {

	private String command;
	private String[] params;
	
	public InternalMsg(Message m){
		String[] msgParts = SimpleParser.getMessageAttributes(m);
		
		
		this.command = msgParts[0].toUpperCase();
		this.params = new String[msgParts.length-1];
		for (int i=0;i< this.params.length;i++){
			params[i] = msgParts[i+1].toUpperCase();
		}
		//TODO entfernen
		System.out.println(this.toString());
	}

	public String getCommand() {
		return command;
	}

	public String[] getParams() {
		return params;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(command);
		sb.append(":");
		for (int i=0;i< this.params.length;i++){
			sb.append(params[i]);
			sb.append(",");
		}
		return sb.toString();
	}
}
