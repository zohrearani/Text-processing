package ir.mitrc.corpus.api;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;

import ir.malek.newsanalysis.preprocess.Preprocess;
import ir.malek.newsanalysis.preprocess.Token;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

public class ApiFactory  {
	public  static OntModel onto;
	protected static String ns = new String("http://www.mitrc.ir/mobina#");
	protected String wordUri=ns+"واژه";
	//protected String senseUri=ns+"";
	private static Map<String ,HashSet<String>> wordLabel2UrisHash=new HashMap<String, HashSet<String>>();
    private Preprocess preprocess;
   
   /* public ApiFactory(String ontPath,OntModelSpec spec){
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		onto=readOntology(ontPath,spec);//"resource/mobin_wordnet.owl");
		createWordsLabel2UriHash();
	}*/

    public ApiFactory(InputStream ontInputPath, Preprocess preprocessor) {
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		System.err.print("loading persian wordnet ... ");
		long startTime = System.currentTimeMillis();
		onto = readOntology(ontInputPath);
		long totalTime = System.currentTimeMillis() - startTime;
		System.err.println("done ["+totalTime/1000.0+" sec].");
        preprocess=preprocessor;
        createWordsLabel2UriHash();
    }
    public ApiFactory(InputStream ontInputStream) {
    	org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		System.err.print("loading persian wordnet ... ");
		long startTime = System.currentTimeMillis();
		onto = readOntology(ontInputStream);
		long totalTime = System.currentTimeMillis() - startTime;
		System.err.print("done ["+totalTime/1000.0+" sec].");
        preprocess=new Preprocess();
        createWordsLabel2UriHash();
    }
    public ApiFactory() {
    	org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
    	System.err.print("loading persian wordnet ... ");
    	long startTime = System.currentTimeMillis();
    	onto = readOntology(this.getClass().getClassLoader().getResourceAsStream("root-ontology.owl"));
    	long totalTime = System.currentTimeMillis() - startTime;
    	System.err.print("done ["+totalTime/1000.0+" sec].");
        preprocess=new Preprocess();
        createWordsLabel2UriHash();
    	}
    	/*
    	public ApiFactory(InputStream systemResourceAsStream) {
    		//org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
    	}
    	*/
	public List<String> listSynsetsByWordSurface(String vazhe){
		
		List<String> wordUriList=searchOntWord(vazhe);
		//List<String> sensesUriList=new ArrayList<String>();
		List<String> synsetsList=new ArrayList<String>();
		if (wordUriList==null)
			return null;
		for(String wordUri:wordUriList){
			synsetsList.addAll(listSynsetUriOfWordUri(wordUri));
		}
		return synsetsList;
		
	}
	public void writeOntology(String path,OntModel onto){
		try {
	  		  FileWriter file= new FileWriter(path);
	  		  onto.write(file, "RDF/XML");
	  		  file.close();
	  	} catch (IOException e) {
	  		// System.err.print("An error has occured in ontology writing");
	  	  e.printStackTrace();
	  	}		
	}
	/**
	 * return list of all word surfaces of mobina with the corresponding uris.
	 * @return Map<String, HashSet<String>> wordLabel2UrisHash
	 */
	public Map<String, HashSet<String>> getWordLables2Uris (){
	    	return wordLabel2UrisHash;
	}
	/**
     * **
     * this function returns a Sense Object.
     * @return sense
     */
    public static Isense getSense(String senseUri) {
        Sense sense = null;
        sense = new Sense(senseUri);
        return sense;
    }

	/***
	 * this function returns a Synset Object.
	 * @return a Synset.
	 */
	public static Isynset getSynset(String synsetUri){
		if (synsetUri==null){
			return null;
		}
		SynsetPos synsetPos=Synset.getSynsetPos(synsetUri);
		Synset synset=null;
		if (synsetPos==null){
		//	System.err.println("bad synsetURi pos is null :"+synsetUri);
			return null;
		}
		switch (synsetPos){
		case Noun:
			synset=new NounSynset(synsetUri);
			break;
		case Verb:
			synset=new VerbSynset(synsetUri);
			break;
		case Adverb:
			synset=new AdverbSynset(synsetUri);
			break;
		case Adjective:
			synset=new AdjectiveSynset(synsetUri);
			break;
		default:
		//	System.out.println("unkonwn pos synsetUri");
			return null;
			
		}
		return synset;
	}

	/*****
	 * this function returns an array of words that have surface word matched with entered String.entered String will 
	 * be assumed lemma.
	 * @return array of word URIs.
	 */
	public static ArrayList<String> getFather(String synsetUri){
		Isynset synset=getSynset(synsetUri);
		SynsetPos synsetPos=Synset.getSynsetPos(synsetUri);
		
		if (synsetPos==SynsetPos.Verb)
			return synset.getRelatedSynset(VerbRelationType.Hypernym);
		else if (synsetPos==SynsetPos.Noun)
			return synset.getRelatedSynset(NounRelationType.HypernymOrInstanceOf);
		return new ArrayList<>();
	}
	public static  ArrayList<String> searchOntWord(String vazhe){
		String label=vazhe;
		ArrayList<String> matchedWordUris = null;
		if (label==null){
			return matchedWordUris;
		} 
		if (wordLabel2UrisHash.containsKey(label)){
			
			matchedWordUris=new ArrayList<>();
			matchedWordUris.addAll(wordLabel2UrisHash.get(label));
			return matchedWordUris;
		}
		
		label=normalize(label);
		
		if (wordLabel2UrisHash.containsKey(label)){
			matchedWordUris=new ArrayList<>();
			matchedWordUris.addAll(wordLabel2UrisHash.get(label));
			return matchedWordUris;
		}	
		return matchedWordUris;
	}

