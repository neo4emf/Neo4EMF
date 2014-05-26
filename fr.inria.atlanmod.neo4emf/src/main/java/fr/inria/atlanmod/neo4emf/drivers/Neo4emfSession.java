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
package fr.inria.atlanmod.neo4emf.drivers;

import java.util.Collections;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;

import fr.inria.atlanmod.neo4emf.INeo4emfResource;
import fr.inria.atlanmod.neo4emf.INeo4emfResourceFactory;
import fr.inria.atlanmod.neo4emf.PersistentPackage;
import fr.inria.atlanmod.neo4emf.persistence.PersistenceConfiguration;

/**
 * 
 * @author sunye
 *
 */
public class Neo4emfSession {
	
	private ResourceSet resourceSet = new ResourceSetImpl();
	
	private PersistentPackage ePackage;
	
	private PersistenceConfiguration configuration;

    private INeo4emfResource resource;
	
	
	/**
	 * Creates a Neo4EMF session. 
	 * Presently, only one package can be used during a session.
	 * 
	 * @param ep The EPackage used to initialize the session. 
	 */
	public Neo4emfSession(PersistentPackage ep) {
		assert ep != null : "Null EPackage";
		
		ePackage = ep;
		resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put("neo4emf", 
				INeo4emfResourceFactory.eINSTANCE);
		EPackage.Registry.INSTANCE.put(ePackage.getNsURI(), ePackage);
	}
	
	/**
	 * Creates a resource from a URI.
	 * If the resource already exists, just opens it.
	 * @param uri
	 * @param options the options for the session
	 * @return The resource.
	 */

	public INeo4emfResource createResource(URI uri, Map<String,Object> options) {
		if(options == null) {
			options  = Collections.<String,Object>emptyMap();
		}
		configuration = new PersistenceConfiguration(ePackage, uri, options);
		INeo4emfResourceFactory.eINSTANCE.setConfiguration(configuration);
		resource = (INeo4emfResource) resourceSet.createResource(uri);
		resource.setURI(uri);
		return resource;
	}
	
	/**
	 * Closes the current session.
	 */
	public void close() {
		  resource.shutdown();
	}

}
