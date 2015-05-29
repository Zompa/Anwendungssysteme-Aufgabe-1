package publisher;

// TODO Hat mit Skalierbarkeit ja mal gar nix zu tun 
public class Publisher extends Thread{
	private AuctionManager auctionManager;
	private SQSInformation sqsInformation;
	private SubscribtionMsgProcesser smb;
	private BroadcastMsgProcessor bmp;
	

	public Publisher(int myID) {
		sqsInformation = new SQSInformation(myID);
		auctionManager = new AuctionManager();
		smb = new SubscribtionMsgProcesser(sqsInformation, auctionManager);
		bmp = new BroadcastMsgProcessor(sqsInformation, auctionManager);
		SimpleLogger.log("Publisher Created. ID: " + myID);
	}

	public void run(){
		while (!isInterrupted()){
		this.smb.fetchSubscriptionMsgs();
		this.bmp.fetchBroadcastMsgs();
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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
