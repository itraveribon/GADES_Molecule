package kb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.tdb.TDBFactory;




public class DBPediaExtractor {

	//public static String endpoint = "http://dbpedia.org/sparql";
	private static Dataset endpoint = TDBFactory.createDataset("/media/traverso/TOSHIBA EXT/SemanticSimilarity/triple_data/knowledge_bases/dbpedia/tdb_dbpedia");
	private static Set<String> processedCat = new HashSet<String>();
	private static Set<String> processedRes = new HashSet<String>();
	public static final String ANCESTOR = "http://www.w3.org/2004/02/skos/core#broader";//"http://dbpedia_hierarchy.org/has_ancestor";
	
	public static String getLabel(String wikiURL)
	{
		String sparqlQueryString = "select distinct ?o where {<" + wikiURL + "> <http://www.w3.org/2000/01/rdf-schema#label> ?o . FILTER(lang(?o) = 'en'" + /*" || lang(?o) = 'es' || lang(?o) = 'de'" +*/ ")}";
		
		Query query2 = QueryFactory.create(sparqlQueryString);
		//QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query2);
		QueryExecution qexec = QueryExecutionFactory.create(query2, endpoint);
		ResultSet res = qexec.execSelect();
		Set<String> synsets = new HashSet<String>();
		while (res.hasNext())
		{	
			QuerySolution row = res.nextSolution();
			synsets.add(row.getLiteral("o").getLexicalForm());
		}
		if (synsets.isEmpty())
			return "";
		String label = "";
		for (String elem: synsets)
		{
			label += elem + " ";
		}
		return label;
	}
	
	public static String getURISameAs(String uri, String prefix)
	{
		String sparqlQueryString = "select distinct * where {<" + uri + "> <http://www.w3.org/2002/07/owl#sameAs> ?s. FILTER regex(str(?s), \"" + prefix + "\")}";
		
		Query query2 = QueryFactory.create(sparqlQueryString);
		//QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query2);
		QueryExecution qexec = QueryExecutionFactory.create(query2, endpoint);
		
		ResultSet res = qexec.execSelect();
		Set<String> synsets = new HashSet<String>();
		while (res.hasNext())
		{	
			QuerySolution row = res.nextSolution();
			String url = row.getResource("s").getURI();
			synsets.add(url);
		}
		if (synsets.isEmpty())
			return "";
		return synsets.iterator().next();
	}
	
	public static String getEntryID(String wikiURL)
	{
		String sparqlQueryString = "select distinct * where {<" + wikiURL + "> <http://xmlns.com/foaf/0.1/primaryTopic> ?o}";
		
		Query query2 = QueryFactory.create(sparqlQueryString);
		//QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query2);
		QueryExecution qexec = QueryExecutionFactory.create(query2, endpoint);
		ResultSet res = qexec.execSelect();
		Set<String> synsets = new HashSet<String>();
		while (res.hasNext())
		{	
			QuerySolution row = res.nextSolution();
			synsets.add(row.getResource("o").getURI());
		}
		if (synsets.isEmpty())
			return "";
		return synsets.iterator().next();
	}
	
	private Model fullModel;
	private Model dbpedia;
	private Model hierarchy;
	
	public static void joinDatasets(Dataset dbp, Dataset hier)
	{
		dbp.begin(ReadWrite.WRITE);
		dbp.getDefaultModel().add(hier.getDefaultModel());
		dbp.commit();
		dbp.close();
	}
	
	public DBPediaExtractor(Dataset dbp)
	{
		/*dbpedia = dbp.getDefaultModel();
		hierarchy = ModelFactory.createDefaultModel();
		hierarchy.add(hier.getDefaultModel());*/
		fullModel = dbp.getDefaultModel();
		//fullModel = dbp.getDefaultModel().union(hier.getDefaultModel());
		//fullModel.add(hier.getDefaultModel());
	}
	
	public void iterateTraversalFunction(Set<String> categories, Model mod){
		Set<String> newCategories = new HashSet<String>();
		for (String startCategory: categories)
		{
			if (!processedCat.contains(startCategory))
			{
				String sparqlQueryString = "construct {<" + startCategory + "> <" + ANCESTOR + "> ?o. ?o <http://dbpedia_hierarchy.org/has_depth> ?depth} where {<" + startCategory + "> <" + ANCESTOR + "> ?o. ?o <http://dbpedia_hierarchy.org/has_depth> ?depth}";
				//String sparqlQueryString = "select distinct ?o where {<" + startCategory + "> <" + ANCESTOR + "> ?o}";
				
				Query query2 = QueryFactory.create(sparqlQueryString);
				QueryExecution qexec = QueryExecutionFactory.create(query2, fullModel);//.sparqlService(endpoint, query2);
				mod.add(qexec.execConstruct());
				/*ResultSet rSet = qexec.execSelect();
			
				while (rSet.hasNext())
				{
					QuerySolution qSol = rSet.next();
					RDFNode nod = qSol.get("o");
					String newUri = nod.asResource().getURI();
					newCategories.add(newUri);
					mod.add(mod.getResource(startCategory), mod.getProperty("http://www.w3.org/2004/02/skos/core#broader"), nod);
				}
				mod.add(mod.getResource(startCategory), mod.getProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"), mod.getResource("http://www.w3.org/2004/02/skos/core#Concept"));
				qexec.close();
				sparqlQueryString = "construct {<"  + startCategory + "> <http://dbpedia_hierarchy.org/has_depth> ?o} where {<"  + startCategory + "> <http://dbpedia_hierarchy.org/has_depth> ?o}";
				query2 = QueryFactory.create(sparqlQueryString);
				qexec = QueryExecutionFactory.create(query2, fullModel);
				mod.add(qexec.execConstruct());
				qexec.close();*/
			}
		}
		/*processedCat.addAll(categories);
		newCategories.removeAll(processedCat);
		if (!newCategories.isEmpty())
			iterateTraversalFunction(newCategories, mod);*/
	}
	
