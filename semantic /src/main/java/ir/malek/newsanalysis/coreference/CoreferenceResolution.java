package ir.malek.newsanalysis.coreference;

import ir.malek.newsanalysis.ner.RuleBasedNER;
import ir.malek.newsanalysis.preprocess.Preprocess;
import ir.malek.newsanalysis.preprocess.Token;
import ir.malek.newsanalysis.preprocess.depparse.DependencyParser;
import ir.malek.newsanalysis.preprocess.postagger.TagSet;
import ir.malek.newsanalysis.semantic.classification.NounClassifier;
import ir.malek.newsanalysis.util.io.InFile;
import ir.mitrc.corpus.api.ApiFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.stanford.nlp.util.Pair;

public class CoreferenceResolution {
	static ArrayList<Mention> Mentions = new ArrayList<Mention>();
	static String totText="";
	static Set<String> definitePrefix = new HashSet<String>();
	static Set<String> singlePrefix = new HashSet<String>();
	static Set<String> pluralPrefix = new HashSet<String>();
	static Set<String> indefinitePrefix = new HashSet<String>();
	static Set<String> indefiniteSuffix = new HashSet<String>();
	static Set<String> nonHeadNouns = new HashSet<String>();
	static Set<String> nonMentionNouns = new HashSet<String>();
	static Set<String> singleAffixPR = new HashSet<String>();
	static Set<String> pluralAffixPR = new HashSet<String>();
	static Set<String> pluralAffix = new HashSet<String>();
	static Set<String> singlePR = new HashSet<String>();
	static Set<String> pluralPR = new HashSet<String>();
	static Set<String> reflexivePR = new HashSet<String>();
	static List<String> listAlghab = new ArrayList<String>();
	static List<String> listLoc = new ArrayList<String>();
	static List<String> listPluralPerson = new ArrayList<String>();
	static List<String> listEvent = new ArrayList<String>();
	static List<String> listTime = new ArrayList<String>();
	static List<String> listNonMentionPhrase = new ArrayList<String>();
	static List<Pair<String,String>> twoPartVerbs = new ArrayList<Pair<String,String>>();
	static List<List<String>> twoPartVerbs2 = new ArrayList<List<String>>();
	
	private int mentionNumber = 0;
	
	int merged[] = new int[1000];
	int beforeMerged[] = new int[1000];
	String trace[] = new String[1000];
	ArrayList<Mention> menCluster = new ArrayList<Mention>();
	ArrayList<ArrayList<Mention>> clusterList = new ArrayList<ArrayList<Mention>>();
	
	static Preprocess preprocess;
	static DependencyParser dependencyParser;
	RuleBasedNER ner;
	
