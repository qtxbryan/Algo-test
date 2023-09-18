package map;

public class ArenaConst {

    public static final int OBS_SIZE = 10;
    public static final int BORDER_SIZE = 0;

    public static final int ARENA_SIZE = 200;

    public enum IMG_DIR {
        NORTH, EAST, SOUTH, WEST;

        public static char print(IMG_DIR d) {
            switch (d) {
                case NORTH:
                    return 'N';
                case EAST:
                    return 'E';
                case SOUTH:
                    return 'S';
                case WEST:
                    return 'W';
                default:
                    return 'X';
            }
        }

        public static IMG_DIR getImageDirection(String s) {
            switch (s) {
                case "N":
                    return NORTH;
                case "E":
                    return EAST;
                case "S":
                    return SOUTH;
                case "W":
                    return WEST;
                default:
                    return null;
            }
        }
    }
}
