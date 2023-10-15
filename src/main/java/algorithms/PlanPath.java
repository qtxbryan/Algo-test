package algorithms;

import map.*;
import robot.BotConst;

import java.util.*;

import static robot.BotConst.ROBOT_VIRTUAL_WIDTH;


public class PlanPath {

        private final ArenaMap arenaMap;
        private final int gridCount = (ArenaConst.ARENA_SIZE / ArenaConst.OBS_SIZE)
                + ArenaConst.BORDER_SIZE * 2;


        private final Map<Grid, Grid> visitedMap;
        private final PriorityQueue<Grid> priorityQueue;
        private Grid[][][] grid;
        private int[] finalPosition;
        private ArrayList<Grid> gridPathing;
        private Grid curGrid;
        private double totalCost = 0;

        public PlanPath(ArenaMap arenaMap) {
            this.arenaMap = arenaMap;
            this.visitedMap = new HashMap<>();
            int robotX = arenaMap.getRobot().getX();
            int robotY = arenaMap.getRobot().getY();
            int robotDirection = arenaMap.getRobot().getRobotDir();
            finalPosition = new int[]{robotX, robotY, robotDirection};
            this.priorityQueue = new PriorityQueue<>(new GridCustomComparator());
        }

        public int[] getFinalPosition() {
            return finalPosition;
        }

        /*

         */
        public void clear() {
            visitedMap.clear();
            constructMap();
            int numCells = (ArenaConst.ARENA_SIZE / ArenaConst.OBS_SIZE)
                    + ArenaConst.BORDER_SIZE * 2;

            for (int i = 0; i < numCells; i++) {
                for (int j = 0; j < numCells; j++) {
                    for (int k = 0; k < 4; k++) {
                        grid[j][i][k].setCost(BotConst.MAX_COST, BotConst.MAX_COST);
                    }
                }
            }
            priorityQueue.clear();
        }

        /*
        viableGrid()
        - returns True if no obstacle and not visited yet
         */
        private boolean viableGrid(Grid grid) {
            return !grid.isPicture() && !grid.isVirtualObstacle() && !grid.isVisited();
        }


        /*
        maxTurnSizeX()
        - returns max turn radius for X-axis in grid units (assuming North)
         */
        private int maxTurnSizeX() {
            double gridSize = ArenaConst.OBS_SIZE;

            double largestRadius = Math.max(BotConst.LEFT_TURN_RADIUS_X,
                    BotConst.RIGHT_TURN_RADIUS_X);

            return (int) Math.ceil(largestRadius / gridSize);
        }

        /*
        maxTurnSizeX()
        - returns max turn radius for Y-axis in grid units (assuming North)
         */
        private int maxTurnSizeY() {
            double gridSize = ArenaConst.OBS_SIZE;

            double largestRadius = Math.max(BotConst.LEFT_TURN_RADIUS_Y,
                    BotConst.RIGHT_TURN_RADIUS_Y);

            return (int) Math.ceil(largestRadius / gridSize);
        }


        /*
        getStopPosition()
        - returns position to stop at before obstacle
         */
        private int[] getStopPosition(int x, int y, int dir) {
            int dist = AlgConst.DISTANCE_AWAY_FROM_IMAGE;
            int[] coords = new int[3];
            switch (dir) {
                case 0:
                    coords[0] = x + dist;
                    coords[1] = y;
                    coords[2] = 180;
                    break;
                case 90:
                    coords[0] = x;
                    coords[1] = y - dist;
                    coords[2] = 270;
                    break;
                case 180:
                    coords[0] = x - dist;
                    coords[1] = y;
                    coords[2] = 0;
                    break;
                case 270:
                    coords[0] = x;
                    coords[1] = y + dist;
                    coords[2] = 90;
                    break;
                default:
                    return null;
            }
            return coords;
        }


