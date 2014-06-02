package fr.inria.atlanmod.neo4emf.impl;

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

import java.lang.ref.ReferenceQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.emf.common.util.AbstractTreeIterator;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;
import org.eclipse.emf.ecore.resource.Resource.Internal;
import org.eclipse.emf.ecore.util.EContentsEList;
import org.eclipse.osgi.util.NLS;
import org.neo4j.graphdb.Node;

import fr.inria.atlanmod.neo4emf.INeo4emfObject;
import fr.inria.atlanmod.neo4emf.INeo4emfResource;
import fr.inria.atlanmod.neo4emf.change.IChangeLog;
import fr.inria.atlanmod.neo4emf.change.impl.ChangeLogFactory;
import fr.inria.atlanmod.neo4emf.change.impl.Entry;
import fr.inria.atlanmod.neo4emf.change.impl.SetAttribute;
import fr.inria.atlanmod.neo4emf.persistence.IPersistedEObject;
import fr.inria.atlanmod.neo4emf.persistence.IPersistenceConnection;

public class Neo4emfObject extends MinimalEObjectImpl implements INeo4emfObject {

	protected volatile int loadingOnDemand = 0;

	protected volatile int memoryLock = 0;
	
	private IPersistedEObject persistedEObject = null;

	/**
	 * eObject ID
	 */
	//private long id = -1;
	/**
	 * Partition ID
	 */
	//private int partition = -1;
	/**
	 * eObject temporary attribute node ID
	 */
	//protected long attributeId = -1;
	
	//private int sessionId = -1;
	/**
	 * isProxy flag
	 */
	protected boolean isProxy = false;

	protected ReferenceQueue<Object> garbagedData;

	protected NeoObjectData getObjectData() {
		return null;
	}

	/**
	 * @see INeo4emfObject#getNodeId()
	 */
//	@Override
//	public long getNodeId() {
//		return this.id;
//	}

//	@Override
//	public long getAttributeNodeId() {
//		return this.attributeId;
//	}

//	/**
//	 * @see INeo4emfObject#setNodeId()
//	 */
//	@Override
//	public void setNodeId(final long nodeId) {
//		this.id = nodeId;
//	}

//	@Override
//	public void setAttributeNodeId(final long nodeId) {
//		this.attributeId = nodeId;
//	}
//
//	@Override
//	public int getPartitionId() {
//		return partition;
//	}

//	@Override
//	public void setPartitionId(final int partId) {
//		this.partition = partId;
//	}

	/**
	 * Constructor
	 */
	public Neo4emfObject() {
		super();
		this.garbagedData = new ReferenceQueue<Object>();
	}

	public Neo4emfObject(final EClass eClass) {
		super();
		eSetClass(eClass);
		this.garbagedData = new ReferenceQueue<Object>();
	}

	/**
	 * Clear the field <b>ID</b>
	 */
	public void clearId() {
		id = -1;
	}

	/**
	 * compare two objects of type {@link INeo4emfObject}
	 * 
	 * @param {@link INeo4emfObject}
	 * @return {@link int }
	 */
	@Override
	public int compareTo(final INeo4emfObject o) {
		return this.equals(o) ? 0 : this.eContainer().eClass().getName()
				.compareTo(o.eContainer().eClass().getName());
	}

	@Override
	public boolean eIsProxy() {
		// TODO redefine the eIsproxy method
		return this.isProxy;
	}

	@Override
	public void setProxy(final boolean isProxy) {
		this.isProxy = isProxy;
	}

	@Override
	public void setLoadingOnDemand() {
		this.loadingOnDemand++;
	}

	@Override
	public void unsetLoadingOnDemand() {
		this.loadingOnDemand--;
	}

	@Override
	public void setMemoryLock() {
		this.memoryLock++;
		if (memoryLock == 1) {
			setDataStrongReferences();
		}
	}

	@Override
	public void unsetMemoryLock() {
		this.memoryLock--;
		if (memoryLock == 0) {
			releaseDataStrongReferences();
		}
	}

	@Override
	public void setDataStrongReferences() {
		// Do nothing, needs to be overridden in subclasses
	}

	@Override
	public void releaseDataStrongReferences() {
		// Do nothing, needs to be overridden in subclasses.
	}

	@Override
	public boolean isLoadingOnDemand() {
		return this.loadingOnDemand > 0;
	}

	@Override
	public Object eGet(final EStructuralFeature eFeature) {
		return eGet(eFeature, true);
	}

	@Override
	public Object eGet(final EStructuralFeature str, boolean resolve) {
		return eGet(str, resolve, true);
	}

