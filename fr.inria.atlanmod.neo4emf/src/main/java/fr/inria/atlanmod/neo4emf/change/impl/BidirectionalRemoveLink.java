package fr.inria.atlanmod.neo4emf.change.impl;

import org.eclipse.emf.ecore.EReference;

import fr.inria.atlanmod.neo4emf.INeo4emfObject;
import fr.inria.atlanmod.neo4emf.drivers.impl.Serializer;

public class BidirectionalRemoveLink extends RemoveLink {

	public BidirectionalRemoveLink(INeo4emfObject from, EReference eRef, INeo4emfObject to) {
		super(from,eRef,to);
		assert eRef.getEOpposite() != null : "Try to create a BidirectionalRemoveLink with a uniderctional EReference";
	}

	@Override
	public void process(Serializer serializer) {
		serializer.removeExistingLink(eObject, eReference, referencedEObject);
		serializer.removeExistingLink(referencedEObject, eReference.getEOpposite(), eObject);
		super.release();
	}
	
}
