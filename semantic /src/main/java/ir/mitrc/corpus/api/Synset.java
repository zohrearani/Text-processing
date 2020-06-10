package ir.mitrc.corpus.api;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


public class Synset implements Isynset {
	static OntModel onto=ApiFactory.onto;
	protected String ns =ApiFactory.ns ;
	private String uri =null;	
	private SynsetPos pos =null;	
	private String example =null;		
	private String gloss =null;
	private String label=null;
	private ArrayList<Term> terms=new ArrayList<Term>();

	///TODO wordnet map
	//ArrayList<SynsetMappedWNsynId> MappedWNsynId=new ArrayList<SynsetMappedWNsynId>(); ///objectProperty
	//ArrayList <SynsetSense> Sense=new ArrayList<SynsetSense>(); ///objectProperty

/*	public Synset(OntModel onto,String synsetUri){
		this.setUri(synsetUri);
		this.setFromOnto();
		this.setPos(getSynsetPos(synsetUri));
		this.terms=ApiFactory.listTermOfSynset(this.uri);

	}*/
	public Synset(String synsetUri){
		this.setUri(synsetUri);
		this.setFromOnto();
		this.setPos(getSynsetPos(synsetUri));
		this.terms=ApiFactory.listTermOfSynset(this.listWordsOfSynset(), this.pos);

	}
	
	
	
	
	private void setFromOnto() {
		
		Individual synset=onto.getIndividual(this.getUri());
		if (synset==null){
			System.err.println("null synset: "+this.getUri());
			return;
		}
		OntProperty gloss=onto.getOntProperty(ns+"hasGloss");
		Iterator<RDFNode>sharh=synset.listPropertyValues(gloss);
		
		String glossOfSynset=null;
		while(sharh.hasNext()){
			glossOfSynset+=sharh.next().asLiteral().getValue().toString()+"\t\n";
		}
		this.setGloss(glossOfSynset);
		
		
		OntProperty example=onto.getOntProperty(ns+"hasExample");
		Iterator<RDFNode> mesal=synset.listPropertyValues(example);
		String exampleOfSynset=null;
		while(mesal.hasNext()){
			exampleOfSynset+=mesal.next().asLiteral().getValue().toString()+"\t\n";
		}
		this.setExample(exampleOfSynset);
		
		String label=synset.getLabel("");
		this.setLabel(label);
		
	}
	private void setLabel(String label) {
		this.label=label;
	}
	private void setUri(String uri) {
		this.uri=uri;

	}
	
