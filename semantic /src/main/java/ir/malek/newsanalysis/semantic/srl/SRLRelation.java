package ir.malek.newsanalysis.semantic.srl;

import ir.malek.newsanalysis.preprocess.Token;
import ir.malek.newsanalysis.util.enums.RoleLabel;

import java.util.TreeSet;

/**
 * A structure for Representing Semantic Role labeling for a sentence as a list
 * of tokens. first Argument shows an event and is a predicate in SRL such as a simple or compound Verb like
 * "حمله کردن" or a Action noun Like "حمله" the Role is semantic role form
 * {@linkplain ir.malek.newsanlysis.util.enums.RoleLabel RoleLabel}
 * <br> Last Argument is a participant of the event. 
 * 
 * @author Arani
 *
 */
public class SRLRelation {

	public static final long serialVersionUID = 1L;

	public class Argument {
		private String lemma = null;
		private String uri = null;
		TreeSet<Integer> indexSet = new TreeSet<Integer>();
		private int head = -1;

		public TreeSet<Integer> getIndexSet() {
			return indexSet;

		}

		public int firstIndex() {
			return indexSet.first();
		}

		public String getUri() {
			return uri;
		}

		public void setUri(String uri) {
			this.uri = uri;
		}

		public String getLemma() {
			return lemma;
		}

		public void setLemma(String lemma) {
			this.lemma = lemma;

		}

		public int getHead() {
			return head;
		}

		public void setHead(int head) {
			this.head = head;
		}
	}

	Argument arg1 = new Argument();
	Argument arg2 = new Argument();
	RoleLabel role = RoleLabel.O;
	boolean isPositive = true;
	int ruleNo = -1;
	String ruleConfidence = "";

	public String toString() {
		String out = "\trel\t" + ruleNo + "\t" + ruleConfidence + "\t" + arg1.lemma + "\t" + role + "\t" + arg2.lemma
				+ "\t" + isPositive/*
									 * +"\n"+a.getSemanticCategory()+":"+b.
									 * getSemanticCategory()+"\n"+a.getDepString
									 * ()+"\n"+b.getDepString()+". \n"
									 */;
		return out;
	}

	SRLRelation() {

	}

	SRLRelation(Token a, Token b, RoleLabel role, boolean isPosetive, int NO, String confidence) {

		arg1.lemma = a.lemma();
		arg2.lemma = b.lemma();

		arg1.indexSet.add(a.dep.index);
		arg2.indexSet.add(b.dep.index);
		this.role = role;
		this.ruleNo = NO;
		this.ruleConfidence = confidence;
	}

	public RoleLabel getRole() {
		return role;
	}

	public Argument getArg1() {
		return arg1;
	}

	public Argument getArg2() {
		return arg2;
	}

	public boolean getPolarity() {
		return isPositive;
	}

	public void setRole(RoleLabel role) {
		this.role=role;
		
	}
}
