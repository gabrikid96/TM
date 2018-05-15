/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parameterValidator;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;
import java.io.File;

/**
 *
 * @author mat.aules
 */
public class FileValidator implements IParameterValidator{

    @Override
    public void validate(String command, String value) throws ParameterException {
        File f = new File(value);
        if(!f.exists()) { 
           throw new ParameterException(command + " file " + value + " does not exist");
        }
    }
    
}


