package inkball;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import processing.core.PImage;

class BallTest {
	private Ball ball;

	@BeforeEach
	void ballSetup() {
		ball = new Ball(1);
	}

	@Test
	void getPosVecTest() {
		// default value of a balls position before it is spawned
		Vec2 expected = new Vec2(0, 0);
		Vec2 actual = ball.getPosVec();

		assertEquals(expected.x, actual.x);
		assertEquals(expected.y, actual.y);
	}

	@Test
	void spawnAndHasSpawnedTest() {
		assertFalse(ball.hasSpawned());

		ball.spawn();

		assertTrue(ball.hasSpawned());
	}

	@Test
	void setVelTest() {
		ball.setVel(new Vec2(4, 3));

		Vec2 expected = new Vec2(4, 3);
		Vec2 actual = ball.getVelVec();

		assertEquals(expected.x, actual.x);
		assertEquals(expected.y, actual.y);
	}

	@Test
	void bounceInXandYDirectionTest() {
		ball.setVel(new Vec2(-4, 7));

		ball.bounceX();
		ball.bounceY();

		assertEquals(ball.getVelVec().x, 4);
		assertEquals(ball.getVelVec().y, -7);
	}

	@Test
	void getVelVecTest() {
		ball.setVel(new Vec2(1, 1));
		Vec2 actual = ball.getVelVec();

		Vec2 expected = new Vec2(1, 1);

		assertEquals(expected.x, actual.x);
		assertEquals(expected.y, actual.y);
	}

	@Test
	void setInitPosTest() {
		ball.setInitPos(5, 2);

		Vec2 actual = ball.getPosVec();
		Vec2 expected = new Vec2(160, 128);

		assertEquals(expected.x, actual.x);
		assertEquals(expected.y, actual.y);
	}

	@Test
	void moveTest() {
		ball.setVel(new Vec2(1, 1));
		ball.move();

		Vec2 expected = new Vec2(1, 1);
		Vec2 actual = ball.getPosVec();

		assertEquals(expected.x, actual.x);
		assertEquals(expected.y, actual.y);
	}

}
