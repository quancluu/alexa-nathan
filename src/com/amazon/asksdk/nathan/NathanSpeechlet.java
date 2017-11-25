/**
 Copyright 2014-2015 Amazon.com, Inc. or its affiliates. All Rights Reserved.

 Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance with the License. A copy of the License is located at

 http://aws.amazon.com/apache2.0/

 or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.amazon.asksdk.nathan;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import com.amazon.speech.ui.*;
import com.amazonaws.util.json.JSONArray;
import com.amazonaws.util.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazon.speech.slu.Intent;
import com.amazon.speech.slu.Slot;
import com.amazon.speech.speechlet.IntentRequest;
import com.amazon.speech.speechlet.LaunchRequest;
import com.amazon.speech.speechlet.Session;
import com.amazon.speech.speechlet.SessionEndedRequest;
import com.amazon.speech.speechlet.SessionStartedRequest;
import com.amazon.speech.speechlet.SpeechletV2;
import com.amazon.speech.speechlet.SpeechletResponse;
import com.amazon.speech.json.SpeechletRequestEnvelope;
import twitter4j.User;

import javax.xml.bind.DatatypeConverter;

/**
 * This sample shows how to create a simple speechlet for handling intent requests and managing
 * session interactions.
 */
public class NathanSpeechlet implements SpeechletV2 {
    private static final Logger log = LoggerFactory.getLogger(NathanSpeechlet.class);
    static private Map<String, String> mapFieldToSpeech = new HashMap<>();

    private static final String COLOR_KEY = "COLOR";
    private static final String COLOR_SLOT = "Color";

    // private Map<String, String> mapSymbolToCompany = new HashMap<>();

    static private final String credential = "4dc21a631a4e0f039382c4018a04d8a9:8b9577f457c8e2c108c005d7244f607d";

    static private final Map<String, String> HEADERS = new HashMap<>();

    static private final Map<String, String> mapSymbolToCompany = new HashMap<>();
    static private final Map<String, String> mapCompanyToSymbol = new HashMap<>();
    static private final Map<String, String> mapCompanyNames = new HashMap<>();


    static {
        /*
        mapSymbolToCompany.put("fb", "Facebook");
        mapSymbolToCompany.put("aapl", "Apple");
        mapSymbolToCompany.put("msft", "Microsoft");
        mapSymbolToCompany.put("googl", "Google");
        mapSymbolToCompany.put("ua", "Under Armour");
        mapSymbolToCompany.put("twtr", "Twitter");
        */
        mapCompanyNames.put("google", "alphabet");
        mapCompanyNames.put("alphabet", "google");

        final String encoding = DatatypeConverter.printBase64Binary(credential.getBytes());

        HEADERS.put("Authorization", "Basic " + encoding);

        mapFieldToSpeech.put("usercity", "city");
        mapFieldToSpeech.put("userstate", "state");
        mapFieldToSpeech.put("plannedsales", "sales");
        mapFieldToSpeech.put("group_name", "group name");
        mapFieldToSpeech.put("*", "transactions");
        mapFieldToSpeech.put("sale_date", "sale date");
        mapFieldToSpeech.put("hasgtag_str", "trend");
        mapFieldToSpeech.put("usergender", "gender");

        populateStockMap();
    }

