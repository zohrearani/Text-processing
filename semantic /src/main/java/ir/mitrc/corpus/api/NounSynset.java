package ir.mitrc.corpus.api;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class NounSynset extends Synset implements Isynset {

	/*
	 * public NounSynset (OntModel onto,String synsetUri){ super(onto,
	 * synsetUri); }
	 */
	@SuppressWarnings("static-access")
	OntProperty hypernym = super.onto.getOntProperty(super.ns + "Hypernym");
	@SuppressWarnings("static-access")
	OntProperty holonymMemberOfProperty = super.onto.getOntProperty(super.ns + "HolonymMemberOf");

	OntProperty relatedVerb = super.onto.getOntProperty("http://webprotege.stanford.edu/RphBtBvXqfXta5p1QnVaTt");

	public NounSynset(String synsetUri) {
		super(synsetUri);
	}

	private ArrayList<String> listHasMemberOfSynset() {
		ArrayList<String> HolonymMemberOfSynsetUris = new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset = super.onto.getIndividual(super.getUri());
		if (synset == null) {
			return HolonymMemberOfSynsetUris;
		}
		Iterator<RDFNode> holonymMemberOf = synset.listPropertyValues(holonymMemberOfProperty);
		while (holonymMemberOf.hasNext()) {
			RDFNode eachHolonymMemberOf = holonymMemberOf.next();
			Resource eachHolonymMemberOfRsc = eachHolonymMemberOf.asResource();
			if (eachHolonymMemberOfRsc != null) {
				String eachHolonymMemberOfUri = eachHolonymMemberOfRsc.getURI();
				if (eachHolonymMemberOfUri != null) {
					@SuppressWarnings("static-access")
					Individual synsetIndiv = super.onto.getIndividual(eachHolonymMemberOfUri);
					if (synsetIndiv != null) {
						HolonymMemberOfSynsetUris.add(synsetIndiv.getURI());
					}
				}
			}
		}
		return HolonymMemberOfSynsetUris;
	}

	private ArrayList<String> listHasPartOfSynset() {
		ArrayList<String> HolonymPartOfSynsetUris = new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset = super.onto.getIndividual(super.getUri());
		if (synset == null) {
			return HolonymPartOfSynsetUris;
		}
		@SuppressWarnings("static-access")
		OntProperty holonymPartOfProperty = super.onto.getOntProperty(super.ns + "HolonymPartOf");
		Iterator<RDFNode> holonymPartOf = synset.listPropertyValues(holonymPartOfProperty);
		while (holonymPartOf.hasNext()) {
			RDFNode eachHolonymPartrOf = holonymPartOf.next();
			Resource eachHolonymPartOfRsc = eachHolonymPartrOf.asResource();
			if (eachHolonymPartOfRsc != null) {
				String eachHolonymPartOfUri = eachHolonymPartOfRsc.getURI();
				if (eachHolonymPartOfUri != null) {
					@SuppressWarnings("static-access")
					Individual synsetIndiv = super.onto.getIndividual(eachHolonymPartOfUri);
					if (synsetIndiv != null) {
						HolonymPartOfSynsetUris.add(synsetIndiv.getURI());
					}
				}
			}
		}
		return HolonymPartOfSynsetUris;
	}

	private ArrayList<String> listHasPortionOfSynset() {
		ArrayList<String> HolonymPortionOfSynsetUris = new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset = super.onto.getIndividual(super.getUri());
		if (synset == null) {
			return HolonymPortionOfSynsetUris;
		}
		@SuppressWarnings("static-access")
		OntProperty holonymPortionOfProperty = super.onto.getOntProperty(super.ns + "HolonymPortionOf");
		Iterator<RDFNode> holonymPortionOf = synset.listPropertyValues(holonymPortionOfProperty);
		while (holonymPortionOf.hasNext()) {
			RDFNode eachHolonymPortionOf = holonymPortionOf.next();
			Resource eachHolonymPortionOfRsc = eachHolonymPortionOf.asResource();
			if (eachHolonymPortionOfRsc != null) {
				String eachHolonymPortionOfUri = eachHolonymPortionOfRsc.getURI();
				if (eachHolonymPortionOfUri != null) {
					@SuppressWarnings("static-access")
					Individual synsetIndiv = super.onto.getIndividual(eachHolonymPortionOfUri);
					if (synsetIndiv != null) {
						HolonymPortionOfSynsetUris.add(synsetIndiv.getURI());
					}
				}
			}
		}
		return HolonymPortionOfSynsetUris;
	}

	private ArrayList<String> listIndirectMeronymPartOfSynset() {
		ArrayList<String> indirectMeronymPartOfSynsetUris = new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset = super.onto.getIndividual(super.getUri());
		if (synset == null) {
			return indirectMeronymPartOfSynsetUris;
		}
		@SuppressWarnings("static-access")
		OntProperty indirectMeronymPartOfProperty = super.onto.getOntProperty(super.ns + "indirectMeronymPartOf");
		Iterator<RDFNode> indirectMeronymPartsOf = synset.listPropertyValues(indirectMeronymPartOfProperty);
		while (indirectMeronymPartsOf.hasNext()) {
			RDFNode eachIndirectMeronymPartOf = indirectMeronymPartsOf.next();
			Resource eachIndirectMeronymPartOfRsc = eachIndirectMeronymPartOf.asResource();
			if (eachIndirectMeronymPartOfRsc != null) {
				String eachIndirectMeronymPartOfRscUri = eachIndirectMeronymPartOfRsc.getURI();
				if (eachIndirectMeronymPartOfRscUri != null) {
					@SuppressWarnings("static-access")
					Individual synsetIndiv = super.onto.getIndividual(eachIndirectMeronymPartOfRscUri);
					if (synsetIndiv != null) {
						indirectMeronymPartOfSynsetUris.add(synsetIndiv.getURI());
					}
				}
			}
		}
		return indirectMeronymPartOfSynsetUris;
	}

	private ArrayList<String> listIndirectMeronymPortionOf() {
		ArrayList<String> indirectMeronymPortionOfSynsetUris = new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset = super.onto.getIndividual(super.getUri());
		if (synset == null) {
			return indirectMeronymPortionOfSynsetUris;
		}
		// TODO
		@SuppressWarnings("static-access")
		OntProperty indirectMeronymPortionOfProperty = super.onto.getOntProperty(super.ns + "indirectMeronymPortionOf");
		Iterator<RDFNode> indirectMeronymPortionOf = synset.listPropertyValues(indirectMeronymPortionOfProperty);
		while (indirectMeronymPortionOf.hasNext()) {
			RDFNode eachindirectMeronymPortionOf = indirectMeronymPortionOf.next();
			Resource eachindirectMeronymPortionOfRsc = eachindirectMeronymPortionOf.asResource();
			if (eachindirectMeronymPortionOfRsc != null) {
				String eachindirectMeronymPortionOfRscUri = eachindirectMeronymPortionOfRsc.getURI();
				if (eachindirectMeronymPortionOfRscUri != null) {
					@SuppressWarnings("static-access")
					Individual synsetIndiv = super.onto.getIndividual(eachindirectMeronymPortionOfRscUri);
					if (synsetIndiv != null) {
						indirectMeronymPortionOfSynsetUris.add(synsetIndiv.getURI());
					}
				}
			}
		}
		return indirectMeronymPortionOfSynsetUris;
	}

	private ArrayList<String> listInstancesOfSynset() {
		ArrayList<String> instancesOfSynsetUris = new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset = super.onto.getIndividual(super.getUri());
		if (synset == null) {
			return instancesOfSynsetUris;
		}
		@SuppressWarnings("static-access")
		OntProperty instancesOfProperty = super.onto.getOntProperty(super.ns + "InstaceOf");
		Iterator<RDFNode> instances = synset.listPropertyValues(instancesOfProperty);
		while (instances.hasNext()) {
			RDFNode eachInstance = instances.next();
			Resource instanceRsc = eachInstance.asResource();
			if (instanceRsc != null) {
				String eachInstanceUri = instanceRsc.getURI();
				if (eachInstanceUri != null) {
					@SuppressWarnings("static-access")
					Individual synsetIndiv = super.onto.getIndividual(eachInstanceUri);
					if (synsetIndiv != null) {
						instancesOfSynsetUris.add(synsetIndiv.getURI());
					}
				}
			}
		}
		return instancesOfSynsetUris;
	}

	private ArrayList<String> listIndirectMeronymMemberOfSynset() {
		ArrayList<String> indirectMeronymMemberOfSynsetUris = new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset = super.onto.getIndividual(super.getUri());
		if (synset == null) {
			return indirectMeronymMemberOfSynsetUris;
		}
		// TODO
		@SuppressWarnings("static-access")
		OntProperty indirectMeronymMemberOfProperty = super.onto.getOntProperty(super.ns + "indirectMeronymMemberOf");
		Iterator<RDFNode> indirectMeronymMemberOf = synset.listPropertyValues(indirectMeronymMemberOfProperty);
		while (indirectMeronymMemberOf.hasNext()) {
			RDFNode eachindirectMeronymMemberOf = indirectMeronymMemberOf.next();
			Resource indirectMeronymMemberOfeRsc = eachindirectMeronymMemberOf.asResource();
			if (indirectMeronymMemberOfeRsc != null) {
				String eachindirectMeronymMemberOfeRscUri = indirectMeronymMemberOfeRsc.getURI();
				if (eachindirectMeronymMemberOfeRscUri != null) {
					@SuppressWarnings("static-access")
					Individual synsetIndiv = super.onto.getIndividual(eachindirectMeronymMemberOfeRscUri);
					if (synsetIndiv != null) {
						indirectMeronymMemberOfSynsetUris.add(synsetIndiv.getURI());
					}
				}
			}
		}
		return indirectMeronymMemberOfSynsetUris;
	}

	private ArrayList<String> listIndirectHolonymPartOfSynset() {
		ArrayList<String> indirectHolonymPartOfSynsetUris = new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset = super.onto.getIndividual(super.getUri());
		if (synset == null) {
			return indirectHolonymPartOfSynsetUris;
		}
		// TODO
		@SuppressWarnings("static-access")
		OntProperty indirectHolonymPartOfProperty = super.onto.getOntProperty(super.ns + "indirectHolonymPartOf");
		Iterator<RDFNode> indirectHolonymPartOf = synset.listPropertyValues(indirectHolonymPartOfProperty);
		while (indirectHolonymPartOf.hasNext()) {
			RDFNode eachindirectHolonymPartOf = indirectHolonymPartOf.next();
			Resource indirectHolonymPartOfRsc = eachindirectHolonymPartOf.asResource();
			if (indirectHolonymPartOfRsc != null) {
				String eachindirectHolonymPartOfRscUri = indirectHolonymPartOfRsc.getURI();
				if (eachindirectHolonymPartOfRscUri != null) {
					@SuppressWarnings("static-access")
					Individual synsetIndiv = super.onto.getIndividual(eachindirectHolonymPartOfRscUri);
					if (synsetIndiv != null) {
						indirectHolonymPartOfSynsetUris.add(synsetIndiv.getURI());
					}
				}
			}
		}
		return indirectHolonymPartOfSynsetUris;
	}

	private ArrayList<String> listIndirectHolonymPortionOfSynset() {
		ArrayList<String> indirectHolonymPortionOfSynsetUris = new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset = super.onto.getIndividual(super.getUri());
		if (synset == null) {
			return indirectHolonymPortionOfSynsetUris;
		} // TODO
		@SuppressWarnings("static-access")
		OntProperty indirectHolonymPortionOfProperty = super.onto.getOntProperty(super.ns + "indirectHolonymPortionOf");
		Iterator<RDFNode> indirectHolonymPortionOf = synset.listPropertyValues(indirectHolonymPortionOfProperty);
		while (indirectHolonymPortionOf.hasNext()) {
			RDFNode eachindirectHolonymPortionOf = indirectHolonymPortionOf.next();
			Resource indirectHolonymPortionOfRsc = eachindirectHolonymPortionOf.asResource();
			if (indirectHolonymPortionOfRsc != null) {
				String eachindirectHolonymPortionOfRscUri = indirectHolonymPortionOfRsc.getURI();
				if (eachindirectHolonymPortionOfRscUri != null) {
					@SuppressWarnings("static-access")
					Individual synsetIndiv = super.onto.getIndividual(eachindirectHolonymPortionOfRscUri);
					if (synsetIndiv != null) {
						indirectHolonymPortionOfSynsetUris.add(synsetIndiv.getURI());
					}
				}
			}
		}
		return indirectHolonymPortionOfSynsetUris;
	}

	private ArrayList<String> listIndirectHolonymMemberOfSynset() {
		ArrayList<String> indirectHolonymMemberOfSynsetUris = new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset = super.onto.getIndividual(super.getUri());
		if (synset == null) {
			return indirectHolonymMemberOfSynsetUris;
		}
		// TODO
		@SuppressWarnings("static-access")
		OntProperty indirectHolonymMemberOfProperty = super.onto.getOntProperty(super.ns + "indirectHolonymMemberOf");
		Iterator<RDFNode> indirectHolonymMemberOf = synset.listPropertyValues(indirectHolonymMemberOfProperty);
		while (indirectHolonymMemberOf.hasNext()) {
			RDFNode eachindirectHolonymMemberOf = indirectHolonymMemberOf.next();
			if (eachindirectHolonymMemberOf == null) {
				continue;
			}
			Resource indirectHolonymMemberOfRsc = eachindirectHolonymMemberOf.asResource();
			if (indirectHolonymMemberOfRsc != null) {
				String eachindirectHolonymMemberOfRscUri = indirectHolonymMemberOfRsc.getURI();
				if (eachindirectHolonymMemberOfRscUri != null) {
					@SuppressWarnings("static-access")
					Individual synsetIndiv = super.onto.getIndividual(eachindirectHolonymMemberOfRscUri);
					if (synsetIndiv != null) {
						indirectHolonymMemberOfSynsetUris.add(synsetIndiv.getURI());
					}
				}
			}
		}
		return indirectHolonymMemberOfSynsetUris;
	}

	private ArrayList<String> listMemberOfSynset() {
		ArrayList<String> memberOfSynsetUris = new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset = super.onto.getIndividual(super.getUri());
		if (synset == null) {
			return memberOfSynsetUris;
		}
		@SuppressWarnings("static-access")
		OntProperty memberOf = super.onto.getOntProperty(super.ns + "MeronymMemberOf");
		Iterator<RDFNode> memberOfSynset = synset.listPropertyValues(memberOf);
		while (memberOfSynset.hasNext()) {
			Resource memberOfSynsetRsc = memberOfSynset.next().asResource();
			if (memberOfSynsetRsc == null) {
				continue;
			}
			String memberOfSynUri = memberOfSynsetRsc.getURI();
			if (memberOfSynUri == null) {
				continue;
			}
			@SuppressWarnings("static-access")
			Individual synsetIndiv = super.onto.getIndividual(memberOfSynUri);
			if (synsetIndiv == null) {
				continue;
			}
			memberOfSynsetUris.add(synsetIndiv.getURI());
		}
		return memberOfSynsetUris;
	}

	private ArrayList<String> listHypernymSynset() {
		ArrayList<String> hypernymSynsetUris = new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset = super.onto.getIndividual(super.getUri());
		if (synset == null) {
			return hypernymSynsetUris;
		}
		Iterator<RDFNode> hypernymSynset = synset.listPropertyValues(hypernym);
		if (hypernymSynset == null) {
			return hypernymSynsetUris;
		}
		while (hypernymSynset.hasNext()) {
			Resource synsetRsc = hypernymSynset.next().asResource();
			if (synsetRsc == null) {
				continue;
			}
			String synsetIndivUri = synsetRsc.getURI();
			if (synsetIndivUri == null) {
				continue;
			}
			@SuppressWarnings("static-access")
			Individual synsetIndiv = super.onto.getIndividual(synsetIndivUri);
			if (synsetIndiv == null) {
				continue;
			}

			hypernymSynsetUris.add(synsetIndiv.getURI());
		}
		return hypernymSynsetUris;
	}

	private ArrayList<String> listHyponymSynset() {
		ArrayList<String> hyponymSynsetUris = new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset = super.onto.getIndividual(super.getUri());
		if (synset == null) {
			return hyponymSynsetUris;
		}
		@SuppressWarnings("static-access")
		OntProperty hyponym = super.onto.getOntProperty(super.ns + "Hyponym");
		Iterator<RDFNode> hyponymSynset = synset.listPropertyValues(hyponym);
		if (hyponymSynset == null) {
			return hyponymSynsetUris;
		}
		while (hyponymSynset.hasNext()) {
			Resource hyponymSynsetRsc = hyponymSynset.next().asResource();
			if (hyponymSynsetRsc == null) {
				continue;
			}
			String hyponymSynsetRscUri = hyponymSynsetRsc.getURI();
			if (hyponymSynsetRscUri == null) {
				continue;
			}
			@SuppressWarnings("static-access")
			Individual synsetIndiv = super.onto.getIndividual(hyponymSynsetRscUri);
			if (synsetIndiv == null) {
				continue;
			}
			hyponymSynsetUris.add(synsetIndiv.getURI());
		}
		return hyponymSynsetUris;
	}

	private ArrayList<String> listMeronymPartOf() {
		ArrayList<String> partOfSynsetUris = new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset = super.onto.getIndividual(super.getUri());
		if (synset == null) {
			return partOfSynsetUris;
		}
		@SuppressWarnings("static-access")
		OntProperty partOf = super.onto.getOntProperty(super.ns + "MeronymPartOf");
		Iterator<RDFNode> partOfSynset = synset.listPropertyValues(partOf);
		if (partOfSynset == null) {
			return partOfSynsetUris;
		}
		while (partOfSynset.hasNext()) {
			Resource eachPartOfSynsetRsc = partOfSynset.next().asResource();
			if (eachPartOfSynsetRsc == null) {
				continue;
			}
			String eachPartOfSynsetRscUri = eachPartOfSynsetRsc.getURI();
			if (eachPartOfSynsetRscUri == null) {
				continue;
			}

			@SuppressWarnings("static-access")
			Individual synsetIndiv = super.onto.getIndividual(eachPartOfSynsetRscUri);
			if (synsetIndiv == null) {
				continue;
			}
			partOfSynsetUris.add(synsetIndiv.getURI());
		}
		return partOfSynsetUris;
	}

	private ArrayList<String> listMeronymPortionOf() {
		ArrayList<String> portionOfSynsetUris = new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset = super.onto.getIndividual(super.getUri());
		if (synset == null) {
			return portionOfSynsetUris;
		}
		@SuppressWarnings("static-access")
		OntProperty portionOf = super.onto.getOntProperty(super.ns + "MeronymPortionOf");
		Iterator<RDFNode> portionOfSynset = synset.listPropertyValues(portionOf);
		if (portionOfSynset == null) {
			return portionOfSynsetUris;
		}
		while (portionOfSynset.hasNext()) {
			RDFNode eachPortionOfSynset = portionOfSynset.next();
			Resource eachPortionOfSynsetRsc = eachPortionOfSynset.asResource();
			if (eachPortionOfSynsetRsc == null) {
				continue;
			}
			String eachPortionOfRscSynsetUri = eachPortionOfSynsetRsc.getURI();
			if (eachPortionOfRscSynsetUri == null) {
				continue;
			}
			@SuppressWarnings("static-access")
			Individual synsetIndiv = super.onto.getIndividual(eachPortionOfRscSynsetUri);
			if (synsetIndiv == null) {
				continue;
			}
			portionOfSynsetUris.add(synsetIndiv.getURI());
		}
		return portionOfSynsetUris;
	}

	private ArrayList<String> listSuperTypeOfSynset() {
		ArrayList<String> typeOfSynsetUris = new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset = super.onto.getIndividual(super.getUri());
		if (synset == null) {
			return typeOfSynsetUris;
		} // TODO
		@SuppressWarnings("static-access")
		OntProperty typeOf = super.onto.getOntProperty("http://www.mitrc.ir/farsnet#R8dCrQCzyOLxW9Br2VZH1gO");
		Iterator<RDFNode> typeOfSynset = synset.listPropertyValues(typeOf);
		if (typeOfSynset == null) {
			return typeOfSynsetUris;
		}
		while (typeOfSynset.hasNext()) {
			typeOfSynsetUris.add(typeOfSynset.next().asResource().getURI());
		}

		return typeOfSynsetUris;
	}

	private ArrayList<String> listIndirectHypernymSynset() {
		ArrayList<String> indirectHypernymSynsetUris = new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset = super.onto.getIndividual(super.getUri());
		if (synset == null) {
			return indirectHypernymSynsetUris;
		}
		// TODO
		@SuppressWarnings("static-access")
		ObjectProperty indirectHypernym = super.onto.getObjectProperty("http://www.mitrc.ir/farsnet#indirectHypernym");
		Iterator<RDFNode> indirectHypernymSynset = synset.listPropertyValues(indirectHypernym);
		if (indirectHypernymSynset == null) {
			return indirectHypernymSynsetUris;
		}
		while (indirectHypernymSynset.hasNext()) {
			RDFNode eachIndirectHypernym = indirectHypernymSynset.next();
			Resource eachIndirectHypernymRsc = eachIndirectHypernym.asResource();
			if (eachIndirectHypernymRsc == null) {
				continue;
			}
			String eachIndirectHypernymUri = eachIndirectHypernymRsc.getURI();
			if (eachIndirectHypernymUri == null) {
				continue;
			}
			indirectHypernymSynsetUris.add(eachIndirectHypernymUri);
		}

		return indirectHypernymSynsetUris;
	}

	private ArrayList<String> listIndirectHyponymSynset() {
		ArrayList<String> indirectHyponymSynsetUris = new ArrayList<String>();
		@SuppressWarnings("static-access")
		Individual synset = super.onto.getIndividual(super.getUri());
		if (synset == null) {
			return indirectHyponymSynsetUris;
		} // TODO
		@SuppressWarnings("static-access")
		ObjectProperty indirectHyponym = super.onto.getObjectProperty("http://www.mitrc.ir/farsnet#indirectHyponym");
		Iterator<RDFNode> indirectHyponymSynset = synset.listPropertyValues(indirectHyponym);
		if (indirectHyponymSynset == null) {
			return indirectHyponymSynsetUris;
		}
		while (indirectHyponymSynset.hasNext()) {
			RDFNode eachIndirectHyponym = indirectHyponymSynset.next();
			Resource eachIndirectHyponymRsc = eachIndirectHyponym.asResource();
			if (eachIndirectHyponymRsc == null) {
				continue;
			}
			String eachIndirectHyponymUri = eachIndirectHyponymRsc.getURI();
			if (eachIndirectHyponymUri == null) {
				continue;
			}
			indirectHyponymSynsetUris.add(eachIndirectHyponymUri);
		}

		return indirectHyponymSynsetUris;
	}

	public ArrayList<String> getRelatedSynset(IRelationType relationType) {
		if (!(relationType instanceof NounRelationType)) {
			// TODO System.err.println("WRONG RELATION TYPE");
			return null;
		}

		switch ((NounRelationType) relationType) {
		case Hypernym:
			return this.listHypernymSynset();
		case Hyponym:
			return this.listHyponymSynset();
		case MemberOf:
			return this.listMemberOfSynset();
		case HasMember:
			return this.listHasMemberOfSynset();
		case PartOf:
			return this.listMeronymPartOf();
		case HasPart:
			return this.listHasPartOfSynset();
		case PorionOf:
			return this.listMeronymPortionOf();
		case HasPortion:
			return this.listHasPortionOfSynset();
		case instanceOf:
			return this.listSuperTypeOfSynset();
		case HasInstance:
			return this.listInstancesOfSynset();
		case indirectMeronymPartOf:
			return this.listIndirectMeronymPartOfSynset();
		case indirectMeronymPortionOf:
			return this.listIndirectMeronymPortionOf();
		case indirectMeronymMemberOf:
			return this.listIndirectMeronymMemberOfSynset();
		case indirectHolonymPartOf:
			return this.listIndirectHolonymPartOfSynset();
		case indirectHolonymPortionOf:
			return this.listIndirectHolonymPortionOfSynset();
		case indirectHolonymMemberOf:
			return this.listIndirectHolonymMemberOfSynset();
		case indirectHypernym:
			return this.listIndirectHypernymSynset();
		case indirectHyponym:
			return this.listIndirectHyponymSynset();
		case HypernymOrInstanceOf:
			ArrayList<String> retArray = new ArrayList<String>();
			retArray.addAll(this.listHypernymSynset());
			retArray.addAll(this.listInstancesOfSynset());
			return retArray;
		case RelatedVerb:
			@SuppressWarnings("static-access")
			Individual nounSyn = super.onto.getIndividual(this.getUri());
			NodeIterator relatedVerbs = nounSyn.listPropertyValues(relatedVerb);
			retArray = new ArrayList<>();
			while (relatedVerbs.hasNext()) {
				retArray.add(relatedVerbs.next().asResource().getURI());
			}
			return retArray;
		default:
			break;
		}

		return null;

	}
	/*
	 * public List<String> listIndirectHypernymsUri (){ ArrayList<String>
	 * hypernymSynsetUris=new ArrayList<String>();
	 * 
	 * @SuppressWarnings("static-access") Individual
	 * synset=super.onto.getIndividual(super.getUri()); if (synset==null){
	 * return null; } Iterator<RDFNode>
	 * hypernymSynset=synset.listPropertyValues(hypernym); if
	 * (hypernymSynset==null){ return null; } while (hypernymSynset.hasNext()){
	 * Resource synsetRsc=hypernymSynset.next().asResource(); if
	 * (synsetRsc==null){ continue; } String synsetIndivUri=synsetRsc.getURI();
	 * if (synsetIndivUri==null){ continue; }
	 * 
	 * @SuppressWarnings("static-access") Individual
	 * synsetIndiv=super.onto.getIndividual(synsetIndivUri); if
	 * (synsetIndiv==null){ continue; }
	 * 
	 * 
	 * hypernymSynsetUris.add(synsetIndiv.getURI()); } return
	 * hypernymSynsetUris; }
	 */

}
