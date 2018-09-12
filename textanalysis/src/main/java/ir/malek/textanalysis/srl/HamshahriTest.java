package ir.malek.textanalysis.srl;

import java.util.ArrayList;
import java.util.List;

import ir.malek.newsanalysis.util.io.OutFile;
import ir.malek.newsanalysis.util.news.NewsDoc;
import ir.malek.newsanalysis.util.news.XmlNews;

public class HamshahriTest {

	public static void main(String[] args) {
		SRL srl = new SRL();
		try {
			XmlNews xmlNews = new XmlNews();
			ArrayList<NewsDoc> newsDocList = xmlNews.readXmlFolder("Input/hamshahri");
			System.out.println(newsDocList.size());
			OutFile out = new OutFile("Output\\hamshahri.out");
			for (NewsDoc doc : newsDocList) {
				if (doc != null) {
					List<List<SRLRelation>> rels = srl.getSrl(doc.getText());
					for (List<SRLRelation> relationList : rels) {
						for (SRLRelation rel : relationList) {
 							out.print(rel.toString());
							System.out.print(rel.toString());
						}
						out.println("");
						System.out.println();
					}
					System.out.println("nextDoc");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		// List<List<SRLRelation>> rels= srl.getSrl("کلاس امروز برگزار نمی
		// شود");
		// System.out.println(rels.toString());
	}
}