package ir.malek.newsanalysis.coreference;

import java.util.ArrayList;

import ir.malek.newsanalysis.preprocess.Token;

public class Mention {
	int code;
	int clusterCode;
	int lineage; // is this mention separated from another mention by removing tokens from its beginning?
	String type; //pr, NP, NE
	String SemanticType; //human, place, event, food, ...
	String number;
	String definite;
	int senNo;
	int tokenNo;
	int corefWith;
	String head;
	String NE;
	float salience;
	ArrayList<String> suffix = new ArrayList<String>();
	ArrayList<Token> tokens = new ArrayList<Token>();

	Mention(int codeV, int lineageV, String typeV, String NoV, String definiteV, String semTypeV, int senNoV, int tokenNoV) {
		code = codeV;
		clusterCode = -1;
		lineage = lineageV;
		type = typeV;
		number = NoV;
		definite = definiteV;
		senNo = senNoV;
		tokenNo = tokenNoV;
		corefWith = -1;
		SemanticType = semTypeV;
		head = "";
		NE = "";
		salience = 0;
	}

	String surface() {
		String s = "";
		for (Token t : tokens)
			s = s + t.word() + " ";
		return s;
	}
}
