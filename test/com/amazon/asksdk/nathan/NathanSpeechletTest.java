package com.amazon.asksdk.nathan;

import com.amazonaws.util.json.JSONObject;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.junit.Test;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.xml.bind.DatatypeConverter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NathanSpeechletTest {

    // to get real-time stocks:
    // https://api.intrinio.com/data_point?ticker=FB&frequency=daily&item=last_price
    @Test
    public void testGetStock() throws Exception {
        String api = "https://api.intrinio.com/prices?start_date=2017-11-17&end_date=2017-11-17&frequency=daily&identifier=AAPL";

        final String credential = "4dc21a631a4e0f039382c4018a04d8a9:8b9577f457c8e2c108c005d7244f607d";

        final Map<String, String> HEADERS = new HashMap<>();

        final String encoding = DatatypeConverter.printBase64Binary(credential.getBytes());
        HEADERS.put("Authorization", "Basic " + encoding);

        //    api = "https://api.intrinio.com/prices?start_date=2017-11-17&end_date=2017-11-17&frequency=daily&identifier=APPL";
        final String data = HttpUtil.httpGetWithHeader(api, HEADERS);

        System.out.println(data);
    }

    @Test
    public void testReplaceLast() throws Exception {
        List<String> companyList = new ArrayList<>();
        companyList.add("a");
        companyList.add("b");
        companyList.add("c");

        final String replaceWith = " and ";

        final String companyString = "a,b,c";
        System.out.println(companyString.replaceAll(",*$", " and "));


        String reverse = new StringBuffer(companyString).reverse().toString();
        reverse = reverse.replaceFirst(",", new StringBuffer(replaceWith).reverse().toString());

        final String output = new StringBuffer(reverse).reverse().toString();
        System.out.println(output);
    }

    @Test
    public void testPopulateMap() throws Exception {
        final String fileName = "/company-symbol-name.csv";
        final InputStream inputStream = getClass().getResourceAsStream(fileName);
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        String line;

        Map<String, String> mapSymbolToCompany = new HashMap<>();
        Map<String, String> mapCompanyToSymbol= new HashMap<>();


        while ((line = in.readLine()) != null) {
            final String tmp[] = line.split(",");
            mapSymbolToCompany.put(tmp[0], tmp[0]);
            mapCompanyToSymbol.put(tmp[1].toLowerCase(), tmp[0]);

        }
        inputStream.close();
        in.close();

    }

    @Test
    public void testGetStock2() throws Exception {
        final String apiKey = "HWL6W88L0VT858WB";
        String url = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=MSFT&interval=1min&apikey=";
        url += apiKey;

        final Map<String, String> headers = new HashMap<>();
        headers.put("Upgrade-Insecure-Requests", "1");
        final String data = HttpUtil.httpsGet(url, null);

        final JSONObject jsonObject = new JSONObject(data);

        System.out.println(jsonObject);
    }

    static {
        System.setProperty("javax.net.debug", "ssl,handshake");
    }

    @Test
    public void testHandshakeHostnameVerifier() throws Exception {
        final String apiKey = "HWL6W88L0VT858WB";

        String urlString = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=MSFT&interval=1min&apikey=" + apiKey;
        //     String urlString = "\"https://www.google.com";
        urlString = "https://www.alphavantage.co/";
        URL url = new URL(urlString);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setHostnameVerifier((s, sslSession) -> true);
        conn.getInputStream();
    }

}