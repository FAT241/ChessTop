package org.example.chess;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;

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
    private List<Rectangle> highlightedTiles = new ArrayList<>();
    private Stage primaryStage;
    private List<String> moveHistory = new ArrayList<>();
    private int moveNumber = 1;
    private LocalDateTime gameStartTime;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        UIPanel uiPanel = new UIPanel(primaryStage, () -> startGame(primaryStage), this);
        primaryStage.setScene(uiPanel.createScene());
        primaryStage.setTitle("Chess Game: Player vs AI");
        primaryStage.centerOnScreen();
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    private void startGame(Stage primaryStage) {
        chessBoard = new GridPane();
        board = new Board();
        gameLogic = new GameLogic(board);
        ai = new AI(board, gameLogic);

        for (int row = 0; row < Board.getBoardSize(); row++) {
            for (int col = 0; col < Board.getBoardSize(); col++) {
                Rectangle tile = new Rectangle(Board.getTileSize(), Board.getTileSize());
                tile.setFill((row + col) % 2 == 0 ? Color.WHITE : Color.GRAY);
                chessBoard.add(tile, col, row);
            }
        }

        board.initializeBoard(chessBoard);

        turnLabel = new Label("White's Turn");
        turnLabel.setStyle("-fx-font-size: 16px;");

        Button resetButton = new Button("Reset");
        resetButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 10;");
        resetButton.setOnAction(e -> resetGame());

        VBox root = new VBox(10, turnLabel, chessBoard, resetButton);
        root.setAlignment(Pos.CENTER);

        chessBoard.setOnMouseClicked(this::handleMouseClick);

        Scene scene = new Scene(root, Board.getTileSize() * Board.getBoardSize(), Board.getTileSize() * Board.getBoardSize() + 60);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.centerOnScreen();

        moveHistory.clear();
        moveNumber = 1;
        gameStartTime = LocalDateTime.now();
    }

    public void showGameHistory(Scene previousScene) {
        VBox historyLayout = new VBox(10);
        historyLayout.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Game History");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextArea historyText = new TextArea();
        historyText.setEditable(false);
        historyText.setPrefWidth(500);
        historyText.setPrefHeight(300);

        StringBuilder historyContent = new StringBuilder();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("SELECT game_id, winner, played_date, pgn, move_count, duration FROM games ORDER BY played_date DESC")) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                int gameId = rs.getInt("game_id");
                String winner = rs.getString("winner");
                String playedDate = rs.getString("played_date");
                String pgn = rs.getString("pgn");
                int moveCount = rs.getInt("move_count");
                int duration = rs.getInt("duration");
                historyContent.append(String.format("Game %d | Winner: %s | PlayDate: %s\nMoveCount: %d | Time: %d giây\nPGN: %s\n\n",
                        gameId, winner, playedDate, moveCount, duration, pgn));
            }
        } catch (SQLException e) {
            historyContent.append("Cannot save history: ").append(e.getMessage());
        }

        historyText.setText(historyContent.toString());

        Button closeButton = new Button("CLOSE");
        closeButton.setStyle("-fx-font-size: 14px; -fx-padding: 5 10;");
        closeButton.setOnAction(e -> primaryStage.setScene(previousScene));

        historyLayout.getChildren().addAll(titleLabel, new ScrollPane(historyText), closeButton);

        Scene historyScene = new Scene(historyLayout, 600, 400);
        primaryStage.setScene(historyScene);
        primaryStage.centerOnScreen();
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
                board.movePiece(selectedRow, selectedCol, row, col, chessBoard);
                clearHighlights();
                isWhiteTurn = false;
                turnLabel.setText("Black's Turn");
                selectedPiece = null;

                if (gameLogic.isGameOver(true)) {
                    moveHistory.add(moveNumber + ". " + whiteMove);
                    saveGameToDatabase("Black");
                    showVictoryPanel("Black");
                    return;
                }

                ai.makeMove(chessBoard);
                int[] aiMove = ai.getLastMove();
                String blackMove = convertToChessNotation(aiMove[0], aiMove[1], aiMove[2], aiMove[3]);
                moveHistory.add(moveNumber + ". " + whiteMove + " " + blackMove);
                moveNumber++;

                isWhiteTurn = true;
                turnLabel.setText("White's Turn");

                if (gameLogic.isGameOver(false)) {
                    saveGameToDatabase("White");
                    showVictoryPanel("White");
                }
            } else {
                clearHighlights();
                selectedPiece = null;
            }
        }
    }

    private String convertToChessNotation(int fromRow, int fromCol, int toRow, int toCol) {
        ChessPiece piece = board.getPiece(fromRow, fromCol);
        if (piece == null) {
            return "";
        }
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
        String sql = "INSERT INTO games (winner, pgn, move_count, duration) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, winner);
            pstmt.setString(2, pgn);
            pstmt.setInt(3, moveCount);
            pstmt.setLong(4, duration);
            pstmt.executeUpdate();
            System.out.println("History Saved!");
        } catch (SQLException e) {
            System.out.println("Save History Error " + e.getMessage());
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

    private void resetGame() {
        chessBoard.getChildren().clear();
        for (int row = 0; row < Board.getBoardSize(); row++) {
            for (int col = 0; col < Board.getBoardSize(); col++) {
                Rectangle tile = new Rectangle(Board.getTileSize(), Board.getTileSize());
                tile.setFill((row + col) % 2 == 0 ? Color.WHITE : Color.GRAY);
                chessBoard.add(tile, col, row);
            }
        }

        board = new Board();
        gameLogic = new GameLogic(board);
        ai = new AI(board, gameLogic);
        board.initializeBoard(chessBoard);

        selectedPiece = null;
        isWhiteTurn = true;
        turnLabel.setText("White's Turn");
        clearHighlights();
        chessBoard.setOnMouseClicked(this::handleMouseClick);
        primaryStage.centerOnScreen();

        moveHistory.clear();
        moveNumber = 1;
        gameStartTime = LocalDateTime.now();
    }

    private void showVictoryPanel(String winner) {
        VictoryPanel victoryPanel = new VictoryPanel(primaryStage, () -> startGame(primaryStage), this);
        primaryStage.setScene(victoryPanel.createVictoryScene(winner));
    }

    public static void main(String[] args) {
        launch(args);
    }
}