package algorithms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by cxa123230 on 6/29/2018.
 */
public class ModifiedBandDepth {
    private static final Logger logger = LoggerFactory.getLogger(ModifiedBandDepth.class);
    private static Map<Long, BigInteger> combCache = new HashMap<>();

    static BigInteger comb(final long N) {
        final int K = 2;
        //the code always called for comb(N,2), so I made k a local constant.
        if (combCache.containsKey(N)) return combCache.get(N);
        BigInteger ret = BigInteger.ONE;
        for (int k = 0; k < K; k++) {
            ret = ret.multiply(BigInteger.valueOf(N - k))
                    .divide(BigInteger.valueOf(k + 1));
        }
        combCache.put(N, ret);
        return ret;
    }

    public static double[][] transpose(double[][] m) {
        int rows = m[0].length;
        int cols = m.length;
        double[][] temp = new double[rows][cols];
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < m[0].length; j++)
                temp[j][i] = m[i][j];
        return temp;

    }


    static long whichLength(double arr[], double val, boolean equals) {
        long c = 0;
        for (double a : arr) {
            if (a < val) c++;
            else if (equals && a <= val) c++;
        }
        return c;
    }

    static long whichLength2(BigInteger arr[], BigInteger val, boolean equals) {
        long c = 0;
        for (BigInteger a : arr) {
            if (a.compareTo(val) < 0) c++;
            else if (equals && a.compareTo(val) <= 0) c++;
        }
        return c;
    }


    public double[] compute(BigInteger[][] x) {

        // n: number of observations (samples);  d: dimension of the data
        int n = x.length;
        int d = x[0].length;


        if (d < 2) {
            logger.error("Cannot compute depth on one dimensional data. ");
        }
        double[] depth = new double[n];
        BigInteger[][] matrix = x;
        int i = 100000;
        if (n > 1) {
            for (int column = 0; column < d; column++) {
                Map<BigInteger, Long> cacheG = new HashMap<BigInteger, Long>();
                Map<BigInteger, Long> cacheGE = new HashMap<BigInteger, Long>();
                BigInteger[] a1 = new BigInteger[n];
                for (int r = 0; r < n; r++) {
                    a1[r] = x[r][column];
                }
                for (int r2 = 0; r2 < n; r2++) {
                    matrix[r2][column] = a1[r2];
                }
                BigInteger[] arr1 = new BigInteger[n];
                for (int r = 0; r < n; r++) {
                    arr1[r] = matrix[r][column];
                }
                BigInteger[] arr2 = new BigInteger[n];
                for (int r = 0; r < n; r++) {
                    arr2[r] = matrix[r][column];
                }
                for (int element = 0; element < n; element++) {
                    if (element % i == i - 1) {
                        logger.info(element + " points processed");
                    }

                    BigInteger val = x[element][column];
                    long index1, index2;
                    if (cacheG.containsKey(val)) {
                        index1 = cacheG.get(val);
                    } else {
                        cacheG.put(val, whichLength2(arr1, val, false));
                        index1 = cacheG.get(val);
                    }

                    if (cacheGE.containsKey(val)) {
                        index2 = cacheGE.get(val);
                    } else {
                        cacheGE.put(val, whichLength2(arr1, val, true));
                        index2 = cacheGE.get(val);
                    }

                    long multiplicity = index2 - index1;
                    depth[element] = depth[element] + index1 * (n - (index2)) + multiplicity * (n - index2 + index1) + comb(multiplicity).doubleValue();

                }
                logger.info(n + " data points column:" + column + " Unique:" + cacheG.size());
            }
            for (int i2 = 0; i2 < depth.length; i2++) {
                depth[i2] = depth[i2] / (comb(n).multiply(new BigInteger(d + ""))).doubleValue();
            }
        }


        return (depth);

    }

    public double[] computeOld(double[][] x) {
        {
            // n: number of observations (samples);  d: dimension of the data
            int n = x.length;
            int d = x[0].length;


            if (d == 1) {
                x = transpose(x);
            }
            double[] depth = new double[n];
            double[][] orderedMatrix = x;
            if (n > 1) {
                for (int column = 0; column < d; column++) {
                    double[] a1 = new double[n];
                    for (int r = 0; r < n; r++) {
                        a1[r] = x[r][column];
                    }
                    for (int r2 = 0; r2 < n; r2++) {
                        orderedMatrix[r2][column] = a1[r2];
                    }

                    for (int element = 0; element < n; element++) {
                        int i = 100000;
                        if (element % i == i - 1) {
                            logger.info(element + " points processed");
                        }
                        double[] arr1 = new double[n];
                        for (int r = 0; r < n; r++) {
                            arr1[r] = orderedMatrix[r][column];
                        }
                        long index1 = whichLength(arr1, x[element][column], false);
                        double[] arr2 = new double[n];
                        for (int r = 0; r < n; r++) {
                            arr2[r] = orderedMatrix[r][column];
                        }
                        long index2 = whichLength(arr2, x[element][column], true);
                        long multiplicity = index2 - index1;
                        depth[element] = depth[element] + index1 * (n - (index2)) + multiplicity * (n - index2 + index1) + comb(multiplicity).doubleValue();

                    }
                }
                for (int i = 0; i < depth.length; i++) {
                    depth[i] = depth[i] / (comb(n).multiply(new BigInteger(d + ""))).doubleValue();
                }
            }
            return (depth);
        }
    }
}
