package com.drakeapps.backgammon;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
//import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
/*import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.net.Uri;*/
import android.os.Bundle;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Stack;

import com.drakeapps.backgammon.GameList;
import com.drakeapps.backgammon.Backgammon;
import com.drakeapps.backgammon.SimpleSHA1;

public class OnlineGame {
	
	private String device; // unique device id. generated during each login
	private int id; // user id.
	private int status = NOLOGIN;
	
	private int gameID;
	GameList games;
	
	/**
	 * Server domain and url
	 */
	public static final String SERVER = "api.drakeapps.com";
	public static final String REQURL = "http://" + SERVER + "/backgammon/";
	
	public static final String USERAGENT = "Backgammon Online (android) v. " + Backgammon.versionString;
	
	
	HttpClient client = new DefaultHttpClient();
	/**
	 * getLogin states
	 * LOGGEDIN : everything worked. user is logged in
	 * NOLOGIN  : no login for phone. need to create account or add phone to previous account
	 * NOCONNECT: can't connect to server. user not logged in
	 */
	public static final int LOGGEDIN = 1;
	public static final int NOLOGIN = 2;
	public static final int NOCONNECT = 3;
	
	private static byte[] sBuffer = new byte[512];
	
	public OnlineGame() {
		games = new GameList();
		device = new String();
		//id = 1;
		
	}
	
	public OnlineGame(int id, String device) {
		this.id = id;
		this.device = device;
		this.games = new GameList();
	}
	
	public int getLogin() {
		
		
		
		return LOGGEDIN;
	}
	
	public void login() {
		status = getLogin();
	}
	
	public int getUser() {
		return id;
	}
	
	public String getDevice() {
		return device;
	}
	
