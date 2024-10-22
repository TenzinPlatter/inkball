package inkball;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Vec2Test {
	@Test
	void copyReturnsCorrectValues() {
		Vec2 first = new Vec2(1, 2);
		Vec2 second = first.copy();

		assertEquals(first.x, second.x);
		assertEquals(first.y, second.y);
	}

	@Test
	void copyReturnsNewVec() {
		Vec2 first = new Vec2(1, 2);
		Vec2 second = first.copy();

		first.x++;

		assertEquals(first.x, second.x + 1);
	}

	@Test
	void coordsToPosReturnsCorrectValues() {
		Vec2 coords = new Vec2(1, 1);
		Vec2 pos = coords.coordsToPos();

		assertEquals(coords.x * App.CELLSIZE, pos.x);
		assertEquals(coords.y * App.CELLHEIGHT + App.TOPBAR, pos.y);
	}

	@Test
	void posToCoordsReturnsCorrectValues() {
		Vec2 pos = new Vec2(2 * App.CELLSIZE, 2 * App.CELLHEIGHT + App.TOPBAR);
		Vec2 coords = pos.posToCoords();

		assertEquals(coords.x, (float) ((int) pos.x / App.CELLSIZE));
		assertEquals(coords.y, (float) ((int) ((pos.y - App.TOPBAR) / App.CELLHEIGHT)));
	}

	@Test
	void toReturnsCorrectVector() {
		Vec2 a = new Vec2(1, 1);
		Vec2 b = new Vec2(2, 2);
		Vec2 c = new Vec2(1, 1);

		Vec2 got = a.to(b);

		assertEquals(c.x, got.x);
		assertEquals(c.y, got.y);
	}

	@Test
	void centerCoordsReturnsCorrectValuesOneArg() {
		Vec2 first = new Vec2(100, 100);
		Vec2 second = first.centerCoords(50);

		assertEquals(first.x + 25, second.x);
		assertEquals(first.y + 25, second.y);
	}

	@Test
	void centerCoordsReturnsCorrectValuesTwoArgs() {
		Vec2 first = new Vec2(100, 100);
		Vec2 second = first.centerCoords(50, 70);

		assertEquals(first.x + 25, second.x);
		assertEquals(first.y + 35, second.y);
	}

	@Test
	void distanceToReturnsCorrectValue() {
		Vec2 a = new Vec2(1, 1);
		Vec2 b = new Vec2(4, 5);
		int expectedDistance = 5;;

		assertEquals(expectedDistance, a.distanceTo(b));
	}

	@Test
	void magnitudeReturnsCorrectValue() {
		Vec2 v = new Vec2(5, 12);
		int expectedMagnitude = 13;

		assertEquals(expectedMagnitude, v.magnitude());
	}

	@Test
	void addReturnsCorrectValues() {
		Vec2 first = new Vec2(1, 1);
		Vec2 toAdd = new Vec2(-2, 8);
		Vec2 result = first.add(toAdd);
		Vec2 expectedResult = new Vec2(-1, 9);

		assertEquals(expectedResult.x, result.x);
		assertEquals(expectedResult.y, result.y);
	}

	@Test
	void getUnitVecReturnsCorrectValues() {
		Vec2 got = (new Vec2(4, 3)).getUnitVec();
		Vec2 expected = new Vec2(0.8f, 0.6f);

		assertEquals(expected.x, got.x);
		assertEquals(expected.y, got.y);
	}

	@Test
	void dotReturnsCorrectValue() {
		Vec2 a = new Vec2(1, 2);
		Vec2 b = new Vec2(5, 5);

		assertEquals(a.dot(b), 15);
	}
}
