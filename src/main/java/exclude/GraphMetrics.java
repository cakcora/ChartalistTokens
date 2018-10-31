package exclude;

import edu.uci.ics.jung.algorithms.metrics.Metrics;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import experiments.Exp2SimpleFlow;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import params.Params;
import structure.Contract;
import structure.TWEdge;
import utils.Files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.*;


public class GraphMetrics {
    private static final Logger logger = LoggerFactory.getLogger(GraphMetrics.class);

    public static void main(String[] args) throws Exception {
        int granularity = Params.DAY;
        Set<String> tokenMap = Contract.readTopTokensNames(200);
        tokenMap.remove("beautychain1");
        tokenMap.remove("beautychain2");
        int topCore = 5;
        int maxTokenValue = 30;
        List<String> tokenGraphFiles = Files.getTokenFiles(Params.graphFilesDir);
        tokenGraphFiles.remove(Params.userToUserFile);
        tokenGraphFiles.remove(Params.nodeIdsFile);
        BufferedWriter wr2 = new BufferedWriter(new FileWriter(Params.d + "graphMetrics.txt"));
        for (String tokenFileName : tokenGraphFiles) {

            String token = tokenFileName.substring(7, tokenFileName.length() - 6);
            if (!tokenMap.contains(token)) continue;
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

                    logger.info(token + " " + year + " " + period);


                    int vertexCount = tempGr.getVertexCount();
                    int edgeCount = tempGr.getEdgeCount();
                    double avgCoeff = avgClusCoeff(tempGr);
                    BigInteger sum = BigInteger.ZERO;
                    Collection edges = tempGr.getEdges();
                    for (Object e : edges) {
                        TWEdge edge = (TWEdge) e;

                        sum = sum.add(edge.getEdgeWeight());
                    }
                    BigInteger avg = sum.divide(BigInteger.valueOf(edges.size()));
                    wr2.write(token + "\t" +
                            year + "\t" + period + "\t" +
                            vertexCount + "\t" +
                            edgeCount + "\t" + sum + "\t" + avg + "\t" +
                            avgCoeff + "\r\n");
                    wr2.flush();
                }
            }

        }
        wr2.close();
    }

    public static double avgClusCoeff(DirectedGraph t) {
        String h = "";
        Map<Integer, Double> coeff = new Metrics().clusteringCoefficients(t);

        double sum = 0d;
        for (Double n : coeff.values()) {
            sum += n;
        }
        return sum / coeff.size();
    }
}
