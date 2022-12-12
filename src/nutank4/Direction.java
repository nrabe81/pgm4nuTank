package nutank4;
//*************************************************************
//------ Enumerated Data Type: a "DIRECTION" is restricted to these values
//        (note: Target uses NE, SE, SW, NW)
public enum Direction
{
   //directions are in radian PI/4 (or 45 degrees) order (counter-clockwise)
   RIGHT(0), NE(1), UP(2), NW(3), LEFT(4), SW(5), DOWN(6), SE(7);
   int value;
   Direction(int i){ value = i; } //allows ordinal value access
 
}
