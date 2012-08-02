package com.drakeapps.backgammon;

//import java.util.Random;
import java.util.Stack;
import android.os.Bundle;
import android.util.Log;

import com.drakeapps.backgammon.Dice;
import com.drakeapps.backgammon.Pieces;
import com.drakeapps.backgammon.Score;
import com.drakeapps.backgammon.Backgammon;

public class Game {
	@SuppressWarnings("unchecked")
	public Stack<Pieces> locations[] = new Stack[Backgammon.NUMLOCATIONS];
	public Dice dice = new Dice();
	private Score score = new Score();
	public int yourColor, opponentColor; 
	private int gameType;
	public boolean hasChanged = false;
	private int numMoves;
	
	// move history
	private Stack<Integer> startPos = new Stack<Integer>();
	private Stack<Integer> destPos = new Stack<Integer>();
	private Stack<Integer> countMoves = new Stack<Integer>();
	
	//public Random RNG = new Random();
	
	public Game() {
		for(int i = 0; i < locations.length; i++) {
			locations[i] = new Stack<Pieces>();
		}
	}
	
	
	public void newGame() {
		
		// for testing at least
		// TODO: add other game modes
		gameType = Backgammon.PASSPLAY;
		
		for(int i=0; i<locations.length; i++) {
			locations[i] = new Stack<Pieces>();
		}
		
		// Starting locations of all the pieces
		int white[] = { 1, 1, 12, 12, 12, 12, 12, 17, 17, 17, 19, 19, 19, 19, 19};
		int black[] = { 6, 6, 6, 6, 6, 8, 8, 8, 13, 13, 13, 13, 13, 24, 24};
		
		// testing locations
		// bearing off. check for victory conditions
		// int black[] = { 1, 1, 1, 2, 2, 3, 3, 3, 4, 4, 5, 5, 5, 6, 6 };
		// int white[] = {24,24,24,23,23,22,22,22,21,21,20,20,20,19,19 };
		// this was just for logo screenshot. there aren't enough pieces. there's no way to win
		//int black[] = {25, 20, 20};
		//int white[] = {18, 18, 18, 19, 19, 19, 19, 19, 19, 21, 21, 21};
		// click to win
		//int black[] = { 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 27, 1};
		//int white[] = { 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26, 26,24};
		
		// Add white pieces
		for(int i=0; i < white.length; i++) {
			locations[white[i]].push(new Pieces(Backgammon.WHITE));
		}
		
		// Add black pieces
		for(int i=0; i < black.length; i++) {
			locations[black[i]].push(new Pieces(Backgammon.BLACK));
		}
		
		//yourColor = RNG.nextInt(2) + 1;
		//opponentColor = (yourColor == Backgammon.BLACK) ? Backgammon.WHITE : Backgammon.BLACK;
		/*if(yourColor == Backgammon.BLACK) {
			opponentColor = Backgammon.WHITE;
		} else {
			opponentColor = Backgammon.BLACK;
		}*/
		hasChanged = true;
		numMoves = 0;
		
		dice.rollDice();
		if(dice.dice[0] > dice.dice[1]) {
			yourColor = Backgammon.WHITE;
		} else {
			yourColor = Backgammon.BLACK;
		}
		opponentColor = (yourColor == Backgammon.BLACK) ? Backgammon.WHITE : Backgammon.BLACK;
		startPos = new Stack<Integer>();
		destPos = new Stack<Integer>();
		countMoves = new Stack<Integer>();
		
	}
	