	public CoreferenceResolution() {
	
		String line;
		InFile in=new InFile("..\\semantic\\resources\\coref\\singularPerson.txt");
		line=in.readLine();
		while(line!=null){
			listAlghab.add(line);
			line=in.readLine();
		}
		in.close();
		
		in=new InFile("..\\semantic\\resources\\coref\\pluralPerson.txt");
		line=in.readLine();
		while(line!=null){
			listPluralPerson.add(line);
			line=in.readLine();
		}
		in.close();
		
		in=new InFile("..\\semantic\\resources\\coref\\location.txt");
		line=in.readLine();
		while(line!=null){
			listLoc.add(line);
			line=in.readLine();
		}
		in.close();
		
		in=new InFile("..\\semantic\\resources\\coref\\event.txt");
		line=in.readLine();
		while(line!=null){
			listEvent.add(line);
			line=in.readLine();
		}
		in.close();
		
		in=new InFile("..\\semantic\\resources\\coref\\time.txt");
		line=in.readLine();
		while(line!=null){
			listTime.add(line);
			listNonMentionPhrase.add(line);
			line=in.readLine();
		}
		in.close();
		
		in=new InFile("..\\semantic\\resources\\coref\\nonMentionPhrase.txt");
		line=in.readLine();
		while(line!=null){
			listNonMentionPhrase.add(line);
			line=in.readLine();
		}
		in.close();
		
			
		
		in=new InFile("..\\semantic\\resources\\coref\\twoPartVerbs2.txt");
		line=in.readLine();
		
		String[] arr;
		while(line!=null){
			List<String> listLine = new ArrayList<String>();
			arr=line.split("\t");
			for(int i=0;i<3;i++)
			 listLine.add(arr[i]);
			 twoPartVerbs2.add(listLine);
			line=in.readLine();
		}
		in.close();
		definitePrefix.add("این");
		definitePrefix.add("اين");// this word() has different character with
									// respect to the previous though they seem
									// alike
		definitePrefix.add("آن");
		definitePrefix.add("چنین");
		definitePrefix.add("همین");
		definitePrefix.add("همان");
		
		indefinitePrefix.add("یک");
		indefinitePrefix.add("يك");
		indefinitePrefix.add("یکی");
		indefinitePrefix.add("دو");
		indefinitePrefix.add("دو");
		indefinitePrefix.add("سه");
		indefinitePrefix.add("چند");
		indefinitePrefix.add("چندین");

		indefiniteSuffix.add("ی");
		indefiniteSuffix.add("یی");
		indefiniteSuffix.add("هايي");
		indefiniteSuffix.add("هاي");
		indefiniteSuffix.add("هایی");
		indefiniteSuffix.add("های");
		indefiniteSuffix.add("");
		indefiniteSuffix.add("");
		indefiniteSuffix.add("");
		indefiniteSuffix.add("");

		singlePrefix.add("یک");
		singlePrefix.add("يك");
		singlePrefix.add("یکی");
		
		pluralPrefix.add("دو");
		pluralPrefix.add("سه");
		pluralPrefix.add("چند");
		pluralPrefix.add("چندین");
		pluralPrefix.add("چهار");
		pluralPrefix.add("پنج");

		nonHeadNouns.add("یک");
		nonHeadNouns.add("يك");
		nonHeadNouns.add("دو");
		nonHeadNouns.add("سه");
		nonHeadNouns.add("چند");
		nonHeadNouns.add("چندین");
		nonHeadNouns.add("تعدادی");
		nonHeadNouns.add("این");
		nonHeadNouns.add("اين");// this word has different character with
								// respect to the previous though they seem
								// alike
		nonHeadNouns.add("آن");
		nonHeadNouns.add("که");
		nonHeadNouns.add("كه");

		nonHeadNouns.add("براي");

		singleAffixPR.add("ش");
		singleAffixPR.add("اش");
		singleAffixPR.add("ت");
		singleAffixPR.add("اش");
		singleAffixPR.add("ات");
		singleAffixPR.add("م");
		singleAffixPR.add("ام");

		pluralAffixPR.add("مان");
		pluralAffixPR.add("تان");
		pluralAffixPR.add("شان");
		pluralAffixPR.add("شان");
		
		pluralAffix.add("هايي");
		pluralAffix.add("هاي");
		pluralAffix.add("هایی");
		pluralAffix.add("های");
		pluralAffix.add("مان");
		pluralAffix.add("تان");
		pluralAffix.add("شان");
		pluralAffix.add("شان");

		singlePR.add("من");
		singlePR.add("من");
		singlePR.add("من");
		singlePR.add("تو");
		singlePR.add("او");
		singlePR.add("﻿او");
		singlePR.add("وی");

		pluralPR.add("ما");
		pluralPR.add("شما");
		pluralPR.add("ایشان");
		pluralPR.add("آنها");
		pluralPR.add("آنان");

		reflexivePR.add("خودم");
		reflexivePR.add("خودش");
		reflexivePR.add("خودشان");
		reflexivePR.add("خود");
		reflexivePR.add("خویش");

		// //////////////////////
			
		
		ner=new RuleBasedNER();
	}

	
	////////////
	

	
public void mentionExtraction(List<List<Token>> docTokens) {
		
		
		dependencyParser.process(docTokens);
		ner.setNER(docTokens);

		// ///////////////////////////////////////////////////////////////////////////////////
		 int  sentenceNo = -1, tokenNumber = -2;
		Mention mention = new Mention(mentionNumber, mentionNumber, "unkwn","unkwn", "unkwn","unkwn", 0, 0);
		//----------------------------- del non mention tokens ----------------
		/*
		 * delete non mention tokens: delete verb yar along with a verb
		 */
		int find=-1,mk;
		String [] ph;
		 boolean match=true;
		for (int i = 0; i < docTokens.size(); i++)
			{ 
			totText="";
			for (Token token : docTokens.get(i))
		       totText=totText+token.word()+" ";
		//	System.out.println(totText);
	        for(String s:listNonMentionPhrase)
		    	{
		        	find=totText.indexOf(s.replace("&", " "));
                    if(find>=0)
                      {
                    	 ph=s.split(" ");
                //    	 System.out.println(s);
            	      for ( mk = 0; mk < docTokens.get(i).size() - ph.length + 1; ++mk)
                         {
                             match = true;
                             for (int jj = 0; jj < ph.length; ++jj)
                             {
                                 if (!docTokens.get(i).get(mk + jj).word().equals(ph[jj].replace("&", " ")))
                                 {
                                     match = false;
                                     break;
                                 }
                             }
                         
                             if (match) 
                                 for(int kk=mk;kk<ph.length+mk;kk++)
                                	  docTokens.get(i).get(kk).isMention=false;
                      }
                      }
		    	}
		    	
			}
		/*
		 * delete non mention tokens: delete two part verbs that didn't detected as
		 *  verbs like لو رفتن in sentence سرمربی تیم فوتبال استقلال مانع لو رفتن مبلغ قراردادش با این تیم و عکسبرداری از چک باشگاه استقلال شد
		  */
		for (int i = 0; i < docTokens.size(); i++)
		{ 
		for (int j=0; j<docTokens.get(i).size();j++)
			if(docTokens.get(i).get(j).isMention)
			for(int tvp=0;tvp<twoPartVerbs2.size();tvp++)
			if(twoPartVerbs2.get(tvp).get(0).equals(docTokens.get(i).get(j).word()) // if first part of the verb found in the currect sentense
					&& docTokens.get(i).size()>j+1 
					&& (twoPartVerbs2.get(tvp).get(1).equals(docTokens.get(i).get(j+1).word()) // if second part of the verb found in the currect sentense
							||twoPartVerbs2.get(tvp).get(2).equals(docTokens.get(i).get(j+1).word())
				||twoPartVerbs2.get(tvp).get(1).equals(docTokens.get(i).get(j+1).lemma())
				||twoPartVerbs2.get(tvp).get(2).equals(docTokens.get(i).get(j+1).lemma())))
					 {
				       docTokens.get(i).get(j).isMention=false;
				       docTokens.get(i).get(j+1).isMention=false;
				       break;
					 }
		}
		/*
		 * delete non mention tokens: delete two part verbs that didn't detected as
		 *  verbs like  مانع ... شد in sentence سرمربی تیم فوتبال استقلال مانع لو رفتن مبلغ قراردادش با این تیم و عکسبرداری از چک باشگاه استقلال شد
		  */
		boolean found=false;
		for (int i = 0; i < docTokens.size(); i++)
		{ found=false;
		for (int j=0; j<docTokens.get(i).size();j++)
			if(docTokens.get(i).get(j).tag().equals("V") /*&& j>1 && !docTokens.get(i).get(j-1).tag().equals("V")*/)
		//	if(docTokens.get(i).get(j).word().equals("دارد"))
				for(int k=j;k>0&& !found;k--)
				{
					for(int tvp=0;tvp<twoPartVerbs2.size()&& !found;tvp++)
		            	if((twoPartVerbs2.get(tvp).get(0).equals(docTokens.get(i).get(k).word())
		            			||twoPartVerbs2.get(tvp).get(0).equals(docTokens.get(i).get(k).lemma()))// if first part of the verb found in the currect sentense
					    // && docTokens.get(i).size()>j+1 
					    && (twoPartVerbs2.get(tvp).get(1).equals(docTokens.get(i).get(j).word())
					    		||twoPartVerbs2.get(tvp).get(2).equals(docTokens.get(i).get(j).word()) // if second part of the verb is sentence's verb
					    		||twoPartVerbs2.get(tvp).get(1).equals(docTokens.get(i).get(j).lemma())
		            		||twoPartVerbs2.get(tvp).get(1).equals(docTokens.get(i).get(j).lemma()))
		            			||twoPartVerbs2.get(tvp).get(2).equals(docTokens.get(i).get(j).lemma())
		            		||twoPartVerbs2.get(tvp).get(2).equals(docTokens.get(i).get(j).lemma()))
					 {
				       docTokens.get(i).get(j).isMention=false;
				       docTokens.get(i).get(k).isMention=false;
				       found=true;
					 }
				}
		}
		// ---------------------------------------------------------------
		/*
		 * verify tokens one by one and create mentions on the fly
		 */
	
		for (int i = 0; i < docTokens.size(); i++) {
			sentenceNo++;
			tokenNumber = 0;
		
			for (Token token : docTokens.get(i)) {
				if (token.word().equals("\n"))
					System.out.println("******enter*****");
				else
				{
				tokenNumber++;
		//		System.out.println(tokenNumber+ "\t" +token.word() + "\t" + token.lemma() + "\t"+ token.tag()+ "\t"+ token.getDepRel()+ "\t"+ token.getParent()+ "\t"+ token.getNer()+"\t"+ token.getNer().name().substring(1,token.getNer().name().length())+ "\t"+token.isMention+ "\t");
				// ---------------- create a mention for PR token --------
					if (token.tag().equalsIgnoreCase(TagSet.PRONOUN)){
					Mention PRmention = new Mention(++mentionNumber,mentionNumber, "PR", "single", "def","per", sentenceNo,tokenNumber-1);
					Token newToken = new Token();
					newToken.setWord(token.word());
					newToken.setTag(token.tag());
					newToken.setLemma(token.lemma());
					newToken.dep.depRel = token.dep.depRel;
					newToken.dep.index = token.dep.index;
					newToken.dep.parent = token.dep.parent;
					PRmention.tokens.add(newToken);
					Mentions.add(PRmention);
					//mentionNumber++;
					continue;
				}
				// ----------------- create a mention for NP or NE tokens ----------------
					if ((token.tag().equals(TagSet.NOUN) || token.tag().equals(TagSet.PRONOUN)|| token.tag().equals(TagSet.ADJ) || definitePrefix.contains(token.word())|| indefinitePrefix.contains(token.word()))
						&& !token.dep.depRel.equals("NVE")
						&& (!(singlePR.contains(token.word()) || pluralPR.contains(token.word())))
						&& token.isMention) {
		//			if(startTokenOfMention<0) // if this token is the first token of this mention set the beginning token of mention
		//			  startTokenOfMention=tokenNumber;
					Token newToken = new Token();
					newToken.setWord(token.word());
					newToken.setTag(token.tag());
					newToken.setNer(token.getNer());
					newToken.setLemma(token.lemma());
					newToken.dep.depRel = token.dep.depRel;
					newToken.dep.index = token.dep.index;
					newToken.dep.parent = token.dep.parent;
					mention.tokens.add(newToken);
				} else 
					if (mention.surface().length() > 0)
					{
						mention.tokenNo=tokenNumber-mention.tokens.size()-1;
						mention.senNo=sentenceNo;
						mention.type="np";
						Mentions.add(mention);
						mention = new Mention(++mentionNumber,mentionNumber, "np", "single", "def","unKwn", sentenceNo,0);
			
					}
						
						
					  
						}

			}
		}
		
			
				/*
		 * separate consequent NEs like دولت ترکیه فتح الله گولن
		 */
		
		for (int i=0;i<Mentions.size();i++ )
		{
			Mention mi = Mentions.get(i);
			for(int k=0;k<mi.tokens.size()-1;k++)
			{
				Token t=mi.tokens.get(k);
				if(t!=null && t.getNer()!=null && !t.getNer().name().equals("O"))// if k'th token is NE
					if(k+1<mi.tokens.size()-1)// if mention has next token
						if(!mi.tokens.get(k+1).getNer().name().equals("O") // if next token is NE too
								&&!mi.tokens.get(k+1).getNer().name().substring(1,3).equals(mi.tokens.get(k).getNer().name().substring(1,3)))// if next token has different type
						{
							mention = new Mention(mentionNumber,mentionNumber++, "unkwn", "unkwn", "unkwn","unkwn",mi.senNo, mi.tokenNo+ k);
							for (int j = k+1; j < mi.tokens.size(); j++)
								mention.tokens.add(mi.tokens.get(j));
							Mentions.add(mention);
							for (int j = 0; j < mention.tokens.size(); j++)
								Mentions.get(i).tokens.remove(k+1);
						
						}
			}
					
		}	
		
			
			/*
			 * ---------------- add m's sub_mentions --------------
			 * for NP mentions, by removing a word from the left of mention one by one, new mentions will be created
			 */
		//	if (!mention.type.equals("NE"))
			ArrayList<Mention> MentionsTMP = new ArrayList<Mention>();
			for (int mi=0;mi<Mentions.size();mi++)
			{
				Mention m=Mentions.get(mi);
				for (int msm = 1; msm <= m.tokens.size() - 1; msm++) {
				  if (!m.tokens.get(msm).tag().equals("ADJ")
						&&(m.tokens.get(msm-1).getNer().name().equals("O"))// if this token is not NE
				   	     && (!m.tokens.get(msm).tag().equals("PREP")
								|| definitePrefix.contains(m.tokens.get(msm).word()) 
								|| indefinitePrefix.contains(m.tokens.get(msm).word()))
						        && !(definitePrefix.contains(m.tokens.get(msm - 1).word()) 
								|| indefinitePrefix.contains(m.tokens.get(msm - 1).word()))) {
					Mention sm = new Mention(mentionNumber++,m.code, "np", "unkwn","unkwn","unkwn", m.senNo, m.tokenNo+msm);
					for (int ii = msm; ii < m.tokens.size(); ii++)
						sm.tokens.add(m.tokens.get(ii));
					MentionsTMP.add(sm);
					//mentionNumber++;
				}
			}
			}
			Mentions.addAll(MentionsTMP);
			
			/*
			 * detect and separate mentions that contain appositive structure
			 */
	
			for (int mentionNo = 0; mentionNo < Mentions.size(); mentionNo++) 
			{  Mention mm=Mentions.get(mentionNo);
				int appLoc = appositive(mm);
				int appMenSize = mm.tokens.size();
				if (appLoc > 0) {
					mention = new Mention(mentionNumber,mentionNumber, "np", "unkwn", "unkwn","per",mm.senNo, mm.tokenNo+ appLoc);
					for (int j = appLoc; j < appMenSize; j++)
						mention.tokens.add(mm.tokens.get(j));
				//	mention.corefWith = Mentions.get(mentionNo).code;
					for (int j = appLoc; j < appMenSize; j++)
						Mentions.get(mentionNo).tokens.remove(appLoc);
					Mentions.get(mentionNo).corefWith = mention.code;
					Mentions.get(mentionNo).type = "NE";
					Mentions.get(mentionNo).SemanticType = "per";
					Mentions.add(mention);
					mentionNumber++;
				}
			}
			
			
			/*
			 * detect NEs and assign their type
			 */
			for (Mention mi : Mentions)
			{
				Token t=mi.tokens.get(0);
				if(t!=null && t.getNer()!=null && t.getNer().name().equals("O"))
					{if(mi.type.equals("unkwn"))
						mi.type="np";
					}
				else
				{
					mi.type="ne";
					if(t!=null && t.getNer()!=null)
					{
					if(t.getNer().name().endsWith("LOC"))
						mi.SemanticType="loc";
					else if(t.getNer().name().endsWith("PER"))
					{
						mi.SemanticType="per";
						mi.number="single";
					}
				}
				}
			}
	
			///////////// sort mentions ////////
			Collections.sort(Mentions, new Comparator<Mention>() {

				public int compare(Mention o1, Mention o2) {
					return (o1.senNo*100+o1.tokenNo) - (o2.senNo*100+o2.tokenNo);
				}
			});
			
		
		/*
		 * reassign codes to the mentions so in ascending order of mentions order in the input document	
		 */
			int[][] changeCode = new int[Mentions.size()][2] ;
			for (int mentionNo = 0; mentionNo < Mentions.size(); mentionNo++) 
				{
					changeCode[mentionNo][0]=Mentions.get(mentionNo).code;
			        changeCode[mentionNo][1]=mentionNo;
				}
			/*
			 * replace old codes with new ones
			 */
					for (int mentionNo = 0; mentionNo < Mentions.size(); mentionNo++) 
					for(int i=0;i<Mentions.size();i++)
				      if(Mentions.get(mentionNo).code==changeCode[i][0])
				      {
					       Mentions.get(mentionNo).code=changeCode[i][1];
						       break;
				      }
				
				for (int mentionNo = 0; mentionNo < Mentions.size(); mentionNo++) 
					for(int i=0;i<Mentions.size();i++)
				      if(Mentions.get(mentionNo).lineage==changeCode[i][0])
					       {
				    	  Mentions.get(mentionNo).lineage=changeCode[i][1];
				    	  break;
					       }
			
				for (int mentionNo = 0; mentionNo < Mentions.size(); mentionNo++) 
					for(int i=0;i<Mentions.size();i++)
				      if(Mentions.get(mentionNo).corefWith==changeCode[i][0])
					     {
				    	  Mentions.get(mentionNo).corefWith=changeCode[i][1];
				    	  break;
					     }
				

			
	
		for (Mention mi : Mentions) {
			mi = anaphoricityAndNumber(mi);
		//	System.out.println(mi.surface()+"\t"+mi.number);
			mi = headfinder(mi);
	//		System.out.println(mi.surface()+"\t"+mi.number);
			mi=setNPsemCat(mi);
			
			suffixSeperator(mi);
			
		}
	
	}
///////////////////////////
	// --------------------------------------------
	public static boolean isNumeric(String str) {
		return str.matches("-?\\d+(\\.\\d+)?"); // match a number with optional
												// '-' and decimal.
	}