	protected void setExample(String example){
		this.example=example;
	}
	protected void setGloss(String gloss){
		this.gloss=gloss;
	}
	protected void setPos(SynsetPos synsetPos){
		this.pos=synsetPos;
	}
	/****
	 * this function returns URI of specified Synset.
	 * @return Synset Uri(String).
	 */
	public String getUri(){
		return this.uri;
	}
	/****
	 * this function returns gloss of specified Synset.
	 * @return gloss of Synset(String).
	 */
	public String getGloss(){
		return this.gloss;
	}
	/****
	 * this function returns example of specified Synset.
	 * @return example of Synset(String).
	 */
	public String getExample(){
		return this.example;
	}
	/*****
	 * this function returns Pos of specified Synset.
	 * @return pos.
	 */
	public SynsetPos getPos(){
		return this.pos;
	}
	/*****
	 * this function returns lable of specified Synset.
	 * @return Synset lable(String).
	 */
	public String getLabel(){
		return this.label;
	}
	/****
	 * this function returns URIs of Synsets that have relatedTo relation with specified Synset.
	 * @return ArrayList of related Synset URIs.
	 */
	public ArrayList<String> getRelatedToSynset(){
		Individual synset=onto.getIndividual(this.getUri());
		OntProperty relatedTo=onto.getOntProperty(ns+"RelatedTo");
		Iterator<RDFNode> relatedSynsets=synset.listPropertyValues(relatedTo);
		ArrayList<String> relatedSynsetUris=new ArrayList<String>();
		while (relatedSynsets.hasNext()){
			Individual synsetIndiv=onto.getIndividual(relatedSynsets.next().asResource().getURI());
			relatedSynsetUris.add(synsetIndiv.getURI());
		}
		return relatedSynsetUris;
	}
	/******
	 * this function returns URIs of Synsets that have antonym relation with specified Synset.
	 * @return ArrayList of URIs of Synset.
	 */
	public ArrayList<String> getAntonymSynsets(){
		Individual synset=onto.getIndividual(this.getUri());
		OntProperty antonym=onto.getOntProperty(ns+"Antonym");
		Iterator<RDFNode> antonymSynsets=synset.listPropertyValues(antonym);
		ArrayList<String> antonymSynsetsUris=new ArrayList<String>();
		while (antonymSynsets.hasNext()){
			Individual synsetIndiv=onto.getIndividual(antonymSynsets.next().asResource().getURI());
			antonymSynsetsUris.add(synsetIndiv.getURI());
		}
		return antonymSynsetsUris;
	}
	/*****
	 * this function returns URIs of Senses that belongs to the specified Synset.
	 * @param synsetUri(String).
	 * @return ArrayList of URIs of Sense.
	 */
	public ArrayList<String> listSensesOfSynset(String synsetUri){
		Individual synset=onto.getIndividual(this.getUri());
		OntProperty countainWordSense=onto.getOntProperty(ns+"containWordSense");
		Iterator<RDFNode>senses=synset.listPropertyValues(countainWordSense);
		ArrayList<String>wordSense=new ArrayList<String>();
		while(senses.hasNext()){
			wordSense.add(senses.next().toString());
		}

		return wordSense;	
	}
	/****
	 * this function returns all Words that belongs to the specified Synset.if input parameter is true,the fast version will be run.
	 * if input parameter is false , the classical version will be run.the classical version is preciser but this is a little slower.
	 * @param boolean fast
	 * @return ArrayList of Words labels.
	 */
	public ArrayList<String> listWordsOfSynset(boolean fast){
		if (fast)
			return listWordsOfSynsetFast();
		else
			return listWordsOfSynsetClassic();
	}
	/*****
	 * this function returns all Words that belongs to the specified Synset.it works with faster version.
	 * @return ArrayList of lable of Words.
	 */
	public ArrayList<String> listWordsOfSynset(){
		return listWordsOfSynsetClassic();
	}
	private ArrayList<String> listWordsOfSynsetFast(){
		ArrayList<String> wordsLabels=new ArrayList<String>();
		String Synsetlabel=this.getLabel();
		Synsetlabel=Synsetlabel.replace("»", "");
		Synsetlabel=Synsetlabel.replace("«", "");
		String[] sensesLabel=Synsetlabel.split("،");
		
		for (String label:sensesLabel){
				wordsLabels.add(label.split("-")[0].split("_")[0]);
				
		}
		
		return wordsLabels;
		
	}
	private ArrayList<String> listWordsOfSynsetClassic(){
		ArrayList<String>wordSense=listSensesOfSynset(this.getUri());
		ArrayList<String> words=new ArrayList<String>();
		for (int i=0;i<wordSense.size();i++){
			Isense sense=new Sense(wordSense.get(i));
			words.add(sense.getWord(false));
		}
		return words;
		
	}
	
