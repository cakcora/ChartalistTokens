package experiments;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
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


public class Exp2AlphaCoralFlow {
    private static final Logger logger = LoggerFactory.getLogger(Exp2AlphaCoralFlow.class);

    public static void main(String[] args) throws Exception {
        int granularity = 1;
        Set<String> tokenMap = Contract.readTopTokensNames(5);

        BufferedWriter wr2 = new BufferedWriter(new FileWriter(Params.coralFlowFile));
        List<String> tokenGraphFiles = Files.getTokenFiles(Params.graphFilesDir);


        for (String tokenFileName : tokenGraphFiles) {
            logger.info(tokenFileName);
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
                int maxTokenValue = 30;
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


            Map<Integer, Double[]> corevalMap = new HashMap<>();

            //for(int i=10;i>0;i=i-2)
            {
                final double coralK = 2 / 10d;
                for (int year : graphMap.keySet()) {
                    for (int period : graphMap.get(year).keySet()) {
                        DirectedGraph tempGr = graphMap.get(year).get(period);


                        List<Integer> del = new ArrayList<>();
                        for (Object n : tempGr.getVertices()) {
                            int node = (int) n;

                            Double nodeCore = 1d;
                            if (corevalMap.containsKey(node)) {
                                Double[] doubles = corevalMap.get(node);
                                nodeCore = doubles[0];
                            }
                            if (nodeCore > coralK) {
                                del.add(node);
                            }

                        }
                        int np = tempGr.getVertexCount();
                        int ep = tempGr.getEdgeCount();
                        for (int n : del) tempGr.removeVertex(n);
                        String motifs = Exp2SimpleFlow.getMotifs(tempGr);
                        String coeffs = Exp2SimpleFlow.getCoefficients(tempGr);
                        wr2.write(token + "\t" + coralK + "\t" + year + "\t" + period + "\t" +
                                tempGr.getVertexCount() + "\t" + np + "\t" +
                                tempGr.getEdgeCount() + "\t" + ep + "\t" +
                                motifs + "\t" + coeffs + "\r\n");
                    }
                }
            }
        }
        wr2.close();
    }

}
