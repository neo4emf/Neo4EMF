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
package fr.inria.atlanmod.neo4emf.persistence.neo4j;

import java.util.ArrayList;
import java.util.List;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.index.lucene.LuceneKernelExtensionFactory;
import org.neo4j.kernel.extension.KernelExtensionFactory;
import org.neo4j.kernel.impl.cache.CacheProvider;
import org.neo4j.kernel.impl.cache.WeakCacheProvider;

import fr.inria.atlanmod.neo4emf.persistence.IPersistedEObject;
import fr.inria.atlanmod.neo4emf.persistence.IPersistenceConnection;
import fr.inria.atlanmod.neo4emf.persistence.PersistenceConfiguration;

public class Neo4jConnectionFactory {
	
	public static IPersistenceConnection createNeo4jConnection(PersistenceConfiguration configuration) {
		// the cache providers
		ArrayList<CacheProvider> cacheList = new ArrayList<CacheProvider>();
		System.out.println("Creating db configuration");
		cacheList.add(new WeakCacheProvider());

		// the kernel extensions
		LuceneKernelExtensionFactory lucene = new LuceneKernelExtensionFactory();
		List<KernelExtensionFactory<?>> extensions = new ArrayList<KernelExtensionFactory<?>>();
		extensions.add(lucene);
			
//		Map<String,String> config = new HashMap<String,String>();
//				config.put("cache_type", "weak");

		// the database setup
		GraphDatabaseFactory gdbf = new GraphDatabaseFactory();
		gdbf.setKernelExtensions(extensions);
		gdbf.setCacheProviders(cacheList);
		GraphDatabaseService db = null;
		try {
			db = gdbf.newEmbeddedDatabaseBuilder(configuration.path().getAbsolutePath())
					.setConfig(GraphDatabaseSettings.cache_type, WeakCacheProvider.NAME)
					.newGraphDatabase();
		}catch(Exception e) {
			e.printStackTrace();
		}
		IPersistenceConnection connection = new Neo4jConnection(db, configuration);
		return connection;
	}

}
