package inkball;

public class Vec2 {
    float x;
    float y;

    public Vec2(float x, float y) {
        this.x = x;
        this.y =y;
    }

    double distanceTo(Vec2 v) {
        return Math.sqrt(
                Math.pow((v.x - this.x), 2)
                + Math.pow((v.y - this.y), 2)
        );
    }

    double distanceTo(float x, float y) {
        return Math.sqrt(
                Math.pow((x - this.x), 2)
                + Math.pow((y - this.y), 2)
        );
    }
}
