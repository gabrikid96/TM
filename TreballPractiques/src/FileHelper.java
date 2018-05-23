
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.imageio.ImageIO;


/**
 * 
 */
public class FileHelper
{     
    //private static final String[] IMAGE_EXTENSIONS = {"png", "bmp", "jpeg", "jpg", "gif"};
    
    public static Map<String, BufferedImage> getImagesFromZip(String sourcePath){
        
        Map<String, BufferedImage> images = new TreeMap<>();
        try{            
         ZipInputStream zis = new ZipInputStream(new FileInputStream(sourcePath));
         ZipEntry ze = zis.getNextEntry();
         BufferedImage currentImage;
         while(ze!=null){
            String fileName = ze.getName();
            currentImage = getImage(fileName, sourcePath);
            images.put(fileName, currentImage);
            ze = zis.getNextEntry();
         }

     zis.closeEntry();
     zis.close();

     }catch(IOException ex){
     }        
        return images;
    }
    
    private static BufferedImage getImage(String fileName, String sourcePath) throws IOException{
        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ZipFile zf = new ZipFile(sourcePath);
            ZipEntry entry = zf.getEntry(fileName);
            InputStream in = zf.getInputStream(entry);
            byte[] buffer = new byte[4096];
            for(int n; (n = in.read(buffer)) != -1; )
                out.write(buffer, 0, n);
            in.close();
            zf.close();
            out.close();
            byte[] bytes = out.toByteArray();
            BufferedImage img =  ImageIO.read(new ByteArrayInputStream(bytes));
            return img;
            
        }catch(Exception exception){}
        return null;
    }
    
    public static void saveImagesToZip(Map<String, BufferedImage> files_images, String destinationPath){
        try{
            FileOutputStream outputStream = new FileOutputStream(destinationPath + ".zip");
            ZipOutputStream zos = new ZipOutputStream(outputStream);
            ZipEntry zipEntry;
            String filename;
            for (Entry<String,BufferedImage> entry : files_images.entrySet()){
                filename = entry.getKey().substring(0,entry.getKey().indexOf('.')) + ".jpeg";
                zipEntry = new ZipEntry(filename);
                zos.putNextEntry(zipEntry);
                ImageIO.write(entry.getValue(),"jpeg",zos);
                zos.closeEntry();
            }
            zos.close();
        }catch (FileNotFoundException ex) {
            Logger.getLogger(FileHelper.class.getName()).log(Level.SEVERE, null, ex);
        }catch (IOException ex) {
            Logger.getLogger(FileHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void saveImages(Map<String, BufferedImage> files_images, String destinationPath){
        String filename;
        BufferedImage image;
        
        File folder = new File(destinationPath);
        if(!folder.exists()){
                folder.mkdir();
        }
        for(Map.Entry<String, BufferedImage> entry : files_images.entrySet()) {
            filename = entry.getKey();
            image = entry.getValue();
            saveImage(image, destinationPath + File.separator + filename.substring(0,filename.indexOf('.'))  + ".jpeg");
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
    /*private static String getFileExtension(String fileName) {
        if(fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
        return fileName.substring(fileName.lastIndexOf(".")+1);
        else return "";
    }*/
    
    public static double getFileSize(String filePath){
        File file =new File(filePath);
        if(file.exists()){
            double bytes = file.length();
            double kilobytes = (bytes / 1024);
            double megabytes = (kilobytes / 1024);
            return megabytes;
        }
        return 0;
    }
}
    
