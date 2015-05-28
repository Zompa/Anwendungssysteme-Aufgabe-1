package router;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import util.SimpleParser;
import auction.AuctionCreator;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

/** In this class auctionIDs for auction queues provided by AuctionCreator are collected and stored.
 * 
 * @author christianhoffmann
 *
 */
public class RouterManager extends Thread {
	public static final String ROUTER_REGISTRY_QUEUE_NAME = "ROUTER_REGISTRY_QUEUE_EXAMPLE";
	private final static Logger LOGGER = Logger.getLogger(AuctionCreator.class.getName());

	Collection<RouterThread> routers = new ArrayList<>();
	private Map<Integer, String> auctionIDforAuctionQueue = new HashMap<>();

	private AmazonSQS sqs;

	public RouterManager(AmazonSQS sqs, int numberOfRouterThreads) {
		this.sqs = sqs;

		// init Queue
		CreateQueueRequest createQueueRequest = new CreateQueueRequest(
				ROUTER_REGISTRY_QUEUE_NAME);
		String myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();

		for (int i = 0; i < numberOfRouterThreads; i++) {
			routers.add(new RouterThread(sqs, this));
		}

		LOGGER.info("Initialized");
	}

	@Override
	public void run() {
		for (RouterThread r : routers) {
			r.start();
		}

		// request can be reused. it long-polls.
		ReceiveMessageRequest receiveRequest = new ReceiveMessageRequest()
		.withQueueUrl(ROUTER_REGISTRY_QUEUE_NAME)
		.withMaxNumberOfMessages(10).withWaitTimeSeconds(20);

		while (!isInterrupted()) {
			List<Message> messages = sqs.receiveMessage(receiveRequest).getMessages();

			// execute messages
			String[] attributes;
			for (Message m : messages) {
				try {
					attributes = SimpleParser.getMessageAttributes(m);
					System.out.println(m.getBody());
					for (String s : attributes) System.out.println(s);
					synchronized (auctionIDforAuctionQueue) {
						if (attributes[0].equals("AUCTION_CREATION")) {
							auctionIDforAuctionQueue.put(
									Integer.parseInt(attributes[1]),
									attributes[2]);
						} else
							auctionIDforAuctionQueue.remove(
									Integer.parseInt(attributes[1]),
									attributes[2]);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					String messageReceiptHandle = m.getReceiptHandle();
					sqs.deleteMessage(new DeleteMessageRequest(
							ROUTER_REGISTRY_QUEUE_NAME, messageReceiptHandle));
				}
			}
		}
	}
	
	/** Delivers queue URLs for provided auctionIDs from map
	 * 
	 * @param ID auctionID
	 * @return queueURL
	 */
	public String getQueueURLForID(int ID) {
		synchronized (auctionIDforAuctionQueue) {
			return auctionIDforAuctionQueue.get(ID);
		}
	}
}