        /*
        planPath()
        - returns shortest path to take using A*
         */
        public ArrayList<MoveInterface> planPath(int startX, int startY, int startAngle, int pictureX,
                                                 int pictureY,
                                                 int pictureDirInDegrees, boolean isPicturePos, boolean doBacktrack, boolean print) {

            // Check out of bounds
            if (0 > startX || startX >= gridCount || 0 > startY
                    || startY >= gridCount) {
                this.totalCost += 9999;
                return null;
            }
            clear();

            int endX, endY, endAngleDimension;
            ArrayList<MoveInterface> path = null;
            boolean goalFound = false;

            if (isPicturePos) {
                int[] goal = getStopPosition(pictureX, pictureY, pictureDirInDegrees);
                endX = goal[0];
                endY = goal[1];
                int endDirInDegrees = goal[2];
                endAngleDimension = angleToDimension(
                        endDirInDegrees);

            } else {
                endX = pictureX;
                endY = pictureY;
                endAngleDimension = angleToDimension(pictureDirInDegrees);
            }

            if (!isValidLocation(endX, endY, endAngleDimension)) {
                return null;
            }

            Grid goalGrid = grid[endY][endX][endAngleDimension];
            int maxTurnCountX = maxTurnSizeX();

            int maxTurnCountY = maxTurnSizeY();

            int x, y, dim;
            Grid nextGrid;
            int[] forwardLocation, leftLocation, rightLocation, backwardLocation;
            int nextX, nextY, nextDim, currentTurnCount;
            double currentGCost, hCost, gCost;

            int angleDimension = angleToDimension(startAngle);

            this.curGrid = grid[startY][startX][angleDimension];
            this.priorityQueue.add(curGrid);
            grid[startY][startX][angleDimension].setCost(0, 0);

            while (!goalFound && !priorityQueue.isEmpty()) {
                curGrid = priorityQueue.remove();

                x = curGrid.getX();
                y = curGrid.getY();
                dim = curGrid.getDim();
                currentGCost = curGrid.getGCost();

                if (curGrid == goalGrid) {
                    goalFound = true;
                    finalPosition = new int[]{x, y, dim * 90};
                    break;
                }

                forwardLocation = getForwardNode(x, y, dim);
                leftLocation = getLeftNode(x, y, dim, maxTurnCountX, maxTurnCountY);
                rightLocation = getRightNode(x, y, dim, maxTurnCountX, maxTurnCountY);
                backwardLocation = getBackwardNode(x, y, dim);
                if (forwardLocation != null) {
                    nextX = forwardLocation[0];
                    nextY = forwardLocation[1];
                    nextDim = forwardLocation[2];

                    nextGrid = grid[nextY][nextX][nextDim];

                    gCost = currentGCost + greedy(curGrid, nextGrid);
                    hCost = heuristic(curGrid, goalGrid, endAngleDimension);

                    if (gCost < nextGrid.getGCost()) {
                        visitedMap.put(nextGrid, curGrid);
                        nextGrid.setCost(hCost,
                                gCost);
                        priorityQueue.add(nextGrid);
                    }
                }
                if (backwardLocation != null) {
                    nextX = backwardLocation[0];
                    nextY = backwardLocation[1];
                    nextDim = backwardLocation[2];

                    nextGrid = grid[nextY][nextX][nextDim];

                    gCost = currentGCost + greedy(curGrid, nextGrid);
                    hCost = heuristic(curGrid, goalGrid, endAngleDimension);

                    if (gCost < nextGrid.getGCost()) {
                        visitedMap.put(nextGrid, curGrid);
                        nextGrid.setCost(hCost,
                                gCost);
                        priorityQueue.add(nextGrid);
                    }
                }
                if (leftLocation != null) {
                    nextX = leftLocation[0];
                    nextY = leftLocation[1];
                    nextDim = leftLocation[2];

                    nextGrid = grid[nextY][nextX][nextDim];

                    gCost = currentGCost + greedy(curGrid, nextGrid);
                    hCost = heuristic(curGrid, goalGrid, endAngleDimension);

                    if (gCost < nextGrid.getGCost()) {
                        visitedMap.put(nextGrid, curGrid);
                        nextGrid.setCost(hCost,
                                gCost);
                        priorityQueue.add(nextGrid);
                    }
                }
                if (rightLocation != null) {
                    nextX = rightLocation[0];
                    nextY = rightLocation[1];
                    nextDim = rightLocation[2];

                    nextGrid = grid[nextY][nextX][nextDim];

                    gCost = currentGCost + greedy(curGrid, nextGrid);
                    hCost = heuristic(curGrid, goalGrid, endAngleDimension);

                    if (gCost < nextGrid.getGCost()) {
                        visitedMap.put(nextGrid, curGrid);
                        nextGrid.setCost(hCost,
                                gCost);
                        priorityQueue.add(nextGrid);
                    }
                }
                curGrid.setHasBeenVisited(true);
            }

            if (!goalFound) {
                this.totalCost += 9999;
                return null;
            }
            if (doBacktrack) {
                path = backtrack(goalGrid, print);
            }
            if (print && doBacktrack) {
                System.out.println("Total cost: " + goalGrid.getGCost());
                System.out.println("Nodes expanded: " + visitedMap.size());
            }
            this.totalCost += goalGrid.getGCost();
            return path;
        }


