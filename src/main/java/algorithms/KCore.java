package algorithms;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import structure.Core;

import java.util.*;

/**
 * Created by cxa123230 on 4/25/2018.
 */
public class KCore {
    private static final Logger logger = LoggerFactory.getLogger(KCore.class);

    public static void main(String args[]) throws Exception {
        UndirectedGraph<Integer, Integer> graph = new UndirectedSparseGraph<>();
        graph.addVertex(0);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);

        graph.addEdge(0, 0, 1);
        graph.addEdge(1, 1, 4);
        graph.addEdge(2, 1, 3);
        graph.addEdge(3, 1, 2);
        graph.addEdge(4, 2, 3);
        graph.addEdge(5, 5, 3);
        graph.addEdge(6, 6, 5);


    }

    private static List<Integer> updateDegrees(Map<Integer, Integer> nodeDeg, Map<Integer, HashSet<Integer>> degrees, UndirectedGraph<Integer, Integer> graph) {
        degrees.clear();
        nodeDeg.clear();
        List<Integer> emptied = new ArrayList<>();
        for (int n : graph.getVertices()) {
            int neighborCount = graph.getNeighborCount(n);
            if (neighborCount == 0) {
                emptied.add(n);
            } else {
                if (!degrees.containsKey(neighborCount)) {
                    degrees.put(neighborCount, new HashSet<>());
                }
                degrees.get(neighborCount).add(n);
                nodeDeg.put(n, neighborCount);
            }
        }
        for (int e : emptied) {
            graph.removeVertex(e);
        }
        return emptied;

    }

    public Core findCore(DirectedGraph<Integer, Integer> graph) {
        UndirectedGraph<Integer, Integer> newGraph = new UndirectedSparseGraph<Integer, Integer>();
        int eCount = 0;
        for (int node : graph.getVertices()) {
            newGraph.addVertex(node);
            for (int ne : graph.getNeighbors(node)) {
                newGraph.addEdge(eCount++, node, ne);
            }
        }
        return findCore(newGraph);
    }

    public Core findCore(UndirectedGraph<Integer, Integer> graph) {
        Map<Integer, HashSet<Integer>> nodeSets = new TreeMap<>();
        Map<Integer, Integer> nodeDeg = new HashMap<>();
        updateDegrees(nodeDeg, nodeSets, graph);
        Core core = new Core();
        int kSoFar = 0;
        int stepSize = 1;
        while (true) {
            if (nodeSets.containsKey(1)) stepSize = 1;
            if (nodeSets.containsKey(stepSize)) {
                kSoFar += stepSize;
                for (int node : nodeSets.get(stepSize)) {
                    ArrayList<Integer> g = new ArrayList<Integer>(graph.getNeighbors(node));
                    for (int e = 0; e < stepSize && e < g.size(); e++) {
                        Integer remove = g.get(e);
                        nodeDeg.put(remove, nodeDeg.get(remove) - 1);
                        graph.removeEdge(graph.findEdge(node, remove));
                    }
                    nodeDeg.put(node, 0);
                }
                List<Integer> deletedNodes = updateDegrees(nodeDeg, nodeSets, graph);
                core.addToCore(kSoFar, deletedNodes);
            } else {
                stepSize++;
            }

            if (nodeSets.isEmpty()) break;
        }
        return core;
    }


}
