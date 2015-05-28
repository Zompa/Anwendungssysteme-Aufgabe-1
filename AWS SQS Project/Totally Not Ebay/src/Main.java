

import router.RouterThread;
import router.RouterManager;
import auction.AuctionCreator;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;

public class Main {

	/**
	 * Starts an instance of every module
	 * @param args
	 */
	public static void main(String[] args) {
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider("default").getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (C:\\Users\\Daniel\\.aws\\credentials), and is in valid format.",
                    e);
        }

        AmazonSQS sqs = new AmazonSQSClient(credentials);
        sqs.setRegion(Region.getRegion(Regions.EU_CENTRAL_1));
        
        AuctionCreator auctionCreationService = new AuctionCreator(sqs);
        auctionCreationService.start();
        
        RouterManager routerManager = new RouterManager(sqs, 1);
        routerManager.start();


	}

}
