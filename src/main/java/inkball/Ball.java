package inkball;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.Random;

public class Ball {
    PImage sprite;
    String color;
    private boolean hasSpawned = false;
    private Vec2 pos = new Vec2(0, 0);
    double dx = 0;
    double dy = 0;
    // set as a float for division in draw section
    static final float radius = 16;
    static Random random = new Random();

    public Ball(String color, boolean isInit) {
        this.color = color;
        this.hasSpawned = true;
        init();
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

    public Ball(String color) {
        this.color = color;
        init();
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

    private void init() {
        initSprite();
        initVelocity();
    }

    private void initVelocity() {
        // returns 0 - 1
        int xDir = random.nextInt(2);
        int yDir = random.nextInt(2);

        // gives either 2 or -2
        dx = 2 - (4 * xDir);
        dy = 2 - (4 * yDir);
    }

    private void initSprite() {
        int code = -1;
        switch (this.color) {
            case "grey":
                code = 0;
                break;
            case "orange":
                code = 1;
                break;
            case "blue":
                code = 2;
                break;
            case "green":
                code = 3;
                break;
            case "yellow":
                code = 4;
                break;
        }

        if (code == -1) {
            throw new RuntimeException("Invalid ball color: " + this.color);
        }

        this.sprite = App.getSprite("ball" + code);
    }

    void move() {
        this.pos.x += dx;
        this.pos.y += dy;
    }

    void draw(PApplet window) {
        float xPos = (float)(this.pos.x - Ball.radius);
        float yPos = (float)(this.pos.y + Ball.radius);

        // cast from double to float
        window.image(this.sprite, xPos, yPos);
    }
}
