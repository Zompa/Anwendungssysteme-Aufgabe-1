package publisher;

import java.util.List;

import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class SubscribtionMsgProcesser {
	
	private AuctionManager auctionManager;
	private SQSInformation sqsInformation;

	public SubscribtionMsgProcesser(SQSInformation sqsInfo, AuctionManager auctionM){
		this.auctionManager = auctionM;
		this.sqsInformation = sqsInfo;		
	}

	private void processSubscribe(int subscriberID, int auctionID) {
		Auction subAuction = auctionManager.getAuctionByID(auctionID);
		if (subAuction != null) {
			Subscriber sub = new Subscriber(subscriberID, sqsInformation);
			subAuction.addSubscriber(sub);
			SimpleLogger.log("Client " + subscriberID + " subscribed to auction " + auctionID );
			sendAuctionStatus(subAuction, sub);
		} else {
			SimpleLogger.log("Subscription not possible, auction not found. Auction ID: " + auctionID );
		}
	}

	private void processUnSubscribe(int subscriberID, int auctionID) {
		Auction subAuction = auctionManager.getAuctionByID(auctionID);
		if (subAuction != null) {
			Subscriber remSubscriber = subAuction
					.getSubscriberByID(subscriberID);
			if (remSubscriber != null) {
				subAuction.removeSubscriber(remSubscriber);
			} else {
				SimpleLogger.log("Client " + subscriberID + " unsubscribed from  auction " + auctionID );
			}
		} else {
			SimpleLogger.log("Unsubscription not possible, auction not found. Auction ID: " + auctionID );
		}
	}

	/**
	 * sends information about the auction to a Subscriber
	 * 
	 * @param auct
	 *            Auction the Msg is about
	 * @param recSubscriber
	 *            receiving Subscriber
	 */
	private void sendAuctionStatus(Auction auct, Subscriber recSubscriber) {
		sqsInformation.getSqs().sendMessage(new SendMessageRequest(recSubscriber
				.getSubscriberQueueUrl(), auct.getAuctionHighestBidderMsg()));
	}

	private void processSubscriptionMsg(Message m) {
		util.InternalMsg msg = new util.InternalMsg(m);		
		if (msg.getCommand() == "INVALID"){
			return;
		}
		int subscriberID = Integer.parseInt(msg.getParams()[0]);
		int auctionID = Integer.parseInt(msg.getParams()[1]);		

		SimpleLogger.log("Process Subscription Message " + msg.toString());
		switch (msg.getCommand()) {
		case "SUBSCRIBE":
			processSubscribe(subscriberID, auctionID);
			break;
		case "UNSUBSCRIBE":
			processUnSubscribe(subscriberID, auctionID);
			break;
		default:
			SimpleLogger.log("Subscription Message could not be processed. Msg: " + msg.toString());			
		}
	}

	/**
	 * fetches Msgs and starts the processing
	 */
	public void fetchSubscriptionMsgs() {
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(
				sqsInformation.getSubscribtionQueueUrl());
		//SimpleLogger.log(Publisher.sqsInformation.getSubscribtionQueueUrl());
		List<Message> messages = sqsInformation.getSqs().receiveMessage(receiveMessageRequest)
				.getMessages();
		for (Message message : messages) {
			processSubscriptionMsg(message);
			String messageRecieptHandle = message.getReceiptHandle();
			sqsInformation.getSqs().deleteMessage(new DeleteMessageRequest(
					sqsInformation.getSubscribtionQueueUrl(), messageRecieptHandle));
		}
	}
}
