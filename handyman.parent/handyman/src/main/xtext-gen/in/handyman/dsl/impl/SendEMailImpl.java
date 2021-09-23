/**
 * generated by Xtext 2.16.0
 */
package in.handyman.dsl.impl;

import in.handyman.dsl.DslPackage;
import in.handyman.dsl.SendEMail;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Send EMail</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link in.handyman.dsl.impl.SendEMailImpl#getSource <em>Source</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.SendEMailImpl#getSmtphost <em>Smtphost</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.SendEMailImpl#getSmtpport <em>Smtpport</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.SendEMailImpl#getFrom <em>From</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.SendEMailImpl#getPass <em>Pass</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.SendEMailImpl#getTo <em>To</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.SendEMailImpl#getCc <em>Cc</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.SendEMailImpl#getBcc <em>Bcc</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.SendEMailImpl#getSubject <em>Subject</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.SendEMailImpl#getBody <em>Body</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.SendEMailImpl#getSignature <em>Signature</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.SendEMailImpl#getValue <em>Value</em>}</li>
 * </ul>
 *
 * @generated
 */
public class SendEMailImpl extends ActionImpl implements SendEMail
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
   * The default value of the '{@link #getSmtphost() <em>Smtphost</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSmtphost()
   * @generated
   * @ordered
   */
  protected static final String SMTPHOST_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getSmtphost() <em>Smtphost</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSmtphost()
   * @generated
   * @ordered
   */
  protected String smtphost = SMTPHOST_EDEFAULT;

  /**
   * The default value of the '{@link #getSmtpport() <em>Smtpport</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSmtpport()
   * @generated
   * @ordered
   */
  protected static final String SMTPPORT_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getSmtpport() <em>Smtpport</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSmtpport()
   * @generated
   * @ordered
   */
  protected String smtpport = SMTPPORT_EDEFAULT;

  /**
   * The default value of the '{@link #getFrom() <em>From</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getFrom()
   * @generated
   * @ordered
   */
  protected static final String FROM_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getFrom() <em>From</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getFrom()
   * @generated
   * @ordered
   */
  protected String from = FROM_EDEFAULT;

  /**
   * The default value of the '{@link #getPass() <em>Pass</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getPass()
   * @generated
   * @ordered
   */
  protected static final String PASS_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getPass() <em>Pass</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getPass()
   * @generated
   * @ordered
   */
  protected String pass = PASS_EDEFAULT;

  /**
   * The default value of the '{@link #getTo() <em>To</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getTo()
   * @generated
   * @ordered
   */
  protected static final String TO_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getTo() <em>To</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getTo()
   * @generated
   * @ordered
   */
  protected String to = TO_EDEFAULT;

  /**
   * The default value of the '{@link #getCc() <em>Cc</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getCc()
   * @generated
   * @ordered
   */
  protected static final String CC_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getCc() <em>Cc</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getCc()
   * @generated
   * @ordered
   */
  protected String cc = CC_EDEFAULT;

  /**
   * The default value of the '{@link #getBcc() <em>Bcc</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getBcc()
   * @generated
   * @ordered
   */
  protected static final String BCC_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getBcc() <em>Bcc</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getBcc()
   * @generated
   * @ordered
   */
  protected String bcc = BCC_EDEFAULT;

  /**
   * The default value of the '{@link #getSubject() <em>Subject</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSubject()
   * @generated
   * @ordered
   */
  protected static final String SUBJECT_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getSubject() <em>Subject</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSubject()
   * @generated
   * @ordered
   */
  protected String subject = SUBJECT_EDEFAULT;

  /**
   * The default value of the '{@link #getBody() <em>Body</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getBody()
   * @generated
   * @ordered
   */
  protected static final String BODY_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getBody() <em>Body</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getBody()
   * @generated
   * @ordered
   */
  protected String body = BODY_EDEFAULT;

  /**
   * The default value of the '{@link #getSignature() <em>Signature</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSignature()
   * @generated
   * @ordered
   */
  protected static final String SIGNATURE_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getSignature() <em>Signature</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSignature()
   * @generated
   * @ordered
   */
  protected String signature = SIGNATURE_EDEFAULT;

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
  protected SendEMailImpl()
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
    return DslPackage.Literals.SEND_EMAIL;
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
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.SEND_EMAIL__SOURCE, oldSource, source));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getSmtphost()
  {
    return smtphost;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setSmtphost(String newSmtphost)
  {
    String oldSmtphost = smtphost;
    smtphost = newSmtphost;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.SEND_EMAIL__SMTPHOST, oldSmtphost, smtphost));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getSmtpport()
  {
    return smtpport;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setSmtpport(String newSmtpport)
  {
    String oldSmtpport = smtpport;
    smtpport = newSmtpport;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.SEND_EMAIL__SMTPPORT, oldSmtpport, smtpport));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getFrom()
  {
    return from;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setFrom(String newFrom)
  {
    String oldFrom = from;
    from = newFrom;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.SEND_EMAIL__FROM, oldFrom, from));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getPass()
  {
    return pass;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setPass(String newPass)
  {
    String oldPass = pass;
    pass = newPass;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.SEND_EMAIL__PASS, oldPass, pass));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getTo()
  {
    return to;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setTo(String newTo)
  {
    String oldTo = to;
    to = newTo;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.SEND_EMAIL__TO, oldTo, to));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getCc()
  {
    return cc;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setCc(String newCc)
  {
    String oldCc = cc;
    cc = newCc;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.SEND_EMAIL__CC, oldCc, cc));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getBcc()
  {
    return bcc;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setBcc(String newBcc)
  {
    String oldBcc = bcc;
    bcc = newBcc;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.SEND_EMAIL__BCC, oldBcc, bcc));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getSubject()
  {
    return subject;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setSubject(String newSubject)
  {
    String oldSubject = subject;
    subject = newSubject;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.SEND_EMAIL__SUBJECT, oldSubject, subject));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getBody()
  {
    return body;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setBody(String newBody)
  {
    String oldBody = body;
    body = newBody;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.SEND_EMAIL__BODY, oldBody, body));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getSignature()
  {
    return signature;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setSignature(String newSignature)
  {
    String oldSignature = signature;
    signature = newSignature;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.SEND_EMAIL__SIGNATURE, oldSignature, signature));
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
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.SEND_EMAIL__VALUE, oldValue, value));
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
      case DslPackage.SEND_EMAIL__SOURCE:
        return getSource();
      case DslPackage.SEND_EMAIL__SMTPHOST:
        return getSmtphost();
      case DslPackage.SEND_EMAIL__SMTPPORT:
        return getSmtpport();
      case DslPackage.SEND_EMAIL__FROM:
        return getFrom();
      case DslPackage.SEND_EMAIL__PASS:
        return getPass();
      case DslPackage.SEND_EMAIL__TO:
        return getTo();
      case DslPackage.SEND_EMAIL__CC:
        return getCc();
      case DslPackage.SEND_EMAIL__BCC:
        return getBcc();
      case DslPackage.SEND_EMAIL__SUBJECT:
        return getSubject();
      case DslPackage.SEND_EMAIL__BODY:
        return getBody();
      case DslPackage.SEND_EMAIL__SIGNATURE:
        return getSignature();
      case DslPackage.SEND_EMAIL__VALUE:
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
      case DslPackage.SEND_EMAIL__SOURCE:
        setSource((String)newValue);
        return;
      case DslPackage.SEND_EMAIL__SMTPHOST:
        setSmtphost((String)newValue);
        return;
      case DslPackage.SEND_EMAIL__SMTPPORT:
        setSmtpport((String)newValue);
        return;
      case DslPackage.SEND_EMAIL__FROM:
        setFrom((String)newValue);
        return;
      case DslPackage.SEND_EMAIL__PASS:
        setPass((String)newValue);
        return;
      case DslPackage.SEND_EMAIL__TO:
        setTo((String)newValue);
        return;
      case DslPackage.SEND_EMAIL__CC:
        setCc((String)newValue);
        return;
      case DslPackage.SEND_EMAIL__BCC:
        setBcc((String)newValue);
        return;
      case DslPackage.SEND_EMAIL__SUBJECT:
        setSubject((String)newValue);
        return;
      case DslPackage.SEND_EMAIL__BODY:
        setBody((String)newValue);
        return;
      case DslPackage.SEND_EMAIL__SIGNATURE:
        setSignature((String)newValue);
        return;
      case DslPackage.SEND_EMAIL__VALUE:
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
      case DslPackage.SEND_EMAIL__SOURCE:
        setSource(SOURCE_EDEFAULT);
        return;
      case DslPackage.SEND_EMAIL__SMTPHOST:
        setSmtphost(SMTPHOST_EDEFAULT);
        return;
      case DslPackage.SEND_EMAIL__SMTPPORT:
        setSmtpport(SMTPPORT_EDEFAULT);
        return;
      case DslPackage.SEND_EMAIL__FROM:
        setFrom(FROM_EDEFAULT);
        return;
      case DslPackage.SEND_EMAIL__PASS:
        setPass(PASS_EDEFAULT);
        return;
      case DslPackage.SEND_EMAIL__TO:
        setTo(TO_EDEFAULT);
        return;
      case DslPackage.SEND_EMAIL__CC:
        setCc(CC_EDEFAULT);
        return;
      case DslPackage.SEND_EMAIL__BCC:
        setBcc(BCC_EDEFAULT);
        return;
      case DslPackage.SEND_EMAIL__SUBJECT:
        setSubject(SUBJECT_EDEFAULT);
        return;
      case DslPackage.SEND_EMAIL__BODY:
        setBody(BODY_EDEFAULT);
        return;
      case DslPackage.SEND_EMAIL__SIGNATURE:
        setSignature(SIGNATURE_EDEFAULT);
        return;
      case DslPackage.SEND_EMAIL__VALUE:
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
      case DslPackage.SEND_EMAIL__SOURCE:
        return SOURCE_EDEFAULT == null ? source != null : !SOURCE_EDEFAULT.equals(source);
      case DslPackage.SEND_EMAIL__SMTPHOST:
        return SMTPHOST_EDEFAULT == null ? smtphost != null : !SMTPHOST_EDEFAULT.equals(smtphost);
      case DslPackage.SEND_EMAIL__SMTPPORT:
        return SMTPPORT_EDEFAULT == null ? smtpport != null : !SMTPPORT_EDEFAULT.equals(smtpport);
      case DslPackage.SEND_EMAIL__FROM:
        return FROM_EDEFAULT == null ? from != null : !FROM_EDEFAULT.equals(from);
      case DslPackage.SEND_EMAIL__PASS:
        return PASS_EDEFAULT == null ? pass != null : !PASS_EDEFAULT.equals(pass);
      case DslPackage.SEND_EMAIL__TO:
        return TO_EDEFAULT == null ? to != null : !TO_EDEFAULT.equals(to);
      case DslPackage.SEND_EMAIL__CC:
        return CC_EDEFAULT == null ? cc != null : !CC_EDEFAULT.equals(cc);
      case DslPackage.SEND_EMAIL__BCC:
        return BCC_EDEFAULT == null ? bcc != null : !BCC_EDEFAULT.equals(bcc);
      case DslPackage.SEND_EMAIL__SUBJECT:
        return SUBJECT_EDEFAULT == null ? subject != null : !SUBJECT_EDEFAULT.equals(subject);
      case DslPackage.SEND_EMAIL__BODY:
        return BODY_EDEFAULT == null ? body != null : !BODY_EDEFAULT.equals(body);
      case DslPackage.SEND_EMAIL__SIGNATURE:
        return SIGNATURE_EDEFAULT == null ? signature != null : !SIGNATURE_EDEFAULT.equals(signature);
      case DslPackage.SEND_EMAIL__VALUE:
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
    result.append(", smtphost: ");
    result.append(smtphost);
    result.append(", smtpport: ");
    result.append(smtpport);
    result.append(", from: ");
    result.append(from);
    result.append(", pass: ");
    result.append(pass);
    result.append(", to: ");
    result.append(to);
    result.append(", cc: ");
    result.append(cc);
    result.append(", bcc: ");
    result.append(bcc);
    result.append(", subject: ");
    result.append(subject);
    result.append(", body: ");
    result.append(body);
    result.append(", signature: ");
    result.append(signature);
    result.append(", value: ");
    result.append(value);
    result.append(')');
    return result.toString();
  }

} //SendEMailImpl
