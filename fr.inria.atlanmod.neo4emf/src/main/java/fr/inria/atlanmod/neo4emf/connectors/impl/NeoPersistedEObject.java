package fr.inria.atlanmod.neo4emf.connectors.impl;

import org.neo4j.graphdb.Node;

import fr.inria.atlanmod.neo4emf.connectors.IPersistedEObject;

public class NeoPersistedEObject implements IPersistedEObject {
	
	/**
	 * Neo4j {@link Node} back-end that handle the EObject
	 */
	private Node graphNode = null;
	private Node attributeNode = null;
		
	/**
	 * Construct a new NeoPersistedEObject from the given {@link Node}
	 * @param node {@link Node}
	 */
	NeoPersistedEObject(Node node) {
		assert node != null : "Cannot construct a NeoPersistedEObject from a null Node";
		this.graphNode = node;
	}
	
	void setAttributeNode(Node attributeNode) {
		this.attributeNode = attributeNode;
	}
	
	Node getNode() {
		return this.graphNode;
	}
	
	Node getAttributeNode() {
		return this.attributeNode;
	}
	
	/**
	 * @see IPersistedEObject#getId()
	 */
	@Override
	public long getId() {
		return graphNode.getId();
	}
}