package map;

public class Grid {

    private final int x;
    private final int y;
    private final int dim;
    private boolean isPicture;
    private boolean hasBeenVisited;
    private int pictureId;
    private boolean isVirtualObstacle;
    private double hCost;
    private double gCost;
    private double totalCost;

    public Grid(boolean isPicture, boolean isVirtualObstacle, int x, int y, int dim) {
        this.isPicture = isPicture;
        this.isVirtualObstacle = isVirtualObstacle;
        this.x = x;
        this.y = y;
        this.dim = dim;
        this.hasBeenVisited = false;
        this.gCost = 0;
        this.hCost = 0;
        this.totalCost = 0;
    }

    public void setHasBeenVisited(boolean hasBeenVisited) {
        this.hasBeenVisited = hasBeenVisited;
    }

    public void setCost(double hCost, double gCost) {
        this.hCost = hCost;
        this.gCost = gCost;
        this.totalCost = hCost + gCost;
    }

    public boolean isVisited() {
        return hasBeenVisited;
    }

    public boolean isPicture() {
        return isPicture;
    }

    public void setPicture(boolean picture) {
        isPicture = picture;
    }

    public boolean isVirtualObstacle() {
        return isVirtualObstacle;
    }

    public void setVirtualObstacle(boolean obstacle) {
        isVirtualObstacle = obstacle;
    }

    public int getPictureId() {
        return pictureId;
    }

    public void setPictureId(int pictureId) {
        this.pictureId = pictureId;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDim() {
        return dim;
    }

    public double getCost() {
        return totalCost;
    }

    public double getGCost() {
        return gCost;
    }

    @Override
    public String toString() {
        return "Node{" +
                "x=" + x +
                ", y=" + y +
                ", d=" + dim +
                '}';
    }
}
