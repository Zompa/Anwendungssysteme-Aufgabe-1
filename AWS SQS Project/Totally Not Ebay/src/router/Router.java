package router;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import util.SimpleParser;
import auctionService.AuctionService;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

/**
 * The purpose of this class is distributing client messages to matching auction
 * queues. Auction IDs are stored to validate client messages. This storage will
 * be updated as auctions start or terminate.
 * 
 * @author christianhoffmann
 * 
 */
public class Router extends Thread {
	public static final String ROUTER_QUEUE_NAME = "Bid_Queue";
	private final static Logger LOGGER = Logger.getLogger(Router.class
			.getName());
	private final AmazonSQS sqs;
	private final String bidQueueURL;
	
	private final ReceiveMessageRequest receiveMessageRequest;
	
	private final RouterManager manager;

	public Router(AmazonSQS sqs, RouterManager manager) {
		this.sqs = sqs;
		this.manager = manager;
		// initialize the router's bidQueue in which clients can drop messages
		CreateQueueRequest bidQueueRequest = new CreateQueueRequest(
				ROUTER_QUEUE_NAME);
		bidQueueURL = sqs.createQueue(bidQueueRequest).getQueueUrl();
		LOGGER.info("Bid_Queue initialized");
		
		this.receiveMessageRequest = new ReceiveMessageRequest(
				bidQueueURL).withMaxNumberOfMessages(10).withWaitTimeSeconds(20);
	}

	@Override
	public void run() {
		while (!isInterrupted()) {
			sendBidsToAuctionManager(receiveMessagesfromClient());
		}
	}

	/**
	 * Reads all kinds of messages sent by clients.
	 * 
	 * @return a list containing valid and registered auctionIDs extracted from
	 *         client messages
	 */
	public List<Message> receiveMessagesfromClient() {
		// LOGGER.info("Receiving messages from clients.\n");
		
		//Poll messages from server. Long poll. Waits 20 seconds
		List<Message> receivedClientMessages = sqs.receiveMessage(
				receiveMessageRequest).getMessages();
		return receivedClientMessages;
	}

	/**
	 * Sends client bids to AuctionManager using the matching
	 * Auction_Queue_<i>auctionID</i>
	 * 
	 * @param destinationBidList
	 *            bid list from clients, filled in
	 *            {@link #receiveMessagesfromClient()
	 *            receiveMessagefromClient()}
	 */
	public void sendBidsToAuctionManager(List<Message> destinationBidList) {
		// LOGGER.info("Sending messages to AuctionManager.\n");
		for (Message nextMessage : destinationBidList) {
			try {

				String[] messageAttributes = SimpleParser
						.getMessageAttributes(nextMessage);
				String queueURLfromRouterToAuctionManager = manager.getQueueURLForID(Integer.parseInt(messageAttributes[1]));
				System.out.println(queueURLfromRouterToAuctionManager);
				sqs.sendMessage(new SendMessageRequest(
						queueURLfromRouterToAuctionManager, nextMessage.getBody()));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
	            String messageRecieptHandle = nextMessage.getReceiptHandle();
	            sqs.deleteMessage(new DeleteMessageRequest(ROUTER_QUEUE_NAME, messageRecieptHandle));
			}
		}
	}

	/**
	 * Checks if a String only consists of an Integer value
	 * 
	 * @param s
	 *            input string
	 * @return true if input String only consists of an Integer value
	 */
	public static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		} catch (NullPointerException e) {
			return false;
		}
		return true;
	}
}
