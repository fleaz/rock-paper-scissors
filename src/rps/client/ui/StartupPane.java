package rps.client.ui;

import static rps.client.Application.showMessage;
import static rps.network.NetworkUtil.getIPV4Addresses;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import rps.client.GameController;
import rps.client.GameListener;
import rps.client.UIController;
import rps.game.data.Player;

public class StartupPane {

	private final JPanel connectionPane = new JPanel();

	private final JLabel playerLabel = new JLabel("Spieler:");
	private final JTextField playerName = new JTextField("Admiral Ackbar");

	private final JLabel hostLabel = new JLabel("Host:");
	private final JComboBox<String> hostIP = new JComboBox<String>();

	private final JLabel joinLabel = new JLabel("Beitreten:");
	private final JTextField joinAddr = new JTextField();

	private final JLabel aiLabel = new JLabel("KI:");
	private final JComboBox<GameListener> comboAi = new JComboBox<GameListener>();

	private final JButton startBtn = new JButton("Start");

	private final UIController uiController;
	private final GameController gameController;
	
	private final int rowHeight = 30;
	private final int colWidth = 40;

	private JRadioButton radioHost;
	private JRadioButton radioJoin;
	private JRadioButton radioAi;

	public StartupPane(Container parent, UIController uiController, GameController gameController,
			Vector<GameListener> ais) {

		this.uiController = uiController;
		this.gameController = gameController;

		comboAi.setModel(new DefaultComboBoxModel<GameListener>(ais));
		hostIP.setModel(new DefaultComboBoxModel<String>(getIPV4Addresses()));

		connectionPane.setLayout(null);

		ButtonGroup group = new ButtonGroup();
		radioHost = new JRadioButton();
		radioJoin = new JRadioButton();
		radioAi = new JRadioButton();
		group.add(radioHost);
		group.add(radioJoin);
		group.add(radioAi);
		radioHost.setSelected(true);
		
		connectionPane.setPreferredSize(new Dimension(980, 740));

		playerLabel.setBounds(2*colWidth, rowHeight, 100, 20);
		playerName.setBounds(2*colWidth+100, rowHeight, 150, 20);
		connectionPane.add(playerLabel);
		connectionPane.add(playerName);
		
		radioHost.setBounds(colWidth, 2*rowHeight, 20, 20);
		hostLabel.setBounds(2*colWidth, 2*rowHeight, 100, 20);
		hostIP.setBounds(2*colWidth+100, 2*rowHeight, 150, 20);
		connectionPane.add(radioHost);
		connectionPane.add(hostLabel);
		connectionPane.add(hostIP);
		
		radioJoin.setBounds(colWidth, 3*rowHeight,20,20);
		joinLabel.setBounds(2*colWidth, 3*rowHeight, 100, 20);
		joinAddr.setBounds(2*colWidth+100,3*rowHeight,150,20 );
		connectionPane.add(radioJoin);
		connectionPane.add(joinLabel);
		connectionPane.add(joinAddr);
		
		radioAi.setBounds(colWidth, 4*rowHeight,20,20);
		aiLabel.setBounds(2*colWidth, 4*rowHeight, 100, 20);
		comboAi.setBounds(2*colWidth+100,4*rowHeight,150,20 );
		connectionPane.add(radioAi);
		connectionPane.add(aiLabel);
		connectionPane.add(comboAi);
		
		startBtn.setBounds(3*colWidth, 5*rowHeight, 80, 30);
		connectionPane.add(startBtn);
	
		parent.add(connectionPane);

		bindActions();
	}

	private void bindActions() {
		startBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!isValidPlayerName()) {
					showMessage("bad player name");
					return;
				}
				try {
					uiController.switchToWaitingForOpponentPane();
					if (radioHost.isSelected()) {
						String host = (String) hostIP.getSelectedItem();
						gameController.startHostedGame(getPlayer(), host);
					} else if (radioJoin.isSelected()) {
						String host = joinAddr.getText().trim();
						gameController.startJoinedGame(getPlayer(), host);
					} else {
						GameListener ai = (GameListener) comboAi.getSelectedItem();
						gameController.startAIGame(getPlayer(), ai);
					}
				} catch (IllegalArgumentException ex) {
					// in case of duplicate name
					uiController.switchBackToStartup();
					showMessage(ex.getMessage());
				} catch (Exception ex) {
					showMessage("game could not be started");
					ex.printStackTrace();
					uiController.stopWaitingAndSwitchBackToStartup();
				}
			}
		});
	}

	public void show() {
		connectionPane.setVisible(true);
	}

	public void hide() {
		connectionPane.setVisible(false);
	}

	private boolean isValidPlayerName() {
		return getPlayerName().length() > 0;
	}

	private Player getPlayer() {
		return new Player(getPlayerName());
	}

	private String getPlayerName() {
		return playerName.getText().trim();
	}
}