package experiments;

import algorithms.UndirectedKCore;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import params.Params;
import structure.Core;
import structure.Granularity;
import utils.Files;

import java.io.*;
import java.util.*;

/**
 * Created by cxa123230 on 4/28/2018.
 */
public class Exp1CoreDecompisiton {
    private static final Logger logger = LoggerFactory.getLogger(Exp1CoreDecompisiton.class);

    public static void main(String args[]) throws Exception {

        List<String> files = Files.getTokenFiles(Params.graphFilesDir);
        files.remove("NodeIds.txt");
        String coreFile = "core.txt";
        String tokenbehaviorFile = "investorBehavior.txt";
        BufferedWriter wr = new BufferedWriter(new FileWriter(Params.d + "experiments/" + coreFile));
        BufferedWriter bwr = new BufferedWriter(new FileWriter(Params.d + "experiments/" + tokenbehaviorFile));
        wr.write("token\tyear\tperiod\tnodes\tnewnodes\tedges\tretention1\tretention3\tretention6\tdegeneracy");
        bwr.write("token\tmeanselltime\ticosale\tbuynosale\ticoandbuyssells\tt0\tt1\tt3\tt7\tt15\tt30\tt60\tn\tvariance\tskew\tkurtosis\tmedianselltime");
        int maxCore = 10;
        for (int i = 1; i <= maxCore; i++) {
            wr.write("\tc" + i);
        }
        for (int i = 0; i < maxCore; i++) {
            for (int j = 0; j < maxCore; j++) {
                bwr.write("\tm" + i + "" + j);
            }
        }
        wr.write("\r\n");
        bwr.write("\r\n");
        int granularity = Granularity.DAY;


        StringBuffer sb = new StringBuffer();
        for (String file : files) {
            logger.info("processing " + file);
            BufferedReader br = new BufferedReader(new FileReader(Params.graphFilesDir + file));
            Map<Integer, Map<Integer, DirectedSparseMultigraph>> graphMap = createGraphs(granularity, br);
            Map<Integer, Map<Integer, Core>> coreMap = new HashMap<>();
            Map<Integer, List<Integer>> buyers = new HashMap<Integer, List<Integer>>();
            Map<Integer, List<Integer>> sellers = new HashMap<Integer, List<Integer>>();
            for (int g : graphMap.keySet()) {
                for (int g2 : graphMap.get(g).keySet()) {
                    int previousInvestorCount = buyers.size();
                    DirectedGraph grapht1 = graphMap.get(g).get(g2);

                    int txCount = grapht1.getEdgeCount();
                    for (Object edge : grapht1.getEdges()) {
                        int n = (int) edge;
                        int investor = (int) grapht1.getDest(n);
                        int seller = (int) grapht1.getSource(n);
                        if (!buyers.containsKey(investor)) buyers.put(investor, new ArrayList<>());
                        if (!sellers.containsKey(seller)) sellers.put(seller, new ArrayList<>());
                        Integer period = 0;
                        if (granularity == Granularity.DAY) period = g * 365 + g2;
                        else if (granularity == Granularity.MONTH) period = g * 12 + g2;
                        else throw new Exception("Time granularity is unknown " + granularity);
                        buyers.get(investor).add(period);
                        sellers.get(seller).add(period);
                    }
                    UndirectedKCore undirectedKCore = new UndirectedKCore();
                    Core core = undirectedKCore.findCore(grapht1);
                    if (!coreMap.containsKey(g)) coreMap.put(g, new HashMap<>());
                    coreMap.get(g).put(g2, core);
                    String cores = core.toString(maxCore);


                    double retention1 = 0.0;
                    int i1 = 1;
                    int coreCutoff = 1;
                    for (int i = 1; i <= i1; i++) {
                        retention1 += computeRetainedUsers(granularity, coreMap, g, g2, i, coreCutoff);
                    }
                    retention1 = retention1 / i1;

                    double retention3 = 0.0;

                    coreCutoff = 2;
                    for (int i = 1; i <= i1; i++) {
                        retention3 += computeRetainedUsers(granularity, coreMap, g, g2, i, coreCutoff);
                    }
                    retention3 = retention3 / i1;

                    double retention6 = 0.0;

                    coreCutoff = 3;
                    for (int i = 1; i <= i1; i++) {
                        retention6 += computeRetainedUsers(granularity, coreMap, g, g2, i, coreCutoff);
                    }
                    retention6 = retention6 / i1;


                    int marketparticipants = grapht1.getVertexCount();
                    int allBuyers = buyers.size();
                    int newInvestorCount = allBuyers - previousInvestorCount;
                    String infoString = file + "\t" + g + "\t" + g2 + "\t" + marketparticipants +
                            "\t" + newInvestorCount + "\t" + txCount + "\t" + retention1 + "\t" +
                            retention3 + "\t" + retention6 + "\t" + core.getDegeneracy() + "\t" +
                            cores;
                    sb.append(infoString + "\r\n");
                    logger.info(infoString);
                }
            }

            //buyer and seller

            DescriptiveStatistics firstSellStats = new DescriptiveStatistics();
            Frequency freq = new Frequency();
            int icosale = 0;
            int buynosale = 0;
            int icoandbuyssells = 0;
            int arr[][] = new int[maxCore][maxCore];
            for (int buyer : buyers.keySet()) {
                List<Integer> bPeriods = buyers.get(buyer);
                int bDim = bPeriods.size();
                if (bDim >= maxCore) bDim = maxCore - 1;
                int sDim = 0;
                if (sellers.containsKey(buyer)) {
                    //bought and sold
                    List<Integer> sPeriods = sellers.get(buyer);

                    int firstBuy = bPeriods.get(0);
                    int firstSell = sPeriods.get(0);
                    if (firstSell < firstBuy) {
                        //selling from ico
                        icoandbuyssells++;
                    } else {
                        firstSellStats.addValue(firstSell - firstBuy);
                        freq.addValue(firstSell - firstBuy);

                    }


                    sDim = sPeriods.size();
                    if (sDim >= maxCore) sDim = maxCore - 1;


                } else {
                    //bought and did not sell
                    buynosale++;
                }
                arr[bDim][sDim]++;
            }
            //people that are selling, but we cannot find their buying records?!
            sellers.keySet().removeAll(buyers.keySet());
            for (int seller : sellers.keySet()) {
                int sp = sellers.get(seller).size();
                if (sp >= maxCore) sp = maxCore - 1;
                arr[0][sp]++;
                icosale++;
            }
            StringBuffer matrixInfo = new StringBuffer();
            for (int buyDim = 0; buyDim < maxCore; buyDim++) {
                for (int sellDim = 0; sellDim < maxCore; sellDim++) {
                    if ((buyDim + sellDim) != 0) matrixInfo.append("\t");
                    matrixInfo.append(arr[buyDim][sellDim]);
                }
            }
            double time = firstSellStats.getMean();
            long n = firstSellStats.getN();
            double med = firstSellStats.getPercentile(50);
            double variance = firstSellStats.getVariance();
            double skew = firstSellStats.getSkewness();
            double kurtosis = firstSellStats.getKurtosis();


            long tarr[] = new long[7];
            tarr[0] = freq.getCumFreq(0);
            tarr[1] = freq.getCumFreq(1);
            tarr[2] = freq.getCumFreq(3);
            tarr[3] = freq.getCumFreq(7);
            tarr[4] = freq.getCumFreq(15);
            tarr[5] = freq.getCumFreq(30);
            tarr[6] = freq.getCumFreq(60);

            bwr.write(file + "\t" + time + "\t" + icosale + "\t" + buynosale + "\t" + icoandbuyssells +
                    "\t" + tarr[0] + "\t" + tarr[1] + "\t" + tarr[2] + "\t" + tarr[3] + "\t" + tarr[4] + "\t" + tarr[5] + "\t" + tarr[6] + "\t" +
                    n + "\t" + variance + "\t" + skew + "\t" + kurtosis + "\t" + matrixInfo + "\r\n");
            bwr.flush();
            wr.write(sb.toString());
            wr.flush();
            sb = new StringBuffer();
        }

        bwr.close();
        wr.close();
    }

