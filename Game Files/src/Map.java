package src;
public class Map {
    public int width;
    public int height;
    public Snake snake;

    private static int DEFAULT_WIDTH = 10;
    private static int DEFAULT_HEIGHT = 10;

    public Map(){
        this(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public Map(int width, int height){
        this.width = width;
        this.height = height;
    }

    public void setSnake(Snake snake){
        this.snake = snake;
        this.snake.mapWidth = width;
        this.snake.mapHeight = height;
    }
}
class Mappable{
    public int x;
    public int y;
}