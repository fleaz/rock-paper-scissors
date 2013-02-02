package rps.game;

import java.rmi.RemoteException;

import rps.client.GameListener;
import rps.game.data.Figure;
import rps.game.data.FigureKind;
import rps.game.data.Move;
import rps.game.data.Player;

/**
 * The {@code GameImpl} is an implementation for the {@code Game} interface. It
 * contains the necessary logic to play a game.
 */
public class GameImpl implements Game {

	private GameListener listener1;
	private GameListener listener2;

	public GameImpl(Player player1, GameListener listener1, Player player2, GameListener listener2) {
		this.listener1 = listener1;
		this.listener2 = listener2;
	}

	@Override
	public void sendMessage(Player p, String message) throws RemoteException {
		listener1.chatMessage(p, message);
		listener2.chatMessage(p, message);
		// TODO Auto-generated method stub
	}

	@Override
	public void setInitialAssignment(Player p, FigureKind[] assignment) throws RemoteException {
		// TODO Auto-generated method stub
	}

	@Override
	public void setInitialChoice(Player p, FigureKind kind) throws RemoteException {
		// TODO Auto-generated method stub
	}

	@Override
	public void move(Player movingPlayer, int fromIndex, int toIndex) throws RemoteException {
		// TODO Auto-generated method stub
	}

	@Override
	public void setUpdatedKindAfterDraw(Player p, FigureKind kind) throws RemoteException {
		// TODO Auto-generated method stub
	}

	@Override
	public void surrender(Player p) throws RemoteException {
		// TODO Auto-generated method stub
		listener1.chatMessage(p, "I surrender");
		listener2.chatMessage(p, "I surrender");
	}

	@Override
	public Figure[] getField(Player p) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Move getLastMove(Player p) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Player getOpponent(Player p) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}
}