package ir.mitrc.corpus.api;

import com.hp.hpl.jena.ontology.OntModel;

import java.util.ArrayList;
import java.util.HashMap;

public interface Isynset {
	OntModel onto=ApiFactory.onto;
	String ns = ApiFactory.ns;
	String uri ="";	
	String pos ="";	
	String example ="";		
	String gloss ="";	
	
	///TODO wordnet map
	//ArrayList<SynsetMappedWNsynId> MappedWNsynId=new ArrayList<SynsetMappedWNsynId>(); ///objectProperty
	//ArrayList <SynsetSense> Sense=new ArrayList<SynsetSense>(); ///objectProperty

	/**
	 * this function returns URI of specified Synset.
	 * @return Synset URI(String).
	 */
	public String getUri();
	/**
	 * this function returns gloss of specified Synset!

	 * @return gloss(String). 
 	 */
	public String getGloss();
	/**
	 * this function returns example of specified Synset.

	 * @return example(String).
	 */
	public String getExample();
	/**
	 * this function returns part of speech of specified Sysnset!
	 * @return string (Noun,Verb,Adverb,Adjective)
	 */
	public SynsetPos getPos();
	
	/****
	 * this function returns label of specified Synset!
	 * 
	 */
	public String getLabel(); 
	
	/*****
	 * this function returns list of Synsets that have Related-To relation with specified Synset!
	 * @return list of Synset URIs.
	 */
	public ArrayList<String> getRelatedToSynset();	
	
	/*****
	 * this function returns list of Synsets that are antonym with specified Synset!
	 * @return list of Synset uris.
	 */
	public ArrayList<String> getAntonymSynsets();
	
	
	
	/*****
	 * this function return all senses that belongs specified Synset.
	 * @return list of sense URIs.
	 */
	
	public ArrayList<String> listSensesOfSynset();
	
	/*****
	 * this function returns Synsets that have specified relation with specified Synset.
	 * @param relationType
	 * @return ArrayList of related Synsets URIs.
	 */
	
	public ArrayList<String> getRelatedSynset(IRelationType relationType);

	/*****
	 * this function returns all Synsets that have any relation with specified Synset!
	 * @return ArrayList of Synsets URIs.
	 */

	public ArrayList<String> listAllRelatedSynsets();
	
	/**
	 * this function returns all words that belong specified Synset.if input parameter is true , the fast version will be run
	 * and if input parameter is false , the classical version will be run.the classical version is preciser but 
	 * this is little slower. 
	 * @param boolean
	 * @return ArrayList of Words URIs.
	 */
	public ArrayList<String> listWordsOfSynset(boolean fast);
	
	/****
	 * this function returns all words that belong specified Synset by fast method!
	 * @return ArrayList of Words URIs.
	 */
	public ArrayList<String> listWordsOfSynset();
	
	/****
	 * this function returns all terms of Synset that contains word and POS and frequency
	 * @return ArrayList of terms.
	 */
	public ArrayList<Term> listTerms();
	/******
	 * this function returns all synsets that have any relation with specified synet and their relation uris. 
	 * @return hashMap that maps related synset to its relation uri.
	 */
	
	public HashMap<String, String> listAllRelatedSynsetsWithRelationType();


}
