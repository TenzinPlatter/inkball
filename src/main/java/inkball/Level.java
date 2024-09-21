package inkball;

import processing.core.PApplet;
import processing.data.JSONArray;
import processing.data.JSONObject;

import java.io.*;
import java.util.ArrayList;

public class Level {
    static int GREY = 0;
    static int ORANGE = 1;
    static int BLUE = 2;
    static int GREEN = 3;
    static int YELLOW = 4;

    String layoutFilePath;
    int timeLimit;
    int spawnInterval;
    double increaseModifier;
    double decreaseModifier;
    ArrayList<Ball> balls;

    Cell[][] cells = new Cell[18][18];

    double[] scoreIncrease = new double[5];
    double[] scoreDecrease = new double[5];

    static double[] generalScoreIncrease = new double[5];
    static double[] generalScoreDecrease = new double[5];

    public Level(JSONObject config) {
        layoutFilePath = config.getString("layout");
        timeLimit = config.getInt("time");
        spawnInterval = config.getInt("spawn_interval");
        increaseModifier = config.getFloat("score_increase_from_hole_capture_modifier");
        decreaseModifier = config.getFloat("score_decrease_from_wrong_hole_modifier");

        balls = getBalls(config);
        adjustScoreAmounts();
        setCells();
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

                System.out.printf("Cell at (%d, %d) - %s\n", x, y, c);

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
                }

                else if (c == 'H') {
                    inHole = true;
                }

                else if (
                        (c == '0' || c == '1' || c == '2' || c == '3' || c == '4')
                ) {
                    if (inBall) {
                        cells[x - 1][y] = new Cell("tile", x - 1, y);
                        cells[x - 1][y].giveBall(c);

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

    ArrayList<Ball> getBalls(JSONObject config) {
        ArrayList<Ball> res = new ArrayList<>();
        JSONArray ballColors = config.getJSONArray("balls");

        for (int i = 0; i < ballColors.size(); i++) {
            res.add(new Ball(ballColors.getString(i)));
        }

        return res;
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
