package main.java.chess.entity;

import chess.entity.piece.ChessMan;
import javafx.util.Pair;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static chess.entity.piece.ChessMan.*;

public class ChessBoard implements Cloneable, Externalizable {

    private ChessMan[][] gridArray;

    private static final Long serialVersionUID = 1L;
    private transient Map<ChessMan,Pair<Integer,Integer>> blackSideChessManList;
    private transient Map<ChessMan,Pair<Integer,Integer>> whiteSideChessManList;

    private Map<ChessMan,Pair<Integer,Integer>> buildChessManList(Side side){
        Map<ChessMan,Pair<Integer,Integer>> arrayList = new HashMap<>();
        for (int row=0;row<8;row++)
            for (int col=0;col<8;col++)
                if ((this.gridArray[row][col]!=null)
                        && (this.gridArray[row][col].getSide() == side))
                    arrayList.put(this.gridArray[row][col], new Pair<>(row, col));
        if (side == Side.BLACK)
            blackSideChessManList = arrayList;
        if (side == Side.WHITE)
            whiteSideChessManList = arrayList;

        return arrayList;
    }
    public Map<ChessMan,Pair<Integer,Integer>> getSideChessManList(Side side){
        if (side == Side.BLACK) {
            if (blackSideChessManList == null) {
                blackSideChessManList = buildChessManList(side);
            }
            return blackSideChessManList;
        }
        if (side == Side.WHITE) {
            if (whiteSideChessManList==null) {
                whiteSideChessManList = buildChessManList(side);
            }
            return whiteSideChessManList;
        }
        throw new IllegalStateException("Unexpected value: " + side);
    }

    @SuppressWarnings("unused")
    public ChessBoard(){ this(new ChessMan[8][8]); }
    public ChessBoard(ChessMan[][] gridArray){
        assert gridArray.length == 8;
        for (ChessMan[] line: gridArray) assert line.length == 8;
        this.gridArray = gridArray;
        blackSideChessManList = buildChessManList(Side.BLACK);
        whiteSideChessManList = buildChessManList(Side.WHITE);
    }

    public void tryKillChessMan(int row, int col){
        if (this.gridArray[row][col]!=null) {
            getSideChessManList(this.gridArray[row][col].getSide()).remove(
                    this.gridArray[row][col]
            );
            this.gridArray[row][col] = null;
        }
    }

    public void createChessMan(int row, int col, ChessMan chessMan){
        assert this.gridArray[row][col] == null;
        this.gridArray[row][col] = chessMan;
        getSideChessManList(chessMan.getSide()).put(
                chessMan, new Pair<>(row, col)
        );
    }

    public void moveChessMan(int row, int col, int newRow, int newCol){
        assert this.gridArray[row][col] != null;
        assert this.gridArray[newRow][newCol] == null;

        getSideChessManList(this.gridArray[row][col].getSide())
                .put(this.gridArray[row][col], new Pair<>(newRow, newCol));

        this.gridArray[newRow][newCol] = this.gridArray[row][col];
        this.gridArray[row][col] = null;
        this.gridArray[newRow][newCol].setFirstMoved(true);
    }

    public  ChessMan getChessMan(int row, int col){
        return this.gridArray[row][col];
    }

    @Override
    public String toString() {
        StringBuilder stringBuffer = new StringBuilder();
        for (int row=0;row<8;row++){
            for (int col=0;col<8;col++) {
                if (gridArray[row][col] == null)
                    stringBuffer.append("   ");
                else
                    stringBuffer.append(gridArray[row][col].toString());
                if (col != 7) stringBuffer.append(" | ");
            }
            if (row != 7) {
                stringBuffer.append("\n");
                stringBuffer.append("---".repeat(8 + 7));
                stringBuffer.append("\n");
            } else {
                stringBuffer.append("\n");
            }
        }
        return stringBuffer.toString();
    }

    public int[] getBytesArray(){
        // 为了快速传输数据设计的方法
        // 注意：这个不能用来Save Game，因为棋子的firstMoved没保存
        int[] bytesArray = new int[64];
        for (int row = 0;row<8;row++)
            for (int col = 0;col<8;col++)
                bytesArray[(row<<3) | (col)] =
                        (this.gridArray[row][col]==null)?0:this.gridArray[row][col].getBytesCode();
        return bytesArray;
    }

    @Override
    public ChessBoard clone() {
        try {
            ChessBoard clone = (ChessBoard) super.clone();
            clone.gridArray = new ChessMan[8][8];
            clone.whiteSideChessManList = null;
            clone.blackSideChessManList = null;

            for (int row=0;row<8;row++){
                clone.gridArray[row] = new ChessMan[8];
                for (int col=0;col<8;col++)
                {
                    clone.gridArray[row][col] =
                        (gridArray[row][col]==null)?
                        null:
                        gridArray[row][col].clone();
                }
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public boolean checkGridSize() {
        if (this.gridArray.length != 8)
            return false;
        for (var line:this.gridArray){
            if (line.length != 8) return false;
        }
        return true;
    }

    public boolean checkChessManTypeAndNumber() {
        var c = new int[][]{
                new int[]{0,0,0,0,0,0}, new int[]{0,0,0,0,0,0}
        };  // counter[0] 为白， [1]为黑，[0][0]~[0][5]分别是王、后、车、马、象、兵
        for (var line:this.gridArray){
            for (var chessMan:line){
                if (chessMan==null) continue;
                int counterInd;
                if (chessMan.getSide()==Side.WHITE){
                    counterInd = 0;
                } else if (chessMan.getSide()==Side.BLACK){
                    counterInd = 1;
                } else { continue; }
                var typeInd = switch (chessMan.getType()){
                    case KING -> 0;
                    case QUEEN -> 1;
                    case ROOK -> 2;
                    case KNIGHT -> 3;
                    case BISHOP -> 4;
                    case PAWN -> 5;
                    default -> -1;
                }; if (typeInd==-1) continue;
                c[counterInd][typeInd] += 1;
            }
        }

        for (int i=0;i<2;i++) {
            // 只有一个王，且不能没有王
            if (c[i][0] != 1) return false;
            // 最多8个兵
            if ((c[i][5])>8) return false;
            // 可升变的兵种的数量检查
            if (((
                Math.max(c[i][1] - 1,0) +
                Math.max(c[i][2] - 2,0) +
                Math.max(c[i][3] - 2,0) +
                Math.max(c[i][4] - 2,0)
            ) + c[i][5]) > 8) return false;
        }
        return true;
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(gridArray);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        gridArray = (ChessMan[][]) in.readObject();
        this.whiteSideChessManList = null;
        this.blackSideChessManList = null;
    }

}
