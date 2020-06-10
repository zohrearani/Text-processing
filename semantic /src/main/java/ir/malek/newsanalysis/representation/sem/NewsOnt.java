package ir.malek.newsanalysis.representation.sem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.hp.hpl.jena.ontology.OntModel;
import org.apache.jena.rdf.model.impl.StatementImpl;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.vocabulary.RDFS;

import ir.malek.newsanalysis.ned.WikidataNED;
import ir.malek.newsanalysis.preprocess.Preprocess;
import ir.malek.newsanalysis.preprocess.Token;
import ir.malek.newsanalysis.semantic.srl.SRLRelation;
import ir.malek.newsanalysis.semantic.srl.SRLRelation.Argument;
import ir.malek.newsanalysis.util.enums.RoleLabel;
import ir.malek.newsanalysis.util.enums.SemanticCategory;
import ir.malek.newsanalysis.util.enums.TimeLabel;
import ir.mitrc.corpus.api.ApiFactory;
import ir.mitrc.corpus.api.Isynset;
import ir.mitrc.corpus.api.SynsetPos;

public class NewsOnt extends NewsCore {

	int counter = 0;
	String newsId;
	HashMap<String, String> mention2uri;
	HashMap<String, String> uri2typeUri;
	HashMap<RoleLabel, ObjectProperty> role2property;
	WikidataNED ned;
	public OntModel newsOnt;

	Preprocess preprocess;
	ApiFactory faWnApi;
	List<StatementImpl> stats;
	
	public NewsOnt(Preprocess preprocess, ApiFactory faWnApi) {
		super(faWnApi);
		super.make();
		// onto = super.getOnto();
		newsOnt = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		ned = new WikidataNED();
		mention2uri = new HashMap<String, String>();
		uri2typeUri = new HashMap<String, String>();
		role2property = new HashMap<>();
		for (RoleLabel role : RoleLabel.values()) {
			ObjectProperty hasRole = onto.getObjectProperty(ns2 + "has" + role.toString());
			if (hasRole == null) {
				hasRole = onto.getObjectProperty(ns + "has" + role.toString());
			}
			role2property.put(role, hasRole);
		}

		hasPolarity = onto.getDatatypeProperty(ns2 + "hasPolarity");
		hasModality = onto.getDatatypeProperty(ns2 + "hasModality");
		hasProvenance = onto.getDatatypeProperty(ns2 + "hasProvenance");
		hasEventType = onto.getObjectProperty(ns + "eventType");
		hasActorType = onto.getObjectProperty(ns + "actorType");
		hasPlaceType = onto.getObjectProperty(ns + "placeType");
		hasTimeType = onto.getObjectProperty(ns + "timeType");

		eventClass = onto.getOntClass(ns + "Event");
		actorClass = onto.getOntClass(ns + "Actor");
		timeClass = onto.getOntClass(ns + "Time");
		PlaceClass = onto.getOntClass(ns + "Place");

		this.preprocess = preprocess;
		this.faWnApi = faWnApi;
		stats = new ArrayList<StatementImpl>();
	}

