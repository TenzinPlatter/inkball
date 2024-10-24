package inkball;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processing.core.PApplet;

import static org.junit.jupiter.api.Assertions.*;

class LineTest {
  App app;
  Line line;

  @BeforeEach
  void first() {
    app = new App();
    PApplet.runSketch(new String[] {"inkball"}, app);
    app.setup();
    line = new Line(new Vec2(0, 0));
  }

  @Test
  void addPoint() {
    line.addPoint(new Vec2(1, 1));

    assertEquals(1, line.points.get(1).x);
    assertEquals(1, line.points.get(1).y);
  }

  @Test
  void handleCollision() {
    Ball ball = new Ball(0);
    ball.setInitPos(1, 1);
    ball.setVel(new Vec2(-1, -1));
    ball.spawn();

    line.addPoint(new Vec2(32, 96));

    line.handleCollision(ball);

    assertEquals(-1, ball.getVelVec().x);
    assertEquals(-1, ball.getVelVec().y);
  }
}