package util;

import com.amazonaws.services.sqs.model.Message;

/**
 * Splits a message into its parts. 
 * @author Daniel
 *
 */
public class SimpleParser {

	public static String MESSAGE_SEPERATOR_SIGN = "/";
	
	public static String[] getMessageAttributes(Message m) {
		return m.getBody().split(MESSAGE_SEPERATOR_SIGN);
	}
}
