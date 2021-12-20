/**
 * generated by Xtext 2.16.0
 */
package in.handyman.dsl;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Rest Part</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link in.handyman.dsl.RestPart#getPartName <em>Part Name</em>}</li>
 *   <li>{@link in.handyman.dsl.RestPart#getPartData <em>Part Data</em>}</li>
 * </ul>
 *
 * @see in.handyman.dsl.DslPackage#getRestPart()
 * @model
 * @generated
 */
public interface RestPart extends EObject
{
  /**
   * Returns the value of the '<em><b>Part Name</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Part Name</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Part Name</em>' attribute.
   * @see #setPartName(String)
   * @see in.handyman.dsl.DslPackage#getRestPart_PartName()
   * @model
   * @generated
   */
  String getPartName();

  /**
   * Sets the value of the '{@link in.handyman.dsl.RestPart#getPartName <em>Part Name</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Part Name</em>' attribute.
   * @see #getPartName()
   * @generated
   */
  void setPartName(String value);

  /**
   * Returns the value of the '<em><b>Part Data</b></em>' attribute.
   * <!-- begin-user-doc -->
   * <p>
   * If the meaning of the '<em>Part Data</em>' attribute isn't clear,
   * there really should be more of a description here...
   * </p>
   * <!-- end-user-doc -->
   * @return the value of the '<em>Part Data</em>' attribute.
   * @see #setPartData(String)
   * @see in.handyman.dsl.DslPackage#getRestPart_PartData()
   * @model
   * @generated
   */
  String getPartData();

  /**
   * Sets the value of the '{@link in.handyman.dsl.RestPart#getPartData <em>Part Data</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param value the new value of the '<em>Part Data</em>' attribute.
   * @see #getPartData()
   * @generated
   */
  void setPartData(String value);

} // RestPart
