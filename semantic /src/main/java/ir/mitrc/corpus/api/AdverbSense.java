package ir.mitrc.corpus.api;

import com.hp.hpl.jena.ontology.OntModel;

public class AdverbSense extends Sense {
	
	public AdverbSense (OntModel onto,String senseUri){
		super(onto, senseUri);
	}
	public AdverbSense (String senseUri){
		super(senseUri);
	}
}
