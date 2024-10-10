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

    /**
     * Clamps the value b between values a and c
     * @param a
     * @param b
     * @param c
     * @return
     */
    float clamp(float a, float b, float c) {
        if (b < a) { return a; }
        if (b > c) { return c; }
        return b;
    }

    /**
     * Handles collision for a given ball on this cell, should be called before move
     * @param ball Ball to check
     * @param neighbors Array of bools, stating wether or not the cell has surrounding cells,
     *                  in the order -> Above, Below, Left, Right
     * @return Returns true if collision has happened, false otherwise
     */
    boolean handleCollision(Ball ball, boolean[] neighbors) {
        int ABOVE = 0;
        int BELOW = 1;
        int LEFT = 2;
        int RIGHT = 3;

        if (!this.isWall()) {
            return false;
        }

        Vec2 v = ball.getPosVec();
        float size = App.CELLSIZE;
        int[] coords = this.getPos(false);
        int x = coords[0];
        int y = coords[1];

        float closestX = clamp(x, v.x, x + size);
        float closestY = clamp(y - size, v.y, y);

        if (v.distanceTo(closestX, closestY) > Ball.radius) {
            return false;
        }

        int colorCode = this.getWallColor();
        if (colorCode != -1) {
            ball.setSprite("ball" + colorCode);
        }

        if (
                v.x > x + size && !neighbors[RIGHT]
                || v.x < x && !neighbors[LEFT]
        ) {
            ball.dx *= -1;
        }

        if (
                v.y > y && !neighbors[BELOW]
                || v.y < y + size && !neighbors[ABOVE]
        ) {
            ball.dy *= -1;
        }

        return true;
    }

    /**
     * Method to check the color of a wall
     * @return returns integer code corresponding to color, -1 for no color/ not a wall
     * 1 -> orange
     * 2 -> blue
     * 3 -> green
     * 4 -> yellow
     */
    int getWallColor() {
        for (int i = 1; i < 5; i++) {
            if (App.getSprite("wall" + i) == this.sprite) {
                return i;
            }
        }

        return -1;
    }

    boolean isWall() {
        for (int i = 0; i < 5; i++) {
            if (App.getSprite("wall" + i) == this.sprite) {
                return true;
            }
        }

        return false;
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
