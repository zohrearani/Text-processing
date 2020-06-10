package ir.malek.newsanalysis.time;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.unihd.dbs.heideltime.standalone.DocumentType;
import de.unihd.dbs.heideltime.standalone.HeidelTimeStandalone;
import de.unihd.dbs.heideltime.standalone.OutputType;
import de.unihd.dbs.heideltime.standalone.POSTagger;
import de.unihd.dbs.heideltime.standalone.exceptions.DocumentCreationTimeMissingException;
import de.unihd.dbs.uima.annotator.heideltime.resources.Language;
import ir.malek.newsanalysis.preprocess.Preprocess;
import ir.malek.newsanalysis.preprocess.Token;
import ir.malek.newsanalysis.util.io.OutFile;
import ir.malek.newsanalysis.util.news.NewsDoc;
import ir.malek.newsanalysis.util.news.XmlNews;

public class TimeNormalizerTest {

	static String readDir = "..\\semantic\\src\\main\\resources\\hamshahri";
	static String writeDir = "..\\semantic\\Output\\timeML\\";
	static Integer Error = 0;

	public static void main(String[] args) {

		Preprocess preprocess = new Preprocess();

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

		HeidelTimeStandalone heidelTime = new HeidelTimeStandalone(Language.getLanguageFromString("auto-persian"),
				DocumentType.NEWS, OutputType.TIMEML, "..\\semantic\\src\\main\\resources\\timeResources\\config.props",
				POSTagger.NO);

		OutFile outFile = new OutFile("Output\\out.txt");
		
		XmlNews hamshahriNews = new XmlNews();
		ArrayList<NewsDoc> newsDocList = hamshahriNews.readXmlFolder(readDir);

		for (NewsDoc news : newsDocList) {
			String[] date = news.getEnDate().split("-");
			Integer westernYear = Integer.parseInt(date[0]);
			Integer westernMonth = Integer.parseInt(date[1]);
			Integer westernDay = Integer.parseInt(date[1]);
			//

			String westernDateSlash = westernYear + "/" + westernMonth + "/" + westernDay;
			Date cunow;
			try {
				cunow = formatter.parse(westernDateSlash);
			} catch (ParseException e) {
				cunow = new Date();
			}

			String matn = news.getText();
			List<List<Token>> textTokens = preprocess.process(matn);

			String matn3 = "";
			for (List<Token> sentence : textTokens) {
				for (Token token : sentence) {
					matn3 = matn3.concat(token.word().replace(" ", "_") + " ");
				}
			}
			try {
				String xml = heidelTime.process(matn3, cunow);
				String TimeMLHeader = "<?xml version=\"1.0\"?>\n<TimeML>\n<DOCID>" + news.getStrID()
						+ "</DOCID>\n<DCT><TIMEX3 tid=\"t0\" type=\"DATE\" value=\"" + date + "\">" + date
						+ "</TIMEX3></DCT>\n";

				System.out.println("WRITING FILE= " + writeDir + news.getStrID());
				System.out.println(xml);
				outFile.out.println(TimeMLHeader);
				outFile.out.println(xml);
//				xml.split(" ")
			} catch (DocumentCreationTimeMissingException e) {
				e.printStackTrace();
			}

		}
	}
}