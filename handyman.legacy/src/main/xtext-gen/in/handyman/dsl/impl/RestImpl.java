/**
 * generated by Xtext 2.25.0
 */
package in.handyman.dsl.impl;

import in.handyman.dsl.DslPackage;
import in.handyman.dsl.Rest;
import in.handyman.dsl.RestPart;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Rest</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link in.handyman.dsl.impl.RestImpl#getAuthtoken <em>Authtoken</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.RestImpl#getUrl <em>Url</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.RestImpl#getMethod <em>Method</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.RestImpl#getResourcedatafrom <em>Resourcedatafrom</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.RestImpl#getUrldata <em>Urldata</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.RestImpl#getHeaderdatafrom <em>Headerdatafrom</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.RestImpl#getHeaderdata <em>Headerdata</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.RestImpl#getPostdatafrom <em>Postdatafrom</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.RestImpl#getParentName <em>Parent Name</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.RestImpl#getParentdata <em>Parentdata</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.RestImpl#getParts <em>Parts</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.RestImpl#getAckdatato <em>Ackdatato</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.RestImpl#getAckdata <em>Ackdata</em>}</li>
 * </ul>
 *
 * @generated
 */
public class RestImpl extends ActionImpl implements Rest {
    /**
     * The default value of the '{@link #getAuthtoken() <em>Authtoken</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getAuthtoken()
     */
    protected static final String AUTHTOKEN_EDEFAULT = null;
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
     * The default value of the '{@link #getMethod() <em>Method</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getMethod()
     */
    protected static final String METHOD_EDEFAULT = null;
    /**
     * The default value of the '{@link #getResourcedatafrom() <em>Resourcedatafrom</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getResourcedatafrom()
     */
    protected static final String RESOURCEDATAFROM_EDEFAULT = null;
    /**
     * The default value of the '{@link #getUrldata() <em>Urldata</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getUrldata()
     */
    protected static final String URLDATA_EDEFAULT = null;
    /**
     * The default value of the '{@link #getHeaderdatafrom() <em>Headerdatafrom</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getHeaderdatafrom()
     */
    protected static final String HEADERDATAFROM_EDEFAULT = null;
    /**
     * The default value of the '{@link #getHeaderdata() <em>Headerdata</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getHeaderdata()
     */
    protected static final String HEADERDATA_EDEFAULT = null;
    /**
     * The default value of the '{@link #getPostdatafrom() <em>Postdatafrom</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getPostdatafrom()
     */
    protected static final String POSTDATAFROM_EDEFAULT = null;
    /**
     * The default value of the '{@link #getParentName() <em>Parent Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getParentName()
     */
    protected static final String PARENT_NAME_EDEFAULT = null;
    /**
     * The default value of the '{@link #getParentdata() <em>Parentdata</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getParentdata()
     */
    protected static final String PARENTDATA_EDEFAULT = null;
    /**
     * The default value of the '{@link #getAckdatato() <em>Ackdatato</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getAckdatato()
     */
    protected static final String ACKDATATO_EDEFAULT = null;
    /**
     * The default value of the '{@link #getAckdata() <em>Ackdata</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getAckdata()
     */
    protected static final String ACKDATA_EDEFAULT = null;
    /**
     * The cached value of the '{@link #getAuthtoken() <em>Authtoken</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getAuthtoken()
     */
    protected String authtoken = AUTHTOKEN_EDEFAULT;
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
     * The cached value of the '{@link #getMethod() <em>Method</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getMethod()
     */
    protected String method = METHOD_EDEFAULT;
    /**
     * The cached value of the '{@link #getResourcedatafrom() <em>Resourcedatafrom</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getResourcedatafrom()
     */
    protected String resourcedatafrom = RESOURCEDATAFROM_EDEFAULT;
    /**
     * The cached value of the '{@link #getUrldata() <em>Urldata</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getUrldata()
     */
    protected String urldata = URLDATA_EDEFAULT;
    /**
     * The cached value of the '{@link #getHeaderdatafrom() <em>Headerdatafrom</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getHeaderdatafrom()
     */
    protected String headerdatafrom = HEADERDATAFROM_EDEFAULT;
    /**
     * The cached value of the '{@link #getHeaderdata() <em>Headerdata</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getHeaderdata()
     */
    protected String headerdata = HEADERDATA_EDEFAULT;
    /**
     * The cached value of the '{@link #getPostdatafrom() <em>Postdatafrom</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getPostdatafrom()
     */
    protected String postdatafrom = POSTDATAFROM_EDEFAULT;
    /**
     * The cached value of the '{@link #getParentName() <em>Parent Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getParentName()
     */
    protected String parentName = PARENT_NAME_EDEFAULT;
    /**
     * The cached value of the '{@link #getParentdata() <em>Parentdata</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getParentdata()
     */
    protected String parentdata = PARENTDATA_EDEFAULT;
    /**
     * The cached value of the '{@link #getParts() <em>Parts</em>}' containment reference list.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getParts()
     */
    protected EList<RestPart> parts;
    /**
     * The cached value of the '{@link #getAckdatato() <em>Ackdatato</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getAckdatato()
     */
    protected String ackdatato = ACKDATATO_EDEFAULT;
    /**
     * The cached value of the '{@link #getAckdata() <em>Ackdata</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     * @ordered
     * @see #getAckdata()
     */
    protected String ackdata = ACKDATA_EDEFAULT;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    protected RestImpl() {
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
        return DslPackage.Literals.REST;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
            case DslPackage.REST__PARTS:
                return ((InternalEList<?>) getParts()).basicRemove(otherEnd, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public EList<RestPart> getParts() {
        if (parts == null) {
            parts = new EObjectContainmentEList<RestPart>(RestPart.class, this, DslPackage.REST__PARTS);
        }
        return parts;
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
            case DslPackage.REST__AUTHTOKEN:
                return getAuthtoken();
            case DslPackage.REST__URL:
                return getUrl();
            case DslPackage.REST__METHOD:
                return getMethod();
            case DslPackage.REST__RESOURCEDATAFROM:
                return getResourcedatafrom();
            case DslPackage.REST__URLDATA:
                return getUrldata();
            case DslPackage.REST__HEADERDATAFROM:
                return getHeaderdatafrom();
            case DslPackage.REST__HEADERDATA:
                return getHeaderdata();
            case DslPackage.REST__POSTDATAFROM:
                return getPostdatafrom();
            case DslPackage.REST__PARENT_NAME:
                return getParentName();
            case DslPackage.REST__PARENTDATA:
                return getParentdata();
            case DslPackage.REST__PARTS:
                return getParts();
            case DslPackage.REST__ACKDATATO:
                return getAckdatato();
            case DslPackage.REST__ACKDATA:
                return getAckdata();
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
    public String getAuthtoken() {
        return authtoken;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setAuthtoken(String newAuthtoken) {
        String oldAuthtoken = authtoken;
        authtoken = newAuthtoken;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.REST__AUTHTOKEN, oldAuthtoken, authtoken));
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
            eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.REST__URL, oldUrl, url));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getMethod() {
        return method;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setMethod(String newMethod) {
        String oldMethod = method;
        method = newMethod;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.REST__METHOD, oldMethod, method));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getResourcedatafrom() {
        return resourcedatafrom;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setResourcedatafrom(String newResourcedatafrom) {
        String oldResourcedatafrom = resourcedatafrom;
        resourcedatafrom = newResourcedatafrom;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.REST__RESOURCEDATAFROM, oldResourcedatafrom, resourcedatafrom));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getUrldata() {
        return urldata;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setUrldata(String newUrldata) {
        String oldUrldata = urldata;
        urldata = newUrldata;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.REST__URLDATA, oldUrldata, urldata));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getHeaderdatafrom() {
        return headerdatafrom;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setHeaderdatafrom(String newHeaderdatafrom) {
        String oldHeaderdatafrom = headerdatafrom;
        headerdatafrom = newHeaderdatafrom;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.REST__HEADERDATAFROM, oldHeaderdatafrom, headerdatafrom));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getHeaderdata() {
        return headerdata;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setHeaderdata(String newHeaderdata) {
        String oldHeaderdata = headerdata;
        headerdata = newHeaderdata;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.REST__HEADERDATA, oldHeaderdata, headerdata));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getPostdatafrom() {
        return postdatafrom;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setPostdatafrom(String newPostdatafrom) {
        String oldPostdatafrom = postdatafrom;
        postdatafrom = newPostdatafrom;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.REST__POSTDATAFROM, oldPostdatafrom, postdatafrom));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getParentName() {
        return parentName;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setParentName(String newParentName) {
        String oldParentName = parentName;
        parentName = newParentName;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.REST__PARENT_NAME, oldParentName, parentName));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getParentdata() {
        return parentdata;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setParentdata(String newParentdata) {
        String oldParentdata = parentdata;
        parentdata = newParentdata;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.REST__PARENTDATA, oldParentdata, parentdata));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getAckdatato() {
        return ackdatato;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setAckdatato(String newAckdatato) {
        String oldAckdatato = ackdatato;
        ackdatato = newAckdatato;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.REST__ACKDATATO, oldAckdatato, ackdatato));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public String getAckdata() {
        return ackdata;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @Override
    public void setAckdata(String newAckdata) {
        String oldAckdata = ackdata;
        ackdata = newAckdata;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.REST__ACKDATA, oldAckdata, ackdata));
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @generated
     */
    @SuppressWarnings("unchecked")
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
            case DslPackage.REST__AUTHTOKEN:
                setAuthtoken((String) newValue);
                return;
            case DslPackage.REST__URL:
                setUrl((String) newValue);
                return;
            case DslPackage.REST__METHOD:
                setMethod((String) newValue);
                return;
            case DslPackage.REST__RESOURCEDATAFROM:
                setResourcedatafrom((String) newValue);
                return;
            case DslPackage.REST__URLDATA:
                setUrldata((String) newValue);
                return;
            case DslPackage.REST__HEADERDATAFROM:
                setHeaderdatafrom((String) newValue);
                return;
            case DslPackage.REST__HEADERDATA:
                setHeaderdata((String) newValue);
                return;
            case DslPackage.REST__POSTDATAFROM:
                setPostdatafrom((String) newValue);
                return;
            case DslPackage.REST__PARENT_NAME:
                setParentName((String) newValue);
                return;
            case DslPackage.REST__PARENTDATA:
                setParentdata((String) newValue);
                return;
            case DslPackage.REST__PARTS:
                getParts().clear();
                getParts().addAll((Collection<? extends RestPart>) newValue);
                return;
            case DslPackage.REST__ACKDATATO:
                setAckdatato((String) newValue);
                return;
            case DslPackage.REST__ACKDATA:
                setAckdata((String) newValue);
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
            case DslPackage.REST__AUTHTOKEN:
                setAuthtoken(AUTHTOKEN_EDEFAULT);
                return;
            case DslPackage.REST__URL:
                setUrl(URL_EDEFAULT);
                return;
            case DslPackage.REST__METHOD:
                setMethod(METHOD_EDEFAULT);
                return;
            case DslPackage.REST__RESOURCEDATAFROM:
                setResourcedatafrom(RESOURCEDATAFROM_EDEFAULT);
                return;
            case DslPackage.REST__URLDATA:
                setUrldata(URLDATA_EDEFAULT);
                return;
            case DslPackage.REST__HEADERDATAFROM:
                setHeaderdatafrom(HEADERDATAFROM_EDEFAULT);
                return;
            case DslPackage.REST__HEADERDATA:
                setHeaderdata(HEADERDATA_EDEFAULT);
                return;
            case DslPackage.REST__POSTDATAFROM:
                setPostdatafrom(POSTDATAFROM_EDEFAULT);
                return;
            case DslPackage.REST__PARENT_NAME:
                setParentName(PARENT_NAME_EDEFAULT);
                return;
            case DslPackage.REST__PARENTDATA:
                setParentdata(PARENTDATA_EDEFAULT);
                return;
            case DslPackage.REST__PARTS:
                getParts().clear();
                return;
            case DslPackage.REST__ACKDATATO:
                setAckdatato(ACKDATATO_EDEFAULT);
                return;
            case DslPackage.REST__ACKDATA:
                setAckdata(ACKDATA_EDEFAULT);
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
            case DslPackage.REST__AUTHTOKEN:
                return AUTHTOKEN_EDEFAULT == null ? authtoken != null : !AUTHTOKEN_EDEFAULT.equals(authtoken);
            case DslPackage.REST__URL:
                return URL_EDEFAULT == null ? url != null : !URL_EDEFAULT.equals(url);
            case DslPackage.REST__METHOD:
                return METHOD_EDEFAULT == null ? method != null : !METHOD_EDEFAULT.equals(method);
            case DslPackage.REST__RESOURCEDATAFROM:
                return RESOURCEDATAFROM_EDEFAULT == null ? resourcedatafrom != null : !RESOURCEDATAFROM_EDEFAULT.equals(resourcedatafrom);
            case DslPackage.REST__URLDATA:
                return URLDATA_EDEFAULT == null ? urldata != null : !URLDATA_EDEFAULT.equals(urldata);
            case DslPackage.REST__HEADERDATAFROM:
                return HEADERDATAFROM_EDEFAULT == null ? headerdatafrom != null : !HEADERDATAFROM_EDEFAULT.equals(headerdatafrom);
            case DslPackage.REST__HEADERDATA:
                return HEADERDATA_EDEFAULT == null ? headerdata != null : !HEADERDATA_EDEFAULT.equals(headerdata);
            case DslPackage.REST__POSTDATAFROM:
                return POSTDATAFROM_EDEFAULT == null ? postdatafrom != null : !POSTDATAFROM_EDEFAULT.equals(postdatafrom);
            case DslPackage.REST__PARENT_NAME:
                return PARENT_NAME_EDEFAULT == null ? parentName != null : !PARENT_NAME_EDEFAULT.equals(parentName);
            case DslPackage.REST__PARENTDATA:
                return PARENTDATA_EDEFAULT == null ? parentdata != null : !PARENTDATA_EDEFAULT.equals(parentdata);
            case DslPackage.REST__PARTS:
                return parts != null && !parts.isEmpty();
            case DslPackage.REST__ACKDATATO:
                return ACKDATATO_EDEFAULT == null ? ackdatato != null : !ACKDATATO_EDEFAULT.equals(ackdatato);
            case DslPackage.REST__ACKDATA:
                return ACKDATA_EDEFAULT == null ? ackdata != null : !ACKDATA_EDEFAULT.equals(ackdata);
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
        result.append(" (authtoken: ");
        result.append(authtoken);
        result.append(", url: ");
        result.append(url);
        result.append(", method: ");
        result.append(method);
        result.append(", resourcedatafrom: ");
        result.append(resourcedatafrom);
        result.append(", urldata: ");
        result.append(urldata);
        result.append(", headerdatafrom: ");
        result.append(headerdatafrom);
        result.append(", headerdata: ");
        result.append(headerdata);
        result.append(", postdatafrom: ");
        result.append(postdatafrom);
        result.append(", parentName: ");
        result.append(parentName);
        result.append(", parentdata: ");
        result.append(parentdata);
        result.append(", ackdatato: ");
        result.append(ackdatato);
        result.append(", ackdata: ");
        result.append(ackdata);
        result.append(')');
        return result.toString();
    }

} //RestImpl
