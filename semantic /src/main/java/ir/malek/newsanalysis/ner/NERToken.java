package ir.malek.newsanalysis.ner;

import java.util.HashMap;
import java.util.Map;

import ir.malek.newsanalysis.preprocess.Token;

public class NERToken {

	Map<EntitySubType, Double> candidateSubTypes = new HashMap<EntitySubType, Double>();

	public Token token;

	private EntitySubType bestSubType;
	private double bestSubTypeWeight;

	private void findBestSubType() {
		bestSubTypeWeight = 0;
		bestSubType = null;
		for (Map.Entry<EntitySubType, Double> entry : candidateSubTypes.entrySet()) {
			if (entry.getValue() > bestSubTypeWeight) {
				bestSubType = entry.getKey();
				bestSubTypeWeight = entry.getValue();
			}
		}
	}

	public EntitySubType getBestSubType(double minAcceptableWeight) {
		findBestSubType();
		if (bestSubTypeWeight >= minAcceptableWeight)
			return bestSubType;
		else
			return null;
	}

	public double getBestSubTypeWeight(double minAcceptableWeight) {
		findBestSubType();
		if (bestSubTypeWeight >= minAcceptableWeight)
			return bestSubTypeWeight;
		else
			return 0;
	}

	public void changeSubTypeMinWeight(double minWeight, EntitySubType... subtypes) {
		assert minWeight >= 0 && minWeight <= 1;
		for (int i = 0; i < subtypes.length; i++) {
			if (candidateSubTypes.containsKey(subtypes[i])) {
				candidateSubTypes.put(subtypes[i], candidateSubTypes.get(subtypes[i]) * (1 - minWeight) + minWeight);
			}
		}
	}

	public void changeMainTypeMinWeight(double minWeight, EntityType maintype) {
		assert minWeight >= 0 && minWeight <= 1;
		for (EntitySubType subtype : candidateSubTypes.keySet()) {
			if(subtype==EntitySubType.PER)
				continue;
			if (subtype.getMainType() == maintype) {
				candidateSubTypes.put(subtype, candidateSubTypes.get(subtype) * (1 - minWeight) + minWeight);
			} else { // decrease others weight
				candidateSubTypes.put(subtype, candidateSubTypes.get(subtype) * (1 - (minWeight / 3)));
			}
		}
	}

	public double getTypeProbability(EntityType entityType) {
		double sum = 0;
		for (EntitySubType candidateSubType : candidateSubTypes.keySet()) {
			if (candidateSubType.getMainType() == entityType)
				sum += candidateSubTypes.get(candidateSubType);
		}
		return sum;
	}

	public double getSubTypeProbability(EntitySubType subtype) {
		if (candidateSubTypes.containsKey(subtype))
			return candidateSubTypes.get(subtype);
		return 0;
	}

	NERToken(Token token) {
		this.token = token;
	}

	public String toString() {
		return token.word() + candidateSubTypes.toString();
	}
}
