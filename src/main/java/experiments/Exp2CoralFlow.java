package experiments;

import algorithms.AlphaCore;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import params.Params;
import structure.TWEdge;
import utils.Files;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class Exp2CoralFlow {
    private static final Logger logger = LoggerFactory.getLogger(Exp2CoralFlow.class);

    public static void main(String[] args) throws IOException {
        int granularity = 1;
        BufferedWriter wr = new BufferedWriter(new FileWriter(Params.d + "experiments/" + "coral.txt"));
        BufferedWriter wr2 = new BufferedWriter(new FileWriter(Params.d + "experiments/" + "coralFlows.txt"));
        wr.write("token\tnode\tcoreVal\tedgefactor\tweightfactor\r\n");
        List<String> files = Files.getTokenFiles(Params.graphFilesDir);
        for (String tokenFileName : files) {

            String token = tokenFileName.substring(7, tokenFileName.length() - 6);
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


            Map<Integer, Double[]> corevalMap = new AlphaCore().findAlphaCoreValues(globalGr);
            for (int x : corevalMap.keySet()) {
                Double[] doubles = corevalMap.get(x);
                wr.write(token + "\t" + x + "\t" + doubles[0] + "\t" + doubles[1] + "\t" + doubles[2] + "\r\n");
            }
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
        wr.close();
        wr2.close();
    }

}
