package publisher;

import java.util.Random;

import com.amazonaws.services.sqs.model.SendMessageRequest;

// TODO Hat mit Skalierbarkeit ja mal gar nix zu tun 
public class Publisher extends Thread{
	public static AuctionManager auctionManager;
	public static SQSInformation sqsInformation;
	private SubscribtionMsgProcesser smb;
	private BroadcastMsgProcessor bmp;
	

	public Publisher(int myID) {
		sqsInformation = new SQSInformation(myID);
		auctionManager = new AuctionManager();
		smb = new SubscribtionMsgProcesser();
		bmp = new BroadcastMsgProcessor();
		SimpleLogger.log("Publisher Created. ID: " + myID);
	}

	public void run(){
		while (!isInterrupted()){
		this.smb.fetchSubscriptionMsgs();
		this.bmp.fetchBroadcastMsgs();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		}
	}	

	public static AuctionManager getAuctionManager() {
		return auctionManager;
	}

	public static SQSInformation getSqsInformation() {
		return sqsInformation;
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
