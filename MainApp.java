package chess;

import chess.controller.ChessController;
import chess.util.Utils;
import chess.view.BGMPlayer;
import chess.view.ChessView;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;

import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.util.List;

public class MainApp extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        // 背景音乐控制器
        BGMPlayer bgmPlayer = BGMPlayer.getInstance();
        // 象棋显示、控制器
        ChessController controller = new ChessController();
        ChessView view = new ChessView(controller, bgmPlayer,
                                       primaryStage::setTitle);

        // 创建按钮
        Button btnNewGame = new Button("\uD835\uDCDD\uD835\uDCD4\uD835\uDCE6  \uD835\uDCD6\uD835\uDCD0\uD835\uDCDC\uD835\uDCD4");
        Button btnLoadGame = new Button("\uD835\uDCDB\uD835\uDCDE\uD835\uDCD0\uD835\uDCD3 \uD835\uDCD6\uD835\uDCD0\uD835\uDCDC\uD835\uDCD4");
        Button btnSaveGame = new Button("\uD835\uDCE2\uD835\uDCD0\uD835\uDCE5\uD835\uDCD4 \uD835\uDCD6\uD835\uDCD0\uD835\uDCDC\uD835\uDCD4");
        Button btnQuitGame = new Button("\uD835\uDCE0\uD835\uDCE4\uD835\uDCD8\uD835\uDCE3 \uD835\uDCD6\uD835\uDCD0\uD835\uDCDC\uD835\uDCD4");

        // 绑定按钮方法
        btnNewGame.setOnAction(e -> view.btnNewGame());
        btnLoadGame.setOnAction(e -> view.btnLoadGame());
        btnSaveGame.setOnAction(e -> view.btnSaveGame());
        btnQuitGame.setOnAction(e -> view.btnQuitSave());

        // 界面布局
        VBox vBoxButtons = new VBox();
        vBoxButtons.setAlignment(Pos.TOP_RIGHT);
        Font buttonFont = new Font("Times New Roman", 16);
        List.of(btnNewGame,btnLoadGame,btnSaveGame,btnQuitGame).forEach( e-> {
            vBoxButtons.getChildren().add(e);
            VBox.setVgrow(e, Priority.SOMETIMES);

            e.setMinSize(60,50);
            e.setMaxSize(300, 100);
            e.setFont(buttonFont);
        });

        Pane pane = new Pane();
        pane.getChildren().add(view);
        view.setLayoutX(0);
        view.setLayoutY(0);

        pane.getChildren().add(vBoxButtons);
        vBoxButtons.setMinWidth(100);
        vBoxButtons.setLayoutX(400);
        vBoxButtons.setLayoutY(0);
        vBoxButtons.setPadding(new Insets(0,5,0,0));
        vBoxButtons.setSpacing(5);

        Scene scene = new Scene(pane, 600,450);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Chess Game");

        Runnable onStageSizeChange = () -> {
            double entireHeight = scene.getHeight();
            double entireWidth = scene.getWidth();
            double minSize = (int)Math.min(entireHeight, entireWidth);
            double restSize = (int)(entireWidth - (minSize+5));
            view.setPrefSize(minSize,minSize);

            vBoxButtons.setPrefSize(restSize,entireHeight);
            vBoxButtons.setLayoutX(minSize+5);
        };

        ChangeListener<Number> onStageWidthChange =
                (observable, oldValue, newValue) ->
        {
            double min = 500;
            double max = 800;
            if (newValue.floatValue()<min)
                primaryStage.setWidth(min);
            else if (newValue.floatValue()>max)
                primaryStage.setWidth(max);
            onStageSizeChange.run();
        };
        ChangeListener<Number> onStageHeightChange =
                (observable, oldValue, newValue) ->
        {
            double min = 450;
            double max = 800;
            if (newValue.floatValue()<min)
                primaryStage.setHeight(min);
            else if (newValue.floatValue()>max)
                primaryStage.setHeight(max);
            onStageSizeChange.run();
        };

        primaryStage.heightProperty().addListener(onStageHeightChange);
        primaryStage.widthProperty().addListener(onStageWidthChange);

        primaryStage.maximizedProperty().addListener((o,old_,new_)->{
            if (new_) primaryStage.setMaximized(false);
        });
        primaryStage.getIcons().add(Utils.getIcon());

        primaryStage.setOnCloseRequest(event -> {
            event.consume();
            view.btnQuitSave();
        });

        primaryStage.show();

        // 开启第一局游戏
        controller.newGame();

    }
}
