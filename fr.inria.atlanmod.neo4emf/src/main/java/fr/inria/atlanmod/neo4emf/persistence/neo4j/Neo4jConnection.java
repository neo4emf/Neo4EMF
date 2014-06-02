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
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EClassifier;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexManager;

import fr.inria.atlanmod.neo4emf.INeo4emfObject;
import fr.inria.atlanmod.neo4emf.PersistentPackage;
import fr.inria.atlanmod.neo4emf.drivers.IPersistenceService;
import fr.inria.atlanmod.neo4emf.drivers.impl.NETransaction;
import fr.inria.atlanmod.neo4emf.drivers.impl.Neo4JTransaction;
import fr.inria.atlanmod.neo4emf.persistence.IPersistenceConnection;
import fr.inria.atlanmod.neo4emf.persistence.IPersistedEObject;
import fr.inria.atlanmod.neo4emf.persistence.PersistenceConfiguration;

/**
 * @author sunye
 */
public class Neo4jConnection implements IPersistenceConnection<Neo4jPersistedEObject> {

	/**
	 * Current state of the session
	 */
	private ConnectionState state = ConnectionState.CLOSED;
	
	/**
	 * Persistent state of the session
	 */
	private boolean isDirty = false;

	/**
	 * The Neo4j database service.
	 */
	private final GraphDatabaseService service;

	/**
	 * Registered PersistentPackage for this session.
	 */
	private final PersistentPackage epackage;

	/**
	 * Index for EClass nodes.
	 */
	private Index<Node> metaIndex;
	
	private Index<Relationship> relationshipIndex;

	/**
	 * Node representing the resource.
	 */
	private Node resourceNode;

	/**
	 * Nodes representing the EClasses for the EPackage.
	 */
	private Node[] nodeTypes;
	
	private int sessionId;
	
	private NETransaction currentTx = null;
	
	private int maxTxOperations = 50000;
	private int currentTxOperationCount = 0;

	public Neo4jConnection(GraphDatabaseService gdb, PersistenceConfiguration configuration) {
		assert gdb != null : "Null Database Service";
		assert configuration != null : "Null Configuration";

		service = gdb;
		epackage = configuration.ePackage();
		sessionId = 0;
		
	}

	/**
	 * @see IPersistenceConnection#open()
	 */
	@Override
	public void open() {
		assert state == ConnectionState.CLOSED : "Connection already open";

		state = ConnectionState.OPEN;
		Transaction tx = service.beginTx();
		try {
			this.initializeIndexes();
			this.initializeResource();
			this.initializePackage();
			tx.success();
		} catch (Exception e) {
			tx.failure();
		} finally {
			tx.finish();
		}

	}

	/**
	 * @see IPersistenceConnection#close()
	 */
	@Override
	public void close() {
		assert state == ConnectionState.OPEN : "Connection already closed";

		resourceNode = null;
		nodeTypes = null;
		metaIndex = null;
		state = ConnectionState.CLOSED;
		service.shutdown();
	}
	
	/**
	 * Create the node representing the resource in the database if it does not exist
	 */
	public void createResourceNodeIfAbsent() {
		if (metaIndex.get(IPersistenceService.ID_META, IPersistenceService.RESOURCE_NODE).getSingle() == null) {
			Node resourceNode = service.createNode();
			metaIndex.putIfAbsent(resourceNode, IPersistenceService.ID_META, IPersistenceService.RESOURCE_NODE);
		}
	}

    /*
     * Creates indexes for Meta-classes (ECLass), if absent.
     */
	private void initializeIndexes() {
		assert state == ConnectionState.OPEN : "Connection closed";

		IndexManager manager = service.index();
		metaIndex = manager.forNodes(IPersistenceService.META_ELEMENTS);
		relationshipIndex = manager.forRelationships(IPersistenceService.META_RELATIONSHIPS);
	}

