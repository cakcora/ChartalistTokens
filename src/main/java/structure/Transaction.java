package structure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by cxa123230 on 4/21/2018.
 *
 */
public class Transaction {
    private static final Logger logger = LoggerFactory.getLogger(Transaction.class);
    BigInteger gas_used;
    BigInteger gasLimit;
    long unixTime;
    private ERC20Function funct;
    private String from;
    private String to;
    private BigInteger value;

    public Transaction(String from, String to, BigInteger val, BigInteger gas_used, ERC20Function df, long unixTime) {
        this.from = from;
        this.to = to;
        this.value = val;
        this.funct = df;
        this.gas_used = gas_used;
        this.unixTime = unixTime;
    }

    @Override
    public String toString() {
        String fInfo="";
        if(funct!=null) {
            fInfo="\t"+ ERC20Function.getFunctionName(funct.getCodeString())+
                    "\t"+funct.getValue().toString();
            if(funct.hasAddress()){
                String[] addresses = funct.getAddresses();
                fInfo =fInfo+"\t"+addresses[0];
                if(addresses.length==2){
                    fInfo = fInfo+"\t"+addresses[1];
                }
                else if(addresses.length>2){
                    logger.error("Found a function with more than two parameters.");
                }
            }
        }
        return  unixTime +
                "\t" + from +
                "\t" +  to +
                "\t" + gas_used +
                "\t" +  value +
                fInfo;

    }

    public Set<String> getAllAddresses() {
        Set<String> ads = new HashSet<>();
        ads.add(from);
        ads.add(to);
        if(this.hasAFunction()&&funct.hasAddress()){
            //not all functions have addresses
            Collections.addAll(ads, funct.getAddresses());
        }
        return ads;
    }

    public boolean hasAFunction() {
        return funct != null;
    }

    BigInteger getValue(){
        return value;
    }
}
