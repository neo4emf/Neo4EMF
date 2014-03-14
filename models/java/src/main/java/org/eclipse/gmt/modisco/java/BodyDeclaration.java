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
package org.eclipse.gmt.modisco.java;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Body Declaration</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.gmt.modisco.java.BodyDeclaration#getAbstractTypeDeclaration <em>Abstract Type Declaration</em>}</li>
 *   <li>{@link org.eclipse.gmt.modisco.java.BodyDeclaration#getAnnotations <em>Annotations</em>}</li>
 *   <li>{@link org.eclipse.gmt.modisco.java.BodyDeclaration#getAnonymousClassDeclarationOwner <em>Anonymous Class Declaration Owner</em>}</li>
 *   <li>{@link org.eclipse.gmt.modisco.java.BodyDeclaration#getModifier <em>Modifier</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.gmt.modisco.java.neo4emf.meta.JavaPackage#getBodyDeclaration()
 * @model abstract="true"
 * @generated
 */
public interface BodyDeclaration extends NamedElement {

/** genFeaure.override.javajetinc **/
	/**
	 * Returns the value of the '<em><b>Abstract Type Declaration</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.eclipse.gmt.modisco.java.AbstractTypeDeclaration#getBodyDeclarations <em>Body Declarations</em>}'.
	 * <!-- begin-user-doc -->
	 *XX6a
	 * <p>
	 * If the meaning of the '<em>Abstract Type Declaration</em>' container reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Abstract Type Declaration</em>' container reference.
	 * @see #setAbstractTypeDeclaration(AbstractTypeDeclaration)
	 * @see org.eclipse.gmt.modisco.java.neo4emf.meta.JavaPackage#getBodyDeclaration_AbstractTypeDeclaration()
	 * @see org.eclipse.gmt.modisco.java.AbstractTypeDeclaration#getBodyDeclarations
	 * @model opposite="bodyDeclarations" transient="false" ordered="false"
	 * @generated
	 */
	AbstractTypeDeclaration getAbstractTypeDeclaration();
	/**
	 * Sets the value of the '{@link org.eclipse.gmt.modisco.java.BodyDeclaration#getAbstractTypeDeclaration <em>Abstract Type Declaration</em>}' container reference.
	 * <!-- begin-user-doc -->
	 *YY1-BIS
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Abstract Type Declaration</em>' container reference.
	 * @see #getAbstractTypeDeclaration()
	 * @generated
	 */
	void setAbstractTypeDeclaration(AbstractTypeDeclaration value);
 

/** genFeaure.override.javajetinc **/
	/**
	 * Returns the value of the '<em><b>Annotations</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.gmt.modisco.java.Annotation}.
	 * <!-- begin-user-doc -->
	 *XX6a
	 * <p>
	 * If the meaning of the '<em>Annotations</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Annotations</em>' containment reference list.
	 * @see org.eclipse.gmt.modisco.java.neo4emf.meta.JavaPackage#getBodyDeclaration_Annotations()
	 * @model containment="true"
	 * @generated
	 */
	EList<Annotation> getAnnotations(); 

/** genFeaure.override.javajetinc **/
	/**
	 * Returns the value of the '<em><b>Anonymous Class Declaration Owner</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.eclipse.gmt.modisco.java.AnonymousClassDeclaration#getBodyDeclarations <em>Body Declarations</em>}'.
	 * <!-- begin-user-doc -->
	 *XX6a
	 * <p>
	 * If the meaning of the '<em>Anonymous Class Declaration Owner</em>' container reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Anonymous Class Declaration Owner</em>' container reference.
	 * @see #setAnonymousClassDeclarationOwner(AnonymousClassDeclaration)
	 * @see org.eclipse.gmt.modisco.java.neo4emf.meta.JavaPackage#getBodyDeclaration_AnonymousClassDeclarationOwner()
	 * @see org.eclipse.gmt.modisco.java.AnonymousClassDeclaration#getBodyDeclarations
	 * @model opposite="bodyDeclarations" transient="false" ordered="false"
	 * @generated
	 */
	AnonymousClassDeclaration getAnonymousClassDeclarationOwner();
	/**
	 * Sets the value of the '{@link org.eclipse.gmt.modisco.java.BodyDeclaration#getAnonymousClassDeclarationOwner <em>Anonymous Class Declaration Owner</em>}' container reference.
	 * <!-- begin-user-doc -->
	 *YY1-BIS
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Anonymous Class Declaration Owner</em>' container reference.
	 * @see #getAnonymousClassDeclarationOwner()
	 * @generated
	 */
	void setAnonymousClassDeclarationOwner(AnonymousClassDeclaration value);
 

/** genFeaure.override.javajetinc **/
	/**
	 * Returns the value of the '<em><b>Modifier</b></em>' containment reference.
	 * It is bidirectional and its opposite is '{@link org.eclipse.gmt.modisco.java.Modifier#getBodyDeclaration <em>Body Declaration</em>}'.
	 * <!-- begin-user-doc -->
	 *XX6a
	 * <p>
	 * If the meaning of the '<em>Modifier</em>' containment reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Modifier</em>' containment reference.
	 * @see #setModifier(Modifier)
	 * @see org.eclipse.gmt.modisco.java.neo4emf.meta.JavaPackage#getBodyDeclaration_Modifier()
	 * @see org.eclipse.gmt.modisco.java.Modifier#getBodyDeclaration
	 * @model opposite="bodyDeclaration" containment="true" ordered="false"
	 * @generated
	 */
	Modifier getModifier();
	/**
	 * Sets the value of the '{@link org.eclipse.gmt.modisco.java.BodyDeclaration#getModifier <em>Modifier</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 *YY1-BIS
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Modifier</em>' containment reference.
	 * @see #getModifier()
	 * @generated
	 */
	void setModifier(Modifier value);
 


/*
* Neo4EMF inserted code -- begin
*/

/*
* Neo4EMF inserted code -- end
*/




} // BodyDeclaration