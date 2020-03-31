package com.sebastian.gombert.rewilida.core;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;

public class FrequencyDistributionConsumer extends JCasAnnotator_ImplBase {
	
	private void addOrRaise(HashMap<String,Integer> map, String key) {
		if (map.containsKey(key))
			map.put(key, map.get(key) + 1);
		else
			map.put(key, 1);
	}

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		HashMap<String,Integer> freqDist = new HashMap<>();
		
		for (Lemma lem : JCasUtil.select(aJCas, Lemma.class))
			addOrRaise(freqDist, lem.getValue());
		
		String documentTitle = (new ArrayList<>(JCasUtil.select(aJCas, DocumentMetaData.class))).get(0).getDocumentTitle();
		
		VectorSpace.freqDistributions.put(documentTitle,freqDist);
	}

}
