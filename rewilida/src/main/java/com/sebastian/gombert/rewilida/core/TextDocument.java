package com.sebastian.gombert.rewilida.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.uima.jcas.JCas;

import com.sebastian.gombert.rewilida.imprt.NamedEntityContainer;

public class TextDocument {

	private String name;
	
	private String filename;
	
	private String htmlString;
	
	private String language;
	
	private HashMap<String,Integer> wordFreqCount = new HashMap<>();
	
	private List<NamedEntityContainer> namedEntities;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getHtmlString() {
		return htmlString;
	}

	public void setHtmlString(String htmlString) {
		this.htmlString = htmlString;
	}

	public List<NamedEntityContainer> getNamedEntities() {
		return namedEntities;
	}

	public void setNamedEntities(List<NamedEntityContainer> namedEntities) {
		this.namedEntities = namedEntities;
	}
	
	public void accountForToken(String tk) {
		if (wordFreqCount.containsKey(tk))
			wordFreqCount.put(tk, wordFreqCount.get(tk) +1);
		else
			wordFreqCount.put(tk, 1);
	}
	
	public Set<String> getTypes() {
		return this.wordFreqCount.keySet();
	}
	
	public HashMap<String,Integer> wordFreqs() {
		return this.wordFreqCount;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
	
	
}
