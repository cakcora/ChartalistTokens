package structure;

import org.joda.time.DateTime;
import params.Params;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

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

    public static Map<String, Contract> readTopTokens(int top) throws Exception {
        if (top <= 0) throw new Exception(top + " tokens requested. ");
        Map<String, Contract> myMap = new HashMap<String, Contract>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(Params.tokenInfoFile));
            String line = br.readLine();//read header
            int i = 0;
            while ((line = br.readLine()) != null) {
                String arr[] = line.toLowerCase().split("\t");
                Contract contract = new Contract(arr[1], arr[2], arr[3]);
                myMap.put(contract.getContractAddress(), contract);
                if (++i >= top) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return myMap;
    }

    public static Set<String> readTopTokensNames(int top) throws Exception {
        if (top <= 0) throw new Exception(top + " tokens requested. ");
        Set<String> mySet = new HashSet<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(Params.tokenInfoFile));
            String line = br.readLine();//read header
            int i = 0;
            while ((line = br.readLine()) != null) {
                String arr[] = line.toLowerCase().split("\t");
                Contract contract = new Contract(arr[1], arr[2], arr[3]);
                mySet.add(contract.getShortName());
                if (++i >= top) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mySet;
    }


    public static Map<String, Contract> readTopTokens() throws Exception {
        return readTopTokens(Integer.MAX_VALUE);
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
