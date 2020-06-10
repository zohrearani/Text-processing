package ir.mitrc.corpus.api;

import com.hp.hpl.jena.ontology.OntModel;

public class AdjectiveSense extends Sense {
	
	public AdjectiveSense (OntModel onto,String senseUri){
		super(onto, senseUri);
	}
	public AdjectiveSense (String senseUri){
		super(senseUri);
	}

}