	public void writeFacts(List<List<Token>> docTokens, List<List<SRLRelation>> allRelations, String newsId) {
		int senNo = 0;
		this.newsId = newsId;
		for (List<Token> senTokens : docTokens) {
			List<SRLRelation> relations = allRelations.get(senNo++);
			for (SRLRelation rel : relations) {
				Argument arg1 = rel.getArg1();
				Argument arg2 = rel.getArg2();
				setDefinedUri(arg1, senTokens);
				setDefinedUri(arg2, senTokens);
				ObjectProperty roleProperty = role2property.get(rel.getRole());
				switch (rel.getRole()) {
				case Agent:
				case Patient:
					// relation beteween event and object:
					if (rel.getArg1().getUri() == null) {
						setEventTypeUri(arg1, senTokens);
					}
					if (rel.getArg2().getUri() == null) {
						setAnimateTypeUri(arg2, senTokens);
					}
					Individual event = newsOnt.createIndividual(arg1.getUri(), eventClass);
					Individual eventType = onto.getIndividual(uri2typeUri.get(arg1.getUri()));
					if (eventType == null) {
						eventType = onto.getIndividual(defaultEventTypeUri);
					}
					event.addProperty(hasEventType, eventType);
					System.out.println(arg2.getUri());
					System.out.println(arg1.getUri());
					Individual agent = newsOnt.createIndividual(arg2.getUri(), actorClass);
					Individual agentType = onto.getIndividual(uri2typeUri.get(arg2.getUri()));
					if (agentType == null) {
						agentType = onto.getIndividual(defaultAnimateTypeUri);
					}
					agent.addProperty(hasActorType, agentType);
					agent.addProperty(RDFS.label, arg2.getLemma());
					event.addProperty(roleProperty, agent);
					event.addProperty(RDFS.label, arg1.getLemma());
					newsOnt.addLiteral(event, hasPolarity, rel.getPolarity());
					break;
				case Theme:// relation beteween event and object OR event and
							// event *******************:
				case Instrument:// relation beteween event and object OR event
								// and event *******************:
				case Cause:// relation between 2 event:

					if (rel.getArg1().getUri() == null) {
						setEventTypeUri(rel.getArg1(), senTokens);
					}
					if (rel.getArg2().getUri() == null) {
						setObjectTypeUri(rel.getArg2(), senTokens);
					}
					event = newsOnt.createIndividual(rel.getArg1().getUri(), eventClass);
					eventType = onto.getIndividual(uri2typeUri.get(arg1.getUri()));
					if (eventType == null) {
						eventType = onto.getIndividual(defaultEventTypeUri);
					}
					event.addProperty(hasEventType, eventType);
					Individual object = newsOnt.createIndividual(rel.getArg2().getUri(), actorClass);
					Individual objectType = onto.getIndividual(uri2typeUri.get(arg2.getUri()));
					if (objectType == null) {
						objectType = onto.getIndividual(defaultObjectTypeUri);
					}
					object.addProperty(hasActorType, objectType);
					object.addProperty(RDFS.label, rel.getArg2().getLemma());
					event.addProperty(roleProperty, object);
					event.addProperty(RDFS.label, rel.getArg1().getLemma());
					onto.addLiteral(event, hasPolarity, rel.getPolarity());
					break;
				case Measure:
					// TODO;
					break;
				case Place:
				case StartPlace:
				case EndPlace:
					if (rel.getArg1().getUri() == null) {
						setEventTypeUri(rel.getArg1(), senTokens);
					}
					if (rel.getArg2().getUri() == null) {
						setPlaceTypeUri(rel.getArg2(), senTokens);
					}
					event = newsOnt.createIndividual(rel.getArg1().getUri(), eventClass);
					eventType = onto.getIndividual(uri2typeUri.get(arg1.getUri()));
					if (eventType == null) {
						eventType = onto.getIndividual(defaultEventTypeUri);
					}
					event.addProperty(hasEventType, eventType);
					Individual Place = newsOnt.createIndividual(rel.getArg2().getUri(), PlaceClass);
					Individual PlaceType = onto.getIndividual(uri2typeUri.get(arg2.getUri()));
					if (PlaceType == null) {
						PlaceType = onto.getIndividual(defaultPlaceTypeUri);
					}
					Place.addProperty(hasPlaceType, PlaceType);
					Place.addProperty(RDFS.label, rel.getArg2().getLemma());
					event.addProperty(roleProperty, Place);
					event.addProperty(RDFS.label, rel.getArg1().getLemma());
					newsOnt.addLiteral(event, hasPolarity, rel.getPolarity());
					break;
				case Time:
				case StartTime:
				case EndTime:
					if (rel.getArg1().getUri() == null) {
						setEventTypeUri(rel.getArg1(), senTokens);
					}
					if (rel.getArg2().getUri() == null) {
						setTimeTypeUri(rel.getArg2(), senTokens);
					}
					event = newsOnt.createIndividual(rel.getArg1().getUri(), eventClass);
					eventType = onto.getIndividual(uri2typeUri.get(arg1.getUri()));
					if (eventType == null) {
						eventType = onto.getIndividual(defaultEventTypeUri);
					}
					event.addProperty(hasEventType, eventType);
					Individual time = newsOnt.createIndividual(rel.getArg2().getUri(), timeClass);
					Individual timeType = onto.getIndividual(uri2typeUri.get(arg2.getUri()));
					if (timeType == null) {
						timeType = onto.getIndividual(defaultTimeTypeUri);
					}
					time.addProperty(hasActorType, timeType);
					time.addProperty(RDFS.label, rel.getArg2().getLemma());
					event.addProperty(roleProperty, time);
					time.addProperty(RDFS.label, rel.getArg1().getLemma());
					newsOnt.addLiteral(event, hasPolarity, rel.getPolarity());
					break;
				case Description:
					// TODO
					break;
				case Has:// relation between two object
					// TODO
					break;
				case O:
					break;
				default:
					break;
				}
			}
		}
	}

