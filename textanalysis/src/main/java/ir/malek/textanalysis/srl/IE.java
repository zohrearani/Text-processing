package ir.malek.textanalysis.srl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.io.EncodingPrintWriter.out;
import ir.malek.newsanalysis.preprocess.Preprocess;
import ir.malek.newsanalysis.preprocess.Token;
import ir.malek.newsanalysis.preprocess.postagger.TagSet;
import ir.malek.newsanalysis.util.io.IOUtils;
import ir.malek.newsanalysis.util.io.OutFile;

public class IE {

	public List<IERelation> extractRelation(List<SRLRelation> rels, List<Token> tokensOfSen) {
		List<IERelation> ieRels = new ArrayList<>();
		for (int i = 0; i < rels.size() - 1; i++) {
			SRLRelation rel1 = rels.get(i);
			for (int j = i + 1; j < rels.size(); j++) {
				SRLRelation rel2 = rels.get(j);
				int ind11 = rel1.getArg1().firstIndex();
				int ind12 = rel1.getArg2().firstIndex();
				int ind21 = rel2.getArg1().firstIndex();
				int ind22 = rel2.getArg2().firstIndex();
				if (ind11 == ind21 && (ind12==10000 || !tokensOfSen.get(ind12).tag().equals(TagSet.ADVERB)) && (ind22==10000 || !tokensOfSen.get(ind22).tag().equals(TagSet.ADVERB))) {
					IERelation ieRel = new IERelation(tokensOfSen,rel1.getArg1(),rel1.getArg2(),rel2.getArg2());
					ieRel.confidence1 = rel1.ruleConfidence;
					ieRel.confidence2 = rel2.ruleConfidence;
					ieRels.add(ieRel);
				}
				if (ind12 == ind21 && (ind22==10000 || !tokensOfSen.get(ind22).tag().equals(TagSet.ADVERB))) {
					IERelation ieRel = new IERelation(tokensOfSen,rel1.getArg1(),rel1.getArg2(),rel2.getArg2());
					ieRel.confidence1 = rel1.ruleConfidence;
					ieRel.confidence2 = rel2.ruleConfidence;
					ieRels.add(ieRel);
				}
				if (ind22 == ind11 && (ind12==10000 || !tokensOfSen.get(ind12).tag().equals(TagSet.ADVERB))) {
					IERelation ieRel = new IERelation(tokensOfSen,rel1.getArg1(),rel1.getArg2(),rel2.getArg1());
					ieRel.confidence1 = rel1.ruleConfidence;
					ieRel.confidence2 = rel2.ruleConfidence;
					ieRels.add(ieRel);
				}
			}
		}
		return ieRels;
	}

}
