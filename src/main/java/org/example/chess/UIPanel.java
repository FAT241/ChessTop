package org.example.chess;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

public class UIPanel {
    private Stage primaryStage;
    private Runnable startGameCallback;
    private ChessGame chessGame;
    private TextArea moveList;
    private static final double MIN_TILE_SIZE = 50; // Giảm để bàn cờ nhỏ hơn
    private static final double MAX_TILE_SIZE = 110; // Tăng để bàn cờ lớn hơn
    private String difficulty = "Easy"; // Mặc định là Dễ
    private String timeLimit = "Không giới hạn";
    private String gameMode = "Standard";
    private Label difficultyLabel;
    private Label timeLimitLabel;
    private Label gameModeLabel;
    private Button easyButton, mediumButton, hardButton;
    private ComboBox<String> timeLimitBox;

    private static final double SCENE_WIDTH = 1000; // Tăng kích thước khung hình
    private static final double SCENE_HEIGHT = 750;

    public UIPanel(Stage primaryStage, Runnable startGameCallback, ChessGame chessGame) {
        this.primaryStage = primaryStage;
        this.startGameCallback = startGameCallback;
        this.chessGame = chessGame;
    }

    public Scene createMainMenuScene() {
        VBox layout = new VBox(30);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

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
            System.err.println("Cannot load image: " + e.getMessage());
        }

        Label titleLabel = new Label("Chess Game");
        titleLabel.setStyle("-fx-font-size: 48px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial';");
        titleLabel.setEffect(new DropShadow(15, Color.BLACK));

        Button playButton = new Button("Vào game");
        playButton.setStyle("-fx-font-size: 24px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 15 40; -fx-background-radius: 10; -fx-font-weight: bold;");
        playButton.setEffect(new DropShadow(5, Color.GRAY));
        playButton.setOnMouseEntered(e -> playButton.setStyle("-fx-font-size: 24px; -fx-background-color: #45A049; -fx-text-fill: white; -fx-padding: 15 40; -fx-background-radius: 10; -fx-font-weight: bold;"));
        playButton.setOnMouseExited(e -> playButton.setStyle("-fx-font-size: 24px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 15 40; -fx-background-radius: 10; -fx-font-weight: bold;"));
        playButton.setOnAction(e -> primaryStage.setScene(createSettingsScene()));

        Button historyButton = new Button("Lịch sử trận đấu");
        historyButton.setStyle("-fx-font-size: 24px; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 15 40; -fx-background-radius: 10; -fx-font-weight: bold;");
        historyButton.setEffect(new DropShadow(5, Color.GRAY));
        historyButton.setOnMouseEntered(e -> historyButton.setStyle("-fx-font-size: 24px; -fx-background-color: #1E88E5; -fx-text-fill: white; -fx-padding: 15 40; -fx-background-radius: 10; -fx-font-weight: bold;"));
        historyButton.setOnMouseExited(e -> historyButton.setStyle("-fx-font-size: 24px; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 15 40; -fx-background-radius: 10; -fx-font-weight: bold;"));
        historyButton.setOnAction(e -> chessGame.showGameHistory(createMainMenuScene()));

        Button exitButton = new Button("Thoát");
        exitButton.setStyle("-fx-font-size: 24px; -fx-background-color: #F44336; -fx-text-fill: white; -fx-padding: 15 40; -fx-background-radius: 10; -fx-font-weight: bold;");
        exitButton.setEffect(new DropShadow(5, Color.GRAY));
        exitButton.setOnMouseEntered(e -> exitButton.setStyle("-fx-font-size: 24px; -fx-background-color: #E53935; -fx-text-fill: white; -fx-padding: 15 40; -fx-background-radius: 10; -fx-font-weight: bold;"));
        exitButton.setOnMouseExited(e -> exitButton.setStyle("-fx-font-size: 24px; -fx-background-color: #F44336; -fx-text-fill: white; -fx-padding: 15 40; -fx-background-radius: 10; -fx-font-weight: bold;"));
        exitButton.setOnAction(e -> primaryStage.close());

        layout.getChildren().addAll(titleLabel, playButton, historyButton, exitButton);

