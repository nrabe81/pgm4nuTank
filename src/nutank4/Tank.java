package nutank4;

// This class describes a drawable "Tank"

import java.awt.*;
import java.util.*;
import javax.swing.*;    //for JOptionPane

public class Tank extends java.awt.Polygon    //subclass of Polygon so we can use its intersects method
{
    // ---------------- constants
    private final int MAXSHELLS = 4;
    private final int TANKWIDTH = 30;
    private final int MAXSPEED  = 30;     // milliseconds;
    private final int MINSPEED  = 1;
    private final boolean ENABLE_DELAY = true;

    // ---------------- data
    private int x;                  //Polygon does not have x,y
    private int y;
    private int width  = TANKWIDTH; //Polygon does not have width
    private int height = TANKWIDTH; //Polygon does not have height
    private Color color;
    private int delay;
    private boolean reverseGear;
    private Graphics g;
    private Direction orientation;
    private Direction lastOrientation;
    private int numShells;
    private boolean loaded;
    private static double[] SINs = new double[8]; //faster code if created in constructor
    private static double[] COSs = new double[8];
    private static final int[] X_PTS = {-16,   3,   3,  14,  14,   3,   3, -16};  //tank polygon geometry
    private static final int[] Y_PTS = { 15,  15,   4,   4,  -4,  -4, -14, -14};
    private boolean debug = false; //turns on some println's
    private boolean drawBoundingBox = false; //debug draw box around tank
    private int debugCnt = 0; //for debugging

    // -------------- constructors
    //default constructor - sets it to Black and VERY slow
    public Tank()
    {
        this(Color.BLACK, 25);      //call the other constructor passing in BLACK and 1
    }

