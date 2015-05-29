package client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import util.SimpleParser;

public class Client {
	// Attribute
	private int id;
	private ArrayList<Integer> auctionIDs = new ArrayList<Integer>();
	public static final String CLIENT_QUEUE_NAME = "Client_Update_Queue_";
	@SuppressWarnings("unused")
	private String clientQueueURL;
	private final AmazonSQS sqs;
	private int choosenAuctionId;
	private int highestBid;
	private int highestBidId;
	private Date auctionEnd;

	// Constructor for the client with a fixed auction number
	public Client(AmazonSQS sqs) {
		auctionIDs = new ArrayList<Integer>();
		auctionIDs.add(666);// the id of the only auction in the system
		id = 42;// TODO own client id
		this.sqs = sqs;
		// initialize the client's clientUpdateQueue in which the publisher can
		// drop messages
		CreateQueueRequest clientUpdateQueueRequest = new CreateQueueRequest(
				CLIENT_QUEUE_NAME + id);
		clientQueueURL = sqs.createQueue(clientUpdateQueueRequest)
				.getQueueUrl();
	}

	// Generates message and destination for subscribe message and then sends it
	private void subscribe(int auctionId) {
		String queueURLSubscribe = "SubscribeQueue";
		String subscribeMessage = "SUBSCRIBE/" + id + "/" + choosenAuctionId;
		try {
			sqs.sendMessage(new SendMessageRequest(queueURLSubscribe,
					subscribeMessage));
		} catch (Exception e) {
			System.out.println("An error occured during subscribe sending!");
			e.printStackTrace();
		}
	}

	// Generates message and destination for unsubscribe message and then sends
	// it
	protected void unsubscribe(int auctionId) {
		String queueURLUnsubscribe = "SubscribeQueue";
		String unsubscribeMessage = "UNSUBSCRIBE/" + id + "/"
				+ choosenAuctionId;
		try {
			sqs.sendMessage(new SendMessageRequest(queueURLUnsubscribe,
					unsubscribeMessage));
		} catch (Exception e) {
			System.out.println("An error occured during unsubscribe sending!");
			e.printStackTrace();
		}

	}

	// Reads all messages from Publisher
	public void receiveMessagesfromPublisher() {

		String queueURLfromPublisher = "ClientUpdateQueue" + id;
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(
				queueURLfromPublisher);
		List<Message> receivedfromPublisher = sqs.receiveMessage(
				receiveMessageRequest).getMessages();

		for (Message receivedMessage : receivedfromPublisher) {
			String[] message = SimpleParser
					.getMessageAttributes(receivedMessage);
			if (message[0] == "NEW_HIGHEST_BIDDER") {
				if (Integer.parseInt(message[1]) == choosenAuctionId) {
					highestBid = Integer.parseInt(message[2]);
					highestBidId = Integer.parseInt(message[3]);
				}
			} else if (message[0] == "AUCTION_END") {
				if (Integer.parseInt(message[1]) == choosenAuctionId) {
					highestBid = Integer.parseInt(message[3]);
					highestBidId = Integer.parseInt(message[2]);
				}
			}

			// delete message from queueURLfromPublisher
			String receiptHandle = receivedMessage.getReceiptHandle();
			sqs.deleteMessage(new DeleteMessageRequest(queueURLfromPublisher,
					receiptHandle));
		}
	}

	// Generates message and destination for make bid and then sends it
	public void sendBidToRouter(double bid) {
		String queueURLfromClientToRouter = "Bid_Queue";
		String bidMessage = "MAKE_BID/" + choosenAuctionId + "/" + bid + "/"
				+ id;
		try {
			sqs.sendMessage(new SendMessageRequest(queueURLfromClientToRouter,
					bidMessage));
		} catch (Exception e) {
			System.out.println("An error occured during bid sending!");
			e.printStackTrace();
		}
	}

	// From here on only getter and setter
	public int getClientId() {
		return id;
	}

	public ArrayList<Integer> getAuctionIDs() {
		return auctionIDs;
	}

	public int getChoosenAuctionId() {
		return choosenAuctionId;
	}

	public int getHighestBid() {
		return highestBid;
	}

	public int getHighestBidId() {
		return highestBidId;
	}

	// sets the choosenAuctionID and automatically subscribes to the new action
	// and unsubscribes to old auction
	public void setChoosenAuctionId(int choosenAuctionId) {
		if (this.choosenAuctionId != 0) {
			unsubscribe(this.choosenAuctionId);
		}
		this.choosenAuctionId = choosenAuctionId;
		subscribe(choosenAuctionId);
	}

	public int getID() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setAuctionIDs(ArrayList<Integer> auctionIDs) {
		this.auctionIDs = auctionIDs;
	}

	public Date getAuctionEnd() {
		return auctionEnd;
	}

	public void setAuctionEnd(long date) {
		auctionEnd = new Date(date);
	}

}
