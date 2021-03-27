package src;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.border.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.UIManager;

public class GameCanvas extends JPanel implements Runnable {
    private static final long serialVersionUID = -1049958103353244632L;

    // Spaces to the left/right/above/below the game area
    private static final int borderSize = 2;

    // Attributes for the GameCanvas window
    private static final String EMPTY_STRING = "";
    private static final int DEFAULT_WIDTH = 896;
    private static final int DEFAULT_HEIGHT = 896;
    private static final int END_SCREEN_FONT_SIZE = 50;
    private static final int FORCE_UPDATE = 10;
    private static final int DEFAULT_UPDATE = 1;
    private static final int HIGH_SCORE_FONT = 20;
    private static final int SCORE_FONT = 15;
    private static final int DEFAULT_GRID_SIZE = 64;

    private JFrame window;
    public GameEvents game;
    private JPanel display;
    private JPanel gameOver; // For lost game
    private JPanel winScreen;
    private JPanel mainMenu;
    private JPanel pauseMenu;
    private JPanel endHighScore;
    private JPanel menuHighScore;
    private JPanel plainMenu;
    
    private JLabel endHSVal;
    private JLabel menuHSVal;
    private Font EndScreenFont;
    private Font hsFont;
    private Font hsTitle;
    private Font scoreFont;
    private JDialog namePrompt;
    private HintTextField jt;

    private Thread gameThread; // refers to this object
    private Object lock = this; // Used to get the gameThread to pause
    public boolean pause = false; // ^^
    private String name;
    Object[] jbuttons = new Object[2];

    Color darkGreen = new Color(0, 77, 0);
    Color lightBrown = new Color(223,190,127,255);
    
    private JLabel score;
    private JLabel endLabelWin;
    private JLabel endLabelLoss;
    private JLabel resumeLabel;
    private boolean removedHint = false;
    private int widthSpace;
    private int heightSpace;

    private MouseAdapter buttonHover = new MouseAdapter(){
        public void mouseEntered(MouseEvent evt) {
            if(evt.getSource() instanceof JButton){
                ((JButton)evt.getSource()).setBorder(BorderFactory.createLineBorder(Color.WHITE));
                ((JButton)evt.getSource()).setForeground(Color.WHITE);
                
            }

        }
        public void mouseExited(MouseEvent evt) {
            if(evt.getSource() instanceof JButton){
                ((JButton)evt.getSource()).setBorder(BorderFactory.createLineBorder(darkGreen));
                ((JButton)evt.getSource()).setForeground(darkGreen);
            }
        }
    };

    private KeyListener menuListener = new KeyListener(){
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
    private KeyListener promptKL = new KeyListener(){
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();        
            if(key == KeyEvent.VK_ENTER){
                String txt = jt.getText();
                if(Resources.validName(txt)){
                    name = txt;
                    jt.setText(EMPTY_STRING);
                    namePrompt.setVisible(false);       
                } 
            }
        }
        @Override
        public void keyTyped(KeyEvent e) {}
        @Override
        public void keyReleased(KeyEvent e) {}
    };

    private ComponentAdapter focusAdapter = new ComponentAdapter(){
        @Override
        public void componentShown(ComponentEvent e){
            JComponent src = (JComponent)e.getSource();
            src.requestFocusInWindow();
        }
    };

