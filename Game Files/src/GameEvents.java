package src;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.Queue;


/**
 * Contains RUN method, which handles game logic for every tick of the game.
 */
public class GameEvents implements KeyListener{
    public static final int DIRECTION_NONE = -1;

    public static final int DIRECTION_LEFT = 0;
    public static final int DIRECTION_RIGHT = 1;
    public static final int DIRECTION_UP = 2;
    public static final int DIRECTION_DOWN = 3;
    //Note movement implementation is buggy for buffer size > 2, might result in weird movements
    private static final int MAX_MOVE_BUFFER_SIZE = 2;

    public int currentDirection;
    public int directionMoving;
    public boolean inGame;
    public boolean pause;
    public Map gameMap;
    public Snake snake;
    private Queue<Integer> movementBuffer;
    
    public void init() {
        currentDirection = DIRECTION_NONE;
        directionMoving = DIRECTION_NONE;
        
        gameMap = new Map(10,10);
        snake = new Snake(gameMap.width / 2, gameMap.height / 2);

        gameMap.setSnake(snake);
        Items.updateInvalidCoords(snake.snakeBody);
        
        Apple.init();
        Apple.spawn(snake.length, gameMap.width, gameMap.height);
        
        movementBuffer = new LinkedList<>();
        
    }

    public void pause(){
        pause = true;
    }

    public void resume(){
        pause = false;
    }

    public void run(){
        if(!snake.dead && !pause){
            if(!movementBuffer.isEmpty())
                currentDirection = movementBuffer.poll();
            
            snake.head.direction = currentDirection;
            directionMoving = currentDirection;
            snake.move();
        }     
    }

    public boolean checkWon(){
        if(snake.length == gameMap.width * gameMap.height){
            return true;
        }
        return false;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();        
        //For in-game keys
        if(!inGame || movementBuffer.size() > MAX_MOVE_BUFFER_SIZE){return;}
        switch(key){
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                if((movementBuffer.isEmpty() && directionMoving != DIRECTION_LEFT) || (!movementBuffer.isEmpty() && movementBuffer.peek() != DIRECTION_RIGHT && movementBuffer.peek() != DIRECTION_LEFT))
                    movementBuffer.add(DIRECTION_RIGHT);
                resume();
                break;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                if((movementBuffer.isEmpty() && directionMoving != DIRECTION_RIGHT) || (!movementBuffer.isEmpty() && movementBuffer.peek() != DIRECTION_LEFT && movementBuffer.peek() != DIRECTION_RIGHT))
                    movementBuffer.add(DIRECTION_LEFT);
                resume();
                break;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
                if((movementBuffer.isEmpty() && directionMoving != DIRECTION_DOWN) || (!movementBuffer.isEmpty() && movementBuffer.peek() != DIRECTION_UP && movementBuffer.peek() != DIRECTION_DOWN))
                    movementBuffer.add(DIRECTION_UP);
                resume();
                break;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                if((movementBuffer.isEmpty() && directionMoving != DIRECTION_UP) || (!movementBuffer.isEmpty() && movementBuffer.peek() != DIRECTION_DOWN && movementBuffer.peek() != DIRECTION_UP))
                    movementBuffer.add(DIRECTION_DOWN);
                resume();
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
    
}