package main.java.chess.entity;

import chess.entity.piece.ChessMan;
import chess.entity.move.Move;
import chess.entity.move.PromotionMove;
import chess.entity.rule.ChessRule;

import java.io.*;


public class ChessGame implements Cloneable, Externalizable {

    private static final Long serialVersionUID = 1L;

    // 变量
    private ChessBoard chessBoard;
    private Result result;
    public Status status;

    // 辅助变量，序列化时不用存
    /** 当下是否被check */
    private transient Boolean nowWhiteBeingChecked;
    private transient Boolean nowBlackBeingChecked;

    public boolean getNowBeingChecked(ChessMan.Side side){

        if (side == ChessMan.Side.WHITE) {
            if (nowWhiteBeingChecked == null)
                nowWhiteBeingChecked = ChessRule.ifSideChecked(this, ChessMan.Side.WHITE);
            return this.nowWhiteBeingChecked;
        }
        if (side == ChessMan.Side.BLACK) {
            if (nowBlackBeingChecked == null)
                nowBlackBeingChecked = ChessRule.ifSideChecked(this, ChessMan.Side.BLACK);
            return this.nowBlackBeingChecked;
        }

        throw new IllegalStateException("Unexpected value:" + side);
    }

    public Status tryMove(Move move) {
        if (move instanceof PromotionMove){
            ChessRule.promote(this,(PromotionMove)move);
            return this.status;
        } else{
            var pair = ChessRule.moveFollowedRule(move);
            var isFollowedRule = pair.getKey();
            var info = pair.getValue();
            if (isFollowedRule){
                if (info.isEmpty()) {
                    ChessRule.changeStatus(this, move, false);
                } else if (info.get().equals("enp")){ // 吃过路兵
                    ChessRule.enpCapture(this, move);

                } else
                if (info.get().equals("castling")){
                    // todo 王车易位
                    resetNowBeingChecked();
                }
                return this.status;
            }
            else return null;
        }
    }

    public ChessMan.Side getCurrentSide() {
        return this.status.currentSide;
    }

