
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javax.imageio.ImageIO;


/**
 * A frame with a label to show an image.
 */
public class Unzipper
{
    private final String sourcePath;

    public Unzipper(String sourcePath)
    {
        this.sourcePath = sourcePath;
    }
       
    public Map<String, BufferedImage> getImagesZip(){
        Map<String, BufferedImage> images = new TreeMap<>();
        try{            
         ZipInputStream zis = new ZipInputStream(new FileInputStream(sourcePath));
         ZipEntry ze = zis.getNextEntry();
         BufferedImage currentImage;
         while(ze!=null){
            String fileName = ze.getName();
            currentImage = getImage(fileName);
            images.put(fileName, currentImage);
            ze = zis.getNextEntry();
         }

     zis.closeEntry();
     zis.close();

     }catch(IOException ex){
     }        
        return images;
    }
    
    private BufferedImage getImage(String fileName) throws IOException{
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
}
    
