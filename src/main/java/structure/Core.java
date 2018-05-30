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


    public String toString(int cmax) {
        String c = " ";

        for (int k = 1; k < cmax; k++) {
            if (!core.containsKey(k)) c = c + "0\t";
            else c = c + core.get(k).size() + "\t";
        }
        int f = 0;
        for (int k = cmax; k < max; k++) {
            if (core.containsKey(k))
                f += core.get(k).size();
        }
        c = c + f;
        return c;
    }

    public double findIntersection(Core c2) {
        Set<Integer> k1 = getAll();
        Set<Integer> intersection = new HashSet<Integer>(k1);
        Set<Integer> k2 = c2.getAll();
        intersection.retainAll(k2);
        return 100.0 * (intersection.size() / (double) k1.size());
    }

    private Set<Integer> getK(int k) {
        return this.core.get(k);
    }

    public int getCoreNumber() {
        return max;
    }

    public Set<Integer> getAll() {
        HashSet<Integer> s = new HashSet<>();
        for (Set<Integer> g : core.values()) {
            s.addAll(g);
        }
        return s;
    }

    public Set<Integer> getCoreandHigher(int coreCutoff) {
        HashSet<Integer> s = new HashSet<>();
        for (int k = coreCutoff; k <= max; k++) {
            if (core.containsKey(k))
                s.addAll(core.get(k));
        }
        return s;
    }
}