    @Override
    public ChessGame clone() {
        try {
            ChessGame clone = (ChessGame) super.clone();
            clone.status = status.clone();
            clone.result = result;
            clone.chessBoard = chessBoard.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public void resetNowBeingChecked() {
        nowWhiteBeingChecked = null;
        nowBlackBeingChecked = null;
    }

    public ChessMan getBoardChessMan(int row, int col) {
        return this.chessBoard.getChessMan(row, col);
    }

    public ChessBoard getChessBoard() {
        return chessBoard;
    }

    public Status getStatus() {
        return status;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

    public enum Result {
        NOT_FINISHED, BLACK_WIN, WHITE_WIN, DRAW;
        public static Result valueOf(ChessMan.Side side){
            if (side==null) throw new IllegalStateException("Unexpected value: "+null);
            if (side == ChessMan.Side.WHITE) return WHITE_WIN;
            if (side == ChessMan.Side.BLACK) return BLACK_WIN;
            throw new IllegalStateException("Unexpected value: " + side);
        }
    }

    public static class Status implements Cloneable, Externalizable{

        private static final Long serialVersionUID = 1L;

        public Status() {
            var callerName = Thread.currentThread().getStackTrace()[1].getMethodName();
            if (!callerName.equals("<init>"))
                throw new IllegalCallerException(this.getClass().getName() + "的无参构造方法不应被调用. ");
        }

        @Override
        public Status clone() {
            try {
                Status clone = (Status) super.clone();
                clone.mainStatus = mainStatus;
                clone.currentSide = currentSide;
                clone.isFinished = isFinished;
                clone.enPassantStatus = enPassantStatus.clone();
                return clone;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(mainStatus);
            out.writeObject(enPassantStatus);
            out.writeObject(currentSide);
            out.writeBoolean(isFinished);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            mainStatus = (MainStatus) in.readObject();
            enPassantStatus = (EnPassantStatus) in.readObject();
            currentSide = (ChessMan.Side) in.readObject();
            isFinished = in.readBoolean();
        }

        public enum MainStatus {
            WAIT_FOR_NORMAL_MOVE,
            WAIT_FOR_PROMOTION_CHOICE_GIVEN,
            FINISHED
        }

        public MainStatus mainStatus;
        public ChessMan.Side currentSide;
        public boolean isFinished;

        @SuppressWarnings("SameParameterValue")
        private Status(MainStatus mainStatus, ChessMan.Side currentSide,
                       boolean isFinished, EnPassantStatus enPassantStatus) {
            this.mainStatus = mainStatus;
            this.currentSide = currentSide;
            this.isFinished = isFinished;
            this.enPassantStatus = enPassantStatus;
        }
        public static Status originalStatus(){
            return new Status(
                    MainStatus.WAIT_FOR_NORMAL_MOVE,
                    ChessMan.Side.WHITE,
                    false,
                    new EnPassantStatus(
                        false, null,-1,-1
                    )
            );
        }

        public static final class EnPassantStatus implements Cloneable, Externalizable{

            private static final Long serialVersionUID = 1L;

            public EnPassantStatus() {
                var callerName = Thread.currentThread().getStackTrace()[1].getMethodName();
                if (!callerName.equals("<init>"))
                    throw new IllegalCallerException(this.getClass().getName() + "的无参构造方法不应被调用. ");
            }

            @Override
            public EnPassantStatus clone(){
                try {
                    EnPassantStatus clone = (EnPassantStatus) super.clone();
                    clone.available = available;
                    clone.chessManCanBeCapturedCol = chessManCanBeCapturedCol;
                    clone.chessManCanBeCapturedRow = chessManCanBeCapturedRow;
                    clone.sideCanBeCaptured = sideCanBeCaptured;
                    return clone;
                } catch (CloneNotSupportedException e) {
                    throw new AssertionError();
                }
            }
            public boolean available;
            public ChessMan.Side sideCanBeCaptured;
            public int chessManCanBeCapturedRow;
            public int chessManCanBeCapturedCol;

            public EnPassantStatus(boolean available, ChessMan.Side sideCanBeCaptured,
                                   int row, int col) {
                this.available = available;
                this.sideCanBeCaptured = sideCanBeCaptured;
                this.chessManCanBeCapturedRow = row;
                this.chessManCanBeCapturedCol = col;
            }
            public ChessMan.Side getSideCanBeCaptured(){
                return this.sideCanBeCaptured;
            }

            @Override
            public void writeExternal(ObjectOutput out) throws IOException {
                out.writeBoolean(available);
                out.writeObject(sideCanBeCaptured);
                out.writeInt(chessManCanBeCapturedRow);
                out.writeInt(chessManCanBeCapturedCol);
            }

            @Override
            public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
                available = in.readBoolean();
                sideCanBeCaptured = (ChessMan.Side) in.readObject();
                chessManCanBeCapturedRow = in.readInt();
                chessManCanBeCapturedCol = in.readInt();
            }

            @Override
            public String toString() {
                return "EnPassantStatus{" +
                        "available=" + available +
                        ", sideCanBeCaptured=" + sideCanBeCaptured +
                        ", chessManCanBeCapturedRow=" + chessManCanBeCapturedRow +
                        ", chessManCanBeCapturedCol=" + chessManCanBeCapturedCol +
                        '}';
            }
        }
        public EnPassantStatus enPassantStatus;
    }

    public ChessGame(){
        this.result = Result.NOT_FINISHED;
        this.status = Status.originalStatus();
        this.chessBoard = ChessRule.getOriginalChessBoard();
        nowWhiteBeingChecked = null;
        nowBlackBeingChecked = null;
    }

    public int[] getBoardBytesArray(){
        return chessBoard.getBytesArray();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(chessBoard);
        out.writeObject(result);
        out.writeObject(status);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        this.chessBoard = (ChessBoard) in.readObject();
        this.result = (Result) in.readObject();
        this.status = (Status) in.readObject();
        this.resetNowBeingChecked();
    }
}
