package ir.mitrc.corpus.api;

public class OntWord {
	String pronounciation="";
	int frequency=0;
	String wordUri="";
	
	public OntWord(String wordUri,String pronounciation,int frequency){
		this.pronounciation=pronounciation;
		this.frequency=frequency;
		this.wordUri=wordUri;
	}

}
