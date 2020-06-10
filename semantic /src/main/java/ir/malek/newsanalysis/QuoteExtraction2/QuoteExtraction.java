package ir.malek.newsanalysis.QuoteExtraction2;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import ir.malek.newsanalysis.coref.CorefResolution;
import ir.malek.newsanalysis.preprocess.Token;
import ir.malek.newsanalysis.preprocess.normalizer.Normalizer;
import ir.malek.newsanalysis.util.collection.ListUtil;
import ir.malek.newsanalysis.util.enums.NERLabel;
import ir.malek.newsanalysis.util.enums.QuoteLabel;
import ir.malek.newsanalysis.util.io.IOUtils;
import ir.malek.newsanalysis.util.performance.NLPPerformance;

public class QuoteExtraction {
	Set<String> directQuoteVerbs = new HashSet<String>();
	Set<String> indirectQuoteVerbs = new HashSet<String>();
	List<String> pronouns = new ArrayList<String>(Arrays.asList("وی", "ایشان", "او"));

	private InputStream directQuoteVerbsInputStream = this.getClass().getClassLoader().getResourceAsStream("QE-Resources/directQuoteVerbs.txt");
	private InputStream indirectQuoteVerbsInputStream = this.getClass().getClassLoader().getResourceAsStream("QE-Resources/indirectQuoteVerbs.txt");
	private Normalizer normalizer;
	public NLPPerformance performance = new NLPPerformance(TimeUnit.NANOSECONDS);

	public QuoteExtraction() {
		normalizer = new Normalizer("validChar.txt", "changingChar.txt");
		directQuoteVerbs = loadStrSet(directQuoteVerbsInputStream);
		indirectQuoteVerbs = loadStrSet(indirectQuoteVerbsInputStream);
	}

	private List<String> createOneToMaxLengthConcatinatedTokens(List<Token> tokens, int startIndex, int maxLength) {
		int endIndex = (startIndex + maxLength) > tokens.size() ? tokens.size() : (startIndex + maxLength);
		List<String> concatTokens = new ArrayList<String>();
		concatTokens.add(removeSpaces(tokens.get(startIndex).word()));
		for (int i = startIndex + 1; i < endIndex; i++) {
			concatTokens.add(concatTokens.get(concatTokens.size() - 1) + removeSpaces(tokens.get(i).word()));
		}
		return concatTokens;
	}

	public void setQuote(List<List<Token>> docTokens) {
		long startTime = System.nanoTime();
		setDirectQuote(docTokens);
		setIndirectQuote(docTokens);
		setContinuousQuotes(docTokens);
		long endTime = System.nanoTime();
		performance.add(endTime - startTime, ListUtil.getSize(docTokens));
	}

	private void setDirectQuote(List<List<Token>> docTokens) {
		boolean nextSentenceIsQuote = false;
		int maxLength = 4;
		for (List<Token> senTokens : docTokens) {
			if (nextSentenceIsQuote) {
				for (int i = 0; i < senTokens.size(); i++) {
					senTokens.get(i).setQuoteLabel(QuoteLabel.DQ);
				}
				nextSentenceIsQuote = false;
			} else {
				for (int i = 0; i < senTokens.size(); i++) {
					senTokens.get(i).setQuoteLabel(QuoteLabel.O);
					List<String> concatTokens = createOneToMaxLengthConcatinatedTokens(senTokens, i, maxLength);
					for (int j = concatTokens.size() - 1; j >= 0; j--) {
						if (directQuoteVerbs.contains(concatTokens.get(j))) { // if the sentence contains a quotation verb
							int verbLoc = i + j - 1;
							setQuoter(senTokens, verbLoc, verbLoc);
							for (int k = i; k < i + j + 1; k++) { // labeling quotation verb
								senTokens.get(k).setQuoteLabel(QuoteLabel.DV);
							}
							for (int k = i + j + 1; k < senTokens.size(); k++) { // labeling quote
								senTokens.get(k).setQuoteLabel(QuoteLabel.DQ);
							}
							if ((i + j + 2) >= senTokens.size()) {
								nextSentenceIsQuote = true;
							}
							i = senTokens.size();
							break;
						}
					}
				}
			}
		}
	}

	private void setContinuousQuotes(List<List<Token>> docTokens) {
		// Search in docTokens to find continuous sentences in a quotation.
		// If the previous sentence is a quotation and the current sentence is in the same paragraph, the current sentence would be a quotation.
		for (int i = 1; i < docTokens.size(); i++) {
			boolean prevSentenceIsDirectQuote;
			if (docTokens.get(i - 1).size() > 0)
				prevSentenceIsDirectQuote = docTokens.get(i - 1).get(docTokens.get(i - 1).size() - 1).getQuoteLabel() == QuoteLabel.DQ;
			else
				prevSentenceIsDirectQuote = false;

			boolean currentSentenceIsQuote = false;
			for (Token token : docTokens.get(i)) {
				if (token.getQuoteLabel() != QuoteLabel.O) {
					currentSentenceIsQuote = true;
					break;
				}
			}
			if (prevSentenceIsDirectQuote && currentSentenceIsQuote == false) {
				for (Token token : docTokens.get(i)) {
					token.setQuoteLabel(QuoteLabel.DQ);
				}
			}
		}
	}

