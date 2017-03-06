package wikidata;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.riot.RiotException;

import kb.KnowledgeGraphConstruction;

public class WikidataKG {
	
	public static Model createWikidataKG(Set<String> uris)
	{
		Model res = ModelFactory.createDefaultModel();
		String endpoint = "https://query.wikidata.org/sparql";
		String sparqlQueryString;
		Query query2;
		QueryExecution qexec;
		for (String u: uris)
		{
			sparqlQueryString = "construct {<" + u + "> ?p ?o.} where {<" + u + "> ?p ?o. FILTER (!regex(str(?o), \"statement\")) . }";
			query2 = QueryFactory.create(sparqlQueryString);
			qexec = QueryExecutionFactory.sparqlService(endpoint, query2);
			res.add(qexec.execConstruct());
		}
		
		sparqlQueryString = "select ?o where {?s ?p ?o}";
		query2 = QueryFactory.create(sparqlQueryString);
		qexec = QueryExecutionFactory.create(query2,res);
		ResultSet selRes = qexec.execSelect();
		while (selRes.hasNext())
		{
			QuerySolution qSol = selRes.next();
			RDFNode nod = qSol.get("o");
			if (nod.isResource())
			{
				String aux = nod.toString();
				res.add(KnowledgeGraphConstruction.getDBpediaWikidataMapping(nod.toString()));
			}
		}
		return res;
	}
	
	public static void main(String[] args) throws IOException, InterruptedException
	{
		//KnowledgeGraphConstruction.getDBpediaWikidataMapping("http://www.wikidata.org/entity/Q613786");
		String entity = "http://www.wikidata.org/entity/Q613786";
				
		Set<String> wikidata = new HashSet<String>();
		wikidata.add(entity);
		int count = 0;
		
		Model m = createWikidataKG(wikidata);
		FileOutputStream out;
		try {
			out = new FileOutputStream("resources/WikiData/people_wikidata.ttl");
			m.write(out, "NT");
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}
	
	/*public static void main(String[] args) throws IOException, InterruptedException
	{
		//KnowledgeGraphConstruction.getDBpediaWikidataMapping("http://www.wikidata.org/entity/Q613786");
		String listFile = "resources/lists/list_gold";
		FileReader fr = new FileReader(listFile);
		BufferedReader bf = new BufferedReader(fr);
		String line;
		Set<String> setUri1 = new HashSet<String>();
		
		while ((line = bf.readLine()) != null)
		{
			String standardURI = line.replace("<", "").replace(">", "");
			setUri1.add(standardURI);
		}
		bf.close();
		
		Set<String> wikidata = new HashSet<String>();
		int count = 0;
		for (String uri: setUri1)
		{
			System.out.println(uri);
			String wikidataUri = KnowledgeGraphConstruction.getWikidataEntity(uri);
			if (!wikidataUri.equals(""))
				wikidata.add(wikidataUri);
			if (count > 100)
				Thread.sleep(3000);
			count++;
		}
		Model m = createWikidataKG(wikidata);
		FileOutputStream out;
		try {
			out = new FileOutputStream("resources/people_wikidata.ttl");
			m.write(out, "NT");
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
	}*/

}
