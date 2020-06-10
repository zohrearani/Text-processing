package ir.malek.newsanalysis.coref;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import ir.malek.newsanalysis.ner.RuleBasedNER;
import ir.malek.newsanalysis.preprocess.Token;
import ir.malek.newsanalysis.preprocess.normalizer.Normalizer;
import ir.malek.newsanalysis.util.collection.ListUtil;
import ir.malek.newsanalysis.util.collection.Pair;
import ir.malek.newsanalysis.util.enums.NERLabel;
import ir.malek.newsanalysis.util.enums.QuoteLabel;
import ir.malek.newsanalysis.util.io.IOUtils;
import ir.malek.newsanalysis.util.performance.NLPPerformance;

public class CorefResolution {
	Map<String, String> pronouns;
	Map<String, String> startingWords;
	private Normalizer normalizer;
	private InputStream pronounsInputStream = this.getClass().getClassLoader().getResourceAsStream("coref/pronouns.txt");
	private InputStream startingWordsInputStream = this.getClass().getClassLoader().getResourceAsStream("coref/startingWords.txt");
	public NLPPerformance performance = new NLPPerformance(TimeUnit.NANOSECONDS);

	public CorefResolution() {
		normalizer = new Normalizer("validChar.txt", "changingChar.txt");
		pronouns = readStringMap(pronounsInputStream);
		startingWords = readStringMap(startingWordsInputStream);
	}

	public void setMentions(List<List<Token>> docTokens) {
		Set<String> acceptablePOS = new HashSet<String>(Arrays.asList("N", "ADJ", "CONJ"));
		for (List<Token> senTokens : docTokens) {
			int j;
			for (int i = 0; i < senTokens.size(); i += j) {
				j = 1;
				// set NER mentions
				if (senTokens.get(i).getNer().toString().startsWith("B")) {
					while (i + j < senTokens.size() && senTokens.get(i + j).getNer().toString().startsWith("I"))
						j++;
					senTokens.get(i).setMentionLength(j);
					continue;
				}
				// set NED mentions
				if (senTokens.get(i).getNED().startsWith("Q")) {
					String NED = senTokens.get(i).getNED();
					while (i + j < senTokens.size() && senTokens.get(i + j).getNED().equals(NED))
						j++;
					senTokens.get(i).setMentionLength(j);
					continue;
				}
				// set pronoun mentions
				if (pronouns.containsKey(senTokens.get(i).word())) {
					senTokens.get(i).setMentionLength(1);
					continue;
				}
				// set mentions that starts with the startingWords
				if (startingWords.getOrDefault(senTokens.get(i).word(), "").equals("PER") && (i == 0 || senTokens.get(i - 1).word().equals("یک") == false)) {
					Set<Integer> mentionIndices = new HashSet<Integer>();
					mentionIndices.add(i);
					while ((i + j) < senTokens.size() && mentionIndices.contains(senTokens.get(i + j).dep.parent) && acceptablePOS.contains(senTokens.get(i + j).tag())) {
						mentionIndices.add(i + j);
						j++;
					}
					while (j > 1 && (senTokens.get(i + j - 1).tag().equals("CONJ") || senTokens.get(i + j - 1).tag().equals("PUNC"))) {
						j--;
					}
					if (j > 1)
						senTokens.get(i).setMentionLength(j);
					continue;
				}
				// set mentions such as این معلم
				if (senTokens.get(i).word().equals("این") && i < senTokens.size() - 1 && startingWords.getOrDefault(senTokens.get(i + 1).word(), "").equals("PER")) {
					Set<Integer> mentionIndices = new HashSet<Integer>();
					mentionIndices.add(i + 1);
					j++;
					while ((i + j) < senTokens.size() && mentionIndices.contains(senTokens.get(i + j).dep.parent)) {
						mentionIndices.add(i + j);
						j++;
					}
					while (senTokens.get(i + j - 1).tag().equals("CONJ") || senTokens.get(i + j - 1).tag().equals("PUNC")) {
						j--;
					}
					senTokens.get(i).setMentionLength(j);
					continue;
				}
			}
		}
	}

