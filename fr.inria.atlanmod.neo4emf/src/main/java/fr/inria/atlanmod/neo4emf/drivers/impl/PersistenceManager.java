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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

import fr.inria.atlanmod.neo4emf.INeo4emfObject;
import fr.inria.atlanmod.neo4emf.INeo4emfResource;
import fr.inria.atlanmod.neo4emf.drivers.ILoader;
import fr.inria.atlanmod.neo4emf.drivers.IPersistenceManager;
import fr.inria.atlanmod.neo4emf.drivers.IPersistenceServiceFactory;
import fr.inria.atlanmod.neo4emf.drivers.IProxyManager;
import fr.inria.atlanmod.neo4emf.drivers.ISerializer;
import fr.inria.atlanmod.neo4emf.impl.Neo4emfObject;
import fr.inria.atlanmod.neo4emf.impl.Neo4emfResource;
import fr.inria.atlanmod.neo4emf.persistence.IPersistenceConnection;
import fr.inria.atlanmod.neo4emf.persistence.IPersistedEObject;
import fr.inria.atlanmod.neo4emf.persistence.PersistenceConfiguration;
import fr.inria.atlanmod.neo4emf.persistence.neo4j.Neo4jConnectionFactory;
import fr.inria.atlanmod.neo4emf.persistence.neo4j.Neo4jConnection;
import fr.inria.atlanmod.neo4emf.resourceUtil.Neo4emfResourceUtil;

public class PersistenceManager implements IPersistenceManager {

	protected IPersistenceConnection connection;
	/**
	 * The loader and eObjects builder {@link Loader}
	 */
	protected ILoader loader;
	/**
	 * Serializer and nodes converter {@link Serializer}
	 */
	protected ISerializer serializer;
	/**
	 * The resource {@link Neo4emfResource}
	 */
	protected INeo4emfResource resource;
	/**
	 * The loaded elements manager {@link ProxyManager}
	 */
	protected IProxyManager proxyManager;
	
	/**
	 * Global constructor
	 * 
	 * @param neo4emfResource
	 *            {@link Neo4emfResource}
	 * @param storeDirectory
	 *            {@link String}
	 * @param eRef2relType
	 *            {@link Map}
	 */

	
	public PersistenceManager(INeo4emfResource neo4emfResource,
			PersistenceConfiguration configuration) {
		assert configuration != null : "Null configuration";
		if(configuration.options().containsKey("max_operation_per_transaction")) {
			maxOperationPerTransaction = (int)configuration.options().get("max_operation_per_transaction");
		}
		this.resource = neo4emfResource;
		// TODO share the connection between seria and loader
		this.serializer = new Serializer(this, configuration);
		this.proxyManager = new ProxyManager();
		this.loader = new Loader(this);
	}

	@Override
	public void load() {
		try {
			loader.load();
		} catch (Exception e) {
			shutdown();
			e.printStackTrace();
		}
	}

	@Override
	public void save() {
		serializer.save(false);
	}
	
	@Override
	public void dirtySave() {
		serializer.save(true);
	}

	@Override
	public void shutdown() {
		connection.close();
	}

	@Override
	public void fetchAttributes(EObject obj) {
		Node node = getNodeById(obj);
		// TODO find a way to avoid that call when appropriate (maybe
		// if there is a non changed changelog)
		Node attributeNode = null;
		if(((Neo4emfObject)obj).getAttributeNodeId() > -1) {
			attributeNode = getAttributeNodeById(obj);
//			((INeo4emfObject)obj).loadAllAttributesFrom(attributeNode);
//			return;
		}
//		((INeo4emfObject)obj).loadAllAttributesFrom(node);
		loader.fetchAttributes(obj,node,attributeNode);	
	}

	@Override
	public void getOnDemand(EObject obj, int featureId) {
		try {
			EClass eClass = obj.eClass();
			EStructuralFeature feature = eClass.getEStructuralFeature(featureId);
			EClass parent = feature.getEContainingClass();
			
//			EPackage ePackage = eClass.getEPackage();
		//	RelationshipType relationship =  getRelTypefromERef(ePackage.getNsURI(),eClass.getClassifierID(), featureId);
			RelationshipType relationship =  null;
			if ( relationship == null ) {
				  //throw new NullPointerException(" Cannot find the relationship ");
				String stri = Neo4emfResourceUtil.formatRelationshipName(parent,feature);
				relationship = DynamicRelationshipType.withName(stri);
			}
			// TODO All the node processing should be done in the connexion, and return only the EObjects associated
			List <Node> nodes= persistenceService.getNodesOnDemand(((INeo4emfObject)obj).getNodeId(),
						relationship);
			List<Node> addLinkNodes = persistenceService.getAddLinkNodesOnDemand(((INeo4emfObject)obj).getNodeId(), relationship);
			List<Node> removeLinkNodes = persistenceService.getRemoveLinkNodesOnDemand(((INeo4emfObject)obj).getNodeId(), relationship);
			nodes.addAll(addLinkNodes);
			nodes.removeAll(removeLinkNodes);
			loader.getObjectsOnDemand(obj, featureId,getNodeById(obj), nodes);
		}catch(Exception e){ 
			shutdown();
			e.printStackTrace();
		}
	}

	@Override
	public EObject getContainerOnDemand(EObject eObject, int featureId) {
		EObject eResult = null;
		try {
			List<Node> singleNode = persistenceService.getNodesOnDemand(
					((INeo4emfObject) eObject).getNodeId(),
					getRelTypefromERef(eObject.eClass().getClassifierID(),
							featureId));
			eResult = loader.getContainerOnDemand(eObject, featureId,
					getNodeById(eObject), singleNode.get(0));
		} catch (Exception e) {
			shutdown();
			e.printStackTrace();
		}
		return eResult;
	}

	@Override
	public EList<INeo4emfObject> getAllInstancesOfType(EClass eClass) {
		EList<EClass> classesList = ((Loader) loader).subClassesOf(eClass);
		EList<INeo4emfObject> allInstances = new BasicEList<INeo4emfObject>();
		List<Node> nodeList = new ArrayList<Node>();
		try {
			for (EClass eCls : classesList) {
				nodeList = this.persistenceService.getAllNodesOfType(eCls);
				if (nodeList.isEmpty()) {
					continue;
				}
				allInstances
						.addAll(this.loader.getAllInstances(eCls, nodeList));
			}
		} catch (Exception e) {
			shutdown();
			e.printStackTrace();
		}
		return allInstances;
	}

	public INeo4emfResource getResource() {
		return resource;
	}
	
	/**
	 * {@link IPersistenceManager#getProxyManager()}
	 */
	@Override
	public IProxyManager getProxyManager() {
		return proxyManager;
	}
	
	public void registerEObject(INeo4emfObject eObject, IPersistedEObject persistedObject) {
		eObject.attachPersistedEObject(persistedObject);
		proxyManager.putToProxy(eObject);
	}
	
	public INeo4emfObject getObjectFromProxy(EClass eClassifier, Node n) {
		return proxyManager.getObjectFromProxy(eClassifier, n.getId());
	}
}
