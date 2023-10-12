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

  public static String translateImage(int imageId) {
    switch (imageId) {
      case 11:
        return "1";
      case 12:
        return "2";
      case 13:
        return "3";
      case 14:
        return "4";
      case 15:
        return "5";
      case 16:
        return "6";
      case 17:
        return "7";
      case 18:
        return "8";
      case 19:
        return "9";
      case 20:
        return "A";
      case 21:
        return "B";
      case 22:
        return "C";
      case 23:
        return "D";
      case 24:
        return "E";
      case 25:
        return "F";
      case 26:
        return "G";
      case 27:
        return "H";
      case 28:
        return "S";
      case 29:
        return "T";
      case 30:
        return "U";
      case 31:
        return "V";
      case 32:
        return "W";
      case 33:
        return "X";
      case 34:
        return "Y";
      case 35:
        return "Z";
      case 36:
        return "UP";
      case 37:
        return "DOWN";
      case 38:
        return "RIGHT";
      case 39:
        return "LEFT";
      case 40:
        return "STOP";
      case 99:
        return "BULLSEYE";
      default:
        return "";
    }
  }

}
