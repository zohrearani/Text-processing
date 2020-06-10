package ir.mitrc.corpus.api;

import com.hp.hpl.jena.ontology.OntModel;

public class VerbSense extends Sense {
	
	public VerbSense (OntModel onto,String senseUri){
		super(onto, senseUri);
	}
	public VerbSense (String senseUri){
		super(senseUri);
	}

}
