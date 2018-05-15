package parameterValidator;



import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

public class BinaritationValidator implements IParameterValidator{

    @Override
    public void validate(String command, String value) throws ParameterException {
        
        try{
            int val = Integer.parseInt(value);
            if (val < 0 || val > 255){
                throw new ParameterException(command + " value must be between 0 and 255");
            }
        }catch(NumberFormatException ex){
            throw new ParameterException(command + " value invalid format");
        }
    }
    
}