
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Codec {
    /**
     * Grau de similitud
     */
    public static final float SIMILARITY_THRESHOLD = 10.0F;
    
    /**
     * Metode que recull una serie d'imatges i les codifica segons el GOP establert.
     * @param files_images imatges a codificar
     * @param data dades a omplir, per a cada imatge ens guardem les tesel·les que hem eliminat
     * @return imatges codificades
     */
    public static Map<String, BufferedImage> encodeImages(Map<String, BufferedImage> files_images, Map<String, Map<Integer,ArrayList<Integer>>> data){
        long T_ini = System.nanoTime();
        BufferedImage image_I, image_P;
        String filename_I;
        Map<String, BufferedImage> encoded_images = new TreeMap<>();
        
        BufferedImage [] images  = new BufferedImage[files_images.size()];
        images = (BufferedImage[]) files_images.values().toArray(images);
        
        String[] filenames  = new String[files_images.size()];
        filenames = (String[]) files_images.keySet().toArray(filenames);
        
        Map<Integer,ArrayList<Integer>> data_P;
        BufferedImage last_P = images[0];
        encoded_images.put(filenames[0].substring(0,filenames[0].indexOf('.')) + "_I.jpeg", last_P);
        
        int currentGop = 0;
        for (int i = 1; i < files_images.size(); i++){
            image_I = currentGop < ArgParser.getInstance().getGop() ? last_P : images[i-1];
            image_P = new BufferedImage(images[i].getColorModel(), (WritableRaster)images[i].getData(),
                                images[i].getColorModel().isAlphaPremultiplied(), null);
            data_P = new HashMap<>();
            System.out.print("Filename " + filenames[i]);
            image_P = Codec.encode(new BufferedImage(image_I.getColorModel(), (WritableRaster)image_I.getData(),
                                image_I.getColorModel().isAlphaPremultiplied(), null), image_P, data_P);
            last_P = new BufferedImage(image_P.getColorModel(), (WritableRaster)image_P.getData(),
                                image_P.getColorModel().isAlphaPremultiplied(), null);
            filename_I = filenames[i].substring(0,filenames[i].indexOf('.'));
            
            encoded_images.put(filename_I + ".jpeg", image_P);
            data.put(filename_I,data_P);
            
            currentGop++;
            currentGop = currentGop >= ArgParser.getInstance().getGop() ? 0 : currentGop;
            //System.out.println("==================\n");
        }
        System.out.printf("Total Time Encode %.2fs\n\n", (System.nanoTime() - T_ini)* 1e-9);
        return encoded_images;
    }
    

    
    /**
     * Metode que recull una serie d'imatges i les descodifica
     * @param files_images imatges a descodificar
     * @param data dades que ens permet descodificarles
     * @return imatges descodificades
     */
    public static Map<String, BufferedImage> decodeImages(Map<String, BufferedImage> files_images, Map<String, Map<Integer,ArrayList<Integer>>> data){
        BufferedImage image_I, image_P;
        Map<String, BufferedImage> decoded_images = new TreeMap<>();
        BufferedImage [] images  = new BufferedImage[files_images.size()];
        images = (BufferedImage[]) files_images.values().toArray(images);
        
        String[] filenames  = new String[files_images.size()];
        filenames = (String[]) files_images.keySet().toArray(filenames);
        String [] keys = new String[data.size()];
        keys = data.keySet().toArray(keys);
        image_I = images[0];
        decoded_images.put(filenames[0],  images[0]);
        for (int i = 1; i < files_images.size(); i++){
            image_P = images[i];
            image_I = Codec.decode(image_I, image_P, data.get(keys[i-1]));
            decoded_images.put(keys[i-1] + ".jpeg", image_I);
        }
        
    
        System.out.println("Decoded images: " + decoded_images.size());
        return decoded_images;
    }
    
    /**
     * Metode que codifica una imatge en base a una altra de referencia.
     * @param baseImage imatge de referencia
     * @param pImage imatge a codificar
     * @param data dades a omplir en cas que tinguem que eliminar tesel·les.
     * @return imatge codificada
     */
    public static BufferedImage encode(BufferedImage baseImage, BufferedImage pImage, Map<Integer,ArrayList<Integer>> data) {
        long T_ini = System.nanoTime();
        int numTiles = ArgParser.getInstance().getNTiles();
        ArrayList<ImageTile> list_tiles = getTiles(baseImage, numTiles);
        int seekRange = ArgParser.getInstance().getSeekRange();
        BufferedImage imageEncoded = new BufferedImage(pImage.getColorModel(), (WritableRaster)pImage.getData(), pImage.getColorModel().isAlphaPremultiplied(), null);
        int numTile = 0;
        for (ImageTile imageTile : list_tiles) {
            BufferedImage baseTile = imageTile.getImage();
            WritableRaster  resultRaster = (WritableRaster)imageEncoded.getData();
            int tes_x0 = imageTile.getStartX();
            int tes_y0 = imageTile.getStartY();
            int imageHeight = imageEncoded.getHeight();
            int imageWidth = imageEncoded.getWidth();
            int tileHeight = baseTile.getHeight();
            int tileWidth = baseTile.getWidth();
            
            boolean found = false;
            for (int y = tes_y0 - seekRange; y <= tes_y0 + seekRange && !found; y++){
                for (int x = tes_x0 - seekRange; x <= tes_x0 + seekRange && !found; x++){
                    if (x >= 0 && x <= imageWidth - tileWidth && y >= 0 && y <= imageHeight - tileHeight)
                    {
                        WritableRaster pRaster = (WritableRaster)resultRaster.createChild(x, y, tileWidth,
                                tileHeight, 0, 0, null);
                        
                        BufferedImage pTile = new BufferedImage(imageEncoded.getColorModel(), pRaster,
                                imageEncoded.getColorModel().isAlphaPremultiplied(), null);
                        
                        if (evaluateSimilarity(baseTile,pTile) < ArgParser.getInstance().getQuality()){
                            float[] averageColorValues = getAverageColor(pTile);
                            Color averageColor =  new Color((int)averageColorValues[0], (int)averageColorValues[1], (int) averageColorValues[2]);
                            
                            for (int j = 0; j < tileHeight; j++) {
                                for (int i = 0; i < tileWidth; i++) {
                                    pImage.setRGB(i + x, j + y, averageColor.getRGB());
                                }
                            }
                            /*System.out.println("Num tesela: " + numTile);
                            System.out.println("X0: " + x);
                            System.out.println("Y0: " + y);*/
                            ArrayList<Integer> x0y0 = new ArrayList<>();
                            x0y0.add(x);
                            x0y0.add(y);
                            data.put(numTile, x0y0);

                            imageEncoded = new BufferedImage(pImage.getColorModel(), (WritableRaster)pImage.getData(),
                                pImage.getColorModel().isAlphaPremultiplied(), null);
                            found = true;
                        }                                     
                    }
                }
            }
            numTile++;
        }
        //System.out.println("Num coincidencias: " + data.size());
        System.out.printf(" => Time Encode %.2fms\n", (System.nanoTime() - T_ini)* 1e-6);
        return imageEncoded;
    }
    
    /**
     * Metode que retorna una llista de tesel·les a partir d'una imatge
     * @param bi imatge a tesel·lar.
     * @param nTiles número de tesel·les (verticals i horitzontals) a dividir la imatge 
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
    
    /**
     * Funcio que calcula la similitud entre 2 tesel·les.
     * @param baseTile tesel·la base
     * @param pTile tesel·la a comprimir.
     * @return valor de qualitat
     */
    private static double evaluateSimilarity(BufferedImage baseTile, BufferedImage pTile){
        float[] averageBaseColor = getAverageColor(baseTile);
        float[] averagePTile = getAverageColor(pTile);
        return SIMILARITY_THRESHOLD * (Math.sqrt(averageBaseColor[0] - averagePTile[0]) + Math.sqrt(averageBaseColor[1] - averagePTile[1]) + Math.sqrt(averageBaseColor[2] - averagePTile[2]));
    }
    
    /**
     * Retorna el valor promig d'una imatge, ja sigui una tesel·la com una imatge completa.
     * @param tile imatge (o tesela)
     * @return Llista amb els valors promig dels 3 canals (RGB)
     */
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
    }
    
    /**
     * Decodifica una imatge basant-se en una altra imatge de referencia i les seves dades (tesel·les eliminades al encode).
     * @param imageBase imatge de referencia
     * @param imageToDecode imatge a decodificar
     * @param data dades per a poder decodificar
     * @return imatge descodificada
     */
    public static BufferedImage decode(BufferedImage imageBase, BufferedImage imageToDecode, Map<Integer,ArrayList<Integer>> data) {
        ArrayList<ImageTile> list_teselas = Codec.getTiles(imageBase,ArgParser.getInstance().getNTiles());
        for (Entry<Integer, ArrayList<Integer>> entry : data.entrySet()){
            imageToDecode = restoreTileColor(imageToDecode, list_teselas.get(entry.getKey()).getImage(), entry.getValue().get(0), entry.getValue().get(1));
        }        
        return imageToDecode;
    }
        
    /**
     * Funcio que restaura el color original de la tesel·la de la imatge a codificar utilitzant la tesel·la base.
     * @param imageToDecode Imatge a recuperar el color
     * @param baseTile tesel·la de la imatge base d'on s'extraura el color
     * @param tes_x0 posicio X de la tesel·la
     * @param tes_y0 posicio Y de la tesel·la
     * @return imatge restaurada (descodificada)
     */
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
    

