package com.sebastian.gombert.rewilida.imprt;

import java.io.IOException;

public class PersonContainer extends NamedEntityContainer {

	public PersonContainer() throws IOException {
		super();
		this.setType("per");
		
		this.setQuery(loadSparqlQuery("person.sparql"));
	}
}
