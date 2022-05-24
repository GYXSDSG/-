package chess.view;

import chess.util.Utils;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public class BGMPlayer{
    private static final Media[] BGMS =
            {new Media(Utils.getResourcePath("/music/BackgroundMusic0.mp3")),
                    new Media(Utils.getResourcePath("/music/BackgroundMusic1.mp3")),
                    new Media(Utils.getResourcePath("/music/BackgroundMusic2.mp3"))
            };
    private static int mediaPtr;
    private static final int mediasLength;
    static {
        mediasLength = BGMS.length;
        mediaPtr = 0;
    }
    private static int mediaPtrPlusOne(){
        mediaPtr += 1;
        if (mediaPtr == mediasLength){mediaPtr = 0;}
        return mediaPtr;
    }
    private static MediaPlayer mediaPlayer;


    private BGMPlayer(){
    }

    private static BGMPlayer instance;

    public static BGMPlayer getInstance(){
        if (instance == null){
            instance = new BGMPlayer();
            mediaPlayer = new MediaPlayer(BGMS[mediaPtr]);
            mediaPlayer.setAutoPlay(true);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.play();
        }
        return instance;
    }
    public void newBGM(){
        mediaPlayer.stop();
        mediaPlayer = createMediaPlayer(mediaPtrPlusOne());
        System.gc();
    }
    private static MediaPlayer createMediaPlayer (int ptr){
        MediaPlayer newMediaPlayer = new MediaPlayer(BGMS[ptr]);
        newMediaPlayer.setAutoPlay(true);
        newMediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        newMediaPlayer.play();
        return newMediaPlayer;
    }
}

