package com.sebastian.gombert.rewilida.imprt;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.tcas.Annotation;
import org.apache.uima.resource.ResourceInitializationException;

import com.sebastian.gombert.rewilida.core.ApplicationState;
import com.sebastian.gombert.rewilida.core.LinkedDataEntity;
import com.sebastian.gombert.rewilida.core.TextDocument;
import com.sebastian.gombert.rewilida.core.VectorSpace;

import de.tudarmstadt.ukp.dkpro.core.api.ner.type.NamedEntity;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Lemma;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Paragraph;
import de.tudarmstadt.ukp.dkpro.core.api.segmentation.type.Token;

import javafx.collections.*;

public class ToHTMLConsumer extends JCasAnnotator_ImplBase {
	
	private boolean addIfNotThere(List<NamedEntityContainer> ents, NamedEntityContainer cont, boolean neOrLik) {
		for (NamedEntityContainer ent : ents)
			if (ent.getType().equals(cont.getType()) && ent.getName().equals(cont.getName()))
				return false;
		ents.add(cont);
		this.accepted += 1;
		if (neOrLik)
			this.neAcceppted += 1;
		else
			this.likelihoodAccepted += 1;
		
		return true;
	}
	
	private void addToken(String token, StringBuilder strBuild, TextDocument td, String lemma) {
		strBuild.append(token);
		strBuild.append(' ');
		
		td.accountForToken(lemma);
	}

	private void addTokenNoAccount(String token, StringBuilder strBuild, TextDocument td, String lemma) {
		strBuild.append(token);
		strBuild.append(' ');
		
		//td.accountForToken(lemma);
	}
	
	private void beginDiv(String kind, StringBuilder strBuild) {
		strBuild.append('<');
		strBuild.append(kind);
		strBuild.append('>');
	}
	
	private void endDiv(String kind, StringBuilder strBuild) {
		strBuild.append("</");
		strBuild.append(kind);
		strBuild.append('>');
	}
	
	private boolean jCasContains(Class<? extends Annotation> anno, JCas aJCas) {
		return JCasUtil.select(aJCas, anno).size() > 0;
	}
	
	private boolean tokenBelongsToNamedEntity(Token tk, JCas aJCas) {
		return JCasUtil.selectCovered(aJCas, NamedEntity.class, tk).size() > 0 && !(JCasUtil.selectCovered(aJCas, NamedEntity.class, tk).get(0).getValue().equals("O"));
	}
	
	private void beginLink(String type, String name, StringBuilder strBuild) {
		strBuild.append("<a href=\"neresolve:");
		strBuild.append(type);
		strBuild.append(':');
		strBuild.append(name);
		strBuild.append("\">");
	}
	
	private boolean namedEntityOpen = false;
	
	private NamedEntityContainer activeContainer;
	
	private StringBuilder strBuild = new StringBuilder();
	
	private StringBuilder buildTempYes = new StringBuilder();
	
	private StringBuilder buildTempNo = new StringBuilder();
	
