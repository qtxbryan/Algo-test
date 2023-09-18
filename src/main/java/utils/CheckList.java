package utils;

import algorithms.MoveInterface;
import algorithms.StraightLine;
import algorithms.Turn;
import utils.MsgConst.INSTRUCTION_TYPE;

import java.util.ArrayList;
import java.util.List;

public class CheckList {

  static MsgMgr comm = MsgMgr.getCommMgr();
  static ImageAPI imageAPI = new ImageAPI();
  static String image = null;

  /**
   * main module to start the algo and establish communication with rpi
   *
   * @param args
   */
  public static void main(String[] args) {
    comm.connectToRPi();

    sendMovesToRobot3(start());
    while (image.equals("Bullseye") || image.equals("None")) {
      System.out.println("Bullseye detected! Sending moves to check the next side");
      sendMovesToRobot3(turnToNextSide());
    }

    System.out.println("Image detected!");
    System.out.println(image);

    comm.endConnection();

    return;
  }

  /**
   * First move of the robot in task 3, creates a line move that travels straight until it detects
   * an obstacle 20cm away
   *
   * @return
   */
  private static ArrayList<MoveInterface> start() {
    ArrayList<MoveInterface> straight = new ArrayList<>();
    straight.add(new StraightLine(0, 0, 200, 0, 0, true, false));
    return straight;
  }

  /**
   * Moves the robot to the next face of the object
   *
   * @return
   */
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


  private static void sendMovesToRobot3(ArrayList<MoveInterface> moveList) {
    int tryCount = 4;
    ArrayList<MoveInterface> backwardMoveList = null;
    String commandsToSend = encodeMoves(moveList);

    sendToRobot(commandsToSend);
    List<String> obj = imageAPI.detect();
    //if obj not detected, create a LineMove for moving backwards by 5cm
    //TODO check whether we pass negative values when reverse = true
    if ((obj == null)) {
      backwardMoveList.add(new StraightLine(0, 0, -5, 0, 0, true, true));
    }
    // retry if image taken is null up to 4 times
    while ((obj == null) && tryCount > 0) {
      tryCount--;
      commandsToSend = encodeMoves(backwardMoveList);
      sendToRobot(commandsToSend);
      obj = imageAPI.detect();
    }

    if (obj.get(0).equals("Bullseye") || obj.get(0).equals("None")) {
      image = obj.get(0);
    }

    //Get image Number and Image ID
    else {
      image = String.format("Image Number: %s | Image ID: %s", obj.get(0), obj.get(1));
    }

    return;
  }

  private static void sendToRobot(String cmd) {
    comm.sendMsg(cmd);
    String receiveMsg = null;

    // buffer so we can space commands
    try {
      Thread.sleep(500);//time is in ms (1000 ms = 1 second)
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    //TODO find out what message we receive back from the robot after sending moves
    while (receiveMsg == null || !receiveMsg.equals("A")) {
      receiveMsg = comm.recieveMsg();
    }

    System.out.println("Message: " + receiveMsg + "\n");
    try {
      Thread.sleep(500); //time is in ms (1000 ms = 1 second)
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private static String encodeMoves(ArrayList<MoveInterface> moveList) {
    String commandsToSend = "";
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
        commandsToSend += "t " + INSTRUCTION_TYPE.encode(instructionType) + " 90" + ",";
      }
//      System.out.println(commandsToSend);
    }
    return commandsToSend.substring(0, commandsToSend.length() - 1);
  }
}
