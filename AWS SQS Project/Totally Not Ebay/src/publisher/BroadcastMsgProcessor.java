package publisher;

import java.util.List;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class BroadcastMsgProcessor {
	private AmazonSQS sqs = SQSInformation.sqs;

	private void processBroadcastMsg(Message m) {
		util.InternalMsg msg = new util.InternalMsg(m);
		SimpleLogger.log("Process Broadcast Message " + msg.toString());
		int auctionID = Integer.parseInt(msg.getParams()[0]);

		// TODO nicht verarbeitbare Nachrichten verkraften
		switch (msg.getCommand()) {
		case "AUCTION_SCHEDULED":
			auctionScheduled(auctionID);
			break;
		case "AUCTION_STARTED":
			// No need for processing
			break;
		case "NEW_HIGHEST_BIDDER":
			double bid = Double.parseDouble(msg.getParams()[1]);
			int bidderID = Integer.parseInt(msg.getParams()[2]);
			newHighestBidder(auctionID, bid, bidderID);
			break;
		case "AUCTION_END":
			int winnerID = Integer.parseInt(msg.getParams()[1]);
			double winningBid = Double.parseDouble(msg.getParams()[2]);
			auctionEnd(auctionID, winnerID, winningBid);
			break;
		default:
			SimpleLogger.log("Broadcast Message could not be processed. Msg: " + msg.toString());			
		}
	}

	private void auctionScheduled(int auctID) {
		// nicht tun wenn bereits vorhanden
		Publisher.auctionManager.addAuction(new Auction(auctID));
		SimpleLogger.log("Created new auction. ID: " + auctID);		
	}

	private void newHighestBidder(int auctID, double bid, int bidderID) {
		Auction updateAuction = Publisher.auctionManager.getAuctionByID(auctID);
		if (updateAuction != null) {
			if (bid > updateAuction.getHighestBid()) {
				updateAuction.setHighestBid(bid);
				updateAuction.setHighestBidder(bidderID);
				for (Subscriber s : updateAuction.getSubscriberList()) {
					String msg = updateAuction.getAuctionHighestBidderMsg();
					sqs.sendMessage(new SendMessageRequest(s
							.getSubscriberQueueUrl(), msg));
					SimpleLogger.log("Sent Message: " + msg + " to ClientdID "
							+ s.getSubscriberID());
				}
			} else {
				SimpleLogger
						.log("Update highest bidder not posible: bid lower than before");
			}
		}
		else{
			SimpleLogger.log("Update highest bidder not posible: Auction does not exist. ID:" + auctID);
		}
		
	}

	private void auctionEnd(int auctID, int winnerID, double winningBid) {

	}

	/**
	 * fetches Msgs and starts the processing
	 */
	public void fetchBroadcastMsgs() {
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(
				SQSInformation.ReceiveBroadcastQueueUrl);
		List<Message> messages = sqs.receiveMessage(receiveMessageRequest)
				.getMessages();
		for (Message message : messages) {
			processBroadcastMsg(message);
			String messageRecieptHandle = message.getReceiptHandle();
			sqs.deleteMessage(new DeleteMessageRequest(
					SQSInformation.ReceiveBroadcastQueueUrl,
					messageRecieptHandle));
		}
	}
}