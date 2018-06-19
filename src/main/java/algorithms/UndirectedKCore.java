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
public class UndirectedKCore {
    private static final Logger logger = LoggerFactory.getLogger(UndirectedKCore.class);

    public static void main(String args[]) throws Exception {
        UndirectedGraph<Integer, Integer> graph = new UndirectedSparseGraph<>();
        graph.addVertex(0);
        graph.addVertex(1);
        graph.addVertex(2);
        graph.addVertex(3);
        graph.addVertex(4);

        graph.addEdge(0, 0, 1);
        graph.addEdge(1, 1, 2);
        graph.addEdge(2, 1, 3);
        graph.addEdge(3, 1, 4);
        graph.addEdge(4, 2, 3);
        graph.addEdge(5, 2, 4);
        graph.addEdge(6, 3, 4);
        graph.addEdge(7, 3, 5);
        graph.addEdge(8, 3, 6);
        graph.addEdge(9, 5, 6);

        graph.addEdge(10, 4, 7);
        graph.addEdge(11, 7, 8);
        graph.addEdge(12, 1, 9);
        graph.addEdge(13, 2, 9);
        graph.addEdge(14, 3, 9);
        graph.addEdge(15, 4, 9);

        Core core = new UndirectedKCore().findCore(graph);
        int f = core.getDegeneracy();
        logger.info(core.getDegeneracy() + "");
        logger.info(core.getCoreK(4).toString());


    }

    private static List<Integer> updateDegreeSets(Map<Integer, HashSet<Integer>> nodeSets, UndirectedGraph<Integer, Integer> graph) {
        nodeSets.clear();
        List<Integer> emptied = new ArrayList<>();
        for (int n : graph.getVertices()) {
            int neighborCount = graph.getNeighborCount(n);
            if (neighborCount == 0) {
                emptied.add(n);
            } else {
                if (!nodeSets.containsKey(neighborCount)) {
                    nodeSets.put(neighborCount, new HashSet<>());
                }
                nodeSets.get(neighborCount).add(n);
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
        List<Integer> initial0DegreeNodes = updateDegreeSets(nodeSets, graph);
        Core core = new Core();
        int kSoFar = 1;
        int stepSize = 1;
        while (graph.getVertexCount() > 0) {
            while (nodeSets.containsKey(kSoFar)) {
                for (int node : nodeSets.get(kSoFar)) {
                    ArrayList<Integer> g = new ArrayList<Integer>(graph.getNeighbors(node));
                    for (Integer node2 : g) {
                        graph.removeEdge(graph.findEdge(node, node2));
                    }
                }
                List<Integer> zeroDegreeNodes = updateDegreeSets(nodeSets, graph);
                core.addToCore(kSoFar, zeroDegreeNodes);
            }
            kSoFar += stepSize;
            if (nodeSets.isEmpty()) break;
        }
        return core;
    }
}
