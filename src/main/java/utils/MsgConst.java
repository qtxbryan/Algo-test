package utils;


public class MsgConst {

  public static final String HOST_ADDRESS = "192.168.15.1";
  public static final int PORT = 1234;

  public enum INSTRUCTION_TYPE {
    FORWARD, BACKWARD, FORWARD_LEFT, FORWARD_RIGHT, BACKWARD_LEFT, BACKWARD_RIGHT, SPECIAL, STOP_AFTER,
    RESET_WHEELS;

    public static String encode(INSTRUCTION_TYPE i) {
      switch (i) {
        case FORWARD:
          return "0";
        case BACKWARD:
          return "1";

        case FORWARD_LEFT:
          return "0";
        case FORWARD_RIGHT:
          return "1";

        default:
          return "-1";
      }
    }
  }

}
