package inkball;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import processing.core.PApplet;

import java.io.UnsupportedEncodingException;

import static org.junit.jupiter.api.Assertions.*;

class AppTest {
  App app;

  @BeforeEach
  void appSetup() {
    App.setScore(0);
    app = new App();
    PApplet.runSketch(new String[] {"inkball"}, app);
    app.setup();
  }

  @Test
  void loadImageFromPathThrowsExceptionForInvalidPath() {
    assertThrows(RuntimeException.class, () -> app.loadImageFromPath("invalid"));
  }

  @Test
  void getColorCodeThrowsForInvalidColor() {
    assertThrows(RuntimeException.class, () -> {
      App.getColorCode("Not a color");
    });
  }

  @Test
  void scoreGetterAndSetterTest() {
    assertEquals(0, App.getScore());

    App.setScore(20);

    assertEquals(20, App.getScore());
  }

  @Test
  void addScoreTest() {
    assertEquals(0, App.getScore());

    App.addScore(10);

    assertEquals(10, App.getScore());
  }
}
