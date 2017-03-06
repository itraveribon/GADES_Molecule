package wikidata;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class WDDBpeification {

	public static void main(String[] args) throws IOException {
		/*Model m = ModelFactory.createDefaultModel();
		m.read("/home/traverso/git/GADES/GADES/resources/dbpedia_2015-10.nt");
		m.read("resources/dcterms_od.ttl");
		Model people = ModelFactory.createDefaultModel();
		people.read("/home/traverso/workspace/GADES_Molecule/resources/WikiData/goldStandard_wd_cleaned.nt", "NT");
		*/
		Map<String, String> entMap = new HashMap<String, String>();
		Map<String, String> relMap = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new FileReader("resources/WikiData/adSem.nt"));
		
		Set<String> entities = new HashSet<String>();
		Set<String> properties = new HashSet<String>();
		Set<String> eqProp = new HashSet<String>();
		String line;
		PrintWriter writer = new PrintWriter("resources/WikiData/adSem_filtered.nt");
		while ((line = br.readLine()) != null)
		{
			String[] vec = line.split(" ");
			entities.add(vec[0]);
			entities.add(vec[2]);
			properties.add(vec[1]);
			if (vec[1].contains("equivalentProperty"))
			{
				writer.println(vec[0] + " " + vec[1] + " " + vec[2]);
				eqProp.add(vec[0] + " " + vec[1] + " " + vec[2] + " .");
				relMap.put(vec[2], vec[0]);
			}
			else
			{
				String cat = vec[0] + vec[2];
				//if (cat.contains("http://dbpedia.org") && cat.contains("http://www.wikidata.org"))
				//{
					writer.println(vec[0] + " " + vec[1] + " " + vec[2]);
					//entMap.put(vec[0], vec[2]);
					entMap.put(vec[2], vec[0]);
				//}
			}
		}
		writer.close();
		br.close();
		System.out.println(entities.size());
		System.out.println(properties.size());
		
		writer = new PrintWriter("resources/WikiData/goldStandard_wd_upd_cleaned_dbp.nt");
		br = new BufferedReader(new FileReader("resources/WikiData/goldStandard_wd_upd_cleaned.nt"));
		while ((line = br.readLine()) != null)
		{
			String[] vec = line.split(" ");
			String dbp = entMap.get(vec[2]);
			if (dbp == null)
			{
				dbp = "";
				for (int i = 2; i < vec.length - 1; i++)
				{
					dbp += vec[i] + " ";
				}
			}
			String dbpProp = relMap.get(vec[1]);
			if (dbpProp == null)
				dbpProp = vec[1];
			if (!dbp.contains("http://www.wikidata.org/entity/statement"))
				writer.println(vec[0] + " " + dbpProp + " " + dbp + " .");
		}
		br.close();
		/*for (String s: eqProp)
		{
			writer.println(s);
		}*/
		writer.close();

	}

}
