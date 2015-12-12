package com.getAllTweets;

import twitter4j.conf.*;
import twitter4j.*;
import twitter4j.auth.*;
import twitter4j.api.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;



/**
 * Created by varunjoshi on 9/13/15.
 */
public class TwitterGet {

    Twitter twitter;
    String searchString = "Путин AND lang:ru";
    //String searchString = "#prayforparis AND lang:en";
    List<Status> tweets;
    int totalTweets;

    void setup()
    {


        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setJSONStoreEnabled(true);
        cb.setOAuthConsumerKey("PZqCP0ePklpeAV9XSRAis4DAf");
        cb.setOAuthConsumerSecret("uXrzEfpWL5loKG3rldxE1hG65he32ctseJpo4SS6ecKztxyZah");
        cb.setOAuthAccessToken("451017865-cXx9jdEr6eWSBQFe9qfc7E9S4AWDvn7jjjEPMFT4");
        cb.setOAuthAccessTokenSecret("asnUm24q8XHOzLcesSfWgI5D6UVY3BCkatFP4sOZZEPtr");

        TwitterFactory tf = new TwitterFactory(cb.build());

        twitter = tf.getInstance();
        getNewTweets();
    }


    void getNewTweets(){
        try{
            Query query = new Query(searchString);

            query.setCount(500); //by default its 15 set count
            QueryResult result = twitter.search(query);
            tweets = result.getTweets(); // the return of this function is list



            PrintWriter writer = new PrintWriter("!!!123lateedsNight123.json", "UTF-8"); //name of the file

            PrintWriter writerObj = new PrintWriter("raw/raw_json_prayforparis.json", "UTF-8"); //name of the file


            for (Status tweet : tweets) {
                //System.out.println(tweet.getFromUser() + ":" + tweet.getText());
                String json = TwitterObjectFactory.getRawJSON(tweet);
                //System.out.println(json);
                writerObj.println(json);


                JSONObject obj = new JSONObject(json);

                JSONObject newObj = new JSONObject();

                //System.out.println(obj.getString("text"));

                String tweetText = obj.getString("text");

                //String regex = "^https?://[^/]+/([^/]+)/.*$";

                Pattern MY_PATTERN = Pattern.compile("#(\\w+|\\W+)");

                Pattern URL_PATTERN= Pattern.compile(".*http://.*");

                Matcher mat = MY_PATTERN.matcher(tweetText);
                List<String> str=new ArrayList<String>();
                while (mat.find()) {
                    //System.out.println(mat.group(1));
                    str.add(mat.group(1));
                }

                Matcher matchURL = URL_PATTERN.matcher(tweetText);
                List<String> str_url=new ArrayList<String>();
                //System.out.println("url "+matchURL.find() + " " + matchURL.toString());

                while (matchURL.find()) {
                    //System.out.println(matchURL.group(0)+"  "+ matchURL.group(1));
                    str_url.add(matchURL.group(0));
                }



                newObj.put("id",obj.getString("id"));
                newObj.put("text",obj.getString("text"));
                newObj.put("lang",obj.getString("lang"));
                newObj.put("created_at", obj.getString("created_at"));

                /*

                if(obj.has("twitter_hashtags")){
                    newObj.put("twitter_hashtags",obj.getString("twitter_hashtags"));
                }
                else{
                    newObj.put("twitter_hashtags","[]");
                }
                if(obj.has("twitter_urls")){
                    newObj.put("twitter_urls",obj.getString("twitter_urls"));
                }
                else{
                    newObj.put("witter_urls","[]");
                }
                */
                //System.out.println("url: "+str_url);
                newObj.put("twitter_hashtags",str);
                newObj.put("twitter_urls",str_url);
                //newObj.put("twitter_urls",obj.getString("twitter_urls"));
                str.clear();
                str_url.clear();
                writer.println(newObj);
            }
            writer.close();
            writerObj.close();

            /*tweets = result.getTweets();

            for(Status s:tweets){
                totalTweets++;
                System.out.println(tweets.toString());
            }

            System.out.println("totalTweets "+totalTweets);
            */
            //printTweetsToFile();
        }
        catch(Exception e){
            System.out.println("Exception "+e);
        }


    }


    void printTweetsToFile(){
        //System.out.println(tweets);
        try {
            PrintWriter writer = new PrintWriter("raw/text123.json", "UTF-8");
            writer.println(tweets);
            //writer.println("The second line"+tweets.toString());
            writer.close();
        }
        catch(Exception e){
            System.out.println("Exception e");
        }
    }

    public static void main(String[] args) {
        //System.out.println("TwitterGet.main");
        TwitterGet TwitterGetObj = new TwitterGet();
        TwitterGetObj.setup();
    }

}
