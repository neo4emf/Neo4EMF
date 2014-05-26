package fr.inria.atlanmod.neo4emf.persistence;

import java.util.List;

import fr.inria.atlanmod.neo4emf.INeo4emfObject;

public interface IPersistenceConnection<T extends IPersistedEObject> {
	
	/**
	 * Open the connection with the back-end
	 */
	public void open();	
	/**
	 * Close the connection with the back-end
	 */
	public void close();
	
	public void startSession();
	
	public void endSession();
	/**
	 * Set the connection in regular or dirty mode
	 * @param isDirty true if the persistence have to be dirty, false otherwise
	 */
	public void setDirtyState(boolean isDirty);
	/**
	 * Persist regularly dirty objects in the back-end
	 */
	public void commitDirtyEObjects();
	/**
	 * Remove dirty objects from the back-end
	 */
	public void rollbackDirtyEObjects();
	/**
	 * Add the given {@link INeo4emfObject} in the back-end
	 * @param eObject the object to save
	 * @param dirty the nature of the object addition (dirty-saving or regular one)
	 * @return the created {@link IPersistedEObject}
	 */
	public IPersistedEObject createPersistedEObject(INeo4emfObject eObject);
	
	public void deletePersistedEObject(T persistedEObject);

	public void persistAttribute(T owner, String attributeName, Object attributeValue);
	
	public void persistReference(T from, T to, String label);
	
	public void removeReference(T from, T to, String label);
	
	public Object getAttributeValue(T owner, String attributeName);
	
	public List<IPersistedEObject> getReferencedObjects(T owner, String label);
}
