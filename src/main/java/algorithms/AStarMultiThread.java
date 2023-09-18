package algorithms;

import map.ArenaMap;
import map.Obstacle;

import java.util.ArrayList;
import java.util.stream.IntStream;


public class AStarMultiThread implements Runnable {

    private final int[] permutation;
    private final ArenaMap arenaMap;
    private double totalCost;

    public AStarMultiThread(int[] Permutation, ArenaMap arenaMap) {
        this.permutation = Permutation;
        this.arenaMap = arenaMap;
    }


    public void run() {
        ArrayList<Obstacle> list = ArenaMap.getObstacles();
        PlanPath algo = new PlanPath(arenaMap);
        int[] indexArray = IntStream.range(0, list.size()).toArray();
        try {
            FindCost pathing = new FindCost();
            totalCost = pathing.getPathCost(permutation, list, algo, arenaMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getTotalCost() {
        return totalCost;
    }

    public int[] getPath() {
        return permutation;
    }
}
