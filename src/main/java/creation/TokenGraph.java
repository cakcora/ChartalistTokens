package creation;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import params.Params;
import structure.Contract;
import structure.TWEdge;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by cxa123230 on 4/16/2018.
 */


public class TokenGraph {
    private static final Logger logger = LoggerFactory.getLogger(TokenGraph.class);

    public static void main(String args[]) throws Exception {


        FileUtils.cleanDirectory(new File(Params.graphFilesDir));
        Map<String, Contract> tokenMap = Contract.readTopTokens();
        List<String> files = getTokenFiles(Params.tokenFilesDir);
        logger.info("Found " + files.size() + " tokens.");

        for (String f : files) {
            List<TWEdge> edges = readFiles(tokenMap, Params.tokenFilesDir, f);
            writeEdges(Params.graphFilesDir, f, edges);
        }
    }

    private static void writeEdges(String graphFilesDir, String f, List<TWEdge> edges) throws IOException {
        BufferedWriter wr = new BufferedWriter(new FileWriter(graphFilesDir + "network" + f));
        for (TWEdge edge : edges) {
            wr.write(edge.toString() + "\r\n");
        }
        wr.close();
    }

    private static List<TWEdge> readFiles(Map<String, Contract> tokenMap, String dir, String filename) throws IOException {


        BufferedReader br = new BufferedReader(new FileReader(dir + filename));

        logger.info("parsing " + filename);
        List<TWEdge> edges = new ArrayList<>();
        String line;
        while ((line = br.readLine()) != null) {
            String arr[] = line.split("\t");
            try {
                TWEdge edge = null;
                long unixTime = Long.parseLong(arr[0]);
                String from = arr[1].trim();
                String to = arr[2].trim();
                BigInteger gas_used = new BigInteger(arr[3]);
                BigInteger ethValue = new BigInteger(arr[4]);
                if (arr.length == 5) {
                    edge = new TWEdge(unixTime, from, to, ethValue);
                } else if (arr.length > 5) {
                    //has a function
                    String tokenName;
                    Contract contract;
                    if (tokenMap.containsKey(to)) {
                        contract = tokenMap.get(to);
                        tokenName = contract.getShortName();
                    } else
                        tokenName = "unknowntoken";

                    edge = getTWEdge(arr, tokenName);
                    edges.add(edge);
                }

            } catch (Exception e) {
                logger.error("Transaction is corrupted. Data is: " + line);
            }
        }

        return edges;
    }


    private static TWEdge getTWEdge(String[] arr, String tokenName) {

        String functionName = arr[5];
        long unixTime = Long.parseLong(arr[0]);
        String from = arr[1].trim();
        String to = arr[7].trim();
        BigInteger ethValue = new BigInteger(arr[4]);
        BigInteger tokenValue = new BigInteger("0");
        switch (functionName) {
            case "transfer": {
                tokenValue = new BigInteger(arr[6]);
                break;
            }
            case "approve": {
                tokenValue = new BigInteger(arr[6]);
                break;
            }
            case "approveAndCall": {
                tokenValue = new BigInteger(arr[6]);
                break;
            }
            case "transferFrom": {
                from = arr[7];
                to = arr[8];
                tokenValue = new BigInteger(arr[6]);
                break;
            }
            case "balanceOf": {
                break;
            }
            case "transferEvent": {
                tokenValue = new BigInteger(arr[6]);
                from = arr[7];
                to = arr[8];
                break;
            }
            case "approvalEvent": {
                tokenValue = new BigInteger(arr[6]);
                from = arr[7];
                to = arr[8];
                break;
            }
            default: {
                logger.error(functionName + " parameters are unknown.");
            }
        }

        return new TWEdge(unixTime, from, to, ethValue, tokenName, tokenValue);
    }

    private static List<String> getTokenFiles(String dir) {
        List<String> results = new ArrayList<String>();
        File[] files = new File(dir).listFiles();

        for (File file : files) {
            if (file.isFile()) {
                results.add(file.getName());
            }
        }
        return results;
    }


}
