package map;

import map.ArenaConst.IMG_DIR;
import robot.Bot;
import robot.BotConst;

import javax.swing.*;
import java.util.ArrayList;


public class ArenaMap extends JPanel {

    private static ArrayList<Obstacle> obstacles;
    private static Bot bot;

    public ArenaMap(Bot bot) {
        ArenaMap.bot = bot;
        System.out.printf("Bot at %d, %d\n", bot.getX(), bot.getY());
        obstacles = new ArrayList<>();
    }

    public static ArrayList<Obstacle> getObstacles() {
        return obstacles;
    }


    public boolean addPictureObstacle(int x, int y, IMG_DIR imageDirection) {
        int numGrids = (ArenaConst.ARENA_SIZE / ArenaConst.OBS_SIZE)
                + ArenaConst.BORDER_SIZE * 2;
        Obstacle obstacle = new Obstacle(x + ArenaConst.BORDER_SIZE,
                y + ArenaConst.BORDER_SIZE, imageDirection);
        if (x < 0 || x >= numGrids || y < 0 || y >= numGrids) {
            System.out.println("Position is out of bounds");
            return false;
        }
        if (overlapWithCar(obstacle)) {
            System.out.printf("Cannot add obstacle centered at <%d, %d> due to overlap with car\n", x, y);
            return false;
        }
        for (Obstacle i : obstacles) {
            if (overlapWithObstacle(i, obstacle)) {
                System.out.printf("Cannot add obstacle centered at <%d, %d> due to overlap with obstacle\n",
                        x, y);
                return false;
            }
        }
        obstacles.add(obstacle);
        System.out.printf("Added obstacle centered at <%d, %d>, with direction %c\n", x, y,
                IMG_DIR.print(imageDirection));
        return true;
    }


    private boolean overlapWithCar(Obstacle obstacle) {
        int minimumGap = (BotConst.ROBOT_VIRTUAL_WIDTH - ArenaConst.OBS_SIZE) / 2
                / ArenaConst.OBS_SIZE;

        return (Math.abs(obstacle.getX() - bot.getX()) < minimumGap + 1
                && Math.abs(obstacle.getY() - bot.getY()) < minimumGap + 1);
    }


    private boolean overlapWithObstacle(Obstacle obs1, Obstacle obs2) {
        return (obs1.getX() == obs2.getX()) && (obs1.getY() == obs2.getY());
    }

    public Bot getRobot() {
        return bot;
    }
}
