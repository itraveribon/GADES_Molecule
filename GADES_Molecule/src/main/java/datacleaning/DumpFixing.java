package datacleaning;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class DumpFixing {

	public static Map<String, String> getURIMapping(int dump) throws IOException
	{
		FileReader fr = new FileReader("resources/lists/list_dump" + dump);
		BufferedReader bf = new BufferedReader(fr);
		String line;
		
		Map<String, String> uriMap = new HashMap<String, String>();
		while ((line = bf.readLine()) != null)
		{
			String standardURI = line.replace("/dump" + dump, "");
			uriMap.put(standardURI, line);
		}
		bf.close();
		return uriMap;
	}
	public static void main(String[] args) throws IOException {
		for (int dump = 0; dump < 3; dump++)
		{
			PrintWriter writer = new PrintWriter("resources/Camilo/dump" + dump + "_fixed.nt");
			FileReader fr = new FileReader("resources/Camilo/dump" + dump + ".nt");
			//PrintWriter writer = new PrintWriter("resources/goldStandard_fixed.nt");
			//FileReader fr = new FileReader("resources/goldStandard.nt");
			BufferedReader bf = new BufferedReader(fr);
			String line;
			
			Map<String, String> uriMap = getURIMapping(dump);
			while ((line = bf.readLine()) != null)
			{
				String[] parts = line.split(" ");
				for (int i = 0; i < parts.length; i++)
				{
					String newUri = uriMap.get(parts[i]);
					if (newUri != null)
					{
						line = line.replace(parts[i], newUri);
					}
				}
				/*if (parts.length >= 3)
				{
					if (parts[2].contains("^^"))
					{
						String newLiteral = "\"" + parts[2].replace("^^", "\"^^");
						line = line.replace(parts[2], newLiteral);
					}
				}*/
				writer.println(line);
			}
			bf.close();
			writer.close();
		}

	}

}