    private static double computeRetainedUsers(int granularity, Map<Integer, Map<Integer, Core>> coreMap, int g, int g2, int back, int coreCutoff) {
        Core core2 = coreMap.get(g).get(g2);
        Core core1 = findPrev(coreMap, g, g2, granularity, back);
        Set<Integer> k1 = core1.getCoreandHigher(coreCutoff);
        Set<Integer> intersection = new HashSet<Integer>(k1);
        Set<Integer> k2 = core2.getCoreandHigher(coreCutoff);

        intersection.retainAll(k2);
        double size = k1.size();
        double size1 = intersection.size();
        if (size1 == 0) return 0.0;
        return 100.0 * (size1 / size);
    }

    private static Core findPrev(Map<Integer, Map<Integer, Core>> coreMap, int year, int period, int granularity, int back) {
        if (granularity == Granularity.DAY) {
            if (period <= back) {
                year = year - 1;
                period = 365 - (back - period);
            } else period = period - back;
        }

        if (granularity == Granularity.MONTH) {
            if (period <= back) {
                year = year - 1;
                period = 12 - (back - period);
            } else period = period - back;

        }
        if (coreMap.containsKey(year))
            if (coreMap.get(year).containsKey(period)) {
                return coreMap.get(year).get(period);
            }
        return new Core();
    }

    private static Map<Integer, Map<Integer, DirectedSparseMultigraph>> createGraphs(int granularity, BufferedReader br) throws IOException {
        Map<Integer, Map<Integer, DirectedSparseMultigraph>> graphMap = new TreeMap<>();
        String line = "";


        while ((line = br.readLine()) != null) {
            String arr[] = line.split(" ");
            int node1 = Integer.parseInt(arr[0]);
            int node2 = Integer.parseInt(arr[1]);
            long unixTime = Long.parseLong(arr[2]);
            DateTime time = new DateTime(1000 * unixTime);
            int year = time.year().get();
            int timePeriod = computePeriod(granularity, time);
            Graph graph = locateGraph(graphMap, year, timePeriod);
            graph.addVertex(node1);
            graph.addVertex(node2);
            graph.addEdge(graph.getEdgeCount(), node1, node2);
        }
        br.close();
        return graphMap;
    }


    static Graph locateGraph(Map<Integer, Map<Integer, DirectedSparseMultigraph>> gm, int year, int tp) {
        if (!gm.containsKey(year)) gm.put(year, new TreeMap<>());
        if (!gm.get(year).containsKey(tp)) gm.get(year).put(tp, new DirectedSparseMultigraph<>());
        return gm.get(year).get(tp);
    }

    static int computePeriod(int granularity, DateTime time) {
        if (granularity == Granularity.DAY) return time.getDayOfYear();
        if (granularity == Granularity.WEEK) return time.getWeekOfWeekyear();
        if (granularity == Granularity.MONTH) return time.getMonthOfYear();
        return -1;
    }
}
