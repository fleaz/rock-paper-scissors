package rps.client.ai;

import java.rmi.RemoteException;

import rps.client.GameListener;
import rps.game.Game;
import rps.game.data.Player;

/**
 * This class contains a very basic AI, that allows to play a game against it.
 * The main benefit is to be able to test the UI.
 */
public class BasicAi implements GameListener {

	private Player player = new Player("Basic AI");
	private Game game;

	@Override
	public void chatMessage(Player sender, String message) throws RemoteException {
		// TODO Auto-generated method stub
		if (!player.equals(sender)) {
			game.sendMessage(player, "you said: " + message);
		}
	}

	@Override
	public void provideInitialAssignment(Game game) throws RemoteException {
		this.game = game;
		// TODO Auto-generated method stub

	}

	@Override
	public void provideInitialChoice() throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startGame() throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void provideNextMove() throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void figureMoved() throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void figureAttacked() throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void provideChoiceAfterFightIsDrawn() throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void gameIsLost() throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void gameIsWon() throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void gameIsDrawn() throws RemoteException {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		return player.getNick();
	}
}