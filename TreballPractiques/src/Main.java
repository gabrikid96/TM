
import com.beust.jcommander.JCommander;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;

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
            Map<String, Map<Integer,ArrayList<Integer>>> data = new HashMap<>();
            Map<String, BufferedImage> encoded_images = encode(files_images, data);
            Map<String, BufferedImage> decoded_images = decode(encoded_images,data);
            
            //Proba per mostrar les decode
            showImages(new ArrayList<>(decoded_images.values()),  mainWindow);
            FileHelper.saveImagesToZip(decoded_images,"decoded");
            //TODO : Fer que guardi aquestes imatges en un zip
            /*ZipHelper.*/
            //saveImages(encoded_images, "encoded");
            //FileHelper.saveImagesToZip(encoded_images,"encoded");
            
        }
        
        
        /*files_images = applyFilters(files_images);
        if (argParser.getOutput() != null && !argParser.getOutput().isEmpty()){
            FileHelper.saveImages(files_images, argParser.getOutput());
            
        }        
        showImages(new ArrayList<>(files_images.values()),  mainWindow);*/
    }
    
    private static Map<String, BufferedImage> encode(Map<String, BufferedImage> files_images, Map<String, Map<Integer,ArrayList<Integer>>> data){
        BufferedImage image_I, image_P;
        String filename_I, filename_P;
        Map<String, BufferedImage> encoded_images = new TreeMap<>();
        
        BufferedImage [] images  = new BufferedImage[files_images.size()];
        images = (BufferedImage[]) files_images.values().toArray(images);
        
        String[] filenames  = new String[files_images.size()];
        filenames = (String[]) files_images.keySet().toArray(filenames);
        
        Map<Integer,ArrayList<Integer>> data_P;
        for (int P = 0, I = 0; P < (int)Math.ceil((double)files_images.size()/argParser.getGop()); P++, I+=argParser.getGop()){
            image_I = images[I];
            filename_I = filenames[I];
            
            image_P = I + argParser.getGop() > images.length ? images[P] : images[I + argParser.getGop() -1];
            filename_P = I + argParser.getGop() > filenames.length ? filenames[P] : filenames[I + argParser.getGop() -1];
            filename_I = filename_I.substring(0,filename_I.indexOf('.'));
            System.out.println("I: " + filename_I + "\nP: " + filename_P);
            encoded_images.put(filename_I + "_I.jpeg", image_I);
            encoded_images.put(filename_I + "_P.jpeg", image_P);
            data_P = new HashMap<>();
            Codec.Encode(new BufferedImage(image_I.getColorModel(), (WritableRaster)image_I.getData(),
                                image_I.getColorModel().isAlphaPremultiplied(), null), image_P, data_P);
            data.put(filename_I,data_P);
        }        
        System.out.println("Encoded images: " + encoded_images.size());
        return encoded_images;
    }
    
    private static Map<String, BufferedImage> decode(Map<String, BufferedImage> files_images, Map<String, Map<Integer,ArrayList<Integer>>> data){
        BufferedImage image_I, image_P;
        String filename_I, filename_P;
        Map<String, BufferedImage> decoded_images = new TreeMap<>();
        BufferedImage [] images  = new BufferedImage[files_images.size()];
        images = (BufferedImage[]) files_images.values().toArray(images);
        
        String[] filenames  = new String[files_images.size()];
        filenames = (String[]) files_images.keySet().toArray(filenames);
        String key = "";
        for (int I = 0, P = 1; I < files_images.size(); I+=2, P+=2){
            image_I = images[I];
            filename_I = filenames[I];
            
            image_P = images[P];
            filename_P = filenames[P];
            key = filename_I.substring(0, filename_I.indexOf('_'));
            decoded_images.put(filename_I, image_I);
            decoded_images.put(key + ".jpeg", Codec.Decode(image_I, image_P, data.get(key)));
        }
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
        currentFrame = currentFrame > images.size() - 1 ? 0 : currentFrame++;
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
