package inkball;

import processing.core.PApplet;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class Level {
    private static int GREY = 0;
    private static int ORANGE = 1;
    private static int BLUE = 2;
    private static int GREEN = 3;
    private static int YELLOW = 4;

    private String layoutFilePath;
    private int timeLimit;
    private int spawnInterval;
    private long lastSpawnTime;
    private double increaseModifier;
    private double decreaseModifier;

    private ArrayList<Ball> balls = new ArrayList<>();
    private ArrayList<int[]> spawnerLocs = new ArrayList<>();
    private ArrayList<Vec2> holeLocs = new ArrayList<>();
    private ArrayList<Line> lines = new ArrayList<>();

    private Cell[][] cells = new Cell[18][18];

    private double[] scoreIncrease = new double[5];
    private double[] scoreDecrease = new double[5];

    private static double[] generalScoreIncrease = new double[5];
    private static double[] generalScoreDecrease = new double[5];

    public Level(JSONObject config) {
        layoutFilePath = config.getString("layout");
        timeLimit = config.getInt("time");
        spawnInterval = config.getInt("spawn_interval");
        increaseModifier = config.getFloat("score_increase_from_hole_capture_modifier");
        decreaseModifier = config.getFloat("score_decrease_from_wrong_hole_modifier");

        setCells();
        setBalls(config);
        adjustScoreAmounts();
    }

    /**
     * Trys to spawn next ball, fails if timer has not passed or no next ball to be spawned
     * @param time
     * @return
     */
    void trySpawnNext(long time) {
        float timePassedInSeconds = (time - lastSpawnTime) / 1000;

        boolean timerPassed = timePassedInSeconds > spawnInterval;

        if (!timerPassed) {
            return;
        }

        for (Ball b : this.balls) {
            if (b.hasSpawned()) {
                continue;
            }

            b.spawn();
            lastSpawnTime = time;
            break;
        }
    }

    boolean levelOver() {
        return this.balls.isEmpty();
    }

    void printCells() {
        for (int y = 0; y < 18; y++) {
            for (int x = 0; x < 18; x++) {
                Cell c = cells[x][y];
                int[] pos = c.getPos(true);
                System.out.printf("Cell at (%d, %d) - type: %s\n", pos[0], pos[1], c.getType());
            }
        }
    }

    void draw(PApplet window) {
        drawCells(window);
        drawLines(window);
        drawBalls(window);
    }

    void drawLines(PApplet window) {
        //TODO: handle collision
        for (Ball ball : this.balls) {
            this.handleLinesCollision(ball);
        }

        for (Line line : this.lines) {
            line.draw(window);
        }
    }

    void handleLinesCollision(Ball ball) {
        for (Line line : this.lines) {
            line.handleCollision(ball);
        }
    }

    /**
     * method to draw balls and handle all movement/sprite editing
     * @param window Window to draw onto
     */
    void drawBalls(PApplet window) {
        for (Ball b : balls) {
            if (!b.hasSpawned()) {
                continue;
            }

            boolean collided = false;

            // handles collision with walls
            for (int y = 0; y < 18; y++) {
                for (int x = 0; x < 18; x++) {
                    if (!collided) {
                        // sets to true if there was a collision, false otherwise
                        collided = cells[x][y].handleCollision(b, this.getNeighborsArr(x, y));
                    }
                }
            }

            // distance to center of closest hole
            Vec2 hole = this.handleHole(b);

            if (hole != null) {
                this.handleCapture(b, cells[(int)hole.x][(int)hole.y]);
            }

            b.move();
            b.draw(window);
        }
    }

    void handleCapture(Ball ball, Cell hole) {
        if (ball.color == 0 || hole.getColorFor("hole") == 0) {
            this.balls.remove(ball);
            App.addScore(this.scoreIncrease[ball.color]);
        }

        else if (ball.color == hole.getColorFor("hole")) {
            this.balls.remove(ball);
            App.addScore(this.scoreIncrease[ball.color]);
        }

        else {
            // move ball to back of queue as a copy
            this.balls.remove(ball);
            this.balls.add(new Ball(ball.color));
            App.addScore(-1 * this.scoreDecrease[ball.color]);
        }
    }

    /**
     * Finds closest hole to ball passed in and handles sprite shrinking for it
     * @param b Ball to be checked
     * @return Returns either the location of the hole that captured the ball, or null
     */
    Vec2 handleHole(Ball b) {
        float closestDistance = Float.MAX_VALUE;
        Vec2 closestHole = this.holeLocs.get(0);

        for (Vec2 loc : this.holeLocs) {
            Vec2 v = loc.centerCoords(64);
            float dist = (float) b.getPosVec().distanceTo(v);

            if (dist < closestDistance) {
                closestDistance = dist;
                closestHole = loc;
            }
        }

        if (closestDistance > 32) {
            b.spriteScaleFactor = 1;
            return null;
        }

        // ball will be captured when center is within 5px of holes center

        if (closestDistance <= 5) {
            // sprite scaling shouldn't matter as ball should be removed before it has a chance
            // to be drawn
            return closestHole;
        }

        Vec2 velUpdate = b.getPosVec().projectionOnto(closestHole);
        b.dx += (float) (velUpdate.x * 0.005);
        b.dy += (float) (velUpdate.y * 0.005);

        b.spriteScaleFactor = 32 / closestDistance;
        return null;
    }

    /**
     * Method for getting boolean neighbors array used in handling ball collision
     * @param x
     * @param y
     * @return returns array of booleans encoding whether neighboring cell is a wall
     * in order -> Above, Below, Left, Right
     */
    boolean[] getNeighborsArr(int x, int y) {
        if (x < 0 || x >= 18 || y < 0 || y >= 18) {
            throw new IllegalArgumentException("Illegal x or y coordinate: " + x + ", " + y);
        }

        boolean[] neighbors = new boolean[4];
        int ABOVE = 0;
        int BELOW = 1;
        int LEFT = 2;
        int RIGHT = 3;

        try {
            neighbors[ABOVE] = cells[x][y - 1].isWall();
        } catch (Exception e) {
            neighbors[ABOVE] = false;
        }

        try {
            neighbors[BELOW] = cells[x][y + 1].isWall();
        } catch (Exception e) {
            neighbors[BELOW] = false;
        }

        try {
            neighbors[LEFT] = cells[x - 1][y].isWall();
        } catch (Exception e) {
            neighbors[LEFT] = false;
        }

        try {
            neighbors[RIGHT] = cells[x + 1][y].isWall();
        } catch (Exception e) {
            neighbors[RIGHT] = false;
        }

        return neighbors;
    }

    void drawCells(PApplet window) {
        for (int y = 0; y < 18; y++) {
            for (int x = 0; x < 18; x++) {
                cells[x][y].draw(window);
            }
        }
    }

    void setCells() {
        FileReader layoutFile;
        try {
            layoutFile = new FileReader(layoutFilePath);
        } catch (FileNotFoundException e) { throw new RuntimeException(e); }

        BufferedReader layout = new BufferedReader(layoutFile);
        String line;
        int y = 0;
        boolean inBall = false;
        boolean inHole = false;
        char c;

        try { line = layout.readLine(); } catch (IOException e) { throw new RuntimeException(); }

        while (line != null) {
            for (int x = 0; x < 18; x++) {
                try {
                    c = line.charAt(x);
                } catch (Exception e) {
                    cells[x][y] = new Cell("tile", x, y);
                    continue;
                }

                if (cells[x][y] != null) {
                    continue;
                }

                if (c == ' ') {
                    cells[x][y] = new Cell("tile", x, y);
                }

                else if (c == 'X') {
                    cells[x][y] = new Cell("wall0", x, y);
                }

                else if (c == 'B') {
                    inBall = true;
                }

                else if (c == 'S') {
                    cells[x][y] = new Cell("entrypoint", x, y);
                    spawnerLocs.add(new int[] {x, y});
                }

                else if (c == 'H') {
                    inHole = true;
                }

                else if (c == '0' || c == '1' || c == '2' || c == '3' || c == '4') {
                    if (inBall) {
                        cells[x - 1][y] = new Cell("tile", x - 1, y);
                        giveBallInit(c, x, y);

                        cells[x][y] = new Cell("tile", x, y);
                        inBall = false;

                    } else if (inHole) {
                        cells[x - 1][y] = new Cell("hole" + c, x - 1, y);
                        cells[x][y] = new Cell("none", x, y);
                        cells[x - 1][y + 1] = new Cell("none", x - 1, y + 1);
                        cells[x][y + 1] = new Cell("none", x, y + 1);

                        cells[x - 1][y].setHole();
                        this.holeLocs.add(new Vec2(x - 1, y));

                        inHole = false;

                    } else {
                        cells[x][y] = new Cell("wall" + c, x, y);
                    }
                }
            }

            try { line = layout.readLine(); } catch (IOException e) { throw new RuntimeException(); }
            y++;
        }

        try { layout.close(); } catch (IOException e) { throw new RuntimeException(); }
    }

    void giveBallInit(char colorCode, int x, int y) {
        int color = (int) (colorCode - '0');
        if (color < 0 || color > 4) {
            throw new IllegalArgumentException("Illegal color code: " + color);
        }

        Ball res = new Ball(color, true);
        res.setInitPos(x, y);
        res.spawn();
        (this.balls).add(res);
    }

    void setBalls(JSONObject config) {
        Random random = new Random();
        JSONArray ballColors = config.getJSONArray("balls");

        for (int i = 0; i < ballColors.size(); i++) {
            Ball newBall = new Ball(
                    App.getColorCode(ballColors.getString(i))
            );
            int j = random.nextInt(spawnerLocs.size());
            int[] initPos = spawnerLocs.get(j);

            newBall.setInitPos(initPos[0], initPos[1]);

            balls.add(newBall);
        }
    }

    void printBallColors() {
        for (Ball b : balls) {
            System.out.printf("Ball: %s\n", b.color);
        }
    }


    void adjustScoreAmounts() {
        for (int i = 0; i < generalScoreIncrease.length; i++) {
            scoreIncrease[i] = generalScoreIncrease[i] * increaseModifier;
            scoreDecrease[i] = generalScoreDecrease[i] * decreaseModifier;
        }
    }

    static void setScoreAmounts(JSONObject increase, JSONObject decrease) {
        generalScoreIncrease[GREY] = increase.getInt("grey");
        generalScoreIncrease[ORANGE] = increase.getInt("orange");
        generalScoreIncrease[BLUE] = increase.getInt("blue");
        generalScoreIncrease[GREEN] = increase.getInt("green");
        generalScoreIncrease[YELLOW] = increase.getInt("yellow");

        generalScoreDecrease[GREY] = decrease.getInt("grey");
        generalScoreDecrease[ORANGE] = decrease.getInt("orange");
        generalScoreDecrease[BLUE] = decrease.getInt("blue");
        generalScoreDecrease[GREEN] = decrease.getInt("green");
        generalScoreDecrease[YELLOW] = decrease.getInt("yellow");
    }

    void displayConfig() {
        System.out.printf("Layout File: %s\n", layoutFilePath);
        System.out.printf("Max time: %d\n", timeLimit);
        System.out.printf("Spawn Interval: %d\n", spawnInterval);
        System.out.printf("Score increase modifier: %f\n", increaseModifier);
        System.out.printf("Score decrease modifier: %f\n", decreaseModifier);

        System.out.println();
        System.out.println("Ball Colours:");
        for (Ball b : balls) {
            System.out.println("Ball: " + b.color);
        }

        System.out.println();
        System.out.println("Points for colors:");
        System.out.println("Grey: " + scoreIncrease[GREY]);
        System.out.println("Orange: " + scoreIncrease[ORANGE]);
        System.out.println("Blue: " + scoreIncrease[BLUE]);
        System.out.println("Green: " + scoreIncrease[GREEN]);
        System.out.println("Yellow: " + scoreIncrease[YELLOW]);

        System.out.println();
        System.out.println("Point loss for colors:");
        System.out.println("Grey: " + generalScoreDecrease[GREY]);
        System.out.println("Orange: " + generalScoreDecrease[ORANGE]);
        System.out.println("Blue: " + generalScoreDecrease[BLUE]);
        System.out.println("Green: " + generalScoreDecrease[GREEN]);
        System.out.println("Yellow: " + generalScoreDecrease[YELLOW]);
    }
}
