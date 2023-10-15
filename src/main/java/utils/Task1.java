package utils;

import algorithms.AStarPath;
import algorithms.MoveInterface;
import algorithms.PlanPath;
import algorithms.Turn;
import map.ArenaConst;
import map.ArenaConst.IMG_DIR;
import map.ArenaMap;
import map.Grid;
import map.Obstacle;
import robot.Bot;
import robot.BotConst;
import utils.MsgConst.INSTRUCTION_TYPE;

import java.util.ArrayList;
import java.util.List;

public class Task1 {

  static Bot bot = new Bot(BotConst.ROBOT_INITIAL_CENTER_COORDINATES,
          BotConst.ROBOT_DIRECTION.NORTH,
          false);

  static ArenaMap arenaMap = new ArenaMap(bot);
  static AStarPath fast = new AStarPath(arenaMap);
  static PlanPath algo = new PlanPath(arenaMap);
  static MsgMgr comm = MsgMgr.getCommMgr();
  static ImageAPI imageAPI = new ImageAPI();


  public static void main(String[] args) {
    comm.connectToRPi();
//    imageAPI.clearDir();

    String receiveMsg = null;


    recvObstacles(); // Receive the obstacles command from androidx

//    System.out.println("Receieve msg: " + receiveMsg);

    int[] path = fast.AStarPath(true);
    System.out.println("Algorithm ready, waiting for start signal... " + "\n");
    while (receiveMsg == null || !receiveMsg.startsWith("ALG")) {
      receiveMsg = comm.recieveMsg();
    }
    doThePath(path);
    System.out.println("No more possible nodes to visit. Pathing finished");
//    imageAPI.combineImages();
    comm.sendMsg("AND:ENDED");

    comm.endConnection();
  }


  private static void doThePath(int[] path) {
    algo.constructMap();
    ArrayList<Obstacle> map = ArenaMap.getObstacles();
    Bot r = arenaMap.getRobot();
    int startX = r.getX();
    int startY = r.getY();
    int startAngle = r.getRobotDir();
    Obstacle next;
    ArrayList<MoveInterface> arrayList;
    int count = 0;

    for (int i : path) {

      System.out.println("What is i: " + i);

      next = map.get(i);
      System.out.println("---------------Path " + count + "---------------");
      System.out.println(next.getX() + ", " + next.getY());
      arrayList = algo.planPath(startX, startY, startAngle, next.getX(), next.getY(),
              next.getImadeDirectionAngle(), true, true, true);
      if (arrayList != null) {
        sendMovesToRobot(arrayList, i);
        int[] coords = algo.getFinalPosition();
        startX = coords[0];
        startY = coords[1];
        startAngle = coords[2];
        count++;
      } else {
        System.out.println("No path found, trying to path to the next obstacle");
      }
    }
  }


  private static void sendMovesToRobot(ArrayList<MoveInterface> moveList, int i) {
    int tryCount = 1;
    ArrayList<MoveInterface> backwardMoveList;
    int[] coords;

    int[] backwardCoords;

    String commandsToSend = encodeMoves(moveList);

    sendToRobot(commandsToSend);
    List<String> obj = imageAPI.detect();

    while (obj.get(0).equals("\"[]\"") && tryCount > 0) {
      System.out.println("Try reversing since no image");
      tryCount--;

      coords = algo.getFinalPosition();
      backwardCoords = algo.getReversePos(coords[0], coords[1], coords[2] / 90);
      if (backwardCoords == null) {
        System.out.println("Reversing is not possible");
        break;
      }
      System.out.println("Reversing to retake picture...");
      backwardMoveList = algo.planPath(coords[0], coords[1], coords[2], backwardCoords[0],
              backwardCoords[1],
              backwardCoords[2] * 90, false, true, true);
      if (backwardMoveList != null) {
        commandsToSend = encodeMoves(backwardMoveList);

        sendToRobot(commandsToSend);
        obj = imageAPI.detect();
      } else {
        break;
      }
    }

    sendImageToAndroid(i, obj);
  }


