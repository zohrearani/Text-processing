package ir.malek.newsanalysis.ner;

import java.util.List;
import java.util.Map;

import ir.malek.newsanalysis.ner.EntitySubType;
import ir.malek.newsanalysis.ner.RuleBasedNER;
import ir.malek.newsanalysis.preprocess.Preprocess;
import ir.malek.newsanalysis.preprocess.Token;
import ir.malek.newsanalysis.preprocess.depparse.DependencyParser;
import ir.malek.newsanalysis.semantic.classification.VerbClassifier;
import ir.malek.newsanalysis.semantic.srl.SRL;
import ir.malek.newsanalysis.semantic.srl.SRLRelation;
import ir.malek.newsanalysis.semantic.srl.SemanticRoleExtractor;
import ir.malek.newsanalysis.util.io.InFile;
import ir.malek.newsanalysis.util.io.OutFile;

public class RuleBasedNERTest {

	public static void main(String[] args) {

	}

//		System.out.println("\nLocations:\n");
//		for (Map.Entry<String, EntitySubType> loc : NER.getLocEntities().entrySet()) {
//			System.out.println(loc.getKey() + "\t" + loc.getValue());
//		}
//
//		System.out.println("\nOrganizations:\n");
//		for (Map.Entry<String, EntitySubType> org : NER.getOrgEntities().entrySet()) {
//			System.out.println(org.getKey() + "\t" + org.getValue());
//		}
//
//		System.out.println("\nPerson:\n");
//		for (Map.Entry<String, EntitySubType> per : NER.getPerEntities().entrySet()) {
//			System.out.println(per.getKey() + "\t" + per.getValue());
//		}

	public static void mirasText(){
		Preprocess preprocess=new Preprocess();
		RuleBasedNER ner = new RuleBasedNER();
		InFile inFile=new InFile("C:\\Users\\Arani\\Desktop\\MirasText\\MirasText-00");
		OutFile outFile=new OutFile("miras" + 0 + ".conll");;
		String news="";
		int i=0;
		while ((news=inFile.readLine())!=null && i++<500) {
            if (i% 100 == 0) {
                outFile.close();
                outFile = new OutFile("miras" + i / 100 + ".conll");
            }
            System.out.println("news " + i + "...");
            String newsText = news.split("[*][*][*]")[0];
            String conllStr = "";
            List<List<Token>> newsTokens = preprocess.process(newsText);
            ner.setNER(newsTokens);
            for (List<Token> sentenceTokens : newsTokens) {
                for (Token token : sentenceTokens) {
                    conllStr += token.word() + "\t";
                    conllStr += token.lemma() + "\t";
                    conllStr += token.tag() + "\t";
                    conllStr += token.getNer() + "\n";
                }
                conllStr += "\n";
                outFile.println(conllStr);
            }

        }
	}


}
