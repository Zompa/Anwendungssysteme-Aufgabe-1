package auction;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import util.SimpleParser;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

/**
 * This class handles one auction. It polls for new bids and sends messages when
 * changes occur (start of auction, end of auction, new bid)
 * 
 * @author Daniel
 * 
 */
public class AuctionThread extends Thread {

	private final static String QUEUE_NAME_PREFIX = "Auction_Queue_";
	private final static Logger LOGGER = Logger.getLogger(AuctionThread.class
			.getName());
	private static final int AUCTION_START_OFFSET = 5;
	private final AmazonSQS sqs;
	private final String name;
	private final int auctionTime;

	/**
	 * set on initialization.
	 */
	private final String receiveQueueURL;
	/**
	 * set on initialization
	 */
	private final String sendQueueURL; // TODO

	/**
	 * set on initialization. will be reused
	 */
	private final ReceiveMessageRequest receiveRequest;
	// For now
	private final int auctionID = 666;

	private double highestBid = 0.0d;
	private String highestBidder = "Chuck Norris";

	private String receiveQueueURLAbsolute = QUEUE_NAME_PREFIX + auctionID;

	private Date startDate;
	private Date endDate;

	/**
	 * @param sqs
	 * @param name
	 *            Name of the Auction
	 * @param auctionTime
	 *            Time of the Auction in seconds
	 */
	public AuctionThread(AmazonSQS sqs, String name, int auctionTime) {
		super();
		this.sqs = sqs;
		this.name = name;
		this.auctionTime = auctionTime;

		// init queues
		CreateQueueRequest createQueueRequest = new CreateQueueRequest(
				QUEUE_NAME_PREFIX + auctionID);
		receiveQueueURL = sqs.createQueue(createQueueRequest).getQueueUrl();
		// TODO change
		createQueueRequest = new CreateQueueRequest("AUCTION_BROADCAST_QUEUE");
		sendQueueURL = sqs.createQueue(createQueueRequest).getQueueUrl();

		receiveRequest = new ReceiveMessageRequest(receiveQueueURL)
				.withWaitTimeSeconds(10);

		LOGGER.info("Auction initialized: " + auctionID);

		registerAuctionAtRouter(false);
	}

	/**
	 * Registers the auction on the routers to allow the transmitting of
	 * messages
	 */
	private void registerAuctionAtRouter(boolean destruction) {

		List<String> routerQueueURLs = getRouterQueueURLs(sqs);

		String message = destruction ? "AUCTION_DESTRUCTION/"
				: "AUCTION_CREATION/";
		message += auctionID + "/" + receiveQueueURLAbsolute;
		for (String url : routerQueueURLs) {

			sqs.sendMessage(new SendMessageRequest(url, message));
		}
		LOGGER.info("Registered Auction Creation/Destruction");
	}

	/**
	 * This method is a placeholder. It would normally ask AWS for all existing
	 * Queues and sorts out all queues that start with "ROUTER_REGISTRY_QUEUE_".
	 * As we only have one router in this example, the method is not implemented
	 * 
	 * it only returns "ROUTER_REGISTRY_QUEUE_EXAMPLE"
	 * 
	 * @param sqs
	 * @return
	 */
	private static List<String> getRouterQueueURLs(AmazonSQS sqs) {
		List<String> result = new ArrayList<>();
		result.add("ROUTER_REGISTRY_QUEUE_EXAMPLE");
		return result;
	}

	@Override
	public void run() {
		// setting end time
		Calendar calendar = Calendar.getInstance(); // auctionTime
	//	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		startDate = new Date(calendar.getTime().getTime() + AUCTION_START_OFFSET*1000);
	//	System.out.println(sdf.format(calendar.getTime()));
		endDate = new Date(startDate.getTime() + auctionTime*1000);
	//	System.out.println(sdf.format(startDate));
	//	System.out.println(sdf.format(endDate));
		broadcastAuctionScheduled();
		try {
			Thread.sleep(AUCTION_START_OFFSET * 1000);
		} catch (InterruptedException e1) {
			interrupt();
		}
		broadcastAuctionStart();
	//	System.out.println(sdf.format(Calendar.getInstance().getTime()));
		// main loop: endTimeMillis is the end time of the auction
		while (!isInterrupted() && Calendar.getInstance().getTime().before(endDate)) {
			// check for new bids and process them
			processBids(sqs.receiveMessage(receiveRequest).getMessages());

			// I cant use long polling or the thread might not close the auction
			// in time
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				interrupt();
			}
		}
		registerAuctionAtRouter(true);
		broadcastAuctionResults();
	}


	/**
	 * checks if a bid is valid and sets a new highest Bidder if necessary.
	 * 
	 * @param messages
	 */
	private void processBids(List<Message> messages) {
		String[] messageAttributes;
		String bidder;
		double bid;

		LOGGER.info("Processing new biddings");
		for (Message message : messages) {
			// Try catch to prevent whole thread from shutting down in case of
			// malicious bid
			try {
				messageAttributes = SimpleParser.getMessageAttributes(message);

				bid = Double.parseDouble(messageAttributes[2]);
				bidder = messageAttributes[3];

				if (bid > highestBid) {
					highestBid = bid;
					highestBidder = bidder;
					broadcastBidChange();
				}
			} catch (Exception e) {
				LOGGER.info("Processing failed: " + message.getBody());
				e.printStackTrace();
			} finally {
				// delete message
				String messageRecieptHandle = message.getReceiptHandle();
				sqs.deleteMessage(new DeleteMessageRequest(receiveQueueURL,
						messageRecieptHandle));
			}

		}

	}


	private void broadcastBidChange() {
		String messageText = "NEW_HIGHEST_BIDDER/" + auctionID + "/"
				+ highestBid + "/" + highestBidder;
		broadcast(messageText);
	}
	
	private void broadcastAuctionScheduled() {
		String messageText = "AUCTION_SCHEDULED/" + auctionID + "/" + startDate.getTime() + "/" + endDate.getTime()
				+ "/" + name;
		broadcast(messageText);
	}
	
	public void broadcastAuctionStart() {
		String messageText = "AUCTION_STARTED/" + auctionID + "/" + endDate.getTime()
				+ "/" + name;
		broadcast(messageText);
	}

	public void broadcastAuctionResults() {
		String messageText = "AUCTION_END/" + auctionID + "/" + highestBidder
				+ "/" + highestBid;
		broadcast(messageText);
	}

	private void broadcast(String messageText) {

		LOGGER.info(messageText);
		sqs.sendMessage(new SendMessageRequest(sendQueueURL, messageText));

	}

}
