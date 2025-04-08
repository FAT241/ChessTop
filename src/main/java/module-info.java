module org.example.chess {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens org.example.chess to javafx.fxml;
    exports org.example.chess;
}