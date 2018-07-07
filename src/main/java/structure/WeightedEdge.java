package structure;

import creation.TokenFiltering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

/**
 * Created by cxa123230 on 4/23/2018.
 */
public class WeightedEdge {
    private static final Logger logger = LoggerFactory.getLogger(TokenFiltering.class);
    long unixTime;
    BigInteger ethVal = new BigInteger("0");
    BigInteger tokenValue;
    int node;
    int node2;

    public WeightedEdge(long unixTime, BigInteger tokenValue, int from, int to) {
        this.unixTime = unixTime;
        if (tokenValue.compareTo(BigInteger.ZERO) < 0) {
            this.tokenValue = BigInteger.ZERO;
            logger.error("A token value of " + tokenValue.toString() + " has been passed as edge weight");
        } else this.tokenValue = tokenValue;
    }

    public WeightedEdge(long unixTime, BigInteger ethValue, BigInteger tokenValue) {
        this.unixTime = unixTime;
        this.ethVal = ethValue;
        this.tokenValue = tokenValue;
    }

    @Override
    public String toString() {
        return unixTime +
                " " + ethVal +
                " " + tokenValue
                ;
    }


    public String toTDAEdge() {
        return
                " " + unixTime +
                        " " + tokenValue
                ;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WeightedEdge twEdge = (WeightedEdge) o;

        if (unixTime != twEdge.unixTime) return false;
        if (ethVal != null ? !ethVal.equals(twEdge.ethVal) : twEdge.ethVal != null) return false;
        return tokenValue != null ? tokenValue.equals(twEdge.tokenValue) : twEdge.tokenValue == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (unixTime ^ (unixTime >>> 32));
        result = 31 * result + (ethVal != null ? ethVal.hashCode() : 0);
        result = 31 * result + (tokenValue != null ? tokenValue.hashCode() : 0);
        return result;
    }

    public BigInteger getEdgeWeight() {
        return this.tokenValue;
    }
}
