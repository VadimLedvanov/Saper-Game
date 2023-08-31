package com.javarush.games.minesweeper;

import com.javarush.engine.cell.Color;
import com.javarush.engine.cell.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MinesweeperGame extends Game {
    private static final int SIDE = 9;
    private GameObject[][] gameField = new GameObject[SIDE][SIDE];
    private static final String MINE = "\uD83D\uDE32";
    private int countMinesOnField;
    private static final String FLAG = "\uD83D\uDD25";
    private int countFlags;

    private boolean isGameStopped;

    private int countClosedTiles = SIDE * SIDE;
    private int score;

    @Override
    public void initialize() {
        setScreenSize(SIDE, SIDE);
        createGame();
    }

    private void createGame() {
        for (int y = 0; y < SIDE; y++) {
            for (int x = 0; x < SIDE; x++) {
                boolean isMine = getRandomNumber(10) < 1;
                if (isMine) {
                    countMinesOnField++;
                }
                gameField[y][x] = new GameObject(x, y, isMine);
                setCellColor(x, y, Color.ORANGE);
                setCellValue(x, y, "");
            }
        }

        countMineNeighbors();
        countFlags = countMinesOnField;
    }

    private List<GameObject> getNeighbors(GameObject gameObject) {
        List<GameObject> result = new ArrayList<>();
        for (int y = gameObject.y - 1; y <= gameObject.y + 1; y++) {
            for (int x = gameObject.x - 1; x <= gameObject.x + 1; x++) {
                if (y < 0 || y >= SIDE) {
                    continue;
                }
                if (x < 0 || x >= SIDE) {
                    continue;
                }
                if (gameField[y][x] == gameObject) {
                    continue;
                }
                result.add(gameField[y][x]);
            }
        }
        return result;
    }

    private void countMineNeighbors() {
        for (int width = 0; width < SIDE; width++) {
            for (int height = 0; height < SIDE; height++) {
                for (int x = 0; x < SIDE; x++) {
                    GameObject gameObject = gameField[width][height];
                    int count = 0;
                    if (!gameObject.isMine) {
                        for (GameObject neighbor : getNeighbors(gameObject)) {
                            if (neighbor.isMine) {
                                count += 1;
                            }
                        }
                    }
                    gameObject.countMineNeighbors = count;
                }
            }
        }
    }

    private void openTile(int x, int y) {
        if (gameField[y][x].isOpen)
            return;
        if (gameField[y][x].isFlag)
            return;
        if (isGameStopped)
            return;


        if (gameField[y][x].isMine) {
            setCellValueEx(x,y, Color.RED, MINE);
            gameOver();
        } else {
            setCellColor(x,y,Color.GREEN);
            gameField[y][x].isOpen = true;
            countClosedTiles -= 1;
            score += 5;
            setScore(score);
            if (gameField[y][x].countMineNeighbors == 0) {
                setCellValue(x, y, "");
                getNeighbors(gameField[y][x]).stream()
                        .filter(cell -> !cell.isOpen)
                        .forEach(cell -> openTile(cell.x, cell.y));
            } else {
                setCellNumber(x, y, gameField[y][x].countMineNeighbors);
            }
        }

        if (countClosedTiles == countMinesOnField && !gameField[y][x].isMine)
            win();

    }

    @Override
    public void onMouseLeftClick(int x, int y) {
        if (isGameStopped) {
            restart();
            return;
        }
        openTile(x, y);

    }

    private void markTile(int x, int y) {
        if (isGameStopped)
            return;
        if (countFlags != 0 && !gameField[y][x].isFlag && !gameField[y][x].isOpen)
        {
            gameField[y][x].isFlag = true;
            countFlags -= 1;
            setCellValue(x, y, FLAG);
            setCellColor(x, y, Color.YELLOW);
        } else if (gameField[y][x].isFlag) {
            gameField[y][x].isFlag = false;
            countFlags += 1;
            setCellValue(x, y, "");
            setCellColor(x, y, Color.ORANGE);
        }
    }

    @Override
    public void onMouseRightClick(int x, int y) {
        markTile(x, y);
    }

    private void gameOver() {
        isGameStopped = true;
        showMessageDialog(Color.BLACK, "Game Over", Color.WHITE, 55);
    }

    private void win() {
        isGameStopped = true;
        showMessageDialog(Color.BLACK, "YOU WIN", Color.WHITE, 55);
    }

    private void restart() {
        countClosedTiles = SIDE * SIDE;
        score = 0;
        setScore(score);
        countMinesOnField = 0;
        isGameStopped = false;
        createGame();
    }
}