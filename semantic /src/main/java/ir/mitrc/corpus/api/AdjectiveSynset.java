package ir.mitrc.corpus.api;

import java.util.ArrayList;
import java.util.Iterator;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public  class AdjectiveSynset extends Synset implements Isynset{
	
	/*public AdjectiveSynset (OntModel onto,String synsetUri){
		super(onto, synsetUri);
	}*/
	public AdjectiveSynset (String synsetUri){
		super(synsetUri);
	}
	
	protected ArrayList<String> listSynonymSynsets(){
		ArrayList<String> synonymSynsetsUris=new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset=super.onto.getIndividual(super.getUri());
		if (synset==null){
			return synonymSynsetsUris;
		}
		@SuppressWarnings("static-access")
		OntProperty synonym=super.onto.getOntProperty(super.ns+"Synonym");
		Iterator<RDFNode> synonymSynsets=synset.listPropertyValues(synonym);
		while (synonymSynsets.hasNext()){
			RDFNode eachSynonymSynset=synonymSynsets.next();
			Resource eachSynonymSynsetRsc=eachSynonymSynset.asResource();
			if (eachSynonymSynsetRsc==null){
				continue;
			}
			String eachSynonymSynsetUri=eachSynonymSynsetRsc.getURI();
			if (eachSynonymSynsetUri==null){
				continue;
			}
			@SuppressWarnings("static-access")
			Individual synsetIndiv=super.onto.getIndividual(eachSynonymSynsetUri);
			if (synsetIndiv==null){
				continue;
			}
			synonymSynsetsUris.add(synsetIndiv.getURI());
		}
		return synonymSynsetsUris;
	}
	
	protected ArrayList<String> listAttributeSynsets(){
		ArrayList<String> attributeSynsetsUris=new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset=super.onto.getIndividual(super.getUri());
		if (synset==null){
			return attributeSynsetsUris;
		}
		@SuppressWarnings("static-access")
		OntProperty attribute=super.onto.getOntProperty("http://www.mitrc.ir/farsnet#attribute");
		Iterator<RDFNode> attributeSynsets=synset.listPropertyValues(attribute);
		while (attributeSynsets.hasNext()){
			RDFNode eachAttributeSynset=attributeSynsets.next();
			Resource eachAttributeSynRsc=eachAttributeSynset.asResource();
			if (eachAttributeSynRsc==null){
				continue;
			}
			String eachAttributeSynUri=eachAttributeSynRsc.getURI();
			if (eachAttributeSynUri==null){
				continue;
			}
			@SuppressWarnings("static-access")
			Individual synsetIndiv=super.onto.getIndividual(eachAttributeSynUri);
			if (synsetIndiv==null){
				continue;
			}
			attributeSynsetsUris.add(synsetIndiv.getURI());
		}
		return attributeSynsetsUris;
	}
	
	protected ArrayList<String> listHasAttributeSynsets(){
		ArrayList<String> hasAttributeSynsetsUris=new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset=super.onto.getIndividual(super.getUri());
		if (synset==null){
			return hasAttributeSynsetsUris;
		}
		@SuppressWarnings("static-access")
		OntProperty hasAttribute=super.onto.getOntProperty("http://www.mitrc.ir/farsnet#HasAttribute");
		Iterator<RDFNode> hasAttributeSynsets=synset.listPropertyValues(hasAttribute);
		while (hasAttributeSynsets.hasNext()){
			RDFNode eachHasAttributeSynset=hasAttributeSynsets.next();
			Resource eachHasAttributeSynRsc=eachHasAttributeSynset.asResource();
			if (eachHasAttributeSynRsc==null){
				continue;
			}
			String eachHasAttributeSynUri=eachHasAttributeSynRsc.getURI();
			if (eachHasAttributeSynUri==null){
				continue;
			}
			@SuppressWarnings("static-access")
			Individual synsetIndiv=super.onto.getIndividual(eachHasAttributeSynUri);
			if (synsetIndiv==null){
				continue;
			}
			hasAttributeSynsetsUris.add(synsetIndiv.getURI());
		}
		return hasAttributeSynsetsUris;
	}
/*****
 * this function returns ArrayList of Synset URIs that have entered relation with specified Synset.
 * @return ArrayList of Synset URIs.
 */
public ArrayList<String> getRelatedSynset(IRelationType relationType){
	if (!(relationType instanceof AdjectiveRelationType)){
		//TODO System.err.println("WRONG RELATION TYPE");
		return null;
	}
		
		switch((AdjectiveRelationType) relationType){
			case Similar:
				return this.listSynonymSynsets();
			case AttributeOf:
				return this.listAttributeSynsets();
			case HasAttribute:
				return this.listHasAttributeSynsets();
		default:
			break;	
			}
		
		
		return null;
		
	}


}
