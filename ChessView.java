package chess.view;

import chess.controller.ChessController;
import chess.entity.ChessGame;
import chess.entity.piece.ChessMan;
import chess.util.Utils;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.util.Optional;

public class ChessView extends StackPane {

    // 当前Side
    private ChessMan.Side currentSide;
    private boolean interactable;
    private FileChooser fileChooser;

    public void setInteractable(boolean interactable) {
        this.interactable = interactable;
    }

    public void btnLoadGame() {
        var confirm = confirmDialog("确认",
                "确实要放弃当前棋局，从文件中读取局面吗？");
        if (!confirm) return;

        var file = this.getFileChooser().showOpenDialog(this.getScene().getWindow());

        if (file==null){
            messageBox("错误","没有选择文件！");
            return;
        }

        var retValue = controller.tryLoadGame(file);
        switch (retValue) {
            case 101 -> messageBox("错误","棋盘并非 8*8！("+retValue+")");
            case 102 ->messageBox("错误","棋子并非六种之一，棋子并非黑白棋子！("+retValue+")");
            case 103 ->messageBox("错误","导入数据只有棋盘，没有下一步行棋的方的提示！("+retValue+")");
            case 104 -> messageBox("错误","文件格式有误，无法从文件中读取棋局！("+retValue+")");
            case 105 -> messageBox("错误","找不到文件或文件读取错误，读取失败！("+retValue+")");
            case 106 -> messageBox("错误","文件格式错误，读取失败！("+retValue+")");
            case 0 -> messageBox("信息","读取成功！");
            default -> throw new IllegalStateException("Unexpected value: " + retValue);
        }
    }

    public void btnSaveGame() {
        var file = this.getFileChooser().showSaveDialog(this.getScene().getWindow());
        if (file==null){
            messageBox("错误","没有选择文件！棋局未保存！");
            return;
        }
        var retValue = controller.trySaveGame(file);
        switch (retValue) {
            case 0 -> messageBox("信息","保存成功！");
            case 1 -> messageBox("错误","在保存文件过程中发生错误，保存失败！");
            default -> throw new IllegalStateException("Unexpected value: " + retValue);
        }
    }

