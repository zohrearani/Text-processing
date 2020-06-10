package ir.malek.newsanalysis.ner;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import ir.malek.newsanalysis.preprocess.Token;
import ir.malek.newsanalysis.preprocess.normalizer.Normalizer;
import ir.malek.newsanalysis.util.collection.ListUtil;
import ir.malek.newsanalysis.util.enums.NERLabel;
import ir.malek.newsanalysis.util.io.IOUtils;
import ir.malek.newsanalysis.util.performance.NLPPerformance;
import ir.malek.newsanalysis.util.string.StringUtil;

public class RuleBasedNER {
	private ArrayList<ArrayList<NERToken>> docNERTokens;
	private Map<Entity, Double> entities; // entities and their probability
	private Set<String> notEntities; // strings that are not a person or organization or location
	private Set<String> famousPeople;
	private Map<String, EntityType> preNames; // include words that usually used before named entities. example: <آقای> -> <PER>, <شهر> -> <LOC> 
	private Map<String, EntityType> postNames; // include words that usually used after named entities.
	private Set<String> frequentWords; // frequent words
	private Set<String> frequentWordsWithoutSpaces; // frequent words

	private InputStream entitiesInputStream = this.getClass().getClassLoader().getResourceAsStream("NER-Resources/entities.txt");
	private InputStream notEntitiesInputStream = this.getClass().getClassLoader().getResourceAsStream("NER-Resources/not_entities.txt");
	private InputStream famousIranianPeopleInputStream = this.getClass().getClassLoader().getResourceAsStream("NER-Resources/FamousPeople-Iran.txt");
	private InputStream famousIranianPeopleAliasInputStream = this.getClass().getClassLoader().getResourceAsStream("NER-Resources/FamousPeople-IranAlias.txt");
	private InputStream famousWorldPeopleInputStream = this.getClass().getClassLoader().getResourceAsStream("NER-Resources/FamousPeople-World.txt");
	private InputStream famousWorldPeopleAliasInputStream = this.getClass().getClassLoader().getResourceAsStream("NER-Resources/FamousPeople-WorldAlias.txt");
	private InputStream famousPeopleSahabeInputStream = this.getClass().getClassLoader().getResourceAsStream("NER-Resources/FamousPeople-Sahabe.txt");
	private InputStream preNamesInputStream = this.getClass().getClassLoader().getResourceAsStream("NER-Resources/prenames.txt");
	private InputStream postNamesInputStream = this.getClass().getClassLoader().getResourceAsStream("NER-Resources/postnames.txt");
	private InputStream frequentWordsInputStream = this.getClass().getClassLoader().getResourceAsStream("NER-Resources/FrequentWords-WithoutPersons-FromBijankhan.txt");
	private Normalizer normalizer;
	public NLPPerformance performance = new NLPPerformance(TimeUnit.NANOSECONDS);

	private static final double MIN_THRESHOLD_FOR_LABELING = 0.4;

	public RuleBasedNER() {
		normalizer = new Normalizer("validChar.txt", "changingChar.txt");
		loadEntities(entitiesInputStream);
		notEntities = readStrSet(notEntitiesInputStream);
		famousPeople = readStrSet(famousIranianPeopleInputStream);
		famousPeople.addAll(readStrSet(famousIranianPeopleAliasInputStream));
		famousPeople.addAll(readStrSet(famousWorldPeopleInputStream));
		famousPeople.addAll(readStrSet(famousWorldPeopleAliasInputStream));
		famousPeople.addAll(readStrSet(famousPeopleSahabeInputStream));
		preNames = loadStrMapEntityType(preNamesInputStream);
		loadFrequentWords(frequentWordsInputStream);
		famousPeople.removeAll(frequentWords);
		// postNames = loadStrMapEntityType(postNamesFilePath);
	}

