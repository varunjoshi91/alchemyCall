package com.getAllTweets.com;


import twitter4j.conf.*;
import twitter4j.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import twitter4j.auth.*;
import twitter4j.api.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;

/**
 * Created by varunjoshi on 9/20/15.
 */
public class twitWalk {

    Twitter twitter; //twitter object for processing

    //Queries
    //String searchString = "фильм AND lang:ru";
    //String searchString = "#usopen";
    //String searchString = "taylor swift AND lang:en AND until:2015-09-17";

    //String searchString = "lang:en";
    //Queries

    List<Status> tweets; // twitter data

    int totalTweets; // count of tweets

    void setup(String filename,String lang,String query, String untilDate) //Setting up connection handler to twitter
    {

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setJSONStoreEnabled(true);
        cb.setOAuthConsumerKey("PZqCP0ePklpeAV9XSRAis4DAf");
        cb.setOAuthConsumerSecret("uXrzEfpWL5loKG3rldxE1hG65he32ctseJpo4SS6ecKztxyZah");
        cb.setOAuthAccessToken("451017865-cXx9jdEr6eWSBQFe9qfc7E9S4AWDvn7jjjEPMFT4");
        cb.setOAuthAccessTokenSecret("asnUm24q8XHOzLcesSfWgI5D6UVY3BCkatFP4sOZZEPtr");

        TwitterFactory tf = new TwitterFactory(cb.build());

        twitter = tf.getInstance();
        getNewTweets(filename,lang,query,untilDate);
    }

    void getNewTweets(String filename,String lang,String queryTweet, String untilDate){

        String searchString = queryTweet+" AND lang:"+lang+" AND until:"+untilDate;

        //String searchString = "taylor swift AND lang:en AND until:2015-09-12";

        try {
            Query query = new Query(searchString);

            query.setCount(100); //by default its 15 set count
            QueryResult result = twitter.search(query);
            tweets = result.getTweets(); // the return of this function is list

            PrintWriter writer = new PrintWriter(filename+".json", "UTF-8"); //name of the file "collection_1_en.json"

            writer.println("[");

            //for (Status tweet : tweets) {

                for(int i = 0; i < tweets.size(); i++) {

                String json = TwitterObjectFactory.getRawJSON(tweets.get(i));

                JSONObject obj = new JSONObject(json); // object that handles the recieved JSON format
                JSONObject newObj = new JSONObject();  // object that handles the final JSON format

                newObj.put("id", obj.getString("id")); // getting the id
                newObj.put("lang", obj.getString("lang")); //getting the lang

                newObj.put("favorited", obj.getString("favorited")); //getting the favourited status
                newObj.put("retweeted", obj.getString("retweeted")); //getting the retweet status

                //newObj.put("location", obj.getJSONObject("user").getString("location")); //getting the location


                DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss Z yyyy", Locale.US);
                Date date = sdf.parse(obj.getString("created_at"));
                newObj.put("created_at",formatter.format(date));


                if(obj.getString("lang").equals("en"))
                    newObj.put("text_en", obj.getString("text"));
           else if(obj.getString("lang").equals("ru"))
                    newObj.put("text_ru", obj.getString("text"));
           else if(obj.getString("lang").equals("de"))
                    newObj.put("text_de", obj.getString("text"));

                    //newObj.put("tweet_hashtags", obj.getJSONObject("entities").getJSONArray("hashtags").getJSONObject(0)); //getting the hashtags

                    //newObj.put("tweet_url", obj.getJSONObject("entities").getJSONArray("urls")); //getting the lang

                    JSONArray tempArray1=obj.getJSONObject("entities").getJSONArray("urls");
                    JSONArray tempArray2=obj.getJSONObject("entities").getJSONArray("hashtags");

                    //System.out.println(obj.getJSONObject("entities"));
                    if(tempArray1.length() == 0){
                        try {
                            tempArray1 = obj.getJSONObject("entities").getJSONArray("media");
                        }
                        catch(Exception e){
                            tempArray1=obj.getJSONObject("entities").getJSONArray("urls");
                        }
                    }

                    JSONArray urlArray=new JSONArray();
                    JSONArray hashTagsArray=new JSONArray();

                    for(int index = 0 ; index<tempArray1.length();index++){
                        JSONObject tempObj = tempArray1.getJSONObject(index);
                        urlArray.put(tempObj.get("expanded_url"));
                        //System.out.println("tempObj "+tempObj.toString());
                    }

                    for(int index = 0 ; index<tempArray2.length();index++){
                        JSONObject tempObj = tempArray2.getJSONObject(index);
                        hashTagsArray.put(tempObj.get("text"));
                    }

                    newObj.put("tweet_url", urlArray); //getting the lang
                    newObj.put("tweet_hashtags", hashTagsArray); //getting the lang

                    if(i < tweets.size()-1)
                    {
                        writer.println(newObj+",");
                    }
                    else{
                        writer.println(newObj);
                    }



            }
            writer.println("]");
            writer.close();


        }
        catch(Exception e){
            System.out.println(e);
        }




    }


    public static void main(String[] args) {
        //System.out.println("TwitterGet.main");
        twitWalk TwitterGetObj = new twitWalk();

        /*English Tweets
        TwitterGetObj.setup("collection_1_en","en","taylor swift","2015-09-21");
        TwitterGetObj.setup("collection_2_en","en","game of thrones","2015-09-21");
        TwitterGetObj.setup("collection_3_en","en","eminem","2015-09-20");
        TwitterGetObj.setup("collection_4_en","en","justin bieber","2015-09-20");
        TwitterGetObj.setup("collection_5_en","en","#mockingjay","2015-09-21");
        /*English Tweets
        TwitterGetObj.setup("collection_6_en","en","#WorthItVMA","2015-09-21");
        TwitterGetObj.setup("collection_7_en","en","#marvel","2015-09-21");
        TwitterGetObj.setup("collection_8_en","en","#batmanvssuperman","2015-09-21");*/

        /*German Tweets
        TwitterGetObj.setup("collection_1_de","de","Big Brother","2015-09-20");
        TwitterGetObj.setup("collection_2_de","de","bibibeipopstars","2015-09-21");
        TwitterGetObj.setup("collection_3_de","de","Deutschland 83","2015-09-20");
        TwitterGetObj.setup("collection_4_de","de","Downfall","2015-09-20");
        TwitterGetObj.setup("collection_5_de","de","#FearTheWalkingDead","2015-09-21");
        /*German Tweets*/
        //TwitterGetObj.setup("collection_6_de","de","#HalliGalli","2015-09-21");
        TwitterGetObj.setup("collection_7_de","de","rammstein","2015-09-21");

        /*Russian Tweets*/
        //TwitterGetObj.setup("collection_1_ru","ru","My Chemical Romance","2015-09-20");
        //TwitterGetObj.setup("collection_2_ru","ru","movies","2015-09-21");
        //TwitterGetObj.setup("collection_3_ru","ru","rammstein","2015-09-21");
        //TwitterGetObj.setup("collection_4_ru","ru","music","2015-09-20");
        //TwitterGetObj.setup("collection_5_ru","ru","entertainment","2015-09-21");//
        /*Russian Tweets
        TwitterGetObj.setup("collection_6_ru","ru","Джиган","2015-09-21");//Наргиз
        TwitterGetObj.setup("collection_7_ru","ru","Наргиз","2015-09-21");*/


    /*void getNewTweets(String filename,String lang,String query, String untilDate){

        String searchString = query+" AND lang:"+lang+" AND until:"+untilDate;

        //String searchString = "taylor swift AND lang:en AND until:2015-09-12";*/


    }

}
