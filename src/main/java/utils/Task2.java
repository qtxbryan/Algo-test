package utils;

import algorithms.MoveInterface;
import algorithms.StraightLine;
import algorithms.Turn;
import robot.BotConst;
import utils.MsgConst.INSTRUCTION_TYPE;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// import com.ibm.j9ddr.vm29.pointer.generated.rankTableEntryPointer;

public class Task2 {

  static MsgMgr comm = MsgMgr.getCommMgr();
  static ImageAPI imageAPI = new ImageAPI();
  static int dir = 0;


  public static void main(String[] args) {
    // sendMovesToRobot2(start());
    // sendMovesToRobot2(smallTurn(0));

    comm.connectToRPi();
    comm.sendMsg("STM:w 0 30");

    // Detect when first wall is reached
    detectWall();
    int firstDirection = detectDirection();
    doFirstMoves(firstDirection);

    // Detect when second wall is reached
    detectWall();
    int secondDirection = detectDirection();
    doSecondMoves(secondDirection);

    // Detect when bullseye is reached
    detectWall();
    detectBullseye();
    doParking(secondDirection);

    comm.endConnection();

    return;
  }

  private static void detectWall() {
    String receiveMsg = null;
    while (receiveMsg == null || !receiveMsg.startsWith("ALG")) {
      receiveMsg = comm.recieveMsg();
    }
    System.out.println("Wall detected.");
    return;
  }

  private static int detectDirection() {
    String receiveMsg = null;
    System.out.println("Waiting to detect direction...");

    while (receiveMsg == null || !receiveMsg.startsWith("ALG")) {
      receiveMsg = comm.recieveMsg();
    }

    List<String> obj = imageAPI.detect();
    String imageId = "";

    int tryCount = 0;
    // no image detected, try again
    while (obj.get(0).equals("\"[]\"") && tryCount < 1) {
      System.out.println("No image, trying to detect again.");
      obj = imageAPI.detect();
      tryCount += 1;
    }
    
    if (!obj.get(0).equals("\"[]\"")) {
      imageId = obj.get(4).replace("\"", "");
      imageId = imageId.replace("\\", "");
      System.out.println("Detected image: " + MsgConst.translateImage(Integer.parseInt(imageId)));

      // no arrow detected, try again
      if (imageId != "38" && imageId != "39") {
        System.out.println("Not arrow, trying to detect again.");
        obj = imageAPI.detect();
        if (!obj.get(0).equals("\"[]\"")) {
          imageId = obj.get(4).replace("\"", "");
          imageId = imageId.replace("\\", "");
          System.out.println("New detected image: " + MsgConst.translateImage(Integer.parseInt(imageId)));
        }
      }
    }

    // right
    if (imageId == "38") {
      System.out.println("Right arrow detected.");
      return 1;
    }
    // left
    else if (imageId == "39") {
      System.out.println("Left arrow detected.");
      return 0;
    }
    // guess if no arrow detected
    else {
      System.out.println("Could not detect arrow, proceeding randomly.");
      Random random = new Random();
      return random.nextInt(2);
    }
  }

  private static void detectBullseye() {
    List<String> obj = imageAPI.detect();
    int tryCount = 0;
    // no image, try again
    while (obj.get(0).equals("\"[]\"") && tryCount < 1) {
      System.out.println("No image, trying to detect again.");
      obj = imageAPI.detect();
      tryCount += 1;
    }

    if (!obj.get(0).equals("\"[]\"")) {
      String imageId = obj.get(4).replace("\"", "");
      imageId = imageId.replace("\\", "");
      
      // retry if not bullseye
      if (imageId != "99") {
        obj = imageAPI.detect();
        if (!obj.get(0).equals("\"[]\"")) {
          imageId = obj.get(4).replace("\"", "");
          imageId = imageId.replace("\\", "");
        }
      }

      if (imageId == "99") {
        System.out.println("Detected bullseye");
        // return true;
      }
    }
    System.out.println("Could not detect bullseye :-(");
    // return false;
  }