	public boolean loadGame(int gameType, int white[], int black[], int yourColor, int dice[]) {
		if(!(white.length == 15 & black.length == 15)) {
			return false;
		}
		
		// reinit locations
		for(int i=0; i<locations.length; i++) {
			locations[i] = new Stack<Pieces>();
		}
		
		this.gameType = gameType;
		
		// Add white pieces
		for(int i=0; i < white.length; i++) {
			locations[white[i]].push(new Pieces(Backgammon.WHITE));
		}
		
		// Add black pieces
		for(int i=0; i < black.length; i++) {
			locations[black[i]].push(new Pieces(Backgammon.BLACK));
		}
		
		this.dice.loadDice(dice);
		
		this.yourColor = yourColor;
		opponentColor = (yourColor == Backgammon.BLACK) ? Backgammon.WHITE : Backgammon.BLACK;
		
		numMoves = 0;
		
		startPos = new Stack<Integer>();
		destPos = new Stack<Integer>();
		countMoves = new Stack<Integer>();
		
		return true;
	}
	
	public int checkStatus() {
		if(locations[Backgammon.BLACKWIN].size() >= Backgammon.NEEDTOWIN){
			// Black wins
			return Backgammon.BLACK;
		}
		else if(locations[Backgammon.WHITEWIN].size() >= Backgammon.NEEDTOWIN) {
			// White wins
			return Backgammon.WHITE;
		}
		else {
			return Backgammon.NONE;
		}
	}

	// 
	public boolean finishMove() {
		if(gameType == Backgammon.PASSPLAY) {
			int tempColor = yourColor;
			yourColor = opponentColor;
			opponentColor = tempColor;
			dice.reset();
			numMoves = 0;
			return true;
		} 
		
		return false;
		
	}
	
	public boolean finishMove(OnlineGame o) {
		
		if(o.submitBoard(locations)) {
			return true;
		}
		
		return false;
	}
	