	@Override
	public Object eGet(final EStructuralFeature eFeature,
			final boolean resolve, final boolean coreType) {
		try {
			setLoadingOnDemand();
			int featureID = eDerivedStructuralFeatureID(eFeature);
			Assert.isTrue(featureID >= 0,
					"Invalid feature : " + eFeature.getName());
			return eGet(featureID, resolve, coreType);
		} finally {
			unsetLoadingOnDemand();
		}
	}

	@Override
	public Object eGet(final int featureID, final boolean resolve,
			final boolean coreType) {

		EStructuralFeature eFeature = eClass().getEStructuralFeature(featureID);
		Assert.isNotNull(eFeature, "Invalid feature : " + eFeature);
		// agomez - 2013-12-06: Disable notification of GET
		// Object result = simpleGet(featureID, resolve, coreType, true);
		Object result = simpleGet(featureID, resolve, coreType, false);
		if (this.id == -1 || eResource() == null) {
			return result;
		}
		if (!eFeature.isMany()) {
			if (result != null) {
				return result;
			}
		} else {
			if (!((List<?>) result).isEmpty()) {
				return result;
			}
		}

		Assert.isTrue(eResource() != null
				&& eResource() instanceof INeo4emfResource,
				"The resource is either null or not of type INeo4emfResource");
		INeo4emfResource resource = (INeo4emfResource) eResource();
		if (eFeature instanceof EAttribute) {
			resource.fetchAttributes(this);
		} else if (resolve) {
			resource.getOnDemand(this, featureID);
		}
		return simpleGet(featureID, resolve, coreType, false);

	}

	protected Object eDynamicGet(final int dynamicFeatureID,
			final EStructuralFeature eFeature, final boolean resolve,
			final boolean coreType) {
		Assert.isTrue(dynamicFeatureID >= 0, "invalid Feature with " + eFeature);
		Object result = eSettingDelegate(eFeature).dynamicGet(this,
				eSettings(), dynamicFeatureID, resolve, coreType);
		if (result == null) {
			INeo4emfResource resource = (INeo4emfResource) eResource();
			if (eFeature instanceof EAttribute) {
				resource.fetchAttributes(this);
			} else {
				resource.getOnDemand(this, dynamicFeatureID
						+ eStaticFeatureCount());
			}
		}
		// agomez - 2013-12-06: Disable notification of GET
		// return simpleGet(dynamicFeatureID + eStaticFeatureCount(), resolve,
		// coreType, true);

		return simpleGet(dynamicFeatureID + eStaticFeatureCount(), resolve,
				coreType, false);

	}

	protected Object simpleGet(final int featureID, final boolean resolve,
			final boolean coreType, final boolean notificationRequired) {
		int dynamicFeatureID = featureID - eStaticFeatureCount();
		EStructuralFeature eFeature = eClass().getEStructuralFeature(featureID);
		Assert.isTrue(eFeature != null, "Invalid featureID: " + featureID);
		Object result = eSettingDelegate(eFeature).dynamicGet(this,
				eSettings(), dynamicFeatureID, resolve, coreType);
		// TODO check if it can be removed
		/*
		 * if (result != null && notificationRequired) { Notification msg = new
		 * ENotificationImpl(this, INeo4emfNotification.GET, eFeature, null,
		 * null); if (getChangeLog() != null) { // TODO Check if it is still
		 * needed getChangeLog().addNewEntry(msg); } }
		 */
		return result;
	}

	private IChangeLog<Entry> getChangeLog() {
		return eResource() != null && eResource() instanceof Neo4emfResource ? ((Neo4emfResource) eResource())
				.getChangeLog() : null;
	}

	@Override
	protected void eDynamicSet(final int dynamicFeatureID,
			final EStructuralFeature eFeature, Object newValue) {
		eSettingDelegate(eFeature).dynamicSet(this, eSettings(),
				dynamicFeatureID, newValue);
	}

	@Override
	public void eSet(EStructuralFeature eFeature, Object newValue) {
		int featureID = eDerivedStructuralFeatureID(eFeature);
		Assert.isTrue(featureID >= 0, "Invalid Feature : " + eFeature.getName());
		eSet(featureID, newValue);
		addChangelogEntry(newValue, eFeature);
	}

	@Override
	public void eSet(int featureID, Object newValue) {
		super.eSet(featureID, newValue);
		EStructuralFeature eFeature = eClass().getEStructuralFeature(featureID);
		int dynamicFeatureID = featureID - eStaticFeatureCount();
		Assert.isTrue(eFeature.isChangeable(),
				" illegal argument feature Cannot be changed : " + eFeature);
		eDynamicSet(dynamicFeatureID, eFeature, newValue);
		addChangelogEntry(newValue, eFeature);

	}

	public void addChangelogEntry(Object newValue, int eStructuralFeatureId) {
		addChangelogEntry(newValue,
				eClass().getEStructuralFeature(eStructuralFeatureId));
	}

