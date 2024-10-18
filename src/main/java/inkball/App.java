package inkball;

import processing.core.PApplet;
import processing.core.PImage;
import processing.data.JSONArray;
import processing.data.JSONObject;
import processing.event.KeyEvent;
import processing.event.MouseEvent;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import java.io.*;
import java.util.*;

public class App extends PApplet {

    public static final int CELLSIZE = 32; //8;
    public static final int CELLHEIGHT = 32;

    public static final int CELLAVG = 32;
    public static final int TOPBAR = 64;
    public static int WIDTH = 576; //CELLSIZE*BOARD_WIDTH;
    public static int HEIGHT = 640; //BOARD_HEIGHT*CELLSIZE+TOPBAR;
    public static final int BOARD_WIDTH = WIDTH/CELLSIZE;
    public static final int BOARD_HEIGHT = 20;

    public static final int INITIAL_PARACHUTES = 1;

    public static final int FPS = 60;

    public String configPath;
    JSONObject config;
    static HashMap<String, PImage> sprites = new HashMap<>();
    ArrayList<Level> levels = new ArrayList<>();

    private JSONArray levelConfigs;

    Level currentLevel;
    int currentLevelIndex = 0;

    static private float score = 0;

    public App() {
        this.configPath = "config.json";
    }

    static PImage getSprite(String name) {
        return sprites.get(name);
    }

    /**
     * Initialise the setting of the window size.
     */
	@Override
    public void settings() {
        size(WIDTH, HEIGHT);
    }

    /**
     * Load all resources such as images. Initialise the elements such as the player and map elements.
     */
	@Override
    public void setup() {
        frameRate(FPS);
        config = loadJSONObject(configPath);

        loadSprites();
        loadLevels();
    }

    void loadLevels() {
        this.levelConfigs = config.getJSONArray("levels");
        JSONObject ballScoreIncrease = config.getJSONObject("score_increase_from_hole_capture");
        JSONObject ballScoreDecrease = config.getJSONObject("score_decrease_from_wrong_hole");

        Level.setScoreAmounts(ballScoreIncrease, ballScoreDecrease);

        for (int i = 0; i < levelConfigs.size(); i++) {
            JSONObject levelConfig = levelConfigs.getJSONObject(i);
            levels.add(new Level(levelConfig));
        }

        currentLevel = levels.get(currentLevelIndex);
    }

    void loadSprites() {
        sprites.put("tile", loadImageFromPath("tile.png"));
        sprites.put("entrypoint", loadImageFromPath("entrypoint.png"));

        String[] spriteTypes = { "ball", "hole", "wall" };
        for (int i = 0; i <= 4; i++) {
            for (int j = 0; j < 3; j++) {
                PImage result = loadImageFromPath(spriteTypes[j] + i + ".png");
                if (result != null) {
                    sprites.put(spriteTypes[j] + i, result);
                }
            }
        }

    }

    PImage loadImageFromPath(String filename) {
        try {
            return loadImage(
                    URLDecoder.decode(this.getClass().getResource(filename).getPath(),
                    StandardCharsets.UTF_8.name())
            );
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    static int getColorCode(String color) {
        switch (color) {
            case "grey":
                return 0;
            case "orange":
                return 1;
            case "blue":
                return 2;
            case "green":
                return 3;
            case "yellow":
                return 4;
            default:
                throw new RuntimeException("Unknown color: " + color);
        }
    }

    static void addScore(float amount) {
        App.score += amount;
    }

    /**
     * Receive key pressed signal from the keyboard.
     */
	@Override
    public void keyPressed(KeyEvent event){
        if (key == ' ') {
            this.currentLevel.togglePause();
        }

        if (key == 'r') {
            App.score -= this.currentLevel.currentScore;
            this.currentLevel = new Level(levelConfigs.getJSONObject(this.currentLevelIndex));
        }
    }

    /**
     * Receive key released signal from the keyboard.
     */
	@Override
    public void keyReleased(){
        
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (mouseButton == LEFT) {
            this.currentLevel.addLineMouse(mouseX, mouseY);
        }
    }
	
	@Override
    public void mouseDragged(MouseEvent e) {
        if (mouseButton == LEFT) {
            this.currentLevel.addCurrentLinePoint(mouseX, mouseY);
        } else if (mouseButton == RIGHT) {
            this.currentLevel.removeCurrentLinePoint(mouseX, mouseY);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        this.currentLevel.removeCurrentLine();
    }

    /**
     * Draw all elements in the game by current frame.
     */
	@Override
    public void draw() {
        if (currentLevel.levelOver()) {
            //TODO: game over message
            currentLevelIndex++;
            currentLevel = levels.get(currentLevelIndex);
        }

        currentLevel.draw(this);

        this.drawScore();
    }

    void drawScore() {
        fill(0);
        textAlign(RIGHT, BOTTOM);
        textSize(18);

        text("Score: " + (int) App.score, App.WIDTH - 10, App.TOPBAR - 26);
    }


    public static void main(String[] args) {
        PApplet.main("inkball.App");
    }

}