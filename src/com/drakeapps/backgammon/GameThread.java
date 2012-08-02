package com.drakeapps.backgammon;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.drakeapps.backgammon.Backgammon.Panel;


class GameThread extends Thread {
    private SurfaceHolder _surfaceHolder;
    private Panel _panel;
    private boolean _run = false;
    public boolean _needBuild = false;

    public GameThread(SurfaceHolder surfaceHolder, Panel panel) {
        _surfaceHolder = surfaceHolder;
        _panel = panel;
    }

    public void setRunning(boolean run) {
        _run = run;
    }

    public SurfaceHolder getSurfaceHolder() {
        return _surfaceHolder;
    }

    @Override
    public void run() {
        Canvas c;
        while (_run) {
            c = null;
            try {
                c = _surfaceHolder.lockCanvas(null);
                synchronized (_surfaceHolder) {
                	//if(needNewGame && gameState == LOCAL) {
                    //	_panel.newGame();
                    //}
                    _panel.onDraw(c);
                    
                    //super.wait(16);
                    try {
                    	super.wait(16);
                    } catch (Exception e) {
                    	
                    }
                    
                }
            } finally {
                // do this in a finally so that if an exception is thrown
                // during the above, we don't leave the Surface in an
                // inconsistent state
                if (c != null) {
                    _surfaceHolder.unlockCanvasAndPost(c);
                }
            }
        }
    }
}
