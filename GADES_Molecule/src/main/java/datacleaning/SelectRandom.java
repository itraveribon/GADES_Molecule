package datacleaning;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectRandom {

	public static void main(String[] args) throws IOException {
		String listFile = "resources/lists/list_gold";
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
		List<String> listMol = new ArrayList<String>(setUri1);
		Collections.shuffle(listMol);
		PrintWriter writer = new PrintWriter("resources/lists/list_gold_random");
		for (int i = 0; i < 500; i++)
		{
			String s = listMol.get(i);
			writer.println("<" + s + ">");
			
		}
		writer.close();
	}

}
