package rps.game;

import java.rmi.RemoteException;

import rps.client.Application;
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
	public Figure[] getField() throws RemoteException {
		return game.getField();
	}

	@Override
	public void move(Player p, int from, int to) throws RemoteException {
		try {
			if(to==from) {
				throw new IllegalArgumentException("Source is destination!"); // Zug zu gleichem Feld soll durchgeführt werden
			} else if(to<0) {
				throw new IllegalArgumentException("Destination smaler then 0!"); // Zug führt zu negativer Position
			} else if(game.getField()[from]== null) {
				throw new IllegalArgumentException("Source is empty!"); // keine Figur im from-Feld
			} else if(!game.getField()[from].belongsTo(p)) {
				throw new IllegalArgumentException("Source is not an own figure!"); // Figur, die bewegt werden soll gehört dem Gegner
			} else if(!game.getField()[from].getKind().isMovable()) {
				throw new IllegalArgumentException("Can't move unmovable figure!"); // Figur ist nicht beweglich	
			} else if(game.getField()[to]!= null && game.getField()[to].belongsTo(p)) {
				throw new IllegalArgumentException("Can't attack own figure!"); // eigene Figur soll angegriffen werden
			} else if(from%7==0 && (to+1)%7==0 || (from+1)%7==0 && to%7==0) {
				throw new IllegalArgumentException("Move is out of bounds!"); // Zug über Grenzen des Spielfeldes hinaus (z.B. 6 -> 7)
			} else if(Math.abs(to-from) != 1 && Math.abs(to-from) != 7) {
				throw new IllegalArgumentException("Can't move that far!"); // Zug nicht nach links/rechts/oben/unten
			}
			else {
				game.move(p, from, to);
			}		
		} catch(IllegalArgumentException e) {
			Application.showMessage(e.getMessage());
			throw e;
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