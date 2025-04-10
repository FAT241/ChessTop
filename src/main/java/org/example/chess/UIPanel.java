package org.example.chess;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class UIPanel {
    private Stage primaryStage;
    private Runnable startGameCallback;
    private ChessGame chessGame;

    public UIPanel(Stage primaryStage, Runnable startGameCallback, ChessGame chessGame) {
        this.primaryStage = primaryStage;
        this.startGameCallback = startGameCallback;
        this.chessGame = chessGame;
    }

    public Scene createScene() {
        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);

        try {
            Image backgroundImage = new Image(getClass().getResourceAsStream("/pieces/background1.PNG"));
            BackgroundImage background = new BackgroundImage(
                    backgroundImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
            );
            layout.setBackground(new Background(background));
        } catch (Exception e) {
            layout.setBackground(new Background(new BackgroundFill(Color.LIGHTBLUE, null, null)));
        }

        Label titleLabel = new Label("Welcome to Chess Game");
        titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
        titleLabel.setEffect(new DropShadow(10, Color.BLACK));

        Button startButton = new Button("Start Game");
        startButton.setStyle("-fx-font-size: 20px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 15 30;");
        startButton.setOnAction(e -> {
            startGameCallback.run();
            primaryStage.centerOnScreen();
        });

        Button historyButton = new Button("Game History");
        historyButton.setStyle("-fx-font-size: 20px; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 15 30;");
        historyButton.setOnAction(e -> chessGame.showGameHistory(layout.getScene()));

        Button exitButton = new Button("Exit");
        exitButton.setStyle("-fx-font-size: 20px; -fx-background-color: #F44336; -fx-text-fill: white; -fx-padding: 15 30;");
        exitButton.setOnAction(e -> primaryStage.close());

        layout.getChildren().addAll(titleLabel, startButton, historyButton, exitButton);
        Scene scene = new Scene(layout, Board.getTileSize() * Board.getBoardSize(), Board.getTileSize() * Board.getBoardSize() + 60);
        primaryStage.setResizable(true);
        return scene;
    }
}