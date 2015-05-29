package publisher;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;

public class SQSInformation {
	public AmazonSQS getSqs() {
		return sqs;
	}

	public String getSubscribtionQueueUrl() {
		return SubscribtionQueueUrl;
	}

	public String getReceiveBroadcastQueueUrl() {
		return ReceiveBroadcastQueueUrl;
	}

	private AmazonSQS sqs;
	private String SubscribtionQueueUrl;
	private String ReceiveBroadcastQueueUrl;

	public SQSInformation(int publisherID) {
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
		
		//sqs.deleteQueue(new DeleteQueueRequest(SubscribtionQueueUrl));
		CreateQueueRequest createQueueRequest = new CreateQueueRequest("SubscribeQueue" + publisherID);
		SubscribtionQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();	
		
		createQueueRequest = new CreateQueueRequest("AUCTION_BROADCAST_QUEUE"+ publisherID);
		ReceiveBroadcastQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
	}
}
