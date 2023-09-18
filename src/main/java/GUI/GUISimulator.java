package GUI;

import algorithms.AStarPath;
import algorithms.MoveInterface;
import algorithms.PlanPath;
import algorithms.Turn;
import java.awt.Point;
import java.util.ArrayList;
import java.util.function.UnaryOperator;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.PathTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.util.converter.IntegerStringConverter;
import map.ArenaConst;
import map.ArenaConst.IMG_DIR;
import map.ArenaMap;
import robot.Bot;
import robot.BotConst;


public class GUISimulator extends Application {

    private static Bot bot;
    private static AStarPath aStarPath;
    private static PlanPath planPath;
    private static ArenaMap arenaMap = null;
    private final int width = ArenaConst.ARENA_SIZE;
    private final int scale = Integer.valueOf(GUIConstant.SIZE);
    private final int size = width * scale;
    private final int arenaSIze = size / ((ArenaConst.ARENA_SIZE / ArenaConst.OBS_SIZE)
            + ArenaConst.BORDER_SIZE * 2);
    private final ArrayList<Obstacle> obstacleArrayList = new ArrayList<>();
    private Timeline timeline;
    private double timeSeconds = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        bot = new Bot(BotConst.ROBOT_INITIAL_CENTER_COORDINATES,
                BotConst.ROBOT_DIRECTION.NORTH, false);
        arenaMap = new ArenaMap(bot);
        aStarPath = new AStarPath(arenaMap);
        planPath = new PlanPath(arenaMap);

        Pane gridPane = new Pane();
        gridPane.setMinWidth(size);
        gridPane.setMinHeight(size);

        gridPane.setBackground(
                new Background(new BackgroundFill(drawGridLines(), new CornerRadii(0), null)));

        Rectangle startPosition = new Rectangle(1, 17 * arenaSIze, 3 * arenaSIze, 3 * arenaSIze);
        startPosition.setFill(Color.CYAN);
        gridPane.getChildren().add(startPosition);

        Rectangle bot = new Rectangle(0, 0, 23 * scale, 20 * scale);
        Point robotCoords = BotConst.ROBOT_INITIAL_CENTER_COORDINATES;
        bot.setX(robotCoords.getX() * arenaSIze - bot.getWidth() / 4);
        bot.setY(robotCoords.getY() * arenaSIze - bot.getHeight() / 4);
        System.out.println(bot.getX());
        Stop[] stop = new Stop[]{new Stop(0, Color.BLACK), new Stop(1, Color.RED)};
        LinearGradient lg1 = new LinearGradient(0.7, 0, 1, 0, true, CycleMethod.NO_CYCLE, stop);
        bot.setFill(lg1);
        bot.setStrokeWidth(20);
        Image perry1 = new Image("file:///Users/bryantan/Desktop/Algorithm-main/src/main/java/optimusPrime.jpeg");
        Image perry2 = new Image("file:///Users/bryantan/Desktop/Algorithm-main/src/main/java/optimusPrime.jpeg");

        Timeline anyaTimeLine = new Timeline(
                new KeyFrame(Duration.seconds(0.5), e -> bot.setFill(new ImagePattern(perry1))),
                new KeyFrame(Duration.seconds(1.0), e -> bot.setFill(new ImagePattern(perry2)))
        );

        anyaTimeLine.setCycleCount(9999999);
        anyaTimeLine.play();
        gridPane.getChildren().addAll(bot);

        Label shortestPathLabel = new Label("Shortest path: ");
        Label timerLabel = new Label("Time: ");

        Label xLabel = new Label("X Pos:");
        Label yLabel = new Label("Y Pos:");
        Label dirLabel = new Label("Direction:");

        TextField xField = new TextField();
        TextField yField = new TextField();

        UnaryOperator<TextFormatter.Change> integerFilter = change -> {
            String newText = change.getControlNewText();
            if (newText.matches("-?([0-9][0-9]*)?")) {
                return change;
            }
            return null;
        };

        xField.setTextFormatter(
                new TextFormatter<Integer>(new IntegerStringConverter(), 0, integerFilter));
        yField.setTextFormatter(
                new TextFormatter<Integer>(new IntegerStringConverter(), 0, integerFilter));

        ObservableList<String> options = FXCollections.observableArrayList(
                "North", "South", "East", "West");
        ComboBox<String> directionBox = new ComboBox<>(options);
        directionBox.getSelectionModel().selectFirst();

