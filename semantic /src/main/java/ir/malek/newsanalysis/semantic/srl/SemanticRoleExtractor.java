package ir.malek.newsanalysis.semantic.srl;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeSet;

import com.github.andrewoma.dexx.collection.HashSet;

import ir.malek.newsanalysis.preprocess.Token;
import ir.malek.newsanalysis.preprocess.postagger.TagSet;
import ir.malek.newsanalysis.util.enums.RoleLabel;
import ir.malek.newsanalysis.util.io.InFile;
import ir.malek.newsanalysis.util.io.OutFile;

/**
 * 
 * @author Z. Arani
 *
 */

class IF {
	public String dep;
	public String posTag;
	public String category;
	public String parentCategory;
	public ArrayList<String> FCCategoryList = new ArrayList<String>();
	public String FCCategory;
	public String Lemma;
	public ArrayList<String> Lemmas = new ArrayList<String>();
	public String sense;
}

class THEN {
	public String arg1;
	public String role;
	public String arg2;
	public String arg3;
	public String function;
	public String confidence;
	int No = -1;
}

class Rule {

	public IF If;
	public THEN Then;

	public Rule(IF If, THEN Then) {
		this.If = If;
		this.Then = Then;
	}
}

/**
 * 
 * <h1>Semantic Role Labeling engine</h1> main method of this class is
 * {@linkplain #SemanticRoleExtractor)}
 * 
 * @author Arani
 *
 */
public class SemanticRoleExtractor {

	static int prenounIndex = 10000;
	ArrayList<Rule> ruleSet = new ArrayList<Rule>();
	OutFile outtaggedLemmas = new OutFile(new File("..//Output//TaggedSentences.txt").getAbsolutePath());
	OutFile outruleLog = new OutFile(new File("..//Output//logOfrules.txt").getAbsolutePath());

	List<Token> tokensOfSen;
	ArrayList<SRLRelation> output;
	List<TreeSet<Integer>> compound2;
	List<TreeSet<Integer>> compound3;
	List<ArrayList<Integer>> compoundPrevious;
	ArrayList<ArrayList<Integer>> firstChildsArray;
	public static HashMap<String, String> valencyHash = new HashMap<String, String>();
	ExVerbs exVerbs = new ExVerbs();

	/**
	 * name of functions to be call for some purposes.
	 *
	 */
	private enum FunctionInRule {
		o, CompoundPrevious, Compound3_V,Compound3,Compound2_V, Compound2, Transfer, Transfer2, CopySbj, Negative, ActVerbEx;
	}

	/**
	 * The constructor loads rules from a file like resources/Rules3.0.txt
	 */
	public SemanticRoleExtractor() {
		loadRules(this.getClass().getClassLoader().getResourceAsStream("Rules3.0.txt"));
		// loadValencies(".."+File.separator+"semantic"+File.separator+
		// "resources"+File.separator+"PerValLex3.txt");
	}

	/**
	 * 
	 * @param tokensOfSen
	 *            includes parse + //sense + semantic category for each tokens.
	 */
	public ArrayList<SRLRelation> extractRole(List<Token> tokensOfSen) {
		this.tokensOfSen = tokensOfSen;

		output = new ArrayList<SRLRelation>();
		compound2 = new ArrayList<TreeSet<Integer>>();
		compound3 = new ArrayList<TreeSet<Integer>>();
		compoundPrevious = new ArrayList<ArrayList<Integer>>();
		firstChildsArray = new ArrayList<>();

		String sentence = "";
		for (int i = 0; i < tokensOfSen.size(); i++) {
			sentence += tokensOfSen.get(i).word() + " ";
			firstChildsArray.add(new ArrayList<Integer>());
			outruleLog.println(tokensOfSen.get(i).getDepString() + "\t" + tokensOfSen.get(i).getSemanticCategory());
		}
		outruleLog.println(sentence);
		for (int i = 0; i < tokensOfSen.size(); i++) {
			int parentIndex = tokensOfSen.get(i).dep.parent;
			if (parentIndex > -1)
				firstChildsArray.get(parentIndex).add(i);
		}
		for (int i = 0; i < tokensOfSen.size(); i++) {
			if (tokensOfSen.get(i) == null)
				System.out.println("null token!:\t" + i + "\tin sentence:\t" + sentence);

			THEN thenPart = findMatchRule(tokensOfSen.get(i));
			if (thenPart == null || thenPart.No == -1) {
				// outruleLog.println(" روی توکن ***" +
				// tokensOfSen.get(i).lemma() + "*** قانونی مطابقت داده نشد");

			} else {
				SRLRelation rel = makeRelation(tokensOfSen, tokensOfSen.get(i).dep.parent, thenPart);
				if (rel != null) {
					output.add(rel);
					// outruleLog.println(rel.toString());
				}
			}
		}

		checkTense(tokensOfSen, output);
		compounding(output);
		for (SRLRelation rel : output) {
			outruleLog.println(rel.toString());
		}

		return output;

	}

