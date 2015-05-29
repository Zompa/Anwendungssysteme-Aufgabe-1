package publisher;

import java.util.Random;

import com.amazonaws.services.sqs.model.SendMessageRequest;

// TODO Hat mit Skalierbarkeit ja mal gar nix zu tun 
public class Publisher {
	public static AuctionManager auctionManager;
	

	public static void main(String[] args) {
		SQSInformation.initialize();
		auctionManager = new AuctionManager();
		try {
			test1();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	
	private static void test1() throws InterruptedException{
		SubscribtionMsgProcesser smb = new SubscribtionMsgProcesser();
		BroadcastMsgProcessor bmp = new BroadcastMsgProcessor();
		
		SQSInformation.sqs.sendMessage(new SendMessageRequest(SQSInformation.ReceiveBroadcastQueueUrl, "AUCTION_SCHEDULED/666/STARTZEITPUNKT/ENDZEITPUNKT/AUCTION_NAME)"));

		SQSInformation.sqs.sendMessage(new SendMessageRequest(SQSInformation.SubscribtionQueueUrl, "SUBSCRIBE/23/666"));

		SQSInformation.sqs.sendMessage(new SendMessageRequest(SQSInformation.ReceiveBroadcastQueueUrl, "NEW_HIGHEST_BIDDER/666/42.2/12"));
		//SQSInformation.sqs.sendMessage(new SendMessageRequest(SQSInformation.ReceiveBroadcastQueueUrl, "STRANGEMESSAGE"));
		
		Random rand = new Random();		
		while (true){
			smb.fetchSubscriptionMsgs();
			bmp.fetchBroadcastMsgs();
			SQSInformation.sqs.sendMessage(new SendMessageRequest(SQSInformation.SubscribtionQueueUrl, "SUBSCRIBE/"+rand.nextInt() +"/666"));

			SQSInformation.sqs.sendMessage(new SendMessageRequest(SQSInformation.ReceiveBroadcastQueueUrl, "NEW_HIGHEST_BIDDER/666/"+rand.nextDouble() +"/12"));
			Thread.sleep(1000);
		}
		
		
	}
	/*
	private static void test2(){
		SQSInformation.sqs.sendMessage(new SendMessageRequest(SQSInformation.SubscribtionQueueUrl, "SUBSCRIBE/12/23"));
		SQSInformation.sqs.sendMessage(new SendMessageRequest(SQSInformation.SubscribtionQueueUrl, "SUBSCRIBE/23/34"));
		
		SubscribtionMsgProcesser smb = new SubscribtionMsgProcesser();
		smb.fetchSubscriptionMsgs();
	}
	*/
}
