package inkball;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.Random;

public class Ball {
    PImage sprite;
    int color;
    private boolean hasSpawned = false;
    private Vec2 pos = new Vec2(0, 0);
    float dx = 0;
    float dy = 0;
    float spriteScaleFactor = 1f;
    // set as a float for division in draw section
    static final float radius = 16;
    static Random random = new Random();

    public Ball(int color, boolean isInit) {
        this.color = color;
        this.hasSpawned = true;
        setSprite("ball" + this.color);
        initVelocity();
    }

    Vec2 getPosVec() {
        return this.pos;
    }

    void spawn() {
        this.hasSpawned = true;
    }

    boolean hasSpawned() {
        return this.hasSpawned;
    }

    public Ball(int color) {
        this.color = color;
        setSprite("ball" + this.color);
        initVelocity();
    }

    void setVel(Vec2 newVel) {
        this.dx = newVel.x;
        this.dy = newVel.y;
    }

    /**
     * Multiples the balls x velocity by -1
     */
    void bounceX() {
        this.dx *= -1;
    }

    /**
     * Multiples the balls y velocity by -1
     */
    void bounceY() {
        this.dy *= -1;
    }


    Vec2 getVelVec() {
        return new Vec2(this.dx, this.dy);
    }

    /**
     * Params should be passed as coords
     * @param x
     * @param y
     */
    void setInitPos(int x, int y) {
        x *= App.CELLSIZE;
        y = y  * App.CELLSIZE + App.TOPBAR;
        this.pos.x = x;
        this.pos.y = y;
    }

    /**
     * Sets sprite for ball
     * @param sprite Cannot be null
     */
    void setSprite(PImage sprite) {
        if (sprite == null) {
            throw new IllegalArgumentException("Cannot set sprite as null");
        }
        this.sprite = sprite;
    }

    /**
     * Wrapper to convert string to PImage for other setSprite method
     * @param spriteName Sprite name. **Not a path**
     */
    void setSprite(String spriteName) {
        // will do error handling as well as set sprite
        this.setSprite(App.getSprite(spriteName));
    }

    private void initVelocity() {
        // returns 0 - 1
        int xDir = random.nextInt(1);
        int yDir = random.nextInt(1);

        // gives either 2 or -2
        dx = 1 - (2 * xDir);
        dy = 1 - (2 * yDir);
    }

    void move() {
        this.pos.x += dx;
        this.pos.y += dy;
    }

    void draw(PApplet window) {
        float xPos = (float)(this.pos.x - Ball.radius);
        float yPos = (float)(this.pos.y + Ball.radius);

        // same size for both width and height
        float size = Ball.radius * this.spriteScaleFactor * 2;

        // needs casting from double to float
        window.image(this.sprite, xPos, yPos, size, size);
    }
}
