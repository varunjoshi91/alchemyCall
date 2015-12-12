package com.getAllTweets.com;

import javax.json.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;


public class queryTrans {
    String queryStr;
    final String detectLangEndpoint="https://translate.yandex.net/api/v1.5/tr.json/detect?";
    final String translateEndpoint="https://translate.yandex.net/api/v1.5/tr.json/translate?";
    //final String api_key="trnsl.1.1.20151203T154217Z.ccc587945a4cc109.efa5a68bd521b44e9360f98a8613e343a366d583";
    final String api_key="trnsl.1.1.20151212T012049Z.86fb4b11ef415075.457a142857c9ed95ceceb9f6b25e00ea80f2aa52";
    private String charset = java.nio.charset.StandardCharsets.UTF_8.name();
    String[] translation=new String[5];



    public queryTrans(){

    }

    public String detectLang(String query){
        String lang="";
        String urlStr=detectLangEndpoint+"key="+api_key+"&text="+query;
        String result="";
        //System.out.println("Query "+query);
        try{
            URL url = new URL(urlStr);
            URLConnection conn = url.openConnection ();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = rd.readLine()) != null)
            {
                sb.append(line);
            }
            rd.close();
            result = sb.toString();
            JsonReader jr=Json.createReader(new StringReader(result));
            JsonObject jobj=jr.readObject();
            lang=jobj.getString("lang");
        }catch(Exception e){
            System.out.println("Exception in detect "+e);
            e.printStackTrace();

        }
        //System.out.println(result);
        //System.out.println(lang);
        return lang;
    }

    public String[] translate(String query) throws UnsupportedEncodingException {
        String[] langs={"ar","en","de","fr","ru"};
        String fl="de";
        int i,j;
        for(i=0;i<langs.length;i++)
            if(fl.equals(langs[i]))
                break;
        if(i==langs.length){
            System.out.println(fl+" is not supported!");
            System.exit(-1);
        }
        translation[i]=queryStr;
        for(j=0;j<langs.length;j++){
            if(j==i)
                continue;
            String urlStr=String.format(translateEndpoint+"key=%s&text=%s&lang=%s",
                    URLEncoder.encode(api_key, charset),
                    URLEncoder.encode(query, charset),
                    URLEncoder.encode(langs[j], charset));

            /*String urlStr = String.format("apikey=%s&text=%s`&outputMode=json",
                    URLEncoder.encode(api_key, charset),
                    URLEncoder.encode(query, charset),
                    URLEncoder.encode(langs[j], charset));*/

            String result="";
            try{
                URL url = new URL(urlStr);
                URLConnection conn = url.openConnection ();

                // Get the response
                BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = rd.readLine()) != null)
                {
                    sb.append(line);
                }
                rd.close();
                result = sb.toString();
                JsonReader jr=Json.createReader(new StringReader(result));
                JsonObject jobj=jr.readObject();
                //System.out.println(result);
                JsonArray jArr;
                jArr=jobj.getJsonArray("text");
                translation[j]=((JsonString)jArr.get(0)).getString();
            }catch(Exception e){
                System.out.println("Exception in translate");
                e.printStackTrace();

            }
        }
        //for(int k=0;k<translation.length;k++)
            //System.out.println(translation[k]);

        return translation;
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        queryTrans q1=new queryTrans();
        //q1.detectLang();
        q1.translate("Париж атаки");
    }
}
