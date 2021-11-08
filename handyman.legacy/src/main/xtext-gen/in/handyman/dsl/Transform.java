/**
 * generated by Xtext 2.25.0
 */
package in.handyman.dsl;

import org.eclipse.emf.common.util.EList;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Transform</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link in.handyman.dsl.Transform#getOn <em>On</em>}</li>
 *   <li>{@link in.handyman.dsl.Transform#getValue <em>Value</em>}</li>
 * </ul>
 *
 * @model
 * @generated
 * @see in.handyman.dsl.DslPackage#getTransform()
 */
public interface Transform extends Action {
    /**
     * Returns the value of the '<em><b>On</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>On</em>' attribute.
     * @model
     * @generated
     * @see #setOn(String)
     * @see in.handyman.dsl.DslPackage#getTransform_On()
     */
    String getOn();

    /**
     * Sets the value of the '{@link in.handyman.dsl.Transform#getOn <em>On</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @param value the new value of the '<em>On</em>' attribute.
     * @generated
     * @see #getOn()
     */
    void setOn(String value);

    /**
     * Returns the value of the '<em><b>Value</b></em>' attribute list.
     * The list contents are of type {@link java.lang.String}.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Value</em>' attribute list.
     * @model unique="false"
     * @generated
     * @see in.handyman.dsl.DslPackage#getTransform_Value()
     */
    EList<String> getValue();

} // Transform
