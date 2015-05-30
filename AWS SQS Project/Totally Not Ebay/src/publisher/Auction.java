package publisher;
import java.util.ArrayList;


public class Auction {

	private int AuctionID;
	private double highestBid; 
	private int highestBidder;
	private long auctionEnd;
	private ArrayList<Subscriber> SubscriberList; 
	
	public Auction(int auctID, long auctionEnd){
		this.AuctionID = auctID;
		this.highestBid = 0.0;
		this.highestBidder = -1;
		this.SubscriberList = new ArrayList<Subscriber>();
		this.auctionEnd = auctionEnd;
	}
	
	/**
	 * returns who holds the highest bid
	 * @return sqs Message “NEW_HIGHEST_BIDDER/(AUCTION_ID)/(BID)/(BIDDER)”
	 */
	public String getAuctionHighestBidderMsg(){
		return "NEW_HIGHEST_BIDDER/" + this.AuctionID + "/"  + this.highestBid + "/" + this.highestBidder + "/" + this.auctionEnd;
	}
	
	public String getAuctionEndMessage(){
		return "AUCTION_END/" + this.AuctionID + "/"  + this.highestBidder + "/" + this.highestBid;
	}
	
	/**
	 * returns an Subscriber Object by its ID
	 * @param subscriberID
	 * @return matching Subscriber Object, if not found null
	 */
	public Subscriber getSubscriberByID(int subscriberID){
		for (Subscriber s : SubscriberList){
			if (s.getSubscriberID() == subscriberID){
				return s;
			}
		}
		return null;
	}
	
	
	public int getAuctionID() {
		return AuctionID;
	}

	public double getHighestBid() {
		return highestBid;
	}

	public void setHighestBid(double highestBid) {
		this.highestBid = highestBid;
	}

	public int getHighestBidder() {
		return highestBidder;
	}

	public void setHighestBidder(int highestBidder) {
		this.highestBidder = highestBidder;
	}

	public ArrayList<Subscriber> getSubscriberList() {
		return SubscriberList;
	}

	public void addSubscriber(Subscriber sub){
		if (this.getSubscriberByID(sub.getSubscriberID())== null){
		this.SubscriberList.add(sub);
		}
	}
	
	public void removeSubscriber(Subscriber sub){
		this.SubscriberList.remove(sub);
	}
	
}