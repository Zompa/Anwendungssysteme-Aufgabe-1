package publisher;

import com.amazonaws.services.sqs.model.CreateQueueRequest;

public class Subscriber {

	private int subscriberID;
	private String SubscriberQueueUrl;

	
	public int getSubscriberID() {
		return subscriberID;
	}

	public String getSubscriberQueueUrl() {
		return SubscriberQueueUrl;
	}

	public Subscriber(int ID, SQSInformation sqsInfo){
		this.subscriberID = ID;
		//TODO Was passiert wenn Queue schon vorhanden?
		CreateQueueRequest createQueueRequest = new CreateQueueRequest("CLIENT_UPDATE_" + this.subscriberID);
		SubscriberQueueUrl = sqsInfo.getSqs().createQueue(createQueueRequest).getQueueUrl();		
	}
	
	
}