	/*
	 * set argUri and return typeUri
	 */
	private void setDefinedUri(Argument arg, List<Token> senTokens) {

		if (mention2uri.get(arg.getLemma()) != null) {
			arg.setUri(mention2uri.get(arg.getLemma()));
			return;
		}
		Token token = senTokens.get(arg.firstIndex());

//		if (token.getTimeLabel().equals(TimeLabel.B_Time)) {
//			String timeValue = token.getNormalizedTime().split("_")[1];
//			String timeType = token.getNormalizedTime().split("_")[0].toLowerCase();
//			String uri = timeNs + timeValue;
//			arg.setUri(uri);
//			mention2uri.put(arg.getLemma(), arg.getUri());
//			uri2typeUri.put(arg.getUri(), timeNs + timeType);
//		}
		String uri = nsWikidata + token.getNED();
		if (uri != null && !uri.equals(nsWikidata + "O") && uri != "") {
			arg.setUri(uri);
			String type = "";
			if (token.getNer().toString().startsWith("B_")) {
				type = token.getNESubType().toLowerCase();
				if (type == null) {
					type = token.getNer().toString().split("_")[1].toLowerCase();
				} // TODO
			}
			mention2uri.put(arg.getLemma(), arg.getUri());
			uri2typeUri.put(arg.getUri(), entityNs + type);
		}
	}

	/*
	 * set eventUri in argument and return eventTypeUri :)
	 */
	private void setEventTypeUri(Argument arg, List<Token> senTokens) {
		if (arg.getIndexSet().size() > 1) {
			String lemma = arg.getLemma();
			List<String> synsets = faWnApi.listSynsetsByWordSurface(lemma);
			if (synsets != null && synsets.size() > 0) {
				Isynset synset = ApiFactory.getSynset(synsets.get(0));
				SynsetPos pos = synset.getPos();
				if (pos == SynsetPos.Verb) {
					String uri = synsets.get(0) + "_" + newsId + "-" + counter++;
					arg.setUri(uri);
					mention2uri.put(arg.getLemma(), uri);
					String EventTypeUri = findEventType(synsets.get(0));
					if (EventTypeUri == null) {
						EventTypeUri = defaultEventTypeUri;
					}
					uri2typeUri.put(uri, EventTypeUri);
					return;
				}
			}
		}
		Token a = senTokens.get(arg.firstIndex());
		if (a.getSense() != null && a.getSense() != "") {
			String uri = a.getSense() + "_" + newsId + "-" + counter++;
			arg.setUri(uri);
			mention2uri.put(arg.getLemma(), uri);
			String EventTypeUri = findEventType(a.getSense());
			if (EventTypeUri == null) {
				EventTypeUri = defaultAnimateTypeUri;
			}
			uri2typeUri.put(uri, EventTypeUri);
			return;
		}
		String uri = defaultEventTypeUri + "_" + newsId + "-" + counter++;
		arg.setUri(uri);
		mention2uri.put(arg.getLemma(), uri);
		uri2typeUri.put(uri, defaultEventTypeUri);
	}