	public boolean getGames() {
		//URL url = new URL(this.REQURL);
		if(status == LOGGEDIN) {
			HttpGet request = new HttpGet(REQURL+"getgames.php?id="+id);
			request.setHeader("User-Agent", USERAGENT);
			
			try {
				HttpResponse response = client.execute(request);
				
				StatusLine status = response.getStatusLine();
				if(status.getStatusCode() != 200){
					return false;
				}
				
				HttpEntity entity = response.getEntity();
				InputStream inputStream = entity.getContent();
				
				ByteArrayOutputStream content = new ByteArrayOutputStream();
				
				// Read response into a buffered stream
	            int readBytes = 0;
	            while ((readBytes = inputStream.read(sBuffer)) != -1) {
	                content.write(sBuffer, 0, readBytes);
	            }
	            
	            String s = new String(content.toByteArray());
	            
	            for(String split : s.split("\n")) {
	            	String g[] = split.split(";");
	            	if(g.length == 7) {
	            		try {
	            			this.games.addGame(Integer.parseInt(g[0]), g[1], Boolean.parseBoolean(g[2]), g[3], Integer.parseInt(g[4]), Integer.parseInt(g[5]), Integer.parseInt(g[6]));
	            		} catch (Exception e) {
	            			e.printStackTrace();
	            		}
	            	}
	            }
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return true;
	}
	
	public boolean loggedIn() {
		// i don't think i need to check login
		// i'll let my server handle wrong credentials
		//if(status != LOGGEDIN) {
			//if(doLogin()) {
				status = LOGGEDIN;
			//} else {
			//	status = NOLOGIN;
			//}
		//}
		//return (status == LOGGEDIN);
		return true;
	}
	
	private boolean doLogin() {
		// TODO Auto-generated method stub
		HttpGet request = new HttpGet(REQURL+"checklogin.php?user="+id+"&device="+device);
		request.setHeader("User-Agent", USERAGENT);
		try {
			HttpResponse response = client.execute(request);
			
			StatusLine status = response.getStatusLine();
			if(status.getStatusCode() != 200){
				return false;
			}
			
			HttpEntity entity = response.getEntity();
			InputStream inputStream = entity.getContent();
			
			ByteArrayOutputStream content = new ByteArrayOutputStream();
			
			// Read response into a buffered stream
            int readBytes = 0;
            while ((readBytes = inputStream.read(sBuffer)) != -1) {
                content.write(sBuffer, 0, readBytes);
            }
            
            String s = new String(content.toByteArray());
            if(s.equalsIgnoreCase("true")) {
            	return true;
            } else {
            	return false;
            }
            
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void printGames() {
		System.out.println(games.getHTML());
	}

	public void restoreLogin(Bundle map) {
		device = map.getString("loginID");
		id = map.getInt("userID");
	}
	
	public void saveLogin(Bundle map) {
		map.putString("loginID", device);
		map.putInt("userID", id);
	}
	
	public void setLogin(String user, String dev) {
		this.id = Integer.parseInt(user);
		this.device = dev;
	}
	public void setLogin(int user, String dev) {
		this.id = user;
		this.device = dev;
	}
	
	
	public void newLogin() {
		// TODO Auto-generated method stub
		
	}

	public String generateParameters() {
		// TODO Auto-generated method stub
		return "?user=" + id + "&device=" + device + "&v=" + Backgammon.versionInt;
	}

	public void setCurrentGame(String game) {
		// TODO Auto-generated method stub
		
	}

	public boolean loadGame(int gameid, Game g) {
		HttpGet request = new HttpGet(REQURL+"getgame.php?user="+id+"&device="+device+"&game="+gameid);
		request.setHeader("User-Agent", USERAGENT);
		try {
			HttpResponse response = client.execute(request);
			
			StatusLine status = response.getStatusLine();
			if(status.getStatusCode() != 200){
				Log.v("online-game", "server error");
				return false;
			}
			
			HttpEntity entity = response.getEntity();
			InputStream inputStream = entity.getContent();
			
			ByteArrayOutputStream content = new ByteArrayOutputStream();
			
			// Read response into a buffered stream
            int readBytes = 0;
            while ((readBytes = inputStream.read(sBuffer)) != -1) {
                content.write(sBuffer, 0, readBytes);
            }
            
            String s = new String(content.toByteArray());
            
            Log.v("online-game", s);
            
            String lines[] = s.split("\\r?\\n");
            
            if(lines.length != 3) {
            	Log.v("online-game", "wrong line length: " + lines.length);
            	return false;
            	
            }
            String line[] = lines[0].split(";");
            
            if(line.length != 5) {
            	Log.v("online-game", "wrong first line");
            	return false;
            }
            
            int yourColor = Integer.parseInt(line[0]);
            //boolean yourTurn = Boolean.parseBoolean(line[1]);
            //String opponent = line[2];
            int dice[] = new int[2];
            dice[0] = Integer.parseInt(line[3]);
            dice[1]	= Integer.parseInt(line[4]);
            
            
            line = lines[1].split(";");
            String line2[] = lines[2].split(";");
            if(line.length != 15 && line2.length != 15) {
            	Log.v("online-game", "wrong game lines length: " + line.length + "  " + line2.length);
            	return false;
            }
            
            int black[] = new int[15];
            int white[] = new int[15];
            
            for(int i=0; i < 15; i++) {
            	black[i] = Integer.parseInt(line[i]);
            	white[i] = Integer.parseInt(line2[i]);
            }
            
            g.loadGame(Backgammon.ONLINE, white, black, yourColor, dice);
            
            this.gameID = gameid;
            
            return true;
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void transfer(Game game) {
		// TODO Auto-generated method stub
		
	}

	public boolean submitBoard(Stack<Pieces>[] locations) {
		/*int black[] = new int[15];
		int white[] = new int[15];
		int blackCount = 0, whiteCount = 0;*/
		ArrayList<Integer> black = new ArrayList<Integer>();
		ArrayList<Integer> white = new ArrayList<Integer>();
		
		
		
		for(int i=0; i < locations.length; i++) {
			if(locations[i].size() > 0) {
				if(locations[i].peek().color == Backgammon.BLACK) {
					for(int j=0; j < locations[i].size(); j++) {
						black.add(i);
					}
				} else {
					for(int j=0; j < locations[i].size(); j++) {
						white.add(i);
					}
				}
			}
		}
		
		int blackScore = locations[Backgammon.BLACKWIN].size();
		int whiteScore = locations[Backgammon.WHITEWIN].size();
		
		if(black.size() != 15 || white.size() != 15) {
			Log.v("online-game", "location sizes wrong. quitting");
			return false;
		}
		
		String url = REQURL+"submitboard.php?user="+id+"&device="+device;
		
		String blackParam = "";
		String whiteParam = "";
		
		for(int b : black) {
			blackParam += b+";";
		}
		for(int w : white) {
			whiteParam += w+";";
		}
		
		url += "&game="+gameID;
		url += "&black="+blackParam;
		url += "&white="+whiteParam;
		url += "&whitescore="+whiteScore;
		url += "&blackscore="+blackScore;
		url += "&version="+Backgammon.versionInt;
		
		// To prevent cheating by just uploading your own board
		// Has to be black locs, whitelocs, white score, black score, plus key
		// All in one string without spaces
		// if you know this, and the key, you can easily cheat
		// if someone really wants to reverse engineer this enough to do so, then they can cheat all they want
		String hash = "";
		try {
			hash = SimpleSHA1.SHA1(gameID+blackParam+whiteParam+whiteScore+blackScore+"james is awesome");
		} catch (Exception e) {
			e.printStackTrace();
		}
		/*try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.reset();
			String test = 
			md.digest(test.getBytes());
			hash = md.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		
		url += "&hash="+hash; 
		
		
		HttpGet request = new HttpGet(url);
		//HttpPost request = new HttpPost(url);
		//request.setParams(null);
		request.setHeader("User-Agent", USERAGENT);
		try {
			HttpResponse response = client.execute(request);
			
			StatusLine status = response.getStatusLine();
			if(status.getStatusCode() != 200){
				Log.v("online-game", "server status code" + status.getStatusCode());
				return false;
			}
			
			HttpEntity entity = response.getEntity();
			InputStream inputStream = entity.getContent();
			
			ByteArrayOutputStream content = new ByteArrayOutputStream();
			
			// Read response into a buffered stream
            int readBytes = 0;
            while ((readBytes = inputStream.read(sBuffer)) != -1) {
                content.write(sBuffer, 0, readBytes);
            }
            
            String s = new String(content.toByteArray());
            Log.v("online-game", s);
            if(s.equalsIgnoreCase("true")) {
            	return true;
            } else {
            	return false;
            }
            
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return false;
	}

	public void setGameID(int gameID) {
		this.gameID = gameID;
	}

	public int getGameID() {
		return gameID;
	}

	public String saveToFile() {
		
		String s = id + ";" + device;
		
		
		return s;
		
	}
	
	public boolean loadFromFile(String file) {
		String line[] = file.split(";");
		if(line.length != 2) {
			return false;
		}
		this.id = Integer.parseInt(line[0]);
		this.device = line[1];
		
		return true;
		
	}
}
