package src;
import java.util.Stack;

public class Snake {
    //Note: Head is a part of the snakeBody
    public SnakeSegment head;
    public SnakeSegment ghostTail;
    public Stack<SnakeSegment> snakeBody;

    public int length;
    public boolean dead;

    public int score;
    public boolean updatedScore = true;
    //Set when a map object calls setSnake()
    int mapWidth;
    int mapHeight;

    public Snake(int x, int y){
        this.dead = false;
        this.snakeBody = new Stack<SnakeSegment>();
        this.head = new SnakeSegment(x,y);
        this.ghostTail = new SnakeSegment(head);

        this.snakeBody.push(head);
        this.length = 1;
        this.score = 0;
    }

    /**
     * Sets the ghost tail to the value of the previous tail,
     * becoming the actual tail and attaching to snake if grow()
     * triggers. 
     */
    public void move(){
        //Right now, this feels bad because tail is created regardless of whether apple exists or not
        updateGhostTail();

        for(SnakeSegment s: snakeBody){
            s.move();
        }

        if(isCollision()){
            this.dead = true;
        } 
    }

    public void updateGhostTail(){
        ghostTail = new SnakeSegment(snakeBody.peek());
    }

    public void grow(){
        snakeBody.push(ghostTail);
        length++;
        score += 100;
    }

    public boolean isCollision(){
        //Checks for fruit
        if(!Apple.apples.isEmpty() && Apple.apples.peek().x == head.x && Apple.apples.peek().y == head.y) {
            Apple.removeFruit();
            grow();
            Items.updateInvalidCoords(snakeBody);
            Apple.spawn(length, mapWidth, mapHeight);
            updatedScore = false;
        }
        
        //Check for out of bounds,
        if(head.x < 0 || head.x >= mapWidth || head.y < 0 || head.y >= mapHeight){
            return true;
        }

        //Check if head same coords as body
        for(SnakeSegment s: snakeBody){
            //previous only null for head, this makes sure that 
            //the collision is between the head and another segment
            if(s.previous != null && s.x == head.x && s.y == head.y){
                return true;
            }
        }

        return false;
    }

}
class SnakeSegment extends Mappable{
    public static final int DIRECTION_NONE = -1;
    
    public static final int DIRECTION_LEFT = 0;
    public static final int DIRECTION_RIGHT = 1;
    public static final int DIRECTION_UP = 2;
    public static final int DIRECTION_DOWN = 3;

    //Coordinates of the body
    public int direction;
    public int prevDirection;
    public SnakeSegment previous;
    public SnakeSegment next;

    public void move(){
        switch(direction){
            case DIRECTION_LEFT:
                this.x--;
                break;
            case DIRECTION_RIGHT:
                this.x++;
                break;
            case DIRECTION_UP:
                this.y--;
                break;
            case DIRECTION_DOWN:
                this.y++;
                break;
        }
        
        this.prevDirection = this.direction;
        if(previous != null){ this.direction = previous.prevDirection;}   
    }

    /**
     * Default Constructor (Used for Snake Head)
     */
    public SnakeSegment(int x, int y){
        this.x = x;
        this.y = y;
        this.direction = DIRECTION_NONE;
        this.previous = null;
        this.next = null;
    }

    /**
     * Constructors used for snake tails
     * @param other
     */
    public SnakeSegment(SnakeSegment other){
        this.x = other.x;
        this.y = other.y;
        this.direction = other.direction;
        this.previous = other;
        this.next = null;
        other.next = this;
    }
}