	public void setNER(List<List<Token>> docTokens) {
		long startTime = System.nanoTime();

		// create docNERTokens by docTokens and set candidate subtypes for each token
		// the size of docNERTokens would be the same as docTokens
		docNERTokens = new ArrayList<ArrayList<NERToken>>();
		for (List<Token> senTokens : docTokens) {
			ArrayList<NERToken> senNERTokens = getNERTokens(senTokens);
			docNERTokens.add(senNERTokens);
		}

		// weighting candidate subtypes for each token by previous tokens
		Set<String> acceptablePOS = new HashSet<String>(Arrays.asList("N", "ADJ", "AR", "CONJ"));
		Set<String> notAcceptablePOSforEnd = new HashSet<String>(Arrays.asList("CONJ", "PUNC"));
		List<String> passivizingWords = new ArrayList<String>(Arrays.asList("این", "آن", "یک")); // if these words are appeared in token(i-2), do not weight token(i) according to token(i-1) 
		for (List<NERToken> senNERTokens : docNERTokens) {
			int j;
			for (int i = 0; i < senNERTokens.size(); i += j) {
				j = 1;
				String currentWord = senNERTokens.get(i).token.word();
				String previousWord = i > 0 ? senNERTokens.get(i - 1).token.word() : "";
				String nextWord = i < senNERTokens.size() - 1 ? senNERTokens.get(i + 1).token.word() : "";
				if (preNames.containsKey(currentWord) && senNERTokens.get(i).getTypeProbability(preNames.get(currentWord)) < 0.9) {
					if (passivizingWords.contains(previousWord) || StringUtil.isNumeric(previousWord) || StringUtil.isSupAdj(previousWord))
						continue;
					EntityType predictedEntityTypeByPrevToken = preNames.get(currentWord);

					Set<Integer> mentionIndices = new HashSet<Integer>();
					mentionIndices.add(i);
					if (senNERTokens.get(i + 1).getTypeProbability(EntityType.PER) > 0.3 || preNames.containsKey(nextWord)) {
						mentionIndices.add(i + 1);
						j++;
					}
					while ((i + j) < senNERTokens.size() && mentionIndices.contains(senNERTokens.get(i + j).token.dep.parent) && acceptablePOS.contains(senNERTokens.get(i + j).token.tag()) && (j != 1 || senNERTokens.get(i + j).token.word().equals("و") == false)
							&& (predictedEntityTypeByPrevToken != EntityType.PER || senNERTokens.get(i + j).getTypeProbability(EntityType.PER) > 0.01)) {
						mentionIndices.add(i + j);
						j++;
					}
					while (j > 1 && notAcceptablePOSforEnd.contains(senNERTokens.get(i + j - 1).token.tag())) {
						j--;
					}
					if (j > 1) {
						for (int k = 0; k < j; k++) {
							senNERTokens.get(i + k).changeMainTypeMinWeight(0.9, predictedEntityTypeByPrevToken);
						}
					}
				}
			}
		}

		// weighting candidate subtypes if person names are next to each other
		weightingPersons();

		// labelling
		labelingNamedEntities(MIN_THRESHOLD_FOR_LABELING);

		// calculate time
		long endTime = System.nanoTime();
		performance.add(endTime - startTime, ListUtil.getSize(docTokens));
	}

	private List<String> createOneToMaxLengthConcatinatedTokens(List<Token> tokens, int startIndex, int maxLength) {
		int endIndex = (startIndex + maxLength) > tokens.size() ? tokens.size() : (startIndex + maxLength);
		List<String> concatTokens = new ArrayList<String>();
		concatTokens.add(tokens.get(startIndex).word());
		if (tokens.get(startIndex).tag().equals("V") == false) {
			for (int i = startIndex + 1; i < endIndex; i++) {
				if (tokens.get(i).tag().equals("PUNC")) // if token(i) is punctuation, concatenate it without space
					concatTokens.add(concatTokens.get(concatTokens.size() - 1) + tokens.get(i).word());
				else
					concatTokens.add(concatTokens.get(concatTokens.size() - 1) + " " + tokens.get(i).word());
			}
		}
		return concatTokens;
	}

