package org.example.chess;

import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;

public class AI {
    private Board board;
    private GameLogic gameLogic;
    private int[] lastMove; // Lưu nước đi cuối: [fromRow, fromCol, toRow, toCol]
    private static final int MAX_DEPTH = 3; // Độ sâu tìm kiếm tối đa
    private static final int PAWN_VALUE = 1;
    private static final int KNIGHT_VALUE = 3;
    private static final int BISHOP_VALUE = 3;
    private static final int ROOK_VALUE = 5;
    private static final int QUEEN_VALUE = 9;
    private static final int KING_VALUE = 1000;

    public AI(Board board, GameLogic gameLogic) {
        this.board = board;
        this.gameLogic = gameLogic;
        this.lastMove = new int[4]; // Khởi tạo mảng để lưu nước đi cuối
    }

    public void makeMove(GridPane chessBoard) {
        List<int[]> validMoves = getAllValidMoves(false); // Lấy tất cả nước đi hợp lệ của AI (quân đen)

        if (validMoves.isEmpty()) {
            return; // Không có nước đi hợp lệ
        }

        // Sử dụng Minimax với Alpha-Beta Pruning để chọn nước đi tốt nhất
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = null;
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (int[] move : validMoves) {
            // Thực hiện nước đi tạm thời
            ChessPiece piece = board.getPiece(move[0], move[1]);
            if (piece == null) continue; // Bỏ qua nếu không có quân cờ

            ChessPiece capturedPiece = board.getPiece(move[2], move[3]);
            board.setPiece(move[0], move[1], null);
            board.setPiece(move[2], move[3], piece);
            gameLogic.setLastMove(move[0], move[1], move[2], move[3]);

            // Xử lý nhập thành
            if (piece.getType().equals("king") && Math.abs(move[1] - move[3]) == 2) {
                int rookFromCol = (move[3] > move[1]) ? 7 : 0;
                int rookToCol = (move[3] > move[1]) ? 5 : 3;
                ChessPiece rook = board.getPiece(move[0], rookFromCol);
                if (rook != null) { // Kiểm tra rook có tồn tại
                    board.setPiece(move[0], rookFromCol, null);
                    board.setPiece(move[0], rookToCol, rook);
                }
            }

            // Đánh giá nước đi bằng Minimax
            int score = minimax(MAX_DEPTH - 1, false, alpha, beta);

            // Khôi phục trạng thái bàn cờ
            board.setPiece(move[0], move[1], piece);
            board.setPiece(move[2], move[3], capturedPiece);
            if (piece.getType().equals("king") && Math.abs(move[1] - move[3]) == 2) {
                int rookFromCol = (move[3] > move[1]) ? 7 : 0;
                int rookToCol = (move[3] > move[1]) ? 5 : 3;
                ChessPiece rook = board.getPiece(move[0], rookToCol);
                if (rook != null) { // Kiểm tra rook có tồn tại
                    board.setPiece(move[0], rookToCol, null);
                    board.setPiece(move[0], rookFromCol, rook);
                }
            }

            // Cập nhật nước đi tốt nhất
            if (score > bestScore) {
                bestScore = score;
                bestMove = move;
            }
            alpha = Math.max(alpha, bestScore);
            if (beta <= alpha) {
                break; // Cắt tỉa Alpha-Beta
            }
        }

        if (bestMove != null) {
            lastMove = bestMove;
            board.movePiece(lastMove[0], lastMove[1], lastMove[2], lastMove[3], chessBoard);
            gameLogic.setLastMove(lastMove[0], lastMove[1], lastMove[2], lastMove[3]); // Lưu nước đi cuối
        }
    }

    // Thu thập tất cả nước đi hợp lệ của một bên
    private List<int[]> getAllValidMoves(boolean isWhite) {
        List<int[]> validMoves = new ArrayList<>();
        for (int fromRow = 0; fromRow < Board.getBoardSize(); fromRow++) {
            for (int fromCol = 0; fromCol < Board.getBoardSize(); fromCol++) {
                ChessPiece piece = board.getPiece(fromRow, fromCol);
                if (piece != null && piece.isWhite() == isWhite) {
                    for (int toRow = 0; toRow < Board.getBoardSize(); toRow++) {
                        for (int toCol = 0; toCol < Board.getBoardSize(); toCol++) {
                            if (gameLogic.isValidMove(piece, fromRow, fromCol, toRow, toCol)) {
                                validMoves.add(new int[]{fromRow, fromCol, toRow, toCol});
                            }
                        }
                    }
                }
            }
        }
        return validMoves;
    }

