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

  // Test connecting to RPI
  public static void main(String[] args) {

    System.out.println("Waiting to connect with RPi...");
    comm.connectToRPi();

    start();
    sendToRobot("STM:F030,W020");

    // Detect when first wall is reached and check direction
    detectWall();
    int firstDirection = detectDirection();
    doFirstMoves(firstDirection);

    // Detect when second wall is reached and check direction
    detectWall();
    int secondDirection = detectDirection();
    doSecondMoves(secondDirection);

    int A = 30;
    int B = 20;

    // doParking(A,B,firstDirection,secondDirection);

    comm.endConnection();

    System.out.println("Parked successfully!");
    return;
  }

  // public static void main(String[] args) {

  //   sendToRobot("STM:W020");

  //   // Detect when first wall is reached and check direction
  //   int firstDirection = 0;
  //   doFirstMoves(firstDirection);

  //   // Detect when second wall is reached and check direction
  //   int secondDirection = 0;
  //   doSecondMoves(secondDirection);

  //   int A = 30;
  //   int B = 20;

  //   doParking(A,B,firstDirection,secondDirection);

  //   System.out.println("Parked successfully!");
  //   return;
  // }


  private static void sendToRobot(String cmd) {
    comm.sendMsg(cmd);
    System.out.println(cmd);
  }

  private static void start() {
    String receiveMsg = null;
    while (receiveMsg == null || !receiveMsg.startsWith("ALG")) {
      receiveMsg = comm.recieveMsg();
    }
  }

  private static void detectWall() {
    String receiveMsg = null;
    while (receiveMsg == null || !receiveMsg.startsWith("X")) {
      receiveMsg = comm.recieveMsg();
    }
    System.out.println("Wall detected.");
    return;
  }

  private static int detectDirection() {
    System.out.println("Detecting direction...");

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
      if (Integer.parseInt(imageId) != 38 && Integer.parseInt(imageId) != 39) {
        System.out.println("Not arrow, trying to detect again.");
        obj = imageAPI.detect();
        if (!obj.get(0).equals("\"[]\"")) {
          imageId = obj.get(4).replace("\"", "");
          imageId = imageId.replace("\\", "");
          System.out.println("New detected image: " + MsgConst.translateImage(Integer.parseInt(imageId)));
        }
        else {
          System.out.println("Could not detect arrow, proceeding randomly.");
          Random random = new Random();
          return random.nextInt(2);
        }
      }
    }
    else {
          System.out.println("Could not detect arrow, proceeding randomly.");
          Random random = new Random();
          return random.nextInt(2);
    }

    // right
    if (Integer.parseInt(imageId) == 38) {
      System.out.println("Right arrow detected.");
      return 1;
    }
    // left
    else if (Integer.parseInt(imageId) == 39) {
      System.out.println("Left arrow detected.");
      return 0;
    }
    else {
      System.out.println("Could not detect arrow, proceeding randomly.");
      Random random = new Random();
      return random.nextInt(2);
    }
  }

  private static void doFirstMoves(int d) {
    String moves;

    // left
    if (d == 0) {
      moves = "STM:LR00";
    }
    
    // right
    else {
      moves = "STM:RL00";
    }

    sendToRobot(moves);
  }

  private static void doSecondMoves(int d) {
    String moves;

    // left
    if (d == 0) {
      moves = "STM:LL00";
    }
    
    // right
    else {
      moves = "STM:RR00";
    }

    sendToRobot(moves);
  }


  private static int finalFirstDist(int A, int d1, int d2) {
    // LL
    if (d1 == 0 && d2 == 0) {
      return (int) (BotConst.LEFT_TURN_RADIUS_Y + BotConst.SPECIAL_TURN_L + A);
    }
    // LR
    else if (d1 == 0 && d2 == 1) {
      return (int) (BotConst.RIGHT_TURN_RADIUS_Y + BotConst.SPECIAL_TURN_L + A);
    }
    // RL
    else if (d1 == 1 && d2 == 0) {
      return (int) (BotConst.LEFT_TURN_RADIUS_Y + BotConst.SPECIAL_TURN_R + A);
    }
    // RR
    else {
      return (int) (BotConst.RIGHT_TURN_RADIUS_Y + BotConst.SPECIAL_TURN_R + A);
    }
  }


  private static int finalSecondDist(int B, int d1, int d2) {
    // LL
    if (d1 == 0 && d2 == 0) {
      return (int) ((B + BotConst.RIGHT_TURN_RADIUS_Y - BotConst.RIGHT_TURN_RADIUS_X - 2 * BotConst.LEFT_TURN_RADIUS_Y)/2);
    }
    // LR
    else if (d1 == 0 && d2 == 1) {
      return (int) ((B + BotConst.LEFT_TURN_RADIUS_Y - BotConst.LEFT_TURN_RADIUS_X - 2 * BotConst.RIGHT_TURN_RADIUS_Y)/2);
    }
    // RL
    else if (d1 == 1 && d2 == 0) {
      return (int) ((B + BotConst.RIGHT_TURN_RADIUS_Y - BotConst.RIGHT_TURN_RADIUS_X - 2 * BotConst.LEFT_TURN_RADIUS_Y)/2);
    }
    // RR
    else {
      return (int) ((B + BotConst.LEFT_TURN_RADIUS_Y - BotConst.LEFT_TURN_RADIUS_X - 2 * BotConst.RIGHT_TURN_RADIUS_Y)/2);
    }
  }

  // private static int finalFirstDist(int A, int d1, int d2) {
  //   // LL
  //   if (d1 == 0 && d2 == 0) {
  //     return (int) (48 + A);
  //   }
  //   // LR
  //   else if (d1 == 0 && d2 == 1) {
  //     return (int) (49 + A);
  //   }
  //   // RL
  //   else if (d1 == 1 && d2 == 0) {
  //     return (int) (53 + A);
  //   }
  //   // RR
  //   else {
  //     return (int) (54 + A);
  //   }
  // }


  // private static int finalSecondDist(int B, int d1, int d2) {
  //   // LL
  //   if (d1 == 0 && d2 == 0) {
  //     return (int) ((B - 44)/2);
  //   }
  //   // LR
  //   else if (d1 == 0 && d2 == 1) {
  //     return (int) ((B - 45)/2);
  //   }
  //   // RL
  //   else if (d1 == 1 && d2 == 0) {
  //     return (int) ((B - 44)/2);
  //   }
  //   // RR
  //   else {
  //     return (int) ((B - 45)/2);
  //   }
  // }

  private static void doParking(int A, int B, int d1, int d2) {
    int f1 = finalFirstDist(A, d1, d2);
    int f2 = finalSecondDist(B, d1, d2);

    if (d2 == 0) {
      if (f2 >= 0) {
        sendToRobot("STM:F" + f1 + ",Y090,F" + f2 + ",Z088,W020");
      }
      else {
        sendToRobot("STM:F" + f1 + ",Y090,B" + -f2 + ",Z088,W020");
      }
    }
    else {
      if (f2 >= 0) {
        sendToRobot("STM:F" + f1 + ",Z088,F" + f2 + ",Y090,W020");
      }
      else {
        sendToRobot("STM:F" + f1 + ",Z088,B" + -f2 + ",Y090,W020");
      }
    }
  }










  // private static ArrayList<MoveInterface> start() {
  //   ArrayList<MoveInterface> straight = new ArrayList<>();
  //   straight.add(new StraightLine(0, 0, 0, 200, 0, true, false));
  //   return straight;
  // }


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
