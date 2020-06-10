package ir.malek.newsanalysis.time;

import ir.malek.newsanalysis.preprocess.Token;
import ir.malek.newsanalysis.util.enums.TimeLabel;

import java.io.OutputStream;
import java.io.PrintStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.unihd.dbs.heideltime.standalone.DocumentType;
import de.unihd.dbs.heideltime.standalone.HeidelTimeStandalone;
import de.unihd.dbs.heideltime.standalone.OutputType;
import de.unihd.dbs.heideltime.standalone.POSTagger;
import de.unihd.dbs.heideltime.standalone.exceptions.DocumentCreationTimeMissingException;
import de.unihd.dbs.uima.annotator.heideltime.resources.Language;

/**
 * <h1>Temporal Expression Tagging</h1> This class find Temporal mentions in the
 * text and normalize them to a standard time. we use
 * <a href="https://github.com/HeidelTime/heideltime">HeidelTime</a> Library and
 * TimeMX output format. using TIMEX3 tags with normalization attributes, most
 * importantly type ({@linkplain ir.malek.newsanlysis.util.TimeType TimeType}
 * date, time, duration, set) and value to represent the main semantics of
 * expressions in standard format.
 * 
 * 
 * @author Arani
 *
 */
public class TimeNormalizer {
	HeidelTimeStandalone heidelTime;
	SimpleDateFormat formatter;
	PrintStream dummyStream = new PrintStream(new OutputStream() {
		public void write(int b) {
			// NO-OP
			// we write this method to prevent the heidelTime from printing
			// anything
		}
	});

	public TimeNormalizer() {
		System.err.println("Adding annotator time");
		//System.getProperty()
		String configPath = "semantic\\src\\main\\resources\\timeResources\\config.props"; // persian
		// instead
		// auto-persian????
		System.setOut(dummyStream);
		heidelTime = new HeidelTimeStandalone(Language.getLanguageFromString("auto-persian"), DocumentType.NEWS,
				OutputType.TIMEML, configPath, POSTagger.NO);
		formatter = new SimpleDateFormat("yyyy/MM/dd");
		System.setOut(System.out);
	}

	public void process(List<List<Token>> tokens, String baseTime, String newsID) { // DateTime
																					// instead
																					// of
																					// date????
		String[] date = baseTime.split("/");
		String westernDateSlash = date[0] + "/" + date[1] + "/" + date[2];
		Date cunow;
		try {
			cunow = formatter.parse(westernDateSlash);
		} catch (ParseException e) {
			cunow = new Date();
		}
		String matn = "";
		for (List<Token> sentence : tokens) {
			for (Token token : sentence) {
				matn = matn.concat(token.word().replace(" ", "_") + " ");
			}
		}
		try {
			String xml = heidelTime.process(matn, cunow);
			setTime(tokens, xml);
			// String TimeMLHeader = "<?xml
			// version=\"1.0\"?>\n<TimeML>\n<DOCID>" + newsID +
			// "</DOCID>\n<DCT><TIMEX3 tid=\"t0\" type=\"DATE\" value=\"" + date
			// + "\">" + date + "</TIMEX3></DCT>\n";
		} catch (DocumentCreationTimeMissingException e) {
			e.printStackTrace();
		}
	}

	private void setTime(List<List<Token>> tokens, String xml) {
		String[] words = xml.replace("<", " <").replace(">", "> ").replace("  ", " ").split(" ");
		int k = 0;
		String value = "";
		String type = "";
		for (int i = 0; i < tokens.size(); i++) {
			List<Token> sentence = tokens.get(i);
			for (int j = 0; j < sentence.size(); j++) {
				Token token = sentence.get(j);
				if (words[k].startsWith("<")) {

					while (words[k].endsWith(">")) {
						if (words[k].startsWith("type") || words[k].startsWith("Type"))
							type = words[k].split("=")[1];
						if (words[k].startsWith("value") || words[k].startsWith("Value"))
							value = words[k].split("=")[1];

						k++;
					}
					token.setTimeLabel(TimeLabel.B_Time);
					String normalizedTime = type + "_" + value;
					token.setNormalizedTime(normalizedTime);

					while (!words[k].startsWith("<")) {
						token.setTimeLabel(TimeLabel.I_Time);
						j++;
						if (j >= sentence.size())
							break;
					}
					while (words[k].endsWith(">")) {
						k++;
					}
				} else
					token.setTimeLabel(TimeLabel.O);

			}
		}
	}

}
