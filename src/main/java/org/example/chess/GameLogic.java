package org.example.chess;

// Lớp xử lý logic luật chơi cờ vua
public class GameLogic {
    private Board board; // Tham chiếu đến bàn cờ
    private int[] lastMove; // Lưu nước đi cuối: [fromRow, fromCol, toRow, toCol]

    public GameLogic(Board board) {
        this.board = board;
        this.lastMove = new int[4]; // Khởi tạo mảng để lưu nước đi cuối
    }

    // Kiểm tra nước đi hợp lệ cho một quân cờ
    public boolean isValidMove(ChessPiece piece, int fromRow, int fromCol, int toRow, int toCol) {
        return isValidMove(piece, fromRow, fromCol, toRow, toCol, true);
    }

    // Kiểm tra nước đi hợp lệ, với tùy chọn có kiểm tra chiếu hay không
    private boolean isValidMove(ChessPiece piece, int fromRow, int fromCol, int toRow, int toCol, boolean checkKingInCheck) {
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
                    } else if (colDiff == 1 && toRow == fromRow - 1) {
                        // Tốt trắng ăn chéo
                        if (board.getPiece(toRow, toCol) != null) {
                            basicMoveValid = true;
                        }
                        // Bắt tốt qua đường
                        else if (fromRow == 3) {
                            int[] last = getLastMove();
                            int lastFromRow = last[0], lastFromCol = last[1], lastToRow = last[2], lastToCol = last[3];
                            ChessPiece lastMovedPiece = board.getPiece(lastToRow, lastToCol);
                            if (lastMovedPiece != null && lastMovedPiece.getType().equals("pawn") && !lastMovedPiece.isWhite() &&
                                    lastFromRow == 1 && lastToRow == 3 && lastToCol == toCol) {
                                basicMoveValid = true;
                            }
                        }
                    }
                } else {
                    if (colDiff == 0 && board.getPiece(toRow, toCol) == null) {
                        // Tốt đen đi thẳng
                        basicMoveValid = (toRow == fromRow + 1) || (fromRow == 1 && toRow == 3 && board.getPiece(2, toCol) == null);
                    } else if (colDiff == 1 && toRow == fromRow + 1) {
                        // Tốt đen ăn chéo
                        if (board.getPiece(toRow, toCol) != null) {
                            basicMoveValid = true;
                        }
                        // Bắt tốt qua đường
                        else if (fromRow == 4) {
                            int[] last = getLastMove();
                            int lastFromRow = last[0], lastFromCol = last[1], lastToRow = last[2], lastToCol = last[3];
                            ChessPiece lastMovedPiece = board.getPiece(lastToRow, lastToCol);
                            if (lastMovedPiece != null && lastMovedPiece.getType().equals("pawn") && lastMovedPiece.isWhite() &&
                                    lastFromRow == 6 && lastToRow == 4 && lastToCol == toCol) {
                                basicMoveValid = true;
                            }
                        }
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
                // Di chuyển bình thường của Vua
                if (rowDiff <= 1 && colDiff <= 1) {
                    basicMoveValid = true;
                }
                // Nhập thành (castling)
                else if (rowDiff == 0 && colDiff == 2 && !piece.hasMoved()) {
                    boolean isWhite = piece.isWhite();
                    int rookCol = (toCol > fromCol) ? 7 : 0; // Nhập thành bên vua (kingside) hoặc bên hậu (queenside)
                    ChessPiece rook = board.getPiece(fromRow, rookCol);

                    // Kiểm tra điều kiện nhập thành
                    if (rook == null || !rook.getType().equals("rook") || rook.hasMoved()) {
                        return false;
                    }

                    // Kiểm tra đường đi giữa Vua và Xe có trống không
                    int step = (toCol > fromCol) ? 1 : -1;
                    for (int col = fromCol + step; col != rookCol; col += step) {
                        if (board.getPiece(fromRow, col) != null) {
                            return false;
                        }
                    }

                    // Kiểm tra Vua có bị chiếu hoặc đi qua ô bị chiếu không
                    if (isKingInCheck(isWhite)) {
                        return false;
                    }
                    for (int col = fromCol; col != toCol + step; col += step) {
                        // Lưu lại trạng thái tạm
                        ChessPiece originalFrom = board.getPiece(fromRow, fromCol);
                        ChessPiece originalTo = board.getPiece(fromRow, col);

                        board.setPiece(fromRow, fromCol, null);
                        board.setPiece(fromRow, col, piece);

                        boolean inCheck = isKingInCheck(isWhite);

                        // Khôi phục trạng thái
                        board.setPiece(fromRow, fromCol, originalFrom);
                        board.setPiece(fromRow, col, originalTo);

                        if (inCheck) {
                            return false;
                        }
                    }

                    basicMoveValid = true;
                }
                break;
            default:
                return false;
        }

        // Kiểm tra xem nước đi có làm Vua bị chiếu không (nếu yêu cầu)
        if (basicMoveValid && checkKingInCheck) {
            boolean isWhite = piece.isWhite();
            ChessPiece temp = board.getPiece(toRow, toCol);
            board.setPiece(fromRow, fromCol, null);
            board.setPiece(toRow, toCol, piece);
            boolean inCheck = isKingInCheck(isWhite);
            board.setPiece(fromRow, fromCol, piece);
            board.setPiece(toRow, toCol, temp);
            return !inCheck;
        }

        return basicMoveValid;
    }

    // Kiểm tra xem nước đi có dẫn đến phong cấp không
    public boolean isPawnPromotion(ChessPiece piece, int toRow) {
        if (!piece.getType().equals("pawn")) return false;
        if (piece.isWhite() && toRow == 0) return true; // Tốt trắng đến hàng 0
        if (!piece.isWhite() && toRow == 7) return true; // Tốt đen đến hàng 7
        return false;
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

    // Kiểm tra xem Vua có bị chiếu không
    public boolean isKingInCheck(boolean isWhite) {
        int kingRow = -1, kingCol = -1;

        // Tìm vị trí của Vua
        for (int row = 0; row < Board.getBoardSize(); row++) {
            for (int col = 0; col < Board.getBoardSize(); col++) {
                ChessPiece piece = board.getPiece(row, col);
                if (piece != null && piece.getType().equals("king") && piece.isWhite() == isWhite) {
                    kingRow = row;
                    kingCol = col;
                    break;
                }
            }
        }

        // Nếu không tìm thấy Vua (đã bị ăn), trả về true
        if (kingRow == -1) {
            System.out.println("King not found for " + (isWhite ? "White" : "Black")); // Thêm log để debug
            return true;
        }

        // Kiểm tra xem có quân cờ nào của đối phương có thể tấn công Vua không
        for (int row = 0; row < Board.getBoardSize(); row++) {
            for (int col = 0; col < Board.getBoardSize(); col++) {
                ChessPiece piece = board.getPiece(row, col);
                if (piece != null && piece.isWhite() != isWhite) {
                    // Không kiểm tra lại chiếu trong isValidMove để tránh đệ quy
                    boolean canAttackKing = isValidMove(piece, row, col, kingRow, kingCol, false);
                    if (canAttackKing) {
                        System.out.println(piece.getType() + " at (" + row + ", " + col + ") can attack king at (" + kingRow + ", " + kingCol + ")"); // Thêm log để debug
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // Kiểm tra game kết thúc (chiếu hết hoặc Vua bị ăn)
    public boolean isGameOver(boolean isWhiteTurn) {
        boolean isWhite = !isWhiteTurn;

        // Kiểm tra xem Vua của bên hiện tại còn trên bàn cờ không
        boolean kingExists = false;
        int kingRow = -1, kingCol = -1;
        for (int row = 0; row < Board.getBoardSize(); row++) {
            for (int col = 0; col < Board.getBoardSize(); col++) {
                ChessPiece piece = board.getPiece(row, col);
                if (piece != null && piece.getType().equals("king") && piece.isWhite() == isWhite) {
                    kingExists = true;
                    kingRow = row;
                    kingCol = col;
                    break;
                }
            }
        }
        if (!kingExists) {
            System.out.println("Game Over: King of " + (isWhite ? "White" : "Black") + " is captured"); // Thêm log để debug
            return true; // Vua đã bị ăn, game kết thúc
        }

        // Kiểm tra chiếu hết: Vua bị chiếu và không có nước đi hợp lệ để thoát
        if (isKingInCheck(isWhite)) {
            System.out.println("King of " + (isWhite ? "White" : "Black") + " is in check at (" + kingRow + ", " + kingCol + ")");
            for (int fromRow = 0; fromRow < Board.getBoardSize(); fromRow++) {
                for (int fromCol = 0; fromCol < Board.getBoardSize(); fromCol++) {
                    ChessPiece piece = board.getPiece(fromRow, fromCol);
                    if (piece != null && piece.isWhite() == isWhite) {
                        for (int toRow = 0; toRow < Board.getBoardSize(); toRow++) {
                            for (int toCol = 0; toCol < Board.getBoardSize(); toCol++) {
                                if (isValidMove(piece, fromRow, fromCol, toRow, toCol)) {
                                    System.out.println("Valid move found for " + piece.getType() + " from (" + fromRow + ", " + fromCol + ") to (" + toRow + ", " + toCol + ")");
                                    return false; // Có nước đi hợp lệ, chưa chiếu hết
                                }
                            }
                        }
                    }
                }
            }
            System.out.println("Checkmate: " + (isWhite ? "White" : "Black") + " has no valid moves"); // Thêm log để debug
            // In trạng thái bàn cờ khi chiếu hết
            System.out.println("Board state:");
            for (int row = 0; row < Board.getBoardSize(); row++) {
                StringBuilder rowState = new StringBuilder();
                for (int col = 0; col < Board.getBoardSize(); col++) {
                    ChessPiece piece = board.getPiece(row, col);
                    if (piece == null) {
                        rowState.append(" . ");
                    } else {
                        rowState.append(piece.isWhite() ? "W" : "B").append(piece.getType().charAt(0)).append(" ");
                    }
                }
                System.out.println(rowState);
            }
            return true; // Chiếu hết
        }
        return false;
    }

    // Lưu nước đi cuối cùng (dùng cho bắt tốt qua đường)
    public void setLastMove(int fromRow, int fromCol, int toRow, int toCol) {
        lastMove[0] = fromRow;
        lastMove[1] = fromCol;
        lastMove[2] = toRow;
        lastMove[3] = toCol;
    }

    public int[] getLastMove() {
        return lastMove;
    }
}