package inkball;

import processing.core.PApplet;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.io.*;
import java.util.*;

public class Level {
    private static int GREY = 0;
    private static int ORANGE = 1;
    private static int BLUE = 2;
    private static int GREEN = 3;
    private static int YELLOW = 4;
    int timeLeft;
    private long timerLast = System.currentTimeMillis();

    private String layoutFilePath;
    private int spawnInterval;
    private long lastSpawnTime;
    private double increaseModifier;
    private double decreaseModifier;

    private float nextBallSpawnLastMeasuredTimeTill = 0;

    private boolean paused = false;
    private long pausedTimeDiff = 0;
    private boolean justUnpaused = false; private ArrayList<Ball> balls = new ArrayList<>();
    private ArrayList<int[]> spawnerLocs = new ArrayList<>();
    private ArrayList<Vec2> holeLocs = new ArrayList<>();
    private ArrayList<Line> lines = new ArrayList<>();

    private Cell[][] cells = new Cell[18][18];

    public float currentScore = 0;

    private double[] scoreIncrease = new double[5];
    private double[] scoreDecrease = new double[5];

    private static double[] generalScoreIncrease = new double[5];
    private static double[] generalScoreDecrease = new double[5];

    private Line currentLine = null;

    boolean inEndAnim = false;
    private int framesSinceLastScoreAdd = 0;
    private Vec2[] currentYellowCells = new Vec2[2];

    public Level(JSONObject config) {
        layoutFilePath = config.getString("layout");
        timeLeft = config.getInt("time");
        spawnInterval = config.getInt("spawn_interval");
        increaseModifier = config.getFloat("score_increase_from_hole_capture_modifier");
        decreaseModifier = config.getFloat("score_decrease_from_wrong_hole_modifier");

        setCells();
        setBalls(config);
        adjustScoreAmounts();
    }

    /**
     *
     * @return If timer for level is over
     */
    boolean timerEmpty() {
        return this.timeLeft <= 0;
    }

    /**
     * Start animation for level end
     */
    void startLevelEndAnim() {
        this.inEndAnim = true;

        this.cells[0][0].setYellowWall();
        this.cells[this.cells.length - 1][this.cells[0].length - 1].setYellowWall();

        this.currentYellowCells = new Vec2[] {
                new Vec2(0, 0),
                new Vec2(this.cells.length - 1, this.cells[0].length - 1)
        };
    }

    /**
     * Pauses game if unpaused and vice versa
     */
    void togglePause() {
        if (!this.paused) {
            this.pausedTimeDiff = System.currentTimeMillis();
        } else {
            this.pausedTimeDiff = System.currentTimeMillis() - this.pausedTimeDiff;
            this.justUnpaused = true;
        }

        this.paused = !this.paused;
    }

    /**
     * Method to add a line to the level with mouse press
     * @param x X pos of mouse
     * @param y Y pos of mouse
     */
    void addLineMouse(int x, int y) {
        if (currentLine != null) {
            throw new RuntimeException("Cannot add line before old current line has been removed");
        }

        currentLine = new Line(new Vec2(x, y));
        lines.add(currentLine);
    }

    /**
     * Method to add a point to the line currently being added
     * @param x
     * @param y
     */
    void addCurrentLinePoint(int x, int y) {
        if (currentLine == null) {
            // shouldn't be able to get here as it means mouseDragged was triggered after MB1 was raised
            return;
        }

        currentLine.addPoint(new Vec2(x, y));
    }

    /**
     * If the pos collides with a section of a line, tries to remove that line
     * @param x
     * @param y
     */
    void removeCurrentLinePoint(int x, int y) {
        for (Line line : lines) {
            if (line.checkForCollision(new Vec2(x, y), 5) != null) {
                lines.remove(line);
                return;
            }
        }
    }

    /**
     * Call when mouse is lifted to stop adding points to this line
     */
    void removeCurrentLine() {
        this.currentLine = null;
    }