	// --------------- suffix seperator -------------------
	public void suffixSeperator(Mention m) {
		if (m.tokens.size() > 0)
			for (Token t : m.tokens)
				if(t.lemma().length()<t.word().length())
				m.suffix.add(t.word().substring(t.lemma().length(),t.word().length()));

	}
//////////////////
	public Mention setNPsemCat(Mention m) {
		if(m.type.equalsIgnoreCase("np"))
			if(listLoc.contains(m.head))
				m.SemanticType="loc";
			else if(listEvent.contains(m.head))
			m.SemanticType="event";
			else if(listPluralPerson.contains(m.head))
				{
				m.SemanticType="per";
				m.number="plural";
				}
			else if(listAlghab.contains(m.head))
			{
				m.SemanticType="per";
			//	m.number="single";
				}
		return m;
	}
	// ---------------------------- anaphoricity status ------------------
	public Mention anaphoricityAndNumber(Mention m) {
		String FTsuffix="";
		String LTsuffix="";
		if (m.tokens.size() > 0) {
			Token ft = m.tokens.get(0);// first token
			Token lt = m.tokens.get(m.tokens.size() - 1); // last token
			if(ft.lemma().length()<ft.word().length())
			 FTsuffix = ft.word().substring(ft.lemma().length(),ft.word().length()); // first
											// token
											// suffix
			if(lt.lemma().length()<	lt.word().length())
			 LTsuffix = lt.word().substring(lt.lemma().length(),	lt.word().length()); // last
											// token
											// suffix
			if (m.tokens.get(0).tag().equals("PR"))
				m.definite = "def";
			else if (definitePrefix.contains(m.tokens.get(0).word()))
				m.definite = "def";
			else if (indefinitePrefix.contains(m.tokens.get(0).word()))
				m.definite = "indef";
			else
				for (Token t : m.tokens)
					if (t.word().length() == t.lemma().length() + 1)
						if (t.word()
								.substring(t.word().length(),
										t.lemma().length() + 1).equals("ی"))
							m.definite = "indef";
			if (FTsuffix.equals("ی") || LTsuffix.equals("ی")) {
				m.definite = "indef";
				m.number = "single";
			}

			if (isNumeric(ft.lemma())) {
				m.definite = "indef";
				if (Double.parseDouble (ft.lemma()) > 1)
					m.number = "plural";
				else
					m.number = "single";
			}

			if (FTsuffix.equals("های") || LTsuffix.equals("هایی")) {
				m.definite = "indef";
				m.number = "plural";
			}

			if (FTsuffix.equals("ها") || FTsuffix.equals("ان")
					|| FTsuffix.equals("جات"))
				m.number = "plural";

			if (LTsuffix.equals("ها") || LTsuffix.equals("ان")
					|| LTsuffix.equals("جات"))
				m.number = "plural";

	
			if (singleAffixPR.contains(m.tokens.get(0).lemma())) {
				m.definite = "def";
				m.number = "single";
				m.type = "PR";
				m.SemanticType="per";
			}

			if (singlePR.contains(m.tokens.get(0).lemma())) {
				m.definite = "def";
				m.number = "single";
				m.type = "PR";
				m.SemanticType="per";
			}

			if (pluralPR.contains(m.tokens.get(0).lemma())) {
				m.definite = "def";
				m.number = "plural";
				m.type = "PR";
				m.SemanticType="per";
			}

			if (reflexivePR.contains(m.tokens.get(0).lemma())) {
				m.definite = "def";
				m.type = "PR";
				m.SemanticType="per";
			}

			if (singlePrefix.contains(m.tokens.get(0).lemma()))
				m.number = "single";

			if (pluralPrefix.contains(m.tokens.get(0).lemma()))
				m.number = "plural";
		}
        if(m.number.equals("unkwn"))
        	m.number = "single";
		return m;
	}

