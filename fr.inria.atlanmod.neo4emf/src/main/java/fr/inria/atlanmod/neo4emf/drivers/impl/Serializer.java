package fr.inria.atlanmod.neo4emf.drivers.impl;

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

import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EReference;

import fr.inria.atlanmod.neo4emf.INeo4emfObject;
import fr.inria.atlanmod.neo4emf.RelationshipMapping;
import fr.inria.atlanmod.neo4emf.change.impl.Entry;
import fr.inria.atlanmod.neo4emf.connectors.IConnection;
import fr.inria.atlanmod.neo4emf.connectors.IPersistedEObject;
import fr.inria.atlanmod.neo4emf.drivers.ISerializer;
import fr.inria.atlanmod.neo4emf.drivers.NEConfiguration;
import fr.inria.atlanmod.neo4emf.drivers.NEConnectionFactory;

public class Serializer implements ISerializer {

	/**
	 * TODO: Comment this
	 */
	PersistenceManager manager;
	
	RelationshipMapping mapping;
	
	IConnection connection;
	
	/**
	 * TODO: Comment this
	 */
//	Map<String, Object> defaultOptions = new HashMap<String, Object>();
	
	public Serializer(PersistenceManager manager, NEConfiguration configuration) {
		this.manager = manager;
		this.mapping = configuration.ePackage().getRelationshipMapping();
		this.connection = NEConnectionFactory.createNEConnection(configuration);
	}

	@Override
	/**
	 *  @see {@link INeo4emfResource#save()}
	 */
	public void save(boolean dirtySave) {
		// FIXME put this 
//		manager.startNewSession();
		/*
		 * The number of database operations within a transaction is bounded,
		 * that's why changes may be processed in several transactions.
		 */
		connection.startSession();
		connection.setDirtyState(dirtySave);
		if(!dirtySave) {
			connection.commitDirtyEObjects();
		}
		List<Entry> changes = manager.getResource().getChangeLog().changes();
		while (!changes.isEmpty()) {
//			currentTx = manager.createTransaction();
			try {
				for(Entry each : changes) {
					each.process(this);
				}
//				currentTx.success();
				changes.clear();
			} catch (Exception e) {
//				currentTx.abort();
				System.err.println("An error occured during serialization");
				e.printStackTrace();
				return;
			} finally {
//				currentTx.commit();
			}
		}
		connection.setDirtyState(false);
		connection.endSession();
	}
	
	public void deleteExistingObject(INeo4emfObject eObject) {
		connection.deletePersistedEObject(eObject);
	}


	public void setAttributeValue(INeo4emfObject eObject,
			EAttribute at, Object newValue) {
		connection.persistAttribute(eObject, at.getName(), newValue);
	}
	
	public void addNewLink(INeo4emfObject from, EReference eRef, INeo4emfObject to) {
		connection.persistReference(from, to, getSerializedReferenceName(eRef));
	}

	public void removeExistingLink(INeo4emfObject from, EReference eRef, INeo4emfObject to) {
		connection.removeReference(from,to,getSerializedReferenceName(eRef));
	}

	public void createNewObject(INeo4emfObject eObject) {
		IPersistedEObject persistedEObject = null;
		if (eObject.getNodeId() == -1) {
			persistedEObject = this.connection.createPersistedEObject(eObject);
			manager.registerEObject(eObject,persistedEObject);
		} else {
			persistedEObject = this.connection.getPersistedEObject(eObject);
		}
		eObject.saveAllAttributesTo(connection);
		eObject.saveAllReferencesTo(connection);
	}
	
	private String getSerializedReferenceName(EReference eRef) {
		return mapping.relationshipAt(eRef.getEContainingClass().getClassifierID(), eRef.getFeatureID()).name();
	}
}
