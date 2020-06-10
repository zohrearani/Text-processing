package ir.malek.newsanalysis.coreference;

import java.util.List;

import ir.malek.newsanalysis.ner.RuleBasedNER;
import ir.malek.newsanalysis.preprocess.Preprocess;
import ir.malek.newsanalysis.preprocess.Token;
import ir.malek.newsanalysis.preprocess.depparse.DependencyParser;
import ir.malek.newsanalysis.util.io.InFile;

public class TestCoreferenceResolution {

	public static void main(String[] args) {
		Preprocess preprocess;
		DependencyParser dependencyParser;
		RuleBasedNER ner = new RuleBasedNER();

		CoreferenceResolution2 CR = new CoreferenceResolution2();
		//String inputText = InFile.readFileText("..\\semantic\\resources\\coref\\sampleInput\\shahram.txt");
		String inputText = InFile.readFileText("C:\\Hamshahri 2007\\851011\\HAM2-851011-002.txt");
		preprocess = new Preprocess();
		dependencyParser = new DependencyParser();
		List<List<Token>> docTokens = preprocess.process(inputText);
		dependencyParser.process(docTokens);
		ner.setNER(docTokens);
		CR.setCoref(docTokens);
		for (int i = 0; i < docTokens.size(); i++) {
			for (Token token : docTokens.get(i)) {
				if (token.getCoref() == "") {
					System.out.println(token.word() + "\t" + token.lemma() + "\t" + "-" + "\t" + token.corefSet.toString());
				} else {
					System.out.println(token.word() + "\t" + token.lemma() + "\t" + token.getCoref() + "\t" + token.corefSet.toString());
				}
			}
		}

	}

}
