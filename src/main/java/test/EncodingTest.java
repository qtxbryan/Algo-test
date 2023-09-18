package test;

import algorithms.MoveInterface;
import algorithms.StraightLine;
import algorithms.Turn;
import utils.MsgConst.INSTRUCTION_TYPE;

import java.util.ArrayList;

public class EncodingTest {

  public static void main(String[] args) {
    System.out.println(encodeMoves(turnToNextSide()));
  }

  private static String encodeMoves(ArrayList<MoveInterface> moveList) {
    String commandsToSend = "STM:";
    INSTRUCTION_TYPE instructionType;
    String formatted;
    int DistanceBeforeObstacle = 20; // Ahmad Code in cm
    for (MoveInterface move : moveList) {
      int measure = 0;
      if (move.isStraightLineMovement()) {
        measure = (int) move.getLength();
        if (measure == 200) {
          commandsToSend += "w " + DistanceBeforeObstacle + ",";
        } else {
          formatted = String.format("%03d", measure);
          // Ahmad Change here
          if (move.isBack()) {
            instructionType = INSTRUCTION_TYPE.BACKWARD;
          } else {
            instructionType = INSTRUCTION_TYPE.FORWARD;
          }
          commandsToSend += "f " + INSTRUCTION_TYPE.encode(instructionType) + " " + formatted + ",";
        }
        // This is to get the distance need to travel

        // commandsToSend += formatted + INSTRUCTION_TYPE.encode(instructionType) + ",";
      } else {
        Turn moveConverted = (Turn) move;
        if (moveConverted.isLeftTurn()) {
          instructionType = INSTRUCTION_TYPE.FORWARD_LEFT;
        } else {
          instructionType = INSTRUCTION_TYPE.FORWARD_RIGHT;
        }
        if (instructionType == INSTRUCTION_TYPE.FORWARD_LEFT) {
          commandsToSend += "t " + INSTRUCTION_TYPE.encode(instructionType) + " 93" + ",";
        } else if (instructionType == INSTRUCTION_TYPE.FORWARD_RIGHT) {
          commandsToSend += "t " + INSTRUCTION_TYPE.encode(instructionType) + " 90" + ",";
        }
      }
//      System.out.println(commandsToSend);
    }
    return commandsToSend.substring(0, commandsToSend.length() - 1);
  }

  private static ArrayList<MoveInterface> turnToNextSide() {

    ArrayList<MoveInterface> moveList = new ArrayList<>();

    // Constants
    int viewDist = 20;
    int radius = 25;
    int obsWidth = 10;
    int dist;

    // Move 1: Reverse
    dist = radius * 3 - viewDist - obsWidth / 2;
    if (dist > 0) {        // Reverse
      moveList.add(new StraightLine(0, 0, 0, dist, 90, true, true));
    } else if (dist < 0) { // Forward
      moveList.add(new StraightLine(0, 0, 0, -dist, 90, true, false));
    }

    // Move 2: Turn 1
    moveList.add(new Turn(0, 0, radius, radius, 90, radius, radius, false, false));

    // Move 3: Straight
    dist = viewDist + obsWidth / 2 - radius;
    if (dist > 0) {        // Forward
      moveList.add(new StraightLine(0, 0, dist, 0, 0, true, false));
    } else if (dist < 0) { // Reverse
      moveList.add(new StraightLine(0, 0, -dist, 0, 0, true, true));
    }

    // Move 4: Turn 2
    moveList.add(new Turn(0, 0, radius, radius, 90, radius, radius, false, true));

    // Move 5: Turn 3
    moveList.add(new Turn(0, 0, radius, radius, 90, radius, radius, false, true));

    return moveList;
  }
}
