package creation;

import algorithms.KCore;
import edu.uci.ics.jung.algorithms.metrics.Metrics;
import edu.uci.ics.jung.algorithms.metrics.TriadicCensus;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import params.Params;
import structure.Core;
import utils.Files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by cxa123230 on 4/28/2018.
 */
public class GraphMetrics {
    private static final Logger logger = LoggerFactory.getLogger(GraphMetrics.class);

    public static void main(String args[]) throws Exception {

        List<String> files = Files.getTokenFiles(Params.graphFilesDir);
        files.remove(Params.userToUser + ".txt");
        String fileName = "metrics.txt";
        files.remove(fileName);
        BufferedWriter wr = new BufferedWriter(new FileWriter(Params.graphFilesDir + fileName));
        for (String file : files) {
            //String file = "networkraidenTX.txt";
            BufferedReader br = new BufferedReader(new FileReader(Params.graphFilesDir + file));
            Map<Integer, Map<Integer, DirectedSparseMultigraph>> graphMap = new TreeMap<>();
            int granularity = 7;

            String line = "";
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
                graph.addEdge(graph.getEdgeCount(), node1, node2);


            }

            int s = 0;
            int nodeSize = 0;
            int edgeSize = 0;
            for (int g : graphMap.keySet()) {
                for (int g2 : graphMap.get(g).keySet()) {
                    DirectedGraph t = graphMap.get(g).get(g2);
                    nodeSize += t.getVertexCount();
                    edgeSize += t.getEdgeCount();

                    String motifs = getMotifs(t);
                    String coeffs = getCoefficients(t);
                    KCore kCore = new KCore();
                    Core core = kCore.findCore(t);
                    String cores = core.toString();
//                logger.info(core.getCoreNumber()+"");
                    String s1 = file + "\t" + g + "\t" + g2 + "\t" + t.getVertexCount() + "\t" + t.getEdgeCount() + "\t" + motifs + coeffs + core.getCoreNumber() + "\t" + cores;
                    logger.info(s1);
                    wr.write(s1 + "\r\n");
                }
                s += graphMap.get(g).size();
            }
            logger.info(nodeSize / (double) s + ", " + edgeSize / (double) s + ", " + s + " graphs were created for " + file);
        }
        wr.close();
    }

    private static String getCoefficients(DirectedGraph t) {
        String h = "";
        Map<Integer, Double> coeff = new Metrics().clusteringCoefficients(t);
        Map<Integer, Integer> ma = new TreeMap<>();
        int bins = 10;
        for (int y = 0; y <= bins; y++) ma.put(y, 0);
        for (Double n : coeff.values()) {
            int val = (int) (n * bins);
            ma.put(val, 1 + ma.get(val));
        }
        for (int y = 0; y <= bins; y++) h = h + "\t" + ma.get(y);
        return h;
    }


    private static String getMotifs(DirectedGraph t) {
        String q = "";
        long[] triad_counts = new TriadicCensus().getCounts(t);
        for (int d = 1; d <= 16; d++) q = q + "\t" + triad_counts[d];
        return q;
    }

    private static Graph getGraph(Map<Integer, Map<Integer, DirectedSparseMultigraph>> gm, int year, int tp) {
        if (!gm.containsKey(year)) gm.put(year, new TreeMap<>());
        if (!gm.get(year).containsKey(tp)) gm.get(year).put(tp, new DirectedSparseMultigraph<>());
        return gm.get(year).get(tp);
    }

    private static int getPeriod(int granularity, DateTime time) {
        if (granularity == 1) return time.getDayOfYear();
        if (granularity == 7) return time.getWeekOfWeekyear();
        if (granularity == 31) return time.getMonthOfYear();
        return -1;
    }
}
