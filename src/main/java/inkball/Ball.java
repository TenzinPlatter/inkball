package inkball;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.Random;

public class Ball {
    PImage sprite;
    String color;
    private boolean isInitBall = false;
    private boolean hasSpawned = false;
    private double x = 0;
    private double y = 0;
    private double dx = 0;
    private double dy = 0;
    static Random random = new Random();

    public Ball(String color, boolean isInit) {
        this.color = color;
        this.isInitBall = isInit;
        init(isInit);
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
        this.x = x;
        this.y = y;
    }

    private void init(boolean isInit) {
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

        this.sprite = App.getSprite("ball" + code);
    }

    void move() {
        x += dx;
        y += dy;
    }

    void draw(PApplet window) {
        float width = (float)(this.x - 32f/2);
        float height = (float)(this.y + 32f/2);

        // cast from double to float
        window.image(this.sprite, width, height);
    }
}
