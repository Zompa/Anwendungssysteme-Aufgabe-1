package publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import util.InternalMsg;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class PublisherBalancer {
	private static int nextPublisherID = 1;
	private static String globalBroadcastQueueUrl;
	private static String globalSubscribtionQueueUrl;
	private static AmazonSQS sqs;
	private static ArrayList<String> BroadcastQueueList ;
	private static ArrayList<String> SubscriptionQueueList ;

	public static void main(String[] args) {
		setSqsInfo();
		BroadcastQueueList = new ArrayList<String>();
		SubscriptionQueueList = new ArrayList<String>();
		
		for (int i= 0;i < 2; i++){
			createPublisher();
		}
		
		try {
			test();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	static private void createPublisher(){
		Publisher p = new Publisher(nextPublisherID);
		CreateQueueRequest createQueueRequest = new CreateQueueRequest("AUCTION_BROADCAST_QUEUE" + nextPublisherID);
		BroadcastQueueList.add(sqs.createQueue(createQueueRequest).getQueueUrl());
		createQueueRequest = new CreateQueueRequest("SubscribeQueue" + nextPublisherID);
		SubscriptionQueueList.add(sqs.createQueue(createQueueRequest).getQueueUrl());
		p.start();
		nextPublisherID++;
	}
	
	private static void processSubscription(){
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(
				globalSubscribtionQueueUrl);;
		List<Message> messages = sqs.receiveMessage(receiveMessageRequest)
				.getMessages();
		for (Message message : messages) {
			sendSubscriptionToCorrectPublisher(message);
			String messageRecieptHandle = message.getReceiptHandle();
			sqs.deleteMessage(new DeleteMessageRequest(
					globalSubscribtionQueueUrl,messageRecieptHandle));
		}
	}
	
	private static void sendSubscriptionToCorrectPublisher(Message msg){
		InternalMsg intMsg = new InternalMsg(msg);
		// this position is AuctionID gerade= Publisher1; ungerade Publisher2
		if ((Integer.parseInt(intMsg.getParams()[1]) % 2) == 0){
			sqs.sendMessage(new SendMessageRequest(SubscriptionQueueList.get(0),msg.getBody()));
		}
		else{
			sqs.sendMessage(new SendMessageRequest(SubscriptionQueueList.get(1),msg.getBody()));
		}
		SimpleLogger.log("SubMessage balanced: " + intMsg.toString());
	}

	
	private static void processBroadcast(){
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(
				globalBroadcastQueueUrl);
		//SimpleLogger.log(sqsInformation.getReceiveBroadcastQueueUrl());
		List<Message> messages = sqs.receiveMessage(receiveMessageRequest)
				.getMessages();
		for (Message message : messages) {
			sendBroadcastToCorrectPublisher(message);
			String messageRecieptHandle = message.getReceiptHandle();
			sqs.deleteMessage(new DeleteMessageRequest(
					globalBroadcastQueueUrl,messageRecieptHandle));
		}
	}
	
	private static void sendBroadcastToCorrectPublisher(Message msg){
		InternalMsg intMsg = new InternalMsg(msg);
		// this position is AuctionID gerade= Publisher1; ungerade Publisher2
		SimpleLogger.log("tempLog" + intMsg.toString());
		if ((Integer.parseInt(intMsg.getParams()[0]) % 2) == 0){
			sqs.sendMessage(new SendMessageRequest(BroadcastQueueList.get(0),msg.getBody()));
		}
		else{
			sqs.sendMessage(new SendMessageRequest(BroadcastQueueList.get(1),msg.getBody()));
		}
		SimpleLogger.log("BroadMessage balanced: " + intMsg.toString());
	}

	private static void setSqsInfo() {
		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider("default")
					.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. ");
		}

		sqs = new AmazonSQSClient(credentials);
		Region eur = Region.getRegion(Regions.EU_CENTRAL_1);
		sqs.setRegion(eur);

		CreateQueueRequest createQueueRequest = new CreateQueueRequest(
				"SubscribeQueue");
		globalSubscribtionQueueUrl = sqs.createQueue(createQueueRequest)
				.getQueueUrl();

		createQueueRequest = new CreateQueueRequest("AUCTION_BROADCAST_QUEUE");
		globalBroadcastQueueUrl = sqs.createQueue(createQueueRequest)
				.getQueueUrl();
	}
	
	private static void test() throws InterruptedException {
	
		sqs.sendMessage(new SendMessageRequest(globalBroadcastQueueUrl,
				"AUCTION_SCHEDULED/666/STARTZEITPUNKT/ENDZEITPUNKT/AUCTION_NAME"));
		sqs.sendMessage(new SendMessageRequest(globalBroadcastQueueUrl,
				"AUCTION_SCHEDULED/665/STARTZEITPUNKT/ENDZEITPUNKT/AUCTION_NAME"));

		Random rand = new Random();
		while (true) {
			sqs.sendMessage(new SendMessageRequest(globalSubscribtionQueueUrl,
					"SUBSCRIBE/" + rand.nextInt() + "/666"));
			sqs.sendMessage(new SendMessageRequest(globalBroadcastQueueUrl,
					"NEW_HIGHEST_BIDDER/666/" + rand.nextDouble() + "/12"));
			sqs.sendMessage(new SendMessageRequest(globalSubscribtionQueueUrl,
					"SUBSCRIBE/" + rand.nextInt() + "/665"));
			sqs.sendMessage(new SendMessageRequest(globalBroadcastQueueUrl,
					"NEW_HIGHEST_BIDDER/665/" + rand.nextDouble() + "/12"));
			
			processBroadcast();
			processSubscription();
			Thread.sleep(1000);
		}
	}
}