  private static void doFirstMoves(int d) {
    String moves;
    int diff;

    // left
    if (d == 0) {
      moves = "STM:t 2 90";

      if (BotConst.LEFT_TURN_RADIUS_X + BotConst.RIGHT_TURN_RADIUS_Y < BotConst.LEFT_TURN_RADIUS_Y + BotConst.RIGHT_TURN_RADIUS_X) {
        diff = (int)((BotConst.LEFT_TURN_RADIUS_Y + BotConst.RIGHT_TURN_RADIUS_X)-(BotConst.LEFT_TURN_RADIUS_X + BotConst.RIGHT_TURN_RADIUS_Y));
        moves += ",f 0 " + diff;
      }

      moves += ",t 3 90";

      if (BotConst.RIGHT_TURN_RADIUS_X + BotConst.RIGHT_TURN_RADIUS_Y + BotConst.LEFT_TURN_RADIUS_Y < 70) {
        diff = (int)(70-(BotConst.RIGHT_TURN_RADIUS_X + BotConst.RIGHT_TURN_RADIUS_Y + BotConst.LEFT_TURN_RADIUS_Y));
        moves += ",f 0 " + diff;
      }
      else if (BotConst.RIGHT_TURN_RADIUS_X + BotConst.RIGHT_TURN_RADIUS_Y + BotConst.LEFT_TURN_RADIUS_Y > 70) {
        diff = (int)((BotConst.RIGHT_TURN_RADIUS_X + BotConst.RIGHT_TURN_RADIUS_Y + BotConst.LEFT_TURN_RADIUS_Y)-70);
        moves += ",f 1 " + diff;
      }

      moves += ",t 3 90";
      
      if (BotConst.LEFT_TURN_RADIUS_X + BotConst.RIGHT_TURN_RADIUS_Y > BotConst.LEFT_TURN_RADIUS_Y + BotConst.RIGHT_TURN_RADIUS_X) {
        diff = (int)((BotConst.LEFT_TURN_RADIUS_X + BotConst.RIGHT_TURN_RADIUS_Y)-(BotConst.LEFT_TURN_RADIUS_Y + BotConst.RIGHT_TURN_RADIUS_X));
        moves += ",f 0 " + diff;
      }

      moves += ",t 2 90, w 0 30";
    }
    // right
    else {
      moves = "STM:t 3 90";

      if (BotConst.RIGHT_TURN_RADIUS_X + BotConst.LEFT_TURN_RADIUS_Y < BotConst.RIGHT_TURN_RADIUS_Y + BotConst.LEFT_TURN_RADIUS_X) {
        diff = (int)((BotConst.RIGHT_TURN_RADIUS_Y + BotConst.LEFT_TURN_RADIUS_X)-(BotConst.RIGHT_TURN_RADIUS_X + BotConst.LEFT_TURN_RADIUS_Y));
        moves += ",f 0 " + diff;
      }

      moves += ",t 2 90";

      if (BotConst.LEFT_TURN_RADIUS_X + BotConst.LEFT_TURN_RADIUS_Y + BotConst.RIGHT_TURN_RADIUS_Y < 70) {
        diff = (int)(70-(BotConst.LEFT_TURN_RADIUS_X + BotConst.LEFT_TURN_RADIUS_Y + BotConst.RIGHT_TURN_RADIUS_Y));
        moves += ",f 0 " + diff;
      }
      else if (BotConst.RIGHT_TURN_RADIUS_X + BotConst.RIGHT_TURN_RADIUS_Y + BotConst.LEFT_TURN_RADIUS_Y > 70) {
        diff = (int)((BotConst.RIGHT_TURN_RADIUS_X + BotConst.RIGHT_TURN_RADIUS_Y + BotConst.LEFT_TURN_RADIUS_Y)-70);
        moves += ",f 1 " + diff;
      }

      moves += ",t 2 90";
      
      if (BotConst.LEFT_TURN_RADIUS_X + BotConst.RIGHT_TURN_RADIUS_Y > BotConst.LEFT_TURN_RADIUS_Y + BotConst.RIGHT_TURN_RADIUS_X) {
        diff = (int)((BotConst.LEFT_TURN_RADIUS_X + BotConst.RIGHT_TURN_RADIUS_Y)-(BotConst.LEFT_TURN_RADIUS_Y + BotConst.RIGHT_TURN_RADIUS_X));
        moves += ",f 0 " + diff;
      }
      
      moves += ",t 3 90, w 0 30";
    }

    comm.sendMsg(moves);
  }

