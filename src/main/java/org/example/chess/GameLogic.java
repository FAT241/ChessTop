package org.example.chess;

public class GameLogic {
    private Board board;
    private int[] lastMove;

    public GameLogic(Board board) {
        this.board = board;
        this.lastMove = new int[4];
    }

    public boolean isValidMove(ChessPiece piece, int fromRow, int fromCol, int toRow, int toCol) {
        if (piece == null || toRow < 0 || toRow >= Board.getBoardSize() || toCol < 0 || toCol >= Board.getBoardSize()) return false;

        // Không cho phép ăn quân cùng màu hoặc ăn vua đối phương
        ChessPiece targetPiece = board.getPiece(toRow, toCol);
        if (targetPiece != null) {
            if (targetPiece.isWhite() == piece.isWhite()) return false;
            if (targetPiece.getType().equals("king")) return false;
        }

        // Kiểm tra nước đi cơ bản
        if (!isValidPieceMove(piece, fromRow, fromCol, toRow, toCol)) return false;

        // Kiểm tra xem nước đi có làm vua bị chiếu không
        ChessPiece capturedPiece = board.getPiece(toRow, toCol);
        board.setPiece(fromRow, fromCol, null);
        board.setPiece(toRow, toCol, piece);
        boolean inCheck = isKingInCheck(piece.isWhite());
        board.setPiece(fromRow, fromCol, piece);
        board.setPiece(toRow, toCol, capturedPiece);
        return !inCheck;
    }

    private boolean isValidPieceMove(ChessPiece piece, int fromRow, int fromCol, int toRow, int toCol) {
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);
        int direction = piece.isWhite() ? -1 : 1;

        switch (piece.getType()) {
            case "pawn":
                if (colDiff == 0 && board.getPiece(toRow, toCol) == null) {
                    if (toRow == fromRow + direction) return true;
                    if ((piece.isWhite() && fromRow == 6) || (!piece.isWhite() && fromRow == 1)) {
                        return toRow == fromRow + 2 * direction && board.getPiece(fromRow + direction, toCol) == null;
                    }
                } else if (colDiff == 1 && toRow == fromRow + direction) {
                    if (board.getPiece(toRow, toCol) != null) return true;
                    int enPassantRow = piece.isWhite() ? 3 : 4;
                    if (fromRow == enPassantRow) {
                        int[] last = getLastMove();
                        int lastFromRow = last[0], lastToRow = last[2], lastToCol = last[3];
                        ChessPiece lastMovedPiece = board.getPiece(lastToRow, lastToCol);
                        if (lastMovedPiece != null && lastMovedPiece.getType().equals("pawn") && lastMovedPiece.isWhite() != piece.isWhite() &&
                                lastFromRow == (piece.isWhite() ? 1 : 6) && lastToRow == enPassantRow && lastToCol == toCol) {
                            return true;
                        }
                    }
                }
                return false;
            case "rook":
                return (rowDiff == 0 || colDiff == 0) && pathClear(fromRow, fromCol, toRow, toCol);
            case "knight":
                return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
            case "bishop":
                return rowDiff == colDiff && pathClear(fromRow, fromCol, toRow, toCol);
            case "queen":
                return (rowDiff == colDiff || rowDiff == 0 || colDiff == 0) && pathClear(fromRow, fromCol, toRow, toCol);
            case "king":
                if (rowDiff <= 1 && colDiff <= 1) return true;
                if (rowDiff == 0 && colDiff == 2 && !piece.hasMoved()) {
                    boolean isWhite = piece.isWhite();
                    int rookCol = (toCol > fromCol) ? 7 : 0;
                    ChessPiece rook = board.getPiece(fromRow, rookCol);
                    if (rook == null || !rook.getType().equals("rook") || rook.hasMoved()) return false;
                    int step = (toCol > fromCol) ? 1 : -1;
                    for (int col = fromCol + step; col != rookCol; col += step) {
                        if (board.getPiece(fromRow, col) != null) return false;
                    }
                    if (isKingInCheck(isWhite)) return false;
                    for (int col = fromCol; col != toCol + step; col += step) {
                        ChessPiece originalFrom = board.getPiece(fromRow, fromCol);
                        ChessPiece originalTo = board.getPiece(fromRow, col);
                        board.setPiece(fromRow, fromCol, null);
                        board.setPiece(fromRow, col, piece);
                        boolean inCheck = isKingInCheck(isWhite);
                        board.setPiece(fromRow, fromCol, originalFrom);
                        board.setPiece(fromRow, col, originalTo);
                        if (inCheck) return false;
                    }
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

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

    public boolean isKingInCheck(boolean isWhite) {
        int kingRow = -1, kingCol = -1;
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

        if (kingRow == -1) {
            System.out.println("King not found for " + (isWhite ? "White" : "Black"));
            return true;
        }

        for (int row = 0; row < Board.getBoardSize(); row++) {
            for (int col = 0; col < Board.getBoardSize(); col++) {
                ChessPiece piece = board.getPiece(row, col);
                if (piece != null && piece.isWhite() != isWhite) {
                    if (isValidPieceMove(piece, row, col, kingRow, kingCol)) {
                        System.out.println(piece.getType() + " at (" + row + ", " + col + ") can attack king at (" + kingRow + ", " + kingCol + ")");
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isGameOver(boolean isWhiteTurn) {
        boolean isWhite = !isWhiteTurn;
        if (!isKingInCheck(isWhite)) return false;

        for (int fromRow = 0; fromRow < Board.getBoardSize(); fromRow++) {
            for (int fromCol = 0; fromCol < Board.getBoardSize(); fromCol++) {
                ChessPiece piece = board.getPiece(fromRow, fromCol);
                if (piece != null && piece.isWhite() == isWhite) {
                    for (int toRow = 0; toRow < Board.getBoardSize(); toRow++) {
                        for (int toCol = 0; toCol < Board.getBoardSize(); toCol++) {
                            if (isValidMove(piece, fromRow, fromCol, toRow, toCol)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }

        System.out.println("Checkmate: " + (isWhite ? "White" : "Black") + " has no valid moves");
        return true;
    }

    public boolean isPawnPromotion(ChessPiece piece, int toRow) {
        return piece.getType().equals("pawn") && ((piece.isWhite() && toRow == 0) || (!piece.isWhite() && toRow == 7));
    }

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