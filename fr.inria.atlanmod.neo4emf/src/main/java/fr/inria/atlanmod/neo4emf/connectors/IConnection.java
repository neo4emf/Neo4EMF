package fr.inria.atlanmod.neo4emf.connectors;

import java.util.List;

import fr.inria.atlanmod.neo4emf.INeo4emfObject;

public interface IConnection {
	
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
	/**
	 * @param eObject the {@link INeo4emfObject} to retrieve the persisted object from
	 * @return the {@link IPersistedEObject} associated to the given {@link INeo4emfObject}
	 */
	public IPersistedEObject getPersistedEObject(INeo4emfObject eObject);
	/**
	 * Delete the given {@link INeo4emfObject} from the back-end
	 * @param eObject the {@link INeo4emfObject} to delete
	 */
	public void deletePersistedEObject(INeo4emfObject eObject);
	
	public void persistAttribute(INeo4emfObject owner, String attributeName, Object attributeValue);
	
	public void persistReference(INeo4emfObject from, INeo4emfObject to, String label);
	
	public void removeReference(INeo4emfObject from, INeo4emfObject to, String label);
	
	public Object getAttributeValue(INeo4emfObject owner, String attributeName);
	
	public List<IPersistedEObject> getReferencedObjects(INeo4emfObject owner, String label);
}
