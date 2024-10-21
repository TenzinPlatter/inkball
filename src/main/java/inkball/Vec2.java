package inkball;

public class Vec2 {
  float x;
  float y;

  public Vec2(float x, float y) {
    this.x = x;
    this.y =y;
  }

  /**
   *
   * @return A copy of the vector
   */
  Vec2 copy() {
    return new Vec2(this.x, this.y);
  }

  /**
   * Converts this vecs position from coords to position, does not check for valid coords
   * @return New vec
   */
  Vec2 coordsToPos() {
    return new Vec2(
            this.x * App.CELLSIZE,
            this.y * App.CELLHEIGHT + App.TOPBAR
    );
  }

  /**
   * Converts this vecs position from position to coords, does not check for valid coords
   * @return New vec
   */
  Vec2 posToCoords() {
    return new Vec2(
            (float) ((int) this.x / App.CELLSIZE),
            (float) ((int) (this.y - App.TOPBAR) / App.CELLHEIGHT)
    );
  }

  /**
   * Finds the vector between this and another
   * @param v Starting point
   * @return
   */
  Vec2 to (Vec2 v) {
    return new Vec2(
            v.x - this.x,
            v.y - this.y
    );
  }

  /**
   * Centers the point of the vec, assuming a rectangle with width and height given, also assuming
   * vec originally points to top left
   * @param width
   * @param height
   * @return New vec pointing to new location
   */
  Vec2 centerCoords(float width, float height) {
    return new Vec2(
            x + width/2,
            y + height/2
    );
  }

  /**
   * Centers the point of the vec, assuming a square with sidelength given, also assuming
   * vec originally points to top left
   * @param length
   * @return
   */
  Vec2 centerCoords(float length) {
    return new Vec2(
            x + length/2,
            y + length/2
    );
  }

  /**
   *
   * @param v Second vector
   * @return Distance to second vector
   */
  double distanceTo(Vec2 v) {
    return Math.sqrt(
            Math.pow((v.x - this.x), 2)
                    + Math.pow((v.y - this.y), 2)
    );
  }

  double magnitude() {
    return this.distanceTo(0, 0);
  }

  /**
   * Adds another vector
   * @param v Vector to add
   * @return Vector after being added
   */
  Vec2 add(Vec2 v) {
    return new Vec2(this.x + v.x, this.y + v.y);
  }

  /**
   * Normalises the vector
   * @return The normalised vector
   */
  Vec2 getUnitVec() {
    float magnitude = (float) this.magnitude();

    float x = this.x / magnitude;
    float y = this.y / magnitude;

    return new Vec2(x, y);
  }

  /**
   * Returns the dot product between this and another vector
   * @param v Second vector
   * @return The value of the dot product
   */
  float dot(Vec2 v) {
    return (this.x * v.x) + (this.y * v.y);
  }

  /**
   * Returns the distance between this vector and another
   * @param x x coordinate of second vector
   * @param y y coordinate of second vector
   * @return Distance value
   */
  double distanceTo(float x, float y) {
    return Math.sqrt(
            Math.pow((x - this.x), 2)
                    + Math.pow((y - this.y), 2)
    );
  }
}
