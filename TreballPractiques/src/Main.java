
import com.beust.jcommander.JCommander;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
        
        //TODO : establir valor per defecte de GOP en cas de no informar-se per paràmetre
        //TODO : establir valor per defecte de nTiles en cas de no informar-se per paràmetre
        //TODO : establir valor per defecte de seekRange en cas de no informar-se per paràmetre
        //TODO : establir valor per defecte de quality en cas de no informar-se per paràmetre
        if (argParser.getEncode()){
            double fileSize = FileHelper.getFileSize(argParser.getInput());
            Map<String, BufferedImage> files_images = FileHelper.getImagesFromZip(argParser.getInput());
            files_images = applyFilters(files_images);
            showImages(new ArrayList<>(files_images.values()),  mainWindow);
            
            //TODO : passar files_images a jpeg
            Map<String, Map<Integer,ArrayList<Integer>>> data = new TreeMap<>();
            Map<String, BufferedImage> encoded_images = Codec.encodeImages(files_images, data);
            
            //TODO : guardar el data.
            String outputName = argParser.getOutput() != null && !argParser.getOutput().isEmpty() ? 
                    argParser.getOutput() : //si tenim output
                    "Encoded_" + new SimpleDateFormat("HH-mm").format(Calendar.getInstance().getTime());//si no en tenim, fiquem un per defecte
            FileHelper.saveImagesToZip(encoded_images,outputName);
            double newSize = FileHelper.getFileSize(outputName+".zip");
            System.out.printf("Input size: %.2f KB\n", fileSize);      
            System.out.printf("Output size: %.2f KB\n", newSize);    
            System.out.printf("Compression rate: %.2f\n", (fileSize/newSize));           
            System.out.printf("Space saving: %.2f ", (100 - (fileSize * newSize)));
            System.out.println("%");
        }
        
        if (argParser.getDecode()){
            //TODO : falta veure com afagem el data per decodificar.
            Map<String, BufferedImage> encoded_images = FileHelper.getImagesFromZip(argParser.getInput());
            /*Map<String, BufferedImage> decoded_images = Codec.decode(encoded_images,data);
            FileHelper.saveImagesToZip(decoded_images,"decoded");
            showImages(new ArrayList<>(decoded_images.values()),  mainWindow);*/
        }
        
        
        /*files_images = applyFilters(files_images);
        if (argParser.getOutput() != null && !argParser.getOutput().isEmpty()){
            FileHelper.saveImages(files_images, argParser.getOutput());
            
        }        
        showImages(new ArrayList<>(files_images.values()),  mainWindow);*/
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
