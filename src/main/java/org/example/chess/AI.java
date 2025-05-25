package org.example.chess;

import javafx.scene.layout.GridPane;

import java.util.ArrayList; import java.util.List; import java.util.Random;

public class AI { private Board board; private GameLogic gameLogic; private int[] lastMove; private static final int MAX_DEPTH = 3; private String difficulty;

    public AI(Board board, GameLogic gameLogic, String difficulty) {
        this.board = board;
        this.gameLogic = gameLogic;
        this.difficulty = difficulty;
        this.lastMove = new int[4];
    }

    public void makeMove(GridPane chessBoard) {
        List<int[]> validMoves = getValidMoves(false); // Lấy nước đi hợp lệ cho Đen
        if (validMoves.isEmpty()) {
            System.out.println("Không có nước đi hợp lệ cho Đen");
            return;
        }

        int[] bestMove = switch (difficulty) {
            case "Easy" -> randomMove(validMoves);
            case "Medium" -> greedyMove(validMoves);
            case "Hard" -> minimaxMove(validMoves);
            default -> randomMove(validMoves); // Mặc định là dễ
        };

        if (bestMove != null) {
            lastMove = bestMove;
            board.movePiece(bestMove[0], bestMove[1], bestMove[2], bestMove[3], chessBoard);
        }
    }

    private List<int[]> getValidMoves(boolean isWhite) {
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

    // Thuật toán Dễ: Chọn ngẫu nhiên
    private int[] randomMove(List<int[]> validMoves) {
        Random random = new Random();
        return validMoves.get(random.nextInt(validMoves.size()));
    }

    // Thuật toán Trung bình: Tìm kiếm tham lam
    private int[] greedyMove(List<int[]> validMoves) {
        int bestValue = Integer.MIN_VALUE;
        int[] bestMove = null;

        for (int[] move : validMoves) {
            ChessPiece capturedPiece = board.getPiece(move[2], move[3]);
            int value = 0;
            if (capturedPiece != null) {
                value = switch (capturedPiece.getType()) {
                    case "pawn" -> 1;
                    case "knight", "bishop" -> 3;
                    case "rook" -> 5;
                    case "queen" -> 9;
                    default -> 0;
                };
            }
            if (value > bestValue) {
                bestValue = value;
                bestMove = move;
            }
        }

        return bestMove != null ? bestMove : randomMove(validMoves); // Nếu không ăn được quân, chọn ngẫu nhiên
    }

    // Thuật toán Khó: Minimax với Alpha-Beta Pruning
    private int[] minimaxMove(List<int[]> validMoves) {
        int bestEval = Integer.MIN_VALUE;
        int[] bestMove = null;

        for (int[] move : validMoves) {
            ChessPiece piece = board.getPiece(move[0], move[1]);
            ChessPiece capturedPiece = board.getPiece(move[2], move[3]);
            board.setPiece(move[0], move[1], null);
            board.setPiece(move[2], move[3], piece);
            gameLogic.setLastMove(move[0], move[1], move[2], move[3]);

            int eval = minimax(MAX_DEPTH - 1, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
            board.setPiece(move[0], move[1], piece);
            board.setPiece(move[2], move[3], capturedPiece);

            if (eval > bestEval) {
                bestEval = eval;
                bestMove = move;
            }
        }
        return bestMove;
    }

    private int minimax(int depth, boolean isMaximizing, int alpha, int beta) {
        if (depth == 0 || gameLogic.isGameOver(!isMaximizing)) {
            return evaluateBoard();
        }

        List<int[]> validMoves = getValidMoves(!isMaximizing);
        if (validMoves.isEmpty()) return evaluateBoard();

        if (isMaximizing) {
            int maxEval = Integer.MIN_VALUE;
            for (int[] move : validMoves) {
                ChessPiece piece = board.getPiece(move[0], move[1]);
                ChessPiece capturedPiece = board.getPiece(move[2], move[3]);
                board.setPiece(move[0], move[1], null);
                board.setPiece(move[2], move[3], piece);
                gameLogic.setLastMove(move[0], move[1], move[2], move[3]);

                int eval = minimax(depth - 1, false, alpha, beta);
                maxEval = Math.max(maxEval, eval);
                alpha = Math.max(alpha, eval);

                board.setPiece(move[0], move[1], piece);
                board.setPiece(move[2], move[3], capturedPiece);

                if (beta <= alpha) break;
            }
            return maxEval;
        } else {
            int minEval = Integer.MAX_VALUE;
            for (int[] move : validMoves) {
                ChessPiece piece = board.getPiece(move[0], move[1]);
                ChessPiece capturedPiece = board.getPiece(move[2], move[3]);
                board.setPiece(move[0], move[1], null);
                board.setPiece(move[2], move[3], piece);
                gameLogic.setLastMove(move[0], move[1], move[2], move[3]);

                int eval = minimax(depth - 1, true, alpha, beta);
                minEval = Math.min(minEval, eval);
                beta = Math.min(beta, eval);

                board.setPiece(move[0], move[1], piece);
                board.setPiece(move[2], move[3], capturedPiece);

                if (beta <= alpha) break;
            }
            return minEval;
        }
    }

    private int evaluateBoard() {
        int score = 0;
        for (int row = 0; row < Board.getBoardSize(); row++) {
            for (int col = 0; col < Board.getBoardSize(); col++) {
                ChessPiece piece = board.getPiece(row, col);
                if (piece != null) {
                    int value = switch (piece.getType()) {
                        case "pawn" -> 1;
                        case "knight", "bishop" -> 3;
                        case "rook" -> 5;
                        case "queen" -> 9;
                        case "king" -> 0;
                        default -> 0;
                    };
                    score += piece.isWhite() ? -value : value;
                }
            }
        }
        return score;
    }

    public int[] getLastMove() {
        return lastMove;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

}