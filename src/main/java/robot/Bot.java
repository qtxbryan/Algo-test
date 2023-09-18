package robot;

import robot.BotConst.ROBOT_DIRECTION;

import java.awt.*;


public class Bot {

    private Point centre;
    private ROBOT_DIRECTION dir;
    private Boolean isARobot;

    public Bot(Point centre, ROBOT_DIRECTION dir, Boolean isARobot) {
        this.centre = centre;
        this.dir = dir;
        this.isARobot = isARobot;
    }

    public Point getCentre() {
        return centre;
    }

    public void setCentre(Point centre) {
        this.centre = centre;
    }

    public ROBOT_DIRECTION getDir() {
        return dir;
    }

    public void setDir(ROBOT_DIRECTION dir) {
        this.dir = dir;
    }

    public void setDirection(int angle) {
        switch (angle) {
            case 0:
                dir = ROBOT_DIRECTION.EAST;
                break;
            case 90:
                dir = ROBOT_DIRECTION.NORTH;
                break;
            case 180:
                dir = ROBOT_DIRECTION.WEST;
                break;
            case 270:
                dir = ROBOT_DIRECTION.SOUTH;
                break;
            default:
                dir = ROBOT_DIRECTION.EAST;
                break;
        }
    }

    public int getRobotDir() {
        int degree;
        switch (dir) {
            case EAST:
                degree = 0;
                break;
            case NORTH:
                degree = 90;
                break;
            case WEST:
                degree = 180;
                break;
            case SOUTH:
                degree = 270;
                break;
            default:
                degree = -1;
                break;
        }
        return degree;
    }

    public int getX() {
        return centre.x;
    }

    public int getY() {
        return centre.y;
    }

    public Boolean getIsARobot() {
        return isARobot;
    }

    public void setIsARobot(Boolean isARobot) {
        this.isARobot = isARobot;
    }
}
