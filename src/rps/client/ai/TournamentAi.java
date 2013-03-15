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

	/*
	 * Time limits for tournament mode.
	 * 
	 * 75% of max move duration are used as recursion anchor in the minimax
	 * algorithm.
	 */
	private final int maxDurationForMoveInMilliSeconds;
	//private final int maxDurationForAllMovesInMilliSeconds;
	
	/*
	 * game related objects
	 */
	private Game game;
	private Player player = new Player("Gruppe (#105)");
	
	/*
	 * flags
	 */
	private boolean lastAttackWasDrawn;
	
	private int movesCounter; //Zahl der ausgeführten Züge der KI
	
	/*
	 * discovered statistic
	 */
	private int discoveredRocks, discoveredPapers, discoveredScissors, discoveredTraps;
	private ArrayList<Figure> discoveredFigures;
	
	/*
	 * choice management
	 */
	private ArrayList<FigureKind> lastFigureKindChoices;
	private FigureKind providedInitialChoice;

	private FigureKind providedChoiceAfterDrawnFight;
	private boolean isInitialChoiceAlreadyProvided;

	private boolean isChoiceAfterDrawnFightProvided;
	private boolean lastMoveWasAnAttack;

	/**
<<<<<<< HEAD
	 * Create tournament ai.
=======
	 * Create tournament AI.
>>>>>>> tournament-ai
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
	 * returns the AI player.
	 */
	public Player getPlayer() {
		return this.player;
	}

	@Override
	public void chatMessage(Player sender, String message) throws RemoteException {
		if (!player.equals(sender)) {
			game.sendMessage(player, "you said: " + message);
		}
	}

	/**
	 * Provide an initial assignment.
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
		
		// Liste mit allen eigenen Figuren erstellen
		ArrayList<FigureKind> list = new ArrayList<FigureKind>();
				
		for(int i =0; i<4; i++) {
			list.add(FigureKind.PAPER);
			list.add(FigureKind.ROCK);
			list.add(FigureKind.SCISSORS);
		}
		
		Collections.shuffle(list); // Liste mischen -> zufällige Anordnung
		
		// komplettes Feld
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
		// this means that the other player has choosen the same kind
		if(this.isInitialChoiceAlreadyProvided) {
			this.lastFigureKindChoices.add(this.providedInitialChoice);
		}

		FigureKind initialChoice = BasicAi.randomChoice();
		this.providedInitialChoice = initialChoice;
		this.isInitialChoiceAlreadyProvided = true;
		this.game.setInitialChoice(getPlayer(), initialChoice);
	}

	@Override
	public void startGame() throws RemoteException {}

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
		
		if(this.isChoiceAfterDrawnFightProvided) {
			updateProvidedChoicesStatisticAfterDrawnFight();
			this.isChoiceAfterDrawnFightProvided = false;
		}
		
		if(this.lastMoveWasAnAttack || this.lastAttackWasDrawn) {
			updateDiscoveredStatistic(this.game.getLastMove());
			this.lastMoveWasAnAttack = false;
		}

		//"mini-eröffnungsbuch": 2 first moves für die Ai: zwei figuren in der mitte je 1 feld vor
		/*if(firstMoves==0) {
			if(this.game.getField()[32].getKind().isMovable()) {
				this.game.move(this.player, 32, 25);
			}
			else {
				this.game.move(this.player, 30, 23);
			}
			firstMoves++;
		}
		else if(firstMoves==1) {
			if(this.game.getField()[31].getKind().isMovable()) {
				this.game.move(this.player, 31, 24);
			}
			else {
				this.game.move(this.player, 30, 23);
			}
			firstMoves++;
		}
		*/
		//normaler Zug
		//else {
			Move move = minimax(this.game.getField(), moveCalculationStartedAt);
			this.game.move(this.player, move.getFrom(), move.getTo());
		//}
	}

	@Override
	public void figureMoved() throws RemoteException {}
	
	/**
	 * Called when a figure is attacked.
	 * 
	 * Then the discovered statistic is updated with the figure involved
	 * in the attack.
	 */

	@Override
	public void figureAttacked() throws RemoteException {
		// update statistic about discovered figures
		// updateDiscoveredStatistic(this.game.getLastMove());
		this.lastMoveWasAnAttack = true;
	}


	/**
	 * Called when a choice must be provided after a drawn fight.
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
		
		this.lastAttackWasDrawn = true;
	}
	
	/**
	 * Game is lost.
	 * 
	 * The AI is reseted.
	 */

	@Override
	public void gameIsLost() throws RemoteException {
		resetAI();
	}
	
	/**
	 * Game is won.
	 * 
	 * The AI is reseted.
	 */

	@Override
	public void gameIsWon() throws RemoteException {
		resetAI();
	}
	
	/**
	 * Game is drawn
	 * 
	 * The AI is reseted.
	 */
	@Override
	public void gameIsDrawn() throws RemoteException {
		resetAI();
	}
	
	/**
	 * Print AI Object as String.
	 */

	@Override
	public String toString() {
		return "Tournament AI";
	}
	

	/**
	 * minimax algorithm
	 * 
	 * Is called by provideNextMove to determine a good move for the ai.
	 * 
	 * @param board
	 * @return
	 * @throws RemoteException
	 */
	public Move minimax(Figure[] board, long startTime) throws RemoteException {
		Figure[] newBoard;
		int score = Integer.MIN_VALUE;
		Move result = null;
		
		board = replaceHiddenFiguresWithPossibleRealFigures(board);
		
		for(Move move: BasicAi.getPossibleMoves(board, this.player)) {
			if(move == null) {
				continue;
			} 
			
			newBoard = performMove(board, move);
			int minScore = minValue(newBoard, startTime, Integer.MIN_VALUE, Integer.MAX_VALUE);
			
			if(minScore >= score) {
				result = move;
				score = minScore;
			}
		}

		movesCounter++;
		return result;
	}
	
	
	/**
	 * max-Function of the minimax algorithm.
	 * 
	 * @param board
	 * @param depth
	 * @param alpha
	 * @param beta
	 * @return
	 * @throws RemoteException
	 */
	public int maxValue(Figure[] board, long startTime, int alpha, int beta) throws RemoteException {
		// end-state reached?
		if(terminalTest(board)) {
			return utility(board);
		}
		// stop recursion
		else if(System.nanoTime()-startTime >= 750000L * this.maxDurationForMoveInMilliSeconds) {
			return utility(board);
		}
		
		int v = Integer.MIN_VALUE;
		
		for(Move move: BasicAi.getPossibleMoves(board, this.player)) {
			// skip empty moves (no valid action)
			if(move == null) {
				continue;
			}
			
			// update state
			Figure[] newBoard = performMove(board, move);
			
			// evaluate
			v = Math.max(v, minValue(newBoard, startTime, alpha, beta));
			if(v >= beta) {
				return v;
			} else {
				alpha = Math.max(alpha, v);
			}
		}
		
		return v;
	}
	
	/**
	 * min-Function of the minimax algorithm
	 * 
	 * @param board
	 * @param depth
	 * @param alpha
	 * @param beta
	 * @return
	 * @throws RemoteException
	 */
	public int minValue(Figure[] board, long startTime, int alpha, int beta) throws RemoteException {
		// end-state reached?
		if(terminalTest(board)) {
			return utility(board);
		}
		// stop recursion
		else if(System.nanoTime()-startTime >= 750000L * this.maxDurationForMoveInMilliSeconds) {
			return utility(board);
		}
		
		int v = Integer.MAX_VALUE;

		for(Move move: BasicAi.getPossibleMoves(board, getOpponent())) {
			// skip empty moves (no valid action)
			if(move == null) {
				continue;
			}
			
			// update state
			Figure[] newBoard = performMove(board, move);
			
			// evaluate
			v = Math.min(v, maxValue(newBoard, startTime, alpha, beta));
			if(v <= alpha) {
				return v;
			} else {
				beta = Math.min(beta, v);
			}
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
	public boolean terminalTest(Figure[] board) throws RemoteException {
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
	public int utility(Figure[] board) throws RemoteException {
		int result = 0;

		// return a static value if the game is over
		if(gameIsLost(board)) {
			result = Integer.MIN_VALUE;
		} else if(gameIsDefinitlyWon(board)) {
			result = Integer.MAX_VALUE;
		} else if(gameIsDrawn(board)) {
			result = 0;
		}
		// use a heuristic to score an unfinished game
		else {
			
			for(int i=0; i<board.length; i++) {
				if(board[i] != null) {
					if(board[i].belongsTo(getPlayer())) {
						
						if(movesCounter < 7) {//andere Bewertung für die ersten 7 züge -> Aufbau
							//je weiter vorne eine figur steht, desto höher ihre bewertung
							if(i<7) { // letzte reihe
								result += 80;
							}
							else if(i<14) { // vorletzte Reihe
								result += 70;
							}
							else if(i<21) { // 4. Reihe
								result += 60;
							}
							else if(i<28) { // 3. Reihe
								result -= Math.abs(i - 24); //entfernung vom mittelfeld wird abgezogen -> Ai startet das Vorziehen über die Mitte
								result += 50;
							}
							else if(i<35) { //2. Reihe
								result += 30;
							}
						}
						else { //im weiteren spielverlauf (ab 8. Zug)
							//jede reihe gleichwertig, hauptsache nach vorne
							for(int a=35; a>i; a-=7) {
								result++;
							}							
						}
						
						result += 10000; // +10000 für jede eigene figur auf dem Feld
					} else {
						result -= 10000; // -10000 für jede gegnerische figur auf dem Feld
					}
				}
			}			
		}
		
		return result;
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
	 * Returns a choice for the ai.
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
	 * Returns the opponent player.
	 * 
	 * @return
	 * @throws RemoteException
	 */
	private Player getOpponent() throws RemoteException {
		return this.game.getOpponent(this.player);
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
	 * the flag was not discovered yet (then the opponent has only his trap
	 * and flag remaining.
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
		this.lastFigureKindChoices = new ArrayList<FigureKind>();
		this.providedInitialChoice = null;
		this.isInitialChoiceAlreadyProvided = false;
		this.isChoiceAfterDrawnFightProvided = false;
		this.movesCounter = 0;
	}
}
