package ir.malek.newsanalysis.semantic.classification;

import java.util.ArrayList;
import java.util.Hashtable;
//import java.util.Iterator;
import java.util.List;
//import java.util.Set;
//import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

//import java.awt.font.LineMetrics;
import java.io.*;

//import com.hp.hpl.jena.ontology.Individual;
//import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
//import com.hp.hpl.jena.ontology.OntModelSpec;
//import com.hp.hpl.jena.ontology.OntProperty;
//import com.hp.hpl.jena.rdf.model.ModelFactory;
import ir.malek.newsanalysis.util.io.IOUtils;
import ir.malek.newsanalysis.util.io.InFile;
//import ir.malek.newsanalysis.util.io.InFile;
//import ir.malek.newsanalysis.util.io.OutFile;
import ir.mitrc.corpus.api.ApiFactory;
import ir.mitrc.corpus.api.Isynset;
import ir.mitrc.corpus.api.NounRelationType;

/**
 * 
 * The class uses Persian Wordnet to classify nouns and noun synsets into 5 Types:
 * These types have been defined by {@link ir.malek.newsanalysis.util.enums.SemanticCategory}
 * (Object, Animate, Place, Time, Act).
 * 
 * @author M.Haghollahi, Z.Arani
 * 
 *  
 *
 */
public class NounClassifier {
	static OntModel onto;
	static ApiFactory api;
	static Hashtable<Integer, String> oldtopSyns;
	static Hashtable<String, String> topSyns = new Hashtable<String, String>();
	static Hashtable<String, Integer> otherKnsh;
	static Hashtable<String, Integer> bons;
	int threshold = 500;
	private String ns;
	private InputStream lightVerbsInputStream = this.getClass().getClassLoader().getResourceAsStream("LightVerbs.txt");
	private InputStream verbListInputStream = this.getClass().getClassLoader().getResourceAsStream("VerbList.txt");
	private InputStream stemsInputStream = this.getClass().getClassLoader().getResourceAsStream("Stems.txt");
	private ArrayList<String> verbList=new ArrayList<String>();
	
	VerbClassifier verbClassifier;
	
	public NounClassifier(ApiFactory api) {
		NounClassifier.api = api;
		ns = "http://www.mitrc.ir/mobina#";
		initNounCats();
		initOtherKoneshi();
		verbClassifier=new VerbClassifier();
	}

	private void initNounCats() {
		List<String> allLines = IOUtils.linesFromFile(this.getClass().getClassLoader().getResourceAsStream("topSynsetUris.txt"));
		for (String line : allLines) {
			if (line != null && line.split("\t").length == 2) {
				String[] lineMap = line.split("\t");
				topSyns.put(lineMap[0], lineMap[1]);
			}
		}
	}

	private void initOtherKoneshi() {
		otherKnsh = new Hashtable<String, Integer>();

		Hashtable<String, Integer> lightVHashMap = loadLightVerbs(lightVerbsInputStream);
		otherKnsh = findKoneshiNounList(verbListInputStream, stemsInputStream, lightVHashMap);
	}

	private Hashtable<String, Integer> loadLightVerbs(InputStream inputStream) {
		Hashtable<String, Integer> lightVerbHash = new Hashtable<String, Integer>();
		List<String> lines=IOUtils.linesFromFile(inputStream);
		for (String line:lines){
			lightVerbHash.put(line, 0);
		}
		return lightVerbHash;
	}

	private Hashtable<String, Integer> findKoneshiNounList(InputStream verbListInputStream, InputStream stemsInputStream,
			Hashtable<String, Integer> liVeHash) {
		// To load verb list
		// and
		// To filter verbs that don't contain light verb part
		// and
		// To find action nouns

		String[] cell;
		Hashtable<String, Integer> koneshiNounHash = new Hashtable<String, Integer>();
		List<String> lines=IOUtils.linesFromFile(verbListInputStream);
		
		for (String line:lines) {
			cell = line.split("\t");
			verbList.add(line);
			if (liVeHash.containsKey(cell[2])) {
				koneshiNounHash.put(cell[4], 0);
			}
		}

		lines=IOUtils.linesFromFile(stemsInputStream);
		for (String line:lines) {
			koneshiNounHash.put(line, 0);
			koneshiNounHash.put(line + "ش", 0);
			koneshiNounHash.put(line + "گی", 0);
			koneshiNounHash.put(line + "ندگی", 0);
			koneshiNounHash.put(line + "ن", 0);
		}
		return (koneshiNounHash);
	}

