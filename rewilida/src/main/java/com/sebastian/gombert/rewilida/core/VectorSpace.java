package com.sebastian.gombert.rewilida.core;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.matetools.MateLemmatizer;
import org.dkpro.core.opennlp.OpenNlpLemmatizer;
import org.dkpro.core.opennlp.OpenNlpSegmenter;
import org.dkpro.core.textcat.LanguageIdentifier;

import java.util.Set;

public class VectorSpace {
	
	public static final HashMap<String,HashMap<String,Integer>> freqDistributions = new HashMap<>();
	
	private ArrayList<String> dimIdx;
	
	private HashMap<String,Integer> docFrequencies;
	
	private static AnalysisEngineDescription langIdent;
	
	private static AnalysisEngineDescription seg;
	
	private static AnalysisEngineDescription lemmatizer;
	
	private int numDocs;
	
	private void addOrRaise(HashMap<String,Integer> map, String key) {
		if (map.containsKey(key))
			map.put(key, map.get(key) + 1);
		else
			map.put(key, 1);
	}
	
	public VectorSpace(Set<String> types, int numDocs) throws ResourceInitializationException {
		if (langIdent == null || seg == null || lemmatizer == null) {
			seg = createEngineDescription(OpenNlpSegmenter.class);
			lemmatizer = createEngineDescription(MateLemmatizer.class);
		}
		
		this.docFrequencies = new HashMap<>();
		this.numDocs = numDocs;
		
		for (String ty : types) {
			addOrRaise(docFrequencies, ty);
		}
		
		this.dimIdx = new ArrayList<>(types);
	}
	
	public static HashMap<String,HashMap<String,Integer>> textsToFreqDists(HashMap<String,String> texts, String langCode) throws ResourceInitializationException, AnalysisEngineProcessException, CollectionException, IOException {
		if (langIdent == null || seg == null || lemmatizer == null) {
			seg = createEngineDescription(OpenNlpSegmenter.class);
			lemmatizer = createEngineDescription(MateLemmatizer.class);
		}
		
		String[] txts = new String[texts.size()];
		String[] textNames = new String[texts.size()];
		
		ArrayList<Entry<String,String>> ordered = new ArrayList<>(texts.entrySet());
		for (int i = 0; i < ordered.size(); i++) {
			if (!freqDistributions.containsKey(ordered.get(i).getKey())) {
				txts[i] = ordered.get(i).getValue();
				textNames[i] = ordered.get(i).getKey();
			}
		}
		
		CollectionReaderDescription read = createReaderDescription(StringReader.class,
				StringReader.INPUT_TEXT, txts,
				StringReader.TEXT_NAMES, textNames,
				StringReader.LANG, langCode);
		
		AnalysisEngineDescription consu = createEngineDescription(FrequencyDistributionConsumer.class);
		
		SimplePipeline.runPipeline(read, seg, lemmatizer, consu);
		
		HashMap<String,HashMap<String,Integer>> ret = new HashMap<>();
		for (String textName : textNames)
			ret.put(textName, freqDistributions.get(textName));
		
		return ret;
	}

	public double calculateCosineDist(HashMap<String,Integer> absoluteFrequenciesA, HashMap<String,Integer> absoluteFrequenciesB) {
		int nA = 0;
		for (Entry<String,Integer> ent : absoluteFrequenciesA.entrySet())
			nA += ent.getValue();
		if (nA == 0)
			nA = 1;
	
		int nB = 0;
		for (Entry<String,Integer> ent : absoluteFrequenciesB.entrySet())
			nB += ent.getValue();
		if (nB == 0)
			nB = 1;
		
		double sumAB = 0;
		double sumA = 0;
		double sumB = 0;
		
		for (String dim : dimIdx) {
			
			sumAB += ((((double)(absoluteFrequenciesA.containsKey(dim) ? absoluteFrequenciesA.get(dim) : 0) / nA) * Math.log(numDocs / docFrequencies.get(dim)))
					* (((double)(absoluteFrequenciesB.containsKey(dim) ? absoluteFrequenciesB.get(dim) : 0) / nB) * Math.log(numDocs / docFrequencies.get(dim))));

			sumA += (absoluteFrequenciesA.containsKey(dim) ? absoluteFrequenciesA.get(dim) : 0) ^ 2;
			sumB += (absoluteFrequenciesB.containsKey(dim) ? absoluteFrequenciesB.get(dim) : 0) ^ 2;
		}
		sumA = Math.sqrt(sumA);
		sumB = Math.sqrt(sumB);
		
		if (sumA > 0 && sumB > 0)
			return (sumAB / (sumA * sumB));
		else
			return Double.MAX_VALUE;
	}
	
	public List<String> getSortedTexts(HashMap<String,HashMap<String,Integer>> freqDists, HashMap<String,Integer> mainTextFreqDist) {
		List<String> ret = new ArrayList<>(freqDists.keySet());
		
		Collections.sort(ret, (a,b) -> {
			
			double retu = (calculateCosineDist(freqDists.get(a), mainTextFreqDist))
					- (calculateCosineDist(freqDists.get(b), mainTextFreqDist));
			
			return (int)(retu * 100000000);
		});
		
		return ret;
	}
	
}
