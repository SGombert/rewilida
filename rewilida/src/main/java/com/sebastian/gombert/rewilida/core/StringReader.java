package com.sebastian.gombert.rewilida.core;

import java.io.IOException;
import java.util.HashMap;

import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.component.JCasCollectionReader_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Progress;

import de.tudarmstadt.ukp.dkpro.core.api.metadata.type.DocumentMetaData;

public class StringReader extends JCasCollectionReader_ImplBase {
	
	public static final String INPUT_TEXT = "input_text";
	@ConfigurationParameter(name = INPUT_TEXT, description = "input_text", mandatory = true)
	private String[] inputTexts;
	
	public static final String TEXT_NAMES = "text_names";
	@ConfigurationParameter(name = TEXT_NAMES, description = "text_names", mandatory = true)
	private String[] textNames;	
	
	public static final String LANG = "lang";
	@ConfigurationParameter(name = LANG, description = "input_text", mandatory = true)
	private String lang;

	private int ptr = 0;
	
	@Override
	public boolean hasNext() throws IOException, CollectionException {
		return ptr < inputTexts.length;
	}

	@Override
	public Progress[] getProgress() {
		return null;
	}

	@Override
	public void getNext(JCas jCas) throws IOException, CollectionException {
		
		jCas.setDocumentText(inputTexts[ptr]);
		
		jCas.setDocumentLanguage(lang);
		
		DocumentMetaData meta = new DocumentMetaData(jCas);
		meta.setDocumentTitle(textNames[ptr]);
		meta.setBegin(0);
		meta.setEnd(inputTexts[ptr].length() -1);
		meta.addToIndexes();
		
		ptr++;

	}


}
