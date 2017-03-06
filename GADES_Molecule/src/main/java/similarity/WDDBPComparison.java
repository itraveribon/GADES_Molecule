package similarity;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;

import kb.KnowledgeGraphConstruction;
import ontologyManagement.MyOWLIndividual;
import ontologyManagement.MyOWLOntology;

public class WDDBPComparison {
	
	public static void getComparableEntities(String listFile, Set<String> list1, Set<String> list2) throws IOException
	{
		FileReader fr = new FileReader(listFile);
		BufferedReader bf = new BufferedReader(fr);
		String line;
		Set<String> setUri1 = new HashSet<String>();
		
		while ((line = bf.readLine()) != null)
		{
			String standardURI = line.replace("<", "").replace(">", "");
			String[] vec = standardURI.split(" ");
			list1.add(vec[0]);
			list2.add(vec[2]);
		}
		bf.close();
	}
	
	public static void fixObjectProperties(String output)
	{
		Model m = ModelFactory.createDefaultModel();
		m.read(output);
		String sparqlQueryString = "select ?p ?o where {?s ?p ?o .}";
		
		Query query2 = QueryFactory.create(sparqlQueryString);
		QueryExecution qexec = QueryExecutionFactory.create(query2, m);
		
		ResultSet resSet = qexec.execSelect();
		Resource oProp = m.createResource("http://www.w3.org/2002/07/owl#ObjectProperty");
		Resource dProp = m.createResource("http://www.w3.org/2002/07/owl#DatatypeProperty");
		while (resSet.hasNext())
		{
			QuerySolution qSol = resSet.next();
			RDFNode nod = qSol.get("o");
			RDFNode prop = qSol.get("p");
			if (nod != null && nod.isResource())
			{
				m.add((Resource) prop, RDF.type, oProp);
			}
			if (nod != null && !nod.isResource())
			{
				m.add((Resource) prop, RDF.type, dProp);
			}
		}
		
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

	public static void main(String[] args) throws Exception {
		Model m1 = ModelFactory.createDefaultModel();
		m1.read("resources/WikiData/goldStandard_dbp_cleaned.nt");
		
		Model m2 = ModelFactory.createDefaultModel();
		m2.read("resources/WikiData/goldStandard_wd_upd_cleaned_dbp.nt");
		
		m1.add(m2);
		String newModel = "resources/WikiData/together.nt";
		m1.write(new FileOutputStream(newModel), "NT");
		String enrichedModel = "resources/WikiData/together_enriched.nt";
		//KnowledgeGraphConstruction.createKnowledgeGraph(newModel, enrichedModel);
		
		System.out.println("Knowledge Graph Created");
		
		
		Set<String> taxProperties = new HashSet<String>();
		taxProperties.add("http://purl.org/dc/terms/subject");
		taxProperties.add("http://www.w3.org/2004/02/skos/core#broader");
		MyOWLOntology o = new MyOWLOntology(enrichedModel, "http://dbpedia.org/", "HermiT", taxProperties);
		Set<String> setUri1 = new HashSet<String>();
		Set<String> setUri2 = new HashSet<String>();
		getComparableEntities("resources/WikiData/trueGoldStandard.nt", setUri1, setUri2);
		o.addIndividuals(setUri2);
		List<String> listInd1 = new ArrayList<String>(setUri1);
		List<String> listInd2 = new ArrayList<String>(setUri2);
		
		String method = "jaccard";
		PrintWriter simWriter = new PrintWriter("resources/WikiData/results_" + method + ".txt");
		int N = listInd1.size();
		int M = listInd2.size();
		int totalComparisons = N * M;
		PrintWriter molWriter = new PrintWriter("resources/WikiData/molecules_dbp_" + method + ".txt");
		for (int i = 0; i < N; i++)
		{
			molWriter.println(listInd1.get(i));
		}
		molWriter.close();
		molWriter = new PrintWriter("resources/WikiData/molecules_wd_" + method + ".txt");
		for (int i = 0; i < M; i++)
		{
			molWriter.println(listInd2.get(i));
		}
		molWriter.close();
		
		MyOWLIndividual ind10 = o.getMyOWLIndividual("http://dbpedia.org/resource/Eugenio_Bonivento");
		MyOWLIndividual ind20 = o.getMyOWLIndividual("http://www.wikidata.org/entity/Q16554625");
		double sim0 = ind10.similarity(ind20);
		System.out.println(sim0);
		
		int count = 0;
		for (int i = 0; i < N; i++)
		{
			String a = listInd1.get(i);
			MyOWLIndividual ind1 = o.getMyOWLIndividual(a);
			/*for (int j = 0; j < i; j++)
			{
				simWriter.print("0\t");
			}*/
			for (int j = 0; j < M; j++)
			{
				String b = listInd2.get(j);
				MyOWLIndividual ind2 = o.getMyOWLIndividual(b);
				double sim = ind1.similarity(ind2);
				if (j + 1 == M)
					simWriter.print(sim + "\n");
				else
					simWriter.print(sim + "\t");
				count++;
				System.out.println(count + "/" + totalComparisons + "\t" + sim);
			}
		}
		simWriter.close();


	}

}
