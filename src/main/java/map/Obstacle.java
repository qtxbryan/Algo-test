package map;

import map.ArenaConst.IMG_DIR;

import java.awt.*;


public class Obstacle {

    private int imageId;
    private IMG_DIR imageDirection;
    private Point centerCoordinate;

    public Obstacle(int x, int y, IMG_DIR imageDirection) {
        this.imageDirection = imageDirection;
        this.centerCoordinate = new Point(x, y);
        imageId = 0;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public IMG_DIR getImageDirection() {
        return imageDirection;
    }

    public void setImageDirection(IMG_DIR imageDirection) {
        this.imageDirection = imageDirection;
    }

    public int getImadeDirectionAngle() {
        int degree;
        switch (imageDirection) {
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
                degree = 0;
                break;
        }
        return degree;
    }

    public Point getCenterCoordinate() {
        return centerCoordinate;
    }

    public void setCenterCoordinate(Point centerCoordinate) {
        this.centerCoordinate = centerCoordinate;
    }

    public Point getBottomLeftCoordinate() {
        return new Point(centerCoordinate.x - ArenaConst.OBS_SIZE / 2,
                centerCoordinate.y - ArenaConst.OBS_SIZE / 2);
    }

    public int getX() {
        return centerCoordinate.x;
    }

    public int getY() {
        return centerCoordinate.y;
    }
}
