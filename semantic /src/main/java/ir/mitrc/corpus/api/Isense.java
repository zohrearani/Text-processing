package ir.mitrc.corpus.api;

import java.util.ArrayList;


public interface Isense {
	
	/*****
	 * this function returns word that connected to entered sense. if input parameter is true ,
	 * means classical version and if input parameter is false , means the faster version.
	 * classical version is more precise but a little slower. 
	 * @return label of Word(String).
	 */
	public String getWord(boolean option);
	
	/*****
	 * this function return URI of Synset that specified Sense belongs it.
	 * @return Synset URI(String).
	 */
	public String getSynsetUri();
	
	public ArrayList<String> getRelatedSenses(IRelationType relationType);
}