	private void checkTense(List<Token> tokensOfSen, ArrayList<SRLRelation> relations) {
		for (Token token : tokensOfSen) {
			if (token.tag().equals(TagSet.VERB)) {
				int index = token.dep.index;
				boolean agent = false;
				boolean patient = false;
				for (SRLRelation rel : relations) {
					if (rel.arg1.indexSet.contains(index))
						if (rel.role.equals(RoleLabel.Agent)) {
							{
								agent = true;
							}
							if (rel.equals(RoleLabel.Patient)) {
								patient = true;
							}
						}
				}
				if (!agent) {
					if (token.getSemanticCategory().startsWith("A_") && !ActVerbEx(index).equals("Pas")) {
						SRLRelation newRel = new SRLRelation();
						newRel.arg1.indexSet = new TreeSet<>();
						newRel.arg1.indexSet.add(index);
						newRel.arg1.setLemma(token.lemma());
						newRel.role = RoleLabel.Agent;
						newRel.arg2.indexSet = new TreeSet<>();
						newRel.arg2.indexSet.add(prenounIndex);
						if (token.getTense().endsWith("N"))
							newRel.isPositive = false;
						// if (token.getTense().contains("PLUR_3"))
						// newRel.arg2.setLemma("آنها*");
						if (token.getTense().contains("PLUR_2"))
							newRel.arg2.setLemma("شما*");
						if (token.getTense().contains("PLUR_1"))
							newRel.arg2.setLemma("ما*");
						// if (token.getTense().contains("SING_3"))
						// newRel.arg2.setLemma("او*");
						if (token.getTense().contains("SING_2"))
							newRel.arg2.setLemma("تو*");
						if (token.getTense().contains("SING_1"))
							newRel.arg2.setLemma("من*");
						if (newRel.arg2.getLemma() != null)
							relations.add(newRel);
					}
				}
				if (!patient) {
					if (token.getSemanticCategory().startsWith("PAS") || ActVerbEx(index).equals("Pas")) {
						SRLRelation newRel = new SRLRelation();
						newRel.arg1.indexSet = new TreeSet<>();
						newRel.arg1.indexSet.add(index);
						newRel.role = RoleLabel.Agent;
						newRel.arg2.indexSet = new TreeSet<>();
						newRel.arg2.indexSet.add(prenounIndex);
						if (token.getTense().endsWith("N"))
							newRel.isPositive = false;
						// if (token.getTense().contains("PLUR_3"))
						// newRel.arg2.setLemma("آنها*");
						if (token.getTense().contains("PLUR_2"))
							newRel.arg2.setLemma("شما*");
						if (token.getTense().contains("PLUR_1"))
							newRel.arg2.setLemma("ما*");
						// if (token.getTense().contains("SING_3"))
						// newRel.arg2.setLemma("او*");
						if (token.getTense().contains("SING_2"))
							newRel.arg2.setLemma("تو*");
						if (token.getTense().contains("SING_1"))
							newRel.arg2.setLemma("من*");
						if (newRel.arg2.getLemma() != null)
							relations.add(newRel);
					}
				}
			}
		}

	}

