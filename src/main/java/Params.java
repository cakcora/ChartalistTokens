import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;


/**
 * Created by cxa123230 on 4/16/2018.
 */
public class Params {

    static String dir ="D:/Ethereum/fromVadim/";
    static boolean countPerDay = true;
    static boolean storeContractTransactions =true;
    private static String tokenFile = "D:/Dropbox/Publications/PostDoc work/Chartalist/data/top200Tokens.csv";
    private static String erc20FunctionsFile = "D:/Dropbox/Publications/PostDoc work/Chartalist/data/erc20FunctionsFile.txt";
    public static int ercStandardFunctionLength = 10;
    public static int eth64 = 64;
    public static String tokenFilesDir ="D:/Ethereum/tokens/";
    public static int ethAddressPrecedingZeros = 24;
    public static String ethAddressPrecedingString = "0x";

    static Map<String, Contract> creatTokenInfo()
    {
        Map<String, Contract> myMap = new HashMap<String, Contract>();
        myMap.put("0x004A1e27a5edb05E13d1eF8Db952092c239518Ff", new Contract("BLZ","0x5732046A883704404F284Ce41FfADd5b007FD668","0x004A1e27a5edb05E13d1eF8Db952092c239518Ff"));
        myMap.put("0x960b236A07cf122663c4303350609A66A7B288C0",new Contract("AGN","0x7f478213dD4A4df6016922aA47b860f0Bdf50075","0x960b236A07cf122663c4303350609A66A7B288C0"));
        return myMap;
    }


    static Map<String, Contract> readTopTokens() throws Exception {
        return readTopTokens(Integer.MAX_VALUE);
    }

    static Map<String, Contract> readTopTokens(int top) throws Exception {
        if(top<=0) throw new Exception(top+" tokens requested. ");
        Map<String, Contract> myMap = new HashMap<String, Contract>();
        try {
             BufferedReader br = new BufferedReader(new FileReader(tokenFile));
            String line = br.readLine();//read header
            int i=0;
            while((line=br.readLine())!=null){
               String arr[] = line.split(",");
                Contract contract = new Contract(arr[1], arr[2], arr[3]);
                myMap.put(contract.getContractAddress(), contract);
                if(++i>=top) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myMap;
    }

    static Map<String, ERC20Function> readERC20Functions() throws Exception {

        Map<String, ERC20Function> funcMap = new HashMap<String, ERC20Function>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(erc20FunctionsFile));
            String line = br.readLine();//read header
            int i=0;
            while((line=br.readLine())!=null){
                String arr[] = line.split("\t");
                ERC20Function ethFunction = new ERC20Function(arr[0]);
                ERC20Function.setName(arr[0],arr[1]);
                funcMap.put(ethFunction.getCodeString(), ethFunction);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return funcMap;
    }

}
