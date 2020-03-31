package com.sebastian.gombert.rewilida.core;

import static org.apache.uima.fit.factory.AnalysisEngineFactory.createEngineDescription;
import static org.apache.uima.fit.factory.CollectionReaderFactory.createReaderDescription;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.resource.ResourceInitializationException;
import org.dkpro.core.corenlp.CoreNlpNamedEntityRecognizer;
import org.dkpro.core.io.tei.TeiReader;
import org.dkpro.core.matetools.MateLemmatizer;
import org.dkpro.core.opennlp.OpenNlpLemmatizer;
import org.dkpro.core.opennlp.OpenNlpNamedEntityRecognizer;
import org.dkpro.core.opennlp.OpenNlpSegmenter;
import org.dkpro.core.textcat.LanguageIdentifier;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.EventListener;

import com.sebastian.gombert.rewilida.imprt.NamedEntityContainer;
import com.sebastian.gombert.rewilida.imprt.ToHTMLConsumer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class MainWindowController {
	
	private Stage thisStage;
	
	public void init(Stage thisStage) {
		this.thisStage = thisStage;
		
		MainWindowController _this = this;
		
		this.textView.getEngine().getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
			if (newState == Worker.State.SUCCEEDED) {
				EventListener listener = new EventListener() {
					@Override
					public void handleEvent(org.w3c.dom.events.Event evt) {
						String href = ((Element)evt.getTarget()).getAttribute("href");
						
						String[] hrefSplit = href.split(":");
						String namedEntityName = hrefSplit[2].replace('_', ' ');
						String type = hrefSplit[1];
						
						for (NamedEntityContainer ne : _this.namedEntities.getItems()) {
							if (ne.getName().toLowerCase().equals(namedEntityName.toLowerCase()) && ne.getType().equals(type)) {
								_this.namedEntities.getSelectionModel().select(ne);
								try {
									_this.updateInfoView(ne);
								} catch (AnalysisEngineProcessException | ResourceInitializationException
										| CollectionException | IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
				};

				Document doc = _this.textView.getEngine().getDocument();
				NodeList lista = doc.getElementsByTagName("a");
				for (int i = 0; i < lista.getLength(); i++)
					((EventTarget)lista.item(i)).addEventListener("click",listener, false);

			}
		});
		
		this.documents.setCellFactory(column -> {
		    return new ListCell<TextDocument>() {
		        @Override
		        protected void updateItem(TextDocument item, boolean empty) {
		            super.updateItem(item, empty);
		            
		            if (!empty) {
			            setText(item.getFilename());
			       
			            
			            this.setOnMouseClicked(e -> {
			            	_this.namedEntities.setItems(FXCollections.observableList(item.getNamedEntities()));
			            	_this.textView.getEngine().loadContent(item.getHtmlString());
			            });
		            } else {
		            	setText("");
		            }
		        }
		      
		    };
		});
		
		this.namedEntities.setCellFactory(column -> {
			return new ListCell<NamedEntityContainer>() {
		        @Override
		        protected void updateItem(NamedEntityContainer item, boolean empty) {
		            super.updateItem(item, empty);
		            
		            if (!empty) {
		            	
		            	
			            setText(item.getName().replace('_', ' ') + " (" + item.getType() + ")");
			            this.setOnMouseClicked(e -> {
			            	try {
								_this.updateInfoView(item);
							} catch (AnalysisEngineProcessException | ResourceInitializationException
									| CollectionException | IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
			            });
		            } else {
		            	setText("");
		            }
		        }				
			};
		});
		
		ObservableList<TextDocument> textDocuments = FXCollections.observableArrayList(new ArrayList<TextDocument>());
		
		ApplicationState.APPLICATION_StATE.registerObject("TEXTS", textDocuments);
		this.documents.setItems(textDocuments);
		
	}
	
	private void updateInfoView(NamedEntityContainer ne) throws AnalysisEngineProcessException, ResourceInitializationException, CollectionException, IOException {
		if (ne.getHtmlCache() != null) {
			this.infoView.getEngine().loadContent(ne.getHtmlCache());
		} else {
			List<LinkedDataEntity> ents = LinkedDataEntity.fromSPARQLQuery(ne.getQuery(), ne.getProps(), this.documents.getSelectionModel().getSelectedItem().getLanguage());
			
			if (ents.size() == 0) {
				List<LinkedDataEntity> entities = LinkedDataEntity.getDisambigEntities(ne.getDisambiguationQuery(), ne.getNormalQuery(), this.documents.getSelectionModel().getSelectedItem().getLanguage(), new ArrayList<>());
				
				if (entities.size() == 0) {
					this.infoView.getEngine().loadContent("");
					ne.setInfoAcq("NULL");
				} else if (entities.size() == 1) {
					LinkedDataEntity mostProb = entities.get(0);
					String html = mostProb.toHTML();
					this.infoView.getEngine().loadContent(html);
					ne.setHtmlCache(html);
					ne.setInfoAcq("Disambiguation");
					ne.setEntityTitle(mostProb.getName());
				} else if (entities.size() > 0) {
					LinkedDataEntity mostProb = LinkedDataEntity.getMostProbableEntity(ne.getContextualFreqDist(), entities, this.documents.getSelectionModel().getSelectedItem().getLanguage());
					String html = mostProb.toHTML();
					this.infoView.getEngine().loadContent(html);
					ne.setInfoAcq("Disambiguation");
					ne.setHtmlCache(html);
					ne.setEntityTitle(mostProb.getName());
				}
			} else {
				LinkedDataEntity mostProb = ents.get(0);
				String html = mostProb.toHTML();
				this.infoView.getEngine().loadContent(html);
				ne.setInfoAcq("By Label");
				ne.setHtmlCache(html);	
				ne.setEntityTitle(mostProb.getName());
			}
			
//			List<LinkedDataEntity> ents = LinkedDataEntity.fromSPARQLQuery(ne.getQuery(), ne.getProps(), this.documents.getSelectionModel().getSelectedItem().getLanguage());
//			
//			if (ents.size() == 0) {
//				this.infoView.getEngine().loadContent("");
//			} else if (ents.size() == 1) {
//				this.infoView.getEngine().loadContent(ents.get(0).toHTML());
//			} else {
//				this.infoView.getEngine().loadContent(
//						LinkedDataEntity.getMostProbableEntity(
//								this.documents.getSelectionModel().getSelectedItem().wordFreqs(), ents, this.documents.getSelectionModel().getSelectedItem().getLanguage()).toHTML()
//						);
//			}
		}
	}

	@FXML
	private ListView<TextDocument> documents;
	
	@FXML
	private ListView<NamedEntityContainer> namedEntities;
	
	@FXML
	private WebView textView;
	
	@FXML
	private WebView infoView;
	
	@FXML
	private void quit(Event e) {
		this.thisStage.close();
	}
	
	private void writeFoundEntities(String outPath) throws IOException {
		BufferedWriter write = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outPath), "utf8"));
		for (NamedEntityContainer ent : this.documents.getItems().get(this.documents.getItems().size() -1).getNamedEntities()) {
			write.write(ent.getName().replace('\n', ' '));
			write.write('\t');
			write.write(ent.getMethod());
			
			try {
				updateInfoView(ent);
			} catch (AnalysisEngineProcessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ResourceInitializationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CollectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			write.write('\t');
			write.write(ent.getInfoAcq());
			write.write('\t');
			
			write.write(ent.getEntityTitle());		
			
			write.newLine();
		}
		write.close();
	}
	
	@FXML
	private void openText(Event e) throws ResourceInitializationException, AnalysisEngineProcessException, CollectionException, IOException {
		FileChooser fileChooser = new FileChooser();
		
		File selectedFile = fileChooser.showOpenDialog(thisStage);
		
		if (null != selectedFile) {
			String path = selectedFile.getAbsolutePath();
			
			CollectionReaderDescription read = createReaderDescription(TeiReader.class,
					TeiReader.PARAM_SOURCE_LOCATION, path,
					TeiReader.PARAM_READ_POS, false,
					TeiReader.PARAM_READ_CONSTITUENT, false,
					TeiReader.PARAM_READ_NAMED_ENTITY, false,
					TeiReader.PARAM_READ_LEMMA, false,
					TeiReader.PARAM_READ_TOKEN, false,
					TeiReader.PARAM_READ_SENTENCE, false,
					TeiReader.PARAM_READ_PARAGRAPH, true,
					TeiReader.PARAM_OMIT_IGNORABLE_WHITESPACE, true				
					);
			
			AnalysisEngineDescription langIdent = createEngineDescription(LanguageIdentifier.class);
			
			AnalysisEngineDescription seg = createEngineDescription(OpenNlpSegmenter.class);
			
			AnalysisEngineDescription lemma = createEngineDescription(MateLemmatizer.class);

			AnalysisEngineDescription ner = createEngineDescription(CoreNlpNamedEntityRecognizer.class);

			AnalysisEngineDescription consu = createEngineDescription(ToHTMLConsumer.class);
			
			SimplePipeline.runPipeline(read, langIdent, seg, lemma, ner, consu);
			
			TextDocument doc = this.documents.getItems().get(this.documents.getItems().size() -1);
			doc.setName(selectedFile.getName());
			doc.setFilename(selectedFile.getName());
			this.documents.getItems().remove(doc);
			this.documents.getItems().add(doc);
			this.documents.getSelectionModel().select(this.documents.getItems().get(this.documents.getItems().size() -1));
			
			//writeFoundEntities("D:/selec_" + this.documents.getItems().get(this.documents.getItems().size() -1).getName() + ".txt");
		}
		
	}
}
