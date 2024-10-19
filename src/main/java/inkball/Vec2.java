package inkball;

public class Vec2 {
    float x;
    float y;

    public Vec2(float x, float y) {
        this.x = x;
        this.y =y;
    }

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
     * Prints the x and y coordinates of this vector
     */
    void print() {
        System.out.printf("Vec: (%f %f)\n", this.x, this.y);
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

    double distanceTo(Vec2 v) {
        return Math.sqrt(
                Math.pow((v.x - this.x), 2)
                + Math.pow((v.y - this.y), 2)
        );
    }

    double magnitude() {
        return this.distanceTo(0, 0);
    }

    Vec2 add(Vec2 v) {
        return new Vec2(this.x + v.x, this.y + v.y);
    }

    Vec2 getUnitVec() {
        float magnitude = (float) this.magnitude();

        float x = this.x / magnitude;
        float y = this.y / magnitude;

        return new Vec2(x, y);
    }

    float dot(Vec2 v) {
        return (this.x * v.x) + (this.y * v.y);
    }

    Vec2 projectionOnto(Vec2 v) {
        float dotUV = this.dot(v);
        float magV = (float) Math.sqrt((v.x * v.x) + (v.y * v.y));
        float k = dotUV / magV;

        return new Vec2(k * v.x, k * v.y);
    }

    double distanceTo(float x, float y) {
        return Math.sqrt(
                Math.pow((x - this.x), 2)
                + Math.pow((y - this.y), 2)
        );
    }
}