    /*****
	 * this function returns an array of words that have surface word matched with entered String.if second parameter
	 * is true , entered String will be lemmatized and if second parameter is false , entered String will be assumed lemma.
	 * @return array of word URIs.
	 */
	public ArrayList<String> searchOntWord(String vazhe , boolean needsLemma) {
		ArrayList<String> matchedWords=new ArrayList<>();
		if (needsLemma){
			matchedWords=searchOntWordWithLematization(vazhe);
			return matchedWords;
		}
		else {
			matchedWords=searchOntWord(vazhe);
			return matchedWords;
			
		}
	}
	
	public ArrayList<String> listNounSysnset(String wordSurface){
			
			ArrayList<String> matchedWordUris=searchOntWord(wordSurface);
			ArrayList<Individual> allSensesOfWord=new ArrayList<Individual>();

			for(String wordUri:matchedWordUris){
				Individual vazhe=onto.getIndividual(wordUri);
				OntProperty meansOfWords=onto.getOntProperty(ns + "means");
				Iterator<RDFNode> sensesOfWord = vazhe.listPropertyValues(meansOfWords);		
				while (sensesOfWord.hasNext()){
					Individual tempIndividual=onto.getIndividual(sensesOfWord.next().asResource().getURI());
					allSensesOfWord.add(tempIndividual);
				}
			}
			ArrayList<String> arrayOfAdjSynsets=new ArrayList<String>();		
			OntProperty synsetOfSensesOfWord=onto.getOntProperty(ns+"inSynset");
			OntProperty infinitive=onto.getOntProperty(ns+"Infinitive");
			OntProperty hasSingle=onto.getOntProperty(ns+"Single");
			OntProperty hasRefrence=onto.getOntProperty(ns+"ReferTo");
			
			OntClass nounSynsetClass=onto.getOntClass(ns+"گروه_معنایی_اسم_ها");
			for(Individual senseOfWord:allSensesOfWord){
				
				Iterator<RDFNode> synsets=senseOfWord.listPropertyValues(synsetOfSensesOfWord);
				Iterator<RDFNode> infinitives=senseOfWord.listPropertyValues(infinitive);
				Iterator<RDFNode> hasSingles=senseOfWord.listPropertyValues(hasSingle);
				Iterator<RDFNode> hasReferences=senseOfWord.listPropertyValues(hasRefrence);
				while(synsets.hasNext()){
					Individual synsetIndiv=onto.getIndividual(synsets.next().asResource().getURI());
					if (synsetIndiv==null || synsetIndiv.getURI()==null){
						continue;
					}
					if (synsetIndiv.hasRDFType(nounSynsetClass))
						arrayOfAdjSynsets.add(synsetIndiv.getURI());
				}
				while (infinitives.hasNext()){
					Individual relatedObj=onto.getIndividual(infinitives.next().asResource().getURI());
					Isense relatedSense=new Sense(relatedObj.getURI());
					
					String synsetUri=relatedSense.getSynsetUri();
					Individual synsetIndiv=onto.getIndividual(synsetUri);
					if (synsetIndiv.hasRDFType(nounSynsetClass))
						arrayOfAdjSynsets.add(synsetUri);
				}
				while (hasSingles.hasNext()){
					Individual relatedObj=onto.getIndividual(hasSingles.next().asResource().getURI());
					Isense relatedSense=new Sense(relatedObj.getURI());
					if (relatedObj.getURI()==null){
						System.out.println(relatedObj);
						continue;
					}
					String synsetUri=relatedSense.getSynsetUri();
					Individual synsetIndiv=onto.getIndividual(synsetUri);
					if (synsetIndiv.hasRDFType(nounSynsetClass))
						arrayOfAdjSynsets.add(synsetUri);
				}
				while (hasReferences.hasNext()){
					Individual relatedObj=onto.getIndividual(hasReferences.next().asResource().getURI());
					Isense relatedSense=new Sense(relatedObj.getURI());
					String synsetUri=relatedSense.getSynsetUri();
					Individual synsetIndiv=onto.getIndividual(synsetUri);
					if (synsetIndiv.hasRDFType(nounSynsetClass))
						arrayOfAdjSynsets.add(synsetUri);
				}
			}
			
		return arrayOfAdjSynsets;
	}
	
