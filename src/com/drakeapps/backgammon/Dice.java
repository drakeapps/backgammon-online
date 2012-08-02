package com.drakeapps.backgammon;

import java.util.Random;


public class Dice {
	public int dice[] = new int[2];
	public int numDice; 
	public boolean hasRolled = false;
	public boolean hasDoubles = false;
	
	Random RNG = new Random();
	
	public void rollDice() {
		int random;
		// Random number 0-5
		random = RNG.nextInt(6);
		// Need 1 more to be 1-6
		dice[0] = random+1;
		
		random = RNG.nextInt(6);
		dice[1] = random+1;
		
		if(dice[0] == dice[1]){
			//DOUBLES!!!
			hasDoubles = true;
			numDice = 4;
		} else {
			hasDoubles = false;
			numDice = 2;
		}
		hasRolled = true;
	}
	/**
	 * Returns current die
	 * @return
	 */
	public int getDie() {
		if(numDice > 1) {
			return dice[0];
		} else {
			return dice[1];
		}
	}
	
	public void useDie() {
		numDice--;
		//dice[0] = dice[1];
	}
	
	public void undoDie() {
		numDice++;
	}
	
	public boolean canMove() {
		return (numDice > 0 && hasRolled);
	}
	/**
	 * Switch first die with second die
	 */
	public boolean switchDice(){
		if(numDice > 1) {
			int temp;
			temp = dice[0];
			dice[0] = dice[1];
			dice[1] = temp;
			return true;
		} else {
			return false;
		}
	}
	public void reset() {
		hasRolled = false;
		hasDoubles = false;
		dice[0] = 0;
		dice[1] = 0;
	}
	public int[] getDiceArray() {
		return dice;
	}
	public void restoreDice(int[] intArray) {
		dice = intArray;
	}
	
	public void loadDice(int [] intArray) {
		dice = intArray;
		
		if(dice[0] == dice[1]){
			//DOUBLES!!!
			hasDoubles = true;
			numDice = 4;
		} else {
			hasDoubles = false;
			numDice = 2;
		}
		hasRolled = true;
	}
}
