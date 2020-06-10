
package ir.malek.newsanalysis.wsd;

import ir.malek.newsanalysis.preprocess.Preprocess;
import ir.malek.newsanalysis.preprocess.Token;

import ir.mitrc.corpus.api.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WSD {

	static ApiFactory api;
	static Preprocess pereprocess;
	static WSDBorders wsdBorders = new WSDBorders();
	static HashMap<String, List<String>> synsetUri2ArtificialText;
	static String path = "..//semantic//src//main//resources//wsdSynToAText.txt";
	private static final Log LOG = LogFactory.getLog(WSD.class);

	public WSD(ApiFactory api, Preprocess preprocess, boolean update) {
		long startTime = System.currentTimeMillis();
		System.err.print("Adding annotator wsd ... ");
		WSD.api = api;
		WSD.pereprocess = preprocess;

		if (update) {
			createSynsetUri2ArtificialText();
		} else {
			loadSynsetUri2ArtificialText(path);
		}
		long totalTime = System.currentTimeMillis() - startTime;
		System.err.println("done [" + totalTime / 1000.0 + " sec].");

	}
	
	public WSD(ApiFactory api){
		WSD.api = api;
		loadSynsetUri2ArtificialText(path);
	}

	public WSD() {
		api=new ApiFactory();
		loadSynsetUri2ArtificialText(path);
	}

	private void loadSynsetUri2ArtificialText(String path) {

		FileInputStream wsdFile;
		try {
			wsdFile = new FileInputStream(new File(path));
			ObjectInputStream oi = new ObjectInputStream(wsdFile);
			synsetUri2ArtificialText = (HashMap<String, List<String>>) oi.readObject();
			oi.close();
			//TODO check shavad!
		} catch (IOException | ClassNotFoundException e) {
			System.err.println("cannot read wsd file! we will create it in a few minites");
			e.printStackTrace();
			createSynsetUri2ArtificialText();
		}

	}

	public void setWSD(List<List<Token>> docTokens) {
		List<String> inputTextList = new ArrayList<>();
		for (List<Token> senTokens : docTokens) {
			for (Token token : senTokens) {
				if (!token.stopWord() && !token.tag().equals("PUNC")) {
					inputTextList.add(token.lemma());
				}
			}
		}
		HashMap<String, String> map = new HashMap<>();
		String selectedSynset = null;
		Isynset synset = null;
		for (int i = 0; i < inputTextList.size(); i++) {
			map.put(inputTextList.get(i), disambiguate(inputTextList, inputTextList.get(i), true));
		}
		for (List<Token> senTokens : docTokens) {
			for (Token token : senTokens) {
				selectedSynset = map.get(token.lemma());
				if (selectedSynset != null) {
					token.setSense(selectedSynset);
					synset = ApiFactory.getSynset(selectedSynset);
					if (synset == null) {
						token.setSense(null);
						continue;
					}
				}
			}
		}
	}

	public List<WsdTuple> disambiguateText(String inputText, boolean isLemma) {
		List<String> inputTextList = createArtificialList(inputText);
		LOG.info("artificial text is successfully created for input text :D");
		// System.out.print("artificial text is successfully created:D");
		List<WsdTuple> tupleArrayOutput = new ArrayList<WsdTuple>();
		String selectedSynset = null;
		Isynset synset = null;
		String synsetUri = null;
		ArrayList<Term> synsetTerms = new ArrayList<Term>();

		tupleArrayOutput = calculateTf(inputTextList);
		LOG.info("TF vector for input text is successfully created :D");
		// System.out.println("TF vector is successfully created for input
		// text");
		for (int i = 0; i < tupleArrayOutput.size(); i++) {
			selectedSynset = disambiguate(inputTextList, tupleArrayOutput.get(i).lemma, isLemma);

			// in dastoor bayad hazf shavad: (niyaz be cheke bishtar darad)
			synset = ApiFactory.getSynset(selectedSynset);
			if (synset == null) {
				tupleArrayOutput.get(i).setSynsetUri(null);
				continue;
			}
			synsetUri = synset.getUri();
			synsetTerms = synset.listTerms();
			tupleArrayOutput.get(i).setTerms(synsetTerms);
			tupleArrayOutput.get(i).setSynsetUri(synsetUri);
		}

		return tupleArrayOutput;
	}

	private ArrayList<WsdTuple> calculateTf(List<String> inputTextList) {
		ArrayList<WsdTuple> tupleArrayOutput = new ArrayList<WsdTuple>();
		String lemma = null;
		int firstOccurence = -1;
		for (int i = 0; i < inputTextList.size(); i++) {
			lemma = inputTextList.get(i);
			firstOccurence = inputTextList.indexOf(lemma);
			if (firstOccurence == i) {
				tupleArrayOutput.add(new WsdTuple(lemma, null, 1, null));

			} else {
				tupleArrayOutput.add(new WsdTuple(lemma, null, 0, null));
				tupleArrayOutput.get(firstOccurence).inputBasedTf += 1;

			}
		}
		WsdTuple eachTuple = null;
		for (int i = 0; i < tupleArrayOutput.size(); i++) {
			eachTuple = tupleArrayOutput.get(i);
			if (eachTuple.inputBasedTf == 0) {
				tupleArrayOutput.remove(eachTuple);
				i--;
			}
		}
		return tupleArrayOutput;
	}

	public WsdTuple disambiguateWord(String inputText, String lemma, boolean isLemma) {

		List<String> inputTextList = createArtificialList(inputText);
		int firstOccurrence = inputTextList.indexOf(lemma);
		if (firstOccurrence == -1 || lemma.equals(null) || lemma.isEmpty()) {
			return null;
		}
		int lemmaTf = 0;
		String selectedSynsetUri = null;
		String eachInputLemma = null;
		for (int i = 0; i < inputTextList.size(); i++) {
			eachInputLemma = inputTextList.get(i);
			if (eachInputLemma.equals(lemma) && i == firstOccurrence) {
				selectedSynsetUri = disambiguate(inputTextList, lemma, isLemma);
				if (selectedSynsetUri == null) {
					return null;
				}
				lemmaTf++;
			}
			if (eachInputLemma.equals(lemma) && i != firstOccurrence) {
				lemmaTf++;
			}
		}
		@SuppressWarnings("static-access")
		Isynset selectedSynst = api.getSynset(selectedSynsetUri);
		if (selectedSynst.equals(null)) {
			return null;
		}
		ArrayList<Term> synsetTerms = selectedSynst.listTerms();
		WsdTuple selectedTuple = new WsdTuple(lemma, selectedSynsetUri, lemmaTf, synsetTerms);
		return selectedTuple;
	}

	private String disambiguate(List<String> inputTextList, String word, boolean isLemma) {
		ArrayList<List<String>> artificialTexts = new ArrayList<List<String>>();
		ArrayList<String> selectedSynsetUris = new ArrayList<String>();
		ArrayList<String> wordUris = api.searchOntWord(word, isLemma);
		if (wordUris == null || wordUris.size() == 0) {
			return null;
		}
		ArrayList<String> eachWordSynsetUris = new ArrayList<String>();
		List<String> artificialText = null;
		for (String eachWordUri : wordUris) {
			eachWordSynsetUris = api.listSynsetUriOfWordUri(eachWordUri);

			for (String eachSynsetUri : eachWordSynsetUris) {
				artificialText = synsetUri2ArtificialText.get(eachSynsetUri);
				if (artificialText == null)
					continue;
				artificialTexts.add(artificialText);
				selectedSynsetUris.add(eachSynsetUri);
			}

		}
		if (selectedSynsetUris.size() == 0)
			return null;
		if (selectedSynsetUris.size() < 2)
			return selectedSynsetUris.get(0);

		int id = compareLists(inputTextList, artificialTexts);
		return selectedSynsetUris.get(id);
	}

	private int compareLists(List<String> inputTextList, ArrayList<List<String>> artificialTexts) {

		int id = 0;
		double maxSimilarity = 0;
		double similarity = 0;
		for (int i = 0; i < artificialTexts.size(); i++) {
			similarity = wsdBorders.getJaccardSimilarity(inputTextList, artificialTexts.get(i));
			if (similarity > maxSimilarity) {
				maxSimilarity = similarity;
				id = i;
			}
		}

		return id;
	}

	private static String createArtificialString(String synsetUri) {

		String createText = "";
		Isynset synset = ApiFactory.getSynset(synsetUri);
		if (synset == null) {
			return "";
		}
		ArrayList<String> relatedUri = synset.listAllRelatedSynsets();
		relatedUri.add(synsetUri);
		Isynset relatedSynset = null;
		ArrayList<String> words = new ArrayList<String>();
		if (relatedUri != null || (relatedUri.size() > 0)) {
			for (int l = 0; l < relatedUri.size(); l++) {
				relatedSynset = new Synset(relatedUri.get(l));
				words = relatedSynset.listWordsOfSynset();
				if (words == null) {
					continue;
				}
				for (String eachWord : words) {
					createText = createText + " " + eachWord + " ";
				}
			}
		}
		createText += synset.getGloss() + synset.getExample() + " ";
		return createText.replace("null", "");
	}

	private static List<String> createArtificialList(String text) {
		List<String> list = new ArrayList<String>();
		List<List<Token>> tokens = pereprocess.process(text);
		String lemma = null;
		for (List<Token> tokenList : tokens) {
			for (Token eachToken : tokenList) {
				if (eachToken.stopWord() != true && (!eachToken.tag().equals("PUNC"))) {
					lemma = eachToken.lemma();
					list.add(lemma);
				}
			}
		}
		return list;

	}

	private static void createSynsetUri2ArtificialText() {
		List<String> allSynsetUris = api.listAllSynsets();
		LOG.info("list of all synset uris is successfully created :D");
		// System.out.println("list of all synsetUris is successfully created
		// :D");
		synsetUri2ArtificialText = new HashMap<String, List<String>>();
		// System.out.println("allsynsetUris: "+allSynsetUris.size());
		String artificalText = "";
		List<String> wordList = null;
		for (String synUri : allSynsetUris) {
			artificalText = createArtificialString(synUri);
			wordList = createArtificialList(artificalText);
			if (wordList == null) {
				// System.out.println("null wordList created!!");
			} else {
				synsetUri2ArtificialText.put(synUri, wordList);
			}
		}

		// System.out.println("synset2...: "+counter+" ***** "+
		// synsetUri2ArtificialText.size());

		try {
			FileOutputStream wsdFile = new FileOutputStream(new File(path));
			ObjectOutputStream o = new ObjectOutputStream(wsdFile);

			o.writeObject(synsetUri2ArtificialText);
			o.close();
		} catch (IOException e) {
			System.err.println("cannot write wsd file!");
			e.printStackTrace();
		}

	}
}
