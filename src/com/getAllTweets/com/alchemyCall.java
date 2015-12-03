package com.getAllTweets.com;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

/**
 * Created by varunjoshi on 11/26/15.
 */
public class alchemyCall {

    final String apiKey = "a400b5e6a60265dc15f9e405f4fcc0795545396c";
    final String alchemyUrl = "http://gateway-a.watsonplatform.net/calls/text/TextGetRankedConcepts";
    private String charset = java.nio.charset.StandardCharsets.UTF_8.name();
    private final String USER_AGENT  = "Mozilla/5.0";




    //String text = "Everyone Thinks #Drake\\u2019s Brutal \\\"Diamonds Dancing\\\" Verse Is About Nicki Minaj http://t.co/zqByrinj69 http://t.co/gOmYBUz491";

    public void readFromJSONFile(String fileName){

        try{
            PrintWriter writer = new PrintWriter("simple_waste_1.json", "UTF-8");

            String aLine="";
            JSONParser parser = new JSONParser();
            Object obj = new Object();

            /*Write to JSON*/




            FileInputStream fstream1 = new FileInputStream(fileName);
            DataInputStream in = new DataInputStream(fstream1);
            BufferedReader br   = new BufferedReader(new InputStreamReader(in, "UTF8"));
            while ( (aLine = br.readLine())  != null) {
                JSONObject newObj = new JSONObject();
                obj = parser.parse(aLine);
                JSONObject jsonObject = (JSONObject) obj;
                String concept = tryCatchBs(jsonObject.get("text").toString());
                //System.out.println(concept);

                JSONParser newParser = new JSONParser();
                Object newObjPar = new Object();
                newObjPar = newParser.parse(concept);//varun

                JSONArray conceptObj = (JSONArray) newObjPar;
                JSONObject tempObj = new JSONObject();

                //tempObj.put(conceptObj.get(""));
                //String conceptString = "";
                ArrayList<String> conceptString = new ArrayList<String>();
                ArrayList<String> relevanceString = new ArrayList<String>();
                for(int i=0;i<conceptObj.size();i++) {
                    JSONObject tempConceptObj = (JSONObject) conceptObj.get(i);
                    //System.out.println(tempConceptObj.keySet());
                    conceptString.add(tempConceptObj.get("text").toString());
                    relevanceString.add(tempConceptObj.get("relevance").toString());
                }

                newObj.putAll(jsonObject);
                newObj.put("concept_tag",conceptString);
                newObj.put("relevance_tag",relevanceString);

                //System.out.println(newObj.get("concept_tag"));

                writer.println(newObj);

            }
            br.close();
            writer.close();











        }
        catch (Exception e){
            System.out.println(e);
        }

    }

    public String tryCatchBs(String queryText){
        String response="";
    try {
        String query = String.format("apikey=%s&text=%s`&outputMode=json",
                URLEncoder.encode(apiKey, charset),
                URLEncoder.encode(queryText, charset));

        response = fetchHTTPData(alchemyUrl, query);

       // JSONObject jsonResponse = new JSONObject(response);

        Object myObject = new Object(); // blank object

        JSONParser parserObj = new JSONParser();
        myObject = parserObj.parse(response); // parsed string output to json

        JSONObject jsonObject = (JSONObject) myObject;

        response = jsonObject.get("concepts").toString();

       /* JSONObject newObjJson = (JSONObject) jsonObject.clone();
        newObjJson.put("concept_tag",response); //joshi*/





        //JSONArray jsonMainArr = mainJSON.getJSONArray("source");

        if (!response.equals(""))
            System.out.println("");
            //System.out.println("Hey man will it work "+response);

        else
            System.out.println("");
            //System.out.println("No response from Language detection server...");
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
        String queryFileName = "rj.json";//  /Users/varunjoshi/IdeaProjects/getTweets/queries.txt
        queryFileName = new File("").getAbsolutePath() +"/"+queryFileName;
        System.out.println(queryFileName);
        if (!QueryRun.IsFileExists(queryFileName) ) {
            System.out.println("Please provide queries.txt file in the current directory ! File not found...");
            return;
        }
        alchemyCall alchemyCallObj = new alchemyCall();
        alchemyCallObj.readFromJSONFile(queryFileName);
        System.out.println("Function runs very fast ");
        //alchemyCallObj.runQueriesOnSolr();

    }



}
