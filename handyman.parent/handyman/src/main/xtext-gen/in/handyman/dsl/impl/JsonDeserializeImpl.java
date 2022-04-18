/**
 * generated by Xtext 2.16.0
 */
package in.handyman.dsl.impl;

import in.handyman.dsl.DslPackage;
import in.handyman.dsl.JsonDeserialize;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Json Deserialize</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link in.handyman.dsl.impl.JsonDeserializeImpl#getSource <em>Source</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.JsonDeserializeImpl#getTargetTable <em>Target Table</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.JsonDeserializeImpl#getInput <em>Input</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.JsonDeserializeImpl#getValue <em>Value</em>}</li>
 * </ul>
 *
 * @generated
 */
public class JsonDeserializeImpl extends ActionImpl implements JsonDeserialize
{
  /**
   * The default value of the '{@link #getSource() <em>Source</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSource()
   * @generated
   * @ordered
   */
  protected static final String SOURCE_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getSource() <em>Source</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSource()
   * @generated
   * @ordered
   */
  protected String source = SOURCE_EDEFAULT;

  /**
   * The default value of the '{@link #getTargetTable() <em>Target Table</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getTargetTable()
   * @generated
   * @ordered
   */
  protected static final String TARGET_TABLE_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getTargetTable() <em>Target Table</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getTargetTable()
   * @generated
   * @ordered
   */
  protected String targetTable = TARGET_TABLE_EDEFAULT;

  /**
   * The default value of the '{@link #getInput() <em>Input</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getInput()
   * @generated
   * @ordered
   */
  protected static final String INPUT_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getInput() <em>Input</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getInput()
   * @generated
   * @ordered
   */
  protected String input = INPUT_EDEFAULT;

  /**
   * The default value of the '{@link #getValue() <em>Value</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getValue()
   * @generated
   * @ordered
   */
  protected static final String VALUE_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getValue() <em>Value</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getValue()
   * @generated
   * @ordered
   */
  protected String value = VALUE_EDEFAULT;

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected JsonDeserializeImpl()
  {
    super();
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  protected EClass eStaticClass()
  {
    return DslPackage.Literals.JSON_DESERIALIZE;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getSource()
  {
    return source;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setSource(String newSource)
  {
    String oldSource = source;
    source = newSource;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.JSON_DESERIALIZE__SOURCE, oldSource, source));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getTargetTable()
  {
    return targetTable;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setTargetTable(String newTargetTable)
  {
    String oldTargetTable = targetTable;
    targetTable = newTargetTable;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.JSON_DESERIALIZE__TARGET_TABLE, oldTargetTable, targetTable));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getInput()
  {
    return input;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setInput(String newInput)
  {
    String oldInput = input;
    input = newInput;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.JSON_DESERIALIZE__INPUT, oldInput, input));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getValue()
  {
    return value;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setValue(String newValue)
  {
    String oldValue = value;
    value = newValue;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.JSON_DESERIALIZE__VALUE, oldValue, value));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public Object eGet(int featureID, boolean resolve, boolean coreType)
  {
    switch (featureID)
    {
      case DslPackage.JSON_DESERIALIZE__SOURCE:
        return getSource();
      case DslPackage.JSON_DESERIALIZE__TARGET_TABLE:
        return getTargetTable();
      case DslPackage.JSON_DESERIALIZE__INPUT:
        return getInput();
      case DslPackage.JSON_DESERIALIZE__VALUE:
        return getValue();
    }
    return super.eGet(featureID, resolve, coreType);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void eSet(int featureID, Object newValue)
  {
    switch (featureID)
    {
      case DslPackage.JSON_DESERIALIZE__SOURCE:
        setSource((String)newValue);
        return;
      case DslPackage.JSON_DESERIALIZE__TARGET_TABLE:
        setTargetTable((String)newValue);
        return;
      case DslPackage.JSON_DESERIALIZE__INPUT:
        setInput((String)newValue);
        return;
      case DslPackage.JSON_DESERIALIZE__VALUE:
        setValue((String)newValue);
        return;
    }
    super.eSet(featureID, newValue);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public void eUnset(int featureID)
  {
    switch (featureID)
    {
      case DslPackage.JSON_DESERIALIZE__SOURCE:
        setSource(SOURCE_EDEFAULT);
        return;
      case DslPackage.JSON_DESERIALIZE__TARGET_TABLE:
        setTargetTable(TARGET_TABLE_EDEFAULT);
        return;
      case DslPackage.JSON_DESERIALIZE__INPUT:
        setInput(INPUT_EDEFAULT);
        return;
      case DslPackage.JSON_DESERIALIZE__VALUE:
        setValue(VALUE_EDEFAULT);
        return;
    }
    super.eUnset(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public boolean eIsSet(int featureID)
  {
    switch (featureID)
    {
      case DslPackage.JSON_DESERIALIZE__SOURCE:
        return SOURCE_EDEFAULT == null ? source != null : !SOURCE_EDEFAULT.equals(source);
      case DslPackage.JSON_DESERIALIZE__TARGET_TABLE:
        return TARGET_TABLE_EDEFAULT == null ? targetTable != null : !TARGET_TABLE_EDEFAULT.equals(targetTable);
      case DslPackage.JSON_DESERIALIZE__INPUT:
        return INPUT_EDEFAULT == null ? input != null : !INPUT_EDEFAULT.equals(input);
      case DslPackage.JSON_DESERIALIZE__VALUE:
        return VALUE_EDEFAULT == null ? value != null : !VALUE_EDEFAULT.equals(value);
    }
    return super.eIsSet(featureID);
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  @Override
  public String toString()
  {
    if (eIsProxy()) return super.toString();

    StringBuffer result = new StringBuffer(super.toString());
    result.append(" (source: ");
    result.append(source);
    result.append(", targetTable: ");
    result.append(targetTable);
    result.append(", input: ");
    result.append(input);
    result.append(", value: ");
    result.append(value);
    result.append(')');
    return result.toString();
  }

} //JsonDeserializeImpl
