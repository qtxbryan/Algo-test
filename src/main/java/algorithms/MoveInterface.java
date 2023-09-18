package algorithms;

public abstract class MoveInterface {

    private final double curX;
    private final double curY;
    private final double newX;
    private final double newY;
    private final int dirDeg;
    private final boolean isStraightLineMovement;
    private boolean isBack;

    public MoveInterface(double curX, double curY, double newX, double newY, int dirDeg,
                         boolean isStraightLineMovement,
                         boolean isBack) {
        this.curX = curX;
        this.curY = curY;
        this.newX = newX;
        this.newY = newY;
        this.dirDeg = dirDeg;
        this.isStraightLineMovement = isStraightLineMovement;
        this.isBack = isBack;
    }

    public abstract double getLength();

    public double getCurX() {
        return curX;
    }

    public boolean isBack() {
        return isBack;
    }

    public void setBack(boolean back) {
        isBack = back;
    }

    public boolean isStraightLineMovement() {
        return isStraightLineMovement;
    }

    public int getDirDeg() {
        return dirDeg;
    }

    public double getCurY() {
        return curY;
    }

    public double getNewX() {
        return newX;
    }

    public double getNewY() {
        return newY;
    }

    @Override
    public String toString() {
        return "<" + curX + ", " + curY + ">, <"
                + newX + ", " + newY + ">, rev = " + isBack;
    }

}
