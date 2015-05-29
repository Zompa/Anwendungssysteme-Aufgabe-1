package util;

public class MessageFormatException extends Exception{

	public MessageFormatException(String msg){
		super("Nachricht entspricht nicht dem geforderten Format" + msg);
	}
}
