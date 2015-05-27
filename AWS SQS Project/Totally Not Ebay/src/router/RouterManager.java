package router;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import util.SimpleParser;
import auctionService.AuctionCreationService;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

public class RouterManager extends Thread {

	public static final String ROUTER_REGISTRY_QUEUE_NAME = "ROUTER_REGISTRY_QUEUE_EXAMPLE";

	private final static Logger LOGGER = Logger
			.getLogger(AuctionCreationService.class.getName());

	Collection<Router> routers = new ArrayList<>();
	private Map<Integer, String> AuctionIDtoAuctionQueue = new HashMap<>();

	private AmazonSQS sqs;

	public RouterManager(AmazonSQS sqs, int numberOfRouterThreads) {

		this.sqs = sqs;

		// init Queue
		CreateQueueRequest createQueueRequest = new CreateQueueRequest(
				ROUTER_REGISTRY_QUEUE_NAME);
		String myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();

		for (int i = 0; i < numberOfRouterThreads; i++) {
			routers.add(new Router(sqs, this));
		}

		LOGGER.info("Initialized");
	}

	@Override
	public void run() {
		for (Router r : routers) {
			r.start();
		}

		// request can be reused. it long-polls
		ReceiveMessageRequest receiveRequest = new ReceiveMessageRequest()
				.withQueueUrl(ROUTER_REGISTRY_QUEUE_NAME)
				.withMaxNumberOfMessages(10).withWaitTimeSeconds(20);

		while (!isInterrupted()) {
			List<Message> messages = sqs.receiveMessage(receiveRequest)
					.getMessages();

			// execute messages
			String[] parameters;
			for (Message m : messages) {
				try {

					parameters = SimpleParser.getMessageAttributes(m);
					System.out.println(m.getBody());
					for (String s: parameters) System.out.println(s);
					synchronized (AuctionIDtoAuctionQueue) {
						if (parameters[0].equals("AUCTION_CREATION")) {
							AuctionIDtoAuctionQueue.put(
									Integer.parseInt(parameters[1]),
									parameters[2]);
						} else
							AuctionIDtoAuctionQueue.put(
									Integer.parseInt(parameters[1]),
									parameters[2]);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					String messageRecieptHandle = m.getReceiptHandle();
					sqs.deleteMessage(new DeleteMessageRequest(
							ROUTER_REGISTRY_QUEUE_NAME, messageRecieptHandle));
				}
			}
		}
	}

	public String getQueueURLForID(int ID) {
		synchronized (AuctionIDtoAuctionQueue) {
			return AuctionIDtoAuctionQueue.get(ID);
		}
	}
}
