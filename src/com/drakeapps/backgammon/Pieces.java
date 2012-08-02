package com.drakeapps.backgammon;


import com.drakeapps.backgammon.Backgammon;
/**
 * Right now only color, but using this instead of just int since it could be extended for further use
 * @author James
 *
 */
public class Pieces {

	public int color;
	
	
	
	public Pieces () {
		color = Backgammon.NONE;
	}
	
	public Pieces(int newColor) {
		color = newColor;
	}
}
