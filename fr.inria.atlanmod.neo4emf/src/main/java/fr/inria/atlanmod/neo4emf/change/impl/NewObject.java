package fr.inria.atlanmod.neo4emf.change.impl;

import fr.inria.atlanmod.neo4emf.INeo4emfObject;
import fr.inria.atlanmod.neo4emf.drivers.impl.Serializer;

/**
 *
 * @author sunye
 */
public class NewObject extends Entry {
	
    public NewObject(INeo4emfObject value) {
        super(value);
    }

	@Override
	public void process(Serializer serializer) {
		serializer.createNewObject(eObject);
		super.release();
	}
    
}
