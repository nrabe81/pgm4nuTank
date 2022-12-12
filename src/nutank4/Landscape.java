package nutank4;

// This class implements a "Landscape" that has a simple obstacle course for a Tank

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;    //for JOptionPane

public class Landscape extends Frame implements Runnable
{
	//------------ constants
	private final int WALLWIDTH = 10;
	private final int TANKWIDTH = 30;
	private final int TARGETWIDTH = TANKWIDTH;
	private final int GREENX = 150;        //x values of green and orange walls are constant for this program
	private final int ORANGEX = 375;
	private final int NUMSEGMENTS = 10;    //number of "segments" height and width is broken up into; used for placement
	private final int ANIMATIONDELAY = 25;	//Animation display rate (in milliseconds), so at least 20fps

	//------------ data
	private static boolean won;
	private static boolean lost;

	private Thread animationThread;
        volatile boolean finished = false;

	private Graphics g;
	private int landscapeID=0;

	private ArrayList<Wall> wallAL;
	private ArrayList<Tank> tankAL;
	private Tank theTank;
	private Target target;

        private boolean targetMoves = false;
	private int targetx;
	private int targety; 
        private int targetDx = 1;  //target x move direction (+1,-1)
        private int targetDy = 1;  //target y move direction (+1,-1)
        private int targetCnt= 0;  //helps moves target, slower than tank
        private Direction targOrientation = Direction.SE;
   
	private int greeny;   //(GREENX is a constant)
	private int orangey;  //(ORANGEX is a constant)

	private Insets insets;
   
        private Random rand = new Random();

	//-----------  constructor(s)
	// Default constructor - generates a random position for the green opening, the orange opening,
	// and the target.
	public Landscape()
	{  
            // generate random numbers for the landscapeID
            int greenYOffset  = rand.nextInt(10);    	//integer from 1-10
            int orangeYOffset = rand.nextInt(10);	   //integer from 1-10
            int targetXOffset = rand.nextInt(10);		//integer from 1-10
            int targetYOffset = rand.nextInt(10);		//integer from 1-10

            // build an ID number that will identify this landscape
            landscapeID = greenYOffset*1000 + orangeYOffset*100 + targetXOffset*10 + targetYOffset;

            // call another method to do the rest of the initialization (other constructors call it, too)
            initialize("", landscapeID);

            System.gc(); //garbage collection before we start

	}