	public ArrayList<String> listVerbSysnset(String wordSurface){
		
		ArrayList<String> matchedWordUris=searchOntWord(wordSurface);
		ArrayList<Individual> allSensesOfWord=new ArrayList<>();

		for(String wordUri:matchedWordUris){
			Individual vazhe=onto.getIndividual(wordUri);
			OntProperty meansOfWords=onto.getOntProperty(ns + "means");
			Iterator<RDFNode> sensesOfWord = vazhe.listPropertyValues(meansOfWords);		
			while (sensesOfWord.hasNext()){
				Individual tempIndividual=onto.getIndividual(sensesOfWord.next().asResource().getURI());
				allSensesOfWord.add(tempIndividual);
			}
		}
		ArrayList<String> arrayOfVerbSynsets=new ArrayList<>();
		OntProperty synsetOfSensesOfWord=onto.getOntProperty(ns+"inSynset");
		OntProperty infinitive=onto.getOntProperty(ns+"Infinitive");
		OntProperty hasRefrence=onto.getOntProperty(ns+"ReferTo");
		
		OntClass verbSynsetClass=onto.getOntClass(ns+"گروه_معنایی_فعل_ها");
		for (Individual senseIndiv:allSensesOfWord){
			Iterator<RDFNode> synsets=senseIndiv.listPropertyValues(synsetOfSensesOfWord);
			Iterator<RDFNode> infinitives=senseIndiv.listPropertyValues(infinitive);
			Iterator<RDFNode> hasRefrences=senseIndiv.listPropertyValues(hasRefrence);
			while(synsets.hasNext()){
				Individual synsetIndiv=onto.getIndividual(synsets.next().asResource().getURI());
				if (synsetIndiv==null || synsetIndiv.getURI()==null){
					continue;
				}
				if (synsetIndiv.hasRDFType(verbSynsetClass))
					arrayOfVerbSynsets.add(synsetIndiv.getURI());
			}
			while (infinitives.hasNext()){
				Individual relatedObj=onto.getIndividual(infinitives.next().asResource().getURI());
				Isense relatedSense=new Sense(relatedObj.getURI());
				
				String synsetUri=relatedSense.getSynsetUri();
				Individual synsetIndiv=onto.getIndividual(synsetUri);
				if (synsetIndiv.hasRDFType(verbSynsetClass))
					arrayOfVerbSynsets.add(synsetUri);
			}
			while (hasRefrences.hasNext()){
				Individual relatedObj=onto.getIndividual(hasRefrences.next().asResource().getURI());
				Isense relatedSense=new Sense(relatedObj.getURI());
				String synsetUri=relatedSense.getSynsetUri();
				Individual synsetIndiv=onto.getIndividual(synsetUri);
				if (synsetIndiv.hasRDFType(verbSynsetClass))
					arrayOfVerbSynsets.add(synsetUri);
			}
		}

		return arrayOfVerbSynsets;
	}
	
	
	public ArrayList<String> listAdvSynset(String wordSurface){
	
		ArrayList<String> matchedWordUris=searchOntWord(wordSurface);
		ArrayList<Individual> allSensesOfWord=new ArrayList<>();
		ArrayList<String> arrayOfAdvSynsets=new ArrayList<>();
		
		for(String wordUri:matchedWordUris){
			Individual vazhe=onto.getIndividual(wordUri);
			OntProperty meansOfWords=onto.getOntProperty(ns + "means");
			Iterator<RDFNode> sensesOfWord = vazhe.listPropertyValues(meansOfWords);		
			while (sensesOfWord.hasNext()){
				Individual tempIndividual=onto.getIndividual(sensesOfWord.next().asResource().getURI());
				allSensesOfWord.add(tempIndividual);
			}
		}
				
		OntProperty synsetOfSensesOfWord=onto.getOntProperty(ns+"inSynset");
		OntProperty hasRefrence=onto.getOntProperty(ns+"ReferTo");
		
		OntClass adjSynsetClass=onto.getOntClass(ns+"گروه_معنایی_قیدها");
		for(int i=0;i<allSensesOfWord.size();i++){
			
			
			
			Iterator<RDFNode> synsets=allSensesOfWord.get(i).listPropertyValues(synsetOfSensesOfWord);
			Iterator<RDFNode> hasRefrences=allSensesOfWord.get(i).listPropertyValues(hasRefrence);
			while(synsets.hasNext()){
				Individual synsetIndiv=onto.getIndividual(synsets.next().asResource().getURI());
				if (synsetIndiv==null || synsetIndiv.getURI()==null){
					continue;
				}
				if (synsetIndiv.hasRDFType(adjSynsetClass))
					arrayOfAdvSynsets.add(synsetIndiv.getURI());
			}
			while (hasRefrences.hasNext()){
				Individual relatedObj=onto.getIndividual(hasRefrences.next().asResource().getURI());
				Isense relatedSense=new Sense(relatedObj.getURI());
				String synsetUri=relatedSense.getSynsetUri();
				Individual synsetIndiv=onto.getIndividual(synsetUri);
				if (synsetIndiv.hasRDFType(adjSynsetClass))
					arrayOfAdvSynsets.add(synsetUri);
			}
		}
		return arrayOfAdvSynsets;
	}
	