        Scene scene = new Scene(layout, SCENE_WIDTH, SCENE_HEIGHT);
        primaryStage.setResizable(true);
        return scene;
    }

    public Scene createSettingsScene() {
        VBox layout = new VBox(25);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

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
            System.err.println("Cannot load image: " + e.getMessage());
        }

        Label titleLabel = new Label("Cài đặt trò chơi");
        titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF; -fx-font-family: 'Arial';");
        titleLabel.setEffect(new DropShadow(10, Color.BLACK));

        gameModeLabel = new Label("Chế độ: Tiêu chuẩn");
        gameModeLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #FFFFFF; -fx-font-weight: bold;");

        ComboBox<String> gameModeBox = new ComboBox<>();
        gameModeBox.getItems().addAll("Tiêu chuẩn", "Cờ chớp");
        gameModeBox.setValue("Tiêu chuẩn");
        gameModeBox.setStyle("-fx-font-size: 16px; -fx-background-color: #FFFFFF; -fx-border-color: #B0BEC5; -fx-border-radius: 5; -fx-padding: 5;");
        gameModeBox.setPrefWidth(200);
        gameModeBox.setOnAction(e -> {
            gameMode = gameModeBox.getValue().equals("Cờ chớp") ? "Blitz" : "Standard";
            gameModeLabel.setText("Chế độ: " + gameModeBox.getValue());
            updateTimeLimitOptions();
        });

        difficultyLabel = new Label("Độ khó: Trung bình");
        difficultyLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #FFFFFF; -fx-font-weight: bold;");

        easyButton = new Button("Dễ");
        easyButton.setStyle("-fx-font-size: 18px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 5; -fx-font-weight: bold;");
        easyButton.setEffect(new DropShadow(5, Color.GRAY));
        easyButton.setOnAction(e -> selectDifficulty("Easy"));

        mediumButton = new Button("Trung bình");
        mediumButton.setStyle("-fx-font-size: 18px; -fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 5; -fx-font-weight: bold; -fx-border-color: #FFFF00; -fx-border-width: 2;");
        mediumButton.setEffect(new DropShadow(5, Color.GRAY));
        mediumButton.setOnAction(e -> selectDifficulty("Medium"));

        hardButton = new Button("Khó");
        hardButton.setStyle("-fx-font-size: 18px; -fx-background-color: #F44336; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 5; -fx-font-weight: bold;");
        hardButton.setEffect(new DropShadow(5, Color.GRAY));
        hardButton.setOnAction(e -> selectDifficulty("Hard"));

        HBox difficultyBox = new HBox(15, easyButton, mediumButton, hardButton);
        difficultyBox.setAlignment(Pos.CENTER);

        timeLimitLabel = new Label("Thời gian: Không giới hạn");
        timeLimitLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #FFFFFF; -fx-font-weight: bold;");

        timeLimitBox = new ComboBox<>();
        updateTimeLimitOptions();
        timeLimitBox.setStyle("-fx-font-size: 16px; -fx-background-color: #FFFFFF; -fx-border-color: #B0BEC5; -fx-border-radius: 5; -fx-padding: 5;");
        timeLimitBox.setPrefWidth(200);
        timeLimitBox.setOnAction(e -> {
            timeLimit = timeLimitBox.getValue();
            timeLimitLabel.setText("Thời gian: " + timeLimit);
        });

        Button startButton = new Button("Bắt đầu");
        startButton.setStyle("-fx-font-size: 24px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 15 40; -fx-background-radius: 10; -fx-font-weight: bold;");
        startButton.setEffect(new DropShadow(5, Color.GRAY));
        startButton.setOnMouseEntered(e -> startButton.setStyle("-fx-font-size: 24px; -fx-background-color: #45A049; -fx-text-fill: white; -fx-padding: 15 40; -fx-background-radius: 10; -fx-font-weight: bold;"));
        startButton.setOnMouseExited(e -> startButton.setStyle("-fx-font-size: 24px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 15 40; -fx-background-radius: 10; -fx-font-weight: bold;"));
        startButton.setOnAction(e -> {
            chessGame.setDifficulty(difficulty);
            chessGame.setTimeLimit(timeLimit);
            chessGame.setGameMode(gameMode);
            startGameCallback.run();
            updateMoveList("");
            primaryStage.centerOnScreen();
        });

        Button backButton = new Button("Quay lại");
        backButton.setStyle("-fx-font-size: 24px; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 15 40; -fx-background-radius: 10; -fx-font-weight: bold;");
        backButton.setEffect(new DropShadow(5, Color.GRAY));
        backButton.setOnMouseEntered(e -> backButton.setStyle("-fx-font-size: 24px; -fx-background-color: #1E88E5; -fx-text-fill: white; -fx-padding: 15 40; -fx-background-radius: 10; -fx-font-weight: bold;"));
        backButton.setOnMouseExited(e -> backButton.setStyle("-fx-font-size: 24px; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 15 40; -fx-background-radius: 10; -fx-font-weight: bold;"));
        backButton.setOnAction(e -> primaryStage.setScene(createMainMenuScene()));

        HBox buttonBox = new HBox(20, startButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);

        layout.getChildren().addAll(titleLabel, gameModeLabel, gameModeBox, difficultyLabel, difficultyBox, timeLimitLabel, timeLimitBox, buttonBox);
        Scene scene = new Scene(layout, SCENE_WIDTH, SCENE_HEIGHT);
        primaryStage.setResizable(true);
        return scene;
    }

    private void updateTimeLimitOptions() {
        timeLimitBox.getItems().clear();
        if (gameMode.equals("Blitz")) {
            timeLimitBox.getItems().addAll("3 phút", "3 phút + 2 giây", "5 phút", "5 phút + 3 giây");
            timeLimitBox.setValue("3 phút");
            timeLimit = "3 phút";
        } else {
            timeLimitBox.getItems().addAll("Không giới hạn", "5 phút", "10 phút", "15 phút");
            timeLimitBox.setValue("Không giới hạn");
            timeLimit = "Không giới hạn";
        }
        timeLimitLabel.setText("Thời gian: " + timeLimit);
    }

    private void selectDifficulty(String selectedDifficulty) {
        difficulty = selectedDifficulty;
        difficultyLabel.setText("Độ khó: " + getDifficultyText(difficulty));

        easyButton.setStyle("-fx-font-size: 18px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 5; -fx-font-weight: bold;");
        mediumButton.setStyle("-fx-font-size: 18px; -fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 5; -fx-font-weight: bold;");
        hardButton.setStyle("-fx-font-size: 18px; -fx-background-color: #F44336; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 5; -fx-font-weight: bold;");

        String selectedStyle = "-fx-border-color: #FFFF00; -fx-border-width: 2;";
        if (difficulty.equals("Easy")) {
            easyButton.setStyle(easyButton.getStyle() + selectedStyle);
        } else if (difficulty.equals("Medium")) {
            mediumButton.setStyle(mediumButton.getStyle() + selectedStyle);
        } else if (difficulty.equals("Hard")) {
            hardButton.setStyle(hardButton.getStyle() + selectedStyle);
        }
    }

    private String getDifficultyText(String difficulty) {
        return switch (difficulty) {
            case "Easy" -> "Dễ";
            case "Medium" -> "Trung bình";
            case "Hard" -> "Khó";
            default -> "Không xác định";
        };
    }

    public void updateGameLayout(VBox root, GridPane chessBoard, Label turnLabel, HBox timeBox) {
        moveList = new TextArea();
        moveList.setEditable(false);
        moveList.setPrefWidth(250);
        moveList.setPrefHeight(500);
        moveList.setStyle("-fx-font-size: 14px; -fx-background-color: #F5F5F5; -fx-border-color: #CCCCCC; -fx-border-width: 1; -fx-font-family: 'Arial';");
        moveList.setText("Danh sách nước đi:\n");

        ScrollPane moveListScroll = new ScrollPane(moveList);
        moveListScroll.setFitToWidth(true);
        moveListScroll.setPrefWidth(270);
        moveListScroll.setStyle("-fx-background-color: transparent;");

        Button resetButton = new Button("Reset");
        resetButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 15; -fx-background-color: #FF9800; -fx-text-fill: white; -fx-background-radius: 5;");
        resetButton.setEffect(new DropShadow(3, Color.GRAY));
        resetButton.setOnAction(e -> {
            chessGame.resetGame();
            updateMoveList("");
        });

        Button backButton = new Button("Quay lại");
        backButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 15; -fx-background-color: #2196F3; -fx-text-fill: white; -fx-background-radius: 5;");
        backButton.setEffect(new DropShadow(3, Color.GRAY));
        backButton.setOnAction(e -> primaryStage.setScene(createMainMenuScene()));

        HBox buttonBox = new HBox(15, resetButton, backButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));

        VBox rightPanel = new VBox(10, moveListScroll, buttonBox);
        rightPanel.setAlignment(Pos.CENTER);

        // Tạo topPanel để chứa turnLabel và timeBox
        VBox topPanel = new VBox(2, turnLabel, timeBox); // Giữ spacing 10 giữa turnLabel và timeBox
        topPanel.setAlignment(Pos.CENTER);
        topPanel.setPadding(new Insets(30, 10, 10, 10)); // Thêm padding top 20px để dịch xuống

        // Tạo layout chính
        VBox mainLayout = new VBox(3);
        HBox gameLayout = new HBox(3);
        gameLayout.setAlignment(Pos.CENTER);
        gameLayout.setPadding(new Insets(3));

        VBox boardContainer = new VBox(root);
        boardContainer.setAlignment(Pos.CENTER);
        gameLayout.getChildren().addAll(boardContainer, rightPanel);

        mainLayout.getChildren().addAll(topPanel, gameLayout);
        mainLayout.setAlignment(Pos.CENTER);

        primaryStage.widthProperty().addListener((obs, oldVal, newVal) -> adjustBoardSize(root, chessBoard));
        primaryStage.heightProperty().addListener((obs, oldVal, newVal) -> adjustBoardSize(root, chessBoard));

        Scene gameScene = new Scene(mainLayout, SCENE_WIDTH, SCENE_HEIGHT);
        primaryStage.setScene(gameScene);
        adjustBoardSize(root, chessBoard);
        chessGame.setGameScene(gameScene);
    }

    public void updateMoveList(String moveNotation) {
        if (moveList != null) {
            if (moveNotation.isEmpty()) {
                moveList.setText("Danh sách nước đi:\n");
            } else {
                moveList.appendText(moveNotation + "\n");
                moveList.setScrollTop(Double.MAX_VALUE);
            }
        }
    }

    private void adjustBoardSize(VBox root, GridPane chessBoard) {
        double windowWidth = primaryStage.getWidth();
        double windowHeight = primaryStage.getHeight();
        double availableWidth = windowWidth - 270; // Trừ chiều rộng của rightPanel
        double availableHeight = windowHeight - 150; // Trừ chiều cao của topPanel và padding

        double tileSize = Math.min(availableWidth, availableHeight) / Board.getBoardSize();
        tileSize = Math.max(MIN_TILE_SIZE, Math.min(MAX_TILE_SIZE, tileSize));
        Board.setTileSize((int) tileSize);

        for (int row = 0; row < Board.getBoardSize(); row++) {
            for (int col = 0; col < Board.getBoardSize(); col++) {
                Rectangle tile = null;
                for (var node : chessBoard.getChildren()) {
                    if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col && node instanceof Rectangle) {
                        tile = (Rectangle) node;
                        break;
                    }
                }
                if (tile != null) {
                    tile.setWidth(tileSize);
                    tile.setHeight(tileSize);
                }

                ChessPiece piece = chessGame.getBoard().getPiece(row, col);
                if (piece != null) {
                    piece.getImageView().setFitWidth(tileSize * 0.9);
                    piece.getImageView().setFitHeight(tileSize * 0.9);
                }
            }
        }

        chessBoard.setPrefWidth(tileSize * Board.getBoardSize());
        chessBoard.setPrefHeight(tileSize * Board.getBoardSize());
        root.setPrefWidth(tileSize * Board.getBoardSize());
        root.setPrefHeight(tileSize * Board.getBoardSize());
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getTimeLimit() {
        return timeLimit;
    }

    public String getGameMode() {
        return gameMode;
    }
}