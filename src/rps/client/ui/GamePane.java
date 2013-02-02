package rps.client.ui;

import static javax.swing.BoxLayout.Y_AXIS;

import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.rmi.RemoteException;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import rps.game.Game;
import rps.game.data.Player;

public class GamePane {

	private final JPanel gamePane = new JPanel();
	private final JTextField chatInput = new JTextField();
	private final JTextArea chat = new JTextArea(4, 30);
	private final JScrollPane scrollPane = new JScrollPane(chat);

	private Game game;
	private Player player;

	public GamePane(Container parent) {

		gamePane.setLayout(new BoxLayout(gamePane, Y_AXIS));
		gamePane.add(chatInput);
		gamePane.add(scrollPane);

		chat.setLineWrap(true);
		chat.setEditable(false);

		gamePane.setVisible(false);

		parent.add(gamePane);

		bindButtons();
	}

	private void bindButtons() {
		chatInput.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				boolean isEnter = e.getKeyCode() == KeyEvent.VK_ENTER;
				if (isEnter) {
					addToChat();
				}
			}

			@Override
			public void keyTyped(KeyEvent e) {
			}

			@Override
			public void keyReleased(KeyEvent e) {
			}
		});
	}

	private void addToChat() {
		String message = chatInput.getText().trim();
		if (message.length() > 0) {
			try {
				game.sendMessage(player, message);
				chatInput.setText("");
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void hide() {
		gamePane.setVisible(false);
	}

	public void startGame(Player player, Game game) {
		this.player = player;
		this.game = game;
		reset();
		gamePane.setVisible(true);
	}

	public void receivedMessage(Player sender, String message) {

		if (chat.getText().length() != 0) {
			chat.append("\n");
		}
		String formatted = sender.getNick() + ": " + message;
		chat.append(formatted);
		chat.setCaretPosition(chat.getDocument().getLength());
	}

	private void reset() {
		chat.setText(null);
	}
}