    static private void populateStockMap() {
        try {
            final String fileName = "/company-symbol-name.csv";
            final InputStream inputStream = new NathanSpeechlet().getClass().getResourceAsStream(fileName);
            BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
            String line;


            while ((line = in.readLine()) != null) {
                final String tmp[] = line.split(",");
                final String companyName = tmp[1].replace("Corporation", "")
                        .replace("Incorporated", "")
                        .toLowerCase().replace("inc.", "").trim();
                mapSymbolToCompany.put(tmp[0].toLowerCase().trim(), companyName);
                mapCompanyToSymbol.put(companyName, tmp[0].toLowerCase().trim());
            }
            inputStream.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getSymbolByCompanyName(final String companyName) {
        String symbol = mapCompanyToSymbol.get(companyName.toLowerCase());
        if (StringUtils.isEmpty(symbol) == false) {
            return symbol;
        }

        final String companyName2 = mapCompanyNames.get(companyName.toLowerCase());

        return mapCompanyToSymbol.get(companyName2);
    }

    @Override
    public void onSessionStarted(SpeechletRequestEnvelope<SessionStartedRequest> requestEnvelope) {
        log.info("onSessionStarted requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
        // any initialization logic goes here
    }

    @Override
    public SpeechletResponse onLaunch(SpeechletRequestEnvelope<LaunchRequest> requestEnvelope) {
        log.info("onLaunch requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
        return getWelcomeResponse();
    }

    @Override
    public SpeechletResponse onIntent(SpeechletRequestEnvelope<IntentRequest> requestEnvelope) {
        IntentRequest request = requestEnvelope.getRequest();
        Session session = requestEnvelope.getSession();

        // Get intent from the request object.

        Intent intent = request.getIntent();
        String intentName = (intent != null) ? intent.getName() : null;

        // Note: If the session is started with an intent, no welcome message will be rendered;
        // rather, the intent specific response will be returned.
        log.info("onIntent intentName={}, requestId={}, sessionId={}", intentName, request.getRequestId(), session);

        /*
        final String fileName = "/Dashboard_Template_BAR.json";

        log.info("data=" + MiscUtil.ReadDataFromFileName(fileName));
        */

        if ("MyStockIntent".equals(intentName)) {
            return doMyStockIntent(intent, session);
        } else if ("DoneIntent".equals(intentName)) {
            return doMyStockIntent(intent, session);
        } else if ("CompanyIntent".equals(intentName)) {
            return doCompanyIntent(intent, session);
        } else if ("StockIntent".equals(intentName)) {
            return doStockIntent(intent, session);
        } else if ("TrumpTweetIntent".equals(intentName)) {
            return doTrumpIntent(intent, session);
        } else if ("TrendIntent".equals(intentName)) {
            return doTrendIntent(intent, session);
        } else if ("WelcomeIntent".equals(intentName)) {
            return doWelcome(intent, session);
        }

        if ("MyColorIsIntent".equals(intentName)) {
            return setColorInSession(intent, session);
        } else if ("WhatsMyColorIntent".equals(intentName)) {
            return getColorFromSession(intent, session);
        } else if ("MyStockIntent".equals(intentName)) {
            return doMyStockIntent(intent, session);
        } else {
            String errorSpeech = "This is unsupported.  Please try something else.";
            return getSpeechletResponse(errorSpeech, errorSpeech, true);
        }
    }

    @Override
    public void onSessionEnded(SpeechletRequestEnvelope<SessionEndedRequest> requestEnvelope) {
        log.info("onSessionEnded requestId={}, sessionId={}", requestEnvelope.getRequest().getRequestId(),
                requestEnvelope.getSession().getSessionId());
        // any cleanup logic goes here
    }

    /**
     * Creates and returns a {@code SpeechletResponse} with a welcome message.
     *
     * @return SpeechletResponse spoken and visual welcome message
     */
    private SpeechletResponse getWelcomeResponse() {
        // Create the welcome message.
        String speechText =
                "Welcome to the Alexa Skills Kit sample. Please tell me your favorite color by "
                        + "saying, my favorite color is red";
        String repromptText =
                "Please tell me your favorite color by saying, my favorite color is red";

        return getSpeechletResponse(speechText, repromptText, true);
    }

    /**
     * Creates a {@code SpeechletResponse} for the intent and stores the extracted color in the
     * Session.
     *
     * @param intent intent for the request
     * @return SpeechletResponse spoken and visual response the given intent
     */
    private SpeechletResponse setColorInSession(final Intent intent, final Session session) {
        // Get the slots from the intent.
        Map<String, Slot> slots = intent.getSlots();

        // Get the color slot from the list of slots.
        Slot favoriteColorSlot = slots.get(COLOR_SLOT);
        String speechText, repromptText;

        // Check for favorite color and create output to user.
        if (favoriteColorSlot != null) {
            // Store the user's favorite color in the Session and create response.
            String favoriteColor = favoriteColorSlot.getValue();
            session.setAttribute(COLOR_KEY, favoriteColor);
            speechText =
                    String.format("I now know that your favorite color is %s. You can ask me your "
                            + "favorite color by saying, what's my favorite color?", favoriteColor);
            repromptText =
                    "You can ask me your favorite color by saying, what's my favorite color?";

        } else {
            // Render an error since we don't know what the users favorite color is.
            speechText = "I'm not sure what your favorite color is, please try again";
            repromptText =
                    "I'm not sure what your favorite color is. You can tell me your favorite "
                            + "color by saying, my favorite color is red";
        }

        return getSpeechletResponse(speechText, repromptText, true);
    }

    /**
     * Creates a {@code SpeechletResponse} for the intent and get the user's favorite color from the
     * Session.
     *
     * @param intent intent for the request
     * @return SpeechletResponse spoken and visual response for the intent
     */
    private SpeechletResponse getColorFromSession(final Intent intent, final Session session) {
        String speechText;
        boolean isAskResponse = false;

        // Get the user's favorite color from the session.
        String favoriteColor = (String) session.getAttribute(COLOR_KEY);

        // Check to make sure user's favorite color is set in the session.
        if (StringUtils.isNotEmpty(favoriteColor)) {
            speechText = String.format("Your favorite color is %s. Goodbye.", favoriteColor);
        } else {
            // Since the user's favorite color is not set render an error message.
            speechText =
                    "I'm not sure what your favorite color is. You can say, my favorite color is "
                            + "red";
            isAskResponse = true;
        }

        return getSpeechletResponse(speechText, speechText, isAskResponse);
    }

    /**
     * Returns a Speechlet response for a speech and reprompt text.
     */
    private SpeechletResponse getSpeechletResponse(String speechText, String repromptText,
                                                   boolean isAskResponse) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard();
        card.setTitle("Session");
        card.setContent(speechText);

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech();
        speech.setText(speechText);

        if (isAskResponse) {
            // Create reprompt
            PlainTextOutputSpeech repromptSpeech = new PlainTextOutputSpeech();
            repromptSpeech.setText(repromptText);
            Reprompt reprompt = new Reprompt();
            reprompt.setOutputSpeech(repromptSpeech);

            return SpeechletResponse.newAskResponse(speech, reprompt, card);

        } else {
            return SpeechletResponse.newTellResponse(speech, card);
        }
    }

    final String getLastPrice(final String stockSymbol) throws Exception {
        final String url = "https://api.intrinio.com/prices?page_size=1&identifier=";

        final String[] myStocks = {stockSymbol}; // {"fb", "ua", "twtr","msft", "aapl","googl"};

        StringBuilder speechBuilder = new StringBuilder();
        String dateValue = null;
        for (final String symbol : myStocks) {
            final String companyName = mapSymbolToCompany.get(symbol);

            final String api = url + symbol.toUpperCase();
            try {

                final String data = HttpUtil.httpGetWithHeader(api, HEADERS);
                final JSONObject response = new JSONObject(data);
                final JSONArray jsonArrayData = response.getJSONArray("data");
                if (jsonArrayData.length() == 0) {
                    speechBuilder.append(" Company " + companyName).append(". No price information.");

                } else {
                    final JSONObject item = jsonArrayData.getJSONObject(0);
                    if (StringUtils.isEmpty(dateValue)) {
                        dateValue = item.getString("date");
                        dateValue = TwitterUtil.getDateSpeech(dateValue, "yyyy-MM-dd");
                        speechBuilder.append("On " + dateValue + ", here are the closed prices : ");
                    }

                    item.remove("ex_dividend");
                    item.remove("split_ratio");
                    item.remove("adj_open");
                    item.remove("adj_high");
                    item.remove("adj_low");
                    item.remove("adj_close");
                    item.remove("adj_volume");
                    item.remove("date");
                    item.remove("volume");
                    item.remove("open");
                    item.remove("high");
                    item.remove("low");

                    final Iterator it = item.keys();
                    speechBuilder.append(" Company " + companyName).append(" ");
                    while (it.hasNext()) {
                        final String key = it.next().toString();
                        speechBuilder.append(". ");
                        //    speechBuilder.append(key).append(" at: ");
                        speechBuilder.append(item.get(key));
                        speechBuilder.append(" . ");
                    }
                }
            } catch (Exception e) {
                throw new Exception("Got exception for api=" + api + ". e=" + e);
            }
            speechBuilder.append(" . ");
        }
        return speechBuilder.toString();
    }

    final String resolveWeek(final String dateValue) {
        int index = dateValue.indexOf("W");
        final String weekValue = dateValue.substring(index + 1);
        final int weekNum = Integer.parseInt(weekValue);
        index = index - 1;

        final int yearValue = Integer.parseInt(dateValue.substring(0, index));

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, yearValue);
        calendar.set(Calendar.WEEK_OF_YEAR, weekNum);

        final String dateValue2 = new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
        return dateValue2;
    }

    final String getMyStock(final String dateValue, final String stockSymbol) throws Exception {

        final String url = "https://api.intrinio.com/prices?start_date=" + dateValue + "&end_date=" + dateValue + "&frequency=daily&identifier=";
        String myStocks[] = stockSymbol.split(",");// {"fb", "ua", "twtr", "msft", "aapl", "googl"};

        StringBuilder speechBuilder = new StringBuilder();
        String closedPriceSpeec = "here are the closed prices";
        if (myStocks.length == 1) {
            closedPriceSpeec = "here is the closed price for";
        }
        final String dateSpeech = TwitterUtil.getDateSpeech(dateValue, "yyyy-MM-dd");

        // TODO: you are asking for stock prices of the following company:
        speechBuilder.append("On " + dateSpeech + ", ");
        for (final String symbol : myStocks) {
            final String companyName = mapSymbolToCompany.get(symbol);

            final String api = url + symbol.toUpperCase();
            try {

                final String data = HttpUtil.httpGetWithHeader(api, HEADERS);
                final JSONObject response = new JSONObject(data);
                final JSONArray jsonArrayData = response.getJSONArray("data");
                if (jsonArrayData.length() == 0) {
                    String lastStockInfo = getLastPrice(symbol);
                    if (speechBuilder.toString().contains("No price information is available")) {

                    } else {
                        speechBuilder.append(" No price information is available, but here is the latest info I have . ");
                    }
                    final int index = lastStockInfo.indexOf("Company");
                    final String dateInfo = lastStockInfo.substring(0, index);
                    if (speechBuilder.toString().contains(dateInfo)) {
                        lastStockInfo = lastStockInfo.substring(index);
                    }
                    speechBuilder.append(lastStockInfo);

                    //   return speechBuilder.toString();
                } else {
                    if (speechBuilder.toString().contains(closedPriceSpeec)) {

                    } else {
                        speechBuilder.append(" ").append(closedPriceSpeec).append(" : ");
                    }

                    final JSONObject item = jsonArrayData.getJSONObject(0);
                    item.remove("ex_dividend");
                    item.remove("split_ratio");
                    item.remove("adj_open");
                    item.remove("adj_high");
                    item.remove("adj_low");
                    item.remove("adj_close");
                    item.remove("adj_volume");
                    item.remove("date");
                    item.remove("volume");
                    item.remove("open");
                    item.remove("high");
                    item.remove("low");

                    final Iterator it = item.keys();
                    speechBuilder.append(" Company " + companyName).append(" ");
                    while (it.hasNext()) {
                        final String key = it.next().toString();
                        speechBuilder.append(". ");
                        speechBuilder.append(item.get(key));
                        speechBuilder.append(" . ");
                    }
                }
            } catch (Exception e) {
                throw new Exception("Got exception for api=" + api + ". e=" + e);
            }
            speechBuilder.append(" . ");
        }

        speechBuilder.append(" Goodbye .");

        return speechBuilder.toString().replace("Company", "");
    }

    private SpeechletResponse doMyStockIntent(final Intent intent, final Session session) {
        // Get the slots from the intent.
        final Map<String, Slot> slots = intent.getSlots();

        // Get the color slot from the list of slots.
        Slot companySlot = slots.get("Company");
        Slot dateSlot = slots.get("DATE");
        String speechText = "";

        Slot symbolSlot = slots.get("Symbol");

        String repromptText = null;
        String company = "Facebook";
        String companySpeech = "Face book";
        String symbol = "fb";

        Date myDate = new Date();
        String dateValue = new SimpleDateFormat("yyyy-MM-dd").format(myDate);

        if (dateSlot != null) {
            final String tmpValue = dateSlot.getValue();
            if (tmpValue != null) {
                if (tmpValue.contains("W")) {
                    dateValue = resolveWeek(tmpValue);
                } else {
                    dateValue = tmpValue;
                }
            } else {
                // dateValue is not provided. use default.
            }
        } else {
            // dateSlot is null. Use default.
        }
        try {
            String mySymbolList = "fb,ua,twtr,msft,aapl,googl";
            final String attValueList = (String) session.getAttribute("symbolList");
            if (StringUtils.isEmpty(attValueList)) {

            } else {
                mySymbolList = attValueList;
            }
            speechText = getMyStock(dateValue, mySymbolList);
            return getSpeechletResponse(speechText, repromptText, false);

        } catch (Exception e) {
            speechText = "Got exception " + e;
        }
        return getSpeechletResponse(speechText, repromptText, false);

        /*
        if (companySlot == null) {
            // Use default one.
            try {

                dateValue = dateSlot.getValue();
                speechText = getMyStock(dateValue);

            } catch (Exception e) {
                speechText = "Got exception " + e;
            }
            return getSpeechletResponse(speechText, repromptText, false);
        }

        // Check for favorite color and create output to user.
        if (companySlot != null) {
            // Store the user's favorite color in the Session and create response.
            String companyName = companySlot.getValue();
            if (companyName.toLowerCase().contains("book")) {
                companyName = "facebook";
                symbol = "fb";
            } else if (companyName.toLowerCase().contains("apple")) {
                companyName = "apple";
                symbol = "appl";
            } else if (companyName.toLowerCase().contains("soft")) {
                companyName = "microsoft";
                symbol = "msft";
            } else if (companyName.toLowerCase().contains("google")) {

            }

            companySpeech = mapFieldToSpeech.get(companyName);
            if (StringUtils.isEmpty(companySpeech) == true) {
                companySpeech = companyName;
            }
        }

        speechText =
                String.format("You are asking about company %s & symbol %s ",
                        companySpeech, symbol);


        return getSpeechletResponse(speechText, repromptText, false);
        */
    }

    private SpeechletResponse doStockIntent(final Intent intent, final Session session) {
        // Get the slots from the intent.
        final Map<String, Slot> slots = intent.getSlots();
        String repromptText = null;

        // Get the color slot from the list of slots.
        Slot companySlot = slots.get("COMPANY_NAME");
        Slot dateSlot = slots.get("DATE");
        String speechText = "";
        String symbol = "fb";
        String companyName = "facebook";
        if (StringUtils.isEmpty(companyName) == false) {
            companyName = companySlot.getValue();
            final String tmp = mapCompanyNames.get(companyName);
            if (StringUtils.isEmpty(tmp) == false) {
                companyName = tmp;
            }
        }
        log.info("company=" + companyName + ", symbol=" + symbol + ", mapCompanyToSymbol keys:" + mapCompanyToSymbol.keySet());

        symbol = getSymbolByCompanyName(companyName.toLowerCase());
        if (symbol == null) {
            speechText = "Sorry. I am unable to find the symbol for company name " + companyName + ". Please try another company?";
            boolean isAskResponse = true;
            return getSpeechletResponse(speechText, repromptText, isAskResponse);
        }

        Date myDate = new Date();
        String dateValue = new SimpleDateFormat("yyyy-MM-dd").format(myDate);

        if (dateSlot != null) {
            final String tmpValue = dateSlot.getValue();
            if (tmpValue != null) {
                if (tmpValue.contains("W")) {
                    dateValue = resolveWeek(tmpValue);
                } else {
                    dateValue = tmpValue;
                }
            } else {
                // dateValue is not provided. use default.
            }
        } else {
            // dateSlot is null. Use default.
        }
        try {
            speechText = getMyStock(dateValue, symbol);
            return getSpeechletResponse(speechText, repromptText, false);

        } catch (Exception e) {
            speechText = "Got exception " + e;
        }
        return getSpeechletResponse(speechText, repromptText, false);
    }

    private SpeechletResponse doCompanyIntent(final Intent intent, final Session session) {
        // Get the slots from the intent.
        final Map<String, Slot> slots = intent.getSlots();
        String repromptText = null;

        // Get the color slot from the list of slots.
        Slot companySlot = slots.get("COMPANY_NAME");

        String speechText = "";

        if (companySlot != null) {
            final String value = companySlot.getValue();
            if (StringUtils.isEmpty(value)) {
                return doMyStockIntent(intent, session);
            }
            if (value.toLowerCase().contains("no")) {
                return doMyStockIntent(intent, session);
            }
            final String symbol = getSymbolByCompanyName(value.toLowerCase());
            if (StringUtils.isEmpty(symbol)) {
                speechText = "Sorry. I am unable to find stock symbol for company name " + value + ". Please try another company?";
                boolean isAskResponse = true;
                return getSpeechletResponse(speechText, repromptText, isAskResponse);
            }

            speechText = "You asked for company name " + value + " . What else? ";

            String attValueList = (String) session.getAttribute("symbolList");
            if (StringUtils.isEmpty(attValueList)) {
                attValueList = "";
                speechText += "When done, say. No more.";

            } else {
                attValueList += ",";
            }

            attValueList += symbol;
            session.setAttribute("symbolList", attValueList);

            final boolean isAskResponse = true;
            return getSpeechletResponse(speechText, repromptText, isAskResponse);
        }

        Slot dateSlot = slots.get("DATE");
        String symbol = "fb";
        String companyName = "facebook";
        if (StringUtils.isEmpty(companyName) == false) {
            companyName = companySlot.getValue();
            final String tmp = mapCompanyNames.get(companyName);
            if (StringUtils.isEmpty(tmp) == false) {
                companyName = tmp;
            }
        }
        log.info("company=" + companyName + ", symbol=" + symbol + ", mapCompanyToSymbol keys:" + mapCompanyToSymbol.keySet());

        symbol = getSymbolByCompanyName(companyName.toLowerCase());
        if (symbol == null) {
            speechText = "Sorry. I am unable to find the symbol for company name " + companyName + " . Please try another company? ";
            // speechText += " What's the company name? ";
            boolean isAskResponse = true;
            return getSpeechletResponse(speechText, repromptText, isAskResponse);

        }

        Date myDate = new Date();
        String dateValue = new SimpleDateFormat("yyyy-MM-dd").format(myDate);

        if (dateSlot != null) {
            final String tmpValue = dateSlot.getValue();
            if (tmpValue != null) {
                if (tmpValue.contains("W")) {
                    dateValue = resolveWeek(tmpValue);
                } else {
                    dateValue = tmpValue;
                }
            } else {
                // dateValue is not provided. use default.
            }
        } else {
            // dateSlot is null. Use default.
        }
        try {
            speechText = getMyStock(dateValue, symbol);
            return getSpeechletResponse(speechText, repromptText, false);

        } catch (Exception e) {
            speechText = "Got exception " + e;
        }
        return getSpeechletResponse(speechText, repromptText, false);
    }


    /**
     * Wrapper for creating the Ask response from the input strings.
     *
     * @param stringOutput   the output to be spoken
     * @param isOutputSsml   whether the output text is of type SSML
     * @param repromptText   the reprompt for if the user doesn't reply or is misunderstood.
     * @param isRepromptSsml whether the reprompt text is of type SSML
     * @return SpeechletResponse the speechlet response
     */
    private SpeechletResponse newAskResponse(String stringOutput, boolean isOutputSsml,
                                             String repromptText, boolean isRepromptSsml) {
        OutputSpeech outputSpeech, repromptOutputSpeech;
        if (isOutputSsml) {
            outputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) outputSpeech).setSsml(stringOutput);
        } else {
            outputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) outputSpeech).setText(stringOutput);
        }

        if (isRepromptSsml) {
            repromptOutputSpeech = new SsmlOutputSpeech();
            ((SsmlOutputSpeech) repromptOutputSpeech).setSsml(repromptText);
        } else {
            repromptOutputSpeech = new PlainTextOutputSpeech();
            ((PlainTextOutputSpeech) repromptOutputSpeech).setText(repromptText);
        }
        Reprompt reprompt = new Reprompt();
        reprompt.setOutputSpeech(repromptOutputSpeech);
        return SpeechletResponse.newAskResponse(outputSpeech, reprompt);
    }

    static private final Map<String, String> mapNameToSpeech = new HashMap<>();

    static {
        mapNameToSpeech.put("realDonaldTrump", "Donald Trump");
        mapNameToSpeech.put("JeffBezos", "Jeff Bezos");
        mapNameToSpeech.put("Leadershipfreak", "Dan Rockwell");

    }

    private String cleanText(final String inTextRaw) throws Exception {
        String textRaw = inTextRaw;
        int index = textRaw.indexOf("https://");
        if (index < 0) {
            index = textRaw.indexOf("http://");
        }

        if (index > 0) {
            textRaw = textRaw.substring(0, index);
        }

        textRaw = textRaw.trim();
        return textRaw;
    }

    private SpeechletResponse doTrumpIntent(final Intent intent, final Session session) {
        // Get the slots from the intent.

        String repromptText = null;
        int maxTweets = 3;
        final String maxTweetsValue = System.getenv("MAX_TWEETS");
        if (StringUtils.isEmpty(maxTweetsValue) == false) {
            maxTweets = Integer.parseInt(maxTweetsValue);
        }
        String[] userNameList = {"realDonaldTrump", "Leadershipfreak", "JeffBezos"};

        final String userList = System.getenv("USER_LIST");
        if (StringUtils.isEmpty(userList) == false) {
            userNameList = userList.split(",");
        }

        String speechText = "";

        try {
            final TwitterUtil twitterUtil = new TwitterUtil();

            final Slot personSlot = intent.getSlot("PERSON");
            if (personSlot != null) {
                final String personValue = personSlot.getValue();

                if (StringUtils.isEmpty(personValue)) {

                } else {

                    final User user = twitterUtil.getUserInfoByUserName(personValue.trim());
                    if (user != null) {
                        userNameList = new String[]{user.getScreenName()};
                        mapNameToSpeech.put(user.getScreenName(), user.getName());
                    } else {
                        // ignore.
                    }
                }
            }

            final int length = userNameList.length;

            String nameList = ""; // "Here are the latest tweets from ";

            for (String userName : userNameList) {
                userName = userName.trim();
                final String speechName = mapNameToSpeech.get(userName);// + " with screen name " + userName;

                try {
                    if (userName.equals(userNameList[length - 1])) {
                        // this is the last one.
                        if (length > 1) {
                            nameList += " and ";
                        }
                        nameList += speechName;
                    } else {
                        if (nameList.length() == 0) {
                            nameList = speechName;
                        } else {
                            nameList += ", " + speechName;
                        }
                    }
                    final org.json.JSONArray jsonArray = twitterUtil.getTweetsByUser(userName);
                    int numTweets = jsonArray.length();
                    if (numTweets > maxTweets) {
                        numTweets = maxTweets;
                    }
                    for (int i = 0; i < numTweets; i++) {

                        final org.json.JSONObject jsonObject = jsonArray.getJSONObject(i);
                        final String text = cleanText(jsonObject.getString("full_text"));

                        final String createdAt = jsonObject.getString("created_at");
                        final String createdAtStr = TwitterUtil.getTwitterDateSpeech(createdAt);
                        speechText += " Tweet ";
                        if (length > 1) {
                            speechText += " from " + speechName + ": ";
                        }
                        speechText += " created on " + createdAtStr + " . " + text + " . ";
                    }

                } catch (Exception e) {
                    speechText = "Got exception " + e;
                }
            }

            speechText = "Here are the latest tweets from " + nameList + ": " + speechText + "Goodbye!";
        } catch (Exception e) {
            speechText = "Got exception " + e;
        }
        return getSpeechletResponse(speechText, repromptText, false);
    }


    private SpeechletResponse doTrendIntent(final Intent intent, final Session session) {
        String repromptText = null;
        String speechText = "";

        try {
            final TwitterUtil twitterUtil = new TwitterUtil();
            final String result = twitterUtil.getTrendTopics();
            final JSONArray jsonArray = new JSONArray(result).getJSONObject(0).getJSONArray("trends");
            int length = jsonArray.length();
            final int max = 5;
            final List<String> trendList = new ArrayList<>();

            for (int i = 0; i < length; i++) {
                final JSONObject jsonObject = jsonArray.getJSONObject(i);
                final String name = jsonObject.getString("name");
                final String volume = jsonObject.get("tweet_volume").toString();
                if (volume.contains("nul")) {

                } else {
                    String text = name + " . tweeted " + volume + " times . ";
                    if (trendList.size() == (max - 1)) {
                        text = "And finally, " + text;
                    }
                    trendList.add(text);
                }

                if (trendList.size() >= max) {
                    break;
                }
            }

            for (final String item : trendList) {
                speechText += item;
            }

            speechText = "Here are the top " + trendList.size() + " trends on twitter right now: " + speechText + " Goodbye!";

        } catch (Exception e) {
            e.printStackTrace();
            speechText = "Got exception " + e;
        }

        return getSpeechletResponse(speechText, repromptText, false);
    }


    private SpeechletResponse doWelcome(final Intent intent, final Session session) {

        final String speechText = "Hi, I am Alexa with Nathan skill. You can ask me questions like: " +
                "Alexa, ask Nathan what are the latest tweets from Trump. " +
                "Or, Alexa ask Nathan what's trending on Twitter right now. Or " +
                "Alexa, ask Nathan about my stocks today, yesterday, last week, last friday or last month" +
                //   ". or, Alexa, ask Nathan how are my stocks doing today, yesterday, last week, last friday or last month" +
                ". Goodbye!";
        final String repromptText = null;


        return getSpeechletResponse(speechText, repromptText, false);
    }
}
