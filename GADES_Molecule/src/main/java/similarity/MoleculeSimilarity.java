package similarity;

import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

import ontologyManagement.MyOWLIndividual;
import ontologyManagement.MyOWLLogicalEntity;
import ontologyManagement.MyOWLOntology;
import ontologyManagement.OWLLink;

public class MoleculeSimilarity {
	
	public static void main(String[] args) throws Exception {
		String ontFile = "resources/people_enriched_goldStandard.ttl"; 
		//Model m = ModelFactory.createDefaultModel();
		//m.read(ontFile);
		
		
		MyOWLOntology o = new MyOWLOntology(ontFile, "http://dbpedia.org/", "HermiT");
		/*MyOWLIndividual a = o.getMyOWLIndividual("http://dbpedia.org/resource/Airavt/dump0");
		MyOWLIndividual b = o.getMyOWLIndividual("http://dbpedia.org/resource/Airavt/dump1");
		Set<MyOWLLogicalEntity> anns = new HashSet<MyOWLLogicalEntity>();
		anns.add(a);
		anns.add(b);
		o.setOWLLinks(anns);
		Set<OWLLink> neighA = a.getNeighbors();
		Set<OWLLink> neighB = b.getNeighbors();
		System.out.println(b.similarityNeighbors(a));
		System.out.println(b.taxonomicSimilarity(a));
		System.out.println(b.similarity(a));*/
		
		Set<MyOWLIndividual> comparableIndividuals = getComparableEntities(o);
		List<MyOWLIndividual> listInd = new ArrayList<MyOWLIndividual>(comparableIndividuals);
		PrintWriter simWriter = new PrintWriter("resources/results.txt");
		//double totalComparisons = listInd.size() * (listInd.size() + 1) / 2;
		BigInteger totalComparisons = new BigInteger(listInd.size() + "");
		BigInteger ex1 = new BigInteger((listInd.size() + 1) + "");
		totalComparisons = totalComparisons.multiply(ex1);
		totalComparisons = totalComparisons.divide(new BigInteger("2"));
		BigInteger count = BigInteger.ZERO;
		for (int i = 0; i < listInd.size(); i++)
		{
			MyOWLIndividual ind1 = listInd.get(i);
			for (int j = i; j < listInd.size(); j++)
			{
				MyOWLIndividual ind2 = listInd.get(j);
				double sim = ind1.similarity(ind2);
				count = count.add(BigInteger.ONE);
				System.out.println(ind1 + "\t" + ind2 + "\t" + sim + "\t" + count + "/" + totalComparisons);
				simWriter.println(ind1 + "\t" + ind2 + "\t" + sim);
				simWriter.println(ind2 + "\t" + ind1 + "\t" + sim);
			}
		}
		simWriter.close();
	}
	
	public static Set<MyOWLIndividual> getComparableEntities(MyOWLOntology o)
	{
		Set<MyOWLIndividual> individuals = o.getMyOWLIndividuals();
		Set<MyOWLIndividual> comparableIndividuals = new HashSet<MyOWLIndividual>();
		for (MyOWLIndividual i: individuals)
		{
			if (i.getName().contains("dump"))
			{
				comparableIndividuals.add(i);
			}
		}
		return comparableIndividuals;
	}
}
