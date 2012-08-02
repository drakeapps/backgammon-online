package com.drakeapps.backgammon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

//import java.util.Hashtable;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
//import android.media.AudioManager;
//import android.media.MediaPlayer;
//import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.Toast;


// this file is huge
// rebuildGraphics is twice what it should be in order to support difference screen sizes
// also each piece with a number is a different graphic
// letting android put the text on it was inconsistent and uglier
// this makes the file bigger, the number of files higher, and makes the application use more memory
// but overall, this way works better

public class Backgammon extends Activity {
	
	public static final String versionString = "1.3 (full)";
	public static final int versionInt = 13; // should be pulled from the manifest, but I don't know how to do that
	
	public static final boolean freeVersion = false;
	
	public static final int NONE = 0;
	public static final int BLACK = 1;
	public static final int WHITE = 2;
	
	public static final int NEEDTOWIN = 15;
	public static final int NUMLOCATIONS = 28;
	
	public static final int SINGLE = 1;
	public static final int PASSPLAY = 2;
	public static final int ONLINE = 3;
	
	public static final int WHITESPAWN = 0;
	public static final int BLACKSPAWN = 25;
	
	public static final int WHITEWIN = 26;
	public static final int BLACKWIN = 27;
	
	private static final int NEW_GAME = 1;
	private static final int TOGGLE_VIBRATE = 2;
	private static final int GOTO_MENU = 3;
	private static final int CHANGE_UPDATE = 4;
	
	private static final int WEB_VIEW = 1;
	
	Context context;
	Toast toast;
	
	public static final int UPDATE_TIMES[] = { 5, 15, 30, 60, 240, 0 };
	
	//private static final String SAVEGAME = "game.bgf";
	//private static final String SAVECRED = "online.cred";
	
	//private boolean needNewGame = false;
	private boolean willVibrate = true;
	
	private boolean gameWinning = false;
	
	private boolean yourMove = false;
	
	// the time back has been pressed. This is to enter dev mode, which i might not implement
	private int backTimeCount = 0;
	
	//public Random RNG = new Random();
	
	private Intent myIntent;
	
	public static final String ICICLE_KEY = "drake-apps-backgammon";
	
	/**
	 * GUI States : gameState
	 * MENU     : Showing menu screen
	 * LOCAL    : Playing local game
	 * WEB      : Accessing web info
	 * MULTI    : Playing online game 
	 * SETTINGS : Settings screen
	 * LOADING  : Loading screen
	 */
	public static final int MENU = 1;
	public static final int LOCAL = 2;
	public static final int WEB = 3;
	public static final int MULTI = 4;
	public static final int SETTINGS = 5;
	public static final int LOADING = 6;
	
	
	public static final int PICKED_GAME = 1;
	public static final int LOGGED_IN = 2;
	public static final int FAILED = 3;
	
	
	public static final String PREFS_NAME = "BackgammonPreferences";
	
	public int gameState = MENU;
	
	Vibrator v;
	
	OnlineGame ogame;
	Game game;
	Panel panel;
	
	
	private SoundPoolSoundManager soundManager;
	
	private boolean showSubmit = false;
	
