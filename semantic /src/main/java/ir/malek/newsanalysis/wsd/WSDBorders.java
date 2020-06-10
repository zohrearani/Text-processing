package ir.malek.newsanalysis.wsd;

import java.util.ArrayList;
import java.util.List;


public class WSDBorders {
	
	protected static double getCosineSimilitary(List<String> inputList1,List<String> inputList2){
		double similarity=0;
		
		List <Integer> tfList1=getTfList(inputList1);
		List <Integer> tfList2=getTfList(inputList2);
		
		double inputList1Value=getListAbsoluteValue(tfList1);
		double inputList2Value=getListAbsoluteValue(tfList2);
		if (tfList1==null || tfList2==null){
			return 0;
		}
		int inputListsDotProduct=getProductOfLists(tfList1, tfList2);
		double inputListsValueProduction=inputList1Value*inputList2Value;
		if (inputListsValueProduction==0){
			similarity=1000;
		}
		else {
			similarity=inputListsDotProduct/(inputList1Value*inputList2Value);
		}
		return similarity;
	}
	
	protected double getJaccardSimilarity(List<String> List1, List<String> List2){
		double similarTokens=0;
		double jaccardSimilarity=0;
		for (String token1:List1){
			if (List2==null || List2.size()==0){
				System.out.println();
			}
			for(String token2:List2){
				if (token1.equals(token2)){
					similarTokens++;
				}
			}
		}
		jaccardSimilarity=similarTokens/(List1.size()+List2.size()-similarTokens);
		return jaccardSimilarity;
	}
	
	
	
	
	private static List<Integer> getTfList(List<String> list) {
		if (list.equals(null) || list.size()==0){
			return null;
		}
		List<Integer> tfList=new ArrayList<Integer>();
		int arrayCounter=0;
		String eachWord = null;
		for (int index=0;index<list.size();index++){
			eachWord=list.get(index);
			int firstOccurence=list.indexOf(eachWord);
			if (firstOccurence==-1){
				tfList.add(arrayCounter,0);
				arrayCounter++;
			}
			if (index==firstOccurence){
				tfList.add(arrayCounter,1);
				arrayCounter++;
			}
			else if (tfList.contains(eachWord)){
				int occurence=tfList.indexOf(eachWord);
				tfList.set(occurence, tfList.get(occurence)+1);
			}
		}
		return tfList;
	}
	
	private static double getListAbsoluteValue(List<Integer> inputList){
		double ListAbsoluteValue=0;
		int squareElement=0;
		for (int eachElement : inputList){
			squareElement+=eachElement*eachElement;
		}
		ListAbsoluteValue=Math.sqrt(squareElement);
		return ListAbsoluteValue;
	}
	private static int getProductOfLists(List<Integer> inputList1,List<Integer> inputList2){
		int productValue=0;
		for (int i=0;i<inputList1.size();i++){
			productValue+=inputList1.get(i)*inputList2.get(i);
		}
		return productValue;
	}
	
	
	

}
