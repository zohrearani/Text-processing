package ir.malek.newsanalysis.semantic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.pipeline.Annotation;
import ir.malek.newsanalysis.preprocess.CoreAnnotations;
import ir.malek.newsanalysis.preprocess.lemmarizer.Lemmatizer;
import org.maltparser.concurrent.graph.ConcurrentDependencyGraph;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;

import ir.malek.newsanalysis.QuoteExtraction2.QuoteExtraction;
import ir.malek.newsanalysis.ned.WikidataNED;
import ir.malek.newsanalysis.ner.RuleBasedNER;
import ir.malek.newsanalysis.preprocess.Preprocess;
import ir.malek.newsanalysis.preprocess.Token;
import ir.malek.newsanalysis.preprocess.depparse.DependencyParser;
import ir.malek.newsanalysis.representation.sem.NewsOnt;
import ir.malek.newsanalysis.semantic.classification.SemanticClassifier;
import ir.malek.newsanalysis.semantic.srl.SRL;
import ir.malek.newsanalysis.semantic.srl.SRLRelation;
import ir.malek.newsanalysis.time.TimeNormalizer;
import ir.malek.newsanalysis.util.enums.NERLabel;
import ir.malek.newsanalysis.util.io.IOUtils;
import ir.malek.newsanalysis.util.io.InFile;
import ir.malek.newsanalysis.util.io.OutFile;
import ir.malek.newsanalysis.wsd.WSD;
import ir.mitrc.corpus.api.ApiFactory;

/**
 * Unit test for simple App.
 */
public class AppTest {
	public static void main(String[] arg) {
		// testMutFromFaWordNet();
		// getVerb();
		// getGenre();
		//String inputText = "تجربه ای که من داشتم این بود که به تعدادی از میوه ها حمله برده شد.";
		//nerWsdTime(inputText);
		//khosravi();
        lemmatizeTabnak("/home/arani/Desktop/tabnak_v1.0.0/");

	}

