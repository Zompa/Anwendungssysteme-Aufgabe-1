package publisher;

import java.util.ArrayList;

/**
 * Built for use in the Publisher. Stores all Auctions
 * @author Paul
 *
 */
public class AuctionManager {
	private ArrayList<Auction> activeAuctions; 

	public AuctionManager() {
		activeAuctions = new ArrayList<Auction>();
	}

	public void addAuction(Auction newAuct) {
		activeAuctions.add(newAuct);
	}

	public void removeAuction(Auction remAuct){
		if (activeAuctions.contains(remAuct))
		{
			activeAuctions.remove(remAuct);	
		}	
	}
	/**
	 * retrieves Auction by ID
	 * @param auctID
	 * @return Auction Object or null if no matching Auction found 
	 */
	public Auction getAuctionByID(int auctID){
		for (Auction a : activeAuctions){
			if (a.getAuctionID() == auctID){
				return a;
			}
		}
		return null;
	}
	
	public boolean hasAuctionWithID(int auctID){
		for (Auction a : activeAuctions){
			if (a.getAuctionID() == auctID){
				return true;
			}
		}
		return false;
	}
}
