package ir.malek.newsanalysis.semantic.srl;

import ir.malek.newsanalysis.ner.RuleBasedNER;
import ir.malek.newsanalysis.preprocess.Preprocess;
import ir.malek.newsanalysis.preprocess.Token;
import ir.malek.newsanalysis.preprocess.depparse.DependencyParser;
import ir.malek.newsanalysis.semantic.classification.VerbClassifier;
import ir.malek.newsanalysis.util.io.InFile;
import ir.malek.newsanalysis.util.io.OutFile;

import java.util.List;

public class Miras {
    public static void main(String[] args) {
        mirasText3();
    }

    public static void mirasText3(){
        Preprocess preprocess=new Preprocess();
        RuleBasedNER ner = new RuleBasedNER();
        DependencyParser depParser=new DependencyParser();
        SRL srl=new SRL(preprocess);
        VerbClassifier verbCls=new VerbClassifier();
        InFile inFile=new InFile("C:\\Users\\Arani\\Desktop\\MirasText\\MirasText-00");
        OutFile outFile=new OutFile("miras" + 0 + ".conll");;
        String news="";
        List<List<SRLRelation>> srls;
        int i=0;
        while ((news=inFile.readLine())!=null && i++<500) {
            if ( i % 100 == 0) {
                outFile.close();
                outFile = new OutFile("miras" + i / 100 + ".conll");
            }
            System.out.println("news " + i + "...");
            String newsText = news.split("[*][*][*]")[0];
            String conllStr = "";
            List<List<Token>> newsTokens = preprocess.process(newsText);
            depParser.process(newsTokens);
            ner.setNER(newsTokens);
            srls = srl.getSrl(newsTokens);
            for (List<Token> sentenceTokens : newsTokens) {
                for (Token token : sentenceTokens) {
                    conllStr += token.word() + "\t";
                    conllStr += token.lemma() + "\t";
                    conllStr += token.tag() + "\t";
                    conllStr += token.getNer() + "\t";
                    if (token.tag().equals("V"))
                        conllStr += token.getTense() + "_" + verbCls.getTransitivity(token.lemma())+"\t";
                    else
                        conllStr +="O\t";
                    conllStr += token.getVerbPart() + "\t";
                    conllStr += token.getVerbBio() + "\n";
                }
                conllStr += "\n";
                outFile.println(conllStr);
            }

        }
    }
}
