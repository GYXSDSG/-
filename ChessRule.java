package chess.entity.rule;

import chess.entity.ChessBoard;
import chess.entity.ChessGame;
import chess.entity.move.BaseMove;
import chess.entity.piece.ChessMan;
import chess.entity.piece.ChessMan.Side;
import chess.entity.piece.ChessMan.Type;
import chess.entity.move.Move;
import chess.entity.move.PromotionMove;
import chess.util.Utils;
import javafx.util.Pair;

import java.util.*;

public class ChessRule {

//    static boolean staticFlagDebug = false;
    private ChessRule(){}

    public static ChessBoard getOriginalChessBoard(){

        ChessMan[][] boardArray = new ChessMan[8][8];
        boardArray[7][0] = new ChessMan(Type.ROOK,Side.WHITE);
        boardArray[7][7] = new ChessMan(Type.ROOK,Side.WHITE);
        boardArray[7][1] = new ChessMan(Type.KNIGHT,Side.WHITE);
        boardArray[7][6] = new ChessMan(Type.KNIGHT,Side.WHITE);
        boardArray[7][2] = new ChessMan(Type.BISHOP,Side.WHITE);
        boardArray[7][5] = new ChessMan(Type.BISHOP,Side.WHITE);
        boardArray[7][3] = new ChessMan(Type.KING,Side.WHITE);
        boardArray[7][4] = new ChessMan(Type.QUEEN,Side.WHITE);

        for (int i=0;i<8;i++)
            boardArray[6][i] = new ChessMan(Type.PAWN,Side.WHITE);

        boardArray[0][0] = new ChessMan(Type.ROOK,Side.BLACK);
        boardArray[0][7] = new ChessMan(Type.ROOK,Side.BLACK);
        boardArray[0][1] = new ChessMan(Type.KNIGHT,Side.BLACK);
        boardArray[0][6] = new ChessMan(Type.KNIGHT,Side.BLACK);
        boardArray[0][2] = new ChessMan(Type.BISHOP,Side.BLACK);
        boardArray[0][5] = new ChessMan(Type.BISHOP,Side.BLACK);
        boardArray[0][3] = new ChessMan(Type.KING,Side.BLACK);
        boardArray[0][4] = new ChessMan(Type.QUEEN,Side.BLACK);

        for (int i=0;i<8;i++)
            boardArray[1][i] = new ChessMan(Type.PAWN,Side.BLACK);

        return new ChessBoard(boardArray);
    }

    public static Pair<Boolean,Optional<String>> moveFollowedRuleWithOutCheckedChecking
            (BaseMove move) {
        if (move==null) return Utils.falseWithoutOptional;
        if (move.chessMan().getType()!= Type.KING) {
            var mvRule =
                    MoveRule.moveRules.get(move.chessMan().getType());
            return mvRule.isMoveFollowedRule(move);
        } else {
            return MoveRule.isKingMoveFollowedRuleWithOutCheckedChecking(move);
        }
    }
    public static Pair<Boolean,Optional<String>> moveFollowedRule(BaseMove move) {
        if (move==null) return Utils.falseWithoutOptional;
        var mvRule =
                MoveRule.moveRules.get(move.chessMan().getType());
        return mvRule.isMoveFollowedRule(move);
    }

