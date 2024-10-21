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

    private boolean gameOver = false;

    public App() {
        this.configPath = "config.json";
    }

    /**
     * Returns the sprite associated with the name passed in. E.G. :
     * wall4 -> resources/inkball/wall4.png
     * @param name File name of the image without extension
     * @return PImage object of sprite
     */
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

    /**
     * Loads all levels from config file into this.levels
     */
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

    /**
     * Load all sprites for inkball array into hashmap for getting through getSprite
     */
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

    /**
     * Loads an image using its filename
     * @param filename Filename of resource **including extension**
     * @return A PImage object of the file
     */
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

    /**
     * Returns the associated color code as seen in files
     * @param color Color to get code for
     * @return The color code
     * @throws RuntimeException When color passed in is not a valid color
     */
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
        if (key == 'r') {
            if (this.gameOver) {
                loadLevels();
                App.score = 0;
                this.gameOver = false;
            } else {
                App.score -= this.currentLevel.currentScore;
                this.currentLevel = new Level(levelConfigs.getJSONObject(this.currentLevelIndex));
            }

        }

        if (this.currentLevel.timerEmpty()) {
            return;
        }

        if (key == ' ') {
            this.currentLevel.togglePause();
        }

    }

    /**
     * Receive key released signal from the keyboard.
     */
	@Override
    public void keyReleased(){
        
    }

    /**
     * Mouse button falls
     * @param e Event context
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (this.gameOver || this.currentLevel.inEndAnim || this.currentLevel.timerEmpty()) {
            return;
        }

        if (mouseButton == LEFT) {
            this.currentLevel.addLineMouse(mouseX, mouseY);
        }
    }

    /**
     * Mouse move while button has fallen
     * @param e Event context
     */
	@Override
    public void mouseDragged(MouseEvent e) {
        if (this.gameOver || this.currentLevel.inEndAnim || this.currentLevel.timerEmpty()) {
            return;
        }

        if (mouseButton == LEFT) {
            this.currentLevel.addCurrentLinePoint(mouseX, mouseY);
        } else if (mouseButton == RIGHT) {
            this.currentLevel.removeCurrentLinePoint(mouseX, mouseY);
        }
    }

    /**
     * Mouse button rises
     * @param e Event Context
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        if (this.gameOver || this.currentLevel.inEndAnim || this.currentLevel.timerEmpty()) {
            return;
        }

        this.currentLevel.removeCurrentLine();
    }

    /**
     * Draw all elements in the game by current frame.
     */
	@Override
    public void draw() {
        if (currentLevel.levelOver() && !this.gameOver) {
            if (!this.currentLevel.inEndAnim) {
                // need to start animation
                if (!this.currentLevel.timerEmpty()) {
                    this.currentLevel.startLevelEndAnim();
                }

                // either level ended with no time or animation over
                else {
                    currentLevelIndex++;
                    try {
                        currentLevel = levels.get(currentLevelIndex);
                    } catch (IndexOutOfBoundsException e) {
                        this.gameOver = true;
                        currentLevel.togglePause();
                    }
                }
            }
        }

        if (this.currentLevel.levelLost && !this.currentLevel.paused) {
            this.currentLevel.togglePause();
        }

        currentLevel.draw(this);
        this.drawScore();

        if (this.gameOver) {
            this.drawGameOver();
        }
    }

    /**
     * Draws ended text for when game is over
     */
    void drawGameOver() {
        this.textAlign(PApplet.CENTER, PApplet.CENTER);
        this.textSize(30);
        this.text("===ENDED===", App.WIDTH/2f + 70, App.TOPBAR/2f - 15);
    }

    /**
     * Draws the current score to the window
     */
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