	// the logic is ugly in this
	public boolean canMove() {
		int amount = dice.getDie();


		// TRIPPED OFF
		if(yourColor == Backgammon.WHITE) {
			if(locations[Backgammon.WHITESPAWN].size() > 0) {
				if(!(locations[amount].size() == 0 || (locations[amount].size() > 0 && locations[amount].peek().color == yourColor) || (locations[amount].size() == 1 && locations[amount].peek().color == opponentColor))){
					if(dice.switchDice()) {
						amount = dice.getDie();
						if(!(locations[amount].size() == 0 || (locations[amount].size() > 0 && locations[amount].peek().color == yourColor) || (locations[amount].size() == 1 && locations[amount].peek().color == opponentColor))){
							return false;
						}
					} else {
						return false;
					}
				}
			}
		}
		if(yourColor == Backgammon.BLACK) {
			if(locations[Backgammon.BLACKSPAWN].size() > 0) {
				if(!(locations[Backgammon.BLACKSPAWN - amount].size() == 0 || (locations[Backgammon.BLACKSPAWN - amount].size() > 0 && locations[Backgammon.BLACKSPAWN - amount].peek().color == yourColor) || (locations[Backgammon.BLACKSPAWN - amount].size() == 1 && locations[Backgammon.BLACKSPAWN - amount].peek().color == opponentColor))){
					if(dice.switchDice()) {
						amount = dice.getDie();
						if(!(locations[Backgammon.BLACKSPAWN - amount].size() == 0 || (locations[Backgammon.BLACKSPAWN - amount].size() > 0 && locations[Backgammon.BLACKSPAWN - amount].peek().color == yourColor) || (locations[Backgammon.BLACKSPAWN - amount].size() == 1 && locations[Backgammon.BLACKSPAWN - amount].peek().color == opponentColor))){
							return false;
						}
					} else {
						return false;
					}
				}
			}
		}
		
		//if(yourColor == Backgammon.WHITE && canBearOff()){
		//	for(int)
		//}
		
		
		
		/*if(yourColor == Backgammon.WHITE) {
			if(locations[Backgammon.WHITESPAWN].size() > 0) {
				if(!(locations[amount].size() == 0 || (locations[amount].size() > 0 && locations[amount].peek().color == yourColor) || (locations[amount].size() == 1 && locations[amount].peek().color == opponentColor))){
					if(dice.switchDice()) {
						amount = dice.getDie();
						if(!(locations[amount].size() == 0 || (locations[amount].size() > 0 && locations[amount].peek().color == yourColor) || (locations[amount].size() == 1 && locations[amount].peek().color == opponentColor))){
							return false;
						}
					} else {
						return false;
					}
				}
			}
		}
		if(yourColor == Backgammon.BLACK) {
			if(locations[Backgammon.BLACKSPAWN].size() > 0) {
				if(!(locations[Backgammon.BLACKSPAWN - amount].size() == 0 || (locations[Backgammon.BLACKSPAWN - amount].size() > 0 && locations[Backgammon.BLACKSPAWN - amount].peek().color == yourColor) || (locations[Backgammon.BLACKSPAWN - amount].size() == 1 && locations[Backgammon.BLACKSPAWN - amount].peek().color == opponentColor))){
					if(dice.switchDice()) {
						amount = dice.getDie();
						if(!(locations[Backgammon.BLACKSPAWN - amount].size() == 0 || (locations[Backgammon.BLACKSPAWN - amount].size() > 0 && locations[Backgammon.BLACKSPAWN - amount].peek().color == yourColor) || (locations[Backgammon.BLACKSPAWN - amount].size() == 1 && locations[Backgammon.BLACKSPAWN - amount].peek().color == opponentColor))){
							return false;
						}
					} else {
						return false;
					}
				}
			}
		}*/
		
		int limit;
		if(yourColor == Backgammon.WHITE) {
			if(canBearOff(yourColor)) {
				limit = 25;
				return true;
			} else {
				limit = 24;
			}
		} else {
			if(canBearOff(yourColor)) {
				limit = 0;
				return true;
			} else {
				limit = 1;
			}
		}
		
		for(int i=1; i < 25; i++) {
			if(yourColor == Backgammon.WHITE) {
				if(!(amount + i > limit)) {
					Log.v("game", "checking piece "+i+" has size "+locations[i].size());
					if(locations[i].size() > 0 && locations[i].peek().color == yourColor) {
						if(locations[amount+i].size() == 0 || (locations[amount+i].size() > 0 && locations[amount+i].peek().color == yourColor) || (locations[amount+i].size() == 1 && locations[amount+i].peek().color == opponentColor)) {
							Log.v("game", "move at "+i+" with die "+amount);
							return true;
						} 
					}
				}
			} else {
				if((i - amount > limit)) {
					Log.v("game", "checking piece "+i+" has size "+locations[i].size());
					if(locations[i].size() > 0 && locations[i].peek().color == yourColor) {
						if(locations[i - amount].size() == 0 || (locations[i - amount].size() > 0 && locations[i - amount].peek().color == yourColor) || (locations[i - amount].size() == 1 && locations[i - amount].peek().color == opponentColor)) {
							Log.v("game", "move at "+i+" with die "+amount);
							return true;
						} 
					}
				}
			}
				
		}
		
		if(dice.switchDice()) {
			amount = dice.getDie();
			
			for(int i=1; i < 25; i++) {
				if(yourColor == Backgammon.WHITE) {
					if(!(amount + i > limit)) {
						if(locations[i].size() > 0 && locations[i].peek().color == yourColor) {
							if(locations[amount+i].size() == 0 || (locations[amount+i].size() > 0 && locations[amount+i].peek().color == yourColor) || (locations[amount+i].size() == 1 && locations[amount+i].peek().color == opponentColor)) {
								Log.v("game", "move at "+i+" with die "+amount);
								return true;
							} 
						}
					}
				} else {
					if((i - amount > limit)) {
						if(locations[i].size() > 0 && locations[i].peek().color == yourColor) {
							if(locations[i - amount].size() == 0 || (locations[i - amount].size() > 0 && locations[i - amount].peek().color == yourColor) || (locations[i - amount].size() == 1 && locations[i - amount].peek().color == opponentColor)) {
								Log.v("game", "move at "+i+" with die "+amount);
								return true;
							} 
						}
					}
				}
					
			}
		}
		
		return false;
		//return true;
	}
	
