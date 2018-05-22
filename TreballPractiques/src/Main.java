
import com.beust.jcommander.JCommander;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author grodrich7.alumnes
 */
public class Main {
    public static JCommander jComander;
    public static ArgParser argParser = ArgParser.getInstance();
    public static int currentFrame = 0;
    public static BufferedImage currentImage;
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        jComander = new JCommander(argParser, args);
        
        MainWindow mainWindow = new MainWindow();
        
        Map<String, BufferedImage> files_images = FileHelper.getImagesFromZip(argParser.getInput());
                
        if (argParser.getEncode()){
            Map<String, Map<Integer,ArrayList<Integer>>> data = new TreeMap<>();
            Map<String, BufferedImage> encoded_images = encode(files_images, data);
            FileHelper.saveImagesToZip(encoded_images,"encoded");
            
            Map<String, BufferedImage> decoded_images = decode(encoded_images,data);
            FileHelper.saveImagesToZip(decoded_images,"decoded");
            
            //Map<String, BufferedImage> decoded_images = decode(encoded_images,data);
            showImages(new ArrayList<>(decoded_images.values()),  mainWindow);            
            //TODO : Fer que guardi aquestes imatges en un zip
            /*ZipHelper.*/
            
            
            
        }
        
        
        /*files_images = applyFilters(files_images);
        if (argParser.getOutput() != null && !argParser.getOutput().isEmpty()){
            FileHelper.saveImages(files_images, argParser.getOutput());
            
        }        
        showImages(new ArrayList<>(files_images.values()),  mainWindow);*/
    }
    
    private static Map<String, BufferedImage> encode(Map<String, BufferedImage> files_images, Map<String, Map<Integer,ArrayList<Integer>>> data){
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
            image_I = currentGop < argParser.getGop() ? last_P : images[i-1];
            image_P = images[i];
            data_P = new HashMap<>();
            System.out.println("==================\nFilename " + filenames[i]);
            image_P = Codec.Encode(new BufferedImage(image_I.getColorModel(), (WritableRaster)image_I.getData(),
                                image_I.getColorModel().isAlphaPremultiplied(), null), image_P, data_P);
            last_P = new BufferedImage(image_P.getColorModel(), (WritableRaster)image_P.getData(),
                                image_P.getColorModel().isAlphaPremultiplied(), null);
            filename_I = filenames[i].substring(0,filenames[i].indexOf('.'));
            
            encoded_images.put(filename_I + ".jpeg", image_P);
            data.put(filename_I,data_P);
            
            currentGop++;
            currentGop = currentGop >= argParser.getGop() ? 0 : currentGop;
            System.out.println("==================\n");
        }
        System.out.println("Encoded images: " + encoded_images.size());
        System.out.printf("Total Time Encode %.2fs\n\n", (System.nanoTime() - T_ini)* 1e-9);
        return encoded_images;
    }
    
    private static Map<String, BufferedImage> decode(Map<String, BufferedImage> files_images, Map<String, Map<Integer,ArrayList<Integer>>> data){
        BufferedImage image_I, image_P;
        Map<String, BufferedImage> decoded_images = new TreeMap<>();
        BufferedImage [] images  = new BufferedImage[files_images.size()];
        images = (BufferedImage[]) files_images.values().toArray(images);
        
        String[] filenames  = new String[files_images.size()];
        filenames = (String[]) files_images.keySet().toArray(filenames);
        String [] keys = new String[data.size()];
        keys = data.keySet().toArray(keys);
        int numKey = 0;
        /*for (int I = 0, P = 1; I < files_images.size(); I+=2, P+=2){
            image_I = images[I];
            filename_I = filenames[I];
            
            image_P = images[P];
            decoded_images.put(filename_I, image_I);
            decoded_images.put(keys[numKey] + ".jpeg", Codec.Decode(image_I, image_P, data.get(keys[numKey])));
            numKey++;
        }*/
        image_I = images[0];
        decoded_images.put(filenames[0],  images[0]);
        for (int i = 1; i < files_images.size(); i++){
            image_P = images[i];
            image_I = Codec.Decode(image_I, image_P, data.get(keys[i-1]));
            decoded_images.put(keys[i-1] + ".jpeg", image_I);
            
        }
        
    
        System.out.println("Decoded images: " + decoded_images.size());
        return decoded_images;
    }
    private static void showImages(ArrayList<BufferedImage> images, MainWindow window){
        window.setVisible(true);        
        Timer timer;
        timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run(){
                updateImage(images, window);
            }
        };
        
        if(argParser.getFps() != 0){
            timer.schedule(task, 0, 1000L/argParser.getFps());
        }else{
            timer.schedule(task, 0, 1000L/10);
        }
    }
    
    private static void updateImage(ArrayList<BufferedImage> images, MainWindow window){
        currentImage = images.get(currentFrame);
        window.UpdateIcon(currentImage);
        currentFrame++;
        currentFrame = currentFrame > images.size() - 1 ? 0 : currentFrame;
    }
    
    private static Map<String, BufferedImage> applyFilters(Map<String, BufferedImage> files_images){
        if (argParser.getNegative() || argParser.getAveraging()!= 0 || argParser.getBinarization()!= 0){            
            String filename;
            BufferedImage image;
            for(Map.Entry<String, BufferedImage> entry : files_images.entrySet()) {
                filename = entry.getKey();
                image = entry.getValue();

                image = argParser.getBinarization()!= 0 ? applyFilter(image, Filters.BINARITATION, argParser.getBinarization()) : image;
                image = argParser.getNegative() ? applyFilter(image, Filters.NEGATIVATION, 0) : image;
                image = argParser.getAveraging()!= 0 ?  applyFilter(image, Filters.AVERAGING, argParser.getAveraging()) : image; 
                
                files_images.put(filename, image);
            }
        }
        return files_images;
    }
    
    private static BufferedImage applyFilter(BufferedImage image, int filter, int value){
        switch(filter){
            case Filters.DEFAULT:
                return image;
            case Filters.BINARITATION:
                return Filters.BinaritationFilter(image, value);
            case Filters.NEGATIVATION:
                return Filters.NegativitationFilter(image);
            case Filters.AVERAGING:
                return Filters.AveragingFilter(image,value);
            default:
                return image;
        }
    }  
}
