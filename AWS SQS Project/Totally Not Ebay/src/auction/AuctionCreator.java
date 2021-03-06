package auction;

import java.util.List;
import java.util.logging.Logger;

import util.SimpleParser;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

/**
 * The purpose of this class is listening for auction creation request and
 * creating auctions accordingly. It listens for requests via long polling. As
 * soon as a request arrives it creates a new AuctionService. Simple.
 * 
 * @author Daniel
 * 
 */
public class AuctionCreator extends Thread {

	public static final String AUCTION_CREATION_QUEUE_NAME = "Create_Auction_Queue";

	private final static Logger LOGGER = Logger.getLogger(AuctionCreator.class
			.getName());
	private final AmazonSQS sqs;

	/**
	 * Use this constructor. It does not start the thread.
	 * 
	 * @param sqs
	 */
	public AuctionCreator(AmazonSQS sqs) {
		this.sqs = sqs;

		// init Queue
		CreateQueueRequest createQueueRequest = new CreateQueueRequest(
				AUCTION_CREATION_QUEUE_NAME);
		String myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();

		LOGGER.info("Initialized");

	}

	@Override
	public void run() {
		// request can be reused. it long-polls
		ReceiveMessageRequest receiveRequest = new ReceiveMessageRequest()
				.withQueueUrl(AUCTION_CREATION_QUEUE_NAME)
				.withMaxNumberOfMessages(10).withWaitTimeSeconds(20);

		LOGGER.info("Start Polling Loop");
		// Thread loop
		while (!isInterrupted()) {
			 LOGGER.info("Long polling");
			// poll messages
			List<Message> messages = sqs.receiveMessage(receiveRequest)
					.getMessages();
			// execute messages
			for (Message m : messages) {
				createAuction(m);
			}
		}
	}

	/**
	 * This methods checks if a message is a valid auction creation request and
	 * creates a new AuctionService if this is the case.
	 * 
	 * @param m
	 */
	private void createAuction(Message m) {

		LOGGER.info("Create Auction");
		// try catch to prevent whole Thread from shutting down because of one
		// malicious message
		try {
			String[] attributes = SimpleParser.getMessageAttributes(m);
			if (attributes[0].equals("CREATE_AUCTION")) {
				if (!isAuctionRunning(attributes[3]))
					new AuctionThread(sqs, attributes[1],
							Integer.parseInt(attributes[2]),Integer.parseInt(attributes[3])).start();
			} else {
				LOGGER.info("WRONG MESSAGE: " + m.getBody());
			}
		} catch (Exception e) {

			LOGGER.info("WRONG MESSAGE: " + m.getBody());
			e.printStackTrace();
		} finally {
			// delete message
			String messageRecieptHandle = m.getReceiptHandle();
			sqs.deleteMessage(new DeleteMessageRequest(
					AUCTION_CREATION_QUEUE_NAME, messageRecieptHandle));
		}

	}

	private boolean isAuctionRunning(String queueID) {
		for (String queueUrl : sqs.listQueues(
				AuctionThread.QUEUE_NAME_PREFIX + queueID).getQueueUrls()) {
			if (queueUrl.equals(AuctionThread.QUEUE_NAME_PREFIX + queueID))
				return true;
		}
		return false;
	}
}