	// move piece given it's location
	// amount to move is determined by the function
	public boolean movePiece(int location) {
		int amount = dice.getDie();
		int destination = location + amount;
		
		if(!dice.canMove()) return false;
		if(locations[location].size() < 1) return false;
		if(locations[location].peek().color != yourColor) return false;
		
		
		if(yourColor == Backgammon.WHITE) {
			if(locations[Backgammon.WHITESPAWN].size() > 0 && location != Backgammon.WHITESPAWN) return false;
			if(destination > 25) {
				if(canBearOff(yourColor)) {
					for(int i = (25 - dice.getDie()); i > 19; i--) {
						if(locations[i].size() > 0 && locations[i].peek().color == yourColor) {
							return false;
						}
					}
					score.whiteScored();
					startPos.push(location);
					destPos.push(Backgammon.WHITEWIN);
					countMoves.push(1);
					numMoves++;
					locations[Backgammon.WHITEWIN].push(locations[location].pop());
					return true;
				}
				return false;
			}
			else if(destination == 25) {
				if(canBearOff(yourColor)) {
					score.whiteScored();
					startPos.push(location);
					destPos.push(Backgammon.WHITEWIN);
					countMoves.push(1);
					numMoves++;
					locations[Backgammon.WHITEWIN].push(locations[location].pop());
					return true;
				}
				
			}
			else {
				// pieces there. check if 
				if(locations[destination].size() > 0) {
					if(locations[destination].peek().color != yourColor) {
						if(locations[destination].size() == 1) {
							startPos.push(destination);
							destPos.push(Backgammon.BLACKSPAWN);
							locations[Backgammon.BLACKSPAWN].push(locations[destination].pop());
							startPos.push(location);
							destPos.push(destination);
							countMoves.push(2);
							numMoves++;
							locations[destination].push(locations[location].pop());
							return true;
						}
						return false;
					} else {
						startPos.push(location);
						destPos.push(destination);
						countMoves.push(1);
						numMoves++;
						locations[destination].push(locations[location].pop());
						return true;
					}
				}
				else {
					startPos.push(location);
					destPos.push(destination);
					countMoves.push(1);
					numMoves++;
					locations[destination].push(locations[location].pop());
					return true;
				}
			}
		}
		
		if(yourColor == Backgammon.BLACK) {
			if(locations[Backgammon.BLACKSPAWN].size() > 0 && location != Backgammon.BLACKSPAWN) return false;
			destination = location - amount;
			if(destination < 0) {
				if(canBearOff(yourColor)) {
					for(int i = dice.getDie(); i < 7; i++) {
						if(locations[i].size() > 0 && locations[i].peek().color == yourColor) {
							return false;
						}
					}
					score.blackScored();
					startPos.push(location);
					destPos.push(Backgammon.BLACKWIN);
					countMoves.push(1);
					numMoves++;
					locations[Backgammon.BLACKWIN].push(locations[location].pop());
					return true;
				}
				return false;
			}
			else if(destination == 0) {
				if(canBearOff(yourColor)) {
					score.whiteScored();
					startPos.push(location);
					destPos.push(Backgammon.BLACKWIN);
					countMoves.push(1);
					numMoves++;
					locations[Backgammon.BLACKWIN].push(locations[location].pop());
					return true;
				}
			}
			else {
				// pieces there. check if 
				if(locations[destination].size() > 0) {
					if(locations[destination].peek().color != yourColor) {
						if(locations[destination].size() == 1) {
							startPos.push(destination);
							destPos.push(Backgammon.WHITESPAWN);
							locations[Backgammon.WHITESPAWN].push(locations[destination].pop());
							startPos.push(location);
							destPos.push(destination);
							countMoves.push(2);
							numMoves++;
							locations[destination].push(locations[location].pop());
							return true;
						} else {
							return false;
						}
					} else {
						startPos.push(location);
						destPos.push(destination);
						countMoves.push(1);
						numMoves++;
						locations[destination].push(locations[location].pop());
						return true;
					}
				}
				else {
					startPos.push(location);
					destPos.push(destination);
					countMoves.push(1);
					numMoves++;
					locations[destination].push(locations[location].pop());
					return true;
				}
			}
		}
		return false;
	}
	public boolean undoMove() {
		if(numMoves > 0) {
			locations[startPos.pop()].push(locations[destPos.pop()].pop());
			if(countMoves.pop() > 1) {
				locations[startPos.pop()].push(locations[destPos.pop()].pop());
			}
			numMoves--;
			dice.undoDie();
			//hasChanged = true;
			return true;
		} else {
			return false;
		}
	}
	
