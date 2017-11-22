package com.amazon.asksdk.nathan;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.junit.Test;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import org.json.JSONObject;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.CertificateException;
import javax.security.cert.X509Certificate;
import java.io.*;
import java.net.URLEncoder;
import java.util.Base64;
import java.util.Date;

import static org.junit.Assert.*;

public class TwitterUtilTest {
    static private final String WOEID = "23424977";// "2514815";
    /*
    [{"name":"Washington","placeType":{"code":7,"name":"Town"},"url":"http:\/\/where.yahooapis.com\/v1\/place\/2514815","parentid":23424977,"country":"United States","woeid":2514815,"countryCode":"US"}]
     */
    final static String TwitterTokenURL = "https://api.twitter.com/oauth2/token";

    final static String TwitterStreamURL = "https://api.twitter.com/1.1/statuses/user_timeline.json?count=5&screen_name=";

    final String consumerKey = "hXGuHUhiYDyrwGxXVHqDCZjux";
    final String consumerSecret = "pxfwzbvrKI9rhApeXNXDOyAvK4I0aJfjI8NHZAgP7hxasS43Eh";
    final String accessToken = "15693417-ODOXxuEsij4eqV1t3SKtUs1odaSHoIWrqL2ctO1ds";
    final String accessTokenSecret = "5XuBshdU2m5oIkKTm6kGFjYQOYrWJEUB9b9VCQj8MTZ3l";
    private Authenticated auth;

    @Test
    public void testHello() throws Exception {
        System.out.println("hello");
    }

    private String cleanText(final String textRaw) throws Exception {
        int index = textRaw.indexOf("https:");
        if (index < 0) {
            index = textRaw.indexOf("http:");
        }
        String text = textRaw;
        if (index > 0) {
            text = textRaw.substring(0, index);
        }
        return text;
    }

    @Test
    public void testGetTwitterDate() throws Exception {

        final String twitterDate = "Mon Nov 20 11:55:57 +0000 2017";
        final Date date = TwitterUtil.getTwitterDate(twitterDate);

        System.out.println(date);
    }

    @Test
    public void testGetTwitterDateSpeech() throws Exception {

        final String twitterDate = "Mon Nov 20 11:55:57 +0000 2017";
        final String dateSpeech = TwitterUtil.getTwitterDateSpeech(twitterDate);

        System.out.println(dateSpeech);
    }

    @Test
    public void testCleanText() throws Exception {
        final String textRaw = "Amazon Prime heads to Middle Earth. https://t.co/QowUmf8t3S";
        final String text = cleanText(textRaw);

        System.out.println(text);
    }

    @Test
    public void testGetTweetsByUser() throws Exception {
        final String userName = "realDonaldTrump";
        //    final String userName = "JeffBezos";
        TwitterUtil twitterUtil = new TwitterUtil();
        final org.json.JSONArray jsonArray = twitterUtil.getTweetsByUser(userName);

        final int size = jsonArray.length();
        for (int i = 0; i < size; i++) {
            final JSONObject jsonItem = jsonArray.getJSONObject(i);

            final String textRaw = jsonItem.getString("full_text");
            final String text = cleanText(textRaw);
            final String createdAt = jsonItem.getString("created_at");
            System.out.println(createdAt + ": text=" + text);

        }
    }

    @Test
    public void testGetTweets() throws Exception {
        final String userName = "";
        TwitterUtil twitterUtil = new TwitterUtil();
        final org.json.JSONArray jsonArray = twitterUtil.getTweetsByUser(userName);

        final int size = jsonArray.length();
        for (int i = 0; i < size; i++) {
            final JSONObject jsonItem = jsonArray.getJSONObject(i);

            final String text = jsonItem.getString("full_text");
            final String createdAt = jsonItem.getString("created_at");
            System.out.println(createdAt + ": text=" + text);

        }
    }

    @Test
    public void testExample() throws Exception {


        System.setProperty("twitter4j.oauth.consumerKey", consumerKey);
        System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret);
        System.setProperty("twitter4j.oauth.accessToken", accessToken);
        System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret);
        System.setProperty("twitter4j.jsonStoreEnabled", "true");

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(consumerKey); // INPUT CREDENTIALS HERE!!
        cb.setOAuthConsumerSecret(consumerSecret);
        cb.setOAuthAccessToken(accessToken);
        cb.setOAuthAccessTokenSecret(accessTokenSecret);
        cb.setJSONStoreEnabled(true);

        Twitter twitter = new TwitterFactory(cb.build()).getInstance();

        Query query = new Query("from:realDonaldTrump");


        QueryResult result = twitter.search(query);


        for (Status tweet : result.getTweets()) {
            final String userName = tweet.getUser().getName();
            final boolean retweet = tweet.isRetweet();
            if (retweet) {

            } else {

                final String tweetText = tweet.getText();
                System.out.println(tweet.getUser().getName() + ":" + tweetText);

            }
            //    String json = DataObjectFactory.getRawJSON(tweet);
            //    System.out.println(json);
        }

