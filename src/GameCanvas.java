package src;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class GameCanvas extends JPanel implements Runnable {
    private static final long serialVersionUID = -1049958103353244632L;
    private static final int borderSize = 2; // Spaces to the left/right/above/below the game area
    private static final String EMPTY_STRING = ""; 
    public static final int DEFAULT_WIDTH = 896;// Attributes for the GameCanvas window
    public static final int DEFAULT_HEIGHT = 896;
    private static final int END_SCREEN_FONT_SIZE = 50;
    private static final int FORCE_UPDATE = 10;
    private static final int DEFAULT_UPDATE = 1;
    private static final int HIGH_SCORE_FONT = 20;
    private static final int SCORE_FONT = 15;
    private static final int DEFAULT_GRID_SIZE = 64;
    private static final Color darkGreen = new Color(0, 77, 0);
    private static final Color lightBrown = new Color(223,190,127,255);   
    private static final Dimension DEFAULT_SPACER = new Dimension(5,10);
    public static Font scoreFont;
    public static Font hsFont;

    public GameEvents game;
    private HintTextField nameField;
    private JDialog namePrompt;
    private JFrame window; 
    private JPanel display;
    private JPanel gameOver; 
    private JPanel winScreen;
    private JPanel mainMenu;
    private JPanel pauseMenu;
    private JPanel endHighScore;
    private JPanel menuHighScore;
    private JPanel plainMenu;
    private JLabel endHSVal;
    private JLabel menuHSVal;
    private JLabel score;
    private JLabel endLabelWin;
    private JLabel endLabelLoss;
    private JLabel resumeLabel;
    private Font EndScreenFont;
    private Font hsTitle;

    private Thread gameThread; 
    private Object lock = this;
    private String name;

    public boolean pause = false; 
    private boolean removedHint = false;
    private int widthSpace;
    private int heightSpace;

    private KeyListener menuListener;
    private KeyListener promptKL;
    private ComponentAdapter focusAdapter;
    private ActionListener prompt;
    private ActionListener menuAL;
    private ActionListener gameOverAL;

    public GameCanvas() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public GameCanvas(int width, int height) {
        Resources.importAllResources();
        game = new GameEvents();
        game.init();
        
        initListeners();
        initFonts();
        initJDialog();
        initGameCanvas(width, height);
        initGameOverScreen(width, height);
        initMainMenu(width, height);
        initPauseMenu(width, height);
        initGameWon(width, height);
        initHighScorePanels(width, height);
        initPlainMenu();
        setKeyListeners();
        initDisplay();
        initWindow();
        changeDisplay("Main Menu");
        
        widthSpace = DEFAULT_GRID_SIZE; // To determine size of each grid unit on the map
        heightSpace = DEFAULT_GRID_SIZE;
    } 
    /*-----------------------------Updating Scores/Changing view -------------------------- */
    public void changeDisplay(String jpanelName){
        CardLayout c = (CardLayout)(display.getLayout());
        c.show(display, jpanelName);
    }
    private void updateHighScores(){
        endHSVal.setText("<html>" + Resources.scoresToString() + "</html>");
        menuHSVal.setText("<html>" + Resources.scoresToString() + "</html>");
    }
    /*----------------------- Methods Handle Initialization -------------------------------- */
    private void initGameCanvas(int width, int height){
        resumeLabel = new MenuLabel("Move to resume... ", hsTitle, Color.WHITE);
        gameThread = new Thread(this);
        score = new MenuLabel("Score: " + 0, hsTitle, darkGreen);

        this.setSize(width, height);
        this.setPreferredSize(new Dimension(width, height));
        this.setVisible(true);
        this.setFocusable(true);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.addComponentListener(focusAdapter);
        this.addKeyListener(game);   
        this.add(Box.createRigidArea(new Dimension(5,40)));
        this.add(score);
    }
    
    private void initDisplay(){
        display = new MenuPanel(new CardLayout());
        display.add(mainMenu, "Main Menu");
        display.add(pauseMenu, "Pause Menu");
        display.add(gameOver, "Game Lost");
        display.add(this, "Game Panel");
        display.add(winScreen, "Game Won");
        display.add(endHighScore, "Game Over Scores");
        display.add(menuHighScore, "Menu Scores");
        display.add(plainMenu, "Plain Menu");
    }
    
    private void initWindow(){
        window = new JFrame("Snake Game");
        window.setLayout(new BoxLayout(window.getContentPane(), BoxLayout.Y_AXIS));
        window.add(display);
        window.pack();
        window.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        window.setVisible(true);
        window.setFocusable(true);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        center(window);
    }
    private void initPlainMenu(){
        plainMenu = new MenuPanel();
        plainMenu.add(new MenuLabel(Resources.plainBG));
    }
    private void initJDialog(){
        JLabel enterName = new MenuLabel("New Personal Best", hsTitle, darkGreen);
        JButton ok = new MenuButton("Ok", prompt, "Doesn't Matter");
        JPanel promptBg = new MenuPanel();
        
        namePrompt = new JDialog();
        namePrompt.setModal(true);
        namePrompt.add(promptBg);

        nameField = new HintTextField("Enter name");
        nameField.addKeyListener(promptKL);
        
        promptBg.setBackground(lightBrown);
        promptBg.add(enterName);
        promptBg.add(Box.createRigidArea(DEFAULT_SPACER));
        promptBg.add(nameField);
        promptBg.add(Box.createRigidArea(DEFAULT_SPACER));
        promptBg.add(ok);

        namePrompt.setPreferredSize(new Dimension(320,220));
        namePrompt.setSize(new Dimension(320,220));
        namePrompt.getRootPane().setBorder( BorderFactory.createLineBorder(darkGreen, 4) );
        namePrompt.setUndecorated(true);
        namePrompt.setVisible(false);
        namePrompt.setFocusable(true);
        
        center(namePrompt);
        namePrompt.pack();
    }
    private void initFonts(){
        EndScreenFont = new Font(Font.SERIF, Font.PLAIN, END_SCREEN_FONT_SIZE);
        hsFont = new Font(Font.SERIF, Font.PLAIN, HIGH_SCORE_FONT + 2);
        hsTitle = new Font(Font.SERIF, Font.BOLD, HIGH_SCORE_FONT + 5);
        scoreFont = new Font(Font.SERIF, Font.BOLD, SCORE_FONT);
    }
    private void initListeners(){
        menuListener = new KeyListener(){
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();        
                if(key == KeyEvent.VK_ESCAPE)
                    togglePauseMenu();
            }
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {}
        };
        promptKL = new KeyListener(){
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();        
                if(key == KeyEvent.VK_ENTER){
                    String txt = nameField.getText();
                    if(Resources.validName(txt)){
                        name = txt;
                        nameField.setText(EMPTY_STRING);
                        namePrompt.setVisible(false);       
                    } 
                }
            }
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent e) {}
        };
    
        focusAdapter = new ComponentAdapter(){
            @Override
            public void componentShown(ComponentEvent e){
                JComponent src = (JComponent)e.getSource();
                src.requestFocusInWindow();
            }
        };
    
        prompt = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String txt = nameField.getText();
                if(Resources.validName(txt)){
                    name = txt;
                    nameField.setText(EMPTY_STRING);
                    namePrompt.setVisible(false);
                }
            }
        };
    
        menuAL = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                switch (e.getActionCommand()) {
                    case "Start":
                        changeDisplay("Game Panel"); 
                        window.revalidate();window.repaint();
                        startGame();
                        break;
                    case "Resume":
                        togglePauseMenu();
                        break;
                    case "Exit":
                        System.exit(0);
                        break;
                    case "High Scores":
                        changeDisplay("Menu Scores");
                        window.revalidate(); window.repaint();
                        break;
                    case "Main Menu":
                        changeDisplay("Main Menu");
                        window.revalidate(); window.repaint();
                        break;
                }
            }
        };
    
       gameOverAL = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                switch (e.getActionCommand()) {
                    case "Exit":
                        System.exit(0);
                        break;
                    case "Restart":
                        changeDisplay("Game Panel");
                        game.init(); updateScore(FORCE_UPDATE);
                        window.revalidate(); window.repaint();
                        resume();
                        break;
                    case "High Scores":
                        changeDisplay("Game Over Scores");
                        window.revalidate(); window.repaint();
                }
            }
        };
    }
    private void initHighScorePanels(int width, int height){
        JLabel bg = new MenuLabel(Resources.hsBackground);
        JLabel bgMenu = new MenuLabel(Resources.hsBackground);
        JButton back = new MenuButton("Back", menuAL, "Main Menu");
        JButton playAgain = new MenuButton("Play Again", gameOverAL, "Restart");
        JButton exit = new MenuButton("Quit", gameOverAL, "Exit");

        endHighScore = new MenuPanel();
        menuHighScore = new MenuPanel();

        endHSVal = new MenuLabel("<html>" + Resources.scoresToString() + "</html>", hsFont, darkGreen);
        menuHSVal = new MenuLabel("<html>" + Resources.scoresToString() + "</html>", hsFont, darkGreen);      

        endHighScore.add(bg);
        bg.add(Box.createRigidArea(new Dimension(5, 400)));
        bg.add(endHSVal);
        bg.add(Box.createRigidArea(DEFAULT_SPACER));
        bg.add(playAgain);
        bg.add(Box.createRigidArea(DEFAULT_SPACER));
        bg.add(exit);
        
        menuHighScore.add(bgMenu);
        bgMenu.add(Box.createRigidArea(new Dimension(5,400)));
        bgMenu.add(menuHSVal);
        bgMenu.add(Box.createRigidArea(DEFAULT_SPACER));
        bgMenu.add(back);
    }
    private void initGameOverScreen(int width, int height) {
        JLabel bg = new MenuLabel(Resources.loseScreen);
        JButton viewHighScores = new MenuButton("View High Scores", gameOverAL, "High Scores");
        JButton quit = new MenuButton("Quit", gameOverAL, "Exit");        
        JButton restart = new MenuButton("Restart", gameOverAL, "Restart");

        endLabelLoss = new MenuLabel("Filler", EndScreenFont, darkGreen);
        gameOver = new MenuPanel();
        gameOver.add(bg);
        gameOver.addComponentListener(focusAdapter);

        bg.add(Box.createRigidArea(new Dimension(5,430)));
        bg.add(endLabelLoss);
        bg.add(Box.createRigidArea(DEFAULT_SPACER));
        bg.add(quit);
        bg.add(Box.createRigidArea(DEFAULT_SPACER));
        bg.add(restart);
        bg.add(Box.createRigidArea(DEFAULT_SPACER));
        bg.add(viewHighScores);
    }
    private void initGameWon(int width, int height){
        JLabel bg = new MenuLabel(Resources.winScreen);
        JButton quit = new MenuButton("Quit", gameOverAL, "Exit");
        JButton restart = new MenuButton("Play Again", gameOverAL, "Restart");
        JButton viewHighScores = new MenuButton("View High Scores", gameOverAL, "High Scores");

        endLabelWin = new MenuLabel("Score Filler", EndScreenFont, darkGreen);

        winScreen = new MenuPanel();
        winScreen.add(bg);

        bg.add(Box.createRigidArea(new Dimension(5,430)));
        bg.add(endLabelWin);
        bg.add(Box.createRigidArea(DEFAULT_SPACER));
        bg.add(restart);
        bg.add(Box.createRigidArea(DEFAULT_SPACER));
        bg.add(quit); 
        bg.add(Box.createRigidArea(DEFAULT_SPACER));
        bg.add(viewHighScores);  
    }
    private void initPauseMenu(int width, int height){
        JLabel bg = new MenuLabel(Resources.pauseBackground);
        JButton resume = new MenuButton("Resume", menuAL, "Resume");
        JButton exit = new MenuButton("Exit", menuAL, "Exit");
        
        pauseMenu = new MenuPanel();
        pauseMenu.add(bg);
        pauseMenu.addComponentListener(focusAdapter);

        bg.add(Box.createRigidArea(new Dimension(5,500)));
        bg.add(resume);
        bg.add(Box.createRigidArea(DEFAULT_SPACER));
        bg.add(exit);
    }
    private void initMainMenu(int width, int height) {
        JLabel menuBackground = new MenuLabel(Resources.mainMenuBackground);
        JButton start = new MenuButton("Start", menuAL, "Start");
        JButton highScore = new MenuButton("High Scores", menuAL, "High Scores");
        JButton quit = new MenuButton("Quit", gameOverAL, "Exit");

        mainMenu = new MenuPanel();

        mainMenu.add(menuBackground);
        menuBackground.add(Box.createRigidArea(new Dimension(5,450)));
        menuBackground.add(start);
        menuBackground.add(Box.createRigidArea(DEFAULT_SPACER));
        menuBackground.add(highScore);
        menuBackground.add(Box.createRigidArea(DEFAULT_SPACER));
        menuBackground.add(quit);
        mainMenu.addComponentListener(focusAdapter);
    }
    public void togglePauseMenu(){
        if(!pause){
            pause = true;
            game.pause();
            removedHint = false;
            add(resumeLabel);
            changeDisplay("Pause Menu");
            window.revalidate();
            window.repaint();
        }
        else{
            changeDisplay("Game Panel");
            //this.requestFocusInWindow();
            window.revalidate();
            window.repaint();
            resume();   
        }
    }
    private void setKeyListeners(){
        addKeyListener(menuListener);
        pauseMenu.addKeyListener(menuListener);
    } 
    private void setNewHighScore(String mode){
        if(game.snake.score > 100 && game.snake.score > Resources.lowestScore()){
            changeDisplay("Plain Menu");
            namePrompt.setVisible(true);
            
            if(name == null || name.equals(EMPTY_STRING)){name = "Anonymous";}

            Resources.addHighScore(game.snake.score, name);
            Resources.writeHighScores(); updateHighScores();
            name = null; changeDisplay(mode);
            window.revalidate(); window.repaint();
        }
    } 
    private static void center(JFrame comp){
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int w = comp.getSize().width; int h = comp.getSize().height;
        int x = dim.width/ 2 - w/2; int y = dim.height/ 2 - h/2;
        comp.setLocation(x, y);
    }
    private static void center(JDialog comp){
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        int w = comp.getSize().width; int h = comp.getSize().height;
        int x = dim.width/ 2 - w/2; int y = dim.height/ 2 - h/2;
        comp.setLocation(x, y);
    }
    private int getLineX(int xVal) { return widthSpace * borderSize + xVal * widthSpace;}
    private int getLineY(int yVal) { return heightSpace * borderSize + yVal * heightSpace;}
    /*------------------------- Game Conditions --------------------------------------- */
    private boolean snakeIdle(){ return game.pause;}
    private void exitLost() {
        endLabelLoss.setText("<html><div style='text-align: center;'>Score: " 
            + game.snake.score + "</div></html>");
        changeDisplay("Game Lost");
        window.revalidate(); window.repaint();
        setNewHighScore("Game Lost");
    }
    private void exitWon(){
        endLabelWin.setText("<html><div style='text-align: center;'>Score: " 
            + game.snake.score + "</div></html>");
        changeDisplay("Game Won");
        window.revalidate(); window.repaint();
        setNewHighScore("Game Won");
    }
    public void run() {
        while (game.inGame) {
            if(game.checkWon()){
                exitWon();
                pause();
                try { pauseThread();} 
                catch (Exception e) {
                    System.out.println("Error in exiting the game thread after win");
                    e.printStackTrace();
                }
            }

            game.run();
            updateScore(DEFAULT_UPDATE);
            revalidate(); repaint();
            
            if(!removedHint && !snakeIdle()){
                remove(resumeLabel);
                removedHint = true;
            }
            if (pause || game.snake.dead) {
                if(game.snake.dead){ exitLost();}    
                pause();
                try { pauseThread();} 
                catch (Exception e) {
                    System.out.println("Error in exiting the game thread");
                    e.printStackTrace();
                }
            }
           sleep();
        }
    }
    private void sleep(){
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            System.out.println("Error in sleeping thread");
            e.printStackTrace();
        }
    }
    private void updateScore(int status){
        if(status == FORCE_UPDATE || !game.snake.updatedScore){
            score.setText("Score: " + game.snake.score);
            game.snake.updatedScore = true;
        }
    }
    public void startGame() { game.inGame = true; gameThread.start();} 
    private void pause() { game.inGame = false; pause = true;} 
    private void resume() { pause = false; game.inGame = true; synchronized (lock) {lock.notifyAll();}}
    private void pauseThread() {
        synchronized (lock) {
            if (pause) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    /*---------------------------------------Paint Logic -----------------------------------*/
    public void paintSnake(Graphics g){
        for(SnakeSegment s: game.snake.snakeBody){
            int dir = s.direction;

            if(s.previous == null){ 
                if(dir == SnakeSegment.DIRECTION_NONE){
                    dir = SnakeSegment.DIRECTION_UP;
                }       
                g.drawImage(Resources.snakeHead[dir], getLineX(s.x) + 1, getLineY(s.y) + 1, this);
            }
            else{ 
                if(s.next == null || s.next.direction == dir){
                    g.drawImage(Resources.snakeStraightBody[dir], getLineX(s.x) + 1, getLineY(s.y) + 1, this);
                }
                else{
                    int nDir = s.next.direction;
                    switch(dir){
                        case SnakeSegment.DIRECTION_UP:
                            if(nDir == SnakeSegment.DIRECTION_LEFT)
                                g.drawImage(Resources.snakeBentBody[Resources.NORTH_EAST], getLineX(s.x) + 1, getLineY(s.y) + 1, this);
                            else
                                g.drawImage(Resources.snakeBentBody[Resources.NORTH_WEST], getLineX(s.x) + 1, getLineY(s.y) + 1, this);
                            break;
                        case SnakeSegment.DIRECTION_DOWN:
                            if(nDir == SnakeSegment.DIRECTION_LEFT)
                                g.drawImage(Resources.snakeBentBody[Resources.SOUTH_EAST], getLineX(s.x) + 1, getLineY(s.y) + 1, this);
                            else
                                g.drawImage(Resources.snakeBentBody[Resources.SOUTH_WEST], getLineX(s.x) + 1, getLineY(s.y) + 1, this);
                            break;
                        case SnakeSegment.DIRECTION_LEFT:
                            if(nDir == SnakeSegment.DIRECTION_UP)
                                g.drawImage(Resources.snakeBentBody[Resources.SOUTH_WEST], getLineX(s.x) + 1, getLineY(s.y) + 1, this);
                            else
                                g.drawImage(Resources.snakeBentBody[Resources.NORTH_WEST], getLineX(s.x) + 1, getLineY(s.y) + 1, this);
                            break;
                        case SnakeSegment.DIRECTION_RIGHT:
                            if(nDir == SnakeSegment.DIRECTION_UP)
                                g.drawImage(Resources.snakeBentBody[Resources.SOUTH_EAST], getLineX(s.x) + 1, getLineY(s.y) + 1, this);
                            else
                                g.drawImage(Resources.snakeBentBody[Resources.NORTH_EAST], getLineX(s.x) + 1, getLineY(s.y) + 1, this);
                            break;
                    }

                }
            }
        }
    }
    public void paintFruit(Graphics g){
        for(Apple a: Apple.apples){
            g.drawImage(Resources.apple, getLineX(a.x), getLineY(a.y), this);
        }
    }
    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        g.drawImage(Resources.gameBoard, 0, 0, this);    
        paintSnake(g);
        paintFruit(g);        
    }
}