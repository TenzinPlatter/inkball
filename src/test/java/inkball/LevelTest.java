package inkball;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processing.core.PApplet;

import static org.junit.jupiter.api.Assertions.*;

class LevelTest {
  App app;

  @BeforeEach
  void appSetup() {
    app = new App();
    PApplet.runSketch(new String[] {"inkball"}, app);
    app.setup();
  }

  @Test
  void rotateYellowCellsTest() {
    app.currentLevel.startLevelEndAnim();
    int len = 18;

    assertEquals(App.getSprite("wall4"), app.currentLevel.cells[0][0].sprite);
    assertEquals(App.getSprite("wall4"), app.currentLevel.cells[len - 1][len - 1].sprite);

    app.currentLevel.rotateYellowCells();

    assertEquals(App.getSprite("wall4"), app.currentLevel.cells[1][0].sprite);
    assertEquals(App.getSprite("wall4"), app.currentLevel.cells[len - 2][len - 1].sprite);
  }

  @Test
  void rotateYellowCellsThrowsOutsideOfAnimation() {
    assertThrows(RuntimeException.class, () -> app.currentLevel.rotateYellowCells());
  }

  @Test
  void removeLinePointTest() {
    Line line = new Line(new Vec2(0, 0));
    line.addPoint(new Vec2(5, 5));

    app.currentLevel.lines.add(line);

    assertEquals(1, app.currentLevel.lines.size());

    app.currentLevel.removeCurrentLinePoint(3, 3);

    assertEquals(0, app.currentLevel.lines.size());
  }
}