	protected static  SynsetPos getSynsetPos(String synsetUri){
		Individual tempindiv=onto.getIndividual(synsetUri);
		if (tempindiv==null)
			return null;
		Iterator<Resource> tempResources=tempindiv.listRDFTypes(true);

		while (tempResources.hasNext()){
			Resource eachTempResources=tempResources.next();
			if (eachTempResources!=null){
				String NameOfTypeOfSynset=eachTempResources.getLocalName();
				if (NameOfTypeOfSynset==null)
					continue;
				if (NameOfTypeOfSynset.equalsIgnoreCase("گروه_معنایی_اسم_ها")){
					return SynsetPos.Noun;
				}
				else if(NameOfTypeOfSynset.equalsIgnoreCase("گروه_معنایی_فعل_ها")){
					return SynsetPos.Verb;
				}
				else if(NameOfTypeOfSynset.equalsIgnoreCase("گروه_معنایی_قیدها")){
					return SynsetPos.Adverb;
				}
				else if(NameOfTypeOfSynset.equalsIgnoreCase("گروه_معنایی_صفت_ها")){
					return SynsetPos.Adjective;
				}
				else if(NameOfTypeOfSynset.equalsIgnoreCase("NameIndividual")){
					continue;
				}
			}
			
		}
		return null;
	}
	/*****
	 * this function returns URIs of all Senses that belongs to the specified Synset.
	 * @return ArrayList of URIs of Senses.
	 */
	public ArrayList<String> listSensesOfSynset(){
		Individual vazhe=onto.getIndividual(this.getUri());
		OntProperty countainWordSense=onto.getOntProperty(ns+"containWordSense");
		Iterator<RDFNode>senses=vazhe.listPropertyValues(countainWordSense);
		ArrayList<String>wordSense=new ArrayList<String>();
		ArrayList<String> wordSenseLabels=new ArrayList<String>();
		while(senses.hasNext()){
			wordSense.add(senses.next().toString());
		}
		for (int i=0;i<wordSense.size();i++){
			String wordSenseLbl=onto.getIndividual(wordSense.get(i)).getLabel("");
			wordSenseLabels.add(wordSenseLbl);
		}
		for(int k=0;k<wordSenseLabels.size();k++){
			System.out.println("sense are: "+wordSenseLabels.get(k));
		}
		
		return wordSense;	
	}
	
	/*****
	 * this function returns URIs of Synsets that have Domain Relation with specified Synset.
	 * @return ArrayList of URIs of Synsets.
	 */
	public ArrayList<String> getDomainSynset(){
		Individual synset=onto.getIndividual(this.getUri());
		OntProperty domain=onto.getOntProperty(ns+"Domain");
		Iterator<RDFNode> domainSynset=synset.listPropertyValues(domain);
		ArrayList<String> domainSynsetUris=new ArrayList<String>();
		while (domainSynset.hasNext()){
			Individual synsetIndiv=onto.getIndividual(domainSynset.next().asResource().getURI());
			domainSynsetUris.add(synsetIndiv.getURI());
		}
		return domainSynsetUris;
	}
	/*****
	 * this function returns URIs of Synsets that have DomainV2 Relation with specified Synset.
	 * @return ArrayList of URIs of Synsets.
	 */
	public ArrayList<String> getDomainSynsetV2(){
		Individual synset=onto.getIndividual(this.getUri());
		OntProperty domain2=onto.getOntProperty("http://www.mitrc.ir/farsnet#R9aJzT1ASc96L6dxvtQTdU");
		Iterator<RDFNode> domainSynsetV2=synset.listPropertyValues(domain2);
		ArrayList<String> domainSynsetUrisV2=new ArrayList<String>();
		while (domainSynsetV2.hasNext()){
			Individual synsetIndiv=onto.getIndividual(domainSynsetV2.next().asResource().getURI());
			domainSynsetUrisV2.add(synsetIndiv.getURI());
		}
		return domainSynsetUrisV2;
	}
	/*****
	 * this function returns URIs of Synsets that have any relation with specified Sysnet.
	 * @return ArrayList of URIs of related Synsets.
	 */
	public ArrayList<String> listAllRelatedSynsets() {
		ArrayList<String> relatedSynsetsUris=new ArrayList<String>();
		Individual synset=onto.getIndividual(this.getUri());
		if (synset==null){
			return relatedSynsetsUris;
		}
		StmtIterator allProperties=synset.listProperties();
		while (allProperties.hasNext()){
			Statement eachStmt=allProperties.nextStatement();
			Resource subject=eachStmt.getSubject();
			RDFNode object=eachStmt.getObject();
			Property predicate = eachStmt.getPredicate();
			if (subject!=null && object!=null && predicate!=null && !object.isLiteral()){
				String subjectUri=subject.getURI();
				String objectUri=object.asResource().getURI();
				if (subjectUri!=null && objectUri!=null){
					Individual objectIndiv=onto.getIndividual(objectUri);
					if (objectIndiv!=null && objectIndiv!=null){
						Iterator<Resource> objTypes=objectIndiv.listRDFTypes(true);
						Resource syn=onto.getResource(ns+"SynSet");
						Resource nounSyn=onto.getResource(ns+"Noun");
						Resource verbSyn=onto.getResource(ns+"Verb");
						Resource adjSyn=onto.getResource(ns+"Adjective");
						Resource advSyn=onto.getResource(ns + "Adverb");
						while (objTypes.hasNext()){
							Resource eachType=objTypes.next();
							if (eachType.equals(syn) || eachType.equals(nounSyn) || eachType.equals(verbSyn) || eachType.equals(adjSyn) || eachType.equals(advSyn)){
								relatedSynsetsUris.add(objectUri);
							}
						}


					}
				}

			}
		}

		return relatedSynsetsUris;
	}
	
