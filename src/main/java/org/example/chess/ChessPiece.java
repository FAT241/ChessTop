package org.example.chess;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

// Lớp định nghĩa một quân cờ
public class ChessPiece {
    private String type; // Loại quân cờ: pawn, rook, knight, bishop, queen, king
    private boolean isWhite; // Màu: true = trắng, false = đen
    private ImageView imageView; // Hình ảnh hiển thị

    // Constructor: Tạo quân cờ với loại, màu và đường dẫn ảnh
    public ChessPiece(String type, boolean isWhite, String imagePath) {
        this.type = type;
        this.isWhite = isWhite;
        try {
            this.imageView = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
            this.imageView.setFitWidth(70); // Kích thước cố định, nhỏ hơn TILE_SIZE (80)
            this.imageView.setFitHeight(70);
        } catch (NullPointerException e) {
            System.err.println("Cannot upload picture: " + imagePath);
            throw e;
        }
    }

    // Getter
    public String getType() { return type; }
    public boolean isWhite() { return isWhite; }
    public ImageView getImageView() { return imageView; }
}