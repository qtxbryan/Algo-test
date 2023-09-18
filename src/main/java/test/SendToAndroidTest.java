package test;

import utils.BotAPI;
import utils.ImageAPI;
import utils.MsgMgr;

import java.util.List;

public class SendToAndroidTest {

  static MsgMgr comm = MsgMgr.getCommMgr();
  static ImageAPI imageAPI = new ImageAPI();
  static String image = null;
  static BotAPI botAPI;

  public static void main(String[] args) {
    ImageAPI imageAPI = new ImageAPI();
    List<String> obj = imageAPI.detect();
    sendImageToAndroid(1, obj);

  }

  private static void sendImageToAndroid(int obstacleID, List<String> image) {
    String msg;
    // IMG-<obj_id>-<img_id>
    msg = "AND:IMG" + "-" + (obstacleID + 1) + "-" + image.get(1);
    System.out.println(msg);
    try { // try to wait
      Thread.sleep(500);// time is in ms (1000 ms = 1 second)
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
