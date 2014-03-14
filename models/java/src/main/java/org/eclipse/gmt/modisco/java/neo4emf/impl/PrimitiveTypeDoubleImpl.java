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
**/
package org.eclipse.gmt.modisco.java.neo4emf.impl;

import fr.inria.atlanmod.neo4emf.INeo4emfResource;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.gmt.modisco.java.PrimitiveTypeDouble;

import org.eclipse.gmt.modisco.java.neo4emf.meta.JavaPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Primitive Type Double</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * </p>
 *
 * @generated
 */
public class PrimitiveTypeDoubleImpl extends PrimitiveTypeImpl implements PrimitiveTypeDouble {

	 
	
	 
	 
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PrimitiveTypeDoubleImpl() {
		super();
		
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	 @Override
	protected DataPrimitiveTypeDouble getData(){
		if ( data == null || !(data instanceof DataPrimitiveTypeDouble)){
			data = new DataPrimitiveTypeDouble();
			if (isLoaded())
			((INeo4emfResource) this.eResource()).fetchAttributes(this);
			}
		return (DataPrimitiveTypeDouble) data;
	}
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return JavaPackage.eINSTANCE.getPrimitiveTypeDouble();
	}


/*
* Neo4EMF inserted code -- begin
*/

/*
* Neo4EMF inserted code -- end
*/




// data Class generation 
protected static  class DataPrimitiveTypeDouble extends DataPrimitiveType {


	/**
	 *Constructor of DataPrimitiveTypeDouble
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DataPrimitiveTypeDouble() {
		 super(); 
	}
	
		
	/**
	 *Constructor of DataPrimitiveTypeDouble
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param {@link PrimitiveType }
	 * @generated
	 */
	//public DataPrimitiveTypeDouble(DataPrimitiveType data)
	//{
	//	this();		
	//	
	//	}
	
	
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String toString(){	
		StringBuffer result = new StringBuffer(super.toString());		
		result.append(')');
		return result.toString();
	}
		

/*
* Neo4EMF inserted code -- begin
*/

/*
* Neo4EMF inserted code -- end
*/
}//end data class
} //PrimitiveTypeDoubleImpl