    /*
     * Creates a node corresponding to a resource and adds it to the
     * meta-classes index, if absent.
     */
	private void initializeResource() {
		assert metaIndex != null : "Null meta index";

		resourceNode = metaIndex.get(IPersistenceService.ID_META,
				IPersistenceService.RESOURCE_NODE).getSingle();

		if (resourceNode == null) {
			resourceNode = service.createNode();
			metaIndex.putIfAbsent(resourceNode, IPersistenceService.ID_META,
					IPersistenceService.RESOURCE_NODE);
		}
	}

    /*
     * Creates nodes corresponding to all meta-classes of the associated package, if needed
     * and adds these nodes to the nodeTypes array.
     */
	private void initializePackage() {
		assert epackage != null;
		assert metaIndex != null;

		nodeTypes = new Node[epackage.getEClassifiers().size()];
		for (EClassifier each : epackage.getEClassifiers()) {
			String id = each.getEPackage().getName() + "_"
					+ each.getClassifierID();
			Node n = metaIndex.get(IPersistenceService.ID_META, id).getSingle();
			if (n == null) {
				n = this.createTypeNode(id, each);
			}
			nodeTypes[each.getClassifierID()] = n;
		}
	}

    /*
     * creates a node corresponding to a meta-class and adds it to the meta-classes index.
     */
	private Node createTypeNode(String id, EClassifier type) {
		Node n = service.createNode();
		n.setProperty(IPersistenceService.ECLASS_NAME, type.getName());
		n.setProperty(IPersistenceService.NS_URI, type.getEPackage().getNsURI());
		n.setProperty(IPersistenceService.ECLASS_ID, type.getClassifierID());
		metaIndex.putIfAbsent(n, IPersistenceService.ID_META, id);
		return n;
	}

	////////
	
	/**
	 * @see IPersistenceConnection#startSession()
	 */
	@Override
	public void startSession() {
		sessionId++;
		currentTx = createTransaction();
	}
	
	/**
	 * @see IPersistenceConnection#endSession()
	 */
	@Override
	public void endSession() {
		if(currentTx != null) {
			currentTx.commit();
		}
	}
	
	/**
	 * @see IPersistenceConnection#setDirtyState(boolean)
	 */
	@Override
	public void setDirtyState(boolean isDirty) {
		this.isDirty = isDirty;
	}
	
	/**
	 * @see IPersistenceConnection#commitDirtyEObjects()
	 */
	@Override
	public void commitDirtyEObjects() {
		Iterator<Relationship> dirtyRelIt = service.index().forRelationships("meta_relationships").get("id_meta", "temporary_relationship");
		while(dirtyRelIt.hasNext()) {
			Relationship rel = dirtyRelIt.next();
			if(rel.getType().equals(IPersistenceService.SET_ATTRIBUTE)) {
				persistSetAttributeDirtyRelationship(rel);
			}
			if(rel.getType().equals(IPersistenceService.ADD_LINK)) {
				persistAddLinkDirtyRelationship(rel);
			}
			if(rel.getType().equals(IPersistenceService.REMOVE_LINK)) {
				persistRemoveLinkDirtyRelationship(rel);
			}
			if(rel.getType().equals(IPersistenceService.DELETE)) {
				persistDeleteDirtyRelationship(rel);
			}
			if(rel.getType().equals(IPersistenceService.IS_ROOT)) {
				relationshipIndex.remove(rel);
			}
		}
	}
	
	/**
	 * @see IPersistenceConnection#rollbackDirtyEObjects()
	 */
	@Override
	public void rollbackDirtyEObjects() {
		Iterator<Relationship> relIt = relationshipIndex.get(IPersistenceService.ID_META, IPersistenceService.TMP_RELATIONSHIP).iterator();
		while(relIt.hasNext()) {
			relIt.next().delete();
		}
		Iterator<Node> nodesIt = metaIndex.get(IPersistenceService.ID_META, IPersistenceService.TMP_NODE).iterator();
		while(nodesIt.hasNext()) {
			nodesIt.next().delete();
		}
	}
	