	public HashMap<String, String> listAllRelatedSynsetsWithRelationType(){
		HashMap<String, String> synset2RelationType=new HashMap<String,String>();
		Individual synset=onto.getIndividual(this.getUri());
		if (synset==null){
			return synset2RelationType;
		}
		StmtIterator allProperties=synset.listProperties();
		while (allProperties.hasNext()){
			Statement eachStmt=allProperties.nextStatement();
			Resource subject=eachStmt.getSubject();
			RDFNode object=eachStmt.getObject();
			Property predicate = eachStmt.getPredicate();
			if (subject!=null && object!=null && predicate!=null && !object.isLiteral()){
				String subjectUri=subject.getURI();
				String objectUri=object.asResource().getURI();
				if (subjectUri!=null && objectUri!=null){
					Individual objectIndiv=onto.getIndividual(objectUri);
					if (objectIndiv!=null && objectIndiv!=null){
						Iterator<Resource> objTypes=objectIndiv.listRDFTypes(true);
						Resource syn=onto.getResource(ns+"SynSet");
						Resource nounSyn=onto.getResource(ns+"Noun");
						Resource verbSyn=onto.getResource(ns+"Verb");
						Resource adjSyn=onto.getResource(ns+"Adjective");
						Resource advSyn=onto.getResource(ns + "Adverb");
						while (objTypes.hasNext()){
							Resource eachType=objTypes.next();
							if (eachType.equals(syn) || eachType.equals(nounSyn) || eachType.equals(verbSyn) || eachType.equals(adjSyn) || eachType.equals(advSyn)){
								synset2RelationType.put(objectUri, predicate.getURI());
							}
						}


					}
				}

			}
		}
		
		return synset2RelationType;
	}
	/*****
	 * this function returns URIs of Synsets that have specified relation with specified Synset.
	 * @param Relation Type
	 * @return ArrayList of URIs of related Synsets.
	 */
	public ArrayList<String> getRelatedSynset(IRelationType relationType) {
		
		switch (this.getPos()){
		case Noun:
			NounSynset nounSynset=new NounSynset(this.getUri());
			return nounSynset.getRelatedSynset(relationType);
		case Verb:
			VerbSynset verbSynset=new VerbSynset(this.getUri());
			return verbSynset.getRelatedSynset(relationType);
		case Adjective:
			AdjectiveSynset adjectiveSynset=new AdjectiveSynset(this.getUri());
			return adjectiveSynset.getRelatedSynset(relationType);	
		case Adverb:
			AdverbSynset adverbSynset=new AdverbSynset(this.getUri());
			return adverbSynset.getRelatedSynset(relationType);
			
		default:
			break;
		}
		return null;
	}
	public ArrayList<Term> listTerms(){
		return this.terms;
	}
	
}
