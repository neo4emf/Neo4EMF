package fr.inria.atlanmod.neo4emf.change.impl;

import org.eclipse.emf.ecore.EReference;

import fr.inria.atlanmod.neo4emf.INeo4emfObject;
import fr.inria.atlanmod.neo4emf.INeo4emfResource;
import fr.inria.atlanmod.neo4emf.change.IChangeLog;
import fr.inria.atlanmod.neo4emf.change.IChangeLogFactory;
import fr.inria.atlanmod.neo4emf.persistence.PersistenceConfiguration;

public class ChangeLogFactory implements IChangeLogFactory {
	
	public static int DEFAULT_CL_SIZE = 100000;
	
	public static IChangeLogFactory init() {
		if (eINSTANCE == null) {
			return new ChangeLogFactory();
		} else {
			return eINSTANCE;
		}
	}

	@Override
	public IChangeLog<Entry> createChangeLog(INeo4emfResource resource, PersistenceConfiguration configuration) {
		if(configuration.options().containsKey("cl_size")) {
			return new ChangeLog(resource,(int)configuration.options().get("cl_size"));
		}
		return new ChangeLog(resource, DEFAULT_CL_SIZE);
	}
	
	@Override
	public Entry createAddLink(INeo4emfObject from, EReference eRef, INeo4emfObject to) {
		if(eRef.getEOpposite() == null) {
			// Unidirectional EReference
			return new UnidirectionalAddLink(from, eRef, to);
		}
		else {
			// Bidirectional EReference
			return new BidirectionalAddLink(from, eRef, to);
		}
	}
	
	@Override
	public Entry createRemoveLink(INeo4emfObject from, EReference eRef, INeo4emfObject to) {
		if(eRef.getEOpposite() == null) {
			// Unidirectional EReference
			return new UnidirectionalRemoveLink(from, eRef, to);
		}
		else {
			// Bidirectional EReference
			return new BidirectionalRemoveLink(from, eRef, to);
		}
	}
}
