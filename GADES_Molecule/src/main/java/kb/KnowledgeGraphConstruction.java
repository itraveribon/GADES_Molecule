package kb;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.query.Dataset;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RiotException;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.engine.http.QueryEngineHTTP;
import org.apache.jena.tdb.TDBFactory;

public class KnowledgeGraphConstruction {

	public static final String TDB_DBPEDIA = "/media/traverso/TOSHIBA EXT/SemanticSimilarity/triple_data/knowledge_bases/dbpedia/tdb_dbpedia";
	public static final String TDB_DBPEDIA_HIERARCHY = "/media/traverso/TOSHIBA EXT/SemanticSimilarity/triple_data/knowledge_bases/dbpedia_category_hierarchy_capital";
	
	public static void countSubjects(Model m)
	{
		String sparqlQueryString = "select distinct ?s where {?s ?p ?o .}";
		
		Query query2 = QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(query2, m);
		ResultSet res = qexec.execSelect();
		int count = 0;
		while (res.hasNext())
		{
			res.next();
			count++;
		}
		System.out.println(count);
		
		sparqlQueryString = "select distinct ?s where {?s <http://purl.org/dc/terms/subject> ?o .}";
		
		query2 = QueryFactory.create(sparqlQueryString);
		qexec = QueryExecutionFactory.create(query2, m);
		res = qexec.execSelect();
		count = 0;
		while (res.hasNext())
		{
			res.next();
			count++;
		}
		System.out.println(count);
		
		
	}
	public static Set<String> getEntities(Model m)
	{
		Set<String> entities = new HashSet<String>();
		String sparqlQueryString = "select distinct ?o where {?s ?p ?o . FILTER regex(str(?o), \"http://dbpedia.org/\") .}";
		
		Query query2 = QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(query2, m);
		ResultSet res = qexec.execSelect();
		
		while (res.hasNext())
		{
			QuerySolution qSol = res.next();
			RDFNode node = qSol.get("o");
			if (node.isURIResource())
			{
				String uri = node.asResource().getURI();
				entities.add(uri);
			}
		}
		return entities;
	}
	
	public static void createKnowledgeGraph(String input, String output)
	{
		Model m = ModelFactory.createDefaultModel();
		m.read("/home/traverso/git/GADES/GADES/resources/dbpedia_2015-10.nt");
		m.read("resources/dcterms_od.ttl");
		m.read("resources/infobox_property_definitions_en_owl.ttl");
		m.read("resources/foaf.rdf");
		//m.read("resources/wikidata.rdf");
		//m.read("resources/wikidata_properties.nt");
		Model people = ModelFactory.createDefaultModel();
		//people.read("/home/traverso/git/GADES/GADES/resources/DBPedia_People_2016-07-22_19-50-12.nq");
		people.read(input, "NT");
		
		//countSubjects(people);
		Set<String> entities = getEntities(people);
	
		Dataset dbp = TDBFactory.createDataset("/media/traverso/TOSHIBA EXT/SemanticSimilarity/triple_data/knowledge_bases/dbpedia_and_hierarchy/tdb_dbpedia");
		DBPediaExtractor extractor = new DBPediaExtractor(dbp);
		int count = 0;
		for (String e: entities)
		{
			Model entHier = extractor.getMolecule(e);
			if (entHier.isEmpty())
			{
				RDFNode node = people.getResource(e);
				people.removeAll(null, null, node);
			}
			else
				people.add(entHier);
			count++;
			System.out.println(count + "/" + entities.size());
		}
		m.add(people);
		FileOutputStream out;
		try {
			out = new FileOutputStream(output);
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
	
	public static String getWikidataEntity(String input)
	{
		String endpoint = "http://dbpedia.org/sparql";
		//Dataset dbp = TDBFactory.createDataset("/media/traverso/TOSHIBA EXT/SemanticSimilarity/triple_data/knowledge_bases/dbpedia_and_hierarchy/tdb_dbpedia");
		String sparqlQueryString = "select distinct ?o where {<" + input + "> <http://www.w3.org/2002/07/owl#sameAs> ?o . FILTER regex(str(?o), \"http://www.wikidata.org/\") .}";
		Query query2 = QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query2);//.create(query2, dbp);
		ResultSet res = qexec.execSelect();
		
		while (res.hasNext())
		{
			QuerySolution qSol = res.next();
			RDFNode node = qSol.get("o");
			if (node.isURIResource())
			{
				String uri = node.asResource().getURI();
				return uri;
			}
		}
		return "";
	}
	
	public static Model getDBpediaWikidataMapping(String wikidataUri)
	{
		String endpoint = "http://dbpedia.org/sparql";
		String sparqlQueryString = "construct {?s <http://www.w3.org/2002/07/owl#sameAs> <" + wikidataUri + ">} where {?s <http://www.w3.org/2002/07/owl#sameAs> <" + wikidataUri + "> .}";
		Query query2 = QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(endpoint, query2);
		System.out.println(wikidataUri);
		Model res;
		try{
			res = qexec.execConstruct();
		}catch (RiotException exc)
		{
			QueryEngineHTTP qhttp = new QueryEngineHTTP(endpoint, query2);
			qhttp.setModelContentType(WebContent.contentTypeRDFXML);
			res = qhttp.execConstruct();
			qhttp.close();
		}
		return res;
	}
	
	public static void main(String[] args) {
		Model m = ModelFactory.createDefaultModel();
		m.read("/home/traverso/git/GADES/GADES/resources/dbpedia_2015-10.nt");
		m.read("resources/dcterms_od.ttl");
		Model people = ModelFactory.createDefaultModel();
		//people.read("/home/traverso/git/GADES/GADES/resources/DBPedia_People_2016-07-22_19-50-12.nq");
		//people.read("/home/traverso/workspace/GADES_Molecule/resources/goldStandard_fixed.nt", "NT");
		people.read("/home/traverso/workspace/GADES_Molecule/resources/WikiData/goldStandard_wd_upd_cleaned_dbp.nt", "NT");
		//people.read("/home/traverso/workspace/GADES_Molecule/resources/WikiData/goldStandard_dbp_cleaned.nt", "NT");
		
		//countSubjects(people);
		Set<String> entities = getEntities(people);
	
		//Dataset hier = TDBFactory.createDataset(TDB_DBPEDIA_HIERARCHY);
		Dataset dbp = TDBFactory.createDataset("/media/traverso/TOSHIBA EXT/SemanticSimilarity/triple_data/knowledge_bases/dbpedia_and_hierarchy/tdb_dbpedia");
		//DBPediaExtractor.joinDatasets(dbp, hier);
		//System.out.println("Join finished");
		DBPediaExtractor extractor = new DBPediaExtractor(dbp);
		//Model hierarchy = hier.getDefaultModel();
		//Model dbpedia = dbp.getDefaultModel();
		int count = 0;
		for (String e: entities)
		{
			if (count > 0)
			{
				Model entHier = extractor.getMolecule(e);
				if (entHier.isEmpty())
				{
					RDFNode node = people.getResource(e);
					people.removeAll(null, null, node);
				}
				else
					people.add(entHier);
			}
			count++;
			System.out.println(count + "/" + entities.size());
		}
		m.add(people);
		FileOutputStream out;
		try {
			//out = new FileOutputStream("resources/people_enriched_goldStandard.ttl");
			out = new FileOutputStream("resources/WikiData/goldStandard_wd_enriched.nt");
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

}