        Button obstacleButton = new Button("DOOFENSHMIRTZ ATTACKS");
        EventHandler<ActionEvent> addObstacle = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                String dir = directionBox.getValue();
                System.out.println(Integer.parseInt(xField.getText()));
                addObstacle(gridPane, Integer.parseInt(xField.getText()),
                        Integer.parseInt(yField.getText()), IMG_DIR.valueOf(dir.toUpperCase()));
            }
        };
        obstacleButton.setOnAction(addObstacle);
        obstacleButton.setMinWidth(30);

        Button simulateButton = new Button("STOP DOOFENSHMIRTZ");
        EventHandler<ActionEvent> runSimulation = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                if (timeline != null) {
                    timeline.stop();
                }
                timeSeconds = 0;

                timerLabel.setText("Time: " + Math.round(timeSeconds * 100.0) / 100.0);
                timeline = new Timeline();
                timeline.setCycleCount(Timeline.INDEFINITE);
                timeline.getKeyFrames().add(
                        new KeyFrame(Duration.millis(1),
                                new EventHandler<ActionEvent>() {

                                    public void handle(ActionEvent event) {
                                        timeSeconds += .001;

                                        timerLabel.setText("Time: " +
                                                Math.round(timeSeconds * 100.0) / 100.0);
                                        if (timeSeconds >= 360) {
                                            timeline.stop();
                                        }
                                    }
                                }));
                timeline.playFromStart();
                runSimulation(shortestPathLabel, bot, timeline);
            }
        };

        simulateButton.setOnAction(runSimulation);

        GridPane buttonBar = new GridPane();
        buttonBar.setAlignment(Pos.CENTER);
        buttonBar.add(xLabel, 0, 0);
        buttonBar.add(yLabel, 1, 0);
        buttonBar.add(dirLabel, 2, 0);
        buttonBar.add(xField, 0, 1);
        buttonBar.add(yField, 1, 1);
        buttonBar.add(directionBox, 2, 1);
        buttonBar.add(obstacleButton, 0, 2, 3, 1);
        buttonBar.add(simulateButton, 3, 2, 3, 1);
        obstacleButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        simulateButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(15);
        buttonBar.getColumnConstraints().addAll(cc, cc, cc, cc);
        buttonBar.setMinWidth(size);
        buttonBar.setMinHeight(100);

        VBox vbox = new VBox(gridPane, shortestPathLabel, timerLabel, buttonBar);

        primaryStage.setTitle("Simulator");
        primaryStage.setScene(new Scene(vbox, size, size + 150));
        primaryStage.setResizable(false);
        primaryStage.show();

    }

    public void runSimulation(Label label, Rectangle robot, Timeline timeline) {
        ArrayList<ArrayList<MoveInterface>> moveList = new ArrayList<>();
        ArrayList<map.Obstacle> pictureList = ArenaMap.getObstacles();
        SequentialTransition seqT = new SequentialTransition();

        planPath.constructMap();

        int[] fastestPath = aStarPath.AStarPath();
        String text = "Shortest path: ";
        int[] startCoords = new int[3];
        startCoords[0] = bot.getX();
        startCoords[1] = bot.getY();
        startCoords[2] = bot.getRobotDir();
        map.Obstacle n;
        for (int i : fastestPath) {
            n = pictureList.get(i);
            text += "<" + n.getX() + ", " + n.getY() + ">, ";
            moveList.add(
                    planPath.planPath(startCoords[0], startCoords[1], startCoords[2], n.getX(), n.getY(),
                            n.getImadeDirectionAngle(), true, true, true));
            startCoords = planPath.getFinalPosition();
        }

        label.setText(text);
        seqT = getPathAnimation(robot, moveList);
        seqT.play();
        seqT.setOnFinished(e -> timeline.stop());
    }


    private ImagePattern drawGridLines() {

        Canvas canvas = new Canvas(arenaSIze, arenaSIze);

        GraphicsContext gc =
                canvas.getGraphicsContext2D();

        gc.setStroke(Color.WHITE);
        gc.strokeRect(0.5, 0.5, arenaSIze, arenaSIze);
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, arenaSIze, arenaSIze);
        gc.strokeRect(0.5, 0.5, arenaSIze, arenaSIze);

        Image image = canvas.snapshot(new SnapshotParameters(), null);
        ImagePattern pattern = new ImagePattern(image, 0, 0, arenaSIze, arenaSIze, false);

        gc.setFill(pattern);

        return pattern;

    }

    private SequentialTransition getPathAnimation(Rectangle robot,
                                                  ArrayList<ArrayList<MoveInterface>> pathList) {

        SequentialTransition seqT = new SequentialTransition();
        double radiusOfY, radiusOfX;
        double nextX;
        double nextY;
        double duration;
        int finalDir;

        ArrayList<MoveInterface> paths;
        int len = pathList.size();
        for (int i = 0; i < len; i++) {
            paths = pathList.get(i);
            if (paths == null) {
                PauseTransition pauseTransition = new PauseTransition(Duration.millis(1));
                seqT.getChildren().add(pauseTransition);
                continue;
            }
            nextX = paths.get(0).getCurX() * scale;
            nextY = paths.get(0).getCurY() * scale;
            for (MoveInterface move : paths) {
                Path path = new Path();
                PathTransition pathTransition = new PathTransition();
                pathTransition.setNode(robot);
                pathTransition.setOrientation(
                        PathTransition.OrientationType.ORTHOGONAL_TO_TANGENT);
                pathTransition.setCycleCount(0);
                pathTransition.setAutoReverse(false);
                duration = move.getLength() / BotConst.MOVE_SPEED;
                pathTransition.setDuration(Duration.millis(duration * 1000));
                pathTransition.setInterpolator(Interpolator.LINEAR);

                if (move.isStraightLineMovement()) {
                    if (move.isBack()) {
                        pathTransition.setInterpolator(Reverse.reverse(Interpolator.LINEAR));
                        MoveTo moveTo = new MoveTo(move.getNewX() * scale, move.getNewY() * scale);
                        path.getElements().add(moveTo);
                        LineTo line = new LineTo(nextX, nextY);
                        path.getElements().add(line);
                    } else {
                        MoveTo moveTo = new MoveTo(nextX, nextY);
                        path.getElements().add(moveTo);
                        LineTo line = new LineTo(move.getNewX() * scale, move.getNewY() * scale);
                        path.getElements().add(line);
                    }
                    pathTransition.setPath(path);
                } else {
                    MoveTo moveTo = new MoveTo(nextX, nextY);
                    path.getElements().add(moveTo);
                    finalDir = move.getDirDeg();
                    ArcTo turn = new ArcTo();
                    Turn arc = (Turn) move;
                    radiusOfY = arc.getTurnRadiusY() * scale;
                    radiusOfX = arc.getTurnRadiusX() * scale;

                    turn.setX(move.getNewX() * scale);
                    turn.setY(move.getNewY() * scale);
                    if (arc.isLeftTurn()) {
                        turn.setSweepFlag(false);
                        if (finalDir == 90 || finalDir == 270) {
                            turn.setRadiusX(radiusOfX);
                            turn.setRadiusY(radiusOfY);
                        } else {
                            turn.setRadiusX(radiusOfY);
                            turn.setRadiusY(radiusOfX);
                        }
                    } else {
                        turn.setSweepFlag(true);
                        if (finalDir == 180 || finalDir == 0) {
                            turn.setRadiusX(radiusOfY);
                            turn.setRadiusY(radiusOfX);
                        } else {
                            turn.setRadiusX(radiusOfX);
                            turn.setRadiusY(radiusOfY);
                        }
                    }
                    path.getElements().add(turn);
                    pathTransition.setPath(path);
                }
                nextX = move.getNewX() * scale;
                nextY = move.getNewY() * scale;
                seqT.getChildren().add(pathTransition);
            }
            if (i < len - 1) {
                PauseTransition pauseTransition = new PauseTransition(Duration.millis(2000));
                seqT.getChildren().add(pauseTransition);
            }
        }
        return seqT;
    }

    private void addObstacle(Pane arenaPane, int x, int y, IMG_DIR dir) {
        boolean success = arenaMap.addPictureObstacle(x, y, dir);
        if (success) {
            Obstacle obs = new Obstacle(x, y, dir);
            obs.addToPane(arenaPane);
            obstacleArrayList.add(obs);
        }
    }

    private class Obstacle {

        Rectangle obstacle;
        Rectangle indicator;
        Label idLabel;


        public Obstacle(int x, int y, IMG_DIR dir) {
            int xPos = x * arenaSIze;
            int yPos = y * arenaSIze;

            obstacle = new Rectangle(xPos, yPos, arenaSIze, arenaSIze);
            Image doof1 = new Image("file:///Users/bryantan/Desktop/Algorithm-main/src/main/java/obstacle1.jpeg");
            Image doof2 = new Image("file:///Users/bryantan/Desktop/Algorithm-main/src/main/java/obstacle1.jpeg");
            Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(0.5), e -> obstacle.setFill(new ImagePattern(doof1))),
                    new KeyFrame(Duration.seconds(1.0), e -> obstacle.setFill(new ImagePattern(doof2)))
            );

            timeline.setCycleCount(9999999);
            timeline.play();
            switch (dir) {
                case NORTH:
                    indicator = new Rectangle(xPos, yPos, arenaSIze, arenaSIze / 10);
                    break;
                case SOUTH:
                    indicator = new Rectangle(xPos, yPos + (arenaSIze - arenaSIze / 10), arenaSIze,
                            arenaSIze / 10);
                    break;
                case WEST:
                    indicator = new Rectangle(xPos, yPos, arenaSIze / 10, arenaSIze);
                    break;
                case EAST:
                    indicator = new Rectangle(xPos + (arenaSIze - arenaSIze / 10), yPos, arenaSIze / 10,
                            arenaSIze);
                    break;
                default:
                    indicator = null;
            }
            indicator.setFill(GUIConstant.IMAGE_SIDE);
            idLabel = new Label(String.valueOf(obstacleArrayList.size() + 1));
            idLabel.setAlignment(Pos.CENTER);
            idLabel.setFont(new Font(5 * scale));
            idLabel.setTextFill(GUIConstant.TEXT_COLOR);
            idLabel.setTranslateX(xPos + (arenaSIze / 4));
            idLabel.setTranslateY(yPos + (arenaSIze / 4));
        }

        public void setText(String text) {
            idLabel.setText(text);
        }

        public void addToPane(Pane pane) {
            pane.getChildren().addAll(obstacle, indicator, idLabel);
        }

        public void removeFromPane(Pane pane) {
            pane.getChildren().removeAll(obstacle, indicator, idLabel);
        }
    }
}
