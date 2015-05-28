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
	public static AmazonSQS sqs;
	public static String SubscribtionQueueUrl;

	public static void initialize() {
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
		SubscribtionQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();	
	}
}
