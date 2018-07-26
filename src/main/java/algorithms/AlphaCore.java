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

    public static Map<Integer, Double> findAlphaCoreValues(DirectedSparseMultigraph<Integer, TWEdge> graph) {

        Map<Integer, Double> coreVals = new HashMap<>();
        Map<Integer, Integer> nodes = new HashMap();


        int stepsize = 1;
        int a = (100);
        double alpha = a / 100d;

        boolean keepIterating = true;
        double[] depths = null;
        while (graph.getEdgeCount() > 0) {
            if (!keepIterating) {
                a = a - stepsize;
                alpha = (a - stepsize) / 100.0;
            } else {
                logger.info("Iterating for " + alpha + "\tN:" + graph.getVertexCount() + "\tE:" + graph.getEdgeCount());
                BigInteger featureMap[][] = createFeatures(graph, nodes);
                depths = new ModifiedBandDepth().compute(featureMap);
                logger.info("Computed depth for " + alpha);
            }
            keepIterating = false;
            double maxDepth = 0d;
            for (double d : depths) {
                if (d > maxDepth) maxDepth = d;
            }
            if ((maxDepth + stepsize / 100.0) < alpha) {
                a = (int) (maxDepth * 100);
                alpha = (a - stepsize) / 100.0;

            }
            for (int i = 0; i < depths.length; i++) {
                if (depths[i] >= alpha) {
                    coreVals.put(nodes.get(i), alpha);
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

        Collection<Integer> vertices = graph.getVertices();
        for (int n : vertices) {
            coreVals.put(n, alpha);
            logger.info("Adding: " + n);
        }
        return coreVals;
    }


    private static BigInteger[][] createFeatures(DirectedSparseMultigraph graph, Map<Integer, Integer> nodes) {
        int nIndex = 0;
        List<BigInteger[]> featureMap = new ArrayList<>();
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
            featureMap.add(new BigInteger[]{BigInteger.valueOf(edges.size()), edgeWeights2});
            nodes.put(nIndex, n);
            nIndex++;
        }
        int size = featureMap.size();
        BigInteger[][] featureArr = new BigInteger[size][2];
        for (int i = 0; i < size; i++) {
            BigInteger[] doubles = featureMap.get(i);
            featureArr[i][0] = doubles[0];
            featureArr[i][1] = doubles[1];
        }
        return featureArr;
    }
}
