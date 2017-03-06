package kb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.tdb.TDBFactory;

public class WikipediaHierarchyFix {

	public static void main(String[] args) {
		Model m = ModelFactory.createDefaultModel();
		m.read("/media/traverso/TOSHIBA EXT/SemanticSimilarity/category_labels_en.ttl");
		String sparqlQueryString = "select ?s where {?s ?p ?o .}";
		
		Query query2 = QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(query2, m);
		ResultSet resSet = qexec.execSelect();
		Set<String> categories = new HashSet<String>();
		Map<String, String> toCap = new HashMap<String, String>();
		while (resSet.hasNext())
		{
			QuerySolution qSol = resSet.next();
			RDFNode node = qSol.get("s");
			String cat = node.asResource().getURI();
			categories.add(cat);
			toCap.put(cat.toLowerCase(), cat);
		}
		
		Model newHier = ModelFactory.createDefaultModel();
		Dataset hier = TDBFactory.createDataset("/media/traverso/TOSHIBA EXT/SemanticSimilarity/triple_data/knowledge_bases/dbpedia_category_hierarchy/tdb-hierarchy-2");
		Model hierarchy = hier.getDefaultModel();
		Property broader = hierarchy.getProperty("http://www.w3.org/2004/02/skos/core#broader");
		Property depth = hierarchy.getProperty("http://dbpedia_hierarchy.org/has_depth");
		
		for (String cat: categories)
		{
			sparqlQueryString = "select ?o ?depth WHERE { <" + cat.toLowerCase() + "> <http://dbpedia_hierarchy.org/has_ancestor> ?o. <" + cat.toLowerCase() + "> <http://dbpedia_hierarchy.org/has_depth> ?depth.}";
			//sparqlQueryString = "construct {<" + cat + "> <http://www.w3.org/2004/02/skos/core#broader> ?o. <" + cat + "> <http://dbpedia_hierarchy.org/has_depth> ?depth.} "
			//		+ "WHERE { <" + cat.toLowerCase() + "> <http://dbpedia_hierarchy.org/has_ancestor> ?o. <" + cat.toLowerCase() + "> <http://dbpedia_hierarchy.org/has_depth> ?depth.}";
			query2 = QueryFactory.create(sparqlQueryString);
			qexec = QueryExecutionFactory.create(query2, hierarchy);
			ResultSet res = qexec.execSelect();
			while (res.hasNext())
			{
				QuerySolution qSol = res.next();
				String oURI = qSol.get("o").asResource().getURI();
				Integer depthNode = qSol.get("depth").asLiteral().getInt();
				Resource o = hierarchy.getResource(toCap.get(oURI));
				Resource s = hierarchy.getResource(cat);
				newHier.add(s, broader, o);
				newHier.addLiteral(s, depth, depthNode);
			}
			//newHier.add(qexec.execConstruct());
		}
		Dataset result = TDBFactory.createDataset("/media/traverso/TOSHIBA EXT/SemanticSimilarity/triple_data/knowledge_bases/dbpedia_category_hierarchy_capital");
		result.begin(ReadWrite.WRITE);
		result.getDefaultModel().add(newHier);
		result.commit();
		result.close();
	}

}
