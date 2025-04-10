package org.example.chess;

import javafx.scene.layout.GridPane;

// Lớp quản lý bàn cờ và trạng thái quân cờ
public class Board {
    private static final int BOARD_SIZE = 8; // Kích thước bàn cờ 8x8
    private static final int TILE_SIZE = 80; // Kích thước mỗi ô
    private ChessPiece[][] board; // Mảng lưu quân cờ

    public Board() {
        board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
    }

    // Khởi tạo bàn cờ với các quân cờ ban đầu
    public void initializeBoard(GridPane chessBoard) {
        // Xóa toàn bộ trạng thái bàn cờ trước khi khởi tạo
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

        // Thêm quân cờ vào GridPane để hiển thị
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] != null) {
                    chessBoard.add(board[row][col].getImageView(), col, row);
                }
            }
        }
    }

    // Di chuyển quân cờ từ vị trí cũ sang vị trí mới, xử lý ăn quân nếu có
    public void movePiece(int fromRow, int fromCol, int toRow, int toCol, GridPane chessBoard) {
        ChessPiece piece = board[fromRow][fromCol]; // Lấy quân cờ đang di chuyển
        if (piece == null) return;

        board[fromRow][fromCol] = null; // Xóa quân cờ khỏi vị trí cũ trong mảng

        // Nếu có chessBoard, cập nhật giao diện
        if (chessBoard != null) {
            chessBoard.getChildren().remove(piece.getImageView()); // Xóa hình ảnh khỏi giao diện tại vị trí cũ

            // Nếu ô đích có quân (tức là ăn quân), xóa quân bị ăn khỏi giao diện
            if (board[toRow][toCol] != null) {
                chessBoard.getChildren().remove(board[toRow][toCol].getImageView()); // Xóa quân bị ăn khỏi GridPane
                System.out.println("Captured " + board[toRow][toCol].getType() + " at (" + toRow + ", " + toCol + ")");
            }

            piece.getImageView().setTranslateX(0); // Reset vị trí translate để tránh lỗi hiển thị
            piece.getImageView().setTranslateY(0);
            chessBoard.getChildren().remove(piece.getImageView()); // Đảm bảo không có bản sao của hình ảnh
            chessBoard.add(piece.getImageView(), toCol, toRow); // Thêm hình ảnh vào GridPane tại vị trí mới
        } else {
            // Nếu không có chessBoard, chỉ ghi log (dùng cho kiểm tra logic)
            if (board[toRow][toCol] != null) {
                System.out.println("Captured " + board[toRow][toCol].getType() + " at (" + toRow + ", " + toCol + ")");
            }
        }

        board[toRow][toCol] = piece; // Đặt quân cờ vào vị trí mới trong mảng
        System.out.println("Moved " + piece.getType() + " from (" + fromRow + ", " + fromCol + ") to (" + toRow + ", " + toCol + ")");
    }

    // Lấy quân cờ tại vị trí (row, col)
    public ChessPiece getPiece(int row, int col) {
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) return null;
        return board[row][col];
    }

    // Getter cho kích thước ô
    public static int getTileSize() {
        return TILE_SIZE;
    }

    // Getter cho kích thước bàn cờ
    public static int getBoardSize() {
        return BOARD_SIZE;
    }
}