package org.example.chess;

import javafx.animation.TranslateTransition;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

public class Board {
    private static int TILE_SIZE = 80;
    private static final int BOARD_SIZE = 8;
    private ChessPiece[][] board;

    public Board() {
        board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
    }

    public void initializeBoard(GridPane chessBoard) {
        board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];

        // White pieces
        board[7][0] = new ChessPiece("rook", true, "/pieces/75px_white_rook.png");
        board[7][1] = new ChessPiece("knight", true, "/pieces/75px_white_knight.png");
        board[7][2] = new ChessPiece("bishop", true, "/pieces/75px_white_bishop.png");
        board[7][3] = new ChessPiece("queen", true, "/pieces/75px_white_queen.png");
        board[7][4] = new ChessPiece("king", true, "/pieces/75px_white_king.png");
        board[7][5] = new ChessPiece("bishop", true, "/pieces/75px_white_bishop.png");
        board[7][6] = new ChessPiece("knight", true, "/pieces/75px_white_knight.png");
        board[7][7] = new ChessPiece("rook", true, "/pieces/75px_white_rook.png");
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[6][i] = new ChessPiece("pawn", true, "/pieces/75px_white_pawn.png");
        }

        // Black pieces
        board[0][0] = new ChessPiece("rook", false, "/pieces/75px_black_rook.png");
        board[0][1] = new ChessPiece("knight", false, "/pieces/75px_black_knight.png");
        board[0][2] = new ChessPiece("bishop", false, "/pieces/75px_black_bishop.png");
        board[0][3] = new ChessPiece("queen", false, "/pieces/75px_black_queen.png");
        board[0][4] = new ChessPiece("king", false, "/pieces/75px_black_king.png");
        board[0][5] = new ChessPiece("bishop", false, "/pieces/75px_black_bishop.png");
        board[0][6] = new ChessPiece("knight", false, "/pieces/75px_black_knight.png");
        board[0][7] = new ChessPiece("rook", false, "/pieces/75px_black_rook.png");
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[1][i] = new ChessPiece("pawn", false, "/pieces/75px_black_pawn.png");
        }

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] != null) {
                    ImageView imageView = board[row][col].getImageView();
                    imageView.setFitWidth(TILE_SIZE * 0.9);
                    imageView.setFitHeight(TILE_SIZE * 0.9);
                    chessBoard.add(imageView, col, row);
                }
            }
        }
    }

    public void movePiece(int fromRow, int fromCol, int toRow, int toCol, GridPane chessBoard) {
        ChessPiece piece = board[fromRow][fromCol];
        if (piece == null) return;

        ChessPiece targetPiece = board[toRow][toCol];
        if (targetPiece != null && targetPiece.isWhite() == piece.isWhite()) return;

        if (piece.getType().equals("king") && Math.abs(fromCol - toCol) == 2) {
            int rookFromCol = (toCol > fromCol) ? 7 : 0;
            int rookToCol = (toCol > fromCol) ? fromCol + 1 : fromCol - 1;
            ChessPiece rook = board[fromRow][rookFromCol];
            if (rook != null) {
                board[fromRow][rookFromCol] = null;
                board[fromRow][rookToCol] = rook;
                if (chessBoard != null) {
                    chessBoard.getChildren().remove(rook.getImageView());
                    ImageView rookImage = rook.getImageView();
                    rookImage.setFitWidth(TILE_SIZE * 0.9);
                    rookImage.setFitHeight(TILE_SIZE * 0.9);
                    chessBoard.add(rookImage, rookToCol, fromRow);
                }
                rook.setHasMoved(true);
            }
        }

        if (piece.getType().equals("pawn") && Math.abs(fromCol - toCol) == 1 && getPiece(toRow, toCol) == null) {
            int direction = piece.isWhite() ? -1 : 1;
            if ((piece.isWhite() && fromRow == 3) || (!piece.isWhite() && fromRow == 4)) {
                if (toRow == fromRow + direction) {
                    ChessPiece capturedPawn = board[fromRow][toCol];
                    if (capturedPawn != null) {
                        board[fromRow][toCol] = null;
                        if (chessBoard != null) {
                            chessBoard.getChildren().remove(capturedPawn.getImageView());
                        }
                    }
                }
            }
        }

        board[fromRow][fromCol] = null;
        if (chessBoard != null) {
            chessBoard.getChildren().remove(piece.getImageView());
            if (targetPiece != null) {
                chessBoard.getChildren().removeIf(node -> node instanceof ImageView &&
                        GridPane.getRowIndex(node) == toRow && GridPane.getColumnIndex(node) == toCol);
            }
            ImageView pieceImage = piece.getImageView();
            pieceImage.setTranslateX(0);
            pieceImage.setTranslateY(0);
            pieceImage.setFitWidth(TILE_SIZE * 0.9);
            pieceImage.setFitHeight(TILE_SIZE * 0.9);
            chessBoard.add(pieceImage, toCol, toRow);
        }
        board[toRow][toCol] = piece;
        piece.setHasMoved(true);
    }

    public void animateMove(int fromRow, int fromCol, int toRow, int toCol, GridPane chessBoard, Runnable onComplete) {
        ChessPiece piece = board[fromRow][fromCol];
        if (piece == null || chessBoard == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        ChessPiece targetPiece = board[toRow][toCol];
        if (targetPiece != null && targetPiece.isWhite() == piece.isWhite()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        if (piece.getType().equals("king") && Math.abs(fromCol - toCol) == 2) {
            int rookFromCol = (toCol > fromCol) ? 7 : 0;
            int rookToCol = (toCol > fromCol) ? fromCol + 1 : fromCol - 1;
            ChessPiece rook = board[fromRow][rookFromCol];
            if (rook != null) {
                TranslateTransition rookTransition = new TranslateTransition(Duration.millis(150), rook.getImageView());
                rookTransition.setToX((rookToCol - rookFromCol) * TILE_SIZE);
                rookTransition.setToY(0);
                rookTransition.setOnFinished(e -> {
                    chessBoard.getChildren().remove(rook.getImageView());
                    rook.getImageView().setTranslateX(0);
                    rook.getImageView().setTranslateY(0);
                    chessBoard.add(rook.getImageView(), rookToCol, fromRow);
                    board[fromRow][rookFromCol] = null;
                    board[fromRow][rookToCol] = rook;
                    rook.setHasMoved(true);
                });
                rookTransition.play();
            }
        }

        if (piece.getType().equals("pawn") && Math.abs(fromCol - toCol) == 1 && getPiece(toRow, toCol) == null) {
            int direction = piece.isWhite() ? -1 : 1;
            if ((piece.isWhite() && fromRow == 3) || (!piece.isWhite() && fromRow == 4)) {
                if (toRow == fromRow + direction) {
                    ChessPiece capturedPawn = board[fromRow][toCol];
                    if (capturedPawn != null) {
                        board[fromRow][toCol] = null;
                        chessBoard.getChildren().remove(capturedPawn.getImageView());
                    }
                }
            }
        }

        ImageView pieceImage = piece.getImageView();
        TranslateTransition transition = new TranslateTransition(Duration.millis(150), pieceImage);
        transition.setToX((toCol - fromCol) * TILE_SIZE);
        transition.setToY((toRow - fromRow) * TILE_SIZE);
        transition.setOnFinished(e -> {
            chessBoard.getChildren().remove(pieceImage);
            pieceImage.setTranslateX(0);
            pieceImage.setTranslateY(0);
            if (targetPiece != null) {
                chessBoard.getChildren().removeIf(node -> node instanceof ImageView &&
                        GridPane.getRowIndex(node) == toRow && GridPane.getColumnIndex(node) == toCol);
            }
            pieceImage.setFitWidth(TILE_SIZE * 0.9);
            pieceImage.setFitHeight(TILE_SIZE * 0.9);
            chessBoard.add(pieceImage, toCol, toRow);
            board[fromRow][fromCol] = null;
            board[toRow][toCol] = piece;
            piece.setHasMoved(true);
            if (onComplete != null) onComplete.run();
        });
        transition.play();
    }

    public ChessPiece getPiece(int row, int col) {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) return null;
        return board[row][col];
    }

    public void setPiece(int row, int col, ChessPiece piece) {
        if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
            board[row][col] = piece;
        }
    }

    public static void setTileSize(int size) {
        TILE_SIZE = size;
    }

    public static int getTileSize() {
        return TILE_SIZE;
    }

    public static int getBoardSize() {
        return BOARD_SIZE;
    }
}