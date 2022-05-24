package chess.view;

import chess.entity.piece.ChessMan;
import chess.util.Utils;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.effect.Glow;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Pair;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

class ChessButton extends Button {
    private final int row;
    private final int col;
    boolean notEmpty;

    @SuppressWarnings("unused")
    private boolean selected;
    ChessView parent;
    ChessMan.Type type;
    ChessMan.Side side;

    private static final Glow glowEffect = new Glow();
    private static final Border borderSelected =
            new Border(new BorderStroke(
                    Color.GRAY, BorderStrokeStyle.SOLID,
                    CornerRadii.EMPTY,new BorderWidths(4)
            ));
    private static final Border borderNotSelected = Border.EMPTY;

    public void glowOff(){
        this.setEffect(null);
    }
    public void glowOn(){
        this.setEffect(glowEffect);
    }

    ChessButton(ChessView parent, int row, int col) {
        super();
        this.parent = parent;
        this.row = row;
        this.col = col;

        this.selected = false;
        this.notEmpty = false;
        this.type = ChessMan.Type.EMPTY;
        this.side = ChessMan.Side.EMPTY;

        this.setMinSize(50, 50);
        this.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        this.setPadding(Insets.EMPTY);
        this.setBorder(Border.EMPTY);

        this.draw();

        // 鼠标划过事件
        this.setOnMouseEntered(e->this.glowOn());
        this.setOnMouseExited(e->this.glowOff());
        // 鼠标按下事件
        this.setOnMouseClicked(e->
            this.parent.onChessButtonClicked(this)
        );
    }

    public void loadBytesCode(int bytesCode) {
        int iType = bytesCode >> 8;
        this.type = ChessMan.Type.fromByte(iType);
        int iSide = bytesCode & 0xff;
        this.side = ChessMan.Side.fromByte(iSide);

        //noinspection RedundantIfStatement
        if ((iType == 0) && (iSide == 0))
            this.notEmpty = false;
        else
            this.notEmpty = true;
        this.draw();
    }
    private static final Map<Pair<ChessMan.Type,ChessMan.Side>, Background> chessImageResourceMap;
    static {
        Map<Pair<ChessMan.Type, ChessMan.Side>, String> chessImageResourceMap1 = new HashMap<>() {{
            put(new Pair<>(ChessMan.Type.EMPTY, ChessMan.Side.EMPTY),
                    "/image/blank.png");

            put(new Pair<>(ChessMan.Type.KING, ChessMan.Side.WHITE),
                    "/image/king-white.png");
            put(new Pair<>(ChessMan.Type.QUEEN, ChessMan.Side.WHITE),
                    "/image/queen-white.png");
            put(new Pair<>(ChessMan.Type.KNIGHT, ChessMan.Side.WHITE),
                    "/image/knight-white.png");
            put(new Pair<>(ChessMan.Type.BISHOP, ChessMan.Side.WHITE),
                    "/image/bishop-white.png");
            put(new Pair<>(ChessMan.Type.ROOK, ChessMan.Side.WHITE),
                    "/image/rook-white.png");
            put(new Pair<>(ChessMan.Type.PAWN, ChessMan.Side.WHITE),
                    "/image/pawn-white.png");

            put(new Pair<>(ChessMan.Type.KING, ChessMan.Side.BLACK),
                    "/image/king-black.png");
            put(new Pair<>(ChessMan.Type.QUEEN, ChessMan.Side.BLACK),
                    "/image/queen-black.png");
            put(new Pair<>(ChessMan.Type.KNIGHT, ChessMan.Side.BLACK),
                    "/image/knight-black.png");
            put(new Pair<>(ChessMan.Type.BISHOP, ChessMan.Side.BLACK),
                    "/image/bishop-black.png");
            put(new Pair<>(ChessMan.Type.ROOK, ChessMan.Side.BLACK),
                    "/image/rook-black.png");
            put(new Pair<>(ChessMan.Type.PAWN, ChessMan.Side.BLACK),
                    "/image/pawn-black.png");
        }};
        Map<Pair<ChessMan.Type,ChessMan.Side>, Background> chessImageResourceMap2 = new HashMap<>();
        chessImageResourceMap1.forEach((pair,image) ->
            chessImageResourceMap2.put(pair,
                new Background(
                    new BackgroundImage(
                        new Image(Utils.getResourcePath(
                            chessImageResourceMap1.get(pair)
                        )),
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundRepeat.NO_REPEAT,
                        BackgroundPosition.DEFAULT,
                        new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, false)
                    )
                )
            )
        );
        chessImageResourceMap = unmodifiableMap(chessImageResourceMap2);
    }

    public void draw() {
        this.setBackground(
                chessImageResourceMap.get(new Pair<>(this.type,this.side))
        );
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        if (selected) {
            assert this.notEmpty;
            this.setBorder(borderSelected);
        } else
            this.setBorder(borderNotSelected);
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
