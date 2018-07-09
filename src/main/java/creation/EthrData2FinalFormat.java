package creation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.utils.Numeric;
import params.Params;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Created by cxa123230 on 5/4/2018.
 */
public class EthrData2FinalFormat {
    private static final Logger logger = LoggerFactory.getLogger(EthrData2FinalFormat.class);

    public static void main(String args[]) throws Exception {

        //Format from Vadim: from,gas,gasprice,input,to,value,gas_used,miner,number,timestamp
        //ethr format: block_hash,block_number,from,gas,gasprice,hash,input,nonce,to,transaction_index,value,v,r,s
        int startBlock = 5000001, endBlock = 5565630;
        int fIndex = 1;
        BufferedReader hbr = new BufferedReader(new FileReader(Params.ethrDir + "headers/header.txt"));
//header_data format blocknumber,difficulty,extra_data,gas_limit,gas_used,hash,logs_bloom,miner, mix_hash,nonce,number,parent_hash, receipts_root,sha3_uncles,size,state_root,timestamp,total_difficulty,transactions_root
        HashMap<Long, String> bMap = new HashMap<Long, String>();
        HashMap<Long, String> mMap = new HashMap<Long, String>();
        String line;
        hbr.readLine();
        while ((line = hbr.readLine()) != null) {
            String[] arr = line.split(",");
            Long blockNumber = Long.parseLong(arr[0]);
            String unixTime = (arr[16]);
            String miner = arr[7];
            bMap.put(blockNumber, unixTime);
            mMap.put(blockNumber, miner);
        }
        logger.info("Blocks: " + bMap.size());
        String form = "from,gas,gasprice,input,to,value,gas_used,miner,number,timestamp";
        String fileName = Params.vDir + (50 + fIndex) + ".csv";
        BufferedWriter wr = new BufferedWriter(new FileWriter(fileName));
        wr.append(form + "\r\n");
        StringBuffer sb = new StringBuffer();
        for (int block = startBlock; block < endBlock; block++) {
            String bFile = Params.ethrDir + "block_" + block + ".csv";
            Path p = Paths.get(bFile);
            if (!Files.exists(p)) continue;
            BufferedReader br = new BufferedReader(new FileReader(bFile));

            br.readLine();
            while ((line = br.readLine()) != null) {
                try {
                    String arr[] = line.split(",");
                    {//#Transaction_data format: block_hash,block_number,from,gas,gasprice,hash,input,nonce,to,transaction_index,value,v,r,s

                        String from = arr[2].trim();
                        String data = arr[6].trim();
                        String to = arr[8].trim();
                        String gas = (arr[3].trim());
                        String gas_price = (arr[4].trim());
                        String value = (arr[10]);
                        String blockNumberHex = arr[1];
                        long blocknumber = Numeric.toBigInt(blockNumberHex).longValue();
                        String timeStamp = "";
                        String miner = "";
                        if (!bMap.containsKey(blocknumber)) {
                            long b = blocknumber;
                            while (!bMap.containsKey(b)) {
                                b++;
                            }
                            timeStamp = bMap.get(b);
                            miner = "unknown";
                        } else {
                            timeStamp = bMap.get(blocknumber);
                            miner = mMap.get(blocknumber);
                        }
                        sb.append(from);
                        sb.append(",");
                        sb.append(gas);
                        sb.append(",");
                        sb.append(gas_price);
                        sb.append(",");
                        sb.append(data);
                        sb.append(",");
                        sb.append(to);
                        sb.append(",");
                        sb.append(value);
                        sb.append(",");
                        sb.append("-1");
                        sb.append(",");
                        sb.append(miner);
                        sb.append(",");
                        sb.append(blockNumberHex);
                        sb.append(",");
                        sb.append(timeStamp);
                        sb.append("\r\n");

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            if (block % 1000 == 0) {
                logger.info("processed " + block);
                wr.write(sb.toString());
                sb = new StringBuffer();
            }
            br.close();
        }
        if (sb.length() > 0)
            wr.write(sb.toString());
        wr.close();
    }

}
