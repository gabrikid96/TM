
import com.beust.jcommander.JCommander;
import java.awt.event.WindowEvent;
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
        
        MainWindow windowEncode = new MainWindow("Original");
        MainWindow windowDecode = new MainWindow("Decoded");
        String outputName = argParser.getOutput() != null && !argParser.getOutput().isEmpty() ? 
                    argParser.getOutput() : //si tenim output
                    "Encoded_" + new SimpleDateFormat("HH-mm").format(Calendar.getInstance().getTime());//si no en tenim, fiquem un per defecte
        initializeParameterValues();
        if (argParser.getEncode()){
            
            Map<String, BufferedImage> files_images = FileHelper.getImagesFromZip(argParser.getInput());
            FileHelper.saveImagesToZip(files_images,"originals");
            double fileSize = FileHelper.getFileSize("originals.zip");
            files_images = applyFilters(files_images);
            //showImages(new ArrayList<>(files_images.values()),  mainWindow);
            
            Map<String, Map<Integer,ArrayList<Integer>>> data = new TreeMap<>();
            Map<String, BufferedImage> encoded_images = Codec.encodeImages(files_images, data);
            
            //TODO : guardar el data.
            
            FileHelper.saveImagesToZip(encoded_images,outputName,data, argParser.getNTiles());
            double newSize = FileHelper.getFileSize(outputName+".zip");
            System.out.printf("Input size: %.5f KB\n", fileSize);      
            System.out.printf("Output size: %.5f KB\n", newSize);    
            System.out.printf("Compression rate: %.2f\n", (fileSize/newSize));           
            System.out.printf("Space saving: %.2f ",100 - newSize*100/fileSize);
            System.out.println("%");
            //Map<String, BufferedImage> decoded_images = Codec.decodeImages(encoded_images,data);
            showImages(new ArrayList<>(files_images.values()),  windowEncode);
            //showImages(new ArrayList<>(decoded_images.values()),  windowDecode);
        }
        
        if (argParser.getDecode()){
            String referenceOutput = "./" + outputName + ".zip";
            //TODO : falta veure com afagem el data per decodificar.
            Map<String, BufferedImage> encoded_images;
            Map<String, Map<Integer,ArrayList<Integer>>> data;
            if (argParser.getEncode()){
                encoded_images = FileHelper.getImagesFromZip(referenceOutput);
                data = FileHelper.getEncodeDataFromZip(referenceOutput);
            }else{
                encoded_images = FileHelper.getImagesFromZip(argParser.getInput());
                data = FileHelper.getEncodeDataFromZip(argParser.getInput());
            }
            
            Map<String, BufferedImage> decoded_images = Codec.decodeImages(encoded_images,data);
            FileHelper.saveImagesToZip(decoded_images,"decoded");
            showImages(new ArrayList<>(decoded_images.values()),  windowDecode);
        }
    }
    
    
    private static void showImages(ArrayList<BufferedImage> images, MainWindow window){
        if (!argParser.getBatch()){
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
        
        }else{
            window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
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

    private static void initializeParameterValues() {
        if(argParser.getGop() == 0){
            argParser.setGOP(3);
            System.out.println("No s'ha trobat el paràmetre de GOP. S'utilitzarà el valor 3 per defecte");
        }
        if(argParser.getNTiles() == 0){
            argParser.setNTiles(6);
            System.out.println("No s'ha trobat el paràmetre de nTiles. S'utilitzarà el valor 6 per defecte");
        }
        if(argParser.getSeekRange() == 0){
            argParser.setSeekRange(3);
            System.out.println("No s'ha trobat el paràmetre del rang de búsqueda. S'utilitzarà el valor 3 per defecte");
        }
        if(argParser.getQuality() == 0){
            argParser.setQuality(9);
            System.out.println("No s'ha trobat el paràmetre de qualitat. S'utilitzarà el valor 9 per defecte");
        }
    }
}