  private static String encodeMoves(ArrayList<MoveInterface> moveList) {
    String commandsToSend = "STM:";
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

        if (instructionType == INSTRUCTION_TYPE.FORWARD_LEFT) {
          commandsToSend += "t " + INSTRUCTION_TYPE.encode(instructionType) + " 90" + ",";
        } else if (instructionType == INSTRUCTION_TYPE.FORWARD_RIGHT) {
          commandsToSend += "t " + INSTRUCTION_TYPE.encode(instructionType) + " 90" + ",";
        }
      }

    }
    return commandsToSend.substring(0, commandsToSend.length() - 1);
  }


  private static void sendToRobot(String cmd) {
    System.out.printf("Local command sent: %s\n", cmd);
    comm.sendMsg(cmd);
    String receiveMsg = null;

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    sendPathToAndroid();
    // Check if can continue
    while (receiveMsg == null || !receiveMsg.equals("X")) {
      receiveMsg = comm.recieveMsg();
    }

    System.out.println("Message: " + receiveMsg + "\n");
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }


  private static void sendImageToAndroid(int obstacleID, List<String> image) {
    String msg;
    String imageId;
    if (image.size() >= 5) {
      imageId = image.get(4).replace("\"", "");
      imageId = imageId.replace("\\", "");
      System.out.println("Detected image: " + MsgConst.translateImage(Integer.parseInt(imageId)));
    }
    else {
      imageId = "NULL";
    }
      msg = "AND:IMG" + "-" + (obstacleID + 1) + "-" + imageId;
      comm.sendMsg(msg);
    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }


  private static void sendPathToAndroid() {
    ArrayList<Grid> path = algo.getNodePath();
    String pathString = "AND:ALG";

    for (Grid n : path) {
      pathString += "|" + (n.getX() - ArenaConst.BORDER_SIZE) + ","
              + (n.getY() - ArenaConst.BORDER_SIZE) + "," + n.getDim() * 90;
    }
    comm.sendMsg(pathString);
  }


  /**
   * Get obstacle list from android
   *
   */
  private static void recvObstacles() {
    String receiveMsg = null;
    System.out.println("Waiting to receive obstacle list...");

    while (receiveMsg == null || !receiveMsg.startsWith("ALG")) {
      receiveMsg = comm.recieveMsg();
    }

    System.out.println("Received Obstacles String: " + receiveMsg + "\n");

    String[] positions = receiveMsg.split("\\|");

    for (int i = 1; i < positions.length; i++) {
      String[] obs = positions[i].split(",");
      arenaMap.addPictureObstacle(Integer.parseInt(obs[0]), Integer.parseInt(obs[1]),
              IMG_DIR.getImageDirection(obs[2]));
    }
  }
}


