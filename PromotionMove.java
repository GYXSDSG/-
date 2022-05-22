package main.java.chess.entity.move;

import chess.entity.ChessGame;
import chess.entity.piece.ChessMan;

public class PromotionMove extends Move{
    private final ChessMan newChessMan;
    public ChessMan newChessMan(){
        return newChessMan;
    }
    public PromotionMove(ChessMan chessMan, ChessMan newChessMan, int row, int col, ChessGame environment) {
        super(chessMan, row, col, row, col, environment);
        this.newChessMan = newChessMan;
    }
}
