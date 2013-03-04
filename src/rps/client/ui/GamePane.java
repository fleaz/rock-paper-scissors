package rps.client.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
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
	private final JTextArea log = new JTextArea(4, 30);
	private final JScrollPane scrollPane = new JScrollPane(chat);
	private final JScrollPane logPane = new JScrollPane(log);
	private final JPanel boardBackground = new JPanel();
	private final JPanel boardFigures = new JPanel();
	private final JPanel boardArrows = new JPanel();
	private final JPanel boardButtons = new JPanel();

	private Game game;
	private Player player;

	private ImageIcon iconWhite;
	private ImageIcon iconBlack;
	private ImageIcon emptyIcon;
	private ImageIcon unknown;
	private ImageIcon redTrap;
	private ImageIcon redFlag;
	private ImageIcon redStone;
	private ImageIcon redPaper;
	private ImageIcon redScissors;
	private ImageIcon blueTrap;
	private ImageIcon blueFlag;
	private ImageIcon blueStone;
	private ImageIcon bluePaper;
	private ImageIcon blueScissors;
	
	
	public String themePath = "img/default/";

	private GridBagConstraints gbcBackground = new GridBagConstraints();
	private GridBagConstraints gbcFigures = new GridBagConstraints();
	private GridBagConstraints gbcArrows = new GridBagConstraints();
	private GridBagConstraints gbcButtons = new GridBagConstraints();
	private JLabel[] backgroundTiles = new JLabel[42];
	private JLabel[] arrows = new JLabel[42];
	private JLabel[] traps = new JLabel[42];
	private JButton[] fieldButtons = new JButton[42];

	public GamePane(Container parent) {
		gamePane.setLayout(null);

		boardBackground.setBounds(20, 15, 700, 600);
		boardFigures.setBounds(20, 15, 700, 600);
		boardArrows.setBounds(20, 15, 700, 600);
		boardButtons.setBounds(20, 15, 700, 600);
		
		logPane.setBounds(740, 15, 225, 600);
		
		scrollPane.setBounds(20, 630, 700, 80);
		chatInput.setBounds(20, 710, 700, 20);
				
		gamePane.add(boardArrows);
		gamePane.add(boardFigures);
		gamePane.add(boardBackground);
		gamePane.add(boardButtons);

		gamePane.add(logPane);
		log.setLineWrap(true);
		log.setEditable(false);
		
		gamePane.add(scrollPane);
		gamePane.add(chatInput);

		chat.setLineWrap(true);
		chat.setEditable(false);
		
		this.loadPictures();
		
		this.drawBackground();
		this.drawFigures();
		this.drawArrows();
		this.drawButtons();

		gamePane.setVisible(false);

		parent.add(gamePane);
		bindButtons();
	}

	private void loadPictures(){
		try {
			this.iconWhite = new ImageIcon(ImageIO.read(new File("img/field_white.png")));
			this.iconBlack = new ImageIcon(ImageIO.read(new File("img/field_black.png")));
			this.emptyIcon = new ImageIcon(ImageIO.read(new File("img/empty.png")));
			
			this.unknown = new ImageIcon(ImageIO.read(new File(this.themePath + "unknown.png")));
			this.redTrap = new ImageIcon(ImageIO.read(new File(this.themePath + "red_trap.png")));
			this.redFlag = new ImageIcon(ImageIO.read(new File(this.themePath + "red_flag.png")));
			this.redStone = new ImageIcon(ImageIO.read(new File(this.themePath + "red_rock.png")));
			this.redPaper = new ImageIcon(ImageIO.read(new File(this.themePath + "red_paper.png")));
			this.redScissors = new ImageIcon(ImageIO.read(new File(this.themePath + "red_scissor.png")));
			this.blueTrap = new ImageIcon(ImageIO.read(new File(this.themePath + "blue_trap.png")));
			this.blueFlag = new ImageIcon(ImageIO.read(new File(this.themePath + "blue_flag.png")));
			this.blueStone = new ImageIcon(ImageIO.read(new File(this.themePath + "blue_rock.png")));
			this.bluePaper = new ImageIcon(ImageIO.read(new File(this.themePath + "blue_paper.png")));
			this.blueScissors = new ImageIcon(ImageIO.read(new File(this.themePath + "blue_scissor.png")));
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private void drawBackground() {
		GridBagLayout gbl = new GridBagLayout();
		this.boardBackground.setLayout(gbl);
		this.boardBackground.setOpaque(false);
		gbcBackground.fill = GridBagConstraints.HORIZONTAL;

		for (int i = 0; i < 42; i++) {
			this.gbcBackground.gridy = Math.round(i / 7);
			this.gbcBackground.gridx = i % 7;
			this.gbcBackground.gridheight = 1;
			
			if (i % 2 == 0) {
				this.backgroundTiles[i] = new JLabel(this.iconWhite);
				gbl.setConstraints(this.backgroundTiles[i], this.gbcBackground);
				this.boardBackground.add(this.backgroundTiles[i]);
			} else {
				this.backgroundTiles[i] = new JLabel(this.iconBlack);
				gbl.setConstraints(this.backgroundTiles[i], this.gbcBackground);
				this.boardBackground.add(this.backgroundTiles[i]);
			}
		}
	}

	
	private void drawArrows() {
		GridBagLayout gbl = new GridBagLayout();
		this.boardArrows.setLayout(gbl);
		this.boardArrows.setOpaque(false);
		this.gbcArrows.fill = GridBagConstraints.HORIZONTAL;

		for (int i = 0; i < 42; i++) {
			this.arrows[i] = new JLabel(this.emptyIcon);
			this.arrows[i].setOpaque(false);
			
			this.gbcArrows.gridy = Math.round(i / 7);
			this.gbcArrows.gridx = i % 7;
			this.gbcArrows.gridheight = 1;
			
			gbl.setConstraints(this.arrows[i], this.gbcArrows);
			this.boardArrows.add(this.arrows[i]);
		}
	}
	
	private void drawFigures(){		
		GridBagLayout gbl = new GridBagLayout();
		this.boardFigures.setLayout(gbl);
		this.boardFigures.setOpaque(false);
		this.gbcFigures.fill = GridBagConstraints.HORIZONTAL;
		
		for (int i=0; i < 42; i++){
			this.traps[i] = new JLabel(this.emptyIcon);
			this.traps[i].setOpaque(false);
			
			this.gbcFigures.gridy = Math.round(i / 7);
			this.gbcFigures.gridx = i % 7;
			this.gbcFigures.gridheight = 1;

			gbl.setConstraints(this.traps[i], gbcFigures);
			this.boardFigures.add(this.traps[i]);
		}
	}
	
	private void drawButtons() {
		GridBagLayout gbl = new GridBagLayout();
		this.boardButtons.setLayout(gbl);
		gbcButtons.fill = GridBagConstraints.HORIZONTAL;

		for (int i = 0; i < 42; i++) {
			this.fieldButtons[i] = new JButton();
			this.fieldButtons[i].setOpaque(false);
			this.fieldButtons[i].setContentAreaFilled(false);
			this.fieldButtons[i].setBorderPainted(false);
			this.fieldButtons[i].setRolloverEnabled(false);
			this.fieldButtons[i].setPreferredSize(new Dimension(100, 100));
			this.gbcButtons.gridy = Math.round(i / 7);
			this.gbcButtons.gridx = i % 7;
			this.gbcButtons.gridheight = 1;

			gbl.setConstraints(this.fieldButtons[i], gbcButtons);
			this.boardButtons.add(this.fieldButtons[i]);

		}

	}

	private void bindButtons() {
		chatInput.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				boolean isEnter = e.getKeyCode() == KeyEvent.VK_ENTER;
				if (isEnter) {
					addToChat();
					printLog("test");
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

	public void printLog(String message) {
		log.append(message);
		log.append("\n");
		log.setCaretPosition(log.getDocument().getLength());
	}
	
	private void reset() {
		chat.setText(null);
	}

	public void redraw() {
		log.append("> Theme changed");
		log.append("\n");
//		try{
//			
//			
//		} catch (IOException e){
//			e.printStackTrace();
//		}
		
		for (int i=10; i < 20; i++){
			//this.traps[i].setIcon(this.trapIcon);
		}
	}
}