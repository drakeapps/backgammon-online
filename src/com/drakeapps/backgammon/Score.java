package com.drakeapps.backgammon;

import com.drakeapps.backgammon.Backgammon;

public class Score {

	
	int blackScore, whiteScore;
	
	public Score() {
		blackScore = 0;
		whiteScore = 0;
	}
	
	public Score(int black, int white) {
		blackScore = black;
		whiteScore = white;
	}
	
	public void addScore(int color) {
		if(color == Backgammon.WHITE) {
			whiteScored();
		}
		else if(color == Backgammon.BLACK) {
			blackScored();
		}
	}
	
	public void whiteScored() {
		whiteScore++;
	}
	public void blackScored() {
		blackScore++;
	}
}
