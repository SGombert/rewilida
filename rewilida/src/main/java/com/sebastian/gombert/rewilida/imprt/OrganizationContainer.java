package com.sebastian.gombert.rewilida.imprt;

import java.io.IOException;

public class OrganizationContainer extends NamedEntityContainer {

	public OrganizationContainer() throws IOException {
		super();
		this.setType("org");
		
		this.setQuery(loadSparqlQuery("organization.sparql"));
	}
	
}
