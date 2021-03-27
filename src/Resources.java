package src;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Scanner;
import java.awt.Image;
import javax.swing.ImageIcon;

public class Resources {
    public static final int DEFAULT_SCORE_LENGTH = 10;
    private static final int DEFAULT_SCORE_WIDTH = 2;
    private static final int SCORE_INDEX = 0;
    private static final int NAME_INDEX = 1;
    public static final String DELIMITER = "~!/";
    public static final String NEWLINE = "<br/>";
    private static final String PATH = "./lib/img";
    public static final int NORTH_WEST = 0;
    public static final int NORTH_EAST = 1;
    public static final int SOUTH_WEST = 2;
    public static final int SOUTH_EAST = 3;

    public static int scoreLength;

    public static File highScoreSheet;
    public static Scanner scoreScanner;
    public static String[][] highScores;
    
    public static Image[] snakeHead;
    public static Image[] snakeStraightBody;
    public static Image[] snakeBentBody;
    public static ImageIcon mainMenuBackground;
    public static Image apple;
    public static Image gameBoard;
    public static ImageIcon pauseBackground;
    public static ImageIcon loseScreen;
    public static ImageIcon winScreen;
    public static ImageIcon hsBackground;
    public static ImageIcon plainBG;
    
    //HighScores must be in sorted order by score highest to lowest
    public static void importAllResources() {
        snakeHead = new Image[4];
        snakeStraightBody = new Image[4];
        snakeBentBody = new Image[4];
        
        try {
            highScoreSheet = new File("./bin/highScores.txt");
            scoreScanner = new Scanner(highScoreSheet);
        } catch (FileNotFoundException e) {
            System.out.println("High Score Sheet not found");
        }
        readHighScores();
        importImages();
        //Import the other images here     
                
    }

    private static void importImages(){
        snakeHead[GameEvents.DIRECTION_UP] = new ImageIcon(PATH + "/SnakeHead/Up.png").getImage();
        snakeHead[GameEvents.DIRECTION_DOWN] = new ImageIcon(PATH + "/SnakeHead/Down.png").getImage();
        snakeHead[GameEvents.DIRECTION_LEFT] = new ImageIcon(PATH + "/SnakeHead/Left.png").getImage();
        snakeHead[GameEvents.DIRECTION_RIGHT] = new ImageIcon(PATH + "/SnakeHead/Right.png").getImage();

        snakeStraightBody[GameEvents.DIRECTION_UP] = new ImageIcon(PATH + "/SnakeStraightBody/Vertical.png").getImage();
        snakeStraightBody[GameEvents.DIRECTION_DOWN] = new ImageIcon(PATH + "/SnakeStraightBody/Vertical.png").getImage();
        snakeStraightBody[GameEvents.DIRECTION_LEFT] = new ImageIcon(PATH + "/SnakeStraightBody/Horizontal.png").getImage();
        snakeStraightBody[GameEvents.DIRECTION_RIGHT] = new ImageIcon(PATH + "/SnakeStraightBody/Horizontal.png").getImage();

        snakeBentBody[NORTH_WEST] = new ImageIcon(PATH + "/SnakeBent/NorthWest.png").getImage();
        snakeBentBody[NORTH_EAST] = new ImageIcon(PATH + "/SnakeBent/NorthEast.png").getImage();
        snakeBentBody[SOUTH_EAST] = new ImageIcon(PATH + "/SnakeBent/SouthEast.png").getImage();
        snakeBentBody[SOUTH_WEST] = new ImageIcon(PATH + "/SnakeBent/SouthWest.png").getImage();
        
        apple = new ImageIcon(PATH + "/apple.png").getImage();
        gameBoard = new ImageIcon(PATH + "/grid.png").getImage();
        loseScreen = new ImageIcon(PATH + "/loseScreen.png");
        winScreen = new ImageIcon(PATH + "/win.png"); 
        pauseBackground = new ImageIcon(PATH + "/paused.png");
        mainMenuBackground = new ImageIcon(PATH + "/StartMenu.png");
        hsBackground = new ImageIcon(PATH + "/hs.png"); 
        plainBG = new ImageIcon(PATH + "/plainBG.png");
        
    }

    public static void readHighScores(){
        scoreLength = 0;
        highScores = new String[DEFAULT_SCORE_LENGTH][DEFAULT_SCORE_WIDTH];
        
        while(scoreScanner.hasNextLine()){
            highScores[scoreLength] = scoreScanner.nextLine().split(DELIMITER);    
            scoreLength++;
        }
    }

    public static void printArray(){
        System.out.println(Arrays.deepToString(highScores));
    }

    /**
     * Assume that the given score is always greater than
     * the lowest top 10 scores
     * 
     * @param score
     * @param name
     */
    public static void addHighScore(int score, String name){
        if (scoreLength > DEFAULT_SCORE_LENGTH){
            throw new ArrayIndexOutOfBoundsException("Cannot add high score error");
        }

        if (scoreLength == 0){
            highScores[scoreLength] = new String[]{Integer.toString(score), name};
            scoreLength++;
            return;
        }

        for(int i = scoreLength - 1; i >= 0; i--){
            if(highScores[i] == null){
                continue;
            }

            if(score <= Integer.parseInt(highScores[i][SCORE_INDEX])){           
                shiftDown(i+1, scoreLength);
                highScores[i+1] = new String[]{Integer.toString(score), name};
                
                if(scoreLength < DEFAULT_SCORE_LENGTH) {scoreLength++;}
                break;
            }
            else if (i == 0){
                shiftDown(i, scoreLength);
                highScores[i] = new String[]{Integer.toString(score), name};
                if(scoreLength < DEFAULT_SCORE_LENGTH) {scoreLength++;}
                break;
            }
        }          
    }

    /**
     * Helper method to shift all elements of highScores 
     * at the specified index down 1
     * @param index
     */
    private static void shiftDown(int index, int scoreLength){
        for(int i = scoreLength - 1; i >= index; i--){
            if(i + 1 == DEFAULT_SCORE_LENGTH){
                continue;
            }
            highScores[i + 1] = highScores[i];
        }
    }

    /**
     * Writes top 10 high scores to file
     */

    public static void writeHighScores(){
        try {
            PrintWriter scorePrinter = new PrintWriter(highScoreSheet);
            for(int i = 0; i < scoreLength; i++){
                scorePrinter.println(highScores[i][SCORE_INDEX] + DELIMITER + highScores[i][NAME_INDEX]);
            }
            scorePrinter.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }        
    }

    public static String scoresToString(){
        StringBuffer toReturn = new StringBuffer();
        for(int i = 0; i < scoreLength; i++){
            toReturn.append((i+1 + ".  ")+ highScores[i][SCORE_INDEX] + " - " + highScores[i][NAME_INDEX] + NEWLINE);
        }

        return toReturn.toString();
    }

    public static int lowestScore(){
        if(scoreLength == 0){ return 0; }
        return Integer.parseInt(highScores[scoreLength - 1][SCORE_INDEX]);
    }

    public static boolean validName(String name){
        if(name.contains(NEWLINE) || name.contains(DELIMITER) || 
            name.contains("<html>") || name.contains("</html>") ||
            name.contains("<html/>") || name.contains("<br>") || name.contains("</br>")){
                return false;
        }
        return true;
    }
}
