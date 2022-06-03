/**
 * generated by Xtext 2.16.0
 */
package in.handyman.dsl.impl;

import in.handyman.dsl.DeleteFolder;
import in.handyman.dsl.DslPackage;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Delete Folder</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link in.handyman.dsl.impl.DeleteFolderImpl#getFoldersource <em>Foldersource</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.DeleteFolderImpl#getZipsource <em>Zipsource</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.DeleteFolderImpl#getValue <em>Value</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DeleteFolderImpl extends ActionImpl implements DeleteFolder
{
  /**
   * The default value of the '{@link #getFoldersource() <em>Foldersource</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getFoldersource()
   * @generated
   * @ordered
   */
  protected static final String FOLDERSOURCE_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getFoldersource() <em>Foldersource</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getFoldersource()
   * @generated
   * @ordered
   */
  protected String foldersource = FOLDERSOURCE_EDEFAULT;

  /**
   * The default value of the '{@link #getZipsource() <em>Zipsource</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getZipsource()
   * @generated
   * @ordered
   */
  protected static final String ZIPSOURCE_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getZipsource() <em>Zipsource</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getZipsource()
   * @generated
   * @ordered
   */
  protected String zipsource = ZIPSOURCE_EDEFAULT;

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
  protected DeleteFolderImpl()
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
    return DslPackage.Literals.DELETE_FOLDER;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getFoldersource()
  {
    return foldersource;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setFoldersource(String newFoldersource)
  {
    String oldFoldersource = foldersource;
    foldersource = newFoldersource;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.DELETE_FOLDER__FOLDERSOURCE, oldFoldersource, foldersource));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getZipsource()
  {
    return zipsource;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setZipsource(String newZipsource)
  {
    String oldZipsource = zipsource;
    zipsource = newZipsource;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.DELETE_FOLDER__ZIPSOURCE, oldZipsource, zipsource));
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
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.DELETE_FOLDER__VALUE, oldValue, value));
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
      case DslPackage.DELETE_FOLDER__FOLDERSOURCE:
        return getFoldersource();
      case DslPackage.DELETE_FOLDER__ZIPSOURCE:
        return getZipsource();
      case DslPackage.DELETE_FOLDER__VALUE:
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
      case DslPackage.DELETE_FOLDER__FOLDERSOURCE:
        setFoldersource((String)newValue);
        return;
      case DslPackage.DELETE_FOLDER__ZIPSOURCE:
        setZipsource((String)newValue);
        return;
      case DslPackage.DELETE_FOLDER__VALUE:
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
      case DslPackage.DELETE_FOLDER__FOLDERSOURCE:
        setFoldersource(FOLDERSOURCE_EDEFAULT);
        return;
      case DslPackage.DELETE_FOLDER__ZIPSOURCE:
        setZipsource(ZIPSOURCE_EDEFAULT);
        return;
      case DslPackage.DELETE_FOLDER__VALUE:
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
      case DslPackage.DELETE_FOLDER__FOLDERSOURCE:
        return FOLDERSOURCE_EDEFAULT == null ? foldersource != null : !FOLDERSOURCE_EDEFAULT.equals(foldersource);
      case DslPackage.DELETE_FOLDER__ZIPSOURCE:
        return ZIPSOURCE_EDEFAULT == null ? zipsource != null : !ZIPSOURCE_EDEFAULT.equals(zipsource);
      case DslPackage.DELETE_FOLDER__VALUE:
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
    result.append(" (foldersource: ");
    result.append(foldersource);
    result.append(", zipsource: ");
    result.append(zipsource);
    result.append(", value: ");
    result.append(value);
    result.append(')');
    return result.toString();
  }

} //DeleteFolderImpl