    private ActionListener prompt = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            String txt = jt.getText();
            if(Resources.validName(txt)){
                name = txt;
                jt.setText(EMPTY_STRING);
                namePrompt.setVisible(false);
            }
        }
    };

    private ActionListener menuAL = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
                case "Start":
                    changeDisplay("Game Panel");
                    window.revalidate();
                    window.repaint();
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
                    window.revalidate();
                    window.repaint();
                    break;
                case "Main Menu":
                    changeDisplay("Main Menu");
                    window.revalidate();
                    window.repaint();
                    break;
            }
        }
    };

    private ActionListener gameOverAL = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            switch (e.getActionCommand()) {
                case "Exit":
                    System.exit(0);
                    break;
                case "Restart":
                    changeDisplay("Game Panel");
                    game.init();
                    updateScore(FORCE_UPDATE);
                    window.revalidate();
                    window.repaint();
                    resume();
                    break;
                case "High Scores":
                    changeDisplay("Game Over Scores");
                    window.revalidate();
                    window.repaint();
            }
        }
    };

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
    /**
     * Default Constructor creates GameCanvas of size 800x800, and initializes
     * window
     */
    public GameCanvas() {
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * Creates custom sized GameCanvas window
     * 
     * @param width
     * @param height
     */
    public GameCanvas(int width, int height) {
        initFonts();
        Resources.importAllResources();
        initJDialog();

        UIManager.put("OptionPane.background", lightBrown);
        UIManager.put("Panel.background", lightBrown);
        UIManager.put("Panel.foreground", darkGreen);
        UIManager.put("OptionPane.messageForeground", darkGreen);
        UIManager.put("OptionPane.messageFont",  new FontUIResource(scoreFont));
        UIManager.put("Button.foreground", darkGreen);


        window = new JFrame("Snake Game");
        window.setLayout(new BoxLayout(window.getContentPane(), BoxLayout.Y_AXIS));

        createDialogButtons();

        this.setSize(width, height);
        this.setPreferredSize(new Dimension(width, height));
        this.setVisible(true);
        this.setFocusable(true);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        display = new JPanel(new CardLayout());
        display.setSize(width, height);
        display.setPreferredSize(new Dimension(width, height));
        display.setVisible(true);
        display.setFocusable(true);
    
        initGameOverScreen(width, height);
        initMainMenu(width, height);
        initPauseMenu(width, height);
        initGameWon(width, height);
        initHighScorePanels(width, height);
        initPlainMenu(width, height);
        setKeyListeners();
        
        display.add(mainMenu, "Main Menu");
        display.add(pauseMenu, "Pause Menu");
        display.add(gameOver, "Game Lost");
        display.add(this, "Game Panel");
        display.add(winScreen, "Game Won");
        display.add(endHighScore, "Game Over Scores");
        display.add(menuHighScore, "Menu Scores");
        display.add(plainMenu, "Plain Menu");

        mainMenu.addComponentListener(focusAdapter);
        pauseMenu.addComponentListener(focusAdapter);
        gameOver.addComponentListener(focusAdapter);
        this.addComponentListener(focusAdapter);
        
        resumeLabel = new JLabel("Move to resume... ");
        resumeLabel.setForeground(Color.WHITE);
        resumeLabel.setFont(hsTitle);
        resumeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        window.add(display);
        window.pack();
        window.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        window.setVisible(true);
        window.setFocusable(true);
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        center(window);

        game = new GameEvents();
        
        game.init();
        gameThread = new Thread(this);
        this.add(Box.createRigidArea(new Dimension(5,30)));
        score = new JLabel("Score: " + 0);
        score.setAlignmentX(Component.CENTER_ALIGNMENT);
        score.setForeground(darkGreen);
        score.setFont(hsTitle);
        this.add(Box.createRigidArea(new Dimension(5, 10)));

        addKeyListener(game); 
        add(score);
        changeDisplay("Main Menu");

        // To determine size of each grid unit on the map
        widthSpace = DEFAULT_GRID_SIZE;
        heightSpace = DEFAULT_GRID_SIZE;
    }

    public void changeDisplay(String jpanelName){
        CardLayout c = (CardLayout)(display.getLayout());
        c.show(display, jpanelName);
    }

    private void createDialogButtons(){
        JButton ok = new JButton("YES");
        JButton cancel = new JButton("NO");
        jbuttons[0] = ok;
        jbuttons[1] = cancel;
    }

    private void initPlainMenu(int width, int height){
        plainMenu = new JPanel();
        plainMenu.setPreferredSize(new Dimension(width, height));
        plainMenu.add(new JLabel(Resources.plainBG));
        plainMenu.setVisible(true);
    }
    private void initJDialog(){
        namePrompt = new JDialog();
        JPanel jp = new JPanel();
        jp.setBackground(lightBrown);
        namePrompt.setModal(true);

        Border line = BorderFactory.createLineBorder(Color.DARK_GRAY);
        Border empty = new EmptyBorder(0, 20, 0, 0);
        CompoundBorder border = new CompoundBorder(line, empty);

        JLabel enterName = new JLabel("New High Score");
        enterName.setForeground(darkGreen);
        enterName.setFont(hsTitle);
        enterName.setAlignmentX(Component.CENTER_ALIGNMENT);

        jt = new HintTextField("Enter name");
        jt.addKeyListener(promptKL);
        jt.setMargin(new Insets(0,20,0,0));
        jt.setAlignmentX(Component.CENTER_ALIGNMENT);
        jt.setOpaque(false);
        jt.setForeground(darkGreen);
        jt.setFont(hsFont);
        jt.setMaximumSize(new Dimension(280, 50));
        jt.setBorder(border);

        JButton b = new JButton("Ok");
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMinimumSize(new Dimension(280, 50));
        addFlair(b);
        b.addActionListener(prompt);

        namePrompt.add(jp);
        jp.setLayout(new BoxLayout(jp, BoxLayout.Y_AXIS));

        jp.add(enterName);
        jp.add(Box.createRigidArea(new Dimension(5, 10)));
        jp.add(jt);
        jp.add(Box.createRigidArea(new Dimension(5, 10)));
        jp.add(b);

        namePrompt.setPreferredSize(new Dimension(320,220));
        namePrompt.setSize(new Dimension(320,220));
        namePrompt.getRootPane().setBorder( BorderFactory.createLineBorder(darkGreen, 4) );
        namePrompt.setUndecorated(true);
        namePrompt.setVisible(false);
        namePrompt.setFocusable(true);
        
        center(namePrompt);
        namePrompt.pack();
    }

    private void setNewHighScore(String mode){
        if(game.snake.score > 100 && game.snake.score > Resources.lowestScore()){
            changeDisplay("Plain Menu");
            namePrompt.setVisible(true);
            
            
            if(name == null || name.equals(EMPTY_STRING)){name = "Anonymous";}

            Resources.addHighScore(game.snake.score, name);
            Resources.writeHighScores();
            updateHighScores();
            name = null;
            changeDisplay(mode);
            window.revalidate();
            window.repaint();
        }
    }

    private void updateHighScores(){
        endHSVal.setText("<html>" + Resources.scoresToString() + "</html>");
        menuHSVal.setText("<html>" + Resources.scoresToString() + "</html>");

    }

    private void initFonts(){
        EndScreenFont = new Font(Font.SERIF, Font.PLAIN, END_SCREEN_FONT_SIZE);
        hsFont = new Font(Font.SERIF, Font.PLAIN, HIGH_SCORE_FONT + 2);
        hsTitle = new Font(Font.SERIF, Font.BOLD, HIGH_SCORE_FONT + 5);
        scoreFont = new Font(Font.SERIF, Font.BOLD, SCORE_FONT);
    }

    private void initHighScorePanels(int width, int height){
        JLabel bg = new JLabel(Resources.hsBackground);
        bg.setAlignmentX(Component.CENTER_ALIGNMENT);
        bg.setPreferredSize(new Dimension(width, height));
        bg.setLayout(new BoxLayout(bg, BoxLayout.Y_AXIS));

        JLabel bgMenu = new JLabel(Resources.hsBackground);
        bgMenu.setAlignmentX(Component.CENTER_ALIGNMENT);
        bgMenu.setPreferredSize(new Dimension(width, height));
        bgMenu.setLayout(new BoxLayout(bgMenu, BoxLayout.Y_AXIS));

        endHighScore = new JPanel();
        endHighScore.setLayout(new BoxLayout(endHighScore, BoxLayout.Y_AXIS));

        menuHighScore = new JPanel();
        menuHighScore.setLayout(new BoxLayout(menuHighScore, BoxLayout.Y_AXIS));

        endHSVal = new JLabel("<html>" + Resources.scoresToString() + "</html>", SwingConstants.CENTER);
        endHSVal.setFont(hsFont);     
        endHSVal.setAlignmentX(Component.CENTER_ALIGNMENT);
        endHSVal.setForeground(darkGreen);

        menuHSVal = new JLabel("<html>" + Resources.scoresToString() + "</html>", SwingConstants.CENTER);
        menuHSVal.setFont(hsFont);
        menuHSVal.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuHSVal.setForeground(darkGreen);
        
        JButton back = new JButton("Back");
        back.setFont(scoreFont);
        back.addActionListener(menuAL);
        back.setActionCommand("Main Menu");
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        addFlair(back);
        
        JButton playAgain = new JButton("Play Again");
        playAgain.addActionListener(gameOverAL);
        playAgain.setActionCommand("Restart");
        playAgain.setAlignmentX(Component.CENTER_ALIGNMENT);
        playAgain.setFont(scoreFont);
        addFlair(playAgain);

        JButton exit = new JButton("Quit");
        exit.addActionListener(gameOverAL);
        exit.setActionCommand("Exit");
        exit.setAlignmentX(Component.CENTER_ALIGNMENT);
        exit.setFont(scoreFont);
        addFlair(exit);

        endHighScore.add(bg);
        bg.add(Box.createRigidArea(new Dimension(5, 400)));
        bg.add(endHSVal);
        bg.add(Box.createRigidArea(new Dimension(5,10)));
        bg.add(playAgain);
        bg.add(Box.createRigidArea(new Dimension(5,10)));
        bg.add(exit);
        
        menuHighScore.add(bgMenu);
        bgMenu.add(Box.createRigidArea(new Dimension(5,400)));
        bgMenu.add(menuHSVal);
        bgMenu.add(Box.createRigidArea(new Dimension(5,10)));
        bgMenu.add(back);
        
        menuHighScore.setSize(width, height);
        menuHighScore.setPreferredSize(new Dimension(width, height));
        menuHighScore.setVisible(true);
        menuHighScore.setFocusable(true);

        endHighScore.setSize(width, height);
        endHighScore.setPreferredSize(new Dimension(width, height));
        endHighScore.setVisible(true);
        endHighScore.setFocusable(true);
    }

    /**
     * Constructs the Game Over (You Lose) JPanel
     * 
     * @param width
     * @param height
     */
    private void initGameOverScreen(int width, int height) {
        JLabel bg = new JLabel(Resources.loseScreen);
        bg.setAlignmentX(Component.CENTER_ALIGNMENT);
        bg.setPreferredSize(new Dimension(width, height));
        bg.setLayout(new BoxLayout(bg, BoxLayout.Y_AXIS));

        endLabelLoss = new JLabel("Filler", SwingConstants.CENTER);
        endLabelLoss.setForeground(darkGreen);
        endLabelLoss.setFont(EndScreenFont);
        endLabelLoss.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton viewHighScores = new JButton("View High Scores");
        viewHighScores.setFont(scoreFont);
        viewHighScores.addActionListener(gameOverAL);
        viewHighScores.setActionCommand("High Scores");
        viewHighScores.setAlignmentX(Component.CENTER_ALIGNMENT);
        addFlair(viewHighScores);

        JButton quit = new JButton("Quit");
        quit.setFont(scoreFont);
        quit.addActionListener(gameOverAL);
        quit.setActionCommand("Exit");
        quit.setAlignmentX(Component.CENTER_ALIGNMENT);
        addFlair(quit);
        
        JButton restart = new JButton("Restart");
        restart.setFont(scoreFont);
        restart.addActionListener(gameOverAL);
        restart.setActionCommand("Restart");
        restart.setAlignmentX(Component.CENTER_ALIGNMENT);
        addFlair(restart);

        gameOver = new JPanel();
        gameOver.setLayout(new BoxLayout(gameOver, BoxLayout.Y_AXIS));
        gameOver.add(bg);
        
        bg.add(Box.createRigidArea(new Dimension(5,430)));
        bg.add(endLabelLoss);
        bg.add(Box.createRigidArea(new Dimension(5,10)));
        bg.add(quit);
        bg.add(Box.createRigidArea(new Dimension(5,10)));
        bg.add(restart);
        bg.add(Box.createRigidArea(new Dimension(5,10)));
        bg.add(viewHighScores);
        
        gameOver.setSize(width, height);
        gameOver.setPreferredSize(new Dimension(width, height));
        gameOver.setVisible(true);
        gameOver.setFocusable(true);
    }

    private void addFlair(JButton b){
        b.setMinimumSize(new Dimension(280, 50));
        b.setMaximumSize(new Dimension(280, 50));
        b.setForeground(darkGreen);
        b.setBorder(BorderFactory.createLineBorder(darkGreen));
        b.setContentAreaFilled(false);
        b.addMouseListener(buttonHover);
    }

    private void initGameWon(int width, int height){
        JLabel bg = new JLabel(Resources.winScreen);
        bg.setAlignmentX(Component.CENTER_ALIGNMENT);
        bg.setPreferredSize(new Dimension(width, height));
        bg.setLayout(new BoxLayout(bg, BoxLayout.Y_AXIS));

        endLabelWin = new JLabel("Score Filler", SwingConstants.CENTER);
        endLabelWin.setForeground(darkGreen);
        endLabelWin.setFont(EndScreenFont);
        endLabelWin.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton quit = new JButton("Quit");
        quit.setFont(scoreFont);
        quit.addActionListener(gameOverAL);
        quit.setActionCommand("Exit");
        quit.setAlignmentX(Component.CENTER_ALIGNMENT);
        addFlair(quit);

        JButton restart = new JButton("Play Again");
        restart.setFont(scoreFont);
        restart.addActionListener(gameOverAL);
        restart.setActionCommand("Restart");
        restart.setAlignmentX(Component.CENTER_ALIGNMENT);
        addFlair(restart);

        JButton viewHighScores = new JButton("View High Scores");
        viewHighScores.setFont(scoreFont);
        viewHighScores.addActionListener(gameOverAL);
        viewHighScores.setActionCommand("High Scores");
        viewHighScores.setAlignmentX(Component.CENTER_ALIGNMENT);
        addFlair(viewHighScores);

        winScreen = new JPanel();
        winScreen.setLayout(new BoxLayout(winScreen, BoxLayout.Y_AXIS));
        winScreen.add(bg);

        bg.add(Box.createRigidArea(new Dimension(5,430)));
        bg.add(endLabelWin);
        bg.add(Box.createRigidArea(new Dimension(5,10)));
        bg.add(restart);
        bg.add(Box.createRigidArea(new Dimension(5,10)));
        bg.add(quit); 
        bg.add(Box.createRigidArea(new Dimension(5,10)));
        bg.add(viewHighScores);


        winScreen.setSize(width, height);
        winScreen.setPreferredSize(new Dimension(width, height));
        winScreen.setVisible(true);
        winScreen.setFocusable(true);      
    }

    public void togglePauseMenu(){
        if(!pause){
            pause = true;
            game.pause();
            removedHint = false;
            add(resumeLabel);

            changeDisplay("Pause Menu");
            //pauseMenu.requestFocusInWindow();
            
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

    private void initPauseMenu(int width, int height){

        JLabel bg = new JLabel(Resources.pauseBackground);
        bg.setAlignmentX(Component.CENTER_ALIGNMENT);
        bg.setPreferredSize(new Dimension(width, height));
        bg.setLayout(new BoxLayout(bg, BoxLayout.Y_AXIS));

        pauseMenu = new JPanel();
        pauseMenu.setLayout(new BoxLayout(pauseMenu, BoxLayout.Y_AXIS));

        JButton resume = new JButton("Resume");
        resume.setFont(scoreFont);
        resume.addActionListener(menuAL);
        resume.setActionCommand("Resume");
        resume.setAlignmentX(Component.CENTER_ALIGNMENT);
        resume.setMinimumSize(new Dimension(280, 50));
        resume.setMaximumSize(new Dimension(280, 50));
        resume.setForeground(darkGreen);
        resume.setBorder(BorderFactory.createLineBorder(darkGreen));
        resume.setContentAreaFilled(false);
        resume.addMouseListener(buttonHover);

        JButton exit = new JButton("Exit");
        exit.setFont(scoreFont);
        exit.addActionListener(menuAL);
        exit.setActionCommand("Exit");
        exit.setAlignmentX(Component.CENTER_ALIGNMENT);
        exit.setMinimumSize(new Dimension(280, 50));
        exit.setMaximumSize(new Dimension(280, 50));
        exit.setForeground(darkGreen);
        exit.setBorder(BorderFactory.createLineBorder(darkGreen));
        exit.setContentAreaFilled(false);
        exit.addMouseListener(buttonHover);

        pauseMenu.add(bg);
        bg.add(Box.createRigidArea(new Dimension(5,500)));
        bg.add(resume);
        bg.add(Box.createRigidArea(new Dimension(5,10)));
        bg.add(exit);
        
        pauseMenu.setSize(width, height);
        pauseMenu.setPreferredSize(new Dimension(width, height));
        pauseMenu.setVisible(true);
        pauseMenu.setFocusable(true);
    }

    /**
     * Constructs the main menu
     * 
     * @param width
     * @param height
     */
    private void initMainMenu(int width, int height) {
        JLabel menuBackground = new JLabel(Resources.mainMenuBackground);
        menuBackground.setAlignmentX(Component.CENTER_ALIGNMENT);
        menuBackground.setPreferredSize(new Dimension(width, height));
        menuBackground.setLayout(new BoxLayout(menuBackground, BoxLayout.Y_AXIS));

        JButton start = new JButton("Start");
        start.setFont(scoreFont);
        start.addActionListener(menuAL);
        start.setActionCommand("Start");
        start.setAlignmentX(Component.CENTER_ALIGNMENT);
        start.setMinimumSize(new Dimension(280, 50));
        start.setMaximumSize(new Dimension(280, 50));
        start.setForeground(darkGreen);
        start.setBorder(BorderFactory.createLineBorder(darkGreen));
        start.setContentAreaFilled(false);
        start.addMouseListener(buttonHover);
        

        JButton highScore = new JButton("High Scores");
        highScore.setFont(scoreFont);
        highScore.addActionListener(menuAL);
        highScore.setActionCommand("High Scores");
        highScore.setAlignmentX(Component.CENTER_ALIGNMENT);
        highScore.setMinimumSize(new Dimension(280, 50));
        highScore.setMaximumSize(new Dimension(280, 50));
        highScore.setContentAreaFilled(false);
        highScore.setBorder(BorderFactory.createLineBorder(darkGreen));
        highScore.setForeground(darkGreen);
        highScore.addMouseListener(buttonHover);

        JButton quit = new JButton("Quit");
        quit.setFont(scoreFont);
        quit.addActionListener(gameOverAL);
        quit.setActionCommand("Exit");
        quit.setAlignmentX(Component.CENTER_ALIGNMENT);
        quit.setMinimumSize(new Dimension(280, 50));
        quit.setMaximumSize(new Dimension(280, 50));
        quit.setContentAreaFilled(false);
        quit.setBorder(BorderFactory.createLineBorder(darkGreen));
        quit.setForeground(darkGreen);
        quit.addMouseListener(buttonHover);

        mainMenu = new JPanel();
        mainMenu.setLayout(new BoxLayout(mainMenu, BoxLayout.Y_AXIS));

        mainMenu.add(menuBackground);
        menuBackground.add(Box.createRigidArea(new Dimension(5,450)));
        menuBackground.add(start);
        menuBackground.add(Box.createRigidArea(new Dimension(5,10)));
        menuBackground.add(highScore);
        menuBackground.add(Box.createRigidArea(new Dimension(5,10)));
        menuBackground.add(quit);
        
        mainMenu.setSize(width, height);
        mainMenu.setPreferredSize(new Dimension(width, height));
        mainMenu.setVisible(true);
        mainMenu.setFocusable(true);
    }

    /**
     * Converts grid x coord (i.e. 0 - 10) into pixel coordinates for drawing the
     * borders of each square
     * 
     * @param xVal
     * @return
     */
    private int getLineX(int xVal) {
        return widthSpace * borderSize + xVal * widthSpace;
    }

    private int getLineY(int yVal) {
        return heightSpace * borderSize + yVal * heightSpace;
    }

    private void exitLost() {
        endLabelLoss.setText("<html><div style='text-align: center;'>Score: " + game.snake.score + "</div></html>");
        changeDisplay("Game Lost");
        window.revalidate();
        window.repaint();

        setNewHighScore("Game Lost");
    }

    private void exitWon(){
        endLabelWin.setText("<html><div style='text-align: center;'>Score: " + game.snake.score + "</div></html>");
        changeDisplay("Game Won");

        window.revalidate();
        window.repaint();
        setNewHighScore("Game Won");
    }

    private boolean snakeIdle(){
        return game.pause;
    }

    public void run() {
        while (game.inGame) {
            if(game.checkWon()){
                exitWon();
                pause();
                try {
                    pauseThread();
                } catch (Exception e) {
                    System.out.println("Error in exiting the game thread after win");
                    e.printStackTrace();
                }
            }

            game.run();
            updateScore(DEFAULT_UPDATE);
            revalidate();
            repaint();
            
            if(!removedHint && !snakeIdle()){
                remove(resumeLabel);
                removedHint = true;
            }
            if (pause || game.snake.dead) {
                if(game.snake.dead){ 
                    exitLost();    
                }

                pause();
                try {
                    pauseThread();
                } catch (Exception e) {
                    System.out.println("Error in exiting the game thread");
                    e.printStackTrace();
                }
            }

            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                System.out.println("Error in sleeping thread");
                e.printStackTrace();
            }
        }
    }

    private void updateScore(int status){
        if(status == FORCE_UPDATE || !game.snake.updatedScore){
            score.setText("Score: " + game.snake.score);
            game.snake.updatedScore = true;
        }
    }

    public void startGame() {
        game.inGame = true;
        gameThread.start();
    }

    private void pause() {
        game.inGame = false;
        pause = true;
    }

    private void resume() {
        pause = false;
        game.inGame = true;
        synchronized (lock) {
            lock.notifyAll();
        }
    }

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

    public void paintGrid(Graphics g){
        for(int i = 0; i <= game.gameMap.width; i++){
            int x = getLineX(i);
            int startY = getLineY(0);
            int endY = getLineY(game.gameMap.height);

            g.drawLine(x, startY, x, endY);
        }

        for(int i = 0; i <= game.gameMap.height; i++){
            int y = getLineY(i);
            int startX = getLineX(0);
            int endX = getLineX(game.gameMap.width);
            
            g.drawLine(startX, y, endX, y);
        }
    }

    public void paintSnake(Graphics g){
        for(SnakeSegment s: game.snake.snakeBody){
            int dir = s.direction;

            if(s.previous == null){ //Snake head
                if(dir == SnakeSegment.DIRECTION_NONE){
                    dir = SnakeSegment.DIRECTION_UP;
                }       
                g.drawImage(Resources.snakeHead[dir], getLineX(s.x) + 1, getLineY(s.y) + 1, this);
            }
            else{ //Snake body
                //null problem because of ghostTail
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
        //paintGrid(g);
        paintSnake(g);
        paintFruit(g);        
    }
}