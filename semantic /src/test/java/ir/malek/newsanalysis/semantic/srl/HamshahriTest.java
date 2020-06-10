package ir.malek.newsanalysis.semantic.srl;

import java.util.ArrayList;
import java.util.List;

import ir.malek.newsanalysis.util.io.OutFile;
import ir.malek.newsanalysis.util.news.NewsDoc;
import ir.malek.newsanalysis.util.news.XmlNews;


public class HamshahriTest {

	
	public static void main(String [] args){
		SRL srl=new SRL();
		int count=0;
		try{
		XmlNews xmlNews=new XmlNews();
		ArrayList<NewsDoc> newsDocList=xmlNews.readXmlFolder("/home/arani/Documents/corpus/Corpus_utdbrgham2/2007/test");
		System.out.println(newsDocList.size());
		OutFile out=new OutFile("hamshahri.out");
		for (NewsDoc doc:newsDocList){
			if (doc!=null ){
			List<List<SRLRelation>> rels=srl.getSrl(doc.getText());
			out.print(rels.toString());
			System.out.println(doc.getStrID());
			}
			if (count++>10)
				break;
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		//List<List<SRLRelation>> rels= srl.getSrl("کلاس امروز برگزار نمی شود");
		//System.out.println(rels.toString());
	}
}