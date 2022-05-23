package chess.util;


import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.util.Objects;
import java.util.Optional;

public class Utils {

    private static Image icon;

    private Utils(){}
    private static final String jarPath;
    private static final boolean isInJar;
    static {
        // 检查是运行在文件目录还是在jar包里
        jarPath = Utils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        isInJar = !(new File(jarPath)).isDirectory();
    }

    public static String getResourcePath(String inputPath){
        var path = inputPath;
        if (isInJar) {
            path = String.format("jar:file:%s!%s", jarPath, path);
        } else {
            path = Objects.requireNonNull(Utils.class.getResource(path)).toExternalForm();
        }
        return path;
    }

    public static Image getIcon() {
        if (icon==null)
            icon = new Image(Utils.getResourcePath("/image/windowIcon.png"));
        return icon;
    }

    public static void setDialogIcon(@SuppressWarnings("rawtypes") Dialog dialog){
        ((Stage)dialog.getDialogPane().getScene().getWindow()).getIcons().add(icon);
    }

    public static final Pair<Boolean, Optional<String>> falseWithoutOptional
            = new Pair<>(false, Optional.empty());
    public static final Pair<Boolean, Optional<String>> trueWithoutOptional
            = new Pair<>(true, Optional.empty());


}
