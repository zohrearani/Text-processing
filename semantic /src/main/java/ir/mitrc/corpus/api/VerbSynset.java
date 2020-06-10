package ir.mitrc.corpus.api;

import java.util.ArrayList;
import java.util.Iterator;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class VerbSynset extends Synset implements Isynset{
	
	/*public VerbSynset (OntModel onto,String synsetUri){
		super(onto, synsetUri);
	}*/
	public VerbSynset (String synsetUri){
		super(synsetUri);
	}

	private ArrayList<String> listCausedSynset(){
		ArrayList<String> causedSynsetUris=new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset=super.onto.getIndividual(super.getUri());
		if (synset==null){
			return causedSynsetUris;
		}
		@SuppressWarnings("static-access")
		OntProperty caused=super.onto.getOntProperty(super.ns+"Causes");
		Iterator<RDFNode> causedSynset=synset.listPropertyValues(caused);
		while (causedSynset.hasNext()){
			RDFNode eachCausedSynset=causedSynset.next();
			Resource eachCausedSynsetRsc=eachCausedSynset.asResource();
			if (eachCausedSynsetRsc==null){
				continue;
			}
			String eachCausedSynsetUri=eachCausedSynsetRsc.getURI();
			if (eachCausedSynsetUri==null){
				continue;
			}
			@SuppressWarnings("static-access")
			Individual synsetIndiv=super.onto.getIndividual(eachCausedSynsetUri);
			if (synsetIndiv==null){
				continue;
			}
			causedSynsetUris.add(synsetIndiv.getURI());
		}
		return causedSynsetUris;
	}
	
	private ArrayList<String> listIsCausedBySynset(){
		ArrayList<String> isCausedBySynsetUris=new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset=super.onto.getIndividual(super.getUri());
		if (synset==null){
			return isCausedBySynsetUris;
		}
		@SuppressWarnings("static-access")
		OntProperty isCausedBy=super.onto.getOntProperty(super.ns+"IsCausedBy");
		Iterator<RDFNode> isCausedBySynsets=synset.listPropertyValues(isCausedBy);
		while (isCausedBySynsets.hasNext()){
			RDFNode eachIsCausedBySyn=isCausedBySynsets.next();
			Resource eachIsCausedBySynRsc=eachIsCausedBySyn.asResource();
			if (eachIsCausedBySynRsc==null){
				continue;
			}
			String eachIsCausedByUri=eachIsCausedBySynRsc.getURI();
			if (eachIsCausedByUri==null){
				continue;
			}
			@SuppressWarnings("static-access")
			Individual synsetIndiv=super.onto.getIndividual(eachIsCausedByUri);
			if (synsetIndiv==null){
				continue;
			}
			isCausedBySynsetUris.add(synsetIndiv.getURI());
		}
		return isCausedBySynsetUris;
	}
	
	private ArrayList<String> listHypernymSynset(){
		ArrayList<String> hypernymSynsetUris=new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset=super.onto.getIndividual(super.getUri());
		if (synset==null){
			return hypernymSynsetUris;
		}
		@SuppressWarnings("static-access")
		OntProperty hypernym=super.onto.getOntProperty(super.ns+"Hypernym");
		Iterator<RDFNode> hypernymSynset=synset.listPropertyValues(hypernym);
		while (hypernymSynset.hasNext()){
			RDFNode eachHypernymSynset=hypernymSynset.next();
			Resource eachHypernymSynRsc=eachHypernymSynset.asResource();
			if (eachHypernymSynRsc==null){
				continue;
			}
			String eachHypernymUri=eachHypernymSynRsc.getURI();
			if (eachHypernymUri==null){
				continue;
			}
			@SuppressWarnings("static-access")
			Individual synsetIndiv=super.onto.getIndividual(eachHypernymUri);
			if (synsetIndiv==null){
				continue;
			}
			hypernymSynsetUris.add(synsetIndiv.getURI());
		}
		return hypernymSynsetUris;
	}
	
	private ArrayList<String> listHyponymSynset(){
		ArrayList<String> hyponymSynsetUris=new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset=super.onto.getIndividual(super.getUri());
		if (synset==null){
			return hyponymSynsetUris;
		}
		@SuppressWarnings("static-access")
		OntProperty hyponym=super.onto.getOntProperty(super.ns+"Hyponym");
		Iterator<RDFNode> hyponymSynset=synset.listPropertyValues(hyponym);
		while (hyponymSynset.hasNext()){
			RDFNode eachHyponymSynset=hyponymSynset.next();
			Resource eachHyponymSynRsc=eachHyponymSynset.asResource();
			if (eachHyponymSynRsc==null){
				continue;
			}
			String eachHyponymUri=eachHyponymSynRsc.getURI();
			if (eachHyponymUri==null){
				continue;
			}
			@SuppressWarnings("static-access")
			Individual synsetIndiv=super.onto.getIndividual(eachHyponymUri);
			if (synsetIndiv==null){
				continue;
			}
			hyponymSynsetUris.add(synsetIndiv.getURI());
		}
		return hyponymSynsetUris;
	}
	
	private ArrayList<String> listIndirectHypernymSynset (){
		ArrayList<String> indirectHypernymSynsetUris=new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset=super.onto.getIndividual(super.getUri());
		if (synset==null){
			return indirectHypernymSynsetUris;
		}
		@SuppressWarnings("static-access")
		ObjectProperty indirectHypernym=super.onto.getObjectProperty("http://www.mitrc.ir/farsnet#indirectHypernym");
		Iterator<RDFNode> indirectHypernymSynset=synset.listPropertyValues(indirectHypernym);
		if (indirectHypernymSynset==null){
			return indirectHypernymSynsetUris;
		}
		while (indirectHypernymSynset.hasNext()){
			RDFNode eachIndirectHypernym=indirectHypernymSynset.next();
			Resource eachIndirectHypernymRsc=eachIndirectHypernym.asResource();
			if (eachIndirectHypernymRsc==null){
				continue;
			}
			String eachIndirectHypernymUri=eachIndirectHypernymRsc.getURI();
			if (eachIndirectHypernymUri==null){
				continue;
			}
			indirectHypernymSynsetUris.add(eachIndirectHypernymUri);
		}

		return indirectHypernymSynsetUris;
	}
	
	private ArrayList<String> listIndirectHyponymSynset (){
		ArrayList<String> indirectHyponymSynsetUris=new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset=super.onto.getIndividual(super.getUri());
		if (synset==null){
			return indirectHyponymSynsetUris;
		}
		@SuppressWarnings("static-access")
		ObjectProperty indirectHyponym=super.onto.getObjectProperty("http://www.mitrc.ir/farsnet#indirectHyponym");
		Iterator<RDFNode> indirectHyponymSynset=synset.listPropertyValues(indirectHyponym);
		if (indirectHyponymSynset==null){
			return indirectHyponymSynsetUris;
		}
		while (indirectHyponymSynset.hasNext()){
			RDFNode eachIndirectHyponym=indirectHyponymSynset.next();
			Resource eachIndirectHyponymRsc=eachIndirectHyponym.asResource();
			if (eachIndirectHyponymRsc==null){
				continue;
			}
			String eachIndirectHyponymUri=eachIndirectHyponymRsc.getURI();
			if (eachIndirectHyponymUri==null){
				continue;
			}
			indirectHyponymSynsetUris.add(eachIndirectHyponymUri);
		}

		return indirectHyponymSynsetUris;
	}
/*****
 * this function returns URIs of all Synsets that have specified relation with specified Synset.
 * @param relation Type
 * @return ArrayList of Synset URIs.
 */
public ArrayList<String> getRelatedSynset(IRelationType relationType){
	if (!(relationType instanceof VerbRelationType)){
		//TODO System.err.println("WRONG RELATION TYPE");
		return null;
	}
		switch((VerbRelationType) relationType){
			case Hypernym:
				return this.listHypernymSynset();
			case Hyponym:
				return this.listHyponymSynset();
			case Couse:
				return this.listCausedSynset();
			case CausedBy:
				return this.listIsCausedBySynset();
			case Entail : 
				//TODO must be added.
				return null;
			case EntailedBy :
				//TODO must be added.
				return null;
			case IndirectHypernym:
				return this.listIndirectHypernymSynset();
			case IndirectHyponym:
				return this.listIndirectHyponymSynset();
		default:
			break;	
			}
		
		
		return null;
		
	}

}
