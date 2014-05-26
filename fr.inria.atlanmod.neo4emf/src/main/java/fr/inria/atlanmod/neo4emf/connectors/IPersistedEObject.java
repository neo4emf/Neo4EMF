package fr.inria.atlanmod.neo4emf.connectors;


public interface IPersistedEObject {
	
	/**
	 * @return a long unique identifier for the back-end persisted object
	 */
	public long getId();
}
