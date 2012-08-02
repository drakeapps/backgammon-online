package com.drakeapps.backgammon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class CheckService extends Service {
	private Timer timer = new Timer();
	
	//private static final int NEW_GAME_ID = 1;
	
	String url, userAgent;
	String ns;
	NotificationManager mNotificationManager;
	int icon = R.drawable.notification;
	CharSequence tickerText = "Your move";
	CharSequence title = "Backgammon Online";
	CharSequence body = "It's your move in Backgammon Online";
	int updateTime;
	OnlineGame ogame;
	
	public void onCreate() {
		Log.v("CheckService", "starting service");
		ogame = new OnlineGame();
		super.onCreate();
		SharedPreferences settings = getSharedPreferences(Backgammon.PREFS_NAME, 0);
		String string = settings.getString("onlineGame", "");
		ogame.loadFromFile(string);
		updateTime = settings.getInt("updateTime", 1);
		Log.v("CheckService", "updateTime: "+updateTime);
		if(Backgammon.UPDATE_TIMES[updateTime] == 0) {
			stopservice();
			Log.v("CheckService", "updating turned off");
		} else {
			url = OnlineGame.REQURL+"checkgames.php"+ogame.generateParameters();
			//updateTime = update;
			userAgent = OnlineGame.USERAGENT;
			ns = Context.NOTIFICATION_SERVICE;
			mNotificationManager = (NotificationManager) getSystemService(ns);
			startservice();
		}
		
	}
	
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public void startservice() {
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				HttpClient client = new DefaultHttpClient();
				byte[] sBuffer = new byte[512];
				HttpGet request = new HttpGet(url);
				request.setHeader("User-Agent", userAgent);
				Log.v("CheckService", "checking for moves");
				try {
					HttpResponse response = client.execute(request);
					
					StatusLine status = response.getStatusLine();
					if(status.getStatusCode() != 200){
						
					} else {
					
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
			            	long when = System.currentTimeMillis();
			            	Notification notification = new Notification(icon, tickerText, when);
			            	
			            	Context context = getApplicationContext();
			            	Intent notificationIntent = new Intent(context, Backgammon.class);
			            	
			            	PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
			            	
			            	notification.setLatestEventInfo(context, title, body, contentIntent);
			            	notification.defaults |= Notification.DEFAULT_SOUND;
			            	notification.defaults |= Notification.DEFAULT_VIBRATE;
			            	notification.defaults |= Notification.DEFAULT_LIGHTS;
			            	notification.flags |= Notification.FLAG_AUTO_CANCEL;
			            	
			            	
			            	int id = (int) count;

			            	mNotificationManager.notify(id, notification);
			            	
			            } else if(count == -1) {
			            	stopservice();
			            }
		            } catch(Exception e) {
		            	e.printStackTrace();
		            	
		            }
					}
		            
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		}, 0, Backgammon.UPDATE_TIMES[updateTime]*60*1000);
		
		
	}
	
	@Override
	public void onDestroy() {
		stopservice();
	}
	
	public void stopservice() {
		if(timer != null) {
			timer.cancel();
		}
		Log.v("CheckService", "stopped service");
	}
}
