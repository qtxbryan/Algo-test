package utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Task3 {

  static MsgMgr comm = MsgMgr.getCommMgr();
  static ImageAPI imageAPI = new ImageAPI();

  public static void main(String[] args) {

    System.out.println("Waiting to connect with RPi...");
    comm.connectToRPi();

    ArrayList<Integer> image_details = detectImage();
    String forward_command = getForwardCommand(image_details);
    // int d1 = image_details.get(1) + 20;

    start();
    
    // Robot detected left arrow
    if (image_details.get(0) == 0) {
      sendToRobot("STM:" + forward_command + ",LR00");
    }
    // Robot detected right arrow
    else {
      sendToRobot("STM:" + forward_command + ",RL00");
    }

    waitForRobotToMove();

    image_details = detectImage();
    int d2 = image_details.get(1) + 20;
    forward_command = getForwardCommand(image_details);

    // Robot detected left arrow
    if (image_details.get(0) == 0) {
      sendToRobot("STM:" + forward_command + ",LL00");
    }
    // Robot detected right arrow
    else { 
      sendToRobot("STM:" + forward_command + ",RR00");
    }

    waitForRobotToMove();

    // ADJUST THIS FOR ROBOT COMING BACK MOVEMENT
    int final_d = d2 + 40 + 35;
    if (final_d < 100) {
      sendToRobot("STM:F0" + final_d + ",Z090");
    }
    else {
      sendToRobot("STM:F" + final_d + ",Z090");
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
  private static ArrayList<Integer> detectImage() {
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
      if (Integer.parseInt(imageId) == 38) {
        System.out.println("Right arrow detected.");
        image_info.add(1);
      }

      // left arrow detected
      else if (Integer.parseInt(imageId) == 39) {
        System.out.println("Left arrow detected.");
        image_info.add(0);
      }

      // give random number since wrong image detected
      else {
        Random random = new Random();
        image_info.add(random.nextInt(2));
      }
      
      image_dist = getDistanceByImageWidth(image_width);
      dist_to_move = image_dist - 20;
      image_info.add(dist_to_move);
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
    if (width >= 110) { return 10; }
    else if (width >= 78) { return 20; }
    else if (width >= 60) { return 30; }
    else if (width >= 50) { return 40; }
    else if (width >= 43) { return 50; }
    else if (width >= 37) { return 60; }
    else if (width >= 32) { return 70; }
    else if (width >= 29) { return 80; }
    else if (width >= 27) { return 90; }
    else if (width >= 25) { return 100; }
    else if (width >= 23) { return 110; }
    else if (width >= 22) { return 120; }
    else if (width >= 21) { return 130; }
    else if (width >= 20) { return 140; }
    else { return 150; }
  }
}