	public ArrayList<String> listAdjSysnset(String wordSurface){
		
		ArrayList<String> matchedWordUris=searchOntWord(wordSurface);
		ArrayList<Individual> allSensesOfWord=new ArrayList<>();
		ArrayList<String> arrayOfAdjectiveSynsets=new ArrayList<>();
		
		for(String wordUri:matchedWordUris){
			Individual vazhe=onto.getIndividual(wordUri);
			OntProperty meansOfWords=onto.getOntProperty(ns + "means");
			Iterator<RDFNode> sensesOfWord = vazhe.listPropertyValues(meansOfWords);		
			while (sensesOfWord.hasNext()){
				Individual tempIndividual=onto.getIndividual(sensesOfWord.next().asResource().getURI());
				allSensesOfWord.add(tempIndividual);
			}
		}
				
		OntProperty synsetOfSensesOfWord=onto.getOntProperty(ns+"inSynset");
		OntProperty hasSingle=onto.getOntProperty(ns+"Single");
		OntProperty hasRefrence=onto.getOntProperty(ns+"ReferTo");
		
		OntClass adjSynsetClass=onto.getOntClass(ns+"گروه_معنایی_صفت_ها");
		for(int i=0;i<allSensesOfWord.size();i++){
			
			
			
			Iterator<RDFNode> synsets=allSensesOfWord.get(i).listPropertyValues(synsetOfSensesOfWord);
			Iterator<RDFNode> hasSingles=allSensesOfWord.get(i).listPropertyValues(hasSingle);
			Iterator<RDFNode> hasRefrences=allSensesOfWord.get(i).listPropertyValues(hasRefrence);
			while(synsets.hasNext()){
				Individual synsetIndiv=onto.getIndividual(synsets.next().asResource().getURI());
				if (synsetIndiv==null || synsetIndiv.getURI()==null){
					continue;
				}
				if (synsetIndiv.hasRDFType(adjSynsetClass))
					arrayOfAdjectiveSynsets.add(synsetIndiv.getURI());
			}
			while (hasSingles.hasNext()){
				Individual relatedObj=onto.getIndividual(hasSingles.next().asResource().getURI());
				Isense relatedSense=new Sense(relatedObj.getURI());
				if (relatedObj.getURI()==null){
					System.out.println(relatedObj);
					continue;
				}
				String synsetUri=relatedSense.getSynsetUri();
				Individual synsetIndiv=onto.getIndividual(synsetUri);
				if (synsetIndiv.hasRDFType(adjSynsetClass))
					arrayOfAdjectiveSynsets.add(synsetUri);
			}
			while (hasRefrences.hasNext()){
				Individual relatedObj=onto.getIndividual(hasRefrences.next().asResource().getURI());
				Isense relatedSense=new Sense(relatedObj.getURI());
				String synsetUri=relatedSense.getSynsetUri();
				Individual synsetIndiv=onto.getIndividual(synsetUri);
				if (synsetIndiv.hasRDFType(adjSynsetClass))
					arrayOfAdjectiveSynsets.add(synsetUri);
			}
		}
		return arrayOfAdjectiveSynsets;
	}
	
	public OntModel getOntology(){
	    return onto;
	}

	private static String normalize(String label) {
		label=label.replace("ه ی ", "ه ");
		label=label.replace("ه‌ی ", "ه ");
		label=label.replaceAll("[ \u200cًٌٍَُِّ ء ٰ ة]", "");
		label=label.replace("ئ","ی");
		label=label.replace("ؤ","و");
		label=label.replace("أ","ا");
		label=label.replace("إ","ا");
		label=label.replace("ي","ی");
		label=label.replace("ك","ک");
		label=label.replace("آ", "ا");
		label=label.replace("هٔ", "ه");
		label=label.replace("_", "");
		label=label.replace("ـ", "");
		label=label.replace("ٔ", "");
		label=label.replace("*", "");
		label=label.split("_")[0];
		
		
		return label;
	}

	private void createWordsLabel2UriHash() {

		OntClass Word=onto.getOntClass(wordUri);
		//OntClass Word=onto.getOntClass("http://www.mitrc.ir/farsnet#Word");
		if (Word==null){
			System.out.println("wrong uri for Word Class!");
			return;
		}
			
		Iterator<Individual> wordsList=onto.listIndividuals(Word);

		DatatypeProperty otherForm=onto.getDatatypeProperty(ns+"hasOtherForm");
		if (otherForm==null)
			System.err.println("bad Uri for otherForm!");
		int wordCounter=0;
		while (wordsList.hasNext()){
			wordCounter++;
			HashSet<String> uriSet=new HashSet<String>();
			Individual wordIndiv=wordsList.next();
			final NodeIterator labels = wordIndiv.listPropertyValues(RDFS.label);
			while( labels.hasNext() ) {
			    final RDFNode labelNode = labels.next();
			    final String label = labelNode.asLiteral().toString().split("@")[0].split("_")[0];
			    if (wordLabel2UrisHash.containsKey(label)){
					uriSet=wordLabel2UrisHash.get(label);
					uriSet.add(wordIndiv.getURI());

					wordLabel2UrisHash.put(label, uriSet);
				}
				else{
					uriSet.add(wordIndiv.getURI());
					wordLabel2UrisHash.put(label, uriSet);
				}
			    String newLabel=normalize(label);
		  		if (!newLabel.equalsIgnoreCase(label)){
					if (wordLabel2UrisHash.containsKey(newLabel)){
						uriSet=wordLabel2UrisHash.get(newLabel);
						uriSet.add(wordIndiv.getURI());
						wordLabel2UrisHash.put(newLabel, uriSet);
					}
					else{
						uriSet.add(wordIndiv.getURI());
						wordLabel2UrisHash.put(newLabel, uriSet);
                    }
                }
		  		Iterator<RDFNode> otherFormsIt=wordIndiv.listPropertyValues(otherForm);
		  		while(otherFormsIt.hasNext()){
		  			RDFNode node=otherFormsIt.next();
					String otherLabel=node.asLiteral().getValue().toString().split(Pattern.quote("^^"))[0];
					//System.out.println(otherLabel);
	  				if (!otherLabel.equalsIgnoreCase(label)){
	  					if (wordLabel2UrisHash.containsKey(otherLabel)){
								uriSet=wordLabel2UrisHash.get(otherLabel);
								uriSet.add(wordIndiv.getURI());
								wordLabel2UrisHash.put(otherLabel, uriSet);
						}
						else{
							uriSet.add(wordIndiv.getURI());
							wordLabel2UrisHash.put(otherLabel, uriSet);
		                }
	  				}	
		  		}
		  		
			}
		}
        System.err.println("wordSurfaces: "+wordLabel2UrisHash.values().size()+"	words: "+wordCounter );
    }

