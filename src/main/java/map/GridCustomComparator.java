package map;

import java.util.Comparator;


public class GridCustomComparator implements Comparator<Grid> {

    @Override
    public int compare(Grid o1, Grid o2) {
        double c1 = o1.getCost();
        double c2 = o2.getCost();
        if (c1 < c2) {
            return -1;
        }
        if (c1 > c2) {
            return 1;
        }
        return 0;
    }
}

