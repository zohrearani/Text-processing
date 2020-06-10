package ir.malek.newsanalysis.semantic.srl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.maltparser.concurrent.graph.ConcurrentDependencyGraph;

import edu.stanford.nlp.io.IOUtils;
import ir.malek.newsanalysis.ner.RuleBasedNER;
import ir.malek.newsanalysis.preprocess.Preprocess;
import ir.malek.newsanalysis.preprocess.Token;
import ir.malek.newsanalysis.preprocess.depparse.DependencyParser;
import ir.malek.newsanalysis.semantic.classification.SemanticClassifier;
import ir.malek.newsanalysis.time.TimeNormalizer;
import ir.malek.newsanalysis.util.collection.ListUtil;
import ir.malek.newsanalysis.util.enums.NERLabel;
import ir.malek.newsanalysis.util.io.InFile;
import ir.malek.newsanalysis.util.io.OutFile;
import ir.malek.newsanalysis.util.performance.NLPPerformance;
import ir.malek.newsanalysis.wsd.WSD;
import ir.mitrc.corpus.api.ApiFactory;

/**
 * This class uses a rule-based Semantic Role Labeling method.<br>
 * Some examples of rules are as follow:<br>
 * <br>
 * 
 * <table style="width:100%">
 * <tr>
 * <th>Dep</th>
 * <th>POS</th>
 * <th>Category</th>
 * <th>PCategory</th>
 * <th>FCcategory</th>
 * <th>WORD</th>
 * <th>Sense</th>
 * <th>Arg1</th>
 * <th>Role</th>
 * <th>Arg2</th>
 * <th>Cofidence</th>
 * </tr>
 * <tr>
 * <th>SBJ</th>
 * <th>o</th>
 * <th>ANM</th>
 * <th>A_T</th>
 * <th>o</th>
 * <th>o</th>
 * <th>o</th>
 * <th>parent</th>
 * <th>Agent</th>
 * <th>this</th>
 * <th>1</th>
 * </tr>
 * </table>
 * 
 * 
 * The first 7 columns are IF_Part and columns 8,9,10,11 make THEN_Part of the
 * rule. Finally the last column is the confidence coefficient for the rule,
 * which shows the credit of extracted roles by this rule according to the
 * training corpus (
 * <a href="http://dadegan.ir/catalog/perdt/about"> dadegan</a>). the 'o'
 * character in the IF-part means "don't care". every
 * {@linkplain ir.malek.newsanalysis.prerpcess.Token Token} in the input
 * sentence has information related to the IF_Part. all the rules are checked
 * for the tokens of the input sentence one by one. when this information match
 * the IF_Part of the rule is triggered and THEN_Part of the rule will be
 * applied <br>
 * 
 * @author Arani
 *
 */
public class SRL extends SemanticRoleExtractor {
	Preprocess preprocess;
	DependencyParser depParser;
	TimeNormalizer timeNormalizer;

	ApiFactory faWnApi;
	SemanticClassifier classifier;
	RuleBasedNER namedEntityAnnotator;
	WSD wsd;
	public NLPPerformance performance = new NLPPerformance(TimeUnit.NANOSECONDS);

	public SRL() {
		preprocess = new Preprocess();
		faWnApi = new ApiFactory(this.getClass().getClassLoader().getResourceAsStream("root-ontology.owl"), preprocess);
		classifier = new SemanticClassifier(faWnApi);
		namedEntityAnnotator = new RuleBasedNER();
		// timeNormalizer = new TimeNormalizer();
		// wsd = new WSD(faWnApi, preprocess, false);

	}

	public SRL(Preprocess preprocess) {
		this.preprocess = preprocess;
		depParser = new DependencyParser();
		faWnApi = new ApiFactory(this.getClass().getClassLoader().getResourceAsStream("root-ontology.owl"), preprocess);
		classifier = new SemanticClassifier(faWnApi);
		namedEntityAnnotator = new RuleBasedNER();
		// timeNormalizer = new TimeNormalizer();
		// wsd = new WSD(faWnApi, preprocess, false);
	}

