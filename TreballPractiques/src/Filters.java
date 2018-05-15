
import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author grodrich7.alumnes
 */
public class Filters {
    public static final int DEFAULT = 0;    
    public static final int BINARITATION = 1;    
    public static final int NEGATIVATION = 2;    
    public static final int AVERAGING = 3;

    
    public static BufferedImage BinaritationFilter(BufferedImage image, int threshold){
    
        BufferedImage bi = new BufferedImage(image.getWidth(),  image.getHeight(), image.getType());
        for(int i = 0; i < image.getWidth(); i++) {
            for(int j = 0; j < image.getHeight(); j++) {
                int rgb = image.getRGB(i,j);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = (rgb >> 0) & 0xff;
                   
                r = r > threshold || g > threshold || b > threshold ? 255 : 0;
                g = r > threshold || g > threshold || b > threshold ? 255 : 0;
                b = r > threshold || g > threshold || b > threshold ? 255 : 0;
                rgb= (rgb & 0xff000000) | (r << 16) | (g << 8) | (b << 0);
                bi.setRGB(i, j,rgb);
            }  
        }
        return bi;
    }
    
     public static BufferedImage NegativitationFilter(BufferedImage image){
        int width = image.getWidth();
        int height = image.getHeight();
        //convert to negative
        for(int y = 0; y < height; y++){
          for(int x = 0; x < width; x++){
            int p = image.getRGB(x,y);
            int a = (p>>24)&0xff;
            int r = (p>>16)&0xff;
            int g = (p>>8)&0xff;
            int b = p&0xff;
            //subtract RGB from 255
            r = 255 - r;
            g = 255 - g;
            b = 255 - b;
            //set new RGB value
            p = (a<<24) | (r<<16) | (g<<8) | b;
            image.setRGB(x, y, p);
          }
        }
        
        return image;
    }
     
     public static BufferedImage AveragingFilter(BufferedImage img, int maskSize) {
        for (int y = 1; y + 1 < img.getHeight(); y++) {
            for (int x = 1; x + 1 < img.getWidth(); x++) {
                Color tempColor = getFilteredValue(img, y, x, getMask(maskSize));
                img.setRGB(x, y, tempColor.getRGB());

            }
        }
        return img;
    }
     
     private static int[][] getMask(int maskSize){
         int [][] mask = new int[maskSize][maskSize];
         for (int i = 0; i < maskSize ; i++){
             for (int j = 0; j < maskSize ; j++){
                 mask[i][j] = 1;
             }
         }
         return mask;
     }
 
    private static Color getFilteredValue(final BufferedImage givenImage, int y, int x, int[][] filter) {
        int r = 0, g = 0, b = 0;
        for (int j = -1; j <= 1; j++) {
            for (int k = -1; k <= 1; k++) {
 
                r += (filter[1 + j][1 + k] * (new Color(givenImage.getRGB(x + k, y + j))).getRed());
                g += (filter[1 + j][1 + k] * (new Color(givenImage.getRGB(x + k, y + j))).getGreen());
                b += (filter[1 + j][1 + k] * (new Color(givenImage.getRGB(x + k, y + j))).getBlue());
            }
 
        }
        r = r / sum(filter);
        g = g / sum(filter);
        b = b / sum(filter);
        return new Color(r, g, b);
    }
 
    private static int sum(int[][] filter) {
        int sum = 0;
        for (int y = 0; y < filter.length; y++) {
            for (int x = 0; x < filter[y].length; x++) {
                sum += filter[y][x];
            }
        }
        return sum;
    }
}
