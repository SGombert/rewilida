package com.sebastian.gombert.rewilida.imprt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class NamedEntityContainer {
	
	private String name;
	
	private String query;
	
	private String disambiguationQuery;
	
	private String type;
	
	private String htmlCache;
	
	private int idInText;
	
	private HashMap<String,Integer> contextualFreqDist = new HashMap<>();
	
	private List<String> props = new ArrayList<>();
	
	private String method;
	
	private String infoAcq;
	
	private String entityTitle;
	
	protected static String loadSparqlQuery(String fileName) throws IOException {
		File fl = new File(ToHTMLConsumer.class.getClassLoader().getResource(fileName).getPath());
		BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(fl), "utf8"));
		StringBuilder strBuild = new StringBuilder();
		
		read.lines().forEach(line -> {
			strBuild.append(line);
			strBuild.append(' ');
		});
		
		read.close();
		
		return strBuild.toString();
	}
	
	public NamedEntityContainer() throws IOException {
		this.disambiguationQuery = loadSparqlQuery("disambig.sparql");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getQuery() {
		String[] nameParts = this.name.split(" ");
		StringBuilder queryNameBuilder = new StringBuilder();
		boolean moreThanOne = false;
		for (String namePart : nameParts) {
			queryNameBuilder.append(namePart.substring(0, 1).toUpperCase());
			queryNameBuilder.append(namePart.substring(1));
			
			if (moreThanOne)
				queryNameBuilder.append(' ');
			else
				moreThanOne = true;		
		}
		if (queryNameBuilder.charAt(queryNameBuilder.length() -1) == ' ')
			queryNameBuilder.setLength(queryNameBuilder.length() -1);

		
		return query.replace("[[ENT]]", queryNameBuilder.toString());
	}
	
	public String getNormalQuery() {
		String[] nameParts = this.name.split(" ");
		StringBuilder queryNameBuilder = new StringBuilder();
		boolean moreThanOne = false;
		for (String namePart : nameParts) {
			queryNameBuilder.append(namePart.substring(0, 1).toUpperCase());
			queryNameBuilder.append(namePart.substring(1));
			
			if (moreThanOne)
				queryNameBuilder.append(' ');
			else
				moreThanOne = true;		
		}
		if (queryNameBuilder.charAt(queryNameBuilder.length() -1) == ' ')
			queryNameBuilder.setLength(queryNameBuilder.length() -1);

		
		return query;		
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof NamedEntityContainer && ((NamedEntityContainer)obj).getName().equals(this.getName()));
	}

	public List<String> getProps() {
		return props;
	}

	public void setProps(List<String> props) {
		this.props = props;
	}

	public String getDisambiguationQuery() {
		String[] nameParts = this.name.split(" ");
		StringBuilder queryNameBuilder = new StringBuilder();
		boolean moreThanOne = false;
		for (String namePart : nameParts) {
			queryNameBuilder.append(namePart.substring(0, 1).toUpperCase());
			queryNameBuilder.append(namePart.substring(1));
			
			if (moreThanOne)
				queryNameBuilder.append(' ');
			else
				moreThanOne = true;		
		}
		if (queryNameBuilder.charAt(queryNameBuilder.length() -1) == ' ')
			queryNameBuilder.setLength(queryNameBuilder.length() -1);
		
		return disambiguationQuery.replace("[[ENT]]", queryNameBuilder.toString());
	}

	public void setDisambiguationQuery(String disambiguationQuery) {
		this.disambiguationQuery = disambiguationQuery;
	}

	public String getHtmlCache() {
		return htmlCache;
	}

	public void setHtmlCache(String htmlCache) {
		this.htmlCache = htmlCache;
	}

	public int getIdInText() {
		return idInText;
	}

	public void setIdInText(int idInText) {
		this.idInText = idInText;
	}

	public HashMap<String,Integer> getContextualFreqDist() {
		return contextualFreqDist;
	}

	public void setContextualFreqDist(HashMap<String,Integer> contextualFreqDist) {
		this.contextualFreqDist = contextualFreqDist;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getInfoAcq() {
		return infoAcq;
	}

	public void setInfoAcq(String infoAcq) {
		this.infoAcq = infoAcq;
	}

	public String getEntityTitle() {
		return entityTitle;
	}

	public void setEntityTitle(String entityTitle) {
		this.entityTitle = entityTitle;
	}

	
}