	/**
	 * this function reads an Ontology and returns it.
	 * @return OntModel
	 */
	private  OntModel readOntology(InputStream owlInputStream) {
		onto = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		onto.read(owlInputStream, null);
		return onto;
	}
	/**
	 * this function reads an Ontology and returns it.
	 * @return OntModel
	 */
	@SuppressWarnings("unused")
	private OntModel readOntology(InputStream owlInputStream,OntModelSpec spec) {
        onto = ModelFactory.createOntologyModel(spec);
        onto.read(owlInputStream, null);
	    return onto;
	}

	private ArrayList<String> searchOntWordWithLematization(String vazhe){
		ArrayList<String> lematizedWord=new ArrayList<>();
		List<List<Token>> tokenList = preprocess.process(vazhe);
		for (List<Token> tokens:tokenList){
			for (Token token:tokens){
				if (token.stopWord()||token.word().matches("[0-9a-zA-Z/.)(:؟?۰۰-۹-_}{،,]+")||token.tag().equals("PUNC")){
		    		continue;
				}
				lematizedWord.add(token.lemma());
			}
		}
		ArrayList<String> matchedWordUris = null;
		for (int i=0;i<lematizedWord.size();i++){
			String label=lematizedWord.get(i);
			
			
			if (wordLabel2UrisHash.containsKey(label)){
				matchedWordUris=new ArrayList<>();
				matchedWordUris.addAll(wordLabel2UrisHash.get(label));
			}
			else{
				label=normalize(label);
					if (wordLabel2UrisHash.containsKey(label)){
					matchedWordUris=new ArrayList<>();
					matchedWordUris.addAll(wordLabel2UrisHash.get(label));
					
				}
			}
		}	
		return matchedWordUris;
		
	}

	@SuppressWarnings("unused")
	private String getWordBySense(String SenseUri){
		Individual sense=onto.getIndividual(SenseUri);
		String labelOfSense=sense.getLabel("");
		String vazhe=labelOfSense.split("-")[0];
		return vazhe;
	}

	@SuppressWarnings("unused")
	private ArrayList<String> listMeansOfWord(String wordUri){
		Individual vazhe=onto.getIndividual(wordUri);
		OntProperty means=onto.getOntProperty(ns+"means");
		Iterator<RDFNode>allMeans=vazhe.listPropertyValues(means);
		ArrayList<String>wordSenseUris=new ArrayList<String>();
		while(allMeans.hasNext()){
			wordSenseUris.add(allMeans.next().toString());
		}
		/*for(int i=0;i<wordSenseUris.size();i++){
			System.out.println("All of means are: "+wordSenseUris.get(i));
		}*/
		return wordSenseUris;
	}

	protected static ArrayList<Term> listTermOfSynset(ArrayList<String> listWordsOfSynset,SynsetPos pos){
		ArrayList<Term> synsetTerms=new ArrayList<Term>();
		for (String eachWord : listWordsOfSynset){
			ArrayList<String> wordUris=searchOntWord(eachWord);
			if (wordUris==null || wordUris.isEmpty())
				continue;
			for (String eachWordUri : wordUris){
				Term eachTerm=new Term(eachWord,pos.name(),getWordFrequency(eachWordUri));
				synsetTerms.add(eachTerm);
	
			}
		}
		return synsetTerms;
	}
	public String getLabel(String uri){
		Individual indiv=onto.getIndividual(uri);
		return indiv.getLabel(null);
	}
	/**
	 * list wordSurfaces of a synset  
	 * @return ArrayList
	 */
	public static ArrayList<String> listWordsOfSynset(String synsetUri){
		Isynset synset=getSynset(synsetUri);
		ArrayList<String> words=new ArrayList<String>();
		if (synset==null||synset.getLabel()==null){
			return words;
		}
		String label=synset.getLabel().replace("«", "").replace("»", "");
		String [] sensesLabel=label.split("،");
		
		for(String sense:sensesLabel){
			words.add(sense.split("-")[0].split("_")[0]);
		}
		return words;
	}

