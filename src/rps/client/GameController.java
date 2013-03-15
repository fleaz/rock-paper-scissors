package rps.client;

import static rps.network.NetworkUtil.hostNetworkGame;

import java.rmi.RemoteException;
import java.sql.SQLException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import rps.client.ui.GamePane;
import rps.game.Game;
import rps.game.data.Figure;
import rps.game.data.FigureKind;
import rps.game.data.Player;
import rps.highscore.Highscore;
import rps.highscore.Score;
import rps.network.GameRegistry;
import rps.network.NetworkUtil;

/**
 * this class is responsible for controlling all game related events.
 */
public class GameController implements GameListener {

	private UIController uiController;
	private GamePane gamePane;

	private GameRegistry registry;
	private Player player;
	private Game game;
	
	private JFrame finalScreen = new JFrame();
	
	private int moveCounter = 0;
	
	
	public Player getPlayer() {
		return this.player;
	}

	public void setComponents(UIController uiController, GamePane gamePane) {
		this.uiController = uiController;
		this.gamePane = gamePane;
	}

	public void startHostedGame(Player player, String host) {
		this.player = player;
		registry = hostNetworkGame(host);
		register(player, this);
	}

	public void startJoinedGame(Player player, String host) {
		this.player = player;
		registry = NetworkUtil.requestRegistry(host);
		register(player, this);
	}

	public void startAIGame(Player player, GameListener ai) {
		this.player = player;
		registry = NetworkUtil.hostLocalGame();
		register(new Player(ai.toString()), ai);
		register(player, this);
	}

	private void register(Player player, GameListener listener) {
		try {
			GameListener multiThreadedListener = decorateListener(listener);
			registry.register(multiThreadedListener);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	private static GameListener decorateListener(GameListener listener) {
		try {
			listener = new MultiThreadedGameListener(listener);
			listener = new RMIGameListener(listener);
			return listener;
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public void unregister() {
		try {
			if (registry != null) {
				registry.unregister(player);
			}
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public void surrender() {
		try {
			game.surrender(player);
		} catch (RemoteException e) {
			throw new RuntimeException(e);
		}
	}

	public void resetForNewGame() {
		surrender();
	}

	public void exit() {
		if (registry != null) {
			unregister();
		}
		if (game != null) {
			surrender();
		}
	}

	private void playAgain(int n){
		if(n == 0){
			this.uiController.switchBackToStartup();
		}
		else{
			this.uiController.handleExit();
		}
	}
	
	private int calculateScore(){
		int score=0;
		
		try {
			Figure[] board = this.game.getField();
			
			for(int i=0;i<board.length;i++){
				if(board[i] == null){
					continue;
				}
				else{
					if(board[i].belongsTo(this.player)){
						if(board[i].getKind() == FigureKind.TRAP){ // My trap
							score-=30;
						}else{
							score+=40;
						}
					}
					else{
						if(board[i].getKind() == FigureKind.TRAP){ // Opponents trap
							score+=100;
						}else{
							score-=30;
						}
					}
				}
				
			}
			score+= 300; //additional for winning
			score-= this.moveCounter;
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		if (score >0){
			return score;
		}
		else{
			return 0;
		}
	}
	@Override
	public void chatMessage(Player sender, String message) throws RemoteException {
		gamePane.receivedMessage(sender, message);
	}

	@Override
	public void provideInitialAssignment(Game game) throws RemoteException {
		this.game = game;
		uiController.switchToGamePane();
		gamePane.startGame(player, game);
	}

	@Override
	public void provideInitialChoice() throws RemoteException {
		this.gamePane.askInitial();
	}

	@Override
	public void startGame() throws RemoteException {
	}

	@Override
	public void provideNextMove() throws RemoteException {
		gamePane.redraw();
		gamePane.printTurnInfo("Du bist am Zug");
	}

	@Override
	public void figureMoved() throws RemoteException {
		gamePane.printTurnInfo("Warten auf anderen Spieler");
		this.moveCounter++;
		gamePane.lastMoveArrow();
	}

	@Override
	public void figureAttacked() throws RemoteException {
		gamePane.printFight();
		gamePane.lastMoveArrow();

	}

	@Override
	public void provideChoiceAfterFightIsDrawn() throws RemoteException {
		gamePane.printLog("Unentschieden! Neue Auswahl");
		gamePane.printLog("---");
		gamePane.askAfterDraw();
	}

	@Override
	public void gameIsLost() throws RemoteException {
		gamePane.redraw();
		int n = JOptionPane.showConfirmDialog(
			    this.finalScreen,
			    "Sie haben leider verloren."+
			    "Möchten sie ein neues Spiel starten?",
			    "Game over",
			    JOptionPane.YES_NO_OPTION);
		this.playAgain(n);

	}

	@Override
	public void gameIsWon() throws RemoteException {
		gamePane.redraw();
		int n = JOptionPane.showConfirmDialog(
			    this.finalScreen,
			    "Herzlichen Glückwunsch. Sie haben mit "+calculateScore()+" Punkten gewonnen\n"+
				"Möchten sie ein neues Spiel starten?",
			    "Game over",
			    JOptionPane.YES_NO_OPTION);
		Highscore score = new Highscore();
		try {
			score.insertHighscore(new Score(this.player.getNick(),calculateScore(),this.game.getOpponent(player).getNick()));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		this.playAgain(n);
	}

	@Override
	public void gameIsDrawn() throws RemoteException {
		gamePane.redraw();
		int n = JOptionPane.showConfirmDialog(
			    this.finalScreen,
			    "Das Spiel ist unentschieden."+
			    "Möchten sie ein neues Spiel starten?",
			    "Game over",
			    JOptionPane.YES_NO_OPTION);
		this.playAgain(n);
	}
}