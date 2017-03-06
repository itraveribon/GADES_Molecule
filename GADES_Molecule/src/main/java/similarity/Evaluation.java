package similarity;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Evaluation {

	public static void main(String[] args) throws IOException {
		String file = args[0];//"/home/traverso/workspace/GADES_Molecule/resources/matching_0-1TransE.txt";
		double thr = Double.parseDouble(args[1]);
		FileReader fr = new FileReader(file);
		BufferedReader bf = new BufferedReader(fr);
		String line;
		
		double right = 0, predicted = 0, total = 0;
		while ((line = bf.readLine()) != null)
		{
			String[] uris = line.split("\t");
			String uri1 = uris[0].replace("dump0", "");
			uri1 = uri1.replace("dump1", "");
			uri1 = uri1.replace("dump2", "");
			String uri2 = uris[1].replace("dump0", "");
			uri2 = uri2.replace("dump1", "");
			uri2 = uri2.replace("dump2", "");
			Double sim = Double.parseDouble(uris[2]);
			if (sim >= thr)
			{
				if (uri1.equals(uri2))
					right++;
				predicted++;
			}
			total++;
		}
		System.out.println("Precision:" +  right/predicted);
		//System.out.println(right + " " + predicted);
		System.out.println("Recall:" +  right/total);

	}

}
