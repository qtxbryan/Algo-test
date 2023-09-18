package algorithms;

public class StraightLine extends MoveInterface {

    public StraightLine(double x1, double y1, double x2, double y2, int dirInDegrees, boolean isLine,
                        boolean isReversing) {
        super(x1, y1, x2, y2, dirInDegrees, isLine, isReversing);
    }

    @Override
    public double getLength() {
        double x1 = getCurX();
        double y1 = getCurY();
        double x2 = getNewX();
        double y2 = getNewY();

        return Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    }

    @Override
    public String toString() {
        return "Line: " + super.toString();
    }
}
