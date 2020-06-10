package ir.mitrc.corpus.api;

import java.util.ArrayList;

public  class AdverbSynset extends Synset implements Isynset {
	
	/*public AdverbSynset (OntModel onto,String synsetUri){
		super(onto, synsetUri);
	}*/
	public AdverbSynset (String synsetUri){
		super(synsetUri);
	}
	/*****
	 * this function returns ArrayList of Synset URIs that have entered relation with specified Synset.
	 * @param Irelation
	 * @return ArrayList of Synset URIs.
	 */
public ArrayList<String> getRelatedSynset(IRelationType relationType){
	if (!(relationType instanceof AdverbRelationType)){
		//TODO System.err.println("WRONG RELATION TYPE");
		return null;
	}
	return null;
		
	}
}
