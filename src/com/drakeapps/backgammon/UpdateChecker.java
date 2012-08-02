package com.drakeapps.backgammon;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class UpdateChecker {
	
	/**
	 * currentVersion : version you are /currently/ using
	 * updateVersion  : version that's online
	 */
	private int currentVersion;
	private int updateVersion;
	private boolean checkedUpdate;
	
	private static final String updateURL = "http://api.drakeapps.com/currentversion.php";
	
	HttpClient client = new DefaultHttpClient();
	private static byte[] sBuffer = new byte[512];
	
	public UpdateChecker(int version) {
		currentVersion = version;
		
		checkedUpdate = false;
	}
	
	public boolean upToDate() {
		if(!checkedUpdate) {
			checkedUpdate = getUpdate();
		}
		
		if(checkedUpdate) {
			if(currentVersion < updateVersion) {
				return false;
			}
		}
		
		return true;
	}
	
	public boolean getUpdate() {
		HttpGet request = new HttpGet(updateURL);
		request.setHeader("User-Agent", OnlineGame.USERAGENT);
		
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
            
            updateVersion = Integer.parseInt(s);
            
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	
}
