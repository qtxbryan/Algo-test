package utils;


public class MsgConst {

  public static final String HOST_ADDRESS = "192.168.15.1";
//  public static final String HOST_ADDRESS = "127.0.0.1";
  public static final int PORT = 1234;


  public enum INSTRUCTION_TYPE {
    FORWARD, BACKWARD, FORWARD_LEFT, FORWARD_RIGHT, BACKWARD_LEFT, BACKWARD_RIGHT, SPECIAL, STOP_AFTER,
    RESET_WHEELS;

    public static String encode(INSTRUCTION_TYPE i) {
      switch (i) {
        case FORWARD, FORWARD_LEFT:
          return "0";
        case BACKWARD, FORWARD_RIGHT:
          return "1";

          default:
          return "-1";
      }
    }
  }

}
