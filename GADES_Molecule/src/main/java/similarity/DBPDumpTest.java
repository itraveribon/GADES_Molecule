package similarity;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLIndividual;

import de.paul.annotations.AncestorAnnotation;
import de.paul.annotations.Annotatable;
import de.paul.annotations.Category;
import de.paul.annotations.DepthAnnotation;
import de.paul.annotations.SemanticallyExpandedAnnotation;
import de.paul.annotations.WeightedAnnotation;
import de.paul.annotations.NeighborhoodAnnotation;
import de.paul.kb.dbpedia.DBPediaHandler;
import de.paul.kb.dbpedia.categories.WikiCatHierarchyHandler;
import de.paul.similarity.entityScorers.CombinedEntityPairScorer;
import de.paul.similarity.entityScorers.LCAScorer;
import de.paul.util.CombineMode;
import de.paul.util.Directionality;
import de.paul.util.Paths;
import ontologyManagement.MyOWLIndividual;
import ontologyManagement.MyOWLLogicalEntity;
import ontologyManagement.MyOWLOntology;
import ontologyManagement.OWLLink;

public class DBPDumpTest {
	
	public static AncestorAnnotation getAncestorAnnotation(MyOWLIndividual entA, MyOWLOntology o)
	{
		WikiCatHierarchyHandler hierHandler =  WikiCatHierarchyHandler
				.getInstance(Paths.TDB_DBPEDIA_HIERARCHY);
		Set<OWLIndividual> cats = o.getCategories(entA.getOWLNamedIndividual(), false);
		Set<Category> categories = new HashSet<Category>();
		for (OWLIndividual ind: cats)
		{
			int depth = hierHandler.getDepth(ind.toStringID());
			DepthAnnotation dAnn = new DepthAnnotation(ind.toStringID(), 1, depth);
			categories.add(new Category(ind.toStringID(), depth, dAnn));
		}
		AncestorAnnotation aa = new AncestorAnnotation(entA.toString(), 1, categories);
		return aa;
	}
	
	public static NeighborhoodAnnotation getNeighborhoodAnnotation(MyOWLIndividual entA, MyOWLOntology o, int expandRadius)
	{
		DBPediaHandler dbhandler = DBPediaHandler.getInstance(Paths.TDB_DBPEDIA);
		Set<OWLLink> links = entA.getNeighbors();
		Set<Annotatable> neighs = new HashSet<Annotatable>();
		for (OWLLink l: links)
		{
			MyOWLLogicalEntity ent = l.getDestiny();
			if (ent != null)
			{
				WeightedAnnotation wA = new WeightedAnnotation(ent.getURI(), 1);
				neighs.add(wA);
			}
		}
		if (expandRadius > 1)
		{
			neighs.addAll(dbhandler.getNeighborsOutEdges(neighs));
		}
		NeighborhoodAnnotation nA = new NeighborhoodAnnotation(entA.getURI(), 1, neighs);
		return nA;
	}
	
	public static double similarity(MyOWLIndividual entA, MyOWLIndividual entB, MyOWLOntology o) throws Exception
	{
		
		AncestorAnnotation aa = getAncestorAnnotation(entA, o);
		AncestorAnnotation ab = getAncestorAnnotation(entB, o);
		NeighborhoodAnnotation nA = getNeighborhoodAnnotation(entA, o, 2);
		NeighborhoodAnnotation nB = getNeighborhoodAnnotation(entB, o, 2);
		//NeighborhoodAnnotation nA = new NeighborhoodAnnotation(entA, 1, DBPediaHandler.getInstance(Paths.TDB_DBPEDIA), 2, Directionality.OUTGOING);
		//NeighborhoodAnnotation nB = new NeighborhoodAnnotation(entB, 1, DBPediaHandler.getInstance(Paths.TDB_DBPEDIA), 2, Directionality.OUTGOING);
		SemanticallyExpandedAnnotation seA = new SemanticallyExpandedAnnotation(aa, nA, CombineMode.PLUS);
		SemanticallyExpandedAnnotation seB = new SemanticallyExpandedAnnotation(ab, nB, CombineMode.PLUS);
		CombinedEntityPairScorer cScorer = new CombinedEntityPairScorer(seA, seB, CombineMode.PLUS);
		cScorer.setLeft();
		return cScorer.score();
	}

	public static void main(String[] args) throws Exception {
		String entA = "http://dbpedia.org/resource/Barack_Obama";
		String entB = "http://dbpedia.org/resource/Michelle_Obama";
		WikiCatHierarchyHandler hierHandler =  WikiCatHierarchyHandler
				.getInstance(Paths.TDB_DBPEDIA_HIERARCHY);
		AncestorAnnotation aa = new AncestorAnnotation(entA, 1,
				DBPediaHandler.getInstance(Paths.TDB_DBPEDIA),
				WikiCatHierarchyHandler.getInstance(Paths.TDB_DBPEDIA_HIERARCHY));
		AncestorAnnotation ab = new AncestorAnnotation(entB, 1,
				DBPediaHandler.getInstance(Paths.TDB_DBPEDIA),
				WikiCatHierarchyHandler.getInstance(Paths.TDB_DBPEDIA_HIERARCHY));
		NeighborhoodAnnotation nA = new NeighborhoodAnnotation(entA, 1, DBPediaHandler.getInstance(Paths.TDB_DBPEDIA), 2, Directionality.OUTGOING);
		NeighborhoodAnnotation nB = new NeighborhoodAnnotation(entB, 1, DBPediaHandler.getInstance(Paths.TDB_DBPEDIA), 2, Directionality.OUTGOING);
		SemanticallyExpandedAnnotation seA = new SemanticallyExpandedAnnotation(aa, nA, CombineMode.PLUS);
		SemanticallyExpandedAnnotation seB = new SemanticallyExpandedAnnotation(ab, nB, CombineMode.PLUS);
		CombinedEntityPairScorer cScorer = new CombinedEntityPairScorer(seA, seB, CombineMode.PLUS);
		LCAScorer scorer = new LCAScorer(aa, ab);
		cScorer.setLeft();
		scorer.setLeft();
		System.out.println(scorer.score());
		System.out.println(cScorer.score());

	}

}
