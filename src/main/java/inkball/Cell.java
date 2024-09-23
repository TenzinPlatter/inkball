package inkball;

import processing.core.PApplet;
import processing.core.PImage;

public class Cell {
    private boolean isHole = false;
    private String type;
    private PImage sprite;
    private int x;
    private int y;
    private Ball ball;

    public Cell(String type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;

        // none squares will return null for sprite
        this.sprite = App.getSprite(type);
    }


    void setHole() {
        this.isHole = true;
    }

    boolean isHole() {
        return this.isHole;
    }

    int[] getPos(boolean asCoords) {
        if (asCoords) {
            return new int[] {this.x, this.y};
        }

        return new int[] {this.x * App.CELLSIZE, (this.y * App.CELLSIZE) + 64};
    }

    String getType() {
        return this.type;
    }

    void draw(PApplet window) {
        if (this.sprite == null) {
            return;
        }

        int[] pos = this.getPos(false);
        int width = (this.isHole()) ? App.CELLSIZE * 2 : App.CELLSIZE;
        int height = (this.isHole()) ? App.CELLSIZE * 2 : App.CELLSIZE;

        window.image(this.sprite, pos[0], pos[1], width, height);
    }
}