	/****
	 * this function returns label of entered word.
	 * @return label of Word (String).
	 */
	public String getWordLabel(String wordUri){
		Individual word=onto.getIndividual(wordUri);
		String wordLabel=word.getLabel("");
		return wordLabel;
	}
	
	/*****
	 * this function returns all Senses that belong to entered Synset.
	 * @return array of URIs of Senses.
	 */
	public ArrayList<String> listSensesOfSynset(String synsetUri){
		Individual vazhe=onto.getIndividual(synsetUri);
		OntProperty countainWordSense=onto.getOntProperty(ns+"containWordSense");
		Iterator<RDFNode>senses=vazhe.listPropertyValues(countainWordSense);
		ArrayList<String>wordSense=new ArrayList<>();
		while(senses.hasNext()){
			wordSense.add(senses.next().toString());
		}

		return wordSense;	
	}

	/**
	 * this function returns all synset uris that entered word belongs them!
	 * @return ArrayList of SysnsetIDs(integer).
	 */
	public ArrayList<String> listSynsetUriOfWordUri(String wordUri){
		Individual vazhe=onto.getIndividual(wordUri);
		OntProperty meansOfWords=onto.getOntProperty(ns + "means");
		
		Iterator<RDFNode> sensesOfWord = vazhe.listPropertyValues(meansOfWords);		
		ArrayList<Individual> allSensesOfWord=new ArrayList<>();
		while (sensesOfWord.hasNext()){
			Individual tempIndividual=onto.getIndividual(sensesOfWord.next().asResource().getURI());
			allSensesOfWord.add(tempIndividual);
		}
		ArrayList<String> arrayOfSynsetOfSensesOfWord=new ArrayList<>();
		OntProperty synsetOfSensesOfWord=onto.getOntProperty(ns+"inSynset");
		OntProperty infinitive=onto.getOntProperty(ns+"Infinitive");
		OntProperty hasSingle=onto.getOntProperty(ns+"Single");
		OntProperty hasRefrence=onto.getOntProperty(ns+"ReferTo");
		for(int counter=0;counter<allSensesOfWord.size();counter++){
			Iterator<RDFNode> synsets=allSensesOfWord.get(counter).listPropertyValues(synsetOfSensesOfWord);
			Iterator<RDFNode> infinitives=allSensesOfWord.get(counter).listPropertyValues(infinitive);
			Iterator<RDFNode> hasSingles=allSensesOfWord.get(counter).listPropertyValues(hasSingle);
			Iterator<RDFNode> hasRefrences=allSensesOfWord.get(counter).listPropertyValues(hasRefrence);
			while(synsets.hasNext()){
				Individual synsetIndiv=onto.getIndividual(synsets.next().asResource().getURI());
				if (synsetIndiv==null || synsetIndiv.getURI()==null){
					continue;
				}
				arrayOfSynsetOfSensesOfWord.add(synsetIndiv.getURI());
			}
			while (infinitives.hasNext()){
				Individual relatedObj=onto.getIndividual(infinitives.next().asResource().getURI());
				Isense relatedSense=new Sense(relatedObj.getURI());
				String synsetUri=relatedSense.getSynsetUri();
				arrayOfSynsetOfSensesOfWord.add(synsetUri);
			}
			while (hasSingles.hasNext()){
				Individual relatedObj=onto.getIndividual(hasSingles.next().asResource().getURI());
				Isense relatedSense=new Sense(relatedObj.getURI());
				if (relatedObj.getURI()==null){
					System.out.println(relatedObj);
					continue;
				}
				String synsetUri=relatedSense.getSynsetUri();
				arrayOfSynsetOfSensesOfWord.add(synsetUri);
			}
			while (hasRefrences.hasNext()){
				Individual relatedObj=onto.getIndividual(hasRefrences.next().asResource().getURI());
				Isense relatedSense=new Sense(relatedObj.getURI());
				String synsetUri=relatedSense.getSynsetUri();
				arrayOfSynsetOfSensesOfWord.add(synsetUri);
			}
		}

		return arrayOfSynsetOfSensesOfWord;
	}
/****
 * this function returns true if entered URI related to Synset Object.
 * @return boolean
 */
	public boolean isSynset(String objectUri){
		Individual indiv = onto.getIndividual(objectUri);
		Iterator<Resource> typesOfIndiv=indiv.listRDFTypes(true);
		Resource syn=onto.getRestriction(ns+"SynSet");
		Resource nounSyn=onto.getResource(ns+"Noun");
		Resource verbSyn=onto.getResource(ns+"Verb");
		Resource adjSyn=onto.getResource(ns+"Adjective");
		Resource advSyn=onto.getResource(ns+"Adverb");
		while (typesOfIndiv.hasNext()){
			Resource eachType=typesOfIndiv.next();
			if (eachType.equals(syn) || eachType.equals(nounSyn) || eachType.equals(verbSyn) || eachType.equals(adjSyn) || eachType.equals(advSyn)){
				return true;
			}
		}
		return false;
	}
	
