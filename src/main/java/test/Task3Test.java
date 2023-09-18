package test;

import algorithms.MoveInterface;
import algorithms.StraightLine;
import algorithms.Turn;
import map.ArenaConst;
import robot.BotConst;
import utils.BotAPI;
import utils.ImageAPI;
import utils.MsgConst.INSTRUCTION_TYPE;
import utils.MsgMgr;

import java.util.ArrayList;
import java.util.List;

public class Task3Test {

  static MsgMgr comm = MsgMgr.getCommMgr();
  static ImageAPI imageAPI = new ImageAPI();
  static String image = null;
  static BotAPI botAPI;


  public static void main(String[] args) {
    calculateTurnSizeX();
    calculateTurnSizeY();
    return;
  }

  private static int calculateTurnSizeY() {
    double gridSize = ArenaConst.OBS_SIZE;

    double largestRadius = Math.max(BotConst.LEFT_TURN_RADIUS_Y,
        BotConst.RIGHT_TURN_RADIUS_Y);

    System.out.println(Math.ceil(largestRadius / gridSize));
    return (int) Math.ceil(largestRadius / gridSize);
  }

  private static int calculateTurnSizeX() {
    double gridSize = ArenaConst.OBS_SIZE;

    double largestRadius = Math.max(BotConst.LEFT_TURN_RADIUS_X,
        BotConst.RIGHT_TURN_RADIUS_X);

    System.out.println(Math.ceil(largestRadius / gridSize));
    return (int) Math.ceil(largestRadius / gridSize);
  }


  private static ArrayList<MoveInterface> turnToNextSide() {

    ArrayList<MoveInterface> moveList = new ArrayList<>();

    int viewDist = 20;
    int radius = 25;
    int obsWidth = 10;
    int dist;

    dist = radius * 3 - viewDist - obsWidth / 2;
    if (dist > 0) {
      moveList.add(new StraightLine(0, 0, 0, dist, 90, true, true));
    } else if (dist < 0) {
      moveList.add(new StraightLine(0, 0, 0, -dist, 90, true, false));
    }

    moveList.add(new Turn(0, 0, radius, radius, 90, radius, radius, false, false));

    dist = viewDist + obsWidth / 2 - radius;
    if (dist > 0) {
      moveList.add(new StraightLine(0, 0, dist, 0, 0, true, false));
    } else if (dist < 0) {
      moveList.add(new StraightLine(0, 0, -dist, 0, 0, true, true));
    }

    moveList.add(new Turn(0, 0, radius, radius, 90, radius, radius, false, true));

    moveList.add(new Turn(0, 0, radius, radius, 90, radius, radius, false, true));

    return moveList;
  }


  private static void sendMovesToRobot3(ArrayList<MoveInterface> moveList, int tryCount) {
    ArrayList<MoveInterface> backwardMoveList = null;

    ArrayList<String> listOfCommands = new ArrayList<>();
    System.out.println("Send this to the Robot:");
    listOfCommands.add("f 1 40,t 1 90,f 1 10,f 0 35,t 0 182");
    listOfCommands.add("f 1 40,t 1 90,f 1 10,f 0 35,t 0 180");
    listOfCommands.add("f 1 40,t 1 90,f 1 10,f 0 45,t 0 180");

    System.out.println(listOfCommands.get(tryCount));
    botAPI.postCommand(listOfCommands.get(tryCount));

    System.out.println("Waiting for Image to be detected!");
    try {
      Thread.sleep(15000);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    List<String> obj = imageAPI.detect();

    if ((obj == null)) {
      backwardMoveList.add(new StraightLine(0, 0, -5, 0, 0, true, true));
    }

    while ((obj == null) && tryCount > 0) {
      tryCount--;
      String commandsToSend = encodeMoves(backwardMoveList);
      botAPI.postCommand(commandsToSend);
      obj = imageAPI.detect();
    }

    if (obj.get(0).equals("Bullseye") || obj.get(0).equals("None")) {
      image = obj.get(0);
    } else {
      image = String.format("Image Number: %s | Image ID: %s", obj.get(0), obj.get(1));
    }

    return;
  }


  private static String encodeMoves(ArrayList<MoveInterface> moveList) {
    String commandsToSend = "";
    INSTRUCTION_TYPE instructionType;
    String formatted;
    int DistanceBeforeObstacle = 20;
    for (MoveInterface move : moveList) {
      int measure = 0;
      if (move.isStraightLineMovement()) {
        measure = (int) move.getLength();
        if (measure == 200) {
          commandsToSend += "w " + DistanceBeforeObstacle + ",";
        } else {
          formatted = String.format("%03d", measure);

          if (move.isBack()) {
            instructionType = INSTRUCTION_TYPE.BACKWARD;
          } else {
            instructionType = INSTRUCTION_TYPE.FORWARD;
          }
          commandsToSend += "f " + INSTRUCTION_TYPE.encode(instructionType) + " " + formatted + ",";
        }


      } else {
        Turn moveConverted = (Turn) move;
        if (moveConverted.isLeftTurn()) {
          instructionType = INSTRUCTION_TYPE.FORWARD_LEFT;
        } else {
          instructionType = INSTRUCTION_TYPE.FORWARD_RIGHT;
        }
        commandsToSend += "t " + INSTRUCTION_TYPE.encode(instructionType) + " 90" + ",";
      }

    }
    return commandsToSend.substring(0, commandsToSend.length() - 1);
  }
}