        public void clearCost() {
            this.totalCost = 0;
        }

        public double getTotalCost() {
            return totalCost;
        }

        /*
        getForwardNode()
        - returns node after taking forward step
         */
        private int[] getForwardNode(int x, int y, int dim) {
            int[] pair;
            switch (dim) {
                case 0:
                    pair = new int[]{x + 1, y, dim};
                    break;
                case 1:
                    pair = new int[]{x, y - 1, dim};
                    break;
                case 2:
                    pair = new int[]{x - 1, y, dim};
                    break;
                case 3:
                    pair = new int[]{x, y + 1, dim};
                    break;
                default:
                    pair = null;
                    break;
            }
            if (pair != null && isValidLocation(pair[0], pair[1], dim)) {
                return pair;
            } else {
                return null;
            }
        }

        /*
        getBackwardNode()
        - returns node after taking backward step
         */
        private int[] getBackwardNode(int x, int y, int dim) {
            int[] pair;
            switch (dim) {
                case 0:
                    pair = new int[]{x - 1, y, dim};
                    break;
                case 1:
                    pair = new int[]{x, y + 1, dim};
                    break;
                case 2:
                    pair = new int[]{x + 1, y, dim};
                    break;
                case 3:
                    pair = new int[]{x, y - 1, dim};
                    break;
                default:
                    pair = null;
                    break;
            }
            if (pair != null && isValidLocation(pair[0], pair[1], dim)) {
                return pair;
            } else {
                return null;
            }
        }

        /*
        getLeftNode()
        - returns node after making left turn
         */
        private int[] getLeftNode(int x, int y, int dim, int maxTurnCountX, int maxTurnCountY) {
            int[] pair;
            int[] check;
            int nextPosLeft = maxTurnCountX - 1;
            int nextPosStraight = maxTurnCountY - 1;
            switch (dim) {
                case 0:
                    pair = new int[]{x + nextPosStraight, y - nextPosLeft, 1};
                    check = new int[]{x + nextPosStraight - 1, y - 1, 1};
                    break;
                case 1:
                    pair = new int[]{x - nextPosLeft, y - nextPosStraight, 2};
                    check = new int[]{x - 1, y - nextPosStraight + 1, 1};
                    break;
                case 2:
                    pair = new int[]{x - nextPosStraight, y + nextPosLeft, 3};
                    check = new int[]{x - nextPosStraight + 1, y + 1, 3};
                    break;
                case 3:
                    pair = new int[]{x + nextPosLeft, y + nextPosStraight, 0};
                    check = new int[]{x + 1, y + nextPosStraight - 1, 0};
                    break;
                default:
                    pair = null;
                    check = null;
                    break;
            }
            if (pair != null && isValidTurnLocation(pair[0], pair[1], pair[2]) && isValidTurnLocation(check[0],
                    check[1], check[2])) {
                return pair;
            } else {
                return null;
            }
        }

