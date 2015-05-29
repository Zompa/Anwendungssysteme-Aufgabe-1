package client;

import java.util.ArrayList;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;

public class ClientMain {
	public static void main(String[] args) {
		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider("default")
					.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. ",
					e);
		}

		AmazonSQS sqs = new AmazonSQSClient(credentials);
		sqs.setRegion(Region.getRegion(Regions.EU_CENTRAL_1));
		Client c = new Client(sqs);
		ClientGUI test = new ClientGUI(c);
	}
}
