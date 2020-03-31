package com.sebastian.gombert.rewilida.core;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	public static void main(String...args) {
		launch(args);
	}

	@Override
	public void start(Stage arg0) throws Exception {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/interface.fxml"));		
		loader.setController(new MainWindowController());
		
		Parent root = (Parent) loader.load();
		
		MainWindowController ctrl = loader.<MainWindowController>getController();
		
		ctrl.init(arg0);
		
		Scene scene = new Scene(root);
		arg0.setTitle("ReWiLiDa");
		arg0.setScene(scene);
		arg0.show();
	}
}