	private static void lemmatizeTabnak(String folderPath) {
        String path ="";// "../preprocess/src/main/resources/";
        Lemmatizer lemmatizer = new Lemmatizer(path+"wordLemma.txt",
                path+"wordTag.txt",path+"lemmatizingRules.txt",
                path+"pluralNounEx.txt",path+"verbStem.txt", true);

        try {
            File rootFile = new File(folderPath);
            File[] inputFiles = rootFile.listFiles();
            if (inputFiles != null) {
                for (File f : inputFiles) {
                    List<String> outputLines = new ArrayList<>();
                    if (f.isDirectory())
                        continue;
                    List<String> lines = Files.readAllLines(Paths.get(f.getPath()));
                    List<Token> tokens = new ArrayList<>();
                    for (String line : lines) {
                        if (line.startsWith("#")) {
                            outputLines.add(line);
                            continue;
                        }
                        if (line.trim().isEmpty()) {
                            tokens = lemmatizer.lemmatize(tokens);
                            for (Token token:tokens){
                                String match = "";
                                outputLines.add(token.getWordNo()+"\t"+token.word()+"\t"+token.getFeat()+"\t"+token.lemma()
                                );
                            }
                            tokens = new ArrayList<>();
                        } else {
                            String[] parts = line.split("\t");
                            if (parts.length==4) {
                                String word = parts[1];
                                String pos = parts[2].replace("e","" );
                                pos = pos.replace("PRO","PR" ).replace("AJ","ADJ");
                                if (pos.equals("P")) pos="PREP";

                                Token token = new Token();
                                token.setWord(word.replaceAll("ئ", "ی")
                                .replaceAll("ؤ","و").replaceAll("أ","ا" ));
                                token.setTag(pos);
                                token.setWordNo(Integer.valueOf(parts[0]));
                                token.setFeat(parts[2]);
                                token.setCoref(parts[3]);
                                tokens.add(token);
                            }
                        }
                    }
                    if (Files.notExists(Paths.get(folderPath+"/news/"+f.getName()))){
                        Files.createFile(Paths.get(folderPath+"/news/"+f.getName()));
                    }
                    Files.write(Paths.get(folderPath+"/news/"+f.getName()), outputLines);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void nerWsdTime(String inputText) {
		Preprocess preprocess=new Preprocess();
		DependencyParser depParser = new DependencyParser();
		List<List<Token>> inputSentences = preprocess.process(inputText);
		for (List<Token> tokensOfSentence : inputSentences) {
			ConcurrentDependencyGraph graph = depParser.rawParse(tokensOfSentence);
			if (graph == null)
				continue;
			for (int i = 0; i < tokensOfSentence.size(); i++) {
				Token token = tokensOfSentence.get(i);
				token.setDep(graph.getDependencyNode(i + 1).toString());
			}
		}
		RuleBasedNER namedEntityAnnotator= new RuleBasedNER();
		namedEntityAnnotator.setNER(inputSentences);
	//	TimeNormalizer timeNormalizer=new TimeNormalizer();
	//	timeNormalizer.process(inputSentences, "2017/05/12", "168294");
		ApiFactory api=new ApiFactory();
	//	WSD wsd= new WSD(api,preprocess,false);
	//	wsd.setWSD(inputSentences);
		SemanticClassifier classifier= new SemanticClassifier(api);
	//	WikidataNED ned = new WikidataNED();
	//	ned.setNED(inputSentences);
		SRL srl= new SRL(preprocess,api,classifier);
		List<List<SRLRelation>> allRelations=srl.getSrl(inputSentences);
//		for (List<Token> tokensOfSentence : inputSentences) {
//			for (int i = 0; i < tokensOfSentence.size(); i++) {
//				Token token = tokensOfSentence.get(i);
//				if (token.getNer() == null || token.getNer().equals(NERLabel.O)) {
//					if (token.getSense() != null && !token.getSense().equals(""))
//						token.setSemanticCategory(classifier.defineCat(token));
//					else
//						token.setSemanticCategory(classifier.defineCat(token));
//				} else {
//					token.setSemanticCategory(token.getNer().toString());
//				}
//				System.err.println(token.word()+"\t"+token.getSemanticCategory()+"\t"+token.getNer()+"\t"+token.getSense()+"\t"+token.getNED());
//			}
//		}
		for (List<SRLRelation> relList:allRelations){
			for (SRLRelation rel:relList){
				System.err.println(rel.toString());
			}
		}
//		NewsOnt newsOnt = new NewsOnt(preprocess, api);
//		newsOnt.writeFacts(inputSentences, allRelations,"10010101" );
//		newsOnt.writeOnt("testOnt.rdf",newsOnt.newsOnt);
	}

	public static void testMutFromFaWordNet() {
		ApiFactory faWnApi = new ApiFactory(AppTest.class.getClassLoader().getResourceAsStream("root-ontology.owl"));

		ArrayList<String> mutsList = new ArrayList<>();
		String[] wordsArray = InFile.readFileText("..\\preprocess\\src\\main\\resources\\muts.txt").split("\n");
		Collections.addAll(mutsList, wordsArray);
		Set<String> wordNetWords = faWnApi.listAllWords();
		System.out.println("wordnet:" + wordNetWords.size() + "\t mut:" + mutsList.size());
		for (String word : wordNetWords) {

			if (/* word.contains(" ")|| */word.contains("\u200c")) {
				if (!mutsList.contains(word.replace("\u200c", " "))) {
					System.out.println(word.replace("\u200c", " "));
				}
			}
		}
	}

	public static void testWordTagFromFaWordNet() {
		ApiFactory faWnApi = new ApiFactory(AppTest.class.getClassLoader().getResourceAsStream("root-ontology.owl"));

		ArrayList<String> wordTagsList = new ArrayList<>();
		String[] wordsArray = InFile.readFileText("..\\preprocess\\src\\main\\resources\\wordTag.txt").split("\n");
		Collections.addAll(wordTagsList, wordsArray);
		Set<String> wordNetWords = faWnApi.listAllWords();
		System.out.println("wordnet:" + wordNetWords.size() + "\t mut:" + wordTagsList.size());
		for (String word : wordNetWords) {

			if (/* word.contains(" ")|| */word.contains("\u200c")) {
				if (!wordTagsList.contains(word.replace("\u200c", " "))) {
					System.out.println(word);
				}
			}
		}
	}

	private static void getAdj() {
		ApiFactory faWnApi = new ApiFactory(AppTest.class.getClassLoader().getResourceAsStream("root-ontology.owl"));
		List<String> adjSyn = faWnApi.listAllAdjectiveSynsets();
		OutFile out = new OutFile("adj.txt");
		for (String uri : adjSyn) {
			ArrayList<String> senses = ApiFactory.listWordsOfSynset(uri);
			for (String sense : senses) {
				out.print(sense + "-");
			}
			out.println("");
		}
	}
	private static void getVerb() {
		ApiFactory faWnApi = new ApiFactory(AppTest.class.getClassLoader().getResourceAsStream("root-ontology.owl"));
		List<String> verbSyn = faWnApi.listAllVerbSynsets();
		OutFile out = new OutFile("verb.txt");
		for (String uri : verbSyn) {
			ArrayList<String> senses = ApiFactory.listWordsOfSynset(uri);
			for (String sense : senses) {
				out.print(sense + "-");
			}
			out.println("");
		}
	}

	private static void getGenre() {
		ApiFactory faWnApi = new ApiFactory(AppTest.class.getClassLoader().getResourceAsStream("root-ontology.owl"));
		OntModel onto = faWnApi.getOntology();
		DatatypeProperty genreProperty = onto.getDatatypeProperty("http://www.mitrc.ir/mobina#Genre");
		NodeIterator it = onto.listObjectsOfProperty(genreProperty);
		while (it.hasNext()) {
			RDFNode node = it.next();
			System.out.println(node.toString());
		}
	}

	public static void khosravi() {
		// ArrayList<String> csvList = new ArrayList<>();
		String folderPath = "D:\\corpus\\rumor detection for persian tweet dataset\\file2_dataset\\file\\rumor_tweet2\\";
		String outputPath = folderPath + "output\\";
		File[] files = new File(folderPath)
				.listFiles(/*
							 * new FilenameFilter() {
							 * 
							 * @Override public boolean accept(File dir, String
							 * name) { return name.endsWith(".xml"); } }
							 */);
		Preprocess preprocess = new Preprocess();
		RuleBasedNER ner = new RuleBasedNER();
		QuoteExtraction qe = new QuoteExtraction();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].isFile()) {
					if (files[i].getName().endsWith(".csv")) {
						// String line=
						List<String> lines = IOUtils.linesFromFile(folderPath + files[i].getName());
						String outString = "";
						OutFile out = new OutFile(outputPath + files[i].getName() + ".txt");
						for (int j = 1; j < lines.size(); j++) {
							String line = lines.get(j);
							String part = line.split(",")[3];

							List<List<Token>> tokens = preprocess.process(part);
							ner.setNER(tokens);
							qe.setQuote(tokens);
							for (List<Token> senTokens : tokens) {
								for (Token token : senTokens) {
									outString += token.word() + "\t" + token.lemma() + "\t" + token.tag() + "\t"
											+ token.getTense() + "\t" + token.getQuoteLabel() + "\n";
								}
							}
							outString +="\n";
						}
						out.print(outString);
						out.close();
					}
				}
			}
		} else
			System.err.println("Bad Directory for xmlNews!");
		return;
	}
}
