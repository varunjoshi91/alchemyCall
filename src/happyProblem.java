/**
 * Created by varunjoshi on 10/6/15.
 */
import java.io.PrintWriter;
import java.util.*;
import java.io.*;
public class happyProblem {

public int  recCall(char[] digits1){
int sumOfSquare=0;
    //System.out.println(digits1.length);
        for(int y=0; y < digits1.length ; y++)
        {

            sumOfSquare=sumOfSquare + Character.getNumericValue(digits1[y]) * Character.getNumericValue(digits1[y]);


        }
    //System.out.println(sumOfSquare);
    return sumOfSquare;
}

    public static void main(String[] args) {

        try{
        BufferedReader in = new BufferedReader(new FileReader("PracticeInput.txt"));
        String line = in.readLine();

            happyProblem newObj= new happyProblem();

            while(line != null){
                int i=0;
                int a=0;
                StringTokenizer tk = new StringTokenizer(line);
                a = Integer.parseInt(tk.nextToken());

                //String testString="500000";
                String testString=Integer.toString(a);

                char[] digits1 = testString.toCharArray();
                //System.out.println(digits1);



                int result=newObj.recCall(digits1);



                ArrayList A = new ArrayList();

                int index=0;
                int flag=0;
                while(result != 1){

                    if(flag == 1){
                        break;
                    }

                    for(int j=0;j<A.size();j++){
                        if(A.get(j).equals(result) ){
                            //System.out.println("Found similar "+ A.get(j));
                            flag=1;
                            break;
                        }
                    }
                    A.add(index, result);
                    index++;

                    digits1 = Integer.toString(result).toCharArray();
                    result=newObj.recCall(digits1);
                    //System.out.println("Surbhi "+result);
                }
                if(result == 1 && a==1){
                    System.out.println("happy " + (A.size()));
                }
                else if(result == 1){
                    System.out.println("happy " + (A.size() + 1));
                    //A.clear();
                }
                else{
                    System.out.println("unhappy " + (A.size()));
                    //A.clear();
                }

                //System.out.println("A: "+ A);
                line = in.readLine();

            }


        }



        catch(Exception e){
            System.out.println(e);
        }
//        i= Integer.parseInt(args[0]);



        try{
        PrintWriter writer = new PrintWriter("final_answer.txt"); //name of the file
        }
        catch(Exception E){
            System.out.println(E);
        }


        //System.out.println("Final digits1[i] " + sumOfSquare);




    }
}
