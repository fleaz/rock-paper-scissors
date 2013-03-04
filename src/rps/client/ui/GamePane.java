package rps.client.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Container;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import rps.game.Game;
import rps.game.data.Figure;
import rps.game.data.FigureKind;
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
	
	private final JFrame frame = new JFrame();
	private final JFrame frame2 = new JFrame();

	private Game game;
	private Player player;

	private ImageIcon iconWhite;
	private ImageIcon iconBlack;
	private ImageIcon emptyIcon;
	private ImageIcon unknown;
	private ImageIcon redTrap;
	private ImageIcon redFlag;
	private ImageIcon redRock;
	private ImageIcon redPaper;
	private ImageIcon redScissors;
	private ImageIcon blueTrap;
	private ImageIcon blueFlag;
	private ImageIcon blueRock;
	private ImageIcon bluePaper;
	private ImageIcon blueScissors;
	
	private Figure[] board = new Figure[42];
	
	public String themePath = "img/default/";

	private GridBagConstraints gbcBackground = new GridBagConstraints();
	private GridBagConstraints gbcFigures = new GridBagConstraints();
	private GridBagConstraints gbcArrows = new GridBagConstraints();
	private GridBagConstraints gbcButtons = new GridBagConstraints();
	private JLabel[] backgroundTiles = new JLabel[42];
	private JLabel[] arrows = new JLabel[42];
	private JLabel[] figures = new JLabel[42];
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
		gamePane.add(scrollPane);
		gamePane.add(chatInput);

		log.setLineWrap(true);
		log.setEditable(false);
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

	public void askInitial() {
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Object[] options = {"Schere",
                			"Stein",
							"Papier"};
		int n = JOptionPane.showOptionDialog(frame,
				"Kampf um den Start. "
				+ "Womit kaempfst du?",
				"Startkampf",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[1]);
		switch(n){
			case 0:
				this.printLog("> Schere");
				try{
					this.game.setInitialChoice(this.player, FigureKind.SCISSORS);
				}
				catch (RemoteException e){
					//TODO
				}
				break;
			case 1:
				this.printLog("> Stein");
				try{
					this.game.setInitialChoice(this.player, FigureKind.ROCK);
				}
				catch (RemoteException e){
					//TODO
				}
				break;
			case 2:
				this.printLog("> Papier");
				try{
					this.game.setInitialChoice(this.player, FigureKind.PAPER);
				}
				catch (RemoteException e){
					//TODO
				}
				break;
			default:
				this.printLog("> Stein");
				try{
					this.game.setInitialChoice(this.player, FigureKind.ROCK);
				}
				catch (RemoteException e){
					//TODO
				}
				break;
		}
	}

	public void askLineup() {
		this.frame2.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Object[] options = {"Manuell",
							"Zufaellig",
                			"Nur Flagge/Falle manuell"};
		int n = JOptionPane.showOptionDialog(frame,
				"Kampf um den Start. "
				+ "Womit kaempfst du?",
				"Startkampf",
				JOptionPane.YES_NO_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null,
				options,
				options[1]);
		switch(n){
			case 0:
				this.printLog("> Manuell");
				break;
			case 1:
				this.printLog("> Zufaellig");
				ArrayList<FigureKind> list = new ArrayList<FigureKind>();
				
				list.add(FigureKind.TRAP);
				list.add(FigureKind.FLAG);
				
				for(int i =0; i<4; i++) {
					list.add(FigureKind.PAPER);
					list.add(FigureKind.ROCK);
					list.add(FigureKind.SCISSORS);
				}
				
				Collections.shuffle(list); // Liste mischen -> zufällige Anordnung
				
				FigureKind[] initialAssignment = new FigureKind[42];
				
				for(int i = 0; i<list.size(); i++) {
					initialAssignment[i+28] = list.get(i);
				}
				try{
					this.game.setInitialAssignment(this.player, initialAssignment);
				}
				catch (RemoteException e){
					//TODO
				}				
				break;
			case 2:
				this.printLog("> Halb-Manuel");
				break;
			default:
				this.printLog("> Zufaellig");
				break;
		}
		this.redraw();
	}
	
	private void loadPictures(){
		try {
			this.iconWhite = new ImageIcon(ImageIO.read(new File("img/field_white.png")));
			this.iconBlack = new ImageIcon(ImageIO.read(new File("img/field_black.png")));
			this.emptyIcon = new ImageIcon(ImageIO.read(new File("img/empty.png")));
			
			this.unknown = new ImageIcon(ImageIO.read(new File(this.themePath + "unknown.png")));
			this.redTrap = new ImageIcon(ImageIO.read(new File(this.themePath + "red_trap.png")));
			this.redFlag = new ImageIcon(ImageIO.read(new File(this.themePath + "red_flag.png")));
			this.redRock = new ImageIcon(ImageIO.read(new File(this.themePath + "red_rock.png")));
			this.redPaper = new ImageIcon(ImageIO.read(new File(this.themePath + "red_paper.png")));
			this.redScissors = new ImageIcon(ImageIO.read(new File(this.themePath + "red_scissor.png")));
			this.blueTrap = new ImageIcon(ImageIO.read(new File(this.themePath + "blue_trap.png")));
			this.blueFlag = new ImageIcon(ImageIO.read(new File(this.themePath + "blue_flag.png")));
			this.blueRock = new ImageIcon(ImageIO.read(new File(this.themePath + "blue_rock.png")));
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
			this.figures[i] = new JLabel(this.emptyIcon);
			this.figures[i].setOpaque(false);
			
			this.gbcFigures.gridy = Math.round(i / 7);
			this.gbcFigures.gridx = i % 7;
			this.gbcFigures.gridheight = 1;

			gbl.setConstraints(this.figures[i], gbcFigures);
			this.boardFigures.add(this.figures[i]);
		}
	}
	
	private void drawButtons() {
		GridBagLayout gbl = new GridBagLayout();
		this.boardButtons.setLayout(gbl);
		this.boardButtons.setOpaque(false);
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
		this.askLineup();

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
		this.loadPictures();
		try{
			board = this.game.getField();
		}
		catch (RemoteException e){
			//TODO
		}
		for (int i=0; i < 42; i++){
			if(this.board[i] != null){
				if(this.board[i].belongsTo(this.player)){
					switch(this.board[i].getKind()){
						case TRAP:
							this.figures[i].setIcon(blueTrap);
							break;
						case FLAG:
							this.figures[i].setIcon(blueFlag);
							break;
						case ROCK:
							this.figures[i].setIcon(blueRock);
							break;
						case PAPER:
							this.figures[i].setIcon(bluePaper);
							break;
						case SCISSORS:
							this.figures[i].setIcon(blueScissors);
							break;
						case HIDDEN:
							this.figures[i].setIcon(unknown);
							break;
						default:
							this.figures[i].setIcon(unknown);
							break;
					}
				}
				else{
					switch(this.board[i].getKind()){
					case TRAP:
						this.figures[i].setIcon(redTrap);
						break;
					case FLAG:
						this.figures[i].setIcon(redFlag);
						break;
					case ROCK:
						this.figures[i].setIcon(redRock);
						break;
					case PAPER:
						this.figures[i].setIcon(redPaper);
						break;
					case SCISSORS:
						this.figures[i].setIcon(redScissors);
						break;
					case HIDDEN:
						this.figures[i].setIcon(unknown);
						break;
					default:
						this.figures[i].setIcon(unknown);
						break;
					}
				}
			}
				
		}
	}
}