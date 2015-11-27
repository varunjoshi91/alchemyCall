package com.getAllTweets.com;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by varunjoshi on 11/26/15.
 */
public class alchemyCall {

    final String apiKey = "a400b5e6a60265dc15f9e405f4fcc0795545396c";
    final String alchemyUrl = "http://gateway-a.watsonplatform.net/calls/text/TextGetRankedConcepts";
    private String charset = java.nio.charset.StandardCharsets.UTF_8.name();
    private final String USER_AGENT  = "Mozilla/5.0";


    String text = "Everyone Thinks #Drake\\u2019s Brutal \\\"Diamonds Dancing\\\" Verse Is About Nicki Minaj http://t.co/zqByrinj69 http://t.co/gOmYBUz491";

    public String tryCatchBs(){
        String response="";
    try {
        String query = String.format("apikey=%s&text=%s`&outputMode=json",
                URLEncoder.encode(apiKey, charset),
                URLEncoder.encode(text, charset));

        response = fetchHTTPData(alchemyUrl, query);
        if (!response.equals(""))
            System.out.println("Hey man will it work "+response);

        else
            System.out.println("No response from Language detection server...");
        return response;
    }
    catch(Exception ex) {
        System.out.println("Exception occured while detecting language...");
        ex.printStackTrace();
    }
        return response;

    }

    public String fetchHTTPData(String URL, String query) throws IOException {
        String response = "";
        int responseCode = 0;
        HttpURLConnection httpConn = (HttpURLConnection) new URL(URL + "?" + query).openConnection();
        httpConn.setDoOutput(true); // Triggers POST.
        httpConn.setRequestProperty("Accept-Charset", charset);
        httpConn.setRequestProperty("User-Agent", USER_AGENT);
        responseCode = httpConn.getResponseCode();
        if ( responseCode == 200) { //OK
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(httpConn.getInputStream()));
            String inputLine;
            StringBuffer responseBuffer = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                responseBuffer.append(inputLine);
            }
            in.close();
            response = responseBuffer.toString();
        }

        return response;
    }

    public static void main(String []args) {
        String queryFileName = "queries.txt";//  /Users/varunjoshi/IdeaProjects/getTweets/queries.txt
        queryFileName = new File("").getAbsolutePath() +"/"+queryFileName;
        System.out.println(queryFileName);
        if (!QueryRun.IsFileExists(queryFileName) ) {
            System.out.println("Please provide queries.txt file in the current directory ! File not found...");
            return;
        }
        alchemyCall alchemyCallObj = new alchemyCall();
        System.out.println("Function runs very fast "+alchemyCallObj.tryCatchBs());
        //alchemyCallObj.runQueriesOnSolr();

    }



}