    /**
     * 进行吃过路兵的状态推演
     */
    public static void enpCapture(ChessGame game, Move move){
        assert game.chessBoard.getChessMan(move.toRow(),move.toCol()) == null;

        game.chessBoard.tryKillChessMan(game.status.enPassantStatus.chessManCanBeCapturedRow,game.status.enPassantStatus.chessManCanBeCapturedCol);
        game.chessBoard.moveChessMan(move.fromRow(), move.fromCol(), move.toRow(), move.toCol());

        if (!ChessRule.ifEncircle(game, move.chessMan().getSide().inverse())) {
            assert (game.status.mainStatus == ChessGame.Status.MainStatus.WAIT_FOR_NORMAL_MOVE);
            game.status.currentSide = game.status.currentSide.inverse();
        } else { // 发生了无棋可走
            game.status.mainStatus = ChessGame.Status.MainStatus.FINISHED;
            game.result = ChessGame.Result.DRAW;
        }
        game.status.enPassantStatus.available = false;

    }
    /**
     * @param move: 只能是NormalMove, 不能是Promotion
     */
    @SuppressWarnings("UnnecessaryReturnStatement")
    public static void changeStatus(ChessGame game, BaseMove move, boolean ifIgnoreEncircle) {
        assert game.chessBoard.getChessMan(move.fromRow(),move.fromCol()) != null;
        assert !(move instanceof PromotionMove);
        game.status.enPassantStatus.available = false;
        var destinationGrid = game.chessBoard.getChessMan(move.toRow(),move.toCol());
        //noinspection IfStatementWithIdenticalBranches
        if (destinationGrid!=null &&(destinationGrid.getType()==Type.KING)) {
            var loser = destinationGrid.getSide();
            game.chessBoard.tryKillChessMan(move.toRow(), move.toCol());
            game.chessBoard.moveChessMan(move.fromRow(), move.fromCol(), move.toRow(), move.toCol());
            game.status.mainStatus = ChessGame.Status.MainStatus.FINISHED;
            game.result = (loser==Side.BLACK)? ChessGame.Result.WHITE_WIN: ChessGame.Result.BLACK_WIN;
            return;
        } else {
            var firstMoved = move.chessMan().isFirstMoved();
            game.chessBoard.tryKillChessMan(move.toRow(), move.toCol());
            game.chessBoard.moveChessMan(move.fromRow(), move.fromCol(), move.toRow(), move.toCol());
            if ((ifIgnoreEncircle)||(!ChessRule.ifEncircle(game, move.chessMan().getSide().inverse()))) {
                assert (game.status.mainStatus == ChessGame.Status.MainStatus.WAIT_FOR_NORMAL_MOVE);
                if (!ChessRule.checkPromotable(move)) { // 如果不能升变
                    game.status.currentSide = game.status.currentSide.inverse();
                    if (ChessRule.checkPawnFirstMove(move, firstMoved)){
                        game.status.enPassantStatus.available = true;
                        game.status.enPassantStatus.sideCanBeCaptured = move.chessMan().getSide();
                        game.status.enPassantStatus.chessManCanBeCapturedRow = move.toRow();
                        game.status.enPassantStatus.chessManCanBeCapturedCol = move.toCol();
                    }
                } else {  // 可以Promote
                    game.status.mainStatus = ChessGame.Status.MainStatus.WAIT_FOR_PROMOTION_CHOICE_GIVEN;
                }
            } else { // 发生了无棋可走
                game.status.mainStatus = ChessGame.Status.MainStatus.FINISHED;
                game.result = ChessGame.Result.DRAW;
            }
            return;
        }
    }


    public static void changeStatusIgnoringEncircle(ChessGame game, BaseMove move) {
        changeStatus(game, move, true);
    }

    public static boolean ifKillEnemyKing(ChessGame game, BaseMove move) {
        if (move==null) return false;
        assert game.chessBoard.getChessMan(move.fromRow(),move.fromCol()) != null;
        assert !(move instanceof PromotionMove);
        var destinationGrid = game.chessBoard.getChessMan(move.toRow(),move.toCol());
        //noinspection RedundantIfStatement
        if (destinationGrid!=null &&(destinationGrid.getType()==Type.KING)&&(destinationGrid.getSide()!=move.chessMan().getSide()))
            return true;
        return false;
    }

