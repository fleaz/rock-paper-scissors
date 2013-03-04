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
		//game.getOpponent(p);
		
		// leeres Feld in der Startaufstellung -> Exception
		for(int i=41; i>27; i--) {
			if(assignment[i] == null) {
				throw new IllegalArgumentException("Illegal assignment.");
			}
		}
		
		if(assignment.length > 42) {
			throw new IllegalArgumentException("Illegal assignment.");
		}
		else {
			//überprüfen, ob die Figurentypen in der richtigen Anzahl vorkommen (4x ROCK etc.)
			int paperCounter = 0;
			int rockCounter = 0;
			int scissorsCounter = 0;
			int trapCounter = 0;
			int flagCounter = 0;
			for(int i=0; i<assignment.length; i++) {
				if(assignment[i] == FigureKind.PAPER) paperCounter++;
				if(assignment[i] == FigureKind.ROCK	) rockCounter++;
				if(assignment[i] == FigureKind.SCISSORS) scissorsCounter++;
				if(assignment[i] == FigureKind.FLAG) flagCounter++;
				if(assignment[i] == FigureKind.TRAP) trapCounter++;
			}
			if(paperCounter==4 && rockCounter ==4 && scissorsCounter==4 && flagCounter==1 && trapCounter==1) {
				game.setInitialAssignment(p, assignment);
			}
			else {
				throw new IllegalArgumentException("Illegal assignment.");
			}
			
		}
	}

	@Override
	public Figure[] getField(Player p) throws RemoteException {
		return game.getField(p);
	}

	@Override
	public void move(Player p, int from, int to) throws RemoteException {
		//mögliche ungültige Züge:
		// Zug zu gleichem Feld soll durchgeführt werden
		// oder: Zug führt zu negativer Position
		// oder: keine Figur im from-Feld
		// oder: Figur, die bewegt werden soll gehört dem Gegner
		// oder: Figur ist nicht beweglich		
		// oder: eigene Figur soll angegriffen werden
		// oder: Zug über Grenzen des Spielfeldes hinaus (z.B. 6 -> 7)
		if(to==from || to<0 || game.getField(p)[from]== null || !game.getField(p)[from].belongsTo(p) || !game.getField(p)[from].getKind().isMovable()|| game.getField(p)[to]!= null && game.getField(p)[to].belongsTo(p) || (from%7==0 && (to+1)%7==0 || (from+1)%7==0 && to%7==0)) {
			throw new IllegalArgumentException("Illegal move.");
		}
		else {
			game.move(p, from, to);
		}		
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
		if(kind.isMovable()) { 
			game.setInitialChoice(p, kind);
		}
		else {
			throw new IllegalArgumentException("Illegal choice.");
		}
	}

	@Override
	public void setUpdatedKindAfterDraw(Player p, FigureKind kind) throws RemoteException {
		if(kind.isMovable()) {
			game.setUpdatedKindAfterDraw(p, kind);
		}
		else {
			throw new IllegalArgumentException("Illegal choice.");
		}
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