	/**
	 * @see IPersistenceConnection#createPersistedEObject(INeo4emfObject, boolean)
	 */
	@Override
	public IPersistedEObject createPersistedEObject(INeo4emfObject eObject) {
		Node newNode;
		try {
			if(eObject.eContainer() == null && eObject.eResource() != null) {
				// Root object
				newNode = this.createRootNode(eObject);
			}
			else {
				newNode = this.basicCreateNode(eObject);
			}
		}catch(Exception e) {
			newNode = null;
		}
		IPersistedEObject createdObject = new Neo4jPersistedEObject(newNode, eObject, this, sessionId);
		return createdObject;
	}
	
	/**
	 * @see IPersistenceConnection#deletePeristentEObject(INeo4emfObject)
	 */
	@Override
	public void deletePersistedEObject(Neo4jPersistedEObject persistedEObject) {
		Node persistedNode = persistedEObject.getNode();
		if(isDirty) {
			Relationship deleteRel = persistedNode.createRelationshipTo(persistedNode, IPersistenceService.DELETE);
			currentTxOperationCount++;
			if(currentTxOperationCount == maxTxOperations) {
				currentTx.commit();
				currentTx = createTransaction();
			}
			relationshipIndex.add(deleteRel, IPersistenceService.ID_META, IPersistenceService.TMP_RELATIONSHIP);
		}
		else {
			Iterator<Relationship> it = persistedNode.getRelationships().iterator();
			while(it.hasNext()) {
				it.next().delete();
				currentTxOperationCount++;
				if(currentTxOperationCount == maxTxOperations) {
					currentTx.commit();
					currentTx = createTransaction();
				}
			}
			persistedNode.delete();
			currentTxOperationCount++;
			if(currentTxOperationCount == maxTxOperations) {
				currentTx.commit();
				currentTx = createTransaction();
			}
		}
	}
	
	/**
	 * @see IPersistenceConnection#persistAttribute(IPersistedEObject, String, Object)
	 */
	@Override
	public void persistAttribute(Neo4jPersistedEObject owner, String attributeName,
			Object attributeValue) {
		Node persistedNode = owner.getNode();
		if(isDirty) {
			Node attributeNode = owner.getAttributeNode();
			if(attributeNode == null) {
				// There is no attribute node associated to the given object
				attributeNode = createAttributeNode(persistedNode);
				owner.setAttributeNode(attributeNode);
			}
			attributeNode.setProperty(attributeName, attributeValue);
			currentTxOperationCount++;
			if(currentTxOperationCount == maxTxOperations) {
				currentTx.commit();
				currentTx = createTransaction();
			}
		}
		else {
			persistedNode.setProperty(attributeName, attributeValue);
			currentTxOperationCount++;
			if(currentTxOperationCount == maxTxOperations) {
				currentTx.commit();
				currentTx = createTransaction();
			}
		}
	}
	
