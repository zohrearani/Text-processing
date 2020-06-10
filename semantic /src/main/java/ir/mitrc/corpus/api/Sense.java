package ir.mitrc.corpus.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Pattern;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class Sense implements Isense {
	
	static OntModel onto=ApiFactory.onto;
	private String uri ="";	
	String ns = ApiFactory.ns;
	
	public Sense(OntModel onto,String senseUri){
		Sense.onto=onto;
		this.setUri(senseUri);
	}
	public Sense(String senseUri){
		this.setUri(senseUri);
	}
	
	private void setUri(String uri) {
		this.uri=uri;
		
	}
	/****
	 * this function returns URI of specified Sense.
	 * @return sense URI.
	 */
	public String getUri(){
		return this.uri;
	}
	
	
	public String getWord(boolean option){
		String word="";
		if (option){
			word=getWordClassic();
		}
		else {
			word=getWordFast();
		}
		return word;
	}
	@SuppressWarnings("unused")
	private String getWordClassicWithoutInference() {
		Individual sense=onto.getIndividual(this.getUri());
		if (sense==null){
			return null;
		}
		Iterator<Individual> allWords=onto.listIndividuals();
		OntProperty means=onto.getOntProperty("http://www.mitrc.ir/farsnet#means");
		String outputWord="";
		while (allWords.hasNext()){
			Individual eachWord = allWords.next();
			if (eachWord==null){
				continue;
			}
			if (eachWord.hasProperty(means)){
				Iterator<RDFNode> wordMeans=eachWord.listPropertyValues(means);
				while (wordMeans.hasNext()){
					RDFNode wordMean=wordMeans.next();
					if (wordMean==null){
						continue;
					}
					String wordMeanUri=wordMean.asResource().getURI();
					if (wordMeanUri!=null){
						Individual wordMeanIndiv=onto.getIndividual(wordMeanUri);
						if (wordMeanIndiv!=null && wordMeanIndiv.equals(sense)){
							outputWord=eachWord.getLabel("");
						}
					}
				}
			}
		}
		return outputWord;
	}
	
	private String getWordClassic(){
		String wordLable=null;
		Individual sense=onto.getIndividual(this.getUri());
		if (sense==null){
			return wordLable;
		}
		//TODO
		ObjectProperty inverseMeans=onto.getObjectProperty(ns+"inverseMeans");
		Iterator<RDFNode> words=sense.listPropertyValues(inverseMeans);
		while (words.hasNext()){
			RDFNode eachWord = words.next();
			if (eachWord==null){
				continue;
			}
			Resource eachWordRsc=eachWord.asResource();
			if (eachWordRsc==null){
				continue;
			}
			String eachWordUri=eachWordRsc.getURI();
			if (eachWordUri==null){
				continue;
			}
			Individual eachWordIndiv=onto.getIndividual(eachWordUri);
			if (eachWordIndiv==null){
				continue;
			}
			wordLable=eachWordIndiv.getLabel("");
			break;
		}
		return wordLable;
		
	}
	
	private String getWordFast(){
		Individual sense = onto.getIndividual(this.getUri());
		if (sense==null){
			return null;
		}
		String senseLabel=sense.getLabel("");
		if (senseLabel==null){
			return null;
		}
		return senseLabel.split(Pattern.quote("-"))[0].split("_")[0];
		 
	}
	/******
	 * this function returns URI of Synset that specified Sense belongs it.
	 * @return Synset URI.
	 */
	public String getSynsetUri(){
		Individual sense=onto.getIndividual(this.getUri());
		if (sense==null){
			return null;
		}
		OntProperty inSynset=onto.getOntProperty(ns+"inSynset");
		RDFNode synset=sense.getPropertyValue(inSynset);
		if (synset==null){
			return null;
		}
		String synsetUri=synset.asResource().getURI();
		if (synsetUri==null){
			return null;
		}
		return synsetUri;
	}
	private ArrayList<String> listHasSingleSenses(){
		ArrayList<String> hasSingleSenseUris=new ArrayList<String>();
		Individual sense=onto.getIndividual(this.getUri());
		if (sense==null){
			return hasSingleSenseUris;
		}
		OntProperty hasSingle=onto.getOntProperty(ns+"Single");
		Iterator<RDFNode> hasSingleSenses=sense.listPropertyValues(hasSingle);
		if (hasSingleSenses==null){
			return hasSingleSenseUris;
		}
		while (hasSingleSenses.hasNext()){
			Resource senseRsc=hasSingleSenses.next().asResource();
			if (senseRsc==null){
				continue;
			}
			String senseIndivUri=senseRsc.getURI();
			if (senseIndivUri==null){
				continue;
			}
			Individual senseIndiv=onto.getIndividual(senseIndivUri);
			if (senseIndiv==null){
				continue;
			}
			
			hasSingleSenseUris.add(senseIndiv.getURI());
		}
		return hasSingleSenseUris;
	}
	
	private ArrayList<String> listHasPluralSenses(){
		ArrayList<String> pluralSenseUris=new ArrayList<String>();
		Individual sense=onto.getIndividual(this.getUri());
		if (sense==null){
			return pluralSenseUris;
		}
		OntProperty hasPlural=onto.getOntProperty("http://www.mitrc.ir/farsnet#HasPlural");
		Iterator<RDFNode> pluralSenses=sense.listPropertyValues(hasPlural);
		if (pluralSenses==null){
			return pluralSenseUris;
		}
		while (pluralSenses.hasNext()){
			Resource senseRsc=pluralSenses.next().asResource();
			if (senseRsc==null){
				continue;
			}
			String senseIndivUri=senseRsc.getURI();
			if (senseIndivUri==null){
				continue;
			}
			Individual senseIndiv=onto.getIndividual(senseIndivUri);
			if (senseIndiv==null){
				continue;
			}
			
			pluralSenseUris.add(senseIndiv.getURI());
		}
		return pluralSenseUris;
	}
	
	public ArrayList<String> getRelatedSenses(IRelationType relationType){
		if (!(relationType instanceof SenseRelationType)){
			//TODO System.err.println("WRONG RELATION TYPE");
			return null;
		}
		switch ((SenseRelationType) relationType){
			case HasPlural:
				return this.listHasPluralSenses();
			case HasSingle :
				return this.listHasSingleSenses();
			default :
				break;
		}
		return null;
	}

	
}
