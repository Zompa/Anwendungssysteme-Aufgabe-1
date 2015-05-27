package auctionService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import util.SimpleParser;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

/** The purpose of this class is distributing client messages to matching auction queues.
 *  Auction IDs are stored to validate client messages.
 *  This storage will be updated as auctions start or terminate.
 * 
 * @author christianhoffmann
 *
 */
public class Router extends Thread {
	public static final String ROUTER_QUEUE_NAME = "Bid_Queue";
	private final static Logger LOGGER = Logger.getLogger(Router.class.getName());
	private final AmazonSQS sqs;
	public static String bidQueueURL;

	/** contains auctionIDs of all registered auctions
	 * 
	 */
	private HashSet<Integer> auctionIDSet = new HashSet<>();

	/** contains valid bid messages for AuctionManager
	 * 
	 */
	private List<Message> destinationBidList = new ArrayList<>();


	public Router(AmazonSQS sqs) {
		this.sqs = sqs;

		// initialize the router's bidQueue in which clients can drop messages 
		CreateQueueRequest bidQueueRequest = new CreateQueueRequest(ROUTER_QUEUE_NAME);
		bidQueueURL = sqs.createQueue(bidQueueRequest).getQueueUrl();
		LOGGER.info("Bid_Queue initialized");
	}

	@Override
	public void run() {
		while(!isInterrupted()) {
			receiveMessagesfromAuctionManager();
			destinationBidList = receiveMessagesfromClient();
			sendBidsToAuctionManager(destinationBidList);
		}	
	}

	/** Reads messages sent by AuctionManager concerning occurred changes (start of auction, end of auction).
	 * 	Stores new Auction_IDs to auctionIDSet when new auctions started.
	 * 	Removes Auction_IDs from auctionIDSet when an auction terminated.
	 * 
	 */
	public void receiveMessagesfromAuctionManager() {
		String queueURLfromAuctionManagerToRouter = AuctionService.getSendQueueURL();
		//LOGGER.info("Receiving messages from AuctionManager.\n");
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueURLfromAuctionManagerToRouter);
		List<Message> receivedChangesfromAuctionManager = sqs.receiveMessage(receiveMessageRequest).getMessages();

		for(Message receivedChangeMessage : receivedChangesfromAuctionManager) {
			try {
				String[] receivedChangeMessageString = SimpleParser.getMessageAttributes(receivedChangeMessage);
				if(receivedChangeMessageString[0].equals("AUCTION_STARTED")) {
					auctionIDSet.add(Integer.parseInt(receivedChangeMessageString[1]));
					LOGGER.info("Registered new auction");
				}	
				else if(receivedChangeMessageString[0].equals("AUCTION_END")) {
					auctionIDSet.remove(Integer.parseInt(receivedChangeMessageString[1]));
					LOGGER.info("Deleted auction");
				} else LOGGER.info("No auctions updated");
					
			} catch (Exception e) {
				LOGGER.info("An error occurred while updating the Auction_ID storage");
				e.printStackTrace();
			} finally {
				//delete message from queue
				String receiptHandle = receivedChangeMessage.getReceiptHandle();
				sqs.deleteMessage(new DeleteMessageRequest(queueURLfromAuctionManagerToRouter, receiptHandle));
				//LOGGER.info("Deleted Message from queue");
			}
		}
	}

	/** Reads all kinds of messages sent by clients.
	 * 
	 * @return a list containing valid and registered auctionIDs extracted from client messages
	 */
	public List<Message> receiveMessagesfromClient() {
		//LOGGER.info("Receiving messages from clients.\n");
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(bidQueueURL);
		List<Message> receivedClientMessages = sqs.receiveMessage(receiveMessageRequest).getMessages();

		for(Message clientMessage : receivedClientMessages) {
			try {
				String[] messageAttributes = SimpleParser.getMessageAttributes(clientMessage);
				//check if sent message contains an auctionID already contained in auctionIDSet
				//System.out.println(Integer.parseInt(messageAttributes[1]));		
				if(messageAttributes[0].equals("MAKE_BID")
					&& isInteger(messageAttributes[1]) 
					&& auctionIDSet.contains(Integer.parseInt(messageAttributes[1]))
				) destinationBidList.add(clientMessage); 
				else LOGGER.info("This client message does not include a valid auctionID");
			} catch (Exception e) {
				LOGGER.info("An error occurred while reading client messages");
				e.printStackTrace();
			} finally {
				//delete message from Bid_Queue
				String receiptHandle = clientMessage.getReceiptHandle();
				sqs.deleteMessage(new DeleteMessageRequest(bidQueueURL, receiptHandle));
			}
		} return destinationBidList;
	}


	/** Sends client bids to AuctionManager using the matching Auction_Queue_<i>auctionID</i>
	 * 
	 * @param destinationBidList bid list from clients, filled in {@link #receiveMessagesfromClient() receiveMessagefromClient()}
	 */
	public void sendBidsToAuctionManager(List<Message> destinationBidList) {
		//LOGGER.info("Sending messages to AuctionManager.\n");
		Iterator<Message> listIterator = destinationBidList.iterator();
		while(listIterator.hasNext()) {
			Message nextMessage = listIterator.next();
			String[] messageAttributes = SimpleParser.getMessageAttributes(nextMessage);
			String queueURLfromRouterToAuctionManager = "Auction_Queue_" + messageAttributes[1];
			try {
				sqs.sendMessage(new SendMessageRequest(queueURLfromRouterToAuctionManager, nextMessage.getBody()));
				//delete bid from list
				//destinationBidList.remove(nextMessage); //not such a clever way
				listIterator.remove(); //remove the last element returned by iterator to avoid ConcurrentModificationException at next()
			} catch (Exception e) {
				LOGGER.info("An error occurred while sending client messages to AuctionManager");
				e.printStackTrace();
			} finally {
				
			}
		}
	}


	/** Checks if a String only consists of an Integer value
	 * 
	 * @param s input string
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
	
	public static String getBidQueueURL() {
		return bidQueueURL;
	}

}
