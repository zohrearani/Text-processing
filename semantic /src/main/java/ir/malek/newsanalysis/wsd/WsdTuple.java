package ir.malek.newsanalysis.wsd;

import ir.mitrc.corpus.api.Term;

import java.util.ArrayList;

public class WsdTuple {
	String synsetUri=null;
	ArrayList<Term> synsetTerms=new ArrayList<Term>();
	int inputBasedTf=-1;
	String lemma=null;
	public WsdTuple(String lemma,String synsetUri,int tf,ArrayList<Term> synsetTerms){
		this.synsetUri=synsetUri;
		this.inputBasedTf=tf;
		this.synsetTerms=synsetTerms;
		this.lemma=lemma;
	}
	
	public String getSynsetUri(){
		return this.synsetUri;
		
	}

	public ArrayList<Term> getSynsetTerm() {
		return this.synsetTerms;
	}
	public int getTf(){
		return this.inputBasedTf;
	}
	public String getLemma(){
		return this.lemma;
	}

	public void setSynsetUri(String synset) {
		this.synsetUri=synset;
		
	}

	public void setTerms(ArrayList<Term> synsetTerms) {
		this.synsetTerms=synsetTerms;
		
	}

}
