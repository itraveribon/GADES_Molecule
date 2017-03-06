package datacleaning;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class InfoBoxPropertiesFixing {

	public static void main(String[] args) throws IOException {
		Model m = ModelFactory.createDefaultModel();
		m.read("resources/infobox_property_definitions_en.ttl");
		
		String sparqlQueryString = "construct {?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#ObjectProperty> .} "
				+ "WHERE {?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Property> .}";
		
		Query query2 = QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(query2, m);
		m.add(qexec.execConstruct());
		FileOutputStream out;
		try {
			out = new FileOutputStream("resources/infobox_property_definitions_en_owl.ttl");
			m.write(out, "NT");
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