	/**
	 * @see IPersistenceConnection#persistReference(INeo4emfObject, INeo4emfObject, String)
	 */
	@Override
	public void persistReference(Neo4jPersistedEObject from, Neo4jPersistedEObject to,
			String label) {
		if(isDirty) {
		//	NeoPersistedEObject neoFrom = getOrCreateNeoPersistedEObject(from);
		//	NeoPersistedEObject neoTo = getOrCreateNeoPersistedEObject(to);
		//	if(neoFrom == null || neoTo == null) {
		//		// At least one of the given object doesn't have a resource, there is
		//		// no reason to persist the reference
		//		return;
		//	}
			Node fromNode = from.getNode();
			Node toNode = to.getNode();
			/*
			 * Remove the DELETE relationship that may have been generated. (This
			 * happens when the EObject has been removed from its container).
			 */
			Iterator<Relationship> it = fromNode.getRelationships(
					IPersistenceService.DELETE).iterator();
			while (it.hasNext()) {
				it.next().delete();
				currentTxOperationCount++;
				if(currentTxOperationCount == maxTxOperations) {
					currentTx.commit();
					currentTx = createTransaction();
				}
			}
			/*
			 * If there is a REMOVE_LINK relationship with the same base
			 * label removes it. In that case it is not necessary to
			 * create a ADD_LINK relationship because the base graph contains the
			 * base RelationshipType.
			 */
			it = fromNode.getRelationships(IPersistenceService.REMOVE_LINK)
					.iterator();
			while (it.hasNext()) {
				Relationship rel = it.next();
				if (rel.getProperty("gen_rel").equals(label)
						&& rel.getOtherNode(fromNode).getId() == toNode.getId()) {
					rel.delete();
					currentTxOperationCount++;
					if(currentTxOperationCount == maxTxOperations) {
						currentTx.commit();
						currentTx = createTransaction();
					}
					return;
				}
			}
			Relationship addLinkRel = fromNode.createRelationshipTo(toNode,
					IPersistenceService.ADD_LINK);
			currentTxOperationCount++;
			if(currentTxOperationCount == maxTxOperations) {
				currentTx.commit();
				currentTx = createTransaction();
			}
			addLinkRel.setProperty("gen_rel", label);
			currentTxOperationCount++;
			if(currentTxOperationCount == maxTxOperations) {
				currentTx.commit();
				currentTx = createTransaction();
			}
			relationshipIndex.add(addLinkRel, IPersistenceService.ID_META,
					IPersistenceService.TMP_RELATIONSHIP);
			return;
		}
		else {
			//NeoPersistedEObject neoFrom = (NeoPersistedEObject)getPersistedEObject(from);
			//NeoPersistedEObject neoTo = (NeoPersistedEObject)getPersistedEObject(to);
			Node fromNode = from.getNode();
			Node toNode = to.getNode();
			fromNode.createRelationshipTo(toNode, DynamicRelationshipType.withName(label));
			currentTxOperationCount++;
			if(currentTxOperationCount == maxTxOperations) {
				currentTx.commit();
				currentTx = createTransaction();
			}
		}
	}
	
	/**
	 * @see IPersistenceConnection#removeReference(INeo4emfObject, INeo4emfObject, String)
	 */
	@Override
	public void removeReference(Neo4jPersistedEObject from, Neo4jPersistedEObject to,
			String label) {
		if(isDirty) {
			//NeoPersistedEObject neoFrom = (NeoPersistedEObject)getPersistedEObject(from);
			//NeoPersistedEObject neoTo = (NeoPersistedEObject)getPersistedEObject(to);
			Node fromNode = from.getNode();
			Node toNode = to.getNode();
			/*
			 * If there is a ADD_LINK relationship with the same base
			 * RelationshipType removes it. In that case it is not necessary to
			 * create a REMOVE_LINK relationship because the base graph doesn't have
			 * a RelationshipType relationship between from and to.
			 */
			Iterator<Relationship> it = fromNode.getRelationships(
					IPersistenceService.ADD_LINK).iterator();
			while (it.hasNext()) {
				Relationship rel = it.next();
				if(rel.getProperty("gen_rel").equals(label)
						&& rel.getOtherNode(fromNode).getId() == toNode.getId()) {
					rel.delete();
					currentTxOperationCount++;
					if(currentTxOperationCount == maxTxOperations) {
						currentTx.commit();
						currentTx = createTransaction();
					}
					return;
				}
			}
			Relationship removeLinkRel = fromNode.createRelationshipTo(toNode,
					IPersistenceService.REMOVE_LINK);
			removeLinkRel.setProperty("gen_rel", label);
			currentTxOperationCount++;
			if(currentTxOperationCount == maxTxOperations) {
				currentTx.commit();
				currentTx = createTransaction();
			}
			relationshipIndex.add(removeLinkRel, IPersistenceService.ID_META,
					IPersistenceService.TMP_RELATIONSHIP);
			return;
		}
		else {
			//NeoPersistedEObject neoFrom = (NeoPersistedEObject)getPersistedEObject(from);
			//NeoPersistedEObject neoTo = (NeoPersistedEObject)getPersistedEObject(to);
			Node fromNode = from.getNode();
			Node toNode = to.getNode();
			Iterator<Relationship> it = fromNode.getRelationships(DynamicRelationshipType.withName(label)).iterator();
			while(it.hasNext()) {
				Relationship rel = it.next();
				// TODO check if the toNode is needed ?
				if(rel.getEndNode().getId() == toNode.getId()) {
					rel.delete();
					currentTxOperationCount++;
					if(currentTxOperationCount == maxTxOperations) {
						currentTx.commit();
						currentTx = createTransaction();
					}
					return;
				}
			}
		}
	}
	