	private SRLRelation makeRelation(List<Token> tokens, int parent, THEN thenPart) {
		SRLRelation rel = null;

		FunctionInRule Fun = FunctionInRule.valueOf(thenPart.function);
		TreeSet<Integer> com3 = new TreeSet<Integer>();
		TreeSet<Integer> com2 = new TreeSet<Integer>();
		ArrayList<Integer> conj = new ArrayList<Integer>();
		ArrayList<Integer> comPer = new ArrayList<Integer>();
		Token a;
		Token b;
		switch (Fun) {
		case o:
			a = tokens.get(Integer.parseInt(thenPart.arg1));
			b = tokens.get(Integer.parseInt(thenPart.arg2));
			// outruleLog.println("o: " + a.lemma() + ":" + thenPart.role + ":"
			// + b.lemma());
			rel = new SRLRelation(a, b, RoleLabel.valueOf(thenPart.role), true, thenPart.No, thenPart.confidence);
			if (a.tag().equalsIgnoreCase(TagSet.VERB) && a.getTense().endsWith("N"))
				rel.isPositive = !rel.isPositive;
			break;
        case Compound2_V:
            int a_index=Integer.parseInt(thenPart.arg1);
            int b_index=Integer.parseInt(thenPart.arg2);
            a = tokens.get(a_index);
            b = tokens.get(b_index);
            if (a_index<b_index) {
                a.setVerbBio("B_verb");
                b.setVerbBio("I_verb");
            }else{
                b.setVerbBio("B_verb");
                a.setVerbBio("I_verb");
            }
            outruleLog.println(
                    "\tCompound2:\t" + thenPart.No + "\t" + a.lemma() + "\t" + thenPart.role + "\t" + b.lemma());
            com2.add(a.dep.index);
            com2.add(b.dep.index);
            boolean added = false;
            for (TreeSet<Integer> set : compound2) {
                if (set.contains(a.dep.index) || set.contains(b.dep.index)) {
                    set.addAll(com2);
                    added = true;
                }
            }
            if (!added)
                compound2.add(com2);
            break;
		case Compound2:
			a = tokens.get(Integer.parseInt(thenPart.arg1));
			b = tokens.get(Integer.parseInt(thenPart.arg2));
			outruleLog.println(
					"\tCompound2:\t" + thenPart.No + "\t" + a.lemma() + "\t" + thenPart.role + "\t" + b.lemma());
			com2.add(a.dep.index);
			com2.add(b.dep.index);
			added = false;
			for (TreeSet<Integer> set : compound2) {
				if (set.contains(a.dep.index) || set.contains(b.dep.index)) {
					set.addAll(com2);
					added = true;
				}
			}
			if (!added)
				compound2.add(com2);
			break;
		case CopySbj:
			int sbjParent = Integer.parseInt(thenPart.arg1);
			int sbjIndex = CopySbj(sbjParent);
			if (sbjIndex < 0)
				break;
			Token aPrime = tokens.get(sbjIndex);
			b = tokens.get(Integer.parseInt(thenPart.arg2));
			RoleLabel role = findRole(Integer.parseInt(thenPart.arg1), sbjIndex, output);
			if (role != RoleLabel.O) {
				rel = new SRLRelation(b, aPrime, role, true, thenPart.No, thenPart.confidence);
				if (aPrime.tag().equalsIgnoreCase(TagSet.VERB) && aPrime.getTense().endsWith("N"))
					rel.isPositive = !rel.isPositive;
				outruleLog.println("\tCopySbj:\t" + thenPart.No + "\t" + b.lemma() + "\t" + thenPart.role + "\t"
						+ tokens.get(sbjIndex).lemma());
			}
			break;
		case Compound3_V:
		    a_index=Integer.parseInt(thenPart.arg1);
		    b_index=Integer.parseInt(thenPart.arg3);
		    int c_index=Integer.parseInt(thenPart.arg2);
			a = tokens.get(a_index);
			b = tokens.get(b_index);
			Token c = tokens.get(c_index);
			if (a_index<b_index && a_index<c_index) {
                a.setVerbBio("B_verb");
                b.setVerbBio("I_verb");
                c.setVerbBio("I_verb");
            }else
                if (b_index<a_index && b_index<c_index){
                    a.setVerbBio("I_verb");
                    b.setVerbBio("B_verb");
                    c.setVerbBio("I_verb");
                }
                else{
                    a.setVerbBio("I_verb");
                    b.setVerbBio("I_verb");
                    c.setVerbBio("B_verb");
                }


			com3.add(a.dep.index);
			com3.add(b.dep.index);
			com3.add(c.dep.index);
			compound3.add(com3);
			if (compound2.size() > 0 && compound2.get(compound2.size() - 1).contains(a.dep.index)) {
				compound2.remove(compound2.size() - 1);
			}
			outruleLog.println("\tCompound3-v:\t" + thenPart.No + "\t" + a.lemma() + "\t" + b.lemma() + "\t" + c.lemma());
			break;
		case Compound3:
			a = tokens.get(Integer.parseInt(thenPart.arg1));
			b = tokens.get(Integer.parseInt(thenPart.arg3));
			c = tokens.get(Integer.parseInt(thenPart.arg2));
			com3.add(a.dep.index);
			com3.add(b.dep.index);
			com3.add(c.dep.index);
			compound3.add(com3);
			if (compound2.size() > 0 && compound2.get(compound2.size() - 1).contains(a.dep.index)) {
				compound2.remove(compound2.size() - 1);
			}
			outruleLog.println("\tCompound3:\t" + thenPart.No + "\t" + a.lemma() + "\t" + b.lemma() + "\t" + c.lemma());
			break;
		case CompoundPrevious:
			int ind = Integer.parseInt(thenPart.arg1);
			a = tokens.get(ind);
			if (ind < 1)
				break;
			b = tokens.get(ind - 1);
			if (compoundPrevious.size() > 0
					&& compoundPrevious.get(compoundPrevious.size() - 1).contains(b.dep.index)) {
				compoundPrevious.get(compoundPrevious.size() - 1).add(a.dep.index);
			} else {
				comPer.add(b.dep.index);
				comPer.add(a.dep.index);
				compoundPrevious.add(comPer);
			}
			outruleLog
					.println("\tCompoundPrevious:\t" + thenPart.No + "\t" + a.lemma() + "\t" + "-" + "\t" + b.lemma());
			break;
		case Transfer:
			a = tokens.get(Integer.parseInt(thenPart.arg1));
			b = tokens.get(Integer.parseInt(thenPart.arg2));
			conj.add(a.dep.index);
			conj.add(b.dep.index);
			transfering(conj, thenPart.No);
			outruleLog.println("\tTransfer:\t" + thenPart.No + "\t" + a.lemma() + "\t" + "-" + "\t" + b.lemma());

			break;
		case Transfer2:
			a = tokens.get(Integer.parseInt(thenPart.arg1));
			b = tokens.get(Integer.parseInt(thenPart.arg2));
			conj.add(a.dep.index);
			conj.add(b.dep.index);
			transfering2(conj, thenPart.No);
			outruleLog.println("\tTransfer2:\t" + thenPart.No + "\t" + a.lemma() + "\t" + "-" + "\t" + b.lemma());

			break;
		case ActVerbEx:
			String out = ActVerbEx(parent);
			a = tokens.get(Integer.parseInt(thenPart.arg1));
			b = tokens.get(Integer.parseInt(thenPart.arg2));

			if (!out.equalsIgnoreCase("Skip")) {
				rel = new SRLRelation(a, b, RoleLabel.valueOf(thenPart.role), true, thenPart.No, thenPart.confidence);
				if (a.tag().equalsIgnoreCase(TagSet.VERB) && a.getTense().endsWith("N"))
					rel.isPositive = !rel.isPositive;
			} else
				break;

			if (out.equals("Act") && (b.getSemanticCategory().equals("ANM") || b.getSemanticCategory().equals("LOC"))) {
				rel.role = RoleLabel.Agent;
			} else if (out.equals("Pas")
					&& (b.getSemanticCategory().equals("ANM") || b.getSemanticCategory().equals("LOC"))) {
				rel.role = RoleLabel.Patient;
			} else if (out.equals("Act")
					&& (b.getSemanticCategory().equals("Time") || b.getSemanticCategory().equals("OBJ"))) {
				rel.role = RoleLabel.Instrument;
			} else if (out.equals("Pas")
					&& (b.getSemanticCategory().equals("Time") || b.getSemanticCategory().equals("OBJ"))) {
				rel.role = RoleLabel.Theme;
			} else if (out.equals("Act") && b.getSemanticCategory().equals("ACT")) {
				rel.role = RoleLabel.Cause;
			} else if (out.equals("Pas") && b.getSemanticCategory().equals("ACT")) {
				rel.role = RoleLabel.Theme;
			}
			break;
		default:
			outruleLog.println("\tDefault\t" + thenPart.No + "\t" + Fun);
			break;
		}
		return rel;
	}

