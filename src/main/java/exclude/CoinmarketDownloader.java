package exclude;

import org.apache.commons.io.IOUtils;
import params.Params;

import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by cxa123230 on 7/16/2018.
 */
public class CoinmarketDownloader {

    public static void main(String[] args) throws IOException, InterruptedException {

        //scrapped page: view-source:https://coinmarketcap.com/tokens/views/all/
        BufferedReader br = new BufferedReader(new FileReader(Params.dDir + "data\\coinmarketscrappedpage.html"));
        String line = "";
        HashSet<String> set = new HashSet<>();
        while ((line = br.readLine()) != null) {
            String s = "\"/currencies/";
            if (line.contains(s)) {
                int beginIndex = line.indexOf(s);
                String l = line.substring(beginIndex + s.length());
                l = l.substring(0, l.indexOf("/"));
                set.add(l);
            }
        }
        System.out.println(set.toString());

        for (String token : set)
            try {
                download(token);
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
                TimeUnit.SECONDS.sleep(100);
            }
    }

    private static void download(String token) throws IOException, ParseException {
        BufferedWriter wr = new BufferedWriter(new FileWriter(Params.dDir + "data/priced/crawler/" + token));
        wr.write("Date\tOpen\tHigh\tLow\tClose\tVolume\tMarket Cap\n");
        String urlBase = "https://coinmarketcap.com/currencies/" + token + "/historical-data/?start=20130428&end=20180716";
        String page = getTokenPriceData(urlBase);
        String arr[] = page.split("<tr class=\\\"text-right\\\">");
        System.out.println(token + " is processed" + arr.length + " days");

        DateFormat fmt = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        for (String s : arr) {
            if (s.contains("fiat data-format-value")) {
                String substring = s.substring(0, s.indexOf("</tr>")).trim();
//                    System.out.println(substring);
                if (substring.length() > 0) {
//<td class="text-left">Jul 14, 2018</td>
//<td data-format-fiat data-format-value="1.51891">1.52</td>
//<td data-format-fiat data-format-value="1.53574">1.54</td>
//<td data-format-fiat data-format-value="1.50229">1.50</td>
//<td data-format-fiat data-format-value="1.52779">1.53</td>
//<td data-format-market-cap data-format-value="130245.0">130,245</td>
//<td data-format-market-cap data-format-value="42857000.0">42,857,000</td>

                    String a2[] = substring.split("<td");
                    if (a2.length == 8) {
                        Date Date = fmt.parse(a2[1].substring(a2[1].indexOf(">") + 1, a2[1].indexOf("</")));
                        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

                        String Open = a2[2].substring(a2[2].indexOf(">") + 1, a2[2].indexOf("</"));
                        String High = a2[3].substring(a2[3].indexOf(">") + 1, a2[3].indexOf("</"));
                        String Low = a2[4].substring(a2[4].indexOf(">") + 1, a2[4].indexOf("</"));
                        String Close = a2[5].substring(a2[5].indexOf(">") + 1, a2[5].indexOf("</"));
                        String Volume = a2[6].substring(a2[6].indexOf(">") + 1, a2[6].indexOf("</"));
                        String MarketCap = a2[7].substring(a2[7].indexOf(">") + 1, a2[7].indexOf("</"));
                        wr.write(dateFormat.format((Date)) + "\t" + Open + "\t" + High + "\t" + Low + "\t" + Close + "\t" + Volume + "\t" + MarketCap + "\r\n");
                    } else {
                        //error?
                    }
                }
            }
        }
        wr.close();
    }

    private static String getTokenPriceData(String url) throws IOException {
        InputStream in = new URL(url).openStream();

        try {
            String pageContent = (IOUtils.toString(in));
            return pageContent;
        } finally {
            IOUtils.closeQuietly(in);
        }
    }


}
