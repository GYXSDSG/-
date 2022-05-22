package main.java.chess.entity.move;
import chess.entity.ChessBoard;
import chess.entity.ChessGame;
import chess.entity.piece.ChessMan;

public class BaseMove implements Cloneable{
    private ChessMan chessMan;
    private final int fromRow;
    private final int fromCol;
    private int toRow;
    private int toCol;
    private ChessGame environment;

    @Override
    public BaseMove clone(){
        BaseMove clone = null;
        try {
            clone = (BaseMove) super.clone();
        } catch (CloneNotSupportedException e){
            throw new RuntimeException("在BaseMove.clone()中发生CloneNotSupportedException错误. ");
        }
        assert clone != null;
        clone.environment = this.environment.clone();
        ChessBoard clonedBoard = clone.environment.getChessBoard();
        ChessMan clonedChessMan = clonedBoard.getChessMan(fromRow,fromCol);
        assert clonedChessMan != null;
        clone.chessMan = clonedChessMan;
        return clone;
    }
    public static BaseMove createEmptyEditableMove(ChessMan chessMan, int fromRow, int fromCol, ChessGame environment){
        return new BaseMove(chessMan, fromRow, fromCol, -1, -1, environment);
    }
    public BaseMove(ChessMan chessMan, int fromRow, int fromCol, int toRow, int toCol,
                    ChessGame environment) {
        this.chessMan = chessMan;
        this.fromRow = fromRow;
        this.fromCol = fromCol;
        this.toRow = toRow;
        this.toCol = toCol;
        this.environment = environment;
    }

    public ChessMan chessMan() {
        return chessMan;
    }

    public int fromRow() {
        return fromRow;
    }

    public int fromCol() {
        return fromCol;
    }

    public int toRow() {
        return toRow;
    }

    public int toCol() {
        return toCol;
    }

    public ChessBoard environmentBoard() {
        return environment.getChessBoard();
    }
    public ChessGame environment(){
        return environment;
    }

    public BaseMove setTestInstance(int rowOffset, int colOffset){
        var toRow = this.fromRow + rowOffset;
        var toCol = this.fromCol + colOffset;
        if (toRow>7 || toRow<0) return null;
        if (toCol>7 || toCol<0) return null;

        this.toRow = toRow;
        this.toCol = toCol;

        return this;
    }

    @Override
    public String toString() {
        return "BaseMove{" +
                "chessMan=" + chessMan +
                ", fromRow=" + fromRow +
                ", fromCol=" + fromCol +
                ", toRow=" + toRow +
                ", toCol=" + toCol +
                ", environment=" + environment +
                '}';
    }
}
