package ir.malek.newsanalysis.semantic.srl;

import ir.malek.newsanalysis.util.io.InFile;
import ir.malek.newsanalysis.util.io.OutFile;

import java.util.ArrayList;

public class ConllComparison {
    static InFile file1=new InFile("c1.txt");
    static InFile file2=new InFile("c2.txt");
    OutFile outFile=new OutFile("result.txt");
    public static void main(String [] args){
        ArrayList<String> column1 = new ArrayList<>();
        String miras1;
        while (!(miras1=file1.readLine()).equals(null) || (miras1.equals(""))){
            column1.add(miras1);
        }
        ArrayList<String> column2 = new ArrayList<>();
        String miras2;
        while (!(miras2=file2.readLine()).equals(null) || (miras2.equals(""))){
            column2.add(miras2);
        }
        compriseTokenizer((String[])column1.toArray(),(String[])column2.toArray());

    }
    public static void compriseTokenizer(String[] column1, String[] column2){
        int index1=0;
        int index2=0;
        String [] result=new String[column1.length];
        while (index1<column1.length && index2<column2.length) {
            if (column1[index1].equals(column2[index2])) {
                index1++;
                index2++;
                result[index1]="equal";
                break;
            }
            if (column1[index1].length()==column2[index2].length()){
                result[index1]="normalization not match.";
            }
            if (column1[index1].length()<column2[index2].length()){
                if (column2[index2].startsWith(column1[index1]) || column2[index2].contains(column1[index1]) ) {
                    if (index1<column1.length-2) {
                        index1++;
                        result[index1] = "shorter";
                        break;
                    }
                }
                if (column2[index2].endsWith(column1[index1])) {
                    index1++;
                    index2++;
                    break;
                }
                int tempIndex=index1;
                while (!column1[tempIndex].equals(column2[index2])&&tempIndex<column1.length) {
                    tempIndex++;
                }
                if (tempIndex<column1.length) {
                    index1 = tempIndex;
                    result[index1]="shorter";
                    break;
                }
                else {
                    tempIndex = index2;
                    while (!column1[index1].equals(column2[tempIndex])&&tempIndex<column2.length) {
                        tempIndex++;
                    }
                    if (tempIndex<column2.length) {
                        index2 = tempIndex;
                        result[index1]="longer";
                        break;
                    }
                }
            }
            if (column1[index1].length()>column2[index2].length()){
                if (column1[index1].startsWith(column2[index2]) || column1[index1].contains(column2[index2]) ) {
                    index2++;
                    result[index1] = "longer";
                    break;
                }
                if (column1[index1].endsWith(column2[index2])) {
                    index1++;
                    index2++;
                    break;
                }
                int tempIndex=index1;
                while (!column1[tempIndex].equals(column2[index2])&& tempIndex<column1.length) {
                    tempIndex++;
                }
                if (tempIndex<column1.length) {
                    index1 = tempIndex;
                    result[index1]="shorter";
                    break;
                }
                else {
                    tempIndex = index2;
                    while (!column1[index1].equals(column2[tempIndex])&&tempIndex<column2.length) {
                        tempIndex++;
                    }
                    if (tempIndex<column2.length) {
                        index2 = tempIndex;
                        result[index1]="longer";
                        break;
                    }
                }

            }

        }
    }
}