	public void setCoref(List<List<Token>> docTokens, boolean romoveSingleReferences) {
		long startTime = System.nanoTime();
		setMentions(docTokens);
		Map<String, String> prevRefs = new HashMap<String, String>(); // <key, "sentenceNumber-tokenNumber">    key: Qid or fullname or surname
		String lastOutOfQuotePersonRef = "-"; // آخرین شخص دیده شده در متن که خارج از نقل قول می باشد
		String lastInQuotePersonRef = "-"; // آخرین شخص دیده شده در متن که درون یک نقل قول می باشد
		Set<Pair<Integer, Integer>> toBeReferencedByNextRef = new HashSet<Pair<Integer, Integer>>();
		for (int i = 0; i < docTokens.size(); i++) {
			for (int j = 0; j < docTokens.get(i).size(); j++) {
				Token token = docTokens.get(i).get(j);
				if (token.getMentionLength() > 0) {
					if (token.getNED().startsWith("Q") || token.getNer() != NERLabel.O) { // if the current mention recognized by NED or NER it could be a reference
						// extract Qid, fullName and surname
						String Qid = token.getNED();
						String fullName = token.word();
						for (int k = 1; k < token.getMentionLength(); k++)
							fullName += " " + docTokens.get(i).get(j + k).word();
						String surname = findSurname(docTokens.get(i), j, token.getMentionLength());
						// search for Qid, fullName and surname in prevRefs. If found any of them, add the two others. 
						if (prevRefs.containsKey(Qid)) {
							prevRefs.put(fullName, prevRefs.get(Qid));
							if (surname.length() > 0)
								prevRefs.put(surname, prevRefs.get(Qid));
						} else if (prevRefs.containsKey(fullName)) {
							if (Qid.startsWith("Q"))
								prevRefs.put(Qid, prevRefs.get(fullName));
							if (surname.length() > 0)
								prevRefs.put(surname, prevRefs.get(fullName));
						} else if (prevRefs.containsKey(surname)) {
							if (Qid.startsWith("Q"))
								prevRefs.put(Qid, prevRefs.get(surname));
							prevRefs.put(fullName, prevRefs.get(surname));
						}
						// add the new reference if it doesn't exist
						if (prevRefs.containsKey(fullName) == false) {
							prevRefs.put(fullName, i + "-" + j);
							if (Qid.startsWith("Q"))
								prevRefs.put(Qid, i + "-" + j);
							if (surname.length() > 0)
								prevRefs.put(surname, i + "-" + j);
						}
						// set mention reference
						token.setMentionRef(prevRefs.get(fullName));
						// if the current mention is a person
						if (token.getNer() == NERLabel.B_PER) {
							if (token.getQuoteLabel() != QuoteLabel.DQ && token.getQuoteLabel() != QuoteLabel.IQ) { // if the person is out of quotation
								lastOutOfQuotePersonRef = prevRefs.get(fullName);
								// referencing previous mentions
								for (Pair<Integer, Integer> index : toBeReferencedByNextRef)
									docTokens.get(index.getLeft()).get(index.getRight()).setMentionRef(lastOutOfQuotePersonRef);
								toBeReferencedByNextRef.clear();
							} else { // if the person is in a quotation
								lastInQuotePersonRef = prevRefs.get(fullName);
							}
						}
					} else if (pronouns.containsKey(token.word())) { // else if the current mention is a pronoun
						if (token.word().equals("وی") || token.word().equals("ایشان") || token.word().equals("او")) { // ضمیر سوم شخص
							if (token.getQuoteLabel() != QuoteLabel.DQ && token.getQuoteLabel() != QuoteLabel.IQ) {
								token.setMentionRef(lastOutOfQuotePersonRef);
							} else {
								token.setMentionRef(lastInQuotePersonRef);
							}
						}
					} else { // it is a mention that starts with a startingWords
						if (startingWords.getOrDefault(token.word(), "").equals("PER") || (token.word().equals("این") && startingWords.getOrDefault(docTokens.get(i).get(j + 1).word(), "").equals("PER"))) {
							if (token.getQuoteLabel() != QuoteLabel.DQ && token.getQuoteLabel() != QuoteLabel.IQ) {
								if (lastOutOfQuotePersonRef.length() > 1)
									token.setMentionRef(lastOutOfQuotePersonRef);
								else
									toBeReferencedByNextRef.add(new Pair<Integer, Integer>(i, j));
							} else {
								if (lastInQuotePersonRef.length() > 1)
									token.setMentionRef(lastInQuotePersonRef);
								else
									toBeReferencedByNextRef.add(new Pair<Integer, Integer>(i, j));
							}
						}
					}
				}
			}
		}
		if (romoveSingleReferences) {
			removeSingleReferences(docTokens);
		}
		long endTime = System.nanoTime();
		performance.add(endTime - startTime, ListUtil.getSize(docTokens));
	}