	public boolean canBearOff() {
		return canBearOff(yourColor);
	}
	
	public boolean canBearOff(int color) {
		
		if(color == Backgammon.BLACK) {
			for(int i = 7; i < 25; i++) {
				if(locations[i].size() > 0 && locations[i].peek().color == Backgammon.BLACK) {
					return false;
				}
			}
		} else {
			for(int i = 0; i < 18; i++) {
				if(locations[i].size() > 0 && locations[i].peek().color == Backgammon.WHITE) {
					return false;
				}
			}
		}
		Log.v("game", "can bear off");
		return true;
	}
	
	public void restoreGame(Bundle map) {
		
		// Setup board. Reinitialize in case somehow newGame() was already called
		for(int i=0; i<locations.length; i++) {
			locations[i] = new Stack<Pieces>();
		}
		
		// This if statement should protect it against the fc at startup
		// it's crashing b/c the game isn't correctly saved
		// don't know why so i just make sure it doesnt crash
		// better lose a game than crash and lose a game
		if(
				map.containsKey("blackList") && 
				map.containsKey("whiteList") && 
				map.containsKey("locsArray") &&
				map.containsKey("numMoves") &&
				map.containsKey("turn") &&
				map.containsKey("destArray") &&
				map.containsKey("countMoves") &&
				map.containsKey("dice") &&
				map.containsKey("gameType") &&
				map.containsKey("numDice")
			) {
			int pieces[] = map.getIntArray("blackList");
			for(int piece : pieces) {
				locations[piece].add(new Pieces(Backgammon.BLACK));
			}
			pieces = map.getIntArray("whiteList");
			for(int piece : pieces) {
				locations[piece].add(new Pieces(Backgammon.WHITE));
			}
			
			// Restore rest of the game info
			yourColor = map.getInt("turn");
			opponentColor = (yourColor == Backgammon.BLACK) ? Backgammon.WHITE : Backgammon.BLACK;
			
			numMoves = map.getInt("numMoves");
			
			if(numMoves > 0) {
				int locs[] = map.getIntArray("locsArray");
				int dest[] = map.getIntArray("destArray");
				int count[] = map.getIntArray("countMoves");
				
				for(int loc : locs) {
					startPos.push(loc);
				}
				
				for(int det : dest) {
					destPos.push(det);
				}
				
				for(int c : count) {
					countMoves.push(c);
				}
				
			}
			gameType = map.getInt("gameType");
			
			dice.restoreDice(map.getIntArray("dice"));
			dice.numDice = map.getInt("numDice");
			dice.hasRolled = true;
			
			hasChanged = true;
		} 
		else {
			newGame();
		}
		
		
	}
	