        /*
        getRightNode()
        - returns node after making right turn
         */
        private int[] getRightNode(int x, int y, int dim, int maxTurnCountX, int maxTurnCountY) {
            int[] pair;
            int[] check;
            int[] check2;
            int nextPosRight = maxTurnCountX - 1;
            int nextPosStraight = maxTurnCountY - 1;
            switch (dim) {
                case 0:
                    pair = new int[]{x + nextPosStraight, y + nextPosRight, 3};
                    check = new int[]{x + nextPosStraight - 1, y + 1, 3};
                    break;
                case 1:
                    pair = new int[]{x + nextPosRight, y - nextPosStraight, 0};
                    check = new int[]{x + 1, y - nextPosStraight + 1, 0};
                    break;
                case 2:
                    pair = new int[]{x - nextPosStraight, y - nextPosRight, 1};
                    check = new int[]{x - nextPosStraight + 1, y - 1, 1};
                    break;
                case 3:
                    pair = new int[]{x - nextPosRight, y + nextPosStraight, 2};
                    check = new int[]{x - 1, y + nextPosStraight - 1, 2};
                    break;
                default:
                    pair = null;
                    check = null;
                    break;
            }
            if (pair != null && isValidTurnLocation(pair[0], pair[1], pair[2]) && isValidTurnLocation(check[0],
                    check[1], check[2])) {
                return pair;
            } else {
                return null;
            }
        }

        /*
        heuristic()
        - returns h for a*
         */
        private double heuristic(Grid n1, Grid n2, int endDim) {
            int abs1 = Math.abs(n1.getX() - n2.getX());
            int abs2 = Math.abs(n1.getY() - n2.getY());

            return (abs1 + abs2) * BotConst.MOVE_COST;
        }


        /*
        greedy()
        - returns cost of moving to next position
         */
        private double greedy(Grid n1, Grid n2) {
            int turnCost = 0;
            int cost = BotConst.MOVE_COST;

            if (n1.getDim() != n2.getDim()) {
                turnCost = BotConst.TURN_COST_90;
            } else {
                switch (n1.getDim()) {
                    case 0:
                        if (n1.getX() > n2.getX()) {
                            cost = BotConst.REVERSE_COST;
                        }
                        break;
                    case 1:
                        if (n1.getY() > n2.getY()) {
                            cost = BotConst.REVERSE_COST;
                        }
                        break;
                    case 2:
                        if (n1.getX() < n2.getX()) {
                            cost = BotConst.REVERSE_COST;
                        }
                        break;
                    case 3:
                        if (n1.getY() < n2.getY()) {
                            cost = BotConst.REVERSE_COST;
                        }
                        break;
                }
            }

            return cost + turnCost;
        }