//package utils;
//
//import algorithms.AStarPath;
//import algorithms.MoveInterface;
//import algorithms.PlanPath;
//import algorithms.Turn;
//import map.ArenaConst;
//import map.ArenaConst.IMG_DIR;
//import map.ArenaMap;
//import map.Grid;
//import map.Obstacle;
//import robot.Bot;
//import robot.BotConst;
//import utils.MsgConst.INSTRUCTION_TYPE;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class Task1 {
//
//  static Bot bot = new Bot(BotConst.ROBOT_INITIAL_CENTER_COORDINATES,
//      BotConst.ROBOT_DIRECTION.NORTH,
//      false);
//
//  static ArenaMap arenaMap = new ArenaMap(bot);
//  static AStarPath fast = new AStarPath(arenaMap);
//  static PlanPath algo = new PlanPath(arenaMap);
//  static MsgMgr comm = MsgMgr.getCommMgr();
//  static ImageAPI imageAPI = new ImageAPI();
//
//
//  public static void main(String[] args) {
//    comm.connectToRPi();
////    imageAPI.clearDir();
//
//    String receiveMsg = null;
//
//    recvObstacles(); // Receive the obstacles command from android
//
//    int[] path = fast.AStarPath();
//    System.out.println("Algorithm ready, waiting for start signal... " + "\n");
//    while (receiveMsg == null || !receiveMsg.startsWith("ALG")) {
//      receiveMsg = comm.recieveMsg();
//    }
//    doThePath(path);
//    System.out.println("No more possible nodes to visit. Pathing finished");
////    imageAPI.combineImages();
//    comm.sendMsg("AND:ENDED");
//
//    comm.endConnection();
//  }
//
//
//  private static void doThePath(int[] path) {
//    algo.constructMap();
//    ArrayList<Obstacle> map = ArenaMap.getObstacles();
//    Bot r = arenaMap.getRobot();
//    int startX = r.getX();
//    int startY = r.getY();
//    int startAngle = r.getRobotDir();
//    Obstacle next;
//    ArrayList<MoveInterface> arrayList;
//    int count = 0;
//    for (int i : path) {
//      next = map.get(i);
//      System.out.println("---------------Path " + count + "---------------");
//      System.out.println(next.getX() + ", " + next.getY());
//      arrayList = algo.planPath(startX, startY, startAngle, next.getX(), next.getY(),
//          next.getImadeDirectionAngle(), true, true, true);
//      if (arrayList != null) {
//        sendMovesToRobot(arrayList, i);
//        int[] coords = algo.getFinalPosition();
//        startX = coords[0];
//        startY = coords[1];
//        startAngle = coords[2];
//        count++;
//      } else {
//        System.out.println("No path found, trying to path to the next obstacle");
//      }
//    }
//  }
//
//
//  /*
//  I COMMENTED OUT SOME STUFF FOR IMAGE
//   */
//  private static void sendMovesToRobot(ArrayList<MoveInterface> moveList, int i) {
//    int tryCount = 2;
//    ArrayList<MoveInterface> backwardMoveList;
//    int[] coords;
//
//    int[] backwardCoords;
//
//    String commandsToSend = encodeMoves(moveList);
//
//    sendToRobot(commandsToSend);
//    List<String> obj = imageAPI.detect();
//
//    while ((obj == null || obj.get(0).equals("Bullseye"))
//        || obj.get(1).equals("None") && tryCount > 0) {
//      tryCount--;
//
//      coords = algo.getFinalPosition();
//      backwardCoords = algo.getReversePos(coords[0], coords[1], coords[2] / 90);
//
//      if (backwardCoords == null) {
//        break;
//      }
//      System.out.println("Reversing to retake picture...");
//      backwardMoveList = algo.planPath(coords[0], coords[1], coords[2], backwardCoords[0],
//          backwardCoords[1],
//          backwardCoords[2] * 90, false, true, true);
//      if (backwardMoveList != null) {
//        commandsToSend = encodeMoves(backwardMoveList);
//
//        sendToRobot(commandsToSend);
////        obj = imageAPI.detect();
//      } else {
//        break;
//      }
//    }
////    sendImageToAndroid(i, obj);
//  }
//
//
//  private static String encodeMoves(ArrayList<MoveInterface> moveList) {
//    String commandsToSend = "STM:";
//    INSTRUCTION_TYPE instructionType;
//    String formatted;
//    int DistanceBeforeObstacle = 20;
//    for (MoveInterface move : moveList) {
//      int measure = 0;
//      if (move.isStraightLineMovement()) {
//        measure = (int) move.getLength();
//        if (measure == 200) {
//          commandsToSend += "w " + DistanceBeforeObstacle + ",";
//        } else {
//          formatted = String.format("%03d", measure);
//
//          if (move.isBack()) {
//            instructionType = INSTRUCTION_TYPE.BACKWARD;
//          } else {
//            instructionType = INSTRUCTION_TYPE.FORWARD;
//          }
//          commandsToSend += "f " + INSTRUCTION_TYPE.encode(instructionType) + " " + formatted + ",";
//        }
//
//
//      } else {
//        Turn moveConverted = (Turn) move;
//        if (moveConverted.isLeftTurn()) {
//          instructionType = INSTRUCTION_TYPE.FORWARD_LEFT;
//        } else {
//          instructionType = INSTRUCTION_TYPE.FORWARD_RIGHT;
//        }
//
//        if (instructionType == INSTRUCTION_TYPE.FORWARD_LEFT) {
//          commandsToSend += "t " + INSTRUCTION_TYPE.encode(instructionType) + " 90" + ",";
//        } else if (instructionType == INSTRUCTION_TYPE.FORWARD_RIGHT) {
//          commandsToSend += "t " + INSTRUCTION_TYPE.encode(instructionType) + " 90" + ",";
//        }
//      }
//
//    }
//    return commandsToSend.substring(0, commandsToSend.length() - 1);
//  }
//
//
//  private static void sendToRobot(String cmd) {
//    System.out.printf("Local command sent: %s\n", cmd);
//    comm.sendMsg(cmd);
//    String receiveMsg = null;
//
//    try {
//      Thread.sleep(500);
//    } catch (InterruptedException e) {
//      e.printStackTrace();
//    }
//    sendPathToAndroid();
////    while (receiveMsg == null || !receiveMsg.equals("A")) {
////      receiveMsg = comm.recieveMsg();
////    }
//
////    System.out.println("Message: " + receiveMsg + "\n");
//    try {
//      Thread.sleep(500);
//    } catch (InterruptedException e) {
//      e.printStackTrace();
//    }
//  }
//
//
//  private static void sendImageToAndroid(int obstacleID, List<String> image) {
//    String msg;
//
//    msg = "AND:IMG" + "-" + (obstacleID + 1) + "-" + image.get(1);
//    comm.sendMsg(msg);
//    try {
//      Thread.sleep(500);
//    } catch (InterruptedException e) {
//      e.printStackTrace();
//    }
//  }
//
//
//  private static void sendPathToAndroid() {
//    ArrayList<Grid> path = algo.getNodePath();
//    String pathString = "AND:ALG";
//
//    for (Grid n : path) {
//      pathString += "|" + (n.getX() - ArenaConst.BORDER_SIZE) + ","
//          + (n.getY() - ArenaConst.BORDER_SIZE) + "," + n.getDim() * 90;
//    }
//    comm.sendMsg(pathString);
//  }
//
//
//  private static void recvObstacles() {
//    String receiveMsg = null;
//    System.out.println("Waiting to receive obstacle list...");
//    while (receiveMsg == null || !receiveMsg.startsWith("ALG")) {
//      receiveMsg = comm.recieveMsg();
//    }
////    while (receiveMsg == null) {
////      System.out.println("Go");
////      receiveMsg = comm.recieveMsg();
////      System.out.println(receiveMsg);
////    }
////    receiveMsg = comm.recieveMsg();
//
//    System.out.println("Received Obstacles String: " + receiveMsg + "\n");
//
//    String[] positions = receiveMsg.split("\\|");
//
//    for (int i = 1; i < positions.length; i++) {
//      String[] obs = positions[i].split(",");
//      arenaMap.addPictureObstacle(Integer.parseInt(obs[0]), Integer.parseInt(obs[1]),
//          IMG_DIR.getImageDirection(obs[2]));
//    }
//  }
//}