	public static OntWord getWord(String wordUri){
		Individual wordIndiv=onto.getIndividual(wordUri);
		if (wordIndiv==null){
			return null;
		}
		DatatypeProperty pronounciationProp=onto.getDatatypeProperty("http://www.mitrc.ir/farsnet#hasAvaInfo");
		RDFNode pronounciation=wordIndiv.getPropertyValue(pronounciationProp);
		String pronounciationValue="";
		if (pronounciation!=null)
			 pronounciationValue=pronounciation.asLiteral().getValue().toString();
	
		
		int frequencyValue=getWordFrequency(wordUri);

		return new OntWord(wordUri,pronounciationValue,frequencyValue);
		
	}
	
	private static int getWordFrequency(String wordUri) {
		Individual wordIndiv=onto.getIndividual(wordUri);
		if (wordIndiv==null){
			return 0;
		}
		DatatypeProperty frequencyProp=onto.getDatatypeProperty("http://www.mitrc.ir/Mobina#frequency");
		if (frequencyProp==null){
			frequencyProp=onto.createDatatypeProperty("http://www.mitrc.ir/Mobina#frequency");
		}
		RDFNode frequency=wordIndiv.getPropertyValue(frequencyProp);
		String literalFrequency;
		if (frequency==null)
			literalFrequency="0";
		else{
			literalFrequency=frequency.asLiteral().getValue().toString();
			if (literalFrequency==null){
				literalFrequency="0";
			}
		}
		return Integer.parseInt(literalFrequency);
	}

	public static ArrayList<Term> listTermOfSynset(String synsetUri){
		ArrayList<Term> synsetTerms=new ArrayList<Term>();
		if (synsetUri==null){
			System.err.println("null synset in listTermOfSynset");
			return synsetTerms;
		}
		Isynset synset=new Synset(synsetUri);
		SynsetPos synsetPos=synset.getPos();
		ArrayList<String> listWordsOfSynset=synset.listWordsOfSynset();
		
		synsetTerms=listTermOfSynset(listWordsOfSynset,synsetPos);
		return synsetTerms;
	}
	
	public ArrayList<Term> listMostFrequency(ArrayList<String> synsetUris,int termNumber){
		ArrayList<Term> mostFrequencyTerms=new ArrayList<Term>();
		ArrayList<Term> outputMostFreqeuncyTerms=new ArrayList<>();
		for (String eachSynsetUri : synsetUris){
			Isynset synset=getSynset(eachSynsetUri);
			if (synset==null){
				continue;
			}
			ArrayList<Term> synsetTerms=synset.listTerms();
			mostFrequencyTerms.addAll(synsetTerms);
		}
		Collections.sort(mostFrequencyTerms, new Comparator<Term>() {

			public int compare(Term term1, Term term2) {
				return term2.frequency-term1.frequency;
			}
		});
		
		int frequencyTermCounter=0;
		String lastTerm="";
		for (Term eachSortedTerm : mostFrequencyTerms){
			if (frequencyTermCounter>=termNumber){
				break;
			}
			if (eachSortedTerm.lemma.equals(lastTerm)){
				continue;
			}
			outputMostFreqeuncyTerms.add(eachSortedTerm);
			lastTerm=eachSortedTerm.lemma;
			frequencyTermCounter++;

			
		}
		
		return outputMostFreqeuncyTerms;
	}
	
	public ArrayList<Term> listMostFrequencyTermsBasedOnSet(ArrayList<String> synsetUris , int termNumber){
		
		SortedSet<Term> terms= new TreeSet<>(new Comparator<Term>() {

            public int compare(Term term1, Term term2) {
                return term2.frequency - term1.frequency;
            }
        });
		for (String eachSynsetUri : synsetUris){
			Isynset synset=getSynset(eachSynsetUri);
			if (synset==null){
				continue;
			}
			ArrayList<Term> synsetTerms=synset.listTerms();
			if (synsetTerms==null){
				continue;
			}
			terms.addAll(synsetTerms);
		}
		int frequencyTermCounter=0;
		ArrayList<Term> outputTermsArray=new ArrayList<Term>();
		for (Term eachSortedTerm : terms){
			if (frequencyTermCounter>=termNumber){
				break;
			}
			outputTermsArray.add(eachSortedTerm);
			frequencyTermCounter++;

			
		}
		return outputTermsArray;
	}
	/**
	 * return list of all word surfaces of mobina 
	 * @return Set<String> 
	 */
	public Set<String> listAllWords(){
		return wordLabel2UrisHash.keySet();
	}

	public List<String> listAllLabels(){
		OntClass wordClass=onto.getOntClass(ns+"واژه");
		Iterator<Individual> indivsOfWords=onto.listIndividuals(wordClass);
		ArrayList<String> labels=new ArrayList<>();
		while (indivsOfWords.hasNext()){
			Iterator<RDFNode> labelItr=indivsOfWords.next().listLabels("");
			while (labelItr.hasNext()) {
				labels.add(labelItr.next().toString());
			}
		}

		return labels;
	}


