package com.drakeapps.backgammon;

import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class OnlineView extends Activity{
	WebView webview;
	OnlineGame game = new OnlineGame();
	boolean trigger = false;
	Bundle map;
	
	private static final int GOTO_MENU = 1;
	
	int playGame;
	
	private void quitView(int mode) {
		Bundle info = new Bundle();
		
		info.putInt("game", playGame);
		info.putString("device", game.getDevice());
		info.putInt("user", game.getUser());
		
		//Intent intent = new Intent();
		getIntent().putExtras(info);
		
		SharedPreferences settings = getSharedPreferences(Backgammon.PREFS_NAME, 0);
    	SharedPreferences.Editor editor = settings.edit();
    	editor.putString("onlineGame", game.saveToFile());
    	editor.commit();
    	
    	
		Log.v( "online-view", "quitting view");
		
		if(mode == 1)
			setResult(RESULT_OK, getIntent());
		else 
			setResult(RESULT_CANCELED, getIntent());
		
		//Intent intent = new Intent();
		//setResult(RESULT_OK, intent);
		finish();
		
	}
	
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		webview = new WebView(this);
		setContentView(webview);
		
				
		String url = OnlineGame.REQURL + "login.php";
		
		Log.v( "online-view", "starting this mess");
		
		
		SharedPreferences settings = getSharedPreferences(Backgammon.PREFS_NAME, 0);
		String string = settings.getString("onlineGame", "");
		Log.v("OnlineView", string);
		if(!game.loadFromFile(string)) {
    		game.newLogin();
		} else {
			if(game.loggedIn()) {
				Log.v("OnlineView", "Logged in");
				url = OnlineGame.REQURL + "gamelist.php" + game.generateParameters();
			}
		}
		
		
		webview.loadUrl(url);
		webview.getSettings().setJavaScriptEnabled(true);
		
		//final Context myApp = this;  
		  
		/* An instance of this class will be registered as a JavaScript interface */  
		class MyJavaScriptInterface  
		{  
		    @SuppressWarnings("unused")  
		    public void showHTML(String html)  
		    {  
		        String s[] = html.split(",");
		        if(s.length > 1) {
		        	game.setLogin(s[0], s[1]);
		        }
		    }
		    
		    @SuppressWarnings("unused")  
		    public void setLogin(String id, String device) {
		    	game.setLogin(id, device);
		    }
		    
		    @SuppressWarnings("unused")  
		    public void chooseGame(String id) {
		    	//game.setCurrentGame(id);
		    	playGame = Integer.parseInt(id);
		    	Log.v( "online-view", "chose game");
		    	quitView(1); // don't think this works from some reason. probably limitation in the javascript interface
		    	Log.v( "online-view", "tried to quit. didn't quit");
		    }
		}  
		
		webview.addJavascriptInterface(new MyJavaScriptInterface(), "HTMLOUT");  
		
		webview.setWebViewClient(new WebViewClient() {
			/*@Override  
		    public boolean shouldOverrideUrlLoading(WebView view, String url)  
		    {  
		        
		          
		        if (url.equals(OnlineGame.REQURL + "loggedin.php")) {  
		            trigger = true;
		            //return true;  
		        }  
		        
		        //view.loadUrl(url);
		        //return true;
		        
		        return false;  
		    }*/
			
			
			
			@Override
			public void onPageFinished(WebView view, String url) {
				if(url.equals(OnlineGame.REQURL + "loggedin.php")) {  
					//webview.loadUrl("javascript:window.HTMLOUT.showHTML(document.getElementsByTagName('span')[0].innerHTML);");
					webview.loadUrl(OnlineGame.REQURL + "gamelist.php" + game.generateParameters());	
				}
				
				if(url.equals(OnlineGame.REQURL + "loadgame.php")) {
					Log.v( "online-view", "because the javascript is stupid. got to loadgame. quitting now");
					quitView(1);
				}
				
				
			}

			@Override
			public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
				// TODO: This isn't working on a EVO for some reason. Fix it
				
				if(event.getAction() == KeyEvent.KEYCODE_BACK) {
					Log.v( "online-view", "pressed back. quitting");
					quitView(3);
				}
				
				return super.shouldOverrideKeyEvent(view, event);
			}
			
			
		});  
	}


	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case GOTO_MENU:
			quitView(4);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	public boolean onCreateOptionsMenu(Menu menu) {
    	menu.add(0, GOTO_MENU, 0, "Home");
    	//menu.add(0, NEW_GAME, 1, "New Game");
    	//menu.add(0, TOGGLE_VIBRATE, 2, "Toggle Vibrate");
    	return true;
    }
	
}
