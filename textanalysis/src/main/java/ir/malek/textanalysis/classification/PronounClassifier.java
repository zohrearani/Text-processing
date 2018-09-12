package ir.malek.textanalysis.classification;

import java.io.InputStream;
import java.util.Hashtable;
import java.util.List;

import ir.malek.newsanalysis.util.io.IOUtils;
/**
 * 
 * @author Z. Arani
 *
 */
public class PronounClassifier {
	Hashtable<String, String> pronounHash;
	
	public PronounClassifier(){		
		loadPronouns(this.getClass().getClassLoader().getResourceAsStream("PronounCat.txt"));
	}
	
	public String catWithWord(String word) {
		if (pronounHash.containsKey(word)){
			return pronounHash.get(word);
		}
		else
			return "OBJ";
	}
	
	private Hashtable<String, String> loadPronouns(InputStream inputStream) {
		String pronoun, category;
		pronounHash = new Hashtable<String, String>();
		List<String> lines = IOUtils.linesFromFile(inputStream);
		for (String line : lines) {
			String[] element = line.split("\t");
			if (element.length > 1) {
				pronoun = element[0];
				category = element[1];
				pronounHash.put(pronoun, category);
			}
		}

		return pronounHash;
	}
}
