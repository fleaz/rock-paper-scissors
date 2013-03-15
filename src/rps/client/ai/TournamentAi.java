package rps.client.ai;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import rps.client.GameListener;
import rps.game.Game;
import rps.game.data.AttackResult;
import rps.game.data.Figure;
import rps.game.data.FigureKind;
import rps.game.data.Move;
import rps.game.data.Player;

/**
 * This class contains an advanced AI, that should participate in the
 * tournament.
 */
public class TournamentAi implements GameListener {

	/**
	 * Max recursion depth.
	 */
	private int maxDepth = 6;

	/**
	 * Time limits for tournament mode.
	 */
	private final int maxDurationForMoveInMilliSeconds;
	//private final int maxDurationForAllMovesInMilliSeconds;
	
	/**
	 * The game.
	 */
	private Game game;
	
	/**
	 * The player.
	 */
	private Player player = new Player("Gruppe (#105)");
	
	/**
	 * Flag whether the last attack was drawn.
	 */
	private boolean lastAttackWasDrawn;
	
	/**
	 * Discovered statistic.
	 */
	private int discoveredRocks, discoveredPapers, discoveredScissors, discoveredTraps;
	private ArrayList<Figure> discoveredFigures;
	
	/**
	 * Choices statistic.
	 */
	private ArrayList<FigureKind> lastFigureKindChoices;
	private FigureKind providedInitialChoice;

	private FigureKind providedChoiceAfterDrawnFight;
	private boolean isInitialChoiceAlreadyProvided;

	private boolean isChoiceAfterDrawnFightProvided;
	private boolean lastMoveWasAnAttack;

	/**
	 * Create tournament AI.
	 * 
	 * Tournament AI uses the minimax algorithm to provide moves.
	 * The algorithm uses alpha-beta-pruning for optimization.
	 * 
	 * @param maxDurationForMoveInMilliSeconds
	 * @param maxDurationForAllMovesInMilliSeconds
	 */
	public TournamentAi(int maxDurationForMoveInMilliSeconds, int maxDurationForAllMovesInMilliSeconds) {
		this.maxDurationForMoveInMilliSeconds = maxDurationForMoveInMilliSeconds;

		resetAI();
	}
	
	/**
	 * Returns the AI player.
	 */
	@Override
	public Player getPlayer() {
		return this.player;
	}
	

	/**
	 * Returns the opponent player.
	 * 
	 * @return
	 * @throws RemoteException
	 */
	public Player getOpponent() throws RemoteException {
		return this.game.getOpponent(getPlayer());
	}

	/**
	 * Sends a chat message.
	 */
	@Override
	public void chatMessage(Player sender, String message) throws RemoteException {
		if (!player.equals(sender)) {
			game.sendMessage(player, "you said: " + message);
		}
	}

	/**
	 * Provides an initial assignment.
	 * 
	 * In a random column the flag is placed behind a trap. The other
	 * figures are randomly placed on the remaining positions.
	 */
	@Override
	public void provideInitialAssignment(Game game) throws RemoteException {
		this.game = game;
		
		// random flag and trap position
		Random random = new Random();
		int randomFlagPosition = 28 + random.nextInt(7);
		
		// list of remaining figures
		ArrayList<FigureKind> list = new ArrayList<FigureKind>();
				
		for(int i =0; i<4; i++) {
			list.add(FigureKind.PAPER);
			list.add(FigureKind.ROCK);
			list.add(FigureKind.SCISSORS);
		}
		
		// get random distribution
		Collections.shuffle(list);
		
		// create the assignment
		FigureKind[] initialAssignment = new FigureKind[42];
		
		int i=0;
		for(int j=28, k=35; j<35; j++, k++) {
			if(j == randomFlagPosition) {
				initialAssignment[j] = FigureKind.TRAP;
				initialAssignment[k] = FigureKind.FLAG;
				continue;
			}
			
			initialAssignment[j] = list.get(i); // 28 offset for top assignment
			initialAssignment[k] = list.get(i+1); // 28 offset for top assignment
			i += 2;
		}
	
		this.game.setInitialAssignment(this.player, initialAssignment);
		
		// needed if figureAttacked is called after gameIsWon in the previous game
		this.lastMoveWasAnAttack = false;
	}

