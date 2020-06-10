package ir.malek.newsanalysis.ned;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ir.malek.newsanalysis.ner.RuleBasedNER;
import ir.malek.newsanalysis.preprocess.Token;
import ir.malek.newsanalysis.preprocess.normalizer.Normalizer;
import ir.malek.newsanalysis.util.collection.ListUtil;
import ir.malek.newsanalysis.util.enums.NERLabel;
import ir.malek.newsanalysis.util.io.IOUtils;
import ir.malek.newsanalysis.util.performance.NLPPerformance;
import ir.malek.newsanalysis.util.string.StringUtil;

public class WikidataNED {
	private HashMap<String, String> labelToQ;

	private InputStream labelToQInputStream = this.getClass().getClassLoader().getResourceAsStream("NED-Resources/labelToQ_POL2.txt");
	private Normalizer normalizer;
	public NLPPerformance performance = new NLPPerformance(TimeUnit.NANOSECONDS);

	public WikidataNED() {
		normalizer = new Normalizer("validChar.txt", "changingChar.txt");
		loadLabelToQ(labelToQInputStream);
	}

	private List<String> createOneToMaxLengthConcatinatedTokens(List<Token> tokens, int startIndex, int maxLength) {
		int endIndex = (startIndex + maxLength) > tokens.size() ? tokens.size() : (startIndex + maxLength);
		List<String> concatTokens = new ArrayList<String>();
		concatTokens.add(tokens.get(startIndex).word());
		for (int i = startIndex + 1; i < endIndex; i++)
			if (tokens.get(i).tag().equals("PUNC")) // if token(i) is punctuation, concatenate it without space
				concatTokens.add(concatTokens.get(concatTokens.size() - 1) + tokens.get(i).word());
			else
				concatTokens.add(concatTokens.get(concatTokens.size() - 1) + " " + tokens.get(i).word());
		return concatTokens;
	}

	public void setNED(List<List<Token>> docTokens) {
		long startTime = System.nanoTime();

		int maxLength = 7;
		for (List<Token> senTokens : docTokens) {
			for (int i = 0; i < senTokens.size();) {

				// find maximum match
				String ID = "O"; // Q id in wikidata
				List<String> concatTokens = createOneToMaxLengthConcatinatedTokens(senTokens, i, maxLength);
				String fullNameByNER = getFullNameByNER(senTokens, i);
				int j;
				for (j = concatTokens.size() - 1; j >= 0; j--) {
					if (fullNameByNER == null || StringUtil.removeSpaces(concatTokens.get(j)).equals(StringUtil.removeSpaces(fullNameByNER))) {
						ID = labelToQ.getOrDefault(concatTokens.get(j), "O");
						if (j == 0) { // for single-word-phrases:
							if (senTokens.get(i).getNer() == NERLabel.O) { // if NER not recognized a name
								ID = "O";
							}
							if (senTokens.get(i).getNESubType().equals("IRANIAN_FIRST_NAME")) { // or if NER taged it as IRANIAN_FIRST_NAME
								ID = "O";
							}
							if (senTokens.get(i).getNESubType().equals("IRANIAN_LAST_NAME")) { // or if NER taged it as IRANIAN_LAST_NAME
								if (i > 0 && (senTokens.get(i - 1).getNer() == NERLabel.B_PER || senTokens.get(i - 1).getNer() == NERLabel.I_PER)) { // and if NER tag that token as part of a multi-token-name
									ID = "O";
								}
								if (i < senTokens.size() - 1 && senTokens.get(i + 1).getNer() == NERLabel.I_PER) { // and if NER tag that token as part of a multi-token-name
									ID = "O";
								}
							}
						}
						if (ID.equals("O") == false)
							break;
					}
				}

				// labeling NED
				// current value of j is (the length of maximum match)-1
				do {
					senTokens.get(i).setNED(ID);
					j--;
					i++;
				} while (j >= 0);
			}
		}
		// calculate time
		long endTime = System.nanoTime();
		performance.add(endTime - startTime, ListUtil.getSize(docTokens));
	}

	private String getFullNameByNER(List<Token> senTokens, int i) {
		if (senTokens.get(i).getNer().toString().startsWith("I") && i>0 && senTokens.get(i-1).getNESubType().equals("SURN")==false) {
			return "";
		}
		if (senTokens.get(i).getNer().toString().startsWith("B") || (senTokens.get(i).getNer().toString().startsWith("I") && i>0  && senTokens.get(i-1).getNESubType().equals("SURN"))) {
			String fullName = senTokens.get(i).word();
			for (int j = i + 1; j < senTokens.size() && senTokens.get(j).getNer().toString().startsWith("I"); j++) {
				fullName += senTokens.get(j).word();
			}
			return fullName;
		} else {
			return null;
		}
	}

	private void loadLabelToQ(InputStream inputStream) {
		labelToQ = new HashMap<String, String>();
		List<String> lines = IOUtils.linesFromFile(inputStream);
		String[] parts;
		for (String line : lines) {
			parts = line.split("\t");
			if (parts.length == 2) {
				labelToQ.put(normalizer.process(parts[0]), parts[1]);
			}
		}
	}

}
