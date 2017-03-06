package similarity;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import kb.KnowledgeGraphConstruction;
import ontologyManagement.MyOWLIndividual;
import ontologyManagement.MyOWLOntology;

public class DumpComparison {

	public static Set<String> getComparableEntities(String listFile) throws IOException
	{
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
		return setUri1;
	}
	
	public static void main(String[] args) throws Exception {
		int dump1 = Integer.parseInt(args[0]), dump2 = Integer.parseInt(args[1]);
		
		Model m1 = ModelFactory.createDefaultModel();
		m1.read("resources/dump" + dump1 + "_fixed.nt");
		//m1.read("resources/Camilo/dump" + dump1 + "_fixed.nt");
		
		Model m2 = ModelFactory.createDefaultModel();
		m2.read("resources/dump" + dump2 + "_fixed.nt");
		//m2.read("resources/Camilo/dump" + dump2 + "_fixed.nt");
		
		m1.add(m2);
		String newModel = "resources/dump" + dump1 + "-" + dump2 + ".nt";
		//String newModel = "resources/Camilo/dump" + dump1 + "-" + dump2 + ".nt";
		m1.write(new FileOutputStream(newModel), "NT");
		String enrichedModel = "resources/dump" + dump1 + "-" + dump2 + "_enriched.nt";
		//String enrichedModel = "resources/Camilo/dump" + dump1 + "-" + dump2 + "_enriched.nt";
		//KnowledgeGraphConstruction.createKnowledgeGraph(newModel, enrichedModel);
		//System.out.println("Knowledge Graph Created");
		
		Set<String> taxProperties = new HashSet<String>();
		taxProperties.add("http://purl.org/dc/terms/subject");
		taxProperties.add("http://www.w3.org/2004/02/skos/core#broader");
		MyOWLOntology o = new MyOWLOntology(enrichedModel, "http://dbpedia.org/", "HermiT", taxProperties);
		Set<String> setUris = getComparableEntities("resources/lists/list_gold_random");
		
		List<String> listInd1 = new ArrayList<String>(setUris);
		//List<String> listInd2 = new ArrayList<String>(setUris);
		PrintWriter simWriter = new PrintWriter("resources/results_" + dump1 + "-" + dump2 + "Random_GBBS.txt");
		//PrintWriter simWriter = new PrintWriter("resources/Camilo/results_" + dump1 + "-" + dump2 + "Jaccard.txt");
		int N = listInd1.size();
		int M = listInd1.size();
		int totalComparisons = N * M;
		/*BigInteger totalComparisons = new BigInteger(listInd1.size() + "");
		BigInteger ex1 = new BigInteger((listInd1.size() + 1) + "");
		totalComparisons = totalComparisons.multiply(ex1);
		totalComparisons = totalComparisons.divide(new BigInteger("2"));*/
		int count = 0;
		MyOWLIndividual ind10 = o.getMyOWLIndividual("http://dbpedia.org/resource/2015–16_AZAL_PFC_season__Ismayil_Ibrahimli__1/dump0");
		MyOWLIndividual ind20 = o.getMyOWLIndividual("http://dbpedia.org/resource/2015–16_AZAL_PFC_season__Ismayil_Ibrahimli__1/dump2");
		//double sim0 = ind10.similarity(ind20);
		PrintWriter molWriter = new PrintWriter("resources/molecules_" + dump1 + "-" + dump2 + "_GBSS.txt");
		for (int i = 0; i < N; i++)
		{
			molWriter.println(listInd1.get(i));
		}
		molWriter.close();
		for (int i = 0; i < N; i++)
		//FileReader fr = new FileReader("resources/lists/Dump" + dump1 + "_Dump" + dump2 + "_ALLvsALL_20000_random_samples.txt");
		//BufferedReader bf = new BufferedReader(fr);
		//String line;
		//while ((line = bf.readLine()) != null)
		{
			//String[] uris = line.split("\t");
			String a = listInd1.get(i)  + "/dump" + dump1;//uris[0];
			//String b = listInd1.get(i)  + "/dump" + dump2;//uris[1];
			MyOWLIndividual ind1 = o.getMyOWLIndividual(a);
			//MyOWLIndividual ind2 = o.getMyOWLIndividual(b);
			//double sim = ind1.similarity(ind2);
			//count++;
			//System.out.println(ind1 + "\t" + ind2 + "\t" + sim + "\t" + count + "/" + totalComparisons);
			//simWriter.println(ind1 + "\t" + ind2 + "\t" + sim);
			/*for (int j = 0; j < i; j++)
			{
				simWriter.print("0\t");
			}*/
			for (int j = 0; j < M; j++)
			{
				String b = listInd1.get(j) + "/dump" + dump2;
				MyOWLIndividual ind2 = o.getMyOWLIndividual(b);
				double sim = DBPDumpTest.similarity(ind1, ind2, o);//ind1.similarity(ind2);
				if (j + 1 == M)
					simWriter.print(sim + "\n");
				else
					simWriter.print(sim + "\t");
				count++;
				System.out.println(count + "/" + totalComparisons + "\t" + sim);
				//System.out.println(ind1 + "\t" + ind2 + "\t" + sim + "\t" + count + "/" + totalComparisons);
				//simWriter.println(ind1 + "\t" + ind2 + "\t" + sim);
			}
		}
		//bf.close();
		simWriter.close();

	}

}
