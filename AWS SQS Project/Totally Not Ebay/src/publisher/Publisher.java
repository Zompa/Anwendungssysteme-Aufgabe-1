package publisher;

import com.amazonaws.services.sqs.model.SendMessageRequest;

// TODO Hat mit Skalierbarkeit ja mal gar nix zu tun 
public class Publisher {
	public static AuctionManager auctionManager;
	

	public static void main(String[] args) {
		SQSInformation.initialize();
		auctionManager = new AuctionManager();
		test1();
	}

	
	private static void test1(){
		SQSInformation.sqs.sendMessage(new SendMessageRequest(SQSInformation.SubscribtionQueueUrl, "SUBSCRIBE/12/23"));
		SQSInformation.sqs.sendMessage(new SendMessageRequest(SQSInformation.SubscribtionQueueUrl, "SUBSCRIBE/23/34"));
		
		SubscribtionMsgProcesser smb = new SubscribtionMsgProcesser();
		smb.fetchSubscriptionMsgs();
	}
}
