/**
 * Copyright (c) 2013 Atlanmod INRIA LINA Mines Nantes
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    Atlanmod INRIA LINA Mines Nantes - initial API and implementation
 * Descritpion ! To come
 * @author Sunye
 */
package fr.inria.atlanmod.neo4emf.persistence;

import java.io.File;
import java.util.Map;

import org.eclipse.emf.common.util.URI;

import fr.inria.atlanmod.neo4emf.PersistentPackage;
import fr.inria.atlanmod.neo4emf.persistence.PersistenceConnectionFactory.PersistenceBackend;

public class PersistenceConfiguration {
	
	private final PersistentPackage ePackage;
	
	private final URI uri;
	
	private final PersistenceBackend backend;
	
	private final Map<String,Object> options;
	
	private final File path;
	
	
	public PersistenceConfiguration(PersistentPackage ep, URI uri, PersistenceBackend backend, Map<String,Object> map) {
		this.ePackage = ep;
		this.uri = uri;
		this.backend = backend;
		this.options = map;
		
		String name = uri.isHierarchical() ? uri.path() : uri.opaquePart();
		path = new File(name);
	}
	
	public PersistentPackage ePackage() {
		return ePackage;
	}
	
	public URI uri() {
		return uri;
	}
	
	public Map<String,Object> options() {
		return options;
	}
	
	public File path() {
		return path;
	}
	
	public PersistenceBackend backend() {
		return backend;
	}
}
