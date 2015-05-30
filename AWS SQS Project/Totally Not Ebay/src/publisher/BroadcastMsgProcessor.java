package publisher;

import java.util.List;

import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.sun.media.jfxmedia.logging.Logger;

public class BroadcastMsgProcessor {
	private AuctionManager auctionManager;
	private SQSInformation sqsInformation;

	public BroadcastMsgProcessor(SQSInformation sqsInfo, AuctionManager auctionM){
		this.auctionManager = auctionM;
		this.sqsInformation = sqsInfo;		
	}
	
	private void processBroadcastMsg(Message m) {
		util.InternalMsg msg = new util.InternalMsg(m);
		if (msg.getCommand() == "INVALID"){
			return;
		}
		SimpleLogger.log("Process Broadcast Message " + msg.toString());
		int auctionID = Integer.parseInt(msg.getParams()[0]);

		switch (msg.getCommand()) {
		case "AUCTION_SCHEDULED":
			long auctionEnd = 0;
			try {
				auctionEnd = Long.parseLong(msg.getParams()[1]);
			}
			catch (Exception ex){
				ex.printStackTrace();
			}
			SimpleLogger.log("TempAuctionEnd: "+ auctionEnd  + " " + msg.getParams()[1] );
			auctionScheduled(auctionID, auctionEnd);
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

	private void auctionScheduled(int auctID, long auctionEnd) {
		if (auctionManager.hasAuctionWithID(auctID) == false){
		auctionManager.addAuction(new Auction(auctID, auctionEnd));
		SimpleLogger.log("Created new auction. ID: " + auctID);		
		}
		else{
			SimpleLogger.log("Auction not created - already exists. ID: " + auctID);
		}
	}

	private void newHighestBidder(int auctID, double bid, int bidderID) {
		Auction updateAuction = auctionManager.getAuctionByID(auctID);
		if (updateAuction != null) {
			if (bid > updateAuction.getHighestBid()) {
				updateAuction.setHighestBid(bid);
				updateAuction.setHighestBidder(bidderID);
				for (Subscriber s : updateAuction.getSubscriberList()) {
					String msg = updateAuction.getAuctionHighestBidderMsg();
					sqsInformation.getSqs().sendMessage(new SendMessageRequest(s
							.getSubscriberQueueUrl(), msg));
					//SimpleLogger.log(s.getSubscriberQueueUrl());
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
		Auction endAuction = auctionManager.getAuctionByID(auctID);
		if (endAuction != null) {
			for (Subscriber s : endAuction.getSubscriberList()) {
				String msg = endAuction.getAuctionEndMessage();
				sqsInformation.getSqs().sendMessage(new SendMessageRequest(s
						.getSubscriberQueueUrl(), msg));
				//SimpleLogger.log(s.getSubscriberQueueUrl());
				SimpleLogger.log("Sent Message: " + msg + " to ClientdID "
						+ s.getSubscriberID());
			}
			auctionManager.removeAuction(endAuction);
		}
	}

	/**
	 * fetches Msgs and starts the processing
	 */
	public void fetchBroadcastMsgs() {
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(
				sqsInformation.getReceiveBroadcastQueueUrl());
		//SimpleLogger.log(sqsInformation.getReceiveBroadcastQueueUrl());
		List<Message> messages = sqsInformation.getSqs().receiveMessage(receiveMessageRequest)
				.getMessages();
		for (Message message : messages) {
			processBroadcastMsg(message);
			String messageRecieptHandle = message.getReceiptHandle();
			sqsInformation.getSqs().deleteMessage(new DeleteMessageRequest(
					sqsInformation.getReceiveBroadcastQueueUrl(),
					messageRecieptHandle));
		}
	}
}
