package publisher;

import java.util.List;
import java.util.Map.Entry;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class SubscribtionMsgProcesser {
	private AmazonSQS sqs = SQSInformation.sqs;

	private void processSubscribe(int subscriberID, int auctionID) {
		Auction subAuction = Publisher.auctionManager.getAuctionByID(auctionID);
		if (subAuction != null) {
			Subscriber sub = new Subscriber(subscriberID);
			subAuction.addSubscriber(sub);
			SimpleLogger.log("Client " + subscriberID + " subscribed to auction " + auctionID );
			sendAuctionStatus(subAuction, sub);
		} else {
			SimpleLogger.log("Subscription not possible, auction not found. Auction ID: " + auctionID );
		}
	}

	private void processUnSubscribe(int subscriberID, int auctionID) {
		Auction subAuction = Publisher.auctionManager.getAuctionByID(auctionID);
		if (subAuction != null) {
			Subscriber remSubscriber = subAuction
					.getSubscriberByID(subscriberID);
			if (remSubscriber != null) {
				subAuction.removeSubscriber(remSubscriber);
			} else {
				SimpleLogger.log("Client " + subscriberID + " unsibscribed from  auction " + auctionID );
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
		sqs.sendMessage(new SendMessageRequest(recSubscriber
				.getSubscriberQueueUrl(), auct.getAuctionHighestBidderMsg()));
	}

	private void processSubscriptionMsg(Message m) {
		util.InternalMsg msg = new util.InternalMsg(m);		
		int subscriberID = Integer.parseInt(msg.getParams()[0]);
		int auctionID = Integer.parseInt(msg.getParams()[1]);		

		SimpleLogger.log("Process Subscription Message " + msg.toString());
		//TODO nicht verarbeitbare Nachrichten verkraften
		switch (msg.getCommand()) {
		case "SUBSCRIBE":
			processSubscribe(subscriberID, auctionID);
			break;
		case "UNSUBSCRIBE":
			processUnSubscribe(subscriberID, auctionID);
			break;
		}
	}

	/**
	 * fetches Msgs and starts the processing
	 */
	public void fetchSubscriptionMsgs() {
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(
				SQSInformation.SubscribtionQueueUrl);
		List<Message> messages = sqs.receiveMessage(receiveMessageRequest)
				.getMessages();
		for (Message message : messages) {
			processSubscriptionMsg(message);
			String messageRecieptHandle = message.getReceiptHandle();
			sqs.deleteMessage(new DeleteMessageRequest(
					SQSInformation.SubscribtionQueueUrl, messageRecieptHandle));
		}
	}
}