	/**
	 * Provides an initial choice.
	 * 
	 *  The choice is random. If it is not the first initial choice, the old
	 *  initial choice is added to the last choices statistic.
	 */
	@Override
	public void provideInitialChoice() throws RemoteException {
		// initial choice was already given
		// this means that the other player had chosen the same kind
		if(this.isInitialChoiceAlreadyProvided) {
			this.lastFigureKindChoices.add(this.providedInitialChoice);
		}

		FigureKind initialChoice = BasicAi.randomChoice();
		this.providedInitialChoice = initialChoice;
		this.isInitialChoiceAlreadyProvided = true;
		this.game.setInitialChoice(getPlayer(), initialChoice);
	}

	/**
	 * Called when the game was started.
	 */
	@Override
	public void startGame() throws RemoteException {
		// this is needed when gameIsWon is called before figureAttacked in GameImpl
		this.lastMoveWasAnAttack = false;
	}

	/**
	 * Provide next move.
	 * 
	 * Uses the minimax algorithm to provide a move.
	 */
	@Override
	public void provideNextMove() throws RemoteException {
		long moveCalculationStartedAt = System.nanoTime();
		
		// first move to provide
		if(this.isInitialChoiceAlreadyProvided) {
			updateChoicesStatisticWithInitialChoice(this.game.getField());
			this.isInitialChoiceAlreadyProvided = false;
		}
		
		// update choices statistic after a drawn fight
		if(this.isChoiceAfterDrawnFightProvided) {
			updateProvidedChoicesStatisticAfterDrawnFight();
			this.isChoiceAfterDrawnFightProvided = false;
		}
		
		// update discovered statistic
		if(this.lastMoveWasAnAttack || this.lastAttackWasDrawn) {
			updateDiscoveredStatistic(this.game.getLastMove());
			this.lastMoveWasAnAttack = false;
		}

		// provide the move
		Move move;
		try {
			move = minimax(this.game.getField(), moveCalculationStartedAt);
		} catch(StackOverflowError e) {
			// get a move via minimax algorithm
			// if the recursion doesnt work correctly and the stack overflows,
			// provide a random move to continue the game
			move = getPossibleMoves(this.game.getField(), getPlayer(), getOpponent())[0];
		}
		
		this.game.move(this.player, move.getFrom(), move.getTo());
		
		// time diff
		long timeDiffNanos = System.nanoTime()-moveCalculationStartedAt;
		if(timeDiffNanos >= 850000L * this.maxDurationForMoveInMilliSeconds) {
			this.maxDepth--;
		} else {
			this.maxDepth++;
		}
		
		// output the time it took
		StringBuffer sb = new StringBuffer();
		sb.append("The move took ").append((System.nanoTime()-moveCalculationStartedAt)/1000000).append("ms");
		sb.append(" with depth ").append(this.maxDepth);
		System.out.println(sb);
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
	public void figureAttacked() throws RemoteException {
		this.lastMoveWasAnAttack = true;
	}


	/**
	 * Provides a choice after a drawn fight.
	 * 
	 * The choice is determined with the last provided choices of the
	 * opponent. Although a flag is set so that the new created opponent
	 * figure with the new kind won't be evaluated in the discovered
	 * statistic update method.
	 */
	@Override
	public void provideChoiceAfterFightIsDrawn() throws RemoteException {
		// after drawn fight choices were also drawn
		if(this.isChoiceAfterDrawnFightProvided) {
			this.lastFigureKindChoices.add(this.providedChoiceAfterDrawnFight);
		}
		
		FigureKind choice = getChoiceByLastProvidedChoicesOfTheOpponent();
		this.providedChoiceAfterDrawnFight = choice;
		this.isChoiceAfterDrawnFightProvided = true;
		
		this.game.setUpdatedKindAfterDraw(player, choice);
		
		// important for discovered statistic
		this.lastAttackWasDrawn = true;
	}
	
	/**
	 * Called when the game is lost.
	 */

	@Override
	public void gameIsLost() throws RemoteException {
		resetAI();
	}
	
	/**
	 * Called when the game is won.
	 */

	@Override
	public void gameIsWon() throws RemoteException {
		resetAI();
	}
	
	/**
	 * Called when the game is drawn.
	 */
	@Override
	public void gameIsDrawn() throws RemoteException {
		resetAI();
	}
	
	/**
	 * Returns the nick of the player.
	 */

	@Override
	public String toString() {
		return "Tournament AI";
	}
	

	/**
	 * Returns the possible moves of player.
	 * 
	 * A possible move is a move with an own figure to another field on
	 * the board with distance one that is not blocked by another own figure.
	 * 
	 * @param board
	 * @param player
	 * @return
	 * @throws RemoteException
	 */
	public static Move[] getPossibleMoves(Figure[] board, Player player, Player opponent) throws RemoteException {
		Move[] possibleMoves = new Move[48];  // 12 own movable figures with 4 directions
		int counter = 0;
		
		for(int i=0; i<board.length; i++) {		
			if(board[i] != null && board[i].belongsTo(player) && board[i].getKind().isMovable()) { 	
				try{
					if(getLeftFieldByIndex(board, i).belongsTo(opponent)) {
						possibleMoves[counter] = new Move(i, i-1, board);
						counter++;
					}
				} catch(NullPointerException e) {
					possibleMoves[counter] = new Move(i, i-1, board);
					counter++;
				} catch (IndexOutOfBoundsException e){}	
				
				try{
					if(getRightFieldByIndex(board, i).belongsTo(opponent)) {
						possibleMoves[counter] = new Move(i, i+1, board);
						counter++;
					}
				} catch(NullPointerException e) {
					possibleMoves[counter] = new Move(i, i+1, board);
					counter++;
				} catch (IndexOutOfBoundsException e){}
				
				try{
					if(getUpFieldByIndex(board, i).belongsTo(opponent)) {
						possibleMoves[counter] = new Move(i, i-7, board);
						counter++;
					}
				} catch(NullPointerException e) {
					possibleMoves[counter] = new Move(i, i-7, board);
					counter++;
				} catch (IndexOutOfBoundsException e){}
				
				try{
					if(getDownFieldByIndex(board, i).belongsTo(opponent)) {
						possibleMoves[counter] = new Move(i, i+7, board);
						counter++;
					}
				} catch(NullPointerException e) {
					possibleMoves[counter] = new Move(i, i+7, board);
					counter++;
				} catch (IndexOutOfBoundsException e){}
			}
		}
		
		return possibleMoves;
	}
	
	/**
	 * Returns the field left of i.
	 * 
	 * @param i
	 * @return
	 * @throws IndexOutOfBoundsException
	 * @throws RemoteException
	 */
	private static Figure getLeftFieldByIndex(Figure[] board, int i) throws IndexOutOfBoundsException, RemoteException{
		if(i%7==0) {
			throw new IndexOutOfBoundsException();
		}
		return board[i-1];
	}
	
	/**
	 * Returns the field right of i.
	 * 
	 * @param i
	 * @return
	 * @throws IndexOutOfBoundsException
	 * @throws RemoteException
	 */
	private static Figure getRightFieldByIndex(Figure[] board, int i) throws IndexOutOfBoundsException, RemoteException{
		if((i+1)%7==0) {
			throw new IndexOutOfBoundsException();
		}
		return board[i+1];
	}
	
	/**
	 * Returns the field above i.
	 * 
	 * @param i
	 * @return
	 * @throws IndexOutOfBoundsException
	 * @throws RemoteException
	 */
	private static Figure getUpFieldByIndex(Figure[] board, int i) throws IndexOutOfBoundsException, RemoteException{
		if(i<7) {
			throw new IndexOutOfBoundsException();
		}
		return board[i-7];
	}
	
	/**
	 * Returns the field under i.
	 * 
	 * @param i
	 * @return
	 * @throws IndexOutOfBoundsException
	 * @throws RemoteException
	 */
	private static Figure getDownFieldByIndex(Figure[] board, int i) throws IndexOutOfBoundsException, RemoteException{
		if(i>=35) {
			throw new IndexOutOfBoundsException();
		}
		return board[i+7];
	}	
	
	/**
	 * Minimax algorithm.
	 * 
	 * Is called by provideNextMove to determine a good move for the AI.
	 * 
	 * @param board
	 * @param startTime
	 * @return
	 * @throws RemoteException
	 */
	private Move minimax(Figure[] board, long startTime) throws RemoteException {
		Figure[] newBoard = replaceHiddenFiguresWithPossibleRealFigures(board);;
		
		int score = Integer.MIN_VALUE;
		Move result = null;

		for(Move move: getPossibleMoves(newBoard, getPlayer(), getOpponent())) {
			if(move == null) {
				continue;
			} 
			
			int minScore = minValue(performMove(newBoard, move), startTime, 1, Integer.MIN_VALUE, Integer.MAX_VALUE);
			
			if(minScore >= score) {
				result = move;
				score = minScore;
			}
		}
		
		return result;
	}
	
	/**
	 * max-Function of the minimax algorithm.
	 * 
	 * @param board
	 * @param startTime
	 * @param depth
	 * @param alpha
	 * @param beta
	 * @return
	 * @throws RemoteException
	 */
	private int maxValue(Figure[] board, long startTime, int depth, int alpha, int beta) throws RemoteException {
		if(depth == this.maxDepth || terminalTest(board) || System.nanoTime()-startTime >= 850000L * this.maxDurationForMoveInMilliSeconds) {
			return utility(board);
		}
		
		int v = Integer.MIN_VALUE;

		for(Move move: getPossibleMoves(board, getPlayer(), getOpponent())) {
			// skip empty moves (no valid action)
			if(move == null) {
				return v;
			}
			
			// evaluate
			v = Math.max(v, minValue(performMove(board, move), startTime, depth+1, alpha, beta));
			if(v >= beta) {
				return v;
			}
				
			alpha = Math.max(alpha, v);
		}
		
		return v;
	}
	
	/**
	 * min-Function of the minimax algorithm.
	 * 
	 * @param board
	 * @param startTime
	 * @param depth
	 * @param alpha
	 * @param beta
	 * @return
	 * @throws RemoteException
	 */
	private int minValue(Figure[] board, long startTime, int depth, int alpha, int beta) throws RemoteException {
		if(depth == this.maxDepth || terminalTest(board) || System.nanoTime()-startTime >= 850000L * this.maxDurationForMoveInMilliSeconds) {
			return utility(board);
		}
		
		int v = Integer.MAX_VALUE;

		for(Move move: getPossibleMoves(board, getOpponent(), getPlayer())) {
			// skip empty moves (no valid action)
			if(move == null) {
				return v;
			}
			
			// evaluate
			v = Math.min(v, maxValue(performMove(board, move), startTime, depth+1, alpha, beta));
			if(v <= alpha) {
				return v;
			}
				
			beta = Math.min(beta, v);
		}
		
		return v;
	}
	
	/**
	 * Return whether the game is over.
	 * 
	 * This is the case for a won, lost oder drawn end state.
	 * 
	 * @return
	 * @throws RemoteException 
	 */
	private boolean terminalTest(Figure[] board) throws RemoteException {
		return gameIsDrawn(board) || gameIsDefinitlyWon(board) || gameIsLost(board);
	}
	
	/**
	 * Determines a score for a given state.
	 * 
	 * If the game is lost, drawn or won, a static value is returned.
	 * Otherwise the difference between the own remaining figures and the
	 * opponent remaining figures is calculated.
	 * 
	 * @return
	 * @throws RemoteException 
	 */
	private int utility(Figure[] board) throws RemoteException {
		// return a static value if the game is over
		if(gameIsLost(board)) {
			return Integer.MIN_VALUE;
		} else if(gameIsDefinitlyWon(board)) {
			return Integer.MAX_VALUE;
		} else if(gameIsDrawn(board)) {
			return 0;
		}
		
		// use a heuristic to score an unfinished game	
		int result = 0;
		
		// get flag position
		int flagPosition = 0;
		for(int i=0; i<board.length; i++) {
			if(board[i] != null && board[i].belongsTo(getPlayer()) && board[i].getKind() == FigureKind.FLAG) {
				flagPosition = i;
				break;
			}
		}
		
		//  do same statistics
		int totalFlagDistance = 0, totalDistanceToAllOpponentFigures = 0, rowSum = 0;
		int ownFigureCount = 0, opponentFigureCount = 0;
		for(int i=0; i<board.length; i++) {
			if(board[i] != null) {
				if(board[i].belongsTo(getPlayer())) {
					ownFigureCount++;
					rowSum += i / 7;
				} else {
					totalFlagDistance += distance(flagPosition, i);
					opponentFigureCount++;
					
					for(int j=0; j<board.length; j++) {
						if(board[j] != null && board[j].belongsTo(getOpponent())) {
							totalDistanceToAllOpponentFigures += distance(j, i);
						}
					}
				}
			}
		}
		
		// are the opponent figures near to my flag?
		result += 5 * totalFlagDistance / opponentFigureCount;
		// did I lose a lot figures?
		result += 5 * ownFigureCount;
		// did the opponent lose a lot figures?
		result -= 30 * opponentFigureCount;
		// am I near the opponents figures?
		result -= 5 * totalDistanceToAllOpponentFigures / ownFigureCount / opponentFigureCount;
		// do I go forward?
		result += 25 * rowSum / ownFigureCount;
		
		return result;
	}
	
	/**
	 * Returns the amount of moves to come from a to b.
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private int distance(int a, int b) {
		int columnOfA = a % 7;
		int columnOfB = b % 7;
		
		int columnDistance = Math.abs(columnOfA-columnOfB);
		int rowDistance = Math.abs((a-columnOfA)-(b-columnOfB)) / 7;
		
		return columnDistance + rowDistance;
	}

	/**
	 * Replace hidden figures on a board with possible figures for the player.
	 * 
	 * Uses the discovered figures statistic to replace opponent figures of
	 * hidden kind with figures with a real figure kind. The right amount of
	 * each kind is secured through the discovered figures statistic.
	 * 
	 * @param board
	 * @return
	 * @throws RemoteException
	 */
	private Figure[] replaceHiddenFiguresWithPossibleRealFigures(Figure[] board)
			throws RemoteException {
		// clone old board
		Figure[] newBoard = board.clone();
		
		// create array with remaining figure kinds for hidden figures
		ArrayList<FigureKind> hiddenFigures = new ArrayList<FigureKind>();
		for(int i=this.discoveredRocks; i<4; i++) {
			hiddenFigures.add(FigureKind.ROCK);
		}
		for(int i=this.discoveredPapers; i<4; i++) {
			hiddenFigures.add(FigureKind.PAPER);
		}
		for(int i=this.discoveredScissors; i<4; i++) {
			hiddenFigures.add(FigureKind.SCISSORS);
		}
		if(this.discoveredTraps == 0) {
			hiddenFigures.add(FigureKind.TRAP);
		}
		hiddenFigures.add(FigureKind.FLAG);

		// shuffle array to get a random allocation
		Collections.shuffle(hiddenFigures);
		
		// replace hidden figures with figure kinds from the shuffled array
		int j=0;
		for(int i=0; i<newBoard.length; i++) {
			if(newBoard[i] != null && newBoard[i].getKind() == FigureKind.HIDDEN) {
				newBoard[i] = new Figure(hiddenFigures.get(j), getOpponent());
				j++;
			}
		}
		
		// return board without hidden figures
		return newBoard;
	}
	
	/**
	 * Updates a board with a given move.
	 * 
	 * This is used in the minimax algorithm to update a state. The method is
	 * similar to the move-Method in GameImpl but does not do any listener
	 * calls.
	 * 
	 * @param board
	 * @param move
	 * @return
	 * @throws RemoteException
	 */
	private Figure[] performMove(Figure[] board, Move move) throws RemoteException {
		Figure[] newBoard = board.clone();
		
		int fromIndex = move.getFrom();
		int toIndex = move.getTo();
		
		// normal move
		if(board[toIndex] == null) {
			newBoard[toIndex] = board[fromIndex];
			newBoard[fromIndex] = null;
		}
		// attack
		else {
			FigureKind fromKind = board[fromIndex].getKind();
			FigureKind toKind = board[toIndex].getKind();
			AttackResult attackResult = fromKind.attack(toKind);
			
			if(attackResult == AttackResult.WIN_AGAINST_FLAG || attackResult == AttackResult.WIN) {
				newBoard[toIndex] = board[fromIndex];
				newBoard[fromIndex] = null;
			} else if(attackResult == AttackResult.LOOSE) {
				newBoard[fromIndex] = null;
			} else if(attackResult == AttackResult.LOOSE_AGAINST_TRAP) {
				newBoard[fromIndex] = null;
				newBoard[toIndex] = null;
			} else if(attackResult == AttackResult.DRAW) {
				FigureKind aiChoice = getChoiceByLastProvidedChoicesOfTheOpponent();
				FigureKind opponentChoice = getRandomChoiceWithLastProvidedPrefered();
				
				Figure aiFigure = new Figure(aiChoice, this.player);
				Figure opponentFigure = new Figure(opponentChoice, getOpponent());
				
				if(board[fromIndex].belongsTo(this.player)) {
					newBoard[fromIndex] = aiFigure;
					newBoard[toIndex] = opponentFigure;
				} else {
					newBoard[fromIndex] = opponentFigure;
					newBoard[toIndex] = aiFigure;
				}
				
				newBoard = performMove(newBoard, move);
			}
		}

		return newBoard;
	}
	
	/**
	 * Returns an opponent choice with preference for previous choices.
	 * 
	 * @return
	 */
	private FigureKind getRandomChoiceWithLastProvidedPrefered() {
		Random random = new Random();
		
		int rockChoiceAmount = getRockChoiceCount();
		int paperChoiceAmount = getPaperChoiceCount();
		int scissorsChoiceAmount = getScissorsChoiceCount();
		
		int choiceSum = rockChoiceAmount + paperChoiceAmount + scissorsChoiceAmount;
		int randomNumber = random.nextInt(choiceSum);
		
		FigureKind choice = null;
		
		if(randomNumber < rockChoiceAmount) {
			choice = FigureKind.ROCK;
		} else if(randomNumber < (rockChoiceAmount + paperChoiceAmount)) {
			choice = FigureKind.PAPER;
		} else {
			choice = FigureKind.SCISSORS;
		}
		
		return choice;
	}

	/**
	 * Returns a choice for the AI.
	 * 
	 * The method returns the figure kind that would have one the most times
	 * in the history.
	 * 
	 * @return
	 */
	private FigureKind getChoiceByLastProvidedChoicesOfTheOpponent() {
		int rocksCounter, scissorsCounter, paperCounter;
		FigureKind result = null;
		
		rocksCounter = getRockChoiceCount();
		scissorsCounter = getScissorsChoiceCount();
		paperCounter = getPaperChoiceCount();
		
		if(rocksCounter >= paperCounter && rocksCounter >= scissorsCounter) {
			result = FigureKind.PAPER;
		} else if(paperCounter >= rocksCounter && paperCounter >= scissorsCounter) {
			result = FigureKind.SCISSORS; 
		} else if(scissorsCounter >= paperCounter && scissorsCounter >= rocksCounter) {
			result = FigureKind.ROCK;
		}
		
		return result;
	}

	/**
	 * Get amount of papers in previous opponent choices.
	 * 
	 * @return
	 */
	private int getPaperChoiceCount() {
		int counter=0;
		
		for(int i=0; i<this.lastFigureKindChoices.size(); i++) {
			if(this.lastFigureKindChoices.get(i) == FigureKind.PAPER) {
				counter++;
			}
		}
		
		return counter;
	}

	/**
	 * Get amount of scissors in previous opponent choices.
	 * 
	 * @return
	 */
	private int getScissorsChoiceCount() {
		int counter=0;
		
		for(int i=0; i<this.lastFigureKindChoices.size(); i++) {
			if(this.lastFigureKindChoices.get(i) == FigureKind.SCISSORS) {
				counter++;
			}
		}
		
		return counter;
	}

	/**
	 * Get amount of rocks in previous opponent choices.
	 * 
	 * @return
	 */
	private int getRockChoiceCount() {
		int counter=0;
		
		for(int i=0; i<this.lastFigureKindChoices.size(); i++) {
			if(this.lastFigureKindChoices.get(i) == FigureKind.ROCK) {
				counter++;
			}
		}
		
		return counter;
	}
	
	/**
	 * Determines whether the game is definitely won.
	 * 
	 * Checks whether the opponent has no figures left on the board.
	 * Game could be won even if the method returns false. This is
	 * because the flag is hidden and only undiscovered by the FigureHidingGame
	 * decorator in a real game and not in a simulation. However, if no
	 * figures are left, the game is definitly won.
	 * 
	 * @param board
	 * @return
	 * @throws RemoteException
	 */
	private boolean gameIsDefinitlyWon(Figure[] board) throws RemoteException {
		for(int i=0; i<board.length; i++) {
			if(board[i] != null && board[i].belongsTo(getOpponent())) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Determines whether the game is lost.
	 * 
	 * The game is lost if the own flag is not on the board anymore.
	 * 
	 * @param board
	 * @return
	 */

	private boolean gameIsLost(Figure[] board) {
		boolean gameIsLost = true;
		for(int i=0; i<board.length; i++) {
			if(board[i] != null && board[i].belongsTo(getPlayer()) && board[i].getKind() == FigureKind.FLAG) {
				gameIsLost = false;
			}
		}
		return gameIsLost;
	}
	
	/**
	 * Determines whether the game is drawn.
	 * 
	 * The method checks whether the AI has no movable figures left and the
	 * opponent has only one figure left (the flag) or two figures left and
	 * the trap was not discovered yet (then the opponent has only his trap
	 * and flag remaining).
	 * 
	 * @param board
	 * @return
	 * @throws RemoteException
	 */
	private boolean gameIsDrawn(Figure[] board) throws RemoteException {
		int opponentFigures = 0;
		
		for(int i=0; i<board.length; i++) {
			if(board[i] != null) {
				if(board[i].belongsTo(getPlayer()) && board[i].getKind().isMovable()) {
					return false;
				}
				
				if(board[i].belongsTo(getOpponent())) {
					opponentFigures++;
				}
			}
		}
		
		return (opponentFigures==1 || (opponentFigures==2 && this.discoveredTraps==0));
	}
	
	/**
	 * Updates the last choices statistic with the initial choice.
	 * 
	 * This method is called on the first move provided. If the field
	 * is unmodified, the AI won the initial choice battle and the
	 * opponents choice was the kind loosing against the kind provided
	 * by the AI and this one is added. Otherwise the kind which wins against
	 * the choice provided by the AI is added to the statistic.
	 * 
	 * @param board
	 */
	private void updateChoicesStatisticWithInitialChoice(Figure[] board) {
		boolean boardIsUnmodified = true;
		for(int i=7, j=28; i<14; i++, j++) {
			if(board[i] == null || board[j] == null) {
				boardIsUnmodified = false;
				break;
			}
		}
		
		// AI won
		if(boardIsUnmodified) {
			if(this.providedInitialChoice == FigureKind.PAPER) {
				this.lastFigureKindChoices.add(FigureKind.ROCK);
			} else if(this.providedInitialChoice == FigureKind.ROCK) {
				this.lastFigureKindChoices.add(FigureKind.SCISSORS);
			} else if(this.providedInitialChoice == FigureKind.SCISSORS) {
				this.lastFigureKindChoices.add(FigureKind.PAPER);
			}
		} 
		// opponent won 
		else {
			if(this.providedInitialChoice == FigureKind.PAPER) {
				this.lastFigureKindChoices.add(FigureKind.SCISSORS);
			} else if(this.providedInitialChoice == FigureKind.ROCK) {
				this.lastFigureKindChoices.add(FigureKind.PAPER);
			} else if(this.providedInitialChoice == FigureKind.SCISSORS) {
				this.lastFigureKindChoices.add(FigureKind.ROCK);
			}
		}
	}
	
	/**
	 * Updates the choices statistic with the choice provided after a drawn fight.
	 * 
	 * @throws RemoteException
	 */
	private void updateProvidedChoicesStatisticAfterDrawnFight() throws RemoteException {
		Move lastMove = this.game.getLastMove();
		int fromIndex = lastMove.getFrom();
		int toIndex = lastMove.getTo();
		Figure[] oldBoard = lastMove.getOldField();
		
		FigureKind opponentChoice;
		if(oldBoard[fromIndex].belongsTo(getOpponent())) {
			opponentChoice = oldBoard[fromIndex].getKind();
		} else {
			opponentChoice = oldBoard[toIndex].getKind();
		}
		
		this.lastFigureKindChoices.add(opponentChoice);
		this.isChoiceAfterDrawnFightProvided = false;
	}
	
	
	/**
	 * Update statistic about discovered figures.
	 * 
	 * Is called when an attack happens. The last move is evaluated.
	 * If the opponent figure is in the list of discovered figures,
	 * nothing happens. Otherwise the type of the figure is evaluated
	 * and the corresponding counter is incremented. Then the figure 
	 * is added to the discovered list to prevent statistic updates
	 * on future attacks.
	 * 
	 * @throws RemoteException
	 */
	private void updateDiscoveredStatistic(Move lastMove) throws RemoteException {
		// get information about attack from last move
		Figure[] oldBoard = lastMove.getOldField();
		int fromIndex = lastMove.getFrom();
		int toIndex = lastMove.getTo();
		
		if(this.lastAttackWasDrawn) {
			if(oldBoard[fromIndex].belongsTo(getOpponent())) {
				this.discoveredFigures.add(oldBoard[fromIndex]);
			}
			else {
				this.discoveredFigures.add(oldBoard[toIndex]);
			}
			
			this.lastAttackWasDrawn = false;
			return;
		}
		
		// figure out which kind the involved opponent figure has
		Figure discoveredFigure;
		FigureKind discoveredFigureKind;
		
		if(oldBoard[fromIndex].belongsTo(this.player)) {
			discoveredFigure = oldBoard[toIndex];
		} else {
			discoveredFigure = oldBoard[fromIndex];
		}
		discoveredFigureKind = discoveredFigure.getKind();
		
		// find out whether this figure was already discovered
		for(int i=0; i<this.discoveredFigures.size(); i++) {
			// compare the discovered figure with the 
			// already discovered figures by object address
			if(discoveredFigure == this.discoveredFigures.get(i)) {
				// leave search algorithm
				return;
			}
		}
		
		this.discoveredFigures.add(discoveredFigure);
		
		// increment the type counter
		if(discoveredFigureKind == FigureKind.ROCK) {
			this.discoveredRocks += 1;
		} else if(discoveredFigureKind == FigureKind.PAPER) {
			this.discoveredPapers += 1;
		} else if(discoveredFigureKind == FigureKind.SCISSORS) {
			this.discoveredScissors += 1;
		} else if(discoveredFigureKind == FigureKind.TRAP) {
			this.discoveredTraps += 1;
		}
	}

	/**
	 * Reset the AI state.
	 * 
	 * Resets flags, counters and lists that hold the state of the AI.
	 */
	private void resetAI() {		
		this.discoveredFigures = new ArrayList<Figure>();
		this.discoveredPapers = 0;
		this.discoveredRocks = 0;
		this.discoveredScissors = 0;
		this.discoveredTraps = 0;
		this.lastAttackWasDrawn = false;
		this.lastMoveWasAnAttack = false;
		this.lastFigureKindChoices = new ArrayList<FigureKind>();
		this.providedInitialChoice = null;
		this.providedChoiceAfterDrawnFight = null;
		this.isInitialChoiceAlreadyProvided = false;
		this.isChoiceAfterDrawnFightProvided = false;
	}
}
