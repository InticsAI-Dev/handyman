/**
 * generated by Xtext 2.25.0
 */
package in.handyman.dsl;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Copydata</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link in.handyman.dsl.Copydata#getSource <em>Source</em>}</li>
 *   <li>{@link in.handyman.dsl.Copydata#getTo <em>To</em>}</li>
 *   <li>{@link in.handyman.dsl.Copydata#getValue <em>Value</em>}</li>
 *   <li>{@link in.handyman.dsl.Copydata#getWriteThreadCount <em>Write Thread Count</em>}</li>
 *   <li>{@link in.handyman.dsl.Copydata#getFetchBatchSize <em>Fetch Batch Size</em>}</li>
 *   <li>{@link in.handyman.dsl.Copydata#getWriteBatchSize <em>Write Batch Size</em>}</li>
 * </ul>
 *
 * @model
 * @generated
 * @see in.handyman.dsl.DslPackage#getCopydata()
 */
public interface Copydata extends Action {
    /**
     * Returns the value of the '<em><b>Source</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Source</em>' attribute.
     * @model
     * @generated
     * @see #setSource(String)
     * @see in.handyman.dsl.DslPackage#getCopydata_Source()
     */
    String getSource();

    /**
     * Sets the value of the '{@link in.handyman.dsl.Copydata#getSource <em>Source</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @param value the new value of the '<em>Source</em>' attribute.
     * @generated
     * @see #getSource()
     */
    void setSource(String value);

    /**
     * Returns the value of the '<em><b>To</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>To</em>' attribute.
     * @model
     * @generated
     * @see #setTo(String)
     * @see in.handyman.dsl.DslPackage#getCopydata_To()
     */
    String getTo();

    /**
     * Sets the value of the '{@link in.handyman.dsl.Copydata#getTo <em>To</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @param value the new value of the '<em>To</em>' attribute.
     * @generated
     * @see #getTo()
     */
    void setTo(String value);

    /**
     * Returns the value of the '<em><b>Value</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Value</em>' attribute.
     * @model
     * @generated
     * @see #setValue(String)
     * @see in.handyman.dsl.DslPackage#getCopydata_Value()
     */
    String getValue();

    /**
     * Sets the value of the '{@link in.handyman.dsl.Copydata#getValue <em>Value</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @param value the new value of the '<em>Value</em>' attribute.
     * @generated
     * @see #getValue()
     */
    void setValue(String value);

    /**
     * Returns the value of the '<em><b>Write Thread Count</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Write Thread Count</em>' attribute.
     * @model
     * @generated
     * @see #setWriteThreadCount(String)
     * @see in.handyman.dsl.DslPackage#getCopydata_WriteThreadCount()
     */
    String getWriteThreadCount();

    /**
     * Sets the value of the '{@link in.handyman.dsl.Copydata#getWriteThreadCount <em>Write Thread Count</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @param value the new value of the '<em>Write Thread Count</em>' attribute.
     * @generated
     * @see #getWriteThreadCount()
     */
    void setWriteThreadCount(String value);

    /**
     * Returns the value of the '<em><b>Fetch Batch Size</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Fetch Batch Size</em>' attribute.
     * @model
     * @generated
     * @see #setFetchBatchSize(String)
     * @see in.handyman.dsl.DslPackage#getCopydata_FetchBatchSize()
     */
    String getFetchBatchSize();

    /**
     * Sets the value of the '{@link in.handyman.dsl.Copydata#getFetchBatchSize <em>Fetch Batch Size</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @param value the new value of the '<em>Fetch Batch Size</em>' attribute.
     * @generated
     * @see #getFetchBatchSize()
     */
    void setFetchBatchSize(String value);

    /**
     * Returns the value of the '<em><b>Write Batch Size</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @return the value of the '<em>Write Batch Size</em>' attribute.
     * @model
     * @generated
     * @see #setWriteBatchSize(String)
     * @see in.handyman.dsl.DslPackage#getCopydata_WriteBatchSize()
     */
    String getWriteBatchSize();

    /**
     * Sets the value of the '{@link in.handyman.dsl.Copydata#getWriteBatchSize <em>Write Batch Size</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     *
     * @param value the new value of the '<em>Write Batch Size</em>' attribute.
     * @generated
     * @see #getWriteBatchSize()
     */
    void setWriteBatchSize(String value);

} // Copydata
