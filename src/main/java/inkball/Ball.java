package inkball;

import processing.core.PApplet;
import processing.core.PImage;

import java.util.Random;

public class Ball {
  private PImage sprite;
  int color;
  private boolean hasSpawned = false;
  private final Vec2 pos = new Vec2(0, 0);
  float dx = 0;
  float dy = 0;
  float spriteScaleFactor = 1f;

  // set as a float for division in draw section
  static final float radius = 12f;
  static Random random = new Random();

  public Ball(int colorcode) {
    this.color = colorcode;
    setSprite("ball" + this.color);
    setInitVelocity();
  }

  PImage getSprite() {
    return this.sprite;
  }

  Vec2 getPosVec() {
    return this.pos.copy();
  }

  void spawn() {
    this.hasSpawned = true;
  }

  boolean hasSpawned() {
    return this.hasSpawned;
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
    return new Vec2(this.dx, this.dy).copy();
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
   * @throws IllegalArgumentException If sprite is null
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


  /**
   * Randomly sets initial velocity
   */
  private void setInitVelocity() {
    // returns 0 - 1
    int xDir = random.nextInt(2);
    int yDir = random.nextInt(2);

    // gives either 1 or -1
    // (not 2 as using 60fps)
    dx = 1 - (2 * xDir);
    dy = 1 - (2 * yDir);
  }

  /**
   * Adds balls velocity to its position
   */
  void move() {
    this.pos.x += dx;
    this.pos.y += dy;
  }

  /**
   * Draws a ball to the screen
   * @param window Window to draw ball onto
   */
  void draw(PApplet window) {
    float xPos = this.pos.x - Ball.radius;
    float yPos = this.pos.y - Ball.radius;

    // same size for both width and height
    float size = Ball.radius * this.spriteScaleFactor * 2;

    // needs casting from double to float
    window.image(this.sprite, xPos, yPos, size, size);
  }
}
