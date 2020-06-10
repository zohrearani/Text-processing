package ir.malek.newsanalysis.coref;

import java.util.List;

import ir.malek.newsanalysis.ned.WikidataNED;
import ir.malek.newsanalysis.ner.RuleBasedNER;
import ir.malek.newsanalysis.preprocess.Preprocess;
import ir.malek.newsanalysis.preprocess.Token;
import ir.malek.newsanalysis.preprocess.depparse.DependencyParser;
import ir.malek.newsanalysis.util.io.InFile;

public class TestCorefResolution {
	public static void main(String[] args) {
		Preprocess preprocess = new Preprocess();
		DependencyParser dependencyParser = new DependencyParser();
		RuleBasedNER ner = new RuleBasedNER();
		WikidataNED NED = new WikidataNED();
		// QuoteExtraction2 QE = new QuoteExtraction2();

		CorefResolution CR = new CorefResolution();

		String inputText = InFile.readFileText("C:\\Hamshahri 2007\\851011\\HAM2-851011-005.txt");
		List<List<Token>> docTokens = preprocess.process(inputText);
		dependencyParser.process(docTokens);
		ner.setNER(docTokens);
		NED.setNED(docTokens);
		

		CR.setCoref(docTokens,true);
		for (int i = 0; i < docTokens.size(); i++) {
			for (Token token : docTokens.get(i)) {
				System.out.println(token.word() + "\t" + token.lemma() + "\t" + token.getMentionLength() + "\t" + token.getMentionRef());
			}
		}

	}

}
