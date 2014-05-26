package fr.inria.atlanmod.neo4emf.persistence.neo4j;

import org.eclipse.emf.ecore.EObject;
import org.neo4j.graphdb.Node;

import fr.inria.atlanmod.neo4emf.INeo4emfObject;
import fr.inria.atlanmod.neo4emf.persistence.IPersistedEObject;
import fr.inria.atlanmod.neo4emf.persistence.IPersistenceConnection;

public class Neo4jPersistedEObject implements IPersistedEObject {
	
	/**
	 * Neo4j {@link Node} back-end that handle the {@link EObject}
	 */
	private Node node = null;
	/**
	 * Neo4j {@link Node} back-end that handle the attributes of the {@link EObject}
	 */
	private Node attributeNode = null;
	/**
	 * {@link INeo4emfObject} associated to the Neo4j {@link Node}
	 */
	private INeo4emfObject eObject = null;
	/**
	 * The connection that handle the transactions
	 */
	private IPersistenceConnection connection = null;
	/**
	 * The identifier of the session the {@link IPersistedEObject} has been created in
	 */
	private int sessionId = 0;
		
	/**
	 * Construct a new {@link Neo4jPersistedEObject}
	 * @param node the Neo4j {@link Node} containing the object informations
	 * @param eObject the {@link INeo4emfObject} associated to the {@link Node}
	 * @param connection the {@link IPersistenceConnection} that handle the transactions on the database
	 * @param sessionId the identifier of the session the {@link IPersistedEObject} has been created in
	 */
	Neo4jPersistedEObject(Node node, INeo4emfObject eObject, IPersistenceConnection connection, int sessionId) {
		assert node != null : "Cannot construct a NeoPersistedEObject from a null Node";
		assert eObject != null : "Cannot construct a NeoPersistedEObject from a null EObject";
		assert connection != null : "Cannot construct a NeoPersistedEObject with a null IPersistenceConnection";
		this.node = node;
		this.eObject = eObject;
		this.connection = connection;
		this.sessionId = sessionId;
	}
	
	void setAttributeNode(Node attributeNode) {
		this.attributeNode = attributeNode;
	}
	
	Node getNode() {
		return this.node;
	}
	
	Node getAttributeNode() {
		return this.attributeNode;
	}
	
	int getSessionId() {
		return this.sessionId;
	}
	
	/**
	 * @see IPersistedEObject#addAttribute(String, Object)
	 */
	@Override
	public void addAttribute(String name, Object value) {
		assert name != null && !name.isEmpty(): "Cannot save an attribute with an empty name";
		connection.persistAttribute(this, name, value);
	}

	/**
	 * @see IPersistedEObject#addReference(String, IPersistedEObject)
	 */
	@Override
	public void addReference(String name, IPersistedEObject value) {
		assert name != null && !name.isEmpty() : "Cannot save a reference with an empty label";
		connection.persistReference(this, value, name);
	}
	
	/**
	 * @see IPersistedEObject#removeReference(String, IPersistedEObject)
	 */
	@Override
	public void removeReference(String name, IPersistedEObject value) {
		assert name != null && !name.isEmpty() : "Cannot remove a reference with an empty label";
		connection.removeReference(this, value, name);
	}
}