package ir.malek.newsanalysis.representation.sem;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.BadURIException;

import ir.malek.newsanalysis.semantic.classification.NounClassifier;
import ir.malek.newsanalysis.util.enums.RoleLabel;
import ir.malek.newsanalysis.util.enums.TimeType;
import ir.malek.newsanalysis.util.io.OutFile;
import ir.mitrc.corpus.api.ApiFactory;
import ir.mitrc.corpus.api.Isynset;
import ir.mitrc.corpus.api.NounRelationType;
import ir.mitrc.corpus.api.SynsetPos;

public class NewsCore {
	OntModel onto;
	String path = "semantic//src//main//resources//news//";
	String ns = "http://semanticweb.cs.vu.nl/2009/11/sem/";
	String ns2 = "http://semanticweb.cs.vu.nl/2009/11/sem2/";
	String nsWikidata = "http://www.wikidata.org/entity/";

	String timeNs = ns2 + "time#";
	String entityNs = ns2 + "entity#";
	String animateNs = ns2 + "animate#";

	String defaultAnimateTypeUri = ns2 + "defaultAnimateType";
	String defaultObjectTypeUri = ns2 + "defaultEntityType";
	String defaultEventTypeUri = ns2 + "defaultEventType";
	String defaultPlaceTypeUri = ns2 + "defaultPlaceType";
	String defaultTimeTypeUri = ns2 + "defaultTimeType";

	ApiFactory faWnApi;

	DatatypeProperty hasPolarity;
	DatatypeProperty hasModality;
	DatatypeProperty hasProvenance;

	ObjectProperty hasEventType;
	ObjectProperty hasActorType;
	ObjectProperty hasPlaceType;
	ObjectProperty hasTimeType;

	OntClass eventClass;
	OntClass actorClass;
	OntClass timeClass;
	OntClass PlaceClass;

	public NewsCore(ApiFactory faWnApi) {
		this.faWnApi = faWnApi;
	}

	public NewsCore() {
		faWnApi = new ApiFactory();
	}

	private void ReadSem(String path) {
		try {
			onto = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			InputStream owlPath = new FileInputStream(path);
			onto.read(owlPath, null);
		} catch (FileNotFoundException e) {
			System.err.println("An error has occured in ontology loading");
			e.printStackTrace();
		}

	}

	/**
	 * populate EventType in sem.rdf Adding VerbSynset and actionNounSynset from
	 * Persian-Wordnet to Sem
	 */
	private void addActions() {

		NounClassifier classifier = new NounClassifier(faWnApi);
		List<String> verbs = faWnApi.listAllVerbSynsets();
		List<String> nouns = faWnApi.listAllNounSynsets();
		List<String> actNouns = new ArrayList<>();
		for (String str : nouns) {
			if (classifier.catWithSense(str) == null)
				System.out.println("NULL:" + str);
			if (classifier.catWithSense(str).equals("ACT")) {
				actNouns.add(str);
			}
		}
		List<String> allActions = new ArrayList<>();
		allActions.addAll(verbs);
		allActions.addAll(actNouns);
		OntClass eventTypeClass = onto.getOntClass(ns + "EventType");
		OutFile outFile = new OutFile("actSynset.txt");
		for (String event : allActions) {
			Individual eventIndiv = onto.createIndividual(event, eventTypeClass);
			String label = faWnApi.getLabel(event);
			eventIndiv.setLabel(label, "FA");
			outFile.println(label + "\t" + event); // TODO: comment this line.
		}

		String defaultEventTypeUri = ns2 + "defaultEventType";
		onto.createIndividual(defaultEventTypeUri, eventTypeClass);
	}

	private void addTimeTypes() {
		OntClass timeTypeClass = onto.getOntClass(ns + "TimeType");
		for (TimeType type : TimeType.values()) {
			onto.createIndividual(ns2 + "time#" + type.toString(), timeTypeClass);
		}
		String defaultTimeTypeUri = ns2 + "defaultTimeType";
		onto.createIndividual(defaultTimeTypeUri, timeTypeClass);
	}

	private void addPlaceType() {
		OntClass placeTypeClass = onto.getOntClass(ns + "PlaceType");
		/*
		 * for (PlaceType type:PlaceType.values()){
		 * onto.createIndividual(ns2+type.toString(),PlaceTypeClass); }
		 */
		String defaultPlaceTypeUri = ns2 + "defaultPlaceType";
		onto.createIndividual(defaultPlaceTypeUri, placeTypeClass);
	}

	private void addActorType() {
		String defaultAnimateTypeUri = ns2 + "defaultAnimateType";
		String defaultObjectTypeUri = ns2 + "defaultEntityType";
		String person = ns2 + "personActorType";
		String organization = ns2 + "organizationActorType";
		OntClass actorTypeClass = onto.getOntClass(ns + "ActorType");
		onto.createIndividual(defaultAnimateTypeUri, actorTypeClass);
		onto.createIndividual(defaultObjectTypeUri, actorTypeClass);
		onto.createIndividual(person, actorTypeClass);
		onto.createIndividual(organization, actorTypeClass);
	}

