package ir.mitrc.corpus.api;

import java.util.ArrayList;
import java.util.Iterator;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class NounSense extends Sense implements Isense{
	
	public NounSense (OntModel onto,String senseUri){
		super(onto, senseUri);
	}
	public NounSense (String senseUri){
		super(senseUri);
	}
	private ArrayList<String> listSinglesOfSense(){
		ArrayList<String> sensesUris=new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual sense=super.onto.getIndividual(super.getUri());
		if (sense==null){
			return sensesUris;
		}
		@SuppressWarnings("static-access")
		ObjectProperty hashSingle=super.onto.getObjectProperty("http://www.mitrc.ir/mobina#Single");
		Iterator<RDFNode> singles=sense.listPropertyValues(hashSingle);
		while (singles.hasNext()){
			RDFNode eachSingle=singles.next();
			Resource eachSingleRsc=eachSingle.asResource();
			if (eachSingleRsc==null){
				continue;
			}
			String eachSingleUri=eachSingleRsc.getURI();
			if (eachSingleUri==null){
				continue;
			}
			@SuppressWarnings("static-access")
			Individual eachSingleIndiv=super.onto.getIndividual(eachSingleUri);
			if (eachSingleIndiv==null){
				continue;
			}
			sensesUris.add(eachSingleIndiv.getURI());
		}
		return sensesUris;
	}
	
	/*private ArrayList<String> listPluralsOfSense(){
		ArrayList<String> sensesUris=new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual sense=super.onto.getIndividual(super.getUri());
		if (sense==null){
			return sensesUris;
		}
		@SuppressWarnings("static-access")
		ObjectProperty hasPlural=super.onto.getObjectProperty("http://www.mitrc.ir/farsnet#RV5LtSLTSdy0wsFb7176Oi");
		Iterator<RDFNode> plurals=sense.listPropertyValues(hasPlural);
		while (plurals.hasNext()){
			RDFNode eachPlural=plurals.next();
			Resource eachPluralRsc=eachPlural.asResource();
			if (eachPluralRsc==null){
				continue;
			}
			String eachPluralUri=eachPluralRsc.getURI();
			if (eachPluralUri==null){
				continue;
			}
			@SuppressWarnings("static-access")
			Individual eachPluralIndiv=super.onto.getIndividual(eachPluralUri);
			if (eachPluralIndiv==null){
				continue;
			}
			sensesUris.add(eachPluralIndiv.getURI());
		}
		return sensesUris;
	}*/
	public ArrayList<String> getRelatedSense(IRelationType senseRelationType){
		if (!(senseRelationType instanceof SenseRelationType)){
			//TODO System.err.println("WRONG RELATION TYPE");
			return null;
		}
		switch((SenseRelationType) senseRelationType){
			case HasSingle:
				return this.listSinglesOfSense();
			default:
				break;	
			}
		
		
		return null;
		
	}

}
