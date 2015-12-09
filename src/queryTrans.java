import javax.json.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;


public class queryTrans {
    String queryStr;
    final String detectLangEndpoint="https://translate.yandex.net/api/v1.5/tr.json/detect?";
    final String translateEndpoint="https://translate.yandex.net/api/v1.5/tr.json/translate?";
    final String api_key="trnsl.1.1.20151203T154217Z.ccc587945a4cc109.efa5a68bd521b44e9360f98a8613e343a366d583";
    String[] translation=new String[5];



    public queryTrans(){

    }

    public String detectLang(String query){
        String lang="";
        String urlStr=detectLangEndpoint+"key="+api_key+"&text="+query;
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
            lang=jobj.getString("lang");
        }catch(Exception e){
            e.printStackTrace();

        }
        //System.out.println(result);
        //System.out.println(lang);
        return lang;
    }

    public String[] translate(String query){
        String[] langs={"ar","en","de","fr","ru"};
        String fl=detectLang(query);
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
            String urlStr=translateEndpoint+"key="+api_key+"&text="+query+"&lang="+langs[j];
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
                e.printStackTrace();

            }
        }
        for(int k=0;k<translation.length;k++)
            System.out.println(translation[k]);

        return translation;
    }

    public static void main(String[] args){
        queryTrans q1=new queryTrans();
        //q1.detectLang();
        q1.translate("Париж атаки");
    }
}
