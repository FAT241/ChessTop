package org.example.chess;

import java.util.HashMap;
import java.util.Map;

public class GameLogic {
    private Board board;
    private int[] lastMove;
    private Map<String, Integer> positionCount;
    private int moveCountWithoutCaptureOrPawn;

    public GameLogic(Board board) {
        this.board = board;
        this.lastMove = new int[4];
        this.positionCount = new HashMap<>();
        this.moveCountWithoutCaptureOrPawn = 0;
    }

    public boolean isValidMove(ChessPiece piece, int fromRow, int fromCol, int toRow, int toCol) {
        if (piece == null || toRow < 0 || toRow >= Board.getBoardSize() || toCol < 0 || toCol >= Board.getBoardSize()) return false;

        ChessPiece targetPiece = board.getPiece(toRow, toCol);
        if (targetPiece != null) {
            if (targetPiece.isWhite() == piece.isWhite()) return false;
            if (targetPiece.getType().equals("king")) return false;
        }

        if (!isValidPieceMove(piece, fromRow, fromCol, toRow, toCol)) return false;

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

        // Kiểm tra hòa do lặp ba lần
        String boardState = getBoardState(isWhiteTurn);
        if (positionCount.getOrDefault(boardState, 0) >= 3) {
            System.out.println("Draw: Threefold repetition. State: " + boardState);
            return true;
        }

        // Kiểm tra hòa do luật 50 nước
        if (moveCountWithoutCaptureOrPawn >= 100) {
            System.out.println("Draw: 50-move rule");
            return true;
        }

        // Kiểm tra hòa do thiếu vật chất
        if (isInsufficientMaterial()) {
            System.out.println("Draw: Only 2 kings left");
            return true;
        }

        boolean inCheck = isKingInCheck(isWhite);
        boolean hasValidMove = false;

        for (int fromRow = 0; fromRow < Board.getBoardSize(); fromRow++) {
            for (int fromCol = 0; fromCol < Board.getBoardSize(); fromCol++) {
                ChessPiece piece = board.getPiece(fromRow, fromCol);
                if (piece != null && piece.isWhite() == isWhite) {
                    for (int toRow = 0; toRow < Board.getBoardSize(); toRow++) {
                        for (int toCol = 0; toCol < Board.getBoardSize(); toCol++) {
                            if (isValidMove(piece, fromRow, fromCol, toRow, toCol)) {
                                ChessPiece capturedPiece = board.getPiece(toRow, toCol);
                                board.setPiece(fromRow, fromCol, null);
                                board.setPiece(toRow, toCol, piece);
                                boolean stillInCheck = isKingInCheck(isWhite);
                                board.setPiece(fromRow, fromCol, piece);
                                board.setPiece(toRow, toCol, capturedPiece);
                                if (!stillInCheck) {
                                    hasValidMove = true;
                                    break;
                                }
                            }
                        }
                        if (hasValidMove) break;
                    }
                    if (hasValidMove) break;
                }
            }
            if (hasValidMove) break;
        }

        if (!hasValidMove) {
            if (inCheck) {
                System.out.println("Checkmate: " + (isWhite ? "White" : "Black") + " Lose");
            } else {
                System.out.println("Draw: " + (isWhite ? "White" : "Black") + " No valid moves (Stalemate)");
            }
            return true;
        }

        return false;
    }

    public boolean isInsufficientMaterial() {
        int pieceCount = 0;
        boolean onlyKings = true;
        for (int row = 0; row < Board.getBoardSize(); row++) {
            for (int col = 0; col < Board.getBoardSize(); col++) {
                ChessPiece piece = board.getPiece(row, col);
                if (piece != null) {
                    pieceCount++;
                    if (!piece.getType().equals("king")) {
                        onlyKings = false;
                    }
                }
            }
        }
        return pieceCount == 2 && onlyKings;
    }

    public boolean isPawnPromotion(ChessPiece piece, int toRow) {
        return piece.getType().equals("pawn") && ((piece.isWhite() && toRow == 0) || (!piece.isWhite() && toRow == 7));
    }

    public void setLastMove(int fromRow, int fromCol, int toRow, int toCol) {
        lastMove[0] = fromRow;
        lastMove[1] = fromCol;
        lastMove[2] = toRow;
        lastMove[3] = toCol;

        // Cập nhật bộ đếm cho luật 50 nước
        ChessPiece movedPiece = board.getPiece(toRow, toCol);
        ChessPiece capturedPiece = board.getPiece(toRow, toCol) != movedPiece ? board.getPiece(toRow, toCol) : null;
        if (movedPiece.getType().equals("pawn") || capturedPiece != null) {
            moveCountWithoutCaptureOrPawn = 0;
        } else {
            moveCountWithoutCaptureOrPawn++;
        }

        // Cập nhật trạng thái bàn cờ cho lặp ba lần
        boolean isWhiteTurnAfterMove = !movedPiece.isWhite(); // Lượt đi thay đổi sau khi di chuyển
        String boardState = getBoardState(isWhiteTurnAfterMove);
        positionCount.put(boardState, positionCount.getOrDefault(boardState, 0) + 1);
        System.out.println("Updated position count for state " + boardState + ": " + positionCount.get(boardState));
    }

    public int[] getLastMove() {
        return lastMove;
    }

    public Map<String, Integer> getPositionCount() {
        return positionCount;
    }

    public int getMoveCountWithoutCaptureOrPawn() {
        return moveCountWithoutCaptureOrPawn;
    }

    public String getBoardState(boolean isWhiteTurn) {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < Board.getBoardSize(); row++) {
            for (int col = 0; col < Board.getBoardSize(); col++) {
                ChessPiece piece = board.getPiece(row, col);
                sb.append(piece == null ? "-" : (piece.isWhite() ? "W" : "B") + piece.getType().charAt(0));
            }
        }
        sb.append(isWhiteTurn ? "W" : "B");
        for (int col : new int[]{0, 7}) {
            ChessPiece rook = board.getPiece(7, col);
            ChessPiece king = board.getPiece(7, 4);
            sb.append(rook != null && rook.getType().equals("rook") && !rook.hasMoved() && king != null && !king.hasMoved() ? "1" : "0");
            rook = board.getPiece(0, col);
            king = board.getPiece(0, 4);
            sb.append(rook != null && rook.getType().equals("rook") && !rook.hasMoved() && king != null && !king.hasMoved() ? "1" : "0");
        }
        int enPassantCol = -1;
        if (lastMove[0] == (isWhiteTurn ? 1 : 6) && lastMove[2] == (isWhiteTurn ? 3 : 4) && board.getPiece(lastMove[2], lastMove[3]) != null &&
                board.getPiece(lastMove[2], lastMove[3]).getType().equals("pawn")) {
            enPassantCol = lastMove[3];
        }
        sb.append(enPassantCol);
        return sb.toString();
    }

    public void reset() {
        positionCount.clear();
        moveCountWithoutCaptureOrPawn = 0;
    }
}