	private void addRoleProperty() {

		ObjectProperty hasActor = onto.getObjectProperty(ns + "hasActor");
		ObjectProperty hasAgent = onto.createObjectProperty(ns2 + "hasAgent");
		hasAgent.setSuperProperty(hasActor);
		ObjectProperty hasPatient = onto.createObjectProperty(ns2 + "hasPatient");
		hasPatient.setSuperProperty(hasActor);
		ObjectProperty hasInstrument = onto.createObjectProperty(ns2 + "hasInstrument");
		hasInstrument.setSuperProperty(hasActor);
		ObjectProperty hasTheme = onto.createObjectProperty(ns2 + "hasTheme");
		hasTheme.setSuperProperty(hasActor);

		ObjectProperty hasCause = onto.createObjectProperty(ns2 + "hasCause");
		hasCause.setLabel("hasCause", "EN");

		ObjectProperty hasDescription = onto.createObjectProperty(ns2 + "hasDescription");
		hasDescription.setLabel("hasDescription", "EN");

		ObjectProperty hasHas = onto.createObjectProperty(ns2 + "hasHas");
		hasHas.setLabel("hasHas", "EN");

		ObjectProperty hasTime = onto.getObjectProperty(ns + "hasTime");
		ObjectProperty hasStartTime = onto.createObjectProperty(ns2 + "hasStartTime");
		hasStartTime.setSuperProperty(hasTime);
		ObjectProperty hasEndTime = onto.createObjectProperty(ns2 + "hasEndTime");
		hasEndTime.setSuperProperty(hasTime);

		ObjectProperty hasPlace = onto.getObjectProperty(ns + "hasPlace");
		ObjectProperty hasStartPlace = onto.createObjectProperty(ns2 + "hasStartPlace");
		hasStartPlace.setSuperProperty(hasPlace);
		ObjectProperty hasEndPlace = onto.createObjectProperty(ns2 + "hasEndPlace");
		hasEndPlace.setSuperProperty(hasPlace);
		ObjectProperty hasMeasure = onto.createObjectProperty(ns2 + "hasMeasure");
		hasEndPlace.setSuperProperty(hasMeasure);

		for (RoleLabel role : RoleLabel.values()) {
			ObjectProperty hasRole = onto.getObjectProperty(ns + "has" + role.toString());
			if (hasRole == null) {
				hasRole = onto.createObjectProperty(ns2 + "has" + role.toString());
			}
		}

		hasPolarity = onto.createDatatypeProperty(ns2 + "hasPolarity");
		hasPolarity.setLabel("hasPolarity", "EN");
		hasModality = onto.createDatatypeProperty(ns2 + "hasModality");
		hasModality.setLabel("hasModality", "EN");
		hasProvenance = onto.createDatatypeProperty(ns2 + "hasProvenance");
		hasProvenance.setLabel("hasProvenance", "EN");
	}

	/**
	 * Read Sem.rdf and apply all changes on it:
	 */

	public void writeOnt(String pathName, OntModel onto) {
		try {
			FileWriter outStream = new FileWriter(pathName);
			onto.write(outStream, "N-TRIPLE");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BadURIException e) {
			e.printStackTrace();
		}

	}

	public OntModel getOnto() {
		if (onto != null)
			return onto;
		else {
			make();
			return onto;
		}

	}

	public void make() {
		ReadSem(path + "sem.rdf");
		addActions();
		addTimeTypes();
		addPlaceType();
		addActorType();
		addRoleProperty();

	}

	public String findEventType(String uri) {
		OntClass eventTypeClass = onto.getOntClass(ns + "EventType");
		Isynset synset = ApiFactory.getSynset(uri);
		SynsetPos pos = synset.getPos();
		if (pos == SynsetPos.Noun) {
			String eventUri = findRelatedVerb(uri);
			if (eventUri != null)
				return eventUri;
		}
		Individual eventIndiv = onto.getIndividual(uri);
		int counter = 0;
		if (eventIndiv == null || !(eventIndiv.hasRDFType(eventTypeClass))) {
			Vector<String> fathers = new Vector<>();
			fathers.addAll(ApiFactory.getFather(uri));
			while (fathers.size() > 0 && counter < 20) {
				counter++;
				String synUri = fathers.get(0);
				eventIndiv = onto.getIndividual(synUri);
				if (eventIndiv != null && eventIndiv.hasRDFType(eventTypeClass)) {
					return eventIndiv.getURI();
				}
				fathers.addAll(ApiFactory.getFather(fathers.get(0)));
				fathers.remove(0);
			}
			return null;
		}
		return eventIndiv.getURI();
	}

	public String findRelatedVerb(String uri) {
		Isynset synset = ApiFactory.getSynset(uri);
		if (synset.getPos() == SynsetPos.Verb)
			return uri;
		if (synset.getPos() == SynsetPos.Noun) {
			ArrayList<String> relatedVerbs = new ArrayList<>();
			Vector<String> fathers = new Vector<>();
			fathers.add(uri);
			int counter = 0;
			while (fathers.size() > 0 && counter < 20) {
				counter++;
				synset = ApiFactory.getSynset(fathers.get(0));
				relatedVerbs = synset.getRelatedSynset(NounRelationType.RelatedVerb);
				if (relatedVerbs != null && relatedVerbs.size() > 0 && ApiFactory.getSynset(relatedVerbs.get(0)) != null)
					return relatedVerbs.get(0);
				fathers.addAll(ApiFactory.getFather(fathers.get(0)));
				fathers.remove(fathers.get(0));
			}
		}
		return null;
	}
}
