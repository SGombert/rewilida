package com.sebastian.gombert.rewilida.imprt;

import java.io.IOException;

public class MiscContainer extends NamedEntityContainer {

	public MiscContainer() throws IOException {
		super();
		this.setType("misc");
		
		this.setQuery(loadSparqlQuery("location.sparql"));
	}
}
