/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parameterValidator;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

public class MaxIntValidator implements IParameterValidator{

    @Override
    public void validate(String command, String value) throws ParameterException {
        
        try{
            int val = Integer.parseInt(value);
            if (val < 0){
                throw new ParameterException(command + " must be greater than 0");
            }
        }catch(NumberFormatException ex){
            throw new ParameterException(command + " value invalid format");
        }
    }
    
}