  private static void doSecondMoves(int d) {
    String moves;
    int diff;

    // left
    if (d == 0) {
      moves = "STM:t 2 90";
      diff = (int) (70 - BotConst.LEFT_TURN_RADIUS_X - BotConst.RIGHT_TURN_RADIUS_Y);
      moves += ",f 0 " + diff;
      moves += ",t 3 90,t 3 90";
      diff = (int) (140 - BotConst.RIGHT_TURN_RADIUS_X - BotConst.RIGHT_TURN_RADIUS_Y);
      moves += ",f 0 " + diff;
      moves += ",t 3 90,t 3 90";
      if (BotConst.RIGHT_TURN_RADIUS_X + BotConst.LEFT_TURN_RADIUS_Y < 35) {
        diff = (int) (35 - BotConst.RIGHT_TURN_RADIUS_X - BotConst.LEFT_TURN_RADIUS_Y);
        moves += ",f 0 " + diff;
      }
      else if (BotConst.RIGHT_TURN_RADIUS_X + BotConst.LEFT_TURN_RADIUS_Y > 35) {
        diff = (int) (35 - BotConst.RIGHT_TURN_RADIUS_X - BotConst.LEFT_TURN_RADIUS_Y);
        moves += ",f 1 " + diff;
      }
      moves += ",t 2 90,w 0 30";
    }
    // right
    else {
      moves = "STM:t 3 90";
      diff = (int) (70 - BotConst.RIGHT_TURN_RADIUS_X - BotConst.LEFT_TURN_RADIUS_Y);
      moves += ",f 0 " + diff;
      moves += ",t 2 90,t 2 90";
      diff = (int) (140 - BotConst.LEFT_TURN_RADIUS_X - BotConst.LEFT_TURN_RADIUS_Y);
      moves += ",f 0 " + diff;
      moves += ",t 2 90,t 2 90";
      if (BotConst.LEFT_TURN_RADIUS_X + BotConst.RIGHT_TURN_RADIUS_Y < 35) {
        diff = (int) (35 - BotConst.LEFT_TURN_RADIUS_X - BotConst.RIGHT_TURN_RADIUS_Y);
        moves += ",f 0 " + diff;
      }
      else if (BotConst.LEFT_TURN_RADIUS_X + BotConst.RIGHT_TURN_RADIUS_Y > 35) {
        diff = (int) (35 - BotConst.LEFT_TURN_RADIUS_X - BotConst.RIGHT_TURN_RADIUS_Y);
        moves += ",f 1 " + diff;
      }
      moves += ",t 3 90,w 0 30";
    }

    comm.sendMsg(moves);
  }

