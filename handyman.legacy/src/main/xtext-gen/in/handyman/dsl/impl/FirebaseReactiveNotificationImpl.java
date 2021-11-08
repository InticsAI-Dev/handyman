/**
 * generated by Xtext 2.25.0
 */
package in.handyman.dsl.impl;

import in.handyman.dsl.DslPackage;
import in.handyman.dsl.FirebaseReactiveNotification;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Firebase Reactive Notification</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link in.handyman.dsl.impl.FirebaseReactiveNotificationImpl#getUrl <em>Url</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.FirebaseReactiveNotificationImpl#getFbjson <em>Fbjson</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.FirebaseReactiveNotificationImpl#getGroupPath <em>Group Path</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.FirebaseReactiveNotificationImpl#getClassFqn <em>Class Fqn</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.FirebaseReactiveNotificationImpl#getDbSrc <em>Db Src</em>}</li>
 * </ul>
 *
 * @generated
 */
public class FirebaseReactiveNotificationImpl extends ActionImpl implements FirebaseReactiveNotification {
    /**
     * The default value of the '{@link #getUrl() <em>Url</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getUrl()
     */
    protected static final String URL_EDEFAULT = null;
    /**
     * The default value of the '{@link #getFbjson() <em>Fbjson</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getFbjson()
     */
    protected static final String FBJSON_EDEFAULT = null;
    /**
     * The default value of the '{@link #getGroupPath() <em>Group Path</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getGroupPath()
     */
    protected static final String GROUP_PATH_EDEFAULT = null;
    /**
     * The default value of the '{@link #getClassFqn() <em>Class Fqn</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getClassFqn()
     */
    protected static final String CLASS_FQN_EDEFAULT = null;
    /**
     * The default value of the '{@link #getDbSrc() <em>Db Src</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getDbSrc()
     */
    protected static final String DB_SRC_EDEFAULT = null;
    /**
     * The cached value of the '{@link #getUrl() <em>Url</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getUrl()
     */
    protected String url = URL_EDEFAULT;
    /**
     * The cached value of the '{@link #getFbjson() <em>Fbjson</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getFbjson()
     */
    protected String fbjson = FBJSON_EDEFAULT;
    /**
     * The cached value of the '{@link #getGroupPath() <em>Group Path</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getGroupPath()
     */
    protected String groupPath = GROUP_PATH_EDEFAULT;
    /**
     * The cached value of the '{@link #getClassFqn() <em>Class Fqn</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getClassFqn()
     */
    protected String classFqn = CLASS_FQN_EDEFAULT;
    /**
     * The cached value of the '{@link #getDbSrc() <em>Db Src</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getDbSrc()
     */
    protected String dbSrc = DB_SRC_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    protected FirebaseReactiveNotificationImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return DslPackage.Literals.FIREBASE_REACTIVE_NOTIFICATION;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
            case DslPackage.FIREBASE_REACTIVE_NOTIFICATION__URL:
                return getUrl();
            case DslPackage.FIREBASE_REACTIVE_NOTIFICATION__FBJSON:
                return getFbjson();
            case DslPackage.FIREBASE_REACTIVE_NOTIFICATION__GROUP_PATH:
                return getGroupPath();
            case DslPackage.FIREBASE_REACTIVE_NOTIFICATION__CLASS_FQN:
                return getClassFqn();
            case DslPackage.FIREBASE_REACTIVE_NOTIFICATION__DB_SRC:
                return getDbSrc();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getUrl() {
        return url;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setUrl(String newUrl) {
        String oldUrl = url;
        url = newUrl;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.FIREBASE_REACTIVE_NOTIFICATION__URL, oldUrl, url));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getFbjson() {
        return fbjson;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setFbjson(String newFbjson) {
        String oldFbjson = fbjson;
        fbjson = newFbjson;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.FIREBASE_REACTIVE_NOTIFICATION__FBJSON, oldFbjson, fbjson));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getGroupPath() {
        return groupPath;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setGroupPath(String newGroupPath) {
        String oldGroupPath = groupPath;
        groupPath = newGroupPath;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.FIREBASE_REACTIVE_NOTIFICATION__GROUP_PATH, oldGroupPath, groupPath));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getClassFqn() {
        return classFqn;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setClassFqn(String newClassFqn) {
        String oldClassFqn = classFqn;
        classFqn = newClassFqn;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.FIREBASE_REACTIVE_NOTIFICATION__CLASS_FQN, oldClassFqn, classFqn));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getDbSrc() {
        return dbSrc;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setDbSrc(String newDbSrc) {
        String oldDbSrc = dbSrc;
        dbSrc = newDbSrc;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.FIREBASE_REACTIVE_NOTIFICATION__DB_SRC, oldDbSrc, dbSrc));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case DslPackage.FIREBASE_REACTIVE_NOTIFICATION__URL:
                setUrl((String) newValue);
                return;
            case DslPackage.FIREBASE_REACTIVE_NOTIFICATION__FBJSON:
                setFbjson((String) newValue);
                return;
            case DslPackage.FIREBASE_REACTIVE_NOTIFICATION__GROUP_PATH:
                setGroupPath((String) newValue);
                return;
            case DslPackage.FIREBASE_REACTIVE_NOTIFICATION__CLASS_FQN:
                setClassFqn((String) newValue);
                return;
            case DslPackage.FIREBASE_REACTIVE_NOTIFICATION__DB_SRC:
                setDbSrc((String) newValue);
                return;
        }
        super.eSet(featureID, newValue);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void eUnset(int featureID) {
        switch (featureID) {
            case DslPackage.FIREBASE_REACTIVE_NOTIFICATION__URL:
                setUrl(URL_EDEFAULT);
                return;
            case DslPackage.FIREBASE_REACTIVE_NOTIFICATION__FBJSON:
                setFbjson(FBJSON_EDEFAULT);
                return;
            case DslPackage.FIREBASE_REACTIVE_NOTIFICATION__GROUP_PATH:
                setGroupPath(GROUP_PATH_EDEFAULT);
                return;
            case DslPackage.FIREBASE_REACTIVE_NOTIFICATION__CLASS_FQN:
                setClassFqn(CLASS_FQN_EDEFAULT);
                return;
            case DslPackage.FIREBASE_REACTIVE_NOTIFICATION__DB_SRC:
                setDbSrc(DB_SRC_EDEFAULT);
                return;
        }
        super.eUnset(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public boolean eIsSet(int featureID) {
        switch (featureID) {
            case DslPackage.FIREBASE_REACTIVE_NOTIFICATION__URL:
                return URL_EDEFAULT == null ? url != null : !URL_EDEFAULT.equals(url);
            case DslPackage.FIREBASE_REACTIVE_NOTIFICATION__FBJSON:
                return FBJSON_EDEFAULT == null ? fbjson != null : !FBJSON_EDEFAULT.equals(fbjson);
            case DslPackage.FIREBASE_REACTIVE_NOTIFICATION__GROUP_PATH:
                return GROUP_PATH_EDEFAULT == null ? groupPath != null : !GROUP_PATH_EDEFAULT.equals(groupPath);
            case DslPackage.FIREBASE_REACTIVE_NOTIFICATION__CLASS_FQN:
                return CLASS_FQN_EDEFAULT == null ? classFqn != null : !CLASS_FQN_EDEFAULT.equals(classFqn);
            case DslPackage.FIREBASE_REACTIVE_NOTIFICATION__DB_SRC:
                return DB_SRC_EDEFAULT == null ? dbSrc != null : !DB_SRC_EDEFAULT.equals(dbSrc);
        }
        return super.eIsSet(featureID);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String toString() {
        if (eIsProxy()) return super.toString();

        StringBuilder result = new StringBuilder(super.toString());
        result.append(" (url: ");
        result.append(url);
        result.append(", fbjson: ");
        result.append(fbjson);
        result.append(", groupPath: ");
        result.append(groupPath);
        result.append(", classFqn: ");
        result.append(classFqn);
        result.append(", dbSrc: ");
        result.append(dbSrc);
        result.append(')');
        return result.toString();
    }

} //FirebaseReactiveNotificationImpl
