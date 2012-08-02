package com.drakeapps.backgammon;

import java.util.ArrayList;
import com.drakeapps.backgammon.GameInfo;

public class GameList {
	ArrayList<GameInfo> list;
	
	/**
	 * How to sort list
	 * Used in getHTML
	 * Default sorting includes isPlayable before non-playable
	 * 
	 * DATE : Sort by date since last play
	 * NAME : Sort by opponents name
	 * ALL  : Sort by date, but not sorted by playable
	 */
	public static final int DATE = 1;
	public static final int NAME = 2;
	public static final int ALL  = 3;
	
	public GameList() {
		list = new ArrayList<GameInfo>();
	}
	
	public void addGame(int id, String opponent, boolean move, String last, int turns, int yourscore, int oppscore) {
		list.add(new GameInfo(id, opponent, move, last, turns, yourscore, oppscore));
	}
	
	public int getSize() {
		return list.size();
	}
	
	public boolean isEmpty() {
		return list.isEmpty();
	}
	/**
	 * Count of playableGames 
	 * @return
	 */
	public int playableGames() {
		int i = 0;
		for(GameInfo game : list) {
			if(game.isPlayable())
				i++;
		}
		return i;
	}
	
	public boolean hasPlayable() {
		for(GameInfo game : list) {
			if(game.isPlayable())
				return true;
		}
		return false;
	}
	
	public String getHTML(int sort) {
		String html = "";
		html += "<html><body><h2>Your Move</h2><ul>";
		
		// Default sort is by date. This is how it's returned from the server
		// We have to return playable games first, then the rest later 
		for(GameInfo game : list) {
			if(game.isPlayable()) {
				html += "<li><a href=\""+String.valueOf(game.getID())+"\">";
				html += "<span class=\"title\">Game vs. "+game.getOpponent()+"</span><br />";
				html += "<span class=\"sub\">"+game.getTurns()+" turns</span>";
				html += "<span class=\"score\">"+game.getYourScore()+" to "+game.getOpponentScore()+"</span>";
				html += "</a></li>";
			}
		}
		html += "</ul><h2>Their Move</h2><ul>";
		for(GameInfo game : list) {
			if(!game.isPlayable()) {
				html += "<li><a href=\""+String.valueOf(game.getID())+"\">";
				html += "<span class=\"title\">Game vs. "+game.getOpponent()+"</span><br />";
				html += "<span class=\"sub\">"+game.getTurns()+" turns</span>";
				html += "<span class=\"score\">"+game.getYourScore()+" to "+game.getOpponentScore()+"</span>";
				html += "</a></li>";
			}
		}
		
		return html;
	}
	public String getHTML() {
		return getHTML(DATE);
	}
	
}
