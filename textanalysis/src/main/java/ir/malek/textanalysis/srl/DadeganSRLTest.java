package ir.malek.textanalysis.srl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ir.malek.newsanalysis.preprocess.Preprocess;
import ir.malek.newsanalysis.preprocess.Token;
import ir.malek.newsanalysis.preprocess.postagger.TagSet;
import ir.malek.newsanalysis.preprocess.tokenizer.Inflection;
import ir.malek.textanalysis.classification.NounClassifier;
import ir.malek.textanalysis.classification.PronounClassifier;
import ir.malek.textanalysis.classification.VerbClassifier;
import ir.malek.newsanalysis.util.io.InFile;
import ir.malek.newsanalysis.util.io.OutFile;
import ir.mitrc.corpus.api.ApiFactory;

public class DadeganSRLTest {
	private static SemanticRoleExtractor roleExtractor;
	Inflection inflect;

	public DadeganSRLTest() {
		roleExtractor = new SemanticRoleExtractor();
		inflect = new Inflection();

	}

	public String parse(String dadeganFilePath, String outputPath) {
		String output = null;
		OutFile outputFile = new OutFile(outputPath);
		String line = "";
		List<Token> tokensOfSen = new ArrayList<Token>();
		int n = -1;
		String[] lineParts = null;
		String[] lines = InFile.readFileText(dadeganFilePath).split("\n");
		String sentence = "";
		for (int k = 0; k < lines.length && k < 10000; k++) {
			n++;
			line = lines[k];
			if (line != null)
				lineParts = line.split("\t");
			if (line == null || lineParts.length == 0 || lineParts[0].equals("") || k == lines.length - 1) {
				ArrayList<SRLRelation> rels = new ArrayList<SRLRelation>();
				for (Token token : tokensOfSen) {
					if (token.tag().equals(TagSet.VERB)) {
						token.setTense(inflect.findInflection(token));
					}
				}
				rels = roleExtractor.extractRole(tokensOfSen);
				n = 0;
				outputFile.println(sentence);
				// System.out.println(sentence);
				sentence = "";
				for (int i = 0; i < rels.size(); i++) {
					output += rels.get(i).toString() + "\n";
					outputFile.println(rels.get(i).toString());
					// System.out.println(rels.get(i).toString());
				}
				tokensOfSen = new ArrayList<Token>();
			} else {
				Token token = new Token();
				token.setIndex(n);
				token.setDep(line);
				token.setWord(lineParts[1]);
				String[] verbParts = lineParts[2].split("#");
				if (verbParts.length > 2) {
					token.setLemma(verbParts[0] + verbParts[1] + "ن");
				} else if (verbParts.length > 1) {
					String bon = verbParts[0];
					if (bon != null && bon.length() > 0)
						token.setLemma(bon + "ن");
					else
						token.setLemma("بودن");
				} else
					token.setLemma(lineParts[2]);
				if (lineParts[10] == null || lineParts[10].equals("")) {
					lineParts[10] = lineParts[11];
				}
				lineParts[10] = lineParts[10].replaceAll("B_ORG", "ANM").replaceAll("B_LOC", "LOC").replaceAll("B_ANM", "ANM").replace("B_TIME", "TIME").replace("NOT FOUND", "OBJ");
				token.setSemanticCategory(lineParts[10]);
				token.setSense(lineParts[9]);
				token.setTag(lineParts[3]);
				tokensOfSen.add(token);
				sentence += lineParts[1] + " ";
			}
		}
		return output;
	}

	private static void defineCat() {

		String stopWords = InFile.readFileText("..//preprocess//src//main//resources//stopword.txt");
		ArrayList<String> stopList = new ArrayList<String>();
		for (String line : stopWords.split("\n")) {
			stopList.add(line);
		}
		OutFile outfile = new OutFile("categoriesTest.txt");
		String[] conllText = InFile.readFileText("..//semantic//" + "resources//test//lemma_POS2.txt").split("\n");

		Preprocess preprocess = new Preprocess();
		VerbClassifier verbCat = new VerbClassifier();
		PronounClassifier proCat = new PronounClassifier();
		ApiFactory faWnApi = new ApiFactory(DadeganSRLTest.class.getClassLoader().getResourceAsStream("root-ontology.owl"), preprocess);
		NounClassifier nounCat = new NounClassifier(faWnApi);
		String out = "";
		// int count=0;
		for (String line : conllText) {
			// count++;
			String[] lineParts = line.split("\t");
			if (line.length() == 0 || lineParts.length == 0) {
				out += "\n";
				continue;
			}

			if (lineParts.length > 1) {
				String lem = lineParts[0];
				if (lineParts[1].equals("CONJ") || lineParts[1].equals("PREP")) {
					out += "stopWord\n";
					continue;
				}
				if (lineParts[1].equals("PR")) {
					out += proCat.catWithWord(lem) + "\n";
					continue;
				}
				/*
				 * if (stopList.contains(lem)){ out+="stopWord\n"; continue;
				 * }else
				 */ if (lem.equals("") || lem.equals(".") || lem.equals("?") || lem.equals("]") || lem.equals("[") || lem.equals("(") || lem.equals(")") || lem.equals("،") || lem.equals(":") || lem.equals("«") || lem.equals("»") || lem.equals("؛"))
					out += "PU\n";
				else {
					String[] verb = lem.split("#");
					if (verb.length > 2) {
						verb[0] = verb[0] + verb[1] + "ن";
						if (lineParts[2].equals("PASS"))
							out += "PAS\n";
						else if (lineParts[2].equals("ACT"))
							out += "A_" + verbCat.getTransitivity(verb[0]) + "\n";
						else
							out += "A_N\n";
					} else if (verb.length > 1) {
						verb[0] = verb[0] + "ن";
						if (lineParts[2].equals("PASS"))
							out += "PAS\n";
						else if (lineParts[2].equals("ACT"))
							out += "A_" + verbCat.getTransitivity(verb[0]) + "\n";
						else
							out += "A_N\n";
					} else
						out += nounCat.catWithWord(lem) + "\n";

				}
			}
		}
		outfile.print(out);
	}

	public static void main(String[] args) {

		// defineCat();

		String dadeganFilePath = ".." + File.separator + "semantic"+ File.separator + "src"+ File.separator + "main" + File.separator + "resources" + File.separator + "test" + File.separator + "dadeganWithNounClass6.txt";
		DadeganSRLTest dadeganTester = new DadeganSRLTest();
		String outputPath = "output2.txt";
		dadeganTester.parse(dadeganFilePath, outputPath);

	}
}
