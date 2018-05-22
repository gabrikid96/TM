
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author grodrich7.alumnes
 */
public class Codec {
    public static final float SIMILARITY_THRESHOLD = 5.0F;
    
    public static BufferedImage Encode(BufferedImage baseImage, BufferedImage pImage, Map<Integer,ArrayList<Integer>> data) {
        long T_ini = System.nanoTime();
        int numTiles = ArgParser.getInstance().getNTiles();
        ArrayList<ImageTile> list_tiles = getTiles(baseImage, numTiles);
        int seekRange = ArgParser.getInstance().getSeekRange();
        BufferedImage result = new BufferedImage(pImage.getColorModel(), (WritableRaster)pImage.getData(), pImage.getColorModel().isAlphaPremultiplied(), null);
        int numTile = 0;
        for (ImageTile imageTile : list_tiles) {
            BufferedImage baseTile = imageTile.getImage();
            WritableRaster  resultRaster = (WritableRaster)result.getData();
            int tes_x0 = imageTile.getStartX();
            int tes_y0 = imageTile.getStartY();
            int imageHeight = result.getHeight();
            int imageWidth = result.getWidth();
            int tileHeight = baseTile.getHeight();
            int tileWidth = baseTile.getWidth();
            
            boolean found = false;
            for (int y = tes_y0 - seekRange; y <= tes_y0 + seekRange && !found; y++){
                for (int x = tes_x0 - seekRange; x <= tes_x0 + seekRange && !found; x++){
                    if (x >= 0 && x <= imageWidth - tileWidth && y >= 0 && y <= imageHeight - tileHeight)
                    {
                        WritableRaster tesela_destino_wras = (WritableRaster)resultRaster.createChild(x, y, tileWidth,
                                tileHeight, 0, 0, null);
                        
                        BufferedImage tesela_destino = new BufferedImage(result.getColorModel(), tesela_destino_wras,
                                result.getColorModel().isAlphaPremultiplied(), null);
                        
                        if (evaluateSimilarity(baseTile,tesela_destino) < ArgParser.getInstance().getQuality()){
                            float[] averageColorValues = getAverageColor(tesela_destino);
                            Color averageColor =  new Color((int)averageColorValues[0], (int)averageColorValues[1], (int) averageColorValues[2]);
                            
                            for (int j = 0; j < tileHeight; j++) {
                                for (int i = 0; i < tileWidth; i++) {
                                    pImage.setRGB(i + x, j + y, averageColor.getRGB());
                                }
                            }
                            System.out.println("Num tesela: " + numTile);
                            System.out.println("X0: " + x);
                            System.out.println("Y0: " + y);
                            ArrayList<Integer> x0y0 = new ArrayList<>();
                            x0y0.add(x);
                            x0y0.add(y);
                            data.put(numTile, x0y0);

                            result = new BufferedImage(pImage.getColorModel(), (WritableRaster)pImage.getData(),
                                pImage.getColorModel().isAlphaPremultiplied(), null);
                            found = true;
                        }                                     
                    }
                }
            }
            numTile++;
        }
        System.out.println("Num coincidencias: " + data.size());
        System.out.printf("Time Encode %.2fms\n", (System.nanoTime() - T_ini)* 1e-6);
        return result;
    }
    
    
    /**
     *
     * @param bi
     * @param nTiles
     * @return
     */
    public static ArrayList<ImageTile> getTiles(BufferedImage bi, int nTiles) {
    ArrayList<ImageTile> list_teselas = new ArrayList();
    int altura = bi.getHeight();
    int ancho = bi.getWidth();
    double tamy = (double) altura / nTiles;
    double tamx = (double) ancho / nTiles;
    WritableRaster wras = (WritableRaster)bi.getData();
    
    for (int x = 0; x < nTiles; x++) {
      for (int y = 0; y < nTiles; y++)
        if (x * tamx + tamx <= ancho && y * tamy + tamy <= altura)
        {
            WritableRaster tesela = (WritableRaster)wras.createChild((int)(x * tamx), (int)(y * tamy),(int)tamx, (int)tamy, 0, 0, null);
            BufferedImage imagen = new BufferedImage(bi.getColorModel(), tesela, bi.getColorModel().isAlphaPremultiplied(), null);
            list_teselas.add(new ImageTile(imagen, (int)(x * tamx), (int)(y * tamy)));
        }
    }
    return list_teselas;
  }
    
    private static double evaluateSimilarity(BufferedImage baseTile, BufferedImage pTile){
        float[] averageBaseColor = getAverageColor(baseTile);
        float[] averagePTile = getAverageColor(pTile);
        return SIMILARITY_THRESHOLD * (Math.sqrt(averageBaseColor[0] - averagePTile[0]) + Math.sqrt(averageBaseColor[1] - averagePTile[1]) + Math.sqrt(averageBaseColor[2] - averagePTile[2]));
    }
    
    private static float[] getAverageColor(BufferedImage tile){
        int heightTile = tile.getHeight();
        int widthTile = tile.getWidth();
        float red = 0.f, green = 0.f, blue = 0.f;
        int totalPixels = heightTile * widthTile;
        for (int h = 0; h < heightTile; h++) {
            for (int p = 0; p < widthTile; p++) {
                Color cd = new Color(tile.getRGB(p, h));
                red += cd.getRed();
                green += cd.getGreen();
                blue += cd.getBlue();
            }
        }
        return new float [] {red /= totalPixels,green /= totalPixels,blue /= totalPixels};
        //return new Color((int)(red /= totalPixels), (int)(green /= totalPixels), (int) (blue /= totalPixels));
    }
    
    public static BufferedImage Decode(BufferedImage imageBase, BufferedImage imageToDecode, Map<Integer,ArrayList<Integer>> data) {
        ArrayList<ImageTile> list_teselas = Codec.getTiles(imageBase,ArgParser.getInstance().getNTiles());
        for (Entry<Integer, ArrayList<Integer>> entry : data.entrySet()){
            imageToDecode = restoreTileColor(imageToDecode, list_teselas.get(entry.getKey()).getImage(), entry.getValue().get(0), entry.getValue().get(1));
        }        
        return imageToDecode;
    }
        
    private static BufferedImage restoreTileColor(BufferedImage imageToDecode, BufferedImage baseTile, int tes_x0, int tes_y0){
        int heightTile = baseTile.getHeight();
        int widthTile = baseTile.getWidth();
        Color color_tes,color_img; 
        int[] rgb_sum;
        for  (int i = 0; i < widthTile; i++){
            for (int j = 0; j < heightTile; j++){
                color_tes = new Color(baseTile.getRGB(i, j));
                color_img = new Color(imageToDecode.getRGB(i + tes_x0, j + tes_y0));
                int[] rgb_tes = {color_tes.getRed(),color_tes.getGreen(), color_tes.getBlue()};
                int[] rgb_img = {color_img.getRed(),color_img.getGreen(), color_img.getBlue()};
                
                rgb_sum = new int[3];
                for (int channel = 0; channel < 3; channel++){
                    rgb_img[channel] = rgb_img[channel] >= 128 ? -((rgb_img[channel] -128) * 2) : rgb_img[channel] * 2;
                    rgb_img[channel] = rgb_img[channel] > 255 ? 255 : 0;
                    rgb_sum[channel] += rgb_tes[channel];
                }
                imageToDecode.setRGB(i + tes_x0, j+ tes_y0, new Color(rgb_sum[0], rgb_sum[1], rgb_sum[2]).getRGB());
            }
        }
        return imageToDecode;
    }

    
}
    