	private int updateTime = 1;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        
    	this.v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);  
    	
    	this.game = new Game();
        this.ogame = new OnlineGame();
    	
        /**
         * Restore the game and online creds
         * Try to restore from the Bundle (faster)
         * Then try to restore from SharedPrefs (slower)
         */
        if(savedInstanceState == null) {
        	if(!loadFile()) 
        		game.newGame();
        } else {
        	Bundle map = savedInstanceState.getBundle(ICICLE_KEY);
        	if(map != null) {
        		game.restoreGame(map);
        		ogame.restoreLogin(map);
        		
        		if(!game.dice.canMove() || !game.canMove()) {
            		//game.finishMove();
            		//game.dice.rollDice();
        			showSubmit = true;
            	}
        		willVibrate = map.getBoolean("willVibrate");
        		updateTime = map.getInt("updateTime");
        		int gState = map.getInt("gameState");
        		if(gState == LOCAL) {
        			gameState = LOCAL;
        		}
        	} else {
        		if(!loadFile()) 
        			game.newGame();
        	}
        }
        context = getApplicationContext();
        
        //stopService(new Intent(this, CheckService.class));
        startService(new Intent(this, CheckService.class));
        
        super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //webview = new WebView(this);
        panel = new Panel(this);
        setContentView(panel);
        
        
        
        
        
        new CheckForMoves().execute(new Object());
        
    }    
    
    /**
     * Load from SharedPrefs
     * 
     * @return
     * false : local game failed to load
     * true  : local game loaded. other values unimportant
     */
    private boolean loadFile() {
		/*try {
			FileInputStream fis = openFileInput(SAVEGAME);
			String s;
			
			fis.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return false;*/
    	Log.v("Backgammon", "loadFile()");
    	
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	String string = settings.getString("gameInfo", "");
    	Log.v("Backgammon", string);
    	if(!game.loadFromFile(string))
    		return false;
    	
    	string = settings.getString("onlineGame", "");
    	Log.v("Backgammon", string);
    	// We don't care if the online game doesn't load. We let the WebView worry about that
    	//if(!ogame.loadFromFile(string))
    		//return false;
    	ogame.loadFromFile(string);
    	if(!game.dice.canMove() || !game.canMove()) {
    		//game.finishMove();
    		//game.dice.rollDice();
			showSubmit = true;
    	}
		willVibrate = settings.getBoolean("willVibrate", true);
		yourMove = settings.getBoolean("yourMove", false);
		updateTime = settings.getInt("updateTime", 1);
		int gState = settings.getInt("gameState", MENU);
		if(gState == LOCAL) {
			gameState = LOCAL;
		}
		
		Log.v("Backgammon", "Game loaded");
		
    	return true;
	}
    
	@Override
    public void onSaveInstanceState(Bundle outState) {
    	Bundle m = new Bundle();
    	game.saveGame(m);
    	ogame.saveLogin(m);
    	m.putBoolean("willVibrate", willVibrate);
    	m.putBoolean("showSubmit", showSubmit);
    	m.putInt("gameState", gameState);
    	m.putInt("updateTime", updateTime);
    	
    	outState.putBundle(ICICLE_KEY, m);
    }
    
    
    @Override
	protected void onStop() {
    	
		/* Writes to file output. sharedpreferences might be a better idea.
		 * String string = game.saveToFile();
    	try {
    		FileOutputStream fos = openFileOutput(SAVEGAME, MODE_PRIVATE);
    		fos.write(string.getBytes());
    		fos.close();
    	} catch( Exception e) {
    		e.printStackTrace();
    	}
		
		string = ogame.saveToFile();
    	try {
    		FileOutputStream fos = openFileOutput(SAVECRED, MODE_PRIVATE);
    		fos.write(string.getBytes());
    		fos.close();
    	} catch( Exception e) {
    		e.printStackTrace();
    	}*/
    	
    	super.onStop();
    	
    	Log.v("Backgammon", "onStop");
    	
    	stopService(new Intent(this, CheckService.class));
        startService(new Intent(this, CheckService.class));
    	
    	SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
    	SharedPreferences.Editor editor = settings.edit();
    	
    	editor.putBoolean("willVibate", willVibrate);
    	editor.putBoolean("showSubmit", showSubmit);
    	editor.putBoolean("yourMove", yourMove);
    	editor.putInt("gameState", gameState);
    	editor.putInt("updateTime", updateTime);
    	
    	String string = game.saveToFile();
    	Log.v("Backgammon", string);
    	if(!string.equals(""))
    		editor.putString("gameInfo", string);
    	
    	string = ogame.saveToFile();
    	Log.v("Backgammon", string);
    	editor.putString("onlineGame", string);
    	
    	editor.commit();
    	
		super.onPause();
	}
    
    /**
     * Returning of the WebView
     * If you picked a game. Load it
     * Else go to the menu
     */
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	   	
    	Log.v("backgammon", "got stuffs back. now test it");
    	
    	if(resultCode == RESULT_OK) {
    		
    		gameState = LOADING;
    		panel.rebuildGraphics();
    		
    		Bundle m = intent.getExtras();
    		
    		Log.v("backgammon", "result ok. try loading online game");
    		
    		ogame.setLogin(m.getInt("user"), m.getString("device"));
    		//SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        	//SharedPreferences.Editor editor = settings.edit();
        	//String string = ogame.saveToFile();
        	//editor.putString("onlineGame", string);
        	//editor.commit();
    		
    		Log.v("backgammon", "user: "+m.getInt("user") + " game: "+m.getInt("game") + " device: "+m.getString("device"));
    		
    		new CheckForMoves().execute(new Object());
    		
    		new LoadOnlineGame().execute(m);
    		
    		/*if(ogame.loadGame(m.getInt("game"), game)) {
    			// show new game
    			Log.v("backgammon", "game loading worked");
    			showSubmit = false;
    			this.gameState = MULTI;
    			panel.rebuildGraphics();
    		} else {
    			// go back to menu
    			Log.v("backgammon", "game loading failed. Show menu");
    			this.gameState = MENU;
    			panel.rebuildGraphics();
    		}*/
    	}
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, GOTO_MENU, 0, "Menu");
    	menu.add(0, NEW_GAME, 1, "New Game");
    	menu.add(1, TOGGLE_VIBRATE, 2, "Toggle Sound/Vibrate");
    	menu.add(1, CHANGE_UPDATE, 3, "Update Frequency");
    	return true;
    }
    
    /**
     * This has to be outside of panel for some reason
     * 
     */
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {			
    		if(gameState == LOCAL || gameState == MULTI) {
				gameState = MENU;
				// save the game since if you start it again, it will load from this
				// it loads from this, because it has to get rid of the online game if there is one
				// maybe putting an option to only load the game if an online game has been previously opened
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		    	SharedPreferences.Editor editor = settings.edit();
		    	String string = game.saveToFile();
		    	Log.v("Backgammon", string);
		    	if(!string.equals(""))
		    		editor.putString("gameInfo", string);
		    	
		    	editor.commit();
				panel.rebuildGraphics(); 
				return true;
			} else if(gameState == MENU) {
				finish();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}


	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			if(backTimeCount > 5000) {
				
			}
			/*if(gameState == LOCAL || gameState == ONLINE) {
				gameState = MENU;
				rebuildGraphics();
				return true;
			} else if(gameState == MENU) {
				finish();
				return true;
			}*/
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	
    	switch(item.getItemId()) {
    	case GOTO_MENU:
    		this.gameState = MENU;
    		panel.rebuildGraphics();
    		return true;
    	
    	case NEW_GAME:
    		// the below is a lie. i fixed that a while back
    		// Kinda ugly
    		// Can't call the panel class directly to start new game, so it just checks needNewGame
    		if(gameState == LOCAL)
    			panel.newGame();
    		//needNewGame = true;
    		return true;
    		
    	case TOGGLE_VIBRATE:
    		willVibrate = !willVibrate;
    		if(willVibrate)
    			v.vibrate(400);
    		return true;
    	
    	case CHANGE_UPDATE:
    		updateTime++;
    		if(updateTime >= UPDATE_TIMES.length)
    			updateTime = 0;
    		
    		if(UPDATE_TIMES[updateTime] == 0) {
    			panel.showToast("Background notifications turned off");
    		} else {
    			panel.showToast("Will check every " + UPDATE_TIMES[updateTime] + " minutes");
    		}
    		
    		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        	SharedPreferences.Editor editor = settings.edit();
        	
        	editor.putInt("updateTime", updateTime);
        	
        	editor.commit();
        	stopService(new Intent(this, CheckService.class));
    		startService(new Intent(this, CheckService.class));
    		return true;
    		
    	default:
        	return super.onOptionsItemSelected(item);
    	}
    } 
    
    /**
     * Function to start the WebView
     */
    public void startWebView() {
    	Log.v( "backgammon", "starting webview");
    	// not sure if this is causing the menu to be unresponsive coming back from WebView
    	// yeah, pretty sure it was. gamestate was set but immediately changed back to this during onLoad
    	//gameState = WEB;
    	
    	myIntent = new Intent(this, OnlineView.class);
		startActivityForResult(myIntent, WEB_VIEW);
    }
    /**
     * open the market for full version
     */
    public void openMarket() {
    	String market = "market://details?id=com.drakeapps.backgammon";
    	try {
    		Intent i = new Intent(Intent.ACTION_VIEW);
    		i.setData(Uri.parse(market));
    		startActivity(i);
    	} catch(Exception e) {
    		// if you don't have the market, it'll throw an error
    		// android.content.ActivityNotFoundException
    		e.printStackTrace();
    	}
    }
    
    public void openTutorial() {
    	String tutorialURL = "http://drakeapps.com/backgammontutorial.php";
    	try {
    		Intent i = new Intent(Intent.ACTION_VIEW);
    		i.setData(Uri.parse(tutorialURL));
    		startActivity(i);
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    }
    
    // Panel and Backgammon are pretty well integrated
    // so they're going to stay in the same file
    
    /**
     * The main part
     * 
     * Draws the Backgammon board and menu
     * Interprets the user inputs
     * 
     * @author James
     *
     */
    class Panel extends SurfaceView implements SurfaceHolder.Callback {
        private GameThread _thread;
        private ArrayList<GraphicObject> _graphics = new ArrayList<GraphicObject>();
        private HashMap<Integer, Bitmap> bitmaps = new HashMap<Integer, Bitmap>();
        
        //private Game game = new Game();
        //private boolean doneFirst = false; 
        
        private double size;
        private double size2;
        
        
        // OH GOD I HOPE THE JAVA COMPILER OPTIMIZES THESE
        
        public static final int dice1 = 10;
        public static final int dice2 = 11;
        public static final int dice3 = 12;
        public static final int dice4 = 13;
        public static final int dice5 = 14;
        public static final int dice6 = 15;
        public static final int dicex = 16;
        public static final int dicearrow = 17;
        
        public static final int board = 1;
        
        public static final int whitepiece = 2;
        public static final int blackpiece = 3;
        public static final int titlescreen = 4;
        public static final int titlescreenyourmove = 18;
        public static final int adbar = 19;
        public static final int loading = 20;
        
        public static final int undobutton = 5;
        public static final int piecehighlight = 6;
        public static final int whitewins = 7;
        public static final int blackwins = 8;
        public static final int submitbutton = 9;
        public static final int youwin = 100;
        
        public static final int piece0 = 50;
        public static final int piece1 = 51;
        public static final int piece2 = 52;
        public static final int piece3 = 53;
        public static final int piece4 = 54;
        public static final int piece5 = 55;
        public static final int piece6 = 56;
        public static final int piece7 = 57;
        public static final int piece8 = 58;
        public static final int piece9 = 59;
        public static final int piece10 = 60;
        public static final int piece11 = 61;
        public static final int piece12 = 62;
        public static final int piece13 = 63;
        public static final int piece14 = 64;
        public static final int piece15 = 65;
        
        public static final int piecewhite0 = 70;
        public static final int piecewhite1 = 71;
        public static final int piecewhite2 = 72;
        public static final int piecewhite3 = 73;
        public static final int piecewhite4 = 74;
        public static final int piecewhite5 = 75;
        public static final int piecewhite6 = 76;
        public static final int piecewhite7 = 77;
        public static final int piecewhite8 = 78;
        public static final int piecewhite9 = 79;
        public static final int piecewhite10 = 80;
        public static final int piecewhite11 = 81;
        public static final int piecewhite12 = 82;
        public static final int piecewhite13 = 83;
        public static final int piecewhite14 = 84;
        public static final int piecewhite15 = 85;
        
        public static final int piecewhite1small = 21;
        public static final int piecewhite2small = 22;
        public static final int piecewhite3small = 23;
        public static final int piecewhite4small = 24;
        public static final int piecewhite5small = 25;
        public static final int piecewhite6small = 26;
        public static final int piecewhite7small = 27;
        public static final int piecewhite8small = 28;
        public static final int piecewhite9small = 29;
        public static final int piecewhite10small = 90;
        public static final int piecewhite11small = 91;
        public static final int piecewhite12small = 92;
        public static final int piecewhite13small = 93;
        public static final int piecewhite14small = 94;
        public static final int piecewhite15small = 95;
        
        public static final int piece1small = 31;
        public static final int piece2small = 32;
        public static final int piece3small = 33;
        public static final int piece4small = 34;
        public static final int piece5small = 35;
        public static final int piece6small = 36;
        public static final int piece7small = 37;
        public static final int piece8small = 38;
        public static final int piece9small = 39;
        public static final int piece10small = 40;
        public static final int piece11small = 41;
        public static final int piece12small = 42;
        public static final int piece13small = 43;
        public static final int piece14small = 44;
        public static final int piece15small = 45;
        
        
        public static final int SOUND_CLICK = 1;
        public static final int SOUND_WIN = 2;
        
        private int screenWidth;
        private int screenHeight;
        
        //private SoundPool soundPool;
        //private HashMap<Integer, Integer> soundPoolMap;
        
        
        public Panel(Context context) {
            super(context);
            getHolder().addCallback(this);
            _thread = new GameThread(getHolder(), this);
            setFocusable(true);
            
            // Check for high res display
            if(getContext().getResources().getDisplayMetrics().densityDpi >= 190) {
            	size = 1.5; 
            	size2 = 1.5;
            } else if(getContext().getResources().getDisplayMetrics().densityDpi <= 130) {
            	size = .65;
            	size2 = .75;
            } else {
            	size = 1;
            	size2 = 1;
            }
            
            screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
            screenHeight = getContext().getResources().getDisplayMetrics().heightPixels;
            
            // load sounds
            
            soundManager = new SoundPoolSoundManager(context);
            soundManager.init();
            
            
            loadImages();
            
            
            rebuildGraphics();
            
        }
        
        
        public void newGame() {
        	synchronized (_thread.getSurfaceHolder()) {
	        	game.newGame();
	        	game.dice.rollDice();
	        	showSubmit = false;
	        	gameWinning = false;
	        	rebuildGraphics();
        	}
        	//needNewGame = false;
        }
        
        public GraphicObject getDice(int dice) {
        	if(dice == 1) {
        		return new GraphicObject(getImage(dice1));
        	}
        	if(dice == 2) {
        		return new GraphicObject(getImage(dice2));
        	}
        	if(dice == 3) {
        		return new GraphicObject(getImage(dice3));
        	}
        	if(dice == 4) {
        		return new GraphicObject(getImage(dice4));
        	}
        	if(dice == 5) {
        		return new GraphicObject(getImage(dice5));
        	}
        	if(dice == 6) {
        		return new GraphicObject(getImage(dice6));
        	}
        	return new GraphicObject(getImage(dicex));
        }  
        
        
        
        
        public void showToast(CharSequence text) {
        	
    		int duration = Toast.LENGTH_SHORT;
    		toast = Toast.makeText(context, text, duration);
    		toast.show();
        }


		/**
         * The screen was touched!!
         * The locations were manually done
         * Not the most elegant way, but the most consistent
         */
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            synchronized (_thread.getSurfaceHolder()) {
                
            	if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    /*GraphicObject graphic = new GraphicObject(BitmapFactory.decodeResource(getResources(), R.drawable.redchecker));
                    graphic.getCoordinates().setX((int) event.getX() - graphic.getGraphic().getWidth() / 2);
                    graphic.getCoordinates().setY((int) event.getY() - graphic.getGraphic().getHeight() / 2);
                    _graphics.add(graphic);*/
            		
            		int _x, _y;
            		_x = (int) event.getX();
                	_y = (int) event.getY();
                	
            		if(gameState == MENU) {
            			if(size > 1) {
            				if(300 < _y && _y < 375) {
	            				if(257 < _x && _x < 413) {
	            					gameState = LOCAL;
	            					SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	            			    	String string = settings.getString("gameInfo", "");
	            			    	Log.v("Backgammon", string);
	            			    	if(!game.loadFromFile(string))
	            			    		game.newGame();
	            					rebuildGraphics();
	            				} else if(68 < _x && _x < 222) {
	            					startWebView();
	            				} else if(450 < _x && _x < 630) {
	            					openTutorial();
	            				}
            				}
            			}
            			else if(size < 1) {
            				if(140 < _y && _y < 180) {
	            				if(105 < _x && _x < 190) {
	            					gameState = LOCAL;
	            					SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	            			    	String string = settings.getString("gameInfo", "");
	            			    	Log.v("Backgammon", string);
	            			    	if(!game.loadFromFile(string))
	            			    		game.newGame();
	            					rebuildGraphics();
	            				} else if(9 < _x && _x < 90) {
	            					startWebView();
	            				} else if(200 < _x && _x < 300) {
	            					openTutorial();
	            				}
            				}
            			}
            			else {
            				if(200 < _y && _y < 250) {
	            				if(170 < _x && _x < 280) {
	            					gameState = LOCAL;
	            					SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
	            			    	String string = settings.getString("gameInfo", "");
	            			    	Log.v("Backgammon", string);
	            			    	if(!game.loadFromFile(string))
	            			    		game.newGame();
	            					rebuildGraphics();
	            				} else if(45 < _x && _x < 150) {
	            					startWebView();
	            				} else if(298 < _x && _x < 422) {
	            					openTutorial();
	            				}
            				}
            			}
            		} else if(gameState == SETTINGS) {
            			
            		} else {
            			
	            		
	                	
	                	//GraphicObject board = new GraphicObject(BitmapFactory.decodeResource(getResources(), R.drawable.boardnew));
	                	GraphicObject board = new GraphicObject(getImage(Panel.board));
            			int boardHeight = board.getGraphic().getHeight();
	                	//int boardWidth = board.getGraphic().getWidth();
	                	
	                	
	                	
	                	if(showSubmit) {
	                		if(_x > 420*size && 132*size2 < _y && _y < 168*size2) {
	            				if(game.undoMove()) {
		            				showSubmit = false;
		            				rebuildGraphics();
	            				}
	                		}
	                		if(100*size < _x && _x < 330*size && 100*size2 < _y && _y < 200*size2) {
	                			if(gameState == LOCAL) {
		                			game.finishMove();
			                		game.dice.rollDice();
			                		showSubmit = false;
			                		rebuildGraphics();
	                			} else if(gameState == MULTI) {
	                				//game.finishMove(ogame);
	                				showToast("Submitting...");
	                				new SubmitBoard().execute(new String());
	                				showSubmit = false;
	                				gameState = MENU;
	                				rebuildGraphics();
	                			}
	                		}
	                	} else if(gameWinning) {
	                		if(gameState == MULTI) {
	                			//game.finishMove(ogame);
	                			showToast("Submitting...");
                				new SubmitBoard().execute(new String());
	                			gameWinning = false;
	                			gameState = MENU;
	                			rebuildGraphics();
	                		}
	                	} else {
	                		
		                	if(_y > 10 && _y < boardHeight + 10 && _x > 5/* && _x < boardWidth + 5*/) {
		                		int location = -1;
			                	boolean _top;
		                		
			                	
		                		/*
		                		 * failed algorithm. never finished. just hard coding the variables
		                		 * if(_y > (boardHeight/2 + 10)) {
			                		float divider = (float) boardWidth / 13;
			                		location = -1 * (Math.round(_x / divider) - 14);
			                	}
			                	else {
			                		float divider = (float) boardWidth / 13;
			                		location = (Math.round(_x / divider) + 12);
			                	}*/
		                		
			                	
			                	_top = _y < (boardHeight/2 + 10);
			                	
		                		_x -= 5;
		                		//_y -= 10;
		                		
		                		_x = (int) Math.round(_x/size);
		                		_y = (int) Math.round(_y/size2);
		                		
		                		if(8 < _x &&  _x < 39) {
		                			if(_top) {
		                				location = 13;
		                			} else {
		                				location = 12;
		                			}
		                		}
		                		else if(39 < _x &&  _x < 71) {
		                			if(_top) {
		                				location = 14;
		                			} else {
		                				location = 11;
		                			}
		                		}
		                		else if(71 < _x &&  _x < 103) {
		                			if(_top) {
		                				location = 15;
		                			} else {
		                				location = 10;
		                			}
		                		}
		                		else if(103 < _x &&  _x < 135) {
		                			if(_top) {
		                				location = 16;
		                			} else {
		                				location = 9;
		                			}
		                		}
		                		else if(135 < _x &&  _x < 168) {
		                			if(_top) {
		                				location = 17;
		                			} else {
		                				location = 8;
		                			}
		                		}
		                		else if(168 < _x &&  _x < 199) {
		                			if(_top) {
		                				location = 18;
		                			} else {
		                				location = 7;
		                			}
		                		}
		                		else if(199 < _x &&  _x < 230) {
		                			if(game.yourColor == BLACK) {
		                				location = BLACKSPAWN;
		                			} else {
		                				location = WHITESPAWN;
		                			}
		                		}
		                		else if(230 < _x &&  _x < 262) {
		                			if(_top) {
		                				location = 19;
		                			} else {
		                				location = 6;
		                			}
		                		}
		                		else if(262 < _x &&  _x < 293) {
		                			if(_top) {
		                				location = 20;
		                			} else {
		                				location = 5;
		                			}
		                		}
		                		else if(293 < _x &&  _x < 326) {
		                			if(_top) {
		                				location = 21;
		                			} else {
		                				location = 4;
		                			}
		                		}
		                		else if(326 < _x &&  _x < 356) {
		                			if(_top) {
		                				location = 22;
		                			} else {
		                				location = 3;
		                			}
		                		}
		                		else if(356 < _x &&  _x < 387) {
		                			if(_top) {
		                				location = 23;
		                			} else {
		                				location = 2;
		                			}
		                		}
		                		else if(387 < _x &&  _x < 420) {
		                			if(_top) {
		                				location = 24;
		                			} else {
		                				location = 1;
		                			}
		                		} else if(420 < _x /*&& _x < 460*/) {
		                			if(5 < _y && _y < 80){
		                				game.dice.switchDice();
		                				rebuildGraphics();
		                			}
		                			if(132 < _y && _y < 168) {
		                				if(game.undoMove()) {
		                					rebuildGraphics();
		                				}
		                				
		                			}
		                		}
		                		
		                		if(location > -1) {
		                			game.hasChanged = game.movePiece(location);
		                			// die one might have failed, try the second die
		                			if(!game.hasChanged) {
		                				game.dice.switchDice();
		                				game.hasChanged = game.movePiece(location);
		                				// nothing happened. put it back
		                				if(!game.hasChanged) {
		                					game.dice.switchDice();
		                				}
		                			}
		                		}
			                	
		                		
		                		
			                	if(game.hasChanged) {
			                		game.dice.useDie();
			                		if(!game.dice.canMove() || !game.canMove()) {
				                		//game.finishMove();
				                		//game.dice.rollDice();
			                			showSubmit = true;
				                	}
			                		rebuildGraphics();
			                		/*try {
			                			MediaPlayer mp = MediaPlayer.create(Backgammon.this, R.raw.click);
			                			mp.start();
			                		} catch (Exception e) {
			                			e.printStackTrace();
			                		}*/
			                		if(willVibrate)
			                			soundManager.playSound(SoundPoolSoundManager.SOUND_CLICK);
			                		game.hasChanged = false;
			                	} else {
			                		if(willVibrate)
			                			v.vibrate(100);
			                	}
			                	if(!game.canMove()) {
			                		//game.finishMove();
			                		//game.dice.rollDice();
			                		showSubmit = true;
			                		rebuildGraphics();
			                	}
		                	}
	                	}
	                }
                	
                }
                return true;
            }
        }
        
        /**
         * Load all images into a hashmap
         * i was getting the images every rebuild
         * that was stupid
         * this is a much better way
         * there used to be a slightly barely noticeable lag
         * now there is none
         * takes a little longer at the beginning to load
         * and uses a little more memory
         * but much less prone to memory leakage
         */
        public void loadImages() {
        	
        	bitmaps.put(titlescreen, BitmapFactory.decodeResource(getResources(), R.drawable.titlescreen));
        	bitmaps.put(titlescreenyourmove, BitmapFactory.decodeResource(getResources(), R.drawable.titlescreenyourmove));
        	bitmaps.put(board, BitmapFactory.decodeResource(getResources(), R.drawable.board));
        	bitmaps.put(whitepiece, BitmapFactory.decodeResource(getResources(), R.drawable.whitepiece));
        	bitmaps.put(blackpiece, BitmapFactory.decodeResource(getResources(), R.drawable.blackpiece));
        	bitmaps.put(undobutton, BitmapFactory.decodeResource(getResources(), R.drawable.undobutton));
        	bitmaps.put(piecehighlight, BitmapFactory.decodeResource(getResources(), R.drawable.piecehighlight));
        	bitmaps.put(whitewins, BitmapFactory.decodeResource(getResources(), R.drawable.whitewins));
        	bitmaps.put(blackwins, BitmapFactory.decodeResource(getResources(), R.drawable.blackwins));
        	bitmaps.put(youwin, BitmapFactory.decodeResource(getResources(), R.drawable.youwin));
        	bitmaps.put(submitbutton, BitmapFactory.decodeResource(getResources(), R.drawable.submitbutton));
        	bitmaps.put(adbar, BitmapFactory.decodeResource(getResources(), R.drawable.adbar));
        	bitmaps.put(loading, BitmapFactory.decodeResource(getResources(), R.drawable.loading));
        	
        	bitmaps.put(dice1, BitmapFactory.decodeResource(getResources(), R.drawable.dice1));
        	bitmaps.put(dice2, BitmapFactory.decodeResource(getResources(), R.drawable.dice2));
        	bitmaps.put(dice3, BitmapFactory.decodeResource(getResources(), R.drawable.dice3));
        	bitmaps.put(dice4, BitmapFactory.decodeResource(getResources(), R.drawable.dice4));
        	bitmaps.put(dice5, BitmapFactory.decodeResource(getResources(), R.drawable.dice5));
        	bitmaps.put(dice6, BitmapFactory.decodeResource(getResources(), R.drawable.dice6));
        	bitmaps.put(dicex, BitmapFactory.decodeResource(getResources(), R.drawable.dicex));
        	bitmaps.put(dicearrow, BitmapFactory.decodeResource(getResources(), R.drawable.dicearrow));
        	
        	
        	bitmaps.put(piece0, BitmapFactory.decodeResource(getResources(), R.drawable.piece0));
        	bitmaps.put(piece1, BitmapFactory.decodeResource(getResources(), R.drawable.piece1));
        	bitmaps.put(piece2, BitmapFactory.decodeResource(getResources(), R.drawable.piece2));
        	bitmaps.put(piece3, BitmapFactory.decodeResource(getResources(), R.drawable.piece3));
        	bitmaps.put(piece4, BitmapFactory.decodeResource(getResources(), R.drawable.piece4));
        	bitmaps.put(piece5, BitmapFactory.decodeResource(getResources(), R.drawable.piece5));
        	bitmaps.put(piece6, BitmapFactory.decodeResource(getResources(), R.drawable.piece6));
        	bitmaps.put(piece7, BitmapFactory.decodeResource(getResources(), R.drawable.piece7));
        	bitmaps.put(piece8, BitmapFactory.decodeResource(getResources(), R.drawable.piece8));
        	bitmaps.put(piece9, BitmapFactory.decodeResource(getResources(), R.drawable.piece9));
        	bitmaps.put(piece10, BitmapFactory.decodeResource(getResources(), R.drawable.piece10));
        	bitmaps.put(piece11, BitmapFactory.decodeResource(getResources(), R.drawable.piece11));
        	bitmaps.put(piece12, BitmapFactory.decodeResource(getResources(), R.drawable.piece12));
        	bitmaps.put(piece13, BitmapFactory.decodeResource(getResources(), R.drawable.piece13));
        	bitmaps.put(piece14, BitmapFactory.decodeResource(getResources(), R.drawable.piece14));
        	bitmaps.put(piece15, BitmapFactory.decodeResource(getResources(), R.drawable.piece15));
        	
        	bitmaps.put(piecewhite0, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite0));
        	bitmaps.put(piecewhite1, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite1));
        	bitmaps.put(piecewhite2, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite2));
        	bitmaps.put(piecewhite3, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite3));
        	bitmaps.put(piecewhite4, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite4));
        	bitmaps.put(piecewhite5, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite5));
        	bitmaps.put(piecewhite6, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite6));
        	bitmaps.put(piecewhite7, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite7));
        	bitmaps.put(piecewhite8, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite8));
        	bitmaps.put(piecewhite9, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite9));
        	bitmaps.put(piecewhite10, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite10));
        	bitmaps.put(piecewhite11, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite11));
        	bitmaps.put(piecewhite12, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite12));
        	bitmaps.put(piecewhite13, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite13));
        	bitmaps.put(piecewhite14, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite14));
        	bitmaps.put(piecewhite15, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite15));
        	
        	
        	bitmaps.put(piecewhite1small, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite1small));
        	bitmaps.put(piecewhite2small, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite2small));
        	bitmaps.put(piecewhite3small, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite3small));
        	bitmaps.put(piecewhite4small, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite4small));
        	bitmaps.put(piecewhite5small, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite5small));
        	bitmaps.put(piecewhite6small, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite6small));
        	bitmaps.put(piecewhite7small, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite7small));
        	bitmaps.put(piecewhite8small, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite8small));
        	bitmaps.put(piecewhite9small, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite9small));
        	bitmaps.put(piecewhite10small, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite10small));
        	bitmaps.put(piecewhite11small, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite11small));
        	bitmaps.put(piecewhite12small, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite12small));
        	bitmaps.put(piecewhite13small, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite13small));
        	bitmaps.put(piecewhite14small, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite14small));
        	bitmaps.put(piecewhite15small, BitmapFactory.decodeResource(getResources(), R.drawable.piecewhite15small));
        	
        	bitmaps.put(piece1small, BitmapFactory.decodeResource(getResources(), R.drawable.piece1small));
        	bitmaps.put(piece2small, BitmapFactory.decodeResource(getResources(), R.drawable.piece2small));
        	bitmaps.put(piece3small, BitmapFactory.decodeResource(getResources(), R.drawable.piece3small));
        	bitmaps.put(piece4small, BitmapFactory.decodeResource(getResources(), R.drawable.piece4small));
        	bitmaps.put(piece5small, BitmapFactory.decodeResource(getResources(), R.drawable.piece5small));
        	bitmaps.put(piece6small, BitmapFactory.decodeResource(getResources(), R.drawable.piece6small));
        	bitmaps.put(piece7small, BitmapFactory.decodeResource(getResources(), R.drawable.piece7small));
        	bitmaps.put(piece8small, BitmapFactory.decodeResource(getResources(), R.drawable.piece8small));
        	bitmaps.put(piece9small, BitmapFactory.decodeResource(getResources(), R.drawable.piece9small));
        	bitmaps.put(piece10small, BitmapFactory.decodeResource(getResources(), R.drawable.piece10small));
        	bitmaps.put(piece11small, BitmapFactory.decodeResource(getResources(), R.drawable.piece11small));
        	bitmaps.put(piece12small, BitmapFactory.decodeResource(getResources(), R.drawable.piece12small));
        	bitmaps.put(piece13small, BitmapFactory.decodeResource(getResources(), R.drawable.piece13small));
        	bitmaps.put(piece14small, BitmapFactory.decodeResource(getResources(), R.drawable.piece14small));
        	bitmaps.put(piece15small, BitmapFactory.decodeResource(getResources(), R.drawable.piece15small));
        	
        }
        
        public Bitmap getImage(int id) {
        	return bitmaps.get(id);
        }
        
        /**
         * Rebuild the graphics array since the graphics have been changed.
         * Should be called as little as possible, since it takes some work to do
         */
        public void rebuildGraphics() {
        	synchronized (_thread.getSurfaceHolder()) {
        	_graphics = new ArrayList<GraphicObject>();
        	
        	//if(this.ga)
        	
        	if(gameState == LOADING) {
        		 
        		GraphicObject load = new GraphicObject(getImage(loading));	
        		load.getCoordinates().setRealX(screenWidth - load.getGraphic().getWidth()*2);
        		load.getCoordinates().setRealY(screenHeight - load.getGraphic().getHeight()*2);
        		
        		 //GraphicObject load = new GraphicObject(getImage(loading));	
				 //load.getCoordinates().setX(0);
				 //load.getCoordinates().setY(50);
				 _graphics.add(load);
				 
        	} else if(gameState == MULTI || gameState == LOCAL) {
        	
	        	int _xoffset = 0, _yoffset = 0, multiplier = 1, subtractor = 0;
	            
	        	GraphicObject board = new GraphicObject(getImage(Panel.board));
	            board.getCoordinates().setX(5);
	            board.getCoordinates().setY(10);
	            _graphics.add(board);
	            
	            //game.newGame();
	            GraphicObject graphic;
	            if(size >= 1) {
		            /*if(size > 1) {
		            	graphic = new GraphicObject(getImage(adbar));
		            	graphic.getCoordinates().setX(765);
		            	graphic.getCoordinates().setY(50);
		            	_graphics.add(graphic);
		            }*/
	            	for(int i=1; i < game.locations.length-3; i++) {
		            	if(i < 7) {
		            		_xoffset = (size > 1) ? 395 : 399;//board.getGraphic().getWidth() - 16 - 15;
		            		_yoffset = (size > 1) ? 255 : 260; //(int) Math.round(20*size);//board.getGraphic().getHeight()-20; 
		            		multiplier = 1;
		            		subtractor = 0;
		            	} else if(i < 13) {
		            		_xoffset = 367;//board.getGraphic().getWidth()- 16 - 47;
		            		_yoffset = (size > 1) ? 255 : 260;// (int) Math.round(20*size);//board.getGraphic().getHeight()-20;
		            		multiplier = 1;
		            		subtractor = 0;
		            	} else if(i < 19) {
		            		_xoffset = 15;
		            		_yoffset = (size > 1) ? 14 : 18;
		            		multiplier = -1;
		            		subtractor = 12;
		            	} else {
		            		_xoffset = (size > 1) ? 44 : 47;
		            		_yoffset = (size > 1) ? 14 : 18;
		            		multiplier = -1;
		            		subtractor = 12;
		            	}
		            	for(int j=0; j < game.locations[i].size(); j++) {
		            		if(j > 4) {
		            			if(game.locations[i].peek().color == WHITE) {
		            				switch(j) {
		            				case 5:
		            					graphic = new GraphicObject(getImage(piecewhite6small));
		            					break;
		            				case 6:
		            					graphic = new GraphicObject(getImage(piecewhite7small));
		            					break;
		            				case 7:
		            					graphic = new GraphicObject(getImage(piecewhite8small));
		            					break;
		            				case 8:
		            					graphic = new GraphicObject(getImage(piecewhite9small));
		            					break;
		            				case 9:
		            					graphic = new GraphicObject(getImage(piecewhite10small));
		            					break;
		            				case 10:
		            					graphic = new GraphicObject(getImage(piecewhite11small));
		            					break;
		            				case 11:
		            					graphic = new GraphicObject(getImage(piecewhite12small));
		            					break;
		            				case 12:
		            					graphic = new GraphicObject(getImage(piecewhite13small));
		            					break;
		            				case 13:
		            					graphic = new GraphicObject(getImage(piecewhite14small));
		            					break;
		            				case 14:
		            					graphic = new GraphicObject(getImage(piecewhite15small));
		            					break;
		            				default:
		            					graphic = new GraphicObject(getImage(piecewhite15small));
		            					break;
		            				}
		            			} else {
		            				switch(j) {
		            				case 5:
		            					graphic = new GraphicObject(getImage(piece6small));
		            					break;
		            				case 6:
		            					graphic = new GraphicObject(getImage(piece7small));
		            					break;
		            				case 7:
		            					graphic = new GraphicObject(getImage(piece8small));
		            					break;
		            				case 8:
		            					graphic = new GraphicObject(getImage(piece9small));
		            					break;
		            				case 9:
		            					graphic = new GraphicObject(getImage(piece10small));
		            					break;
		            				case 10:
		            					graphic = new GraphicObject(getImage(piece11small));
		            					break;
		            				case 11:
		            					graphic = new GraphicObject(getImage(piece12small));
		            					break;
		            				case 12:
		            					graphic = new GraphicObject(getImage(piece13small));
		            					break;
		            				case 13:
		            					graphic = new GraphicObject(getImage(piece14small));
		            					break;
		            				case 14:
		            					graphic = new GraphicObject(getImage(piece15small));
		            					break;
		            				default:
		            					graphic = new GraphicObject(getImage(piece10small));
		            					break;
		            					
		            				}
		            			}
		            			graphic.getCoordinates().setX((size > 1) ? ((_xoffset-(multiplier)*32*(i-1-subtractor))*3/2) : ((_xoffset-(multiplier)*32*(i-1-subtractor)))); //(int) Math.round((_xoffset-(multiplier)*32*(i-1-subtractor))*size)
			            		graphic.getCoordinates().setY((size > 1) ? (_yoffset*3/2) : (_yoffset)); //(int) Math.round((_yoffset)*size)
		            		} else {
			            		if(game.locations[i].peek().color == WHITE) {
			            			graphic = new GraphicObject(getImage(whitepiece));
			            		} else {
			            			graphic = new GraphicObject(getImage(blackpiece));
			            		}
			            		graphic.getCoordinates().setX(((size > 1) ? ((_xoffset-(multiplier)*32*(i-1-subtractor))*3/2) : (_xoffset-(multiplier)*32*(i-1-subtractor))));//(int) Math.round((_xoffset-(multiplier)*32*(i-1-subtractor))*size));
			            		graphic.getCoordinates().setY(((size > 1) ? ((_yoffset-(multiplier)*(j)*25)*3/2) :(_yoffset-(multiplier)*(j)*25)) ); //
		            		}
		            		 _graphics.add(graphic);
		            	}
		            	
		            }
		            if(game.locations[BLACKSPAWN].size() > 0) {
		            	switch(game.locations[BLACKSPAWN].size()) {
		            	case 1:
		            		graphic = new GraphicObject(getImage(piece1small));
		            		break;
		            	case 2:
		            		graphic = new GraphicObject(getImage(piece2small));
		            		break;
		            	case 3:
		            		graphic = new GraphicObject(getImage(piece3small));
		            		break;
		            	case 4:
		            		graphic = new GraphicObject(getImage(piece4small));
		            		break;
		            	case 5:
		            		graphic = new GraphicObject(getImage(piece5small));
		            		break;
		            	case 6:
		            		graphic = new GraphicObject(getImage(piece6small));
		            		break;
		            	default:
		            		graphic = new GraphicObject(getImage(blackpiece));
		            		break;
		            	}
		            	//graphic = new GraphicObject(getImage(blackpiece));
		            	graphic.getCoordinates().setX((board.getGraphic().getWidth() / 2) - ((size > 1) ? 9 : 5));//(int) Math.round(5*size));
		            	graphic.getCoordinates().setY((int) Math.round(.25 * board.getGraphic().getHeight()) + 10);
		            	 _graphics.add(graphic);
		            }
		            if(game.locations[WHITESPAWN].size() > 0) {
		            	switch(game.locations[WHITESPAWN].size()) {
		            	case 1:
		            		graphic = new GraphicObject(getImage(piecewhite1small));
		            		break;
		            	case 2:
		            		graphic = new GraphicObject(getImage(piecewhite2small));
		            		break;
		            	case 3:
		            		graphic = new GraphicObject(getImage(piecewhite3small));
		            		break;
		            	case 4:
		            		graphic = new GraphicObject(getImage(piecewhite4small));
		            		break;
		            	case 5:
		            		graphic = new GraphicObject(getImage(piecewhite5small));
		            		break;
		            	case 6:
		            		graphic = new GraphicObject(getImage(piecewhite6small));
		            		break;
		            	default:
		            		graphic = new GraphicObject(getImage(whitepiece));
		            		break;
		            	}
		            	//graphic = new GraphicObject(getImage(whitepiece));
		            	graphic.getCoordinates().setX((board.getGraphic().getWidth() / 2) - ((size > 1) ? 9 : 5)); //(int) Math.round(5*size));
		            	graphic.getCoordinates().setY((int) Math.round(.75 * board.getGraphic().getHeight()) + 10);
		            	 _graphics.add(graphic);
		            }
		            
		            
		            GraphicObject dice = getDice(game.dice.dice[0]);
		            dice.getCoordinates().setX(board.getGraphic().getWidth() + 10);
		    		dice.getCoordinates().setY(10);
		    		 _graphics.add(dice);
		    		dice = getDice(game.dice.dice[1]);
		    		dice.getCoordinates().setX(board.getGraphic().getWidth() + 10);
		     		dice.getCoordinates().setY(10+((size > 1) ? 52 : 35)+5);
		     		 _graphics.add(dice);
		     		
		     		GraphicObject dicearr = new GraphicObject(getImage(dicearrow));
		     		if(game.dice.numDice > 1) {
		     			dicearr.getCoordinates().setX(board.getGraphic().getWidth() + 10 + dice.getGraphic().getWidth());
		     			dicearr.getCoordinates().setY(10);
		     		} else {
		     			dicearr.getCoordinates().setX(board.getGraphic().getWidth() + 10 + dice.getGraphic().getWidth());
		     			dicearr.getCoordinates().setY(10+((size > 1) ? 52 : 35)+5);
		     		}
		     		_graphics.add(dicearr);
		     		 
		     		GraphicObject undo = new GraphicObject(getImage(undobutton));
		     		undo.getCoordinates().setX(board.getGraphic().getWidth() + 10);
		     		undo.getCoordinates().setY(board.getGraphic().getHeight()/2);
		     		_graphics.add(undo);
		     		 
		     		 
		     		/**
		     		 * Next 2 switch statements get the score image for the current score
		     		 * There's probably a better way to do this, but this works
		     		 * Just not optimal
		     		 * variable blackscore is used for both white and black score
		     		 */
		     		GraphicObject blackscore;
		     		 
		     		if(game.yourColor == BLACK) {
		     			blackscore = new GraphicObject(getImage(piecehighlight)); 
		     			blackscore.getCoordinates().setX(board.getGraphic().getWidth() + 10 - ((size > 1) ? 5 : 3)); //(int) Math.round(3*size));
		         		 blackscore.getCoordinates().setY(board.getGraphic().getHeight() - ((size > 1) ?  52 : 35));//(int) Math.round((32+3)*size));
		         		 _graphics.add(blackscore);
		     		} else {
		     			if(game.yourColor == WHITE) {
		         			blackscore = new GraphicObject(getImage(piecehighlight)); 
		         			blackscore.getCoordinates().setX(board.getGraphic().getWidth() + 10 - ((size > 1) ? 5 : 3));//(int) Math.round(3*size));
		             		blackscore.getCoordinates().setY(board.getGraphic().getHeight()- ((size > 1) ? 112 : 75)); // (int) Math.round((32+40+3)*size));
		             		_graphics.add(blackscore);
		         		}
		     		}
		     		
		     		 switch(game.locations[BLACKWIN].size()) {
		     		 case 0:
    	    			blackscore = new GraphicObject(getImage(piece0));	
    	    			break;
		     		 case 1:
		     			blackscore = new GraphicObject(getImage(piece1));
		     			break;
		     		 case 2:
		     			blackscore = new GraphicObject(getImage(piece2));
		     			break;
		     		 case 3:
		     			blackscore = new GraphicObject(getImage(piece3));
		     			break;
		     		 case 4:
		     			blackscore = new GraphicObject(getImage(piece4));
		     			break;
		     		 case 5:
		     			blackscore = new GraphicObject(getImage(piece5));
		     			break;
		     		 case 6:
		     			blackscore = new GraphicObject(getImage(piece6));
		     			break;
		     		 case 7:
		     			blackscore = new GraphicObject(getImage(piece7));
		     			break;
		     		 case 8:
		     			blackscore = new GraphicObject(getImage(piece8));
		     			break;
		     		 case 9:
		     			blackscore = new GraphicObject(getImage(piece9));
		     			break;
		     		 case 10:
		     			blackscore = new GraphicObject(getImage(piece10));
		     			break;
		     		 case 11:
		     			blackscore = new GraphicObject(getImage(piece11));
		     			break;
		     		 case 12:
		     			blackscore = new GraphicObject(getImage(piece12));
		     			break;
		     		 case 13:
		     			blackscore = new GraphicObject(getImage(piece13));
		     			break;
		     		 case 14:
		     			blackscore = new GraphicObject(getImage(piece14));
		     			break;
		     		 case 15:
		     			blackscore = new GraphicObject(getImage(piece15));
		     			break;
		     		 default:
		     			blackscore = new GraphicObject(getImage(piece0));	
		     		 }
		     		 blackscore.getCoordinates().setX(board.getGraphic().getWidth() + 10);
		     		 blackscore.getCoordinates().setY(board.getGraphic().getHeight()- ((size > 1) ? 48 : 32));//(int) Math.round(32*size));
		     		 _graphics.add(blackscore);
		     		 
		     		 
		     		switch(game.locations[WHITEWIN].size()) {
		     		 case 0:
    	    			blackscore = new GraphicObject(getImage(piecewhite0));	
    	    			break;
		     		 case 1:
		    			blackscore = new GraphicObject(getImage(piecewhite1));
		    			break;
		    		 case 2:
		    			blackscore = new GraphicObject(getImage(piecewhite2));
		    			break;
		    		 case 3:
		    			blackscore = new GraphicObject(getImage(piecewhite3));
		    			break;
		    		 case 4:
		    			blackscore = new GraphicObject(getImage(piecewhite4));
		    			break;
		    		 case 5:
		    			blackscore = new GraphicObject(getImage(piecewhite5));
		    			break;
		    		 case 6:
		    			blackscore = new GraphicObject(getImage(piecewhite6));
		    			break;
		    		 case 7:
		    			blackscore = new GraphicObject(getImage(piecewhite7));
		    			break;
		    		 case 8:
		    			blackscore = new GraphicObject(getImage(piecewhite8));
		    			break;
		    		 case 9:
		    			blackscore = new GraphicObject(getImage(piecewhite9));
		    			break;
		    		 case 10:
		    			blackscore = new GraphicObject(getImage(piecewhite10));
		    			break;
		    		 case 11:
		    			blackscore = new GraphicObject(getImage(piecewhite11));
		    			break;
		    		 case 12:
		    			blackscore = new GraphicObject(getImage(piecewhite12));
		    			break;
		    		 case 13:
		    			blackscore = new GraphicObject(getImage(piecewhite13));
		    			break;
		    		 case 14:
		    			blackscore = new GraphicObject(getImage(piecewhite14));
		    			break;
		    		 case 15:
		    			blackscore = new GraphicObject(getImage(piecewhite15));
		    			break;
		    		 default:
		    			blackscore = new GraphicObject(getImage(piecewhite0));	
		    		 }
		     		
		     		 blackscore.getCoordinates().setX(board.getGraphic().getWidth() + 10);
		    		 blackscore.getCoordinates().setY(board.getGraphic().getHeight()- ((size > 1) ? 108 : 72));//(int) Math.round((32+40)*size));
		    		 _graphics.add(blackscore);
		     		 
		    		 gameWinning = false;
		    		 

    	    		 if(gameState == MULTI && (game.locations[WHITEWIN].size() == 15 || game.locations[BLACKWIN].size() == 15)) {
    	    			 GraphicObject winner = new GraphicObject(getImage(youwin));	
    	    			 winner.getCoordinates().setX((size > 1) ? 140 : 40);
						 winner.getCoordinates().setY((size > 1) ? 130 : 50);
    					 _graphics.add(winner);
    					 if(willVibrate)
    						 soundManager.playSound(SoundPoolSoundManager.SOUND_WIN);
    					 gameWinning = true;
    					 showSubmit = false;
    	    		 } else {
		    		 
						 if(game.locations[WHITEWIN].size() == 15) {
							 GraphicObject winner = new GraphicObject(getImage(whitewins));	
							 winner.getCoordinates().setX((size > 1) ? 140 : 40);
							 winner.getCoordinates().setY((size > 1) ? 130 : 50);
							 _graphics.add(winner);
							 if(willVibrate)
								 soundManager.playSound(SoundPoolSoundManager.SOUND_WIN);
							 gameWinning = true;
							 showSubmit = false;
						 }
						 else if(game.locations[BLACKWIN].size() == 15) {
							 GraphicObject winner = new GraphicObject(getImage(blackwins));	
							 winner.getCoordinates().setX((size > 1) ? 140 : 40);
							 winner.getCoordinates().setY((size > 1) ? 130 : 50);
							 _graphics.add(winner);
							 if(willVibrate)
								 soundManager.playSound(SoundPoolSoundManager.SOUND_WIN);
							 gameWinning = true;
							 showSubmit = false;
						 }
    	    		 }
		    		 
					 if(showSubmit) {
						 GraphicObject submit = new GraphicObject(getImage(submitbutton));
						 submit.getCoordinates().setX((size > 1) ? 190 : 90);
						 submit.getCoordinates().setY((size > 1) ? 170 : 100);
						 _graphics.add(submit);
					 }
        		} else {
        			for(int i=1; i < game.locations.length-3; i++) {
    	            	if(i < 7) {
    	            		_xoffset =  250;//board.getGraphic().getWidth() - 16 - 15;
    	            		_yoffset = 197; //(int) Math.round(20*size);//board.getGraphic().getHeight()-20; 
    	            		multiplier = 1;
    	            		subtractor = 0;
    	            	} else if(i < 13) { 
    	            		_xoffset = 231;//board.getGraphic().getWidth()- 16 - 47;
    	            		_yoffset = 197;// (int) Math.round(20*size);//board.getGraphic().getHeight()-20;
    	            		multiplier = 1;
    	            		subtractor = 0;
    	            	} else if(i < 19) {
    	            		_xoffset = 10;
    	            		_yoffset = 15;
    	            		multiplier = -1;
    	            		subtractor = 12;
    	            	} else {
    	            		_xoffset = 30;
    	            		_yoffset = 15;
    	            		multiplier = -1;
    	            		subtractor = 12;
    	            	}
    	            	for(int j=0; j < game.locations[i].size(); j++) {
    	            		if(j > 4) {
    	            			if(game.locations[i].peek().color == WHITE) {
    	            				switch(j) {
    	            				case 5:
    	            					graphic = new GraphicObject(getImage(piecewhite6small));
    	            					break;
    	            				case 6:
    	            					graphic = new GraphicObject(getImage(piecewhite7small));
    	            					break;
    	            				case 7:
    	            					graphic = new GraphicObject(getImage(piecewhite8small));
    	            					break;
    	            				case 8:
    	            					graphic = new GraphicObject(getImage(piecewhite9small));
    	            					break; 
    	            				case 9:
    	            					graphic = new GraphicObject(getImage(piecewhite10small));
    	            					break;
    	            				case 10:
    	            					graphic = new GraphicObject(getImage(piecewhite11small));
    	            					break;
    	            				case 11:
    	            					graphic = new GraphicObject(getImage(piecewhite12small));
    	            					break;
    	            				case 12:
    	            					graphic = new GraphicObject(getImage(piecewhite13small));
    	            					break;
    	            				case 13:
    	            					graphic = new GraphicObject(getImage(piecewhite14small));
    	            					break;
    	            				case 14:
    	            					graphic = new GraphicObject(getImage(piecewhite15small));
    	            					break;
    	            				default:
    	            					graphic = new GraphicObject(getImage(piecewhite15small));
    	            					break;
    	            				}
    	            			} else {
    	            				switch(j) {
    	            				case 5:
    	            					graphic = new GraphicObject(getImage(piece6small));
    	            					break;
    	            				case 6:
    	            					graphic = new GraphicObject(getImage(piece7small));
    	            					break;
    	            				case 7:
    	            					graphic = new GraphicObject(getImage(piece8small));
    	            					break;
    	            				case 8:
    	            					graphic = new GraphicObject(getImage(piece9small));
    	            					break;
    	            				case 9:
    	            					graphic = new GraphicObject(getImage(piece10small));
    	            					break;
    	            				case 10:
    	            					graphic = new GraphicObject(getImage(piece11small));
    	            					break;
    	            				case 11:
    	            					graphic = new GraphicObject(getImage(piece12small));
    	            					break;
    	            				case 12:
    	            					graphic = new GraphicObject(getImage(piece13small));
    	            					break;
    	            				case 13:
    	            					graphic = new GraphicObject(getImage(piece14small));
    	            					break;
    	            				case 14:
    	            					graphic = new GraphicObject(getImage(piece15small));
    	            					break;
    	            				default:
    	            					graphic = new GraphicObject(getImage(piece10small));
    	            					break;
    	            					
    	            				}
    	            			}
    	            			graphic.getCoordinates().setX(((_xoffset-(multiplier)*20*(i-1-subtractor)))); //(int) Math.round((_xoffset-(multiplier)*32*(i-1-subtractor))*size)
    		            		graphic.getCoordinates().setY((_yoffset)); //(int) Math.round((_yoffset)*size)
    	            		} else {
    		            		if(game.locations[i].peek().color == WHITE) {
    		            			graphic = new GraphicObject(getImage(whitepiece));
    		            		} else {
    		            			graphic = new GraphicObject(getImage(blackpiece));
    		            		}
    		            		graphic.getCoordinates().setX(((_xoffset-(multiplier)*20*(i-1-subtractor))));//(int) Math.round((_xoffset-(multiplier)*32*(i-1-subtractor))*size));
    		            		graphic.getCoordinates().setY(((_yoffset-(multiplier)*(j)*18)) ); //
    	            		}
    	            		 _graphics.add(graphic);
    	            	}
    	            	
    	            }
    	            if(game.locations[BLACKSPAWN].size() > 0) {
    	            	switch(game.locations[BLACKSPAWN].size()) {
		            	case 1:
		            		graphic = new GraphicObject(getImage(piece1small));
		            		break;
		            	case 2:
		            		graphic = new GraphicObject(getImage(piece2small));
		            		break;
		            	case 3:
		            		graphic = new GraphicObject(getImage(piece3small));
		            		break;
		            	case 4:
		            		graphic = new GraphicObject(getImage(piece4small));
		            		break;
		            	case 5:
		            		graphic = new GraphicObject(getImage(piece5small));
		            		break;
		            	case 6:
		            		graphic = new GraphicObject(getImage(piece6small));
		            		break;
		            	default:
		            		graphic = new GraphicObject(getImage(blackpiece));
		            		break;
		            	}
    	            	graphic.getCoordinates().setX((board.getGraphic().getWidth() / 2) - ((size > 1) ? 9 : 5));//(int) Math.round(5*size));
    	            	graphic.getCoordinates().setY((int) Math.round(.25 * board.getGraphic().getHeight()) + 10);
    	            	 _graphics.add(graphic);
    	            }
    	            if(game.locations[WHITESPAWN].size() > 0) {
    	            	switch(game.locations[WHITESPAWN].size()) {
		            	case 1:
		            		graphic = new GraphicObject(getImage(piecewhite1small));
		            		break;
		            	case 2:
		            		graphic = new GraphicObject(getImage(piecewhite2small));
		            		break;
		            	case 3:
		            		graphic = new GraphicObject(getImage(piecewhite3small));
		            		break;
		            	case 4:
		            		graphic = new GraphicObject(getImage(piecewhite4small));
		            		break;
		            	case 5:
		            		graphic = new GraphicObject(getImage(piecewhite5small));
		            		break;
		            	case 6:
		            		graphic = new GraphicObject(getImage(piecewhite6small));
		            		break;
		            	default:
		            		graphic = new GraphicObject(getImage(whitepiece));
		            		break;
		            	}
    	            	graphic.getCoordinates().setX((board.getGraphic().getWidth() / 2) - ((size > 1) ? 9 : 5)); //(int) Math.round(5*size));
    	            	graphic.getCoordinates().setY((int) Math.round(.75 * board.getGraphic().getHeight()) + 10);
    	            	 _graphics.add(graphic);
    	            }
    	            
    	            
    	            GraphicObject dice = getDice(game.dice.dice[0]);
    	            dice.getCoordinates().setX(board.getGraphic().getWidth() + 10);
    	    		dice.getCoordinates().setY(10);
    	    		 _graphics.add(dice);
    	    		dice = getDice(game.dice.dice[1]);
    	    		dice.getCoordinates().setX(board.getGraphic().getWidth() + 10);
    	     		dice.getCoordinates().setY(10+((size > 1) ? 52 : 35)+5);
    	     		 _graphics.add(dice);
    	     		 
    	     		 
    	     		GraphicObject dicearr = new GraphicObject(getImage(dicearrow));
		     		if(game.dice.numDice > 1) {
		     			dicearr.getCoordinates().setX(board.getGraphic().getWidth() + 10 + dice.getGraphic().getWidth());
		     			dicearr.getCoordinates().setY(10);
		     		} else {
		     			dicearr.getCoordinates().setX(board.getGraphic().getWidth() + 10 + dice.getGraphic().getWidth());
		     			dicearr.getCoordinates().setY(10+((size > 1) ? 52 : 35)+5);
		     		}
		     		_graphics.add(dicearr);
    	     		 
    	     		 GraphicObject undo = new GraphicObject(getImage(undobutton));
    	     		 undo.getCoordinates().setX(board.getGraphic().getWidth() + 10);
    	     		 undo.getCoordinates().setY(board.getGraphic().getHeight()/2);
    	     		 _graphics.add(undo);
    	     		 
    	     		 
    	     		 /**
    	     		  * Next 2 switch statements get the score image for the current score
    	     		  * There's probably a better way to do this, but this works
    	     		  * Just not optimal
    	     		  * variable blackscore is used for both white and black score
    	     		  */
    	     		 GraphicObject blackscore;
    	     		 
    	     		if(game.yourColor == BLACK) {
    	     			blackscore = new GraphicObject(getImage(piecehighlight)); 
    	     			blackscore.getCoordinates().setX(board.getGraphic().getWidth() + 10 - ((size > 1) ? 5 : 3)); //(int) Math.round(3*size));
    	         		 blackscore.getCoordinates().setY(board.getGraphic().getHeight() - (28));//(int) Math.round((32+3)*size));
    	         		 _graphics.add(blackscore);
    	     		} else {
    	     			if(game.yourColor == WHITE) {
    	         			blackscore = new GraphicObject(getImage(piecehighlight)); 
    	         			blackscore.getCoordinates().setX(board.getGraphic().getWidth() + 10 - ((size > 1) ? 5 : 3));//(int) Math.round(3*size));
    	             		blackscore.getCoordinates().setY(board.getGraphic().getHeight()- (63)); // (int) Math.round((32+40+3)*size));
    	             		_graphics.add(blackscore);
    	         		}
    	     		}
    	     		 
    	     		 switch(game.locations[BLACKWIN].size()) {
    	     		 case 0:
    	     			blackscore = new GraphicObject(getImage(piece0));
    	     			break;
    	     		 case 1:
    	     			blackscore = new GraphicObject(getImage(piece1));
    	     			break;
    	     		 case 2:
    	     			blackscore = new GraphicObject(getImage(piece2));
    	     			break;
    	     		 case 3:
    	     			blackscore = new GraphicObject(getImage(piece3));
    	     			break;
    	     		 case 4:
    	     			blackscore = new GraphicObject(getImage(piece4));
    	     			break;
    	     		 case 5:
    	     			blackscore = new GraphicObject(getImage(piece5));
    	     			break;
    	     		 case 6:
    	     			blackscore = new GraphicObject(getImage(piece6));
    	     			break;
    	     		 case 7:
    	     			blackscore = new GraphicObject(getImage(piece7));
    	     			break;
    	     		 case 8:
    	     			blackscore = new GraphicObject(getImage(piece8));
    	     			break;
    	     		 case 9:
    	     			blackscore = new GraphicObject(getImage(piece9));
    	     			break;
    	     		 case 10:
    	     			blackscore = new GraphicObject(getImage(piece10));
    	     			break;
    	     		 case 11:
    	     			blackscore = new GraphicObject(getImage(piece11));
    	     			break;
    	     		 case 12:
    	     			blackscore = new GraphicObject(getImage(piece12));
    	     			break;
    	     		 case 13:
    	     			blackscore = new GraphicObject(getImage(piece13));
    	     			break;
    	     		 case 14:
    	     			blackscore = new GraphicObject(getImage(piece14));
    	     			break;
    	     		 case 15:
    	     			blackscore = new GraphicObject(getImage(piece15));
    	     			break;
    	     		 default:
    	     			blackscore = new GraphicObject(getImage(piece0));	
    	     		 }
    	     		 blackscore.getCoordinates().setX(board.getGraphic().getWidth() + 10);
    	     		 blackscore.getCoordinates().setY(board.getGraphic().getHeight()- (25));//(int) Math.round(32*size));
    	     		 _graphics.add(blackscore);
    	     		 
    	     		 
    	     		switch(game.locations[WHITEWIN].size()) {
    	     		 case 0:
    	    			blackscore = new GraphicObject(getImage(piecewhite0));	
    	    			break;
    	     		 case 1:
    	    			blackscore = new GraphicObject(getImage(piecewhite1));
    	    			break;
    	    		 case 2:
    	    			blackscore = new GraphicObject(getImage(piecewhite2));
    	    			break;
    	    		 case 3:
    	    			blackscore = new GraphicObject(getImage(piecewhite3));
    	    			break;
    	    		 case 4:
    	    			blackscore = new GraphicObject(getImage(piecewhite4));
    	    			break;
    	    		 case 5:
    	    			blackscore = new GraphicObject(getImage(piecewhite5));
    	    			break;
    	    		 case 6:
    	    			blackscore = new GraphicObject(getImage(piecewhite6));
    	    			break;
    	    		 case 7:
    	    			blackscore = new GraphicObject(getImage(piecewhite7));
    	    			break;
    	    		 case 8:
    	    			blackscore = new GraphicObject(getImage(piecewhite8));
    	    			break;
    	    		 case 9:
    	    			blackscore = new GraphicObject(getImage(piecewhite9));
    	    			break;
    	    		 case 10:
    	    			blackscore = new GraphicObject(getImage(piecewhite10));
    	    			break;
    	    		 case 11:
    	    			blackscore = new GraphicObject(getImage(piecewhite11));
    	    			break;
    	    		 case 12:
    	    			blackscore = new GraphicObject(getImage(piecewhite12));
    	    			break;
    	    		 case 13:
    	    			blackscore = new GraphicObject(getImage(piecewhite13));
    	    			break;
    	    		 case 14:
    	    			blackscore = new GraphicObject(getImage(piecewhite14));
    	    			break;
    	    		 case 15:
    	    			blackscore = new GraphicObject(getImage(piecewhite15));
    	    			break;
    	    		 default:
    	    			blackscore = new GraphicObject(getImage(piecewhite0));	
    	    		 }
    	     		
    	     		 blackscore.getCoordinates().setX(board.getGraphic().getWidth() + 10);
    	    		 blackscore.getCoordinates().setY(board.getGraphic().getHeight()- (60));//(int) Math.round((32+40)*size));
    	    		 _graphics.add(blackscore);
    	     		 
    	    		 gameWinning = false;
    	    		 
    	    		 if(gameState == MULTI && (game.locations[WHITEWIN].size() == 15 || game.locations[BLACKWIN].size() == 15)) {
    	    			 GraphicObject winner = new GraphicObject(getImage(youwin));	
    					 winner.getCoordinates().setX(0);
    					 winner.getCoordinates().setY(50);
    					 _graphics.add(winner);
    					 if(willVibrate)
    						 soundManager.playSound(SoundPoolSoundManager.SOUND_WIN);
    					 gameWinning = true;
    					 showSubmit = false;
    	    		 } else {
    	    		 
	    				 if(game.locations[WHITEWIN].size() == 15) {
	    					 GraphicObject winner = new GraphicObject(getImage(whitewins));	
	    					 winner.getCoordinates().setX(0);
	    					 winner.getCoordinates().setY(50);
	    					 _graphics.add(winner);
	    					 if(willVibrate)
	    						 soundManager.playSound(SoundPoolSoundManager.SOUND_WIN);
	    					 gameWinning = true;
	    					 showSubmit = false;
	    				 }
	    				 else if(game.locations[BLACKWIN].size() == 15) {
	    					 GraphicObject winner = new GraphicObject(getImage(blackwins));	
	    					 winner.getCoordinates().setX(0);
	    					 winner.getCoordinates().setY(50);
	    					 _graphics.add(winner);
	    					 if(willVibrate)
	    						 soundManager.playSound(SoundPoolSoundManager.SOUND_WIN);
	    					 gameWinning = true;
	    					 showSubmit = false;
	    				 }
    	    		 }
    	    		 
    				 if(showSubmit) {
    					 GraphicObject submit = new GraphicObject(getImage(submitbutton));
    					 submit.getCoordinates().setX(65);
    					 submit.getCoordinates().setY(80);
    					 _graphics.add(submit);
    				 }
        		}
        	} else if(gameState == MENU) {
        		GraphicObject menu;
        		if(yourMove) {
        			menu = new GraphicObject(getImage(titlescreenyourmove));
        		} else {
        			menu = new GraphicObject(getImage(titlescreen));
        		}
        		//menu.getCoordinates().setX((size > 1) ? 190 : 90);
        		//menu.getCoordinates().setY((size > 1) ? 170 : 100);
        		menu.getCoordinates().setX(0);
        		menu.getCoordinates().setY(0);
        		_graphics.add(menu);
        	}
        	}
        }
 
        @Override
        public void onDraw(Canvas canvas) {
           // if(game.hasChanged) {
	        	canvas.drawColor(Color.WHITE);
	            Bitmap bitmap;
	            GraphicObject.Coordinates coords;
	            for (GraphicObject graphic : _graphics) {
	                bitmap = graphic.getGraphic();
	                coords = graphic.getCoordinates();
	                canvas.drawBitmap(bitmap, coords.getX(), coords.getY(), null);
	            }
           // }
           // game.hasChanged = false;
        }
 
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // meh 
        }
 
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            /*_thread.setRunning(true);
            try {
            	_thread.start();
            } catch (Exception e) {
            	try {
            		//_thread.join();
            		rebuildGraphics();
            	}
            	catch (Exception e1) {
            		e1.printStackTrace();
            	}
            }*/
        	
        	if (_thread.getState() == Thread.State.TERMINATED) {
        	    _thread = new GameThread(getHolder(), this);
        	    _thread.setRunning(true);
        	    _thread.start();
        	  }
        	  else {
        	    _thread.setRunning(true);
        	    _thread.start();
        	  } 
        }
 
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // simply copied from sample application LunarLander:
            // we have to tell thread to shut down & wait for it to finish, or else
            // it might touch the Surface after we return and explode
            boolean retry = true;
            _thread.setRunning(false);
            while (retry) {
                try {
                    _thread.join();
                    retry = false;
                } catch (InterruptedException e) {
                    // we will try it again and again...
                }
            }
        }
    }
    
    
    
	private class CheckForMoves extends AsyncTask<Object, Boolean, Boolean> {
    	
		@Override
		protected Boolean doInBackground(Object... params) {
			
			HttpClient client = new DefaultHttpClient();
			byte[] sBuffer = new byte[512];
			HttpGet request = new HttpGet(OnlineGame.REQURL+"checkmoves.php"+ogame.generateParameters());
			request.setHeader("User-Agent", OnlineGame.USERAGENT);
			
			try {
				HttpResponse response = client.execute(request);
				
				StatusLine status = response.getStatusLine();
				if(status.getStatusCode() != 200){
					yourMove = false;
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
	            try {
	            	int count = Integer.parseInt(s);
	            
		            if(count > 0) {
		            	yourMove = true;
		            } else {
		            	yourMove = false;
		            }
	            } catch(Exception e) {
	            	e.printStackTrace();
	            	yourMove = false;
	            }
	            
				
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			
			panel.rebuildGraphics();
			
			return null;
		}

    }
    
    private class SubmitBoard extends AsyncTask<String, Void, Void> {
    	@Override
    	protected Void doInBackground(final String... args) {
    		game.finishMove(ogame);
    		if(willVibrate)
    			soundManager.playSound(SoundPoolSoundManager.SOUND_SENT);
    		new CheckForMoves().execute(new Object());
    		return null;
    	}
    }
    
    private class LoadOnlineGame extends AsyncTask<Bundle, Void, Void> {
    	@Override
    	protected Void doInBackground(final Bundle... args) {
    		Bundle m = args[0];
    		if(ogame.loadGame(m.getInt("game"), game)) {
    			// show new game
    			Log.v("backgammon", "game loading worked");
    			showSubmit = false;
    			gameState = MULTI;
    			panel.rebuildGraphics();
    		} else {
    			// go back to menu
    			Log.v("backgammon", "game loading failed. Show menu");
    			gameState = MENU;
    			panel.rebuildGraphics();
    		}
    		
    		return null;
    	}
    }
    
    
    
    
}