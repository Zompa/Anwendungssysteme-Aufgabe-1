package publisher;

import java.util.ArrayList;

public class AuctionManager {
	private ArrayList<Auction> activeAuctions; // TODO evtl nicht der beste Typ

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
}
