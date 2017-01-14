package ir.part3;

import org.jetbrains.annotations.TestOnly;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by rajanishivarajmaski1 on 10/17/16.
 * https://en.wikipedia.org/wiki/Cohen%27s_kappa
 */
public class ComputeKappa {

    //file directory path
    private static String directoryPath = "/Users/rajanishivarajmaski1/University/CSC849_Search/assignment_4/";

    /**
     * File contains [QryID] 0 [DocID] [Relevance] each line. [Relevance is binary(0 or 1)
     * As stated on wiki this method counts the agreement/disagreement between two raters who each classify N items into C mutually exclusive categories"
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        BufferedReader reader1 = new BufferedReader(new FileReader(new File(directoryPath + "maski-qrels.txt")));
        BufferedReader reader2 = new BufferedReader(new FileReader(new File("/Users/rajanishivarajmaski1/Downloads/trubov-qrels.txt")));
        double a = 0, b = 0, c = 0, d = 0;
        //TODO: It is currently presuming number of items to be 30, so loop iteration is hardcoded. It should actually be equal to file.linecount()
        for (int i = 0; i < 30; i++) {
            String[] values1 = reader1.readLine().split(" ");
            String[] values2 = reader2.readLine().split(" ");
            if (values1[3].equals(values2[3])) {
                if (values1[3].equals("1"))
                    a++;
                else
                    d++;
            } else if (values1[3].equals("1") && values2[3].equals("0")) {
                b++;
            } else if (values1[3].equals("0") && values2[3].equals("1")) {
                c++;
            }

        }
        System.out.println("kappa statistics " + getKappa(a,b,c,d));

    }

    private static double getKappa(double a, double b, double c, double d) {
        double po = (a + d) / (a+b+c+d);
        double  marginala  = ((a + b)*(a+c)) / (a+b+c+d);
        double marginalb = ((c + d)*(b+d)) / (a+b+c+d) ;
        double pe = (marginala + marginalb) / (a+b+c+d);
        return (po - pe) / (1 - pe);
    }

    @TestOnly
    private void testKappa(){
        double  a=61, b=2, c=6, d=25;
        System.out.println("kappa " + getKappa(a,b,c,d));
    }
}