    /**
     * Trys to spawn next ball, fails if timer has not passed or no next ball to be spawned
     * @param time
     * @return
     */
    void trySpawnNext(long time) {
        if (this.justUnpaused) {
            this.lastSpawnTime += this.pausedTimeDiff;
        }

        float timePassedInSeconds = (time - lastSpawnTime) / 1000f;

        boolean timerPassed = timePassedInSeconds >= spawnInterval;

        if (!timerPassed || this.paused) {
            return;
        }

        Random random = new Random();
        for (Ball b : this.balls) {
            if (b.hasSpawned()) {
                continue;
            }


            int j = random.nextInt(spawnerLocs.size());
            int[] initPos = spawnerLocs.get(j);

            b.setInitPos(initPos[0], initPos[1]);

            b.spawn();
            lastSpawnTime = time;
            break;
        }
    }

    boolean levelOver() {
        return this.balls.isEmpty();
    }

    /**
     * Rotates yellow cells for end of level animation
     * Only use after calling startLevelEndAnime
     */
    void rotateYellowCells() {
        if (!this.inEndAnim) {
            throw new RuntimeException("Cannot rotate yellow cells out of animation");
        }

        for (Vec2 v : this.currentYellowCells) {
            this.cells[(int) v.x][(int) v.y].setOldWall();

            if (v.x == 17) {
                if (v.y < 17) {
                    v.y++;
                } else {
                    v.x--;
                }
            }
            if (v.x == 0) {
                if (v.y > 0) {
                    v.y--;
                } else {
                    v.x++;
                }
            }
            // corners will be caught earlier by x conditions
            if (v.y == 17) {
                v.x--;
            }
            if (v.y == 0) {
                v.x++;
            }

            this.cells[(int) v.x][(int) v.y].setYellowWall();
        }
    }

    /**
     * Handles animation for end of level
     * Should be called on every frame
     */
    void handleEndAnimation() {
        if (this.framesSinceLastScoreAdd < 4) {
            this.framesSinceLastScoreAdd++;
            return;
        }

        this.framesSinceLastScoreAdd = 0;
        this.addScore(1);
        this.timeLeft--;

        this.rotateYellowCells();

        if (this.timerEmpty()) {
            this.inEndAnim = false;
        }
    }

    /**
     * Draws everything associated with this level
     * @param window Window to draw onto
     */
    void draw(PApplet window) {
        if (this.inEndAnim) {
            this.handleEndAnimation();
        } else {
            this.handleTimer(System.currentTimeMillis());
            this.trySpawnNext(System.currentTimeMillis());
            this.justUnpaused = false;
        }

        window.background(223);

        // keep this order
        drawCells(window);
        drawBalls(window);
        drawLines(window);

        drawTopBar(window);
    }

    /**
     * Draws elements in top bar
     * @param window Window to draw on
     */
    void drawTopBar(PApplet window) {
        drawText(window);
        drawNextBalls(window);
    }

    /**
     * Draws window showing next five balls
     * @param window Window to draw onto
     */
    void drawNextBalls(PApplet window) {
        window.fill(0);
        window.strokeWeight(4);

        // Rect for room for 5 balls with diameter 24 and gap of 10 between each ball, space-around
        window.rect(10, 10, 210, 37);

        int count = 0;
        int i = 0;
        while (count < 5) {
            Ball ball;
            try {
                ball = this.balls.get(i);
            } catch (IndexOutOfBoundsException e) {
                break;
            }

            if (ball.hasSpawned()) {
                i++;
                continue;
            }

            // start of rect + side padding + offset for each ball
            int xPos = 10 + 5 + count * (32 + 10);
            window.image(ball.getSprite(), xPos, 10 + 5);

            count++;
            i++;
            //TODO: moving animation
        }
    }

