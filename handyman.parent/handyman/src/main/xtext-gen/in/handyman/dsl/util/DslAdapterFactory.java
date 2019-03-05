/**
 * generated by Xtext 2.16.0
 */
package in.handyman.dsl.util;

import in.handyman.dsl.Abort;
import in.handyman.dsl.Action;
import in.handyman.dsl.Callprocess;
import in.handyman.dsl.Catch;
import in.handyman.dsl.ClickSendSms;
import in.handyman.dsl.Copydata;
import in.handyman.dsl.Doozle;
import in.handyman.dsl.Dropfile;
import in.handyman.dsl.DslPackage;
import in.handyman.dsl.Expression;
import in.handyman.dsl.FBCLead;
import in.handyman.dsl.FBFormDownload;
import in.handyman.dsl.Fetch;
import in.handyman.dsl.Finally;
import in.handyman.dsl.FirebaseDatabasePut;
import in.handyman.dsl.FirebaseReactiveNotification;
import in.handyman.dsl.GooglecalPUT;
import in.handyman.dsl.GooglecontactPUT;
import in.handyman.dsl.GooglecontactSelectAll;
import in.handyman.dsl.LoadCsv;
import in.handyman.dsl.Rest;
import in.handyman.dsl.RestPart;
import in.handyman.dsl.SendMail;
import in.handyman.dsl.SlackPUT;
import in.handyman.dsl.SmsLeadSms;
import in.handyman.dsl.Transform;
import in.handyman.dsl.TrelloGET;
import in.handyman.dsl.TrelloPUT;
import in.handyman.dsl.Try;
import in.handyman.dsl.Updatedaudit;
import in.handyman.dsl.WriteCsv;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see in.handyman.dsl.DslPackage
 * @generated
 */
public class DslAdapterFactory extends AdapterFactoryImpl
{
  /**
   * The cached model package.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected static DslPackage modelPackage;

  /**
   * Creates an instance of the adapter factory.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  public DslAdapterFactory()
  {
    if (modelPackage == null)
    {
      modelPackage = DslPackage.eINSTANCE;
    }
  }

  /**
   * Returns whether this factory is applicable for the type of the object.
   * <!-- begin-user-doc -->
   * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
   * <!-- end-user-doc -->
   * @return whether this factory is applicable for the type of the object.
   * @generated
   */
  @Override
  public boolean isFactoryForType(Object object)
  {
    if (object == modelPackage)
    {
      return true;
    }
    if (object instanceof EObject)
    {
      return ((EObject)object).eClass().getEPackage() == modelPackage;
    }
    return false;
  }

  /**
   * The switch that delegates to the <code>createXXX</code> methods.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @generated
   */
  protected DslSwitch<Adapter> modelSwitch =
    new DslSwitch<Adapter>()
    {
      @Override
      public Adapter caseProcess(in.handyman.dsl.Process object)
      {
        return createProcessAdapter();
      }
      @Override
      public Adapter caseTry(Try object)
      {
        return createTryAdapter();
      }
      @Override
      public Adapter caseFinally(Finally object)
      {
        return createFinallyAdapter();
      }
      @Override
      public Adapter caseCatch(Catch object)
      {
        return createCatchAdapter();
      }
      @Override
      public Adapter caseAction(Action object)
      {
        return createActionAdapter();
      }
      @Override
      public Adapter caseFirebaseDatabasePut(FirebaseDatabasePut object)
      {
        return createFirebaseDatabasePutAdapter();
      }
      @Override
      public Adapter caseFirebaseReactiveNotification(FirebaseReactiveNotification object)
      {
        return createFirebaseReactiveNotificationAdapter();
      }
      @Override
      public Adapter caseSmsLeadSms(SmsLeadSms object)
      {
        return createSmsLeadSmsAdapter();
      }
      @Override
      public Adapter caseAbort(Abort object)
      {
        return createAbortAdapter();
      }
      @Override
      public Adapter caseGooglecontactSelectAll(GooglecontactSelectAll object)
      {
        return createGooglecontactSelectAllAdapter();
      }
      @Override
      public Adapter caseSendMail(SendMail object)
      {
        return createSendMailAdapter();
      }
      @Override
      public Adapter caseGooglecontactPUT(GooglecontactPUT object)
      {
        return createGooglecontactPUTAdapter();
      }
      @Override
      public Adapter caseGooglecalPUT(GooglecalPUT object)
      {
        return createGooglecalPUTAdapter();
      }
      @Override
      public Adapter caseFBCLead(FBCLead object)
      {
        return createFBCLeadAdapter();
      }
      @Override
      public Adapter caseFBFormDownload(FBFormDownload object)
      {
        return createFBFormDownloadAdapter();
      }
      @Override
      public Adapter caseDropfile(Dropfile object)
      {
        return createDropfileAdapter();
      }
      @Override
      public Adapter caseDoozle(Doozle object)
      {
        return createDoozleAdapter();
      }
      @Override
      public Adapter caseRest(Rest object)
      {
        return createRestAdapter();
      }
      @Override
      public Adapter caseRestPart(RestPart object)
      {
        return createRestPartAdapter();
      }
      @Override
      public Adapter caseTrelloGET(TrelloGET object)
      {
        return createTrelloGETAdapter();
      }
      @Override
      public Adapter caseTrelloPUT(TrelloPUT object)
      {
        return createTrelloPUTAdapter();
      }
      @Override
      public Adapter caseFetch(Fetch object)
      {
        return createFetchAdapter();
      }
      @Override
      public Adapter caseCallprocess(Callprocess object)
      {
        return createCallprocessAdapter();
      }
      @Override
      public Adapter caseUpdatedaudit(Updatedaudit object)
      {
        return createUpdatedauditAdapter();
      }
      @Override
      public Adapter caseClickSendSms(ClickSendSms object)
      {
        return createClickSendSmsAdapter();
      }
      @Override
      public Adapter caseSlackPUT(SlackPUT object)
      {
        return createSlackPUTAdapter();
      }
      @Override
      public Adapter caseCopydata(Copydata object)
      {
        return createCopydataAdapter();
      }
      @Override
      public Adapter caseWriteCsv(WriteCsv object)
      {
        return createWriteCsvAdapter();
      }
      @Override
      public Adapter caseLoadCsv(LoadCsv object)
      {
        return createLoadCsvAdapter();
      }
      @Override
      public Adapter caseTransform(Transform object)
      {
        return createTransformAdapter();
      }
      @Override
      public Adapter caseExpression(Expression object)
      {
        return createExpressionAdapter();
      }
      @Override
      public Adapter defaultCase(EObject object)
      {
        return createEObjectAdapter();
      }
    };

