package structure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.utils.Numeric;
import params.Params;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cxa123230 on 4/20/2018.
 * here and there
 */
public class ERC20Function {
    private static final Logger logger = LoggerFactory.getLogger(InputDataField.class);
    private static Map<String,String> functionNames = new HashMap<>();

    private static Map<String,Integer> occMap = new HashMap<>();

    private final String codeString;

    private BigInteger value= new BigInteger("0");

    private String addresses[];

    public ERC20Function(String codeString) {
        this.codeString = codeString;
    }

    public static Map<String, ERC20Function> readERC20Functions() throws Exception {

        Map<String, ERC20Function> funcMap = new HashMap<String, ERC20Function>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(Params.erc20FunctionsFile));
            String line = br.readLine();//read header
            int i = 0;
            while ((line = br.readLine()) != null) {
                String arr[] = line.split("\t");
                ERC20Function ethFunction = new ERC20Function(arr[0]);
                setName(arr[0], arr[1]);
                funcMap.put(ethFunction.getCodeString(), ethFunction);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return funcMap;
    }

    public static Map<String, Integer> getOccMap() {
        return occMap;
    }

    static void setParams(String params, ERC20Function f) {
        int length = params.length();
        int eth64 = Params.eth64;
        String codeString = f.getCodeString();
        if(!occMap.containsKey(codeString)){
            occMap.put(codeString,0);
        }
        occMap.put(codeString,1+ occMap.get(codeString));
        switch (getFunctionName(f.getCodeString())){
            case "transfer": {
                if (length / 64 == 2) {
                    f.setAddresses(params.substring(0, eth64));
                    BigInteger value = Numeric.toBigInt(params.substring(eth64));
                    f.setValue(value);
                }
                else logger.info("transfer function has "+length/64 +" params");
                break;
            }
            case "transferFrom":{
                if (length / 64 == 3) {
                    f.setAddresses(params.substring(0, eth64),params.substring(eth64,2* eth64));
                    BigInteger value = Numeric.toBigInt(params.substring(2* eth64));
                    f.setValue(value);
                }
                else logger.info("transferFrom function has "+length/64 +" params");
                break;
            }
            case "totalSupply":{
                break;
            }
            case "balanceOf":{
                if (length / 64 == 1)
                f.setAddresses(params);
                break;
            }
            case "approve":{
                if (length / 64 == 2) {
                    f.setAddresses(params.substring(0, eth64));
                    BigInteger value = Numeric.toBigInt(params.substring(eth64));
                    f.setValue(value);
                }
                else logger.info("approve function has "+length/64 +" params");
                break;
            }
            case "allowance":{
                if (length / 64 == 1) {
                    f.setAddresses(params.substring(0, eth64),params.substring(eth64,2* eth64));
                }
                break;
            }
            case "Transfer":{
                if (length / 64 == 3) {
                    f.setAddresses(params.substring(0, eth64),params.substring(eth64,2* eth64));
                    BigInteger value = Numeric.toBigInt(params.substring(2* eth64));
                    f.setValue(value);
                }
                else logger.info("Transfer function has "+length/64 +" params");
                break;
            }
            case "Approval":{
                if (length / 64 == 3) {
                    f.setAddresses(params.substring(0, eth64),params.substring(eth64,2* eth64));
                    BigInteger value = Numeric.toBigInt(params.substring(2* eth64));
                    f.setValue(value);
                }
                break;
            }
            case "name":{
                break;
            }
            case "decimals":{
                break;
            }
            case "symbol":{
                break;
            }
            case "version":{
                break;
            }
            case "approveAndCall":{

                f.setAddresses(params.substring(0, eth64));
                BigInteger value = Numeric.toBigInt(params.substring(eth64,2* eth64));
                f.setValue(value);
                //discard the bytes data

                break;
            }
            case "receiveApproval":{
                f.setAddresses(params.substring(0, eth64),params.substring(2* eth64,3* eth64));
                BigInteger value = Numeric.toBigInt(params.substring(eth64,2* eth64));
                f.setValue(value);
                //discard the bytes data

                break;
            }





        }

    }

    public static String getFunctionName(String codeString) {
        return functionNames.get(codeString);
    }

    public static void setName(String codeString, String name) {
        functionNames.put(codeString, name);
    }

    public BigInteger getValue() {
        return value;
    }

    private void setValue(BigInteger v) {
        this.value = v;
    }

    public String getCodeString() {
        return codeString;
    }

    boolean hasAddress(){
        return (addresses!=null&&addresses.length>0);
    }

    public String[] getAddresses() {
        return addresses;
    }

    private void setAddresses(String... p) {
        addresses = new String[p.length];
        int i = 0;
        for (String s : p) {
            if (s.length() != Params.eth64)
                logger.error("Parameter " + s + " is not of length " + Params.eth64);
            else {
                //addresses are 40 character long, excluding the 0x at the beginning.
                addresses[i++] = Params.ethAddressPrecedingString + s.substring(Params.ethAddressPrecedingZeros);
            }
        }
    }
}
