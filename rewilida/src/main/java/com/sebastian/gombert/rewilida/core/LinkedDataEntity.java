package com.sebastian.gombert.rewilida.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.resource.ResourceInitializationException;

import com.sebastian.gombert.rewilida.imprt.NamedEntityContainer;
import com.sebastian.gombert.rewilida.imprt.ToHTMLConsumer;

public class LinkedDataEntity {
	public static final String SERVICE = "http://dbpedia.org/sparql";
	
	public static List<LinkedDataEntity> getDisambigEntities(String disambiguationQuery, String normalQuery, String langCode, List<String> propKeys) {
		String disambig = "disambiguation";
		switch (langCode) {
		case "de": disambig = "Begriffsklärung"; break;
		case "en": disambig = "disambiguation"; break;
		}
		
		QueryExecution qe = QueryExecutionFactory.sparqlService(SERVICE, disambiguationQuery.replace("LANG", langCode).replace("[[DISAMBIG]]", disambig));
		ResultSet res = qe.execSelect();
		
		List<LinkedDataEntity> entities = new ArrayList<>();
		
		while (res.hasNext()) {
			String indivQuery = normalQuery.replace("[[ENT]]", res.nextSolution().get("label").asLiteral().getString());
			
			List<LinkedDataEntity> enene = fromSPARQLQuery(indivQuery, propKeys, langCode);
			LinkedDataEntity ent = enene.size() > 0 ? enene.get(0) : null;
			
			if (null != ent) entities.add(ent);
		}
		
		return entities;
	}
	
	public static boolean exists(NamedEntityContainer ne, String langCode) {
		String disambig = "disambiguation";
		switch (langCode) {
		case "de": disambig = "Begriffsklärung"; break;
		case "en": disambig = "disambiguation"; break;
		}
		
		QueryExecution qe = QueryExecutionFactory.sparqlService(SERVICE, ne.getQuery().replace("LANG", langCode));
		ResultSet res = qe.execSelect();
		
		if (res.hasNext()) return true;
		
		qe = QueryExecutionFactory.sparqlService(SERVICE, ne.getDisambiguationQuery().replace("LANG", langCode).replace("[[DISAMBIG]]", disambig));
		res = qe.execSelect();
		
		if (res.hasNext()) return true;
		
		return false;
	}
	
	public static List<LinkedDataEntity> fromSPARQLQuery(String query, List<String> propKeys, String langCode) {
		QueryExecution qe = QueryExecutionFactory.sparqlService(SERVICE, query.replace("LANG", langCode));
		ResultSet res = qe.execSelect();
		List<LinkedDataEntity> entities = new ArrayList<>();
		
		while (res.hasNext()) {
			QuerySolution sol = res.nextSolution();
			
			LinkedDataEntity ent = new LinkedDataEntity();
			ent.setAbstractText(sol.get("abstract").asLiteral().getString());
			ent.setName(sol.get("name").asLiteral().getString());
			
			Set<String> props = new HashSet<>();
			props.add("loc");
			
			for (String propKey : props)
				if (null != sol.get(propKey) && sol.get(propKey).isLiteral())
					ent.registerProperty(propKey, sol.get(propKey).asLiteral().getString());
			
			entities.add(ent);
		}
		
		return entities;
	}
	
	public static LinkedDataEntity getMostProbableEntity(HashMap<String,Integer> mainTextFreqDist, List<LinkedDataEntity> ents, String langCode) throws AnalysisEngineProcessException, ResourceInitializationException, CollectionException, IOException {
		Set<String> types = new HashSet<>();
		HashMap<String,String> texts = new HashMap<>();
		for (LinkedDataEntity ent : ents)
			texts.put(ent.getName(), ent.abstractText);	
		
		HashMap<String,HashMap<String,Integer>> freqDists = VectorSpace.textsToFreqDists(texts, langCode);
		
		for (HashMap<String,Integer> freqs : freqDists.values())
			types.addAll(freqs.keySet());
		
		VectorSpace vecSpace = new VectorSpace(types, freqDists.size() + 1);
		
		List<String> textIds = vecSpace.getSortedTexts(freqDists, mainTextFreqDist);
		
		for (LinkedDataEntity en : ents)
			if (en.getName().equals(textIds.get(0)))
				return en;
		return null;
	}

	private String name;
	
	private String abstractText;
	
	private HashMap<String,String> properties = new HashMap<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAbstractText() {
		return abstractText;
	}

	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText;
	}
	
	public void registerProperty(String key, String value) {
		this.properties.put(key, value);
	}
	
	protected static String loadGoogleMaps(String fileName, String latlong) throws IOException {
		File fl = new File(ToHTMLConsumer.class.getClassLoader().getResource(fileName).getPath());
		BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(fl), "utf8"));
		StringBuilder strBuild = new StringBuilder();
		
		read.lines().forEach(line -> {
			strBuild.append(line);
			strBuild.append('\n');
		});
		
		read.close();
		
		return strBuild.toString().replace("[[LATLONG]]", latlong);
	}
	
	public String toHTML() throws IOException {
		StringBuilder strBuild = new StringBuilder();
		strBuild.append("<html>");
		strBuild.append("<title>");
		strBuild.append("</title>");
		strBuild.append("<body>");
		
		strBuild.append("<h1>");
		strBuild.append(this.name);
		strBuild.append("</h1>");
		
		strBuild.append("<p>");
		strBuild.append(this.abstractText);
		strBuild.append("</p>");
		
		if (this.properties.containsKey("loc")) {
			strBuild.append("<p>");
			String loc = this.properties.get("loc");
			strBuild.append(loadGoogleMaps("googleMaps.txt", loc.replace(' ', ',')));
			
			strBuild.append("</b>");
		}
		
		strBuild.append("</body>");
		strBuild.append("</html>");
		
		return strBuild.toString();
	}
}