    private FileChooser getFileChooser() {
        if (this.fileChooser == null) {
            this.fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("象棋局面文件", "*.sav"),
                    new FileChooser.ExtensionFilter("所有文件", "*.*")
            );
        }
        return this.fileChooser;
    }

    public void btnNewGame() {
        var ifNew = askIfNewGame();
        if (ifNew)
            controller.newGame();
    }

    public void update(int[] boardBytesArray, ChessMan.Side currentSide) {
        setCurrentSide(currentSide);
        redrawBoardFromBytes(boardBytesArray);
    }

    public void btnQuitSave() {
        var confirm = confirmDialog("退出","确实要退出游戏吗？");
            if (confirm) ((Stage)this.getScene().getWindow()).close();
    }

    @FunctionalInterface
    public interface WindowTitleWriter{ void write(String string);}
    private final WindowTitleWriter windowTitleWriter;

    private final ChessController controller;
    private final BGMPlayer bgmPlayer;
    private final SoundsPlayer soundsPlayer;

    private int[] lastPositions;
    private ChessButton lastSelectedButton;
    private ChessButton[][] chessButtons;

    public ChessView(ChessController controller, BGMPlayer mediaPlayer,
                     WindowTitleWriter windowTitleWriter) {
        this.controller = controller;
        controller.bindView(this);
        this.bgmPlayer = mediaPlayer;
        this.windowTitleWriter = windowTitleWriter;

        this.soundsPlayer = new SoundsPlayer();

        initChessManContainer();
        initChessBoardContainer();

        this.setMinSize(400, 400);
        this.setInteractable(false);

    }

    private void initChessManContainer(){
        GridPane chessManContainer = new GridPane();
        chessButtons = new ChessButton[8][8];
        for (int i=0;i<8;i++)
            for (int j=0;j<8;j++){
                ChessButton btnChessMan = new ChessButton(this, i, j);
                chessButtons[i][j] = btnChessMan;

                chessManContainer.add(btnChessMan, j, i); // 先列后行
                GridPane.setHgrow(btnChessMan, Priority.SOMETIMES);
                GridPane.setVgrow(btnChessMan, Priority.SOMETIMES);
            }
        this.getChildren().add(chessManContainer);
        chessManContainer.setBorder(new Border(
            new BorderStroke(Color.BLACK,BorderStrokeStyle.SOLID,CornerRadii.EMPTY,BorderWidths.DEFAULT)));
    }

    private void initChessBoardContainer(){
        GridPane boardContainer = new GridPane();
        var chessBoardBackground = new Background(
            new BackgroundImage(
                new Image(Utils.getResourcePath("/image/chess-board.png"), true),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                new BackgroundSize(BackgroundSize.AUTO,BackgroundSize.AUTO,false,false,true,false)
            )
        );
        boardContainer.setBackground(chessBoardBackground);
        this.getChildren().add(0,boardContainer);
    }

    public void restore(ChessGame game){
        this.setCurrentSide(game.getCurrentSide());
        this.setInteractable(true);
        this.lastSelectedButton = null;
        this.bgmPlayer.newBGM();
        this.lastPositions = null;
        this.redrawBoardFromBytes(game.getBoardBytesArray());
        for (int row=0;row<8;row++)
            for (int col=0;col<8;col++) {
                this.chessButtons[row][col].setSelected(false);
                this.chessButtons[row][col].notEmpty =
                        game.chessBoard.getChessMan(row, col) != null;
            }
    }

    private void redrawBoardFromBytes(int[] byteArrays){
        if (this.lastPositions == null) {
            lastPositions = byteArrays;
            for (int row = 0; row < 8; row++)
                for (int col = 0; col < 8; col++) {
                    var bytesCode = byteArrays[(row << 3) | col];
                    chessButtons[row][col].loadBytesCode(bytesCode);
                }
        } else {
            for (int row = 0; row < 8; row++)
                for (int col = 0; col < 8; col++) {
                    if (byteArrays[(row << 3) | col] != lastPositions[(row << 3) | col]) {
                        var bytesCode = byteArrays[(row << 3) | col];
                        chessButtons[row][col].loadBytesCode(bytesCode);
                    }
                }
            lastPositions = byteArrays;
        }
    }
    @SuppressWarnings("UnnecessaryReturnStatement")
    public void onChessButtonClicked(ChessButton buttonClicked){
        if (!this.interactable) return;
        if (this.lastSelectedButton == null){
            if (buttonClicked.notEmpty) {
                if (buttonClicked.side==currentSide) {
                    buttonClicked.setSelected(true);
                    lastSelectedButton = buttonClicked;
                } else
                    return;
            }
        } else {  // 已有选中棋子
            // 新选中点如果是空地，则移动；否则重置被选择的点
            if (!buttonClicked.notEmpty){ // 是空地
                controller.tryMove(
                    lastSelectedButton.getRow(),lastSelectedButton.getCol(),
                    buttonClicked.getRow(),buttonClicked.getCol()
                );
            } else { // 是棋子
                if (buttonClicked.side == currentSide) {
                    lastSelectedButton.setSelected(false);
                    buttonClicked.setSelected(true);
                    lastSelectedButton = buttonClicked;
                } else {
                    controller.tryMove(
                        lastSelectedButton.getRow(),lastSelectedButton.getCol(),
                        buttonClicked.getRow(),buttonClicked.getCol()
                    );
                }
            }
        }
    }

    public void executeMove(int[] bytesArray, ChessMan.Side currentSide){
        this.soundsPlayer.play("move");
        if (lastSelectedButton!=null){
            lastSelectedButton.setSelected(false);
            lastSelectedButton = null;
        }
        this.update(bytesArray, currentSide);
    }

    public void onMoveFailed() {
        this.soundsPlayer.play("move_failed");
    }

    public void onNewGame(int[] bytesArray, ChessMan.Side currentSide) {
        this.bgmPlayer.newBGM();
        this.update(bytesArray,currentSide);
        this.setInteractable(true);
    }

    public void onGameFinished(ChessGame.Result result) {
        this.showGameResult(result);
        boolean ifNewGame = this.askIfNewGame();
        if (ifNewGame) controller.newGame();
        else {
            this.setInteractable(false);
            this.windowTitleWriter.write("Chess Game [Finished]");
        }
    }

    private void showGameResult(ChessGame.Result result) {
        String title = "游戏结束";
        String message = switch (result) {
            case DRAW -> "平局！";
            case BLACK_WIN -> "黑方胜利！";
            case WHITE_WIN -> "白方胜利！";
            default -> throw new IllegalStateException("Unexpected value: " + result);
        };

        this.messageBox(title, message);
    }

    private void messageBox(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.setGraphic(null);
        alert.setHeaderText(null);
        Utils.setDialogIcon(alert);
        alert.showAndWait();
    }

    public boolean askIfNewGame(){
        return confirmDialog("新游戏？", "要开始新的一局游戏吗？");
    }
    public boolean confirmDialog(String title, String contentText){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setContentText(contentText);
        alert.setHeaderText(null);
        var buttonOK = ((Button)alert.getDialogPane().lookupButton(ButtonType.OK));
        buttonOK.setText("是");
        var buttonCancel = ((Button)alert.getDialogPane().lookupButton(ButtonType.CANCEL));
        buttonCancel.setText("否");
        Utils.setDialogIcon(alert);

        Optional<ButtonType> answer = alert.showAndWait();
        if (answer.isEmpty())
            return false;
        ButtonType ansType = answer.get();
        if (ansType.equals(ButtonType.OK))
            return true;
        if (ansType.equals(ButtonType.CANCEL))
            return false;
        throw new IllegalStateException("Unexpected value: " + ansType.getButtonData());
    }

    public ChessMan.Type msgboxGetTypeToPromote() {
        var dialog = DialogAskPromotionChoice.getInstance();
        dialog.showAndWait();
        return DialogAskPromotionChoice.returnType;
    }

    private void setCurrentSide(ChessMan.Side side){
        currentSide = side;
        var sideStr = side.toString();
        windowTitleWriter.write("Chess Game [Current side: "+ sideStr +"]");
    }

}
