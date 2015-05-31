package client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class ClientGUI {
	Client c;
	JFrame frame;
	JPanel middle;

	/**Generates the window
	 * @param c
	 */
	@SuppressWarnings("static-access")
	public ClientGUI(final Client c) {
		this.c = c;
		frame = new JFrame("Not ebay");
		frame.setLayout(new BorderLayout());

		// the left side of the window
		JPanel left = new JPanel();
		left.setLayout(new GridLayout(4, 1, 10, 10));

		// the button for bidding
		JButton bieten = new JButton("Bieten");

		bieten.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				bid();

			}
		});
		left.add(bieten);

		// the button for choosing an auction
		JButton auktion = new JButton("Auktion auswählen");
		auktion.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				chooseAuktion(c, frame);
				c.receiveMessagesfromPublisher();
				drawInformationPanel();

			}
		});
		left.add(auktion);

		// the button for refreshing the information
		JButton aktualisieren = new JButton("Aktualisieren");
		aktualisieren.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				c.receiveMessagesfromPublisher();
				drawInformationPanel();

			}
		});

		left.add(aktualisieren);

		// a button to end the application
		JButton beenden = new JButton("Beenden");
		beenden.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (c.getChoosenAuctionId() != 0) {
					c.unsubscribe(c.getChoosenAuctionId());
				}
				System.exit(0);

			}
		});
		left.add(beenden);

		frame.add(left, BorderLayout.WEST);

		// the middle of the window
		middle = new JPanel();
		middle.setLayout(new GridLayout(6, 1, 20, 5));
		middle.add(new JLabel("Meine ID ist: " + c.getClientId()));
		middle.add(new JLabel("Die ID der ausgewälten Auktion ist: "
				+ c.getChoosenAuctionId()));
		middle.add(new JLabel("Das höchste Gebot der ausgewälten Auktion ist: "
				+ c.getHighestBid()));
		middle.add(new JLabel("Das höchste Gebot ist vom Client mit der ID: "
				+ c.getHighestBidId()));
		middle.add(new JLabel("Das Auktionsende ist: " + c.getAuctionEnd()));
		middle.add(new JLabel(""));

		frame.add(middle, BorderLayout.CENTER);

		frame.setVisible(true);
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		frame.setSize(600, 400);
	}

	/**
	 * method to repaint the middle panel
	 */
	public void drawInformationPanel() {
		middle.removeAll();
		middle.setLayout(new GridLayout(6, 1, 20, 5));
		middle.add(new JLabel("Meine ID ist: " + c.getClientId()));
		middle.add(new JLabel("Die ID der ausgewälten Auktion ist: "
				+ c.getChoosenAuctionId()));
		middle.add(new JLabel("Das höchste Gebot der ausgewälten Auktion ist: "
				+ c.getHighestBid()));
		middle.add(new JLabel("Das höchste Gebot ist vom Client mit der ID: "
				+ c.getHighestBidId()));
		middle.add(new JLabel("Das Auktionsende ist: " + c.getAuctionEnd()));
		if (c.isAuctionStillRuns()) {
			middle.add(new JLabel("Die Auktion läuft noch"));
		}
		else{
			middle.add(new JLabel("Die Auktion läuft nicht mehr"));
		}
		middle.validate();

	}

	/**a little pop up menu for choosing an auction
	 * @param c
	 * @param frame
	 */
	protected void chooseAuktion(Client c, JFrame frame) {
		int size = c.getAuctionIDs().size();
		Integer[] auctionIds = new Integer[size];
		c.getAuctionIDs().toArray(auctionIds);

		Integer newid = (Integer) JOptionPane.showInputDialog(null,
				"IDs aller verfügbaren Autktionen",
				"Bitte gewünschte Auktion wählen",
				JOptionPane.QUESTION_MESSAGE, null, auctionIds,
				(Integer) c.getChoosenAuctionId());
		c.setChoosenAuctionId(newid);

	}

	/**
	 * a little pop up menu to enter your bid
	 */
	protected void bid() {
		String gebot = JOptionPane
				.showInputDialog("Bitte ihr Gebot für Auktion "
						+ c.getChoosenAuctionId()
						+ " eingeben (maximal zwei Nachkommastellen)");
		try {
			double bid = Double.parseDouble(gebot);
			if (bid <= 0) {
				JOptionPane.showMessageDialog(null,
						"Es wurde ein Gebot kleiner als null eingegeben");
			} else {
				String[] parts = gebot.split("\\.");
				if (gebot.contains(".") && parts[1].length() > 2) {
					JOptionPane
							.showMessageDialog(null,
									"Es wurden mehr als zwei Nachkommastellen eingegeben");
				} else {
					c.sendBidToRouter(bid);
				}
			}
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null,
					"Es wurde kein gültiges Gebot eingegeben");
		} catch (NullPointerException e) {
			JOptionPane.showMessageDialog(null,
					"Es wurde kein gültiges Gebot eingegeben");
		}
	}
}
