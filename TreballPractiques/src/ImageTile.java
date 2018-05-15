
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author grodrich7.alumnes
 */
public class ImageTile {
    private BufferedImage image;
    private int startX;
    private int startY;
    private boolean modified = false;

    public ImageTile(BufferedImage image, int startX, int startY)
    {
      this.image = image;
      this.startX = startX;
      this.startY = startY;
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        this.startX = startX;
    }

    public int getStartY() {
        return startY;
    }

    public void setStartY(int startY) {
        this.startY = startY;
    }

    public static BufferedImage copia(BufferedImage original)
    {
      return new BufferedImage(original.getColorModel(), (WritableRaster)original.getData(), 
        original.getColorModel().isAlphaPremultiplied(), null);
    }

    public boolean isModified()
    {
      return modified;
    }

    public void setModified(boolean modified)
    {
      this.modified = modified;
    }
}
