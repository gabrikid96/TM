
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    public static Map<String, BufferedImage> getImagesFromZip(String sourcePath){
        
        Map<String, BufferedImage> images = new TreeMap<>();
        try{            
         ZipInputStream zis = new ZipInputStream(new FileInputStream(sourcePath));
         ZipEntry ze = zis.getNextEntry();
         BufferedImage currentImage;
         while(ze!=null){
            String fileName = ze.getName();
            currentImage = getImage(fileName, sourcePath);
            if (currentImage != null){
                images.put(fileName.substring(0, fileName.indexOf(".")) + ".jpeg", currentImage);
            }
            ze = zis.getNextEntry();
         }

     zis.closeEntry();
     zis.close();

     }catch(IOException ex){
     }        
        return images;
    }
        
    private static BufferedImage getImage(String fileName, String sourcePath) {
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
            String img_extension = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(bytes));
            
            if(!img_extension.substring(img_extension.indexOf("/")+1, img_extension.length()).equals("jpeg")){
                img = imageToJpeg(img);
            }
            
            return img;
            
        }catch(Exception exception ){}
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
    
    public static void saveImagesToZip(Map<String, BufferedImage> files_images, String destinationPath, Map<String, Map<Integer,ArrayList<Integer>>> data, int nTiles){
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
            ZipEntry e = new ZipEntry("data.dat");
            zos.putNextEntry(e);
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            
            out.writeObject(data);
            //out.writeInt(nTiles);
            
            zos.write(byteOut.toByteArray());
            //zos.write(nTiles);
            
            //writeData(zos,data);
            zos.closeEntry();
            zos.close();
        }catch (FileNotFoundException ex) {
            Logger.getLogger(FileHelper.class.getName()).log(Level.SEVERE, null, ex);
        }catch (IOException ex) {
            Logger.getLogger(FileHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static Map<String, Map<Integer, ArrayList<Integer>>> getEncodeDataFromZip(String sourcePath) {
        try{            
        ZipInputStream zis = new ZipInputStream(new FileInputStream(sourcePath));
        ZipEntry ze = zis.getNextEntry();
        while(ze!=null){
            String fileName = ze.getName();
            if (fileName.equals("data.dat")){
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
                zis.closeEntry();
                zis.close();
                
                ByteArrayInputStream byteIn = new ByteArrayInputStream(out.toByteArray());
                ObjectInputStream ois = new ObjectInputStream(byteIn);
                return (Map<String, Map<Integer, ArrayList<Integer>>>) ois.readObject();
                
            }
            ze = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
        }catch(IOException ex){
        } catch (ClassNotFoundException ex) {        
            Logger.getLogger(FileHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }


    
    private static Map<String, Map<Integer,ArrayList<Integer>>> string2map(String data){
        int guio = data.indexOf('-');
        String nTiles = data.substring(0,guio);
        ArgParser.getInstance().setNTiles(Integer.parseInt(nTiles));
        data = data.substring(guio+2,data.length()-1);
        Pattern p = Pattern.compile("\\{(.*?)\\}");
        Matcher m = p.matcher(data);
        ArrayList<String> values = new ArrayList<>();
        while(m.find()) {
            values.add(m.group(1));
        }
        p = Pattern.compile("(?:[ a-zA-Z ])?([a-zA-Z]+[0-9]{1,}+)");
        m = p.matcher(data);
        ArrayList<String> keys = new ArrayList<>();
        while(m.find()) {
            keys.add(m.group(1));
        }       
        char ch = keys.get(1).charAt(0);
        keys.set(0, ch + keys.get(0));
        Map<String, Map<Integer,ArrayList<Integer>>> dataH = new TreeMap<>();
        
        for (int i = 0; i < keys.size(); i++){
            String [] coincidencia = values.get(i).split("],");
            Map<Integer,ArrayList<Integer>> c = new HashMap();
            for (int j = 0; j < coincidencia.length; j++){
                if(coincidencia[j].length() != 0){
                    int key = Integer.parseInt(coincidencia[j].substring(0,coincidencia[j].indexOf('=')).trim());
                    String[] x0y0 = coincidencia[j].substring(coincidencia[j].indexOf('=')+1,coincidencia[j].length()).split(",");
                    String x = x0y0[0].replace("[","").replace("]", "").trim();
                    String y = x0y0[1].replace("[","").replace("]", "").trim();
                    ArrayList<Integer> xy = new ArrayList<>();
                    xy.add(Integer.parseInt(x));
                    xy.add(Integer.parseInt(y));
                    c.put(key, xy);
                }
            }
            dataH.put(keys.get(i), c);
        }        
        return dataH;
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

    private static BufferedImage imageToJpeg(BufferedImage img){
        BufferedImage newBufferedImage = new BufferedImage(img.getWidth(),img.getHeight(), BufferedImage.TYPE_INT_RGB);
	newBufferedImage.createGraphics().drawImage(img, 0, 0, Color.WHITE, null);
        
        return newBufferedImage;
    }
    
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
    
    /*public static byte[] hash2bytes(Map<String, Map<Integer,ArrayList<Integer>>> data){

    // Convert Map to byte array
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    ObjectOutputStream out = new ObjectOutputStream(byteOut);
    out.writeObject(data);

    // Parse byte array to Map
    ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
    ObjectInputStream in = new ObjectInputStream(byteIn);
    Map<Integer, String> data2 = (Map<Integer, String>) in.readObject();
    System.out.println(data2.toString());
    }*/

    private static void writeData(ZipOutputStream zos, Map<String,Map<Integer, ArrayList<Integer>>> data) throws IOException {
        for (Entry<String, Map<Integer, ArrayList<Integer>>> entry : data.entrySet()){
                zos.write(entry.getValue().size()); //num coincidences
                
                for (Entry<Integer, ArrayList<Integer>> e : entry.getValue().entrySet()){
                    zos.write(e.getKey());//id tile
                    zos.write(e.getValue().get(0));//x0
                    zos.write(e.getValue().get(1));//y0
                }
        }    
    }
    
    private static void getData(){
        
    }
    
}
    
