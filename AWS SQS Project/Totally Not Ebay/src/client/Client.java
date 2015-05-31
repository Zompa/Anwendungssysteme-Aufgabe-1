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
	
	private int id;
	private ArrayList<Integer> auctionIDs = new ArrayList<Integer>();
	public static final String CLIENT_QUEUE_NAME = "Client_Update_Queue_";
	@SuppressWarnings("unused")
	private String clientQueueURL;
	private final AmazonSQS sqs;
	private int choosenAuctionId;
	private double highestBid;
	private int highestBidId;
	private Date auctionEnd;
	private boolean AuctionStillRuns;

	/**Constructor for the client with a fixed auction number
	 * @param sqs
	 */
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

	/**Generates message and destination for subscribe message and then sends it
	 * @param auctionId
	 */
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

	/**Generates message and destination for unsubscribe message and then sends it
	 * @param auctionId
	 */
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

	/**
	 * Reads all messages from Publisher
	 */
	public void receiveMessagesfromPublisher() {

		String queueURLfromPublisher = "Client_Update_Queue_" + id;
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(
				queueURLfromPublisher);
		List<Message> receivedfromPublisher = sqs.receiveMessage(
				receiveMessageRequest).getMessages();

		for (Message receivedMessage : receivedfromPublisher) {
			String[] message = SimpleParser
					.getMessageAttributes(receivedMessage);
			if (message[0].equals("NEW_HIGHEST_BIDDER")) {
				if (Integer.parseInt(message[1]) == choosenAuctionId) {
					highestBid = Double.parseDouble(message[2]);
					highestBidId = Integer.parseInt(message[3]);
					setAuctionEnd(Long.parseLong(message[4]));
					setAuctionStillRuns(true);
				}
			} else if (message[0].equals("AUCTION_END")) {
				if (Integer.parseInt(message[1]) == choosenAuctionId) {
					highestBid = Double.parseDouble(message[3]);
					highestBidId = Integer.parseInt(message[2]);
					setAuctionStillRuns(false);
				}
			}
			// delete message from queueURLfromPublisher
			String receiptHandle = receivedMessage.getReceiptHandle();
			sqs.deleteMessage(new DeleteMessageRequest(queueURLfromPublisher,
					receiptHandle));
		}
	}

	/**Generates message and destination for make bid and then sends it
	 * @param bid
	 */
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
	/**
	 * @return the client ID
	 */
	public int getClientId() {
		return id;
	}

	/**
	 * @return all available auctionIDs
	 */
	public ArrayList<Integer> getAuctionIDs() {
		return auctionIDs;
	}

	/**
	 * @return the choosen auction ID
	 */
	public int getChoosenAuctionId() {
		return choosenAuctionId;
	}

	/**
	 * @return the highest bid 
	 */
	public double getHighestBid() {
		return highestBid;
	}

	/**
	 * @return the ID of the highest bidder
	 */
	public int getHighestBidId() {
		return highestBidId;
	}
 
	/**
	 * sets the choosenAuctionID and automatically subscribes to the new action
	 * and unsubscribes to old auction
	 * 
	 * @param choosenAuctionId
	 */
	public void setChoosenAuctionId(int choosenAuctionId) {
		if (this.choosenAuctionId != 0) {
			unsubscribe(this.choosenAuctionId);
		}
		this.choosenAuctionId = choosenAuctionId;
		subscribe(choosenAuctionId);
	}

	/**
	 * @param id
	 */
	public void setId(int id) {
		this.id = id;
	}

	/**
	 * @param auctionIDs
	 */
	public void setAuctionIDs(ArrayList<Integer> auctionIDs) {
		this.auctionIDs = auctionIDs;
	}

	/**
	 * @return the auction End as a Date
	 */
	public Date getAuctionEnd() {
		return auctionEnd;
	}

	/**
	 * @param date
	 */
	public void setAuctionEnd(long date) {
		auctionEnd = new Date(date);
	}

	/**
	 * @return if the auction still runs
	 */
	public boolean isAuctionStillRuns() {
		return AuctionStillRuns;
	}

	/**
	 * @param auctionStillRuns
	 */
	public void setAuctionStillRuns(boolean auctionStillRuns) {
		AuctionStillRuns = auctionStillRuns;
	}

}