	private void addLinkStuff(Token tk, JCas aJCas, StringBuilder namedEntityNameBuiler, List<NamedEntityContainer> neContainers, TextDocument td, int tokenId) throws IOException {
			
		if (tokenBelongsToNamedEntity(tk, aJCas) && !namedEntityOpen) {
			namedEntityOpen = true;
			
			NamedEntity ne = JCasUtil.selectCovered(aJCas, NamedEntity.class, tk).get(0);
			
			if (ne.getValue().contains("PER")) {
				activeContainer = new PersonContainer();
			} else if (ne.getValue().contains("ORG")) {
				activeContainer = new OrganizationContainer();
			} else if (ne.getValue().contains("LOC")) {
				activeContainer = new LocationContainer();
			} else if (ne.getValue().contains("MISC")) {
				activeContainer = new MiscContainer();
			}
			
			activeContainer.setMethod("Named Entity");
			this.candidates += 1;
			this.neCandida += 1;
			
			namedEntityNameBuiler.append(JCasUtil.selectCovered(aJCas, Lemma.class, tk).get(0).getValue());
			namedEntityNameBuiler.append(' ');
			
			beginLink(activeContainer.getType(), "[[NAME]]", buildTempYes);
			
			activeContainer.setIdInText(tokenId);		

			addToken(tk.getCoveredText(), buildTempYes, td, JCasUtil.selectCovered(aJCas, Lemma.class, tk).get(0).getValue());
			addTokenNoAccount(tk.getCoveredText(), buildTempNo, td, JCasUtil.selectCovered(aJCas, Lemma.class, tk).get(0).getValue());	
			
		} else if (tokenBelongsToNamedEntity(tk, aJCas) && namedEntityOpen) {
			namedEntityNameBuiler.append(tk.getCoveredText());
			namedEntityNameBuiler.append(' ');
			
			addToken(tk.getCoveredText(), buildTempYes, td, JCasUtil.selectCovered(aJCas, Lemma.class, tk).get(0).getValue());
			addTokenNoAccount(tk.getCoveredText(), buildTempNo, td, JCasUtil.selectCovered(aJCas, Lemma.class, tk).get(0).getValue());	
		} else if (!tokenBelongsToNamedEntity(tk, aJCas) && namedEntityOpen){
			activeContainer.setName(namedEntityNameBuiler.toString());
			namedEntityNameBuiler.setLength(0);
			
			
			
			namedEntityOpen = false;		
			
			if (LinkedDataEntity.exists(activeContainer, aJCas.getDocumentLanguage())) {
				endDiv("a", buildTempYes);
				String temp = buildTempYes.toString();
				temp = temp.replace("[[NAME]]", activeContainer.getName());
				strBuild.append(temp);
				addIfNotThere(neContainers, activeContainer, true);
			} else
				strBuild.append(buildTempNo);
			
			buildTempNo = new StringBuilder();
			buildTempYes = new StringBuilder();

			addToken(tk.getCoveredText(), strBuild, td, JCasUtil.selectCovered(aJCas, Lemma.class, tk).get(0).getValue());
			
			
		} else {
			addToken(tk.getCoveredText(), strBuild, td, JCasUtil.selectCovered(aJCas, Lemma.class, tk).get(0).getValue());
		}
	}
	