  private static void doParking(int d) {
    String parkMoves;
    int diff;

    // left, end up on right
    if (d == 0) {
      parkMoves = "STM:t 3 30";

      if (BotConst.RIGHT_TURN_RADIUS_Y + BotConst.LEFT_TURN_RADIUS_X > 70) {
        diff = (int) ((BotConst.RIGHT_TURN_RADIUS_Y + BotConst.LEFT_TURN_RADIUS_X) - 70);
        parkMoves += ",f 1 " + diff;
      }

      if (BotConst.RIGHT_TURN_RADIUS_X + BotConst.LEFT_TURN_RADIUS_Y < 35) {
        diff = (int) (35 - (BotConst.RIGHT_TURN_RADIUS_X + BotConst.LEFT_TURN_RADIUS_Y));
        parkMoves += ",f 0 " + diff;
      }
      else if (BotConst.RIGHT_TURN_RADIUS_X + BotConst.LEFT_TURN_RADIUS_Y > 35) {
        diff = (int) (BotConst.RIGHT_TURN_RADIUS_X + BotConst.LEFT_TURN_RADIUS_Y) - 35;
        parkMoves += ",f 1 " + diff;
      }

      parkMoves += ",t 2 90";
      
      if (BotConst.RIGHT_TURN_RADIUS_Y + BotConst.LEFT_TURN_RADIUS_X < 70) {
        diff = (int) (70 - (BotConst.RIGHT_TURN_RADIUS_Y + BotConst.LEFT_TURN_RADIUS_X));
        parkMoves += ",f 0 " + diff;
      }
    }
    // right, end up on left
    else {
      parkMoves = "STM:t 2 30";

      if (BotConst.LEFT_TURN_RADIUS_Y + BotConst.RIGHT_TURN_RADIUS_X > 70) {
        diff = (int) ((BotConst.LEFT_TURN_RADIUS_Y + BotConst.RIGHT_TURN_RADIUS_X) - 70);
        parkMoves += ",f 1 " + diff;
      }

      if (BotConst.LEFT_TURN_RADIUS_X + BotConst.RIGHT_TURN_RADIUS_Y < 35) {
        diff = (int) (35 - (BotConst.LEFT_TURN_RADIUS_X + BotConst.RIGHT_TURN_RADIUS_Y));
        parkMoves += ",f 0 " + diff;
      }
      else if (BotConst.LEFT_TURN_RADIUS_X + BotConst.RIGHT_TURN_RADIUS_Y > 35) {
        diff = (int) (BotConst.LEFT_TURN_RADIUS_X + BotConst.RIGHT_TURN_RADIUS_Y) - 35;
        parkMoves += ",f 1 " + diff;
      }

      parkMoves += ",t 3 90";
      
      if (BotConst.LEFT_TURN_RADIUS_Y + BotConst.RIGHT_TURN_RADIUS_X < 70) {
        diff = (int) (70 - (BotConst.LEFT_TURN_RADIUS_Y + BotConst.RIGHT_TURN_RADIUS_X));
        parkMoves += ",f 0 " + diff;
      }
    }

    comm.sendMsg(parkMoves);
    System.out.println("Parked successfully!");
  }
















  private static ArrayList<MoveInterface> start() {
    ArrayList<MoveInterface> straight = new ArrayList<>();
    straight.add(new StraightLine(0, 0, 0, 200, 0, true, false));
    return straight;
  }

  /** May need to add straight line in middle of the 2 turns, but not sure the straight line how long, might need to receive the variable from android **/
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

  // private static void sendToRobot(String cmd) {
  //   comm.sendMsg(cmd);
  //   String receiveMsg = null;

  //   try {
  //     Thread.sleep(500);
  //   } catch (InterruptedException e) {
  //     e.printStackTrace();
  //   }
  //   System.out.println("Waiting for reply from robot");
  //   while (receiveMsg == null || !receiveMsg.equals("A")) {
  //     receiveMsg = comm.recieveMsg();
  //   }

  //   System.out.println("Message: " + receiveMsg + "\n");
  //   try {
  //     Thread.sleep(500);
  //   } catch (InterruptedException e) {
  //     e.printStackTrace();
  //   }
  // }

  // private static void sendMovesToRobot2(ArrayList<MoveInterface> moveList) {
  //   String commandsToSend = encodeMoves(moveList);
  //   sendToRobot(commandsToSend);
  //   List<String> obj = imageAPI.detect();
  //   System.out.println(obj.toString());
  //   if (obj.get(0).equals("Left")) {
  //     dir = 0;
  //   }

  //   if (obj.get(0).equals("Right")) {
  //     dir = 1;
  //   }

  //   return;
  // }


  // private static void sendMovesToRobotWithoutImageDetection(ArrayList<MoveInterface> moveList) {
  //   String commandsToSend = encodeMoves(moveList);
  //   sendToRobot(commandsToSend);
  //   return;
  // }


  private static void recvStartSignal() {
    String receiveMsg = null;
    System.out.println("Waiting to receive start signal for task 2...");
    while (receiveMsg == null || !receiveMsg.startsWith("start")) {
      receiveMsg = comm.recieveMsg();
    }
    System.out.println("Received start signal");
    return;
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
