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
        float timePassedInSeconds = (time - lastSpawnTime) * 1000 * 60;

        boolean timerPassed = timePassedInSeconds > spawnInterval;

        if (!timerPassed) {
            return;
        }

        for (Ball b : this.balls) {
            if (!b.hasSpawned()) {
                b.spawn();
                lastSpawnTime = time;
            }
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
        for (int y = 0; y < 18; y++) {
            for (int x = 0; x < 18; x++) {
                cells[x][y].draw(window);
            }
        }

        for (Ball b : balls) {
            if (b.hasSpawned()) {
                b.move();
                b.draw(window);
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

                else if (
                        (c == '0' || c == '1' || c == '2' || c == '3' || c == '4')
                ) {
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

    void giveBallInit(char c, int x, int y) {
        String color;
        switch (c) {
            case '0':
                color = "grey";
                break;
            case '1':
                color = "orange";
                break;
            case '2':
                color = "blue";
                break;
            case '3':
                color = "green";
                break;
            case '4':
                color = "yellow";
                break;
            default:
                throw new RuntimeException("Need to pass giveBallInit a no. 0-4, not: " + c);
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
            Ball newBall = new Ball(ballColors.getString(i));
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
