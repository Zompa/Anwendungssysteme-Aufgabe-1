package publisher;
/**
 * Publisher Thread. Needs to be started by the Balancer, which distributes the Messages
 * @author Paul
 *
 */
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

}