        /*
        backtrack()
        - returns list of all path segments of the final path
         */
        private ArrayList<MoveInterface> backtrack(Grid end, boolean print) {
            Grid curr, prev;
            ArrayList<Grid> path = new ArrayList<>();
            ArrayList<MoveInterface> pathSegments = new ArrayList<>();
            path.add(end);
            curr = end;
            int midpoint = ArenaConst.OBS_SIZE / 2;
            int diffX = maxTurnSizeX() * ArenaConst.OBS_SIZE - ArenaConst.OBS_SIZE;
            int diffY = maxTurnSizeY() * ArenaConst.OBS_SIZE - ArenaConst.OBS_SIZE;
            double[] lineEnd = new double[]{end.getX() * ArenaConst.OBS_SIZE + midpoint,
                    end.getY() * ArenaConst.OBS_SIZE
                            + midpoint};

            double[] lineStart = new double[2];

            int prevDir;
            int currDir;
            double radiusX, radiusY;
            boolean turnLeft;
            int dirInDegrees;
            boolean reversing;
            while (curr != null) {
                reversing = false;
                path.add(curr);
                prev = visitedMap.get(curr);
                currDir = curr.getDim();
                if (prev == null) {
                    lineStart[0] = curr.getX() * ArenaConst.OBS_SIZE + midpoint;
                    lineStart[1] = curr.getY() * ArenaConst.OBS_SIZE + midpoint;
                    switch (currDir) {
                        case 0:
                            if (lineEnd[0] < lineStart[0]) {
                                reversing = true;
                            }
                            break;
                        case 1:
                            if (lineEnd[1] > lineStart[1]) {
                                reversing = true;
                            }
                            break;
                        case 2:
                            if (lineEnd[0] > lineStart[0]) {
                                reversing = true;
                            }
                            break;
                        case 3:
                            if (lineEnd[1] < lineStart[1]) {
                                reversing = true;
                            }
                            break;
                        default:
                    }
                    pathSegments.add(
                            new StraightLine(lineStart[0], lineStart[1], lineEnd[0], lineEnd[1], currDir * 90, true,
                                    reversing));
                } else if (prev.getDim()
                        != currDir) {

                    prevDir = prev.getDim();
                    lineStart[0] = curr.getX() * ArenaConst.OBS_SIZE + midpoint;
                    lineStart[1] = curr.getY() * ArenaConst.OBS_SIZE + midpoint;

                    dirInDegrees = prev.getDim() * 90;
                    if ((dirInDegrees + 90) % 360 == curr.getDim() * 90) {
                        turnLeft = true;
                        radiusX = BotConst.LEFT_TURN_RADIUS_X;
                        radiusY = BotConst.LEFT_TURN_RADIUS_Y;
                    } else {
                        turnLeft = false;
                        radiusX = BotConst.RIGHT_TURN_RADIUS_X;
                        radiusY = BotConst.RIGHT_TURN_RADIUS_Y;
                    }

                    switch (currDir) {
                        case 0:
                            lineStart[0] -= diffX - radiusX;
                            if (lineEnd[0] < lineStart[0]) {
                                reversing = true;
                            }
                            break;
                        case 1:
                            lineStart[1] += diffX - radiusX;
                            if (lineEnd[1] > lineStart[1]) {
                                reversing = true;
                            }
                            break;
                        case 2:
                            lineStart[0] += diffX - radiusX;
                            if (lineEnd[0] > lineStart[0]) {
                                reversing = true;
                            }
                            break;
                        case 3:
                            lineStart[1] -= diffX - radiusX;
                            if (lineEnd[1] < lineStart[1]) {
                                reversing = true;
                            }
                            break;
                        default:
                    }
                    pathSegments.add(
                            new StraightLine(lineStart[0], lineStart[1], lineEnd[0], lineEnd[1], dirInDegrees, true,
                                    reversing));

                    lineEnd[0] = prev.getX() * ArenaConst.OBS_SIZE + midpoint;
                    lineEnd[1] = prev.getY() * ArenaConst.OBS_SIZE + midpoint;
                    switch (prevDir) {
                        case 0:
                            lineEnd[0] += diffY - radiusY;
                            break;
                        case 1:
                            lineEnd[1] -= diffY - radiusY;
                            break;
                        case 2:
                            lineEnd[0] -= diffY - radiusY;
                            break;
                        case 3:
                            lineEnd[1] += diffY - radiusY;
                            break;
                        default:
                    }
                    pathSegments.add(
                            new Turn(lineEnd[0], lineEnd[1], lineStart[0], lineStart[1], dirInDegrees, radiusX,
                                    radiusY, false, turnLeft));
                }
                curr = prev;
            }
            Collections.reverse(path);
            if (print) {
                printPath(path);
            }
            gridPathing = path;
            Collections.reverse(pathSegments);

            for (MoveInterface i : pathSegments) {
                System.out.println(i.toString());
            }

            return pathSegments;
        }

        public ArrayList<Grid> getNodePath() {
            return gridPathing;
        }

        /*
        isValidTurnLocation()
        - returns True is aft/before turn position if valid, no obstacles, etc.
         */
        private boolean isValidTurnLocation(int x, int y, int dim) {
//          open arena
        //    if (x >= ArenaConst.BORDER_SIZE && x <= gridCount - ArenaConst.BORDER_SIZE - 1) {
        //        if (y >= ArenaConst.BORDER_SIZE
        //                && y <= gridCount - ArenaConst.BORDER_SIZE - 1) {
//          closed arena
            if (x > ArenaConst.BORDER_SIZE && x < gridCount - ArenaConst.BORDER_SIZE - 1) {
                if (y > ArenaConst.BORDER_SIZE
                        && y < gridCount - ArenaConst.BORDER_SIZE - 1) {
                    int overlap = 0;
                    for (int x_test = 0; x_test < 3; x_test++) {
                        for (int y_test = 0; y_test < 3; y_test++) {
//                          try this
//                            if (x + x_test - 1 >= ArenaConst.BORDER_SIZE
//                                    && x + x_test - 1 <= gridCount - ArenaConst.BORDER_SIZE - 1) {
//                                if (y + y_test - 1 >= ArenaConst.BORDER_SIZE
//                                        && y + y_test - 1 <= gridCount - ArenaConst.BORDER_SIZE - 1) {
                                    Grid n = grid[y + y_test - 1][x + x_test - 1][dim];
                                    if (n.isPicture()) {
                                        return false;
                                    } else if (x_test == 1 && y_test == 1 && n.isVisited()) {
                                        return false;
                                    } else if (n.isVirtualObstacle()) {
                                        overlap += 1;
                                    }
//                                }
//                            }
//                          if (!viableGrid(n)) {
//                              overlap += 1;
//                          }
                        }
                    }
                    return overlap <= 1;
                }
            }
            return false;
        }

