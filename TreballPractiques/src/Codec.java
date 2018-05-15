
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
    int id_tesela = 0;
    int alto_imagen = destino.getHeight(), ancho_imagen = destino.getWidth();    
    int alto_tesela, ancho_tesela;
    long n_coincidencias = 0L;
    float rt, gt, bt,n;
    
    int seekRange = ArgParser.getInstance().getSeekRange();
    int quality = ArgParser.getInstance().getQuality();
    
    for (ImageTile imageTile : list_tiles) {
      BufferedImage tesela = imageTile.getImage();
      int tes_x0 = imageTile.getStartX();
      int tes_y0 = imageTile.getStartY();

      
      alto_tesela = tesela.getHeight();
      ancho_tesela = tesela.getWidth();
     
      rt = 0.0F;
      gt = 0.0F;
      bt = 0.0F;
      n = alto_tesela * ancho_tesela;
      
      for (int i = 0; i < alto_tesela; i++) {
        for (int j = 0; j < ancho_tesela; j++) {
          Color c = new Color(tesela.getRGB(i, j));
          rt += c.getRed();
          gt += c.getGreen();
          bt += c.getBlue();
        }
      }
      rt /= n;
      gt /= n;
      bt /= n;
      
      boolean flag = false;
      
      int output_counter = 0;
      for (int y = tes_y0 - seekRange; y <= tes_y0 + seekRange; y++){
        for (int x = tes_x0 - seekRange; x <= tes_x0 + seekRange; x++){
            if (x >= 0 && x <= ancho_imagen - ancho_tesela && y >= 0 && y <= alto_imagen - alto_tesela)
            {
                  WritableRaster tesela_destino_wras = (WritableRaster)destino.getData().createChild(x, y, ancho_tesela, 
                    alto_tesela, 0, 0, null);
                  
                  BufferedImage tesela_destino = new BufferedImage(destino.getColorModel(), tesela_destino_wras, 
                    tesela.getColorModel().isAlphaPremultiplied(), null);
                  
                  float rtd = 0.f;
                  float gtd = 0.f;
                  float btd = 0.f;
                  int nd = alto_tesela * ancho_tesela;
                  for (int h = 0; h < alto_tesela; h++) {
                    for (int p = 0; p < ancho_tesela; p++) {
                      Color cd = new Color(destino.getRGB(p, h));
                      rtd += cd.getRed();
                      gtd += cd.getGreen();
                      btd += cd.getBlue();
                    }
                  }
                  rtd /= nd;
                  gtd /= nd;
                  btd /= nd;
                  double valor= funcioComparadora(new Color(rt,gt,bt), new Color(rtd,gtd,btd));
                  double valor = funcioComparadora(rt, gt, bt, rtd, gtd, btd);
                  /*if (valor < quality)
                  {
                    n_coincidencias += 1L;
                    switch (Metodo) {
                    case Blanco: 
                      for (int h = y; h < y + alto_tesela; h++) {
                        for (int p = x; p < x + ancho_tesela; p++) {
                          destino.setRGB(p, h, new Color(0, 0, 0).getRGB());
                        }
                      }
                      break;
                    case Negro: 
                      for (int h = y; h < y + alto_tesela; h++) {
                        for (int p = x; p < x + ancho_tesela; p++) {
                          destino.setRGB(p, h, new Color(255, 255, 255).getRGB());
                        }
                      }
                      break;
                    case Resta: 
                      for (int h = y; h < y + alto_tesela; h++) {
                        for (int p = x; p < x + ancho_tesela; p++) {
                          destino.setRGB(p, h, new Color((int)rtd, (int)gtd, (int)btd).getRGB());
                        }
                      }
                      break;
                    case Suave: 
                      destino = restaTesela(destino, tesela, x, y);
                    }*/
                    
                    result = new ImgContainer(ImgContainer.copia(imagen), "Final");
                    
                    result_track = new ImgContainer(ImgContainer.copia(dibujarTesela(imagen_track, tes)), "Proces");
                    break;
                  }
                  if (seekRange == 0) {
                    System.out.print(".");
                  } else if (seekRange < 10) {
                    if (output_counter % seekRange == 0) {
                      System.out.print(".");
                    }
                  }
                  else if (Math.sqrt(output_counter) % seekRange == 0.0D) {
                    System.out.print(".");
                  }
                  

                  output_counter++;
          }
      }
      id_tesela++;
      System.out.println();
    }
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
        if (x * tamx + tamx <= ancho && x * tamy + tamy <= altura)
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

    
}
    

