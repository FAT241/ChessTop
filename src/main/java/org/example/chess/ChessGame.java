package org.example.chess;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
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
                historyContent.append(String.format("Game %d | Winner: %s | PlayDate: %s\nMoveCount: %d | Time: %d Second\nPGN: %s\n\n",
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
                String whiteMove = "";
                boolean isPawnPromotion = selectedPiece.getType().equals("pawn") && (row == 0);

                board.movePiece(selectedRow, selectedCol, row, col, chessBoard);
                gameLogic.setLastMove(selectedRow, selectedCol, row, col);

                if (isPawnPromotion) {
                    String promotedPiece = showPromotionDialog(true);
                    String imagePath = "/pieces/75px_white_" + promotedPiece + ".png";
                    ChessPiece newPiece = new ChessPiece(promotedPiece, true, imagePath);
                    newPiece.setHasMoved(true);
                    board.setPiece(row, col, newPiece);
                    chessBoard.getChildren().remove(selectedPiece.getImageView());
                    chessBoard.add(newPiece.getImageView(), col, row);
                    whiteMove = convertToChessNotation(selectedRow, selectedCol, row, col) + "=" + promotedPiece.substring(0, 1).toUpperCase();
                } else {
                    whiteMove = convertToChessNotation(selectedRow, selectedCol, row, col);
                    if (selectedPiece.getType().equals("king") && Math.abs(selectedCol - col) == 2) {
                        whiteMove = (col > selectedCol) ? "O-O" : "O-O-O";
                    }
                }

                clearHighlights();
                isWhiteTurn = false;
                turnLabel.setText("Black's Turn");
                selectedPiece = null;

                if (gameLogic.isGameOver(true)) {
                    moveHistory.add(moveNumber + ". " + whiteMove);
                    saveGameToDatabase("White");
                    showLosePanel("Black");
                    return;
                }

                ai.makeMove(chessBoard);
                int[] aiMove = ai.getLastMove();
                String blackMove = "";
                boolean isAIPawnPromotion = board.getPiece(aiMove[2], aiMove[3]).getType().equals("pawn") && (aiMove[2] == 7);

                if (isAIPawnPromotion) {
                    String promotedPiece = "queen";
                    String imagePath = "/pieces/75px_black_" + promotedPiece + ".png";
                    ChessPiece newPiece = new ChessPiece(promotedPiece, false, imagePath);
                    newPiece.setHasMoved(true);
                    board.setPiece(aiMove[2], aiMove[3], newPiece);
                    chessBoard.getChildren().remove(board.getPiece(aiMove[2], aiMove[3]).getImageView());
                    chessBoard.add(newPiece.getImageView(), aiMove[3], aiMove[2]);
                    blackMove = convertToChessNotation(aiMove[0], aiMove[1], aiMove[2], aiMove[3]) + "=" + promotedPiece.substring(0, 1).toUpperCase();
                } else {
                    blackMove = convertToChessNotation(aiMove[0], aiMove[1], aiMove[2], aiMove[3]);
                    if (board.getPiece(aiMove[2], aiMove[3]).getType().equals("king") && Math.abs(aiMove[1] - aiMove[3]) == 2) {
                        blackMove = (aiMove[3] > aiMove[1]) ? "O-O" : "O-O-O";
                    }
                }

                gameLogic.setLastMove(aiMove[0], aiMove[1], aiMove[2], aiMove[3]);
                moveHistory.add(moveNumber + ". " + whiteMove + " " + blackMove);
                moveNumber++;

                // Kiểm tra xem quân trắng có bị chiếu hết không
                if (gameLogic.isGameOver(true)) {
                    saveGameToDatabase("Black");
                    showLosePanel("White");
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

        Label label = new Label("Choose a piece to promote to:");
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
        if (piece == null) {
            return "";
        }
        String pieceType = piece.getType();
        String pieceNotation = pieceType.equals("pawn") ? "" : pieceType.substring(0, 1).toUpperCase();
        String toFile = String.valueOf((char) ('a' + toCol));
        String toRank = String.valueOf(8 - toRow);

        if (pieceType.equals("pawn") && Math.abs(fromCol - toCol) == 1 && board.getPiece(toRow, toCol) == null) {
            int direction = piece.isWhite() ? -1 : 1;
            if ((piece.isWhite() && fromRow == 3) || (!piece.isWhite() && fromRow == 4)) {
                if (toRow == fromRow + direction) {
                    String fromFile = String.valueOf((char) ('a' + fromCol));
                    return fromFile + "x" + toFile + toRank + " e.p.";
                }
            }
        }

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

    private void showLosePanel(String loser) {
        LosePanel losePanel = new LosePanel(primaryStage, () -> startGame(primaryStage), this);
        primaryStage.setScene(losePanel.createLoseScene(loser));
    }

    public String getLastMoveNotation() {
        if (moveHistory.isEmpty()) return "No moves";
        return moveHistory.get(moveHistory.size() - 1);
    }

    public static void main(String[] args) {
        launch(args);
    }
}