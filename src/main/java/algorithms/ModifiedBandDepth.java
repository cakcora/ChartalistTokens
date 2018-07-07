package algorithms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

/**
 * Created by cxa123230 on 6/29/2018.
 */
public class ModifiedBandDepth {
    private static final Logger logger = LoggerFactory.getLogger(ModifiedBandDepth.class);

    static BigInteger comb(final int N, final int K) {
        BigInteger ret = BigInteger.ONE;
        for (int k = 0; k < K; k++) {
            ret = ret.multiply(BigInteger.valueOf(N - k))
                    .divide(BigInteger.valueOf(k + 1));
        }
        return ret;
    }

    public static double[][] transpose(double[][] m) {
        double[][] temp = new double[m[0].length][m.length];
        for (int i = 0; i < m.length; i++)
            for (int j = 0; j < m[0].length; j++)
                temp[j][i] = m[i][j];
        return temp;
    }

    static double[] sort(double[] arr) {
        return arr;
    }

    static int whichLength(double arr[], double val, boolean equals) {
        int c = 0;
        for (double a : arr) {
            if (a < val) c++;
            else if (equals && a <= val) c++;
        }
        return c;
    }

    public double[] computeModifiedBandDepth(double[][] x) {
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
                for (int columns = 0; columns < d; columns++) {
                    double[] a1 = new double[n];
                    for (int r = 0; r < n; r++) {
                        a1[r] = x[r][columns];
                    }
                    double sorted[] = sort(a1);
                    for (int r2 = 0; r2 < n; r2++) {
                        orderedMatrix[r2][columns] = sorted[r2];
                    }

                    for (int element = 0; element < n; element++) {
                        int i = 50000;
                        if (element % i == i - 1) {
                            logger.info(element + " points processed");
                        }
                        double[] arr1 = new double[n];
                        for (int r = 0; r < n; r++) {
                            arr1[r] = orderedMatrix[r][columns];
                        }
                        int index1 = whichLength(arr1, x[element][columns], false);
                        double[] arr2 = new double[n];
                        for (int r = 0; r < n; r++) {
                            arr2[r] = orderedMatrix[r][columns];
                        }
                        int index2 = whichLength(arr2, x[element][columns], true);
                        int multiplicity = index2 - index1;
                        depth[element] = depth[element] + index1 * (n - (index2)) + multiplicity * (n - index2 + index1) + comb(multiplicity, 2).doubleValue();

                    }
                }
                for (int i = 0; i < depth.length; i++) {
                    depth[i] = depth[i] / (comb(n, 2).multiply(new BigInteger(d + ""))).doubleValue();
                }
            }
            return (depth);
        }
    }
}