    // Thuật toán Minimax với Alpha-Beta Pruning
    private int minimax(int depth, boolean isMaximizing, int alpha, int beta) {
        if (depth == 0 || gameLogic.isGameOver(!isMaximizing)) {
            return evaluateBoard();
        }

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            List<int[]> validMoves = getAllValidMoves(false); // AI (quân đen)
            for (int[] move : validMoves) {
                // Thực hiện nước đi tạm thời
                ChessPiece piece = board.getPiece(move[0], move[1]);
                if (piece == null) {
                    System.out.println("Warning: Piece is null at (" + move[0] + ", " + move[1] + ")");
                    continue; // Bỏ qua nếu không có quân cờ
                }

                ChessPiece capturedPiece = board.getPiece(move[2], move[3]);
                board.setPiece(move[0], move[1], null);
                board.setPiece(move[2], move[3], piece);
                gameLogic.setLastMove(move[0], move[1], move[2], move[3]);

                // Xử lý nhập thành
                if (piece.getType().equals("king") && Math.abs(move[1] - move[3]) == 2) {
                    int rookFromCol = (move[3] > move[1]) ? 7 : 0;
                    int rookToCol = (move[3] > move[1]) ? 5 : 3;
                    ChessPiece rook = board.getPiece(move[0], rookFromCol);
                    if (rook != null) { // Kiểm tra rook có tồn tại
                        board.setPiece(move[0], rookFromCol, null);
                        board.setPiece(move[0], rookToCol, rook);
                    }
                }

                // Đánh giá
                int eval = minimax(depth - 1, false, alpha, beta);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);

                // Khôi phục trạng thái
                board.setPiece(move[0], move[1], piece);
                board.setPiece(move[2], move[3], capturedPiece);
                if (piece.getType().equals("king") && Math.abs(move[1] - move[3]) == 2) {
                    int rookFromCol = (move[3] > move[1]) ? 7 : 0;
                    int rookToCol = (move[3] > move[1]) ? 5 : 3;
                    ChessPiece rook = board.getPiece(move[0], rookToCol);
                    if (rook != null) { // Kiểm tra rook có tồn tại
                        board.setPiece(move[0], rookToCol, null);
                        board.setPiece(move[0], rookFromCol, rook);
                    }
                }

                if (beta <= alpha) {
                    break; // Cắt tỉa Alpha-Beta
                }
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            List<int[]> validMoves = getAllValidMoves(true); // Người chơi (quân trắng)
            for (int[] move : validMoves) {
                // Thực hiện nước đi tạm thời
                ChessPiece piece = board.getPiece(move[0], move[1]);
                if (piece == null) {
                    System.out.println("Warning: Piece is null at (" + move[0] + ", " + move[1] + ")");
                    continue; // Bỏ qua nếu không có quân cờ
                }

                ChessPiece capturedPiece = board.getPiece(move[2], move[3]);
                board.setPiece(move[0], move[1], null);
                board.setPiece(move[2], move[3], piece);
                gameLogic.setLastMove(move[0], move[1], move[2], move[3]);

                // Xử lý nhập thành
                if (piece.getType().equals("king") && Math.abs(move[1] - move[3]) == 2) {
                    int rookFromCol = (move[3] > move[1]) ? 7 : 0;
                    int rookToCol = (move[3] > move[1]) ? 5 : 3;
                    ChessPiece rook = board.getPiece(move[0], rookFromCol);
                    if (rook != null) { // Kiểm tra rook có tồn tại
                        board.setPiece(move[0], rookFromCol, null);
                        board.setPiece(move[0], rookToCol, rook);
                    }
                }

                // Đánh giá
                int eval = minimax(depth - 1, true, alpha, beta);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);

                // Khôi phục trạng thái
                board.setPiece(move[0], move[1], piece);
                board.setPiece(move[2], move[3], capturedPiece);
                if (piece.getType().equals("king") && Math.abs(move[1] - move[3]) == 2) {
                    int rookFromCol = (move[3] > move[1]) ? 7 : 0;
                    int rookToCol = (move[3] > move[1]) ? 5 : 3;
                    ChessPiece rook = board.getPiece(move[0], rookToCol);
                    if (rook != null) { // Kiểm tra rook có tồn tại
                        board.setPiece(move[0], rookToCol, null);
                        board.setPiece(move[0], rookFromCol, rook);
                    }
                }

                if (beta <= alpha) {
                    break; // Cắt tỉa Alpha-Beta
                }
            }
            return minEval;
        }
    }

    // Hàm đánh giá trạng thái bàn cờ
    private int evaluateBoard() {
        int score = 0;

        // Đánh giá dựa trên giá trị vật chất
        for (int row = 0; row < Board.getBoardSize(); row++) {
            for (int col = 0; col < Board.getBoardSize(); col++) {
                ChessPiece piece = board.getPiece(row, col);
                if (piece != null) {
                    int pieceValue = getPieceValue(piece.getType());
                    if (piece.isWhite()) {
                        score -= pieceValue; // Quân trắng: trừ điểm
                    } else {
                        score += pieceValue; // Quân đen: cộng điểm
                    }

                    // Đánh giá vị trí (Tốt ở giữa bàn cờ có giá trị hơn)
                    if (piece.getType().equals("pawn")) {
                        int centerDistance = Math.min(Math.abs(row - 3), Math.abs(row - 4)) + Math.min(Math.abs(col - 3), Math.abs(col - 4));
                        if (piece.isWhite()) {
                            score -= (3 - centerDistance); // Tốt trắng ở giữa: trừ ít điểm hơn
                        } else {
                            score += (3 - centerDistance); // Tốt đen ở giữa: cộng thêm điểm
                        }
                    }
                }
            }
        }

        // Phạt nếu Vua bị chiếu
        if (gameLogic.isKingInCheck(false)) { // Vua đen bị chiếu
            score -= 50;
        }
        if (gameLogic.isKingInCheck(true)) { // Vua trắng bị chiếu
            score += 50;
        }

        return score;
    }

    // Trả về giá trị của quân cờ
    private int getPieceValue(String type) {
        switch (type) {
            case "pawn":
                return PAWN_VALUE;
            case "knight":
                return KNIGHT_VALUE;
            case "bishop":
                return BISHOP_VALUE;
            case "rook":
                return ROOK_VALUE;
            case "queen":
                return QUEEN_VALUE;
            case "king":
                return KING_VALUE;
            default:
                return 0;
        }
    }

    public int[] getLastMove() {
        return lastMove;
    }
}