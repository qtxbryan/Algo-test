package utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Task3 {

  static MsgMgr comm = MsgMgr.getCommMgr();
  static ImageAPI imageAPI = new ImageAPI();
  static int count = 0;

  public static void main(String[] args) {

    System.out.println("Waiting to connect with RPi...");
    comm.connectToRPi();

    start();

    ArrayList<Integer> image_details = detectImage(0);
    // String forward_command = getForwardCommand(image_details);
    int d1 = image_details.get(1) + 20;
    
    // Robot detected left arrow
    if (image_details.get(0) == 0) {
      // sendToRobot("STM:" + forward_command + ",LR00");
      if (d1 <= 40) {
        sendToRobot("STM:W020,LR00");  
      }
      else if (d1 < 130) {
        sendToRobot("STM:K0" + (d1-40) + ",LR00");  
      }
      else {
        sendToRobot("STM:K" + (d1-40) + ",LR00");
      }
    }
    // Robot detected right arrow
    else {
      // sendToRobot("STM:" + forward_command + ",RL00");
      if (d1 <= 40) {
        sendToRobot("STM:W020,RL00");  
      }
      else if (d1 < 130) {
        sendToRobot("STM:K0" + (d1-40) + ",RL00");  
      }
      else {
        sendToRobot("STM:K" + (d1-40) + ",RL00");
      }
    }

    waitForRobotToMove();

    image_details = detectImage(1);
    int d2 = image_details.get(1) + 20;
    // forward_command = getForwardCommand(image_details);

    // // Robot detected left arrow
    // if (image_details.get(0) == 0) {
    //   // sendToRobot("STM:" + forward_command + ",LL00");
    //   sendToRobot("STM:W020,LL00");
    // }
    // // Robot detected right arrow
    // else { 
    //   sendToRobot("STM:" + forward_command + ",RR00");
    //   sendToRobot("STM:W020,RR00");
    // }

    // waitForRobotToMove();

    // ADJUST THIS FOR ROBOT COMING BACK MOVEMENT
    int final_d = d2 + 80 - 1;

    if (final_d < 100) {
      if (image_details.get(0) == 0) {
        if (d2 <= 40) {
          sendToRobot("STM:L0" + final_d);
        }
        else if (d2 < 140) {
          sendToRobot("STM:F0" + (d2-40) + ",L0" + final_d);
        }
        else {
          sendToRobot("STM:F" + (d2-40) + ",L0" + final_d);
        }
      }
      else {
        if (d2 <= 40) {
          sendToRobot("STM:R0" + final_d);
        }
        else if (d2 < 140) {
          sendToRobot("STM:F0" + (d2-40) + ",R0" + final_d);
        }
        else {
          sendToRobot("STM:F" + (d2-40) + ",R0" + final_d);
        }
      }
    }
    else {
      if (image_details.get(0) == 0) {
        if (d2 <= 40) {
          sendToRobot("STM:L" + final_d);
        }
        else if (d2 < 140) {
          sendToRobot("STM:F0" + (d2-40) + ",L" + final_d);
        }
        else {
          sendToRobot("STM:F" + (d2-40) + ",L" + final_d);
        }
      }
      else {
        if (d2 <= 40) {
          sendToRobot("STM:R" + final_d);
        }
        else if (d2 < 140) {
          sendToRobot("STM:F0" + (d2-40) + ",R" + final_d);
        }
        else {
          sendToRobot("STM:F" + (d2-40) + ",R" + final_d);
        }
      }
    }

    comm.endConnection();
    System.out.println("Returning to carpark...");
    return;
  }

  /*
   * Sends command to robot
   */
  private static void sendToRobot(String cmd) {
    comm.sendMsg(cmd);
    System.out.println(cmd);
  }

  /*
   * Waits RPI to signal robot is ready to start
   */
  private static void start() {
    String receiveMsg = null;
    while (receiveMsg == null || !receiveMsg.startsWith("ALG")) {
      receiveMsg = comm.recieveMsg();
    }
  }

  /*
   * Waits RPI to signal that robot is done moving
   */
  private static void waitForRobotToMove() {
    String receiveMsg = null;
    while (receiveMsg == null || !receiveMsg.startsWith("X")) {
      receiveMsg = comm.recieveMsg();
    }
    return;
  }

  /*
   * Return image info in list (imageId, dist_to_move)
   *  -> dist_to_move < 0 if backward move needed
   */
  private static ArrayList<Integer> detectImage(int a) {
    System.out.println("Detecting direction...");

    List<String> obj = imageAPI.detect();
    String imageId = "";
    int image_width, image_dist, dist_to_move;
    ArrayList<Integer> image_info = new ArrayList<Integer>();

    if (!obj.get(0).equals("\"[]\"")) {
      imageId = obj.get(4).replace("\"", "");
      imageId = imageId.replace("\\", "");
      image_width = Integer.parseInt(obj.get(2)) - Integer.parseInt(obj.get(0).replace("\"[[", ""));

      // right arrow detected
      if (Integer.parseInt(imageId) == 38 || Integer.parseInt(imageId) == 37) {
        System.out.println("Right arrow detected.");
        image_info.add(1);
      }

      // left arrow detected
      else if (Integer.parseInt(imageId) == 39) {
        System.out.println("Left arrow detected.");
        image_info.add(0);
      }
      
      image_dist = getDistanceByImageWidth(image_width);
      System.out.println("Detected distance: " + image_dist + "cm");
      dist_to_move = image_dist - 20;
      image_info.add(dist_to_move);
    }
    else {
      if (a != 0) {
        sendToRobot("STM:B020");
      }
      obj = imageAPI.detect();
      if (!obj.get(0).equals("\"[]\"")) {
        imageId = obj.get(4).replace("\"", "");
        imageId = imageId.replace("\\", "");
        image_width = Integer.parseInt(obj.get(2)) - Integer.parseInt(obj.get(0).replace("\"[[", ""));

        // right arrow detected
        if (Integer.parseInt(imageId) == 38 || Integer.parseInt(imageId) == 37) {
          System.out.println("Right arrow detected.");
          image_info.add(1);
        }

        // left arrow detected
        else if (Integer.parseInt(imageId) == 39) {
          System.out.println("Left arrow detected.");
          image_info.add(0);
        }

        if (a == 0) {
          image_dist = getDistanceByImageWidth(image_width);
          System.out.println("Detected distance: " + image_dist + "cm");
          dist_to_move = image_dist - 20 - 20;
          image_info.add(dist_to_move);
        }
        else {
          image_info.add(0);
        }
      }
      else {
        Random random = new Random();
        image_info.add(random.nextInt(2));
        image_info.add(0);
      }
    }

    return image_info;
  }

  /*
   * Returns forward command string based on image details
   */
  private static String getForwardCommand(ArrayList<Integer> image_details) {
    String dist_cmd = "";

    // robot needs to move backwards
    if (image_details.get(1) < 0) {
      if (image_details.get(1) < 100) {
        dist_cmd = "B0" + (-image_details.get(1));
      }
      else {
        dist_cmd = "B" + (-image_details.get(1));
      }
    }

    // robot needs to move forward
    else if (image_details.get(1) > 0) {
      if (image_details.get(1) < 100) {
        dist_cmd = "F0" + (image_details.get(1));
      }
      else {
        dist_cmd = "F" + (image_details.get(1));
      }
    }

    return dist_cmd;
  }

  /*
   * Returns image distance from robot based on image width
   */
  private static int getDistanceByImageWidth(int width) {
    if (width >= 140) { return 0; }
    else if (width >= 100) { return 10; }
    else if (width >= 78) { return 20; }
    else if (width >= 60) { return 30; }
    else if (width >= 50) { return 40; }
    else if (width >= 43) { return 50; }
    else if (width >= 36) { return 60; }
    else if (width >= 32) { return 70; }
    else if (width >= 29) { return 80; }
    else if (width >= 27) { return 90; }
    else if (width >= 25) { return 100; }
    else if (width >= 23) { return 110; }
    else if (width >= 21) { return 120; }
    else if (width >= 20) { return 130; }
    else if (width >= 19) { return 140; }
    // 150: 18
    else if (width >= 13) { return 150; }
    else { return 0; }
  }
}