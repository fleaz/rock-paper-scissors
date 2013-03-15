package rps.client.ai;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import rps.client.GameListener;
import rps.game.Game;
import rps.game.data.FigureKind;
import rps.game.data.Player;
import rps.game.data.Move;

/**
 * This class contains a very basic AI, that allows to play a game against it.
 * The main benefit is to be able to test the UI.
 */
public class BasicAi implements GameListener {

	private Player player = new Player("Basic AI");
	private Game game;

	@Override
	public void chatMessage(Player sender, String message) throws RemoteException {
		if (!player.equals(sender)) {
			game.sendMessage(player, "you said: " + message);
		}
	}
	
	/**
	 * Returns the player.
	 */
	public Player getPlayer() {
		return this.player;
	}
	
	/**
	 * Provides a random initial assignment.
	 */
	@Override
	public void provideInitialAssignment(Game game) throws RemoteException {
		game.setInitialAssignment(this.player, createRandomInitialAssignment());
		this.game = game;
	}

	/**
	 * Provides an initial choice.
	 */
	@Override
	public void provideInitialChoice() throws RemoteException {
		this.game.setInitialChoice(this.player, randomChoice());
	}
	
	/**
	 * Called when the game was started.
	 */
	@Override
	public void startGame() throws RemoteException {}
		
	/**
	 * Provides a random move.
	 */
	@Override
	public void provideNextMove() throws RemoteException {
		Move[] possibleMoves = TournamentAi.getPossibleMoves(this.game.getField(), 
				getPlayer(), this.game.getOpponent(getPlayer()));
		
		int amountOfPossibleMoves = 0;
		for(int i=0; i<possibleMoves.length; i++) {
			if(possibleMoves[i] != null) {
				amountOfPossibleMoves++;
			}
		}
		
		Random rand = new Random();
		int randomNumber = rand.nextInt(amountOfPossibleMoves);
		Move move = possibleMoves[randomNumber];
		this.game.move(this.player, move.getFrom(), move.getTo());
	}	
	
	/**
	 * Called when a figure was moved.
	 */
	@Override
	public void figureMoved() throws RemoteException {}

	/**
	 * Called when a figure was attacked.
	 */
	@Override
	public void figureAttacked() throws RemoteException {}

	/**
	 * Provides a random choice after a drawn fight.
	 */
	@Override
	public void provideChoiceAfterFightIsDrawn() throws RemoteException {
		this.game.setUpdatedKindAfterDraw(this.player, randomChoice());
	}

	/**
	 * Called when the game is lost.
	 */
	@Override
	public void gameIsLost() throws RemoteException {
		this.game.sendMessage(this.player, "Herzlichen Glückwunsch, Sie haben gewonnen!");
	}

	/**
	 * Called when the game is won.
	 */
	@Override
	public void gameIsWon() throws RemoteException {
		this.game.sendMessage(this.player, "Sie haben verloren.");
	}

	/**
	 * Called when the game is drawn.
	 */
	@Override
	public void gameIsDrawn() throws RemoteException {
		this.game.sendMessage(this.player, "Unentschieden.");
	}

	/**
	 * Returns the nick of the player.
	 */
	@Override
	public String toString() {
		return player.getNick();
	}
	
	/**
	 * Returns a random choice.
	 * 
	 * @return
	 */
	public static FigureKind randomChoice() {
		Random rand = new Random();
		int randomNumber = rand.nextInt(3) + 1;
		
		FigureKind choice = null;
		switch(randomNumber) {
			case 1:
				choice = FigureKind.SCISSORS;
				break;
			case 2:
				choice = FigureKind.ROCK;
				break;
			case 3:
				choice = FigureKind.PAPER;
				break;
		}
		return choice;
	}
	
	/**
	 * Returns an initial assignment.
	 * 
	 * The assignment is totally random.
	 * 
	 * @return
	 */
	private FigureKind[] createRandomInitialAssignment() {
		ArrayList<FigureKind> list = new ArrayList<FigureKind>();
		
		list.add(FigureKind.TRAP);
		list.add(FigureKind.FLAG);
		
		for(int i =0; i<4; i++) {
			list.add(FigureKind.PAPER);
			list.add(FigureKind.ROCK);
			list.add(FigureKind.SCISSORS);
		}
		
		Collections.shuffle(list);
		
		FigureKind[] initialAssignment = new FigureKind[42];
		
		for(int i = 0; i<list.size(); i++) {
			initialAssignment[i+28] = list.get(i);
		}
		return initialAssignment;
	}

}