	// The size of returned senNERTokens would be the same as input senTokens.
	// Candidate subtypes would be assign to each token by the maximum match between sequence of tokens and the entities.
	private ArrayList<NERToken> getNERTokens(List<Token> senTokens) {
		ArrayList<NERToken> senNERTokens = new ArrayList<NERToken>();

		int maxLength = 7;
		int foundedFamousPeopleLen = 0;
		for (int i = 0; i < senTokens.size(); /*don't increment i here*/) {
			List<String> concatTokens = createOneToMaxLengthConcatinatedTokens(senTokens, i, maxLength);

			// search for notEntities
			int j;
			for (j = concatTokens.size() - 1; j >= 0; j--) {
				if (notEntities.contains(concatTokens.get(j))) {
					break;
				}
			}

			Map<EntitySubType, Double> candidateSubTypes = new HashMap<EntitySubType, Double>();
			if (j < 0) { // if notEntities not found, create candidateSubTypes

				// find candidateSubTypes by maximum match of concatTokens and entities.
				// candidateSubTypes includes the candidate subtypes of the maximum matched and its (theirs) probabilities.
				for (j = concatTokens.size() - 1; j >= 0; j--) {
					if (j > 0 || senTokens.get(i).tag().equals("V") == false)
						candidateSubTypes = getCandidateSubTypes(concatTokens.get(j));
					if (foundedFamousPeopleLen <= 0 && famousPeople.contains(concatTokens.get(j)))
						foundedFamousPeopleLen = j + 1;
					if (candidateSubTypes.size() > 0)
						break;
				}

				// any Iranian person name could also be a street name
				double sumIranianNameProbability = candidateSubTypes.getOrDefault(EntitySubType.IRANIAN_FIRST_NAME, 0.0);
				sumIranianNameProbability += candidateSubTypes.getOrDefault(EntitySubType.IRANIAN_LAST_NAME, 0.0);
				if (sumIranianNameProbability > 0)
					candidateSubTypes.put(EntitySubType.IRANIAN_FAMOUS_STREET, 0.1 * sumIranianNameProbability);

				// if candidateSubTypes is empty and the word is not in frequentWords it may be a named entity.
				if (candidateSubTypes.size() == 0 && frequentWordsWithoutSpaces.contains(StringUtil.removeSpaces(senTokens.get(i).word())) == false && senTokens.get(i).word().contains(" ") == false) {
					candidateSubTypes.put(EntitySubType.IRANIAN_LAST_NAME, 0.2);
					candidateSubTypes.put(EntitySubType.IRANIAN_FIRST_NAME, 0.05);
					candidateSubTypes.put(EntitySubType.US_LAST_NAME, 0.1);
					candidateSubTypes.put(EntitySubType.US_FIRST_NAME, 0.1);
					candidateSubTypes.put(EntitySubType.LOC, 0.1);
					candidateSubTypes.put(EntitySubType.ORG, 0.1);
				}

				// set minimum default for ORG
				if (candidateSubTypes.containsKey(EntitySubType.ORG) == false) {
					candidateSubTypes.put(EntitySubType.ORG, 0.01);
				}

				// set minimum default for LOC
				if (candidateSubTypes.containsKey(EntitySubType.LOC) == false) {
					candidateSubTypes.put(EntitySubType.LOC, 0.01);
				}
			}
			// add PER subtype to candidateSubTypes if foundedFamousPeopleLen>0
			if (foundedFamousPeopleLen > 0) {
				foundedFamousPeopleLen -= Math.max(j + 1, 1);
				candidateSubTypes.put(EntitySubType.PER, MIN_THRESHOLD_FOR_LABELING);
			}

			// create NERTokens and add them to senNERTokens  
			// current value of j is (the length of maximum match)-1
			do {
				NERToken nerToken = new NERToken(senTokens.get(i));
				nerToken.candidateSubTypes = candidateSubTypes;
				senNERTokens.add(nerToken);
				j--;
				i++;
			} while (j >= 0);
		}

		return senNERTokens;
	}

	private void weightingPersons() {
		for (List<NERToken> senNERTokens : docNERTokens) {
			for (int i = 0; i < senNERTokens.size(); i++) {
				if (i < senNERTokens.size() - 1 && senNERTokens.get(i).getSubTypeProbability(EntitySubType.IRANIAN_FIRST_NAME) > 0 && senNERTokens.get(i + 1).getSubTypeProbability(EntitySubType.IRANIAN_LAST_NAME) > 0) {
					// Increment next token's candidate-entity-subtypes weight
					double fnWeight = senNERTokens.get(i).candidateSubTypes.get(EntitySubType.IRANIAN_FIRST_NAME);
					double lnWeight = senNERTokens.get(i + 1).candidateSubTypes.get(EntitySubType.IRANIAN_LAST_NAME);
					senNERTokens.get(i).changeSubTypeMinWeight((fnWeight + lnWeight) / 2, EntitySubType.IRANIAN_FIRST_NAME);
					senNERTokens.get(i + 1).changeSubTypeMinWeight((fnWeight + lnWeight) / 2, EntitySubType.IRANIAN_LAST_NAME);
				}
				if (i < senNERTokens.size() - 1 && senNERTokens.get(i).getSubTypeProbability(EntitySubType.US_FIRST_NAME) > 0 && senNERTokens.get(i + 1).getSubTypeProbability(EntitySubType.US_LAST_NAME) > 0) {
					// Increment next token's candidate-entity-subtypes weight
					double fnWeight = senNERTokens.get(i).candidateSubTypes.get(EntitySubType.US_FIRST_NAME);
					double lnWeight = senNERTokens.get(i + 1).candidateSubTypes.get(EntitySubType.US_LAST_NAME);
					senNERTokens.get(i).changeSubTypeMinWeight((fnWeight + lnWeight) / 2, EntitySubType.US_FIRST_NAME);
					senNERTokens.get(i + 1).changeSubTypeMinWeight((fnWeight + lnWeight) / 2, EntitySubType.US_LAST_NAME);
				}
			}
		}
	}

