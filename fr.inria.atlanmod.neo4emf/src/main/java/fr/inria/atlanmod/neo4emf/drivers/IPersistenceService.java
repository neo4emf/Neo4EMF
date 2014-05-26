package fr.inria.atlanmod.neo4emf.drivers;

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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.neo4j.graphdb.RelationshipType;

import fr.inria.atlanmod.neo4emf.INeo4emfObject;
import fr.inria.atlanmod.neo4emf.connectors.IPersistedEObject;
import fr.inria.atlanmod.neo4emf.drivers.impl.NETransaction;

public interface IPersistenceService {
	/**
	 * NS_URI Constant key to save NS_URI {@link String}
	 */
	public final String NS_URI = "ns_uri";
	/**
	 * META_ELEMENTS Constant key to save add EClass types to the index
	 * {@link String}
	 */
	public final String META_ELEMENTS = "meta_elements";

	public final String META_RELATIONSHIPS = "meta_relationships";
	/**
	 * ROOT_ELEMENTS Constant to meta_elements index name {@link String}
	 */
	public final String ROOT_ELEMENTS = "root_elements";
	/**
	 * ECLASS_NAME Constant to root_elements index name {@link String}
	 */
	public final String ECLASS_NAME = "eClass_name";
	/**
	 * ECLASS_ID Constant to root_elements id {@link String}
	 */
	public final String ECLASS_ID = "eClass_id";
	/**
	 * ID_META Constant {@link String}
	 */
	public final String ID_META = "id_meta";
	/**
	 * the Value of the node representing the resource in the backend
	 */
	public final String RESOURCE_NODE = "resource";
	/**
	 * the Value of the temporary nodes in the backend
	 */
	public final String TMP_NODE = "temporary_node";

	public final String TMP_RELATIONSHIP = "temporary_relationship";
	/**
	 * INSTANCE_OF {@link RelationshipType}
	 */
	public final RelationshipType INSTANCE_OF = MetaRelation.INSTANCE_OF;
	/**
	 * IS_ROOT {@link RelationshipType}
	 */
	public final RelationshipType IS_ROOT = MetaRelation.IS_ROOT;

	/**
	 * ADD_LINK {@link RelationshipType}
	 */
	public final RelationshipType ADD_LINK = MetaRelation.ADD_LINK;
	/**
	 * REMOVE_LINK {@link RelationshipType}
	 */
	public final RelationshipType REMOVE_LINK = MetaRelation.REMOVE_LINK;
	/**
	 * DELETE {@link RelationshipType}
	 */
	public final RelationshipType DELETE = MetaRelation.DELETE;
	/**
	 * SET_ATTRIBUTE {@link RelationshipType}
	 */
	public final RelationshipType SET_ATTRIBUTE = MetaRelation.SET_ATTRIBUTE;

	/**
	 * Create Node from eObject
	 * 
	 * @param eObject
	 *            {@link EObject}
	 */
	IPersistedEObject createNodeFromEObject(INeo4emfObject eObject, boolean isTemporary);

	// TODO check if EReference is appropriate here
	void createLink(INeo4emfObject from, INeo4emfObject to, EReference ref);

	// TODO check if EReference is appropriate here
	void removeLink(INeo4emfObject from, INeo4emfObject to, EReference ref);

	/**
	 * Create an Attribute node for eObject
	 * 
	 * @warning It doesn't check if an Attribute node is already created.
	 * @param eObject
	 *            {@link EObject}
	 */
	void createAttributeNodeForEObject(INeo4emfObject eObject);

	void deleteNodeFromEObject(INeo4emfObject eObject);

	void createAddLinkRelationship(INeo4emfObject from,
			INeo4emfObject to, EReference ref);

	void createRemoveLinkRelationship(INeo4emfObject from,
			INeo4emfObject to, EReference ref);

	void createDeleteRelationship(INeo4emfObject obj);

	void persistDirtyRelationships();
	
	void createResourceNodeIfAbsent();
	
	// TODO find something to do with that
//	List<Node> getNodesOnDemand(long nodeid, RelationshipType relationshipType);

//	List<Node> getAddLinkNodesOnDemand(long nodeid,
//			RelationshipType baseRelationshipType);

//	List<Node> getRemoveLinkNodesOnDemand(long nodeid,
//			RelationshipType baseRelationshipType);


	/**
	 * Stops the persistent service.
	 */
	void shutdown();

	/**
	 * Create and start a transaction
	 * 
	 * @return
	 */
	NETransaction createTransaction();

	void cleanIndexes();

	/**
	 * Enum class for the meta_relations
	 * 
	 * @author Amine BENELALLAM
	 * 
	 */
	public enum MetaRelation implements RelationshipType {
		/**
		 * Instance of relationship
		 */
		INSTANCE_OF,
		/**
		 * IS_ROOT relationship
		 */
		IS_ROOT,
		/**
		 * ADD_LINK relationship
		 */
		ADD_LINK,
		/**
		 * REMOVE_LINK relationship
		 */
		REMOVE_LINK,
		/**
		 * DELETE relationship
		 */
		DELETE,
		/**
		 * SET_ATTRIBUTE relationship
		 */
		SET_ATTRIBUTE
	}
}
