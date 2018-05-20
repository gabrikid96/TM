
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
    
    public static BufferedImage Encode(BufferedImage base, BufferedImage destino, Map<Integer,ArrayList<Integer>> data) {
        long T_ini = System.nanoTime();
        int n_teselas = ArgParser.getInstance().getNTiles();
        ArrayList<ImageTile> list_tiles = getTiles(base, n_teselas);
        
        int alto_tesela, ancho_tesela;
        float rt, gt, bt,n;
        
        int seekRange = ArgParser.getInstance().getSeekRange();
        int quality = ArgParser.getInstance().getQuality();
        BufferedImage result = new BufferedImage(destino.getColorModel(), (WritableRaster)destino.getData(),
                                destino.getColorModel().isAlphaPremultiplied(), null);
        int numTile = 0;
        for (ImageTile imageTile : list_tiles) {
            BufferedImage tesela = imageTile.getImage();
            WritableRaster  result_wras = (WritableRaster)result.getData();
            int tes_x0 = imageTile.getStartX();
            int tes_y0 = imageTile.getStartY();
            int alto_imagen = result.getHeight();
            int ancho_imagen = result.getWidth();
            
            alto_tesela = tesela.getHeight();
            ancho_tesela = tesela.getWidth();
            
            rt = 0.0F;
            gt = 0.0F;
            bt = 0.0F;
            n = alto_tesela * ancho_tesela;
            
            for (int i = 0; i < ancho_tesela; i++) {
                for (int j = 0; j < alto_tesela; j++) {
                    try{
                        Color c = new Color(tesela.getRGB(i, j));
                        rt += c.getRed();
                        gt += c.getGreen();
                        bt += c.getBlue();
                    }catch(Exception ex){                        
                        System.out.println("I: " + i + " J: "+ j);
                        throw ex;
                    }           
                }
            }
            rt /= n;
            gt /= n;
            bt /= n;
            
            for (int y = tes_y0 - seekRange; y <= tes_y0 + seekRange; y++){
                for (int x = tes_x0 - seekRange; x <= tes_x0 + seekRange; x++){
                    if (x >= 0 && x <= ancho_imagen - ancho_tesela && y >= 0 && y <= alto_imagen - alto_tesela)
                    {
                        WritableRaster tesela_destino_wras = (WritableRaster)result_wras.createChild(x, y, ancho_tesela,
                                alto_tesela, 0, 0, null);
                        
                        BufferedImage tesela_destino = new BufferedImage(result.getColorModel(), tesela_destino_wras,
                                tesela.getColorModel().isAlphaPremultiplied(), null);
                        
                        float rtd = 0.f;
                        float gtd = 0.f;
                        float btd = 0.f;
                        int nd = alto_tesela * ancho_tesela;
                        for (int h = 0; h < alto_tesela; h++) {
                            for (int p = 0; p < ancho_tesela; p++) {
                                Color cd = new Color(tesela_destino.getRGB(p, h));
                                rtd += cd.getRed();
                                gtd += cd.getGreen();
                                btd += cd.getBlue();
                            }
                        }
                        rtd /= nd;
                        gtd /= nd;
                        btd /= nd;
                        double valor = funcioComparadora(new Color((int)rt,(int)gt,(int)bt), new Color((int)rtd,(int)gtd,(int)btd));
                        if (valor < quality){
                            destino = smooth_tile_diff(destino,tesela,x,y);
                            ArrayList<Integer> x0y0 = new ArrayList<>();
                            x0y0.add(x);
                            x0y0.add(y);
                            data.put(numTile, x0y0);

                            result = new BufferedImage(destino.getColorModel(), (WritableRaster)destino.getData(),
                                destino.getColorModel().isAlphaPremultiplied(), null);
                            break;
                        }                                     
                    }
                }
            }
            numTile++;
        }
        System.out.printf("Time Encode %.2fms\n\n", (System.nanoTime() - T_ini)* 1e-6);
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
    double tamx = altura / nTiles;
    double tamy = ancho / nTiles;
    

    int h = 0;
    WritableRaster wras = (WritableRaster)bi.getData();
    for (int x = 0; x < nTiles; x++) {
      for (int y = 0; y < nTiles; y++)
        if (x * tamx + tamx <= ancho && y * tamy + tamy <= altura)
        {
            WritableRaster tesela = (WritableRaster)wras.createChild((int)(x * tamx), (int)(y * tamy), 
              (int)tamx, (int)tamy, 0, 0, null);
            BufferedImage imagen = new BufferedImage(bi.getColorModel(), tesela, 
              bi.getColorModel().isAlphaPremultiplied(), null);
            list_teselas.add(new ImageTile(imagen, (int)(x * tamx), (int)(y * tamy)));
            h++;
        }
    }
    return list_teselas;
  }
    
    private static double funcioComparadora(Color c0, Color c1){
        return 10.0 * (Math.sqrt(c0.getRed() - c1.getRed()) + Math.sqrt(c0.getGreen() - c1.getGreen()) + Math.sqrt(c0.getBlue() - c1.getBlue()));
    }
    
    private static BufferedImage smooth_tile_diff(BufferedImage img, BufferedImage tes, int tes_x0, int tes_y0){
        int heightTile = tes.getHeight();
        int widthTile = tes.getWidth();
        float rtd = 0.f;
        float gtd = 0.f;
        float btd = 0.f;
        int nd = heightTile * widthTile;
        for (int h = 0; h < heightTile; h++) {
            for (int p = 0; p < widthTile; p++) {
                Color cd = new Color(tes.getRGB(p, h));
                rtd += cd.getRed();
                gtd += cd.getGreen();
                btd += cd.getBlue();
            }
        }
        rtd /= nd;
        gtd /= nd;
        btd /= nd;
        for (int h = 0; h < heightTile; h++) {
            for (int p = 0; p < widthTile; p++) {
                img.setRGB(h + tes_x0, p+ tes_y0, new Color((int)rtd, (int)gtd, (int)btd).getRGB());
            }
        }/*
        Color color_tes;
        Color color_img;
        int[] rgb_diff;
        for  (int i = 0; i < widthTile; i++){
            for (int j = 0; j < heightTile; j++){
                color_tes = new Color(tes.getRGB(i, j));
                color_img = new Color(img.getRGB(i + tes_x0, j + tes_y0));
                int[] rgb_tes = {color_tes.getRed(),color_tes.getGreen(), color_tes.getBlue()};
                int[] rgb_img = {color_img.getRed(),color_img.getGreen(), color_img.getBlue()};
                rgb_diff = new int[3];
                for (int channel = 0; channel < 3; channel++){
                    rgb_img[channel] -= rgb_tes[channel];
                    if (rgb_diff[channel] >= 0){
                        rgb_diff[channel] = (int) (rgb_diff[channel] / 2.0f);
                    }else{
                        rgb_diff[channel] = -rgb_diff[channel];
                        rgb_diff[channel] = (int) (rgb_diff[channel] / 2.0f);
                        rgb_diff[channel] += 128;
                    }
                }
                img.setRGB(i + tes_x0, j+ tes_y0, new Color(rgb_diff[0], rgb_diff[1], rgb_diff[2]).getRGB());
            }
        }*/
        return img;
    }
    
    public static BufferedImage Decode(BufferedImage base, BufferedImage img, Map<Integer,ArrayList<Integer>> data) {
        ArrayList<ImageTile> list_teselas = Codec.getTiles(base,ArgParser.getInstance().getNTiles());
        for (Entry<Integer, ArrayList<Integer>> entry : data.entrySet()){
            int numTile = entry.getKey();
            int x0_dest = entry.getValue().get(0);
            int y0_dest = entry.getValue().get(1);
            ImageTile tesela = list_teselas.get(numTile);
            BufferedImage tes = tesela.getImage();
            img = smooth_tile_sum(img, tes, x0_dest, y0_dest);
        }
            
        /*while (k >= 0) {
            int x0_dest = data.get(k).get(0);
            int y0_dest = data.get(k).get(1);
            ImageTile tesela = list_teselas.get((int)data.keySet().toArray()[k]);
            BufferedImage tes = tesela.getImage();
            img = smooth_tile_sum(img, tes, x0_dest, y0_dest);
            k--;
        }*/
        
        return img;

    }
        
    private static BufferedImage smooth_tile_sum(BufferedImage img, BufferedImage tes, int tes_x0, int tes_y0){
        int heightTile = tes.getHeight();
        int widthTile = tes.getWidth();
        Color color_tes,color_img; 
        int[] rgb_sum;
        for  (int i = 0; i < widthTile; i++){
            for (int j = 0; j < heightTile; j++){
                color_tes = new Color(tes.getRGB(i, j));
                color_img = new Color(img.getRGB(i + tes_x0, j + tes_y0));
                int[] rgb_tes = {color_tes.getRed(),color_tes.getGreen(), color_tes.getBlue()};
                int[] rgb_img = {color_img.getRed(),color_img.getGreen(), color_img.getBlue()};
                
                rgb_sum = new int[3];
                for (int channel = 0; channel < 3; channel++){
                    if (rgb_img[channel] >= 128){
                        rgb_img[channel] -= 128;
                        rgb_sum[channel] *= 2;
                        rgb_sum[channel] = -rgb_sum[channel];
                    }else{
                        rgb_img[channel] *= 2;
                    }
                    
                    rgb_sum[channel] += rgb_tes[channel];
                    
                    if(rgb_img[channel] > 255){
                        rgb_img[channel] = 255;
                    }else if (rgb_img[channel] < 0){
                        rgb_img[channel] = 0;
                    }
                }
                
                img.setRGB(i + tes_x0, j+ tes_y0, new Color(rgb_sum[0], rgb_sum[1], rgb_sum[2]).getRGB());
            }
        }
        return img;
    }

    
}
    