    /**
     * Increments timer, pauses when game is paused
     * @param time Current system time
     */
    void handleTimer(long time) {
        if (this.justUnpaused) {
            this.timerLast += this.pausedTimeDiff;
        }

        if (this.paused) {
            return;
        }

        int timeInSeconds = (int) (time - this.timerLast) / 1000;

        if (timeInSeconds > 0) {
            this.timerLast = time;
            this.timeLeft--;
        }
    }

    /**
     * Draws text for topbar
     * @param window Window to draw onto
     */
    void drawText(PApplet window) {
        window.fill(0);

        window.textAlign(PApplet.RIGHT, PApplet.BOTTOM);
        window.textSize(18);
        window.text("Time: " + this.timeLeft, App.WIDTH - 10, App.TOPBAR - 8);

        float timeTillNextSpawn;

        if (!this.paused) {
            float timePassedInSeconds = (System.currentTimeMillis() - lastSpawnTime) / 1000f;
            timeTillNextSpawn = spawnInterval - timePassedInSeconds;
            this.nextBallSpawnLastMeasuredTimeTill = timeTillNextSpawn;
        } else {
            timeTillNextSpawn = this.nextBallSpawnLastMeasuredTimeTill;
        }

        if (!this.balls.isEmpty()) {
            window.textAlign(PApplet.LEFT, PApplet.BOTTOM);
            window.text(String.format("%.1f", timeTillNextSpawn), 230, App.TOPBAR - 8);
        }

        if (this.paused && !this.timerEmpty()) {
            window.textAlign(PApplet.CENTER, PApplet.CENTER);
            window.textSize(30);
            window.text("***PAUSED***", App.WIDTH/2f + 70, App.TOPBAR/2f - 15);
        }
    }

    /**
     * Draws all lines associated with this level
     * @param window Window to draw onto
     */
    void drawLines(PApplet window) {
        for (Line line : this.lines) {
            line.draw(window);
        }
    }

    /**
     * Handles collision between lines and a given ball
     * @param ball Ball to be checked
     */
    void handleLinesCollision(Ball ball) {
        for (Line line : this.lines) {
            if (line.handleCollision(ball)) {
                this.lines.remove(line);
                return;
            }
        }
    }

    /**
     * Method to draw balls and handle all movement/sprite editing
     * @param window Window to draw onto
     */
    void drawBalls(PApplet window) {
        List<Ball> toAdd = new ArrayList<>();
        for (Iterator<Ball> it = this.balls.iterator(); it.hasNext(); ) {
            Ball b = it.next();

            if (!b.hasSpawned()) {
                continue;
            }

            boolean collided = false;

            // handles collision with walls
            for (int y = 0; y < 18; y++) {
                for (int x = 0; x < 18; x++) {
                    if (!collided) {
                        if (!cells[x][y].isWall()) {
                            continue;
                        }
                        // sets to true if there was a collision, false otherwise
                        collided = cells[x][y].handleCollision(b, this.getNeighborsArr(x, y));
                    }
                }
                if (collided) {
                    break;
                }
            }

            for (Ball ball : this.balls) {
                this.handleLinesCollision(ball);
            }

            // collision with edges of window
            if (!collided) {
                Vec2 c = b.getPosVec();
                if (c.x - Ball.radius < 0 || c.x + Ball.radius > App.WIDTH) {
                    b.bounceX();
                }

                if (c.y - Ball.radius < App.TOPBAR || c.x + Ball.radius > App.HEIGHT) {
                    b.bounceY();
                }
            }

            // handles all hole logic
            Vec2 holeLoc = this.handleHole(b);
            if (holeLoc != null) {
                Cell hole = cells[(int)holeLoc.x][(int)holeLoc.y];
                if (b.color == 0 || hole.getColorFor("hole") == 0) {
                    this.addScore((float) this.scoreIncrease[b.color]);
                }

                else if (b.color == hole.getColorFor("hole")) {
                    this.addScore((float) this.scoreIncrease[b.color]);
                }

                else {
                    toAdd.add(new Ball(b.color));
                    this.addScore(-1 * (float) this.scoreDecrease[b.color]);
                }

                it.remove();
                continue;
            }

            if (!this.paused) {
                b.move();
            }

            b.draw(window);
        }

        // add any balls that were captured and need to be respawned
        if (!toAdd.isEmpty()) {
            this.balls.addAll(toAdd);
        }
    }

