package org.example.chess;

import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class AI {
    private Board board;
    private GameLogic gameLogic;
    private int[] lastMove;
    private String difficulty;
    private String gameMode; // To adjust for Blitz mode
    private Random random = new Random();

    // Piece-square tables for positional evaluation (simplified for brevity)
    private static final int[][] PAWN_TABLE = {
            { 0,  0,  0,  0,  0,  0,  0,  0},
            {50, 50, 50, 50, 50, 50, 50, 50},
            {10, 10, 20, 30, 30, 20, 10, 10},
            { 5,  5, 10, 25, 25, 10,  5,  5},
            { 0,  0,  0, 20, 20,  0,  0,  0},
            { 5, -5,-10,  0,  0,-10, -5,  5},
            { 5, 10, 10,-20,-20, 10, 10,  5},
            { 0,  0,  0,  0,  0,  0,  0,  0}
    };
    private static final int[][] KNIGHT_TABLE = {
            {-50,-40,-30,-30,-30,-30,-40,-50},
            {-40,-20,  0,  0,  0,  0,-20,-40},
            {-30,  0, 10, 15, 15, 10,  0,-30},
            {-30,  5, 15, 20, 20, 15,  5,-30},
            {-30,  0, 15, 20, 20, 15,  0,-30},
            {-30,  5, 10, 15, 15, 10,  5,-30},
            {-40,-20,  0,  5,  5,  0,-20,-40},
            {-50,-40,-30,-30,-30,-30,-40,-50}
    };
    private static final int[][] BISHOP_TABLE = {
            {-20,-10,-10,-10,-10,-10,-10,-20},
            {-10,  0,  0,  0,  0,  0,  0,-10},
            {-10,  0,  5, 10, 10,  5,  0,-10},
            {-10,  5,  5, 10, 10,  5,  5,-10},
            {-10,  0, 10, 10, 10, 10,  0,-10},
            {-10, 10, 10,  5,  5, 10, 10,-10},
            {-10,  5,  0,  0,  0,  0,  5,-10},
            {-20,-10,-10,-10,-10,-10,-10,-20}
    };
    private static final int[][] ROOK_TABLE = {
            { 0,  0,  0,  0,  0,  0,  0,  0},
            { 5, 10, 10, 10, 10, 10, 10,  5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            {-5,  0,  0,  0,  0,  0,  0, -5},
            { 0,  0,  0,  5,  5,  0,  0,  0}
    };
    private static final int[][] QUEEN_TABLE = {
            {-20,-10,-10, -5, -5,-10,-10,-20},
            {-10,  0,  0,  0,  0,  0,  0,-10},
            {-10,  0,  5,  5,  5,  5,  0,-10},
            { -5,  0,  5,  5,  5,  5,  0, -5},
            {  0,  0,  5,  5,  5,  5,  0, -5},
            {-10,  5,  5,  5,  5,  5,  0,-10},
            {-10,  0,  5,  0,  0,  0,  0,-10},
            {-20,-10,-10, -5, -5,-10,-10,-20}
    };
    private static final int[][] KING_MIDDLE_TABLE = {
            {-30,-40,-40,-50,-50,-40,-40,-30},
            {-30,-40,-40,-50,-50,-40,-40,-30},
            {-30,-40,-40,-50,-50,-40,-40,-30},
            {-30,-40,-40,-50,-50,-40,-40,-30},
            {-20,-30,-30,-40,-40,-30,-30,-20},
            {-10,-20,-20,-20,-20,-20,-20,-10},
            { 20, 20,  0,  0,  0,  0, 20, 20},
            { 20, 30, 10,  0,  0, 10, 30, 20}
    };
    private static final int[][] KING_END_TABLE = {
            {-50,-40,-30,-20,-20,-30,-40,-50},
            {-30,-20,-10,  0,  0,-10,-20,-30},
            {-30,-10, 20, 30, 30, 20,-10,-30},
            {-30,-10, 30, 40, 40, 30,-10,-30},
            {-30,-10, 30, 40, 40, 30,-10,-30},
            {-30,-10, 20, 30, 30, 20,-10,-30},
            {-30,-30,  0,  0,  0,  0,-30,-30},
            {-50,-30,-30,-30,-30,-30,-30,-50}
    };

    public AI(Board board, GameLogic gameLogic, String difficulty) {
        this.board = board;
        this.gameLogic = gameLogic;
        this.difficulty = difficulty;
        this.lastMove = new int[4];
        this.gameMode = "Standard"; // Default, updated via setGameMode
    }

    public void setGameMode(String gameMode) {
        this.gameMode = gameMode;
    }

    public void makeMove(GridPane chessBoard) {
        List<int[]> validMoves = getValidMoves(false); // Black's moves
        if (validMoves.isEmpty()) {
            System.out.println("No valid moves for Black");
            return;
        }

        int[] bestMove = switch (difficulty) {
            case "Easy" -> randomMove(validMoves);
            case "Medium" -> minimaxMove(validMoves, 2); // Shallow Minimax
            case "Hard" -> iterativeDeepeningMove(validMoves);
            default -> randomMove(validMoves);
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

    private int[] randomMove(List<int[]> validMoves) {
        return validMoves.get(random.nextInt(validMoves.size()));
    }

    private int[] minimaxMove(List<int[]> validMoves, int depth) {
        int bestEval = Integer.MIN_VALUE;
        int[] bestMove = null;

        // Move ordering: prioritize captures and checks
        validMoves.sort(Comparator.comparingInt(move -> -evaluateMove(move)));

        for (int[] move : validMoves) {
            ChessPiece piece = board.getPiece(move[0], move[1]);
            ChessPiece capturedPiece = board.getPiece(move[2], move[3]);
            board.setPiece(move[0], move[1], null);
            board.setPiece(move[2], move[3], piece);
            gameLogic.setLastMove(move[0], move[1], move[2], move[3]);

            int eval = minimax(depth - 1, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
            board.setPiece(move[0], move[1], piece);
            board.setPiece(move[2], move[3], capturedPiece);

            if (eval > bestEval) {
                bestEval = eval;
                bestMove = move;
            }
        }
        return bestMove != null ? bestMove : randomMove(validMoves);
    }

    private int[] iterativeDeepeningMove(List<int[]> validMoves) {
        int maxDepth = gameMode.equals("Blitz") ? 3 : 4; // Shallower in Blitz
        int[] bestMove = null;
        int bestEval = Integer.MIN_VALUE;
        long startTime = System.currentTimeMillis();
        long timeLimit = gameMode.equals("Blitz") ? 1000 : 2000; // 1s for Blitz, 2s for Standard

        for (int depth = 1; depth <= maxDepth; depth++) {
            int[] currentBestMove = null;
            int currentBestEval = Integer.MIN_VALUE;

            // Move ordering
            validMoves.sort(Comparator.comparingInt(move -> -evaluateMove(move)));

            for (int[] move : validMoves) {
                ChessPiece piece = board.getPiece(move[0], move[1]);
                ChessPiece capturedPiece = board.getPiece(move[2], move[3]);
                board.setPiece(move[0], move[1], null);
                board.setPiece(move[2], move[3], piece);
                gameLogic.setLastMove(move[0], move[1], move[2], move[3]);

                int eval = minimax(depth - 1, false, Integer.MIN_VALUE, Integer.MAX_VALUE);
                board.setPiece(move[0], move[1], piece);
                board.setPiece(move[2], move[3], capturedPiece);

                if (eval > currentBestEval) {
                    currentBestEval = eval;
                    currentBestMove = move;
                }

                if (System.currentTimeMillis() - startTime > timeLimit) {
                    break; // Time limit reached
                }
            }

            if (currentBestMove != null) {
                bestMove = currentBestMove;
                bestEval = currentBestEval;
            }

            if (System.currentTimeMillis() - startTime > timeLimit) {
                break; // Stop deepening if time is up
            }
        }

        return bestMove != null ? bestMove : randomMove(validMoves);
    }

    private int minimax(int depth, boolean isMaximizing, int alpha, int beta) {
        if (depth == 0 || gameLogic.isGameOver(!isMaximizing)) {
            return evaluateBoard();
        }

        List<int[]> validMoves = getValidMoves(!isMaximizing);
        if (validMoves.isEmpty()) {
            return evaluateBoard();
        }

        // Move ordering
        validMoves.sort(Comparator.comparingInt(move -> -evaluateMove(move)));

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

                if (beta <= alpha) {
                    break;
                }
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

                if (beta <= alpha) {
                    break;
                }
            }
            return minEval;
        }
    }

    private int evaluateBoard() {
        int materialScore = 0;
        int positionalScore = 0;
        int mobilityScore = 0;
        int kingSafetyScore = 0;
        int pawnStructureScore = 0;

        int whiteMaterial = 0;
        int blackMaterial = 0;
        int gamePhase = getGamePhase(); // 0=opening, 1=middlegame, 2=endgame

        for (int row = 0; row < Board.getBoardSize(); row++) {
            for (int col = 0; col < Board.getBoardSize(); col++) {
                ChessPiece piece = board.getPiece(row, col);
                if (piece != null) {
                    int value = switch (piece.getType()) {
                        case "pawn" -> 100;
                        case "knight", "bishop" -> 300;
                        case "rook" -> 500;
                        case "queen" -> 900;
                        default -> 0; // King
                    };
                    int posValue = getPieceSquareValue(piece, row, col, gamePhase);
                    int mobility = getValidMoves(piece.isWhite()).stream()
                            .filter(move -> board.getPiece(move[0], move[1]) == piece)
                            .toList().size();

                    if (piece.isWhite()) {
                        whiteMaterial += value;
                        materialScore -= value;
                        positionalScore -= posValue;
                        mobilityScore -= mobility * 5; // 5 centipawns per move
                    } else {
                        blackMaterial += value;
                        materialScore += value;
                        positionalScore += posValue;
                        mobilityScore += mobility * 5;
                    }

                    if (piece.getType().equals("king")) {
                        kingSafetyScore += piece.isWhite() ? -evaluateKingSafety(piece.isWhite(), row, col) :
                                evaluateKingSafety(piece.isWhite(), row, col);
                    }
                }
            }
        }

        // Pawn structure: penalize doubled pawns, reward passed pawns
        pawnStructureScore = evaluatePawnStructure();

        // Adjust scores based on game phase
        int materialWeight = gamePhase == 2 ? 80 : 100; // Less focus on material in endgame
        int positionalWeight = gamePhase == 0 ? 80 : 100; // Less focus on position in opening
        int mobilityWeight = gamePhase == 1 ? 100 : 80; // More focus on mobility in middlegame
        int kingSafetyWeight = gamePhase == 2 ? 50 : 100; // Less focus on king safety in endgame
        int pawnStructureWeight = gamePhase == 2 ? 120 : 100; // More focus on pawns in endgame

        return (materialScore * materialWeight) / 100 +
                (positionalScore * positionalWeight) / 100 +
                (mobilityScore * mobilityWeight) / 100 +
                (kingSafetyScore * kingSafetyWeight) / 100 +
                (pawnStructureScore * pawnStructureWeight) / 100;
    }

    private int getPieceSquareValue(ChessPiece piece, int row, int col, int gamePhase) {
        int[][] table = switch (piece.getType()) {
            case "pawn" -> PAWN_TABLE;
            case "knight" -> KNIGHT_TABLE;
            case "bishop" -> BISHOP_TABLE;
            case "rook" -> ROOK_TABLE;
            case "queen" -> QUEEN_TABLE;
            case "king" -> gamePhase == 2 ? KING_END_TABLE : KING_MIDDLE_TABLE;
            default -> new int[8][8];
        };
        // Flip row for Black pieces (since tables are for White)
        int tableRow = piece.isWhite() ? 7 - row : row;
        return table[tableRow][col];
    }

    private int evaluateKingSafety(boolean isWhite, int kingRow, int kingCol) {
        int penalty = 0;
        // Check for open files/diagonals around king
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                int r = kingRow + dr;
                int c = kingCol + dc;
                if (r >= 0 && r < 8 && c >= 0 && c < 8) {
                    ChessPiece piece = board.getPiece(r, c);
                    if (piece == null) {
                        penalty += 5; // Open square near king
                    } else if (piece.isWhite() != isWhite) {
                        penalty += 10; // Enemy piece near king
                    }
                }
            }
        }
        return penalty;
    }

    private int evaluatePawnStructure() {
        int score = 0;
        for (int col = 0; col < 8; col++) {
            int whitePawns = 0;
            int blackPawns = 0;
            boolean whitePassed = true;
            boolean blackPassed = true;
            for (int row = 0; row < 8; row++) {
                ChessPiece piece = board.getPiece(row, col);
                if (piece != null && piece.getType().equals("pawn")) {
                    if (piece.isWhite()) {
                        whitePawns++;
                        if (row < 6) { // Not yet promoted
                            for (int c = col - 1; c <= col + 1; c++) {
                                if (c >= 0 && c < 8) {
                                    for (int r = row + 1; r < 8; r++) {
                                        ChessPiece opponent = board.getPiece(r, c);
                                        if (opponent != null && !opponent.isWhite() && opponent.getType().equals("pawn")) {
                                            whitePassed = false;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        blackPawns++;
                        if (row > 1) {
                            for (int c = col - 1; c <= col + 1; c++) {
                                if (c >= 0 && c < 8) {
                                    for (int r = row - 1; r >= 0; r--) {
                                        ChessPiece opponent = board.getPiece(r, c);
                                        if (opponent != null && opponent.isWhite() && opponent.getType().equals("pawn")) {
                                            blackPassed = false;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Penalize doubled pawns
            if (whitePawns > 1) score += 20 * (whitePawns - 1); // Penalty for White
            if (blackPawns > 1) score -= 20 * (blackPawns - 1); // Penalty for Black
            // Reward passed pawns
            if (whitePassed && whitePawns > 0) score -= 50; // Bonus for White
            if (blackPassed && blackPawns > 0) score += 50; // Bonus for Black
        }
        return score;
    }

    private int getGamePhase() {
        int totalMaterial = 0;
        for (int row = 0; row < Board.getBoardSize(); row++) {
            for (int col = 0; col < Board.getBoardSize(); col++) {
                ChessPiece piece = board.getPiece(row, col);
                if (piece != null && !piece.getType().equals("king")) {
                    totalMaterial += switch (piece.getType()) {
                        case "pawn" -> 1;
                        case "knight", "bishop" -> 3;
                        case "rook" -> 5;
                        case "queen" -> 9;
                        default -> 0;
                    };
                }
            }
        }
        if (totalMaterial > 40) return 0; // Opening
        if (totalMaterial > 20) return 1; // Middlegame
        return 2; // Endgame
    }

    private int evaluateMove(int[] move) {
        int score = 0;
        ChessPiece capturedPiece = board.getPiece(move[2], move[3]);
        if (capturedPiece != null) {
            score += switch (capturedPiece.getType()) {
                case "pawn" -> 100;
                case "knight", "bishop" -> 300;
                case "rook" -> 500;
                case "queen" -> 900;
                default -> 0;
            };
        }
        // Bonus for checks
        ChessPiece piece = board.getPiece(move[0], move[1]);
        board.setPiece(move[0], move[1], null);
        board.setPiece(move[2], move[3], piece);
        gameLogic.setLastMove(move[0], move[1], move[2], move[3]);
        if (gameLogic.isKingInCheck(!piece.isWhite())) {
            score += 50;
        }
        board.setPiece(move[0], move[1], piece);
        board.setPiece(move[2], move[3], capturedPiece);
        return score;
    }

    public int[] getLastMove() {
        return lastMove;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }
}