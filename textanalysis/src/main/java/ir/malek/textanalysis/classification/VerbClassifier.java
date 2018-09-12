package ir.malek.textanalysis.classification;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import ir.malek.newsanalysis.preprocess.Preprocess;
import ir.malek.newsanalysis.util.io.IOUtils;

/**
 * 
 * @author Z. Arani
 *
 */
public class VerbClassifier {
	private HashMap<String, String> verbTran = new HashMap<String, String>();
	private InputStream verbTransivityInputStream = Preprocess.class.getClassLoader().getResourceAsStream("verbTransivity.txt");

	public VerbClassifier() {
		readTransitivity();
	}

	private void readTransitivity() {
		List<String> lines = IOUtils.linesFromFile(verbTransivityInputStream);
		for (String line : lines) {
			String[] txt = line.split("\t");
			if (txt.length > 1) {
				switch (Integer.parseInt(txt[1])) {
				case 0:
					verbTran.put(txt[0], "N");
					break;
				case 1:
					verbTran.put(txt[0], "T");
					break;
				default:
					verbTran.put(txt[0], "T");
					break;
				}
			}
		}
	}

	public String getTransitivity(String verb) {
		try {
			if (verbTran.get(verb) != null)
				return verbTran.get(verb);
			else
				return "N";
		} catch (Exception e) {
			return "N";
		}
	}
}
