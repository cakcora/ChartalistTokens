package structure;

import java.math.BigInteger;

/**
 * Created by cxa123230 on 4/23/2018.
 */
public class TWEdge {
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


    public boolean hasTransferredValue() {
        return !tokenValue.equals(BigInteger.ZERO);

    }
}
