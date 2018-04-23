package structure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import params.Params;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by cxa123230 on 4/20/2018.
 */
public class InputDataField {
    private static final Logger logger = LoggerFactory.getLogger(InputDataField.class);
    private static Map<Integer,Long> lengths = new HashMap<>();


    public static ERC20Function parseDataField(String data, Map<String, ERC20Function> functionMap) {

        int length = data.length();

        int erc20FunctionLength = Params.ercStandardFunctionLength;
        if(length> erc20FunctionLength){
            //may have function
            String func = data.substring(0, erc20FunctionLength);
            if(functionMap.containsKey(func)){
                if((length- erc20FunctionLength)%64!=0) {
                    //cannot parse parameters
                    logger.info("Cannot parse data with length "+length);
                }
                else{
                    int paramLength = (length- erc20FunctionLength)/64;

                    if(!lengths.containsKey(paramLength)){
                        lengths.put(paramLength,0L);
                    }
                    lengths.put(paramLength,1+lengths.get(paramLength));
                    ERC20Function ercFunctionInstance  = new ERC20Function(func);
                    ERC20Function.setParams(data.substring(erc20FunctionLength),ercFunctionInstance);
                    return ercFunctionInstance;
                }

            }
        }
        return null;
    }
    public static Map<Integer, Long> getlengths(){
        return lengths;
    }
}
