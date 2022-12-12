/*       File: Program4.java	.	........
 * Programmer: Nolan Rabe   
 *    Purpose: Drive a Tank
 */
package nutank4;

import java.awt.*;
import java.util.Random;
import java.util.Scanner;

/**
 *
 * @author nrabe
 */
public class Program4
{       

    /**
     *
     * @param args
     */
    public static void main(String[ ] args)
	{
            //Constants            
            Direction UP = Direction.UP;
            Direction DOWN = Direction.DOWN;
            Direction LEFT = Direction.LEFT;
            Direction RIGHT = Direction.RIGHT;
            Direction SE = Direction.SE;
            Direction NE = Direction.NE;
            Direction NW = Direction.NW;
            Direction SW = Direction.SW;
            Color BLUE = new Color(0, 0, 255);
            Color RED = new Color(255, 0, 0);
            Color GREEN = new Color(0, 255, 0);
            Color YELLOW = new Color(255,255,150);
            Color MAROON = new Color(139, 0, 0);
            Color PURPLE = new Color(128, 0, 128);
            Color TEAL = new Color(32, 178, 170);
            
            //Variables            
            Scanner kb = new Scanner(System.in);
            Color tankColor;
            Random random = new Random();
            int rando = random.nextInt(256);
            Color randomColor = new Color(rando, 0, rando);
            
            
            //Input            
            System.out.println("Please enter your name: ");
            String name = kb.nextLine();
            
            //Name validator
            if (name.equalsIgnoreCase("Costello"))
            {
               tankColor = new Color(rando, rando, rando); 
            }
            else if(name.equalsIgnoreCase("Abbott"))
            {
               tankColor = new Color(0, rando, 0);
            }
            else
            {
               tankColor = randomColor; 
            }
            

            //create a new Tank with that color and speed; add tank to landscape
            Tank myTank = new Tank(tankColor, 27);
            Landscape ls = new Landscape();//6147);
            ls.addTank(myTank);           


            //50 pts extra credit for a moving target - uncomment to try it
            ls.targetMovement(true);            
               

            //find out some information needed for the calculations            
            Point greenPt = ls.getGreenOpening();
            Point orangePt = ls.getOrangeOpening();
            Point target = ls.getTargetLocation();
            int targetDistance = ls.distanceToTarget();
            int greenX = (int)greenPt.getX();
            int greenY = (int)greenPt.getY();
            int orangeX = (int)orangePt.getX();
            int orangeY = (int)orangePt.getY();
            int targetX = (int)target.getX();
            int targetY = (int)target.getY();
            int tankWidth = myTank.getDimension();
            
            //Start movements
            myTank.turn("left");
            myTank.turn("left");

            //Move to Green opening
            while(myTank.getPositionY() < greenY - tankWidth)
                myTank.move();
            myTank.turn("right");
            myTank.reverse();

            if(!(name.equalsIgnoreCase("Costello") || name.equalsIgnoreCase("Abbott")))
                myTank.setColor(BLUE);

            //Move to middle area
            for(int z = myTank.getPositionX(); z < orangeX - tankWidth*4; z++)
                myTank.move();

            if(!(name.equalsIgnoreCase("Costello") || name.equalsIgnoreCase("Abbott")))
                myTank.setColor(RED);


            //Move through Orange opening
            if(myTank.getPositionY() < orangeY)
            {
                myTank.turn("right");
                while(myTank.getPositionY() < orangeY - tankWidth)
                    myTank.move();
                myTank.turn("left");

                if(!(name.equalsIgnoreCase("Costello") || name.equalsIgnoreCase("Abbott")))
                    myTank.setColor(TEAL);

                while(myTank.getPositionX() < orangeX + tankWidth)
                    myTank.move();
            }
            else if(myTank.getPositionY() > orangeY)
            {
                myTank.turn("left");
                while(myTank.getPositionY() > orangeY - tankWidth)
                    myTank.move();
                myTank.turn("right");

                if(!(name.equalsIgnoreCase("Costello") || name.equalsIgnoreCase("Abbott")))
                    myTank.setColor(MAROON);

                while(myTank.getPositionX() < orangeX + tankWidth)
                    myTank.move();                       
            }
            else
            {
                while(myTank.getPositionX() < orangeX + tankWidth)
                    myTank.move();

                if(!(name.equalsIgnoreCase("Costello") || name.equalsIgnoreCase("Abbott")))
                    myTank.setColor(PURPLE);
            }

            //Turn around and move torwards Target
            myTank.turn("right");
            myTank.turn("right");
            myTank.forward();
                
          

            //Move towards target            
            do{
                //Target is SE of Tank 
                if(myTank.getPositionX() > targetX && myTank.getPositionY() < targetY)
                {
                    if(myTank.getTankOrientation() != SW)
                        do{
                            myTank.turn("half-right");
                        }while(myTank.getTankOrientation() != SW);
                    
                    while(myTank.getPositionX() > targetX && myTank.getPositionY() < targetY)
                        myTank.move(); 
                }
                     
                //Target is NE of Tank
                if(myTank.getPositionX() > targetX && myTank.getPositionY() > targetY)
                {
                    if(myTank.getTankOrientation() != NW)
                        do{
                            myTank.turn("half-right");
                        }while(myTank.getTankOrientation() != NW);
                    
                    while(myTank.getPositionX() > targetX && myTank.getPositionY() > targetY)
                        myTank.move();                    
                }
                target = ls.getTargetLocation();
                //Target is West of Tank "Right"
                if(myTank.getPositionX() < targetX && myTank.getPositionY() == targetY)
                {
                    if(myTank.getTankOrientation() != RIGHT)
                        do{
                            myTank.turn("half-right");
                        }while(myTank.getTankOrientation() != RIGHT);
                    
                    while(myTank.getPositionX() < targetX && myTank.getPositionY() == targetY)
                        myTank.move();                    
                }
                ls.getTargetLocation();
                //Target is SW of Tank
                if(myTank.getPositionX() < targetX && myTank.getPositionY() < targetY)
                {
                    if(myTank.getTankOrientation() != SE)
                        do{
                            myTank.turn("half-right");
                        }while(myTank.getTankOrientation() != SE);
                    
                    while(myTank.getPositionX() < targetX && myTank.getPositionY() < targetY)
                        myTank.move();                    
                }
                ls.getTargetLocation();
                //Target is NW of Tank
                if(myTank.getPositionX() < targetX && myTank.getPositionY() > targetY)
                {
                    if(myTank.getTankOrientation() != NE)
                        do{
                            myTank.turn("half-right");
                        }while(myTank.getTankOrientation() != NE);
                    
                    while(myTank.getPositionX() < targetX && myTank.getPositionY() > targetY)
                        myTank.move();                    
                }
                ls.getTargetLocation();
                //Target is East of Tank "Left"
                if(myTank.getPositionX() > targetX && myTank.getPositionY() == targetY)
                {
                    if(myTank.getTankOrientation() != LEFT)
                        do{
                            myTank.turn("half-right");
                        }while(myTank.getTankOrientation() != LEFT);
                    
                    while(myTank.getPositionX() > targetX && myTank.getPositionY() == targetY)
                        myTank.move();                    
                }
                ls.getTargetLocation();
                //Target is South of Tank "Down"
                if(myTank.getPositionX() == targetX && myTank.getPositionY() < targetY)
                {
                    if(myTank.getTankOrientation() != DOWN)
                        do{
                            myTank.turn("half-right");
                        }while(myTank.getTankOrientation() != DOWN);
                    
                    while(myTank.getPositionX() == targetX && myTank.getPositionY() < targetY)
                        myTank.move();                    
                }
                ls.getTargetLocation();
                //Target is North of Tank "UP"
                if(myTank.getPositionX() == targetX && myTank.getPositionY() > targetY)
                {
                    if(myTank.getTankOrientation() != UP)
                        do{
                            myTank.turn("half-right");
                        }while(myTank.getTankOrientation() != UP);
                    
                    while(myTank.getPositionX() == targetX && myTank.getPositionY() > targetY)
                        myTank.move();                    
                }
                targetX = (int)target.getX();
                targetY = (int)target.getY();
                
            }while(myTank.getPositionX() != targetX || myTank.getPositionY() != targetY);
        
        
        }//end main()
                
}