	public void removeSingleReferences(List<List<Token>> docTokens) {
		Map<String, Integer> refCounts = new HashMap<String, Integer>();
		for (List<Token> senTokens : docTokens) {
			for (Token token : senTokens) {
				String ref = token.getMentionRef();
				if (ref.length() > 1) {
					refCounts.put(ref, refCounts.getOrDefault(ref, 0) + 1);
				}
			}
		}
		for (List<Token> senTokens : docTokens) {
			for (Token token : senTokens) {
				String ref = token.getMentionRef();
				if (refCounts.getOrDefault(ref, 0) <= 1) {
					token.setMentionRef("-");
				}
			}
		}
	}

	public static Map<String, Set<String>> getReferenceNames(List<List<Token>> docTokens) {
		Map<String, Set<String>> refNames = new HashMap<String, Set<String>>();
		for (List<Token> senTokens : docTokens) {
			for (int i = 0; i < senTokens.size(); i++) {
				String ref = senTokens.get(i).getMentionRef();
				if (ref.length() > 1) {
					String fullName = senTokens.get(i).word();
					for (int j = 1; j < senTokens.get(i).getMentionLength(); j++)
						fullName += " " + senTokens.get(i + j).word();
					if (refNames.containsKey(ref)) {
						refNames.get(ref).add(fullName);
					} else {
						Set<String> names = new HashSet<String>();
						names.add(fullName);
						refNames.put(ref, names);
					}
				}
			}
		}
		return refNames;
	}

	public static Map<String, String> getReferenceBestName(List<List<Token>> docTokens) {
		Map<String, String> refBestName = new HashMap<String, String>();
		for (List<Token> senTokens : docTokens) {
			for (int i = 0; i < senTokens.size(); i++) {
				String ref = senTokens.get(i).getMentionRef();
				if (ref.length() > 1 && (senTokens.get(i).getNer() != NERLabel.O || senTokens.get(i).getNED().startsWith("Q"))) {
					String fullName = senTokens.get(i).word();
					for (int j = 1; j < senTokens.get(i).getMentionLength(); j++)
						fullName += " " + senTokens.get(i + j).word();
					if (refBestName.getOrDefault(ref, "").length() < fullName.length()) {
						refBestName.put(ref, fullName);
					}
				}
			}
		}
		return refBestName;
	}

	public static int getMentionStartIndex(List<Token> senTokens, int index) {
		for (int i = 0; i < index; i++) {
			if (senTokens.get(i).getMentionLength() + i > index)
				return i;
		}
		return index;
	}

	private String findSurname(List<Token> senTokens, int startIndex, int length) {
		String surname = "";
		if (senTokens.get(startIndex).getNer() == NERLabel.B_PER)
			for (int k = 0; k < length && (senTokens.get(startIndex + k).getNer() == NERLabel.B_PER || senTokens.get(startIndex + k).getNer() == NERLabel.I_PER); k++)
				if (senTokens.get(startIndex + k).getNESubType().contains("LAST_NAME"))
					surname += " " + senTokens.get(startIndex + k).word();
		surname = surname.trim();
		return surname;
	}

	private Map<String, String> readStringMap(InputStream inputStream) {
		Map<String, String> map = new HashMap<String, String>();
		List<String> lines = IOUtils.linesFromFile(inputStream);
		for (String line : lines) {
			String[] parts = line.split("\t");
			map.put(normalizer.process(parts[0].trim()), parts[1].trim());
		}
		return map;
	}

}
