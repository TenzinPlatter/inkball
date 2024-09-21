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

    void giveBall(char c) {
        String color;
        switch (c) {
            case '0':
                color = "silver";
                break;
            case '1':
                color = "orange";
                break;
            case '2':
                color = "blue";
                break;
            case '3':
                color = "green";
                break;
            case '4':
                color = "gold";
                break;
            default:
                throw new RuntimeException("Need to pass giveBall a no. 0-4, not: " + c);
        }

        this.ball = new Ball(color);
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

        return new int[] {this.x * 32, (this.y * 32) + 64};
    }

    String getType() {
        return this.type;
    }

    void draw(PApplet window) {
        if (this.sprite == null) {
            return;
        }

        int[] pos = this.getPos(false);
        int width = (this.isHole()) ? 64 : 32;
        int height = (this.isHole()) ? 64 : 32;

        window.image(this.sprite, pos[0], pos[1], width, height);
    }
}