    //"parameterized constructor" - receives a color and a speed
    public Tank(Color theColor, int theSpeed)
    {
        super(X_PTS, Y_PTS, X_PTS.length); //create Tank Polygon

        //if the speed is legal, set it (actually the delay value to the timer).
        //If illegal, tell user and set it so something slow
        if (theSpeed < MINSPEED || theSpeed > MAXSPEED)
        {
                JOptionPane.showMessageDialog(null, "Tank's speed must be between " + MINSPEED + " and " + MAXSPEED, "Illegal speed for Tank", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
        }
        else
                delay = MAXSPEED-theSpeed+MINSPEED;   //the faster the speed, the smaller the delay

        //initialize various data
        color = theColor;
        numShells = MAXSHELLS;
        loaded = false;
        reverseGear = false;
        width = height = TANKWIDTH;       //Tank's bounding rectangle will be TANKWIDTH x TANKWIDTH
        orientation = Direction.UP;

        //initialize static SIN's and COS's for computational speed
        for(int i=0; i<8; i++) {
            double rad =  -i*Math.PI/4;
            SINs[i] = Math.sin( rad );
            COSs[i] = Math.cos( rad );
        }
    }//end Tank() constructor


    //X,Y movements (+1,0,-1) for each orientation
    private static int[][] movement = { 
    //RIGHT  NE      UP      NW       LEFT    SW      DOWN    SE
     {1,0}, {1,-1}, {0,-1}, {-1,-1}, {-1,0}, {-1,1}, {0,1}, {1,1} };


    // ---------------- methods
    // move - changes its position by 1 pixel (depending on the orientation)
    public void move()
    {
     x += movement[orientation.value][0] * (reverseGear ? -1 : 1);
     y += movement[orientation.value][1] * (reverseGear ? -1 : 1);

     if(ENABLE_DELAY) tankWait(delay); //go to sleep to slow down animation (based on its speed)
    }//end move()


    //wait/sleep after Tank moves
    public void tankWait(int msec) { //Sleep method - slows Tank animation
            try{ Thread.sleep(msec); }
            catch(Exception ex) {}
    } 


    // turn - "turns" by changing its orientation
    public void turn(String whichWay)
    {
            whichWay = whichWay.trim();   //trim off any leading/trailing white space

            if (whichWay.equals("left"))            orientation = Direction.values()[ (orientation.value+2)%8 ]; //left
            else if (whichWay.equals("right"))      orientation = Direction.values()[ (orientation.value+6)%8 ]; //right
            else if (whichWay.equals("half-left"))  orientation = Direction.values()[ (orientation.value+1)%8 ]; //half-left
            else if (whichWay.equals("half-right")) orientation = Direction.values()[ (orientation.value+7)%8 ]; //half-right
            else
                    System.out.println("Illegal turn: " + whichWay + " (only \"[half-]left\" and \"[half-]right\" allowed)");           
    }//end turn()


    // reverse - "engages reverse gear"
    public void reverse()
            {
                    reverseGear = true;
            }

    // forward - turns off reverse gear so it moves forward
    public void forward()
            {
                    reverseGear = false;
            }

    // setPosition - sets its x and y position to whatever is passed in
    //                       FOR LANDSCAPE CLASS, NOT TO BE USED BY STUDENTS
    public void setPosition(int newX, int newY)
    {
    x = newX;
            y = newY;
    }

    // getPositionX - returns its x coordinate
    public int getPositionX()
    {
            return x;
    }

    // getPositionY - returns its y coordinate
    public int getPositionY()
    {
            return y;
    }

    // getDimension - returns the dimension (both length and width, since its a square)
    public int getDimension()
    {
            return TANKWIDTH; 
    }

    // setColor - sets its color to whatever is passed in
    public void setColor(Color newColor)
    {
        color = newColor;
    }

    //toString - returns its representation as a String
    public String toString()
    {
        return "Tank at: (" + x + ", " + y + ")  orientation: " + orientation + "   size(each side): " + TANKWIDTH;
    }

    public Direction getTankOrientation()
    {
        return orientation;
    }

    //draw - "draws itself" using whatever Graphics object is passed in
    public void draw(Graphics g)
    {
            // OLD CODE ->	g.fillRect(x, y, TANKWIDTH-xcutout(28, TANKWIDTH);
            //	            g.fillRect(x+TANKWIDTH-xcutout, y+ycutout, xcutout, TANKWIDTH-2*ycutout);
    //
    //       -15-14-13-12-11-10 -9 -8 -7 -6 -5 -4 -3 -2 -1  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15
    //   15    +-----------------------------------------------------+  .  .  .  .  .  .  .  .  .  .  .  .
    //   14    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .  .  .  .  .  .  .  .  .  .  .  .
    //   13    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .  .  .  .  .  .  .  .  .  .  .  .
    //   12    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .  .  .  .  .  .  .  .  .  .  .  .
    //   11    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .  .  .  .  .  .  .  .  .  .  .  .
    //   10    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .  .  .  .  .  .  .  .  .  .  .  .
    //    9    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .  .  .  .  .  .  .  .  .  .  .  .
    //    8    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .  .  .  .  .  .  .  .  .  .  .  .
    //    7    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .  .  .  .  .  .  .  .  .  .  .  .
    //    6    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .  .  .  .  .  .  .  .  .  .  .  .
    //    5    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .  .  .  .  .  .  .  .  .  .  .  .
    //    4    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  +--------------------------------+  .
    //    3    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .
    //    2    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .
    //    1    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .
    //    0    |  .  .  .  .  .  .  .  .  .  .  .  .  .  o  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .
    //   -1    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .
    //   -2    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .
    //   -3    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .
    //   -4    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  +--------------------------------+  .
    //   -5    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .  .  .  .  .  .  .  .  .  .  .  .
    //   -6    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .  .  .  .  .  .  .  .  .  .  .  .
    //   -7    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .  .  .  .  .  .  .  .  .  .  .  .
    //   -8    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .  .  .  .  .  .  .  .  .  .  .  .
    //   -9    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .  .  .  .  .  .  .  .  .  .  .  .
    //  -10    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .  .  .  .  .  .  .  .  .  .  .  .
    //  -11    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .  .  .  .  .  .  .  .  .  .  .  .
    //  -12    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .  .  .  .  .  .  .  .  .  .  .  .
    //  -13    |  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  |  .  .  .  .  .  .  .  .  .  .  .  .
    //  -14    +-----------------------------------------------------+  .  .  .  .  .  .  .  .  .  .  .  .
    //  -15    {-15,15},{3,15},{3,4},{14,4},{14,-4},{3,-4},{3,-14},{-15,-14}  .  .  .  .  .  .  .  .  .  .

    //int[] xPts = {-15,   3,   3,  14,  14,   3,   3, -15};  //tank geometry
    //int[] yPts = { 15,  15,   4,   4,  -4,  -4, -14, -14};

    double sin = SINs[ orientation.value ];
    double cos = COSs[ orientation.value ];
    int half = TANKWIDTH/2; //offset from upper-left corner

    reset(); //empty Polygon

    //Rotate Tank points
    for(int i=0; i<X_PTS.length; i++) {
        int xx =  x + half + (int)( X_PTS[i] * cos - Y_PTS[i] * sin + 0.5 ); //rotate points
        int yy =  y + half + (int)( X_PTS[i] * sin + Y_PTS[i] * cos + 0.5 ); 
        addPoint(xx,yy); //add point to Polygon
    }//end for

    g.setColor(color);
    g.fillPolygon(this);

    if(drawBoundingBox){
        Rectangle b = getBounds(); //Polygon bounding rectangle
        g.drawRect( (int)b.getX(), (int)b.getY(), (int)b.getWidth(), (int)b.getHeight() );
    }

    //      if(debug)
    //          if(debugCnt<30) {
    //             if(lastOrientation != orientation) {
    //                lastOrientation = orientation;
    //                debugCnt++;
    //                System.out.println(this);
    //             }
    //          }

    }//end draw()

}


