package main.java.chess.entity.move;

import chess.entity.ChessBoard;
import chess.entity.ChessGame;
import chess.entity.piece.ChessMan;

public class Move extends BaseMove {

    @SuppressWarnings("unused")
    public static BaseMove createEmptyEditableMove(ChessMan chessMan, int fromRow, int fromCol, ChessGame environment){
        throw new IllegalCallerException("createEmptyEditableMove方法不可从Move类型的实例中调用. ");
    }

    public static Move valueOf(BaseMove mv) {
        return new Move(mv.chessMan(), mv.fromRow(), mv.fromCol(), mv.toRow(), mv.toCol(), mv.environment());
    }

    @Override
    public BaseMove setTestInstance(int rowOffset, int colOffset){
        throw new IllegalCallerException("setTestInstance方法不可从Move类型的实例中调用. ");
    }

    public Move(ChessMan chessMan, int fromRow, int fromCol, int toRow, int toCol,
                ChessGame environment) {
        super(chessMan, fromRow, fromCol, toRow, toCol, environment);
    }

}