/*
         twitter = TwitterFactory.getSingleton();
        query = new Query("from:realDonaldTrump");
        result = twitter.search(query);
        for (Status status : result.getTweets()) {
            System.out.println("@" + status.getUser().getScreenName() + ":" + status.getText());
        }
        */

    }


    @Test
    public void testGetTwitterStream() throws Exception {
        final String userName = "realDonaldTrump";
        //    final String results = getTwitterStream(userName);

        authenticate();
        final String searchResult = searchTweet(userName);
        final JSONObject jsonObject = new JSONObject(searchResult);
        final org.json.JSONArray jsonArray = jsonObject.getJSONArray("statuses");

        final int size = jsonArray.length();
        for (int i = 0; i < size; i++) {
            final JSONObject jsonItem = jsonArray.getJSONObject(i);
            try {
                final JSONObject rtStatus = jsonItem.getJSONObject("retweeted_status");
                System.out.println("rtStatus=" + rtStatus);
            } catch (Exception e) {
                final String text = jsonItem.getString("full_text");
                System.out.println("text=" + text);
            }
        }

        /*

        final org.json.JSONArray jsonArray = new org.json.JSONArray(results);

        System.out.println("# tweets: " + jsonArray.length());
        final int size = jsonArray.length();
        for (int i = 0; i < size; i++) {
            final org.json.JSONObject jsonObject = jsonArray.getJSONObject(i);
            final String text = jsonObject.getString("text");

            System.out.println(text);

            final String id = jsonObject.getString("id_str");
            final String details = getTweetDetails(id);
            System.out.println("id=" + id + ", details=" + details);
        }*/
    }

    @Test
    public void testGetTrend() throws Exception {
        final String userName = "realDonaldTrump";
        //    final String results = getTwitterStream(userName);

        authenticate();
        final String searchResult = getTrend();
        final JSONObject jsonObject = new JSONObject(searchResult);
        final org.json.JSONArray jsonArray = jsonObject.getJSONArray("statuses");

        final int size = jsonArray.length();
        for (int i = 0; i < size; i++) {
            final JSONObject jsonItem = jsonArray.getJSONObject(i);
            try {
                final JSONObject rtStatus = jsonItem.getJSONObject("retweeted_status");
                System.out.println("rtStatus=" + rtStatus);
            } catch (Exception e) {
                final String text = jsonItem.getString("full_text");
                System.out.println("text=" + text);
            }
        }
    }


    @Test
    public void testGetTrendDetails() throws Exception {
        //    final String results = getTwitterStream(userName);

        authenticate();
        final String searchResult = getTrendTopics();
        final JSONObject jsonObject = new JSONObject(searchResult);
        final org.json.JSONArray jsonArray = jsonObject.getJSONArray("statuses");

        final int size = jsonArray.length();
        for (int i = 0; i < size; i++) {
            final JSONObject jsonItem = jsonArray.getJSONObject(i);
            try {
                final JSONObject rtStatus = jsonItem.getJSONObject("retweeted_status");
                System.out.println("rtStatus=" + rtStatus);
            } catch (Exception e) {
                final String text = jsonItem.getString("full_text");
                System.out.println("text=" + text);
            }
        }
    }

    private String getTweetDetails(final String id) throws Exception {
        HttpGet httpGet = new HttpGet("https://api.twitter.com/1.1/statuses/show.json?id=" + id);

        // construct a normal HTTPS request and include an Authorization
        // header with the value of Bearer <>
        httpGet.setHeader("Authorization", "Bearer " + auth.access_token);
        httpGet.setHeader("Content-Type", "application/json");
        // update the results with the body of the response
        final String results = getResponseBody(httpGet);

        return results;
    }


    private String searchTweet(final String from) throws Exception {
        final String query = "from:" + from + "%20AND%20-filter:retweets%20AND%20-filter:replies";
        HttpGet httpGet = new HttpGet("https://api.twitter.com/1.1/search/tweets.json?count=4&q=" + query + "&tweet_mode=extended");

        // construct a normal HTTPS request and include an Authorization
        // header with the value of Bearer <>
        httpGet.setHeader("Authorization", "Bearer " + auth.access_token);
        httpGet.setHeader("Content-Type", "application/json");
        // update the results with the body of the response
        final String results = getResponseBody(httpGet);

        return results;
    }

    private String getTrend() throws Exception {

        // Ashburn
        //    final String lat = "39.04375670";
        //    final String lon = "-77.48744160";

        final String lat = "38.9071923";
        final String lon = "-77.03687070000001";
        // Wash DC:

        HttpGet httpGet = new HttpGet("https://api.twitter.com/1.1/trends/closest.json?lat=" + lat + "&long=" + lon);

        // construct a normal HTTPS request and include an Authorization
        // header with the value of Bearer <>
        httpGet.setHeader("Authorization", "Bearer " + auth.access_token);
        httpGet.setHeader("Content-Type", "application/json");
        // update the results with the body of the response
        final String results = getResponseBody(httpGet);

        return results;
    }

    @Test
    public void testGetSock() throws Exception {

        final String apiKey = "HWL6W88L0VT858WB";
        String url = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=MSFT&interval=1min&apikey=";
        url += apiKey;

        HttpGet httpGet = new HttpGet(url);

        final String results = getResponseBody(httpGet);
        System.out.println(results);
    }

    private String getTrendTopics() throws Exception {
        HttpGet httpGet = new HttpGet("https://api.twitter.com/1.1/trends/place.json?id=" + WOEID);

        // construct a normal HTTPS request and include an Authorization
        // header with the value of Bearer <>
        httpGet.setHeader("Authorization", "Bearer " + auth.access_token);
        httpGet.setHeader("Content-Type", "application/json");
        // update the results with the body of the response
        final String results = getResponseBody(httpGet);

        return results;
    }

    private void authenticate() throws Exception {
        // URL encode the consumer key and secret
        String urlApiKey = URLEncoder.encode(consumerKey, "UTF-8");
        String urlApiSecret = URLEncoder.encode(consumerSecret, "UTF-8");

        // Concatenate the encoded consumer key, a colon character, and the
        // encoded consumer secret
        String combined = urlApiKey + ":" + urlApiSecret;

        // Base64 encode the string
        String base64Encoded = Base64.getEncoder().encodeToString(combined.getBytes());

        // Step 2: Obtain a bearer token
        HttpPost httpPost = new HttpPost(TwitterTokenURL);
        httpPost.setHeader("Authorization", "Basic " + base64Encoded);
        httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        httpPost.setEntity(new StringEntity("grant_type=client_credentials"));
        final String rawAuthorization = getResponseBody(httpPost);

        auth = jsonToAuthenticated(rawAuthorization);
    }

    private String getTwitterStream(String screenName) throws Exception {
        String results = null;

        // Step 1: Encode consumer key and secret
        try {
            // URL encode the consumer key and secret
            String urlApiKey = URLEncoder.encode(consumerKey, "UTF-8");
            String urlApiSecret = URLEncoder.encode(consumerSecret, "UTF-8");

            // Concatenate the encoded consumer key, a colon character, and the
            // encoded consumer secret
            String combined = urlApiKey + ":" + urlApiSecret;

            // Base64 encode the string
            String base64Encoded = Base64.getEncoder().encodeToString(combined.getBytes());

            // Step 2: Obtain a bearer token
            HttpPost httpPost = new HttpPost(TwitterTokenURL);
            httpPost.setHeader("Authorization", "Basic " + base64Encoded);
            httpPost.setHeader("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
            httpPost.setEntity(new StringEntity("grant_type=client_credentials"));
            final String rawAuthorization = getResponseBody(httpPost);

            auth = jsonToAuthenticated(rawAuthorization);

            // Applications should verify that the value associated with the
            // token_type key of the returned object is bearer
            if (auth != null && auth.token_type.equals("bearer")) {

                // Step 3: Authenticate API requests with bearer token
                HttpGet httpGet = new HttpGet(TwitterStreamURL + screenName);

                // construct a normal HTTPS request and include an Authorization
                // header with the value of Bearer <>
                httpGet.setHeader("Authorization", "Bearer " + auth.access_token);
                httpGet.setHeader("Content-Type", "application/json");
                // update the results with the body of the response
                results = getResponseBody(httpGet);
            }
        } catch (UnsupportedEncodingException ex) {
        } catch (IllegalStateException ex1) {
        }
        return results;
    }

    private Authenticated jsonToAuthenticated(final String jsonData) throws Exception {
        JSONObject jsonObject = new JSONObject(jsonData);
        final String token_type = jsonObject.getString("token_type");
        final String access_token = jsonObject.getString("access_token");
        Authenticated authenticated = new Authenticated(token_type, access_token);
        return authenticated;
    }


    private String getResponseBody(HttpRequestBase request) throws Exception {
        StringBuilder sb = new StringBuilder();
        try {

            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
            org.apache.http.HttpResponse response = httpClient.execute(request);

            int statusCode = response.getStatusLine().getStatusCode();
            String reason = response.getStatusLine().getReasonPhrase();

            if (statusCode == 200) {

                HttpEntity entity = response.getEntity();
                InputStream inputStream = entity.getContent();

                BufferedReader bReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                String line = null;
                while ((line = bReader.readLine()) != null) {
                    sb.append(line);
                }
            } else {
                sb.append(reason);
            }
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        } catch (ClientProtocolException ex1) {
            ex1.printStackTrace();
        } catch (IOException ex2) {
            ex2.printStackTrace();
        }
        return sb.toString();
    }

}