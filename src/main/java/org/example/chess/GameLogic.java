package org.example.chess;

// Lớp xử lý logic luật chơi cờ vua
public class GameLogic {
    private Board board; // Tham chiếu đến bàn cờ

    public GameLogic(Board board) {
        this.board = board;
    }

    // Kiểm tra nước đi hợp lệ cho một quân cờ
    public boolean isValidMove(ChessPiece piece, int fromRow, int fromCol, int toRow, int toCol) {
        // Kiểm tra vị trí đích có hợp lệ không
        if (toRow < 0 || toRow >= Board.getBoardSize() || toCol < 0 || toCol >= Board.getBoardSize()) return false;

        // Không cho phép ăn quân cùng màu
        if (board.getPiece(toRow, toCol) != null && board.getPiece(toRow, toCol).isWhite() == piece.isWhite()) return false;

        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        // Kiểm tra nước đi hợp lệ theo loại quân cờ
        boolean basicMoveValid = false;
        switch (piece.getType()) {
            case "pawn":
                if (piece.isWhite()) {
                    if (colDiff == 0 && board.getPiece(toRow, toCol) == null) {
                        // Tốt trắng đi thẳng
                        basicMoveValid = (toRow == fromRow - 1) || (fromRow == 6 && toRow == 4 && board.getPiece(5, toCol) == null);
                    } else if (colDiff == 1 && toRow == fromRow - 1 && board.getPiece(toRow, toCol) != null) {
                        // Tốt trắng ăn chéo
                        basicMoveValid = true;
                    }
                } else {
                    if (colDiff == 0 && board.getPiece(toRow, toCol) == null) {
                        // Tốt đen đi thẳng
                        basicMoveValid = (toRow == fromRow + 1) || (fromRow == 1 && toRow == 3 && board.getPiece(2, toCol) == null);
                    } else if (colDiff == 1 && toRow == fromRow + 1 && board.getPiece(toRow, toCol) != null) {
                        // Tốt đen ăn chéo
                        basicMoveValid = true;
                    }
                }
                break;
            case "rook":
                basicMoveValid = (rowDiff == 0 || colDiff == 0) && pathClear(fromRow, fromCol, toRow, toCol);
                break;
            case "knight":
                basicMoveValid = (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
                break;
            case "bishop":
                basicMoveValid = rowDiff == colDiff && pathClear(fromRow, fromCol, toRow, toCol);
                break;
            case "queen":
                basicMoveValid = (rowDiff == colDiff || rowDiff == 0 || colDiff == 0) && pathClear(fromRow, fromCol, toRow, toCol);
                break;
            case "king":
                basicMoveValid = rowDiff <= 1 && colDiff <= 1;
                break;
            default:
                return false;
        }

        return basicMoveValid;
    }

    // Kiểm tra đường đi có trống không (dùng cho Xe, Tượng, Hậu)
    private boolean pathClear(int fromRow, int fromCol, int toRow, int toCol) {
        int rowStep = Integer.compare(toRow, fromRow);
        int colStep = Integer.compare(toCol, fromCol);
        int row = fromRow + rowStep;
        int col = fromCol + colStep;

        while (row != toRow || col != toCol) {
            if (board.getPiece(row, col) != null) return false;
            row += rowStep;
            col += colStep;
        }
        return true;
    }

    // Kiểm tra game kết thúc (khi Vua của một bên bị ăn)
    public boolean isGameOver(boolean isWhiteTurn) {
        // Kiểm tra xem Vua của bên hiện tại còn trên bàn cờ không
        for (int row = 0; row < Board.getBoardSize(); row++) {
            for (int col = 0; col < Board.getBoardSize(); col++) {
                ChessPiece piece = board.getPiece(row, col);
                if (piece != null && piece.getType().equals("king") && piece.isWhite() == isWhiteTurn) {
                    return false; // Vua vẫn còn, game chưa kết thúc
                }
            }
        }
        return true; // Vua đã bị ăn, game kết thúc
    }
}