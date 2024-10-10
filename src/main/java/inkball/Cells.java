package inkball;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Cells {
    Cell[][] cells;
    ArrayList<Vec2> spawnerLocs = new ArrayList<Vec2>();

    public Cells(int rows, int columns) {
        this.cells = new Cell[rows][columns];
    }

    void setCells(String layoutFilePath) {
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
}
