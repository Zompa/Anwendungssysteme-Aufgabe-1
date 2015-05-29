package publisher;

import java.util.Random;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.sun.xml.internal.ws.api.pipe.NextAction;

public class PublisherManager {
private static int nextPublisherID = 1;
private static String globalBroadcastQueueUrl;
private static String globalSubscribtionQueueUrl;
private static AmazonSQS sqs;

public static void main(String[] args) {
	setSqsInfo();
try {
	test();
} catch (InterruptedException e) {
	e.printStackTrace();
}
	
}

private static void test() throws InterruptedException{
	Publisher p = new Publisher(nextPublisherID);
	p.start();
	CreateQueueRequest createQueueRequest = new CreateQueueRequest("SubscribeQueue" + nextPublisherID);
	String p1SubscribtionQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();	
	System.out.println("SubQueue 2: " + p1SubscribtionQueueUrl);
	createQueueRequest = new CreateQueueRequest("AUCTION_BROADCAST_QUEUE"+ nextPublisherID);
	String p1BroadcastQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
	sqs.sendMessage(new SendMessageRequest(p1BroadcastQueueUrl, "AUCTION_SCHEDULED/666/STARTZEITPUNKT/ENDZEITPUNKT/AUCTION_NAME"));
	nextPublisherID = 2;
	
	Publisher p2 = new Publisher(nextPublisherID);
	p2.start();
	createQueueRequest = new CreateQueueRequest("SubscribeQueue" + nextPublisherID);
	String p2SubscribtionQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();	
	System.out.println("SubQueue 2: " + p2SubscribtionQueueUrl);
	createQueueRequest = new CreateQueueRequest("AUCTION_BROADCAST_QUEUE"+ nextPublisherID);
	String p2BroadcastQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
	sqs.sendMessage(new SendMessageRequest(p2BroadcastQueueUrl, "AUCTION_SCHEDULED/665/STARTZEITPUNKT/ENDZEITPUNKT/AUCTION_NAME"));
	
	Random rand = new Random();		
	while (true){

		sqs.sendMessage(new SendMessageRequest(p1SubscribtionQueueUrl, "SUBSCRIBE/"+rand.nextInt() +"/666"));
		sqs.sendMessage(new SendMessageRequest(p1BroadcastQueueUrl, "NEW_HIGHEST_BIDDER/666/"+rand.nextDouble() +"/12"));
		sqs.sendMessage(new SendMessageRequest(p2SubscribtionQueueUrl, "SUBSCRIBE/"+rand.nextInt() +"/665"));
		sqs.sendMessage(new SendMessageRequest(p2BroadcastQueueUrl, "NEW_HIGHEST_BIDDER/665/"+rand.nextDouble() +"/12"));
		Thread.sleep(1000);
	}
	}

	

private static void setSqsInfo(){
	AWSCredentials credentials = null;
	try {
		credentials = new ProfileCredentialsProvider("default")
				.getCredentials();
	} catch (Exception e) {
		throw new AmazonClientException(
				"Cannot load the credentials from the credential profiles file. ");
	}

	sqs = new AmazonSQSClient(credentials);
	Region eur = Region.getRegion(Regions.EU_WEST_1);
	sqs.setRegion(eur);
	
	CreateQueueRequest createQueueRequest = new CreateQueueRequest("SubscribeQueue");
	globalSubscribtionQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();	
	
	createQueueRequest = new CreateQueueRequest("AUCTION_BROADCAST_QUEUE");
	globalBroadcastQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
	}
}
	