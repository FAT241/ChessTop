module org.example.chess {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires javafx.media;
    requires javafx.graphics;

    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    requires java.sql;

    opens org.example.chess to javafx.fxml;
    exports org.example.chess;
}
