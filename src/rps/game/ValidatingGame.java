package rps.game;

import java.rmi.RemoteException;

import rps.game.data.Figure;
import rps.game.data.FigureKind;
import rps.game.data.Move;
import rps.game.data.Player;

public class ValidatingGame implements Game {

	private final Game game;

	public ValidatingGame(Game game, Player player) throws RemoteException {
		this.game = game;
	}

	@Override
	public void setInitialAssignment(Player p, FigureKind[] assignment) throws RemoteException {
		game.setInitialAssignment(p, assignment);
	}

	@Override
	public Figure[] getField(Player p) throws RemoteException {
		return game.getField(p);
	}

	@Override
	public void move(Player p, int from, int to) throws RemoteException {
		game.move(p, from, to);
	}

	@Override
	public Move getLastMove(Player p) throws RemoteException {
		return game.getLastMove(p);
	}

	@Override
	public void sendMessage(Player p, String message) throws RemoteException {
		game.sendMessage(p, message);
	}

	@Override
	public void setInitialChoice(Player p, FigureKind kind) throws RemoteException {
		game.setInitialChoice(p, kind);
	}

	@Override
	public void setUpdatedKindAfterDraw(Player p, FigureKind kind) throws RemoteException {
		game.setUpdatedKindAfterDraw(p, kind);
	}

	@Override
	public void surrender(Player p) throws RemoteException {
		game.surrender(p);
	}

	@Override
	public Player getOpponent(Player p) throws RemoteException {
		return game.getOpponent(p);
	}
}