    /*
    isValidLocation()
    - returns True is position if valid, no obstacles, etc.
     */
    private boolean isValidLocation(int x, int y, int dim) {
//        open arena
    //    if (x >= ArenaConst.BORDER_SIZE && x <= gridCount - ArenaConst.BORDER_SIZE - 1) {
    //        if (y >= ArenaConst.BORDER_SIZE
    //                && y <= gridCount - ArenaConst.BORDER_SIZE - 1) {

//        closed arena
                if (x > ArenaConst.BORDER_SIZE && x < gridCount - ArenaConst.BORDER_SIZE - 1) {
                    if (y > ArenaConst.BORDER_SIZE
                            && y < gridCount - ArenaConst.BORDER_SIZE - 1) {

                    Grid n = grid[y][x][dim];
                    return viableGrid(n);
            }
        }
        return false;
    }

        /*
        constructMap()
        - construct map with obstacle positions
         */
        public void constructMap() {
            ArrayList<Obstacle> obstacleList = ArenaMap.getObstacles();

            grid = new Grid[gridCount][gridCount][4];

            for (int i = 0; i < gridCount; i++) {
                for (int j = 0; j < gridCount; j++) {
                    for (int k = 0; k < 4; k++) {
                        grid[i][j][k] = new Grid(false, false, j, i, k);
                    }
                }
            }

            int angleDimension, x, y, id;

            for (Obstacle pictures : obstacleList) {
                x = pictures.getX();
                y = pictures.getY();
                id = obstacleList.indexOf(pictures);
                angleDimension = angleToDimension(
                        pictures.getImadeDirectionAngle());

                grid[y][x][angleDimension].setPicture(true);
                grid[y][x][angleDimension].setPictureId(id);
                for (int i = 0; i < 4; i++) {
                    grid[y][x][i].setVirtualObstacle(true);
                }
                int[][] pairs = getVirtualObstaclePairs(x, y, AlgConst.BOUNDARY);
                int xVirtual, yVirtual;

                for (int[] pair : pairs) {
                    xVirtual = pair[0];
                    yVirtual = pair[1];
                    for (int i = 0; i < 4; i++) {
                        if (xVirtual >= 0 && xVirtual < gridCount && yVirtual >= 0
                                && yVirtual < gridCount) {

                            grid[yVirtual][xVirtual][i].setVirtualObstacle(true);
                        }
                    }
                }
            }

            int numCells = (ArenaConst.ARENA_SIZE / ArenaConst.OBS_SIZE)
                    + ArenaConst.BORDER_SIZE * 2;

            for (int i = 0; i < numCells; i++) {
                for (int j = 0; j < numCells; j++) {
                    for (int k = 0; k < 4; k++) {
                        grid[j][i][k].setCost(BotConst.MAX_COST, BotConst.MAX_COST);
                    }
                }
            }
            grid[finalPosition[1]][finalPosition[0]][finalPosition[2] / 90].setCost(0, 0);
        }

        /*
        angleToDimension()
        - return dim from angle
        - dim 0: east, 1: north, 2: west, 3: south I THINK
         */
        private int angleToDimension(int angle) {
            return angle / 90;
        }

