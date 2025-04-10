package org.example.chess;

import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AI {
    private Board board;
    private GameLogic gameLogic;
    private Random random;
    private int[] lastMove; // Lưu nước đi cuối: [fromRow, fromCol, toRow, toCol]

    public AI(Board board, GameLogic gameLogic) {
        this.board = board;
        this.gameLogic = gameLogic;
        this.random = new Random();
        this.lastMove = new int[4]; // Khởi tạo mảng để lưu nước đi cuối
    }

    public void makeMove(GridPane chessBoard) {
        List<int[]> validMoves = new ArrayList<>();

        for (int fromRow = 0; fromRow < Board.getBoardSize(); fromRow++) {
            for (int fromCol = 0; fromCol < Board.getBoardSize(); fromCol++) {
                ChessPiece piece = board.getPiece(fromRow, fromCol);
                if (piece != null && !piece.isWhite()) {
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

        if (!validMoves.isEmpty()) {
            lastMove = validMoves.get(random.nextInt(validMoves.size()));
            board.movePiece(lastMove[0], lastMove[1], lastMove[2], lastMove[3], chessBoard);
        }
    }

    public int[] getLastMove() {
        return lastMove;
    }
}