  /**
   * Creates an adapter for the <code>target</code>.
   * <!-- begin-user-doc -->
   * <!-- end-user-doc -->
   * @param target the object to adapt.
   * @return the adapter for the <code>target</code>.
   * @generated
   */
  @Override
  public Adapter createAdapter(Notifier target)
  {
    return modelSwitch.doSwitch((EObject)target);
  }


  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.Process <em>Process</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.Process
   * @generated
   */
  public Adapter createProcessAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.Try <em>Try</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.Try
   * @generated
   */
  public Adapter createTryAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.Finally <em>Finally</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.Finally
   * @generated
   */
  public Adapter createFinallyAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.Catch <em>Catch</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.Catch
   * @generated
   */
  public Adapter createCatchAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.Action <em>Action</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.Action
   * @generated
   */
  public Adapter createActionAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.FirebaseDatabasePut <em>Firebase Database Put</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.FirebaseDatabasePut
   * @generated
   */
  public Adapter createFirebaseDatabasePutAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.FirebaseReactiveNotification <em>Firebase Reactive Notification</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.FirebaseReactiveNotification
   * @generated
   */
  public Adapter createFirebaseReactiveNotificationAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.SmsLeadSms <em>Sms Lead Sms</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.SmsLeadSms
   * @generated
   */
  public Adapter createSmsLeadSmsAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.Abort <em>Abort</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.Abort
   * @generated
   */
  public Adapter createAbortAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.GooglecontactSelectAll <em>Googlecontact Select All</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.GooglecontactSelectAll
   * @generated
   */
  public Adapter createGooglecontactSelectAllAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.SendMail <em>Send Mail</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.SendMail
   * @generated
   */
  public Adapter createSendMailAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.GooglecontactPUT <em>Googlecontact PUT</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.GooglecontactPUT
   * @generated
   */
  public Adapter createGooglecontactPUTAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.GooglecalPUT <em>Googlecal PUT</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.GooglecalPUT
   * @generated
   */
  public Adapter createGooglecalPUTAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.FBCLead <em>FBC Lead</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.FBCLead
   * @generated
   */
  public Adapter createFBCLeadAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.FBFormDownload <em>FB Form Download</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.FBFormDownload
   * @generated
   */
  public Adapter createFBFormDownloadAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.Dropfile <em>Dropfile</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.Dropfile
   * @generated
   */
  public Adapter createDropfileAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.Doozle <em>Doozle</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.Doozle
   * @generated
   */
  public Adapter createDoozleAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.Rest <em>Rest</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.Rest
   * @generated
   */
  public Adapter createRestAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.RestPart <em>Rest Part</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.RestPart
   * @generated
   */
  public Adapter createRestPartAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.TrelloGET <em>Trello GET</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.TrelloGET
   * @generated
   */
  public Adapter createTrelloGETAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.TrelloPUT <em>Trello PUT</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.TrelloPUT
   * @generated
   */
  public Adapter createTrelloPUTAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.Fetch <em>Fetch</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.Fetch
   * @generated
   */
  public Adapter createFetchAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.Callprocess <em>Callprocess</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.Callprocess
   * @generated
   */
  public Adapter createCallprocessAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.Updatedaudit <em>Updatedaudit</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.Updatedaudit
   * @generated
   */
  public Adapter createUpdatedauditAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.ClickSendSms <em>Click Send Sms</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.ClickSendSms
   * @generated
   */
  public Adapter createClickSendSmsAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.SlackPUT <em>Slack PUT</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.SlackPUT
   * @generated
   */
  public Adapter createSlackPUTAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.Copydata <em>Copydata</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.Copydata
   * @generated
   */
  public Adapter createCopydataAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.WriteCsv <em>Write Csv</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.WriteCsv
   * @generated
   */
  public Adapter createWriteCsvAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.LoadCsv <em>Load Csv</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.LoadCsv
   * @generated
   */
  public Adapter createLoadCsvAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.Transform <em>Transform</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.Transform
   * @generated
   */
  public Adapter createTransformAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for an object of class '{@link in.handyman.dsl.Expression <em>Expression</em>}'.
   * <!-- begin-user-doc -->
   * This default implementation returns null so that we can easily ignore cases;
   * it's useful to ignore a case when inheritance will catch all the cases anyway.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @see in.handyman.dsl.Expression
   * @generated
   */
  public Adapter createExpressionAdapter()
  {
    return null;
  }

  /**
   * Creates a new adapter for the default case.
   * <!-- begin-user-doc -->
   * This default implementation returns null.
   * <!-- end-user-doc -->
   * @return the new adapter.
   * @generated
   */
  public Adapter createEObjectAdapter()
  {
    return null;
  }

} //DslAdapterFactory
