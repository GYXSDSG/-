module chess {
    requires javafx.controls;
    requires javafx.media;
    requires javafx.base;
    requires javafx.graphics;

    opens chess to javafx.fxml;
    exports chess;
}