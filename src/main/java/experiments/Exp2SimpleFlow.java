package experiments;

import edu.uci.ics.jung.algorithms.metrics.Metrics;
import edu.uci.ics.jung.algorithms.metrics.TriadicCensus;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import params.Params;
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
public class Exp2SimpleFlow {
    private static final Logger logger = LoggerFactory.getLogger(Exp2SimpleFlow.class);

    public static void main(String args[]) throws Exception {

        List<String> files = Files.getTokenFiles(Params.graphFilesDir);
        files.remove(Params.userToUser + ".txt");
        String fileName = "FlowMotifs.txt";
        files.remove(fileName);

        BufferedWriter wr = new BufferedWriter(new FileWriter(Params.d + "experiments/" + fileName));
//        String doneFile = "doneList.txt";
//        BufferedReader done = new BufferedReader(new FileReader(Params.graphFilesDir + doneFile));
//        String h = "";
//        files.remove(doneFile);
//        files.remove("motifsSoFar.txt");
//        logger.info(files.toString());
//        while ((h = done.readLine()) != null) {
//            if (files.contains(h)) {
//                files.remove(h);
//                logger.info(h + " already computed.");
//            }
//        }
        for (String file : files) {
            BufferedReader br = new BufferedReader(new FileReader(Params.graphFilesDir + file));
            Map<Integer, Map<Integer, DirectedSparseGraph>> graphMap = new TreeMap<>();
            int granularity = 1;

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
                    //String coeffs = getCoefficients(t);
                    //UndirectedKCore kCore = new UndirectedKCore();
                    //Core core = kCore.findCore(t);
                    // String cores = core.toString();
//                logger.info(core.getDegeneracy()+"");
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

    static String getCoefficients(DirectedGraph t) {
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


    static String getMotifs(DirectedGraph t) {
        String q = "";
        long[] triad_counts = new TriadicCensus().getCounts(t);
        for (int d = 1; d <= 16; d++) q = q + "\t" + triad_counts[d];
        return q;
    }

    static Graph getGraph(Map<Integer, Map<Integer, DirectedSparseGraph>> gm, int year, int tp) {
        if (!gm.containsKey(year)) gm.put(year, new TreeMap<>());
        if (!gm.get(year).containsKey(tp)) gm.get(year).put(tp, new DirectedSparseGraph<>());
        return gm.get(year).get(tp);
    }

    static int getPeriod(int granularity, DateTime time) {
        if (granularity == 1) return time.getDayOfYear();
        if (granularity == 7) return time.getWeekOfWeekyear();
        if (granularity == 31) return time.getMonthOfYear();
        return -1;
    }
}
