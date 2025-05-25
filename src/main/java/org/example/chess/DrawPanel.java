package org.example.chess;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class DrawPanel {
    private Stage primaryStage;
    private Runnable resetGameCallback;
    private ChessGame chessGame;

    public DrawPanel(Stage primaryStage, Runnable resetGameCallback, ChessGame chessGame) {
        this.primaryStage = primaryStage;
        this.resetGameCallback = resetGameCallback;
        this.chessGame = chessGame;
    }

    public Scene createDrawScene(String result) {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        try {
            AudioClip drawSound = new AudioClip(getClass().getResource("/pieces/draw_sound.wav").toString());
            drawSound.play();
        } catch (Exception e) {
            System.err.println("Cannot play draw sound: " + e.getMessage());
        }

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
            layout.setBackground(new Background(new BackgroundFill(Color.LIGHTGRAY, null, null)));
            System.err.println("Cannot Upload Image: " + e.getMessage());
        }

        Label drawLabel = new Label(result);
        drawLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
        drawLabel.setEffect(new DropShadow(10, Color.BLACK));

        Label lastMoveLabel = new Label("Nước đi cuối: " + chessGame.getLastMoveNotation());
        lastMoveLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #FFFFFF;");

        Button playAgainButton = new Button("Play Again");
        playAgainButton.setStyle("-fx-font-size: 20px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 15 30; -fx-background-radius: 5;");
        playAgainButton.setEffect(new DropShadow(5, Color.GRAY));
        playAgainButton.setOnAction(e -> {
            resetGameCallback.run();
            primaryStage.centerOnScreen();
        });

        Button historyButton = new Button("Game History");
        historyButton.setStyle("-fx-font-size: 20px; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 15 30; -fx-background-radius: 5;");
        historyButton.setEffect(new DropShadow(5, Color.GRAY));
        historyButton.setOnAction(e -> chessGame.showGameHistory(chessGame.getGameScene()));

        Button exitButton = new Button("Exit");
        exitButton.setStyle("-fx-font-size: 20px; -fx-background-color: #F44336; -fx-text-fill: white; -fx-padding: 15 30; -fx-background-radius: 5;");
        exitButton.setEffect(new DropShadow(5, Color.GRAY));
        exitButton.setOnAction(e -> primaryStage.close());

        layout.getChildren().addAll(drawLabel, lastMoveLabel, playAgainButton, historyButton, exitButton);

        return new Scene(layout, Board.getTileSize() * Board.getBoardSize(), Board.getTileSize() * Board.getBoardSize() + 60);
    }
}