        /*
        getVirtualObstaclePairs()
        - returns [x,y] for all positions within obstacle on arena map
         */
        private int[][] getVirtualObstaclePairs(int x, int y, int thickness) {
            int numCol = 1 + 2 * thickness;
            int numPairs = numCol * numCol - 1;

            int[][] pairArray = new int[numPairs][];
            int[][] coordinateArray = new int[numCol][numCol];
            int dim = coordinateArray.length;
            int relativeCenter = dim / 2;
            int counter = 0;
            for (int y1 = 0; y1 < dim; y1++) {
                for (int x1 = 0; x1 < dim; x1++) {
                    if (x1 != relativeCenter || y1 != relativeCenter) {
                        pairArray[counter] = new int[]{x + x1 - thickness, y + y1 - thickness};
                        counter++;
                    }
                }
            }
            return pairArray;
        }

        public void printPath(List<Grid> path) {

            char[][] printArray = new char[gridCount][gridCount];
            for (int y = 0; y < gridCount; y++) {
                for (int x = 0; x < gridCount; x++) {
                    if (grid[y][x][0].isPicture()) {
                        printArray[y][x] = 'E';
                    } else if (grid[y][x][1].isPicture()) {
                        printArray[y][x] = 'N';
                    } else if (grid[y][x][2].isPicture()) {
                        printArray[y][x] = 'W';
                    } else if (grid[y][x][3].isPicture()) {
                        printArray[y][x] = 'S';
                    } else if (grid[y][x][0].isVirtualObstacle()) {
                        printArray[y][x] = '/';
                    } else {
                        printArray[y][x] = '-';
                    }
                }
            }
            for (Grid n : path) {
                int dir = n.getDim();
                switch (dir) {
                    case 0:
                        printArray[n.getY()][n.getX()] = '>';
                        break;
                    case 1:
                        printArray[n.getY()][n.getX()] = '^';
                        break;
                    case 2:
                        printArray[n.getY()][n.getX()] = '<';
                        break;
                    case 3:
                        printArray[n.getY()][n.getX()] = 'v';
                        break;
                    default:
                        printArray[n.getY()][n.getX()] = 'x';
                        break;
                }
                if (n.getX() == arenaMap.getRobot().getX() && n.getY() == arenaMap.getRobot().getY()) {
                    printArray[n.getY()][n.getX()] = 'R';
                }
            }
            printArray[path.get(0).getY()][path.get(0).getX()] = 'R';

            for (int y = 0; y < gridCount; y++) {
                for (int x = 0; x < gridCount; x++) {
                    System.out.print(printArray[y][x] + "  ");
                }
                System.out.println();
            }
        }

        /*
        getReversePos()
        - returns position after reversing
         */
        public int[] getReversePos(int x, int y, int dim) {
            int[] pair;
            switch (dim) {
                case 0:
                    pair = new int[]{x - 2, y, dim};
                    break;
                case 1:
                    pair = new int[]{x, y + 2, dim};
                    break;
                case 2:
                    pair = new int[]{x + 2, y, dim};
                    break;
                case 3:
                    pair = new int[]{x, y - 2, dim};
                    break;
                default:
                    pair = null;
                    break;
            }
            if (pair != null && canGo(pair[0], pair[1], dim)) {
                return pair;
            } else {
                return null;
            }
        }


        /*
        canGo()
        - returns True is position is valid - no obstacle/picture
         */
        private boolean canGo(int x, int y, int dim) {
//            open arena
            //    if (x >= 0 && x < gridCount && y >= 0 && y < gridCount) {
//            closed arena
                if (x > 0 && x < gridCount-1 && y > 0 && y < gridCount-1) {
//              ignore this
//                    for (int x_test = 0; x_test < 3; x_test++) {
//                        for (int y_test = 0; y_test < 3; y_test++) {
//                            Grid n = grid[y+y_test-1][x+x_test-1][dim];
//                            if (n.isPicture() || n.isVirtualObstacle()) {
//                                return false;
//                            }
//                        }
//                    }
//                    return true;
//              ignore until here
                    Grid n = grid[y][x][dim];
                    return !n.isPicture() && !n.isVirtualObstacle();
                } else {
                    return false;
                }
            }

}