	// -------------- headfinder ---------------------
	public Mention headfinder(Mention m) {
		for (Token t : m.tokens)
			if (t.tag().startsWith("N")
					&& (!nonHeadNouns.contains(t.lemma()) 
							&& !isNumeric(t.lemma()))
					&& t.getNer().name().equals("O")
					) {
			  	 m.head = t.lemma();
				return m;
			}
		return m;
	}

	
	// ---------------------------- del prefix ------------------
	public ArrayList<Mention> delPrefix(ArrayList<Mention> Mentions) {
		for (int mentionNo = 0; mentionNo < Mentions.size(); mentionNo++)
			if (Mentions.get(mentionNo).tokens.size() == 0)
			{
				Mentions.remove(mentionNo);
				
			}
				
			else
				for (int i = 0; i < Mentions.get(mentionNo).tokens.size(); i++)
					if (Mentions.get(mentionNo).tokens.get(i).word().equals("این")
							|| Mentions.get(mentionNo).tokens.get(i).word().equals("یک"))
						for (int k = 0; k < i; k++)
							Mentions.get(mentionNo).tokens.remove(0);
		return Mentions;
	}

	// //////////////////////////////////////
	/*
	 * if a mention contains an appositive structure then separate two coreferent mentions
	 */
	int appositive(Mention m) {
		boolean a,b;
		a=false;
		b=true;
		int i = 0, len = 0;
		for (i = 0; i < m.tokens.size(); i++) {
			if (listAlghab.contains(m.tokens.get(i).word())&& m.tokens.size()>i+1)
				a=true;
			for(int p=i-1;p>=0;p--)
				if(m.tokens.get(p).getNer().name().equals("O"))
				 b=false;
			if(a && b)
				break;
			else
				len += m.tokens.get(i).word().length() + 1;
		}
		if (len < m.surface().length() && len > 3)
			return i;
		return 0;
	}
	
