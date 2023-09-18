package algorithms;

public class Turn extends MoveInterface {

    private final double turnRadiusX;
    private final double turnRadiusY;
    private final boolean leftTurn;

    public Turn(double x1, double y1, double x2, double y2, int dirInDegrees, double turnRadiusX,
                double turnRadiusY, boolean isLine, boolean leftTurn) {
        super(x1, y1, x2, y2, dirInDegrees, isLine, false);
        this.turnRadiusX = turnRadiusX;
        this.turnRadiusY = turnRadiusY;
        this.leftTurn = leftTurn;
    }


    public double getTurnRadiusX() {
        return turnRadiusX;
    }


    public double getTurnRadiusY() {
        return turnRadiusY;
    }


    public boolean isLeftTurn() {
        return leftTurn;
    }


    @Override
    public double getLength() {
        return 2 * Math.PI * turnRadiusX * 0.25;
    }


    @Override
    public String toString() {
        if (leftTurn) {
            return "turnLeft, " + super.toString();
        } else {
            return "turnRight, " + super.toString();
        }
    }
}
