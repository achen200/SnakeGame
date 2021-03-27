package src;
import java.util.Random;
import java.util.Stack;

public class Items extends Mappable {
    public static Stack<Coord> invalidCoords;
    public static Random rint = new Random();
    
    public Items(int x, int y){
        this.x = x;
        this.y = y;
    }

    public static void updateInvalidCoords(Stack<SnakeSegment> snakeBodies){
        invalidCoords = new Stack<Coord>();

        for(SnakeSegment s: snakeBodies){
            invalidCoords.push(new Coord(s.x, s.y));
        }
    }

    /*
    public static void spawn(int xRange, int yRange, Stack<Items> items){
        while(true){
        Coord newLoc = new Coord(rint.nextInt(xRange), rint.nextInt(yRange));
        for(Coord c: invalidCoords){
            if (newLoc.equals(c)){
                continue;
            }
        }

        items.push(new Items(newLoc.x, newLoc.y));
        return;
        }
    }*/
}


class Apple extends Items{ 
    public static Stack<Apple> apples;
    public static boolean disableSpawn = false;

    public Apple(int x, int y){
        super(x,y);
    }

    public static void init(){
        apples = new Stack<>();
    }

    public static void spawn(int snakeLength, int xRange, int yRange){
        if(snakeLength >= xRange*yRange){
            return;
        }
        if(!disableSpawn && apples.isEmpty()){
            Coord newLoc = new Coord(rint.nextInt(xRange), rint.nextInt(yRange));
            
            while(isInvalid(newLoc)){               
                newLoc.x = rint.nextInt(xRange);
                newLoc.y = rint.nextInt(yRange);
            }
            apples.push(new Apple(newLoc.x, newLoc.y));
        }
    }
    
    private static boolean isInvalid(Coord coord){
        for(Coord c: invalidCoords){
            if(c.equals(coord))
                return true;
        }
        return false;
    }
    public static void removeFruit(){
        apples.pop();
    }
}


class Coord extends Mappable{
    public Coord(int x, int y){
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object other){
        if(other instanceof Coord && ((Coord)other).x == x && ((Coord)other).y == y){
            return true;
        }
        return false;
    }
}
