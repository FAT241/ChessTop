package org.example.chess;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
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

public class GameOutcomePanel {
    private Stage primaryStage;
    private Runnable resetGameCallback;
    private ChessGame chessGame;
    private String result;
    private String soundFile;

    public GameOutcomePanel(Stage primaryStage, Runnable resetGameCallback, ChessGame chessGame, String result, String soundFile) {
        this.primaryStage = primaryStage;
        this.resetGameCallback = resetGameCallback;
        this.chessGame = chessGame;
        this.result = result;
        this.soundFile = soundFile;
    }

    public Scene createOutcomeScene() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        // Phát âm thanh
        try {
            AudioClip sound = new AudioClip(getClass().getResource("/sounds/" + soundFile).toString());
            sound.play();
        } catch (Exception e) {
            System.err.println("Cannot play sound: " + e.getMessage());
        }

        // Thiết lập hình nền
        try {
            String backgroundPath = soundFile.equals("victory.wav") ? "/pieces/victory_background.jpg" : "/pieces/background1.PNG";
            Image backgroundImage = new Image(getClass().getResourceAsStream(backgroundPath));
            BackgroundImage background = new BackgroundImage(
                    backgroundImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
            );
            layout.setBackground(new Background(background));
        } catch (Exception e) {
            Color backgroundColor = soundFile.equals("victory.wav") ? Color.LIGHTGOLDENRODYELLOW :
                    soundFile.equals("lose.wav") ? Color.LIGHTCORAL : Color.LIGHTGRAY;
            layout.setBackground(new Background(new BackgroundFill(backgroundColor, null, null)));
            System.err.println("Cannot upload image: " + e.getMessage());
        }

        // Nhãn kết quả
        Label resultLabel = new Label(result);
        resultLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
        resultLabel.setEffect(new DropShadow(10, Color.BLACK));

        // Nhãn nước đi cuối
        Label lastMoveLabel = new Label("Nước đi cuối: " + chessGame.getLastMoveNotation());
        lastMoveLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #FFFFFF;");

        // Nút Play Again
        Button playAgainButton = new Button("Play Again");
        playAgainButton.setStyle("-fx-font-size: 20px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 15 30; -fx-background-radius: 5;");
        playAgainButton.setEffect(new DropShadow(5, Color.GRAY));
        playAgainButton.setOnAction(e -> {
            resetGameCallback.run();
            primaryStage.centerOnScreen();
        });

        // Nút Game History
        Button historyButton = new Button("Game History");
        historyButton.setStyle("-fx-font-size: 20px; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 15 30; -fx-background-radius: 5;");
        historyButton.setEffect(new DropShadow(5, Color.GRAY));
        historyButton.setOnAction(e -> chessGame.showGameHistory(chessGame.getGameScene()));

        // Nút Exit
        Button exitButton = new Button("Exit");
        exitButton.setStyle("-fx-font-size: 20px; -fx-background-color: #F44336; -fx-text-fill: white; -fx-padding: 15 30; -fx-background-radius: 5;");
        exitButton.setEffect(new DropShadow(5, Color.GRAY));
        exitButton.setOnAction(e -> primaryStage.close());

        layout.getChildren().addAll(resultLabel, lastMoveLabel, playAgainButton, historyButton, exitButton);

        return new Scene(layout, Board.getTileSize() * Board.getBoardSize(), Board.getTileSize() * Board.getBoardSize() + 60);
    }
}