	/*
	 * set objectUri in argument and return objectTypeUri :)
	 */
	private void setAnimateTypeUri(Argument arg, List<Token> senTokens) {
		if (arg.getIndexSet().size() > 1) {
			String lemma = arg.getLemma();
			List<String> synsets = faWnApi.listSynsetsByWordSurface(lemma);

			if (synsets != null && synsets.size() > 0) {
				Isynset synset = ApiFactory.getSynset(synsets.get(0));
				SynsetPos pos = synset.getPos();
				if (pos == SynsetPos.Noun) {
					String uri = synsets.get(0) + "_" + newsId + "-" + counter++;
					arg.setUri(uri);
					mention2uri.put(arg.getLemma(), uri);
					uri2typeUri.put(uri, synsets.get(0));
					return;
				}
			}
		}
		Token a = senTokens.get(arg.firstIndex());
		if (a.getSense() != null && a.getSense() != "") {
			String uri = a.getSense() + "_" + newsId + "-" + counter++;
			arg.setUri(uri);
			mention2uri.put(arg.getLemma(), uri);
			uri2typeUri.put(uri, a.getSense());
			return;
		}
		String uri = defaultAnimateTypeUri + "_" + newsId + "-" + counter++;
		arg.setUri(uri);
		mention2uri.put(arg.getLemma(), uri);
		uri2typeUri.put(uri, defaultAnimateTypeUri);
	}

	private void setObjectTypeUri(Argument arg, List<Token> senTokens) {
		// may be an object is itself an event
		if (arg.getIndexSet().size() > 1) {
			String lemma = arg.getLemma();
			List<String> synsets = faWnApi.listSynsetsByWordSurface(lemma);
			if (synsets != null && synsets.size() > 0) {
				Isynset synset = ApiFactory.getSynset(synsets.get(0));
				SynsetPos pos = synset.getPos();
				if (pos == SynsetPos.Noun) {
					String uri = synsets.get(0) + "_" + newsId + "-" + counter++;
					arg.setUri(uri);
					mention2uri.put(arg.getLemma(), uri);
					uri2typeUri.put(uri, synsets.get(0));
					return;
				} else if (pos == SynsetPos.Verb) {
					setEventTypeUri(arg, senTokens);
				}
			}
		}
		Token a = senTokens.get(arg.firstIndex());
		if (a.getSemanticCategory().equals(SemanticCategory.ACT.toString())) {
			setEventTypeUri(arg, senTokens);
			return;
		}
		if (a.getSense() != null && a.getSense() != "") {
			String uri = a.getSense() + "_" + newsId + "-" + counter++;
			arg.setUri(uri);
			mention2uri.put(arg.getLemma(), uri);
			uri2typeUri.put(uri, a.getSense());
			return;
		}
		String uri = defaultObjectTypeUri + "_" + newsId + "-" + counter++;
		arg.setUri(uri);
		mention2uri.put(arg.getLemma(), uri);
		uri2typeUri.put(uri, defaultObjectTypeUri);
	}

	private void setTimeTypeUri(Argument arg, List<Token> senTokens) {
		String uri = defaultTimeTypeUri + "_" + newsId + "-" + counter++;
		arg.setUri(uri);
		mention2uri.put(arg.getLemma(), uri);
		uri2typeUri.put(uri, defaultTimeTypeUri);
	}

	private void setPlaceTypeUri(Argument arg, List<Token> senTokens) {
		Token a = senTokens.get(arg.firstIndex());
		List<String> synsets = faWnApi.listSynsetsByWordSurface(a.lemma());
		if (synsets != null && synsets.size() > 0) {
			Isynset synset = ApiFactory.getSynset(synsets.get(0));
			SynsetPos pos = synset.getPos();
			if (pos == SynsetPos.Noun) {
				String uri = synsets.get(0) + "_" + newsId + "-" + counter++;
				arg.setUri(uri);
				mention2uri.put(arg.getLemma(), uri);
				uri2typeUri.put(uri, synsets.get(0));
				return;
			}
		}
		String uri = defaultPlaceTypeUri + "_" + newsId + "-" + counter++;
		arg.setUri(uri);
		mention2uri.put(arg.getLemma(), uri);
		uri2typeUri.put(uri, defaultPlaceTypeUri);
	}
}
