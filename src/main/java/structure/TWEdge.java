package structure;

import java.math.BigInteger;

/**
 * Created by cxa123230 on 4/23/2018.
 */
public class TWEdge {
    long unixTime;
    BigInteger ethVal;
    String tokenName;
    BigInteger tokenValue;
    String from;
    String to;


    public TWEdge(long unixTime, String from, String to, BigInteger ethValue) {
        this.unixTime = unixTime;
        this.ethVal = ethValue;
        this.from = from;
        this.to = to;
    }

    public TWEdge(long unixTime, String from, String to, BigInteger ethValue, String tokenName, BigInteger tokenValue) {
        this.unixTime = unixTime;
        this.ethVal = ethValue;
        this.from = from;
        this.to = to;
        this.tokenName = tokenName;
        this.tokenValue = tokenValue;
    }

    @Override
    public String toString() {
        return unixTime +
                "," + from +
                "," + to +
                "," + ethVal +
                "," + tokenName +
                "," + tokenValue
                ;
    }


}
