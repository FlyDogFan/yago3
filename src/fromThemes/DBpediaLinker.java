package fromThemes;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;

import deduplicators.ClassExtractor;
import javatools.administrative.Announce;
import javatools.datatypes.FinalSet;
import basics.Fact;
import basics.FactComponent;
import basics.RDFS;
import basics.Theme;
import basics.Theme.ThemeGroup;
import extractors.Extractor;

/**
 * YAGO2s - DBpediaLinker
 * 
 * Computes the links to DBpedia.
 * 
 * @author Fabian M. Suchanek
 * 
 */
public class DBpediaLinker extends Extractor {

	@Override
	public Set<Theme> input() {
		return new FinalSet<>(CoherentTypeExtractor.YAGOTYPES,
				ClassExtractor.YAGOTAXONOMY);
	}

	/** Mapping to DBpedia classes */
	public static final Theme YAGODBPEDIACLASSES = new Theme(
			"yagoDBpediaClasses",
			"Mappings of YAGO classes to YAGO-based DBpedia classes. For the mappings between YAGO classes and other DBpedia classes, as well as the mapping between YAGO relations and DBpedia relations, see http://yago-knowledge.org -> Linking",
			ThemeGroup.LINK);
	/** Mapping to DBpedia instances */
	public static final Theme YAGODBPEDIAINSTANCES = new Theme(
			"yagoDBpediaInstances",
			"Mappings of YAGO instances to DBpedia instances", ThemeGroup.LINK);

	@Override
	public Set<Theme> output() {
		return new FinalSet<>(YAGODBPEDIACLASSES, YAGODBPEDIAINSTANCES);
	}

	@Override
	public void extract() throws Exception {
		Announce.doing("Mapping instances");
		Set<String> instances = new TreeSet<>();
		for (Fact fact : CoherentTypeExtractor.YAGOTYPES) {
			if (!fact.getRelation().equals(RDFS.type)
					|| instances.contains(fact.getArg(1)))
				continue;
			if (!fact.getArg(2).startsWith("<wikicat_"))
				continue;
			if (!fact.getArg(1).startsWith("<"))
				continue;
			String dbp = FactComponent.forUri("http://dbpedia.org/resource/"
					+ FactComponent.stripBrackets(fact.getArg(1)));
			YAGODBPEDIAINSTANCES.write(new Fact(fact.getArg(1), "owl:sameAs",
					dbp));
			instances.add(fact.getArg(1));
		}
		Announce.done();
		Announce.doing("Mapping classes");
		instances = new TreeSet<>();
		for (Fact fact : ClassExtractor.YAGOTAXONOMY) {
			if (!fact.getRelation().equals(RDFS.subclassOf)
					|| instances.contains(fact.getArg(1)))
				continue;
			if (!fact.getArg(1).startsWith("<"))
				continue;
			String dbp = FactComponent.dbpediaClassForYagoClass(fact.getArg(1));
			YAGODBPEDIACLASSES.write(new Fact(fact.getArg(1),
					"owl:equivalentClass", dbp));
			instances.add(fact.getArg(1));
		}
		Announce.done();
	}

	public static void main(String[] args) throws Exception {
		new DBpediaLinker().extract(new File("c:/fabian/data/yago2s"), "test");
	}
}
