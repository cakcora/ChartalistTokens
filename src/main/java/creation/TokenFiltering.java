package creation;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.utils.Numeric;
import params.Params;
import structure.Contract;
import structure.ERC20Function;
import structure.InputDataField;
import structure.Transaction;

import java.io.*;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by cxa123230 on 4/16/2018.
 *
 */


public class TokenFiltering {
    private static final Logger logger = LoggerFactory.getLogger(TokenFiltering.class);
    private static int fileCount = 56;
    public static void main(String args[]) throws Exception {


        FileUtils.cleanDirectory(new File(Params.tokenFilesDir));
        Map<String, Contract> tokenMap = Contract.readTopTokens();

        Map<String, ERC20Function> functionMap = ERC20Function.readERC20Functions();

        Set<String> addressOfInterestList = new HashSet<>();
        logger.info(tokenMap.size() + " tokens."+tokenMap.keySet());
        for (Contract token : tokenMap.values()) {
            addressOfInterestList.add(token.getContractAddress());
        }
        logger.info("Searching the Ethereum user to token transactions only");
        readFiles(tokenMap, functionMap, addressOfInterestList, true);
        saveRemainingTransactions(tokenMap);

        printFunctionParamOcc();
        printFunctionOcc();
        logger.warn("These param and function counts that this class may output are not the whole ERC20 ecosystem. There are some tokens that had few transactions, so their data is not here. Check ERC20TOkenDiscovery class for complete numbers.");

    }


    private static void saveRemainingTransactions(Map<String, Contract> tokenMap) {
        for(Contract token: tokenMap.values()){
            String shortName = token.getShortName();
            long count = token.getTxCount();
            if (count > 0) {
                printDailyOcc(token, shortName, count);
                List<Transaction> transactions = token.getTransactions();
                writeToFile(shortName, transactions);
            }
        }
    }

    private static void printDailyOcc(Contract token, String name, long count) {
        logger.info(name+"[TOKEN] : "+count+" transactions");
        //get daily distributions
        Map<Integer, Map<Integer, Integer>> map = token.getTransactionsByDate();
        for(int year:map.keySet()){
            for(int day:map.get(year).keySet()){
                logger.info("\t"+year+"\t"+day+"\t"+map.get(year).get(day)+" transactions");
            }
        }
    }

    static void printFunctionParamOcc() {
        logger.info("paramaters");
        Map<Integer, Long> paramLengths = InputDataField.getlengths();
        for(Integer i: paramLengths.keySet()){
            logger.info(i+"->"+paramLengths.get(i)+" times");
        }
    }

    static void readFiles(Map<String, Contract> tokenMap, Map<String, ERC20Function> functionMap, Set<String> addressOfInterestList, boolean tokenTransactionsOnly) throws IOException {
        String line;

        for (int i = fileCount; i > 0; i--) {
            BufferedReader br = new BufferedReader(new FileReader(Params.dir + i + ".csv"));
            br.readLine();
            logger.info("parsing " +i+ ".csv");
            while ((line = br.readLine()) != null) {
                String arr[] = line.split(",");
                try {
                    String from = arr[0].trim();
                    String data = arr[3].trim();
                    String to = arr[4].trim();
                    BigInteger gas_used = Numeric.toBigInt(arr[6].trim());
                    BigInteger val = Numeric.toBigInt(arr[5]);
                    long unixTime = Numeric.toBigInt(arr[9]).longValue();
                    String address = isOfInterest(addressOfInterestList, from, to, tokenTransactionsOnly);
                    saveTransaction(tokenMap, functionMap, from, data, to, gas_used, val, unixTime, address);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    }

    private static void saveTransaction(Map<String, Contract> tokenMap, Map<String, ERC20Function> functionMap, String from, String data, String to, BigInteger gas_used, BigInteger val, long unixTime, String address) {
        if (!address.isEmpty()) {
            if (to.isEmpty()) logger.info("line");
            ERC20Function df = InputDataField.parseDataField(data, functionMap);
            Transaction tx = new Transaction(from, to, val, gas_used, df, unixTime);

            Contract token = tokenMap.get(address);
            token.addTransaction(tx, unixTime);
            List<Transaction> transactions = token.getTransactions();
            if (transactions.size() > 5000) {
                writeToFile(token.getShortName(), transactions);
                token.clearTransactions();
            }
        }
    }


    private static void writeToFile(String shortName, List<Transaction> transactions ) {
        try {

            String fileName = Params.tokenFilesDir + shortName + "TX.txt";
            BufferedWriter wr = new BufferedWriter(new FileWriter(fileName,true));
            logger.info("Writing "+shortName+" transactions to "+fileName);
            for(Transaction tx:transactions){
                wr.write(tx+"\n");
            }
            wr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String isOfInterest(Set<String> addresses, String from, String to, boolean tokenTransactionsOnly) {
        String address = "";

        if (tokenTransactionsOnly) {
            // is any end of the transaction a token address?
            if (addresses.contains(from)) {
                return from;
            } else if (addresses.contains(to)) {
                return to;
            }
        } else {
            //searching for user to user transactions
            if (addresses.contains(from) & addresses.contains(to)) {
                return Params.userToUser;
            }
        }
        return address;
    }

    public static void printFunctionOcc() {
        Map<String, Integer> occMap = ERC20Function.getOccMap();
        for(String funcCodeString: occMap.keySet()) {

            long count = occMap.get(funcCodeString);
            logger.info(ERC20Function.getFunctionName(funcCodeString) + ": " + count + " transactions");
        }
    }
}
