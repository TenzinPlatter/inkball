package inkball;

import processing.core.PApplet;

import java.util.ArrayList;

public class Line {
  final ArrayList<Vec2> points = new ArrayList<>();

  public Line(Vec2 start) {
    this.points.add(start);
  }

  /**
   * Adds a point to this line
   * @param point Coordinates of point to be added
   */
  void addPoint(Vec2 point) {
    this.points.add(point);
  }

  /**
   * Checks for collision between passed in ball and line
   * @param ball Ball to be checked
   * @return Returns false for no collision, true for collision
   */
  boolean handleCollision(Ball ball) {
    Vec2 a;
    Vec2 b;
    Vec2 ballV = ball.getVelVec();
    Vec2 ballP = ball.getPosVec();

    // requires passing radius as it is used for checking for mouse collision with lines aswell
    Vec2[] res = checkForCollision(ballP, (int) Ball.radius);

    if (res == null) {
      return false;
    }

    a = res[0];
    b = res[1];

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

    ball.setVel(newVel);

    return true;
  }

  /**
   * Checks for collision between the line and a circle
   * @param point Position of the circle
   * @param radius Radius of the circle
   * @return True if collision, else false
   */
  Vec2[] checkForCollision(Vec2 point, int radius) {
    for (int i = 0; i < points.size() - 1; i++) {
      Vec2 u = points.get(i);
      Vec2 v = points.get(i + 1);

      if (
              u.distanceTo(point) + v.distanceTo(point)
              < u.distanceTo(v) + radius
      ) {
        // collided with wall section
        return new Vec2[] {u, v};
      }
    }

    return null;
  }

  /**
   * Draws a line, won't draw lines with only one point
   * @param window Surface to be drawn to
   */
  void draw(PApplet window) {
    Vec2 last = points.get(0);

    window.strokeWeight(10);
    for (Vec2 point : points) {
      window.line(last.x, last.y, point.x, point.y);

      last = point;
    }
  }
}
