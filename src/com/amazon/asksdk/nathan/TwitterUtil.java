package com.amazon.asksdk.nathan;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.*;
import org.json.JSONArray;
import twitter4j.*;
import twitter4j.JSONObject;
import twitter4j.TimeZone;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

import java.io.*;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by qcluu on 11/18/17.
 */
public class TwitterUtil {
    private static final String timezone = "EST5EDT";
    private static final java.util.TimeZone timeZoneId = java.util.TimeZone.getTimeZone(timezone);
    static private final String WOEID = "23424977";// "2514815";

    final static String TwitterTokenURL = "https://api.twitter.com/oauth2/token";

    final static String TwitterStreamURL = "https://api.twitter.com/1.1/statuses/user_timeline.json?count=5&screen_name=";

    final String consumerKey = "hXGuHUhiYDyrwGxXVHqDCZjux";
    final String consumerSecret = "pxfwzbvrKI9rhApeXNXDOyAvK4I0aJfjI8NHZAgP7hxasS43Eh";
    final String accessToken = "15693417-ODOXxuEsij4eqV1t3SKtUs1odaSHoIWrqL2ctO1ds";
    final String accessTokenSecret = "5XuBshdU2m5oIkKTm6kGFjYQOYrWJEUB9b9VCQj8MTZ3l";
    private Authenticated auth;

    static public String getDateSpeech(final String dateString, final String format) throws Exception{
        SimpleDateFormat sf = new SimpleDateFormat(format);

        sf.setTimeZone(timeZoneId);
        sf.setLenient(true);
        final Date date2 = sf.parse(dateString);


        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy .");
        simpleDateFormat.setTimeZone(timeZoneId);

        return simpleDateFormat.format(date2);
    }

    public TwitterUtil() throws Exception{
            authenticate();
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
        } catch (ClientProtocolException ex1) {
        } catch (IOException ex2) {
        }
        return sb.toString();
    }

    public String getTrendTopics() throws Exception {
        HttpGet httpGet = new HttpGet("https://api.twitter.com/1.1/trends/place.json?id="+WOEID);

        // construct a normal HTTPS request and include an Authorization
        // header with the value of Bearer <>
        httpGet.setHeader("Authorization", "Bearer " + auth.access_token);
        httpGet.setHeader("Content-Type", "application/json");
        // update the results with the body of the response
        final String results = getResponseBody(httpGet);

        return results;
    }

    private Authenticated jsonToAuthenticated(final String jsonData) throws Exception {
        org.json.JSONObject jsonObject = new org.json.JSONObject(jsonData);
        final String token_type = jsonObject.getString("token_type");
        final String access_token = jsonObject.getString("access_token");
        Authenticated authenticated = new Authenticated(token_type, access_token);
        return authenticated;
    }

    private String searchTweet(final String from) throws Exception {
        String query = "-filter:retweets%20AND%20-filter:replies";
        if (StringUtils.isEmpty(from)) {

        } else {
            query += "%20AND%20from:" + from;
        }

        HttpGet httpGet = new HttpGet("https://api.twitter.com/1.1/search/tweets.json?count=4&q=" + query+"&tweet_mode=extended");

        // construct a normal HTTPS request and include an Authorization
        // header with the value of Bearer <>
        httpGet.setHeader("Authorization", "Bearer " + auth.access_token);
        httpGet.setHeader("Content-Type", "application/json");
        // update the results with the body of the response
        final String results = getResponseBody(httpGet);

        return results;
    }

    public static Date getTwitterDate(String date) throws ParseException {

        final String TWITTER="EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(TWITTER);
        final String timezone = "EST5EDT";
        final java.util.TimeZone timeZoneId = java.util.TimeZone.getTimeZone(timezone);
        sf.setTimeZone(timeZoneId);
        sf.setLenient(true);
        return sf.parse(date);
    }

    public static String getTwitterDateSpeech(String date) throws ParseException {

        final String TWITTER="EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(TWITTER);

        sf.setTimeZone(timeZoneId);
        sf.setLenient(true);
        final Date date2 = sf.parse(date);


        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy . 'at' h a");
        simpleDateFormat.setTimeZone(timeZoneId);

        return simpleDateFormat.format(date2);
    }

    public JSONArray getTweetsByUser(final String user) throws Exception {
        final String searchResult = searchTweet(user);
        final org.json.JSONObject jsonObject = new org.json.JSONObject(searchResult);
        final org.json.JSONArray jsonArray = jsonObject.getJSONArray("statuses");

        return jsonArray;
    }

    public static void example() throws TwitterException {
        final String consumerKey = "hXGuHUhiYDyrwGxXVHqDCZjux";
        final String consumerSecret = "pxfwzbvrKI9rhApeXNXDOyAvK4I0aJfjI8NHZAgP7hxasS43Eh";
        final String accessToken = "15693417-ODOXxuEsij4eqV1t3SKtUs1odaSHoIWrqL2ctO1ds";
        final String accessTokenSecret = "5XuBshdU2m5oIkKTm6kGFjYQOYrWJEUB9b9VCQj8MTZ3l";

        System.setProperty("twitter4j.oauth.consumerKey", consumerKey);
        System.setProperty("twitter4j.oauth.consumerSecret", consumerSecret);
        System.setProperty("twitter4j.oauth.accessToken", accessToken);
        System.setProperty("twitter4j.oauth.accessTokenSecret", accessTokenSecret);
        System.setProperty("twitter4j.jsonStoreEnabled","true");

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(consumerKey); // INPUT CREDENTIALS HERE!!
        cb.setOAuthConsumerSecret(consumerSecret);
        cb.setOAuthAccessToken(accessToken);
        cb.setOAuthAccessTokenSecret(accessTokenSecret);
        cb.setJSONStoreEnabled(true);

        Twitter twitter = new TwitterFactory(cb.build()).getInstance();
        Query query = new Query("test");
        QueryResult result = twitter.search(query);
        for (Status tweet : result.getTweets()) {
            System.out.println(tweet.getUser() + ":" + tweet.getText());
            String json = DataObjectFactory.getRawJSON(tweet);
            System.out.println(json);
        }
    }
}