	// ////////////////////////////////////////////////
	public double sim(ArrayList<Token> a, ArrayList<Token> b) {
		double k = 0;
		for (int i = 0; i < a.size(); i++)
			for (int j = 0; j < b.size(); j++)
				if (a.get(i).word().equals(b.get(j).word()))
					if (listAlghab.contains(a.get(i).word()))
						k += 0.5;
					else
						k++;
		return (double) k * 2 / (a.size() + b.size());

	}


//////////////
	void setClusters() {
		int i;
		for (i = 0; i < Mentions.size(); i++)
		{
		ArrayList<Mention> mc = new ArrayList<Mention>();
           mc.add(Mentions.get(i));
	      clusterList.add(mc);
	      merged[i]=-1;
			}
		/*
		 * this loop verify to see if in mention extraction method two mentions have been linked through an appositive structure and if so set their cluster
		 */
		for (i = 0; i < Mentions.size(); i++)
		  if(Mentions.get(i).corefWith>-1)// if there were an appositive structure in mention detection phase
			if(Mentions.get(i).code<Mentions.get(i).corefWith) // to add later mention to perior one
			{  
        		clusterList.get(i).add(clusterList.get(Mentions.get(i).corefWith).get(0));
				merged[Mentions.get(i).corefWith]=firstMentionOfChain(Mentions.get(i).code);
				beforeMerged[Mentions.get(i).corefWith]=(Mentions.get(i).code);
				trace[Mentions.get(i).corefWith]="appositive 1";
			}
			else
			{  
        		clusterList.get(Mentions.get(i).corefWith).add(clusterList.get(i).get(0));
				merged[Mentions.get(i).code]=firstMentionOfChain(Mentions.get(i).corefWith);
				beforeMerged[Mentions.get(i).code]=(Mentions.get(i).corefWith);
				trace[Mentions.get(i).code]="appositive 1";
			}
				
	}
	