	public String catWithWord(String word) {
		if (StringUtils.isNumeric(word))
			return "NUM";
		Vector<String> allSynsVector = new Vector<String>();
		List<String> allSyns = api.listSynsetsByWordSurface(word);
		if ((allSyns == null) || !(allSyns.size() > 0))
			return "Not Found";
		int counter = 0;
		allSynsVector.addAll(allSyns);
		while (!allSynsVector.isEmpty() && counter < threshold) {
			counter++;
			String synUri = allSynsVector.firstElement();
			if (synUri == null)
				return "Not Found";
			if (topSyns.contains(synUri)) {
				return topSyns.get(synUri);
			}
			Isynset synset = ApiFactory.getSynset(synUri);
			if (synset==null) {
				System.err.println(synUri);
				return "Not Found";

			}
			ArrayList<String> fathers = synset.getRelatedSynset(NounRelationType.HypernymOrInstanceOf);
			if (fathers == null)
				allSynsVector.remove(0);
			else {
				Vector<String> fathersVector = new Vector<String>();

				fathersVector.addAll(fathers);
				for (int k = 0; k < fathersVector.size(); k++) {
					if (topSyns.containsKey(fathers.get(k)))
						return topSyns.get(fathers.get(k));
				}
				allSynsVector.addAll(fathers);
				allSynsVector.remove(0);
			}
		}
		if (verbList.contains(word)){
			return  verbClassifier.getTransitivity(word);
		}
		if (isKoneshi(word)) {
			return ("ACT");
		} else
			return ("OBJ");

	}

	public String catWithSense(String synsetUri) {
		Vector<String> allSynsVector = new Vector<String>();
		allSynsVector.add(synsetUri);

		int counter = 0;
		while (!allSynsVector.isEmpty() && counter < threshold) {

			String synUri = allSynsVector.firstElement();
			if (topSyns.containsKey(synUri)) {
				return topSyns.get(synUri);
			}
			Isynset synset = ApiFactory.getSynset(synUri);
			if (synset==null){
				allSynsVector.remove(0);
				continue;
			}
			ArrayList<String> fathers = synset.getRelatedSynset(NounRelationType.HypernymOrInstanceOf);
			if (fathers != null) {
				for (int k = 0; k < fathers.size(); k++) {
					if (topSyns.containsKey(fathers.get(k)))
						return topSyns.get(fathers.get(k));
				}
				allSynsVector.addAll(fathers);
			}
			allSynsVector.remove(0);
		}
		ArrayList<String> words = ApiFactory.listWordsOfSynset(synsetUri);
		for (String word : words) {
			if (verbList.contains(word))
				return  verbClassifier.getTransitivity(word);
			if (isKoneshi(word))
				return ("ACT");
		}
		return ("OBJ");
	}

	private boolean isKoneshi(String word) {
		if (otherKnsh.containsKey(word))
			return true;
		else
			return false;
	}

