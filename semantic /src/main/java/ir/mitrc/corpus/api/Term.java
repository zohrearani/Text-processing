package ir.mitrc.corpus.api;

public class Term {
	String lemma="";
	String pos="";
	int frequency=0;
	
	public Term(String lemma,String pos,int frequency){
		this.lemma=lemma;
		this.pos=pos;
		this.frequency=frequency;
	}
	public String getLemma(){
		return this.lemma;
	}
	public String getPos(){
		return this.pos;
	}
	public int getFrequency(){
		return this.frequency;
	}

	

}