	/**
	 * @see IPersistenceConnection#getAttributeValue(INeo4emfObject, String)
	 */
	@Override
	public Object getAttributeValue(Neo4jPersistedEObject owner, String attributeName) {
//		NeoPersistedEObject neoObject = (NeoPersistedEObject)getPersistedEObject(owner);
		if(owner.getAttributeNode() != null) {
			return owner.getAttributeNode().getProperty(attributeName);
		}
		return owner.getNode().getProperty(attributeName);
	}
	
	@Override
	public List<IPersistedEObject> getReferencedObjects(Neo4jPersistedEObject owner,
			String label) {
//		NeoPersistedEObject neoObject = (NeoPersistedEObject)getPersistedEObject(owner);
		List<IPersistedEObject> referencedObjects = new ArrayList<IPersistedEObject>();
		Node neoNode = owner.getNode();
		
		Iterator<Relationship> baseRelIt = neoNode.getRelationships(Direction.OUTGOING,DynamicRelationshipType.withName(label)).iterator();
		while(baseRelIt.hasNext()) {
			Relationship rel = baseRelIt.next();
			// FIXME check that (null EObject and sessionId)
			referencedObjects.add(new Neo4jPersistedEObject(rel.getEndNode(),null,this,sessionId));
		}
		
		Iterator<Relationship> addLinkRelIt = neoNode.getRelationships(Direction.OUTGOING, IPersistenceService.ADD_LINK).iterator();
		while(addLinkRelIt.hasNext()) {
			Relationship rel = addLinkRelIt.next();
			if(rel.getProperty("gen_rel").equals(label)) {
				// FIXME check that (null EObject and sessionId)
				referencedObjects.add(new Neo4jPersistedEObject(rel.getEndNode(),null,this,sessionId));
			}
		}
		
		Iterator<Relationship> removeLinkIt = neoNode.getRelationships(Direction.OUTGOING, IPersistenceService.REMOVE_LINK).iterator();
		while(removeLinkIt.hasNext()) {
			Relationship rel = removeLinkIt.next();
			if(rel.getProperty("gen_rel").equals(label)) {
				// FIXME no equal method (maybe a list of ids ? and delete PersistedEObject)
				// FIXME check that (null EObject and sessionId)
				referencedObjects.remove(new Neo4jPersistedEObject(rel.getEndNode(),null,this,sessionId));
			}
		}
		return referencedObjects;
	}

	//////////
	// PRIVATE THINGS
	/////////
	
