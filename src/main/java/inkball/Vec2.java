package inkball;

public class Vec2 {
    float x;
    float y;

    public Vec2(float x, float y) {
        this.x = x;
        this.y =y;
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
     * Finds the distance between this (as a point) and another point
     * @param v Second point
     * @return Distance
     */
    double distanceTo(Vec2 v) {
        return Math.sqrt(
                Math.pow((v.x - this.x), 2)
                + Math.pow((v.y - this.y), 2)
        );
    }

    /**
     * Takes the distance between this vector (as a point) and the origin
     * @return Returns the magnitude (distance to origin)
     */
    double magnitude() {
        return this.distanceTo(0, 0);
    }

    /**
     * Adds two vectors together
     * @param v The vector to be added
     * @return Result of the addition
     */
    Vec2 add(Vec2 v) {
        return new Vec2(this.x + v.x, this.y + v.y);
    }

    /**
     * Finds the unit vector for this vector
     * @return The unit vector
     */
    Vec2 getUnitVec() {
        float magnitude = (float) this.magnitude();

        float x = this.x / magnitude;
        float y = this.y / magnitude;

        return new Vec2(x, y);
    }

    /**
     * Finds the dot product between this vector and another
     * @param v The second vector to be used in the dot product
     * @return The value of the dot product
     */
    float dot(Vec2 v) {
        return (this.x * v.x) + (this.y * v.y);
    }

    /**
     * Finds the vector projection of this onto another vector
     * @param v Vector to be projected onto
     * @return Vector projection
     */
    Vec2 projectionOnto(Vec2 v) {
        float dotUV = this.dot(v);
        float magV = (float) Math.sqrt((v.x * v.x) + (v.y * v.y));
        float k = dotUV / magV;

        return new Vec2(k * v.x, k * v.y);
    }

    /**
     * Finds the distance between this (as a point) and another point
     * @param x X coordinate of second point
     * @param y Y coordinate of second point
     * @return Distance value
     */
    double distanceTo(float x, float y) {
        return Math.sqrt(
                Math.pow((x - this.x), 2)
                + Math.pow((y - this.y), 2)
        );
    }
}
