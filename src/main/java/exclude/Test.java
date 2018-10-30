package exclude;

import params.Params;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by cxa123230 on 7/17/2018.
 */
public class Test {

    public static void main(String[] args) throws IOException {

        //scrapped page: view-source:https://coinmarketcap.com/tokens/views/all/
        BufferedReader br = new BufferedReader(new FileReader(Params.dDir + "data\\dum.txt"));
        String line = "";
        ArrayList<String> set = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            String s = "(";
            if (line.contains(s)) {
                int start = line.indexOf("(");
                int end = line.indexOf(")");
                String l = line.substring(0, start);
                set.add(l);
            }
        }
        System.out.println(set.toString());
    }
}