	//////////
	int firstMentionOfChain(int code)
	{
		while(merged[code]>=0)
			code=merged[code];
		return code;
	}
	// //////////////////////////////////////
		/*
		 * if two consecutive mentions contains an appositive structure then make them coreferent mentions
		 */
			void appositiveTwoMentions() {
				for (int i=0;i<clusterList.size();i++)
					if (listAlghab.contains(Mentions.get(i).head) && i>1) {
						if(Mentions.get(i-1).SemanticType.equalsIgnoreCase("per") 
								&& (Mentions.get(i-1).type.equals("ne")
								||Mentions.get(i-1).type.equals("PR")))//ایشان حاج آقا ذاکری پدر  حامد هاکان
							{
						clusterList.get(i-1).add(clusterList.get(i).get(0));
							merged[i]=firstMentionOfChain(Mentions.get(i-1).code);
							beforeMerged[i]=(Mentions.get(i-1).code);
							trace[i]="appositive 2";
							if(Mentions.get(i).SemanticType.equals("unKwn") && !Mentions.get(i-1).SemanticType.equals("unKwn"))
								{
								Mentions.get(i).SemanticType=Mentions.get(i-1).SemanticType;
								Mentions.get(i).number=Mentions.get(i-1).number;
								Mentions.get(i).SemanticType="per";
							
								}
							if(Mentions.get(i-1).SemanticType.equals("unKwn") && !Mentions.get(i).SemanticType.equals("unKwn"))
								{
								Mentions.get(i-1).SemanticType=Mentions.get(i).SemanticType;
								Mentions.get(i-1).number=Mentions.get(i).number;
								Mentions.get(i-1).SemanticType="per";
								clusterList.get(i).add(clusterList.get(i-1).get(0));
								}
							}
					}
				
			}
			//////////////
	/*
	 * 
	 */
	void strHeadMatchSieve(double threshold){
		int i,j,firstCluster;
		boolean haveSameLineageMention=false;
		int clusterNum=clusterList.size();
		for(i=1;i<clusterNum;i++)
			for(j=i-1;j>=0;j--)
				{	for(int ci=0;ci<clusterList.get(i).size();ci++)
					for(int cj=0;cj<clusterList.get(j).size();cj++)
					{
				     Mention m1=clusterList.get(i).get(ci);
			 	     Mention m2=clusterList.get(j).get(cj);
			 	     //-------------------------------
			 	    haveSameLineageMention=false;
			 	    for(int ti=0;ti<clusterList.get(i).size();ti++)
						for(int tj=0;tj<clusterList.get(j).size();tj++)
							if(clusterList.get(i).get(ti).lineage==clusterList.get(j).get(tj).lineage)
								haveSameLineageMention=true;
			 	     //-------------------------------
				        if((m1.type.equalsIgnoreCase("NP")|| m1.type.equalsIgnoreCase("NE"))&&
						(m2.type.equalsIgnoreCase("NP")||m2.type.equalsIgnoreCase("NE"))&&(!haveSameLineageMention)
						&&(m1.head!="")&&(m2.head!="")&&m1.head.equals(m2.head)
						&& sim(m1.tokens, m2.tokens) > threshold
						&& m1.number.equals(m2.number))
				    						
						{
				        	firstCluster=firstMentionOfChain(Mentions.get(j).code);
							for(int t=0;t<clusterList.get(i).size();t++)
								clusterList.get(firstCluster).add(clusterList.get(i).get(t));
							merged[i]=firstMentionOfChain(Mentions.get(j).code);
							beforeMerged[i]=(Mentions.get(j).code);
							trace[i]="strHeadMatchSieve";
							break;
						}
						
					}
			
		}
	} 
	/*
	 * 
	 */
	void strJustHeadMatchSieve(){
		int i,j,firstCluster=-1;
		boolean haveSameLineageMention=false;
		int clusterNum=clusterList.size();
		for(i=1;i<clusterNum;i++)
			for(j=i-1;j>=0;j--)
				{for(int ci=0;ci<clusterList.get(i).size();ci++)
					for(int cj=0;cj<clusterList.get(j).size();cj++)
					{
				     Mention m1=clusterList.get(i).get(ci);
			 	     Mention m2=clusterList.get(j).get(cj);
			 	     //-------------------------------
			 	    haveSameLineageMention=false;
			 	    for(int ti=0;ti<clusterList.get(i).size();ti++)
						for(int tj=0;tj<clusterList.get(j).size();tj++)
							if(clusterList.get(i).get(ti).lineage==clusterList.get(j).get(tj).lineage)
								haveSameLineageMention=true;
			 	     //-------------------------------
				        if((m1.type.equalsIgnoreCase("NP")|| m1.type.equalsIgnoreCase("NE"))&&
						(m2.type.equalsIgnoreCase("NP")||m2.type.equalsIgnoreCase("NE"))&&(!haveSameLineageMention)
						&&(m1.head!="")&&(m2.head!="")&&m1.head.equals(m2.head) && m1.number.equals(m2.number))
				    						
						{
				        	firstCluster=firstMentionOfChain(Mentions.get(j).code);
							for(int t=0;t<clusterList.get(i).size();t++)
								clusterList.get(firstCluster).add(clusterList.get(i).get(t));
							merged[i]=firstMentionOfChain(Mentions.get(j).code);
							beforeMerged[i]=(Mentions.get(j).code);
							trace[i]="strJustHeadMatchSieve";
							break;
						}
						
					}
			
		}
	} 
////////////
	/*
	 * 
	 */
	void strMatchSieve(double threshold){
		int i,j,firstCluster;
		boolean haveSameLineageMention=false;
		int clusterNum=clusterList.size();
		for(i=1;i<clusterNum;i++)
			for(j=i-1;j>=0;j--)
				{	for(int ci=0;ci<clusterList.get(i).size();ci++)
					for(int cj=0;cj<clusterList.get(j).size();cj++)
					{
				     Mention m1=clusterList.get(i).get(ci);
			 	     Mention m2=clusterList.get(j).get(cj);
			 	     //-------------------------------
			 	    haveSameLineageMention=false;
			 	    for(int ti=0;ti<clusterList.get(i).size();ti++)
						for(int tj=0;tj<clusterList.get(j).size();tj++)
							if(clusterList.get(i).get(ti).lineage==clusterList.get(j).get(tj).lineage)
								haveSameLineageMention=true;
			 	     //-------------------------------
				        if((m1.type.equalsIgnoreCase("NP")|| m1.type.equalsIgnoreCase("NE"))&&
						(m2.type.equalsIgnoreCase("NP")||m2.type.equalsIgnoreCase("NE"))&&(!haveSameLineageMention)
						&&(m1.head!="")&&(m2.head!="")
						&& sim(m1.tokens, m2.tokens) > threshold
						&& m1.number.equals(m2.number))
				    						
						{
				        	firstCluster=firstMentionOfChain(Mentions.get(j).code);
							for(int t=0;t<clusterList.get(i).size();t++)
								clusterList.get(firstCluster).add(clusterList.get(i).get(t));
							merged[i]=firstMentionOfChain(Mentions.get(j).code);
							beforeMerged[i]=(Mentions.get(j).code);
							trace[i]="strMatchSieve";
							break;
							}
						
					}
			
		}
	} 
//////////
	void strMatchSieveNE(double threshold){
		int i,j,firstCluster;
		boolean haveSameLineageMention=false;
		int clusterNum=clusterList.size();
		for(i=1;i<clusterNum;i++)
			for(j=i-1;j>=0;j--)
				{for(int ci=0;ci<clusterList.get(i).size();ci++)
					for(int cj=0;cj<clusterList.get(j).size();cj++)
					{
				     Mention m1=clusterList.get(i).get(ci);
			 	     Mention m2=clusterList.get(j).get(cj);
			 	     //-------------------------------
			 	    haveSameLineageMention=false;
			 	    for(int ti=0;ti<clusterList.get(i).size();ti++)
						for(int tj=0;tj<clusterList.get(j).size();tj++)
							if(clusterList.get(i).get(ti).lineage==clusterList.get(j).get(tj).lineage)
								haveSameLineageMention=true;
			 	     //-------------------------------
				        if( (m1.type.equalsIgnoreCase("NE")|| m2.type.equalsIgnoreCase("NE"))
				        		&&(m1.SemanticType.equals(m2.SemanticType)||m1.SemanticType.equalsIgnoreCase("unKwn")||m2.SemanticType.equalsIgnoreCase("unKwn"))&&!haveSameLineageMention
						&& sim(m1.tokens, m2.tokens) > threshold
						&& m1.number.equals(m2.number))
				    						
						{
				        	firstCluster=firstMentionOfChain(Mentions.get(j).code);
							for(int t=0;t<clusterList.get(i).size();t++)
								clusterList.get(firstCluster).add(clusterList.get(i).get(t));
							merged[i]=firstCluster;
							beforeMerged[i]=firstCluster;
							trace[i]="strMatchSieveNE";
							break;
							}
						
					}
			
		}
	} 
//////////
	
