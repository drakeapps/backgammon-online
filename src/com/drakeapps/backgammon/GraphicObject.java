package com.drakeapps.backgammon;

import android.graphics.Bitmap;

class GraphicObject {
    /**
     * Contains the coordinates of the graphic.
     */
    public class Coordinates {
        private int _x;
        private int _y;
        
        public Coordinates() {
        	_x = 100;
        	_y = 0;
        }
        
        public Coordinates(int x, int y) {
        	_x = x;
        	_y = y;
        }
        
        public int getX() {
            return _x + _bitmap.getWidth() / 2;
        }

        public void setX(int value) {
            _x = value - _bitmap.getWidth() / 2;
        }
        
        public void setRealX(int value) {
        	_x = value;
        }

        public int getY() {
            return _y + _bitmap.getHeight() / 2;
        }

        public void setY(int value) {
            _y = value - _bitmap.getHeight() / 2;
        }
        
        public void setRealY(int value) {
        	_y = value;
        }

        public String toString() {
            return "Coordinates: (" + _x + "/" + _y + ")";
        }
    }

    private Bitmap _bitmap;
    private Coordinates _coordinates;

    public GraphicObject(Bitmap bitmap) {
        _bitmap = bitmap;
        _coordinates = new Coordinates();
    }
    
    public GraphicObject(Bitmap bitmap, int x, int y) {
        _bitmap = bitmap;
        _coordinates = new Coordinates(x, y);
    }

    public Bitmap getGraphic() {
        return _bitmap;
    }

    public Coordinates getCoordinates() {
        return _coordinates;
    }
}
