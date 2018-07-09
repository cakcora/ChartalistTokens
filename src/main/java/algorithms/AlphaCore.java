package algorithms;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import structure.TWEdge;

import java.math.BigInteger;
import java.util.*;

/**
 * Created by cxa123230 on 6/24/2018.
 * Weighted: edges transfer tokens
 * Multiple: multiple edges between users
 * Directed: tokens flow in a direction between two users
 */
public class AlphaCore {
    private static final Logger logger = LoggerFactory.getLogger(AlphaCore.class);

    public static Map<Integer, Double[]> findAlphaCoreValues(DirectedSparseMultigraph<Integer, TWEdge> gr) {

        Map<Integer, Double[]> coreVals = new HashMap<>();
        Map<Integer, Integer> nodes = new HashMap();
        DirectedSparseMultigraph graph = new DirectedSparseMultigraph<>();
        for (TWEdge edge : gr.getEdges()) {
            int from = edge.getFrom();
            int to = edge.getTo();
            graph.addEdge(edge, from, to);
        }
        int stepSize = 5;
        for (int a = 100; a > 0; a = a - stepSize) {
            double alpha = a / 100d;

            boolean keepIterating = true;
            double max = 0d;
            while (keepIterating) {
                if (graph.getEdgeCount() == 0) break;
                keepIterating = false;
                double featureMap[][] = createFeatures(graph, nodes);

                double[] depths = new ModifiedBandDepth().computeModifiedBandDepth(featureMap);

                for (double d : depths) {
                    if (d > max) max = d;
                }
                while (100 * max < a) {
                    a = a - stepSize;
                }
                alpha = a / 100d;
                logger.info(alpha + " " + graph.getVertexCount() + "\t" + graph.getEdgeCount());
                for (int i = 0; i < depths.length; i++) {
                    if (depths[i] >= alpha) {
                        coreVals.put(nodes.get(i), new Double[]{alpha, featureMap[i][0], featureMap[i][1]});
                        if (graph.removeVertex(nodes.get(i)))
                            keepIterating = true;
                        else logger.error("Node is not in the graph. " + nodes.get(i));
                    }
                }
                for (Object o : graph.getVertices().toArray()) {
                    int o1 = (int) o;
                    if (graph.getIncidentEdges(o1).size() == 0) {
                        graph.removeVertex(o1);
                    }
                }
            }

        }
        return coreVals;
    }

    private static double[][] createFeatures(DirectedSparseMultigraph graph, Map<Integer, Integer> nodes) {
        int nIndex = 0;
        List<double[]> dummy = new ArrayList<>();
        for (Object node : graph.getVertices()) {
            int n = (int) node;

            Collection edges = graph.getInEdges(n);
            if (edges.size() == 0) {
                continue;
            }

            BigInteger edgeWeights2 = BigInteger.ZERO;
            for (Object edge : edges) {
                TWEdge e = (TWEdge) edge;
                edgeWeights2 = edgeWeights2.add(e.getEdgeWeight());
            }
            dummy.add(new double[]{(double) (edges.size()), edgeWeights2.doubleValue()});
            nodes.put(nIndex, n);
            nIndex++;
        }
        int size = dummy.size();
        double[][] featureMap = new double[size][2];
        for (int i = 0; i < size; i++) {
            double[] doubles = dummy.get(i);
            featureMap[i][0] = doubles[0];
            featureMap[i][1] = doubles[1];
        }
        return featureMap;
    }
}
