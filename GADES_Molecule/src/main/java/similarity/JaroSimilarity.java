package similarity;

import info.debatty.java.stringsimilarity.JaroWinkler;

public class JaroSimilarity {

	public static void main(String[] args) {
		JaroWinkler jw = new JaroWinkler();
		double simTaxDes = jw.similarity("Eugenio Bonivento", "Giacomo Balla");
		System.out.println(simTaxDes);
	}

}
