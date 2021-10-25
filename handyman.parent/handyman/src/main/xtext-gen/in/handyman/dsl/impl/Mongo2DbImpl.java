/**
 * generated by Xtext 2.16.0
 */
package in.handyman.dsl.impl;

import in.handyman.dsl.DslPackage;
import in.handyman.dsl.Mongo2Db;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Mongo2 Db</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link in.handyman.dsl.impl.Mongo2DbImpl#getSourceConnStr <em>Source Conn Str</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.Mongo2DbImpl#getTo <em>To</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.Mongo2DbImpl#getSourceDb <em>Source Db</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.Mongo2DbImpl#getTargetDb <em>Target Db</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.Mongo2DbImpl#getTargetTable <em>Target Table</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.Mongo2DbImpl#getFilter <em>Filter</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.Mongo2DbImpl#getLimit <em>Limit</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.Mongo2DbImpl#getFindAttr <em>Find Attr</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.Mongo2DbImpl#getApplyManipulation <em>Apply Manipulation</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.Mongo2DbImpl#getOnUpdateKey <em>On Update Key</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.Mongo2DbImpl#getFetchBatchSize <em>Fetch Batch Size</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.Mongo2DbImpl#getWriteBatchSize <em>Write Batch Size</em>}</li>
 *   <li>{@link in.handyman.dsl.impl.Mongo2DbImpl#getValue <em>Value</em>}</li>
 * </ul>
 *
 * @generated
 */
public class Mongo2DbImpl extends ActionImpl implements Mongo2Db
{
  /**
   * The default value of the '{@link #getSourceConnStr() <em>Source Conn Str</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSourceConnStr()
   * @generated
   * @ordered
   */
  protected static final String SOURCE_CONN_STR_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getSourceConnStr() <em>Source Conn Str</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSourceConnStr()
   * @generated
   * @ordered
   */
  protected String sourceConnStr = SOURCE_CONN_STR_EDEFAULT;

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
   * The default value of the '{@link #getSourceDb() <em>Source Db</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSourceDb()
   * @generated
   * @ordered
   */
  protected static final String SOURCE_DB_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getSourceDb() <em>Source Db</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getSourceDb()
   * @generated
   * @ordered
   */
  protected String sourceDb = SOURCE_DB_EDEFAULT;

  /**
   * The default value of the '{@link #getTargetDb() <em>Target Db</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getTargetDb()
   * @generated
   * @ordered
   */
  protected static final String TARGET_DB_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getTargetDb() <em>Target Db</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getTargetDb()
   * @generated
   * @ordered
   */
  protected String targetDb = TARGET_DB_EDEFAULT;

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
   * The default value of the '{@link #getFilter() <em>Filter</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getFilter()
   * @generated
   * @ordered
   */
  protected static final String FILTER_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getFilter() <em>Filter</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getFilter()
   * @generated
   * @ordered
   */
  protected String filter = FILTER_EDEFAULT;

  /**
   * The default value of the '{@link #getLimit() <em>Limit</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getLimit()
   * @generated
   * @ordered
   */
  protected static final String LIMIT_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getLimit() <em>Limit</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getLimit()
   * @generated
   * @ordered
   */
  protected String limit = LIMIT_EDEFAULT;

  /**
   * The default value of the '{@link #getFindAttr() <em>Find Attr</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getFindAttr()
   * @generated
   * @ordered
   */
  protected static final String FIND_ATTR_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getFindAttr() <em>Find Attr</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getFindAttr()
   * @generated
   * @ordered
   */
  protected String findAttr = FIND_ATTR_EDEFAULT;

  /**
   * The default value of the '{@link #getApplyManipulation() <em>Apply Manipulation</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getApplyManipulation()
   * @generated
   * @ordered
   */
  protected static final String APPLY_MANIPULATION_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getApplyManipulation() <em>Apply Manipulation</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getApplyManipulation()
   * @generated
   * @ordered
   */
  protected String applyManipulation = APPLY_MANIPULATION_EDEFAULT;

  /**
   * The default value of the '{@link #getOnUpdateKey() <em>On Update Key</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getOnUpdateKey()
   * @generated
   * @ordered
   */
  protected static final String ON_UPDATE_KEY_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getOnUpdateKey() <em>On Update Key</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getOnUpdateKey()
   * @generated
   * @ordered
   */
  protected String onUpdateKey = ON_UPDATE_KEY_EDEFAULT;

  /**
   * The default value of the '{@link #getFetchBatchSize() <em>Fetch Batch Size</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getFetchBatchSize()
   * @generated
   * @ordered
   */
  protected static final String FETCH_BATCH_SIZE_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getFetchBatchSize() <em>Fetch Batch Size</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getFetchBatchSize()
   * @generated
   * @ordered
   */
  protected String fetchBatchSize = FETCH_BATCH_SIZE_EDEFAULT;

  /**
   * The default value of the '{@link #getWriteBatchSize() <em>Write Batch Size</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getWriteBatchSize()
   * @generated
   * @ordered
   */
  protected static final String WRITE_BATCH_SIZE_EDEFAULT = null;

  /**
   * The cached value of the '{@link #getWriteBatchSize() <em>Write Batch Size</em>}' attribute.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @see #getWriteBatchSize()
   * @generated
   * @ordered
   */
  protected String writeBatchSize = WRITE_BATCH_SIZE_EDEFAULT;

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
  protected Mongo2DbImpl()
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
    return DslPackage.Literals.MONGO2_DB;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getSourceConnStr()
  {
    return sourceConnStr;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setSourceConnStr(String newSourceConnStr)
  {
    String oldSourceConnStr = sourceConnStr;
    sourceConnStr = newSourceConnStr;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.MONGO2_DB__SOURCE_CONN_STR, oldSourceConnStr, sourceConnStr));
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
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.MONGO2_DB__TO, oldTo, to));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getSourceDb()
  {
    return sourceDb;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setSourceDb(String newSourceDb)
  {
    String oldSourceDb = sourceDb;
    sourceDb = newSourceDb;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.MONGO2_DB__SOURCE_DB, oldSourceDb, sourceDb));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getTargetDb()
  {
    return targetDb;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setTargetDb(String newTargetDb)
  {
    String oldTargetDb = targetDb;
    targetDb = newTargetDb;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.MONGO2_DB__TARGET_DB, oldTargetDb, targetDb));
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
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.MONGO2_DB__TARGET_TABLE, oldTargetTable, targetTable));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getFilter()
  {
    return filter;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setFilter(String newFilter)
  {
    String oldFilter = filter;
    filter = newFilter;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.MONGO2_DB__FILTER, oldFilter, filter));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getLimit()
  {
    return limit;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setLimit(String newLimit)
  {
    String oldLimit = limit;
    limit = newLimit;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.MONGO2_DB__LIMIT, oldLimit, limit));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getFindAttr()
  {
    return findAttr;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setFindAttr(String newFindAttr)
  {
    String oldFindAttr = findAttr;
    findAttr = newFindAttr;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.MONGO2_DB__FIND_ATTR, oldFindAttr, findAttr));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getApplyManipulation()
  {
    return applyManipulation;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setApplyManipulation(String newApplyManipulation)
  {
    String oldApplyManipulation = applyManipulation;
    applyManipulation = newApplyManipulation;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.MONGO2_DB__APPLY_MANIPULATION, oldApplyManipulation, applyManipulation));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getOnUpdateKey()
  {
    return onUpdateKey;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setOnUpdateKey(String newOnUpdateKey)
  {
    String oldOnUpdateKey = onUpdateKey;
    onUpdateKey = newOnUpdateKey;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.MONGO2_DB__ON_UPDATE_KEY, oldOnUpdateKey, onUpdateKey));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getFetchBatchSize()
  {
    return fetchBatchSize;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setFetchBatchSize(String newFetchBatchSize)
  {
    String oldFetchBatchSize = fetchBatchSize;
    fetchBatchSize = newFetchBatchSize;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.MONGO2_DB__FETCH_BATCH_SIZE, oldFetchBatchSize, fetchBatchSize));
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public String getWriteBatchSize()
  {
    return writeBatchSize;
  }

  /**
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public void setWriteBatchSize(String newWriteBatchSize)
  {
    String oldWriteBatchSize = writeBatchSize;
    writeBatchSize = newWriteBatchSize;
    if (eNotificationRequired())
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.MONGO2_DB__WRITE_BATCH_SIZE, oldWriteBatchSize, writeBatchSize));
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
      eNotify(new ENotificationImpl(this, Notification.SET, DslPackage.MONGO2_DB__VALUE, oldValue, value));
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
      case DslPackage.MONGO2_DB__SOURCE_CONN_STR:
        return getSourceConnStr();
      case DslPackage.MONGO2_DB__TO:
        return getTo();
      case DslPackage.MONGO2_DB__SOURCE_DB:
        return getSourceDb();
      case DslPackage.MONGO2_DB__TARGET_DB:
        return getTargetDb();
      case DslPackage.MONGO2_DB__TARGET_TABLE:
        return getTargetTable();
      case DslPackage.MONGO2_DB__FILTER:
        return getFilter();
      case DslPackage.MONGO2_DB__LIMIT:
        return getLimit();
      case DslPackage.MONGO2_DB__FIND_ATTR:
        return getFindAttr();
      case DslPackage.MONGO2_DB__APPLY_MANIPULATION:
        return getApplyManipulation();
      case DslPackage.MONGO2_DB__ON_UPDATE_KEY:
        return getOnUpdateKey();
      case DslPackage.MONGO2_DB__FETCH_BATCH_SIZE:
        return getFetchBatchSize();
      case DslPackage.MONGO2_DB__WRITE_BATCH_SIZE:
        return getWriteBatchSize();
      case DslPackage.MONGO2_DB__VALUE:
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
      case DslPackage.MONGO2_DB__SOURCE_CONN_STR:
        setSourceConnStr((String)newValue);
        return;
      case DslPackage.MONGO2_DB__TO:
        setTo((String)newValue);
        return;
      case DslPackage.MONGO2_DB__SOURCE_DB:
        setSourceDb((String)newValue);
        return;
      case DslPackage.MONGO2_DB__TARGET_DB:
        setTargetDb((String)newValue);
        return;
      case DslPackage.MONGO2_DB__TARGET_TABLE:
        setTargetTable((String)newValue);
        return;
      case DslPackage.MONGO2_DB__FILTER:
        setFilter((String)newValue);
        return;
      case DslPackage.MONGO2_DB__LIMIT:
        setLimit((String)newValue);
        return;
      case DslPackage.MONGO2_DB__FIND_ATTR:
        setFindAttr((String)newValue);
        return;
      case DslPackage.MONGO2_DB__APPLY_MANIPULATION:
        setApplyManipulation((String)newValue);
        return;
      case DslPackage.MONGO2_DB__ON_UPDATE_KEY:
        setOnUpdateKey((String)newValue);
        return;
      case DslPackage.MONGO2_DB__FETCH_BATCH_SIZE:
        setFetchBatchSize((String)newValue);
        return;
      case DslPackage.MONGO2_DB__WRITE_BATCH_SIZE:
        setWriteBatchSize((String)newValue);
        return;
      case DslPackage.MONGO2_DB__VALUE:
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
      case DslPackage.MONGO2_DB__SOURCE_CONN_STR:
        setSourceConnStr(SOURCE_CONN_STR_EDEFAULT);
        return;
      case DslPackage.MONGO2_DB__TO:
        setTo(TO_EDEFAULT);
        return;
      case DslPackage.MONGO2_DB__SOURCE_DB:
        setSourceDb(SOURCE_DB_EDEFAULT);
        return;
      case DslPackage.MONGO2_DB__TARGET_DB:
        setTargetDb(TARGET_DB_EDEFAULT);
        return;
      case DslPackage.MONGO2_DB__TARGET_TABLE:
        setTargetTable(TARGET_TABLE_EDEFAULT);
        return;
      case DslPackage.MONGO2_DB__FILTER:
        setFilter(FILTER_EDEFAULT);
        return;
      case DslPackage.MONGO2_DB__LIMIT:
        setLimit(LIMIT_EDEFAULT);
        return;
      case DslPackage.MONGO2_DB__FIND_ATTR:
        setFindAttr(FIND_ATTR_EDEFAULT);
        return;
      case DslPackage.MONGO2_DB__APPLY_MANIPULATION:
        setApplyManipulation(APPLY_MANIPULATION_EDEFAULT);
        return;
      case DslPackage.MONGO2_DB__ON_UPDATE_KEY:
        setOnUpdateKey(ON_UPDATE_KEY_EDEFAULT);
        return;
      case DslPackage.MONGO2_DB__FETCH_BATCH_SIZE:
        setFetchBatchSize(FETCH_BATCH_SIZE_EDEFAULT);
        return;
      case DslPackage.MONGO2_DB__WRITE_BATCH_SIZE:
        setWriteBatchSize(WRITE_BATCH_SIZE_EDEFAULT);
        return;
      case DslPackage.MONGO2_DB__VALUE:
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
      case DslPackage.MONGO2_DB__SOURCE_CONN_STR:
        return SOURCE_CONN_STR_EDEFAULT == null ? sourceConnStr != null : !SOURCE_CONN_STR_EDEFAULT.equals(sourceConnStr);
      case DslPackage.MONGO2_DB__TO:
        return TO_EDEFAULT == null ? to != null : !TO_EDEFAULT.equals(to);
      case DslPackage.MONGO2_DB__SOURCE_DB:
        return SOURCE_DB_EDEFAULT == null ? sourceDb != null : !SOURCE_DB_EDEFAULT.equals(sourceDb);
      case DslPackage.MONGO2_DB__TARGET_DB:
        return TARGET_DB_EDEFAULT == null ? targetDb != null : !TARGET_DB_EDEFAULT.equals(targetDb);
      case DslPackage.MONGO2_DB__TARGET_TABLE:
        return TARGET_TABLE_EDEFAULT == null ? targetTable != null : !TARGET_TABLE_EDEFAULT.equals(targetTable);
      case DslPackage.MONGO2_DB__FILTER:
        return FILTER_EDEFAULT == null ? filter != null : !FILTER_EDEFAULT.equals(filter);
      case DslPackage.MONGO2_DB__LIMIT:
        return LIMIT_EDEFAULT == null ? limit != null : !LIMIT_EDEFAULT.equals(limit);
      case DslPackage.MONGO2_DB__FIND_ATTR:
        return FIND_ATTR_EDEFAULT == null ? findAttr != null : !FIND_ATTR_EDEFAULT.equals(findAttr);
      case DslPackage.MONGO2_DB__APPLY_MANIPULATION:
        return APPLY_MANIPULATION_EDEFAULT == null ? applyManipulation != null : !APPLY_MANIPULATION_EDEFAULT.equals(applyManipulation);
      case DslPackage.MONGO2_DB__ON_UPDATE_KEY:
        return ON_UPDATE_KEY_EDEFAULT == null ? onUpdateKey != null : !ON_UPDATE_KEY_EDEFAULT.equals(onUpdateKey);
      case DslPackage.MONGO2_DB__FETCH_BATCH_SIZE:
        return FETCH_BATCH_SIZE_EDEFAULT == null ? fetchBatchSize != null : !FETCH_BATCH_SIZE_EDEFAULT.equals(fetchBatchSize);
      case DslPackage.MONGO2_DB__WRITE_BATCH_SIZE:
        return WRITE_BATCH_SIZE_EDEFAULT == null ? writeBatchSize != null : !WRITE_BATCH_SIZE_EDEFAULT.equals(writeBatchSize);
      case DslPackage.MONGO2_DB__VALUE:
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
    result.append(" (sourceConnStr: ");
    result.append(sourceConnStr);
    result.append(", to: ");
    result.append(to);
    result.append(", sourceDb: ");
    result.append(sourceDb);
    result.append(", targetDb: ");
    result.append(targetDb);
    result.append(", targetTable: ");
    result.append(targetTable);
    result.append(", filter: ");
    result.append(filter);
    result.append(", limit: ");
    result.append(limit);
    result.append(", findAttr: ");
    result.append(findAttr);
    result.append(", applyManipulation: ");
    result.append(applyManipulation);
    result.append(", onUpdateKey: ");
    result.append(onUpdateKey);
    result.append(", fetchBatchSize: ");
    result.append(fetchBatchSize);
    result.append(", writeBatchSize: ");
    result.append(writeBatchSize);
    result.append(", value: ");
    result.append(value);
    result.append(')');
    return result.toString();
  }

} //Mongo2DbImpl