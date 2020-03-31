package com.sebastian.gombert.rewilida.imprt;

import java.io.IOException;

public class LocationContainer extends NamedEntityContainer {

	public LocationContainer() throws IOException {
		super();
		this.setType("loc");
		
		this.setQuery(loadSparqlQuery("location.sparql"));
	}
}
