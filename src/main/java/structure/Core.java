package structure;

import java.util.*;

/**
 * Created by cxa123230 on 4/28/2018.
 */
public class Core {
    private Map<Integer, Set<Integer>> core;
    private int max = 0;

    public Core() {
        core = new HashMap<>();
    }

    public void addToCore(int k, List<Integer> kCoreNodes) {
        core.put(k, new HashSet<Integer>(kCoreNodes));
        if (max < k) max = k;
    }

    @Override
    public String toString() {
        String c = " ";

        for (int k = 1; k <= max || k <= 10; k++) {
            if (!core.containsKey(k)) c = c + "0\t";
            else c = c + core.get(k).size() + "\t";
        }
        return c;
    }

    public double findIntersection(Core c2, int k) {
        Set<Integer> k1 = getK(k);
        Set<Integer> intersection = new HashSet<Integer>(k1);
        Set<Integer> k2 = c2.getK(k);
        intersection.retainAll(k2);
        return intersection.size() / (double) k1.size();
    }

    private Set<Integer> getK(int k) {
        return this.core.get(k);
    }

    public int getCoreNumber() {
        return max;
    }
}
