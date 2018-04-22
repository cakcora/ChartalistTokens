import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cxa123230 on 4/16/2018.
 */
public class Contract {

    String contractName="undefined";
    String contractOwner;
    String contractAddress;

    private long txCount = 0;

    private List<Transaction> transactions = new ArrayList<Transaction>();
    private Map<Integer,Map<Integer,Integer>> yearMap = new HashMap<Integer, Map<Integer, Integer>>();
    public Contract(String name, String address,String owner){
        this.contractAddress = address;
        this.contractName = name;
        this.contractOwner = owner;
        //System.out.println("Found a new contract at "+ contractAddress +" created by "+owner);
    }
    public Contract(String name){
        this.contractName = name;
    }
    public String getContractOwner() {
        return contractOwner;
    }
    public String getContractAddress() {
        return contractAddress;
    }
    public boolean addTransaction(Transaction tx, long unixTime){
        DateTime time = new DateTime(1000 * unixTime);
        int year = time.year().get();
        int day = time.dayOfYear().get();
        addCount(year, day);
        if(!Params.storeContractTransactions) return true;
        return transactions.add(tx);
    }


    private void addCount(int year, int day) {
        if(Params.countPerDay) {
            if (!yearMap.containsKey(year)) yearMap.put(year, new HashMap<Integer, Integer>());
            Map<Integer, Integer> dayMap = yearMap.get(year);
            if (!dayMap.containsKey(day)) {
                dayMap.put(day, 0);
            }
            Integer dayCount = dayMap.get(day);
            dayMap.put(day, 1 + dayCount);
        }
        txCount++;
    }

    public long getTxCount() {
        return txCount;
    }
    public Map<Integer, Map<Integer, Integer>> getTransactionsByDate(){
        return yearMap;
    }
    public String getShortName() {
        return contractName;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void clearTransactions() {
        this.transactions.clear();
    }
}
