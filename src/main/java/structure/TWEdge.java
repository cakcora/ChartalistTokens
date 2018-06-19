package structure;

import creation.TokenFiltering;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

/**
 * Created by cxa123230 on 4/23/2018.
 */
public class TWEdge {
    private static final Logger logger = LoggerFactory.getLogger(TokenFiltering.class);
    long unixTime;
    BigInteger ethVal;
    String tokenName = "NONE";
    BigInteger tokenValue = new BigInteger("0");
    int from;
    int to;
    public TWEdge(long unixTime, int from, int to, BigInteger ethValue) {
        this.unixTime = unixTime;
        this.ethVal = ethValue;
        this.from = from;
        this.to = to;
    }

    public TWEdge(long unixTime, int from, int to, BigInteger ethValue, String tokenName, BigInteger tokenValue) {
        this.unixTime = unixTime;
        this.ethVal = ethValue;
        this.from = from;
        this.to = to;
        this.tokenName = tokenName;
        this.tokenValue = tokenValue;
    }

    @Override
    public String toString() {
        return from +
                " " + to +
                " " + unixTime +
                " " + ethVal +
                " " + tokenName +
                " " + tokenValue
                ;
    }

    public String toTemporalMotifString() {
        return from +
                " " + to +
                " " + unixTime
                ;
    }

    public String toTDAEdge() {
        return from +
                " " + to +
                " " + unixTime +
                " " + tokenValue
                ;
    }


    public boolean hasTransferredTokenValue() {
        return !tokenValue.equals(BigInteger.ZERO);

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TWEdge twEdge = (TWEdge) o;

        if (unixTime != twEdge.unixTime) return false;
        if (from != twEdge.from) return false;
        if (to != twEdge.to) return false;
        if (ethVal != null ? !ethVal.equals(twEdge.ethVal) : twEdge.ethVal != null) return false;
        if (tokenName != null ? !tokenName.equals(twEdge.tokenName) : twEdge.tokenName != null) return false;
        return tokenValue != null ? tokenValue.equals(twEdge.tokenValue) : twEdge.tokenValue == null;

    }

    @Override
    public int hashCode() {
        int result = (int) (unixTime ^ (unixTime >>> 32));
        result = 31 * result + (ethVal != null ? ethVal.hashCode() : 0);
        result = 31 * result + (tokenName != null ? tokenName.hashCode() : 0);
        result = 31 * result + (tokenValue != null ? tokenValue.hashCode() : 0);
        result = 31 * result + from;
        result = 31 * result + to;
        return result;
    }
}