	private void setIndirectQuote(List<List<Token>> docTokens) {
		int maxLength = 7;
		for (List<Token> senTokens : docTokens) {
			for (int i = 0; i < senTokens.size(); i++) {
				if (senTokens.get(i).getQuoteLabel() == QuoteLabel.O) {
					List<String> concatTokens = createOneToMaxLengthConcatinatedTokens(senTokens, i, maxLength);
					for (int j = concatTokens.size() - 1; j >= 0; j--) {
						if (indirectQuoteVerbs.contains(concatTokens.get(j))) { // if the sentence contains a quotation verb
							for (int k = i; k < i + j + 1; k++) { // labeling quotation verb
								senTokens.get(k).setQuoteLabel(QuoteLabel.IV);
							}
							for (int k = i + j + 1; k < senTokens.size(); k++) { // labeling quote
								if (senTokens.get(k).getQuoteLabel() == QuoteLabel.O) {
									senTokens.get(k).setQuoteLabel(QuoteLabel.IQ);
								} else {
									if (senTokens.get(k - 1).word().equals("،")) // if the last word in the quote is comma, remove it from the quote
										senTokens.get(k - 1).setQuoteLabel(QuoteLabel.O);
									break;
								}
							}
							setQuoter(senTokens, i, i);
							i = senTokens.size();
							break;
						}
					}
				}
			}
		}
		// یافتن نقل قولهای غیرمستقیم بصورت: از ... خبر داد
		for (List<Token> senTokens : docTokens) {
			for (int i = 5; i < senTokens.size(); i++) {
				if (senTokens.get(i).word().equals("داد") && senTokens.get(i - 1).word().equals("خبر")) {
					int startOfQuote = getFirstDependent(senTokens, i, "VPP");
					if (startOfQuote >= 0) {
						for (int j = startOfQuote; j < i - 1; j++) {
							senTokens.get(j).setQuoteLabel(QuoteLabel.IQ);
						}
						senTokens.get(i - 1).setQuoteLabel(QuoteLabel.IV);
						senTokens.get(i).setQuoteLabel(QuoteLabel.IV);
						setQuoter(senTokens, i, startOfQuote);
					}

				}
			}
		}
		// یافتن نقل قولهای غیرمستقیم بصورت: به گفته (نامبری) (نقل قول)
		List<String> verbs = new ArrayList<String>(Arrays.asList("گفته", "اعتقاد", "نظر"));
		for (List<Token> senTokens : docTokens) {
			for (int i = 0; i < senTokens.size() - 5; i++) {
				if (senTokens.get(i).getQuoteLabel() == QuoteLabel.O && senTokens.get(i).word().equals("به") && verbs.contains(senTokens.get(i + 1).word()) && senTokens.get(i + 2).getMentionLength() > 0) {
					senTokens.get(i).setQuoteLabel(QuoteLabel.IV);
					senTokens.get(i + 1).setQuoteLabel(QuoteLabel.IV);
					int quoterLen = senTokens.get(i + 2).getMentionLength();
					for (int j = 0; j < quoterLen; j++) {
						senTokens.get(i + j + 2).setQuoteLabel(QuoteLabel.A);
					}
					for (int j = i + quoterLen + 2; j < senTokens.size(); j++) {
						senTokens.get(j).setQuoteLabel(QuoteLabel.IQ);
					}
				}
			}
		}
	}

	private void setQuoter(List<Token> senTokens, int verbLoc, int maxLocForSearch) {
		int quoterLoc = -1;
		for (int i = 0; i < maxLocForSearch; i++) { // finding quoter and label it
			if (senTokens.get(i).getMentionLength() > 0 && senTokens.get(i).getNer() != NERLabel.B_LOC) { // try to find quoter by mentions
				for (int j = 0; j < senTokens.get(i).getMentionLength(); j++)
					senTokens.get(i + j).setQuoteLabel(QuoteLabel.A);
				break; // Don't replace this break by else-if
			}
			if (senTokens.get(i).dep.parent == verbLoc && senTokens.get(i).dep.depRel.equals("SBJ")) { // if not found, try to find it by subject of quotation verb
				quoterLoc = CorefResolution.getMentionStartIndex(senTokens, i);
				int quoterLen = senTokens.get(quoterLoc).getMentionLength();
				if (quoterLen > 0) { // if the subject of quotation verb is part of a mention, tag that mention as the quoter
					for (int j = quoterLoc; j < quoterLoc + quoterLen; j++)
						senTokens.get(j).setQuoteLabel(QuoteLabel.A);
				} else {
					senTokens.get(quoterLoc).setQuoteLabel(QuoteLabel.A);
					while (senTokens.get(++quoterLoc).dep.parent == quoterLoc - 1) {
						senTokens.get(quoterLoc).setQuoteLabel(QuoteLabel.A);
					}
					if (i > 0 && senTokens.get(i - 1).word().equals("این")) {
						senTokens.get(i - 1).setQuoteLabel(QuoteLabel.A);
					}
				}
				// do'nt break hear! quotation verb may have more than one subject
			}
		}
	}

	private int getFirstDependent(List<Token> senTokens, int index, String dependency) {
		for (int i = 0; i < senTokens.size(); i++) {
			if (senTokens.get(i).dep.parent == index && senTokens.get(i).dep.depRel.equals(dependency)) {
				return i;
			}
		}
		return -1;
	}

	private Set<String> loadStrSet(InputStream inputStream) {
		Set<String> set = new HashSet<String>();
		List<String> lines = IOUtils.linesFromFile(inputStream);
		for (String line : lines) {
			set.add(removeSpaces(normalizer.process(line)));
		}
		return set;
	}

	private String removeSpaces(String str) {
		return str.replace(" ", "").replace("\u200c", "").replace("\u200f", "");
	}

}