	/**
	 * Adds a {@link Node} representing a persistent object to the database, a 'IS_ROOT'
	 * link between the new {@link Node} and the resource and a 'INSTANCE-OF' link
	 * between the new {@link Node} and the {@link Node} that represents the object's type
	 * (EClass).
	 * 
	 * @param obj
	 * @return
	 */
	private Node createRootNode(INeo4emfObject eObject) {
		assert eObject.eContainer() == null : "Container not null (not a root node)";

		try {
			Node newNode = this.basicCreateNode(eObject);
			Relationship isRootRel = resourceNode.createRelationshipTo(newNode,
					IPersistenceService.IS_ROOT);
			currentTxOperationCount++;
			if(currentTxOperationCount == maxTxOperations) {
				currentTx.commit();
				currentTx = createTransaction();
			}
			if(isDirty) {
				relationshipIndex.add(isRootRel, IPersistenceService.ID_META, IPersistenceService.TMP_RELATIONSHIP);
			}
			return newNode;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Adds a node representing a persistent object to the database and an
	 * 'INSTANCE-OF' link between the new node and the node that represents the
	 * object's type (EClass).
	 * 
	 * @param obj
	 * @return
	 * @throws Exception
	 */
	private Node basicCreateNode(INeo4emfObject eObject) throws Exception {
		assert eObject.eClass().getClassifierID() < nodeTypes.length : "Invalid type ID";

		int typeID = eObject.eClass().getClassifierID();
		Node newNode = service.createNode();
		currentTxOperationCount++;
		if(currentTxOperationCount == maxTxOperations) {
			currentTx.commit();
			currentTx = createTransaction();
		}
		Relationship instanceOfRel = newNode.createRelationshipTo(nodeTypes[typeID],
				IPersistenceService.INSTANCE_OF);
		currentTxOperationCount++;
		if(currentTxOperationCount == maxTxOperations) {
			currentTx.commit();
			currentTx = createTransaction();
		}
		if(isDirty) {
			metaIndex.add(newNode,IPersistenceService.ID_META,IPersistenceService.TMP_NODE);
			relationshipIndex.add(instanceOfRel, IPersistenceService.ID_META, IPersistenceService.TMP_RELATIONSHIP);
		}
		return newNode;
	}
	
	/**
	 * Create a dedicated attribute {@link Node} to store the dirty property modifications
	 * on the base {@link Node}
	 * @param base the {@link Node}
	 * @return the created attribute {@link Node}
	 */
	private Node createAttributeNode(Node base) {
		Node attributeNode = service.createNode();
		currentTxOperationCount++;
		if(currentTxOperationCount == maxTxOperations) {
			currentTx.commit();
			currentTx = createTransaction();
		}
		Relationship setAttributeRel = base.createRelationshipTo(attributeNode,
				IPersistenceService.SET_ATTRIBUTE);
		currentTxOperationCount++;
		if(currentTxOperationCount == maxTxOperations) {
			currentTx.commit();
			currentTx = createTransaction();
		}
		metaIndex.add(attributeNode, IPersistenceService.ID_META,
				IPersistenceService.TMP_NODE);
		relationshipIndex.add(setAttributeRel, IPersistenceService.ID_META,
				IPersistenceService.TMP_RELATIONSHIP);
		return attributeNode;
	}
	
	// FIXME, what should we do with that ?
//	private Neo4jPersistedEObject getOrCreateNeoPersistedEObject(INeo4emfObject eObject) {
//		if(eObject.getNodeId() == -1) {
//			if(eObject.eResource() != null) {
//				/*
//				 * Happen when a user first add a non containment link then
//				 * add the referenced object to its container.
//				 */
//				return (Neo4jPersistedEObject)this.createPersistedEObject(eObject);
//			}
//			// The eObject is not contained in a resource, there is no reason to
//			// create a node for it
//			return null;
//		}
//		else {
//			return (Neo4jPersistedEObject)getPersistedEObject(eObject);
//		}
//	}
	
	/**
	 * Persist permanently a dirty SET_ATTRIBUTE relationship  
	 * {@see IPersistenceService.SET_ATTRIBUTE}
	 * @param rel the Relationship to persist
	 */
	private void persistSetAttributeDirtyRelationship(Relationship rel) {
		Node baseNode = rel.getStartNode();
		Node attributeNode = rel.getEndNode();
		Iterator<String> attributePropertyKeys = attributeNode.getPropertyKeys().iterator();
		while(attributePropertyKeys.hasNext()) {
			String propertyKey = attributePropertyKeys.next();
			baseNode.setProperty(propertyKey, attributeNode.getProperty(propertyKey));
			currentTxOperationCount++;
			if(currentTxOperationCount == maxTxOperations) {
				currentTx.commit();
				currentTx = createTransaction();
			}
		}
		relationshipIndex.remove(rel);
		rel.delete();
		currentTxOperationCount++;
		if(currentTxOperationCount == maxTxOperations) {
			currentTx.commit();
			currentTx = createTransaction();
		}
		attributeNode.delete();
		currentTxOperationCount++;
		if(currentTxOperationCount == maxTxOperations) {
			currentTx.commit();
			currentTx = createTransaction();
		}
	}
	
	/**
	 * Persist permanently a dirty ADD_LINK relationship
	 * {@see IPersistenceService.ADD_LINK}
	 * @param rel the Relationship to persist
	 */
	private void persistAddLinkDirtyRelationship(Relationship rel) {
		String associatedRelName = (String) rel.getProperty("gen_rel");
		Node from = rel.getStartNode();
		Node to = rel.getEndNode();
		from.createRelationshipTo(to, DynamicRelationshipType.withName(associatedRelName));
		currentTxOperationCount++;
		if(currentTxOperationCount == maxTxOperations) {
			currentTx.commit();
			currentTx = createTransaction();
		}
		relationshipIndex.remove(rel);
		rel.delete();
		currentTxOperationCount++;
		if(currentTxOperationCount == maxTxOperations) {
			currentTx.commit();
			currentTx = createTransaction();
		}
		metaIndex.remove(from, IPersistenceService.ID_META);
		metaIndex.remove(to, IPersistenceService.ID_META);
		
		/*
		 * Remove INSTANCE_OF relationships from the dirty index
		 * TODO change the way INSTANCE_OF relationships are handled to avoid this
		 * iterator
		 */
		Iterator<Relationship> fromIO = from.getRelationships(IPersistenceService.INSTANCE_OF).iterator();
		Iterator<Relationship> toIO = to.getRelationships(IPersistenceService.INSTANCE_OF).iterator();
		while(fromIO.hasNext()) {
			relationshipIndex.remove(fromIO.next(),IPersistenceService.ID_META);
		}
		while(toIO.hasNext()) {
			relationshipIndex.remove(toIO.next(),IPersistenceService.ID_META);
		}
	}
	
	/**
	 * Persist permanently a dirty REMOVE_LINK relationship
	 * {@see IPersistenceService.REMOVE_LINK}
	 * @param rel the Relationship to persist
	 */
	private void persistRemoveLinkDirtyRelationship(Relationship rel) {
		/*
		 * TODO Find a more efficient implementation (maybe with RelationShipType directly
		 * as a relationship property
		 */
		String associatedRelName = (String) rel.getProperty("gen_rel");
		Node from = rel.getStartNode();
		Node to = rel.getEndNode();
		Iterator<Relationship> relIt = from.getRelationships().iterator();
		while(relIt.hasNext()) {
			Relationship r = relIt.next();
			if(r.getType().toString().equals(associatedRelName) &&
					r.getEndNode().equals(to)) {
				r.delete();
				currentTxOperationCount++;
				if(currentTxOperationCount == maxTxOperations) {
					currentTx.commit();
					currentTx = createTransaction();
				}
				break;
			}
		}
		relationshipIndex.remove(rel);
		rel.delete();
		currentTxOperationCount++;
		if(currentTxOperationCount == maxTxOperations) {
			currentTx.commit();
			currentTx = createTransaction();
		}
	}
	
	/**
	 * Persist permanently a dirty DELETE relationship
	 * {@see IPersistenceService.DELETE}
	 * @param rel the Relationship to persist
	 */
	private void persistDeleteDirtyRelationship(Relationship rel) {
		Node n = rel.getStartNode();
		relationshipIndex.remove(rel);
		rel.delete();
		currentTxOperationCount++;
		if(currentTxOperationCount == maxTxOperations) {
			currentTx.commit();
			currentTx = createTransaction();
		}
		// TODO a generic delete method to remove a node and all its associated relationships ?
		Iterator<Relationship> instanceOfRels = n.getRelationships(IPersistenceService.INSTANCE_OF).iterator();
		while(instanceOfRels.hasNext()) {
			instanceOfRels.next().delete();
			currentTxOperationCount++;
			if(currentTxOperationCount == maxTxOperations) {
				currentTx.commit();
				currentTx = createTransaction();
			}
		}
		n.delete();
		currentTxOperationCount++;
		if(currentTxOperationCount == maxTxOperations) {
			currentTx.commit();
			currentTx = createTransaction();
		}
	}
	
	/**
	 * Creates and starts a transaction. All database operations must be
	 * performed within a transaction.
	 * 
	 * @return
	 */
	private NETransaction createTransaction() {
		assert state == ConnectionState.OPEN : "Connection closed";
		currentTxOperationCount = 0;
		return new Neo4JTransaction(service.beginTx());
	}	
	
	/////////
	
	enum ConnectionState {
		OPEN, CLOSED
	}

}
