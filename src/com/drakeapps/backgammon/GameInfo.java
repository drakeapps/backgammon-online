package com.drakeapps.backgammon;

public class GameInfo {
	private int gameID;
	private String opponentName;
	private boolean yourMove;
	private String lastMove;
	private int turnCount;
	private int yourScore;
	private int opponentScore;
	
	public GameInfo() {
		
	}
	
	public GameInfo(int id, String opponent, boolean move, String last, int turns, int yourscore, int oppscore) {
		gameID = id;
		opponentName = opponent;
		yourMove = move;
		lastMove = last;
		turnCount = turns;
		yourScore = yourscore;
		opponentScore = oppscore;
		
	}
	
	/**
	 * Auto-generated getters and setters
	 * @return
	 */
	
	public int getGameID() {
		return gameID;
	}
	
	public void setGameID(int gameID) {
		this.gameID = gameID;
	}

	public String getOpponentName() {
		return opponentName;
	}

	public void setOpponentName(String opponentName) {
		this.opponentName = opponentName;
	}

	public boolean isYourMove() {
		return yourMove;
	}

	public void setYourMove(boolean yourMove) {
		this.yourMove = yourMove;
	}

	public String getLastMove() {
		return lastMove;
	}

	public void setLastMove(String lastMove) {
		this.lastMove = lastMove;
	}

	public int getTurnCount() {
		return turnCount;
	}

	public void setTurnCount(int turnCount) {
		this.turnCount = turnCount;
	}

	public void setYourScore(int yourScore) {
		this.yourScore = yourScore;
	}
	
	
	public boolean isPlayable() {
		return yourMove;
	}
	
	public int getID() {
		return gameID;
	}
	public String getOpponent() {
		return opponentName;
	}
	public int getTurns() {
		return turnCount;
	}
	public int getYourScore() {
		return yourScore;
	}

	public void setOpponentScore(int opponentScore) {
		this.opponentScore = opponentScore;
	}

	public int getOpponentScore() {
		return opponentScore;
	}
	
}