	public void addChangelogEntry(Object newValue, EStructuralFeature eFeature) {
		if (!isLoadingOnDemand() && getChangeLog() != null) {
			if (eFeature instanceof EAttribute) {
				getChangeLog().add(
						new SetAttribute(this, (EAttribute) eFeature, eGet(
								eFeature, false), newValue));
			} else if (eFeature instanceof EReference) {
				EReference ref = (EReference) eFeature;
				if(!isLoadingOnDemand() && isLoaded()) { 
	        		if (ref.getEOpposite() == null
	        				|| getSessionId() < ((INeo4emfObject)newValue).getSessionId()
	        				|| (getSessionId() == ((INeo4emfObject)newValue).getSessionId()
	        						&& getNodeId() < ((INeo4emfObject)newValue).getNodeId())) {
						if (ref.isMany()) {
							if (newValue instanceof Collection) {
								@SuppressWarnings("unchecked")
								Collection<EObject> c = (Collection<EObject>) newValue;
								for (EObject elt : c) {
									getChangeLog().add(ChangeLogFactory.eINSTANCE.createAddLink(this, (EReference)eFeature, (INeo4emfObject)elt));
								}
							} else {
								getChangeLog().add(ChangeLogFactory.eINSTANCE.createAddLink(this, (EReference)eFeature, (INeo4emfObject)newValue));
							}
						} else {
							getChangeLog().add(ChangeLogFactory.eINSTANCE.createAddLink(this, (EReference)eFeature,(INeo4emfObject)newValue));
						}
	        		}
				}
			} else {
				throw new WrappedException(new Exception(NLS.bind(
						"Unexpected EStructuralFeature {0}",
						eFeature.toString())));
			}
		}
	}

	public void addChangelogRemoveEntry(EObject obj, int featureId) {
		INeo4emfObject removedObject = (INeo4emfObject)obj;
		if (!isLoadingOnDemand() && getChangeLog() != null) {
			EReference ref = (EReference)eClass().getEStructuralFeature(featureId);
			if(!isLoadingOnDemand() && isLoaded()) { 
        		if (ref.getEOpposite() == null
        				|| getSessionId() < removedObject.getSessionId()
        				|| (getSessionId() == removedObject.getSessionId()
        						&& getNodeId() < removedObject.getNodeId())) {
        			getChangeLog().add(ChangeLogFactory.eINSTANCE.createRemoveLink(this, ref, removedObject));
        		}
			}
		}
	}

	@Override
	protected Object eVirtualValue(int index) {
		// handle virtual delegation in the eResource
		// Object result = eVirtualValues()[index];
		// if (result != null )
		// return result;
		// else {
		//
		// }
		throw new UnsupportedOperationException();
	}

	public boolean isLoaded() {
		return getNodeId() > -1 & eResource() instanceof INeo4emfResource;
	}

	public static class NeoObjectData {

	}

	@Override
	public boolean eIsSet(EStructuralFeature eFeature) {
		// return simpleGet(eFeature.getFeatureID(),true, true, false) != null;
		return eGet(eFeature) != null;
	}

	public EList<EObject> eResolvedContents() {
		return new EContentsEList<EObject>(this) {
			@Override
			protected boolean resolve() {
				return false;
			}
		};
	}

	public TreeIterator<EObject> eAllResolvedContents() {
		return new AbstractTreeIterator<EObject>(this, false) {
			private static final long serialVersionUID = 1L;

			@Override
			public Iterator<EObject> getChildren(Object object) {
				return ((Neo4emfObject) object).eResolvedContents().iterator();
			}
		};
	}

	@Override
	public void eSetDirectResource(Internal resource) {
		super.eSetDirectResource(resource);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (obj.getClass() != this.getClass()) {
			return false;
		} else if (((Neo4emfObject) obj).getNodeId() == -1) {
			return false;
		} else if (this.getNodeId() == -1) {
			return false;
		} else {
			return ((Neo4emfObject) obj).getNodeId() == this.getNodeId();
		}
	}

	@Override
	public void saveAllAttributesTo(IPersistenceConnection connection) {
		throw new UnsupportedOperationException("Unsupported Method.");
	}

	@Override
	public void loadAllAttributesFrom(IPersistenceConnection connection) {
		throw new UnsupportedOperationException("Unsupported Method.");
	}

	@Override
	public void saveAllReferencesTo(IPersistenceConnection connection) {
		throw new UnsupportedOperationException("Unsupported Method.");
	}

	@Override
	public void loadAllReferencesFrom(IPersistenceConnection connection) {
		throw new UnsupportedOperationException("Unsupported Method.");

	}

	/**
	 * TODO is it still needed ?
	 */
	@Override
	public int hashCode() {
		if (id == -1) {
			return super.hashCode();
		}
		int prime = 17;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}
	
	@Override
	public int getSessionId() {
		return sessionId;
	}
	
	@Override
	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}
}
