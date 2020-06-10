package ir.malek.newsanalysis.representation.ont;
/*
package ir.malek.newsanalysis.prediction.ont;

import java.util.ArrayList;

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
import org.apache.jena.tdb.TDBFactory;
import org.apache.jena.util.FileManager;

public class TdbDBpedia {
	
	static final String tdbDirectory = "..\\dataset\\tdb";
	static String dbdump0 = "..//prediction//resources//faDbpedia//dbpedia_2016_10.owl";
	static final String dbdump1 = "..//prediction//resources//faDbpedia//infobox_properties_mapped_fa.ttl";
	static final String dbdump2 = "..//prediction//resources//faDbpedia//infobox_property_definitions_fa.ttl";
	static final String dbdump3 = "..//prediction//resources//faDbpedia//labels_fa.ttl";
	//static final String dbdump4 = "..//prediction//resources//faDbpedia//instance_types_en.ttl";
	//static final String dbdump5 = "..//prediction//resources//faDbpedia//interlanguage_links_fa.ttl";
	 
	static String fa_ns = "http://fa.dbpedia.org/resource/";
	static String en_ns = "http://dbpedia.org/resource/";
	static String prop_ns = "http://fa.dbpedia.org/property/";
	static String db="http://fa.dbpedia.org/resource/";
	static Dataset dataset;

	public static void main(String[] args) {
		//readFaDbpedia();
		 String label="محمد مصدق";
		// String label="مصدق";
		 //askAllRelationQuery(dataset, "db:" + "محمد_مصدق");
		 ArrayList<String> uris=askLabelQuery(label);
		 for (String uri:uris){
			 askAllRelationQuery(dataset, uri.replace(db, "db:"));
		 }
	}

	private static void readFaDbpedia() {

		dataset = TDBFactory.createDataset(tdbDirectory);
		Model tdbModel = dataset.getDefaultModel();
	
		try {
			//FileManager.get().readModel( tdbModel, dbdump0 );
			FileManager.get().readModel( tdbModel, dbdump3, "TURTLE" );
		} catch (Exception e) {
			e.printStackTrace();
		}
		tdbModel.close();
		System.out.println("read finish");
	}

	private static ArrayList<String> askLabelQuery(String label) {
		String stringQuery = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
				+ "PREFIX db: <http://fa.dbpedia.org/resource/> \n"
				+ "SELECT ?uri ?label \n"
				+ "WHERE { ?uri rdfs:label \"" + label + "\"@fa .\n" + 
				" ?uri rdfs:label ?label" + "}";
		ResultSet results = askQuery(stringQuery, dataset);
		ArrayList<String> uris = new ArrayList<>();
		while (results.hasNext()) {
			QuerySolution soln = results.nextSolution();
			String uri = soln.get("uri").toString();
			uris.add(uri);

		}

		return uris;

	}

	private static ResultSet askAllRelationQuery(Dataset dataset, String entity) {
		String queryString = "PREFIX db: <http://fa.dbpedia.org/resource/> \n"
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
		//ResultSetFormatter.out(System.out, results, query);
		return results;
	}

	private static ResultSet askQuery(OntModel onto, String entity) {
		String queryString = "PREFIX db: <http://fa.dbpedia.org/resource/> \n"
				+ "PREFIX dbp :<http://fa.dbpedia.org/property/> \n" + "SELECT ?p ?v " + "WHERE { db:" + entity
				+ " ?p  ?v . }";
		// "WHERE { ?v dbp:religion db:اسلام . }";
		Query query = QueryFactory.create(queryString);
		System.err.println("query created.");
		QueryExecution qe = QueryExecutionFactory.create(query, onto);
		ResultSet results = qe.execSelect();
		ResultSetFormatter.out(System.out, results, query);
		return results;
	}

}
*/