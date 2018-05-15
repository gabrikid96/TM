
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;

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
    
    public static BufferedImage Encode(BufferedImage base, BufferedImage destino) {
        long T_ini = System.nanoTime();
        int n_teselas = ArgParser.getInstance().getNTiles();
        ArrayList<ImageTile> list_tiles = getTiles(base, n_teselas);
        
        int alto_tesela, ancho_tesela;
        float rt, gt, bt,n;
        
        int seekRange = ArgParser.getInstance().getSeekRange();
        int quality = ArgParser.getInstance().getQuality();
        BufferedImage result = new BufferedImage(destino.getColorModel(), (WritableRaster)destino.getData(),
                                destino.getColorModel().isAlphaPremultiplied(), null);
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
                            System.out.println("Hello");
                            destino = smooth_tile_diff(destino,tesela,x,y);
                            result = new BufferedImage(destino.getColorModel(), (WritableRaster)destino.getData(),
                                destino.getColorModel().isAlphaPremultiplied(), null);
                            break;
                        }                                     
                    }
                }
            }
        }
        long T_fin = System.nanoTime();
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
        Color color_tes,color_img; 
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
        }
        return img;
    }
    
    public static BufferedImage Decode(ImageTile base, ImageTile encoded, int[][] data) {
        ArrayList<ImageTile> list_teselas = Codec.getTiles(base.getImage(),ArgParser.getInstance().getNTiles());
        BufferedImage img = encoded.getImage();
        int k = data[0].length - 1;
        while (k >= 0) {
            int x0_dest = data[3][k];
            int y0_dest = data[4][k];
            ImageTile tesela = list_teselas.get(data[0][k]);
            //System.out.println("decoding id " + tesela.getName() + " (" + count_tesela + " of " + n_teselas + ")");
            BufferedImage tes = tesela.getImage();
            img = smooth_tile_sum(img, tes, x0_dest, y0_dest);
            k--;
        }
        
        return img;

    }
        
    private static BufferedImage smooth_tile_sum(BufferedImage img, BufferedImage tes, int tes_x0, int tes_y0){
        int heightTile = tes.getHeight();
        int widthTile = tes.getWidth();
        Color color_tes,color_img; 
        int[] rgb_sum;
        for  (int i = 0; i < heightTile; i++){
            for (int j = 0; i < widthTile; j++){
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
    