	void strHeadSieve(double threshold) {
		int i, j;
		for (i = 0; i < Mentions.size(); i++)
			for (j = i - 1; j >= 0; j--) // for each antecedent of it
			{
				Mention m1 = Mentions.get(i);
				Mention m2 = Mentions.get(j);
				if (m1.lineage != m2.lineage)
					if (m1.head.length() > 0)
						if (m1.head.equals(m2.head))
							m1.corefWith = m2.code;
			}

	}
//////////////////////////////	
	/*
	 * this is the main method of this coreference system. it extracts mentions and their features
	 */
	
	public void showMentions() {
		System.out.println("surface \t head \t code \t lineage\t suffix \t type \t No \t definite \t SemType \t senNo \t tokenNo \t salience \t coref \t depLink \t merged");

		for (int mi=0;mi<Mentions.size();mi++)
		{Mention mm=Mentions.get(mi);
			if (mm.surface().trim().length() > 0) {
				System.out.println( mm.surface().trim() + "\t" + mm.head
						 + "\t" + mm.code+ "\t" + mm.lineage + "\t" + mm.suffix + "\t" + mm.type
						+ "\t" + mm.number + "\t" + mm.definite + "\t"
							+ mm.SemanticType + "\t" + mm.senNo + "\t" + mm.tokenNo
							+ "\t" + mm.salience+ "\t" + mm.corefWith+ "\t" + mm.tokens.get(0).getDepRel()+ "\t" + merged[mi]+"\t"+ beforeMerged[mi]+"\t" + trace[mi]);
			}
		}
	}
///////////////////////////
	public void setSalience() {
	
		for (Mention mm : Mentions)
           for(int j=0;j<mm.tokens.size();j++)
				if (mm.tokens.get(j).getDepRel().equalsIgnoreCase("sbj")) {
			       mm.salience+=10;
			}
	}

////////////////////////////
	/*
	 * need a ranking model to rank the pronoun's antecedents based on their salience and choose the most salient one between them
	 */
public void pronounSieve() {
	int firstCluster;
for (int i = 0; i < Mentions.size(); i++) 
if(Mentions.get(i).type.equals("PR"))
	for (int k = i-1; k>=0;k--) 
	 if (Mentions.get(k).SemanticType.equalsIgnoreCase(Mentions.get(i).SemanticType) 
			 && Mentions.get(k).number.equalsIgnoreCase(Mentions.get(i).number)
			 && !Mentions.get(k).type.equalsIgnoreCase("PR"))
	 {  
		 firstCluster=firstMentionOfChain(Mentions.get(k).code);
		 Mentions.get(i).corefWith = Mentions.get(k).code;
		 for(int t=0;t<clusterList.get(i).size();t++)
				clusterList.get(firstCluster).add(clusterList.get(i).get(t));
			merged[i]=firstMentionOfChain(Mentions.get(k).code);
			break;
		
	   
	}

}
/////////////////
/*
 * delete non-mention tokens
 */

////////////////////////////
public void eventSieve() {
for (int i = 0; i < Mentions.size(); i++) 
if((Mentions.get(i).head.equals("اتفاق")||Mentions.get(i).head.equals("حادثه")||Mentions.get(i).head.equals("رویداد"))
		&& Mentions.get(i).definite.equals("def"))
for (int k = i-1; k>=0;k--) 
if (Mentions.get(k).SemanticType.equalsIgnoreCase("event") 
&& Mentions.get(k).number.equalsIgnoreCase(Mentions.get(i).number)
&& !Mentions.get(k).type.equalsIgnoreCase("PR"))
{  
Mentions.get(i).corefWith = Mentions.get(k).code;
break;
}

}

//////////

public void showClusters2(List<List<Token>> docTokens)
{int j=0;
  Set<String> myset = new HashSet<String>();
for (int i = 0; i < Mentions.size(); i++)
{
	myset.add("-1");
	
if(!myset.contains(merged[i]+""))
{
System.out.print(merged[i]+" {"+Mentions.get(merged[i]).surface()+" "+Mentions.get(merged[i]).code);
docTokens.get(Mentions.get(merged[i]).senNo).get(Mentions.get(merged[i]).tokenNo).setCoref("("+merged[i]);
if(Mentions.get(merged[i]).tokens.size()>1)
  docTokens.get(Mentions.get(merged[i]).senNo).get(Mentions.get(merged[i]).tokenNo+Mentions.get(merged[i]).tokens.size()-1).setCoref(merged[i]+")");
else
	   docTokens.get(Mentions.get(merged[i]).senNo).get(Mentions.get(merged[i]).tokenNo+Mentions.get(merged[i]).tokens.size()-1).setCoref(")");

for(j=0;j<Mentions.size();j++)
	if(merged[j]==merged[i])
	{
  System.out.print(", "+Mentions.get(j).surface()+" "+Mentions.get(j).code);
   docTokens.get(Mentions.get(j).senNo).get(Mentions.get(j).tokenNo).setCoref("("+merged[i]);
   if(Mentions.get(j).tokens.size()>1)
     docTokens.get(Mentions.get(j).senNo).get(Mentions.get(j).tokenNo+Mentions.get(j).tokens.size()-1).setCoref(merged[i]+")");
   else
	   docTokens.get(Mentions.get(j).senNo).get(Mentions.get(j).tokenNo+Mentions.get(j).tokens.size()-1).setCoref(")");
	}
System.out.println("}");
myset.add(merged[i]+"");
}
}

}
	// ---------------------------- main ------------------
public void setCoref(List<List<Token>> docTokens) {
	mentionExtraction(docTokens);
	setClusters();
	appositiveTwoMentions();
	strHeadMatchSieve(.5);
	strMatchSieve(.9);
	strMatchSieveNE(.5);
	strJustHeadMatchSieve();
	pronounSieve();
	showMentions();
	showClusters2(docTokens);
}

	public static void main(String[] args) throws Exception {

		CoreferenceResolution CR = new CoreferenceResolution();
		//String inputText = InFile.readFileText("..\\semantic\\resources\\coref\\sampleInput\\shahram.txt");
		String inputText = InFile.readFileText("C:\\Hamshahri 2007\\851011\\HAM2-851011-002.txt");
		preprocess = new Preprocess();
		dependencyParser = new DependencyParser();
		List<List<Token>> docTokens = preprocess.process(inputText);
		CR.setCoref(docTokens);
		for (int i = 0; i < docTokens.size(); i++)
			 for (Token token : docTokens.get(i))
				 if(token.getCoref()=="")
					 System.out.println(token.word()+"\t"+"-");
				 else
				 System.out.println(token.word()+"\t"+token.getCoref());
	}
}
