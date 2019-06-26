package creation;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.utils.Numeric;
import params.Params;
import structure.Contract;
import structure.ERC20Function;
import structure.InputDataField;
import structure.Transaction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by cxa123230 on 4/25/2018.
 */
public class ERC20TOkenDiscovery {
    private static final Logger logger = LoggerFactory.getLogger(ERC20TOkenDiscovery.class);

    public static void main(String args[]) throws Exception {

        Map<Integer, Map<Integer, Integer>> txMapInTime = new HashMap<Integer, Map<Integer, Integer>>();
        Map<Integer, Map<Integer, Integer>> eRCFirstAppearanceMap = new HashMap<Integer, Map<Integer, Integer>>();
        Set<String> erc20Tokens = new HashSet<String>();

        Map<String, Contract> tokenMap = Contract.readTopTokens();

        Map<String, ERC20Function> functionMap = ERC20Function.readERC20Functions();
        Map<String, Integer> counts = new HashMap<String, Integer>();
        for (int i = 51; i <= 51; i++) {//the 51st file has all of blocks after 5M
            BufferedReader br = new BufferedReader(new FileReader(Params.vDir + i + ".csv"));
            String line = br.readLine();
            logger.info("parsing " + i + ".csv");
            while ((line = br.readLine()) != null) {
                String arr[] = line.split(",");
                try {
                    String from = arr[0].trim();
                    String data = arr[3].trim();
                    String to = arr[4].trim();
                    BigInteger gas_used = Numeric.toBigInt(arr[6].trim());
                    BigInteger val = Numeric.toBigInt(arr[5]);
                    long unixTime = Numeric.toBigInt(arr[9]).longValue();
                    ERC20Function df = InputDataField.parseDataField(data, functionMap);
                    Transaction tx = new Transaction(from, to, val, gas_used, df, unixTime);
                    if (tx.hasAFunction()) {
                        String tokenAdd = arr[4];
                        if (!tokenMap.containsKey(tokenAdd)) {
                            if (!counts.containsKey(tokenAdd)) {
                                counts.put(tokenAdd, 0);
                            }
                            counts.put(tokenAdd, 1 + counts.get(tokenAdd));
                        }
                        DateTime time = new DateTime(1000 * unixTime);
                        int year = time.year().get();
                        int day = time.dayOfYear().get();

                        if (erc20Tokens.add(tokenAdd)) {
                            // a new ERC20 contract found
                            if (!eRCFirstAppearanceMap.containsKey(year))
                                eRCFirstAppearanceMap.put(year, new HashMap<Integer, Integer>());
                            if (!eRCFirstAppearanceMap.get(year).containsKey(day))
                                eRCFirstAppearanceMap.get(year).put(day, 0);
                            eRCFirstAppearanceMap.get(year).put(day, 1 + eRCFirstAppearanceMap.get(year).get(day));
                        }

                        if (!txMapInTime.containsKey(year)) txMapInTime.put(year, new HashMap<Integer, Integer>());
                        Map<Integer, Integer> dayMap = txMapInTime.get(year);
                        if (!dayMap.containsKey(day)) {
                            dayMap.put(day, 0);
                        }
                        Integer dayCount = dayMap.get(day);
                        dayMap.put(day, 1 + dayCount);


                    }
                } catch (Exception e) {
                    logger.error(line);
                    e.printStackTrace();
                }
            }
        }
        TokenFiltering.printFunctionOcc();
        TokenFiltering.printFunctionParamOcc();

        TokenGraph.printUnknownTokens(counts, 1000);
        logger.info("transactions in time");
        printDailyTransactions(txMapInTime);
        logger.info("erc20 first tx in time");
        printDailyTransactions(eRCFirstAppearanceMap);

    }

    private static void printDailyTransactions(Map<Integer, Map<Integer, Integer>> map) {
        for (int year : map.keySet()) {
            Map<Integer, Integer> dailyMap = map.get(year);
            for (int d : dailyMap.keySet()) {
                logger.info(year + "\t" + d + "\t" + dailyMap.get(d));
            }

        }
    }
}