	// private static OntModel readOntology(String path) {
	// try {
	// onto = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
	// InputStream owlPath = new FileInputStream(path);
	// onto.read(owlPath, null);
	// } catch (FileNotFoundException e) {
	// System.err.println("An error has occured in ontology loading");
	// e.printStackTrace();
	// }
	// return onto;
	// }
	// private static void initNounCats() {
	// oldtopSyns = new Hashtable<Integer, String>();
	//
	// ///////////////////////////////////////
	// ///// Time ////
	// ///////////////////////////////////////
	// oldtopSyns.put(10264, "Time");
	// oldtopSyns.put(12682, "Time");
	// oldtopSyns.put(10368, "Time");
	// oldtopSyns.put(12606, "Time");
	// oldtopSyns.put(12607, "Time");
	// oldtopSyns.put(12660, "Time");
	// oldtopSyns.put(12605, "Time");
	// oldtopSyns.put(12649, "Time");
	// oldtopSyns.put(12609, "Time");
	// oldtopSyns.put(12686, "Time");
	// oldtopSyns.put(12739, "Time");
	// oldtopSyns.put(13490, "Time");
	// oldtopSyns.put(12679, "Time");
	// oldtopSyns.put(12693, "Time");
	// oldtopSyns.put(12692, "Time");
	// oldtopSyns.put(13060, "Time");
	// oldtopSyns.put(12664, "Time");
	// oldtopSyns.put(12613, "Time");
	// oldtopSyns.put(12621, "Time");
	// oldtopSyns.put(12691, "Time");
	// oldtopSyns.put(12603, "Time");
	// oldtopSyns.put(13777, "Time");
	// oldtopSyns.put(23361, "Time");
	// oldtopSyns.put(12631, "Time");
	// oldtopSyns.put(12626, "Time");
	// oldtopSyns.put(13141, "Time");
	// oldtopSyns.put(12681, "Time");
	// oldtopSyns.put(12680, "Time");
	// oldtopSyns.put(13780, "Time");
	// oldtopSyns.put(13446, "Time");
	// oldtopSyns.put(10083, "Time");
	// oldtopSyns.put(12617, "Time");
	// oldtopSyns.put(12614, "Time");
	// oldtopSyns.put(13941, "Time");
	// oldtopSyns.put(12634, "Time");
	// oldtopSyns.put(12632, "Time");
	//
	// ///////////////////////////////////////
	// ///// Location ////
	// ///////////////////////////////////////
	// oldtopSyns.put(12733, "LOC");
	// oldtopSyns.put(11881, "LOC");
	// oldtopSyns.put(11874, "LOC");
	// oldtopSyns.put(14886, "LOC");
	// oldtopSyns.put(14887, "LOC");
	// oldtopSyns.put(13068, "LOC");
	// oldtopSyns.put(12289, "LOC");
	// oldtopSyns.put(12868, "LOC");
	// oldtopSyns.put(11893, "LOC");
	// oldtopSyns.put(10268, "LOC");
	// oldtopSyns.put(11106, "LOC");
	// oldtopSyns.put(11104, "LOC");
	// oldtopSyns.put(13280, "LOC");
	// oldtopSyns.put(12790, "LOC");
	// oldtopSyns.put(13846, "LOC");
	// oldtopSyns.put(11844, "LOC");
	// oldtopSyns.put(13260, "LOC");
	// oldtopSyns.put(13275, "LOC");
	// oldtopSyns.put(11891, "LOC");
	// oldtopSyns.put(13288, "LOC");
	// oldtopSyns.put(11889, "LOC");
	// oldtopSyns.put(13314, "LOC");
	// oldtopSyns.put(13265, "LOC");
	// oldtopSyns.put(10850, "LOC");
	// oldtopSyns.put(11836, "LOC");
	// oldtopSyns.put(13274, "LOC");
	// oldtopSyns.put(10829, "LOC");
	// oldtopSyns.put(11898, "LOC");
	// oldtopSyns.put(11921, "LOC");
	// oldtopSyns.put(11911, "LOC");
	// oldtopSyns.put(13299, "LOC");
	// oldtopSyns.put(13259, "LOC");
	// oldtopSyns.put(14681, "LOC");
	// oldtopSyns.put(11841, "LOC");
	// oldtopSyns.put(13787, "LOC");
	// oldtopSyns.put(12899, "LOC");
	// oldtopSyns.put(13288, "LOC");
	// oldtopSyns.put(11082, "LOC");
	// oldtopSyns.put(11863, "LOC");
	// oldtopSyns.put(11863, "LOC");
	// oldtopSyns.put(11867, "LOC");
	// oldtopSyns.put(11133, "LOC");
	// oldtopSyns.put(12810, "LOC");
	// oldtopSyns.put(13267, "LOC");
	// oldtopSyns.put(11854, "LOC");
	// oldtopSyns.put(11832, "LOC");
	// oldtopSyns.put(11853, "LOC");
	// oldtopSyns.put(11867, "LOC");
	// oldtopSyns.put(12440, "LOC");
	// oldtopSyns.put(11856, "LOC");
	// oldtopSyns.put(12748, "LOC");
	// oldtopSyns.put(11878, "LOC");
	// oldtopSyns.put(14042, "LOC");
	// oldtopSyns.put(10880, "LOC");
	// oldtopSyns.put(10272, "LOC");
	// oldtopSyns.put(13479, "LOC");
	// oldtopSyns.put(13264, "LOC");
	// oldtopSyns.put(10363, "LOC");
	// oldtopSyns.put(13286, "LOC");
	// oldtopSyns.put(11889, "LOC");
	// oldtopSyns.put(11888, "LOC");
	// oldtopSyns.put(13281, "LOC");
	// oldtopSyns.put(10272, "LOC");
	// oldtopSyns.put(11891, "LOC");
	// oldtopSyns.put(17735, "LOC");
	// oldtopSyns.put(13610, "LOC");
	// oldtopSyns.put(12857, "LOC");
	// oldtopSyns.put(11015, "LOC");
	// oldtopSyns.put(12866, "LOC");
	// oldtopSyns.put(11082, "LOC");
	// oldtopSyns.put(10390, "LOC");
	// oldtopSyns.put(11020, "LOC");
	// oldtopSyns.put(19599, "LOC");
	// oldtopSyns.put(26245, "LOC");
	// oldtopSyns.put(11099, "LOC");
	// oldtopSyns.put(11873, "LOC");
	// oldtopSyns.put(20637, "LOC");
	// oldtopSyns.put(23213, "LOC");
	// oldtopSyns.put(11907, "LOC");
	// oldtopSyns.put(12779, "LOC");
	// oldtopSyns.put(13265, "LOC");
	// oldtopSyns.put(12762, "LOC");
	// oldtopSyns.put(12761, "LOC");
	// oldtopSyns.put(12810, "LOC");
	// oldtopSyns.put(10913, "LOC");
	// oldtopSyns.put(10901, "LOC");
	// oldtopSyns.put(12855, "LOC");
	// oldtopSyns.put(12894, "LOC");
	// oldtopSyns.put(11072, "LOC");
	// oldtopSyns.put(10975, "LOC");
	// oldtopSyns.put(11880, "LOC");
	//
	// ///////////////////////////////////////
	// ///// Animate ////
	// ///////////////////////////////////////
	// oldtopSyns.put(10393, "ANM");
	// oldtopSyns.put(13334, "ANM");
	// oldtopSyns.put(12048, "ANM");
	// oldtopSyns.put(13384, "ANM");
	// oldtopSyns.put(13075, "ANM");
	// oldtopSyns.put(12239, "ANM");
	// oldtopSyns.put(12155, "ANM");
	// oldtopSyns.put(12156, "ANM");
	// oldtopSyns.put(11693, "ANM");
	// oldtopSyns.put(11964, "ANM");
	// oldtopSyns.put(13578, "ANM");
	// oldtopSyns.put(13239, "ANM");
	// oldtopSyns.put(22641, "ANM");
	// oldtopSyns.put(22146, "ANM");
	// oldtopSyns.put(13190, "ANM");
	// oldtopSyns.put(11719, "ANM");
	// oldtopSyns.put(11804, "ANM");
	// oldtopSyns.put(11767, "ANM");
	// oldtopSyns.put(13242, "ANM");
	// oldtopSyns.put(13188, "ANM");
	// oldtopSyns.put(11694, "ANM");
	// oldtopSyns.put(11767, "ANM");
	// oldtopSyns.put(13224, "ANM");
	// oldtopSyns.put(11777, "ANM");
	// oldtopSyns.put(11765, "ANM");
	// oldtopSyns.put(13229, "ANM");
	// oldtopSyns.put(13194, "ANM");
	// oldtopSyns.put(13190, "ANM");
	// oldtopSyns.put(13196, "ANM");
	// oldtopSyns.put(11693, "ANM");
	// oldtopSyns.put(15595, "ANM");
	// oldtopSyns.put(11739, "ANM");
	// oldtopSyns.put(13198, "ANM");
	// oldtopSyns.put(11742, "ANM");
	// oldtopSyns.put(13192, "ANM");
	// oldtopSyns.put(11756, "ANM");
	// oldtopSyns.put(13236, "ANM");
	// oldtopSyns.put(10266, "ANM");
	// oldtopSyns.put(13226, "ANM");
	// oldtopSyns.put(13195, "ANM");
	// oldtopSyns.put(13389, "ANM");
	// oldtopSyns.put(13337, "ANM");
	// oldtopSyns.put(15876, "ANM");
	// oldtopSyns.put(12742, "ANM");
	// oldtopSyns.put(10826, "ANM");
	// oldtopSyns.put(12708, "ANM");
	// oldtopSyns.put(788, "ANM");
	//
	// ///////////////////////////////////////
	// ///// Action ////
	// ///////////////////////////////////////
	// oldtopSyns.put(12746, "ACT");
	// oldtopSyns.put(10732, "ACT");
	// oldtopSyns.put(12073, "ACT");
	// oldtopSyns.put(13128, "ACT");
	// oldtopSyns.put(13321, "ACT");
	// oldtopSyns.put(10613, "ACT");
	// oldtopSyns.put(10446, "ACT");
	// oldtopSyns.put(12849, "ACT");
	// oldtopSyns.put(11211, "ACT");
	// oldtopSyns.put(10322, "ACT");
	// oldtopSyns.put(12404, "ACT");
	// oldtopSyns.put(10320, "ACT");
	// oldtopSyns.put(19425, "ACT");
	//
	// }
	// public static void main(String [] args){
	// initNounCats();
	// topSyns=new Hashtable<String, String>();
	// String path="resources/modifiedMobina63.owl";
	// //ApiFactory api=new ApiFactory("resources/modifiedMobina60.owl");
	// //OntModel onto=api.getOntology();
	// onto=readOntology(path);
	// String ns="http://www.mitrc.ir/mobina#";
	// OntProperty
	// synsetId=onto.getOntProperty("http://www.mitrc.ir/"+"hasSynsetId");
	// OntClass nounClass=onto.getOntClass(ns+"گروه_معنایی_اسم_ها");
	// Iterator<Individual> nounIndivs=onto.listIndividuals(nounClass);
	// Hashtable<String, String> farsnetIdTmobinaId=new Hashtable<String,
	// String>();
	// while (nounIndivs.hasNext()){
	// Individual nounIndiv=nounIndivs.next();
	// if(nounIndiv.hasProperty(synsetId)){
	// farsnetIdTmobinaId.put(nounIndiv.getPropertyValue(synsetId).toString(),
	// nounIndiv.getURI());
	//
	//
	// }
	// }
	// Set<Integer> farsIds=oldtopSyns.keySet();
	// Set<Integer> copyFarsIds=new TreeSet<Integer>();
	// copyFarsIds.addAll(farsIds);
	// OutFile topSynsetFile=new
	// OutFile("resources"+File.separator+"topSynsetUris.txt");
	// for(Integer id:farsIds){
	// String nounUri=farsnetIdTmobinaId.get(id.toString());
	// if (nounUri!=null){
	// //topSyns.put(nounUri, oldtopSyns.get(id));
	// copyFarsIds.remove(id);
	// topSynsetFile.println(nounUri+"\t"+oldtopSyns.get(id));
	// }
	// }
	// for (Integer id:copyFarsIds){
	// System.err.println("this SynsetId not Found in farsnet: "+id);
	// }
	// System.out.println(copyFarsIds.size());
	// }

}
