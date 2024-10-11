package inkball;

import processing.core.PApplet;

import java.util.ArrayList;

public class Line {
    private ArrayList<Vec2> points = new ArrayList<>();

    public Line(Vec2 start) {
        this.points.add(start);
    }

    void addPoint(Vec2 point) {
        this.points.add(point);
    }

    /**
     * Checks for collision between passed in ball and line
     * @param ball
     * @return Returns false for no collision, true for collision
     */
    boolean handleCollision(Ball ball) {
        Vec2 a = null;
        Vec2 b = null;
        Vec2 ballP = ball.getPosVec();
        Vec2 ballV = ball.getVelVec();

        for (int i = 0; i < points.size() - 1; i++) {
            Vec2 u = points.get(i);
            Vec2 v = points.get(i + 1);


            // colliding with wall section
            if (
                    u.distanceTo(ballP) + v.distanceTo(ballP)
                    < u.distanceTo(v) + (Ball.radius * ball.spriteScaleFactor)
            ) {
                a = u;
                b = v;
                break;
            }
        }

        // return out if colliding section was not found
        if (a == null) {
            return false;
        }

        float dx = b.x - a.x;
        float dy = b.y - a.y;

        Vec2 n1 = new Vec2(-dy, dx);
        Vec2 n2 = new Vec2(dy, -dx);

        n1 = n1.getUnitVec();
        n2 = n2.getUnitVec();

        Vec2 midpoint = new Vec2((n1.x + n2.x)/2, (n1.y + n2.y)/2);

        Vec2 closest = n1;

        if (midpoint.add(n2).distanceTo(ballP) < midpoint.add(n1).distanceTo(ballP)) {
            closest = n2;
        }

        float k = 2 * ballV.dot(closest);

        Vec2 newVel = new Vec2(
                ballV.x - (k * closest.x),
                ballV.y - (k * closest.y)
        );

        ball.setVel()

        return true;
    }

    void draw(PApplet window) {
        Vec2 last = null;
        for (Vec2 point : points) {
            // round end of lines and turns
            window.ellipse(point.x, point.y, 1, 1);

            if (last == null) {
                last = point;
                continue;
            }

            window.strokeWeight(10);
            window.line(last.x, last.y, point.x, point.y);
        }
    }
}
