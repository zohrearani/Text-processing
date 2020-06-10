package ir.malek.newsanalysis.representation.sem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ir.malek.newsanalysis.ned.WikidataNED;
import ir.malek.newsanalysis.ner.RuleBasedNER;
import ir.malek.newsanalysis.preprocess.Preprocess;
import ir.malek.newsanalysis.preprocess.Token;
import ir.malek.newsanalysis.preprocess.depparse.DependencyParser;
import ir.malek.newsanalysis.semantic.srl.SRL;
import ir.malek.newsanalysis.semantic.srl.SRLRelation;
import ir.malek.newsanalysis.time.TimeNormalizer;
import ir.malek.newsanalysis.util.io.OutFile;
import ir.malek.newsanalysis.util.news.NewsDoc;
import ir.malek.newsanalysis.util.news.XmlNews;
import ir.mitrc.corpus.api.ApiFactory;

public class TestSem {
	static String inputText = "";
	static String time = "";
	static String id = "";
	static String ontAdrs = "";

	public static void main(String arg[]) {
		// initialize:
		initialize();

		// readDocuments():

		// XmlNews xmlNews = new XmlNews();
		// ArrayList<NewsDoc> newsDocList =
		// xmlNews.readXmlFolder("../semantic/resources/hamshahri");
		// System.out.println(newsDocList.size());
		// for (NewsDoc doc : newsDocList) {
		// List<List<Token>> docTokens = preprocess.process(doc.getText());
		// depParser.process(docTokens);
		// ner.setNER(docTokens);
		// timeNormalizer.process(docTokens, doc.getEnDate(), doc.getStrID());
		// List<List<SRLRelation>> allRelations = srl.getSrl(docTokens);
		// System.out.println(allRelations.toString());
		// newsOnt.writeFacts(docTokens, allRelations, doc.getStrID());
		//
		// }
//		inputText = "زلزله ای با قدرت 7.5 ریشتر، تهران را لرزاند.";
//		time = "1396/10/10";
//		id = "101";
//		ontAdrs = "test1.rdf";
//		newsAnalysis(inputText, time, id, ontAdrs);
//
//		inputText = "زلزله 5 ریشتری راور کرمان را لرزاند.";
//		time = "1396/10/10";
//		id = "101-2";
//		ontAdrs = "test1-2.rdf";
//		newsAnalysis(inputText, time, id, ontAdrs);
//
//		inputText = "زلزله شدیدی، تهران را لرزاند.";
//		time = "1396/10/10";
//		id = "102";
//		ontAdrs = "test2.rdf";
//		newsAnalysis(inputText, time, id, ontAdrs);
//
//		inputText = "زمین¬لرزه¬ای به قدرت 7.2 ریشتر، اسلامشهر را لرزاند.";
//		time = "1396/10/10";
//		id = "103";
//		ontAdrs = "test3.rdf";
//		newsAnalysis(inputText, time, id, ontAdrs);
//
//		inputText = "زلزله¬¬ای به قدرت 7.2 ریشتر در مختصات جغرافیایی 35.54 شمالی و 51.23 شرقی رخ داد.";
//		time = "1396/10/10";
//		id = "104";
//		ontAdrs = "test4.rdf";
//		newsAnalysis(inputText, time, id, ontAdrs);
//
//
//
//
//		inputText = ".زلزله¬ای به بزرگی 8 ریشتر در اقیانوس آرام در نزدیکی جزیره هاوایی اتفاق افتاد.";
//		time = "1396/10/10";
//		id = "105";
//		ontAdrs = "test5.rdf";
//		newsAnalysis(inputText, time, id, ontAdrs);
//
//		inputText = "زلزله¬ی 8 ریشتری در عمق 70 کیلومتری زمین و در مختصات جغرافیایی x و y روی داد.";
//		time = "1396/10/10";
//		id = "106";
//		ontAdrs = "test6.rdf";
//		newsAnalysis(inputText, time, id, ontAdrs);
//
//
//
//		inputText = "سوریه سامانه S-300 را از روسیه خریداری می کند.";
//		time = "1396/10/10";
//		id = "107";
//		ontAdrs = "test7.rdf";
//		newsAnalysis(inputText, time, id, ontAdrs);
//
//		inputText = "ترکیه قراردادی برای خرید 10 هواپیمای سوخو با روسیه امضا کرد.";
//		time = "1396/10/10";
//		id = "108";
//		ontAdrs = "test8.rdf";
//		newsAnalysis(inputText, time, id, ontAdrs);
//
//
//		inputText = "عربستان سعودی 100 میلیارد دلار تجهیزات نظامی از آمریکا خریداری می¬کند.";
//		time = "1396/10/10";
//		id = "109";
//		ontAdrs = "test9.rdf";
//		newsAnalysis(inputText, time, id, ontAdrs);
//
//		inputText = "هواپیماهای روسیه پایگاه های نظامی ترکیه را هدف قرار دادند.";
//		time = "1396/10/10";
//		id = "110";
//		ontAdrs = "test10.rdf";
//		newsAnalysis(inputText, time, id, ontAdrs);
//
//		inputText = "روسیه علیه ترکیه اعلام جنگ کرد.";
//		time = "1396/10/10";
//		id = "111";
//		ontAdrs = "test11.rdf";
//		newsAnalysis(inputText, time, id, ontAdrs);
		
		inputText = "در بمباران مناطق مرزی سوریه توسط روسیه، 4 سرباز ترکیه کشته شدند.";
		time = "1396/10/10";
		id = "112";
		ontAdrs = "test16.rdf";
		newsAnalysis(inputText, time, id, ontAdrs);
	}

	static Preprocess preprocess;
	static DependencyParser depParser;
	static RuleBasedNER ner;
	static WikidataNED ned;
	static ApiFactory api;
	//static TimeNormalizer timeNormalizer;
	static SRL srl;
	static NewsOnt newsOnt;
	static OutFile logFile;

	public static void initialize() {
		preprocess = new Preprocess();
		depParser = new DependencyParser();
		ner = new RuleBasedNER();
		ned = new WikidataNED();
		api = new ApiFactory();
		//timeNormalizer = new TimeNormalizer();
		srl = new SRL(preprocess, api);
		newsOnt = new NewsOnt(preprocess, api);
		logFile = new OutFile(new File("..//Output//logFile.txt").getAbsolutePath());
	}

	public static void newsAnalysis(String inputText, String time, String id, String ontAdrs) {
		List<List<Token>> docTokens = preprocess.process(inputText);
		depParser.process(docTokens);
		ner.setNER(docTokens);
		ned.setNED(docTokens);
		//timeNormalizer.process(docTokens, time, id);

		List<List<SRLRelation>> allRelations = srl.getSrl(docTokens);
		

		writeLog(inputText, docTokens, allRelations);
		newsOnt.writeFacts(docTokens, allRelations, id);
		newsOnt.writeOnt(ontAdrs, newsOnt.newsOnt);
	}

	private static void writeLog(String text, List<List<Token>> docTokens, List<List<SRLRelation>> allRelations) {
		logFile.println("");
		logFile.println("*** INPUT:	"+text);
		for (List<Token> tokens: docTokens){
			for (Token token:tokens){
				logFile.println(token.getDepString()+ "\t" + token.getSemanticCategory() + "\t"  + token.getNer() + "\t" + token.getNED() + "\t" + token.getTimeLabel() + "\t" + token.getNormalizedTime() + "\t" + token.getSense() );
			}
		}
		logFile.println("");
		logFile.println(allRelations.toString());
		logFile.println("");
	}

}
