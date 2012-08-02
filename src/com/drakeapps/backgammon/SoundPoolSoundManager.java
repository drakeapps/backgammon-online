package com.drakeapps.backgammon;

// VIA: http://groups.google.com/group/android-developers/browse_thread/thread/3d83bee2892b6f78/ca6fc62d3bc57537?show_docid=ca6fc62d3bc57537&pli=1

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

public class SoundPoolSoundManager { 
    private static final String TAG = "SoundPoolSoundManager"; 
    public static final int SOUNDPOOL_STREAMS = 6;
    public static final int SOUND_CLICK = 1; 
    public static final int SOUND_WIN = 2;
    public static final int SOUND_SENT = 3;
    private boolean enabled = true; 
    private Context context; 
    private SoundPool soundPool; 
    private HashMap<Integer, Integer> soundPoolMap; 
    public SoundPoolSoundManager(Context context) { 
            this.context = context; 
    } 
    public void reInit() { 
            init(); 
    } 
    public void init() { 
            if (enabled) { 
                    Log.d(TAG, "Initializing new SoundPool"); 
                    //re-init sound pool to work around bugs 
                    release(); 
                    soundPool = new SoundPool(SOUNDPOOL_STREAMS, AudioManager.STREAM_MUSIC, 100); 
                    soundPoolMap = new HashMap<Integer, Integer>(); 
                    soundPoolMap.put(SOUND_CLICK, soundPool.load(context, R.raw.click, 1)); 
                    soundPoolMap.put(SOUND_WIN, soundPool.load(context, R.raw.win, 1));
                    soundPoolMap.put(SOUND_SENT, soundPool.load(context, R.raw.sent, 1));
                    Log.d(TAG, "SoundPool initialized"); 
            } 
    } 
    public void release() { 
            if (soundPool != null) { 
                    Log.d(TAG, "Closing SoundPool"); 
                    soundPool.release(); 
                    soundPool = null; 
                    Log.d(TAG, "SoundPool closed"); 
                    return; 
            } 
    } 
    public void playSound(int sound) { 
            if (soundPool != null) { 
                    Log.d(TAG, "Playing Sound " + sound); 
                    AudioManager mgr = (AudioManager) 
                    context.getSystemService(Context.AUDIO_SERVICE); 
                    int streamVolume = mgr.getStreamVolume(AudioManager.STREAM_MUSIC); 
                    Integer soundId = soundPoolMap.get(sound); 
                    if (soundId != null) { 
                    	soundPool.play(soundPoolMap.get(sound), streamVolume, streamVolume, 1, 0, 1f); 
                    } 
            } 
    } 
    public void setEnabled(boolean enabled) { 
            this.enabled = enabled; 
    } 
} 