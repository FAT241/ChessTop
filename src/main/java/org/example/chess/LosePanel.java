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

public class LosePanel {
    private Stage primaryStage;
    private Runnable resetCallback;
    private ChessGame chessGame;

    public LosePanel(Stage primaryStage, Runnable resetCallback, ChessGame chessGame) {
        this.primaryStage = primaryStage;
        this.resetCallback = resetCallback;
        this.chessGame = chessGame;
    }

    public Scene createLoseScene(String winner) {
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
            System.out.println("Cannot load background image: " + e.getMessage());
            layout.setBackground(new Background(new BackgroundFill(Color.LIGHTGOLDENRODYELLOW, null, null)));
        }

        Label loseLabel = new Label("YOU LOSE! " + winner + " WINS!");
        loseLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #FFD700;");
        loseLabel.setEffect(new DropShadow(10, Color.BLACK));

        String lastMoveNotation = chessGame.getLastMoveNotation();
        Label moveLabel = new Label("Checkmate by: " + lastMoveNotation);
        moveLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #FFFFFF;");
        moveLabel.setEffect(new DropShadow(5, Color.BLACK));

        Button playAgainButton = new Button("Play Again");
        playAgainButton.setStyle("-fx-font-size: 20px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 15 30;");
        playAgainButton.setOnAction(e -> {
            resetCallback.run(); // Gọi resetGame
            primaryStage.setScene(chessGame.getGameScene()); // Quay lại gameScene
            primaryStage.centerOnScreen();
        });

        Button historyButton = new Button("Game History");
        historyButton.setStyle("-fx-font-size: 20px; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 15 30;");
        historyButton.setOnAction(e -> chessGame.showGameHistory(layout.getScene()));

        Button exitButton = new Button("Exit");
        exitButton.setStyle("-fx-font-size: 20px; -fx-background-color: #F44336; -fx-text-fill: white; -fx-padding: 15 30;");
        exitButton.setOnAction(e -> primaryStage.close());

        layout.getChildren().addAll(loseLabel, moveLabel, playAgainButton, historyButton, exitButton);

        Scene scene = new Scene(layout, Board.getTileSize() * Board.getBoardSize(), Board.getTileSize() * Board.getBoardSize() + 60);
        primaryStage.setResizable(true);
        return scene;
    }
}