	public void saveGame(Bundle map) {
		//Bundle map = new Bundle();
		
		if(gameType == Backgammon.PASSPLAY) {
		
			int blacks[] = new int[15];
			int whites[] = new int[15];
			
			int bcount = 0, wcount = 0;
			for(int i=0; i < this.locations.length; i++) {
				if(locations[i].size() > 0) {
					if(locations[i].peek().color == Backgammon.BLACK) {
						for(int j=0; j<locations[i].size(); j++) {
							blacks[bcount++] = i;
						}
					} else {
						for(int j=0; j<locations[i].size(); j++) {
							whites[wcount++] = i;
						}
					}
				}
			}
			
			map.putIntArray("blackList", blacks);
			map.putIntArray("whiteList", whites);
			map.putInt("turn", yourColor);
			map.putInt("numMoves", numMoves);
			if(numMoves > 0) {
				int locs[] = new int[numMoves*2];
				int dest[] = new int[numMoves*2];
				int count[] = new int[numMoves];
				int j=0;
				
				for(int i=0; i < numMoves; i++) {
					locs[j] = startPos.pop();
					dest[j] = destPos.pop();
					j++;
					count[i] = countMoves.pop();
					
					if(count[i] > 1) {
						locs[j] = startPos.pop();
						dest[j] = destPos.pop();
						j++;
					}
				}
				
				map.putIntArray("locsArray", locs);
				map.putIntArray("destArray", dest);
				map.putIntArray("countMoves", count);
				
			}
			if(!dice.hasRolled)
				dice.rollDice();
			map.putIntArray("dice", dice.getDiceArray());
			map.putInt("numDice", dice.numDice);
			map.putInt("gameType", gameType);
		}
		
	}

	/**
	 * saveToFile and loadFromFile actually just use Strings
	 * 
	 * @return
	 */
	public String saveToFile() {
		String string = "";
		
		if(gameType == Backgammon.PASSPLAY) {
			int blacks[] = new int[15];
			int whites[] = new int[15];
			
			int bcount = 0, wcount = 0;
			for(int i=0; i < this.locations.length; i++) {
				if(locations[i].size() > 0) {
					if(locations[i].peek().color == Backgammon.BLACK) {
						for(int j=0; j<locations[i].size(); j++) {
							blacks[bcount++] = i;
						}
					} else {
						for(int j=0; j<locations[i].size(); j++) {
							whites[wcount++] = i;
						}
					}
				}
			}
			
		
			if(!dice.hasRolled)
				dice.rollDice();
			
			int diceArray[] = dice.getDiceArray();
			
			
			string += yourColor + ";" + numMoves + ";" + diceArray[0] + ";" + diceArray[1] + ";" + dice.numDice + ";" +gameType + "\n";
			
			string += arrayToString(blacks) + "\n";
			
			string += arrayToString(whites);
		
		
		}
		
		return string;
		
	}
	
	public boolean loadFromFile(String file) {
		String lines[] = file.split("\\r?\\n");
		if(lines.length == 3) {
			for(int i=0; i<locations.length; i++) {
				locations[i] = new Stack<Pieces>();
			}
			String line[] = lines[0].split(";");
			if(line.length != 6) {
				Log.e("Game","Wrong first line length");
				return false;
			}
			this.yourColor = Integer.parseInt(line[0]);
			opponentColor = (yourColor == Backgammon.BLACK) ? Backgammon.WHITE : Backgammon.BLACK;
			//this.numMoves  = Integer.parseInt(line[1]);
			this.numMoves = 0;
			dice.numDice   = Integer.parseInt(line[4]);
			this.gameType  = Integer.parseInt(line[5]);
			int diceArray[] = { Integer.parseInt(line[2]), Integer.parseInt(line[3]) };
			dice.restoreDice(diceArray);
			dice.hasRolled = true;
			
			hasChanged = true;
			
			
			
			line = lines[1].split(";");
			if(line.length != 15) {
				Log.e("Game","black count wrong");
				return false;
			}
			
			
			for(String piece : line) {
				locations[Integer.parseInt(piece)].add(new Pieces(Backgammon.BLACK));
			}
			
			
			line = lines[2].split(";");
			if(line.length != 15) {
				Log.e("Game","white count wrong");
				return false;
			}
			for(String piece : line) {
				locations[Integer.parseInt(piece)].add(new Pieces(Backgammon.WHITE));
			}
			
			return true;
			
			
		} else {
			Log.e("Game","Wrong lines");
			return false;
		}
		
	}
	
	// Converts array to ; delimited String
	// e.g.  a[0];a[1];a[2] 
	private String arrayToString(int array[]) {
		String s = "";
		for(int i=0; i < array.length; i++) {
			s += array[i];
			if(i < (array.length - 1)) {
				s += ";";
			}
		}
		
		return s;
	}
}

