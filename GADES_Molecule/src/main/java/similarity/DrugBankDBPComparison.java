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

public class DrugBankDBPComparison {

	public static void getComparableEntities(String listFile, Set<String> list1, Set<String> list2, MyOWLOntology o) throws IOException
	{
		FileReader fr = new FileReader(listFile);
		BufferedReader bf = new BufferedReader(fr);
		String line;
		Set<String> setUri1 = new HashSet<String>();
		
		while ((line = bf.readLine()) != null)
		{
			String standardURI = line.replace("<", "").replace(">", "");
			String[] vec = standardURI.split(" ");
			if (o.getMyOWLIndividual(vec[0]) != null  && !list1.contains(vec[0]) && !list2.contains(vec[2]))
			{
				list1.add(vec[0]);
				list2.add(vec[2]);
			}
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
	
	public static void createGoldStandard(String fileA, String fileB) throws IOException
	{
		List<String> fA = new ArrayList<String>(), fB = new ArrayList<String>();
		
		FileReader fr = new FileReader(fileA);
		BufferedReader bf = new BufferedReader(fr);
		String line;
		
		while ((line = bf.readLine()) != null)
		{
			fA.add(line);
		}
		bf.close();
		fr = new FileReader(fileB);
		bf = new BufferedReader(fr);
		
		while ((line = bf.readLine()) != null)
		{
			fB.add(line);
		}
		PrintWriter writer = new PrintWriter("resources/DrugBank/trueGoldStandard.nt");
		
		for (int i = 0; i < fA.size(); i++)
		{
			String aux = fA.get(i) + " <http://www.w3.org/2002/07/owl#sameAs> " + fB.get(i);
			writer.println(aux);
		}
		writer.close();
	}

	public static void main(String[] args) throws Exception {
		Model m1 = ModelFactory.createDefaultModel();
		m1.read("resources/DrugBank/dbpedia_drugs.nt");
		
		Model m2 = ModelFactory.createDefaultModel();
		m2.read("resources/DrugBank/drugbank_drugs.nt");
		
		m1.add(m2);
		String newModel = "resources/DrugBank/together.nt";
		m1.write(new FileOutputStream(newModel), "NT");
		String enrichedModel = "resources/DrugBank/together_enriched.nt";
		//KnowledgeGraphConstruction.createKnowledgeGraph(newModel, enrichedModel);
		
		System.out.println("Knowledge Graph Created");
		
		String method = "gades";
		
		Set<String> taxProperties = new HashSet<String>();
		taxProperties.add("http://purl.org/dc/terms/subject");
		taxProperties.add("http://www.w3.org/2004/02/skos/core#broader");
		MyOWLOntology o = new MyOWLOntology(enrichedModel, "http://dbpedia.org/", "HermiT", taxProperties);
		Set<String> setUri1 = new HashSet<String>();
		Set<String> setUri2 = new HashSet<String>();
		createGoldStandard("resources/DrugBank/subjects_drugs_dbpedia.txt", "resources/DrugBank/subjects_drugs_drugbank.txt");
		getComparableEntities("resources/DrugBank/trueGoldStandard.nt", setUri1, setUri2, o);
		o.addIndividuals(setUri2);
		List<String> listInd1 = new ArrayList<String>(setUri1);
		List<String> listInd2 = new ArrayList<String>(setUri2);
		
		PrintWriter simWriter = new PrintWriter("resources/DrugBank/results_" + method + ".txt");
		int N = listInd1.size();
		int M = listInd2.size();
		
		
		
		Set<String> toRemove = new HashSet<String>();
		PrintWriter molWriter = new PrintWriter("resources/DrugBank/molecules_dbp_" + method + ".txt");
		for (int i = 0; i < N; i++)
		{
			String a = listInd1.get(i);
			MyOWLIndividual ind = o.getMyOWLIndividual(a);
			if (ind != null)
				molWriter.println(a);
			else
			{
				toRemove.add(a);
			}
		}
		listInd1.removeAll(toRemove);
		N = listInd1.size();
		toRemove.clear();
		molWriter.close();
		molWriter = new PrintWriter("resources/DrugBank/molecules_db_" + method + ".txt");
		for (int i = 0; i < M; i++)
		{
			String a = listInd2.get(i);
			MyOWLIndividual ind = o.getMyOWLIndividual(a);
			if (ind != null)
				molWriter.println(a);
			else
			{
				toRemove.add(a);
			}
		}
		listInd2.removeAll(toRemove);
		molWriter.close();
		
		MyOWLIndividual dbpPindolol = o.getMyOWLIndividual("http://dbpedia.org/resource/Pindolol");
		MyOWLIndividual db960 = o.getMyOWLIndividual("http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/DB00960");
		double x = dbpPindolol.similarity(db960);
		System.out.println(x);
		
		int count = 0;
		System.out.println(N + " " + M);
		int totalComparisons = N * M;
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
				System.out.println(ind1 + "\t" + ind2);
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
		
		totalComparisons = N * N;
		count = 0;
		simWriter = new PrintWriter("resources/DrugBank/results_dbp_" + method + ".txt");
		for (int i = 0; i < N; i++)
		{
			String a = listInd1.get(i);
			MyOWLIndividual ind1 = o.getMyOWLIndividual(a);
			/*for (int j = 0; j < i; j++)
			{
				simWriter.print("0\t");
			}*/
			for (int j = 0; j < N; j++)
			{
				String b = listInd1.get(j);
				MyOWLIndividual ind2 = o.getMyOWLIndividual(b);
				System.out.println(ind1 + "\t" + ind2);
				double sim = ind1.similarity(ind2);
				if (j + 1 == N)
					simWriter.print(sim + "\n");
				else
					simWriter.print(sim + "\t");
				count++;
				System.out.println(count + "/" + totalComparisons + "\t" + sim);
			}
		}
		simWriter.close();
		totalComparisons = M * M;
		count = 0;
		simWriter = new PrintWriter("resources/DrugBank/results_db_" + method + ".txt");
		for (int i = 0; i < M; i++)
		{
			String a = listInd2.get(i);
			MyOWLIndividual ind1 = o.getMyOWLIndividual(a);
			/*for (int j = 0; j < i; j++)
			{
				simWriter.print("0\t");
			}*/
			for (int j = 0; j < M; j++)
			{
				String b = listInd2.get(j);
				MyOWLIndividual ind2 = o.getMyOWLIndividual(b);
				System.out.println(ind1 + "\t" + ind2);
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
