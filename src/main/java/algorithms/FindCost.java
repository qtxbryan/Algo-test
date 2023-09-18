package algorithms;

import map.ArenaMap;
import map.Obstacle;
import robot.Bot;
import robot.BotConst;

import java.util.ArrayList;


public class FindCost {


    public double getPathCost(int[] path, ArrayList<Obstacle> list, PlanPath algo,
                              ArenaMap arenaMap) {

        Obstacle next;
        Bot bot = arenaMap.getRobot();
        int startX = bot.getX();
        int startY = bot.getY();
        int startAngle = bot.getRobotDir();
        double cost;
        algo.constructMap();
        for (int i : path) {
            next = list.get(i);
            algo.planPath(startX, startY, startAngle, next.getX(), next.getY(),
                    next.getImadeDirectionAngle(), true, false, false);

            int[] coords = algo.getFinalPosition();

            startX = coords[0];
            startY = coords[1];
            startAngle = coords[2];
        }
        cost = algo.getTotalCost();
        algo.clearCost();
        bot.setCentre(BotConst.ROBOT_INITIAL_CENTER_COORDINATES);
        bot.setDir(BotConst.ROBOT_DIRECTION.NORTH);
        return cost;
    }
}
