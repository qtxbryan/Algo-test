package GUI;

import javafx.animation.Interpolator;

public class Reverse extends Interpolator {

    private final Interpolator reverse;

    public Reverse(Interpolator reverse) {
        if (reverse == null) {
            throw new IllegalArgumentException();
        }
        this.reverse = reverse;
    }

    public static Interpolator reverse(Interpolator interpolator) {
        return (interpolator instanceof Reverse)
                ? ((Reverse) interpolator).reverse
                : new Reverse(interpolator);
    }

    @Override
    protected double curve(double t) {
        return reverse.interpolate(0d, 1d, 1 - t);
    }
}
