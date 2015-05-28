package test.auctionService;

import router.RouterThread;
import auction.AuctionThread;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class RouterTest extends Thread {
	
	public static void main(String[] args) {
		System.out.println("Test: init");
		AWSCredentials credentials = null;
		try {
			credentials = new ProfileCredentialsProvider("default")
					.getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. "
							+ "Please make sure that your credentials file is at the correct "
							+ "location (C:\\Users\\Daniel\\.aws\\credentials), and is in valid format.",
					e);
		}

		AmazonSQS sqs = new AmazonSQSClient(credentials);
		sqs.setRegion(Region.getRegion(Regions.EU_CENTRAL_1));

		
		//init
		AuctionThread auctionService = new AuctionThread(sqs, "RouterTestAuction", 4);
		auctionService.start();
		
//		Router router = new Router(sqs);
//		router.start();
//		
//		//client message (bid)
//		try {
//			Thread.sleep(1000);
//		} catch(InterruptedException ioe) {
//			ioe.getCause();
//		}
//		
//		String messageBody = "MAKE_BID/666/42.23/Frido Fröhlich";
//		sqs.sendMessage(new SendMessageRequest(Router.getBidQueueURL(), messageBody));

	}
}
