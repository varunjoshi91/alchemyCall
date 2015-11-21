package com.getAllTweets.com;

        import java.io.File;
        import java.io.FileOutputStream;
        import java.io.FileInputStream;
        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.OutputStream;
        import java.io.OutputStreamWriter;
        import java.io.Writer;
        import java.io.UnsupportedEncodingException;
        import java.util.ArrayList;
        import java.util.HashMap;
/* For language detection */
        import java.io.InputStreamReader;
        import java.net.URLEncoder;
        import java.net.URL;
        import java.net.HttpURLConnection;
        import java.io.DataInputStream;



/* For jSON parsing*/
        import twitter4j.JSONArray;
        import twitter4j.JSONException;
        import twitter4j.JSONObject;

/*
* The program will run the queries as given in queries.txt
* It will get the result  from Koding VM , and generate a TREC-eval compatible output result file
* The TREC-eval will evaluate the found result and compare the same to qrel.txt
* qrel.txt contains manual relevance judgement
* We must try to maximize the score found by TREC-eval on different measures such as F0.5, nDCG etc.
* Part1 : get results from KODING VM
*/
class RunBasicQueriesOnSolr {
    static ArrayList<String> queryNumber = new ArrayList<>();
    private String queryFileName;
    private String detectLanguageURL = "http://ws.detectlanguage.com/0.2/detect";
    private String twitterCoreURL    = "http://joshivarun.koding.io:8983/solr/twitterStuff/select"; //Enter your URL here
    private String charset;
    private final String API_KEY 	 = "105395efdda297b8a6e2d5e325a245e5"; // get your own API key from the detectlanguage.com ( if you wish so)
    private final String USER_AGENT  = "Mozilla/5.0";
    private String trecLogFile       = "!!!!!!!!!!!final_default_01.txt";
    private String workingDir;
    private HashMap<String, Integer> maxIndexSizeData;
    /**
     *
     * @param queryFileName
     */
    RunBasicQueriesOnSolr(String queryFileName) {
        this.queryFileName = queryFileName;
        charset = java.nio.charset.StandardCharsets.UTF_8.name();
        workingDir = new File("").getAbsolutePath()+"/";
        maxIndexSizeData = new HashMap<>();
    }
    /**
     *
     */
    public ArrayList<String> readQueriesFromFile() {
        ArrayList<String> queries = new ArrayList<>();

        String aLine;
        try {
            FileInputStream fstream1 = new FileInputStream(queryFileName);
            DataInputStream in = new DataInputStream(fstream1);
            BufferedReader br   = new BufferedReader(new InputStreamReader(in, "UTF8"));
            while ( (aLine = br.readLine())  != null) {
                //System.out.println("----- "+aLine.substring(0,3));
                queries.add(aLine.substring(9));
                queryNumber.add(aLine.substring(0, 3));
            }
            br.close();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return queries;
    }
    /**
     *
     * @param jSONString
     * @return
     * @throws JSONException
     */
    public String parseJSONStringAndFindLanguage(String jSONString) throws JSONException {
        JSONObject jObj;
        String language = "";
        jObj = new JSONObject(jSONString);
        language = jObj.getJSONObject("data").getJSONArray("detections").getJSONObject(0).get("language").toString();
        return language;

    }
    /**
     *
     * @param URL
     * @param query
     * @return
     * @throws IOException
     */
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
    /**
     * @param :QueryText
     * The param is tested by firing a HTTP post request to http://ws.detectlanguage.com/0.2/detect
     * The output result is in JSON Format which is parsed to extract language field
     */
    public String getLanguageOfQuery(String queryText) {
        String lang = "";
        queryText = queryText.replace(" ","+");
        String response;
        try {
            String query = String.format("q=%s&key=%s",
                    URLEncoder.encode(queryText, charset),
                    URLEncoder.encode(API_KEY, charset));

            response = fetchHTTPData(detectLanguageURL, query);
            if (!response.equals(""))
                lang     = parseJSONStringAndFindLanguage(response);

            else
                System.out.println("No response from Language detection server...");
        }
        catch(Exception ex) {
            System.out.println("Exception occured while detecting language...");
            ex.printStackTrace();
        }
        return lang;
    }
    /**
     *
     * @param responseFromSolr
     * @return
     * @throws JSONException
     * @throws NumberFormatException
     */
    public String parseJSONResponseFromSolr(String queryNumber, String responseFromSolr, String modelName, boolean bIsOnlyNumFoundRequired)
            throws NumberFormatException, JSONException {

        String numFound;
        StringBuilder trec_Response = new StringBuilder();
        JSONObject jObjTemp;
        if (responseFromSolr.equals(""))
        {
            //	System.out.println("queryNumber :" + queryNumber);
            //System.out.println("-------------------------------------");
            return trec_Response.toString();
        }
        JSONObject jObj = new JSONObject(responseFromSolr).getJSONObject("response");
        numFound = jObj.get("numFound").toString();
        if (bIsOnlyNumFoundRequired)
            return numFound;
        JSONArray jObjArray = new JSONArray(jObj.getString("docs").toString());
        for ( int i = 0 ; i < jObjArray.length(); i++) {
            jObjTemp = jObjArray.getJSONObject(i);
            //System.out.println(RunBasicQueriesOnSolr.queryNumber.get(i));
            trec_Response = trec_Response.append(queryNumber + " Q0 " +
                    jObjTemp.getString("id") + " " +
                    String.valueOf(i) + " " +
                    jObjTemp.getString("score") + " " + modelName + System.lineSeparator());

        }
        //System.out.println("***********");
        return trec_Response.toString();
    }
    /**
     *
     * @param trecSupportedOutput
     */
    public void WriteDataInTRECFormat(String trecSupportedOutput ) {
        try {
            File file = new File( workingDir + trecLogFile);
            if (!file.exists()) {
                file.createNewFile();
            }
            OutputStream outputStream       = new FileOutputStream(file);
            Writer       outputStreamWriter = new OutputStreamWriter(outputStream);


            outputStreamWriter.write(trecSupportedOutput);

            outputStreamWriter.close();
            System.out.println("File created !!!! varun joshi");
            System.out.println("Created file is =" + new File(workingDir + trecLogFile).getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    /**
     * See: {@link org.apache.lucene.queryparser.classic queryparser syntax}
     * for more information on Escaping Special Characters
     */
    public String escapeQueryChars(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            // These characters are part of the query syntax and must be escaped
            if (c == '\\' || c == '+' || c == '-' || c == '!'  || c == '(' || c == ')' || c == ':'
                    || c == '^' || c == '[' || c == ']' || c == '\"' || c == '{' || c == '}' || c == '~'
                    || c == '*' || c == '?' || c == '|' || c == '&'  || c == ';' || c == '/'
                    || Character.isWhitespace(c)) {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }
    /**
     * @throws IOException
     *
     */
    public boolean getMaximumIndexSizeBasedOnLanguage() {
        String response, query = "";
        String lang[] = {"en", "de", "ru" };
        for  (int i = 0 ; i < lang.length; i++)
        {
            try {
                query = String.format("q=*:*&fq=lang:%s&start=0&rows=0&fl=numFound&&wt=json&indent=true",URLEncoder.encode(lang[i],charset));
                response = fetchHTTPData(twitterCoreURL , query);
                maxIndexSizeData.put(lang[i],new Integer(Integer.parseInt(parseJSONResponseFromSolr(queryNumber.get(i), response, "default", true)))); //varun
            } catch (IOException | NumberFormatException | JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        if (maxIndexSizeData.size() != 3) {
            return false;
        }
        else
            return true;
    }
    /**
     * reads the input query file and gets the data from Solr
     * Main function to call when fetching data
     * Returns error codes in case of failure
     * 0 : success
     * -1: No data present in query file
     * -2: Unsupported Encoding
     * -3: IO Exception
     */
    public int runQueriesOnSolr() {
        ArrayList<String> inputQueries = readQueriesFromFile();
        String lang, query, response;
        StringBuilder sTrecSupportedResponse;
        if ( inputQueries.size() == 0) {
            return -1; //error code -1: no input queries
        }
        int numOfRows = 0;
        if (!getMaximumIndexSizeBasedOnLanguage())
        {
            return -10; // Cannot query Solr
        }
        sTrecSupportedResponse = new StringBuilder();
        for ( int i = 0 ; i < inputQueries.size(); i++) {
            //System.out.println("+++++ "+inputQueries.get(i));
            //lang  = getLanguageOfQuery(inputQueries.get(i));
            lang  = inputQueries.get(i).substring(0,2);
            query=inputQueries.get(i).substring(3);
            //System.out.println(lang+"  "+query);
            try {
                query = escapeQueryChars(inputQueries.get(i).substring(3));
                //System.out.println("lang :" + lang);
                try {
                    numOfRows = maxIndexSizeData.get(lang);
                }
                catch(Exception ex) {
                    numOfRows = 1000;
                }
                /*query = String.format("q=text_%s:%s&fq=lang:%s&wt=json&start=0&rows=%s&indent=true&fl=id,score",
                        URLEncoder.encode(lang, charset), URLEncoder.encode(query, charset), URLEncoder.encode(lang, charset),
                        URLEncoder.encode(String.valueOf(numOfRows), charset));*/


                //dismax
                query = String.format("q=%s&fq=lang:%s&wt=json&start=0&rows=%s&indent=true&defType=edismax&fl=id,score&qf=text_%s^5+tweet_hashtags^10&pf=text_%s^50+tweet_hashtags^20",
                        URLEncoder.encode(query, charset), URLEncoder.encode(lang, charset),
                        URLEncoder.encode(String.valueOf(numOfRows), charset), URLEncoder.encode(lang, charset), URLEncoder.encode(lang, charset));



                response = fetchHTTPData(twitterCoreURL, query);
                System.out.println("query :" + query);
                Integer.parseInt(RunBasicQueriesOnSolr.queryNumber.get(i));
                sTrecSupportedResponse = sTrecSupportedResponse.append(parseJSONResponseFromSolr(String.format("%03d", Integer.parseInt(RunBasicQueriesOnSolr.queryNumber.get(i))), response, "default", false));
                //System.out.println("sTrecSupportedResponse: "+sTrecSupportedResponse.toString());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                return -2;
            } catch (IOException e) {
                e.printStackTrace();
                return -3;
            } catch (NumberFormatException e) {
                e.printStackTrace();
                return -4;
            } catch (JSONException e) {
                e.printStackTrace();
                return -5;
            }
        }
        WriteDataInTRECFormat(sTrecSupportedResponse.toString());

        return 0;
    }

}
/*
*
*/
class QueryRun {
    /**
     *
     * @param filePathString : path of file to check if it exists or not
     * @return : true, if file exists; else false
     */
    public static boolean IsFileExists(String filePathString) {
        File f = new File(filePathString);

        if(f.exists() && f.isFile()) {
            return true;
        }
        else
            return false;
    }
    public static void main(String []args) {
        String queryFileName = "queries.txt";//  /Users/varunjoshi/IdeaProjects/getTweets/queries.txt
        queryFileName = new File("").getAbsolutePath() +"/"+queryFileName;
        System.out.println(queryFileName);
        if (!QueryRun.IsFileExists(queryFileName) ) {
            System.out.println("Please provide queries.txt file in the current directory ! File not found...");
            return;
        }
        RunBasicQueriesOnSolr runBasic = new RunBasicQueriesOnSolr(queryFileName);
        runBasic.runQueriesOnSolr();
    }

}