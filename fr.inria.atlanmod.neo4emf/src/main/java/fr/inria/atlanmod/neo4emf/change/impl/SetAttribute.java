/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.inria.atlanmod.neo4emf.change.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;

import fr.inria.atlanmod.neo4emf.INeo4emfObject;
import fr.inria.atlanmod.neo4emf.drivers.impl.Serializer;

/**
 * 
 * @author sunye
 */
public class SetAttribute extends Entry {
	private final EAttribute eAttribute;
	private final Object oldValue;
	private final Object newValue;

	public SetAttribute(INeo4emfObject obj, EAttribute attr, Object oldValue,
			Object newValue) {
		super(obj);
		eAttribute = attr;
		this.oldValue = oldValue;
		this.newValue = newValue;
	}

	public SetAttribute(Notification msg) {
		this((INeo4emfObject) msg.getNotifier(), (EAttribute) msg.getFeature(), msg
				.getOldValue(), msg.getNewValue());
	}

	@Override
	public void process(Serializer serializer, boolean isTmp) {
		serializer.setAttributeValue(eObject, eAttribute,newValue, isTmp);

	}
	
	@Deprecated
	public EAttribute geteAttribute() {
		return eAttribute;
	}

}
