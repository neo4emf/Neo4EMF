package fr.inria.atlanmod.neo4emf;

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
 * @author Amine BENELALLAM
 * */
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;

import fr.inria.atlanmod.neo4emf.persistence.IPersistedEObject;
import fr.inria.atlanmod.neo4emf.persistence.IPersistenceConnection;

public interface INeo4emfObject extends EObject, Comparable<INeo4emfObject> {
	
	public void attachPersistedEObject(IPersistedEObject obj);
	
	public IPersistedEObject persistedEObject();
	
	/**
	 * Save all the object's {@link EAttribute} using given {@link IPersistenceConnection}
	 * @param n the back-end {@link IPersistenceConnection}
	 */
	public void saveAllAttributesTo(IPersistenceConnection connection);
	
	/**
	 * Loads attributes from Node.
	 * 
	 * @param n
	 *            the database node
	 */
	public void loadAllAttributesFrom(IPersistenceConnection connection);

	/**
	 * Save all the object's {@link EReference} using given {@link IPersistenceConnection}
	 * @param n the back-end {@link IPersistenceConnection}
	 */
	public void saveAllReferencesTo(IPersistenceConnection connection);
	
	/**
	 * Loads references from Node.
	 * 
	 * @param n
	 *            the database node
	 */
	public void loadAllReferencesFrom(IPersistenceConnection connection);

	/**
	 * returns the <b>ID</b> of the node that represents the element in the
	 * backend and <b>-1</b> if the element have not been persisted yet
	 * 
	 * @return ID {@link long }
	 */
	//public long getNodeId();

	/**
	 * returns the <b>ID</b> of the node that represents the temporary
	 * attributes of the object in the backend and <b>-1</b> if the element is
	 * not in a temporary state.
	 * 
	 * @return AttributeNodeID {@link long}
	 */
	//public long getAttributeNodeId();

	/**
	 * set the <b>ID</b> of the eObject once created from the backend node
	 * 
	 * @param id
	 *            {@link long}
	 */
	//void setNodeId(long id);

	/**
	 * set the <b>attribute ID</b> of the eObject once created from the
	 * temporary backend node.
	 * 
	 * @param id
	 *            {@link long}
	 */
	//void setAttributeNodeId(long id);

	/**
	 * @return ID {@link int}
	 */
	//public int getPartitionId();

	/**
	 * set the partition Id
	 * 
	 * @param id
	 */
	//public void setPartitionId(int id);

	void setProxy(boolean isProxy);

	void setLoadingOnDemand();

	void unsetLoadingOnDemand();

	void setMemoryLock();

	void unsetMemoryLock();

	public void setDataStrongReferences();

	public void releaseDataStrongReferences();

	boolean isLoadingOnDemand();

	public void addChangelogEntry(Object newValue, int eStructuralFeatureId);

	public void addChangelogEntry(Object newValue, EStructuralFeature eFeature);

	public void addChangelogRemoveEntry(EObject removedObject, int featureId);

	boolean isLoaded();
	
	//int getSessionId();
	
	//void setSessionId(int sessionId);
	
}
