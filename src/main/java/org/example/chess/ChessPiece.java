//Theo dõi trạng thái hasMoved để hỗ trợ các luật như nhập thành hoặc di chuyển tốt hai ô.
package org.example.chess;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class ChessPiece {
    private String type; // Loại quân cờ: pawn, rook, knight, bishop, queen, king
    private boolean isWhite; // Màu: true = trắng, false = đen
    private ImageView imageView; // Hình ảnh hiển thị
    private boolean hasMoved = false; // Dùng để kiểm tra quân cờ đã di chuyển chưa

    // Constructor
    public ChessPiece(String type, boolean isWhite, String imagePath) {
        this.type = type;
        this.isWhite = isWhite;
        try {
            this.imageView = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));
            this.imageView.setFitWidth(70);
            this.imageView.setFitHeight(70);
        } catch (NullPointerException e) {
            System.err.println("Cannot upload picture: " + imagePath);
            throw e;
        }
    }

    // Getter & Setter
    public String getType() { return type; }
    public boolean isWhite() { return isWhite; }
    public ImageView getImageView() { return imageView; }

    public boolean hasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }
}