	private void labelingNamedEntities(double threshold) {
		for (List<NERToken> senNERTokens : docNERTokens) {
			EntityType entityType = null;
			EntityType prevEntityType = null;
			for (NERToken nerToken : senNERTokens) {
				prevEntityType = entityType;
				entityType = (nerToken.getBestSubType(threshold) == null) ? null : nerToken.getBestSubType(threshold).getMainType();
				NERLabel nerLabel = entityTypeToNERLabel(entityType, prevEntityType);
				nerToken.token.setNer(nerLabel);
				nerToken.token.setNESubType((entityType == null) ? "O" : nerToken.getBestSubType(threshold).toString());
			}
		}
	}

	public NERLabel entityTypeToNERLabel(EntityType entityType, EntityType prevEntityType) {
		if (entityType == null)
			return NERLabel.O;
		if (prevEntityType == null || entityType != prevEntityType) {
			switch (entityType) {
			case PER:
				return NERLabel.B_PER;
			case ORG:
				return NERLabel.B_ORG;
			case LOC:
				return NERLabel.B_LOC;
			}
		} else {
			switch (entityType) {
			case PER:
				return NERLabel.I_PER;
			case ORG:
				return NERLabel.I_ORG;
			case LOC:
				return NERLabel.I_LOC;
			}
		}
		return NERLabel.O;
	}

	// get candidate subtypes and their probabilities
	private HashMap<EntitySubType, Double> getCandidateSubTypes(String token) {
		HashMap<EntitySubType, Double> candidateSubTypes = new HashMap<EntitySubType, Double>();
		for (EntitySubType subtype : EntitySubType.values()) {
			Entity searchingEntity = new Entity(token, subtype);
			if (entities.containsKey(searchingEntity))
				candidateSubTypes.put(subtype, entities.get(searchingEntity));
		}
		if (preNames.containsKey(token)) {
			if (preNames.get(token) == EntityType.PER)
				candidateSubTypes.put(EntitySubType.SURN, 0.3);
		}
		return candidateSubTypes;
	}

	public Map<String, EntitySubType> getLocEntities() {
		Map<String, EntitySubType> locEntities = new HashMap<String, EntitySubType>();
		for (Entity entity : entities.keySet()) {
			if (entity.getType() == EntityType.LOC)
				locEntities.put(entity.getName(), entity.getSubType());
		}
		return locEntities;
	}

	public Map<String, EntitySubType> getOrgEntities() {
		Map<String, EntitySubType> orgEntities = new HashMap<String, EntitySubType>();
		for (Entity entity : entities.keySet()) {
			if (entity.getType() == EntityType.ORG)
				orgEntities.put(entity.getName(), entity.getSubType());
		}
		return orgEntities;
	}

	public Map<String, EntitySubType> getPerEntities() {
		Map<String, EntitySubType> perEntities = new HashMap<String, EntitySubType>();
		for (Entity entity : entities.keySet()) {
			if (entity.getType() == EntityType.PER)
				perEntities.put(entity.getName(), entity.getSubType());
		}
		return perEntities;
	}

	// Load entities.Each entity has a name, subtype and probability 
	private void loadEntities(InputStream inputStream) {
		entities = new HashMap<Entity, Double>();
		List<String> lines = IOUtils.linesFromFile(inputStream);
		String parts[];
		for (String line : lines) {
			parts = line.split("\t");
			if (parts.length == 3) {
				Entity entity = new Entity(normalizer.process(parts[0]), EntitySubType.valueOf(parts[1]));
				entities.put(entity, Double.parseDouble(parts[2]));
			}
		}
	}

	private Map<String, EntityType> loadStrMapEntityType(InputStream inputStream) {
		Map<String, EntityType> map = new HashMap<String, EntityType>();
		List<String> lines = IOUtils.linesFromFile(inputStream);
		String[] parts;
		for (String line : lines) {
			parts = line.split("\t");
			if (parts.length == 2)
				map.put(normalizer.process(parts[0]), EntityType.valueOf(parts[1]));
		}
		return map;
	}

	private void loadFrequentWords(InputStream inputStream) {
		frequentWords = readStrSet(inputStream);
		frequentWordsWithoutSpaces = new HashSet<String>();
		for(String word:frequentWords){
			frequentWordsWithoutSpaces.add(StringUtil.removeSpaces(word));
		}
	}

	private Set<String> readStrSet(InputStream inputStream) {
		Set<String> strSet = new HashSet<String>();
		List<String> lines = IOUtils.linesFromFile(inputStream);
		for (String line : lines) {
			strSet.add(normalizer.process(line));
		}
		return strSet;
	}

}
