package inkball;

import processing.core.PApplet;
import processing.core.PImage;

public class Cell {
    private boolean isHole = false;
    private String type;
    private PImage sprite;
    private int x;
    private int y;
    private PImage preAnimSprite = null;

    /**
     * Constructor for cell
     * @param type Type of cell e.g. "wall", "none" for non collision tile
     * @param x x coordinate of cell
     * @param y y coordinate of cell
     */
    public Cell(String type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;

        // none squares will return null for sprite
        this.sprite = App.getSprite(type);
    }

    /**
     * Sets a wall to its state before being set as a yellow wall in level end animation
     */
    void setOldWall() {
        this.sprite = preAnimSprite;
    }

    /**
     * Sets a wall as a yellow wall for end level animation
     * Use this method rather than setting manually so that wall can be put back after
     */
    void setYellowWall() {
        this.preAnimSprite = this.sprite;
        this.sprite = App.getSprite("wall4");
    }

    /**
     * Clamps the value b between values a and c
     * @param a Minimum value
     * @param b Value to clamp
     * @param c Maximum
     * @return Clamped value
     */
    float clamp(float a, float b, float c) {
        return Math.max(a, Math.min(b, c));
    }

    /**
     * Handles collision for a given ball on this cell, should be called before move
     * @param ball Ball to check
     * @param neighbors Array of bools, stating whether the cell has surrounding cells,
     *                  in the order -> Above, Below, Left, Right
     * @return Returns true if collision has happened, false otherwise
     */
    boolean handleCollision(Ball ball, boolean[] neighbors) {
        int ABOVE = 0;
        int BELOW = 1;
        int LEFT = 2;
        int RIGHT = 3;

        Vec2 v = ball.getPosVec();
        float size = App.CELLSIZE;
        int[] coords = this.getPos();
        int x = coords[0];
        int y = coords[1];

        float closestX = clamp(x, v.x, x + size);
        float closestY = clamp(y, v.y, y + size);

        if (v.distanceTo(closestX, closestY) > Ball.radius * ball.spriteScaleFactor) {
            return false;
        }

        int colorCode = this.getColorFor("wall");
        if (colorCode != 0) {
            ball.setSprite("ball" + colorCode);
            ball.color = colorCode;
        }

        //TODO: fix bug with side hit changing y velo, maybe add velo checks for direction?
        //TODO: fix ball spawning in and immediately going into hole
        if (
                (v.x > x + size && !neighbors[RIGHT])
                || (v.x < x && !neighbors[LEFT])
        ) {
            ball.bounceX();
        }

        if (
                (v.y > y && !neighbors[BELOW])
                || (v.y < y + size && !neighbors[ABOVE])
        ) {
            ball.bounceY();
        }

        return true;
    }

    /**
     * Method to check the color of a given type of sprite
     * @param type Either hole or wall
     * @return returns integer code corresponding to color:
     0 for no color/grey
     * 1 -> orange
     * 2 -> blue
     * 3 -> green
     * 4 -> yellow
     */
    int getColorFor(String type) {
        for (int i = 1; i < 5; i++) {
            if (App.getSprite(type + i) == this.sprite) {
                return i;
            }
        }

        return 0;
    }

    /**
     *
     * @return Whether the cell is a collisional wall
     */
    boolean isWall() {
        return this.type.contains("wall");
    }

    void setHole() {
        this.isHole = true;
    }

    boolean isHole() {
        return this.isHole;
    }

    int[] getPos() {
        return new int[] {this.x * App.CELLSIZE, (this.y * App.CELLSIZE) + 64};
    }

    /**
     * Draws the cell
     * @param window Window to draw the cell to
     */
    void draw(PApplet window) {
        if (this.sprite == null) {
            return;
        }

        int[] pos = this.getPos();
        int width = (this.isHole()) ? App.CELLSIZE * 2 : App.CELLSIZE;
        int height = (this.isHole()) ? App.CELLSIZE * 2 : App.CELLSIZE;

        window.image(this.sprite, pos[0], pos[1], width, height);
    }
}
