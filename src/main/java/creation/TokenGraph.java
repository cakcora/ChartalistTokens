package creation;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import params.Params;
import structure.Contract;
import structure.TWEdge;
import utils.Files;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cxa123230 on 4/16/2018.
 *
 */


public class TokenGraph {
    private static final Logger logger = LoggerFactory.getLogger(TokenGraph.class);
    private static final Map<String, Integer> unknownTokens = new HashMap<>();
    private static final Map<String, Integer> nodeIds = new HashMap<>(100000);
    static int count = 0;
    public static void main(String args[]) throws Exception {

        FileUtils.cleanDirectory(new File(Params.graphFilesDir));
        Map<String, Contract> tokenMap = Contract.readTopTokens();
        List<String> files = Files.getTokenFiles(Params.tokenFilesDir);
        logger.info("Found " + files.size() + " tokens.");

        for (String f : files) {
            readFiles(tokenMap, Params.tokenFilesDir, f);
        }
        printUnknownTokens(unknownTokens, 1000);
        printNodeIds(Params.graphFilesDir);
    }

    private static void writeEdges(String graphFilesDir, String f, List<TWEdge> edges) throws IOException {
        BufferedWriter wr = new BufferedWriter(new FileWriter(graphFilesDir + "network" + f, true));
        for (TWEdge edge : edges) {
            if (edge.hasTransferredTokenValue()) {
                wr.write(edge.toTDAEdge() + "\r\n");
            }
        }
        wr.close();
    }

    private static void readFiles(Map<String, Contract> tokenMap, String dir, String filename) throws IOException {

        BufferedReader br = new BufferedReader(new FileReader(dir + filename));

        int stdEtherTxLength = 5;
        logger.info("parsing " + filename);
        List<TWEdge> edges = new ArrayList<>();
        String line;
        int count = 0;
        while ((line = br.readLine()) != null) {
            String arr[] = line.split("\t");
            try {
                TWEdge edge = null;
                long unixTime = Long.parseLong(arr[0]);
                int fromID = getId(arr[1].trim());
                String to = arr[2].trim();
                BigInteger ethValue = new BigInteger(arr[4]);
                if (arr.length == stdEtherTxLength) {
                    int toID = getId(to);
                    edge = new TWEdge(unixTime, fromID, toID, ethValue);
                } else if (arr.length > stdEtherTxLength) {
                    //has a function
                    String tokenName;
                    Contract contract;
                    if (tokenMap.containsKey(to)) {
                        contract = tokenMap.get(to);
                        tokenName = contract.getShortName();
                    } else {
                        tokenName = to;
                        addtoUnknownTokens(tokenName);
                    }
                    edge = getTWEdge(arr, nodeIds, tokenName);
                }
                edges.add(edge);
                if (edges.size() > 400000) {
                    writeEdges(Params.graphFilesDir, filename, edges);
                    edges.clear();
                }

            } catch (Exception e) {
                logger.error("Transaction is corrupted. Data is: " + line);
            }
        }
        if (!edges.isEmpty())
            writeEdges(Params.graphFilesDir, filename, edges);
        return;
    }

    public static int getId(String address) {
        if (!nodeIds.containsKey(address)) {
            nodeIds.put(address, count);
            count++;
        }
        return nodeIds.get(address);
    }

    private static void addtoUnknownTokens(String tokenName) {
        if (!unknownTokens.containsKey(tokenName)) {
            unknownTokens.put(tokenName, 0);
        }
        unknownTokens.put(tokenName, 1 + unknownTokens.get(tokenName));
    }


    private static void printNodeIds(String graphFilesDir) throws IOException {
        logger.info("Printing out node ids");
        BufferedWriter wr = new BufferedWriter(new FileWriter(graphFilesDir + "NodeIds.txt"));
        for (String to : nodeIds.keySet()) {
            Integer integer = nodeIds.get(to);
            wr.write(integer + " " + to + "\r\n");
        }
        wr.close();
    }

    static void printUnknownTokens(Map<String, Integer> tokens, int limit) {
        if (!tokens.isEmpty())
            logger.info("These addresses belong to tokens that we do not know.");
        for (String to : tokens.keySet()) {
            Integer integer = tokens.get(to);
            if (integer > limit) {
                logger.info(to + ":" + integer);
            }
        }
    }


    public static TWEdge getTWEdge(String[] arr, Map<String, Integer> nodeIds, String tokenName) {

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
        return new TWEdge(unixTime, getId(from), getId(to), ethValue, tokenName, tokenValue);
    }
}
