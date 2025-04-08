package org.example.chess;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Lớp chính kế thừa từ Application để tạo ứng dụng JavaFX
public class ChessGame extends Application {

    // Kích thước mỗi ô trên bàn cờ (80x80 pixel)
    private static final int TILE_SIZE = 80;
    // Kích thước bàn cờ (8x8 ô)
    private static final int BOARD_SIZE = 8;
    // Mảng 2D lưu trạng thái bàn cờ, mỗi phần tử là quân cờ hoặc null nếu ô trống
    private ChessPiece[][] board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
    // Quân cờ đang được chọn để di chuyển (null nếu chưa chọn)
    private ChessPiece selectedPiece = null;
    // Tọa độ của quân cờ được chọn (hàng, cột)
    private int selectedRow, selectedCol;
    // Xác định lượt chơi: true = trắng (người chơi), false = đen (AI)
    private boolean isWhiteTurn = true;
    // GridPane là container chính để vẽ bàn cờ và quân cờ
    private GridPane chessBoard;
    // Đối tượng Random để AI chọn nước đi ngẫu nhiên
    private Random random = new Random();

    // Phương thức start: Điểm bắt đầu của ứng dụng JavaFX
    @Override
    public void start(Stage primaryStage) {
        // Tạo GridPane để chứa bàn cờ
        chessBoard = new GridPane();

        // Tạo bàn cờ 8x8 với các ô vuông trắng/xám xen kẽ
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // Tạo ô vuông (Rectangle) với kích thước TILE_SIZE
                Rectangle tile = new Rectangle(TILE_SIZE, TILE_SIZE);
                // Màu trắng nếu tổng hàng+cột là chẵn, xám nếu lẻ
                tile.setFill((row + col) % 2 == 0 ? Color.WHITE : Color.GRAY);
                // Thêm ô vào GridPane tại vị trí (col, row)
                chessBoard.add(tile, col, row);
            }
        }

        // Khởi tạo các quân cờ trên bàn cờ
        initializeBoard();

        // Gắn sự kiện chuột vào GridPane để xử lý di chuyển
        chessBoard.setOnMouseClicked(this::handleMouseClick);

        // Tạo Scene với kích thước bàn cờ (8*80 x 8*80)
        Scene scene = new Scene(chessBoard, TILE_SIZE * BOARD_SIZE, TILE_SIZE * BOARD_SIZE);
        // Đặt tiêu đề cửa sổ
        primaryStage.setTitle("Chess Game: Player vs AI");
        // Gắn Scene vào Stage và hiển thị
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Khởi tạo bàn cờ với vị trí ban đầu của các quân cờ
    private void initializeBoard() {
        // Quân trắng (người chơi) ở hàng 7 (quân lớn) và 6 (Tốt)
        board[7][0] = new ChessPiece("rook", true, "pieces/75px_white_rook.png");   // Xe trắng
        board[7][1] = new ChessPiece("knight", true, "pieces/75px_white_knight.png"); // Mã trắng
        board[7][2] = new ChessPiece("bishop", true, "pieces/75px_white_bishop.png"); // Tượng trắng
        board[7][3] = new ChessPiece("queen", true, "pieces/75px_white_queen.png");  // Hậu trắng
        board[7][4] = new ChessPiece("king", true, "pieces/75px_white_king.png");    // Vua trắng
        board[7][5] = new ChessPiece("bishop", true, "pieces/75px_white_bishop.png"); // Tượng trắng
        board[7][6] = new ChessPiece("knight", true, "pieces/75px_white_knight.png"); // Mã trắng
        board[7][7] = new ChessPiece("rook", true, "pieces/75px_white_rook.png");    // Xe trắng
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[6][i] = new ChessPiece("pawn", true, "pieces/75px_white_pawn.png"); // Tốt trắng
        }

        // Quân đen (AI) ở hàng 0 (quân lớn) và 1 (Tốt)
        board[0][0] = new ChessPiece("rook", false, "pieces/75px_black_rook.png");   // Xe đen
        board[0][1] = new ChessPiece("knight", false, "pieces/75px_black_knight.png"); // Mã đen
        board[0][2] = new ChessPiece("bishop", false, "pieces/75px_black_bishop.png"); // Tượng đen
        board[0][3] = new ChessPiece("queen", false, "pieces/75px_black_queen.png");  // Hậu đen
        board[0][4] = new ChessPiece("king", false, "pieces/75px_black_king.png");    // Vua đen
        board[0][5] = new ChessPiece("bishop", false, "pieces/75px_black_bishop.png"); // Tượng đen
        board[0][6] = new ChessPiece("knight", false, "pieces/75px_black_knight.png"); // Mã đen
        board[0][7] = new ChessPiece("rook", false, "pieces/75px_black_rook.png");    // Xe đen
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[1][i] = new ChessPiece("pawn", false, "pieces/75px_black_pawn.png"); // Tốt đen
        }

        // Thêm các quân cờ vào GridPane để hiển thị
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (board[row][col] != null) {
                    chessBoard.add(board[row][col].getImageView(), col, row);
                }
            }
        }
    }

    // Xử lý sự kiện chuột khi người chơi click vào bàn cờ
    private void handleMouseClick(MouseEvent event) {
        // Chỉ cho phép di chuyển khi đến lượt trắng (người chơi)
        if (!isWhiteTurn) return;

        // Tính tọa độ ô được click dựa trên vị trí chuột
        int col = (int) (event.getX() / TILE_SIZE);
        int row = (int) (event.getY() / TILE_SIZE);

        // Kiểm tra tọa độ có nằm trong bàn cờ không
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) return;

        // Nếu chưa chọn quân cờ nào
        if (selectedPiece == null) {
            // Chỉ chọn quân trắng (người chơi)
            if (board[row][col] != null && board[row][col].isWhite()) {
                selectedPiece = board[row][col];
                selectedRow = row;
                selectedCol = col;
                // In log để debug
                System.out.println("Selected piece: " + selectedPiece.getType() + " at (" + row + ", " + col + ")");
            }
        } else {
            // Nếu đã chọn quân cờ, kiểm tra nước đi hợp lệ
            if (isValidMove(selectedPiece, selectedRow, selectedCol, row, col)) {
                // Di chuyển quân cờ
                movePiece(selectedRow, selectedCol, row, col);
                // Chuyển lượt cho AI (đen)
                isWhiteTurn = false;
                selectedPiece = null;
                // Gọi AI thực hiện nước đi
                aiMove();
            } else {
                // In log nếu nước đi không hợp lệ
                System.out.println("Invalid move to (" + row + ", " + col + ")");
                selectedPiece = null; // Reset lựa chọn
            }
        }
    }

    // Di chuyển quân cờ từ vị trí cũ sang vị trí mới
    private void movePiece(int fromRow, int fromCol, int toRow, int toCol) {
        // Lấy quân cờ từ vị trí cũ
        ChessPiece piece = board[fromRow][fromCol];
        // Xóa quân cờ khỏi vị trí cũ trên mảng
        board[fromRow][fromCol] = null;
        // Xóa hình ảnh khỏi GridPane
        chessBoard.getChildren().remove(piece.getImageView());
        // Cập nhật vị trí mới trong mảng
        board[toRow][toCol] = piece;
        // Cập nhật vị trí hiển thị trên GridPane
        GridPane.setConstraints(piece.getImageView(), toCol, toRow);
        chessBoard.getChildren().add(piece.getImageView());
        // In log để debug
        System.out.println("Moved " + piece.getType() + " from (" + fromRow + ", " + fromCol + ") to (" + toRow + ", " + toCol + ")");
    }

    // Logic cho AI (quân đen) thực hiện nước đi
    private void aiMove() {
        // Danh sách lưu tất cả nước đi hợp lệ của quân đen
        List<int[]> possibleMoves = new ArrayList<>();

        // Duyệt toàn bộ bàn cờ để tìm nước đi hợp lệ
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // Chỉ kiểm tra quân đen (không phải trắng)
                if (board[row][col] != null && !board[row][col].isWhite()) {
                    for (int toRow = 0; toRow < BOARD_SIZE; toRow++) {
                        for (int toCol = 0; toCol < BOARD_SIZE; toCol++) {
                            // Nếu nước đi hợp lệ, thêm vào danh sách
                            if (isValidMove(board[row][col], row, col, toRow, toCol)) {
                                possibleMoves.add(new int[]{row, col, toRow, toCol});
                            }
                        }
                    }
                }
            }
        }

        // Nếu có nước đi hợp lệ
        if (!possibleMoves.isEmpty()) {
            // Chọn ngẫu nhiên một nước đi
            int[] move = possibleMoves.get(random.nextInt(possibleMoves.size()));
            // Thực hiện nước đi
            movePiece(move[0], move[1], move[2], move[3]);
            // Chuyển lượt lại cho người chơi (trắng)
            isWhiteTurn = true;
        }
    }

    // Kiểm tra nước đi có hợp lệ không dựa trên luật cờ vua cơ bản
    private boolean isValidMove(ChessPiece piece, int fromRow, int fromCol, int toRow, int toCol) {
        // Không cho phép đi ra ngoài bàn cờ
        if (toRow < 0 || toRow >= BOARD_SIZE || toCol < 0 || toCol >= BOARD_SIZE) return false;
        // Không cho phép ăn quân cùng màu
        if (board[toRow][toCol] != null && board[toRow][toCol].isWhite() == piece.isWhite()) return false;

        // Tính khoảng cách di chuyển
        int rowDiff = Math.abs(toRow - fromRow);
        int colDiff = Math.abs(toCol - fromCol);

        // Kiểm tra luật di chuyển theo loại quân cờ
        switch (piece.getType()) {
            case "pawn":
                if (piece.isWhite()) {
                    // Tốt trắng đi thẳng 1 ô hoặc 2 ô từ hàng 6, không ăn quân
                    if (colDiff == 0 && board[toRow][toCol] == null) {
                        return (toRow == fromRow - 1) || (fromRow == 6 && toRow == 4 && board[5][toCol] == null);
                    }
                    // Tốt trắng ăn chéo 1 ô
                    else if (colDiff == 1 && toRow == fromRow - 1 && board[toRow][toCol] != null) {
                        return true;
                    }
                } else {
                    // Tốt đen đi thẳng 1 ô hoặc 2 ô từ hàng 1, không ăn quân
                    if (colDiff == 0 && board[toRow][toCol] == null) {
                        return (toRow == fromRow + 1) || (fromRow == 1 && toRow == 3 && board[2][toCol] == null);
                    }
                    // Tốt đen ăn chéo 1 ô
                    else if (colDiff == 1 && toRow == fromRow + 1 && board[toRow][toCol] != null) {
                        return true;
                    }
                }
                return false;
            case "rook":
                // Xe đi ngang hoặc dọc, kiểm tra đường đi trống
                return (rowDiff == 0 || colDiff == 0) && pathClear(fromRow, fromCol, toRow, toCol);
            case "knight":
                // Mã đi hình chữ L (2-1 hoặc 1-2)
                return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
            case "bishop":
                // Tượng đi chéo, kiểm tra đường đi trống
                return rowDiff == colDiff && pathClear(fromRow, fromCol, toRow, toCol);
            case "queen":
                // Hậu đi ngang, dọc hoặc chéo, kiểm tra đường đi trống
                return (rowDiff == colDiff || rowDiff == 0 || colDiff == 0) && pathClear(fromRow, fromCol, toRow, toCol);
            case "king":
                // Vua đi 1 ô bất kỳ hướng nào
                return rowDiff <= 1 && colDiff <= 1;
            default:
                return false;
        }
    }

    // Kiểm tra đường đi có bị cản không (dùng cho Xe, Tượng, Hậu)
    private boolean pathClear(int fromRow, int fromCol, int toRow, int toCol) {
        // Tính bước di chuyển (1, -1 hoặc 0) theo hàng và cột
        int rowStep = Integer.compare(toRow, fromRow);
        int colStep = Integer.compare(toCol, fromCol);
        int row = fromRow + rowStep;
        int col = fromCol + colStep;

        // Kiểm tra từng ô trên đường đi
        while (row != toRow || col != toCol) {
            if (board[row][col] != null) return false; // Có quân cản đường
            row += rowStep;
            col += colStep;
        }
        return true; // Đường đi trống
    }

    // Lớp nội bộ định nghĩa một quân cờ
    static class ChessPiece {
        // Loại quân cờ (pawn, rook, knight, bishop, queen, king)
        private String type;
        // Màu quân cờ (true = trắng, false = đen)
        private boolean isWhite;
        // Hình ảnh của quân cờ hiển thị trên giao diện
        private ImageView imageView;

        // Constructor: Tạo quân cờ với loại, màu và đường dẫn ảnh
        public ChessPiece(String type, boolean isWhite, String imagePath) {
            this.type = type;
            this.isWhite = isWhite;
            try {
                // Tải hình ảnh từ resources và tạo ImageView
                this.imageView = new ImageView(new Image(ChessGame.class.getResourceAsStream("/" + imagePath)));
                // Đặt kích thước hình ảnh (nhỏ hơn TILE_SIZE để vừa ô)
                this.imageView.setFitWidth(TILE_SIZE - 10);
                this.imageView.setFitHeight(TILE_SIZE - 10);
            } catch (NullPointerException e) {
                // Báo lỗi nếu không tìm thấy file ảnh
                System.err.println("Không thể tải ảnh: " + imagePath);
                throw e;
            }
        }

        // Getter: Lấy loại quân cờ
        public String getType() { return type; }
        // Getter: Kiểm tra màu quân cờ
        public boolean isWhite() { return isWhite; }
        // Getter: Lấy ImageView để hiển thị
        public ImageView getImageView() { return imageView; }
    }

    // Phương thức main: Điểm khởi chạy ứng dụng JavaFX
    public static void main(String[] args) {
        launch(args);
    }
}