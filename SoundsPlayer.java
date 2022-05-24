package chess.view;

import chess.util.Utils;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.util.*;

public class SoundsPlayer {
    final Map<String, Media> medias;
    public SoundsPlayer(){
        // 找到适合的音效mp3
        medias = new HashMap<>();
        List.of(
                "move",
                "move_failed"
        ).forEach(e->{
            var newMedia = new Media(Utils.getResourcePath("/sound/"+e+".mp3"));

            medias.put(e, newMedia);
        });
    }
    public void play(String soundName){
        var media = medias.get(soundName);
        if (media!=null){
            var mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setAutoPlay(false);
            mediaPlayer.setCycleCount(1);
            mediaPlayer.play();
        }
    }


}

