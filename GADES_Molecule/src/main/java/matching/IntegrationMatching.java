package matching;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ontologyManagement.OWLConcept;
import similarity.ComparableElement;
import similarity.Evaluation;
import similarity.matching.AnnSim;
import similarity.matching.AnnotationComparison;
import similarity.matching.HungarianAlgorithm;

public class IntegrationMatching {
	
	private Map<AnnotationComparison,Double> mapComparisons = null;
	String[] v1;
	String[] v2;
	double[][] costMatrix;
	int[] assignment;
	
	public IntegrationMatching()
	{
	}
	
	public IntegrationMatching(Map<AnnotationComparison,Double> matrix)
	{
		mapComparisons = matrix;
	}
	
	public Map<String, String> getAssignment(PrintWriter writer)
	{
		Map<Integer, String> m1 = new HashMap<Integer, String>();
		Map<Integer, String> m2 = new HashMap<Integer, String>();
		
		for (int i = 0; i< v1.length; i++)
		{
			String s1 = v1[i];
			m1.put(i, s1);
		}
		for (int j = 0; j< v2.length; j++)
		{
			String s2 = v2[j];
			m2.put(j, s2);
		}
		Map<String, String> mapAssign = new HashMap<String, String>();
		for (int i = 0; i < assignment.length; i++)
		{
			int aux = assignment[i];
			if (aux >=0) //If there is an assignment
			{
				double sim = 1 - costMatrix[i][aux];
				writer.println(m1.get(i) + "\t" + m2.get(aux) + "\t" + sim);
				mapAssign.put(m1.get(i), m2.get(aux));
				mapAssign.put(m2.get(aux), m1.get(i));
			}
		}
		return mapAssign;
	}
	
		
	public <T> double matching(Set<T> a, Set<T> b) throws Exception
	{
		if (a.getClass() != b.getClass() && a != Collections.emptySet() && b != Collections.emptySet())// || !(a instanceof Set<ComparableElement>)))// || !(a instanceof Set<ComparableElement>))
			throw new Exception("Invalid comparison between " + a.getClass() + " " + b.getClass());
		else
		{
			if (a.equals(b))
				return 1.0;
			if (a.isEmpty() || b.isEmpty()) //Here we know that, almost one of the set is not empty
				return 0.0;
			costMatrix = new double [a.size()][b.size()];
			Map<Integer, String> m1 = new HashMap<Integer, String>();
			Map<Integer, String> m2 = new HashMap<Integer, String>();
			v1 = a.toArray(new String[a.size()]);
			v2 = b.toArray(new String[b.size()]);
			for (int i = 0; i< v1.length; i++)
			{
				String s1 = v1[i];
				m1.put(i, s1);
				for (int j = 0; j < v2.length; j++)
				{
					String s2 = v2[j];
					m2.put(j, s2);
					AnnotationComparison comp = new AnnotationComparison(s1, s2);
					Double sim = mapComparisons.get(comp);
					if (sim == null)
						sim = 0.0;
					costMatrix[i][j] = 1 - sim; //The hungarian algorithm minimize. Therefore we convert the similarity in distance
				}
			}

			HungarianAlgorithm hungarn = new HungarianAlgorithm(costMatrix);
			assignment = hungarn.execute();
			
			double sim = 0;
			Set<Double> simMatchings = new HashSet<Double>();
			for (int i = 0; i < assignment.length; i++)
			{
				int aux = assignment[i];
				if (aux >=0) //If there is an assignment
				{
					sim += 1-costMatrix[i][aux];
					if (1-costMatrix[i][aux] != 0)
						simMatchings.add(1-costMatrix[i][aux]);
				}
			}
			//System.out.println(Collections.min(simMatchings));
			return 2*sim/(v1.length+v2.length);
		}
	}

	public static void main(String[] args) throws Exception {
		String dump1 = args[0];
		String dump2 = args[1];
		Set<String> mol1 = new HashSet<String>();
		Set<String> mol2 = new HashSet<String>();
		Map<AnnotationComparison, Double> mapComp = new HashMap<AnnotationComparison, Double>();
		FileReader fr;// = new FileReader("resources/results_"+ dump1 + "-" + dump2 + "Jaro.txt");
		BufferedReader bf;// = new BufferedReader(fr);
		String line;
		/*while ((line = bf.readLine()) != null)
		{
			String[] elem = line.split("\t");
			AnnotationComparison ac = new AnnotationComparison(elem[0], elem[1]);
			mapComp.put(ac, Double.parseDouble(elem[2]));
			mol1.add(elem[0]);
			mol2.add(elem[1]);
		}
		bf.close();*/
		String method = "GADESCamilo";//"Jaccard_RandomN";//"Jaro_Random";//"Jaccard_Random";//
		//String file = "resources/results_"+ dump1 + "-" + dump2 + method + ".txt";
		//String file = "resources/500_vs_500_TransE_results_20161110_" + dump1 + "-" + dump2 +".txt";
		String file = "resources/Camilo/results_" + dump1 + "-" + dump2 + "Jaccard.txt";
		double thr = Double.parseDouble(args[2]);//0.9;
		System.out.println("First read");
		fr = new FileReader(file);
		bf = new BufferedReader(fr);
		while ((line = bf.readLine()) != null)
		{
			String[] elem = line.split("\t");
			elem[0] = elem[0].replace("http://dbpedia.org/resource/", "");
			elem[1] = elem[1].replace("http://dbpedia.org/resource/", "");
			AnnotationComparison ac = new AnnotationComparison(elem[0], elem[1]);
			Double sim = Double.parseDouble(elem[2]);
			//if (sim < thr)
			//	sim = 0.0;
			mapComp.put(ac, sim);
			mol1.add(elem[0]);
			mol2.add(elem[1]);
		}
		bf.close();
		System.out.println("Second read");
		IntegrationMatching bpm = new IntegrationMatching(mapComp);
		bpm.matching(mol1, mol2);
		String matchingFile = "resources/Camilo/matching_" + dump1 + "-" + dump2 + method + ".txt";
		PrintWriter writer = new PrintWriter(matchingFile);
		Map<String, String> mapAs = bpm.getAssignment(writer);
		/*PrintWriter writer = new PrintWriter("resources/matching_" + dump1 + "-" + dump2 + ".txt");
		for (String s: mapAs.keySet())
		{
			writer.println(s + "\t" + mapAs.get(s));
		}*/
		writer.close();
		double[] thrs = {0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
		for (int i = 0; i < thrs.length; i++)
		{
			thr = thrs[i];
			String[] vecArg = {matchingFile, "" + thr};
			Evaluation.main(vecArg);
		}

	}

}
