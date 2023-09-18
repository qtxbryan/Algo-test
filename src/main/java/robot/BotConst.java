package robot;

import java.awt.*;

public class BotConst {

    public static final int ROBOT_VIRTUAL_WIDTH = 30;
    public static final Point ROBOT_INITIAL_CENTER_COORDINATES = new Point(1, 18);

    public static final int MOVE_COST = 10;
    public static final int REVERSE_COST = 10;
    public static final int TURN_COST_90 = 60;
    public static final int MAX_COST = Integer.MAX_VALUE;

    public static final double LEFT_TURN_RADIUS_Y = 18;
    public static final double LEFT_TURN_RADIUS_X = 29;
    public static final double RIGHT_TURN_RADIUS_Y = 27;
    public static final double RIGHT_TURN_RADIUS_X = 35;
    public static final double MOVE_SPEED = 100;

    public enum ROBOT_DIRECTION {
        NORTH, EAST, SOUTH, WEST
    }
}