	public Model getCategories(String url)
	{
		Model res = ModelFactory.createDefaultModel();
		Set<String> categories = new HashSet<String>();
		if (!url.toLowerCase().contains("category:"))
		{	
			String sparqlQueryString = "construct {<" + url + "> <http://purl.org/dc/terms/subject> ?o. "
					+ 								"?o <http://www.w3.org/2004/02/skos/core#broader> ?o1. "
					+ 								"?o <http://dbpedia_hierarchy.org/has_depth> ?depth.} where {"
					+ 								"<" + url + "> <http://purl.org/dc/terms/subject> ?o. ?o <" + ANCESTOR + "> ?o1."
					+ 								" ?o <http://dbpedia_hierarchy.org/has_depth> ?depth.}";
			
			Query query2 = QueryFactory.create(sparqlQueryString);
			
			//QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query2);
			QueryExecution qexec = QueryExecutionFactory.create(query2, fullModel);
			
			try{
				res = qexec.execConstruct();
			}catch (RiotException exc)
			{
				/*QueryEngineHTTP qhttp = new QueryEngineHTTP(endpoint, query2);
				qhttp.setModelContentType(WebContent.contentTypeRDFXML);
				res = qhttp.execConstruct();
				qhttp.close();*/
			}
			/*Property p = res.getProperty("http://purl.org/dc/terms/subject");
			StmtIterator it = res.listStatements(null, p, (RDFNode)null);
			
			while (it.hasNext())
			{
				Statement st = it.next();
				String urlCat = st.getObject().asResource().getURI();
				categories.add(urlCat);
			}*/
			return res;
		}
		else
		{
			categories.add(url);
		}
		iterateTraversalFunction(categories, res);
		return res;
	}
	
	private Model getOneHopNeighborhood(String url)
	{
		String sparqlQueryString = "construct {<" + url + "> ?p ?o} where {<" + url + "> ?p ?o. FILTER regex(str(?p), \"http://dbpedia.org/ontology/\") .}";
		
		Query query2 = QueryFactory.create(sparqlQueryString);
		//QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query2);
		QueryExecution qexec = QueryExecutionFactory.create(query2, fullModel);
		Model res = ModelFactory.createDefaultModel();
		try{
			res = qexec.execConstruct();
		}catch (RiotException exc)
		{
			/*QueryEngineHTTP qhttp = new QueryEngineHTTP(endpoint, query2);
			qhttp.setModelContentType(WebContent.contentTypeRDFXML);
			res = qhttp.execConstruct();
			qhttp.close();*/
		}
		
		sparqlQueryString = "select ?o where {<" + url + "> ?p ?o. FILTER regex(str(?p), \"http://dbpedia.org/ontology/\") .}";
		
		query2 = QueryFactory.create(sparqlQueryString);
		//qexec = QueryExecutionFactory.sparqlService(endpoint, query2);
		qexec = QueryExecutionFactory.create(query2, fullModel);
		ResultSet resSet = null;
		try{
			resSet = qexec.execSelect();
		}catch (RiotException exc)
		{
			QueryEngineHTTP qhttp = new QueryEngineHTTP(endpoint.toString(), query2);
			qhttp.setModelContentType(WebContent.contentTypeRDFXML);
			resSet = qhttp.execSelect();
			qhttp.close();
		}
		while (resSet.hasNext())
		{
			QuerySolution qSol = resSet.next();
			RDFNode nod = qSol.get("o");
			if (nod != null && nod.isResource())
			{
				String newUri = nod.asResource().getURI();
				if (!processedRes.contains(newUri))
				{
					Model aux = getCategories(newUri);
					res.add(aux);
					processedRes.add(newUri);
				}
			}
		}
		qexec.close();
		
		return res;
	}
	
	public Model getMolecule(String url)
	{
		
		Model res = getCategories(url);
		
		res.add(getOneHopNeighborhood(url));
		return res;
	}

}
