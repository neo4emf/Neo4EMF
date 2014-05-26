package fr.inria.atlanmod.neo4emf.persistence;

import fr.inria.atlanmod.neo4emf.persistence.neo4j.Neo4jConnectionFactory;

public class PersistenceConnectionFactory {

	public enum PersistenceBackend {
		NEO4J
	}
	
	public static IPersistenceConnection createConnection(PersistenceConfiguration configuration) {
		PersistenceBackend backend = configuration.backend();
		switch(backend) {
			case NEO4J:
				return Neo4jConnectionFactory.createNeo4jConnection(configuration);
			default:
				throw new IllegalArgumentException("Unsupported backend, cannot build the connection");
		}
	}

}
