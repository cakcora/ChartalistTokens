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

    public static void main(String args[]) throws Exception {


        FileUtils.cleanDirectory(new File(Params.tokenFilesDir));
        Map<String, Contract> tokenMap = Contract.readTopTokens(200);

        Map<String, ERC20Function> functionMap = ERC20Function.readERC20Functions();

        Set<String> addressOfInterestList = new HashSet<>();
        logger.info(tokenMap.size() + " tokens."+tokenMap.keySet());
        for (Contract token : tokenMap.values()) {
            addressOfInterestList.add(token.getContractAddress());
        }
        Set<String> userAddressesinTransactions = new HashSet<String>();
        boolean[] wave = new boolean[]{true,false};
        for(boolean tokenTransactionsOnly : wave){
            if(tokenTransactionsOnly){
                logger.info("Searching the Ethereum user to token transactions only");
            }
            else{
                if(userAddressesinTransactions.isEmpty()){
                    throw new Exception("No user address was found.");
                }
                addressOfInterestList = userAddressesinTransactions;
                logger.info("Searching the Ethereum user to user transactions");
            }
            readFiles(tokenMap, functionMap, addressOfInterestList, tokenTransactionsOnly);

            userAddressesinTransactions = parseTransactions(tokenMap, tokenTransactionsOnly);
            printFunctionParamOcc();

            printFunctionOcc();

            // delete token transactions from memory
            tokenMap.clear();
            //in the next step we are interested in user to user transactions.
            tokenMap.put(Params.userToUser, new Contract(Params.userToUser));
        }
    }



    private static Set<String> parseTransactions(Map<String, Contract> tokenMap, boolean tokenTransactionsOnly) {
        Set<String> userAddressesinTransactions = new HashSet<>();
        for(Contract token: tokenMap.values()){
            String name = token.getShortName();
            long count = token.getTxCount();
            if(count>=1){
                printDailyOcc(token, name, count);

                //get userAddressesofthisToken and write them to a file.
                Set<String> userAddressesofthisToken = new HashSet<>();
                List<Transaction> transactions = token.getTransactions();
                writeToFile(token.getShortName(), transactions);

                if(tokenTransactionsOnly){
                    for(Transaction tx: transactions){
                        userAddressesofthisToken.addAll(tx.getAllAddresses());
                    }
                    userAddressesinTransactions.addAll(userAddressesofthisToken);
                    //write all token transactions
                }
            }
        }
        return userAddressesinTransactions;
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
        int count =0;
        for (int i = 1; i <= 50; i++) {
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
                    count = getCount(tokenMap, functionMap, count, from, data, to, gas_used, val, unixTime, address);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static int getCount(Map<String, Contract> tokenMap, Map<String, ERC20Function> functionMap, int count, String from, String data, String to, BigInteger gas_used, BigInteger val, long unixTime, String address) {
        if (!address.isEmpty()) {
            if (to.isEmpty()) logger.info("line");
            ERC20Function df = InputDataField.parseDataField(data, functionMap);
            Transaction tx = new Transaction(from, to, val, gas_used, df, unixTime);

            tokenMap.get(address).addTransaction(tx, unixTime);
            if (address.equalsIgnoreCase(Params.userToUser)) {
                if (count++ > 500000) {
                    writeToFile(address, tokenMap.get(address).getTransactions());
                    tokenMap.get(address).clearTransactions();
                    count = 0;
                }
            }
        }
        return count;
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
