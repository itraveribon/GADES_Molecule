package similarity;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import ontologyManagement.MyOWLIndividual;
import ontologyManagement.MyOWLOntology;

public class CountTriples {

	public static void main(String[] args) throws IOException {
		String enrichedModel = "resources/dump" + "0" + "-" + "2" + "_enriched.nt";
		
		Set<String> taxProperties = new HashSet<String>();
		taxProperties.add("http://purl.org/dc/terms/subject");
		taxProperties.add("http://www.w3.org/2004/02/skos/core#broader");
		MyOWLOntology o = new MyOWLOntology(enrichedModel, "http://dbpedia.org/", "HermiT", taxProperties);
		Set<String> setUris = DumpComparison.getComparableEntities("resources/lists/list_gold_random");
		
		int count = 0;
		for (String uri: setUris)
		{
			MyOWLIndividual ind1 = o.getMyOWLIndividual(uri + "/dump0");
			count += o.getAllTriples(ind1).size();
			MyOWLIndividual ind2 = o.getMyOWLIndividual(uri + "/dump2");
			count += o.getAllTriples(ind2).size();
		}
		
		System.out.println(count);

	}

}