	/**
	 * Load Semantic Role Labeling Rules from input file
	 * 
	 * @param inputStream
	 *            address of input file containing rules
	 */
	private void loadRules(InputStream inputStream) {
		InFile rulesFile = new InFile(inputStream);//
		IF ruleIF = new IF();
		THEN ruleTHEN = new THEN();
		Integer count = 1;
		try {
			ruleSet = new ArrayList<Rule>();
			// rulesFile.readLine();
			String inputRule = rulesFile.readLine();
			while (inputRule != null) {
				String[] ruleParts = inputRule.split("\t");
				// Dep POS Category PCategory FCCategory Lemma Sense Arg1 Role
				// Arg2 Function
				ruleIF = new IF();
				ruleTHEN = new THEN();
				if (ruleParts.length > 10) {
					ruleIF.dep = ruleParts[0];
					ruleIF.posTag = ruleParts[1];
					ruleIF.category = ruleParts[2];
					ruleIF.parentCategory = ruleParts[3];
					ruleIF.FCCategory = ruleParts[4];
					String[] LemmaList = ruleParts[5].split("،");
					for (int i = 0; i < LemmaList.length; i++)
						ruleIF.Lemmas.add(LemmaList[i]);
					ruleIF.Lemma = ruleParts[5];
					ruleIF.sense = ruleParts[6];
					ruleTHEN.role = ruleParts[8];
					ruleTHEN.arg1 = ruleParts[7];
					ruleTHEN.arg2 = ruleParts[9];
					ruleTHEN.function = ruleParts[10];
					ruleTHEN.confidence = ruleParts[11];

					Rule rule = new Rule(ruleIF, ruleTHEN);
					ruleSet.add(rule);
				}
				inputRule = rulesFile.readLine();
				count++;
			}
			System.err.println("Adding annotator srl ... ruleSet Size:" + ruleSet.size());
		} catch (Exception ex) {
			System.err.println("خطا در بارگذاری فایل قوانین semantic rule loading error." + "خط" + count);
			ex.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private void loadValencies(String path) {
		InFile rulesFile = new InFile(new File(path).getAbsolutePath());
		IF ruleIF = new IF();
		THEN ruleTHEN = new THEN();
		Integer count = 1;
		try {
			String inputVal = rulesFile.readLine();
			while (inputVal != null) {
				String[] lineParts = inputVal.split("\t");
				// Dep POS Category PCategory FCCategory Lemma Sense Arg1 Role
				// Arg2 Function
				ruleIF = new IF();
				ruleTHEN = new THEN();
				if (lineParts.length > 10) {
					ruleIF.dep = lineParts[0];
					ruleIF.posTag = lineParts[1];
					ruleIF.category = lineParts[2];
					ruleIF.parentCategory = lineParts[3];
					ruleIF.FCCategory = lineParts[4];
					String[] LemmaList = lineParts[5].split("|");
					for (int i = 0; i < LemmaList.length; i++)
						ruleIF.Lemmas.add(LemmaList[i]);
					ruleIF.Lemma = lineParts[5];
					ruleIF.sense = lineParts[6];
					ruleTHEN.role = lineParts[8];
					ruleTHEN.arg1 = lineParts[7];
					ruleTHEN.arg2 = lineParts[9];
					ruleTHEN.function = lineParts[10];

					Rule rule = new Rule(ruleIF, ruleTHEN);
					ruleSet.add(rule);
				}
				inputVal = rulesFile.readLine();
				count++;
			}
			System.err.println("ظرفیت افعال" + count);
		} catch (Exception ex) {
			System.err.println("خطا در بارگذاری فایل ظرفیت افعال Verb Valencies loading error." + "خط" + count);
			ex.printStackTrace();
		}
		// TODO:استفاده از ظرفیت فعل در تشخیص نوع آرگومان
	}

	private THEN findMatchRule(Token tokenLemma) {

		int index = tokenLemma.dep.index;
		String lemma = tokenLemma.lemma();
		String cPOS = tokenLemma.dep.cPos;
		String parent = tokenLemma.dep.parent + "";// parent is real integer,
													// index = string -1;
		String depRel = tokenLemma.dep.depRel;
		String category = tokenLemma.getSemanticCategory().replaceAll("B_ANM", "ANM").replaceAll("B_ORG", "ANM")
				.replaceAll("B_LOC", "LOC").replaceAll("B_PER", "ANM").replace("B_TIME", "TIME");
		String sense = tokenLemma.getSense();

		if (cPOS == null || cPOS.equalsIgnoreCase("PUNC"))
			return null;

		IF ifPart = new IF();
		ifPart.dep = depRel;
		ifPart.posTag = cPOS;
		ifPart.category = category;
		ifPart.Lemma = lemma; // use lemma for accurate match.
		ifPart.sense = sense;
		int pindex = Integer.parseInt(parent);
		if (pindex > -1) {
			ifPart.parentCategory = tokensOfSen.get(pindex).getSemanticCategory();
		} else
			ifPart.parentCategory = "-";
		ArrayList<Integer> fChilds = firstChildsArray.get(index);
		ifPart.FCCategoryList = new ArrayList<String>();
		if (fChilds != null)
			for (int j = 0; j < fChilds.size(); j++) {
				Token child = tokensOfSen.get(fChilds.get(j));
				ifPart.FCCategoryList
						.add(child.getSemanticCategory().replaceAll("B_ANM", "ANM").replaceAll("B_ORG", "ANM")
								.replaceAll("B_LOC", "LOC").replaceAll("B_PER", "ANM").replace("B_TIME", "TIME"));
			}
		THEN thenPart = new THEN();
		Iterator<Rule> it = ruleSet.iterator();
		IF ruleIF = new IF();
		THEN ruleTHEN = new THEN();
		int ruleNo = 0;
		while (it.hasNext()) {
			Rule pair = it.next();
			ruleNo++;
			ruleIF = pair.If;
			// ruleTHEN = pair.Then;
			ruleTHEN.arg1 = pair.Then.arg1;
			ruleTHEN.arg2 = pair.Then.arg2;
			ruleTHEN.arg3 = pair.Then.arg3;
			ruleTHEN.function = pair.Then.function;
			ruleTHEN.No = pair.Then.No;
			ruleTHEN.role = pair.Then.role;
			ruleTHEN.No = ruleNo;
			ruleTHEN.confidence = pair.Then.confidence;
			if ((ifPart.dep.equalsIgnoreCase(ruleIF.dep) || ruleIF.dep.equalsIgnoreCase("o"))
					&& (ruleIF.posTag.equalsIgnoreCase("o") || ifPart.posTag.equalsIgnoreCase(ruleIF.posTag))
					&& (ruleIF.category.equalsIgnoreCase("o") || ifPart.category.equalsIgnoreCase(ruleIF.category)
							|| (ifPart.posTag.equalsIgnoreCase("PREP") && !ruleIF.category.startsWith("I_")))
					&& (ifPart.parentCategory.equalsIgnoreCase(ruleIF.parentCategory)
							|| ruleIF.parentCategory.equalsIgnoreCase("o"))
					&& (ruleIF.FCCategory.equalsIgnoreCase("o") || ifPart.FCCategoryList.contains(ruleIF.FCCategory))
					&& (ruleIF.Lemma.equalsIgnoreCase("o") || ruleIF.Lemmas.contains(ifPart.Lemma))
					&& (ruleIF.sense.equalsIgnoreCase("o") || ifPart.sense.equalsIgnoreCase(ruleIF.sense))) {

				thenPart.No = ruleNo;
				thenPart.confidence = ruleTHEN.confidence;

				String fcIndex = "";

				if (fChilds.size() > 0) {
					if (ruleIF.FCCategory.equalsIgnoreCase("o")) {
						fcIndex = fChilds.get(0).toString();
					} else {// mikhahim farzandi ra enktekhab konim ke ba
							// tabagheye moshakhas shode yeki bashad
						boolean find = false;
						for (int ind = 0; ind < ifPart.FCCategoryList.size(); ind++) {
							if (ruleIF.FCCategory.equalsIgnoreCase(ifPart.FCCategoryList.get(ind))) {
								fcIndex = fChilds.get(ind).toString();
								find = true;
							}
						}
						if (!find)
							continue;
					}
				}

				if ((ifPart.posTag.equalsIgnoreCase("PREP") || ifPart.posTag.equalsIgnoreCase("CONJ"))
						&& ruleTHEN.arg2.equalsIgnoreCase("this")) {
					boolean find = false;
					if (fcIndex == "") {
						continue;
					}
					ruleTHEN.arg2 = "firstchild";
					if (ruleIF.category.equalsIgnoreCase("o")) {
						find = true;
					} else {
						for (int ind = 0; ind < ifPart.FCCategoryList.size(); ind++) {
							if (ruleIF.category.equalsIgnoreCase(ifPart.FCCategoryList.get(ind))) {
								fcIndex = fChilds.get(ind).toString();
								find = true;
							}
						}
					}
					if (!find)
						continue;

				}
				if (ruleTHEN.arg1.equalsIgnoreCase("parent"))
					thenPart.arg1 = parent;
				else if (ruleTHEN.arg1.equalsIgnoreCase("this"))
					thenPart.arg1 = index + "";
				else if (ruleTHEN.arg1.equalsIgnoreCase("firstchild")) {
					thenPart.arg1 = fcIndex;
					if (fcIndex.equalsIgnoreCase("")) {
						thenPart = new THEN();
						continue;
					}
				} else if (ruleTHEN.arg1.equalsIgnoreCase("Sbj")) {
					int a = findSbj(index);
					if (a == -1)
						continue;
					else
						thenPart.arg1 = a + "";
				} else if (ruleTHEN.arg1.equalsIgnoreCase("Obj")) {
					int a = findObj(index);
					if (a == -1)
						continue;
					else
						thenPart.arg1 = a + "";
				} else if (ruleTHEN.arg1.equalsIgnoreCase("NVE")) {
					int a = findNve(index);
					if (a == -1)
						continue;
					else
						thenPart.arg1 = a + "";
				}

				if (ruleTHEN.arg2.equalsIgnoreCase("parent"))
					thenPart.arg2 = parent;
				else if (ruleTHEN.arg2.equalsIgnoreCase("this"))
					thenPart.arg2 = index + "";
				else if (ruleTHEN.arg2.equalsIgnoreCase("firstchild")) {
					thenPart.arg2 = fcIndex;
					if (fcIndex.equalsIgnoreCase("")) {
						thenPart = new THEN();
						continue;
					}
				}

				thenPart.role = ruleTHEN.role;
				String arg3 = "";
				if (ruleTHEN.role.equalsIgnoreCase("parent"))
					arg3 = parent;
				else if (ruleTHEN.role.equalsIgnoreCase("this"))
					arg3 = index + "";
				else if (ruleTHEN.role.equalsIgnoreCase("firstchild")) {
					arg3 = fcIndex;
					if (fcIndex.equalsIgnoreCase("")) {
						thenPart = new THEN();
						continue;
					}
				}

				thenPart.arg3 = arg3;

				try {
					if (thenPart.arg1 == null || thenPart.arg2 == null || thenPart.arg3 == null)
						System.out.println("NULL Arg in thenPart!!!");
					if (thenPart.arg1.equalsIgnoreCase("") || thenPart.arg2.equalsIgnoreCase("")
							|| Integer.parseInt(thenPart.arg1) < 0 || Integer.parseInt(thenPart.arg2) < 0) {
						// outruleLog.println();
						System.out.println("bad arguments:" + thenPart.No + thenPart.arg1 + ":" + thenPart.role + ":"
								+ thenPart.arg2);
						continue;
					}
					Token a = tokensOfSen.get(Integer.parseInt(thenPart.arg1));
					if (a.tag().equalsIgnoreCase(TagSet.PREP) && thenPart.role.equalsIgnoreCase("Discription")) {
						ArrayList<Integer> aChilds = firstChildsArray.get(a.dep.index);
						if (aChilds.size() > 0) {
							thenPart.arg1 = aChilds.get(0) + "";
						}
						Token a2 = tokensOfSen.get(Integer.parseInt(thenPart.arg1));
						if (a2.tag().equalsIgnoreCase(TagSet.PREP) && thenPart.role.equalsIgnoreCase("Discription")) {
							ArrayList<Integer> a2Childs = firstChildsArray.get(a2.dep.index);
							if (a2Childs.size() > 0) {
								thenPart.arg1 = a2Childs.get(0) + "";
							}
						}
					}

				} catch (Exception ex) {
					ex.printStackTrace();
					System.err.println("آرگومان نامناسب برای استخراج نقش");
				}
				thenPart.function = ruleTHEN.function;
				// outruleLog.println("find match: " + ruleNo + ifPart.dep +
				// "\t" + ifPart.posTag + "\t" + ifPart.Lemma);
				return thenPart;
			}
		}
		return null;
	}

	private void compounding(ArrayList<SRLRelation> output) {
		Compound4(output);
		for (int i = 0; i < compound2.size(); i++) {
			for (int ind : compound2.get(i))
				for (int j = 0; j < output.size(); j++) {
					SRLRelation rel = output.get(j);
					if (rel.arg1.indexSet.size() > 0 && rel.arg1.indexSet.contains(ind)
							/*&&  !rel.role.equals(RoleLabel.Description)*/) {
						rel.arg1.indexSet.addAll(compound2.get(i));
						String newLemma = "";
						for (int k : rel.arg1.indexSet) {
							newLemma = newLemma + tokensOfSen.get(k).lemma() + " ";
						}
						rel.arg1.setLemma(newLemma.trim());
					}
					if (rel.arg2.indexSet.size() > 0 && rel.arg2.indexSet.contains(ind)) {
						rel.arg2.indexSet.addAll(compound2.get(i));
						String newLemma = "";
						for (int k : rel.arg2.indexSet) {
							newLemma = newLemma + tokensOfSen.get(k).lemma() + " ";
						}
						rel.arg2.setLemma(newLemma.trim());
					}
					output.set(j, rel);
				}
		}
		for (int i = 0; i < compound3.size(); i++) {
			for (int ind : compound3.get(i))
				for (int j = 0; j < output.size(); j++) {
					SRLRelation rel = output.get(j);
					if (rel.arg1.indexSet.size() > 0 && rel.arg1.indexSet.contains(ind)) {
						rel.arg1.indexSet.addAll(compound3.get(i));
						String newLemma = "";
						for (int k : rel.arg1.indexSet) {
							newLemma = newLemma + tokensOfSen.get(k).lemma() + " ";
						}
						rel.arg1.setLemma(newLemma.trim());
					}
					if (rel.arg2.indexSet.size() > 0 && rel.arg2.indexSet.contains(ind)) {
						rel.arg2.indexSet.addAll(compound3.get(i));
						String newLemma = "";
						for (int k : rel.arg2.indexSet) {
							newLemma = newLemma + tokensOfSen.get(k).lemma() + " ";
						}
						rel.arg2.setLemma(newLemma.trim());

					}
					output.set(j, rel);
				}
		}
		for (int i = 0; i < compoundPrevious.size(); i++) {
			for (int ind : compoundPrevious.get(i)) {
				for (int j = 0; j < output.size(); j++) {
					SRLRelation rel = output.get(j);
					if (rel.arg1.indexSet.size() > 0 && rel.arg1.indexSet.contains(ind)) {
						rel.arg1.indexSet.addAll(compoundPrevious.get(i));
						String newLemma = "";
						for (int k : rel.arg1.indexSet) {
							newLemma = newLemma + tokensOfSen.get(k).lemma() + " ";
						}
						rel.arg1.setLemma(newLemma.trim());
					}
					if (rel.arg2.indexSet.size() > 0 && rel.arg2.indexSet.contains(ind)) {
						rel.arg2.indexSet.addAll(compoundPrevious.get(i));
						String newLemma = "";
						for (int k : rel.arg2.indexSet) {
							newLemma = newLemma + tokensOfSen.get(k).lemma() + " ";
						}
						rel.arg2.setLemma(newLemma.trim());
					}
					output.set(j, rel);
				}
			}
		}

	}

	private void Compound4(ArrayList<SRLRelation> output) {
		ListIterator<SRLRelation> iterator = output.listIterator();
		while (iterator.hasNext()) {
			SRLRelation rel = iterator.next();
			boolean flag = false;
			if ((rel.getRole().equals(RoleLabel.Theme) || rel.getRole().equals(RoleLabel.EndPlace))
					&& exVerbs.ExistInCompound(rel.arg2.getLemma(), rel.arg1.getLemma())) {
				for (TreeSet<Integer> set : compound2) {
					if (set.contains(rel.arg1.firstIndex())) {
						flag = true;
					}
				}
				for (TreeSet<Integer> set : compound3) {
					if (set.contains(rel.arg1.firstIndex())) {
						flag = true;
					}
				}
				if (exVerbs.exist1(rel.arg2.getLemma(),rel.arg1.getLemma())){
					for (SRLRelation relation:output){
						if (relation.arg1.getIndexSet().contains(rel.arg1.firstIndex()) &&
								relation.getRole().equals(RoleLabel.Agent))
							relation.setRole(RoleLabel.Patient);
					}
				}
				if (!flag) {
					TreeSet<Integer> com2 = new TreeSet<Integer>();
					com2.add(rel.getArg1().firstIndex());
					com2.add(rel.getArg2().firstIndex());
					compound2.add(com2);
					iterator.remove();
				}
			}
		}
	}

	HashSet<ArrayList<String>> compoundVerbList;

	private void transfering(ArrayList<Integer> conj, int no) {
		for (int j = 0; j < output.size(); j++) {
			Token a = tokensOfSen.get(conj.get(0));
			Token b = tokensOfSen.get(conj.get(1));
			if (!a.getSemanticCategory().equals(b.getSemanticCategory())) {
				return;
			}
			SRLRelation rel = output.get(j);
			SRLRelation newRel;
			if (rel.arg2.indexSet.size() > 0 && rel.arg2.indexSet.contains(conj.get(0))
					&& !rel.role.equals(RoleLabel.Description)
					&& !(rel.arg1.firstIndex() > rel.arg2.firstIndex() && rel.arg1.firstIndex() < conj.get(1))) {
				newRel = new SRLRelation();
				newRel.arg1.indexSet = rel.arg1.indexSet;
				newRel.arg2.indexSet = new TreeSet<Integer>();
				newRel.arg2.indexSet.add(conj.get(1));
				newRel.arg1 = rel.arg1;
				newRel.arg2.setLemma(b.lemma());
				newRel.role = rel.role;
				newRel.ruleNo = no;
				output.add(newRel);
			}
			if (rel.arg1.indexSet.size() > 0 && rel.arg1.indexSet.contains(conj.get(0))
					&& !rel.role.equals(RoleLabel.Description)
					&& !(rel.arg2.firstIndex() > rel.arg1.firstIndex() && rel.arg2.firstIndex() < conj.get(1))) {
				newRel = new SRLRelation();
				newRel.arg2.indexSet = rel.arg2.indexSet;
				newRel.arg1.indexSet = new TreeSet<Integer>();
				newRel.arg1.indexSet.add(conj.get(1));
				newRel.arg2 = rel.arg2;
				newRel.arg1.setLemma(b.lemma());
				newRel.role = rel.role;
				newRel.ruleNo = no;
				output.add(newRel);
			}
		}

	}

	private void transfering2(ArrayList<Integer> conj, int no) {
		ArrayList<Integer> aChild = firstChildsArray.get(conj.get(0));
		ArrayList<Integer> bChild = firstChildsArray.get(conj.get(1));
		for (int a : aChild) {
			for (int b : bChild) {
				ArrayList<Integer> conj2 = new ArrayList<Integer>();
				conj2.add(a);
				conj2.add(b);
				transfering(conj2, no);
			}
		}
	}

	private String ActVerbEx(int pindex) {
		ArrayList<Integer> childsIndex = firstChildsArray.get(pindex);
		String out = "";
		Token pToken = tokensOfSen.get(pindex);
		if (!pToken.tag().equals(TagSet.VERB))
			return "Skip";
		for (int i : childsIndex) {
			Token token = tokensOfSen.get(i);
			if (token.dep.depRel.equalsIgnoreCase("TAM")) {
				out = "";
			}
			if (token.dep.depRel.equalsIgnoreCase("LVE"))
				out = "Pas";
			if (token.dep.depRel.equalsIgnoreCase("NVE") || token.dep.depRel.equalsIgnoreCase("VPRT")
					|| token.dep.depRel.equalsIgnoreCase("MOS") || token.dep.depRel.equalsIgnoreCase("OBJ2")) {
				if (exVerbs.exist1(token.lemma(), pToken.lemma()))
					out = "Pas";
				else if (exVerbs.exist2(token.lemma(), pToken.lemma()))
					out = "Pas";
				else if (exVerbs.ExistinNonPassive(token.lemma(), pToken.lemma()))
					out = "Act";
				else if (pToken.getSemanticCategory().equals("PAS") && pToken.lemma().equals("کردن"))
					if (exVerbs.ExistinNonPassive(token.lemma(), "شدن"))
						out = "Act";
			}
			if (token.dep.fPos.equalsIgnoreCase("OBJ"))
				out = "";

		}
		if (out.equalsIgnoreCase(""))
			out = "";
		return out;

	}

	private int findObj(int t) {
		Token token = tokensOfSen.get(t);
		int p = -1;
		p = token.getParent();
		if (p == -1)
			return -1;
		else {
			Token parent = tokensOfSen.get(p);
			if (parent.tag().equalsIgnoreCase(TagSet.VERB)) {
				ArrayList<Integer> childs = firstChildsArray.get(p);
				for (int i = 0; i < childs.size(); i++) {
					int ind = childs.get(i);
					if (tokensOfSen.get(ind).dep.depRel.equalsIgnoreCase("OBJ"))
						if (!tokensOfSen.get(ind).lemma().equals("را"))
							return childs.get(i);
						else {
							ArrayList<Integer> rachilds = firstChildsArray.get(ind);
							if (rachilds.size() > 0)
								return rachilds.get(0);
						}
				}
			}
		}
		return -1;
	}

	private int findSbj(int t) {
		Token token = tokensOfSen.get(t);
		int p = -1;
		p = token.getParent();
		if (p == -1)
			return -1;
		else {
			Token parent = tokensOfSen.get(p);
			if (parent.tag().equalsIgnoreCase(TagSet.VERB)) {
				ArrayList<Integer> childs = firstChildsArray.get(p);
				for (int i = 0; i < childs.size(); i++) {
					int ind = childs.get(i);
					if (tokensOfSen.get(ind).dep.depRel.equalsIgnoreCase("SBJ"))
						if (!tokensOfSen.get(ind).lemma().equals("را"))
							return childs.get(i);
						else {
							ArrayList<Integer> rachilds = firstChildsArray.get(ind);
							if (rachilds.size() > 0)
								return rachilds.get(0);
						}
				}
			}
		}
		return -1;
	}

	private int findNve(int t) {
		Token token = tokensOfSen.get(t);
		int p = token.getParent();
		if (p == -1)
			return -1;
		else {
			Token parent = tokensOfSen.get(p);
			if (parent.tag().equalsIgnoreCase(TagSet.VERB)) {
				ArrayList<Integer> childs = firstChildsArray.get(p);
				for (int i = 0; i < childs.size(); i++) {
					int ind = childs.get(i);
					if (tokensOfSen.get(ind).dep.depRel.equalsIgnoreCase("NVE"))
						return childs.get(i);
				}
			}
		}
		return -1;
	}

	/**
	 * 
	 * @param sbjParent
	 * @return index of new argument
	 */
	private int CopySbj(int sbjParent) {
		ArrayList<Integer> aChilds = firstChildsArray.get(sbjParent);
		if (!(aChilds.size() > 0))
			return -1;
		else {
			for (int c : aChilds) {
				Token cToken = tokensOfSen.get(c);
				if (cToken.dep.depRel.equalsIgnoreCase("SBJ"))
					return cToken.dep.index;
			}
		}
		return -2;
	}

	private RoleLabel findRole(int sbj, int obj, List<SRLRelation> rels) {
		for (SRLRelation rel : rels) {
			if (rel.arg1.indexSet.contains(sbj) && rel.arg2.indexSet.contains(obj)
					&& rel.getRole().equals(RoleLabel.Agent))
				return RoleLabel.Agent;
		}
		return RoleLabel.O;

	}

}
