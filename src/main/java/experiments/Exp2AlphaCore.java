package experiments;

import algorithms.AlphaCore;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import params.Params;
import structure.Contract;
import structure.TWEdge;
import utils.Files;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class Exp2AlphaCore {
    private static final Logger logger = LoggerFactory.getLogger(Exp2AlphaCore.class);

    public static void main(String[] args) throws Exception {
        int granularity = 1;
        boolean useExisting = true;
        Set<String> tokenMap = Contract.readTopTokensNames(200);
        if (!useExisting)
            FileUtils.cleanDirectory(new File(Params.alphaCoreDir));
        List<String> files = Files.getTokenFiles(Params.graphFilesDir);
        files.remove(Params.userToUserFile);
        files.remove(Params.nodeIdsFile);
        for (String tokenFileName : files) {
            String token = tokenFileName.substring(7, tokenFileName.length() - 6);
            if (!tokenMap.contains(token)) continue;
            logger.info(token);
            String fileName = Params.alphaCoreDir + token;
            if (Paths.get(fileName).toFile().exists()) continue;
            BufferedWriter wr = new BufferedWriter(new FileWriter(fileName));
            DirectedSparseMultigraph globalGr = new DirectedSparseMultigraph();
            Map<Integer, Map<Integer, DirectedSparseGraph>> graphMap = new TreeMap<>();
            BufferedReader br = new BufferedReader(new FileReader(Params.graphFilesDir + tokenFileName));
            String line = "";
            while ((line = br.readLine()) != null) {
                String[] arr = line.split(" ");
                int n1 = Integer.parseInt(arr[0]);
                int n2 = Integer.parseInt(arr[1]);
                long unixTime = Long.parseLong(arr[2]);
                BigInteger weight = new BigInteger(arr[3]);
                int maxTokenValue = 30;
                if (arr[3].length() < maxTokenValue) {
                    globalGr.addEdge(new TWEdge(unixTime, n1, n2, weight), n1, n2);
                    DateTime time = new DateTime(1000 * unixTime);
                    int year = time.year().get();
                    int timePeriod = Exp2SimpleFlow.getPeriod(granularity, time);
                    Graph graph = Exp2SimpleFlow.getGraph(graphMap, year, timePeriod);
                    graph.addEdge(new TWEdge(unixTime, n1, n2, weight), n1, n2);
                } else {
                    logger.info("invalid amount of token: " + line);
                }
            }


            Map<Integer, Double> corevalMap = new AlphaCore().findAlphaCoreValues(globalGr);
            logger.info(corevalMap.size() + " nodes");
            for (int node : corevalMap.keySet()) {
                Double coreVal = corevalMap.get(node);
                wr.write(token + "\t" + node + "\t" + coreVal + "\r\n");
            }
            wr.close();
        }

    }

}
