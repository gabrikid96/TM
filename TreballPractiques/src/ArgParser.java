import com.beust.jcommander.Parameter;
import parameterValidator.BinaritationValidator;
import parameterValidator.FileValidator;
import parameterValidator.MaxIntValidator;

public class ArgParser {
    private static ArgParser instance = null;
    
    private ArgParser(){
    }
    
    public static ArgParser getInstance(){
        if(instance == null){
            instance = new ArgParser();
        }
        return instance;
    }

    @Parameter(names = { "--input","-i"}, required = true, description = "Zip file with the input", validateWith=FileValidator.class)
    private String input;
    public String getInput(){
         return input;
    }
    
    @Parameter(names = { "--output","-o"}, required = false, description = "Output to the zip file")
    private String output;
    public String getOutput(){
         return output;
    }
    
    @Parameter(names ={"--encode","-e"}, required = false, description = "Encode")
    private boolean encode;
    public boolean getEncode(){
        return encode;
    }
     
    @Parameter(names ={"--decode","-d"}, required = false, description = "Decode")
    private boolean decode;
    public boolean getDecode(){
        return decode;
    }
    
    @Parameter(names={"--fps", "-fps"},required = false, description = "Frames per second to play images", validateWith=MaxIntValidator.class)
    private int fps;
    public int getFps() {
        return fps;
    }
    
    @Parameter(names={"--binarization"}, required = false, description = "Threshold of binaritation filter", validateWith=BinaritationValidator.class)
    private int binarization;
    public int getBinarization() {
        return binarization;
    }

    @Parameter(names={"--negative"}, required = false, description = "Threshold of binaritation filter")
    private boolean negative;
    public boolean getNegative() {
        return negative;
    }
    
    @Parameter(names={"--averaging"}, required = false, description = "Value of averaging filter", validateWith=MaxIntValidator.class)
    private int averaging;
    public int getAveraging() {
        return averaging;
    }
    
            
    @Parameter(names={"--nTiles"}, required = false, description = "Value of tiles", validateWith=MaxIntValidator.class)
    private int nTiles;
    public int getNTiles() {
        return nTiles;
    }

    public void setNTiles(int nTiles) {
        this.nTiles = nTiles;
    }
    
    
    
    @Parameter(names={"--seekRange"}, required = false, description = "Value of seekRange", validateWith=MaxIntValidator.class)
    private int seekRange;
    public int getSeekRange() {
        return seekRange;
    }
    
    public void setSeekRange(int sr) {
        this.seekRange = sr;
    }
    
    @Parameter(names={"--quality"}, required = false, description = "Value of quality", validateWith=MaxIntValidator.class)
    private int quality;
    public int getQuality() {
        return quality;
    }
    
    public void setQuality(int quality) {
        this.quality = quality;
    }
    
    @Parameter(names = "--GOP", required = false, description = "GOP")
    private int gop;
    public int getGop(){
         return gop;
    }
    
    public void setGOP(int gop) {
        this.gop = gop;
    }
    
    @Parameter(names ={"--batch","-b"}, required = false, description = "")
    private boolean batch;
    public boolean getBatch(){
        return batch;
    }
    
    
}

