package ir.malek.newsanalysis.representation.ont;

import java.util.ArrayList;
import java.util.TreeSet;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.util.FileManager;

public class TdbWikidata {

	static final String wikidataFolder = "C:/Users/Administrator/Desktop/from arani/961024/newsAnalysis/prediction/resources/Wikidata-fa/";
	static final String tdbDirectory = wikidataFolder + "dataset/";
	static final String dbdump0 = wikidataFolder + "wikidata-taxonomy.nt";
	static final String dbdump1 = wikidataFolder + "wikidata-properties.nt";
	static final String dbdump2 = wikidataFolder + "wikidata-property-taxonomy.nt";
	static final String dbdump4 = wikidataFolder + "wikidata-instances.nt.head";
	static final String dbdump5 = wikidataFolder + "wikidata-instances.nt.fa";
	static final String dbdump6 = wikidataFolder + "wikidata-simple-statements.nt.head";
	static final String dbdump7 = wikidataFolder + "wikidata-simple-statements.nt.fa";
	static final String dbdump8 = wikidataFolder + "wikidata-terms.nt.head";
	static final String dbdump9 = wikidataFolder + "wikidata-terms.nt.fa.remUselessDesc";

	static String baseUriEntity = "http://www.wikidata.org/entity/";
	static String baseUri = "http://www.wikidata.org/ontology#";
	static Dataset dataset;

	public static void main(String[] args) {
		readWikiData();
		// ListAllObjectProperty();
		askQuery();
	}

	public static void ListAllObjectProperty() {
		dataset = TDBFactory.createDataset(tdbDirectory);
		Model model = dataset.getDefaultModel();
		//OntModel ontModel=(OntModel) model;
		StmtIterator it=model.listStatements();
		//ExtendedIterator<ObjectProperty> it=ontModel.listObjectProperties();
		
		TreeSet<String> set = new TreeSet<String>();
		int counter=0;
		while (it.hasNext()) {
			try {
				counter++;
				//set.add(it.next().getPredicate());
				
			} catch (Exception e) {
				System.out.println(counter);
			}
		}
		while (it.hasNext()) {
			System.out.println(it.next());
		}
		dataset.close();
	}

	@SuppressWarnings("unused") //we have read data and indexed it in tdbdataset.
	private static void readWikiData() {

		dataset = TDBFactory.createDataset(tdbDirectory);
		Model tdbModel = dataset.getDefaultModel();

		try {
			FileManager.get().readModel(tdbModel, dbdump9, "TURTLE");
		} catch (Exception e) {
			e.printStackTrace();
		}
		tdbModel.close();
		System.out.println("read finish");
	}

	

	private static ResultSet askAllRelationQuery(Dataset dataset, String entity) {
		String queryString = "PREFIX wikientity: <" + baseUri + "> \n"
				+ "PREFIX dbp:<http://fa.dbpedia.org/property/> \n" + "SELECT ?p ?v " + "WHERE { " + entity
				+ " ?p  ?v . }";
		ResultSet results = askQuery(queryString, dataset);
		return results;
	}

	public static ResultSet askQuery(String queryString, Dataset dataset) {
		dataset = TDBFactory.createDataset(tdbDirectory);
		Model tdbModel = dataset.getDefaultModel();
		Query query = QueryFactory.create(queryString);
		System.err.println("query created.");
		QueryExecution qe = QueryExecutionFactory.create(query, tdbModel);
		ResultSet results = qe.execSelect();
		// ResultSetFormatter.out(System.out, results, query);
		return results;
	}

	private static ResultSet askQuery(OntModel onto, String entity) {
		String queryString = "SELECT * WHERE { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } }";
		// "WHERE { ?v dbp:religion db:اسلام . }";
		Query query = QueryFactory.create(queryString);
		System.err.println("query created.");
		QueryExecution qe = QueryExecutionFactory.create(query, onto);
		ResultSet results = qe.execSelect();
		ResultSetFormatter.out(System.out, results, query);
		return results;
	}

	private static void askQuery() {
		String queryString = "SELECT * WHERE { { ?s ?p ?o } UNION { GRAPH ?g { ?s ?p ?o } } }";
		dataset = TDBFactory.createDataset(tdbDirectory);
		Model tdbModel = dataset.getDefaultModel();
		Query query = QueryFactory.create(queryString);
		System.err.println("query created.");
		QueryExecution qe = QueryExecutionFactory.create(query, tdbModel);
		ResultSet results = qe.execSelect();
	//	ResultSetFormatter.out(System.out, results, query);
	}
}
