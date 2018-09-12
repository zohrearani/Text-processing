package ir.malek.textanalysis.classification;

import ir.malek.newsanalysis.preprocess.Token;
import ir.malek.newsanalysis.preprocess.postagger.TagSet;
import ir.mitrc.corpus.api.ApiFactory;

public class SemanticClassifier {
	public NounClassifier nounCat;
	static VerbClassifier verbCat;
	static PronounClassifier proCat;
	// static Inflection inflection;
	boolean fars = false;

	public SemanticClassifier(ApiFactory faWnApi) {
		nounCat = new NounClassifier(faWnApi);
		proCat = new PronounClassifier();
		verbCat = new VerbClassifier();
		// inflection =new Inflection();
		fars = true;
	}

	public String defineCat(Token token) {
		String tag = token.tag();
		if (tag.equals(TagSet.NOUN) || tag.equals(TagSet.ADJ)|| tag.equals(TagSet.ADVERB)) {
			return nounCat.catWithWord(token.lemma());
		} 
		
		if (tag.equals(TagSet.VERB)) {
			String tense = token.getTense();
			if (tense == null)
				System.out.println(token.toString());
			else if (tense.startsWith("M"))
				return "PAS";// passive
			else {
				return "A_" + verbCat.getTransitivity(token.lemma());
			}
		}
		
		if (tag.equals(TagSet.PRONOUN)) {
			return proCat.catWithWord(token.lemma());
		}
		if (tag.equals("DET")) {
			return "PREM";
		} 
		return token.tag();
	}
	
	public String defineCatWithSense(Token token) {
		String tag = token.tag();
		if ((tag.equals(TagSet.NOUN) || tag.equals(TagSet.ADJ))&& !token.getSense().equals("")) {
			return nounCat.catWithSense(token.getSense());
		} 
		else
			return defineCat(token);
	}
	
	/*
	 * public String defineCat(Token token ) { String cat=""; if
	 * (token.getNer()==null||token.getNer().equals("O")){ String
	 * tag=token.tag(); if (tag.equals("N") || tag.equals("AJ") ){
	 * cat=nounCat.catWithWord(token.lemma()); }else if (tag.equals("V")){
	 * String tense=token.getFeat(); if (tense==null)
	 * System.out.println(token.toString()); else if (tense.startsWith("M"))
	 * cat="PAS";//passive else{
	 * cat="A_"+verbCat.getTransitivity(token.lemma()); } }else if
	 * (tag.equals("PR")){ cat=proCat.catWithWord(token.lemma()); }else if
	 * (tag.equals("DET")){ cat="PREM"; }else cat=token.tag(); } else { switch
	 * (token.getNer()){ case B_LOC: cat="LOC"; break; case I_LOC: cat="LOC";
	 * break; case B_ORG: cat="ORG"; break; case I_ORG: cat="ORG"; break; case
	 * B_PER: cat="ANM"; break; case I_PER: cat="ANM"; break; case O://
	 * classification using wordnet: default: String tag=token.tag(); if
	 * (tag.equals("N") || tag.equals("AJ") ){
	 * cat=nounCat.catWithWord(token.lemma()); }else if (tag.equals("V")){
	 * String tense=token.getFeat(); if (tense==null)
	 * System.out.println(token.toString()); else if (tense.startsWith("M"))
	 * cat="PAS";//passive else{
	 * cat="A_"+verbCat.getTransitivity(token.lemma()); } }else if
	 * (tag.equals("PR")){ cat=proCat.catWithWord(token.lemma()); }else if
	 * (tag.equals("DET")){ cat="PREM"; }else cat=token.tag(); break; } }
	 * 
	 * 
	 * return cat; }
	 */
}
