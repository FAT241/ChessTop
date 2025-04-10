package org.example.chess;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class VictoryPanel {
    private Stage primaryStage;
    private Runnable resetCallback;
    private ChessGame chessGame;

    public VictoryPanel(Stage primaryStage, Runnable resetCallback, ChessGame chessGame) {
        this.primaryStage = primaryStage;
        this.resetCallback = resetCallback;
        this.chessGame = chessGame;
    }

    public Scene createVictoryScene(String winner) {
        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);

        try {
            Image backgroundImage = new Image(getClass().getResourceAsStream("/pieces/victory_background.jpg"));
            BackgroundImage background = new BackgroundImage(
                    backgroundImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
            );
            layout.setBackground(new Background(background));
        } catch (Exception e) {
            layout.setBackground(new Background(new BackgroundFill(Color.LIGHTGOLDENRODYELLOW, null, null)));
        }

        Label victoryLabel = new Label(winner + " WIN!");
        victoryLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #FFD700;");
        victoryLabel.setEffect(new DropShadow(10, Color.BLACK));

        Button playAgainButton = new Button("Play Again");
        playAgainButton.setStyle("-fx-font-size: 20px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 15 30;");
        playAgainButton.setOnAction(e -> {
            resetCallback.run();
            primaryStage.centerOnScreen();
        });

        Button historyButton = new Button("Game History");
        historyButton.setStyle("-fx-font-size: 20px; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 15 30;");
        historyButton.setOnAction(e -> chessGame.showGameHistory(layout.getScene()));

        Button exitButton = new Button("Exit");
        exitButton.setStyle("-fx-font-size: 20px; -fx-background-color: #F44336; -fx-text-fill: white; -fx-padding: 15 30;");
        exitButton.setOnAction(e -> primaryStage.close());

        FadeTransition fadeLabel = new FadeTransition(Duration.millis(1000), victoryLabel);
        fadeLabel.setFromValue(0);
        fadeLabel.setToValue(1);

        FadeTransition fadePlayAgain = new FadeTransition(Duration.millis(1000), playAgainButton);
        fadePlayAgain.setFromValue(0);
        fadePlayAgain.setToValue(1);
        fadePlayAgain.setDelay(Duration.millis(500));

        FadeTransition fadeHistory = new FadeTransition(Duration.millis(1000), historyButton);
        fadeHistory.setFromValue(0);
        fadeHistory.setToValue(1);
        fadeHistory.setDelay(Duration.millis(1000));

        FadeTransition fadeExit = new FadeTransition(Duration.millis(1000), exitButton);
        fadeExit.setFromValue(0);
        fadeExit.setToValue(1);
        fadeExit.setDelay(Duration.millis(1500));

        try {
            AudioClip victorySound = new AudioClip(getClass().getResource("/sounds/victory.wav").toString());
            victorySound.play();
        } catch (Exception e) {
            System.out.println("Cannot play Victory Sound: " + e.getMessage());
        }

        layout.getChildren().addAll(victoryLabel, playAgainButton, historyButton, exitButton);

        fadeLabel.play();
        fadePlayAgain.play();
        fadeHistory.play();
        fadeExit.play();

        Scene scene = new Scene(layout, Board.getTileSize() * Board.getBoardSize(), Board.getTileSize() * Board.getBoardSize() + 60);
        primaryStage.setResizable(true);
        return scene;
    }
}