	/*
	 * this constructor should be used when you decide to use getSRL(docTokens)
	 */
	public SRL(Preprocess preprocess, ApiFactory faWnApi) {
		this.preprocess = preprocess;
		this.faWnApi = faWnApi;
		classifier = new SemanticClassifier(faWnApi);
		// wsd = new WSD(faWnApi, preprocess, false);
	}
	public SRL(Preprocess preprocess, ApiFactory faWnApi,SemanticClassifier classifier) {
		this.preprocess = preprocess;
		this.faWnApi = faWnApi;
		this.classifier = classifier;
		// wsd = new WSD(faWnApi, preprocess, false);
	}
/**
 * this function performs a complete pipeline for extracting Semantic Role labeling from a raw text.
 * @param inputText
 * @return
 */
	public List<List<SRLRelation>> getSrl(String inputText) {
		if (depParser == null) {
			depParser = new DependencyParser();
		}
		if (inputText == null || inputText.length() < 1)
			return new ArrayList<>();
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
		namedEntityAnnotator.setNER(inputSentences);
		// timeNormalizer.process(inputSentences, baseTime, newsID);
		// wsd.setWSD(inputSentences);

		return getSrl(inputSentences);
	}

	public List<List<SRLRelation>> getSrl(List<List<Token>> docTokens) {
		long startTime = System.nanoTime();
		// WSD: it seems that preprocess and dependency and ner have been
		// done already but wsd has not done yet!
		// wsd.setWSD(docTokens);
		List<List<SRLRelation>> docRels = new ArrayList<>();
		for (int i = 0; i < docTokens.size(); i++) {
			List<Token> senTokens = docTokens.get(i);
			for (Token token : senTokens) {
				if (token.getNer() == null || token.getNer().equals(NERLabel.O)) {
					if (token.getSense() != null && !token.getSense().equals(""))
						token.setSemanticCategory(classifier.defineCat(token));
					else
						token.setSemanticCategory(classifier.defineCat(token));
				} else {
					token.setSemanticCategory(token.getNer().toString());
				}
			}
			docRels.add(super.extractRole(senTokens));
		}
		long endTime = System.nanoTime();
		performance.add(endTime - startTime, ListUtil.getSize(docTokens));
		return docRels;

	}

	public List<List<SRLRelation>> getSrl(List<List<Token>> docTokens, boolean hasDependency, boolean hasNer) {
		long startTime = System.nanoTime();
		// WSD: it seems that preprocess and dependency and ner have been
		// done already but wsd has not done yet!

		// wsd.setWSD(docTokens);
		if (!hasDependency) {
			for (List<Token> tokensOfSentence : docTokens) {
				ConcurrentDependencyGraph graph = depParser.rawParse(tokensOfSentence);
				if (graph == null)
					continue;
				for (int i = 0; i < tokensOfSentence.size(); i++) {
					Token token = tokensOfSentence.get(i);
					token.setDep(graph.getDependencyNode(i + 1).toString());
				}
			}

		}
		if (!hasNer) {
			namedEntityAnnotator.setNER(docTokens);
		}
		// timeNormalizer.process(inputSentences, baseTime, newsID);
		// wsd.setWSD(inputSentences);
		List<List<SRLRelation>> docRels = new ArrayList<>();
		for (int i = 0; i < docTokens.size(); i++) {
			List<Token> senTokens = docTokens.get(i);
			for (Token token : senTokens) {
				if (token.getNer() == null || token.getNer().equals(NERLabel.O)) {
					if (token.getSense() != null && !token.getSense().equals(""))
						token.setSemanticCategory(classifier.defineCat(token));
					else
						token.setSemanticCategory(classifier.defineCat(token));
				} else {
					token.setSemanticCategory(token.getNer().toString());
				}
			}
			docRels.add(super.extractRole(senTokens));
		}
		long endTime = System.nanoTime();
		performance.add(endTime - startTime, ListUtil.getSize(docTokens));
		return docRels;

	}

	public static void main(String[] args) {
		if (args.length < 2)
			return;
		String inputPath = args[0];
		String outputPath = args[1];
		String inputText = InFile.readFileText(inputPath);
		Preprocess preprocess = new Preprocess();
		List<List<Token>> tokens = preprocess.process(inputText);
		SRL srl = new SRL();
		List<List<SRLRelation>> rels = srl.getSrl(tokens, false, false);
		OutFile outFile = new OutFile(outputPath);
		int k = 0;
		for (List<SRLRelation> sentenceRels : rels) {
			outFile.println(tokens.get(k).toString());
			for (SRLRelation rel : sentenceRels) {
				outFile.println(rel.toString());
			}
		}
		return;
	}
}
