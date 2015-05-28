package client;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class ClientGUI {
	Client c;
	JFrame frame;
	JPanel middle;

	public ClientGUI(final Client c) {
		this.c = c;
		frame = new JFrame("Not ebay");
		frame.setLayout(new BorderLayout());

		// die linke Seite
		JPanel left = new JPanel();
		left.setLayout(new GridLayout(4, 1, 10,10));

		JButton bieten = new JButton("Bieten");

		bieten.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				bid();

			}
		});
		left.add(bieten);

		JButton auktion = new JButton("Auktion auswählen");
		auktion.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				chooseAuktion(c, frame);

			}
		});
		left.add(auktion);
		
		JButton aktualisieren = new JButton("Aktualisieren");
		aktualisieren.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				c.receiveMessagesfromPublisher();
				drawInformationPanel();

			}
		});

		left.add(aktualisieren);

		JButton beenden = new JButton("Beenden");
		beenden.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.exit(0);

			}
		});
		left.add(beenden);

		frame.add(left, BorderLayout.WEST);

		// die Mitte
		middle = new JPanel();
		middle.setLayout(new GridLayout(5, 1, 20, 5));
		middle.add(new JLabel("Meine ID ist: " + c.getClientId()));
		middle.add(new JLabel("Die ID der ausgewälten Auktion ist: "
				+ c.getChoosenAuctionId()));
		middle.add(new JLabel("Das höchste Gebot der ausgewälten Auktion ist: "
				+ c.getHighestBid()));
		middle.add(new JLabel("Das höchste Gebot ist vom Client mit der ID: "
				+ c.getHighestBidId()));
		middle.add(new JLabel("Das Auktionsende ist: " + c.getAuctionEnd()));

		frame.add(middle, BorderLayout.CENTER);

		frame.setVisible(true);
		frame.setDefaultCloseOperation(frame.EXIT_ON_CLOSE);
		frame.setSize(600, 400);
	}

	public void drawInformationPanel() {
		middle.removeAll();
		middle.setLayout(new GridLayout(5, 1, 20, 5));
		middle.add(new JLabel("Meine ID ist: " + c.getClientId()));
		middle.add(new JLabel("Die ID der ausgewälten Auktion ist: "
				+ c.getChoosenAuctionId()));
		middle.add(new JLabel("Das höchste Gebot der ausgewälten Auktion ist: "
				+ c.getHighestBid()));
		middle.add(new JLabel("Das höchste Gebot ist vom Client mit der ID: "
				+ c.getHighestBidId()));
		middle.add(new JLabel("Das Auktionsende ist: " + c.getAuctionEnd()));
		middle.validate();
		

	}

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
		drawInformationPanel();

	}

	protected void bid() {
		String gebot = JOptionPane
				.showInputDialog("Bitte ihr Gebot für Auktion "
						+ c.getChoosenAuctionId() + " eingeben");
		try {
			int bid = Integer.parseInt(gebot);
			if (bid <= 0) {
				JOptionPane.showMessageDialog(null,
						"Es wurde kein gültiges Gebot eingegeben");
			} else {
				c.sendBidToRouter(bid);
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
