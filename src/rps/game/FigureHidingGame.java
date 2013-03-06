package rps.game;

import java.rmi.RemoteException;

import rps.game.data.Figure;
import rps.game.data.FigureKind;
import rps.game.data.Move;
import rps.game.data.Player;

/**
 * This decorator is used to remove all information from get* methods that are
 * not visible for the corresponding player. Most importantly this is the
 * FigureKind of all Figure on the field that are undiscovered yet and do belong
 * to the other player.
 */
public class FigureHidingGame implements Game {

	private final Game game;
	private final Player player;

	public FigureHidingGame(Game game, Player p) throws RemoteException {
		this.game = game;
		this.player = p;
	}

	@Override
	public void setInitialAssignment(Player p, FigureKind[] assignment) throws RemoteException {
		game.setInitialAssignment(p, assignment);
	}

	@Override
	public Figure[] getField() throws RemoteException {
		// clone board with hidden figures
		Figure[] board = this.game.getField();
		Figure[] newBoard = new Figure[42];
		
		for(int i=0; i<42; i++) {
			// skip null fields
			if(board[i] == null) {
				continue;
			}
			
			if(board[i].belongsTo(this.player)
			|| board[i].isDiscovered()) {
				newBoard[i] = board[i].clone();
			} else {
				newBoard[i] = board[i].cloneWithHiddenKind();
			}
		}
		
		return newBoard;
	}

	@Override
	public void move(Player p, int from, int to) throws RemoteException {
		Figure[] board = this.game.getField();
		boolean isAttack = (board[from] != null && board[to] != null);
		// do parent action
		game.move(p, from, to);
		
		// set discovered on attack
		if(isAttack && board[from] != null) {
			board[from].setDiscovered();
		}
		if(isAttack && board[to] != null) {
			board[to].setDiscovered();
		}
	}

	@Override
	public Move getLastMove() throws RemoteException {
		return game.getLastMove();
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