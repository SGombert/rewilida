package com.sebastian.gombert.rewilida.core;

import java.util.HashMap;
import java.util.List;

public class ApplicationState {
	
	public static final ApplicationState APPLICATION_StATE = new ApplicationState();

	private HashMap<String,Object> registry = new HashMap<>();
	
	public void registerObject(String key, Object o) {
		this.registry.put(key, o);
	}
	
	public Object getObject(String key) {
		return this.registry.get(key);
	}
	
}
