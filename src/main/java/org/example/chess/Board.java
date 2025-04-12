package org.example.chess;

import javafx.scene.layout.GridPane;

public class Board {
    private static final int BOARD_SIZE = 8; // Kích thước bàn cờ 8x8
    private static final int TILE_SIZE = 80; // Kích thước mỗi ô
    private ChessPiece[][] board; // Mảng lưu quân cờ

    public Board() {
        board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
    }

    public void initializeBoard(GridPane chessBoard) {
        board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];

        // Quân trắng
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

        // Quân đen
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
                    chessBoard.add(board[row][col].getImageView(), col, row);
                }
            }
        }
    }

    public void movePiece(int fromRow, int fromCol, int toRow, int toCol, GridPane chessBoard) {
        ChessPiece piece = board[fromRow][fromCol];
        if (piece == null) {
            System.out.println("No piece at (" + fromRow + ", " + fromCol + ")");
            return;
        }

        // Kiểm tra ô đích có quân cùng màu không
        ChessPiece targetPiece = board[toRow][toCol];
        if (targetPiece != null && targetPiece.isWhite() == piece.isWhite()) {
            System.out.println("Cannot move to a square occupied by a piece of the same color!");
            return;
        }

        // Xử lý nhập thành
        if (piece.getType().equals("king") && Math.abs(fromCol - toCol) == 2) {
            int rookFromCol = (toCol > fromCol) ? 7 : 0;
            int rookToCol = (toCol > fromCol) ? fromCol + 1 : fromCol - 1;
            ChessPiece rook = board[fromRow][rookFromCol];
            if (rook != null) {
                board[fromRow][rookFromCol] = null;
                board[fromRow][rookToCol] = rook;
                if (chessBoard != null) {
                    chessBoard.getChildren().remove(rook.getImageView());
                    chessBoard.add(rook.getImageView(), rookToCol, fromRow);
                }
                rook.setHasMoved(true);
            }
        }

        // Xử lý bắt tốt qua đường
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
                        System.out.println("Captured pawn via en passant at (" + fromRow + ", " + toCol + ")");
                    }
                }
            }
        }

        // Xóa quân cờ tại vị trí cũ
        board[fromRow][fromCol] = null;

        // Cập nhật giao diện
        if (chessBoard != null) {
            // Xóa ImageView của quân cờ tại vị trí cũ
            chessBoard.getChildren().remove(piece.getImageView());

            // Xóa tất cả ImageView tại ô đích để tránh đè
            if (targetPiece != null) {
                System.out.println("Attempting to capture " + targetPiece.getType() + " at (" + toRow + ", " + toCol + ")");
                if (!chessBoard.getChildren().remove(targetPiece.getImageView())) {
                    System.out.println("Warning: Failed to remove ImageView of " + targetPiece.getType() + " at (" + toRow + ", " + toCol + ")");
                }
                // Debug đặc biệt cho xe đen tại h8
                if (toRow == 0 && toCol == 7 && targetPiece.getType().equals("rook") && !targetPiece.isWhite()) {
                    System.out.println("Captured black rook at h8 (0, 7)");
                }
            }

            // Đặt lại vị trí của ImageView và thêm vào vị trí mới
            piece.getImageView().setTranslateX(0);
            piece.getImageView().setTranslateY(0);
            chessBoard.add(piece.getImageView(), toCol, toRow);
        } else {
            if (targetPiece != null) {
                System.out.println("Captured " + targetPiece.getType() + " at (" + toRow + ", " + toCol + ")");
            }
        }

        // Cập nhật vị trí của quân cờ trên mảng board
        board[toRow][toCol] = piece;
        piece.setHasMoved(true);
        System.out.println("Moved " + piece.getType() + " from (" + fromRow + ", " + fromCol + ") to (" + toRow + ", " + toCol + ")");
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

    public static int getTileSize() {
        return TILE_SIZE;
    }

    public static int getBoardSize() {
        return BOARD_SIZE;
    }
}