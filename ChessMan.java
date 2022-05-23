package chess.entity.piece;

import java.io.Serializable;
import java.util.HashMap;

public class ChessMan implements Cloneable, Serializable {

    private static final Long serialVersionUID = 1L;

    @Override
    public int hashCode(){
        return System.identityHashCode(this);
    }

    public enum Type {
        EMPTY((char)0, 0),
        KING('K',1), QUEEN('Q', 2), ROOK('R',3),
        KNIGHT('N',4), BISHOP('B',5), PAWN('S',6);
        private final char key;
        private final int id;
        private static final HashMap<Integer,Type> fromByteMap =
            new HashMap<>(){{
                for (Type type:Type.values())
                    put(type.id, type);
            }};

        public static Type fromByte(int byte_){
            assert byte_>=0 && byte_<=127;
            return fromByteMap.get(byte_);
        }
        public String getKey(){return String.valueOf(key);}
//        public int getId(){return this.id;}
        Type(char key, int id) {
            this.key = key;
            assert id>=0 && id<128;
            this.id = (byte) id;
        }
    }

    public enum Side {
        EMPTY(0), WHITE(1), BLACK(2);
        private static final HashMap<Integer,Side> fromByteMap =
            new HashMap<>(){{
                for (Side side:Side.values())
                    put(side.id, side);
            }};
        public static Side fromByte(int byte_){
            assert byte_>=0 && byte_<=127;
            return fromByteMap.get(byte_);
        }
        private final int id;
//        public int getId(){return this.id;}
        Side(int id){
            assert id>=0 && id<128;
            this.id = id;
        }

        public Side inverse() {
            assert this!=EMPTY;
            if (this==BLACK) return WHITE;
            else return BLACK;
        }
    }

    private final Type type;
    private final Side side;
    private final int bytesCode; // 0~32767
    private boolean firstMoved;

    public Side getSide(){ return this.side; }
    public Type getType() { return this.type; }

    public ChessMan(Type type, Side side) {
        this.type = type;
        this.side = side;
        this.bytesCode = (type.id<<8) | (side.id);
        this.firstMoved = false;
    }

    public Object clone(){
        Object obj = null;
        try{
            obj = super.clone();
        }catch (Exception e){
            e.printStackTrace();
        }
        return obj;
    }

    @Override
    public String toString() {
        char lBracket = '-';
        char rBracket = '-';

        if (side == Side.BLACK) {
            lBracket = '[';
            rBracket = ']';
        } else if (side == Side.WHITE){
            lBracket = '<';
            rBracket = '>';
        }
        return lBracket + this.getType().getKey() + rBracket;
    }

    public int getBytesCode() {return this.bytesCode;}

    public boolean isFirstMoved() {
        return firstMoved;
    }

    public void setFirstMoved(boolean firstMoved) {
        this.firstMoved = firstMoved;
    }

}
