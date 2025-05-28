package org.example.chess;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class ChessGame extends Application {
    private GridPane chessBoard;
    private Board board;
    private GameLogic gameLogic;
    private AI ai;
    private ChessPiece selectedPiece = null;
    private int selectedRow, selectedCol;
    private boolean isWhiteTurn = true;
    private Label turnLabel;
    private Label whiteTimeLabel;
    private Label blackTimeLabel;
    private List<Rectangle> highlightedTiles = new ArrayList<>();
    private Stage primaryStage;
    private List<String> moveHistory = new ArrayList<>();
    private int moveNumber = 1;
    private LocalDateTime gameStartTime;
    private VBox root;
    private Scene gameScene;
    private UIPanel uiPanel;
    private String difficulty = "Medium";
    private long whiteTime;
    private long blackTime;
    private Timeline timer;
    private String timeLimit;
    private int timeIncrement = 0;
    private String gameMode = "Standard";

    private static class GameRecord {
        private final SimpleIntegerProperty gameId;
        private final SimpleStringProperty winner;
        private final SimpleStringProperty difficulty;
        private final SimpleStringProperty playedDate;
        private final SimpleIntegerProperty moveCount;
        private final SimpleIntegerProperty duration;
        private final SimpleStringProperty pgn;
        private final SimpleStringProperty timeLimit;
        private final SimpleStringProperty gameMode;

        public GameRecord(int gameId, String winner, String difficulty, String playedDate, int moveCount, int duration, String pgn, String timeLimit, String gameMode) {
            this.gameId = new SimpleIntegerProperty(gameId);
            this.winner = new SimpleStringProperty(winner.equals("White") ? "Trắng" : winner.equals("Black") ? "Đen" : winner);
            this.difficulty = new SimpleStringProperty(difficulty != null ? difficulty : "Không xác định");
            this.playedDate = new SimpleStringProperty(playedDate);
            this.moveCount = new SimpleIntegerProperty(moveCount);
            this.duration = new SimpleIntegerProperty(duration);
            this.pgn = new SimpleStringProperty(pgn);
            this.timeLimit = new SimpleStringProperty(timeLimit != null ? timeLimit : "Không giới hạn");
            this.gameMode = new SimpleStringProperty(gameMode != null ? (gameMode.equals("Blitz") ? "Cờ chớp" : "Tiêu chuẩn") : "Tiêu chuẩn");
        }

        public SimpleIntegerProperty gameIdProperty() { return gameId; }
        public SimpleStringProperty winnerProperty() { return winner; }
        public SimpleStringProperty difficultyProperty() { return difficulty; }
        public SimpleStringProperty playedDateProperty() { return playedDate; }
        public SimpleIntegerProperty moveCountProperty() { return moveCount; }
        public SimpleIntegerProperty durationProperty() { return duration; }
        public SimpleStringProperty pgnProperty() { return pgn; }
        public SimpleStringProperty timeLimitProperty() { return timeLimit; }
        public SimpleStringProperty gameModeProperty() { return gameMode; }
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        uiPanel = new UIPanel(primaryStage, () -> resetGame(true), this);
        primaryStage.setScene(uiPanel.createMainMenuScene());
        primaryStage.setTitle("Chess Game: Player vs AI");
        primaryStage.centerOnScreen();
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
        if (ai != null) {
            ai.setDifficulty(difficulty);
            ai.setGameMode(gameMode);
        }
    }

    public void setTimeLimit(String timeLimit) {
        this.timeLimit = timeLimit;
        if (timeLimit.equals("Không giới hạn")) {
            whiteTime = 0;
            blackTime = 0;
            timeIncrement = 0;
        } else {
            String[] parts = timeLimit.split(" \\+ ");
            int minutes = Integer.parseInt(parts[0].split(" ")[0]);
            whiteTime = blackTime = minutes * 60;
            timeIncrement = parts.length > 1 ? Integer.parseInt(parts[1].split(" ")[0]) : 0;
        }
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
        if (ai != null) {
            ai.setGameMode(gameMode);
        }
    }

    public String getGameMode() {
        return gameMode;
    }

    private void resetGame(boolean firstTime) {
        chessBoard = new GridPane();
        board = new Board();
        gameLogic = new GameLogic(board);
        ai = new AI(board, gameLogic, difficulty);
        ai.setGameMode(gameMode);

        for (int row = 0; row < Board.getBoardSize(); row++) {
            for (int col = 0; col < Board.getBoardSize(); col++) {
                Rectangle tile = new Rectangle(Board.getTileSize(), Board.getTileSize());
                tile.setFill((row + col) % 2 == 0 ? Color.WHITE : Color.GRAY);
                chessBoard.add(tile, col, row);
            }
        }

        board.initializeBoard(chessBoard);

        selectedPiece = null;
        isWhiteTurn = true;
        moveHistory.clear();
        moveNumber = 1;
        gameStartTime = LocalDateTime.now();
        gameLogic.reset();
        setTimeLimit(timeLimit);
        clearHighlights();

        if (whiteTimeLabel == null) {
            whiteTimeLabel = new Label("White: --:--");
            whiteTimeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #333333; -fx-padding: 5 10; -fx-border-color: #FFD700; -fx-border-width: 2; -fx-border-radius: 5;");
            whiteTimeLabel.setEffect(new DropShadow(3, Color.BLACK));
        }
        if (blackTimeLabel == null) {
            blackTimeLabel = new Label("Black: --:--");
            blackTimeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: #333333; -fx-padding: 5 10; -fx-border-color: #FFD700; -fx-border-width: 2; -fx-border-radius: 5;");
            blackTimeLabel.setEffect(new DropShadow(3, Color.BLACK));
        }
        if (turnLabel == null) {
            turnLabel = new Label("White's Turn");
            turnLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: black;");
        }

        if (timer != null) {
            timer.stop();
        }
        if (!timeLimit.equals("Không giới hạn")) {
            updateTimeLabels();
            timer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
                if (isWhiteTurn) {
                    if (whiteTime > 0) {
                        whiteTime--;
                        updateTimeLabels();
                        if (whiteTime <= 0) {
                            timer.stop();
                            saveGameToDatabase("Black (Time)");
                            showOutcomePanel("Black Win! (Time)", "lose.wav");
                        }
                    }
                } else {
                    if (blackTime > 0) {
                        blackTime--;
                        updateTimeLabels();
                        if (blackTime <= 0) {
                            timer.stop();
                            saveGameToDatabase("White (Time)");
                            showOutcomePanel("White Win! (Time)", "victory.wav");
                        }
                    }
                }
            }));
            timer.setCycleCount(Animation.INDEFINITE);
            timer.play();
        }

        HBox timeBox = new HBox(20, whiteTimeLabel, blackTimeLabel);
        timeBox.setAlignment(Pos.CENTER);

        root = new VBox(chessBoard);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(10));

        if (firstTime) {
            uiPanel.updateGameLayout(root, chessBoard, turnLabel, timeBox);
        } else {
            turnLabel.setText("White's Turn");
            updateTimeLabels();
            root.getChildren().clear();
            root.getChildren().add(chessBoard);
            uiPanel.updateGameLayout(root, chessBoard, turnLabel, timeBox);
        }

        chessBoard.setOnMouseClicked(this::handleMouseClick);
        primaryStage.centerOnScreen();
    }

    void resetGame() {
        resetGame(false);
    }

    private void updateTimeLabels() {
        if (whiteTimeLabel != null && blackTimeLabel != null) {
            if (timeLimit.equals("Không giới hạn")) {
                whiteTimeLabel.setText("White: --:--");
                blackTimeLabel.setText("Black: --:--");
            } else {
                whiteTimeLabel.setText(String.format("White: %02d:%02d", whiteTime / 60, whiteTime % 60));
                blackTimeLabel.setText(String.format("Black: %02d:%02d", blackTime / 60, blackTime % 60));
            }
        }
    }

    public void showGameHistory(Scene previousScene) {
        VBox historyLayout = new VBox(15);
        historyLayout.setAlignment(Pos.CENTER);
        historyLayout.setPadding(new Insets(20));
        historyLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #ECEFF1, #CFD8DC);");

        Label titleLabel = new Label("Lịch sử trận đấu");
        titleLabel.setFont(new Font("Arial", 28));
        titleLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #263238; -fx-padding: 10;");
        titleLabel.setEffect(new DropShadow(10, Color.GRAY));

        ComboBox<String> filterBox = new ComboBox<>();
        filterBox.getItems().addAll("Tất cả", "Trắng", "Đen", "Easy", "Medium", "Hard", "Tiêu chuẩn", "Cờ chớp");
        filterBox.setValue("Tất cả");
        filterBox.setStyle("-fx-font-size: 14px; -fx-background-color: #FFFFFF; -fx-border-color: #B0BEC5; -fx-border-radius: 5;");

        TableView<GameRecord> historyTable = new TableView<>();
        historyTable.setPrefWidth(800);
        historyTable.setPrefHeight(400);
        historyTable.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #B0BEC5; -fx-border-width: 2; -fx-border-radius: 5; -fx-font-size: 14px;");

        TableColumn<GameRecord, Integer> gameIdColumn = new TableColumn<>("Trận");
        gameIdColumn.setCellValueFactory(cellData -> cellData.getValue().gameIdProperty().asObject());
        gameIdColumn.setPrefWidth(60);
        gameIdColumn.setStyle("-fx-alignment: CENTER; -fx-background-color: #0288D1; -fx-text-fill: white;");

        TableColumn<GameRecord, String> winnerColumn = new TableColumn<>("Người thắng");
        winnerColumn.setCellValueFactory(cellData -> cellData.getValue().winnerProperty());
        winnerColumn.setPrefWidth(100);
        winnerColumn.setStyle("-fx-alignment: CENTER; -fx-background-color: #0288D1; -fx-text-fill: white;");

        TableColumn<GameRecord, String> difficultyColumn = new TableColumn<>("Độ khó");
        difficultyColumn.setCellValueFactory(cellData -> cellData.getValue().difficultyProperty());
        difficultyColumn.setPrefWidth(100);
        difficultyColumn.setStyle("-fx-alignment: CENTER; -fx-background-color: #0288D1; -fx-text-fill: white;");

        TableColumn<GameRecord, String> dateColumn = new TableColumn<>("Ngày");
        dateColumn.setCellValueFactory(cellData -> cellData.getValue().playedDateProperty());
        dateColumn.setPrefWidth(150);
        dateColumn.setStyle("-fx-alignment: CENTER; -fx-background-color: #0288D1; -fx-text-fill: white;");

        TableColumn<GameRecord, Integer> moveCountColumn = new TableColumn<>("Số nước đi");
        moveCountColumn.setCellValueFactory(cellData -> cellData.getValue().moveCountProperty().asObject());
        moveCountColumn.setPrefWidth(100);
        moveCountColumn.setStyle("-fx-alignment: CENTER; -fx-background-color: #0288D1; -fx-text-fill: white;");

        TableColumn<GameRecord, Integer> durationColumn = new TableColumn<>("Thời gian (giây)");
        durationColumn.setCellValueFactory(cellData -> cellData.getValue().durationProperty().asObject());
        durationColumn.setPrefWidth(100);
        durationColumn.setStyle("-fx-alignment: CENTER; -fx-background-color: #0288D1; -fx-text-fill: white;");

        TableColumn<GameRecord, String> timeLimitColumn = new TableColumn<>("Thời gian giới hạn");
        timeLimitColumn.setCellValueFactory(cellData -> cellData.getValue().timeLimitProperty());
        timeLimitColumn.setPrefWidth(100);
        timeLimitColumn.setStyle("-fx-alignment: CENTER; -fx-background-color: #0288D1; -fx-text-fill: white;");

        TableColumn<GameRecord, String> gameModeColumn = new TableColumn<>("Chế độ");
        gameModeColumn.setCellValueFactory(cellData -> cellData.getValue().gameModeProperty());
        gameModeColumn.setPrefWidth(100);
        gameModeColumn.setStyle("-fx-alignment: CENTER; -fx-background-color: #0288D1; -fx-text-fill: white;");

        TableColumn<GameRecord, String> pgnColumn = new TableColumn<>("PGN");
        pgnColumn.setCellValueFactory(cellData -> cellData.getValue().pgnProperty());
        pgnColumn.setPrefWidth(150);
        pgnColumn.setStyle("-fx-alignment: CENTER-LEFT; -fx-background-color: #0288D1; -fx-text-fill: white;");

        historyTable.getColumns().addAll(gameIdColumn, winnerColumn, difficultyColumn, dateColumn, moveCountColumn, durationColumn, timeLimitColumn, gameModeColumn, pgnColumn);

        ObservableList<GameRecord> gameRecords = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT game_id, winner, played_date, pgn, move_count, duration, difficulty, time_limit, game_mode FROM games ORDER BY played_date DESC")) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int gameId = rs.getInt("game_id");
                String winner = rs.getString("winner");
                String playedDate = rs.getString("played_date");
                String pgn = rs.getString("pgn");
                int moveCount = rs.getInt("move_count");
                int duration = rs.getInt("duration");
                String difficulty = rs.getString("difficulty");
                String timeLimit = rs.getString("time_limit");
                String gameMode = rs.getString("game_mode");
                gameRecords.add(new GameRecord(gameId, winner, difficulty, playedDate, moveCount, duration, pgn, timeLimit, gameMode));
            }
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi tải lịch sử");
            alert.setHeaderText("Không thể tải lịch sử trận đấu");
            alert.setContentText("Lỗi: " + e.getMessage());
            alert.showAndWait();
        }

        filterBox.setOnAction(e -> {
            String filter = filterBox.getValue();
            ObservableList<GameRecord> filteredRecords = FXCollections.observableArrayList();
            for (GameRecord record : gameRecords) {
                if (filter.equals("Tất cả") ||
                        record.winnerProperty().get().equals(filter) ||
                        record.difficultyProperty().get().equals(filter) ||
                        record.gameModeProperty().get().equals(filter)) {
                    filteredRecords.add(record);
                }
            }
            historyTable.setItems(filteredRecords);
        });

        historyTable.setItems(gameRecords);

        historyTable.setRowFactory(tv -> {
            TableRow<GameRecord> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    GameRecord gameRecord = row.getItem();
                    showPgnDetail(gameRecord.pgnProperty().get());
                }
            });
            row.setStyle("-fx-background-color: #FFFFFF;");
            row.setOnMouseEntered(e -> {
                if (!row.isEmpty()) row.setStyle("-fx-background-color: #E3F2FD;");
            });
            row.setOnMouseExited(e -> {
                if (!row.isEmpty()) row.setStyle("-fx-background-color: " + (row.getIndex() % 2 == 0 ? "#FFFFFF" : "#F5F7FA") + ";");
            });
            return row;
        });

        Button backButton = new Button("Quay lại");
        backButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-color: #0288D1; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        backButton.setEffect(new DropShadow(5, Color.GRAY));
        backButton.setOnMouseEntered(e -> backButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-color: #0277BD; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;"));
        backButton.setOnMouseExited(e -> backButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-color: #0288D1; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;"));
        backButton.setOnAction(e -> primaryStage.setScene(previousScene));

        Button clearButton = new Button("Xóa tất cả");
        clearButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-color: #D32F2F; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        clearButton.setEffect(new DropShadow(5, Color.GRAY));
        clearButton.setOnMouseEntered(e -> clearButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-color: #C62828; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;"));
        clearButton.setOnMouseExited(e -> clearButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-color: #D32F2F; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;"));
        clearButton.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc muốn xóa toàn bộ lịch sử?");
            if (confirm.showAndWait().get() == ButtonType.OK) {
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("DELETE FROM games")) {
                    pstmt.executeUpdate();
                    gameRecords.clear();
                    historyTable.setItems(gameRecords);
                } catch (SQLException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Lỗi xóa lịch sử");
                    alert.setHeaderText("Không thể xóa lịch sử");
                    alert.setContentText("Lỗi: " + ex.getMessage());
                    alert.showAndWait();
                }
            }
        });

        Button deleteButton = new Button("Xóa");
        deleteButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-color: #D32F2F; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        deleteButton.setEffect(new DropShadow(5, Color.GRAY));
        deleteButton.setOnMouseEntered(e -> deleteButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-color: #C62828; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;"));
        deleteButton.setOnMouseExited(e -> deleteButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-color: #D32F2F; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;"));
        deleteButton.setOnAction(e -> {
            GameRecord selectedGame = historyTable.getSelectionModel().getSelectedItem();
            if (selectedGame == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Chưa chọn ván đấu");
                alert.setHeaderText(null);
                alert.setContentText("Vui lòng chọn một ván đấu để xóa.");
                alert.showAndWait();
            } else {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                        "Bạn có chắc muốn xóa ván đấu #" + selectedGame.gameIdProperty().get() + "?");
                if (confirm.showAndWait().get() == ButtonType.OK) {
                    deleteSingleGame(selectedGame.gameIdProperty().get(), gameRecords, historyTable);
                }
            }
        });

        HBox buttonBox = new HBox(10, backButton, deleteButton, clearButton);
        buttonBox.setAlignment(Pos.CENTER);

        historyLayout.getChildren().addAll(titleLabel, filterBox, historyTable, buttonBox);

        Scene historyScene = new Scene(historyLayout, 1200, 800);
        primaryStage.setScene(historyScene);
        primaryStage.centerOnScreen();
    }

    private void deleteSingleGame(int gameId, ObservableList<GameRecord> gameRecords, TableView<GameRecord> historyTable) {
        String sql = "DELETE FROM games WHERE game_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, gameId);
            pstmt.executeUpdate();

            gameRecords.removeIf(record -> record.gameIdProperty().get() == gameId);
            historyTable.refresh();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi xóa ván đấu");
            alert.setHeaderText("Không thể xóa ván đấu");
            alert.setContentText("Lỗi: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void showPgnDetail(String pgn) {
        Stage pgnStage = new Stage();
        pgnStage.initModality(Modality.APPLICATION_MODAL);
        pgnStage.initOwner(primaryStage);
        pgnStage.setTitle("Chi tiết PGN");

        VBox pgnLayout = new VBox(10);
        pgnLayout.setAlignment(Pos.CENTER);
        pgnLayout.setPadding(new Insets(20));
        pgnLayout.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #B0BEC5; -fx-border-width: 2; -fx-border-radius: 5;");

        Label titleLabel = new Label("PGN của trận đấu");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #263238;");

        TextArea pgnText = new TextArea(pgn);
        pgnText.setEditable(false);
        pgnText.setWrapText(true);
        pgnText.setPrefWidth(500);
        pgnText.setPrefHeight(300);
        pgnText.setStyle("-fx-font-size: 14px; -fx-background-color: #ECEFF1; -fx-border-color: #B0BEC5; -fx-border-width: 1;");

        Button closeButton = new Button("Đóng");
        closeButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-color: #D32F2F; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;");
        closeButton.setOnMouseEntered(e -> closeButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-color: #C62828; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;"));
        closeButton.setOnMouseExited(e -> closeButton.setStyle("-fx-font-size: 14px; -fx-padding: 10 20; -fx-background-color: #D32F2F; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;"));
        closeButton.setOnAction(e -> pgnStage.close());

        pgnLayout.getChildren().addAll(titleLabel, new ScrollPane(pgnText), closeButton);

        Scene pgnScene = new Scene(pgnLayout, 800, 500);
        pgnStage.setScene(pgnScene);
        pgnStage.showAndWait();
    }

    private void handleMouseClick(MouseEvent event) {
        if (!isWhiteTurn) return;

        int col = (int) (event.getX() / Board.getTileSize());
        int row = (int) (event.getY() / Board.getTileSize());

        if (row < 0 || row >= Board.getBoardSize() || col < 0 || col >= Board.getBoardSize()) return;

        if (selectedPiece == null) {
            ChessPiece piece = board.getPiece(row, col);
            if (piece != null && piece.isWhite()) {
                selectedPiece = piece;
                selectedRow = row;
                selectedCol = col;
                highlightValidMoves();
            }
        } else {
            if (gameLogic.isValidMove(selectedPiece, selectedRow, selectedCol, row, col)) {
                String whiteMove = convertToChessNotation(selectedRow, selectedCol, row, col);
                if (selectedPiece.getType().equals("king") && Math.abs(selectedCol - col) == 2) {
                    whiteMove = (col > selectedCol) ? "O-O" : "O-O-O";
                }

                board.movePiece(selectedRow, selectedCol, row, col, chessBoard);
                gameLogic.setLastMove(selectedRow, selectedCol, row, col);

                if (gameLogic.isPawnPromotion(selectedPiece, row)) {
                    String promotedPiece = showPromotionDialog(true);
                    String imagePath = "/pieces/75px_white_" + promotedPiece + ".png";
                    ChessPiece newPiece = new ChessPiece(promotedPiece, true, imagePath);
                    newPiece.setHasMoved(true);
                    board.setPiece(row, col, newPiece);
                    chessBoard.getChildren().remove(selectedPiece.getImageView());
                    chessBoard.add(newPiece.getImageView(), col, row);
                    whiteMove = convertToChessNotation(selectedRow, selectedCol, row, col) + "=" + promotedPiece.substring(0, 1).toUpperCase();
                }

                whiteTime += timeIncrement;
                updateTimeLabels();

                clearHighlights();
                isWhiteTurn = false;
                turnLabel.setText("Black's Turn");
                selectedPiece = null;

                uiPanel.updateMoveList(moveNumber + ". " + whiteMove);

                if (gameLogic.isGameOver(true)) {
                    moveHistory.add(moveNumber + ". " + whiteMove);
                    String result = gameLogic.isKingInCheck(false) ? "White" : "Draw";
                    if (result.equals("Draw")) {
                        String reason = gameLogic.getPositionCount().getOrDefault(gameLogic.getBoardState(true), 0) >= 3 ? "Threefold repetition" :
                                gameLogic.getMoveCountWithoutCaptureOrPawn() >= 100 ? "50-move rule" : "Insufficient material";
                        saveGameToDatabase("Draw (" + reason + ")");
                        showOutcomePanel("Draw! (" + reason + ")", "draw_sound.wav");
                    } else {
                        saveGameToDatabase("White");
                        showOutcomePanel("White Win!", "victory.wav");
                    }
                    return;
                }

                ai.makeMove(chessBoard);
                int[] aiMove = ai.getLastMove();
                String blackMove = convertToChessNotation(aiMove[0], aiMove[1], aiMove[2], aiMove[3]);
                ChessPiece aiPiece = board.getPiece(aiMove[2], aiMove[3]);

                if (gameLogic.isPawnPromotion(aiPiece, aiMove[2])) {
                    String promotedPiece = "queen";
                    String imagePath = "/pieces/75px_black_" + promotedPiece + ".png";
                    ChessPiece newPiece = new ChessPiece(promotedPiece, false, imagePath);
                    newPiece.setHasMoved(true);
                    board.setPiece(aiMove[2], aiMove[3], newPiece);
                    chessBoard.getChildren().remove(aiPiece.getImageView());
                    chessBoard.add(newPiece.getImageView(), aiMove[3], aiMove[2]);
                    blackMove = convertToChessNotation(aiMove[0], aiMove[1], aiMove[2], aiMove[3]) + "=" + promotedPiece.substring(0, 1).toUpperCase();
                } else if (aiPiece.getType().equals("king") && Math.abs(aiMove[1] - aiMove[3]) == 2) {
                    blackMove = (aiMove[3] > aiMove[1]) ? "O-O" : "O-O-O";
                }

                blackTime += timeIncrement;
                updateTimeLabels();

                gameLogic.setLastMove(aiMove[0], aiMove[1], aiMove[2], aiMove[3]);
                moveHistory.add(moveNumber + ". " + whiteMove + " " + blackMove);
                moveNumber++;

                uiPanel.updateMoveList(blackMove);

                if (gameLogic.isGameOver(false)) {
                    String result = gameLogic.isKingInCheck(true) ? "Black" : "Draw";
                    if (result.equals("Draw")) {
                        String reason = gameLogic.getPositionCount().getOrDefault(gameLogic.getBoardState(false), 0) >= 3 ? "Threefold repetition" :
                                gameLogic.getMoveCountWithoutCaptureOrPawn() >= 100 ? "50-move rule" : "Insufficient material";
                        saveGameToDatabase("Draw (" + reason + ")");
                        showOutcomePanel("Draw! (" + reason + ")", "draw_sound.wav");
                    } else {
                        saveGameToDatabase("Black");
                        showOutcomePanel("Black Win!", "lose.wav");
                    }
                } else {
                    isWhiteTurn = true;
                    turnLabel.setText("White's Turn");
                }
            } else {
                clearHighlights();
                selectedPiece = null;
            }
        }
    }

    private String showPromotionDialog(boolean isWhite) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(primaryStage);
        dialog.setTitle("Pawn Promotion");

        Label label = new Label("Choose piece to promote to:");
        label.setStyle("-fx-font-size: 14px;");

        Button queenButton = new Button("Queen");
        Button rookButton = new Button("Rook");
        Button bishopButton = new Button("Bishop");
        Button knightButton = new Button("Knight");

        final String[] selectedPiece = new String[1];
        queenButton.setOnAction(e -> {
            selectedPiece[0] = "queen";
            dialog.close();
        });
        rookButton.setOnAction(e -> {
            selectedPiece[0] = "rook";
            dialog.close();
        });
        bishopButton.setOnAction(e -> {
            selectedPiece[0] = "bishop";
            dialog.close();
        });
        knightButton.setOnAction(e -> {
            selectedPiece[0] = "knight";
            dialog.close();
        });

        HBox buttonBox = new HBox(10, queenButton, rookButton, bishopButton, knightButton);
        buttonBox.setAlignment(Pos.CENTER);
        VBox dialogLayout = new VBox(10, label, buttonBox);
        dialogLayout.setAlignment(Pos.CENTER);
        dialogLayout.setStyle("-fx-padding: 20;");

        Scene dialogScene = new Scene(dialogLayout, 300, 150);
        dialog.setScene(dialogScene);
        dialog.showAndWait();

        return selectedPiece[0] != null ? selectedPiece[0] : "queen";
    }

    private String convertToChessNotation(int fromRow, int fromCol, int toRow, int toCol) {
        ChessPiece piece = board.getPiece(toRow, toCol);
        if (piece == null) return "";
        String pieceType = piece.getType();
        String pieceNotation = pieceType.equals("pawn") ? "" : pieceType.substring(0, 1).toUpperCase();
        String toFile = String.valueOf((char) ('a' + toCol));
        String toRank = String.valueOf(8 - toRow);
        return pieceNotation + toFile + toRank;
    }

    private void saveGameToDatabase(String winner) {
        String pgn = String.join(" ", moveHistory);
        int moveCount = moveHistory.size();
        long duration = ChronoUnit.SECONDS.between(gameStartTime, LocalDateTime.now());
        String sql = "INSERT INTO games (winner, pgn, move_count, duration, difficulty, time_limit, game_mode) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, winner);
            pstmt.setString(2, pgn);
            pstmt.setInt(3, moveCount);
            pstmt.setLong(4, duration);
            pstmt.setString(5, difficulty);
            pstmt.setString(6, timeLimit);
            pstmt.setString(7, gameMode);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi lưu lịch sử");
            alert.setHeaderText("Không thể lưu lịch sử ván đấu");
            alert.setContentText("Lỗi: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void highlightValidMoves() {
        clearHighlights();
        for (int toRow = 0; toRow < Board.getBoardSize(); toRow++) {
            for (int toCol = 0; toCol < Board.getBoardSize(); toCol++) {
                if (gameLogic.isValidMove(selectedPiece, selectedRow, selectedCol, toRow, toCol)) {
                    Rectangle highlight = new Rectangle(Board.getTileSize(), Board.getTileSize());
                    highlight.setFill(Color.color(Color.YELLOW.getRed(), Color.YELLOW.getGreen(), Color.YELLOW.getBlue(), 0.5));
                    chessBoard.add(highlight, toCol, toRow);
                    highlightedTiles.add(highlight);
                }
            }
        }
    }

    private void clearHighlights() {
        chessBoard.getChildren().removeAll(highlightedTiles);
        highlightedTiles.clear();
    }

    private void showOutcomePanel(String result, String soundFile) {
        if (timer != null) {
            timer.stop();
        }
        GameOutcomePanel outcomePanel = new GameOutcomePanel(primaryStage, () -> resetGame(), this, result, soundFile);
        primaryStage.setScene(outcomePanel.createOutcomeScene());
    }

    public String getLastMoveNotation() {
        if (moveHistory.isEmpty()) return "No moves yet";
        return moveHistory.get(moveHistory.size() - 1);
    }

    public Scene getGameScene() {
        return gameScene;
    }

    public void setGameScene(Scene scene) {
        this.gameScene = scene;
    }

    public Board getBoard() {
        return board;
    }

    public List<String> getMoveHistory() {
        return moveHistory;
    }

    public LocalDateTime getGameStartTime() {
        return gameStartTime;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getTimeLimit() {
        return timeLimit;
    }

    public static void main(String[] args) {
        if (!DatabaseConnection.testConnection()) {
            System.err.println("Không thể kết nối đến cơ sở dữ liệu. Vui lòng kiểm tra cấu hình.");
            return;
        }
        launch(args);
    }
}