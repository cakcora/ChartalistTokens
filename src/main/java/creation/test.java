package creation;

import params.Params;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by cxa123230 on 5/4/2018.
 */
public class test {
    public static void main(String args[]) throws Exception {
        String line, l2;
        int count = 0;
        for (int i = 48; i <= 50; i++) {
            BufferedReader br = new BufferedReader(new FileReader(Params.dir + i + ".csv"));
            while ((line = br.readLine()) != null) {
                System.out.println(i + "__" + line);
            }

        }
    }
}
