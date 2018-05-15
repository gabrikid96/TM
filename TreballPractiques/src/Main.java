
import com.beust.jcommander.JCommander;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        
        Unzipper unzipper = new Unzipper(argParser.getInput());
        Map<String, BufferedImage> files_images = unzipper.getImagesZip();
        encode(files_images);
        /*
        files_images = applyFilters(files_images);
        if (argParser.getOutput() != null && !argParser.getOutput().isEmpty()){
            saveImages(files_images, argParser.getOutput());
        }        
        showImages(new ArrayList<>(files_images.values()),  mainWindow);*/
    }
    
    private static void encode(Map<String, BufferedImage> files_images){
        Map<String, BufferedImage> encoded_images = new TreeMap<>();
        String filename;
        BufferedImage base = (BufferedImage) files_images.values().toArray()[0];
        BufferedImage destino = (BufferedImage) files_images.values().toArray()[1];
        
        BufferedImage result = Codec.Encode(base, destino);
        
        int[][]data;
        //BufferedImage decoded = Codec.Decode(base, destino, data);
        System.out.println("Im done!");
        saveImage(result, "result.jpeg");
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
    
    private static void saveImages(Map<String, BufferedImage> files_images, String destinationPath){
        File file;
        String filename;
        BufferedImage image;
        
        File folder = new File(destinationPath);
        if(!folder.exists()){
                folder.mkdir();
        }
        for(Map.Entry<String, BufferedImage> entry : files_images.entrySet()) {
            filename = entry.getKey();
            image = entry.getValue();
            saveImage(image, destinationPath + File.separator + filename.substring(0,filename.indexOf('.')));
        }
        
    }
    private static void saveImage(BufferedImage image, String destination){
        
        File file = new File(destination);
        try {
            ImageIO.write(image, "jpeg", file);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
