package com.sebastian.gombert.rewilida.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.resource.ResourceInitializationException;

public class BGCorpusPreparer {
	
	private static HashMap<String,String> readBackgroundCorpus(String backgroundCorpusName) throws IOException, URISyntaxException {
		HashMap<String,String> mappinger = new HashMap<>();
		
		System.out.println(new File(BGCorpusPreparer.class.getResource(backgroundCorpusName).toURI()).exists());

		for (File fl  : new File(BGCorpusPreparer.class.getResource(backgroundCorpusName).toURI()).listFiles()) {

			BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(fl), "utf8"));
			StringBuilder strBuild = new StringBuilder();
			
			read.lines().forEach(line -> {
				strBuild.append(line);
				strBuild.append(' ');
			});
			
			read.close();
			
			mappinger.put(fl.getName(), strBuild.toString());
		}	
		
		return mappinger;
	}
	
	private static HashMap<String,Integer> accumulateFreqDists(HashMap<String,HashMap<String,Integer>> freqDists) {
		HashMap<String,Integer> ret = new HashMap<>();
		
		for (HashMap<String,Integer> dist : freqDists.values()) {
			for (Entry<String,Integer> ent : dist.entrySet()) {
				if (ret.containsKey(ent.getKey()))
					ret.put(ent.getKey(),ret.get(ent.getKey()) + ent.getValue());
				else
					ret.put(ent.getKey(),ent.getValue());
			}
		}
		
		return ret;
	}

	public static void main(String... args) throws IOException, URISyntaxException, AnalysisEngineProcessException, ResourceInitializationException, CollectionException {
		HashMap<String,String> bgCorpusTexts = readBackgroundCorpus("/goethezeit/");
		
		HashMap<String,HashMap<String,Integer>> freqs = VectorSpace.textsToFreqDists(bgCorpusTexts, "de");
		HashMap<String,Integer> accumulatedFreqDist = accumulateFreqDists(freqs);
		
		BufferedWriter write = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("D:/goethe_freqs.txt"), "utf8"));
		for (Entry<String,Integer> ent : accumulatedFreqDist.entrySet()) {
			write.write(ent.getKey().replace('\n', ' '));
			write.write('\t');
			write.write(ent.getValue().toString());
			write.newLine();
		}
		write.close();
		
	}
}
