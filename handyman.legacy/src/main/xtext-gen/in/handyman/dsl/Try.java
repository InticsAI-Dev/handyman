/**
 * generated by Xtext 2.25.0
 */
package in.handyman.dsl;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Try</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link in.handyman.dsl.Try#getName <em>Name</em>}</li>
 *   <li>{@link in.handyman.dsl.Try#getAction <em>Action</em>}</li>
 * </ul>
 *
 * @model
 * @generated
 * @see in.handyman.dsl.DslPackage#getTry()
 */
public interface Try extends EObject {
    /**
     * Returns the value of the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Name</em>' attribute.
     * @model
     * @generated
     * @see #setName(String)
     * @see in.handyman.dsl.DslPackage#getTry_Name()
     */
    String getName();

    /**
     * Sets the value of the '{@link in.handyman.dsl.Try#getName <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @param value the new value of the '<em>Name</em>' attribute.
     * @generated
     * @see #getName()
     */
    void setName(String value);

    /**
     * Returns the value of the '<em><b>Action</b></em>' containment reference list.
     * The list contents are of type {@link in.handyman.dsl.Action}.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Action</em>' containment reference list.
     * @model containment="true"
     * @generated
     * @see in.handyman.dsl.DslPackage#getTry_Action()
     */
    EList<Action> getAction();

} // Try
