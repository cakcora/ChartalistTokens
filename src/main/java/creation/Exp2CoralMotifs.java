package creation;

import edu.uci.ics.jung.algorithms.metrics.TriadicCensus;
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by cxa123230 on 4/28/2018.
 */
public class Exp2CoralMotifs {
    private static final Logger logger = LoggerFactory.getLogger(Exp2CoralMotifs.class);

    public static void main(String args[]) throws Exception {

        List<String> files = Files.getTokenFiles(Params.graphFilesDir);
        files.remove(Params.userToUser + ".txt");
        String fileName = "CoralFlowMotifs.txt";
        files.remove(fileName);

        BufferedWriter wr = new BufferedWriter(new FileWriter(Params.d + "experiments/" + fileName));
        for (String file : files) {
            BufferedReader br = new BufferedReader(new FileReader(Params.graphFilesDir + file));
            Map<Integer, Map<Integer, DirectedSparseMultigraph>> graphMap = new TreeMap<>();
            int granularity = 1;
            Map coralMap = new HashMap<Integer, Integer>();
            String line = "";
            DirectedGraph globalGraph = new DirectedSparseGraph();
            while ((line = br.readLine()) != null) {
                String arr[] = line.split(" ");
                int node1 = Integer.parseInt(arr[0]);
                int node2 = Integer.parseInt(arr[1]);
                long unixTime = Long.parseLong(arr[2]);
                DateTime time = new DateTime(1000 * unixTime);
                int year = time.year().get();
                int timePeriod = getPeriod(granularity, time);
                Graph graph = getGraph(graphMap, year, timePeriod);
                graph.addVertex(node1);
                graph.addVertex(node2);
                TWEdge edge = new TWEdge(unixTime, node1, node2, new BigInteger(arr[3]));
                graph.addEdge(edge, node1, node2);

                globalGraph.addVertex(node1);
                globalGraph.addVertex(node2);
                globalGraph.addEdge(edge, node1, node2);
            }

            logger.info(file + " has " + globalGraph.getVertexCount() + " vertices and " + globalGraph.getEdgeCount());


            int s = 0;
            int nodeSize = 0;
            int edgeSize = 0;
            for (int g : graphMap.keySet()) {
                for (int g2 : graphMap.get(g).keySet()) {
                    DirectedGraph t = graphMap.get(g).get(g2);
                    nodeSize += t.getVertexCount();
                    edgeSize += t.getEdgeCount();

                    String motifs = getMotifs(t);
                    String s1 = file + "\t" + g + "\t" + g2 + "\t" + t.getVertexCount() + "\t" + t.getEdgeCount() + motifs;
                    logger.info(s1);
                    wr.write(s1 + "\r\n");
                    wr.flush();
                }
                s += graphMap.get(g).size();
            }
            logger.info(nodeSize / (double) s + ", " + edgeSize / (double) s + ", " + s + " graphs were created for " + file);
        }
        wr.close();
    }


    private static String getMotifs(DirectedGraph t) {
        String q = "";
        long[] triad_counts = new TriadicCensus().getCounts(t);
        for (int d = 1; d <= 16; d++) q = q + "\t" + triad_counts[d];
        return q;
    }

    private static Graph getGraph(Map<Integer, Map<Integer, DirectedSparseMultigraph>> gm, int year, int tp) {
        if (!gm.containsKey(year)) gm.put(year, new TreeMap<>());
        if (!gm.get(year).containsKey(tp)) gm.get(year).put(tp, new DirectedSparseMultigraph<Integer, TWEdge>());
        return gm.get(year).get(tp);
    }

    private static int getPeriod(int granularity, DateTime time) {
        if (granularity == 1) return time.getDayOfYear();
        if (granularity == 7) return time.getWeekOfWeekyear();
        if (granularity == 31) return time.getMonthOfYear();
        return -1;
    }
}