	/**
	 * return list of all nounSynsets of mobina 
	 * @return Set<String> nounSynsets Uri
	 */
	public List<String> listAllNounSynsets() {
		OntClass nounSynsetClass=onto.getOntClass(ns+"گروه_معنایی_اسم_ها");
		Iterator<Individual> indivsOfNounSynset=onto.listIndividuals(nounSynsetClass);
		List<String> nounSynsetsUrisArray=new ArrayList<>();
		while (indivsOfNounSynset.hasNext()){
			nounSynsetsUrisArray.add(indivsOfNounSynset.next().getURI());
		}
		
		return nounSynsetsUrisArray;

		
	}
	public List<String> listAllVerbSynsets() {
		OntClass verbSynsetClass=onto.getOntClass(ns+"گروه_معنایی_فعل_ها");
		Iterator<Individual> indivsOfVerbSynset=onto.listIndividuals(verbSynsetClass);
		List<String> verbSynsetsUrisArray=new ArrayList<String>();
		while (indivsOfVerbSynset.hasNext()){
			verbSynsetsUrisArray.add(indivsOfVerbSynset.next().getURI());
		}
		
		return verbSynsetsUrisArray;
	}
	public List<String> listAllAdverbSynsets() {
		OntClass adverbSynsetClass=onto.getOntClass(ns+"گروه_معنایی_قیدها");
		Iterator<Individual> indivsOfAdverbSynset=onto.listIndividuals(adverbSynsetClass);
		List<String> adverbSynsetsUrisArray=new ArrayList<>();
		while (indivsOfAdverbSynset.hasNext()){
			adverbSynsetsUrisArray.add(indivsOfAdverbSynset.next().getURI());
		}
		
		return adverbSynsetsUrisArray;	
	}
	public List<String> listAllAdjectiveSynsets() {
		OntClass adjectiveSynsetClass=onto.getOntClass(ns+"گروه_معنایی_صفت_ها");
		Iterator<Individual> indivsOfAdjectiveSynset=onto.listIndividuals(adjectiveSynsetClass);
		List<String> adjectiveSynsetsUrisArray=new ArrayList<>();
		while (indivsOfAdjectiveSynset.hasNext()){
			adjectiveSynsetsUrisArray.add(indivsOfAdjectiveSynset.next().getURI());
		}
		
		return adjectiveSynsetsUrisArray;	
	}
	public List<String> listAllSynsets() {
		OntClass nounSynsetClass=onto.getOntClass(ns+"گروه_معنایی_اسم_ها");
		OntClass verbSynsetClass=onto.getOntClass(ns+"گروه_معنایی_فعل_ها");
		OntClass adjSynsetClass=onto.getOntClass(ns+"گروه_معنایی_صفت_ها");
		OntClass advSynsetClass=onto.getOntClass(ns+"گروه_معنایی_قیدها");
		
		ArrayList<String> allSynsetUris=new ArrayList<String>();
	
		Iterator<Individual> nounSynIndivs=onto.listIndividuals(nounSynsetClass);
		Iterator<Individual> verbSynIndivs=onto.listIndividuals(verbSynsetClass);
		Iterator<Individual> adjSynIndivs=onto.listIndividuals(adjSynsetClass);
		Iterator<Individual> advSynIndivs=onto.listIndividuals(advSynsetClass);
		
		while (nounSynIndivs.hasNext()){
			allSynsetUris.add(nounSynIndivs.next().getURI());
		}
		while (verbSynIndivs.hasNext()){
			allSynsetUris.add(verbSynIndivs.next().getURI());
		}
		while (adjSynIndivs.hasNext()){
			allSynsetUris.add(adjSynIndivs.next().getURI());
		}
		while (advSynIndivs.hasNext()){
			allSynsetUris.add(advSynIndivs.next().getURI());
		}
		return allSynsetUris;

	}

	public List<List<String>> listAllWordForms(){
		OntClass Word=onto.getOntClass(wordUri);
		List<List<String>> wordForms=new ArrayList<>();
		//OntClass Word=onto.getOntClass("http://www.mitrc.ir/farsnet#Word");
		if (Word==null){
			System.out.println("wrong uri for Word Class!");
			return null;
		}

		Iterator<Individual> wordsList=onto.listIndividuals(Word);

		DatatypeProperty otherForm=onto.getDatatypeProperty(ns+"hasOtherForm");
		if (otherForm==null)
			System.err.println("bad Uri for otherForm!");
		while (wordsList.hasNext()) {
			List<String> forms = new ArrayList<>();
			Individual wordIndiv = wordsList.next();
			final NodeIterator labels = wordIndiv.listPropertyValues(RDFS.label);
			String normalLabel = "";
			while (labels.hasNext()) {
				final RDFNode labelNode = labels.next();
				final String label = labelNode.asLiteral().toString().split("@")[0].split("_")[0];
				forms.add(label);
				normalLabel = normalize(label);
				Iterator<RDFNode> otherFormsIt = wordIndiv.listPropertyValues(otherForm);
				while (otherFormsIt.hasNext()) {
					RDFNode node = otherFormsIt.next();
					String otherLabel = node.asLiteral().getValue().toString().split(Pattern.quote("^^"))[0];
					String normalOtherLabel = normalize(otherLabel);
					if (!normalOtherLabel.equalsIgnoreCase(normalLabel)) {
						forms.add(otherLabel);
					}
				}

			}

			if (forms.size() > 1)
				wordForms.add(forms);
		}
	//	System.err.println("wordSurfaces: "+wordLabel2UrisHash.values().size()+"	words: "+wordCounter );
		return wordForms;
	}
}