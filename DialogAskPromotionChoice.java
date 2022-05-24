package chess.view;

import chess.entity.piece.ChessMan;
import chess.util.Utils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.text.Font;

import java.util.List;

public class DialogAskPromotionChoice extends Dialog<ButtonType> {

    public static ChessMan.Type returnType;
    private static DialogAskPromotionChoice instance;

    private DialogAskPromotionChoice() {
        super();

        this.setTitle("升变");
        var dialogPane = this.getDialogPane();

        dialogPane.getButtonTypes().addAll(ButtonType.CANCEL);
        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setVisible(false);

        //noinspection Convert2MethodRef
        dialogPane.getScene().getWindow().setOnCloseRequest(event ->
            event.consume()  // 销毁事件，从而让对话框无法被关闭（必须选择一个升变）
        );

        var promotionOptions = getOptions();
        dialogPane.setContent(promotionOptions);

        Utils.setDialogIcon(this);
    }

    public static DialogAskPromotionChoice getInstance() {
        if (instance == null)
            instance = new DialogAskPromotionChoice();
        return instance;
    }

    private VBox getOptions() {
        var container = new VBox();
        var label = new Label("请选择要升变成为的棋种：");
        label.setPadding(new Insets(10.0));
        container.getChildren().add(label);
        label.setFont(new Font(16));


        var Options = new HBox();

        String[] resources = {
            "/image/queen-blank.png",
            "/image/rook-blank.png",
            "/image/bishop-blank.png",
            "/image/knight-blank.png"
        };
        ChessMan.Type[] types = {
                ChessMan.Type.QUEEN,
                ChessMan.Type.ROOK,
                ChessMan.Type.BISHOP,
                ChessMan.Type.KNIGHT
        };

        List.of(0,1,2,3).forEach( i ->{
            var btn = new Button();
            btn.setMinSize(50, 50);
            btn.setPrefSize(100,100);
            btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            btn.setPadding(Insets.EMPTY);
            btn.setBorder(Border.EMPTY);

            btn.setBackground(
                new Background(
                    new BackgroundImage(
                        new Image(
                            Utils.getResourcePath(resources[i])
                        ),
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.DEFAULT,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false)
                    )
                )
            );


            var glowEffect = new Glow();
            btn.setOnMouseEntered( e ->
                btn.setEffect(glowEffect)
            );
            btn.setOnMouseExited( e ->
                btn.setEffect(null)
            );
            btn.setOnAction(e->{
                returnType = types[i];
                this.hide();
            });

            var vbox = new VBox();
            vbox.getChildren().add(btn);
            vbox.getChildren().add(new Label(types[i].name()));
            vbox.setAlignment(Pos.CENTER);
            Options.getChildren().add(vbox);

        });

        container.getChildren().add(Options);

        return container;
    }
}