	// Parameterized constructor which receives an int that is the landscapeID
	public Landscape(int theID)
	{
            landscapeID = theID;
      
            // theID can only be a 4-digit number
            if (theID < 0 || theID > 9999)
            {
                JOptionPane.showMessageDialog(null, "Landscape ID must be between 0000 and 9999", "Illegal LandscapeID: " + theID, JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }

            // call another method to do the rest of the initialization (other constructors call it, too)
            initialize("", theID);
	}

	//-----------  methods(s)

        // targetMovement - turns Target movement on/off
        public void targetMovement(boolean b)
        {
           targetMoves = b;
        }

        // targetHit -- true/false
        public boolean targetHit( )
        {
           return theTank.getBounds().intersects( target );
        }

	// getOrangeOpening - returns the opening in the orange wall as a Point (the top/left point of lower section)
	public Point getOrangeOpening()
	{
            return new Point(ORANGEX, orangey);
	}

	// getGreenOpening - returns the opening in the green wall as a Point (the top/left point of lower section)
	public Point getGreenOpening()
	{
            return new Point(GREENX, greeny);
	}

	// getTargetLocation - returns a Point which is the location of the target (actually the upper/left point of its bounding rectangle)
	public Point getTargetLocation()
	{
            return new Point(targetx, targety);
	}

        //compute pixel distance from Tank to Target         
        public int distanceToTarget()
        {
             int dx = theTank.getPositionX() - targetx;
             int dy = theTank.getPositionY() - targety;
             return (int)Math.sqrt( dx * dx + dy * dy );
        }
   
        //give direction to Target
        public Direction directionToTarget()
        {
            //         ydir =   -1    |    0    |   +1
            //              -------------------------------
            //    xdir = -1 |   NW    |   LEFT  |   SW
            //    xdir =  0 |   UP    |  RIGHT  |   DOWN
            //    xdir = +1 |   NE    |  RIGHT  |   SE
            //
            Direction[][] dir = {  {Direction.NW,    Direction.LEFT,   Direction.SW   },
                                   {Direction.UP,    Direction.RIGHT,  Direction.DOWN },
                                   {Direction.NE,    Direction.RIGHT,  Direction.SE   }  };

            int xdir = (int)Math.signum( targetx - theTank.getPositionX() ); //-1,0,1
            int ydir = (int)Math.signum( targety - theTank.getPositionY() );
            return dir[xdir+1][ydir+1]; //-1,0,1 --> 0,1,2
        }

	// addTank - adds the Tank that is received to this instance of Landscape
	public void addTank(Tank t)
	{
            theTank = t;
            theTank.setPosition(WALLWIDTH+40, 
            getInsets().top + WALLWIDTH+20);  //upper left corner; give some space
	}
   
        // getOrientation NE, SE, SW, NW
        public Direction getTargetOrientation() 
        {
           return targOrientation;
        }


	// "Helper" method that is only used by the class and not called from outside (so its private)
	// This will initialize values for instances of the class
	private void initialize(String name, int landscapeID)
	{
            // Display the frame
            setTitle("CSC110AB    " + name + "    Landscape #" + landscapeID);
            setSize(800, 600);
            center();
            setResizable(false);
            setVisible(true);

            // the landscapeID holds the various offsets; break it up into 4 parts
            int greenYOffset  = landscapeID/1000;		// 1st digit will define the y coordinate of the green wall
            int theRest       = landscapeID%1000;
            int orangeYOffset = theRest/100;				// 2nd digit will define the y coordinate of the orange wall
            theRest           = theRest%100;
            int targetXOffset = theRest/10;					// 3rd digit will define the x coordinate of the target
            theRest           = theRest%10;
            int targetYOffset = theRest;							// 4th digit will define the y coordinate of the target

            // use those values to determine the greeny and the orangey
            insets = getInsets();
            int xrange = getWidth()  - (ORANGEX + insets().left + insets().right + WALLWIDTH + WALLWIDTH) - 6*TANKWIDTH;
            int yrange = getHeight() - (insets().top + insets().bottom + WALLWIDTH + WALLWIDTH) - 6*TANKWIDTH;
            int xsegment = xrange/NUMSEGMENTS;
            int ysegment = yrange/NUMSEGMENTS;
            greeny  = insets.top + WALLWIDTH + 4*TANKWIDTH + (greenYOffset*ysegment);
            orangey = insets.top + WALLWIDTH + 4*TANKWIDTH + (orangeYOffset*ysegment);

            // use those values to determine the targetx and targety
            targetx = ORANGEX + 2*TANKWIDTH + (targetXOffset*xsegment);
            targety = insets.top + WALLWIDTH + 4*TANKWIDTH + (targetYOffset*ysegment);
            targetDx = rand.nextInt(2)*2 -1; //-1 or +1
            targetDy = rand.nextInt(2)*2 -1; //-1 or +1

            // add a WindowListener
            addWindowListener(new WindowAdapter(){  public void windowClosing(WindowEvent e){ 
                                                        System.exit(0); 
                                                    }
                                                }
                             );

            // Create and start animation thread
            animationThread = new Thread(this);
            animationThread.start();

            // get the Graphics object that will be used to write to this Frame;
            g = getGraphics();

            wallAL = new ArrayList<Wall>();
            tankAL = new ArrayList<Tank>();

            // create and store walls around the edge
            insets = getInsets();     //get the "insets," or non-frame borders used by title bar or borders
            wallAL.add(new Wall(insets.left, insets.top, 
                        getWidth() - (insets.left + insets.right), WALLWIDTH, Color.RED, true));//top
            wallAL.add(new Wall(insets.left, getHeight() - (insets.bottom+WALLWIDTH), 
                        getWidth() - (insets.left + insets.right), WALLWIDTH, Color.RED, true));//bottom
            wallAL.add(new Wall(insets.left, insets.top, 
                        WALLWIDTH, getHeight() - (insets.top + insets.bottom), Color.RED, true));//left
            wallAL.add(new Wall(getWidth() - (WALLWIDTH+insets.right), insets.top, 
                        WALLWIDTH, getHeight() - (insets.top + insets.bottom), Color.RED, true));//right

            // create and store interior walls

            wallAL.add(new Wall(GREENX, insets.top+WALLWIDTH,WALLWIDTH,greeny-(insets.top+WALLWIDTH+TANKWIDTH),Color.GREEN, false));
            wallAL.add(new Wall(GREENX, greeny, WALLWIDTH, getHeight()-(greeny+WALLWIDTH+insets.bottom),Color.GREEN, false));
            wallAL.add(new Wall(ORANGEX, insets.top+WALLWIDTH,WALLWIDTH,orangey-(insets.top+WALLWIDTH+TANKWIDTH),Color.ORANGE, false));
            wallAL.add(new Wall(ORANGEX, orangey, WALLWIDTH, getHeight()-(orangey+WALLWIDTH+insets.bottom),Color.ORANGE, false));

            // create and store the target
            target = new Target(targetx, targety, TARGETWIDTH, TARGETWIDTH);

            //booleans won and lost will keep track of whether the game is over (and stop drawing)
            won = false;
            lost = false;

            // Anonymous inner class window listener to terminate the program.
            this.addWindowListener(new WindowAdapter(){	public void windowClosing(WindowEvent e){
                                                            System.exit(0);
                                                        }
                                                    }
                                    );
	}//end initialize()
   
   public void stopMe() {
         finished = true; //stop Runnable thread
   }

	// run will actually run this Frame
	public void run()
	{
            //Loop, sleep, and update sprite positions once each ANIMATIONDELAY milliseconds
            long time = System.currentTimeMillis();
            while (!finished) //infinite loop until .stopMe() called
            {
                //Move the Target
                if(targetMoves)      
                   if( ++targetCnt % 2 == 0) //slower than Tank
                   {      
                       targetx = target.x += targetDx;
                       targety = target.y += targetDy;
                       //Bounce Target off walls
                       if(targetx <= ORANGEX+WALLWIDTH*2 || targetx >= 800-WALLWIDTH*2-TARGETWIDTH) targetDx*=-1; //reverse
                       if(targety <= 20+WALLWIDTH*2      || targety >= 600-WALLWIDTH*2-TARGETWIDTH) targetDy*=-1;

                       //set target Orientation           
                       if      (targetDx== 1  && targetDy==-1) targOrientation = Direction.NE;
                                     else if (targetDx== 1  && targetDy== 1) targOrientation = Direction.SE;
                                     else if (targetDx==-1  && targetDy== 1) targOrientation = Direction.SW;
                                else if (targetDx==-1  && targetDy==-1) targOrientation = Direction.NW;
                   }//move Target
            
                   paint(); //draw everything
            
                    try
                    {
   			time += ANIMATIONDELAY;
                        long deltaTime = time - System.currentTimeMillis();
                        //if(deltaTime<0) System.out.println("**ANIMATE Overload: lsID="+landscapeID+"**");
   			Thread.sleep(Math.max(0,time - System.currentTimeMillis()));
                    }
                    catch (InterruptedException e)
                    {
                        System.out.println(e);
                    }//end catch
   		}//end while loop
      
	}//end run method

	// center - will set the x and y of this Frame to the center of the screen
	private void center()
	{
            Dimension ScreenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension FrameSize = this.getSize();
            int x = (ScreenSize.width - FrameSize.width)/2;
            int y = (ScreenSize.height - FrameSize.height)/2 - 65;
            this.setLocation(x, y);
	}

	//------------------------------------------------
	public void windowGainedFocus( WindowEvent    e) {  } //repaint();
	public void windowLostFocus  ( WindowEvent    e) {  } //repaint();
	public void componentResized ( ComponentEvent e) {  } //repaint();
	public void componentMoved   ( ComponentEvent e) {  } //repaint();
	public void componentShown   ( ComponentEvent e) {  } //repaint();
	public void componentHidden  ( ComponentEvent e) {  } //repaint();
	public void update           ( Graphics       g) {  } //repaint();
	public void actionPerformed  ( ActionEvent    e) {  }
	//------------------------------------------------


	// paint - repaints the whole scene.   First checks to see if there have been collisions with Walls (bad)
	//              or collisions with the target (good).    Uses "double-buffering" to eliminate flickering.
	public void paint()
	{
            //don't paint if it is over
            if (won || lost)
                    return;

            // create an image - we will "double buffer" (draw to that image first and then
            // draw the image) to eliminate flickering
            Image image = createImage(getWidth(), getHeight());
            Graphics offG = image.getGraphics();

            // fill the image with the background color
            offG.setColor(Color.WHITE);
            offG.fillRect(12, 12, getWidth()-24, getHeight()-24);

            // tell the Tank to draw itself on the image
            if (theTank != null) theTank.draw(offG);

            // tell the target to draw itself on the image
            if (target != null) 	target.draw(offG);

            // tell each wall to draw itself on the image
            if (wallAL != null)
                for (int i=0; i<wallAL.size(); i++)
                    wallAL.get(i).draw(offG);

            // copy the image to the actual Frame
            if(g!=null) g.drawImage(image, 0, 0, Color.WHITE, null);

            // see if the Tank has collided with a Wall (bad)
            for (int i=0; i<wallAL.size(); i++)     //go through all Walls
                if (!lost && theTank != null && theTank.getBounds().intersects(wallAL.get(i)))
                {
                    lost = true;
                    System.out.println("**LOST lsID="+landscapeID+"**");
                    stopMe(); //stop Runnable
                    JOptionPane.showMessageDialog(null, "Your tank has collided with a wall!!", "Game over", JOptionPane.ERROR_MESSAGE);
                    //System.exit(0);
                }

            // see if the Tank has collided with the target (good)
            if (!won && theTank != null && targetHit() )
            {
                won = true;
                System.out.println("**WON lsID="+landscapeID+"**");
                stopMe(); //stop Runnable
                //JOptionPane.showMessageDialog(null, "Congratulations, you found the target", "Game over", JOptionPane.INFORMATION_MESSAGE);
                //System.exit(0);
                dispose();
            }

            repaint();
	}//end paint()
}

//*********************************************************************
// class Wall describes a Wall
class Wall extends java.awt.Rectangle
{
	// ----------------- data (note: x, y, height, width are inherited from Rectangle)
	private Color color;
	protected boolean isEdge;

	// ----------------- constructors
	public Wall(int theX, int theY, int theWidth, int theHeight, Color theColor, boolean theIsEdge)
	{
            super(theX, theY, theWidth, theHeight);
            this.color = theColor;
            this.isEdge = theIsEdge;
	}

	// ------------------- methods
	public void draw(Graphics g)
	{
            if (g != null)
            {
                g.setColor(color);
                g.fillRect(x, y, width, height);
            }
	}

}

//*********************************************************************
// class Target describes the target
class Target extends java.awt.Rectangle
{
   private Direction targOrientation = Direction.SE;
   
	// ----------------- constructors
	public Target(int theX, int theY, int theWidth, int theHeight)
	{
            super(theX, theY, theWidth, theHeight);
	}

	// ------------------- methods   
	// draw - tells the Graphics object to see its color and draw a circle
	public void draw(Graphics g)
	{
            if (g != null)
            {
                g.setColor(Color.MAGENTA);
                g.fillOval(x, y, width, width);
            }
	}

	// setPosition - sets the x, y coordinates of the bounding rectangle (upper/left corner)
	public void setPosition(int newX, int newY)
	{
            x = newX;
            y = newY;
	}
}


