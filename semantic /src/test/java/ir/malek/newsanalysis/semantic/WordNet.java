package ir.malek.newsanalysis.semantic;

import com.hp.hpl.jena.ontology.*;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDFS;
import ir.malek.newsanalysis.util.io.OutFile;
import ir.mitrc.corpus.api.ApiFactory;
import ir.mitrc.corpus.api.Isynset;
import ir.mitrc.corpus.api.Synset;
import ir.mitrc.corpus.api.Term;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;


public class WordNet {
    public static void main(String [] args) {

        //getWords();
        getSynonyms();
    }
    private static void getSynonyms(){
        ApiFactory faWnApi = new ApiFactory(AppTest.class.getClassLoader().getResourceAsStream("root-ontology.owl"));
        List<String> listAllSynsets = faWnApi.listAllSynsets();
        OutFile out1=new OutFile("synonyms.txt");
        String line="";
        for(String uri:listAllSynsets) {
            Isynset synset = new Synset(uri);
            List<String> terms = synset.listWordsOfSynset();
            line = "";
            if (terms.size() > 1) {
                for (String term : terms) {
                    line += term + "*";
                }
                out1.println(line.trim());
            }

        }

        OutFile out2=new OutFile("wordLabels.txt");
        List<List<String>> wordLabels=faWnApi.listAllWordForms();
        for(List<String> list:wordLabels){
            line="";
            for(String label:list){
                line+=label+"*";
            }
            out2.println(line.trim());
        }


    }
    private static void getWords() {
        ApiFactory faWnApi = new ApiFactory(AppTest.class.getClassLoader().getResourceAsStream("root-ontology.owl"));
        List<String> words = faWnApi.listAllLabels();
        OntModel onto=faWnApi.getOntology();
        ExtendedIterator<OntProperty> allOntProperties = onto.listAllOntProperties();
        int iOntProperties=0;
        while(allOntProperties.hasNext()){
            allOntProperties.next();
            iOntProperties++;
        }
        int iObject=0;
        final ExtendedIterator<ObjectProperty> objectPropertyExtendedIterator = onto.listObjectProperties();
        while (objectPropertyExtendedIterator.hasNext()){
            iObject++;
            objectPropertyExtendedIterator.next();
        }
        int idata=0;
        final ExtendedIterator<DatatypeProperty> dataPropertyExtendedIterator = onto.listDatatypeProperties();
        while (dataPropertyExtendedIterator.hasNext()){
            idata++;
            dataPropertyExtendedIterator.next();
        }

        int indivs=0;
        final ExtendedIterator<Individual> individualExtendedIterator = onto.listIndividuals();
        while (individualExtendedIterator.hasNext()){
            individualExtendedIterator.next();
            indivs++;
        }
        ExtendedIterator<OntClass> classes = onto.listClasses();
        int iClasses=0;
        while (classes.hasNext()){
            classes.next();
            iClasses++;
        }
        ExtendedIterator<AnnotationProperty> annotationProperties = onto.listAnnotationProperties();
        int annotations=0;
        while (annotationProperties.hasNext()){
            annotationProperties.next();
            annotations++;
        }

        System.out.println("ontproperty:\t"+iOntProperties+"dataroperty:\t"+idata+"object:\t"+iObject+"\tindives:\t"+indivs+", classes:\t"+iClasses+"\tannotation:\t"+annotations);
    /*    OutFile out = new OutFile("words.txt");
        for (String label : words) {
            out.println(label.split("_")[0]);
        }*/
    }
    public static void getStatics(){

    }
}
