package rps.client.ui;

import static javax.swing.BoxLayout.X_AXIS;
import static javax.swing.BoxLayout.Y_AXIS;
import static rps.client.Application.showMessage;
import static rps.network.NetworkUtil.getIPV4Addresses;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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

	private final JLabel playerLabel = new JLabel("Player name:");
	private final JTextField playerName = new JTextField("Player X");

	private final JLabel hostLabel = new JLabel("Host:");
	private final JComboBox<String> hostIP = new JComboBox<String>();

	private final JLabel joinLabel = new JLabel("Join:");
	private final JTextField joinAddr = new JTextField();

	private final JLabel aiLabel = new JLabel("AIs:");
	private final JComboBox<GameListener> comboAI = new JComboBox<GameListener>();

	private final JButton startBtn = new JButton("Start");

	private final UIController uiController;
	private final GameController gameController;

	private JRadioButton radioHost;
	private JRadioButton radioJoin;
	private JRadioButton radioAi;

	public StartupPane(Container parent, UIController uiController, GameController gameController,
			Vector<GameListener> ais) {

		this.uiController = uiController;
		this.gameController = gameController;

		comboAI.setModel(new DefaultComboBoxModel<GameListener>(ais));
		hostIP.setModel(new DefaultComboBoxModel<String>(getIPV4Addresses()));

		connectionPane.setLayout(new BoxLayout(connectionPane, Y_AXIS));

		ButtonGroup group = new ButtonGroup();
		radioHost = new JRadioButton();
		radioJoin = new JRadioButton();
		radioAi = new JRadioButton();
		group.add(radioHost);
		group.add(radioJoin);
		group.add(radioAi);
		radioHost.setSelected(true);

		addEntry(connectionPane, null, playerLabel, playerName);
		addEntry(connectionPane, radioHost, hostLabel, hostIP);
		addEntry(connectionPane, radioJoin, joinLabel, joinAddr);
		addEntry(connectionPane, radioAi, aiLabel, comboAI);

		connectionPane.add(startBtn);

		parent.add(connectionPane);

		bindActions();
	}

	private static void addEntry(JPanel container, JComponent c0, JComponent c1, JComponent c2) {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, X_AXIS));
		if (c0 != null) {
			p.add(c0);
		}
		p.add(c1);
		p.add(c2);
		container.add(p);
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
						GameListener ai = (GameListener) comboAI.getSelectedItem();
						gameController.startAIGame(getPlayer(), ai);
					}
				} catch (IllegalArgumentException ex) {
					// in case of duplicate name
					uiController.switchBackToStartup();
					showMessage(ex.getMessage());
				} catch (Exception ex) {
					showMessage("game could not be started");
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