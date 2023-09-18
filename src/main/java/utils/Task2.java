package utils;

import algorithms.MoveInterface;
import algorithms.StraightLine;
import algorithms.Turn;
import utils.MsgConst.INSTRUCTION_TYPE;

import java.util.ArrayList;
import java.util.List;

public class Task2 {

  static MsgMgr comm = MsgMgr.getCommMgr();
  static ImageAPI imageAPI = new ImageAPI();
  static int dir = 0;


  public static void main(String[] args) {
    comm.connectToRPi();

    sendMovesToRobot2(start());
    sendMovesToRobot2(smallTurn(0));

    System.out.println("Parked successfully!");
    comm.endConnection();

    return;
  }


  private static ArrayList<MoveInterface> start() {
    ArrayList<MoveInterface> straight = new ArrayList<>();
    straight.add(new StraightLine(0, 0, 0, 200, 0, true, false));
    return straight;
  }


  private static ArrayList<MoveInterface> smallTurn(int dir) {
    ArrayList<MoveInterface> moveList = new ArrayList<>();

    int viewDist = 20;
    int radius = 25;
    int obsWidth = 10;
    int obsLength = 10;
    boolean turnLeft = true;
    int dist;

    if (dir == 0) {

      moveList.add(new Turn(0, 0, radius, radius, 30, radius, radius, false, true));

      moveList.add(new Turn(0, 0, radius, radius, 30, radius, radius, false, false));


    } else {

      moveList.add(new Turn(0, 0, radius, radius, 90, radius, radius, false, false));

      moveList.add(new Turn(0, 0, radius, radius, 90, radius, radius, false, true));


    }

    return moveList;
  }


  private static ArrayList<MoveInterface> bigTurn(int dir) {

    ArrayList<MoveInterface> moveList = new ArrayList<>();

    int viewDist = 20;
    int radius = 25;
    int obsWidth = 10;
    int obsLength = 60;
    int clearance = 20;
    int dist;

    boolean turn1 = true;
    boolean turnX = false;

    if (dir == 1) {
      turn1 = false;
      turnX = true;
    }

    dist = radius * 2 - viewDist - obsWidth / 2;
    if (dist > 0) {
      moveList.add(new StraightLine(0, 0, 0, dist, 90, true, true));
    } else if (dist < 0) {
      moveList.add(new StraightLine(0, 0, 0, -dist, 90, true, false));
    }

    moveList.add(new Turn(0, 0, radius, radius, 90, radius, radius, false, turn1));

    dist = obsLength / 2 - radius - (radius - clearance);
    if (dist > 0) {
      moveList.add(new StraightLine(0, 0, dist, 0, 0, true, false));
    } else if (dist < 0) {
      moveList.add(new StraightLine(0, 0, -dist, 0, 0, true, true));
    }

    moveList.add(new Turn(0, 0, radius, radius, 90, radius, radius, false, turnX));

    moveList.add(new Turn(0, 0, radius, radius, 90, radius, radius, false, turnX));

    dist = obsLength - (radius - clearance) * 2;
    if (dist > 0) {
      moveList.add(new StraightLine(0, 0, dist, 0, 0, true, false));
    } else if (dist < 0) {
      moveList.add(new StraightLine(0, 0, -dist, 0, 0, true, true));
    }

    moveList.add(new Turn(0, 0, radius, radius, 90, radius, radius, false, turnX));

    dist = 200;
    if (dist > 0) {
      moveList.add(new StraightLine(0, 0, dist, 0, 0, true, false));
    } else if (dist < 0) {
      moveList.add(new StraightLine(0, 0, -dist, 0, 0, true, true));
    }

    return moveList;
  }

  private static ArrayList<MoveInterface> park(int dir) {

    ArrayList<MoveInterface> moveList = new ArrayList<>();

    int viewDist = 20;
    int radius = 25;
    int obsWidth = 10;
    int obsLength = 60;
    int clearance = 20;
    int dist;

    boolean turn1 = false;
    boolean turn2 = true;

    if (dir == 1) {
      turn1 = true;
      turn2 = false;
    }

    dist = radius - viewDist;
    if (dist > 0) {
      moveList.add(new StraightLine(0, 0, 0, dist, 90, true, true));
    } else if (dist < 0) {
      moveList.add(new StraightLine(0, 0, 0, -dist, 90, true, false));
    }

    moveList.add(new Turn(0, 0, radius, radius, 90, radius, radius, false, turn1));

    dist = obsLength / 2 + obsWidth / 2 - radius * 2;
    if (dist > 0) {
      moveList.add(new StraightLine(0, 0, dist, 0, 0, true, false));
    } else if (dist < 0) {
      moveList.add(new StraightLine(0, 0, -dist, 0, 0, true, true));
    }

    moveList.add(new Turn(0, 0, radius, radius, 90, radius, radius, false, turn2));

    dist = 200;
    if (dist > 0) {
      moveList.add(new StraightLine(0, 0, dist, 0, 0, true, false));
    } else if (dist < 0) {
      moveList.add(new StraightLine(0, 0, -dist, 0, 0, true, true));
    }

    return moveList;
  }


  private static void sendMovesToRobot2(ArrayList<MoveInterface> moveList) {
    String commandsToSend = encodeMoves(moveList);
    sendToRobot(commandsToSend);
    List<String> obj = imageAPI.detect();
    System.out.println(obj.toString());
    if (obj.get(0).equals("Left")) {
      dir = 0;
    }

    if (obj.get(0).equals("Right")) {
      dir = 1;
    }

    return;
  }


  private static void sendMovesToRobotWithoutImageDetection(ArrayList<MoveInterface> moveList) {
    String commandsToSend = encodeMoves(moveList);
    sendToRobot(commandsToSend);
    return;
  }


  private static void recvStartSignal() {
    String receiveMsg = null;
    System.out.println("Waiting to receive start signal for task 2...");
    while (receiveMsg == null || !receiveMsg.startsWith("start")) {
      receiveMsg = comm.recieveMsg();
    }
    System.out.println("Received start signal");
    return;
  }

  private static void sendToRobot(String cmd) {
    comm.sendMsg(cmd);
    String receiveMsg = null;

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("Waiting for reply from robot");
    while (receiveMsg == null || !receiveMsg.equals("A")) {
      receiveMsg = comm.recieveMsg();
    }

    System.out.println("Message: " + receiveMsg + "\n");
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private static String encodeMoves(ArrayList<MoveInterface> moveList) {
    String commandsToSend = "STM:";
    INSTRUCTION_TYPE instructionType;
    String formatted;
    int DistanceBeforeObstacle = 30;
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

        if (instructionType == INSTRUCTION_TYPE.FORWARD_LEFT) {
          commandsToSend += "t " + INSTRUCTION_TYPE.encode(instructionType) + " 90" + ",";
        } else if (instructionType == INSTRUCTION_TYPE.FORWARD_RIGHT) {
          commandsToSend += "t " + INSTRUCTION_TYPE.encode(instructionType) + " 90" + ",";
        }
      }

    }
    return commandsToSend.substring(0, commandsToSend.length() - 1);
  }
}
