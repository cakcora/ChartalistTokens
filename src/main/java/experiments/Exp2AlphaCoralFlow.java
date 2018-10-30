package experiments;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import params.Params;
import structure.Contract;
import structure.TWEdge;
import utils.Files;

import java.io.*;
import java.math.BigInteger;
import java.util.*;


public class Exp2AlphaCoralFlow {
    private static final Logger logger = LoggerFactory.getLogger(Exp2AlphaCoralFlow.class);

    public static void main(String[] args) throws Exception {
        int granularity = Params.DAY;
        Set<String> tokenMap = Contract.readTopTokensNames(200);
        tokenMap.remove("beautychain1");
        tokenMap.remove("beautychain2");
        int topCore = 10;
        int maxTokenValue = 30;
//        tokenMap = new HashSet<>(); tokenMap.add("ades"); tokenMap.add("aion");tokenMap.add("aragon");
        List<String> tokenGraphFiles = Files.getTokenFiles(Params.graphFilesDir);
        tokenGraphFiles.remove(Params.userToUserFile);
        tokenGraphFiles.remove(Params.nodeIdsFile);
        BufferedWriter wr2 = new BufferedWriter(new FileWriter(Params.coralFlowFile + topCore));
        for (String tokenFileName : tokenGraphFiles) {

            String token = tokenFileName.substring(7, tokenFileName.length() - 6);
            if (!tokenMap.contains(token)) continue;

            Set<Integer> coreNodes = readTokenCore(token, Params.alphaCoreDir, topCore);
            logger.info(token + " has " + coreNodes.size() + " nodes.");
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
                if (arr[3].length() < maxTokenValue) {
                    globalGr.addEdge(new TWEdge(unixTime, n1, n2, weight), n1, n2);
                    DateTime time = new DateTime(1000 * unixTime);
                    int year = time.year().get();
                    int timePeriod = Exp2SimpleFlow.getPeriod(granularity, time);
                    Graph graph = Exp2SimpleFlow.getGraph(graphMap, year, timePeriod);
                    graph.addEdge(new TWEdge(unixTime, n1, n2, weight), n1, n2);
                } else {
//                    logger.info("invalid amount of token: " + line);
                }
            }

            for (int year : graphMap.keySet()) {
                for (int period : graphMap.get(year).keySet()) {
                    DirectedGraph tempGr = graphMap.get(year).get(period);

                    List<Integer> del = new ArrayList<>();
                    for (Object n : tempGr.getVertices()) {
                        int node = (int) n;

                        if (!coreNodes.contains(node)) {
                            del.add(node);
                        }

                    }
                    int vc1 = tempGr.getVertexCount();
                    int ec1 = tempGr.getEdgeCount();
                    for (int n : del) tempGr.removeVertex(n);

                    int vertexCount = tempGr.getVertexCount();
                    int edgeCount = tempGr.getEdgeCount();
                    if (vertexCount > 1000) {
                        logger.info(token + " network has " + vertexCount + " nodes in " + year + "/" + period);
                    }
                    long[] triads = Exp2SimpleFlow.getMotifCounts(tempGr);
                    String motifs = "";
                    for (long i : triads) {
                        motifs = motifs + i + "\t";
                    }
                    String coeffs = Exp2SimpleFlow.getCoefficients(tempGr);
                    wr2.write(token + "\t" +
                            year + "\t" + period + "\t" +
                            vertexCount + "\t" + vc1 + "\t" +
                            edgeCount + "\t" + ec1 + "\t" +
                            motifs + coeffs + "\r\n");
                    wr2.flush();
                }
            }

        }
        wr2.close();
    }

    private static Set<Integer> readTokenCore(String token, String alphaCoreFile, int topCore) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(alphaCoreFile + token));
        String line = "";
        Map<Integer, Double> coralVals = new HashMap<>();
        DescriptiveStatistics ds = new DescriptiveStatistics();
        while ((line = br.readLine()) != null) {

            String arr[] = line.split("\t");
            if (arr[0].equalsIgnoreCase(token)) {
                double c = Double.parseDouble(arr[2]);
                int node = Integer.parseInt(arr[1]);
                coralVals.put(node, c);
                ds.addValue(c);
            }
        }
        if (coralVals.isEmpty()) {
            logger.info(" no nodes in the network: " + token);
            return new HashSet<>();
        }

        double topThreshold = ds.getPercentile(topCore);
        logger.info("Max depth: " + topThreshold + " for %" + topCore);
        Set<Integer> nodes = new HashSet<>();

        for (int n : coralVals.keySet()) {
            if (coralVals.get(n) <= topThreshold) {
                nodes.add(n);
            }
        }

        return nodes;

    }

}
