package main.java.chess.controller;

import chess.entity.ChessGame;

import chess.entity.piece.ChessMan;
import chess.entity.move.Move;
import chess.entity.move.PromotionMove;
import chess.entity.rule.ChessRule;
import chess.utils.IO;
import chess.utils.Utils;
import chess.view.ChessView;

import java.io.*;

public class ChessController {
    public ChessGame game;
    private ChessView view;
    Utils.intArrayCircleStack boardBackups;
    Utils.CircleStack<Boolean> ifCheckedSequenceBlack;
    Utils.CircleStack<Boolean> ifCheckedSequenceWhite;

    public ChessController(){
    }
    public void bindView(ChessView view){
        this.view = view;
    }
    public void newGame(){
        ChessRule.dbgController = this;
        this.game = new ChessGame();

        this.reinitializeBackupsSequence();

        assert game.getCurrentSide() != ChessMan.Side.EMPTY;
        var currentSide = game.getCurrentSide();

        int[] bytesArray = game.getBoardBytesArray();
        boardBackups.put(bytesArray);
        ifCheckedSequenceBlack.put(game.getNowBeingChecked(ChessMan.Side.BLACK));
        ifCheckedSequenceWhite.put(game.getNowBeingChecked(ChessMan.Side.WHITE));
        view.onNewGame(bytesArray, currentSide);
    }

    private void reinitializeBackupsSequence() {
        this.boardBackups = new Utils.intArrayCircleStack(3 * 4); // 要检查三次局面重复，至少需要12个backup
        int PerpetualCheckTimes = ChessRule.PerpetualCheckTimes;
        this.ifCheckedSequenceBlack = new Utils.CircleStack<>(PerpetualCheckTimes * 2);
        this.ifCheckedSequenceWhite = new Utils.CircleStack<>(PerpetualCheckTimes * 2);
    }

    public void tryMove(int fromRow,int fromCol,int toRow, int toCol){
        ChessMan chessMan = game.getBoardChessMan(fromRow, fromCol);
        if (chessMan == null) return;
        var move=new Move(chessMan, fromRow, fromCol, toRow, toCol, game);
        this.tryMove(move);
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    public void tryMove(Move move){
        ChessGame.Status returnStatus = game.tryMove(move);
        if (returnStatus == null){ // 说明tryMove失败，没有状态改变
            view.onMoveFailed();
            return;
        } else { // move成功，状态改变为returnStatus
            if (returnStatus.mainStatus == ChessGame.Status.MainStatus.FINISHED) {
                var currentSide = game.getCurrentSide();
                var bytesArray = game.getBoardBytesArray();
                boardBackups.put(bytesArray);
                ifCheckedSequenceBlack.put(game.getNowBeingChecked(ChessMan.Side.BLACK));
                ifCheckedSequenceWhite.put(game.getNowBeingChecked(ChessMan.Side.WHITE));

                view.executeMove(bytesArray, currentSide);
                view.onGameFinished(game.getResult(), null);
            } else if (
                    (returnStatus.mainStatus == ChessGame.Status.MainStatus.WAIT_FOR_PROMOTION_CHOICE_GIVEN)
            ) {
                var bytesArray = game.getBoardBytesArray();
                // 注意这里不能boardBackups.put()，否则一黑一白的规则就不对了
                view.update(bytesArray, game.getCurrentSide());

                ChessMan.Type newType = view.msgboxGetTypeToPromote();
                var promotion = new PromotionMove(move.chessMan(), new ChessMan(newType, move.chessMan().getSide()), move.toRow(), move.toCol(),game);
                this.tryMove(promotion);
                return;
            } else if (
                    (returnStatus.mainStatus == ChessGame.Status.MainStatus.WAIT_FOR_NORMAL_MOVE)
            ) {
                var currentSide = game.getCurrentSide();
                var bytesArray = game.getBoardBytesArray();
                var ifThreeCircle = boardBackups.putAndCheckSingularEquals(bytesArray,4);

                var ifPerpetualCheck = ifCheckedSequenceBlack.putAndCheckSingularEquals(game.getNowBeingChecked(ChessMan.Side.BLACK),2);
                ifPerpetualCheck |= ifCheckedSequenceWhite.putAndCheckSingularEquals(game.getNowBeingChecked(ChessMan.Side.WHITE),2);
                view.executeMove(bytesArray, currentSide);
                if (ifThreeCircle){
                    game.status.mainStatus = ChessGame.Status.MainStatus.FINISHED;
                    game.setResult(ChessGame.Result.DRAW);
                    view.onGameFinished(game.getResult(), ChessView::showThreeCircleDraw);
                }
                if (ifPerpetualCheck){
                    game.status.mainStatus = ChessGame.Status.MainStatus.FINISHED;
                    game.setResult(ChessGame.Result.DRAW);
                    view.onGameFinished(game.getResult(), ChessView::showPerpetualCheckDraw);
                }
                return;
            }
        }
    }

    /**
     * @return int返回值，-1:未知错误，0:成功，101:棋盘并非8*8, 102:棋子并非6种之一或并非黑白，103:缺少行棋方，104:文件名错误，105:IO失败，
     * 106: 类未找到错误
     */
    public int tryLoadGame(File file) {
        try {
            // 检查文件格式
            var filePath = file.getAbsolutePath();
            if (filePath.length()< 4) return 104;
            var subString= filePath.substring(filePath.length()-4);
            if (!subString.equalsIgnoreCase(".sav")) return 104;

            ChessGame loadedGame;
            try {
                IO.IgnoreUIDInputStream ois = new IO.IgnoreUIDInputStream(new BufferedInputStream(new FileInputStream(file)));
                loadedGame = (ChessGame) ois.readObject();
                ois.close();
            } catch (IOException e) {
//                e.printStackTrace();
                return 105;
            } catch (ClassNotFoundException e) {
                return 106;
            }
            if (loadedGame == null) return 106;
            var board = loadedGame.getChessBoard();

            // 检查棋盘大小
            if (!board.checkGridSize()){
                return 101;
            }
            // 检查棋子种类数量
            if (!board.checkChessManTypeAndNumber()){
                return 102;
            }
            // 检查行棋方
            if ((loadedGame.status.currentSide!= ChessMan.Side.BLACK) && (loadedGame.status.currentSide!= ChessMan.Side.WHITE)){
                return 103;
            }
            this.game = loadedGame;
            this.reinitializeBackupsSequence();
            this.view.restore(loadedGame);
            return 0;
        }
        catch (Throwable e) {
            return -1; // 未知错误
        }
    }

    /**
     * @return int返回值，0:成功，1:保存失败
     */
    public int trySaveGame(File file){
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(
                    file)));
            oos.writeObject(this.game);
            oos.close();
        } catch (IOException e) {
            return 1;
        }
        return 0;
    }

}
