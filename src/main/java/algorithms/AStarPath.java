package algorithms;

import map.ArenaMap;
import map.Obstacle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;


public class AStarPath {

    private final ArenaMap arenaMap; // map of arena with obstacles and bot

    /*
    AStarPath
    - initialise arena map
     */
    public AStarPath(ArenaMap arenaMap) {
        this.arenaMap = arenaMap;
    }


    /*
    AStarPath()
    - uses multithreading to find path cost of each permutation, then returns shortest path
    - indexArray: obstacles indexes
    - permutation: possible sequence for visiting obstacles
     */
    public int[] AStarPath(boolean good) {
        ArrayList<Obstacle> list = ArenaMap.getObstacles();
        int[] indexArray = IntStream.range(0, list.size()).toArray();
        List<int[]> permutations = getPermutations(
                indexArray);
        double smallestCost = Double.MAX_VALUE;
        int[] shortestPath = permutations.get(0);
        double largestCost = Double.MAX_VALUE;
        int[] otherPath = permutations.get(0);
        int numOfThreads = 6;

        int size = permutations.size();
        for (int i = 0; i < size; i += numOfThreads) {

            Thread[] threads = new Thread[numOfThreads];
            AStarMultiThread[] runnables = new AStarMultiThread[numOfThreads];
            for (int j = 0; j < numOfThreads; j++) {
                if (i + j < size) {
                    runnables[j] = new AStarMultiThread(permutations.get(i + j), arenaMap);
                    threads[j] = new Thread(runnables[j]);
                    threads[j].start();
                }
            }

            for (int j = 0; j < numOfThreads; j++) {
                if (i + j < size) {
                    try {
                        threads[j].join();
                        if (runnables[j].getTotalCost() < smallestCost) {
                            smallestCost = runnables[j].getTotalCost();
                            shortestPath = runnables[j].getPath();
                        }
                        if (runnables[j].getTotalCost() > largestCost) {
                            largestCost = runnables[j].getTotalCost();
                            otherPath = runnables[j].getPath();
                        }
                    } catch (Exception e) {
                        System.out.println("Exception occurred while joining thread: " + e);
                    }
                }
            }
        }

        //Test code
        System.out.println(shortestPath);

        System.out.println("Shortest path cost: " + smallestCost);
        if (good) {
            return shortestPath;
        }
        else {
            return otherPath;
        }
    }


    /*
    getPermutations()
    - returns possible sequences for visiting obstacles
    - obstacle indexes are labelled as nodes
     */
    private List<int[]> getPermutations(int[] nodes) {
        List<int[]> permutations = new ArrayList<>();
        allPermutations(nodes, permutations, nodes.length);
        return permutations;
    }


    /*
    swap()
    - swap items in array
     */
    private void swap(int[] array, int index1, int index2) {
        int temp = array[index1];
        array[index1] = array[index2];
        array[index2] = temp;
    }


    /*
    allPermutations()
    - uses recursion to add permutations to list
     */
    private void allPermutations(int[] permutation, List<int[]> permutations, int n) {
        if (n <= 0) {
            permutations.add(permutation);
            return;
        }
        int[] tempPermutation = Arrays.copyOf(permutation, permutation.length);
        for (int i = 0; i < n; i++) {
            swap(tempPermutation, i, n - 1);
            allPermutations(tempPermutation, permutations, n - 1);
            swap(tempPermutation, i, n - 1);
        }
    }
}