	private HashMap<String,String> readBackgroundCorpus(String backgroundCorpusName) throws IOException, URISyntaxException {
		HashMap<String,String> mappinger = new HashMap<>();
		
		System.out.println(new File(getClass().getResource(backgroundCorpusName).toURI()).exists());

		for (File fl  : new File(getClass().getResource(backgroundCorpusName).toURI()).listFiles()) {

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
	
	private HashMap<String,Integer> accumulateFreqDists(HashMap<String,HashMap<String,Integer>> freqDists) {
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
	
	private int getNumWords(HashMap<String,Integer> freq) {
		int numWords = 0;
		for (Integer val : freq.values())
			numWords += val;
		return numWords;
	}
	
	private HashMap<String,Integer> readInFreqDist(String fileName) throws URISyntaxException, IOException {
		File fl  = new File(getClass().getResource(fileName).toURI());
		BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(fl), "utf8"));
		
		HashMap<String,Integer> ret = new HashMap<>();
		read.lines().forEach(line -> {
			if (line.replace("\n", "").length() > 0) {
				String[] split = line.replace("\n", "").split("\t");
				ret.put(split[0], Integer.parseInt(split[1]));
			}
		});
		
		read.close();
		
		return ret;
	}
	
	private int candidates = 0;
	
	private int accepted = 0;
	
	private int neCandida = 0;
	
	private int neAcceppted = 0;
	
	private int likelihoodCandida = 0;
	
	private int likelihoodAccepted = 0;

	@SuppressWarnings("unchecked")
	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		
		TextDocument doc = new TextDocument();
		
		StringBuilder namedEntityNameBuiler = new StringBuilder();
		List<NamedEntityContainer> neContainers = new ArrayList<>();

		doc.setNamedEntities(neContainers);		
		
		beginDiv("html", strBuild);
		beginDiv("head", strBuild);
		beginDiv("style", strBuild);
		endDiv("style", strBuild);
		endDiv("head", strBuild);
		beginDiv("body", strBuild);
		
		int tokenId = 0;
		
		try {
			if (jCasContains(Paragraph.class, aJCas)) {
				for (Paragraph p : JCasUtil.select(aJCas, Paragraph.class)) {
					beginDiv("p", strBuild);
					
					for (Token tk : JCasUtil.selectCovered(aJCas, Token.class, p)) {
						addLinkStuff(tk, aJCas, namedEntityNameBuiler, neContainers, doc, tokenId);
						tokenId++;
					}
					
					endDiv("p", strBuild);
				}
			} else {
				for (Token tk : JCasUtil.select(aJCas, Token.class)) {
					addLinkStuff(tk, aJCas, namedEntityNameBuiler, neContainers, doc, tokenId);
					tokenId++;
				}
			}
			endDiv("body", strBuild);
			endDiv("html", strBuild);
			
			if (null == ApplicationState.APPLICATION_StATE.getObject("TEXTS"))
				ApplicationState.APPLICATION_StATE.registerObject("TEXTS", FXCollections.observableList(new ArrayList<TextDocument>()));		

			
			doc.setLanguage(aJCas.getDocumentLanguage());
			doc.setHtmlString(strBuild.toString());
			
			HashMap<String,Integer> corpusFreqDist = readInFreqDist("/goethe_freqs.txt");
			int numWordsBG = getNumWords(corpusFreqDist);
			int numForeground = getNumWords(doc.wordFreqs());
			
			String htmlString = strBuild.toString();
			
			List<Lemma> lemmata = new ArrayList<>(JCasUtil.select(aJCas, Lemma.class));
			
			int j = 0;
			for (Lemma lem : lemmata) {				
				double likelihood = ((double)(corpusFreqDist.containsKey(lem.getValue()) ? corpusFreqDist.get(lem.getValue()) : 0) 
						+ (double)(doc.wordFreqs().containsKey(lem.getValue()) ? doc.wordFreqs().get(lem.getValue()) : 0)) /
						(numWordsBG + numForeground);
				
				double singleLikelihood = ((double)(doc.wordFreqs().containsKey(lem.getValue()) ? doc.wordFreqs().get(lem.getValue()) : 0)) / numForeground;
				
				if (likelihood <= singleLikelihood / 8) {
					NamedEntityContainer misc = new MiscContainer();
					misc.setIdInText(j);
					misc.setName(lem.getValue());
					misc.setMethod("Likelihood");
					this.candidates += 1;
					this.likelihoodCandida += 1;
					
					if (LinkedDataEntity.exists(misc, aJCas.getDocumentLanguage())) {
						boolean added = addIfNotThere(neContainers, misc, false);
						if (added) {
							
							for (Lemma le : lemmata) {
								if (le.getValue().equals(lem.getValue())) {
									String coveredText = le.getCoveredText();
									StringBuilder st = new StringBuilder();
									beginLink(misc.getType(), le.getValue(), st);
									st.append(coveredText);
									endDiv("a", st);
									doc.setHtmlString(doc.getHtmlString().replace(" " + coveredText, " " + st.toString()));
								}		
							}
						}
					}
				}
				
				j++;
				System.out.println(j + "/" + lemmata.size());
			}
			
			for (NamedEntityContainer cont : neContainers) {
				int begin = cont.getIdInText() - 150;
				int end = cont.getIdInText() + 150;
				
				if (begin < 0) begin = 0;
				if (end >= lemmata.size() -1) end = lemmata.size() -1;
				
				for (int i = begin; i <= end; i++) {
					String lemma = lemmata.get(i).getValue();
					if (cont.getContextualFreqDist().containsKey(lemma))
						cont.getContextualFreqDist().put(lemma, cont.getContextualFreqDist().get(lemma) + 1);
					else
						cont.getContextualFreqDist().put(lemma, 1);
				}
			}
			
			System.out.println("Candidates: " + this.candidates);
			System.out.println("Accepted: " + this.accepted);
			
			System.out.println("NE Candidates: " + this.neCandida);
			System.out.println("NE Accepted: " + this.neAcceppted);
			
			System.out.println("Lilelihood Candidates: " + this.likelihoodCandida);
			System.out.println("Likelihood Accepted: " + this.likelihoodAccepted);
			
			((ObservableList<TextDocument>)ApplicationState.APPLICATION_StATE.getObject("TEXTS")).add(doc);

		} catch (IOException | URISyntaxException e) {
			throw new AnalysisEngineProcessException(e);
		}
	}

}