    /**
     * Adds score to the App
     * Use this over manual adding or calling of App.addScore so level restarts can preserve initial score
     * @param adjustment How much the score will be increased by
     */
    void addScore(float adjustment) {
        this.currentScore += adjustment;
        App.addScore(adjustment);
    }

    /**
     * Finds closest hole to ball passed in and handles sprite shrinking for it
     * @param ball Ball to be checked
     * @return Returns either the location of the hole that captured the ball, or null
     */
    Vec2 handleHole(Ball ball) {
        Vec2 ballPos = ball.getPosVec();
        Vec2 closestHole = this.holeLocs.get(0).coordsToPos();
        float closestDistance = (float) ballPos.distanceTo(closestHole);
        boolean firstIter = true;

        for (Vec2 loc : this.holeLocs) {
            if (firstIter) {
                firstIter = false;
                continue;
            }

            Vec2 v = loc.coordsToPos().centerCoords(64);
            float dist = (float) ballPos.distanceTo(v);

            if (dist < closestDistance) {
                closestDistance = dist;
                closestHole = v;
            }
        }

        if (closestDistance > 32) {
            ball.spriteScaleFactor = 1;
            return null;
        }

        // ball will be captured when center is within 5px of holes center
        // offset is for lenience
        if (closestDistance <= Ball.radius + 3) {
            // sprite scaling shouldn't matter as ball should be removed before it has a chance
            // to be drawn
            return closestHole.centerCoords(-64).posToCoords();
        }

        Vec2 velUpdate = ballPos.to(closestHole);
        ball.dx += (float) (velUpdate.x * 0.005);
        ball.dy += (float) (velUpdate.y * 0.005);

        ball.spriteScaleFactor = closestDistance / 32;
        return null;
    }

    /**
     * Gets boolean array of whether cells above a given cell are collidable
     * @param x x coordinate of cell to check
     * @param y y coordinate of cell to check
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

    /**
     * Draws all cells associated with level
     * @param window Window to draw onto
     */
    void drawCells(PApplet window) {
        for (int y = 0; y < 18; y++) {
            for (int x = 0; x < 18; x++) {
                cells[x][y].draw(window);
            }
        }
    }

    /**
     * Sets cells from config file
     */
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
                        setBallInit(c, x, y);

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

    /**
     * Sets a ball to spawn when level starts at give position
     * @param colorCode Color code of ball
     * @param x x coordinate of cell ball should spawn on
     * @param y y coordinate of cell ball should spawn on
     */
    void setBallInit(char colorCode, int x, int y) {
        int color = colorCode - '0';
        if (color < 0 || color > 4) {
            throw new IllegalArgumentException("Illegal color code: " + color);
        }

        Ball res = new Ball(color);
        res.setInitPos(x, y);
        res.spawn();
        (this.balls).add(res);
    }

    /**
     * Sets balls to be spawned at an interval from config file
     * @param config Config options
     */
    void setBalls(JSONObject config) {
        JSONArray ballColors = config.getJSONArray("balls");

        for (int i = 0; i < ballColors.size(); i++) {
            Ball newBall = new Ball(
                    App.getColorCode(ballColors.getString(i))
            );
            balls.add(newBall);
        }
    }

    /**
     * Applies the levels score multiplier to amount of points score by each ball
     */
    void adjustScoreAmounts() {
        for (int i = 0; i < generalScoreIncrease.length; i++) {
            scoreIncrease[i] = generalScoreIncrease[i] * increaseModifier;
            scoreDecrease[i] = generalScoreDecrease[i] * decreaseModifier;
        }
    }

    /**
     * Sets general score increase from config file
     * @param increase Increases for each color
     * @param decrease Decreases for each color
     */
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
}