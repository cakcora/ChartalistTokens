package params;

/**
 * Created by cxa123230 on 4/16/2018.
 *
 *
 */
public class Params {
    public static final int DAY = 1;
    public static final int WEEK = 7;
    public static String d = "D:/Ethereum/";
    public static String dDir = "D:/Dropbox/Publications/PostDoc work/3 - Chartalist/";
    public static String vDir = d + "fromVadim/";
    public static boolean countPerDay = true;
    public static boolean storeContractTransactions = true;
    public static String tokenInfoFile = dDir + "data/top200Tokens.csv";
    public static String erc20FunctionsFile = dDir + "data/erc20FunctionsFile.txt";
    public static int ercStandardFunctionLength = 10;
    public static int eth64 = 64;
    public static String tokenFilesDir = d + "tokens/";
    public static int ethAddressPrecedingZeros = 24;
    public static String ethAddressPrecedingString = "0x";


    public static String userToUserFile = "userToUserFile.txt";
    public static String graphFilesDir = d + "graphs/";
    public static String tmotifFilesDir = d + "graphSamples/";
    public static String ethrDir = d + "fromR/";

    public static String coreFile = d + "experiments/" + "core.txt";
    public static String tokenbehaviorFile = d + "experiments/" + "investorBehavior.txt";

    public static String simpleFlowFile = d + "experiments/" + "FlowMotifs.txt";

    public static String coralFlowFile = d + "experiments/" + "alphaCoralFlows.txt";
    public static String nodeIdsFile = "NodeIds.txt";
    public static String alphaCoreDir = d + "alphacore/";

    public static String kCoreDir = d + "kcore/";

}
