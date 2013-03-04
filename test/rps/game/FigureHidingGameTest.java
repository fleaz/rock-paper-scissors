package rps.game;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.rmi.RemoteException;

import org.junit.Before;
import org.junit.Test;

import rps.game.data.Figure;
import rps.game.data.FigureKind;
import rps.game.data.Player;

public class FigureHidingGameTest {

	private Game game;
	private Player thePlayer;
	private Player otherPlayer;

	private Figure[] fields;

	private FigureHidingGame sut;

	@Before
	public void setup() throws RemoteException {

		game = mock(Game.class);

		fields = new Figure[42];
		when(game.getField()).thenReturn(fields);

		thePlayer = new Player("A");
		otherPlayer = new Player("B");

		sut = new FigureHidingGame(game, thePlayer);
	}
	
	// propagation tests for overwritten methods with logic
	
	@Test
	public void moveIsPropagatedToGame() throws RemoteException {
		fields[1] = createFigureForPlayer(thePlayer);
		sut.move(thePlayer, 1, 2);
		verify(game).move(thePlayer, 1, 2);
	}

	@Test
	public void getFieldIsPropagatedToGame() throws RemoteException {
		sut.getField();
		verify(game).getField();
	}

	// figure hiding tests
	
	@Test
	public void ownFiguresAreVisible() throws RemoteException {
		fields[1] = createFigureForPlayer(thePlayer);
		
		assertEquals(FigureKind.PAPER, sut.getField()[1].getKind());
	}
	
	@Test
	public void opponentFiguresAreNotVisible() throws RemoteException {
		fields[1] = createFigureForPlayer(otherPlayer);
		
		assertEquals(FigureKind.HIDDEN, sut.getField()[1].getKind());
	}
	
	@Test
	public void opponentFiguresAreVisibleAfterDraw() throws RemoteException {
		fields[1] = createFigureForPlayer(thePlayer);
		fields[2] = createFigureForPlayer(otherPlayer);
		
		sut.move(thePlayer, 1, 2);
		
		assertEquals(FigureKind.PAPER, sut.getField()[2].getKind());
	}
	
	@Test
	public void opponentFiguresAreVisibleAfterFightWon() throws RemoteException {
		fields[1] = createLosingFigureForPlayer(thePlayer);
		fields[2] = createFigureForPlayer(otherPlayer);
		
		sut.move(thePlayer, 1, 2);
		
		assertEquals(FigureKind.PAPER, sut.getField()[2].getKind());
	}
	
	@Test
	public void lastMoveOwnFiguresAreVisible() throws RemoteException {
		fields[1] = createFigureForPlayer(thePlayer);
		
		sut.move(thePlayer, 1, 2);
		
		Figure[] oldBoard = sut.getLastMove().getOldField();
		int fromIndex = sut.getLastMove().getFrom();
		
		assertEquals(FigureKind.PAPER, oldBoard[fromIndex].getKind());
	}
	
	@Test
	public void lastMoveOpponentFiguresAreVisible() throws RemoteException {
		fields[1] = createFigureForPlayer(otherPlayer);
		
		sut.move(otherPlayer, 1, 2);
		
		Figure[] oldBoard = sut.getLastMove().getOldField();
		int fromIndex = sut.getLastMove().getFrom();
		
		assertEquals(FigureKind.HIDDEN, oldBoard[fromIndex].getKind());
	}
	
	// helpers
	
	private static Figure createFigureForPlayer(Player p) {
		return new Figure(FigureKind.PAPER, p);
	}
	
	private static Figure createLosingFigureForPlayer(Player p) {
		return new Figure(FigureKind.ROCK, p);
	}
}