    private static boolean checkPawnFirstMove(BaseMove move, boolean firstMoved) {
        return (move.chessMan().getType() == Type.PAWN)
                && (!firstMoved)
                && (Math.abs(move.toRow()-move.fromRow()) == 2);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean checkPromotable(BaseMove move) {
        if (move instanceof PromotionMove) return false;  // 如果已经是promote了
        if (move.chessMan().getType()!=Type.PAWN)
            return false;
        else {
            if ((move.chessMan().getSide()==Side.WHITE)
                && (move.toRow()==0)
            ) return true;
            else //noinspection RedundantIfStatement
            if ((move.chessMan().getSide()==Side.BLACK)
                && (move.toRow()==7)
            ) return true;
            else return false;
        }
    }

    private static final Map<Type, int[][]> testPositions = new HashMap<>(){{
        put(Type.KING, new int[][]{
                {0,1},{0,-1},{1,0},{-1,0},{1,1},{1,-1},{-1,1},{-1,-1}
        });
        put(Type.ROOK, new int[][]{
                {0,-7},{0,-6},{0,-5},{0,-4},{0,-3},{0,-2},{0,-1},
                {0,1},{0,2},{0,3},{0,4},{0,5},{0,6},{0,7},
                {-7,0},{-6,0},{-5,0},{-4,0},{-3,0},{-2,0},{-1,0},
                {1,0},{2,0},{3,0},{4,0},{5,0},{6,0},{7,0}
        });
        put(Type.BISHOP, new int[][]{
                {-7,-7},{-6,-6},{-5,-5},{-4,-4},{-3,-3},{-2,-2},{-1,-1},
                {1,1},{2,2},{3,3},{4,4},{5,5},{6,6},{7,7},
                {-1,1},{-2,2},{-3,3},{-4,4},{-5,5},{-6,6},{-7,7},
                {1,-1},{2,-2},{3,-3},{4,-4},{5,-5},{6,-6},{7,-7}
        });
        put(Type.QUEEN, new int[][]{
                {0,-7},{0,-6},{0,-5},{0,-4},{0,-3},{0,-2},{0,-1},
                {0,1},{0,2},{0,3},{0,4},{0,5},{0,6},{0,7},
                {-7,0},{-6,0},{-5,0},{-4,0},{-3,0},{-2,0},{-1,0},
                {1,0},{2,0},{3,0},{4,0},{5,0},{6,0},{7,0},

                {-7,-7},{-6,-6},{-5,-5},{-4,-4},{-3,-3},{-2,-2},{-1,-1},
                {1,1},{2,2},{3,3},{4,4},{5,5},{6,6},{7,7},
                {-1,1},{-2,2},{-3,3},{-4,4},{-5,5},{-6,6},{-7,7},
                {1,-1},{2,-2},{3,-3},{4,-4},{5,-5},{6,-6},{7,-7}
        });
        put(Type.KNIGHT, new int[][]{
                {1,2},{-1,2},{1,-2},{-1,-2},
                {2,1},{-2,1},{2,-1},{-2,-1}
        });
        put(Type.PAWN, new int[][]{
                {1,0},{-1,0},{1,1},{1,-1},{-1,1},{-1,-1}
        });
    }};

    private static boolean ifSideChecked(ChessGame game, Side side){
        var newSide = side.inverse();  // 转换为攻击方
        ChessBoard chessBoard = game.chessBoard;
        ArrayList<Pair<ChessMan, BaseMove>> chessManArray = new ArrayList<>();

        for (var chessMan:chessBoard.getSideChessManList(newSide).keySet()){
            var tempPair = chessBoard.getSideChessManList(newSide).get(chessMan);
            var row = tempPair.getKey();
            var col = tempPair.getValue();
            chessManArray.add(new Pair<>(
                    chessMan,
                    BaseMove.createEmptyEditableMove(chessMan, row, col, game)
            ));
        }
        for (Pair<ChessMan, BaseMove> tuple : chessManArray) {
            var chessMan = tuple.getKey();
            var testMove = tuple.getValue();
            for (int[] pair : testPositions.get(chessMan.getType())) {
                int rowOffset = pair[0];
                int colOffset = pair[1];
                var tempGame = game.clone();
                var tempPair =
                        ChessRule.moveFollowedRuleWithOutCheckedChecking(testMove.setTestInstance(rowOffset, colOffset));
                if (tempPair.getKey()){
                    if (ChessRule.ifKillEnemyKing(tempGame, testMove)) {
                        return true;
                }}
            }
        }
        return false;
    }

    /**
     * @param side: 当前正要走棋的一方
     * @return true说明困毙，false说明没有
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean ifEncircle(ChessGame game, Side side) {
        ChessBoard chessBoard = game.chessBoard;
        ArrayList<Pair<ChessMan, BaseMove>> chessManArray = new ArrayList<>();

        for (ChessMan chessMan:chessBoard.getSideChessManList(side).keySet()){
            var tempPair = chessBoard.getSideChessManList(side).get(chessMan);

            var row = tempPair.getKey();
            var col = tempPair.getValue();
            if (chessMan.getSide()!=side) continue;
            chessManArray.add(new Pair<>(
                    chessMan,
                    BaseMove.createEmptyEditableMove(chessMan, row, col, game)
            ));
        }
        for (Pair<ChessMan, BaseMove> tuple : chessManArray) {
            var chessMan = tuple.getKey();
            var testMove = tuple.getValue();
            for (int[] pair : testPositions.get(chessMan.getType())) {
                int rowOffset = pair[0];
                int colOffset = pair[1];
                var boolPair = MoveRule.moveRules.get(chessMan.getType())
                        .isMoveFollowedRule(
                                testMove.setTestInstance(rowOffset, colOffset)
                        );
                boolean isMovable = boolPair.getKey();
                if (isMovable)
                    return false;
            }
        }
        return true;
    }

    public static void promote(ChessGame game, PromotionMove move) {
        assert (game.status.mainStatus == ChessGame.Status.MainStatus.WAIT_FOR_PROMOTION_CHOICE_GIVEN);
        game.chessBoard.tryKillChessMan(move.fromRow(),move.fromCol());
        game.chessBoard.createChessMan(move.fromRow(),move.fromCol(),move.newChessMan());

        if (game.status.mainStatus == ChessGame.Status.MainStatus.WAIT_FOR_PROMOTION_CHOICE_GIVEN) {
            game.status.currentSide = game.status.currentSide.inverse();
            game.status.mainStatus = ChessGame.Status.MainStatus.WAIT_FOR_NORMAL_MOVE;
        }
    }

    private static class MoveRule {

        public static final Map<ChessMan.Type, MoveRule> moveRules = new HashMap<>();

        public static Pair<Boolean, Optional<String>> isKingMoveFollowedRuleWithOutCheckedChecking(BaseMove mv)  {
            // todo: 没考虑王车易位
            if (Math.max(Math.abs(mv.toRow() - mv.fromRow()), Math.abs(mv.toCol() - mv.fromCol())) != 1){
                return Utils.falseWithoutOptional;
            } else {
                return Utils.trueWithoutOptional;
            }
        }

        @FunctionalInterface
        private interface PredicateWithInfo{
            Pair<Boolean, Optional<String>> test(BaseMove mv);
        }
        private final PredicateWithInfo checkFollowedRuleFunction;
        public Pair<Boolean, Optional<String>> isMoveFollowedRule(BaseMove move) {
            if (move == null) return Utils.falseWithoutOptional;
            return checkFollowedRuleFunction.test(move);
        }

        private MoveRule(ChessMan.Type type, PredicateWithInfo checkFunction) {
            this.checkFollowedRuleFunction = checkFunction;
            MoveRule.moveRules.put(type, this);
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        private static boolean checkIfBarrier(BaseMove mv){
            int towardsRow = Integer.compare(mv.toRow() - mv.fromRow(), 0);
            int towardsCol = Integer.compare(mv.toCol() - mv.fromCol(), 0);

            for (
                    int row=mv.fromRow()+towardsRow, col=mv.fromCol()+towardsCol;
                    (row!=mv.toRow()) || (col!=mv.toCol());
                    row+=towardsRow, col+=towardsCol
            )   if (mv.environmentBoard().getChessMan(row,col)!=null)
                return true;
            return false;
        }
        static {
            var rookRule =
                new MoveRule(Type.ROOK, mv -> {
                    if (((mv.fromRow() == mv.toRow()) || (mv.fromCol() == mv.toCol()))
                        &&
                        !checkIfBarrier(mv)
                        &&
                        !((mv.environmentBoard().getChessMan(mv.toRow(),mv.toCol())!=null) &&
                            mv.environmentBoard().getChessMan(mv.toRow(),mv.toCol()).getSide()==mv.chessMan().getSide()
                        )
                    )
                    return Utils.trueWithoutOptional;
                    else return Utils.falseWithoutOptional;
                });
            new MoveRule(Type.KNIGHT, mv -> {
                if ((switch ((mv.toCol() - mv.fromCol()) * (mv.toRow() - mv.fromRow())) {
                    case -2, 2 -> true;
                    default -> false;
                }) &&
                !((mv.environmentBoard().getChessMan(mv.toRow(),mv.toCol())!=null) &&
                (mv.environmentBoard().getChessMan(mv.toRow(),mv.toCol()).getSide()==mv.chessMan().getSide()))
                ) return Utils.trueWithoutOptional;
                else return Utils.falseWithoutOptional;
            });
            var bishopRule =
                new MoveRule(Type.BISHOP, mv -> {
                    if ((Math.abs(mv.toRow() - mv.fromRow()) == Math.abs(mv.toCol() - mv.fromCol()))
                        &&
                        !checkIfBarrier(mv) &&
                            !((mv.environmentBoard().getChessMan(mv.toRow(),mv.toCol())!=null) &&
                                    (mv.environmentBoard().getChessMan(mv.toRow(),mv.toCol()).getSide()==mv.chessMan().getSide()))
                    ) return Utils.trueWithoutOptional;
                    else return Utils.falseWithoutOptional;
                });
            new MoveRule(Type.KING, mv -> {
                // todo: 没考虑王车易位
                if ((Math.max(Math.abs(mv.toRow() - mv.fromRow()), Math.abs(mv.toCol() - mv.fromCol())) != 1) ||
                        ((mv.environmentBoard().getChessMan(mv.toRow(),mv.toCol())!=null) &&
                                (mv.environmentBoard().getChessMan(mv.toRow(),mv.toCol()).getSide()==mv.chessMan().getSide()))
                ){
                    return Utils.falseWithoutOptional;
                } else {
                    var tempGame = mv.environment().clone();
                    ChessRule.changeStatusIgnoringEncircle(tempGame, mv);
                    if (ifSideChecked(tempGame, mv.chessMan().getSide())) {
                        return Utils.falseWithoutOptional;
                    }
                    else {
                        return Utils.trueWithoutOptional;
                    }
                }
            });
            new MoveRule(Type.QUEEN, mv -> {
                if (rookRule.isMoveFollowedRule(mv).getKey() || bishopRule.isMoveFollowedRule(mv).getKey())
                    return Utils.trueWithoutOptional;
                else return Utils.falseWithoutOptional;
            });
            new MoveRule(Type.PAWN, mv -> {
                boolean retValue;
                Optional<String> enp = Optional.empty();
                int correctForward = switch (mv.chessMan().getSide()){
                    case WHITE -> -1;
                    case BLACK -> 1;
                    default -> throw new IllegalStateException("Unexpected value: " + mv.chessMan().getSide());
                };
                int forward = Integer.compare(mv.toRow() - mv.fromRow(),0);
                if (forward != correctForward) retValue = false;
                else if (mv.toCol() == mv.fromCol()){ //  可能是走
                    if (!mv.chessMan().isFirstMoved())
                        retValue = (mv.environmentBoard().getChessMan(mv.toRow(), mv.toCol())==null)
                            && Math.abs(mv.toRow() - mv.fromRow()) <= 2;
                    else retValue = (mv.environmentBoard().getChessMan(mv.toRow(), mv.toCol())==null)
                            && Math.abs(mv.toRow() - mv.fromRow()) <= 1;
                } else if (
                        (Math.abs(mv.toCol()-mv.fromCol())==1)
                        &&
                        (Math.abs(mv.toRow()-mv.fromRow())==1)
                ){ // 吃子
                    if (mv.environmentBoard().getChessMan(mv.toRow(), mv.toCol())!=null &&
                            (mv.environmentBoard().getChessMan(mv.toRow(), mv.toCol()).getSide()!=mv.chessMan().getSide()))
                        retValue = true;
                    else { // 检查是否是吃过路兵
                        var enpStatus = mv.environment().status.enPassantStatus;
                        if (enpStatus.available){
                            var destinationRow =
                                switch (enpStatus.getSideCanBeCaptured()){
                                    case BLACK -> -1;
                                    case WHITE -> 1;
                                    default -> throw new IllegalStateException("Unexpected value: " + enpStatus.getSideCanBeCaptured());
                                } + enpStatus.chessManCanBeCapturedRow;
                            var destinationCol = enpStatus.chessManCanBeCapturedCol;
                            if ((destinationRow==mv.toRow()) && (destinationCol==mv.toCol())){
                                enp = Optional.of("enp");
                                retValue = true;
                            }
                            else retValue = false;
                        }
                        else retValue = false;
                    }
                } else retValue = false;
                return new Pair<>(retValue, enp);
            });
        }
    }
}
