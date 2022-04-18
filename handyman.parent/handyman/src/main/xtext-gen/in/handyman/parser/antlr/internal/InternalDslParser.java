package in.handyman.parser.antlr.internal;

import org.eclipse.xtext.*;
import org.eclipse.xtext.parser.*;
import org.eclipse.xtext.parser.impl.*;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.parser.antlr.AbstractInternalAntlrParser;
import org.eclipse.xtext.parser.antlr.XtextTokenStream;
import org.eclipse.xtext.parser.antlr.XtextTokenStream.HiddenTokens;
import org.eclipse.xtext.parser.antlr.AntlrDatatypeRuleToken;
import in.handyman.services.DslGrammarAccess;



import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class InternalDslParser extends AbstractInternalAntlrParser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "RULE_STRING", "RULE_ID", "RULE_INT", "RULE_ML_COMMENT", "RULE_SL_COMMENT", "RULE_WS", "RULE_ANY_OTHER", "'process'", "'{'", "'}'", "'try'", "'finally'", "'catch'", "'sendemail'", "'as'", "'db'", "'smtp-host'", "'smtp-port'", "'from'", "'pass'", "'to'", "'cc'", "'bcc'", "'subject'", "'body'", "'signature'", "'using'", "'on-condition'", "'deletefolder'", "'folder'", "'zipfile'", "'terminal'", "'id'", "'java'", "'name-sake-db'", "'abort'", "'dropfile'", "'in-path'", "'doozle'", "'in-table'", "'on'", "'assign'", "'source'", "'callprocess'", "'with-target'", "'from-file'", "'for-every'", "'forkprocess'", "'watermark'", "'copydata'", "'target'", "'by-batch'", "'writecsv'", "'with'", "'loadcsv'", "'pid'", "'transform'", "'deletesql'", "'updatesql'", "'insertsql'", "'truncatesql'", "'dropsql'", "'listfiles'", "'target-table'", "'mongo2db'", "'srcDb'", "'targetDb'", "'targetTable'", "'by-filter'", "'find-attribute'", "'apply-manipulation'", "'on-updatekey'", "'with-fetch-batch-size'", "'with-write-batch-size'", "'ftp'", "'host'", "'port'", "'username'", "'password'", "'action'", "'local-dir'", "'local-file'", "'remote-dir'", "'remote-file'", "'zip'", "'zip-file-path'", "'zip-file-name'", "'buffer-size'", "'unzip'", "'dest-dir'", "'checksum'", "'jsontransform'", "'jsondeserialize'", "'input'", "'restapi'", "'url'", "'method'", "'property'", "'payload'", "'python'", "'rest'", "'secured-by'", "'with-url'", "'and-method-as'", "'update-url-with'", "'update-header-with'", "'update-body-with'", "'parent'", "'into'", "'store-ack-at'", "'part'", "'if'", "'<'", "'>'", "'=='", "'contains'"
    };
    public static final int T__50=50;
    public static final int T__59=59;
    public static final int T__55=55;
    public static final int T__56=56;
    public static final int T__57=57;
    public static final int T__58=58;
    public static final int T__51=51;
    public static final int T__52=52;
    public static final int T__53=53;
    public static final int T__54=54;
    public static final int T__60=60;
    public static final int T__61=61;
    public static final int RULE_ID=5;
    public static final int RULE_INT=6;
    public static final int T__66=66;
    public static final int RULE_ML_COMMENT=7;
    public static final int T__67=67;
    public static final int T__68=68;
    public static final int T__69=69;
    public static final int T__62=62;
    public static final int T__63=63;
    public static final int T__64=64;
    public static final int T__65=65;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int T__33=33;
    public static final int T__34=34;
    public static final int T__35=35;
    public static final int T__36=36;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int T__48=48;
    public static final int T__49=49;
    public static final int T__44=44;
    public static final int T__45=45;
    public static final int T__46=46;
    public static final int T__47=47;
    public static final int T__40=40;
    public static final int T__41=41;
    public static final int T__42=42;
    public static final int T__43=43;
    public static final int T__91=91;
    public static final int T__100=100;
    public static final int T__92=92;
    public static final int T__93=93;
    public static final int T__102=102;
    public static final int T__94=94;
    public static final int T__101=101;
    public static final int T__90=90;
    public static final int T__19=19;
    public static final int T__15=15;
    public static final int T__16=16;
    public static final int T__17=17;
    public static final int T__18=18;
    public static final int T__11=11;
    public static final int T__99=99;
    public static final int T__12=12;
    public static final int T__13=13;
    public static final int T__14=14;
    public static final int T__95=95;
    public static final int T__96=96;
    public static final int T__97=97;
    public static final int T__98=98;
    public static final int T__26=26;
    public static final int T__27=27;
    public static final int T__28=28;
    public static final int T__29=29;
    public static final int T__22=22;
    public static final int T__23=23;
    public static final int T__24=24;
    public static final int T__25=25;
    public static final int T__20=20;
    public static final int T__21=21;
    public static final int T__70=70;
    public static final int T__71=71;
    public static final int T__72=72;
    public static final int RULE_STRING=4;
    public static final int RULE_SL_COMMENT=8;
    public static final int T__77=77;
    public static final int T__119=119;
    public static final int T__78=78;
    public static final int T__118=118;
    public static final int T__79=79;
    public static final int T__73=73;
    public static final int T__115=115;
    public static final int EOF=-1;
    public static final int T__74=74;
    public static final int T__114=114;
    public static final int T__75=75;
    public static final int T__117=117;
    public static final int T__76=76;
    public static final int T__116=116;
    public static final int T__80=80;
    public static final int T__111=111;
    public static final int T__81=81;
    public static final int T__110=110;
    public static final int T__82=82;
    public static final int T__113=113;
    public static final int T__83=83;
    public static final int T__112=112;
    public static final int RULE_WS=9;
    public static final int RULE_ANY_OTHER=10;
    public static final int T__88=88;
    public static final int T__108=108;
    public static final int T__89=89;
    public static final int T__107=107;
    public static final int T__109=109;
    public static final int T__84=84;
    public static final int T__104=104;
    public static final int T__85=85;
    public static final int T__103=103;
    public static final int T__86=86;
    public static final int T__106=106;
    public static final int T__87=87;
    public static final int T__105=105;

    // delegates
    // delegators


        public InternalDslParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public InternalDslParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return InternalDslParser.tokenNames; }
    public String getGrammarFileName() { return "InternalDsl.g"; }



     	private DslGrammarAccess grammarAccess;

        public InternalDslParser(TokenStream input, DslGrammarAccess grammarAccess) {
            this(input);
            this.grammarAccess = grammarAccess;
            registerRules(grammarAccess.getGrammar());
        }

        @Override
        protected String getFirstRuleName() {
        	return "Process";
       	}

       	@Override
       	protected DslGrammarAccess getGrammarAccess() {
       		return grammarAccess;
       	}




    // $ANTLR start "entryRuleProcess"
    // InternalDsl.g:64:1: entryRuleProcess returns [EObject current=null] : iv_ruleProcess= ruleProcess EOF ;
    public final EObject entryRuleProcess() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleProcess = null;


        try {
            // InternalDsl.g:64:48: (iv_ruleProcess= ruleProcess EOF )
            // InternalDsl.g:65:2: iv_ruleProcess= ruleProcess EOF
            {
             newCompositeNode(grammarAccess.getProcessRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleProcess=ruleProcess();

            state._fsp--;

             current =iv_ruleProcess; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleProcess"


    // $ANTLR start "ruleProcess"
    // InternalDsl.g:71:1: ruleProcess returns [EObject current=null] : (otherlv_0= 'process' ( (lv_name_1_0= RULE_STRING ) ) otherlv_2= '{' ( (lv_try_3_0= ruleTry ) ) ( (lv_catch_4_0= ruleCatch ) ) ( (lv_finally_5_0= ruleFinally ) ) otherlv_6= '}' ) ;
    public final EObject ruleProcess() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_name_1_0=null;
        Token otherlv_2=null;
        Token otherlv_6=null;
        EObject lv_try_3_0 = null;

        EObject lv_catch_4_0 = null;

        EObject lv_finally_5_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:77:2: ( (otherlv_0= 'process' ( (lv_name_1_0= RULE_STRING ) ) otherlv_2= '{' ( (lv_try_3_0= ruleTry ) ) ( (lv_catch_4_0= ruleCatch ) ) ( (lv_finally_5_0= ruleFinally ) ) otherlv_6= '}' ) )
            // InternalDsl.g:78:2: (otherlv_0= 'process' ( (lv_name_1_0= RULE_STRING ) ) otherlv_2= '{' ( (lv_try_3_0= ruleTry ) ) ( (lv_catch_4_0= ruleCatch ) ) ( (lv_finally_5_0= ruleFinally ) ) otherlv_6= '}' )
            {
            // InternalDsl.g:78:2: (otherlv_0= 'process' ( (lv_name_1_0= RULE_STRING ) ) otherlv_2= '{' ( (lv_try_3_0= ruleTry ) ) ( (lv_catch_4_0= ruleCatch ) ) ( (lv_finally_5_0= ruleFinally ) ) otherlv_6= '}' )
            // InternalDsl.g:79:3: otherlv_0= 'process' ( (lv_name_1_0= RULE_STRING ) ) otherlv_2= '{' ( (lv_try_3_0= ruleTry ) ) ( (lv_catch_4_0= ruleCatch ) ) ( (lv_finally_5_0= ruleFinally ) ) otherlv_6= '}'
            {
            otherlv_0=(Token)match(input,11,FOLLOW_3); 

            			newLeafNode(otherlv_0, grammarAccess.getProcessAccess().getProcessKeyword_0());
            		
            // InternalDsl.g:83:3: ( (lv_name_1_0= RULE_STRING ) )
            // InternalDsl.g:84:4: (lv_name_1_0= RULE_STRING )
            {
            // InternalDsl.g:84:4: (lv_name_1_0= RULE_STRING )
            // InternalDsl.g:85:5: lv_name_1_0= RULE_STRING
            {
            lv_name_1_0=(Token)match(input,RULE_STRING,FOLLOW_4); 

            					newLeafNode(lv_name_1_0, grammarAccess.getProcessAccess().getNameSTRINGTerminalRuleCall_1_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getProcessRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_1_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_2=(Token)match(input,12,FOLLOW_5); 

            			newLeafNode(otherlv_2, grammarAccess.getProcessAccess().getLeftCurlyBracketKeyword_2());
            		
            // InternalDsl.g:105:3: ( (lv_try_3_0= ruleTry ) )
            // InternalDsl.g:106:4: (lv_try_3_0= ruleTry )
            {
            // InternalDsl.g:106:4: (lv_try_3_0= ruleTry )
            // InternalDsl.g:107:5: lv_try_3_0= ruleTry
            {

            					newCompositeNode(grammarAccess.getProcessAccess().getTryTryParserRuleCall_3_0());
            				
            pushFollow(FOLLOW_6);
            lv_try_3_0=ruleTry();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getProcessRule());
            					}
            					set(
            						current,
            						"try",
            						lv_try_3_0,
            						"in.handyman.Dsl.Try");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            // InternalDsl.g:124:3: ( (lv_catch_4_0= ruleCatch ) )
            // InternalDsl.g:125:4: (lv_catch_4_0= ruleCatch )
            {
            // InternalDsl.g:125:4: (lv_catch_4_0= ruleCatch )
            // InternalDsl.g:126:5: lv_catch_4_0= ruleCatch
            {

            					newCompositeNode(grammarAccess.getProcessAccess().getCatchCatchParserRuleCall_4_0());
            				
            pushFollow(FOLLOW_7);
            lv_catch_4_0=ruleCatch();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getProcessRule());
            					}
            					set(
            						current,
            						"catch",
            						lv_catch_4_0,
            						"in.handyman.Dsl.Catch");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            // InternalDsl.g:143:3: ( (lv_finally_5_0= ruleFinally ) )
            // InternalDsl.g:144:4: (lv_finally_5_0= ruleFinally )
            {
            // InternalDsl.g:144:4: (lv_finally_5_0= ruleFinally )
            // InternalDsl.g:145:5: lv_finally_5_0= ruleFinally
            {

            					newCompositeNode(grammarAccess.getProcessAccess().getFinallyFinallyParserRuleCall_5_0());
            				
            pushFollow(FOLLOW_8);
            lv_finally_5_0=ruleFinally();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getProcessRule());
            					}
            					set(
            						current,
            						"finally",
            						lv_finally_5_0,
            						"in.handyman.Dsl.Finally");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_6=(Token)match(input,13,FOLLOW_2); 

            			newLeafNode(otherlv_6, grammarAccess.getProcessAccess().getRightCurlyBracketKeyword_6());
            		

            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleProcess"


    // $ANTLR start "entryRuleTry"
    // InternalDsl.g:170:1: entryRuleTry returns [EObject current=null] : iv_ruleTry= ruleTry EOF ;
    public final EObject entryRuleTry() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTry = null;


        try {
            // InternalDsl.g:170:44: (iv_ruleTry= ruleTry EOF )
            // InternalDsl.g:171:2: iv_ruleTry= ruleTry EOF
            {
             newCompositeNode(grammarAccess.getTryRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleTry=ruleTry();

            state._fsp--;

             current =iv_ruleTry; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleTry"


    // $ANTLR start "ruleTry"
    // InternalDsl.g:177:1: ruleTry returns [EObject current=null] : (otherlv_0= 'try' otherlv_1= '{' ( (lv_action_2_0= ruleAction ) )* otherlv_3= '}' ) ;
    public final EObject ruleTry() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token otherlv_3=null;
        EObject lv_action_2_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:183:2: ( (otherlv_0= 'try' otherlv_1= '{' ( (lv_action_2_0= ruleAction ) )* otherlv_3= '}' ) )
            // InternalDsl.g:184:2: (otherlv_0= 'try' otherlv_1= '{' ( (lv_action_2_0= ruleAction ) )* otherlv_3= '}' )
            {
            // InternalDsl.g:184:2: (otherlv_0= 'try' otherlv_1= '{' ( (lv_action_2_0= ruleAction ) )* otherlv_3= '}' )
            // InternalDsl.g:185:3: otherlv_0= 'try' otherlv_1= '{' ( (lv_action_2_0= ruleAction ) )* otherlv_3= '}'
            {
            otherlv_0=(Token)match(input,14,FOLLOW_4); 

            			newLeafNode(otherlv_0, grammarAccess.getTryAccess().getTryKeyword_0());
            		
            otherlv_1=(Token)match(input,12,FOLLOW_9); 

            			newLeafNode(otherlv_1, grammarAccess.getTryAccess().getLeftCurlyBracketKeyword_1());
            		
            // InternalDsl.g:193:3: ( (lv_action_2_0= ruleAction ) )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==17||LA1_0==32||LA1_0==35||LA1_0==37||(LA1_0>=39 && LA1_0<=40)||LA1_0==42||LA1_0==45||LA1_0==47||LA1_0==51||LA1_0==53||LA1_0==56||LA1_0==58||(LA1_0>=60 && LA1_0<=66)||LA1_0==68||LA1_0==78||LA1_0==88||LA1_0==92||(LA1_0>=94 && LA1_0<=96)||LA1_0==98||(LA1_0>=103 && LA1_0<=104)) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // InternalDsl.g:194:4: (lv_action_2_0= ruleAction )
            	    {
            	    // InternalDsl.g:194:4: (lv_action_2_0= ruleAction )
            	    // InternalDsl.g:195:5: lv_action_2_0= ruleAction
            	    {

            	    					newCompositeNode(grammarAccess.getTryAccess().getActionActionParserRuleCall_2_0());
            	    				
            	    pushFollow(FOLLOW_9);
            	    lv_action_2_0=ruleAction();

            	    state._fsp--;


            	    					if (current==null) {
            	    						current = createModelElementForParent(grammarAccess.getTryRule());
            	    					}
            	    					add(
            	    						current,
            	    						"action",
            	    						lv_action_2_0,
            	    						"in.handyman.Dsl.Action");
            	    					afterParserOrEnumRuleCall();
            	    				

            	    }


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            otherlv_3=(Token)match(input,13,FOLLOW_2); 

            			newLeafNode(otherlv_3, grammarAccess.getTryAccess().getRightCurlyBracketKeyword_3());
            		

            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleTry"


    // $ANTLR start "entryRuleFinally"
    // InternalDsl.g:220:1: entryRuleFinally returns [EObject current=null] : iv_ruleFinally= ruleFinally EOF ;
    public final EObject entryRuleFinally() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFinally = null;


        try {
            // InternalDsl.g:220:48: (iv_ruleFinally= ruleFinally EOF )
            // InternalDsl.g:221:2: iv_ruleFinally= ruleFinally EOF
            {
             newCompositeNode(grammarAccess.getFinallyRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleFinally=ruleFinally();

            state._fsp--;

             current =iv_ruleFinally; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleFinally"


    // $ANTLR start "ruleFinally"
    // InternalDsl.g:227:1: ruleFinally returns [EObject current=null] : (otherlv_0= 'finally' otherlv_1= '{' ( (lv_action_2_0= ruleAction ) )* otherlv_3= '}' ) ;
    public final EObject ruleFinally() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token otherlv_3=null;
        EObject lv_action_2_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:233:2: ( (otherlv_0= 'finally' otherlv_1= '{' ( (lv_action_2_0= ruleAction ) )* otherlv_3= '}' ) )
            // InternalDsl.g:234:2: (otherlv_0= 'finally' otherlv_1= '{' ( (lv_action_2_0= ruleAction ) )* otherlv_3= '}' )
            {
            // InternalDsl.g:234:2: (otherlv_0= 'finally' otherlv_1= '{' ( (lv_action_2_0= ruleAction ) )* otherlv_3= '}' )
            // InternalDsl.g:235:3: otherlv_0= 'finally' otherlv_1= '{' ( (lv_action_2_0= ruleAction ) )* otherlv_3= '}'
            {
            otherlv_0=(Token)match(input,15,FOLLOW_4); 

            			newLeafNode(otherlv_0, grammarAccess.getFinallyAccess().getFinallyKeyword_0());
            		
            otherlv_1=(Token)match(input,12,FOLLOW_9); 

            			newLeafNode(otherlv_1, grammarAccess.getFinallyAccess().getLeftCurlyBracketKeyword_1());
            		
            // InternalDsl.g:243:3: ( (lv_action_2_0= ruleAction ) )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==17||LA2_0==32||LA2_0==35||LA2_0==37||(LA2_0>=39 && LA2_0<=40)||LA2_0==42||LA2_0==45||LA2_0==47||LA2_0==51||LA2_0==53||LA2_0==56||LA2_0==58||(LA2_0>=60 && LA2_0<=66)||LA2_0==68||LA2_0==78||LA2_0==88||LA2_0==92||(LA2_0>=94 && LA2_0<=96)||LA2_0==98||(LA2_0>=103 && LA2_0<=104)) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // InternalDsl.g:244:4: (lv_action_2_0= ruleAction )
            	    {
            	    // InternalDsl.g:244:4: (lv_action_2_0= ruleAction )
            	    // InternalDsl.g:245:5: lv_action_2_0= ruleAction
            	    {

            	    					newCompositeNode(grammarAccess.getFinallyAccess().getActionActionParserRuleCall_2_0());
            	    				
            	    pushFollow(FOLLOW_9);
            	    lv_action_2_0=ruleAction();

            	    state._fsp--;


            	    					if (current==null) {
            	    						current = createModelElementForParent(grammarAccess.getFinallyRule());
            	    					}
            	    					add(
            	    						current,
            	    						"action",
            	    						lv_action_2_0,
            	    						"in.handyman.Dsl.Action");
            	    					afterParserOrEnumRuleCall();
            	    				

            	    }


            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

            otherlv_3=(Token)match(input,13,FOLLOW_2); 

            			newLeafNode(otherlv_3, grammarAccess.getFinallyAccess().getRightCurlyBracketKeyword_3());
            		

            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleFinally"


    // $ANTLR start "entryRuleCatch"
    // InternalDsl.g:270:1: entryRuleCatch returns [EObject current=null] : iv_ruleCatch= ruleCatch EOF ;
    public final EObject entryRuleCatch() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleCatch = null;


        try {
            // InternalDsl.g:270:46: (iv_ruleCatch= ruleCatch EOF )
            // InternalDsl.g:271:2: iv_ruleCatch= ruleCatch EOF
            {
             newCompositeNode(grammarAccess.getCatchRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleCatch=ruleCatch();

            state._fsp--;

             current =iv_ruleCatch; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleCatch"


    // $ANTLR start "ruleCatch"
    // InternalDsl.g:277:1: ruleCatch returns [EObject current=null] : (otherlv_0= 'catch' otherlv_1= '{' ( (lv_action_2_0= ruleAction ) )* otherlv_3= '}' ) ;
    public final EObject ruleCatch() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token otherlv_3=null;
        EObject lv_action_2_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:283:2: ( (otherlv_0= 'catch' otherlv_1= '{' ( (lv_action_2_0= ruleAction ) )* otherlv_3= '}' ) )
            // InternalDsl.g:284:2: (otherlv_0= 'catch' otherlv_1= '{' ( (lv_action_2_0= ruleAction ) )* otherlv_3= '}' )
            {
            // InternalDsl.g:284:2: (otherlv_0= 'catch' otherlv_1= '{' ( (lv_action_2_0= ruleAction ) )* otherlv_3= '}' )
            // InternalDsl.g:285:3: otherlv_0= 'catch' otherlv_1= '{' ( (lv_action_2_0= ruleAction ) )* otherlv_3= '}'
            {
            otherlv_0=(Token)match(input,16,FOLLOW_4); 

            			newLeafNode(otherlv_0, grammarAccess.getCatchAccess().getCatchKeyword_0());
            		
            otherlv_1=(Token)match(input,12,FOLLOW_9); 

            			newLeafNode(otherlv_1, grammarAccess.getCatchAccess().getLeftCurlyBracketKeyword_1());
            		
            // InternalDsl.g:293:3: ( (lv_action_2_0= ruleAction ) )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( (LA3_0==17||LA3_0==32||LA3_0==35||LA3_0==37||(LA3_0>=39 && LA3_0<=40)||LA3_0==42||LA3_0==45||LA3_0==47||LA3_0==51||LA3_0==53||LA3_0==56||LA3_0==58||(LA3_0>=60 && LA3_0<=66)||LA3_0==68||LA3_0==78||LA3_0==88||LA3_0==92||(LA3_0>=94 && LA3_0<=96)||LA3_0==98||(LA3_0>=103 && LA3_0<=104)) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // InternalDsl.g:294:4: (lv_action_2_0= ruleAction )
            	    {
            	    // InternalDsl.g:294:4: (lv_action_2_0= ruleAction )
            	    // InternalDsl.g:295:5: lv_action_2_0= ruleAction
            	    {

            	    					newCompositeNode(grammarAccess.getCatchAccess().getActionActionParserRuleCall_2_0());
            	    				
            	    pushFollow(FOLLOW_9);
            	    lv_action_2_0=ruleAction();

            	    state._fsp--;


            	    					if (current==null) {
            	    						current = createModelElementForParent(grammarAccess.getCatchRule());
            	    					}
            	    					add(
            	    						current,
            	    						"action",
            	    						lv_action_2_0,
            	    						"in.handyman.Dsl.Action");
            	    					afterParserOrEnumRuleCall();
            	    				

            	    }


            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            otherlv_3=(Token)match(input,13,FOLLOW_2); 

            			newLeafNode(otherlv_3, grammarAccess.getCatchAccess().getRightCurlyBracketKeyword_3());
            		

            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleCatch"


    // $ANTLR start "entryRuleAction"
    // InternalDsl.g:320:1: entryRuleAction returns [EObject current=null] : iv_ruleAction= ruleAction EOF ;
    public final EObject entryRuleAction() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAction = null;


        try {
            // InternalDsl.g:320:47: (iv_ruleAction= ruleAction EOF )
            // InternalDsl.g:321:2: iv_ruleAction= ruleAction EOF
            {
             newCompositeNode(grammarAccess.getActionRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleAction=ruleAction();

            state._fsp--;

             current =iv_ruleAction; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAction"


    // $ANTLR start "ruleAction"
    // InternalDsl.g:327:1: ruleAction returns [EObject current=null] : (this_Copydata_0= ruleCopydata | this_LoadCsv_1= ruleLoadCsv | this_WriteCsv_2= ruleWriteCsv | this_Transform_3= ruleTransform | this_Callprocess_4= ruleCallprocess | this_Forkprocess_5= ruleForkprocess | this_Fetch_6= ruleFetch | this_Doozle_7= ruleDoozle | this_Dropfile_8= ruleDropfile | this_Abort_9= ruleAbort | this_ExecJava_10= ruleExecJava | this_DeleteFolder_11= ruleDeleteFolder | this_Terminal_12= ruleTerminal | this_SendEMail_13= ruleSendEMail | this_Mongo2Db_14= ruleMongo2Db | this_FTP_15= ruleFTP | this_Unzip_16= ruleUnzip | this_Zip_17= ruleZip | this_Checksum_18= ruleChecksum | this_JsonTransform_19= ruleJsonTransform | this_RestApi_20= ruleRestApi | this_Python_21= rulePython | this_Rest_22= ruleRest | this_JsonDeserialize_23= ruleJsonDeserialize | this_ListFiles_24= ruleListFiles | this_DeleteSql_25= ruleDeleteSql | this_UpdateSql_26= ruleUpdateSql | this_InsertSql_27= ruleInsertSql | this_TruncateSql_28= ruleTruncateSql | this_DropSql_29= ruleDropSql ) ;
    public final EObject ruleAction() throws RecognitionException {
        EObject current = null;

        EObject this_Copydata_0 = null;

        EObject this_LoadCsv_1 = null;

        EObject this_WriteCsv_2 = null;

        EObject this_Transform_3 = null;

        EObject this_Callprocess_4 = null;

        EObject this_Forkprocess_5 = null;

        EObject this_Fetch_6 = null;

        EObject this_Doozle_7 = null;

        EObject this_Dropfile_8 = null;

        EObject this_Abort_9 = null;

        EObject this_ExecJava_10 = null;

        EObject this_DeleteFolder_11 = null;

        EObject this_Terminal_12 = null;

        EObject this_SendEMail_13 = null;

        EObject this_Mongo2Db_14 = null;

        EObject this_FTP_15 = null;

        EObject this_Unzip_16 = null;

        EObject this_Zip_17 = null;

        EObject this_Checksum_18 = null;

        EObject this_JsonTransform_19 = null;

        EObject this_RestApi_20 = null;

        EObject this_Python_21 = null;

        EObject this_Rest_22 = null;

        EObject this_JsonDeserialize_23 = null;

        EObject this_ListFiles_24 = null;

        EObject this_DeleteSql_25 = null;

        EObject this_UpdateSql_26 = null;

        EObject this_InsertSql_27 = null;

        EObject this_TruncateSql_28 = null;

        EObject this_DropSql_29 = null;



        	enterRule();

        try {
            // InternalDsl.g:333:2: ( (this_Copydata_0= ruleCopydata | this_LoadCsv_1= ruleLoadCsv | this_WriteCsv_2= ruleWriteCsv | this_Transform_3= ruleTransform | this_Callprocess_4= ruleCallprocess | this_Forkprocess_5= ruleForkprocess | this_Fetch_6= ruleFetch | this_Doozle_7= ruleDoozle | this_Dropfile_8= ruleDropfile | this_Abort_9= ruleAbort | this_ExecJava_10= ruleExecJava | this_DeleteFolder_11= ruleDeleteFolder | this_Terminal_12= ruleTerminal | this_SendEMail_13= ruleSendEMail | this_Mongo2Db_14= ruleMongo2Db | this_FTP_15= ruleFTP | this_Unzip_16= ruleUnzip | this_Zip_17= ruleZip | this_Checksum_18= ruleChecksum | this_JsonTransform_19= ruleJsonTransform | this_RestApi_20= ruleRestApi | this_Python_21= rulePython | this_Rest_22= ruleRest | this_JsonDeserialize_23= ruleJsonDeserialize | this_ListFiles_24= ruleListFiles | this_DeleteSql_25= ruleDeleteSql | this_UpdateSql_26= ruleUpdateSql | this_InsertSql_27= ruleInsertSql | this_TruncateSql_28= ruleTruncateSql | this_DropSql_29= ruleDropSql ) )
            // InternalDsl.g:334:2: (this_Copydata_0= ruleCopydata | this_LoadCsv_1= ruleLoadCsv | this_WriteCsv_2= ruleWriteCsv | this_Transform_3= ruleTransform | this_Callprocess_4= ruleCallprocess | this_Forkprocess_5= ruleForkprocess | this_Fetch_6= ruleFetch | this_Doozle_7= ruleDoozle | this_Dropfile_8= ruleDropfile | this_Abort_9= ruleAbort | this_ExecJava_10= ruleExecJava | this_DeleteFolder_11= ruleDeleteFolder | this_Terminal_12= ruleTerminal | this_SendEMail_13= ruleSendEMail | this_Mongo2Db_14= ruleMongo2Db | this_FTP_15= ruleFTP | this_Unzip_16= ruleUnzip | this_Zip_17= ruleZip | this_Checksum_18= ruleChecksum | this_JsonTransform_19= ruleJsonTransform | this_RestApi_20= ruleRestApi | this_Python_21= rulePython | this_Rest_22= ruleRest | this_JsonDeserialize_23= ruleJsonDeserialize | this_ListFiles_24= ruleListFiles | this_DeleteSql_25= ruleDeleteSql | this_UpdateSql_26= ruleUpdateSql | this_InsertSql_27= ruleInsertSql | this_TruncateSql_28= ruleTruncateSql | this_DropSql_29= ruleDropSql )
            {
            // InternalDsl.g:334:2: (this_Copydata_0= ruleCopydata | this_LoadCsv_1= ruleLoadCsv | this_WriteCsv_2= ruleWriteCsv | this_Transform_3= ruleTransform | this_Callprocess_4= ruleCallprocess | this_Forkprocess_5= ruleForkprocess | this_Fetch_6= ruleFetch | this_Doozle_7= ruleDoozle | this_Dropfile_8= ruleDropfile | this_Abort_9= ruleAbort | this_ExecJava_10= ruleExecJava | this_DeleteFolder_11= ruleDeleteFolder | this_Terminal_12= ruleTerminal | this_SendEMail_13= ruleSendEMail | this_Mongo2Db_14= ruleMongo2Db | this_FTP_15= ruleFTP | this_Unzip_16= ruleUnzip | this_Zip_17= ruleZip | this_Checksum_18= ruleChecksum | this_JsonTransform_19= ruleJsonTransform | this_RestApi_20= ruleRestApi | this_Python_21= rulePython | this_Rest_22= ruleRest | this_JsonDeserialize_23= ruleJsonDeserialize | this_ListFiles_24= ruleListFiles | this_DeleteSql_25= ruleDeleteSql | this_UpdateSql_26= ruleUpdateSql | this_InsertSql_27= ruleInsertSql | this_TruncateSql_28= ruleTruncateSql | this_DropSql_29= ruleDropSql )
            int alt4=30;
            switch ( input.LA(1) ) {
            case 53:
                {
                alt4=1;
                }
                break;
            case 58:
                {
                alt4=2;
                }
                break;
            case 56:
                {
                alt4=3;
                }
                break;
            case 60:
                {
                alt4=4;
                }
                break;
            case 47:
                {
                alt4=5;
                }
                break;
            case 51:
                {
                alt4=6;
                }
                break;
            case 45:
                {
                alt4=7;
                }
                break;
            case 42:
                {
                alt4=8;
                }
                break;
            case 40:
                {
                alt4=9;
                }
                break;
            case 39:
                {
                alt4=10;
                }
                break;
            case 37:
                {
                alt4=11;
                }
                break;
            case 32:
                {
                alt4=12;
                }
                break;
            case 35:
                {
                alt4=13;
                }
                break;
            case 17:
                {
                alt4=14;
                }
                break;
            case 68:
                {
                alt4=15;
                }
                break;
            case 78:
                {
                alt4=16;
                }
                break;
            case 92:
                {
                alt4=17;
                }
                break;
            case 88:
                {
                alt4=18;
                }
                break;
            case 94:
                {
                alt4=19;
                }
                break;
            case 95:
                {
                alt4=20;
                }
                break;
            case 98:
                {
                alt4=21;
                }
                break;
            case 103:
                {
                alt4=22;
                }
                break;
            case 104:
                {
                alt4=23;
                }
                break;
            case 96:
                {
                alt4=24;
                }
                break;
            case 66:
                {
                alt4=25;
                }
                break;
            case 61:
                {
                alt4=26;
                }
                break;
            case 62:
                {
                alt4=27;
                }
                break;
            case 63:
                {
                alt4=28;
                }
                break;
            case 64:
                {
                alt4=29;
                }
                break;
            case 65:
                {
                alt4=30;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }

            switch (alt4) {
                case 1 :
                    // InternalDsl.g:335:3: this_Copydata_0= ruleCopydata
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getCopydataParserRuleCall_0());
                    		
                    pushFollow(FOLLOW_2);
                    this_Copydata_0=ruleCopydata();

                    state._fsp--;


                    			current = this_Copydata_0;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 2 :
                    // InternalDsl.g:344:3: this_LoadCsv_1= ruleLoadCsv
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getLoadCsvParserRuleCall_1());
                    		
                    pushFollow(FOLLOW_2);
                    this_LoadCsv_1=ruleLoadCsv();

                    state._fsp--;


                    			current = this_LoadCsv_1;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 3 :
                    // InternalDsl.g:353:3: this_WriteCsv_2= ruleWriteCsv
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getWriteCsvParserRuleCall_2());
                    		
                    pushFollow(FOLLOW_2);
                    this_WriteCsv_2=ruleWriteCsv();

                    state._fsp--;


                    			current = this_WriteCsv_2;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 4 :
                    // InternalDsl.g:362:3: this_Transform_3= ruleTransform
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getTransformParserRuleCall_3());
                    		
                    pushFollow(FOLLOW_2);
                    this_Transform_3=ruleTransform();

                    state._fsp--;


                    			current = this_Transform_3;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 5 :
                    // InternalDsl.g:371:3: this_Callprocess_4= ruleCallprocess
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getCallprocessParserRuleCall_4());
                    		
                    pushFollow(FOLLOW_2);
                    this_Callprocess_4=ruleCallprocess();

                    state._fsp--;


                    			current = this_Callprocess_4;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 6 :
                    // InternalDsl.g:380:3: this_Forkprocess_5= ruleForkprocess
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getForkprocessParserRuleCall_5());
                    		
                    pushFollow(FOLLOW_2);
                    this_Forkprocess_5=ruleForkprocess();

                    state._fsp--;


                    			current = this_Forkprocess_5;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 7 :
                    // InternalDsl.g:389:3: this_Fetch_6= ruleFetch
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getFetchParserRuleCall_6());
                    		
                    pushFollow(FOLLOW_2);
                    this_Fetch_6=ruleFetch();

                    state._fsp--;


                    			current = this_Fetch_6;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 8 :
                    // InternalDsl.g:398:3: this_Doozle_7= ruleDoozle
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getDoozleParserRuleCall_7());
                    		
                    pushFollow(FOLLOW_2);
                    this_Doozle_7=ruleDoozle();

                    state._fsp--;


                    			current = this_Doozle_7;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 9 :
                    // InternalDsl.g:407:3: this_Dropfile_8= ruleDropfile
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getDropfileParserRuleCall_8());
                    		
                    pushFollow(FOLLOW_2);
                    this_Dropfile_8=ruleDropfile();

                    state._fsp--;


                    			current = this_Dropfile_8;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 10 :
                    // InternalDsl.g:416:3: this_Abort_9= ruleAbort
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getAbortParserRuleCall_9());
                    		
                    pushFollow(FOLLOW_2);
                    this_Abort_9=ruleAbort();

                    state._fsp--;


                    			current = this_Abort_9;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 11 :
                    // InternalDsl.g:425:3: this_ExecJava_10= ruleExecJava
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getExecJavaParserRuleCall_10());
                    		
                    pushFollow(FOLLOW_2);
                    this_ExecJava_10=ruleExecJava();

                    state._fsp--;


                    			current = this_ExecJava_10;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 12 :
                    // InternalDsl.g:434:3: this_DeleteFolder_11= ruleDeleteFolder
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getDeleteFolderParserRuleCall_11());
                    		
                    pushFollow(FOLLOW_2);
                    this_DeleteFolder_11=ruleDeleteFolder();

                    state._fsp--;


                    			current = this_DeleteFolder_11;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 13 :
                    // InternalDsl.g:443:3: this_Terminal_12= ruleTerminal
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getTerminalParserRuleCall_12());
                    		
                    pushFollow(FOLLOW_2);
                    this_Terminal_12=ruleTerminal();

                    state._fsp--;


                    			current = this_Terminal_12;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 14 :
                    // InternalDsl.g:452:3: this_SendEMail_13= ruleSendEMail
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getSendEMailParserRuleCall_13());
                    		
                    pushFollow(FOLLOW_2);
                    this_SendEMail_13=ruleSendEMail();

                    state._fsp--;


                    			current = this_SendEMail_13;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 15 :
                    // InternalDsl.g:461:3: this_Mongo2Db_14= ruleMongo2Db
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getMongo2DbParserRuleCall_14());
                    		
                    pushFollow(FOLLOW_2);
                    this_Mongo2Db_14=ruleMongo2Db();

                    state._fsp--;


                    			current = this_Mongo2Db_14;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 16 :
                    // InternalDsl.g:470:3: this_FTP_15= ruleFTP
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getFTPParserRuleCall_15());
                    		
                    pushFollow(FOLLOW_2);
                    this_FTP_15=ruleFTP();

                    state._fsp--;


                    			current = this_FTP_15;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 17 :
                    // InternalDsl.g:479:3: this_Unzip_16= ruleUnzip
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getUnzipParserRuleCall_16());
                    		
                    pushFollow(FOLLOW_2);
                    this_Unzip_16=ruleUnzip();

                    state._fsp--;


                    			current = this_Unzip_16;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 18 :
                    // InternalDsl.g:488:3: this_Zip_17= ruleZip
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getZipParserRuleCall_17());
                    		
                    pushFollow(FOLLOW_2);
                    this_Zip_17=ruleZip();

                    state._fsp--;


                    			current = this_Zip_17;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 19 :
                    // InternalDsl.g:497:3: this_Checksum_18= ruleChecksum
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getChecksumParserRuleCall_18());
                    		
                    pushFollow(FOLLOW_2);
                    this_Checksum_18=ruleChecksum();

                    state._fsp--;


                    			current = this_Checksum_18;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 20 :
                    // InternalDsl.g:506:3: this_JsonTransform_19= ruleJsonTransform
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getJsonTransformParserRuleCall_19());
                    		
                    pushFollow(FOLLOW_2);
                    this_JsonTransform_19=ruleJsonTransform();

                    state._fsp--;


                    			current = this_JsonTransform_19;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 21 :
                    // InternalDsl.g:515:3: this_RestApi_20= ruleRestApi
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getRestApiParserRuleCall_20());
                    		
                    pushFollow(FOLLOW_2);
                    this_RestApi_20=ruleRestApi();

                    state._fsp--;


                    			current = this_RestApi_20;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 22 :
                    // InternalDsl.g:524:3: this_Python_21= rulePython
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getPythonParserRuleCall_21());
                    		
                    pushFollow(FOLLOW_2);
                    this_Python_21=rulePython();

                    state._fsp--;


                    			current = this_Python_21;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 23 :
                    // InternalDsl.g:533:3: this_Rest_22= ruleRest
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getRestParserRuleCall_22());
                    		
                    pushFollow(FOLLOW_2);
                    this_Rest_22=ruleRest();

                    state._fsp--;


                    			current = this_Rest_22;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 24 :
                    // InternalDsl.g:542:3: this_JsonDeserialize_23= ruleJsonDeserialize
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getJsonDeserializeParserRuleCall_23());
                    		
                    pushFollow(FOLLOW_2);
                    this_JsonDeserialize_23=ruleJsonDeserialize();

                    state._fsp--;


                    			current = this_JsonDeserialize_23;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 25 :
                    // InternalDsl.g:551:3: this_ListFiles_24= ruleListFiles
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getListFilesParserRuleCall_24());
                    		
                    pushFollow(FOLLOW_2);
                    this_ListFiles_24=ruleListFiles();

                    state._fsp--;


                    			current = this_ListFiles_24;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 26 :
                    // InternalDsl.g:560:3: this_DeleteSql_25= ruleDeleteSql
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getDeleteSqlParserRuleCall_25());
                    		
                    pushFollow(FOLLOW_2);
                    this_DeleteSql_25=ruleDeleteSql();

                    state._fsp--;


                    			current = this_DeleteSql_25;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 27 :
                    // InternalDsl.g:569:3: this_UpdateSql_26= ruleUpdateSql
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getUpdateSqlParserRuleCall_26());
                    		
                    pushFollow(FOLLOW_2);
                    this_UpdateSql_26=ruleUpdateSql();

                    state._fsp--;


                    			current = this_UpdateSql_26;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 28 :
                    // InternalDsl.g:578:3: this_InsertSql_27= ruleInsertSql
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getInsertSqlParserRuleCall_27());
                    		
                    pushFollow(FOLLOW_2);
                    this_InsertSql_27=ruleInsertSql();

                    state._fsp--;


                    			current = this_InsertSql_27;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 29 :
                    // InternalDsl.g:587:3: this_TruncateSql_28= ruleTruncateSql
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getTruncateSqlParserRuleCall_28());
                    		
                    pushFollow(FOLLOW_2);
                    this_TruncateSql_28=ruleTruncateSql();

                    state._fsp--;


                    			current = this_TruncateSql_28;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;
                case 30 :
                    // InternalDsl.g:596:3: this_DropSql_29= ruleDropSql
                    {

                    			newCompositeNode(grammarAccess.getActionAccess().getDropSqlParserRuleCall_29());
                    		
                    pushFollow(FOLLOW_2);
                    this_DropSql_29=ruleDropSql();

                    state._fsp--;


                    			current = this_DropSql_29;
                    			afterParserOrEnumRuleCall();
                    		

                    }
                    break;

            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAction"


    // $ANTLR start "entryRuleSendEMail"
    // InternalDsl.g:608:1: entryRuleSendEMail returns [EObject current=null] : iv_ruleSendEMail= ruleSendEMail EOF ;
    public final EObject entryRuleSendEMail() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleSendEMail = null;


        try {
            // InternalDsl.g:608:50: (iv_ruleSendEMail= ruleSendEMail EOF )
            // InternalDsl.g:609:2: iv_ruleSendEMail= ruleSendEMail EOF
            {
             newCompositeNode(grammarAccess.getSendEMailRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleSendEMail=ruleSendEMail();

            state._fsp--;

             current =iv_ruleSendEMail; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleSendEMail"


    // $ANTLR start "ruleSendEMail"
    // InternalDsl.g:615:1: ruleSendEMail returns [EObject current=null] : (otherlv_0= 'sendemail' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'db' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'smtp-host' ( (lv_smtphost_6_0= RULE_STRING ) ) otherlv_7= 'smtp-port' ( (lv_smtpport_8_0= RULE_STRING ) ) otherlv_9= 'from' ( (lv_from_10_0= RULE_STRING ) ) otherlv_11= 'pass' ( (lv_pass_12_0= RULE_STRING ) ) otherlv_13= 'to' ( (lv_to_14_0= RULE_STRING ) ) otherlv_15= 'cc' ( (lv_cc_16_0= RULE_STRING ) ) otherlv_17= 'bcc' ( (lv_bcc_18_0= RULE_STRING ) ) otherlv_19= 'subject' ( (lv_subject_20_0= RULE_STRING ) ) otherlv_21= 'body' ( (lv_body_22_0= RULE_STRING ) ) otherlv_23= 'signature' ( (lv_signature_24_0= RULE_STRING ) ) otherlv_25= 'using' otherlv_26= '{' ( (lv_value_27_0= ruleSelectStatement ) ) otherlv_28= '}' otherlv_29= 'on-condition' ( (lv_condition_30_0= ruleExpression ) ) ) ;
    public final EObject ruleSendEMail() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_source_4_0=null;
        Token otherlv_5=null;
        Token lv_smtphost_6_0=null;
        Token otherlv_7=null;
        Token lv_smtpport_8_0=null;
        Token otherlv_9=null;
        Token lv_from_10_0=null;
        Token otherlv_11=null;
        Token lv_pass_12_0=null;
        Token otherlv_13=null;
        Token lv_to_14_0=null;
        Token otherlv_15=null;
        Token lv_cc_16_0=null;
        Token otherlv_17=null;
        Token lv_bcc_18_0=null;
        Token otherlv_19=null;
        Token lv_subject_20_0=null;
        Token otherlv_21=null;
        Token lv_body_22_0=null;
        Token otherlv_23=null;
        Token lv_signature_24_0=null;
        Token otherlv_25=null;
        Token otherlv_26=null;
        Token otherlv_28=null;
        Token otherlv_29=null;
        AntlrDatatypeRuleToken lv_value_27_0 = null;

        EObject lv_condition_30_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:621:2: ( (otherlv_0= 'sendemail' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'db' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'smtp-host' ( (lv_smtphost_6_0= RULE_STRING ) ) otherlv_7= 'smtp-port' ( (lv_smtpport_8_0= RULE_STRING ) ) otherlv_9= 'from' ( (lv_from_10_0= RULE_STRING ) ) otherlv_11= 'pass' ( (lv_pass_12_0= RULE_STRING ) ) otherlv_13= 'to' ( (lv_to_14_0= RULE_STRING ) ) otherlv_15= 'cc' ( (lv_cc_16_0= RULE_STRING ) ) otherlv_17= 'bcc' ( (lv_bcc_18_0= RULE_STRING ) ) otherlv_19= 'subject' ( (lv_subject_20_0= RULE_STRING ) ) otherlv_21= 'body' ( (lv_body_22_0= RULE_STRING ) ) otherlv_23= 'signature' ( (lv_signature_24_0= RULE_STRING ) ) otherlv_25= 'using' otherlv_26= '{' ( (lv_value_27_0= ruleSelectStatement ) ) otherlv_28= '}' otherlv_29= 'on-condition' ( (lv_condition_30_0= ruleExpression ) ) ) )
            // InternalDsl.g:622:2: (otherlv_0= 'sendemail' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'db' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'smtp-host' ( (lv_smtphost_6_0= RULE_STRING ) ) otherlv_7= 'smtp-port' ( (lv_smtpport_8_0= RULE_STRING ) ) otherlv_9= 'from' ( (lv_from_10_0= RULE_STRING ) ) otherlv_11= 'pass' ( (lv_pass_12_0= RULE_STRING ) ) otherlv_13= 'to' ( (lv_to_14_0= RULE_STRING ) ) otherlv_15= 'cc' ( (lv_cc_16_0= RULE_STRING ) ) otherlv_17= 'bcc' ( (lv_bcc_18_0= RULE_STRING ) ) otherlv_19= 'subject' ( (lv_subject_20_0= RULE_STRING ) ) otherlv_21= 'body' ( (lv_body_22_0= RULE_STRING ) ) otherlv_23= 'signature' ( (lv_signature_24_0= RULE_STRING ) ) otherlv_25= 'using' otherlv_26= '{' ( (lv_value_27_0= ruleSelectStatement ) ) otherlv_28= '}' otherlv_29= 'on-condition' ( (lv_condition_30_0= ruleExpression ) ) )
            {
            // InternalDsl.g:622:2: (otherlv_0= 'sendemail' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'db' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'smtp-host' ( (lv_smtphost_6_0= RULE_STRING ) ) otherlv_7= 'smtp-port' ( (lv_smtpport_8_0= RULE_STRING ) ) otherlv_9= 'from' ( (lv_from_10_0= RULE_STRING ) ) otherlv_11= 'pass' ( (lv_pass_12_0= RULE_STRING ) ) otherlv_13= 'to' ( (lv_to_14_0= RULE_STRING ) ) otherlv_15= 'cc' ( (lv_cc_16_0= RULE_STRING ) ) otherlv_17= 'bcc' ( (lv_bcc_18_0= RULE_STRING ) ) otherlv_19= 'subject' ( (lv_subject_20_0= RULE_STRING ) ) otherlv_21= 'body' ( (lv_body_22_0= RULE_STRING ) ) otherlv_23= 'signature' ( (lv_signature_24_0= RULE_STRING ) ) otherlv_25= 'using' otherlv_26= '{' ( (lv_value_27_0= ruleSelectStatement ) ) otherlv_28= '}' otherlv_29= 'on-condition' ( (lv_condition_30_0= ruleExpression ) ) )
            // InternalDsl.g:623:3: otherlv_0= 'sendemail' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'db' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'smtp-host' ( (lv_smtphost_6_0= RULE_STRING ) ) otherlv_7= 'smtp-port' ( (lv_smtpport_8_0= RULE_STRING ) ) otherlv_9= 'from' ( (lv_from_10_0= RULE_STRING ) ) otherlv_11= 'pass' ( (lv_pass_12_0= RULE_STRING ) ) otherlv_13= 'to' ( (lv_to_14_0= RULE_STRING ) ) otherlv_15= 'cc' ( (lv_cc_16_0= RULE_STRING ) ) otherlv_17= 'bcc' ( (lv_bcc_18_0= RULE_STRING ) ) otherlv_19= 'subject' ( (lv_subject_20_0= RULE_STRING ) ) otherlv_21= 'body' ( (lv_body_22_0= RULE_STRING ) ) otherlv_23= 'signature' ( (lv_signature_24_0= RULE_STRING ) ) otherlv_25= 'using' otherlv_26= '{' ( (lv_value_27_0= ruleSelectStatement ) ) otherlv_28= '}' otherlv_29= 'on-condition' ( (lv_condition_30_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,17,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getSendEMailAccess().getSendemailKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getSendEMailAccess().getAsKeyword_1());
            		
            // InternalDsl.g:631:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:632:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:632:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:633:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_11); 

            					newLeafNode(lv_name_2_0, grammarAccess.getSendEMailAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getSendEMailRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,19,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getSendEMailAccess().getDbKeyword_3());
            		
            // InternalDsl.g:653:3: ( (lv_source_4_0= RULE_STRING ) )
            // InternalDsl.g:654:4: (lv_source_4_0= RULE_STRING )
            {
            // InternalDsl.g:654:4: (lv_source_4_0= RULE_STRING )
            // InternalDsl.g:655:5: lv_source_4_0= RULE_STRING
            {
            lv_source_4_0=(Token)match(input,RULE_STRING,FOLLOW_12); 

            					newLeafNode(lv_source_4_0, grammarAccess.getSendEMailAccess().getSourceSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getSendEMailRule());
            					}
            					setWithLastConsumed(
            						current,
            						"source",
            						lv_source_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,20,FOLLOW_3); 

            			newLeafNode(otherlv_5, grammarAccess.getSendEMailAccess().getSmtpHostKeyword_5());
            		
            // InternalDsl.g:675:3: ( (lv_smtphost_6_0= RULE_STRING ) )
            // InternalDsl.g:676:4: (lv_smtphost_6_0= RULE_STRING )
            {
            // InternalDsl.g:676:4: (lv_smtphost_6_0= RULE_STRING )
            // InternalDsl.g:677:5: lv_smtphost_6_0= RULE_STRING
            {
            lv_smtphost_6_0=(Token)match(input,RULE_STRING,FOLLOW_13); 

            					newLeafNode(lv_smtphost_6_0, grammarAccess.getSendEMailAccess().getSmtphostSTRINGTerminalRuleCall_6_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getSendEMailRule());
            					}
            					setWithLastConsumed(
            						current,
            						"smtphost",
            						lv_smtphost_6_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_7=(Token)match(input,21,FOLLOW_3); 

            			newLeafNode(otherlv_7, grammarAccess.getSendEMailAccess().getSmtpPortKeyword_7());
            		
            // InternalDsl.g:697:3: ( (lv_smtpport_8_0= RULE_STRING ) )
            // InternalDsl.g:698:4: (lv_smtpport_8_0= RULE_STRING )
            {
            // InternalDsl.g:698:4: (lv_smtpport_8_0= RULE_STRING )
            // InternalDsl.g:699:5: lv_smtpport_8_0= RULE_STRING
            {
            lv_smtpport_8_0=(Token)match(input,RULE_STRING,FOLLOW_14); 

            					newLeafNode(lv_smtpport_8_0, grammarAccess.getSendEMailAccess().getSmtpportSTRINGTerminalRuleCall_8_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getSendEMailRule());
            					}
            					setWithLastConsumed(
            						current,
            						"smtpport",
            						lv_smtpport_8_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_9=(Token)match(input,22,FOLLOW_3); 

            			newLeafNode(otherlv_9, grammarAccess.getSendEMailAccess().getFromKeyword_9());
            		
            // InternalDsl.g:719:3: ( (lv_from_10_0= RULE_STRING ) )
            // InternalDsl.g:720:4: (lv_from_10_0= RULE_STRING )
            {
            // InternalDsl.g:720:4: (lv_from_10_0= RULE_STRING )
            // InternalDsl.g:721:5: lv_from_10_0= RULE_STRING
            {
            lv_from_10_0=(Token)match(input,RULE_STRING,FOLLOW_15); 

            					newLeafNode(lv_from_10_0, grammarAccess.getSendEMailAccess().getFromSTRINGTerminalRuleCall_10_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getSendEMailRule());
            					}
            					setWithLastConsumed(
            						current,
            						"from",
            						lv_from_10_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_11=(Token)match(input,23,FOLLOW_3); 

            			newLeafNode(otherlv_11, grammarAccess.getSendEMailAccess().getPassKeyword_11());
            		
            // InternalDsl.g:741:3: ( (lv_pass_12_0= RULE_STRING ) )
            // InternalDsl.g:742:4: (lv_pass_12_0= RULE_STRING )
            {
            // InternalDsl.g:742:4: (lv_pass_12_0= RULE_STRING )
            // InternalDsl.g:743:5: lv_pass_12_0= RULE_STRING
            {
            lv_pass_12_0=(Token)match(input,RULE_STRING,FOLLOW_16); 

            					newLeafNode(lv_pass_12_0, grammarAccess.getSendEMailAccess().getPassSTRINGTerminalRuleCall_12_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getSendEMailRule());
            					}
            					setWithLastConsumed(
            						current,
            						"pass",
            						lv_pass_12_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_13=(Token)match(input,24,FOLLOW_3); 

            			newLeafNode(otherlv_13, grammarAccess.getSendEMailAccess().getToKeyword_13());
            		
            // InternalDsl.g:763:3: ( (lv_to_14_0= RULE_STRING ) )
            // InternalDsl.g:764:4: (lv_to_14_0= RULE_STRING )
            {
            // InternalDsl.g:764:4: (lv_to_14_0= RULE_STRING )
            // InternalDsl.g:765:5: lv_to_14_0= RULE_STRING
            {
            lv_to_14_0=(Token)match(input,RULE_STRING,FOLLOW_17); 

            					newLeafNode(lv_to_14_0, grammarAccess.getSendEMailAccess().getToSTRINGTerminalRuleCall_14_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getSendEMailRule());
            					}
            					setWithLastConsumed(
            						current,
            						"to",
            						lv_to_14_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_15=(Token)match(input,25,FOLLOW_3); 

            			newLeafNode(otherlv_15, grammarAccess.getSendEMailAccess().getCcKeyword_15());
            		
            // InternalDsl.g:785:3: ( (lv_cc_16_0= RULE_STRING ) )
            // InternalDsl.g:786:4: (lv_cc_16_0= RULE_STRING )
            {
            // InternalDsl.g:786:4: (lv_cc_16_0= RULE_STRING )
            // InternalDsl.g:787:5: lv_cc_16_0= RULE_STRING
            {
            lv_cc_16_0=(Token)match(input,RULE_STRING,FOLLOW_18); 

            					newLeafNode(lv_cc_16_0, grammarAccess.getSendEMailAccess().getCcSTRINGTerminalRuleCall_16_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getSendEMailRule());
            					}
            					setWithLastConsumed(
            						current,
            						"cc",
            						lv_cc_16_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_17=(Token)match(input,26,FOLLOW_3); 

            			newLeafNode(otherlv_17, grammarAccess.getSendEMailAccess().getBccKeyword_17());
            		
            // InternalDsl.g:807:3: ( (lv_bcc_18_0= RULE_STRING ) )
            // InternalDsl.g:808:4: (lv_bcc_18_0= RULE_STRING )
            {
            // InternalDsl.g:808:4: (lv_bcc_18_0= RULE_STRING )
            // InternalDsl.g:809:5: lv_bcc_18_0= RULE_STRING
            {
            lv_bcc_18_0=(Token)match(input,RULE_STRING,FOLLOW_19); 

            					newLeafNode(lv_bcc_18_0, grammarAccess.getSendEMailAccess().getBccSTRINGTerminalRuleCall_18_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getSendEMailRule());
            					}
            					setWithLastConsumed(
            						current,
            						"bcc",
            						lv_bcc_18_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_19=(Token)match(input,27,FOLLOW_3); 

            			newLeafNode(otherlv_19, grammarAccess.getSendEMailAccess().getSubjectKeyword_19());
            		
            // InternalDsl.g:829:3: ( (lv_subject_20_0= RULE_STRING ) )
            // InternalDsl.g:830:4: (lv_subject_20_0= RULE_STRING )
            {
            // InternalDsl.g:830:4: (lv_subject_20_0= RULE_STRING )
            // InternalDsl.g:831:5: lv_subject_20_0= RULE_STRING
            {
            lv_subject_20_0=(Token)match(input,RULE_STRING,FOLLOW_20); 

            					newLeafNode(lv_subject_20_0, grammarAccess.getSendEMailAccess().getSubjectSTRINGTerminalRuleCall_20_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getSendEMailRule());
            					}
            					setWithLastConsumed(
            						current,
            						"subject",
            						lv_subject_20_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_21=(Token)match(input,28,FOLLOW_3); 

            			newLeafNode(otherlv_21, grammarAccess.getSendEMailAccess().getBodyKeyword_21());
            		
            // InternalDsl.g:851:3: ( (lv_body_22_0= RULE_STRING ) )
            // InternalDsl.g:852:4: (lv_body_22_0= RULE_STRING )
            {
            // InternalDsl.g:852:4: (lv_body_22_0= RULE_STRING )
            // InternalDsl.g:853:5: lv_body_22_0= RULE_STRING
            {
            lv_body_22_0=(Token)match(input,RULE_STRING,FOLLOW_21); 

            					newLeafNode(lv_body_22_0, grammarAccess.getSendEMailAccess().getBodySTRINGTerminalRuleCall_22_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getSendEMailRule());
            					}
            					setWithLastConsumed(
            						current,
            						"body",
            						lv_body_22_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_23=(Token)match(input,29,FOLLOW_3); 

            			newLeafNode(otherlv_23, grammarAccess.getSendEMailAccess().getSignatureKeyword_23());
            		
            // InternalDsl.g:873:3: ( (lv_signature_24_0= RULE_STRING ) )
            // InternalDsl.g:874:4: (lv_signature_24_0= RULE_STRING )
            {
            // InternalDsl.g:874:4: (lv_signature_24_0= RULE_STRING )
            // InternalDsl.g:875:5: lv_signature_24_0= RULE_STRING
            {
            lv_signature_24_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_signature_24_0, grammarAccess.getSendEMailAccess().getSignatureSTRINGTerminalRuleCall_24_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getSendEMailRule());
            					}
            					setWithLastConsumed(
            						current,
            						"signature",
            						lv_signature_24_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_25=(Token)match(input,30,FOLLOW_4); 

            			newLeafNode(otherlv_25, grammarAccess.getSendEMailAccess().getUsingKeyword_25());
            		
            otherlv_26=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_26, grammarAccess.getSendEMailAccess().getLeftCurlyBracketKeyword_26());
            		
            // InternalDsl.g:899:3: ( (lv_value_27_0= ruleSelectStatement ) )
            // InternalDsl.g:900:4: (lv_value_27_0= ruleSelectStatement )
            {
            // InternalDsl.g:900:4: (lv_value_27_0= ruleSelectStatement )
            // InternalDsl.g:901:5: lv_value_27_0= ruleSelectStatement
            {

            					newCompositeNode(grammarAccess.getSendEMailAccess().getValueSelectStatementParserRuleCall_27_0());
            				
            pushFollow(FOLLOW_8);
            lv_value_27_0=ruleSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getSendEMailRule());
            					}
            					set(
            						current,
            						"value",
            						lv_value_27_0,
            						"in.handyman.Dsl.SelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_28=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_28, grammarAccess.getSendEMailAccess().getRightCurlyBracketKeyword_28());
            		
            otherlv_29=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_29, grammarAccess.getSendEMailAccess().getOnConditionKeyword_29());
            		
            // InternalDsl.g:926:3: ( (lv_condition_30_0= ruleExpression ) )
            // InternalDsl.g:927:4: (lv_condition_30_0= ruleExpression )
            {
            // InternalDsl.g:927:4: (lv_condition_30_0= ruleExpression )
            // InternalDsl.g:928:5: lv_condition_30_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getSendEMailAccess().getConditionExpressionParserRuleCall_30_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_30_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getSendEMailRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_30_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleSendEMail"


    // $ANTLR start "entryRuleDeleteFolder"
    // InternalDsl.g:949:1: entryRuleDeleteFolder returns [EObject current=null] : iv_ruleDeleteFolder= ruleDeleteFolder EOF ;
    public final EObject entryRuleDeleteFolder() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleDeleteFolder = null;


        try {
            // InternalDsl.g:949:53: (iv_ruleDeleteFolder= ruleDeleteFolder EOF )
            // InternalDsl.g:950:2: iv_ruleDeleteFolder= ruleDeleteFolder EOF
            {
             newCompositeNode(grammarAccess.getDeleteFolderRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleDeleteFolder=ruleDeleteFolder();

            state._fsp--;

             current =iv_ruleDeleteFolder; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleDeleteFolder"


    // $ANTLR start "ruleDeleteFolder"
    // InternalDsl.g:956:1: ruleDeleteFolder returns [EObject current=null] : (otherlv_0= 'deletefolder' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'folder' ( (lv_foldersource_4_0= RULE_STRING ) ) otherlv_5= 'zipfile' ( (lv_zipsource_6_0= RULE_STRING ) ) otherlv_7= '{' ( (lv_value_8_0= ruleSelectStatement ) ) otherlv_9= '}' otherlv_10= 'on-condition' ( (lv_condition_11_0= ruleExpression ) ) ) ;
    public final EObject ruleDeleteFolder() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_foldersource_4_0=null;
        Token otherlv_5=null;
        Token lv_zipsource_6_0=null;
        Token otherlv_7=null;
        Token otherlv_9=null;
        Token otherlv_10=null;
        AntlrDatatypeRuleToken lv_value_8_0 = null;

        EObject lv_condition_11_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:962:2: ( (otherlv_0= 'deletefolder' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'folder' ( (lv_foldersource_4_0= RULE_STRING ) ) otherlv_5= 'zipfile' ( (lv_zipsource_6_0= RULE_STRING ) ) otherlv_7= '{' ( (lv_value_8_0= ruleSelectStatement ) ) otherlv_9= '}' otherlv_10= 'on-condition' ( (lv_condition_11_0= ruleExpression ) ) ) )
            // InternalDsl.g:963:2: (otherlv_0= 'deletefolder' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'folder' ( (lv_foldersource_4_0= RULE_STRING ) ) otherlv_5= 'zipfile' ( (lv_zipsource_6_0= RULE_STRING ) ) otherlv_7= '{' ( (lv_value_8_0= ruleSelectStatement ) ) otherlv_9= '}' otherlv_10= 'on-condition' ( (lv_condition_11_0= ruleExpression ) ) )
            {
            // InternalDsl.g:963:2: (otherlv_0= 'deletefolder' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'folder' ( (lv_foldersource_4_0= RULE_STRING ) ) otherlv_5= 'zipfile' ( (lv_zipsource_6_0= RULE_STRING ) ) otherlv_7= '{' ( (lv_value_8_0= ruleSelectStatement ) ) otherlv_9= '}' otherlv_10= 'on-condition' ( (lv_condition_11_0= ruleExpression ) ) )
            // InternalDsl.g:964:3: otherlv_0= 'deletefolder' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'folder' ( (lv_foldersource_4_0= RULE_STRING ) ) otherlv_5= 'zipfile' ( (lv_zipsource_6_0= RULE_STRING ) ) otherlv_7= '{' ( (lv_value_8_0= ruleSelectStatement ) ) otherlv_9= '}' otherlv_10= 'on-condition' ( (lv_condition_11_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,32,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getDeleteFolderAccess().getDeletefolderKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getDeleteFolderAccess().getAsKeyword_1());
            		
            // InternalDsl.g:972:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:973:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:973:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:974:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_25); 

            					newLeafNode(lv_name_2_0, grammarAccess.getDeleteFolderAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getDeleteFolderRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,33,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getDeleteFolderAccess().getFolderKeyword_3());
            		
            // InternalDsl.g:994:3: ( (lv_foldersource_4_0= RULE_STRING ) )
            // InternalDsl.g:995:4: (lv_foldersource_4_0= RULE_STRING )
            {
            // InternalDsl.g:995:4: (lv_foldersource_4_0= RULE_STRING )
            // InternalDsl.g:996:5: lv_foldersource_4_0= RULE_STRING
            {
            lv_foldersource_4_0=(Token)match(input,RULE_STRING,FOLLOW_26); 

            					newLeafNode(lv_foldersource_4_0, grammarAccess.getDeleteFolderAccess().getFoldersourceSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getDeleteFolderRule());
            					}
            					setWithLastConsumed(
            						current,
            						"foldersource",
            						lv_foldersource_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,34,FOLLOW_3); 

            			newLeafNode(otherlv_5, grammarAccess.getDeleteFolderAccess().getZipfileKeyword_5());
            		
            // InternalDsl.g:1016:3: ( (lv_zipsource_6_0= RULE_STRING ) )
            // InternalDsl.g:1017:4: (lv_zipsource_6_0= RULE_STRING )
            {
            // InternalDsl.g:1017:4: (lv_zipsource_6_0= RULE_STRING )
            // InternalDsl.g:1018:5: lv_zipsource_6_0= RULE_STRING
            {
            lv_zipsource_6_0=(Token)match(input,RULE_STRING,FOLLOW_4); 

            					newLeafNode(lv_zipsource_6_0, grammarAccess.getDeleteFolderAccess().getZipsourceSTRINGTerminalRuleCall_6_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getDeleteFolderRule());
            					}
            					setWithLastConsumed(
            						current,
            						"zipsource",
            						lv_zipsource_6_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_7=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_7, grammarAccess.getDeleteFolderAccess().getLeftCurlyBracketKeyword_7());
            		
            // InternalDsl.g:1038:3: ( (lv_value_8_0= ruleSelectStatement ) )
            // InternalDsl.g:1039:4: (lv_value_8_0= ruleSelectStatement )
            {
            // InternalDsl.g:1039:4: (lv_value_8_0= ruleSelectStatement )
            // InternalDsl.g:1040:5: lv_value_8_0= ruleSelectStatement
            {

            					newCompositeNode(grammarAccess.getDeleteFolderAccess().getValueSelectStatementParserRuleCall_8_0());
            				
            pushFollow(FOLLOW_8);
            lv_value_8_0=ruleSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getDeleteFolderRule());
            					}
            					set(
            						current,
            						"value",
            						lv_value_8_0,
            						"in.handyman.Dsl.SelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_9=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_9, grammarAccess.getDeleteFolderAccess().getRightCurlyBracketKeyword_9());
            		
            otherlv_10=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_10, grammarAccess.getDeleteFolderAccess().getOnConditionKeyword_10());
            		
            // InternalDsl.g:1065:3: ( (lv_condition_11_0= ruleExpression ) )
            // InternalDsl.g:1066:4: (lv_condition_11_0= ruleExpression )
            {
            // InternalDsl.g:1066:4: (lv_condition_11_0= ruleExpression )
            // InternalDsl.g:1067:5: lv_condition_11_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getDeleteFolderAccess().getConditionExpressionParserRuleCall_11_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_11_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getDeleteFolderRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_11_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleDeleteFolder"


    // $ANTLR start "entryRuleTerminal"
    // InternalDsl.g:1088:1: entryRuleTerminal returns [EObject current=null] : iv_ruleTerminal= ruleTerminal EOF ;
    public final EObject entryRuleTerminal() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTerminal = null;


        try {
            // InternalDsl.g:1088:49: (iv_ruleTerminal= ruleTerminal EOF )
            // InternalDsl.g:1089:2: iv_ruleTerminal= ruleTerminal EOF
            {
             newCompositeNode(grammarAccess.getTerminalRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleTerminal=ruleTerminal();

            state._fsp--;

             current =iv_ruleTerminal; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleTerminal"


    // $ANTLR start "ruleTerminal"
    // InternalDsl.g:1095:1: ruleTerminal returns [EObject current=null] : (otherlv_0= 'terminal' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'id' ( (lv_id_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= RULE_STRING ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) ) ;
    public final EObject ruleTerminal() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_id_4_0=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        Token lv_value_7_0=null;
        Token otherlv_8=null;
        Token otherlv_9=null;
        EObject lv_condition_10_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:1101:2: ( (otherlv_0= 'terminal' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'id' ( (lv_id_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= RULE_STRING ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) ) )
            // InternalDsl.g:1102:2: (otherlv_0= 'terminal' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'id' ( (lv_id_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= RULE_STRING ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) )
            {
            // InternalDsl.g:1102:2: (otherlv_0= 'terminal' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'id' ( (lv_id_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= RULE_STRING ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) )
            // InternalDsl.g:1103:3: otherlv_0= 'terminal' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'id' ( (lv_id_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= RULE_STRING ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,35,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getTerminalAccess().getTerminalKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getTerminalAccess().getAsKeyword_1());
            		
            // InternalDsl.g:1111:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:1112:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:1112:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:1113:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_27); 

            					newLeafNode(lv_name_2_0, grammarAccess.getTerminalAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getTerminalRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,36,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getTerminalAccess().getIdKeyword_3());
            		
            // InternalDsl.g:1133:3: ( (lv_id_4_0= RULE_STRING ) )
            // InternalDsl.g:1134:4: (lv_id_4_0= RULE_STRING )
            {
            // InternalDsl.g:1134:4: (lv_id_4_0= RULE_STRING )
            // InternalDsl.g:1135:5: lv_id_4_0= RULE_STRING
            {
            lv_id_4_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_id_4_0, grammarAccess.getTerminalAccess().getIdSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getTerminalRule());
            					}
            					setWithLastConsumed(
            						current,
            						"id",
            						lv_id_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,30,FOLLOW_4); 

            			newLeafNode(otherlv_5, grammarAccess.getTerminalAccess().getUsingKeyword_5());
            		
            otherlv_6=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_6, grammarAccess.getTerminalAccess().getLeftCurlyBracketKeyword_6());
            		
            // InternalDsl.g:1159:3: ( (lv_value_7_0= RULE_STRING ) )
            // InternalDsl.g:1160:4: (lv_value_7_0= RULE_STRING )
            {
            // InternalDsl.g:1160:4: (lv_value_7_0= RULE_STRING )
            // InternalDsl.g:1161:5: lv_value_7_0= RULE_STRING
            {
            lv_value_7_0=(Token)match(input,RULE_STRING,FOLLOW_8); 

            					newLeafNode(lv_value_7_0, grammarAccess.getTerminalAccess().getValueSTRINGTerminalRuleCall_7_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getTerminalRule());
            					}
            					setWithLastConsumed(
            						current,
            						"value",
            						lv_value_7_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_8=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_8, grammarAccess.getTerminalAccess().getRightCurlyBracketKeyword_8());
            		
            otherlv_9=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_9, grammarAccess.getTerminalAccess().getOnConditionKeyword_9());
            		
            // InternalDsl.g:1185:3: ( (lv_condition_10_0= ruleExpression ) )
            // InternalDsl.g:1186:4: (lv_condition_10_0= ruleExpression )
            {
            // InternalDsl.g:1186:4: (lv_condition_10_0= ruleExpression )
            // InternalDsl.g:1187:5: lv_condition_10_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getTerminalAccess().getConditionExpressionParserRuleCall_10_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_10_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getTerminalRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_10_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleTerminal"


    // $ANTLR start "entryRuleExecJava"
    // InternalDsl.g:1208:1: entryRuleExecJava returns [EObject current=null] : iv_ruleExecJava= ruleExecJava EOF ;
    public final EObject entryRuleExecJava() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExecJava = null;


        try {
            // InternalDsl.g:1208:49: (iv_ruleExecJava= ruleExecJava EOF )
            // InternalDsl.g:1209:2: iv_ruleExecJava= ruleExecJava EOF
            {
             newCompositeNode(grammarAccess.getExecJavaRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleExecJava=ruleExecJava();

            state._fsp--;

             current =iv_ruleExecJava; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleExecJava"


    // $ANTLR start "ruleExecJava"
    // InternalDsl.g:1215:1: ruleExecJava returns [EObject current=null] : (otherlv_0= 'java' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'using' ( (lv_classFqn_4_0= RULE_STRING ) ) otherlv_5= 'name-sake-db' ( (lv_dbSrc_6_0= RULE_STRING ) ) otherlv_7= '{' ( (lv_value_8_0= RULE_STRING ) ) otherlv_9= '}' otherlv_10= 'on-condition' ( (lv_condition_11_0= ruleExpression ) ) ) ;
    public final EObject ruleExecJava() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_classFqn_4_0=null;
        Token otherlv_5=null;
        Token lv_dbSrc_6_0=null;
        Token otherlv_7=null;
        Token lv_value_8_0=null;
        Token otherlv_9=null;
        Token otherlv_10=null;
        EObject lv_condition_11_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:1221:2: ( (otherlv_0= 'java' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'using' ( (lv_classFqn_4_0= RULE_STRING ) ) otherlv_5= 'name-sake-db' ( (lv_dbSrc_6_0= RULE_STRING ) ) otherlv_7= '{' ( (lv_value_8_0= RULE_STRING ) ) otherlv_9= '}' otherlv_10= 'on-condition' ( (lv_condition_11_0= ruleExpression ) ) ) )
            // InternalDsl.g:1222:2: (otherlv_0= 'java' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'using' ( (lv_classFqn_4_0= RULE_STRING ) ) otherlv_5= 'name-sake-db' ( (lv_dbSrc_6_0= RULE_STRING ) ) otherlv_7= '{' ( (lv_value_8_0= RULE_STRING ) ) otherlv_9= '}' otherlv_10= 'on-condition' ( (lv_condition_11_0= ruleExpression ) ) )
            {
            // InternalDsl.g:1222:2: (otherlv_0= 'java' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'using' ( (lv_classFqn_4_0= RULE_STRING ) ) otherlv_5= 'name-sake-db' ( (lv_dbSrc_6_0= RULE_STRING ) ) otherlv_7= '{' ( (lv_value_8_0= RULE_STRING ) ) otherlv_9= '}' otherlv_10= 'on-condition' ( (lv_condition_11_0= ruleExpression ) ) )
            // InternalDsl.g:1223:3: otherlv_0= 'java' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'using' ( (lv_classFqn_4_0= RULE_STRING ) ) otherlv_5= 'name-sake-db' ( (lv_dbSrc_6_0= RULE_STRING ) ) otherlv_7= '{' ( (lv_value_8_0= RULE_STRING ) ) otherlv_9= '}' otherlv_10= 'on-condition' ( (lv_condition_11_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,37,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getExecJavaAccess().getJavaKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getExecJavaAccess().getAsKeyword_1());
            		
            // InternalDsl.g:1231:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:1232:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:1232:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:1233:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_name_2_0, grammarAccess.getExecJavaAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getExecJavaRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,30,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getExecJavaAccess().getUsingKeyword_3());
            		
            // InternalDsl.g:1253:3: ( (lv_classFqn_4_0= RULE_STRING ) )
            // InternalDsl.g:1254:4: (lv_classFqn_4_0= RULE_STRING )
            {
            // InternalDsl.g:1254:4: (lv_classFqn_4_0= RULE_STRING )
            // InternalDsl.g:1255:5: lv_classFqn_4_0= RULE_STRING
            {
            lv_classFqn_4_0=(Token)match(input,RULE_STRING,FOLLOW_28); 

            					newLeafNode(lv_classFqn_4_0, grammarAccess.getExecJavaAccess().getClassFqnSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getExecJavaRule());
            					}
            					setWithLastConsumed(
            						current,
            						"classFqn",
            						lv_classFqn_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,38,FOLLOW_3); 

            			newLeafNode(otherlv_5, grammarAccess.getExecJavaAccess().getNameSakeDbKeyword_5());
            		
            // InternalDsl.g:1275:3: ( (lv_dbSrc_6_0= RULE_STRING ) )
            // InternalDsl.g:1276:4: (lv_dbSrc_6_0= RULE_STRING )
            {
            // InternalDsl.g:1276:4: (lv_dbSrc_6_0= RULE_STRING )
            // InternalDsl.g:1277:5: lv_dbSrc_6_0= RULE_STRING
            {
            lv_dbSrc_6_0=(Token)match(input,RULE_STRING,FOLLOW_4); 

            					newLeafNode(lv_dbSrc_6_0, grammarAccess.getExecJavaAccess().getDbSrcSTRINGTerminalRuleCall_6_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getExecJavaRule());
            					}
            					setWithLastConsumed(
            						current,
            						"dbSrc",
            						lv_dbSrc_6_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_7=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_7, grammarAccess.getExecJavaAccess().getLeftCurlyBracketKeyword_7());
            		
            // InternalDsl.g:1297:3: ( (lv_value_8_0= RULE_STRING ) )
            // InternalDsl.g:1298:4: (lv_value_8_0= RULE_STRING )
            {
            // InternalDsl.g:1298:4: (lv_value_8_0= RULE_STRING )
            // InternalDsl.g:1299:5: lv_value_8_0= RULE_STRING
            {
            lv_value_8_0=(Token)match(input,RULE_STRING,FOLLOW_8); 

            					newLeafNode(lv_value_8_0, grammarAccess.getExecJavaAccess().getValueSTRINGTerminalRuleCall_8_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getExecJavaRule());
            					}
            					setWithLastConsumed(
            						current,
            						"value",
            						lv_value_8_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_9=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_9, grammarAccess.getExecJavaAccess().getRightCurlyBracketKeyword_9());
            		
            otherlv_10=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_10, grammarAccess.getExecJavaAccess().getOnConditionKeyword_10());
            		
            // InternalDsl.g:1323:3: ( (lv_condition_11_0= ruleExpression ) )
            // InternalDsl.g:1324:4: (lv_condition_11_0= ruleExpression )
            {
            // InternalDsl.g:1324:4: (lv_condition_11_0= ruleExpression )
            // InternalDsl.g:1325:5: lv_condition_11_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getExecJavaAccess().getConditionExpressionParserRuleCall_11_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_11_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getExecJavaRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_11_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleExecJava"


    // $ANTLR start "entryRuleAbort"
    // InternalDsl.g:1346:1: entryRuleAbort returns [EObject current=null] : iv_ruleAbort= ruleAbort EOF ;
    public final EObject entryRuleAbort() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleAbort = null;


        try {
            // InternalDsl.g:1346:46: (iv_ruleAbort= ruleAbort EOF )
            // InternalDsl.g:1347:2: iv_ruleAbort= ruleAbort EOF
            {
             newCompositeNode(grammarAccess.getAbortRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleAbort=ruleAbort();

            state._fsp--;

             current =iv_ruleAbort; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleAbort"


    // $ANTLR start "ruleAbort"
    // InternalDsl.g:1353:1: ruleAbort returns [EObject current=null] : (otherlv_0= 'abort' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= '{' ( (lv_value_4_0= RULE_STRING ) ) otherlv_5= '}' otherlv_6= 'on-condition' ( (lv_condition_7_0= ruleExpression ) ) ) ;
    public final EObject ruleAbort() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_value_4_0=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        EObject lv_condition_7_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:1359:2: ( (otherlv_0= 'abort' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= '{' ( (lv_value_4_0= RULE_STRING ) ) otherlv_5= '}' otherlv_6= 'on-condition' ( (lv_condition_7_0= ruleExpression ) ) ) )
            // InternalDsl.g:1360:2: (otherlv_0= 'abort' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= '{' ( (lv_value_4_0= RULE_STRING ) ) otherlv_5= '}' otherlv_6= 'on-condition' ( (lv_condition_7_0= ruleExpression ) ) )
            {
            // InternalDsl.g:1360:2: (otherlv_0= 'abort' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= '{' ( (lv_value_4_0= RULE_STRING ) ) otherlv_5= '}' otherlv_6= 'on-condition' ( (lv_condition_7_0= ruleExpression ) ) )
            // InternalDsl.g:1361:3: otherlv_0= 'abort' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= '{' ( (lv_value_4_0= RULE_STRING ) ) otherlv_5= '}' otherlv_6= 'on-condition' ( (lv_condition_7_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,39,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getAbortAccess().getAbortKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getAbortAccess().getAsKeyword_1());
            		
            // InternalDsl.g:1369:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:1370:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:1370:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:1371:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_4); 

            					newLeafNode(lv_name_2_0, grammarAccess.getAbortAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getAbortRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getAbortAccess().getLeftCurlyBracketKeyword_3());
            		
            // InternalDsl.g:1391:3: ( (lv_value_4_0= RULE_STRING ) )
            // InternalDsl.g:1392:4: (lv_value_4_0= RULE_STRING )
            {
            // InternalDsl.g:1392:4: (lv_value_4_0= RULE_STRING )
            // InternalDsl.g:1393:5: lv_value_4_0= RULE_STRING
            {
            lv_value_4_0=(Token)match(input,RULE_STRING,FOLLOW_8); 

            					newLeafNode(lv_value_4_0, grammarAccess.getAbortAccess().getValueSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getAbortRule());
            					}
            					setWithLastConsumed(
            						current,
            						"value",
            						lv_value_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_5, grammarAccess.getAbortAccess().getRightCurlyBracketKeyword_5());
            		
            otherlv_6=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_6, grammarAccess.getAbortAccess().getOnConditionKeyword_6());
            		
            // InternalDsl.g:1417:3: ( (lv_condition_7_0= ruleExpression ) )
            // InternalDsl.g:1418:4: (lv_condition_7_0= ruleExpression )
            {
            // InternalDsl.g:1418:4: (lv_condition_7_0= ruleExpression )
            // InternalDsl.g:1419:5: lv_condition_7_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getAbortAccess().getConditionExpressionParserRuleCall_7_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_7_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getAbortRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_7_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleAbort"


    // $ANTLR start "entryRuleDropfile"
    // InternalDsl.g:1440:1: entryRuleDropfile returns [EObject current=null] : iv_ruleDropfile= ruleDropfile EOF ;
    public final EObject entryRuleDropfile() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleDropfile = null;


        try {
            // InternalDsl.g:1440:49: (iv_ruleDropfile= ruleDropfile EOF )
            // InternalDsl.g:1441:2: iv_ruleDropfile= ruleDropfile EOF
            {
             newCompositeNode(grammarAccess.getDropfileRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleDropfile=ruleDropfile();

            state._fsp--;

             current =iv_ruleDropfile; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleDropfile"


    // $ANTLR start "ruleDropfile"
    // InternalDsl.g:1447:1: ruleDropfile returns [EObject current=null] : (otherlv_0= 'dropfile' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'in-path' ( (lv_target_4_0= RULE_STRING ) ) otherlv_5= 'on-condition' ( (lv_condition_6_0= ruleExpression ) ) ) ;
    public final EObject ruleDropfile() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_target_4_0=null;
        Token otherlv_5=null;
        EObject lv_condition_6_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:1453:2: ( (otherlv_0= 'dropfile' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'in-path' ( (lv_target_4_0= RULE_STRING ) ) otherlv_5= 'on-condition' ( (lv_condition_6_0= ruleExpression ) ) ) )
            // InternalDsl.g:1454:2: (otherlv_0= 'dropfile' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'in-path' ( (lv_target_4_0= RULE_STRING ) ) otherlv_5= 'on-condition' ( (lv_condition_6_0= ruleExpression ) ) )
            {
            // InternalDsl.g:1454:2: (otherlv_0= 'dropfile' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'in-path' ( (lv_target_4_0= RULE_STRING ) ) otherlv_5= 'on-condition' ( (lv_condition_6_0= ruleExpression ) ) )
            // InternalDsl.g:1455:3: otherlv_0= 'dropfile' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'in-path' ( (lv_target_4_0= RULE_STRING ) ) otherlv_5= 'on-condition' ( (lv_condition_6_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,40,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getDropfileAccess().getDropfileKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getDropfileAccess().getAsKeyword_1());
            		
            // InternalDsl.g:1463:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:1464:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:1464:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:1465:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_29); 

            					newLeafNode(lv_name_2_0, grammarAccess.getDropfileAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getDropfileRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,41,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getDropfileAccess().getInPathKeyword_3());
            		
            // InternalDsl.g:1485:3: ( (lv_target_4_0= RULE_STRING ) )
            // InternalDsl.g:1486:4: (lv_target_4_0= RULE_STRING )
            {
            // InternalDsl.g:1486:4: (lv_target_4_0= RULE_STRING )
            // InternalDsl.g:1487:5: lv_target_4_0= RULE_STRING
            {
            lv_target_4_0=(Token)match(input,RULE_STRING,FOLLOW_23); 

            					newLeafNode(lv_target_4_0, grammarAccess.getDropfileAccess().getTargetSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getDropfileRule());
            					}
            					setWithLastConsumed(
            						current,
            						"target",
            						lv_target_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_5, grammarAccess.getDropfileAccess().getOnConditionKeyword_5());
            		
            // InternalDsl.g:1507:3: ( (lv_condition_6_0= ruleExpression ) )
            // InternalDsl.g:1508:4: (lv_condition_6_0= ruleExpression )
            {
            // InternalDsl.g:1508:4: (lv_condition_6_0= ruleExpression )
            // InternalDsl.g:1509:5: lv_condition_6_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getDropfileAccess().getConditionExpressionParserRuleCall_6_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_6_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getDropfileRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_6_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleDropfile"


    // $ANTLR start "entryRuleDoozle"
    // InternalDsl.g:1530:1: entryRuleDoozle returns [EObject current=null] : iv_ruleDoozle= ruleDoozle EOF ;
    public final EObject entryRuleDoozle() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleDoozle = null;


        try {
            // InternalDsl.g:1530:47: (iv_ruleDoozle= ruleDoozle EOF )
            // InternalDsl.g:1531:2: iv_ruleDoozle= ruleDoozle EOF
            {
             newCompositeNode(grammarAccess.getDoozleRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleDoozle=ruleDoozle();

            state._fsp--;

             current =iv_ruleDoozle; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleDoozle"


    // $ANTLR start "ruleDoozle"
    // InternalDsl.g:1537:1: ruleDoozle returns [EObject current=null] : (otherlv_0= 'doozle' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'in-table' ( (lv_target_4_0= RULE_STRING ) ) otherlv_5= 'on' ( (lv_on_6_0= RULE_STRING ) ) otherlv_7= 'using' otherlv_8= '{' ( (lv_value_9_0= ruleCreateStatement ) ) otherlv_10= '}' otherlv_11= 'on-condition' ( (lv_condition_12_0= ruleExpression ) ) ) ;
    public final EObject ruleDoozle() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_target_4_0=null;
        Token otherlv_5=null;
        Token lv_on_6_0=null;
        Token otherlv_7=null;
        Token otherlv_8=null;
        Token otherlv_10=null;
        Token otherlv_11=null;
        AntlrDatatypeRuleToken lv_value_9_0 = null;

        EObject lv_condition_12_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:1543:2: ( (otherlv_0= 'doozle' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'in-table' ( (lv_target_4_0= RULE_STRING ) ) otherlv_5= 'on' ( (lv_on_6_0= RULE_STRING ) ) otherlv_7= 'using' otherlv_8= '{' ( (lv_value_9_0= ruleCreateStatement ) ) otherlv_10= '}' otherlv_11= 'on-condition' ( (lv_condition_12_0= ruleExpression ) ) ) )
            // InternalDsl.g:1544:2: (otherlv_0= 'doozle' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'in-table' ( (lv_target_4_0= RULE_STRING ) ) otherlv_5= 'on' ( (lv_on_6_0= RULE_STRING ) ) otherlv_7= 'using' otherlv_8= '{' ( (lv_value_9_0= ruleCreateStatement ) ) otherlv_10= '}' otherlv_11= 'on-condition' ( (lv_condition_12_0= ruleExpression ) ) )
            {
            // InternalDsl.g:1544:2: (otherlv_0= 'doozle' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'in-table' ( (lv_target_4_0= RULE_STRING ) ) otherlv_5= 'on' ( (lv_on_6_0= RULE_STRING ) ) otherlv_7= 'using' otherlv_8= '{' ( (lv_value_9_0= ruleCreateStatement ) ) otherlv_10= '}' otherlv_11= 'on-condition' ( (lv_condition_12_0= ruleExpression ) ) )
            // InternalDsl.g:1545:3: otherlv_0= 'doozle' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'in-table' ( (lv_target_4_0= RULE_STRING ) ) otherlv_5= 'on' ( (lv_on_6_0= RULE_STRING ) ) otherlv_7= 'using' otherlv_8= '{' ( (lv_value_9_0= ruleCreateStatement ) ) otherlv_10= '}' otherlv_11= 'on-condition' ( (lv_condition_12_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,42,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getDoozleAccess().getDoozleKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getDoozleAccess().getAsKeyword_1());
            		
            // InternalDsl.g:1553:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:1554:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:1554:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:1555:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_30); 

            					newLeafNode(lv_name_2_0, grammarAccess.getDoozleAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getDoozleRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,43,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getDoozleAccess().getInTableKeyword_3());
            		
            // InternalDsl.g:1575:3: ( (lv_target_4_0= RULE_STRING ) )
            // InternalDsl.g:1576:4: (lv_target_4_0= RULE_STRING )
            {
            // InternalDsl.g:1576:4: (lv_target_4_0= RULE_STRING )
            // InternalDsl.g:1577:5: lv_target_4_0= RULE_STRING
            {
            lv_target_4_0=(Token)match(input,RULE_STRING,FOLLOW_31); 

            					newLeafNode(lv_target_4_0, grammarAccess.getDoozleAccess().getTargetSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getDoozleRule());
            					}
            					setWithLastConsumed(
            						current,
            						"target",
            						lv_target_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,44,FOLLOW_3); 

            			newLeafNode(otherlv_5, grammarAccess.getDoozleAccess().getOnKeyword_5());
            		
            // InternalDsl.g:1597:3: ( (lv_on_6_0= RULE_STRING ) )
            // InternalDsl.g:1598:4: (lv_on_6_0= RULE_STRING )
            {
            // InternalDsl.g:1598:4: (lv_on_6_0= RULE_STRING )
            // InternalDsl.g:1599:5: lv_on_6_0= RULE_STRING
            {
            lv_on_6_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_on_6_0, grammarAccess.getDoozleAccess().getOnSTRINGTerminalRuleCall_6_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getDoozleRule());
            					}
            					setWithLastConsumed(
            						current,
            						"on",
            						lv_on_6_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_7=(Token)match(input,30,FOLLOW_4); 

            			newLeafNode(otherlv_7, grammarAccess.getDoozleAccess().getUsingKeyword_7());
            		
            otherlv_8=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_8, grammarAccess.getDoozleAccess().getLeftCurlyBracketKeyword_8());
            		
            // InternalDsl.g:1623:3: ( (lv_value_9_0= ruleCreateStatement ) )
            // InternalDsl.g:1624:4: (lv_value_9_0= ruleCreateStatement )
            {
            // InternalDsl.g:1624:4: (lv_value_9_0= ruleCreateStatement )
            // InternalDsl.g:1625:5: lv_value_9_0= ruleCreateStatement
            {

            					newCompositeNode(grammarAccess.getDoozleAccess().getValueCreateStatementParserRuleCall_9_0());
            				
            pushFollow(FOLLOW_8);
            lv_value_9_0=ruleCreateStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getDoozleRule());
            					}
            					set(
            						current,
            						"value",
            						lv_value_9_0,
            						"in.handyman.Dsl.CreateStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_10=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_10, grammarAccess.getDoozleAccess().getRightCurlyBracketKeyword_10());
            		
            otherlv_11=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_11, grammarAccess.getDoozleAccess().getOnConditionKeyword_11());
            		
            // InternalDsl.g:1650:3: ( (lv_condition_12_0= ruleExpression ) )
            // InternalDsl.g:1651:4: (lv_condition_12_0= ruleExpression )
            {
            // InternalDsl.g:1651:4: (lv_condition_12_0= ruleExpression )
            // InternalDsl.g:1652:5: lv_condition_12_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getDoozleAccess().getConditionExpressionParserRuleCall_12_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_12_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getDoozleRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_12_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleDoozle"


    // $ANTLR start "entryRuleFetch"
    // InternalDsl.g:1673:1: entryRuleFetch returns [EObject current=null] : iv_ruleFetch= ruleFetch EOF ;
    public final EObject entryRuleFetch() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFetch = null;


        try {
            // InternalDsl.g:1673:46: (iv_ruleFetch= ruleFetch EOF )
            // InternalDsl.g:1674:2: iv_ruleFetch= ruleFetch EOF
            {
             newCompositeNode(grammarAccess.getFetchRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleFetch=ruleFetch();

            state._fsp--;

             current =iv_ruleFetch; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleFetch"


    // $ANTLR start "ruleFetch"
    // InternalDsl.g:1680:1: ruleFetch returns [EObject current=null] : (otherlv_0= 'assign' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'source' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= RULE_STRING ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) ) ;
    public final EObject ruleFetch() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_source_4_0=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        Token lv_value_7_0=null;
        Token otherlv_8=null;
        Token otherlv_9=null;
        EObject lv_condition_10_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:1686:2: ( (otherlv_0= 'assign' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'source' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= RULE_STRING ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) ) )
            // InternalDsl.g:1687:2: (otherlv_0= 'assign' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'source' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= RULE_STRING ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) )
            {
            // InternalDsl.g:1687:2: (otherlv_0= 'assign' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'source' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= RULE_STRING ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) )
            // InternalDsl.g:1688:3: otherlv_0= 'assign' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'source' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= RULE_STRING ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,45,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getFetchAccess().getAssignKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getFetchAccess().getAsKeyword_1());
            		
            // InternalDsl.g:1696:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:1697:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:1697:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:1698:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_32); 

            					newLeafNode(lv_name_2_0, grammarAccess.getFetchAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getFetchRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,46,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getFetchAccess().getSourceKeyword_3());
            		
            // InternalDsl.g:1718:3: ( (lv_source_4_0= RULE_STRING ) )
            // InternalDsl.g:1719:4: (lv_source_4_0= RULE_STRING )
            {
            // InternalDsl.g:1719:4: (lv_source_4_0= RULE_STRING )
            // InternalDsl.g:1720:5: lv_source_4_0= RULE_STRING
            {
            lv_source_4_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_source_4_0, grammarAccess.getFetchAccess().getSourceSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getFetchRule());
            					}
            					setWithLastConsumed(
            						current,
            						"source",
            						lv_source_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,30,FOLLOW_4); 

            			newLeafNode(otherlv_5, grammarAccess.getFetchAccess().getUsingKeyword_5());
            		
            otherlv_6=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_6, grammarAccess.getFetchAccess().getLeftCurlyBracketKeyword_6());
            		
            // InternalDsl.g:1744:3: ( (lv_value_7_0= RULE_STRING ) )
            // InternalDsl.g:1745:4: (lv_value_7_0= RULE_STRING )
            {
            // InternalDsl.g:1745:4: (lv_value_7_0= RULE_STRING )
            // InternalDsl.g:1746:5: lv_value_7_0= RULE_STRING
            {
            lv_value_7_0=(Token)match(input,RULE_STRING,FOLLOW_8); 

            					newLeafNode(lv_value_7_0, grammarAccess.getFetchAccess().getValueSTRINGTerminalRuleCall_7_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getFetchRule());
            					}
            					setWithLastConsumed(
            						current,
            						"value",
            						lv_value_7_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_8=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_8, grammarAccess.getFetchAccess().getRightCurlyBracketKeyword_8());
            		
            otherlv_9=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_9, grammarAccess.getFetchAccess().getOnConditionKeyword_9());
            		
            // InternalDsl.g:1770:3: ( (lv_condition_10_0= ruleExpression ) )
            // InternalDsl.g:1771:4: (lv_condition_10_0= ruleExpression )
            {
            // InternalDsl.g:1771:4: (lv_condition_10_0= ruleExpression )
            // InternalDsl.g:1772:5: lv_condition_10_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getFetchAccess().getConditionExpressionParserRuleCall_10_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_10_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getFetchRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_10_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleFetch"


    // $ANTLR start "entryRuleCallprocess"
    // InternalDsl.g:1793:1: entryRuleCallprocess returns [EObject current=null] : iv_ruleCallprocess= ruleCallprocess EOF ;
    public final EObject entryRuleCallprocess() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleCallprocess = null;


        try {
            // InternalDsl.g:1793:52: (iv_ruleCallprocess= ruleCallprocess EOF )
            // InternalDsl.g:1794:2: iv_ruleCallprocess= ruleCallprocess EOF
            {
             newCompositeNode(grammarAccess.getCallprocessRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleCallprocess=ruleCallprocess();

            state._fsp--;

             current =iv_ruleCallprocess; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleCallprocess"


    // $ANTLR start "ruleCallprocess"
    // InternalDsl.g:1800:1: ruleCallprocess returns [EObject current=null] : (otherlv_0= 'callprocess' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'with-target' ( (lv_target_4_0= RULE_STRING ) ) otherlv_5= 'from-file' ( (lv_source_6_0= RULE_STRING ) ) otherlv_7= 'using' ( (lv_datasource_8_0= RULE_STRING ) ) otherlv_9= 'for-every' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) ) ) ;
    public final EObject ruleCallprocess() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_target_4_0=null;
        Token otherlv_5=null;
        Token lv_source_6_0=null;
        Token otherlv_7=null;
        Token lv_datasource_8_0=null;
        Token otherlv_9=null;
        Token otherlv_10=null;
        Token otherlv_12=null;
        Token otherlv_13=null;
        AntlrDatatypeRuleToken lv_value_11_0 = null;

        EObject lv_condition_14_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:1806:2: ( (otherlv_0= 'callprocess' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'with-target' ( (lv_target_4_0= RULE_STRING ) ) otherlv_5= 'from-file' ( (lv_source_6_0= RULE_STRING ) ) otherlv_7= 'using' ( (lv_datasource_8_0= RULE_STRING ) ) otherlv_9= 'for-every' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) ) ) )
            // InternalDsl.g:1807:2: (otherlv_0= 'callprocess' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'with-target' ( (lv_target_4_0= RULE_STRING ) ) otherlv_5= 'from-file' ( (lv_source_6_0= RULE_STRING ) ) otherlv_7= 'using' ( (lv_datasource_8_0= RULE_STRING ) ) otherlv_9= 'for-every' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) ) )
            {
            // InternalDsl.g:1807:2: (otherlv_0= 'callprocess' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'with-target' ( (lv_target_4_0= RULE_STRING ) ) otherlv_5= 'from-file' ( (lv_source_6_0= RULE_STRING ) ) otherlv_7= 'using' ( (lv_datasource_8_0= RULE_STRING ) ) otherlv_9= 'for-every' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) ) )
            // InternalDsl.g:1808:3: otherlv_0= 'callprocess' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'with-target' ( (lv_target_4_0= RULE_STRING ) ) otherlv_5= 'from-file' ( (lv_source_6_0= RULE_STRING ) ) otherlv_7= 'using' ( (lv_datasource_8_0= RULE_STRING ) ) otherlv_9= 'for-every' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,47,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getCallprocessAccess().getCallprocessKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getCallprocessAccess().getAsKeyword_1());
            		
            // InternalDsl.g:1816:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:1817:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:1817:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:1818:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_33); 

            					newLeafNode(lv_name_2_0, grammarAccess.getCallprocessAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getCallprocessRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,48,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getCallprocessAccess().getWithTargetKeyword_3());
            		
            // InternalDsl.g:1838:3: ( (lv_target_4_0= RULE_STRING ) )
            // InternalDsl.g:1839:4: (lv_target_4_0= RULE_STRING )
            {
            // InternalDsl.g:1839:4: (lv_target_4_0= RULE_STRING )
            // InternalDsl.g:1840:5: lv_target_4_0= RULE_STRING
            {
            lv_target_4_0=(Token)match(input,RULE_STRING,FOLLOW_34); 

            					newLeafNode(lv_target_4_0, grammarAccess.getCallprocessAccess().getTargetSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getCallprocessRule());
            					}
            					setWithLastConsumed(
            						current,
            						"target",
            						lv_target_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,49,FOLLOW_3); 

            			newLeafNode(otherlv_5, grammarAccess.getCallprocessAccess().getFromFileKeyword_5());
            		
            // InternalDsl.g:1860:3: ( (lv_source_6_0= RULE_STRING ) )
            // InternalDsl.g:1861:4: (lv_source_6_0= RULE_STRING )
            {
            // InternalDsl.g:1861:4: (lv_source_6_0= RULE_STRING )
            // InternalDsl.g:1862:5: lv_source_6_0= RULE_STRING
            {
            lv_source_6_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_source_6_0, grammarAccess.getCallprocessAccess().getSourceSTRINGTerminalRuleCall_6_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getCallprocessRule());
            					}
            					setWithLastConsumed(
            						current,
            						"source",
            						lv_source_6_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_7=(Token)match(input,30,FOLLOW_3); 

            			newLeafNode(otherlv_7, grammarAccess.getCallprocessAccess().getUsingKeyword_7());
            		
            // InternalDsl.g:1882:3: ( (lv_datasource_8_0= RULE_STRING ) )
            // InternalDsl.g:1883:4: (lv_datasource_8_0= RULE_STRING )
            {
            // InternalDsl.g:1883:4: (lv_datasource_8_0= RULE_STRING )
            // InternalDsl.g:1884:5: lv_datasource_8_0= RULE_STRING
            {
            lv_datasource_8_0=(Token)match(input,RULE_STRING,FOLLOW_35); 

            					newLeafNode(lv_datasource_8_0, grammarAccess.getCallprocessAccess().getDatasourceSTRINGTerminalRuleCall_8_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getCallprocessRule());
            					}
            					setWithLastConsumed(
            						current,
            						"datasource",
            						lv_datasource_8_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_9=(Token)match(input,50,FOLLOW_4); 

            			newLeafNode(otherlv_9, grammarAccess.getCallprocessAccess().getForEveryKeyword_9());
            		
            otherlv_10=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_10, grammarAccess.getCallprocessAccess().getLeftCurlyBracketKeyword_10());
            		
            // InternalDsl.g:1908:3: ( (lv_value_11_0= ruleSelectStatement ) )
            // InternalDsl.g:1909:4: (lv_value_11_0= ruleSelectStatement )
            {
            // InternalDsl.g:1909:4: (lv_value_11_0= ruleSelectStatement )
            // InternalDsl.g:1910:5: lv_value_11_0= ruleSelectStatement
            {

            					newCompositeNode(grammarAccess.getCallprocessAccess().getValueSelectStatementParserRuleCall_11_0());
            				
            pushFollow(FOLLOW_8);
            lv_value_11_0=ruleSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getCallprocessRule());
            					}
            					set(
            						current,
            						"value",
            						lv_value_11_0,
            						"in.handyman.Dsl.SelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_12=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_12, grammarAccess.getCallprocessAccess().getRightCurlyBracketKeyword_12());
            		
            otherlv_13=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_13, grammarAccess.getCallprocessAccess().getOnConditionKeyword_13());
            		
            // InternalDsl.g:1935:3: ( (lv_condition_14_0= ruleExpression ) )
            // InternalDsl.g:1936:4: (lv_condition_14_0= ruleExpression )
            {
            // InternalDsl.g:1936:4: (lv_condition_14_0= ruleExpression )
            // InternalDsl.g:1937:5: lv_condition_14_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getCallprocessAccess().getConditionExpressionParserRuleCall_14_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_14_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getCallprocessRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_14_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleCallprocess"


    // $ANTLR start "entryRuleForkprocess"
    // InternalDsl.g:1958:1: entryRuleForkprocess returns [EObject current=null] : iv_ruleForkprocess= ruleForkprocess EOF ;
    public final EObject entryRuleForkprocess() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleForkprocess = null;


        try {
            // InternalDsl.g:1958:52: (iv_ruleForkprocess= ruleForkprocess EOF )
            // InternalDsl.g:1959:2: iv_ruleForkprocess= ruleForkprocess EOF
            {
             newCompositeNode(grammarAccess.getForkprocessRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleForkprocess=ruleForkprocess();

            state._fsp--;

             current =iv_ruleForkprocess; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleForkprocess"


    // $ANTLR start "ruleForkprocess"
    // InternalDsl.g:1965:1: ruleForkprocess returns [EObject current=null] : (otherlv_0= 'forkprocess' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'with-target' ( (lv_target_4_0= RULE_STRING ) ) otherlv_5= 'from-file' ( (lv_source_6_0= RULE_STRING ) ) otherlv_7= 'using' ( (lv_datasource_8_0= RULE_STRING ) ) otherlv_9= 'for-every' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'watermark' ( (lv_forkBatchSize_14_0= RULE_STRING ) ) otherlv_15= 'on-condition' ( (lv_condition_16_0= ruleExpression ) ) ) ;
    public final EObject ruleForkprocess() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_target_4_0=null;
        Token otherlv_5=null;
        Token lv_source_6_0=null;
        Token otherlv_7=null;
        Token lv_datasource_8_0=null;
        Token otherlv_9=null;
        Token otherlv_10=null;
        Token otherlv_12=null;
        Token otherlv_13=null;
        Token lv_forkBatchSize_14_0=null;
        Token otherlv_15=null;
        AntlrDatatypeRuleToken lv_value_11_0 = null;

        EObject lv_condition_16_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:1971:2: ( (otherlv_0= 'forkprocess' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'with-target' ( (lv_target_4_0= RULE_STRING ) ) otherlv_5= 'from-file' ( (lv_source_6_0= RULE_STRING ) ) otherlv_7= 'using' ( (lv_datasource_8_0= RULE_STRING ) ) otherlv_9= 'for-every' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'watermark' ( (lv_forkBatchSize_14_0= RULE_STRING ) ) otherlv_15= 'on-condition' ( (lv_condition_16_0= ruleExpression ) ) ) )
            // InternalDsl.g:1972:2: (otherlv_0= 'forkprocess' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'with-target' ( (lv_target_4_0= RULE_STRING ) ) otherlv_5= 'from-file' ( (lv_source_6_0= RULE_STRING ) ) otherlv_7= 'using' ( (lv_datasource_8_0= RULE_STRING ) ) otherlv_9= 'for-every' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'watermark' ( (lv_forkBatchSize_14_0= RULE_STRING ) ) otherlv_15= 'on-condition' ( (lv_condition_16_0= ruleExpression ) ) )
            {
            // InternalDsl.g:1972:2: (otherlv_0= 'forkprocess' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'with-target' ( (lv_target_4_0= RULE_STRING ) ) otherlv_5= 'from-file' ( (lv_source_6_0= RULE_STRING ) ) otherlv_7= 'using' ( (lv_datasource_8_0= RULE_STRING ) ) otherlv_9= 'for-every' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'watermark' ( (lv_forkBatchSize_14_0= RULE_STRING ) ) otherlv_15= 'on-condition' ( (lv_condition_16_0= ruleExpression ) ) )
            // InternalDsl.g:1973:3: otherlv_0= 'forkprocess' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'with-target' ( (lv_target_4_0= RULE_STRING ) ) otherlv_5= 'from-file' ( (lv_source_6_0= RULE_STRING ) ) otherlv_7= 'using' ( (lv_datasource_8_0= RULE_STRING ) ) otherlv_9= 'for-every' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'watermark' ( (lv_forkBatchSize_14_0= RULE_STRING ) ) otherlv_15= 'on-condition' ( (lv_condition_16_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,51,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getForkprocessAccess().getForkprocessKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getForkprocessAccess().getAsKeyword_1());
            		
            // InternalDsl.g:1981:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:1982:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:1982:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:1983:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_33); 

            					newLeafNode(lv_name_2_0, grammarAccess.getForkprocessAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getForkprocessRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,48,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getForkprocessAccess().getWithTargetKeyword_3());
            		
            // InternalDsl.g:2003:3: ( (lv_target_4_0= RULE_STRING ) )
            // InternalDsl.g:2004:4: (lv_target_4_0= RULE_STRING )
            {
            // InternalDsl.g:2004:4: (lv_target_4_0= RULE_STRING )
            // InternalDsl.g:2005:5: lv_target_4_0= RULE_STRING
            {
            lv_target_4_0=(Token)match(input,RULE_STRING,FOLLOW_34); 

            					newLeafNode(lv_target_4_0, grammarAccess.getForkprocessAccess().getTargetSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getForkprocessRule());
            					}
            					setWithLastConsumed(
            						current,
            						"target",
            						lv_target_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,49,FOLLOW_3); 

            			newLeafNode(otherlv_5, grammarAccess.getForkprocessAccess().getFromFileKeyword_5());
            		
            // InternalDsl.g:2025:3: ( (lv_source_6_0= RULE_STRING ) )
            // InternalDsl.g:2026:4: (lv_source_6_0= RULE_STRING )
            {
            // InternalDsl.g:2026:4: (lv_source_6_0= RULE_STRING )
            // InternalDsl.g:2027:5: lv_source_6_0= RULE_STRING
            {
            lv_source_6_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_source_6_0, grammarAccess.getForkprocessAccess().getSourceSTRINGTerminalRuleCall_6_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getForkprocessRule());
            					}
            					setWithLastConsumed(
            						current,
            						"source",
            						lv_source_6_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_7=(Token)match(input,30,FOLLOW_3); 

            			newLeafNode(otherlv_7, grammarAccess.getForkprocessAccess().getUsingKeyword_7());
            		
            // InternalDsl.g:2047:3: ( (lv_datasource_8_0= RULE_STRING ) )
            // InternalDsl.g:2048:4: (lv_datasource_8_0= RULE_STRING )
            {
            // InternalDsl.g:2048:4: (lv_datasource_8_0= RULE_STRING )
            // InternalDsl.g:2049:5: lv_datasource_8_0= RULE_STRING
            {
            lv_datasource_8_0=(Token)match(input,RULE_STRING,FOLLOW_35); 

            					newLeafNode(lv_datasource_8_0, grammarAccess.getForkprocessAccess().getDatasourceSTRINGTerminalRuleCall_8_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getForkprocessRule());
            					}
            					setWithLastConsumed(
            						current,
            						"datasource",
            						lv_datasource_8_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_9=(Token)match(input,50,FOLLOW_4); 

            			newLeafNode(otherlv_9, grammarAccess.getForkprocessAccess().getForEveryKeyword_9());
            		
            otherlv_10=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_10, grammarAccess.getForkprocessAccess().getLeftCurlyBracketKeyword_10());
            		
            // InternalDsl.g:2073:3: ( (lv_value_11_0= ruleSelectStatement ) )
            // InternalDsl.g:2074:4: (lv_value_11_0= ruleSelectStatement )
            {
            // InternalDsl.g:2074:4: (lv_value_11_0= ruleSelectStatement )
            // InternalDsl.g:2075:5: lv_value_11_0= ruleSelectStatement
            {

            					newCompositeNode(grammarAccess.getForkprocessAccess().getValueSelectStatementParserRuleCall_11_0());
            				
            pushFollow(FOLLOW_8);
            lv_value_11_0=ruleSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getForkprocessRule());
            					}
            					set(
            						current,
            						"value",
            						lv_value_11_0,
            						"in.handyman.Dsl.SelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_12=(Token)match(input,13,FOLLOW_36); 

            			newLeafNode(otherlv_12, grammarAccess.getForkprocessAccess().getRightCurlyBracketKeyword_12());
            		
            otherlv_13=(Token)match(input,52,FOLLOW_3); 

            			newLeafNode(otherlv_13, grammarAccess.getForkprocessAccess().getWatermarkKeyword_13());
            		
            // InternalDsl.g:2100:3: ( (lv_forkBatchSize_14_0= RULE_STRING ) )
            // InternalDsl.g:2101:4: (lv_forkBatchSize_14_0= RULE_STRING )
            {
            // InternalDsl.g:2101:4: (lv_forkBatchSize_14_0= RULE_STRING )
            // InternalDsl.g:2102:5: lv_forkBatchSize_14_0= RULE_STRING
            {
            lv_forkBatchSize_14_0=(Token)match(input,RULE_STRING,FOLLOW_23); 

            					newLeafNode(lv_forkBatchSize_14_0, grammarAccess.getForkprocessAccess().getForkBatchSizeSTRINGTerminalRuleCall_14_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getForkprocessRule());
            					}
            					setWithLastConsumed(
            						current,
            						"forkBatchSize",
            						lv_forkBatchSize_14_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_15=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_15, grammarAccess.getForkprocessAccess().getOnConditionKeyword_15());
            		
            // InternalDsl.g:2122:3: ( (lv_condition_16_0= ruleExpression ) )
            // InternalDsl.g:2123:4: (lv_condition_16_0= ruleExpression )
            {
            // InternalDsl.g:2123:4: (lv_condition_16_0= ruleExpression )
            // InternalDsl.g:2124:5: lv_condition_16_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getForkprocessAccess().getConditionExpressionParserRuleCall_16_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_16_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getForkprocessRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_16_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleForkprocess"


    // $ANTLR start "entryRuleCopydata"
    // InternalDsl.g:2145:1: entryRuleCopydata returns [EObject current=null] : iv_ruleCopydata= ruleCopydata EOF ;
    public final EObject entryRuleCopydata() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleCopydata = null;


        try {
            // InternalDsl.g:2145:49: (iv_ruleCopydata= ruleCopydata EOF )
            // InternalDsl.g:2146:2: iv_ruleCopydata= ruleCopydata EOF
            {
             newCompositeNode(grammarAccess.getCopydataRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleCopydata=ruleCopydata();

            state._fsp--;

             current =iv_ruleCopydata; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleCopydata"


    // $ANTLR start "ruleCopydata"
    // InternalDsl.g:2152:1: ruleCopydata returns [EObject current=null] : (otherlv_0= 'copydata' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'to' ( (lv_to_6_0= RULE_STRING ) ) otherlv_7= 'target' ( (lv_target_8_0= RULE_STRING ) ) otherlv_9= 'by-batch' ( (lv_limit_10_0= RULE_STRING ) ) otherlv_11= 'using' otherlv_12= '{' ( (lv_value_13_0= ruleSelectStatement ) ) otherlv_14= '}' otherlv_15= 'on-condition' ( (lv_condition_16_0= ruleExpression ) ) ) ;
    public final EObject ruleCopydata() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_source_4_0=null;
        Token otherlv_5=null;
        Token lv_to_6_0=null;
        Token otherlv_7=null;
        Token lv_target_8_0=null;
        Token otherlv_9=null;
        Token lv_limit_10_0=null;
        Token otherlv_11=null;
        Token otherlv_12=null;
        Token otherlv_14=null;
        Token otherlv_15=null;
        AntlrDatatypeRuleToken lv_value_13_0 = null;

        EObject lv_condition_16_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:2158:2: ( (otherlv_0= 'copydata' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'to' ( (lv_to_6_0= RULE_STRING ) ) otherlv_7= 'target' ( (lv_target_8_0= RULE_STRING ) ) otherlv_9= 'by-batch' ( (lv_limit_10_0= RULE_STRING ) ) otherlv_11= 'using' otherlv_12= '{' ( (lv_value_13_0= ruleSelectStatement ) ) otherlv_14= '}' otherlv_15= 'on-condition' ( (lv_condition_16_0= ruleExpression ) ) ) )
            // InternalDsl.g:2159:2: (otherlv_0= 'copydata' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'to' ( (lv_to_6_0= RULE_STRING ) ) otherlv_7= 'target' ( (lv_target_8_0= RULE_STRING ) ) otherlv_9= 'by-batch' ( (lv_limit_10_0= RULE_STRING ) ) otherlv_11= 'using' otherlv_12= '{' ( (lv_value_13_0= ruleSelectStatement ) ) otherlv_14= '}' otherlv_15= 'on-condition' ( (lv_condition_16_0= ruleExpression ) ) )
            {
            // InternalDsl.g:2159:2: (otherlv_0= 'copydata' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'to' ( (lv_to_6_0= RULE_STRING ) ) otherlv_7= 'target' ( (lv_target_8_0= RULE_STRING ) ) otherlv_9= 'by-batch' ( (lv_limit_10_0= RULE_STRING ) ) otherlv_11= 'using' otherlv_12= '{' ( (lv_value_13_0= ruleSelectStatement ) ) otherlv_14= '}' otherlv_15= 'on-condition' ( (lv_condition_16_0= ruleExpression ) ) )
            // InternalDsl.g:2160:3: otherlv_0= 'copydata' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'to' ( (lv_to_6_0= RULE_STRING ) ) otherlv_7= 'target' ( (lv_target_8_0= RULE_STRING ) ) otherlv_9= 'by-batch' ( (lv_limit_10_0= RULE_STRING ) ) otherlv_11= 'using' otherlv_12= '{' ( (lv_value_13_0= ruleSelectStatement ) ) otherlv_14= '}' otherlv_15= 'on-condition' ( (lv_condition_16_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,53,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getCopydataAccess().getCopydataKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getCopydataAccess().getAsKeyword_1());
            		
            // InternalDsl.g:2168:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:2169:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:2169:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:2170:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_14); 

            					newLeafNode(lv_name_2_0, grammarAccess.getCopydataAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getCopydataRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,22,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getCopydataAccess().getFromKeyword_3());
            		
            // InternalDsl.g:2190:3: ( (lv_source_4_0= RULE_STRING ) )
            // InternalDsl.g:2191:4: (lv_source_4_0= RULE_STRING )
            {
            // InternalDsl.g:2191:4: (lv_source_4_0= RULE_STRING )
            // InternalDsl.g:2192:5: lv_source_4_0= RULE_STRING
            {
            lv_source_4_0=(Token)match(input,RULE_STRING,FOLLOW_16); 

            					newLeafNode(lv_source_4_0, grammarAccess.getCopydataAccess().getSourceSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getCopydataRule());
            					}
            					setWithLastConsumed(
            						current,
            						"source",
            						lv_source_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,24,FOLLOW_3); 

            			newLeafNode(otherlv_5, grammarAccess.getCopydataAccess().getToKeyword_5());
            		
            // InternalDsl.g:2212:3: ( (lv_to_6_0= RULE_STRING ) )
            // InternalDsl.g:2213:4: (lv_to_6_0= RULE_STRING )
            {
            // InternalDsl.g:2213:4: (lv_to_6_0= RULE_STRING )
            // InternalDsl.g:2214:5: lv_to_6_0= RULE_STRING
            {
            lv_to_6_0=(Token)match(input,RULE_STRING,FOLLOW_37); 

            					newLeafNode(lv_to_6_0, grammarAccess.getCopydataAccess().getToSTRINGTerminalRuleCall_6_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getCopydataRule());
            					}
            					setWithLastConsumed(
            						current,
            						"to",
            						lv_to_6_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_7=(Token)match(input,54,FOLLOW_3); 

            			newLeafNode(otherlv_7, grammarAccess.getCopydataAccess().getTargetKeyword_7());
            		
            // InternalDsl.g:2234:3: ( (lv_target_8_0= RULE_STRING ) )
            // InternalDsl.g:2235:4: (lv_target_8_0= RULE_STRING )
            {
            // InternalDsl.g:2235:4: (lv_target_8_0= RULE_STRING )
            // InternalDsl.g:2236:5: lv_target_8_0= RULE_STRING
            {
            lv_target_8_0=(Token)match(input,RULE_STRING,FOLLOW_38); 

            					newLeafNode(lv_target_8_0, grammarAccess.getCopydataAccess().getTargetSTRINGTerminalRuleCall_8_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getCopydataRule());
            					}
            					setWithLastConsumed(
            						current,
            						"target",
            						lv_target_8_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_9=(Token)match(input,55,FOLLOW_3); 

            			newLeafNode(otherlv_9, grammarAccess.getCopydataAccess().getByBatchKeyword_9());
            		
            // InternalDsl.g:2256:3: ( (lv_limit_10_0= RULE_STRING ) )
            // InternalDsl.g:2257:4: (lv_limit_10_0= RULE_STRING )
            {
            // InternalDsl.g:2257:4: (lv_limit_10_0= RULE_STRING )
            // InternalDsl.g:2258:5: lv_limit_10_0= RULE_STRING
            {
            lv_limit_10_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_limit_10_0, grammarAccess.getCopydataAccess().getLimitSTRINGTerminalRuleCall_10_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getCopydataRule());
            					}
            					setWithLastConsumed(
            						current,
            						"limit",
            						lv_limit_10_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_11=(Token)match(input,30,FOLLOW_4); 

            			newLeafNode(otherlv_11, grammarAccess.getCopydataAccess().getUsingKeyword_11());
            		
            otherlv_12=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_12, grammarAccess.getCopydataAccess().getLeftCurlyBracketKeyword_12());
            		
            // InternalDsl.g:2282:3: ( (lv_value_13_0= ruleSelectStatement ) )
            // InternalDsl.g:2283:4: (lv_value_13_0= ruleSelectStatement )
            {
            // InternalDsl.g:2283:4: (lv_value_13_0= ruleSelectStatement )
            // InternalDsl.g:2284:5: lv_value_13_0= ruleSelectStatement
            {

            					newCompositeNode(grammarAccess.getCopydataAccess().getValueSelectStatementParserRuleCall_13_0());
            				
            pushFollow(FOLLOW_8);
            lv_value_13_0=ruleSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getCopydataRule());
            					}
            					set(
            						current,
            						"value",
            						lv_value_13_0,
            						"in.handyman.Dsl.SelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_14=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_14, grammarAccess.getCopydataAccess().getRightCurlyBracketKeyword_14());
            		
            otherlv_15=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_15, grammarAccess.getCopydataAccess().getOnConditionKeyword_15());
            		
            // InternalDsl.g:2309:3: ( (lv_condition_16_0= ruleExpression ) )
            // InternalDsl.g:2310:4: (lv_condition_16_0= ruleExpression )
            {
            // InternalDsl.g:2310:4: (lv_condition_16_0= ruleExpression )
            // InternalDsl.g:2311:5: lv_condition_16_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getCopydataAccess().getConditionExpressionParserRuleCall_16_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_16_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getCopydataRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_16_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleCopydata"


    // $ANTLR start "entryRuleWriteCsv"
    // InternalDsl.g:2332:1: entryRuleWriteCsv returns [EObject current=null] : iv_ruleWriteCsv= ruleWriteCsv EOF ;
    public final EObject entryRuleWriteCsv() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleWriteCsv = null;


        try {
            // InternalDsl.g:2332:49: (iv_ruleWriteCsv= ruleWriteCsv EOF )
            // InternalDsl.g:2333:2: iv_ruleWriteCsv= ruleWriteCsv EOF
            {
             newCompositeNode(grammarAccess.getWriteCsvRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleWriteCsv=ruleWriteCsv();

            state._fsp--;

             current =iv_ruleWriteCsv; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleWriteCsv"


    // $ANTLR start "ruleWriteCsv"
    // InternalDsl.g:2339:1: ruleWriteCsv returns [EObject current=null] : (otherlv_0= 'writecsv' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'to' ( (lv_to_6_0= RULE_STRING ) ) otherlv_7= 'with' ( (lv_delim_8_0= RULE_STRING ) ) otherlv_9= 'using' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) ) ) ;
    public final EObject ruleWriteCsv() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_source_4_0=null;
        Token otherlv_5=null;
        Token lv_to_6_0=null;
        Token otherlv_7=null;
        Token lv_delim_8_0=null;
        Token otherlv_9=null;
        Token otherlv_10=null;
        Token otherlv_12=null;
        Token otherlv_13=null;
        AntlrDatatypeRuleToken lv_value_11_0 = null;

        EObject lv_condition_14_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:2345:2: ( (otherlv_0= 'writecsv' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'to' ( (lv_to_6_0= RULE_STRING ) ) otherlv_7= 'with' ( (lv_delim_8_0= RULE_STRING ) ) otherlv_9= 'using' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) ) ) )
            // InternalDsl.g:2346:2: (otherlv_0= 'writecsv' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'to' ( (lv_to_6_0= RULE_STRING ) ) otherlv_7= 'with' ( (lv_delim_8_0= RULE_STRING ) ) otherlv_9= 'using' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) ) )
            {
            // InternalDsl.g:2346:2: (otherlv_0= 'writecsv' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'to' ( (lv_to_6_0= RULE_STRING ) ) otherlv_7= 'with' ( (lv_delim_8_0= RULE_STRING ) ) otherlv_9= 'using' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) ) )
            // InternalDsl.g:2347:3: otherlv_0= 'writecsv' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'to' ( (lv_to_6_0= RULE_STRING ) ) otherlv_7= 'with' ( (lv_delim_8_0= RULE_STRING ) ) otherlv_9= 'using' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,56,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getWriteCsvAccess().getWritecsvKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getWriteCsvAccess().getAsKeyword_1());
            		
            // InternalDsl.g:2355:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:2356:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:2356:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:2357:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_14); 

            					newLeafNode(lv_name_2_0, grammarAccess.getWriteCsvAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getWriteCsvRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,22,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getWriteCsvAccess().getFromKeyword_3());
            		
            // InternalDsl.g:2377:3: ( (lv_source_4_0= RULE_STRING ) )
            // InternalDsl.g:2378:4: (lv_source_4_0= RULE_STRING )
            {
            // InternalDsl.g:2378:4: (lv_source_4_0= RULE_STRING )
            // InternalDsl.g:2379:5: lv_source_4_0= RULE_STRING
            {
            lv_source_4_0=(Token)match(input,RULE_STRING,FOLLOW_16); 

            					newLeafNode(lv_source_4_0, grammarAccess.getWriteCsvAccess().getSourceSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getWriteCsvRule());
            					}
            					setWithLastConsumed(
            						current,
            						"source",
            						lv_source_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,24,FOLLOW_3); 

            			newLeafNode(otherlv_5, grammarAccess.getWriteCsvAccess().getToKeyword_5());
            		
            // InternalDsl.g:2399:3: ( (lv_to_6_0= RULE_STRING ) )
            // InternalDsl.g:2400:4: (lv_to_6_0= RULE_STRING )
            {
            // InternalDsl.g:2400:4: (lv_to_6_0= RULE_STRING )
            // InternalDsl.g:2401:5: lv_to_6_0= RULE_STRING
            {
            lv_to_6_0=(Token)match(input,RULE_STRING,FOLLOW_39); 

            					newLeafNode(lv_to_6_0, grammarAccess.getWriteCsvAccess().getToSTRINGTerminalRuleCall_6_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getWriteCsvRule());
            					}
            					setWithLastConsumed(
            						current,
            						"to",
            						lv_to_6_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_7=(Token)match(input,57,FOLLOW_3); 

            			newLeafNode(otherlv_7, grammarAccess.getWriteCsvAccess().getWithKeyword_7());
            		
            // InternalDsl.g:2421:3: ( (lv_delim_8_0= RULE_STRING ) )
            // InternalDsl.g:2422:4: (lv_delim_8_0= RULE_STRING )
            {
            // InternalDsl.g:2422:4: (lv_delim_8_0= RULE_STRING )
            // InternalDsl.g:2423:5: lv_delim_8_0= RULE_STRING
            {
            lv_delim_8_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_delim_8_0, grammarAccess.getWriteCsvAccess().getDelimSTRINGTerminalRuleCall_8_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getWriteCsvRule());
            					}
            					setWithLastConsumed(
            						current,
            						"delim",
            						lv_delim_8_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_9=(Token)match(input,30,FOLLOW_4); 

            			newLeafNode(otherlv_9, grammarAccess.getWriteCsvAccess().getUsingKeyword_9());
            		
            otherlv_10=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_10, grammarAccess.getWriteCsvAccess().getLeftCurlyBracketKeyword_10());
            		
            // InternalDsl.g:2447:3: ( (lv_value_11_0= ruleSelectStatement ) )
            // InternalDsl.g:2448:4: (lv_value_11_0= ruleSelectStatement )
            {
            // InternalDsl.g:2448:4: (lv_value_11_0= ruleSelectStatement )
            // InternalDsl.g:2449:5: lv_value_11_0= ruleSelectStatement
            {

            					newCompositeNode(grammarAccess.getWriteCsvAccess().getValueSelectStatementParserRuleCall_11_0());
            				
            pushFollow(FOLLOW_8);
            lv_value_11_0=ruleSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getWriteCsvRule());
            					}
            					set(
            						current,
            						"value",
            						lv_value_11_0,
            						"in.handyman.Dsl.SelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_12=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_12, grammarAccess.getWriteCsvAccess().getRightCurlyBracketKeyword_12());
            		
            otherlv_13=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_13, grammarAccess.getWriteCsvAccess().getOnConditionKeyword_13());
            		
            // InternalDsl.g:2474:3: ( (lv_condition_14_0= ruleExpression ) )
            // InternalDsl.g:2475:4: (lv_condition_14_0= ruleExpression )
            {
            // InternalDsl.g:2475:4: (lv_condition_14_0= ruleExpression )
            // InternalDsl.g:2476:5: lv_condition_14_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getWriteCsvAccess().getConditionExpressionParserRuleCall_14_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_14_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getWriteCsvRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_14_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleWriteCsv"


    // $ANTLR start "entryRuleLoadCsv"
    // InternalDsl.g:2497:1: entryRuleLoadCsv returns [EObject current=null] : iv_ruleLoadCsv= ruleLoadCsv EOF ;
    public final EObject entryRuleLoadCsv() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleLoadCsv = null;


        try {
            // InternalDsl.g:2497:48: (iv_ruleLoadCsv= ruleLoadCsv EOF )
            // InternalDsl.g:2498:2: iv_ruleLoadCsv= ruleLoadCsv EOF
            {
             newCompositeNode(grammarAccess.getLoadCsvRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleLoadCsv=ruleLoadCsv();

            state._fsp--;

             current =iv_ruleLoadCsv; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleLoadCsv"


    // $ANTLR start "ruleLoadCsv"
    // InternalDsl.g:2504:1: ruleLoadCsv returns [EObject current=null] : (otherlv_0= 'loadcsv' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'pid' ( (lv_pid_4_0= RULE_STRING ) ) otherlv_5= 'from' ( (lv_source_6_0= RULE_STRING ) ) otherlv_7= 'to' ( (lv_to_8_0= RULE_STRING ) ) otherlv_9= 'with' ( (lv_delim_10_0= RULE_STRING ) ) otherlv_11= 'by-batch' ( (lv_limit_12_0= RULE_STRING ) ) otherlv_13= 'using' otherlv_14= '{' ( (lv_value_15_0= ruleSelectStatement ) ) otherlv_16= '}' otherlv_17= 'on-condition' ( (lv_condition_18_0= ruleExpression ) ) ) ;
    public final EObject ruleLoadCsv() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_pid_4_0=null;
        Token otherlv_5=null;
        Token lv_source_6_0=null;
        Token otherlv_7=null;
        Token lv_to_8_0=null;
        Token otherlv_9=null;
        Token lv_delim_10_0=null;
        Token otherlv_11=null;
        Token lv_limit_12_0=null;
        Token otherlv_13=null;
        Token otherlv_14=null;
        Token otherlv_16=null;
        Token otherlv_17=null;
        AntlrDatatypeRuleToken lv_value_15_0 = null;

        EObject lv_condition_18_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:2510:2: ( (otherlv_0= 'loadcsv' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'pid' ( (lv_pid_4_0= RULE_STRING ) ) otherlv_5= 'from' ( (lv_source_6_0= RULE_STRING ) ) otherlv_7= 'to' ( (lv_to_8_0= RULE_STRING ) ) otherlv_9= 'with' ( (lv_delim_10_0= RULE_STRING ) ) otherlv_11= 'by-batch' ( (lv_limit_12_0= RULE_STRING ) ) otherlv_13= 'using' otherlv_14= '{' ( (lv_value_15_0= ruleSelectStatement ) ) otherlv_16= '}' otherlv_17= 'on-condition' ( (lv_condition_18_0= ruleExpression ) ) ) )
            // InternalDsl.g:2511:2: (otherlv_0= 'loadcsv' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'pid' ( (lv_pid_4_0= RULE_STRING ) ) otherlv_5= 'from' ( (lv_source_6_0= RULE_STRING ) ) otherlv_7= 'to' ( (lv_to_8_0= RULE_STRING ) ) otherlv_9= 'with' ( (lv_delim_10_0= RULE_STRING ) ) otherlv_11= 'by-batch' ( (lv_limit_12_0= RULE_STRING ) ) otherlv_13= 'using' otherlv_14= '{' ( (lv_value_15_0= ruleSelectStatement ) ) otherlv_16= '}' otherlv_17= 'on-condition' ( (lv_condition_18_0= ruleExpression ) ) )
            {
            // InternalDsl.g:2511:2: (otherlv_0= 'loadcsv' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'pid' ( (lv_pid_4_0= RULE_STRING ) ) otherlv_5= 'from' ( (lv_source_6_0= RULE_STRING ) ) otherlv_7= 'to' ( (lv_to_8_0= RULE_STRING ) ) otherlv_9= 'with' ( (lv_delim_10_0= RULE_STRING ) ) otherlv_11= 'by-batch' ( (lv_limit_12_0= RULE_STRING ) ) otherlv_13= 'using' otherlv_14= '{' ( (lv_value_15_0= ruleSelectStatement ) ) otherlv_16= '}' otherlv_17= 'on-condition' ( (lv_condition_18_0= ruleExpression ) ) )
            // InternalDsl.g:2512:3: otherlv_0= 'loadcsv' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'pid' ( (lv_pid_4_0= RULE_STRING ) ) otherlv_5= 'from' ( (lv_source_6_0= RULE_STRING ) ) otherlv_7= 'to' ( (lv_to_8_0= RULE_STRING ) ) otherlv_9= 'with' ( (lv_delim_10_0= RULE_STRING ) ) otherlv_11= 'by-batch' ( (lv_limit_12_0= RULE_STRING ) ) otherlv_13= 'using' otherlv_14= '{' ( (lv_value_15_0= ruleSelectStatement ) ) otherlv_16= '}' otherlv_17= 'on-condition' ( (lv_condition_18_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,58,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getLoadCsvAccess().getLoadcsvKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getLoadCsvAccess().getAsKeyword_1());
            		
            // InternalDsl.g:2520:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:2521:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:2521:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:2522:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_40); 

            					newLeafNode(lv_name_2_0, grammarAccess.getLoadCsvAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getLoadCsvRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,59,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getLoadCsvAccess().getPidKeyword_3());
            		
            // InternalDsl.g:2542:3: ( (lv_pid_4_0= RULE_STRING ) )
            // InternalDsl.g:2543:4: (lv_pid_4_0= RULE_STRING )
            {
            // InternalDsl.g:2543:4: (lv_pid_4_0= RULE_STRING )
            // InternalDsl.g:2544:5: lv_pid_4_0= RULE_STRING
            {
            lv_pid_4_0=(Token)match(input,RULE_STRING,FOLLOW_14); 

            					newLeafNode(lv_pid_4_0, grammarAccess.getLoadCsvAccess().getPidSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getLoadCsvRule());
            					}
            					setWithLastConsumed(
            						current,
            						"pid",
            						lv_pid_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,22,FOLLOW_3); 

            			newLeafNode(otherlv_5, grammarAccess.getLoadCsvAccess().getFromKeyword_5());
            		
            // InternalDsl.g:2564:3: ( (lv_source_6_0= RULE_STRING ) )
            // InternalDsl.g:2565:4: (lv_source_6_0= RULE_STRING )
            {
            // InternalDsl.g:2565:4: (lv_source_6_0= RULE_STRING )
            // InternalDsl.g:2566:5: lv_source_6_0= RULE_STRING
            {
            lv_source_6_0=(Token)match(input,RULE_STRING,FOLLOW_16); 

            					newLeafNode(lv_source_6_0, grammarAccess.getLoadCsvAccess().getSourceSTRINGTerminalRuleCall_6_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getLoadCsvRule());
            					}
            					setWithLastConsumed(
            						current,
            						"source",
            						lv_source_6_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_7=(Token)match(input,24,FOLLOW_3); 

            			newLeafNode(otherlv_7, grammarAccess.getLoadCsvAccess().getToKeyword_7());
            		
            // InternalDsl.g:2586:3: ( (lv_to_8_0= RULE_STRING ) )
            // InternalDsl.g:2587:4: (lv_to_8_0= RULE_STRING )
            {
            // InternalDsl.g:2587:4: (lv_to_8_0= RULE_STRING )
            // InternalDsl.g:2588:5: lv_to_8_0= RULE_STRING
            {
            lv_to_8_0=(Token)match(input,RULE_STRING,FOLLOW_39); 

            					newLeafNode(lv_to_8_0, grammarAccess.getLoadCsvAccess().getToSTRINGTerminalRuleCall_8_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getLoadCsvRule());
            					}
            					setWithLastConsumed(
            						current,
            						"to",
            						lv_to_8_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_9=(Token)match(input,57,FOLLOW_3); 

            			newLeafNode(otherlv_9, grammarAccess.getLoadCsvAccess().getWithKeyword_9());
            		
            // InternalDsl.g:2608:3: ( (lv_delim_10_0= RULE_STRING ) )
            // InternalDsl.g:2609:4: (lv_delim_10_0= RULE_STRING )
            {
            // InternalDsl.g:2609:4: (lv_delim_10_0= RULE_STRING )
            // InternalDsl.g:2610:5: lv_delim_10_0= RULE_STRING
            {
            lv_delim_10_0=(Token)match(input,RULE_STRING,FOLLOW_38); 

            					newLeafNode(lv_delim_10_0, grammarAccess.getLoadCsvAccess().getDelimSTRINGTerminalRuleCall_10_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getLoadCsvRule());
            					}
            					setWithLastConsumed(
            						current,
            						"delim",
            						lv_delim_10_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_11=(Token)match(input,55,FOLLOW_3); 

            			newLeafNode(otherlv_11, grammarAccess.getLoadCsvAccess().getByBatchKeyword_11());
            		
            // InternalDsl.g:2630:3: ( (lv_limit_12_0= RULE_STRING ) )
            // InternalDsl.g:2631:4: (lv_limit_12_0= RULE_STRING )
            {
            // InternalDsl.g:2631:4: (lv_limit_12_0= RULE_STRING )
            // InternalDsl.g:2632:5: lv_limit_12_0= RULE_STRING
            {
            lv_limit_12_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_limit_12_0, grammarAccess.getLoadCsvAccess().getLimitSTRINGTerminalRuleCall_12_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getLoadCsvRule());
            					}
            					setWithLastConsumed(
            						current,
            						"limit",
            						lv_limit_12_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_13=(Token)match(input,30,FOLLOW_4); 

            			newLeafNode(otherlv_13, grammarAccess.getLoadCsvAccess().getUsingKeyword_13());
            		
            otherlv_14=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_14, grammarAccess.getLoadCsvAccess().getLeftCurlyBracketKeyword_14());
            		
            // InternalDsl.g:2656:3: ( (lv_value_15_0= ruleSelectStatement ) )
            // InternalDsl.g:2657:4: (lv_value_15_0= ruleSelectStatement )
            {
            // InternalDsl.g:2657:4: (lv_value_15_0= ruleSelectStatement )
            // InternalDsl.g:2658:5: lv_value_15_0= ruleSelectStatement
            {

            					newCompositeNode(grammarAccess.getLoadCsvAccess().getValueSelectStatementParserRuleCall_15_0());
            				
            pushFollow(FOLLOW_8);
            lv_value_15_0=ruleSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getLoadCsvRule());
            					}
            					set(
            						current,
            						"value",
            						lv_value_15_0,
            						"in.handyman.Dsl.SelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_16=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_16, grammarAccess.getLoadCsvAccess().getRightCurlyBracketKeyword_16());
            		
            otherlv_17=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_17, grammarAccess.getLoadCsvAccess().getOnConditionKeyword_17());
            		
            // InternalDsl.g:2683:3: ( (lv_condition_18_0= ruleExpression ) )
            // InternalDsl.g:2684:4: (lv_condition_18_0= ruleExpression )
            {
            // InternalDsl.g:2684:4: (lv_condition_18_0= ruleExpression )
            // InternalDsl.g:2685:5: lv_condition_18_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getLoadCsvAccess().getConditionExpressionParserRuleCall_18_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_18_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getLoadCsvRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_18_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleLoadCsv"


    // $ANTLR start "entryRuleTransform"
    // InternalDsl.g:2706:1: entryRuleTransform returns [EObject current=null] : iv_ruleTransform= ruleTransform EOF ;
    public final EObject entryRuleTransform() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTransform = null;


        try {
            // InternalDsl.g:2706:50: (iv_ruleTransform= ruleTransform EOF )
            // InternalDsl.g:2707:2: iv_ruleTransform= ruleTransform EOF
            {
             newCompositeNode(grammarAccess.getTransformRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleTransform=ruleTransform();

            state._fsp--;

             current =iv_ruleTransform; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleTransform"


    // $ANTLR start "ruleTransform"
    // InternalDsl.g:2713:1: ruleTransform returns [EObject current=null] : (otherlv_0= 'transform' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) ) ;
    public final EObject ruleTransform() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_on_4_0=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        Token otherlv_8=null;
        Token otherlv_9=null;
        AntlrDatatypeRuleToken lv_value_7_0 = null;

        EObject lv_condition_10_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:2719:2: ( (otherlv_0= 'transform' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) ) )
            // InternalDsl.g:2720:2: (otherlv_0= 'transform' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) )
            {
            // InternalDsl.g:2720:2: (otherlv_0= 'transform' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) )
            // InternalDsl.g:2721:3: otherlv_0= 'transform' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,60,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getTransformAccess().getTransformKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getTransformAccess().getAsKeyword_1());
            		
            // InternalDsl.g:2729:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:2730:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:2730:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:2731:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_31); 

            					newLeafNode(lv_name_2_0, grammarAccess.getTransformAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getTransformRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,44,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getTransformAccess().getOnKeyword_3());
            		
            // InternalDsl.g:2751:3: ( (lv_on_4_0= RULE_STRING ) )
            // InternalDsl.g:2752:4: (lv_on_4_0= RULE_STRING )
            {
            // InternalDsl.g:2752:4: (lv_on_4_0= RULE_STRING )
            // InternalDsl.g:2753:5: lv_on_4_0= RULE_STRING
            {
            lv_on_4_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_on_4_0, grammarAccess.getTransformAccess().getOnSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getTransformRule());
            					}
            					setWithLastConsumed(
            						current,
            						"on",
            						lv_on_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,30,FOLLOW_4); 

            			newLeafNode(otherlv_5, grammarAccess.getTransformAccess().getUsingKeyword_5());
            		
            otherlv_6=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_6, grammarAccess.getTransformAccess().getLeftCurlyBracketKeyword_6());
            		
            // InternalDsl.g:2777:3: ( (lv_value_7_0= ruleNonSelectStatement ) )
            // InternalDsl.g:2778:4: (lv_value_7_0= ruleNonSelectStatement )
            {
            // InternalDsl.g:2778:4: (lv_value_7_0= ruleNonSelectStatement )
            // InternalDsl.g:2779:5: lv_value_7_0= ruleNonSelectStatement
            {

            					newCompositeNode(grammarAccess.getTransformAccess().getValueNonSelectStatementParserRuleCall_7_0());
            				
            pushFollow(FOLLOW_8);
            lv_value_7_0=ruleNonSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getTransformRule());
            					}
            					add(
            						current,
            						"value",
            						lv_value_7_0,
            						"in.handyman.Dsl.NonSelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_8=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_8, grammarAccess.getTransformAccess().getRightCurlyBracketKeyword_8());
            		
            otherlv_9=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_9, grammarAccess.getTransformAccess().getOnConditionKeyword_9());
            		
            // InternalDsl.g:2804:3: ( (lv_condition_10_0= ruleExpression ) )
            // InternalDsl.g:2805:4: (lv_condition_10_0= ruleExpression )
            {
            // InternalDsl.g:2805:4: (lv_condition_10_0= ruleExpression )
            // InternalDsl.g:2806:5: lv_condition_10_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getTransformAccess().getConditionExpressionParserRuleCall_10_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_10_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getTransformRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_10_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleTransform"


    // $ANTLR start "entryRuleDeleteSql"
    // InternalDsl.g:2827:1: entryRuleDeleteSql returns [EObject current=null] : iv_ruleDeleteSql= ruleDeleteSql EOF ;
    public final EObject entryRuleDeleteSql() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleDeleteSql = null;


        try {
            // InternalDsl.g:2827:50: (iv_ruleDeleteSql= ruleDeleteSql EOF )
            // InternalDsl.g:2828:2: iv_ruleDeleteSql= ruleDeleteSql EOF
            {
             newCompositeNode(grammarAccess.getDeleteSqlRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleDeleteSql=ruleDeleteSql();

            state._fsp--;

             current =iv_ruleDeleteSql; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleDeleteSql"


    // $ANTLR start "ruleDeleteSql"
    // InternalDsl.g:2834:1: ruleDeleteSql returns [EObject current=null] : (otherlv_0= 'deletesql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) ) ;
    public final EObject ruleDeleteSql() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_on_4_0=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        Token otherlv_8=null;
        Token otherlv_9=null;
        AntlrDatatypeRuleToken lv_value_7_0 = null;

        EObject lv_condition_10_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:2840:2: ( (otherlv_0= 'deletesql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) ) )
            // InternalDsl.g:2841:2: (otherlv_0= 'deletesql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) )
            {
            // InternalDsl.g:2841:2: (otherlv_0= 'deletesql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) )
            // InternalDsl.g:2842:3: otherlv_0= 'deletesql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,61,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getDeleteSqlAccess().getDeletesqlKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getDeleteSqlAccess().getAsKeyword_1());
            		
            // InternalDsl.g:2850:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:2851:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:2851:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:2852:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_31); 

            					newLeafNode(lv_name_2_0, grammarAccess.getDeleteSqlAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getDeleteSqlRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,44,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getDeleteSqlAccess().getOnKeyword_3());
            		
            // InternalDsl.g:2872:3: ( (lv_on_4_0= RULE_STRING ) )
            // InternalDsl.g:2873:4: (lv_on_4_0= RULE_STRING )
            {
            // InternalDsl.g:2873:4: (lv_on_4_0= RULE_STRING )
            // InternalDsl.g:2874:5: lv_on_4_0= RULE_STRING
            {
            lv_on_4_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_on_4_0, grammarAccess.getDeleteSqlAccess().getOnSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getDeleteSqlRule());
            					}
            					setWithLastConsumed(
            						current,
            						"on",
            						lv_on_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,30,FOLLOW_4); 

            			newLeafNode(otherlv_5, grammarAccess.getDeleteSqlAccess().getUsingKeyword_5());
            		
            otherlv_6=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_6, grammarAccess.getDeleteSqlAccess().getLeftCurlyBracketKeyword_6());
            		
            // InternalDsl.g:2898:3: ( (lv_value_7_0= ruleNonSelectStatement ) )
            // InternalDsl.g:2899:4: (lv_value_7_0= ruleNonSelectStatement )
            {
            // InternalDsl.g:2899:4: (lv_value_7_0= ruleNonSelectStatement )
            // InternalDsl.g:2900:5: lv_value_7_0= ruleNonSelectStatement
            {

            					newCompositeNode(grammarAccess.getDeleteSqlAccess().getValueNonSelectStatementParserRuleCall_7_0());
            				
            pushFollow(FOLLOW_8);
            lv_value_7_0=ruleNonSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getDeleteSqlRule());
            					}
            					add(
            						current,
            						"value",
            						lv_value_7_0,
            						"in.handyman.Dsl.NonSelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_8=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_8, grammarAccess.getDeleteSqlAccess().getRightCurlyBracketKeyword_8());
            		
            otherlv_9=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_9, grammarAccess.getDeleteSqlAccess().getOnConditionKeyword_9());
            		
            // InternalDsl.g:2925:3: ( (lv_condition_10_0= ruleExpression ) )
            // InternalDsl.g:2926:4: (lv_condition_10_0= ruleExpression )
            {
            // InternalDsl.g:2926:4: (lv_condition_10_0= ruleExpression )
            // InternalDsl.g:2927:5: lv_condition_10_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getDeleteSqlAccess().getConditionExpressionParserRuleCall_10_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_10_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getDeleteSqlRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_10_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleDeleteSql"


    // $ANTLR start "entryRuleUpdateSql"
    // InternalDsl.g:2948:1: entryRuleUpdateSql returns [EObject current=null] : iv_ruleUpdateSql= ruleUpdateSql EOF ;
    public final EObject entryRuleUpdateSql() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleUpdateSql = null;


        try {
            // InternalDsl.g:2948:50: (iv_ruleUpdateSql= ruleUpdateSql EOF )
            // InternalDsl.g:2949:2: iv_ruleUpdateSql= ruleUpdateSql EOF
            {
             newCompositeNode(grammarAccess.getUpdateSqlRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleUpdateSql=ruleUpdateSql();

            state._fsp--;

             current =iv_ruleUpdateSql; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleUpdateSql"


    // $ANTLR start "ruleUpdateSql"
    // InternalDsl.g:2955:1: ruleUpdateSql returns [EObject current=null] : (otherlv_0= 'updatesql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) ) ;
    public final EObject ruleUpdateSql() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_on_4_0=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        Token otherlv_8=null;
        Token otherlv_9=null;
        AntlrDatatypeRuleToken lv_value_7_0 = null;

        EObject lv_condition_10_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:2961:2: ( (otherlv_0= 'updatesql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) ) )
            // InternalDsl.g:2962:2: (otherlv_0= 'updatesql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) )
            {
            // InternalDsl.g:2962:2: (otherlv_0= 'updatesql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) )
            // InternalDsl.g:2963:3: otherlv_0= 'updatesql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,62,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getUpdateSqlAccess().getUpdatesqlKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getUpdateSqlAccess().getAsKeyword_1());
            		
            // InternalDsl.g:2971:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:2972:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:2972:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:2973:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_31); 

            					newLeafNode(lv_name_2_0, grammarAccess.getUpdateSqlAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getUpdateSqlRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,44,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getUpdateSqlAccess().getOnKeyword_3());
            		
            // InternalDsl.g:2993:3: ( (lv_on_4_0= RULE_STRING ) )
            // InternalDsl.g:2994:4: (lv_on_4_0= RULE_STRING )
            {
            // InternalDsl.g:2994:4: (lv_on_4_0= RULE_STRING )
            // InternalDsl.g:2995:5: lv_on_4_0= RULE_STRING
            {
            lv_on_4_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_on_4_0, grammarAccess.getUpdateSqlAccess().getOnSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getUpdateSqlRule());
            					}
            					setWithLastConsumed(
            						current,
            						"on",
            						lv_on_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,30,FOLLOW_4); 

            			newLeafNode(otherlv_5, grammarAccess.getUpdateSqlAccess().getUsingKeyword_5());
            		
            otherlv_6=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_6, grammarAccess.getUpdateSqlAccess().getLeftCurlyBracketKeyword_6());
            		
            // InternalDsl.g:3019:3: ( (lv_value_7_0= ruleNonSelectStatement ) )
            // InternalDsl.g:3020:4: (lv_value_7_0= ruleNonSelectStatement )
            {
            // InternalDsl.g:3020:4: (lv_value_7_0= ruleNonSelectStatement )
            // InternalDsl.g:3021:5: lv_value_7_0= ruleNonSelectStatement
            {

            					newCompositeNode(grammarAccess.getUpdateSqlAccess().getValueNonSelectStatementParserRuleCall_7_0());
            				
            pushFollow(FOLLOW_8);
            lv_value_7_0=ruleNonSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getUpdateSqlRule());
            					}
            					add(
            						current,
            						"value",
            						lv_value_7_0,
            						"in.handyman.Dsl.NonSelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_8=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_8, grammarAccess.getUpdateSqlAccess().getRightCurlyBracketKeyword_8());
            		
            otherlv_9=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_9, grammarAccess.getUpdateSqlAccess().getOnConditionKeyword_9());
            		
            // InternalDsl.g:3046:3: ( (lv_condition_10_0= ruleExpression ) )
            // InternalDsl.g:3047:4: (lv_condition_10_0= ruleExpression )
            {
            // InternalDsl.g:3047:4: (lv_condition_10_0= ruleExpression )
            // InternalDsl.g:3048:5: lv_condition_10_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getUpdateSqlAccess().getConditionExpressionParserRuleCall_10_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_10_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getUpdateSqlRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_10_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleUpdateSql"


    // $ANTLR start "entryRuleInsertSql"
    // InternalDsl.g:3069:1: entryRuleInsertSql returns [EObject current=null] : iv_ruleInsertSql= ruleInsertSql EOF ;
    public final EObject entryRuleInsertSql() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleInsertSql = null;


        try {
            // InternalDsl.g:3069:50: (iv_ruleInsertSql= ruleInsertSql EOF )
            // InternalDsl.g:3070:2: iv_ruleInsertSql= ruleInsertSql EOF
            {
             newCompositeNode(grammarAccess.getInsertSqlRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleInsertSql=ruleInsertSql();

            state._fsp--;

             current =iv_ruleInsertSql; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleInsertSql"


    // $ANTLR start "ruleInsertSql"
    // InternalDsl.g:3076:1: ruleInsertSql returns [EObject current=null] : (otherlv_0= 'insertsql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) ) ;
    public final EObject ruleInsertSql() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_on_4_0=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        Token otherlv_8=null;
        Token otherlv_9=null;
        AntlrDatatypeRuleToken lv_value_7_0 = null;

        EObject lv_condition_10_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:3082:2: ( (otherlv_0= 'insertsql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) ) )
            // InternalDsl.g:3083:2: (otherlv_0= 'insertsql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) )
            {
            // InternalDsl.g:3083:2: (otherlv_0= 'insertsql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) )
            // InternalDsl.g:3084:3: otherlv_0= 'insertsql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,63,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getInsertSqlAccess().getInsertsqlKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getInsertSqlAccess().getAsKeyword_1());
            		
            // InternalDsl.g:3092:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:3093:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:3093:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:3094:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_31); 

            					newLeafNode(lv_name_2_0, grammarAccess.getInsertSqlAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getInsertSqlRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,44,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getInsertSqlAccess().getOnKeyword_3());
            		
            // InternalDsl.g:3114:3: ( (lv_on_4_0= RULE_STRING ) )
            // InternalDsl.g:3115:4: (lv_on_4_0= RULE_STRING )
            {
            // InternalDsl.g:3115:4: (lv_on_4_0= RULE_STRING )
            // InternalDsl.g:3116:5: lv_on_4_0= RULE_STRING
            {
            lv_on_4_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_on_4_0, grammarAccess.getInsertSqlAccess().getOnSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getInsertSqlRule());
            					}
            					setWithLastConsumed(
            						current,
            						"on",
            						lv_on_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,30,FOLLOW_4); 

            			newLeafNode(otherlv_5, grammarAccess.getInsertSqlAccess().getUsingKeyword_5());
            		
            otherlv_6=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_6, grammarAccess.getInsertSqlAccess().getLeftCurlyBracketKeyword_6());
            		
            // InternalDsl.g:3140:3: ( (lv_value_7_0= ruleNonSelectStatement ) )
            // InternalDsl.g:3141:4: (lv_value_7_0= ruleNonSelectStatement )
            {
            // InternalDsl.g:3141:4: (lv_value_7_0= ruleNonSelectStatement )
            // InternalDsl.g:3142:5: lv_value_7_0= ruleNonSelectStatement
            {

            					newCompositeNode(grammarAccess.getInsertSqlAccess().getValueNonSelectStatementParserRuleCall_7_0());
            				
            pushFollow(FOLLOW_8);
            lv_value_7_0=ruleNonSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getInsertSqlRule());
            					}
            					add(
            						current,
            						"value",
            						lv_value_7_0,
            						"in.handyman.Dsl.NonSelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_8=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_8, grammarAccess.getInsertSqlAccess().getRightCurlyBracketKeyword_8());
            		
            otherlv_9=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_9, grammarAccess.getInsertSqlAccess().getOnConditionKeyword_9());
            		
            // InternalDsl.g:3167:3: ( (lv_condition_10_0= ruleExpression ) )
            // InternalDsl.g:3168:4: (lv_condition_10_0= ruleExpression )
            {
            // InternalDsl.g:3168:4: (lv_condition_10_0= ruleExpression )
            // InternalDsl.g:3169:5: lv_condition_10_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getInsertSqlAccess().getConditionExpressionParserRuleCall_10_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_10_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getInsertSqlRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_10_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleInsertSql"


    // $ANTLR start "entryRuleTruncateSql"
    // InternalDsl.g:3190:1: entryRuleTruncateSql returns [EObject current=null] : iv_ruleTruncateSql= ruleTruncateSql EOF ;
    public final EObject entryRuleTruncateSql() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleTruncateSql = null;


        try {
            // InternalDsl.g:3190:52: (iv_ruleTruncateSql= ruleTruncateSql EOF )
            // InternalDsl.g:3191:2: iv_ruleTruncateSql= ruleTruncateSql EOF
            {
             newCompositeNode(grammarAccess.getTruncateSqlRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleTruncateSql=ruleTruncateSql();

            state._fsp--;

             current =iv_ruleTruncateSql; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleTruncateSql"


    // $ANTLR start "ruleTruncateSql"
    // InternalDsl.g:3197:1: ruleTruncateSql returns [EObject current=null] : (otherlv_0= 'truncatesql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) ) ;
    public final EObject ruleTruncateSql() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_on_4_0=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        Token otherlv_8=null;
        Token otherlv_9=null;
        AntlrDatatypeRuleToken lv_value_7_0 = null;

        EObject lv_condition_10_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:3203:2: ( (otherlv_0= 'truncatesql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) ) )
            // InternalDsl.g:3204:2: (otherlv_0= 'truncatesql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) )
            {
            // InternalDsl.g:3204:2: (otherlv_0= 'truncatesql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) )
            // InternalDsl.g:3205:3: otherlv_0= 'truncatesql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,64,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getTruncateSqlAccess().getTruncatesqlKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getTruncateSqlAccess().getAsKeyword_1());
            		
            // InternalDsl.g:3213:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:3214:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:3214:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:3215:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_31); 

            					newLeafNode(lv_name_2_0, grammarAccess.getTruncateSqlAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getTruncateSqlRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,44,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getTruncateSqlAccess().getOnKeyword_3());
            		
            // InternalDsl.g:3235:3: ( (lv_on_4_0= RULE_STRING ) )
            // InternalDsl.g:3236:4: (lv_on_4_0= RULE_STRING )
            {
            // InternalDsl.g:3236:4: (lv_on_4_0= RULE_STRING )
            // InternalDsl.g:3237:5: lv_on_4_0= RULE_STRING
            {
            lv_on_4_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_on_4_0, grammarAccess.getTruncateSqlAccess().getOnSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getTruncateSqlRule());
            					}
            					setWithLastConsumed(
            						current,
            						"on",
            						lv_on_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,30,FOLLOW_4); 

            			newLeafNode(otherlv_5, grammarAccess.getTruncateSqlAccess().getUsingKeyword_5());
            		
            otherlv_6=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_6, grammarAccess.getTruncateSqlAccess().getLeftCurlyBracketKeyword_6());
            		
            // InternalDsl.g:3261:3: ( (lv_value_7_0= ruleNonSelectStatement ) )
            // InternalDsl.g:3262:4: (lv_value_7_0= ruleNonSelectStatement )
            {
            // InternalDsl.g:3262:4: (lv_value_7_0= ruleNonSelectStatement )
            // InternalDsl.g:3263:5: lv_value_7_0= ruleNonSelectStatement
            {

            					newCompositeNode(grammarAccess.getTruncateSqlAccess().getValueNonSelectStatementParserRuleCall_7_0());
            				
            pushFollow(FOLLOW_8);
            lv_value_7_0=ruleNonSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getTruncateSqlRule());
            					}
            					add(
            						current,
            						"value",
            						lv_value_7_0,
            						"in.handyman.Dsl.NonSelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_8=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_8, grammarAccess.getTruncateSqlAccess().getRightCurlyBracketKeyword_8());
            		
            otherlv_9=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_9, grammarAccess.getTruncateSqlAccess().getOnConditionKeyword_9());
            		
            // InternalDsl.g:3288:3: ( (lv_condition_10_0= ruleExpression ) )
            // InternalDsl.g:3289:4: (lv_condition_10_0= ruleExpression )
            {
            // InternalDsl.g:3289:4: (lv_condition_10_0= ruleExpression )
            // InternalDsl.g:3290:5: lv_condition_10_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getTruncateSqlAccess().getConditionExpressionParserRuleCall_10_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_10_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getTruncateSqlRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_10_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleTruncateSql"


    // $ANTLR start "entryRuleDropSql"
    // InternalDsl.g:3311:1: entryRuleDropSql returns [EObject current=null] : iv_ruleDropSql= ruleDropSql EOF ;
    public final EObject entryRuleDropSql() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleDropSql = null;


        try {
            // InternalDsl.g:3311:48: (iv_ruleDropSql= ruleDropSql EOF )
            // InternalDsl.g:3312:2: iv_ruleDropSql= ruleDropSql EOF
            {
             newCompositeNode(grammarAccess.getDropSqlRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleDropSql=ruleDropSql();

            state._fsp--;

             current =iv_ruleDropSql; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleDropSql"


    // $ANTLR start "ruleDropSql"
    // InternalDsl.g:3318:1: ruleDropSql returns [EObject current=null] : (otherlv_0= 'dropsql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) ) ;
    public final EObject ruleDropSql() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_on_4_0=null;
        Token otherlv_5=null;
        Token otherlv_6=null;
        Token otherlv_8=null;
        Token otherlv_9=null;
        AntlrDatatypeRuleToken lv_value_7_0 = null;

        EObject lv_condition_10_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:3324:2: ( (otherlv_0= 'dropsql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) ) )
            // InternalDsl.g:3325:2: (otherlv_0= 'dropsql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) )
            {
            // InternalDsl.g:3325:2: (otherlv_0= 'dropsql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) ) )
            // InternalDsl.g:3326:3: otherlv_0= 'dropsql' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'on' ( (lv_on_4_0= RULE_STRING ) ) otherlv_5= 'using' otherlv_6= '{' ( (lv_value_7_0= ruleNonSelectStatement ) ) otherlv_8= '}' otherlv_9= 'on-condition' ( (lv_condition_10_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,65,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getDropSqlAccess().getDropsqlKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getDropSqlAccess().getAsKeyword_1());
            		
            // InternalDsl.g:3334:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:3335:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:3335:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:3336:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_31); 

            					newLeafNode(lv_name_2_0, grammarAccess.getDropSqlAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getDropSqlRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,44,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getDropSqlAccess().getOnKeyword_3());
            		
            // InternalDsl.g:3356:3: ( (lv_on_4_0= RULE_STRING ) )
            // InternalDsl.g:3357:4: (lv_on_4_0= RULE_STRING )
            {
            // InternalDsl.g:3357:4: (lv_on_4_0= RULE_STRING )
            // InternalDsl.g:3358:5: lv_on_4_0= RULE_STRING
            {
            lv_on_4_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_on_4_0, grammarAccess.getDropSqlAccess().getOnSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getDropSqlRule());
            					}
            					setWithLastConsumed(
            						current,
            						"on",
            						lv_on_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,30,FOLLOW_4); 

            			newLeafNode(otherlv_5, grammarAccess.getDropSqlAccess().getUsingKeyword_5());
            		
            otherlv_6=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_6, grammarAccess.getDropSqlAccess().getLeftCurlyBracketKeyword_6());
            		
            // InternalDsl.g:3382:3: ( (lv_value_7_0= ruleNonSelectStatement ) )
            // InternalDsl.g:3383:4: (lv_value_7_0= ruleNonSelectStatement )
            {
            // InternalDsl.g:3383:4: (lv_value_7_0= ruleNonSelectStatement )
            // InternalDsl.g:3384:5: lv_value_7_0= ruleNonSelectStatement
            {

            					newCompositeNode(grammarAccess.getDropSqlAccess().getValueNonSelectStatementParserRuleCall_7_0());
            				
            pushFollow(FOLLOW_8);
            lv_value_7_0=ruleNonSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getDropSqlRule());
            					}
            					add(
            						current,
            						"value",
            						lv_value_7_0,
            						"in.handyman.Dsl.NonSelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_8=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_8, grammarAccess.getDropSqlAccess().getRightCurlyBracketKeyword_8());
            		
            otherlv_9=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_9, grammarAccess.getDropSqlAccess().getOnConditionKeyword_9());
            		
            // InternalDsl.g:3409:3: ( (lv_condition_10_0= ruleExpression ) )
            // InternalDsl.g:3410:4: (lv_condition_10_0= ruleExpression )
            {
            // InternalDsl.g:3410:4: (lv_condition_10_0= ruleExpression )
            // InternalDsl.g:3411:5: lv_condition_10_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getDropSqlAccess().getConditionExpressionParserRuleCall_10_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_10_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getDropSqlRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_10_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleDropSql"


    // $ANTLR start "entryRuleListFiles"
    // InternalDsl.g:3432:1: entryRuleListFiles returns [EObject current=null] : iv_ruleListFiles= ruleListFiles EOF ;
    public final EObject entryRuleListFiles() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleListFiles = null;


        try {
            // InternalDsl.g:3432:50: (iv_ruleListFiles= ruleListFiles EOF )
            // InternalDsl.g:3433:2: iv_ruleListFiles= ruleListFiles EOF
            {
             newCompositeNode(grammarAccess.getListFilesRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleListFiles=ruleListFiles();

            state._fsp--;

             current =iv_ruleListFiles; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleListFiles"


    // $ANTLR start "ruleListFiles"
    // InternalDsl.g:3439:1: ruleListFiles returns [EObject current=null] : (otherlv_0= 'listfiles' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'on' ( (lv_to_6_0= RULE_STRING ) ) otherlv_7= 'target-table' ( (lv_targetTable_8_0= RULE_STRING ) ) otherlv_9= 'using' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) ) ) ;
    public final EObject ruleListFiles() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_source_4_0=null;
        Token otherlv_5=null;
        Token lv_to_6_0=null;
        Token otherlv_7=null;
        Token lv_targetTable_8_0=null;
        Token otherlv_9=null;
        Token otherlv_10=null;
        Token otherlv_12=null;
        Token otherlv_13=null;
        AntlrDatatypeRuleToken lv_value_11_0 = null;

        EObject lv_condition_14_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:3445:2: ( (otherlv_0= 'listfiles' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'on' ( (lv_to_6_0= RULE_STRING ) ) otherlv_7= 'target-table' ( (lv_targetTable_8_0= RULE_STRING ) ) otherlv_9= 'using' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) ) ) )
            // InternalDsl.g:3446:2: (otherlv_0= 'listfiles' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'on' ( (lv_to_6_0= RULE_STRING ) ) otherlv_7= 'target-table' ( (lv_targetTable_8_0= RULE_STRING ) ) otherlv_9= 'using' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) ) )
            {
            // InternalDsl.g:3446:2: (otherlv_0= 'listfiles' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'on' ( (lv_to_6_0= RULE_STRING ) ) otherlv_7= 'target-table' ( (lv_targetTable_8_0= RULE_STRING ) ) otherlv_9= 'using' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) ) )
            // InternalDsl.g:3447:3: otherlv_0= 'listfiles' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'on' ( (lv_to_6_0= RULE_STRING ) ) otherlv_7= 'target-table' ( (lv_targetTable_8_0= RULE_STRING ) ) otherlv_9= 'using' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,66,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getListFilesAccess().getListfilesKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getListFilesAccess().getAsKeyword_1());
            		
            // InternalDsl.g:3455:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:3456:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:3456:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:3457:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_14); 

            					newLeafNode(lv_name_2_0, grammarAccess.getListFilesAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getListFilesRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,22,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getListFilesAccess().getFromKeyword_3());
            		
            // InternalDsl.g:3477:3: ( (lv_source_4_0= RULE_STRING ) )
            // InternalDsl.g:3478:4: (lv_source_4_0= RULE_STRING )
            {
            // InternalDsl.g:3478:4: (lv_source_4_0= RULE_STRING )
            // InternalDsl.g:3479:5: lv_source_4_0= RULE_STRING
            {
            lv_source_4_0=(Token)match(input,RULE_STRING,FOLLOW_31); 

            					newLeafNode(lv_source_4_0, grammarAccess.getListFilesAccess().getSourceSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getListFilesRule());
            					}
            					setWithLastConsumed(
            						current,
            						"source",
            						lv_source_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,44,FOLLOW_3); 

            			newLeafNode(otherlv_5, grammarAccess.getListFilesAccess().getOnKeyword_5());
            		
            // InternalDsl.g:3499:3: ( (lv_to_6_0= RULE_STRING ) )
            // InternalDsl.g:3500:4: (lv_to_6_0= RULE_STRING )
            {
            // InternalDsl.g:3500:4: (lv_to_6_0= RULE_STRING )
            // InternalDsl.g:3501:5: lv_to_6_0= RULE_STRING
            {
            lv_to_6_0=(Token)match(input,RULE_STRING,FOLLOW_41); 

            					newLeafNode(lv_to_6_0, grammarAccess.getListFilesAccess().getToSTRINGTerminalRuleCall_6_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getListFilesRule());
            					}
            					setWithLastConsumed(
            						current,
            						"to",
            						lv_to_6_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_7=(Token)match(input,67,FOLLOW_3); 

            			newLeafNode(otherlv_7, grammarAccess.getListFilesAccess().getTargetTableKeyword_7());
            		
            // InternalDsl.g:3521:3: ( (lv_targetTable_8_0= RULE_STRING ) )
            // InternalDsl.g:3522:4: (lv_targetTable_8_0= RULE_STRING )
            {
            // InternalDsl.g:3522:4: (lv_targetTable_8_0= RULE_STRING )
            // InternalDsl.g:3523:5: lv_targetTable_8_0= RULE_STRING
            {
            lv_targetTable_8_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_targetTable_8_0, grammarAccess.getListFilesAccess().getTargetTableSTRINGTerminalRuleCall_8_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getListFilesRule());
            					}
            					setWithLastConsumed(
            						current,
            						"targetTable",
            						lv_targetTable_8_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_9=(Token)match(input,30,FOLLOW_4); 

            			newLeafNode(otherlv_9, grammarAccess.getListFilesAccess().getUsingKeyword_9());
            		
            otherlv_10=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_10, grammarAccess.getListFilesAccess().getLeftCurlyBracketKeyword_10());
            		
            // InternalDsl.g:3547:3: ( (lv_value_11_0= ruleSelectStatement ) )
            // InternalDsl.g:3548:4: (lv_value_11_0= ruleSelectStatement )
            {
            // InternalDsl.g:3548:4: (lv_value_11_0= ruleSelectStatement )
            // InternalDsl.g:3549:5: lv_value_11_0= ruleSelectStatement
            {

            					newCompositeNode(grammarAccess.getListFilesAccess().getValueSelectStatementParserRuleCall_11_0());
            				
            pushFollow(FOLLOW_8);
            lv_value_11_0=ruleSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getListFilesRule());
            					}
            					set(
            						current,
            						"value",
            						lv_value_11_0,
            						"in.handyman.Dsl.SelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_12=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_12, grammarAccess.getListFilesAccess().getRightCurlyBracketKeyword_12());
            		
            otherlv_13=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_13, grammarAccess.getListFilesAccess().getOnConditionKeyword_13());
            		
            // InternalDsl.g:3574:3: ( (lv_condition_14_0= ruleExpression ) )
            // InternalDsl.g:3575:4: (lv_condition_14_0= ruleExpression )
            {
            // InternalDsl.g:3575:4: (lv_condition_14_0= ruleExpression )
            // InternalDsl.g:3576:5: lv_condition_14_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getListFilesAccess().getConditionExpressionParserRuleCall_14_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_14_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getListFilesRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_14_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleListFiles"


    // $ANTLR start "entryRuleMongo2Db"
    // InternalDsl.g:3597:1: entryRuleMongo2Db returns [EObject current=null] : iv_ruleMongo2Db= ruleMongo2Db EOF ;
    public final EObject entryRuleMongo2Db() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleMongo2Db = null;


        try {
            // InternalDsl.g:3597:49: (iv_ruleMongo2Db= ruleMongo2Db EOF )
            // InternalDsl.g:3598:2: iv_ruleMongo2Db= ruleMongo2Db EOF
            {
             newCompositeNode(grammarAccess.getMongo2DbRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleMongo2Db=ruleMongo2Db();

            state._fsp--;

             current =iv_ruleMongo2Db; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleMongo2Db"


    // $ANTLR start "ruleMongo2Db"
    // InternalDsl.g:3604:1: ruleMongo2Db returns [EObject current=null] : (otherlv_0= 'mongo2db' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_sourceConnStr_4_0= RULE_STRING ) ) otherlv_5= 'to' ( (lv_to_6_0= RULE_STRING ) ) otherlv_7= 'srcDb' ( (lv_sourceDb_8_0= RULE_STRING ) ) otherlv_9= 'targetDb' ( (lv_targetDb_10_0= RULE_STRING ) ) otherlv_11= 'targetTable' ( (lv_targetTable_12_0= RULE_STRING ) ) otherlv_13= 'by-filter' ( (lv_filter_14_0= RULE_STRING ) ) otherlv_15= 'by-batch' ( (lv_limit_16_0= RULE_STRING ) ) otherlv_17= 'find-attribute' ( (lv_findAttr_18_0= RULE_STRING ) ) otherlv_19= 'apply-manipulation' ( (lv_applyManipulation_20_0= RULE_STRING ) ) otherlv_21= 'on-updatekey' ( (lv_onUpdateKey_22_0= RULE_STRING ) ) otherlv_23= 'with-fetch-batch-size' ( (lv_fetchBatchSize_24_0= RULE_STRING ) ) otherlv_25= 'with-write-batch-size' ( (lv_writeBatchSize_26_0= RULE_STRING ) ) otherlv_27= 'using' otherlv_28= '{' ( (lv_value_29_0= ruleSelectStatement ) ) otherlv_30= '}' otherlv_31= 'on-condition' ( (lv_condition_32_0= ruleExpression ) ) ) ;
    public final EObject ruleMongo2Db() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_sourceConnStr_4_0=null;
        Token otherlv_5=null;
        Token lv_to_6_0=null;
        Token otherlv_7=null;
        Token lv_sourceDb_8_0=null;
        Token otherlv_9=null;
        Token lv_targetDb_10_0=null;
        Token otherlv_11=null;
        Token lv_targetTable_12_0=null;
        Token otherlv_13=null;
        Token lv_filter_14_0=null;
        Token otherlv_15=null;
        Token lv_limit_16_0=null;
        Token otherlv_17=null;
        Token lv_findAttr_18_0=null;
        Token otherlv_19=null;
        Token lv_applyManipulation_20_0=null;
        Token otherlv_21=null;
        Token lv_onUpdateKey_22_0=null;
        Token otherlv_23=null;
        Token lv_fetchBatchSize_24_0=null;
        Token otherlv_25=null;
        Token lv_writeBatchSize_26_0=null;
        Token otherlv_27=null;
        Token otherlv_28=null;
        Token otherlv_30=null;
        Token otherlv_31=null;
        AntlrDatatypeRuleToken lv_value_29_0 = null;

        EObject lv_condition_32_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:3610:2: ( (otherlv_0= 'mongo2db' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_sourceConnStr_4_0= RULE_STRING ) ) otherlv_5= 'to' ( (lv_to_6_0= RULE_STRING ) ) otherlv_7= 'srcDb' ( (lv_sourceDb_8_0= RULE_STRING ) ) otherlv_9= 'targetDb' ( (lv_targetDb_10_0= RULE_STRING ) ) otherlv_11= 'targetTable' ( (lv_targetTable_12_0= RULE_STRING ) ) otherlv_13= 'by-filter' ( (lv_filter_14_0= RULE_STRING ) ) otherlv_15= 'by-batch' ( (lv_limit_16_0= RULE_STRING ) ) otherlv_17= 'find-attribute' ( (lv_findAttr_18_0= RULE_STRING ) ) otherlv_19= 'apply-manipulation' ( (lv_applyManipulation_20_0= RULE_STRING ) ) otherlv_21= 'on-updatekey' ( (lv_onUpdateKey_22_0= RULE_STRING ) ) otherlv_23= 'with-fetch-batch-size' ( (lv_fetchBatchSize_24_0= RULE_STRING ) ) otherlv_25= 'with-write-batch-size' ( (lv_writeBatchSize_26_0= RULE_STRING ) ) otherlv_27= 'using' otherlv_28= '{' ( (lv_value_29_0= ruleSelectStatement ) ) otherlv_30= '}' otherlv_31= 'on-condition' ( (lv_condition_32_0= ruleExpression ) ) ) )
            // InternalDsl.g:3611:2: (otherlv_0= 'mongo2db' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_sourceConnStr_4_0= RULE_STRING ) ) otherlv_5= 'to' ( (lv_to_6_0= RULE_STRING ) ) otherlv_7= 'srcDb' ( (lv_sourceDb_8_0= RULE_STRING ) ) otherlv_9= 'targetDb' ( (lv_targetDb_10_0= RULE_STRING ) ) otherlv_11= 'targetTable' ( (lv_targetTable_12_0= RULE_STRING ) ) otherlv_13= 'by-filter' ( (lv_filter_14_0= RULE_STRING ) ) otherlv_15= 'by-batch' ( (lv_limit_16_0= RULE_STRING ) ) otherlv_17= 'find-attribute' ( (lv_findAttr_18_0= RULE_STRING ) ) otherlv_19= 'apply-manipulation' ( (lv_applyManipulation_20_0= RULE_STRING ) ) otherlv_21= 'on-updatekey' ( (lv_onUpdateKey_22_0= RULE_STRING ) ) otherlv_23= 'with-fetch-batch-size' ( (lv_fetchBatchSize_24_0= RULE_STRING ) ) otherlv_25= 'with-write-batch-size' ( (lv_writeBatchSize_26_0= RULE_STRING ) ) otherlv_27= 'using' otherlv_28= '{' ( (lv_value_29_0= ruleSelectStatement ) ) otherlv_30= '}' otherlv_31= 'on-condition' ( (lv_condition_32_0= ruleExpression ) ) )
            {
            // InternalDsl.g:3611:2: (otherlv_0= 'mongo2db' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_sourceConnStr_4_0= RULE_STRING ) ) otherlv_5= 'to' ( (lv_to_6_0= RULE_STRING ) ) otherlv_7= 'srcDb' ( (lv_sourceDb_8_0= RULE_STRING ) ) otherlv_9= 'targetDb' ( (lv_targetDb_10_0= RULE_STRING ) ) otherlv_11= 'targetTable' ( (lv_targetTable_12_0= RULE_STRING ) ) otherlv_13= 'by-filter' ( (lv_filter_14_0= RULE_STRING ) ) otherlv_15= 'by-batch' ( (lv_limit_16_0= RULE_STRING ) ) otherlv_17= 'find-attribute' ( (lv_findAttr_18_0= RULE_STRING ) ) otherlv_19= 'apply-manipulation' ( (lv_applyManipulation_20_0= RULE_STRING ) ) otherlv_21= 'on-updatekey' ( (lv_onUpdateKey_22_0= RULE_STRING ) ) otherlv_23= 'with-fetch-batch-size' ( (lv_fetchBatchSize_24_0= RULE_STRING ) ) otherlv_25= 'with-write-batch-size' ( (lv_writeBatchSize_26_0= RULE_STRING ) ) otherlv_27= 'using' otherlv_28= '{' ( (lv_value_29_0= ruleSelectStatement ) ) otherlv_30= '}' otherlv_31= 'on-condition' ( (lv_condition_32_0= ruleExpression ) ) )
            // InternalDsl.g:3612:3: otherlv_0= 'mongo2db' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_sourceConnStr_4_0= RULE_STRING ) ) otherlv_5= 'to' ( (lv_to_6_0= RULE_STRING ) ) otherlv_7= 'srcDb' ( (lv_sourceDb_8_0= RULE_STRING ) ) otherlv_9= 'targetDb' ( (lv_targetDb_10_0= RULE_STRING ) ) otherlv_11= 'targetTable' ( (lv_targetTable_12_0= RULE_STRING ) ) otherlv_13= 'by-filter' ( (lv_filter_14_0= RULE_STRING ) ) otherlv_15= 'by-batch' ( (lv_limit_16_0= RULE_STRING ) ) otherlv_17= 'find-attribute' ( (lv_findAttr_18_0= RULE_STRING ) ) otherlv_19= 'apply-manipulation' ( (lv_applyManipulation_20_0= RULE_STRING ) ) otherlv_21= 'on-updatekey' ( (lv_onUpdateKey_22_0= RULE_STRING ) ) otherlv_23= 'with-fetch-batch-size' ( (lv_fetchBatchSize_24_0= RULE_STRING ) ) otherlv_25= 'with-write-batch-size' ( (lv_writeBatchSize_26_0= RULE_STRING ) ) otherlv_27= 'using' otherlv_28= '{' ( (lv_value_29_0= ruleSelectStatement ) ) otherlv_30= '}' otherlv_31= 'on-condition' ( (lv_condition_32_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,68,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getMongo2DbAccess().getMongo2dbKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getMongo2DbAccess().getAsKeyword_1());
            		
            // InternalDsl.g:3620:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:3621:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:3621:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:3622:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_14); 

            					newLeafNode(lv_name_2_0, grammarAccess.getMongo2DbAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getMongo2DbRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,22,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getMongo2DbAccess().getFromKeyword_3());
            		
            // InternalDsl.g:3642:3: ( (lv_sourceConnStr_4_0= RULE_STRING ) )
            // InternalDsl.g:3643:4: (lv_sourceConnStr_4_0= RULE_STRING )
            {
            // InternalDsl.g:3643:4: (lv_sourceConnStr_4_0= RULE_STRING )
            // InternalDsl.g:3644:5: lv_sourceConnStr_4_0= RULE_STRING
            {
            lv_sourceConnStr_4_0=(Token)match(input,RULE_STRING,FOLLOW_16); 

            					newLeafNode(lv_sourceConnStr_4_0, grammarAccess.getMongo2DbAccess().getSourceConnStrSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getMongo2DbRule());
            					}
            					setWithLastConsumed(
            						current,
            						"sourceConnStr",
            						lv_sourceConnStr_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,24,FOLLOW_3); 

            			newLeafNode(otherlv_5, grammarAccess.getMongo2DbAccess().getToKeyword_5());
            		
            // InternalDsl.g:3664:3: ( (lv_to_6_0= RULE_STRING ) )
            // InternalDsl.g:3665:4: (lv_to_6_0= RULE_STRING )
            {
            // InternalDsl.g:3665:4: (lv_to_6_0= RULE_STRING )
            // InternalDsl.g:3666:5: lv_to_6_0= RULE_STRING
            {
            lv_to_6_0=(Token)match(input,RULE_STRING,FOLLOW_42); 

            					newLeafNode(lv_to_6_0, grammarAccess.getMongo2DbAccess().getToSTRINGTerminalRuleCall_6_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getMongo2DbRule());
            					}
            					setWithLastConsumed(
            						current,
            						"to",
            						lv_to_6_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_7=(Token)match(input,69,FOLLOW_3); 

            			newLeafNode(otherlv_7, grammarAccess.getMongo2DbAccess().getSrcDbKeyword_7());
            		
            // InternalDsl.g:3686:3: ( (lv_sourceDb_8_0= RULE_STRING ) )
            // InternalDsl.g:3687:4: (lv_sourceDb_8_0= RULE_STRING )
            {
            // InternalDsl.g:3687:4: (lv_sourceDb_8_0= RULE_STRING )
            // InternalDsl.g:3688:5: lv_sourceDb_8_0= RULE_STRING
            {
            lv_sourceDb_8_0=(Token)match(input,RULE_STRING,FOLLOW_43); 

            					newLeafNode(lv_sourceDb_8_0, grammarAccess.getMongo2DbAccess().getSourceDbSTRINGTerminalRuleCall_8_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getMongo2DbRule());
            					}
            					setWithLastConsumed(
            						current,
            						"sourceDb",
            						lv_sourceDb_8_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_9=(Token)match(input,70,FOLLOW_3); 

            			newLeafNode(otherlv_9, grammarAccess.getMongo2DbAccess().getTargetDbKeyword_9());
            		
            // InternalDsl.g:3708:3: ( (lv_targetDb_10_0= RULE_STRING ) )
            // InternalDsl.g:3709:4: (lv_targetDb_10_0= RULE_STRING )
            {
            // InternalDsl.g:3709:4: (lv_targetDb_10_0= RULE_STRING )
            // InternalDsl.g:3710:5: lv_targetDb_10_0= RULE_STRING
            {
            lv_targetDb_10_0=(Token)match(input,RULE_STRING,FOLLOW_44); 

            					newLeafNode(lv_targetDb_10_0, grammarAccess.getMongo2DbAccess().getTargetDbSTRINGTerminalRuleCall_10_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getMongo2DbRule());
            					}
            					setWithLastConsumed(
            						current,
            						"targetDb",
            						lv_targetDb_10_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_11=(Token)match(input,71,FOLLOW_3); 

            			newLeafNode(otherlv_11, grammarAccess.getMongo2DbAccess().getTargetTableKeyword_11());
            		
            // InternalDsl.g:3730:3: ( (lv_targetTable_12_0= RULE_STRING ) )
            // InternalDsl.g:3731:4: (lv_targetTable_12_0= RULE_STRING )
            {
            // InternalDsl.g:3731:4: (lv_targetTable_12_0= RULE_STRING )
            // InternalDsl.g:3732:5: lv_targetTable_12_0= RULE_STRING
            {
            lv_targetTable_12_0=(Token)match(input,RULE_STRING,FOLLOW_45); 

            					newLeafNode(lv_targetTable_12_0, grammarAccess.getMongo2DbAccess().getTargetTableSTRINGTerminalRuleCall_12_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getMongo2DbRule());
            					}
            					setWithLastConsumed(
            						current,
            						"targetTable",
            						lv_targetTable_12_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_13=(Token)match(input,72,FOLLOW_3); 

            			newLeafNode(otherlv_13, grammarAccess.getMongo2DbAccess().getByFilterKeyword_13());
            		
            // InternalDsl.g:3752:3: ( (lv_filter_14_0= RULE_STRING ) )
            // InternalDsl.g:3753:4: (lv_filter_14_0= RULE_STRING )
            {
            // InternalDsl.g:3753:4: (lv_filter_14_0= RULE_STRING )
            // InternalDsl.g:3754:5: lv_filter_14_0= RULE_STRING
            {
            lv_filter_14_0=(Token)match(input,RULE_STRING,FOLLOW_38); 

            					newLeafNode(lv_filter_14_0, grammarAccess.getMongo2DbAccess().getFilterSTRINGTerminalRuleCall_14_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getMongo2DbRule());
            					}
            					setWithLastConsumed(
            						current,
            						"filter",
            						lv_filter_14_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_15=(Token)match(input,55,FOLLOW_3); 

            			newLeafNode(otherlv_15, grammarAccess.getMongo2DbAccess().getByBatchKeyword_15());
            		
            // InternalDsl.g:3774:3: ( (lv_limit_16_0= RULE_STRING ) )
            // InternalDsl.g:3775:4: (lv_limit_16_0= RULE_STRING )
            {
            // InternalDsl.g:3775:4: (lv_limit_16_0= RULE_STRING )
            // InternalDsl.g:3776:5: lv_limit_16_0= RULE_STRING
            {
            lv_limit_16_0=(Token)match(input,RULE_STRING,FOLLOW_46); 

            					newLeafNode(lv_limit_16_0, grammarAccess.getMongo2DbAccess().getLimitSTRINGTerminalRuleCall_16_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getMongo2DbRule());
            					}
            					setWithLastConsumed(
            						current,
            						"limit",
            						lv_limit_16_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_17=(Token)match(input,73,FOLLOW_3); 

            			newLeafNode(otherlv_17, grammarAccess.getMongo2DbAccess().getFindAttributeKeyword_17());
            		
            // InternalDsl.g:3796:3: ( (lv_findAttr_18_0= RULE_STRING ) )
            // InternalDsl.g:3797:4: (lv_findAttr_18_0= RULE_STRING )
            {
            // InternalDsl.g:3797:4: (lv_findAttr_18_0= RULE_STRING )
            // InternalDsl.g:3798:5: lv_findAttr_18_0= RULE_STRING
            {
            lv_findAttr_18_0=(Token)match(input,RULE_STRING,FOLLOW_47); 

            					newLeafNode(lv_findAttr_18_0, grammarAccess.getMongo2DbAccess().getFindAttrSTRINGTerminalRuleCall_18_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getMongo2DbRule());
            					}
            					setWithLastConsumed(
            						current,
            						"findAttr",
            						lv_findAttr_18_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_19=(Token)match(input,74,FOLLOW_3); 

            			newLeafNode(otherlv_19, grammarAccess.getMongo2DbAccess().getApplyManipulationKeyword_19());
            		
            // InternalDsl.g:3818:3: ( (lv_applyManipulation_20_0= RULE_STRING ) )
            // InternalDsl.g:3819:4: (lv_applyManipulation_20_0= RULE_STRING )
            {
            // InternalDsl.g:3819:4: (lv_applyManipulation_20_0= RULE_STRING )
            // InternalDsl.g:3820:5: lv_applyManipulation_20_0= RULE_STRING
            {
            lv_applyManipulation_20_0=(Token)match(input,RULE_STRING,FOLLOW_48); 

            					newLeafNode(lv_applyManipulation_20_0, grammarAccess.getMongo2DbAccess().getApplyManipulationSTRINGTerminalRuleCall_20_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getMongo2DbRule());
            					}
            					setWithLastConsumed(
            						current,
            						"applyManipulation",
            						lv_applyManipulation_20_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_21=(Token)match(input,75,FOLLOW_3); 

            			newLeafNode(otherlv_21, grammarAccess.getMongo2DbAccess().getOnUpdatekeyKeyword_21());
            		
            // InternalDsl.g:3840:3: ( (lv_onUpdateKey_22_0= RULE_STRING ) )
            // InternalDsl.g:3841:4: (lv_onUpdateKey_22_0= RULE_STRING )
            {
            // InternalDsl.g:3841:4: (lv_onUpdateKey_22_0= RULE_STRING )
            // InternalDsl.g:3842:5: lv_onUpdateKey_22_0= RULE_STRING
            {
            lv_onUpdateKey_22_0=(Token)match(input,RULE_STRING,FOLLOW_49); 

            					newLeafNode(lv_onUpdateKey_22_0, grammarAccess.getMongo2DbAccess().getOnUpdateKeySTRINGTerminalRuleCall_22_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getMongo2DbRule());
            					}
            					setWithLastConsumed(
            						current,
            						"onUpdateKey",
            						lv_onUpdateKey_22_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_23=(Token)match(input,76,FOLLOW_3); 

            			newLeafNode(otherlv_23, grammarAccess.getMongo2DbAccess().getWithFetchBatchSizeKeyword_23());
            		
            // InternalDsl.g:3862:3: ( (lv_fetchBatchSize_24_0= RULE_STRING ) )
            // InternalDsl.g:3863:4: (lv_fetchBatchSize_24_0= RULE_STRING )
            {
            // InternalDsl.g:3863:4: (lv_fetchBatchSize_24_0= RULE_STRING )
            // InternalDsl.g:3864:5: lv_fetchBatchSize_24_0= RULE_STRING
            {
            lv_fetchBatchSize_24_0=(Token)match(input,RULE_STRING,FOLLOW_50); 

            					newLeafNode(lv_fetchBatchSize_24_0, grammarAccess.getMongo2DbAccess().getFetchBatchSizeSTRINGTerminalRuleCall_24_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getMongo2DbRule());
            					}
            					setWithLastConsumed(
            						current,
            						"fetchBatchSize",
            						lv_fetchBatchSize_24_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_25=(Token)match(input,77,FOLLOW_3); 

            			newLeafNode(otherlv_25, grammarAccess.getMongo2DbAccess().getWithWriteBatchSizeKeyword_25());
            		
            // InternalDsl.g:3884:3: ( (lv_writeBatchSize_26_0= RULE_STRING ) )
            // InternalDsl.g:3885:4: (lv_writeBatchSize_26_0= RULE_STRING )
            {
            // InternalDsl.g:3885:4: (lv_writeBatchSize_26_0= RULE_STRING )
            // InternalDsl.g:3886:5: lv_writeBatchSize_26_0= RULE_STRING
            {
            lv_writeBatchSize_26_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_writeBatchSize_26_0, grammarAccess.getMongo2DbAccess().getWriteBatchSizeSTRINGTerminalRuleCall_26_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getMongo2DbRule());
            					}
            					setWithLastConsumed(
            						current,
            						"writeBatchSize",
            						lv_writeBatchSize_26_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_27=(Token)match(input,30,FOLLOW_4); 

            			newLeafNode(otherlv_27, grammarAccess.getMongo2DbAccess().getUsingKeyword_27());
            		
            otherlv_28=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_28, grammarAccess.getMongo2DbAccess().getLeftCurlyBracketKeyword_28());
            		
            // InternalDsl.g:3910:3: ( (lv_value_29_0= ruleSelectStatement ) )
            // InternalDsl.g:3911:4: (lv_value_29_0= ruleSelectStatement )
            {
            // InternalDsl.g:3911:4: (lv_value_29_0= ruleSelectStatement )
            // InternalDsl.g:3912:5: lv_value_29_0= ruleSelectStatement
            {

            					newCompositeNode(grammarAccess.getMongo2DbAccess().getValueSelectStatementParserRuleCall_29_0());
            				
            pushFollow(FOLLOW_8);
            lv_value_29_0=ruleSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getMongo2DbRule());
            					}
            					set(
            						current,
            						"value",
            						lv_value_29_0,
            						"in.handyman.Dsl.SelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_30=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_30, grammarAccess.getMongo2DbAccess().getRightCurlyBracketKeyword_30());
            		
            otherlv_31=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_31, grammarAccess.getMongo2DbAccess().getOnConditionKeyword_31());
            		
            // InternalDsl.g:3937:3: ( (lv_condition_32_0= ruleExpression ) )
            // InternalDsl.g:3938:4: (lv_condition_32_0= ruleExpression )
            {
            // InternalDsl.g:3938:4: (lv_condition_32_0= ruleExpression )
            // InternalDsl.g:3939:5: lv_condition_32_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getMongo2DbAccess().getConditionExpressionParserRuleCall_32_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_32_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getMongo2DbRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_32_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleMongo2Db"


    // $ANTLR start "entryRuleFTP"
    // InternalDsl.g:3960:1: entryRuleFTP returns [EObject current=null] : iv_ruleFTP= ruleFTP EOF ;
    public final EObject entryRuleFTP() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleFTP = null;


        try {
            // InternalDsl.g:3960:44: (iv_ruleFTP= ruleFTP EOF )
            // InternalDsl.g:3961:2: iv_ruleFTP= ruleFTP EOF
            {
             newCompositeNode(grammarAccess.getFTPRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleFTP=ruleFTP();

            state._fsp--;

             current =iv_ruleFTP; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleFTP"


    // $ANTLR start "ruleFTP"
    // InternalDsl.g:3967:1: ruleFTP returns [EObject current=null] : (otherlv_0= 'ftp' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'host' ( (lv_host_4_0= RULE_STRING ) ) otherlv_5= 'port' ( (lv_port_6_0= RULE_STRING ) ) otherlv_7= 'username' ( (lv_username_8_0= RULE_STRING ) ) otherlv_9= 'password' ( (lv_password_10_0= RULE_STRING ) ) otherlv_11= 'action' ( (lv_action_12_0= RULE_STRING ) ) otherlv_13= 'local-dir' ( (lv_localDir_14_0= RULE_STRING ) ) otherlv_15= 'local-file' ( (lv_localFile_16_0= RULE_STRING ) ) otherlv_17= 'remote-dir' ( (lv_remoteDir_18_0= RULE_STRING ) ) otherlv_19= 'remote-file' ( (lv_remoteFile_20_0= RULE_STRING ) ) otherlv_21= 'from' ( (lv_source_22_0= RULE_STRING ) ) otherlv_23= 'target-table' ( (lv_targetTable_24_0= RULE_STRING ) ) otherlv_25= 'using' otherlv_26= '{' ( (lv_value_27_0= RULE_STRING ) ) otherlv_28= '}' otherlv_29= 'on-condition' ( (lv_condition_30_0= ruleExpression ) ) ) ;
    public final EObject ruleFTP() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_host_4_0=null;
        Token otherlv_5=null;
        Token lv_port_6_0=null;
        Token otherlv_7=null;
        Token lv_username_8_0=null;
        Token otherlv_9=null;
        Token lv_password_10_0=null;
        Token otherlv_11=null;
        Token lv_action_12_0=null;
        Token otherlv_13=null;
        Token lv_localDir_14_0=null;
        Token otherlv_15=null;
        Token lv_localFile_16_0=null;
        Token otherlv_17=null;
        Token lv_remoteDir_18_0=null;
        Token otherlv_19=null;
        Token lv_remoteFile_20_0=null;
        Token otherlv_21=null;
        Token lv_source_22_0=null;
        Token otherlv_23=null;
        Token lv_targetTable_24_0=null;
        Token otherlv_25=null;
        Token otherlv_26=null;
        Token lv_value_27_0=null;
        Token otherlv_28=null;
        Token otherlv_29=null;
        EObject lv_condition_30_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:3973:2: ( (otherlv_0= 'ftp' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'host' ( (lv_host_4_0= RULE_STRING ) ) otherlv_5= 'port' ( (lv_port_6_0= RULE_STRING ) ) otherlv_7= 'username' ( (lv_username_8_0= RULE_STRING ) ) otherlv_9= 'password' ( (lv_password_10_0= RULE_STRING ) ) otherlv_11= 'action' ( (lv_action_12_0= RULE_STRING ) ) otherlv_13= 'local-dir' ( (lv_localDir_14_0= RULE_STRING ) ) otherlv_15= 'local-file' ( (lv_localFile_16_0= RULE_STRING ) ) otherlv_17= 'remote-dir' ( (lv_remoteDir_18_0= RULE_STRING ) ) otherlv_19= 'remote-file' ( (lv_remoteFile_20_0= RULE_STRING ) ) otherlv_21= 'from' ( (lv_source_22_0= RULE_STRING ) ) otherlv_23= 'target-table' ( (lv_targetTable_24_0= RULE_STRING ) ) otherlv_25= 'using' otherlv_26= '{' ( (lv_value_27_0= RULE_STRING ) ) otherlv_28= '}' otherlv_29= 'on-condition' ( (lv_condition_30_0= ruleExpression ) ) ) )
            // InternalDsl.g:3974:2: (otherlv_0= 'ftp' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'host' ( (lv_host_4_0= RULE_STRING ) ) otherlv_5= 'port' ( (lv_port_6_0= RULE_STRING ) ) otherlv_7= 'username' ( (lv_username_8_0= RULE_STRING ) ) otherlv_9= 'password' ( (lv_password_10_0= RULE_STRING ) ) otherlv_11= 'action' ( (lv_action_12_0= RULE_STRING ) ) otherlv_13= 'local-dir' ( (lv_localDir_14_0= RULE_STRING ) ) otherlv_15= 'local-file' ( (lv_localFile_16_0= RULE_STRING ) ) otherlv_17= 'remote-dir' ( (lv_remoteDir_18_0= RULE_STRING ) ) otherlv_19= 'remote-file' ( (lv_remoteFile_20_0= RULE_STRING ) ) otherlv_21= 'from' ( (lv_source_22_0= RULE_STRING ) ) otherlv_23= 'target-table' ( (lv_targetTable_24_0= RULE_STRING ) ) otherlv_25= 'using' otherlv_26= '{' ( (lv_value_27_0= RULE_STRING ) ) otherlv_28= '}' otherlv_29= 'on-condition' ( (lv_condition_30_0= ruleExpression ) ) )
            {
            // InternalDsl.g:3974:2: (otherlv_0= 'ftp' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'host' ( (lv_host_4_0= RULE_STRING ) ) otherlv_5= 'port' ( (lv_port_6_0= RULE_STRING ) ) otherlv_7= 'username' ( (lv_username_8_0= RULE_STRING ) ) otherlv_9= 'password' ( (lv_password_10_0= RULE_STRING ) ) otherlv_11= 'action' ( (lv_action_12_0= RULE_STRING ) ) otherlv_13= 'local-dir' ( (lv_localDir_14_0= RULE_STRING ) ) otherlv_15= 'local-file' ( (lv_localFile_16_0= RULE_STRING ) ) otherlv_17= 'remote-dir' ( (lv_remoteDir_18_0= RULE_STRING ) ) otherlv_19= 'remote-file' ( (lv_remoteFile_20_0= RULE_STRING ) ) otherlv_21= 'from' ( (lv_source_22_0= RULE_STRING ) ) otherlv_23= 'target-table' ( (lv_targetTable_24_0= RULE_STRING ) ) otherlv_25= 'using' otherlv_26= '{' ( (lv_value_27_0= RULE_STRING ) ) otherlv_28= '}' otherlv_29= 'on-condition' ( (lv_condition_30_0= ruleExpression ) ) )
            // InternalDsl.g:3975:3: otherlv_0= 'ftp' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'host' ( (lv_host_4_0= RULE_STRING ) ) otherlv_5= 'port' ( (lv_port_6_0= RULE_STRING ) ) otherlv_7= 'username' ( (lv_username_8_0= RULE_STRING ) ) otherlv_9= 'password' ( (lv_password_10_0= RULE_STRING ) ) otherlv_11= 'action' ( (lv_action_12_0= RULE_STRING ) ) otherlv_13= 'local-dir' ( (lv_localDir_14_0= RULE_STRING ) ) otherlv_15= 'local-file' ( (lv_localFile_16_0= RULE_STRING ) ) otherlv_17= 'remote-dir' ( (lv_remoteDir_18_0= RULE_STRING ) ) otherlv_19= 'remote-file' ( (lv_remoteFile_20_0= RULE_STRING ) ) otherlv_21= 'from' ( (lv_source_22_0= RULE_STRING ) ) otherlv_23= 'target-table' ( (lv_targetTable_24_0= RULE_STRING ) ) otherlv_25= 'using' otherlv_26= '{' ( (lv_value_27_0= RULE_STRING ) ) otherlv_28= '}' otherlv_29= 'on-condition' ( (lv_condition_30_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,78,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getFTPAccess().getFtpKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getFTPAccess().getAsKeyword_1());
            		
            // InternalDsl.g:3983:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:3984:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:3984:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:3985:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_51); 

            					newLeafNode(lv_name_2_0, grammarAccess.getFTPAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getFTPRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,79,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getFTPAccess().getHostKeyword_3());
            		
            // InternalDsl.g:4005:3: ( (lv_host_4_0= RULE_STRING ) )
            // InternalDsl.g:4006:4: (lv_host_4_0= RULE_STRING )
            {
            // InternalDsl.g:4006:4: (lv_host_4_0= RULE_STRING )
            // InternalDsl.g:4007:5: lv_host_4_0= RULE_STRING
            {
            lv_host_4_0=(Token)match(input,RULE_STRING,FOLLOW_52); 

            					newLeafNode(lv_host_4_0, grammarAccess.getFTPAccess().getHostSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getFTPRule());
            					}
            					setWithLastConsumed(
            						current,
            						"host",
            						lv_host_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,80,FOLLOW_3); 

            			newLeafNode(otherlv_5, grammarAccess.getFTPAccess().getPortKeyword_5());
            		
            // InternalDsl.g:4027:3: ( (lv_port_6_0= RULE_STRING ) )
            // InternalDsl.g:4028:4: (lv_port_6_0= RULE_STRING )
            {
            // InternalDsl.g:4028:4: (lv_port_6_0= RULE_STRING )
            // InternalDsl.g:4029:5: lv_port_6_0= RULE_STRING
            {
            lv_port_6_0=(Token)match(input,RULE_STRING,FOLLOW_53); 

            					newLeafNode(lv_port_6_0, grammarAccess.getFTPAccess().getPortSTRINGTerminalRuleCall_6_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getFTPRule());
            					}
            					setWithLastConsumed(
            						current,
            						"port",
            						lv_port_6_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_7=(Token)match(input,81,FOLLOW_3); 

            			newLeafNode(otherlv_7, grammarAccess.getFTPAccess().getUsernameKeyword_7());
            		
            // InternalDsl.g:4049:3: ( (lv_username_8_0= RULE_STRING ) )
            // InternalDsl.g:4050:4: (lv_username_8_0= RULE_STRING )
            {
            // InternalDsl.g:4050:4: (lv_username_8_0= RULE_STRING )
            // InternalDsl.g:4051:5: lv_username_8_0= RULE_STRING
            {
            lv_username_8_0=(Token)match(input,RULE_STRING,FOLLOW_54); 

            					newLeafNode(lv_username_8_0, grammarAccess.getFTPAccess().getUsernameSTRINGTerminalRuleCall_8_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getFTPRule());
            					}
            					setWithLastConsumed(
            						current,
            						"username",
            						lv_username_8_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_9=(Token)match(input,82,FOLLOW_3); 

            			newLeafNode(otherlv_9, grammarAccess.getFTPAccess().getPasswordKeyword_9());
            		
            // InternalDsl.g:4071:3: ( (lv_password_10_0= RULE_STRING ) )
            // InternalDsl.g:4072:4: (lv_password_10_0= RULE_STRING )
            {
            // InternalDsl.g:4072:4: (lv_password_10_0= RULE_STRING )
            // InternalDsl.g:4073:5: lv_password_10_0= RULE_STRING
            {
            lv_password_10_0=(Token)match(input,RULE_STRING,FOLLOW_55); 

            					newLeafNode(lv_password_10_0, grammarAccess.getFTPAccess().getPasswordSTRINGTerminalRuleCall_10_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getFTPRule());
            					}
            					setWithLastConsumed(
            						current,
            						"password",
            						lv_password_10_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_11=(Token)match(input,83,FOLLOW_3); 

            			newLeafNode(otherlv_11, grammarAccess.getFTPAccess().getActionKeyword_11());
            		
            // InternalDsl.g:4093:3: ( (lv_action_12_0= RULE_STRING ) )
            // InternalDsl.g:4094:4: (lv_action_12_0= RULE_STRING )
            {
            // InternalDsl.g:4094:4: (lv_action_12_0= RULE_STRING )
            // InternalDsl.g:4095:5: lv_action_12_0= RULE_STRING
            {
            lv_action_12_0=(Token)match(input,RULE_STRING,FOLLOW_56); 

            					newLeafNode(lv_action_12_0, grammarAccess.getFTPAccess().getActionSTRINGTerminalRuleCall_12_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getFTPRule());
            					}
            					setWithLastConsumed(
            						current,
            						"action",
            						lv_action_12_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_13=(Token)match(input,84,FOLLOW_3); 

            			newLeafNode(otherlv_13, grammarAccess.getFTPAccess().getLocalDirKeyword_13());
            		
            // InternalDsl.g:4115:3: ( (lv_localDir_14_0= RULE_STRING ) )
            // InternalDsl.g:4116:4: (lv_localDir_14_0= RULE_STRING )
            {
            // InternalDsl.g:4116:4: (lv_localDir_14_0= RULE_STRING )
            // InternalDsl.g:4117:5: lv_localDir_14_0= RULE_STRING
            {
            lv_localDir_14_0=(Token)match(input,RULE_STRING,FOLLOW_57); 

            					newLeafNode(lv_localDir_14_0, grammarAccess.getFTPAccess().getLocalDirSTRINGTerminalRuleCall_14_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getFTPRule());
            					}
            					setWithLastConsumed(
            						current,
            						"localDir",
            						lv_localDir_14_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_15=(Token)match(input,85,FOLLOW_3); 

            			newLeafNode(otherlv_15, grammarAccess.getFTPAccess().getLocalFileKeyword_15());
            		
            // InternalDsl.g:4137:3: ( (lv_localFile_16_0= RULE_STRING ) )
            // InternalDsl.g:4138:4: (lv_localFile_16_0= RULE_STRING )
            {
            // InternalDsl.g:4138:4: (lv_localFile_16_0= RULE_STRING )
            // InternalDsl.g:4139:5: lv_localFile_16_0= RULE_STRING
            {
            lv_localFile_16_0=(Token)match(input,RULE_STRING,FOLLOW_58); 

            					newLeafNode(lv_localFile_16_0, grammarAccess.getFTPAccess().getLocalFileSTRINGTerminalRuleCall_16_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getFTPRule());
            					}
            					setWithLastConsumed(
            						current,
            						"localFile",
            						lv_localFile_16_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_17=(Token)match(input,86,FOLLOW_3); 

            			newLeafNode(otherlv_17, grammarAccess.getFTPAccess().getRemoteDirKeyword_17());
            		
            // InternalDsl.g:4159:3: ( (lv_remoteDir_18_0= RULE_STRING ) )
            // InternalDsl.g:4160:4: (lv_remoteDir_18_0= RULE_STRING )
            {
            // InternalDsl.g:4160:4: (lv_remoteDir_18_0= RULE_STRING )
            // InternalDsl.g:4161:5: lv_remoteDir_18_0= RULE_STRING
            {
            lv_remoteDir_18_0=(Token)match(input,RULE_STRING,FOLLOW_59); 

            					newLeafNode(lv_remoteDir_18_0, grammarAccess.getFTPAccess().getRemoteDirSTRINGTerminalRuleCall_18_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getFTPRule());
            					}
            					setWithLastConsumed(
            						current,
            						"remoteDir",
            						lv_remoteDir_18_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_19=(Token)match(input,87,FOLLOW_3); 

            			newLeafNode(otherlv_19, grammarAccess.getFTPAccess().getRemoteFileKeyword_19());
            		
            // InternalDsl.g:4181:3: ( (lv_remoteFile_20_0= RULE_STRING ) )
            // InternalDsl.g:4182:4: (lv_remoteFile_20_0= RULE_STRING )
            {
            // InternalDsl.g:4182:4: (lv_remoteFile_20_0= RULE_STRING )
            // InternalDsl.g:4183:5: lv_remoteFile_20_0= RULE_STRING
            {
            lv_remoteFile_20_0=(Token)match(input,RULE_STRING,FOLLOW_14); 

            					newLeafNode(lv_remoteFile_20_0, grammarAccess.getFTPAccess().getRemoteFileSTRINGTerminalRuleCall_20_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getFTPRule());
            					}
            					setWithLastConsumed(
            						current,
            						"remoteFile",
            						lv_remoteFile_20_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_21=(Token)match(input,22,FOLLOW_3); 

            			newLeafNode(otherlv_21, grammarAccess.getFTPAccess().getFromKeyword_21());
            		
            // InternalDsl.g:4203:3: ( (lv_source_22_0= RULE_STRING ) )
            // InternalDsl.g:4204:4: (lv_source_22_0= RULE_STRING )
            {
            // InternalDsl.g:4204:4: (lv_source_22_0= RULE_STRING )
            // InternalDsl.g:4205:5: lv_source_22_0= RULE_STRING
            {
            lv_source_22_0=(Token)match(input,RULE_STRING,FOLLOW_41); 

            					newLeafNode(lv_source_22_0, grammarAccess.getFTPAccess().getSourceSTRINGTerminalRuleCall_22_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getFTPRule());
            					}
            					setWithLastConsumed(
            						current,
            						"source",
            						lv_source_22_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_23=(Token)match(input,67,FOLLOW_3); 

            			newLeafNode(otherlv_23, grammarAccess.getFTPAccess().getTargetTableKeyword_23());
            		
            // InternalDsl.g:4225:3: ( (lv_targetTable_24_0= RULE_STRING ) )
            // InternalDsl.g:4226:4: (lv_targetTable_24_0= RULE_STRING )
            {
            // InternalDsl.g:4226:4: (lv_targetTable_24_0= RULE_STRING )
            // InternalDsl.g:4227:5: lv_targetTable_24_0= RULE_STRING
            {
            lv_targetTable_24_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_targetTable_24_0, grammarAccess.getFTPAccess().getTargetTableSTRINGTerminalRuleCall_24_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getFTPRule());
            					}
            					setWithLastConsumed(
            						current,
            						"targetTable",
            						lv_targetTable_24_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_25=(Token)match(input,30,FOLLOW_4); 

            			newLeafNode(otherlv_25, grammarAccess.getFTPAccess().getUsingKeyword_25());
            		
            otherlv_26=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_26, grammarAccess.getFTPAccess().getLeftCurlyBracketKeyword_26());
            		
            // InternalDsl.g:4251:3: ( (lv_value_27_0= RULE_STRING ) )
            // InternalDsl.g:4252:4: (lv_value_27_0= RULE_STRING )
            {
            // InternalDsl.g:4252:4: (lv_value_27_0= RULE_STRING )
            // InternalDsl.g:4253:5: lv_value_27_0= RULE_STRING
            {
            lv_value_27_0=(Token)match(input,RULE_STRING,FOLLOW_8); 

            					newLeafNode(lv_value_27_0, grammarAccess.getFTPAccess().getValueSTRINGTerminalRuleCall_27_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getFTPRule());
            					}
            					setWithLastConsumed(
            						current,
            						"value",
            						lv_value_27_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_28=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_28, grammarAccess.getFTPAccess().getRightCurlyBracketKeyword_28());
            		
            otherlv_29=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_29, grammarAccess.getFTPAccess().getOnConditionKeyword_29());
            		
            // InternalDsl.g:4277:3: ( (lv_condition_30_0= ruleExpression ) )
            // InternalDsl.g:4278:4: (lv_condition_30_0= ruleExpression )
            {
            // InternalDsl.g:4278:4: (lv_condition_30_0= ruleExpression )
            // InternalDsl.g:4279:5: lv_condition_30_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getFTPAccess().getConditionExpressionParserRuleCall_30_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_30_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getFTPRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_30_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleFTP"


    // $ANTLR start "entryRuleZip"
    // InternalDsl.g:4300:1: entryRuleZip returns [EObject current=null] : iv_ruleZip= ruleZip EOF ;
    public final EObject entryRuleZip() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleZip = null;


        try {
            // InternalDsl.g:4300:44: (iv_ruleZip= ruleZip EOF )
            // InternalDsl.g:4301:2: iv_ruleZip= ruleZip EOF
            {
             newCompositeNode(grammarAccess.getZipRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleZip=ruleZip();

            state._fsp--;

             current =iv_ruleZip; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleZip"


    // $ANTLR start "ruleZip"
    // InternalDsl.g:4307:1: ruleZip returns [EObject current=null] : (otherlv_0= 'zip' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'zip-file-path' ( (lv_zipFilePath_4_0= RULE_STRING ) ) otherlv_5= 'zip-file-name' ( (lv_zipFileName_6_0= RULE_STRING ) ) otherlv_7= 'source' ( (lv_source_8_0= RULE_STRING ) ) otherlv_9= 'buffer-size' ( (lv_bufferSize_10_0= RULE_STRING ) ) otherlv_11= 'using' otherlv_12= '{' ( (lv_value_13_0= ruleSelectStatement ) ) otherlv_14= '}' otherlv_15= 'on-condition' ( (lv_condition_16_0= ruleExpression ) ) ) ;
    public final EObject ruleZip() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_zipFilePath_4_0=null;
        Token otherlv_5=null;
        Token lv_zipFileName_6_0=null;
        Token otherlv_7=null;
        Token lv_source_8_0=null;
        Token otherlv_9=null;
        Token lv_bufferSize_10_0=null;
        Token otherlv_11=null;
        Token otherlv_12=null;
        Token otherlv_14=null;
        Token otherlv_15=null;
        AntlrDatatypeRuleToken lv_value_13_0 = null;

        EObject lv_condition_16_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:4313:2: ( (otherlv_0= 'zip' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'zip-file-path' ( (lv_zipFilePath_4_0= RULE_STRING ) ) otherlv_5= 'zip-file-name' ( (lv_zipFileName_6_0= RULE_STRING ) ) otherlv_7= 'source' ( (lv_source_8_0= RULE_STRING ) ) otherlv_9= 'buffer-size' ( (lv_bufferSize_10_0= RULE_STRING ) ) otherlv_11= 'using' otherlv_12= '{' ( (lv_value_13_0= ruleSelectStatement ) ) otherlv_14= '}' otherlv_15= 'on-condition' ( (lv_condition_16_0= ruleExpression ) ) ) )
            // InternalDsl.g:4314:2: (otherlv_0= 'zip' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'zip-file-path' ( (lv_zipFilePath_4_0= RULE_STRING ) ) otherlv_5= 'zip-file-name' ( (lv_zipFileName_6_0= RULE_STRING ) ) otherlv_7= 'source' ( (lv_source_8_0= RULE_STRING ) ) otherlv_9= 'buffer-size' ( (lv_bufferSize_10_0= RULE_STRING ) ) otherlv_11= 'using' otherlv_12= '{' ( (lv_value_13_0= ruleSelectStatement ) ) otherlv_14= '}' otherlv_15= 'on-condition' ( (lv_condition_16_0= ruleExpression ) ) )
            {
            // InternalDsl.g:4314:2: (otherlv_0= 'zip' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'zip-file-path' ( (lv_zipFilePath_4_0= RULE_STRING ) ) otherlv_5= 'zip-file-name' ( (lv_zipFileName_6_0= RULE_STRING ) ) otherlv_7= 'source' ( (lv_source_8_0= RULE_STRING ) ) otherlv_9= 'buffer-size' ( (lv_bufferSize_10_0= RULE_STRING ) ) otherlv_11= 'using' otherlv_12= '{' ( (lv_value_13_0= ruleSelectStatement ) ) otherlv_14= '}' otherlv_15= 'on-condition' ( (lv_condition_16_0= ruleExpression ) ) )
            // InternalDsl.g:4315:3: otherlv_0= 'zip' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'zip-file-path' ( (lv_zipFilePath_4_0= RULE_STRING ) ) otherlv_5= 'zip-file-name' ( (lv_zipFileName_6_0= RULE_STRING ) ) otherlv_7= 'source' ( (lv_source_8_0= RULE_STRING ) ) otherlv_9= 'buffer-size' ( (lv_bufferSize_10_0= RULE_STRING ) ) otherlv_11= 'using' otherlv_12= '{' ( (lv_value_13_0= ruleSelectStatement ) ) otherlv_14= '}' otherlv_15= 'on-condition' ( (lv_condition_16_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,88,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getZipAccess().getZipKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getZipAccess().getAsKeyword_1());
            		
            // InternalDsl.g:4323:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:4324:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:4324:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:4325:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_60); 

            					newLeafNode(lv_name_2_0, grammarAccess.getZipAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getZipRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,89,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getZipAccess().getZipFilePathKeyword_3());
            		
            // InternalDsl.g:4345:3: ( (lv_zipFilePath_4_0= RULE_STRING ) )
            // InternalDsl.g:4346:4: (lv_zipFilePath_4_0= RULE_STRING )
            {
            // InternalDsl.g:4346:4: (lv_zipFilePath_4_0= RULE_STRING )
            // InternalDsl.g:4347:5: lv_zipFilePath_4_0= RULE_STRING
            {
            lv_zipFilePath_4_0=(Token)match(input,RULE_STRING,FOLLOW_61); 

            					newLeafNode(lv_zipFilePath_4_0, grammarAccess.getZipAccess().getZipFilePathSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getZipRule());
            					}
            					setWithLastConsumed(
            						current,
            						"zipFilePath",
            						lv_zipFilePath_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,90,FOLLOW_3); 

            			newLeafNode(otherlv_5, grammarAccess.getZipAccess().getZipFileNameKeyword_5());
            		
            // InternalDsl.g:4367:3: ( (lv_zipFileName_6_0= RULE_STRING ) )
            // InternalDsl.g:4368:4: (lv_zipFileName_6_0= RULE_STRING )
            {
            // InternalDsl.g:4368:4: (lv_zipFileName_6_0= RULE_STRING )
            // InternalDsl.g:4369:5: lv_zipFileName_6_0= RULE_STRING
            {
            lv_zipFileName_6_0=(Token)match(input,RULE_STRING,FOLLOW_32); 

            					newLeafNode(lv_zipFileName_6_0, grammarAccess.getZipAccess().getZipFileNameSTRINGTerminalRuleCall_6_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getZipRule());
            					}
            					setWithLastConsumed(
            						current,
            						"zipFileName",
            						lv_zipFileName_6_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_7=(Token)match(input,46,FOLLOW_3); 

            			newLeafNode(otherlv_7, grammarAccess.getZipAccess().getSourceKeyword_7());
            		
            // InternalDsl.g:4389:3: ( (lv_source_8_0= RULE_STRING ) )
            // InternalDsl.g:4390:4: (lv_source_8_0= RULE_STRING )
            {
            // InternalDsl.g:4390:4: (lv_source_8_0= RULE_STRING )
            // InternalDsl.g:4391:5: lv_source_8_0= RULE_STRING
            {
            lv_source_8_0=(Token)match(input,RULE_STRING,FOLLOW_62); 

            					newLeafNode(lv_source_8_0, grammarAccess.getZipAccess().getSourceSTRINGTerminalRuleCall_8_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getZipRule());
            					}
            					setWithLastConsumed(
            						current,
            						"source",
            						lv_source_8_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_9=(Token)match(input,91,FOLLOW_3); 

            			newLeafNode(otherlv_9, grammarAccess.getZipAccess().getBufferSizeKeyword_9());
            		
            // InternalDsl.g:4411:3: ( (lv_bufferSize_10_0= RULE_STRING ) )
            // InternalDsl.g:4412:4: (lv_bufferSize_10_0= RULE_STRING )
            {
            // InternalDsl.g:4412:4: (lv_bufferSize_10_0= RULE_STRING )
            // InternalDsl.g:4413:5: lv_bufferSize_10_0= RULE_STRING
            {
            lv_bufferSize_10_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_bufferSize_10_0, grammarAccess.getZipAccess().getBufferSizeSTRINGTerminalRuleCall_10_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getZipRule());
            					}
            					setWithLastConsumed(
            						current,
            						"bufferSize",
            						lv_bufferSize_10_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_11=(Token)match(input,30,FOLLOW_4); 

            			newLeafNode(otherlv_11, grammarAccess.getZipAccess().getUsingKeyword_11());
            		
            otherlv_12=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_12, grammarAccess.getZipAccess().getLeftCurlyBracketKeyword_12());
            		
            // InternalDsl.g:4437:3: ( (lv_value_13_0= ruleSelectStatement ) )
            // InternalDsl.g:4438:4: (lv_value_13_0= ruleSelectStatement )
            {
            // InternalDsl.g:4438:4: (lv_value_13_0= ruleSelectStatement )
            // InternalDsl.g:4439:5: lv_value_13_0= ruleSelectStatement
            {

            					newCompositeNode(grammarAccess.getZipAccess().getValueSelectStatementParserRuleCall_13_0());
            				
            pushFollow(FOLLOW_8);
            lv_value_13_0=ruleSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getZipRule());
            					}
            					set(
            						current,
            						"value",
            						lv_value_13_0,
            						"in.handyman.Dsl.SelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_14=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_14, grammarAccess.getZipAccess().getRightCurlyBracketKeyword_14());
            		
            otherlv_15=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_15, grammarAccess.getZipAccess().getOnConditionKeyword_15());
            		
            // InternalDsl.g:4464:3: ( (lv_condition_16_0= ruleExpression ) )
            // InternalDsl.g:4465:4: (lv_condition_16_0= ruleExpression )
            {
            // InternalDsl.g:4465:4: (lv_condition_16_0= ruleExpression )
            // InternalDsl.g:4466:5: lv_condition_16_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getZipAccess().getConditionExpressionParserRuleCall_16_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_16_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getZipRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_16_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleZip"


    // $ANTLR start "entryRuleUnzip"
    // InternalDsl.g:4487:1: entryRuleUnzip returns [EObject current=null] : iv_ruleUnzip= ruleUnzip EOF ;
    public final EObject entryRuleUnzip() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleUnzip = null;


        try {
            // InternalDsl.g:4487:46: (iv_ruleUnzip= ruleUnzip EOF )
            // InternalDsl.g:4488:2: iv_ruleUnzip= ruleUnzip EOF
            {
             newCompositeNode(grammarAccess.getUnzipRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleUnzip=ruleUnzip();

            state._fsp--;

             current =iv_ruleUnzip; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleUnzip"


    // $ANTLR start "ruleUnzip"
    // InternalDsl.g:4494:1: ruleUnzip returns [EObject current=null] : (otherlv_0= 'unzip' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'source' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'dest-dir' ( (lv_destDir_6_0= RULE_STRING ) ) otherlv_7= 'buffer-size' ( (lv_bufferSize_8_0= RULE_STRING ) ) otherlv_9= 'using' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) ) ) ;
    public final EObject ruleUnzip() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_source_4_0=null;
        Token otherlv_5=null;
        Token lv_destDir_6_0=null;
        Token otherlv_7=null;
        Token lv_bufferSize_8_0=null;
        Token otherlv_9=null;
        Token otherlv_10=null;
        Token otherlv_12=null;
        Token otherlv_13=null;
        AntlrDatatypeRuleToken lv_value_11_0 = null;

        EObject lv_condition_14_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:4500:2: ( (otherlv_0= 'unzip' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'source' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'dest-dir' ( (lv_destDir_6_0= RULE_STRING ) ) otherlv_7= 'buffer-size' ( (lv_bufferSize_8_0= RULE_STRING ) ) otherlv_9= 'using' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) ) ) )
            // InternalDsl.g:4501:2: (otherlv_0= 'unzip' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'source' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'dest-dir' ( (lv_destDir_6_0= RULE_STRING ) ) otherlv_7= 'buffer-size' ( (lv_bufferSize_8_0= RULE_STRING ) ) otherlv_9= 'using' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) ) )
            {
            // InternalDsl.g:4501:2: (otherlv_0= 'unzip' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'source' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'dest-dir' ( (lv_destDir_6_0= RULE_STRING ) ) otherlv_7= 'buffer-size' ( (lv_bufferSize_8_0= RULE_STRING ) ) otherlv_9= 'using' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) ) )
            // InternalDsl.g:4502:3: otherlv_0= 'unzip' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'source' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'dest-dir' ( (lv_destDir_6_0= RULE_STRING ) ) otherlv_7= 'buffer-size' ( (lv_bufferSize_8_0= RULE_STRING ) ) otherlv_9= 'using' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,92,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getUnzipAccess().getUnzipKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getUnzipAccess().getAsKeyword_1());
            		
            // InternalDsl.g:4510:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:4511:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:4511:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:4512:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_32); 

            					newLeafNode(lv_name_2_0, grammarAccess.getUnzipAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getUnzipRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,46,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getUnzipAccess().getSourceKeyword_3());
            		
            // InternalDsl.g:4532:3: ( (lv_source_4_0= RULE_STRING ) )
            // InternalDsl.g:4533:4: (lv_source_4_0= RULE_STRING )
            {
            // InternalDsl.g:4533:4: (lv_source_4_0= RULE_STRING )
            // InternalDsl.g:4534:5: lv_source_4_0= RULE_STRING
            {
            lv_source_4_0=(Token)match(input,RULE_STRING,FOLLOW_63); 

            					newLeafNode(lv_source_4_0, grammarAccess.getUnzipAccess().getSourceSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getUnzipRule());
            					}
            					setWithLastConsumed(
            						current,
            						"source",
            						lv_source_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,93,FOLLOW_3); 

            			newLeafNode(otherlv_5, grammarAccess.getUnzipAccess().getDestDirKeyword_5());
            		
            // InternalDsl.g:4554:3: ( (lv_destDir_6_0= RULE_STRING ) )
            // InternalDsl.g:4555:4: (lv_destDir_6_0= RULE_STRING )
            {
            // InternalDsl.g:4555:4: (lv_destDir_6_0= RULE_STRING )
            // InternalDsl.g:4556:5: lv_destDir_6_0= RULE_STRING
            {
            lv_destDir_6_0=(Token)match(input,RULE_STRING,FOLLOW_62); 

            					newLeafNode(lv_destDir_6_0, grammarAccess.getUnzipAccess().getDestDirSTRINGTerminalRuleCall_6_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getUnzipRule());
            					}
            					setWithLastConsumed(
            						current,
            						"destDir",
            						lv_destDir_6_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_7=(Token)match(input,91,FOLLOW_3); 

            			newLeafNode(otherlv_7, grammarAccess.getUnzipAccess().getBufferSizeKeyword_7());
            		
            // InternalDsl.g:4576:3: ( (lv_bufferSize_8_0= RULE_STRING ) )
            // InternalDsl.g:4577:4: (lv_bufferSize_8_0= RULE_STRING )
            {
            // InternalDsl.g:4577:4: (lv_bufferSize_8_0= RULE_STRING )
            // InternalDsl.g:4578:5: lv_bufferSize_8_0= RULE_STRING
            {
            lv_bufferSize_8_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_bufferSize_8_0, grammarAccess.getUnzipAccess().getBufferSizeSTRINGTerminalRuleCall_8_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getUnzipRule());
            					}
            					setWithLastConsumed(
            						current,
            						"bufferSize",
            						lv_bufferSize_8_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_9=(Token)match(input,30,FOLLOW_4); 

            			newLeafNode(otherlv_9, grammarAccess.getUnzipAccess().getUsingKeyword_9());
            		
            otherlv_10=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_10, grammarAccess.getUnzipAccess().getLeftCurlyBracketKeyword_10());
            		
            // InternalDsl.g:4602:3: ( (lv_value_11_0= ruleSelectStatement ) )
            // InternalDsl.g:4603:4: (lv_value_11_0= ruleSelectStatement )
            {
            // InternalDsl.g:4603:4: (lv_value_11_0= ruleSelectStatement )
            // InternalDsl.g:4604:5: lv_value_11_0= ruleSelectStatement
            {

            					newCompositeNode(grammarAccess.getUnzipAccess().getValueSelectStatementParserRuleCall_11_0());
            				
            pushFollow(FOLLOW_8);
            lv_value_11_0=ruleSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getUnzipRule());
            					}
            					set(
            						current,
            						"value",
            						lv_value_11_0,
            						"in.handyman.Dsl.SelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_12=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_12, grammarAccess.getUnzipAccess().getRightCurlyBracketKeyword_12());
            		
            otherlv_13=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_13, grammarAccess.getUnzipAccess().getOnConditionKeyword_13());
            		
            // InternalDsl.g:4629:3: ( (lv_condition_14_0= ruleExpression ) )
            // InternalDsl.g:4630:4: (lv_condition_14_0= ruleExpression )
            {
            // InternalDsl.g:4630:4: (lv_condition_14_0= ruleExpression )
            // InternalDsl.g:4631:5: lv_condition_14_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getUnzipAccess().getConditionExpressionParserRuleCall_14_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_14_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getUnzipRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_14_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleUnzip"


    // $ANTLR start "entryRuleChecksum"
    // InternalDsl.g:4652:1: entryRuleChecksum returns [EObject current=null] : iv_ruleChecksum= ruleChecksum EOF ;
    public final EObject entryRuleChecksum() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleChecksum = null;


        try {
            // InternalDsl.g:4652:49: (iv_ruleChecksum= ruleChecksum EOF )
            // InternalDsl.g:4653:2: iv_ruleChecksum= ruleChecksum EOF
            {
             newCompositeNode(grammarAccess.getChecksumRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleChecksum=ruleChecksum();

            state._fsp--;

             current =iv_ruleChecksum; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleChecksum"


    // $ANTLR start "ruleChecksum"
    // InternalDsl.g:4659:1: ruleChecksum returns [EObject current=null] : (otherlv_0= 'checksum' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'host' ( (lv_host_4_0= RULE_STRING ) ) otherlv_5= 'port' ( (lv_port_6_0= RULE_STRING ) ) otherlv_7= 'username' ( (lv_username_8_0= RULE_STRING ) ) otherlv_9= 'password' ( (lv_password_10_0= RULE_STRING ) ) otherlv_11= 'local-dir' ( (lv_localDir_12_0= RULE_STRING ) ) otherlv_13= 'local-file' ( (lv_localFile_14_0= RULE_STRING ) ) otherlv_15= 'remote-dir' ( (lv_remoteDir_16_0= RULE_STRING ) ) otherlv_17= 'remote-file' ( (lv_remoteFile_18_0= RULE_STRING ) ) otherlv_19= 'using' otherlv_20= '{' ( (lv_value_21_0= ruleSelectStatement ) ) otherlv_22= '}' otherlv_23= 'on-condition' ( (lv_condition_24_0= ruleExpression ) ) ) ;
    public final EObject ruleChecksum() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_host_4_0=null;
        Token otherlv_5=null;
        Token lv_port_6_0=null;
        Token otherlv_7=null;
        Token lv_username_8_0=null;
        Token otherlv_9=null;
        Token lv_password_10_0=null;
        Token otherlv_11=null;
        Token lv_localDir_12_0=null;
        Token otherlv_13=null;
        Token lv_localFile_14_0=null;
        Token otherlv_15=null;
        Token lv_remoteDir_16_0=null;
        Token otherlv_17=null;
        Token lv_remoteFile_18_0=null;
        Token otherlv_19=null;
        Token otherlv_20=null;
        Token otherlv_22=null;
        Token otherlv_23=null;
        AntlrDatatypeRuleToken lv_value_21_0 = null;

        EObject lv_condition_24_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:4665:2: ( (otherlv_0= 'checksum' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'host' ( (lv_host_4_0= RULE_STRING ) ) otherlv_5= 'port' ( (lv_port_6_0= RULE_STRING ) ) otherlv_7= 'username' ( (lv_username_8_0= RULE_STRING ) ) otherlv_9= 'password' ( (lv_password_10_0= RULE_STRING ) ) otherlv_11= 'local-dir' ( (lv_localDir_12_0= RULE_STRING ) ) otherlv_13= 'local-file' ( (lv_localFile_14_0= RULE_STRING ) ) otherlv_15= 'remote-dir' ( (lv_remoteDir_16_0= RULE_STRING ) ) otherlv_17= 'remote-file' ( (lv_remoteFile_18_0= RULE_STRING ) ) otherlv_19= 'using' otherlv_20= '{' ( (lv_value_21_0= ruleSelectStatement ) ) otherlv_22= '}' otherlv_23= 'on-condition' ( (lv_condition_24_0= ruleExpression ) ) ) )
            // InternalDsl.g:4666:2: (otherlv_0= 'checksum' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'host' ( (lv_host_4_0= RULE_STRING ) ) otherlv_5= 'port' ( (lv_port_6_0= RULE_STRING ) ) otherlv_7= 'username' ( (lv_username_8_0= RULE_STRING ) ) otherlv_9= 'password' ( (lv_password_10_0= RULE_STRING ) ) otherlv_11= 'local-dir' ( (lv_localDir_12_0= RULE_STRING ) ) otherlv_13= 'local-file' ( (lv_localFile_14_0= RULE_STRING ) ) otherlv_15= 'remote-dir' ( (lv_remoteDir_16_0= RULE_STRING ) ) otherlv_17= 'remote-file' ( (lv_remoteFile_18_0= RULE_STRING ) ) otherlv_19= 'using' otherlv_20= '{' ( (lv_value_21_0= ruleSelectStatement ) ) otherlv_22= '}' otherlv_23= 'on-condition' ( (lv_condition_24_0= ruleExpression ) ) )
            {
            // InternalDsl.g:4666:2: (otherlv_0= 'checksum' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'host' ( (lv_host_4_0= RULE_STRING ) ) otherlv_5= 'port' ( (lv_port_6_0= RULE_STRING ) ) otherlv_7= 'username' ( (lv_username_8_0= RULE_STRING ) ) otherlv_9= 'password' ( (lv_password_10_0= RULE_STRING ) ) otherlv_11= 'local-dir' ( (lv_localDir_12_0= RULE_STRING ) ) otherlv_13= 'local-file' ( (lv_localFile_14_0= RULE_STRING ) ) otherlv_15= 'remote-dir' ( (lv_remoteDir_16_0= RULE_STRING ) ) otherlv_17= 'remote-file' ( (lv_remoteFile_18_0= RULE_STRING ) ) otherlv_19= 'using' otherlv_20= '{' ( (lv_value_21_0= ruleSelectStatement ) ) otherlv_22= '}' otherlv_23= 'on-condition' ( (lv_condition_24_0= ruleExpression ) ) )
            // InternalDsl.g:4667:3: otherlv_0= 'checksum' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'host' ( (lv_host_4_0= RULE_STRING ) ) otherlv_5= 'port' ( (lv_port_6_0= RULE_STRING ) ) otherlv_7= 'username' ( (lv_username_8_0= RULE_STRING ) ) otherlv_9= 'password' ( (lv_password_10_0= RULE_STRING ) ) otherlv_11= 'local-dir' ( (lv_localDir_12_0= RULE_STRING ) ) otherlv_13= 'local-file' ( (lv_localFile_14_0= RULE_STRING ) ) otherlv_15= 'remote-dir' ( (lv_remoteDir_16_0= RULE_STRING ) ) otherlv_17= 'remote-file' ( (lv_remoteFile_18_0= RULE_STRING ) ) otherlv_19= 'using' otherlv_20= '{' ( (lv_value_21_0= ruleSelectStatement ) ) otherlv_22= '}' otherlv_23= 'on-condition' ( (lv_condition_24_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,94,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getChecksumAccess().getChecksumKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getChecksumAccess().getAsKeyword_1());
            		
            // InternalDsl.g:4675:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:4676:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:4676:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:4677:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_51); 

            					newLeafNode(lv_name_2_0, grammarAccess.getChecksumAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getChecksumRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,79,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getChecksumAccess().getHostKeyword_3());
            		
            // InternalDsl.g:4697:3: ( (lv_host_4_0= RULE_STRING ) )
            // InternalDsl.g:4698:4: (lv_host_4_0= RULE_STRING )
            {
            // InternalDsl.g:4698:4: (lv_host_4_0= RULE_STRING )
            // InternalDsl.g:4699:5: lv_host_4_0= RULE_STRING
            {
            lv_host_4_0=(Token)match(input,RULE_STRING,FOLLOW_52); 

            					newLeafNode(lv_host_4_0, grammarAccess.getChecksumAccess().getHostSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getChecksumRule());
            					}
            					setWithLastConsumed(
            						current,
            						"host",
            						lv_host_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,80,FOLLOW_3); 

            			newLeafNode(otherlv_5, grammarAccess.getChecksumAccess().getPortKeyword_5());
            		
            // InternalDsl.g:4719:3: ( (lv_port_6_0= RULE_STRING ) )
            // InternalDsl.g:4720:4: (lv_port_6_0= RULE_STRING )
            {
            // InternalDsl.g:4720:4: (lv_port_6_0= RULE_STRING )
            // InternalDsl.g:4721:5: lv_port_6_0= RULE_STRING
            {
            lv_port_6_0=(Token)match(input,RULE_STRING,FOLLOW_53); 

            					newLeafNode(lv_port_6_0, grammarAccess.getChecksumAccess().getPortSTRINGTerminalRuleCall_6_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getChecksumRule());
            					}
            					setWithLastConsumed(
            						current,
            						"port",
            						lv_port_6_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_7=(Token)match(input,81,FOLLOW_3); 

            			newLeafNode(otherlv_7, grammarAccess.getChecksumAccess().getUsernameKeyword_7());
            		
            // InternalDsl.g:4741:3: ( (lv_username_8_0= RULE_STRING ) )
            // InternalDsl.g:4742:4: (lv_username_8_0= RULE_STRING )
            {
            // InternalDsl.g:4742:4: (lv_username_8_0= RULE_STRING )
            // InternalDsl.g:4743:5: lv_username_8_0= RULE_STRING
            {
            lv_username_8_0=(Token)match(input,RULE_STRING,FOLLOW_54); 

            					newLeafNode(lv_username_8_0, grammarAccess.getChecksumAccess().getUsernameSTRINGTerminalRuleCall_8_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getChecksumRule());
            					}
            					setWithLastConsumed(
            						current,
            						"username",
            						lv_username_8_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_9=(Token)match(input,82,FOLLOW_3); 

            			newLeafNode(otherlv_9, grammarAccess.getChecksumAccess().getPasswordKeyword_9());
            		
            // InternalDsl.g:4763:3: ( (lv_password_10_0= RULE_STRING ) )
            // InternalDsl.g:4764:4: (lv_password_10_0= RULE_STRING )
            {
            // InternalDsl.g:4764:4: (lv_password_10_0= RULE_STRING )
            // InternalDsl.g:4765:5: lv_password_10_0= RULE_STRING
            {
            lv_password_10_0=(Token)match(input,RULE_STRING,FOLLOW_56); 

            					newLeafNode(lv_password_10_0, grammarAccess.getChecksumAccess().getPasswordSTRINGTerminalRuleCall_10_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getChecksumRule());
            					}
            					setWithLastConsumed(
            						current,
            						"password",
            						lv_password_10_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_11=(Token)match(input,84,FOLLOW_3); 

            			newLeafNode(otherlv_11, grammarAccess.getChecksumAccess().getLocalDirKeyword_11());
            		
            // InternalDsl.g:4785:3: ( (lv_localDir_12_0= RULE_STRING ) )
            // InternalDsl.g:4786:4: (lv_localDir_12_0= RULE_STRING )
            {
            // InternalDsl.g:4786:4: (lv_localDir_12_0= RULE_STRING )
            // InternalDsl.g:4787:5: lv_localDir_12_0= RULE_STRING
            {
            lv_localDir_12_0=(Token)match(input,RULE_STRING,FOLLOW_57); 

            					newLeafNode(lv_localDir_12_0, grammarAccess.getChecksumAccess().getLocalDirSTRINGTerminalRuleCall_12_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getChecksumRule());
            					}
            					setWithLastConsumed(
            						current,
            						"localDir",
            						lv_localDir_12_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_13=(Token)match(input,85,FOLLOW_3); 

            			newLeafNode(otherlv_13, grammarAccess.getChecksumAccess().getLocalFileKeyword_13());
            		
            // InternalDsl.g:4807:3: ( (lv_localFile_14_0= RULE_STRING ) )
            // InternalDsl.g:4808:4: (lv_localFile_14_0= RULE_STRING )
            {
            // InternalDsl.g:4808:4: (lv_localFile_14_0= RULE_STRING )
            // InternalDsl.g:4809:5: lv_localFile_14_0= RULE_STRING
            {
            lv_localFile_14_0=(Token)match(input,RULE_STRING,FOLLOW_58); 

            					newLeafNode(lv_localFile_14_0, grammarAccess.getChecksumAccess().getLocalFileSTRINGTerminalRuleCall_14_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getChecksumRule());
            					}
            					setWithLastConsumed(
            						current,
            						"localFile",
            						lv_localFile_14_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_15=(Token)match(input,86,FOLLOW_3); 

            			newLeafNode(otherlv_15, grammarAccess.getChecksumAccess().getRemoteDirKeyword_15());
            		
            // InternalDsl.g:4829:3: ( (lv_remoteDir_16_0= RULE_STRING ) )
            // InternalDsl.g:4830:4: (lv_remoteDir_16_0= RULE_STRING )
            {
            // InternalDsl.g:4830:4: (lv_remoteDir_16_0= RULE_STRING )
            // InternalDsl.g:4831:5: lv_remoteDir_16_0= RULE_STRING
            {
            lv_remoteDir_16_0=(Token)match(input,RULE_STRING,FOLLOW_59); 

            					newLeafNode(lv_remoteDir_16_0, grammarAccess.getChecksumAccess().getRemoteDirSTRINGTerminalRuleCall_16_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getChecksumRule());
            					}
            					setWithLastConsumed(
            						current,
            						"remoteDir",
            						lv_remoteDir_16_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_17=(Token)match(input,87,FOLLOW_3); 

            			newLeafNode(otherlv_17, grammarAccess.getChecksumAccess().getRemoteFileKeyword_17());
            		
            // InternalDsl.g:4851:3: ( (lv_remoteFile_18_0= RULE_STRING ) )
            // InternalDsl.g:4852:4: (lv_remoteFile_18_0= RULE_STRING )
            {
            // InternalDsl.g:4852:4: (lv_remoteFile_18_0= RULE_STRING )
            // InternalDsl.g:4853:5: lv_remoteFile_18_0= RULE_STRING
            {
            lv_remoteFile_18_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_remoteFile_18_0, grammarAccess.getChecksumAccess().getRemoteFileSTRINGTerminalRuleCall_18_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getChecksumRule());
            					}
            					setWithLastConsumed(
            						current,
            						"remoteFile",
            						lv_remoteFile_18_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_19=(Token)match(input,30,FOLLOW_4); 

            			newLeafNode(otherlv_19, grammarAccess.getChecksumAccess().getUsingKeyword_19());
            		
            otherlv_20=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_20, grammarAccess.getChecksumAccess().getLeftCurlyBracketKeyword_20());
            		
            // InternalDsl.g:4877:3: ( (lv_value_21_0= ruleSelectStatement ) )
            // InternalDsl.g:4878:4: (lv_value_21_0= ruleSelectStatement )
            {
            // InternalDsl.g:4878:4: (lv_value_21_0= ruleSelectStatement )
            // InternalDsl.g:4879:5: lv_value_21_0= ruleSelectStatement
            {

            					newCompositeNode(grammarAccess.getChecksumAccess().getValueSelectStatementParserRuleCall_21_0());
            				
            pushFollow(FOLLOW_8);
            lv_value_21_0=ruleSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getChecksumRule());
            					}
            					set(
            						current,
            						"value",
            						lv_value_21_0,
            						"in.handyman.Dsl.SelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_22=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_22, grammarAccess.getChecksumAccess().getRightCurlyBracketKeyword_22());
            		
            otherlv_23=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_23, grammarAccess.getChecksumAccess().getOnConditionKeyword_23());
            		
            // InternalDsl.g:4904:3: ( (lv_condition_24_0= ruleExpression ) )
            // InternalDsl.g:4905:4: (lv_condition_24_0= ruleExpression )
            {
            // InternalDsl.g:4905:4: (lv_condition_24_0= ruleExpression )
            // InternalDsl.g:4906:5: lv_condition_24_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getChecksumAccess().getConditionExpressionParserRuleCall_24_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_24_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getChecksumRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_24_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleChecksum"


    // $ANTLR start "entryRuleJsonTransform"
    // InternalDsl.g:4927:1: entryRuleJsonTransform returns [EObject current=null] : iv_ruleJsonTransform= ruleJsonTransform EOF ;
    public final EObject entryRuleJsonTransform() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleJsonTransform = null;


        try {
            // InternalDsl.g:4927:54: (iv_ruleJsonTransform= ruleJsonTransform EOF )
            // InternalDsl.g:4928:2: iv_ruleJsonTransform= ruleJsonTransform EOF
            {
             newCompositeNode(grammarAccess.getJsonTransformRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleJsonTransform=ruleJsonTransform();

            state._fsp--;

             current =iv_ruleJsonTransform; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleJsonTransform"


    // $ANTLR start "ruleJsonTransform"
    // InternalDsl.g:4934:1: ruleJsonTransform returns [EObject current=null] : (otherlv_0= 'jsontransform' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'target-table' ( (lv_targetTable_6_0= RULE_STRING ) ) otherlv_7= 'using' otherlv_8= '{' ( (lv_value_9_0= ruleSelectStatement ) ) otherlv_10= '}' otherlv_11= 'on-condition' ( (lv_condition_12_0= ruleExpression ) ) ) ;
    public final EObject ruleJsonTransform() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_source_4_0=null;
        Token otherlv_5=null;
        Token lv_targetTable_6_0=null;
        Token otherlv_7=null;
        Token otherlv_8=null;
        Token otherlv_10=null;
        Token otherlv_11=null;
        AntlrDatatypeRuleToken lv_value_9_0 = null;

        EObject lv_condition_12_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:4940:2: ( (otherlv_0= 'jsontransform' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'target-table' ( (lv_targetTable_6_0= RULE_STRING ) ) otherlv_7= 'using' otherlv_8= '{' ( (lv_value_9_0= ruleSelectStatement ) ) otherlv_10= '}' otherlv_11= 'on-condition' ( (lv_condition_12_0= ruleExpression ) ) ) )
            // InternalDsl.g:4941:2: (otherlv_0= 'jsontransform' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'target-table' ( (lv_targetTable_6_0= RULE_STRING ) ) otherlv_7= 'using' otherlv_8= '{' ( (lv_value_9_0= ruleSelectStatement ) ) otherlv_10= '}' otherlv_11= 'on-condition' ( (lv_condition_12_0= ruleExpression ) ) )
            {
            // InternalDsl.g:4941:2: (otherlv_0= 'jsontransform' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'target-table' ( (lv_targetTable_6_0= RULE_STRING ) ) otherlv_7= 'using' otherlv_8= '{' ( (lv_value_9_0= ruleSelectStatement ) ) otherlv_10= '}' otherlv_11= 'on-condition' ( (lv_condition_12_0= ruleExpression ) ) )
            // InternalDsl.g:4942:3: otherlv_0= 'jsontransform' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'target-table' ( (lv_targetTable_6_0= RULE_STRING ) ) otherlv_7= 'using' otherlv_8= '{' ( (lv_value_9_0= ruleSelectStatement ) ) otherlv_10= '}' otherlv_11= 'on-condition' ( (lv_condition_12_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,95,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getJsonTransformAccess().getJsontransformKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getJsonTransformAccess().getAsKeyword_1());
            		
            // InternalDsl.g:4950:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:4951:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:4951:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:4952:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_14); 

            					newLeafNode(lv_name_2_0, grammarAccess.getJsonTransformAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getJsonTransformRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,22,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getJsonTransformAccess().getFromKeyword_3());
            		
            // InternalDsl.g:4972:3: ( (lv_source_4_0= RULE_STRING ) )
            // InternalDsl.g:4973:4: (lv_source_4_0= RULE_STRING )
            {
            // InternalDsl.g:4973:4: (lv_source_4_0= RULE_STRING )
            // InternalDsl.g:4974:5: lv_source_4_0= RULE_STRING
            {
            lv_source_4_0=(Token)match(input,RULE_STRING,FOLLOW_41); 

            					newLeafNode(lv_source_4_0, grammarAccess.getJsonTransformAccess().getSourceSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getJsonTransformRule());
            					}
            					setWithLastConsumed(
            						current,
            						"source",
            						lv_source_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,67,FOLLOW_3); 

            			newLeafNode(otherlv_5, grammarAccess.getJsonTransformAccess().getTargetTableKeyword_5());
            		
            // InternalDsl.g:4994:3: ( (lv_targetTable_6_0= RULE_STRING ) )
            // InternalDsl.g:4995:4: (lv_targetTable_6_0= RULE_STRING )
            {
            // InternalDsl.g:4995:4: (lv_targetTable_6_0= RULE_STRING )
            // InternalDsl.g:4996:5: lv_targetTable_6_0= RULE_STRING
            {
            lv_targetTable_6_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_targetTable_6_0, grammarAccess.getJsonTransformAccess().getTargetTableSTRINGTerminalRuleCall_6_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getJsonTransformRule());
            					}
            					setWithLastConsumed(
            						current,
            						"targetTable",
            						lv_targetTable_6_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_7=(Token)match(input,30,FOLLOW_4); 

            			newLeafNode(otherlv_7, grammarAccess.getJsonTransformAccess().getUsingKeyword_7());
            		
            otherlv_8=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_8, grammarAccess.getJsonTransformAccess().getLeftCurlyBracketKeyword_8());
            		
            // InternalDsl.g:5020:3: ( (lv_value_9_0= ruleSelectStatement ) )
            // InternalDsl.g:5021:4: (lv_value_9_0= ruleSelectStatement )
            {
            // InternalDsl.g:5021:4: (lv_value_9_0= ruleSelectStatement )
            // InternalDsl.g:5022:5: lv_value_9_0= ruleSelectStatement
            {

            					newCompositeNode(grammarAccess.getJsonTransformAccess().getValueSelectStatementParserRuleCall_9_0());
            				
            pushFollow(FOLLOW_8);
            lv_value_9_0=ruleSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getJsonTransformRule());
            					}
            					set(
            						current,
            						"value",
            						lv_value_9_0,
            						"in.handyman.Dsl.SelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_10=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_10, grammarAccess.getJsonTransformAccess().getRightCurlyBracketKeyword_10());
            		
            otherlv_11=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_11, grammarAccess.getJsonTransformAccess().getOnConditionKeyword_11());
            		
            // InternalDsl.g:5047:3: ( (lv_condition_12_0= ruleExpression ) )
            // InternalDsl.g:5048:4: (lv_condition_12_0= ruleExpression )
            {
            // InternalDsl.g:5048:4: (lv_condition_12_0= ruleExpression )
            // InternalDsl.g:5049:5: lv_condition_12_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getJsonTransformAccess().getConditionExpressionParserRuleCall_12_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_12_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getJsonTransformRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_12_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleJsonTransform"


    // $ANTLR start "entryRuleJsonDeserialize"
    // InternalDsl.g:5070:1: entryRuleJsonDeserialize returns [EObject current=null] : iv_ruleJsonDeserialize= ruleJsonDeserialize EOF ;
    public final EObject entryRuleJsonDeserialize() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleJsonDeserialize = null;


        try {
            // InternalDsl.g:5070:56: (iv_ruleJsonDeserialize= ruleJsonDeserialize EOF )
            // InternalDsl.g:5071:2: iv_ruleJsonDeserialize= ruleJsonDeserialize EOF
            {
             newCompositeNode(grammarAccess.getJsonDeserializeRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleJsonDeserialize=ruleJsonDeserialize();

            state._fsp--;

             current =iv_ruleJsonDeserialize; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleJsonDeserialize"


    // $ANTLR start "ruleJsonDeserialize"
    // InternalDsl.g:5077:1: ruleJsonDeserialize returns [EObject current=null] : (otherlv_0= 'jsondeserialize' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'target-table' ( (lv_targetTable_6_0= RULE_STRING ) ) otherlv_7= 'input' ( (lv_input_8_0= RULE_STRING ) ) otherlv_9= 'using' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) ) ) ;
    public final EObject ruleJsonDeserialize() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_source_4_0=null;
        Token otherlv_5=null;
        Token lv_targetTable_6_0=null;
        Token otherlv_7=null;
        Token lv_input_8_0=null;
        Token otherlv_9=null;
        Token otherlv_10=null;
        Token otherlv_12=null;
        Token otherlv_13=null;
        AntlrDatatypeRuleToken lv_value_11_0 = null;

        EObject lv_condition_14_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:5083:2: ( (otherlv_0= 'jsondeserialize' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'target-table' ( (lv_targetTable_6_0= RULE_STRING ) ) otherlv_7= 'input' ( (lv_input_8_0= RULE_STRING ) ) otherlv_9= 'using' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) ) ) )
            // InternalDsl.g:5084:2: (otherlv_0= 'jsondeserialize' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'target-table' ( (lv_targetTable_6_0= RULE_STRING ) ) otherlv_7= 'input' ( (lv_input_8_0= RULE_STRING ) ) otherlv_9= 'using' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) ) )
            {
            // InternalDsl.g:5084:2: (otherlv_0= 'jsondeserialize' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'target-table' ( (lv_targetTable_6_0= RULE_STRING ) ) otherlv_7= 'input' ( (lv_input_8_0= RULE_STRING ) ) otherlv_9= 'using' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) ) )
            // InternalDsl.g:5085:3: otherlv_0= 'jsondeserialize' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'target-table' ( (lv_targetTable_6_0= RULE_STRING ) ) otherlv_7= 'input' ( (lv_input_8_0= RULE_STRING ) ) otherlv_9= 'using' otherlv_10= '{' ( (lv_value_11_0= ruleSelectStatement ) ) otherlv_12= '}' otherlv_13= 'on-condition' ( (lv_condition_14_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,96,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getJsonDeserializeAccess().getJsondeserializeKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getJsonDeserializeAccess().getAsKeyword_1());
            		
            // InternalDsl.g:5093:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:5094:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:5094:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:5095:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_14); 

            					newLeafNode(lv_name_2_0, grammarAccess.getJsonDeserializeAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getJsonDeserializeRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,22,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getJsonDeserializeAccess().getFromKeyword_3());
            		
            // InternalDsl.g:5115:3: ( (lv_source_4_0= RULE_STRING ) )
            // InternalDsl.g:5116:4: (lv_source_4_0= RULE_STRING )
            {
            // InternalDsl.g:5116:4: (lv_source_4_0= RULE_STRING )
            // InternalDsl.g:5117:5: lv_source_4_0= RULE_STRING
            {
            lv_source_4_0=(Token)match(input,RULE_STRING,FOLLOW_41); 

            					newLeafNode(lv_source_4_0, grammarAccess.getJsonDeserializeAccess().getSourceSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getJsonDeserializeRule());
            					}
            					setWithLastConsumed(
            						current,
            						"source",
            						lv_source_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,67,FOLLOW_3); 

            			newLeafNode(otherlv_5, grammarAccess.getJsonDeserializeAccess().getTargetTableKeyword_5());
            		
            // InternalDsl.g:5137:3: ( (lv_targetTable_6_0= RULE_STRING ) )
            // InternalDsl.g:5138:4: (lv_targetTable_6_0= RULE_STRING )
            {
            // InternalDsl.g:5138:4: (lv_targetTable_6_0= RULE_STRING )
            // InternalDsl.g:5139:5: lv_targetTable_6_0= RULE_STRING
            {
            lv_targetTable_6_0=(Token)match(input,RULE_STRING,FOLLOW_64); 

            					newLeafNode(lv_targetTable_6_0, grammarAccess.getJsonDeserializeAccess().getTargetTableSTRINGTerminalRuleCall_6_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getJsonDeserializeRule());
            					}
            					setWithLastConsumed(
            						current,
            						"targetTable",
            						lv_targetTable_6_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_7=(Token)match(input,97,FOLLOW_3); 

            			newLeafNode(otherlv_7, grammarAccess.getJsonDeserializeAccess().getInputKeyword_7());
            		
            // InternalDsl.g:5159:3: ( (lv_input_8_0= RULE_STRING ) )
            // InternalDsl.g:5160:4: (lv_input_8_0= RULE_STRING )
            {
            // InternalDsl.g:5160:4: (lv_input_8_0= RULE_STRING )
            // InternalDsl.g:5161:5: lv_input_8_0= RULE_STRING
            {
            lv_input_8_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_input_8_0, grammarAccess.getJsonDeserializeAccess().getInputSTRINGTerminalRuleCall_8_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getJsonDeserializeRule());
            					}
            					setWithLastConsumed(
            						current,
            						"input",
            						lv_input_8_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_9=(Token)match(input,30,FOLLOW_4); 

            			newLeafNode(otherlv_9, grammarAccess.getJsonDeserializeAccess().getUsingKeyword_9());
            		
            otherlv_10=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_10, grammarAccess.getJsonDeserializeAccess().getLeftCurlyBracketKeyword_10());
            		
            // InternalDsl.g:5185:3: ( (lv_value_11_0= ruleSelectStatement ) )
            // InternalDsl.g:5186:4: (lv_value_11_0= ruleSelectStatement )
            {
            // InternalDsl.g:5186:4: (lv_value_11_0= ruleSelectStatement )
            // InternalDsl.g:5187:5: lv_value_11_0= ruleSelectStatement
            {

            					newCompositeNode(grammarAccess.getJsonDeserializeAccess().getValueSelectStatementParserRuleCall_11_0());
            				
            pushFollow(FOLLOW_8);
            lv_value_11_0=ruleSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getJsonDeserializeRule());
            					}
            					set(
            						current,
            						"value",
            						lv_value_11_0,
            						"in.handyman.Dsl.SelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_12=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_12, grammarAccess.getJsonDeserializeAccess().getRightCurlyBracketKeyword_12());
            		
            otherlv_13=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_13, grammarAccess.getJsonDeserializeAccess().getOnConditionKeyword_13());
            		
            // InternalDsl.g:5212:3: ( (lv_condition_14_0= ruleExpression ) )
            // InternalDsl.g:5213:4: (lv_condition_14_0= ruleExpression )
            {
            // InternalDsl.g:5213:4: (lv_condition_14_0= ruleExpression )
            // InternalDsl.g:5214:5: lv_condition_14_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getJsonDeserializeAccess().getConditionExpressionParserRuleCall_14_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_14_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getJsonDeserializeRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_14_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleJsonDeserialize"


    // $ANTLR start "entryRuleRestApi"
    // InternalDsl.g:5235:1: entryRuleRestApi returns [EObject current=null] : iv_ruleRestApi= ruleRestApi EOF ;
    public final EObject entryRuleRestApi() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleRestApi = null;


        try {
            // InternalDsl.g:5235:48: (iv_ruleRestApi= ruleRestApi EOF )
            // InternalDsl.g:5236:2: iv_ruleRestApi= ruleRestApi EOF
            {
             newCompositeNode(grammarAccess.getRestApiRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleRestApi=ruleRestApi();

            state._fsp--;

             current =iv_ruleRestApi; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleRestApi"


    // $ANTLR start "ruleRestApi"
    // InternalDsl.g:5242:1: ruleRestApi returns [EObject current=null] : (otherlv_0= 'restapi' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'url' ( (lv_url_4_0= RULE_STRING ) ) otherlv_5= 'method' ( (lv_method_6_0= RULE_STRING ) ) otherlv_7= 'property' ( (lv_property_8_0= RULE_STRING ) ) otherlv_9= 'payload' ( (lv_payload_10_0= RULE_STRING ) ) otherlv_11= 'from' ( (lv_source_12_0= RULE_STRING ) ) otherlv_13= 'target-table' ( (lv_targetTable_14_0= RULE_STRING ) ) otherlv_15= 'using' otherlv_16= '{' ( (lv_value_17_0= ruleSelectStatement ) ) otherlv_18= '}' otherlv_19= 'on-condition' ( (lv_condition_20_0= ruleExpression ) ) ) ;
    public final EObject ruleRestApi() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_url_4_0=null;
        Token otherlv_5=null;
        Token lv_method_6_0=null;
        Token otherlv_7=null;
        Token lv_property_8_0=null;
        Token otherlv_9=null;
        Token lv_payload_10_0=null;
        Token otherlv_11=null;
        Token lv_source_12_0=null;
        Token otherlv_13=null;
        Token lv_targetTable_14_0=null;
        Token otherlv_15=null;
        Token otherlv_16=null;
        Token otherlv_18=null;
        Token otherlv_19=null;
        AntlrDatatypeRuleToken lv_value_17_0 = null;

        EObject lv_condition_20_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:5248:2: ( (otherlv_0= 'restapi' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'url' ( (lv_url_4_0= RULE_STRING ) ) otherlv_5= 'method' ( (lv_method_6_0= RULE_STRING ) ) otherlv_7= 'property' ( (lv_property_8_0= RULE_STRING ) ) otherlv_9= 'payload' ( (lv_payload_10_0= RULE_STRING ) ) otherlv_11= 'from' ( (lv_source_12_0= RULE_STRING ) ) otherlv_13= 'target-table' ( (lv_targetTable_14_0= RULE_STRING ) ) otherlv_15= 'using' otherlv_16= '{' ( (lv_value_17_0= ruleSelectStatement ) ) otherlv_18= '}' otherlv_19= 'on-condition' ( (lv_condition_20_0= ruleExpression ) ) ) )
            // InternalDsl.g:5249:2: (otherlv_0= 'restapi' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'url' ( (lv_url_4_0= RULE_STRING ) ) otherlv_5= 'method' ( (lv_method_6_0= RULE_STRING ) ) otherlv_7= 'property' ( (lv_property_8_0= RULE_STRING ) ) otherlv_9= 'payload' ( (lv_payload_10_0= RULE_STRING ) ) otherlv_11= 'from' ( (lv_source_12_0= RULE_STRING ) ) otherlv_13= 'target-table' ( (lv_targetTable_14_0= RULE_STRING ) ) otherlv_15= 'using' otherlv_16= '{' ( (lv_value_17_0= ruleSelectStatement ) ) otherlv_18= '}' otherlv_19= 'on-condition' ( (lv_condition_20_0= ruleExpression ) ) )
            {
            // InternalDsl.g:5249:2: (otherlv_0= 'restapi' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'url' ( (lv_url_4_0= RULE_STRING ) ) otherlv_5= 'method' ( (lv_method_6_0= RULE_STRING ) ) otherlv_7= 'property' ( (lv_property_8_0= RULE_STRING ) ) otherlv_9= 'payload' ( (lv_payload_10_0= RULE_STRING ) ) otherlv_11= 'from' ( (lv_source_12_0= RULE_STRING ) ) otherlv_13= 'target-table' ( (lv_targetTable_14_0= RULE_STRING ) ) otherlv_15= 'using' otherlv_16= '{' ( (lv_value_17_0= ruleSelectStatement ) ) otherlv_18= '}' otherlv_19= 'on-condition' ( (lv_condition_20_0= ruleExpression ) ) )
            // InternalDsl.g:5250:3: otherlv_0= 'restapi' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'url' ( (lv_url_4_0= RULE_STRING ) ) otherlv_5= 'method' ( (lv_method_6_0= RULE_STRING ) ) otherlv_7= 'property' ( (lv_property_8_0= RULE_STRING ) ) otherlv_9= 'payload' ( (lv_payload_10_0= RULE_STRING ) ) otherlv_11= 'from' ( (lv_source_12_0= RULE_STRING ) ) otherlv_13= 'target-table' ( (lv_targetTable_14_0= RULE_STRING ) ) otherlv_15= 'using' otherlv_16= '{' ( (lv_value_17_0= ruleSelectStatement ) ) otherlv_18= '}' otherlv_19= 'on-condition' ( (lv_condition_20_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,98,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getRestApiAccess().getRestapiKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getRestApiAccess().getAsKeyword_1());
            		
            // InternalDsl.g:5258:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:5259:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:5259:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:5260:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_65); 

            					newLeafNode(lv_name_2_0, grammarAccess.getRestApiAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getRestApiRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,99,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getRestApiAccess().getUrlKeyword_3());
            		
            // InternalDsl.g:5280:3: ( (lv_url_4_0= RULE_STRING ) )
            // InternalDsl.g:5281:4: (lv_url_4_0= RULE_STRING )
            {
            // InternalDsl.g:5281:4: (lv_url_4_0= RULE_STRING )
            // InternalDsl.g:5282:5: lv_url_4_0= RULE_STRING
            {
            lv_url_4_0=(Token)match(input,RULE_STRING,FOLLOW_66); 

            					newLeafNode(lv_url_4_0, grammarAccess.getRestApiAccess().getUrlSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getRestApiRule());
            					}
            					setWithLastConsumed(
            						current,
            						"url",
            						lv_url_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,100,FOLLOW_3); 

            			newLeafNode(otherlv_5, grammarAccess.getRestApiAccess().getMethodKeyword_5());
            		
            // InternalDsl.g:5302:3: ( (lv_method_6_0= RULE_STRING ) )
            // InternalDsl.g:5303:4: (lv_method_6_0= RULE_STRING )
            {
            // InternalDsl.g:5303:4: (lv_method_6_0= RULE_STRING )
            // InternalDsl.g:5304:5: lv_method_6_0= RULE_STRING
            {
            lv_method_6_0=(Token)match(input,RULE_STRING,FOLLOW_67); 

            					newLeafNode(lv_method_6_0, grammarAccess.getRestApiAccess().getMethodSTRINGTerminalRuleCall_6_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getRestApiRule());
            					}
            					setWithLastConsumed(
            						current,
            						"method",
            						lv_method_6_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_7=(Token)match(input,101,FOLLOW_3); 

            			newLeafNode(otherlv_7, grammarAccess.getRestApiAccess().getPropertyKeyword_7());
            		
            // InternalDsl.g:5324:3: ( (lv_property_8_0= RULE_STRING ) )
            // InternalDsl.g:5325:4: (lv_property_8_0= RULE_STRING )
            {
            // InternalDsl.g:5325:4: (lv_property_8_0= RULE_STRING )
            // InternalDsl.g:5326:5: lv_property_8_0= RULE_STRING
            {
            lv_property_8_0=(Token)match(input,RULE_STRING,FOLLOW_68); 

            					newLeafNode(lv_property_8_0, grammarAccess.getRestApiAccess().getPropertySTRINGTerminalRuleCall_8_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getRestApiRule());
            					}
            					setWithLastConsumed(
            						current,
            						"property",
            						lv_property_8_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_9=(Token)match(input,102,FOLLOW_3); 

            			newLeafNode(otherlv_9, grammarAccess.getRestApiAccess().getPayloadKeyword_9());
            		
            // InternalDsl.g:5346:3: ( (lv_payload_10_0= RULE_STRING ) )
            // InternalDsl.g:5347:4: (lv_payload_10_0= RULE_STRING )
            {
            // InternalDsl.g:5347:4: (lv_payload_10_0= RULE_STRING )
            // InternalDsl.g:5348:5: lv_payload_10_0= RULE_STRING
            {
            lv_payload_10_0=(Token)match(input,RULE_STRING,FOLLOW_14); 

            					newLeafNode(lv_payload_10_0, grammarAccess.getRestApiAccess().getPayloadSTRINGTerminalRuleCall_10_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getRestApiRule());
            					}
            					setWithLastConsumed(
            						current,
            						"payload",
            						lv_payload_10_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_11=(Token)match(input,22,FOLLOW_3); 

            			newLeafNode(otherlv_11, grammarAccess.getRestApiAccess().getFromKeyword_11());
            		
            // InternalDsl.g:5368:3: ( (lv_source_12_0= RULE_STRING ) )
            // InternalDsl.g:5369:4: (lv_source_12_0= RULE_STRING )
            {
            // InternalDsl.g:5369:4: (lv_source_12_0= RULE_STRING )
            // InternalDsl.g:5370:5: lv_source_12_0= RULE_STRING
            {
            lv_source_12_0=(Token)match(input,RULE_STRING,FOLLOW_41); 

            					newLeafNode(lv_source_12_0, grammarAccess.getRestApiAccess().getSourceSTRINGTerminalRuleCall_12_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getRestApiRule());
            					}
            					setWithLastConsumed(
            						current,
            						"source",
            						lv_source_12_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_13=(Token)match(input,67,FOLLOW_3); 

            			newLeafNode(otherlv_13, grammarAccess.getRestApiAccess().getTargetTableKeyword_13());
            		
            // InternalDsl.g:5390:3: ( (lv_targetTable_14_0= RULE_STRING ) )
            // InternalDsl.g:5391:4: (lv_targetTable_14_0= RULE_STRING )
            {
            // InternalDsl.g:5391:4: (lv_targetTable_14_0= RULE_STRING )
            // InternalDsl.g:5392:5: lv_targetTable_14_0= RULE_STRING
            {
            lv_targetTable_14_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_targetTable_14_0, grammarAccess.getRestApiAccess().getTargetTableSTRINGTerminalRuleCall_14_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getRestApiRule());
            					}
            					setWithLastConsumed(
            						current,
            						"targetTable",
            						lv_targetTable_14_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_15=(Token)match(input,30,FOLLOW_4); 

            			newLeafNode(otherlv_15, grammarAccess.getRestApiAccess().getUsingKeyword_15());
            		
            otherlv_16=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_16, grammarAccess.getRestApiAccess().getLeftCurlyBracketKeyword_16());
            		
            // InternalDsl.g:5416:3: ( (lv_value_17_0= ruleSelectStatement ) )
            // InternalDsl.g:5417:4: (lv_value_17_0= ruleSelectStatement )
            {
            // InternalDsl.g:5417:4: (lv_value_17_0= ruleSelectStatement )
            // InternalDsl.g:5418:5: lv_value_17_0= ruleSelectStatement
            {

            					newCompositeNode(grammarAccess.getRestApiAccess().getValueSelectStatementParserRuleCall_17_0());
            				
            pushFollow(FOLLOW_8);
            lv_value_17_0=ruleSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getRestApiRule());
            					}
            					set(
            						current,
            						"value",
            						lv_value_17_0,
            						"in.handyman.Dsl.SelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_18=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_18, grammarAccess.getRestApiAccess().getRightCurlyBracketKeyword_18());
            		
            otherlv_19=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_19, grammarAccess.getRestApiAccess().getOnConditionKeyword_19());
            		
            // InternalDsl.g:5443:3: ( (lv_condition_20_0= ruleExpression ) )
            // InternalDsl.g:5444:4: (lv_condition_20_0= ruleExpression )
            {
            // InternalDsl.g:5444:4: (lv_condition_20_0= ruleExpression )
            // InternalDsl.g:5445:5: lv_condition_20_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getRestApiAccess().getConditionExpressionParserRuleCall_20_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_20_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getRestApiRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_20_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleRestApi"


    // $ANTLR start "entryRulePython"
    // InternalDsl.g:5466:1: entryRulePython returns [EObject current=null] : iv_rulePython= rulePython EOF ;
    public final EObject entryRulePython() throws RecognitionException {
        EObject current = null;

        EObject iv_rulePython = null;


        try {
            // InternalDsl.g:5466:47: (iv_rulePython= rulePython EOF )
            // InternalDsl.g:5467:2: iv_rulePython= rulePython EOF
            {
             newCompositeNode(grammarAccess.getPythonRule()); 
            pushFollow(FOLLOW_1);
            iv_rulePython=rulePython();

            state._fsp--;

             current =iv_rulePython; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRulePython"


    // $ANTLR start "rulePython"
    // InternalDsl.g:5473:1: rulePython returns [EObject current=null] : (otherlv_0= 'python' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'target' ( (lv_target_6_0= RULE_STRING ) ) otherlv_7= 'using' otherlv_8= '{' ( (lv_value_9_0= ruleSelectStatement ) ) otherlv_10= '}' otherlv_11= 'on-condition' ( (lv_condition_12_0= ruleExpression ) ) ) ;
    public final EObject rulePython() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_source_4_0=null;
        Token otherlv_5=null;
        Token lv_target_6_0=null;
        Token otherlv_7=null;
        Token otherlv_8=null;
        Token otherlv_10=null;
        Token otherlv_11=null;
        AntlrDatatypeRuleToken lv_value_9_0 = null;

        EObject lv_condition_12_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:5479:2: ( (otherlv_0= 'python' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'target' ( (lv_target_6_0= RULE_STRING ) ) otherlv_7= 'using' otherlv_8= '{' ( (lv_value_9_0= ruleSelectStatement ) ) otherlv_10= '}' otherlv_11= 'on-condition' ( (lv_condition_12_0= ruleExpression ) ) ) )
            // InternalDsl.g:5480:2: (otherlv_0= 'python' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'target' ( (lv_target_6_0= RULE_STRING ) ) otherlv_7= 'using' otherlv_8= '{' ( (lv_value_9_0= ruleSelectStatement ) ) otherlv_10= '}' otherlv_11= 'on-condition' ( (lv_condition_12_0= ruleExpression ) ) )
            {
            // InternalDsl.g:5480:2: (otherlv_0= 'python' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'target' ( (lv_target_6_0= RULE_STRING ) ) otherlv_7= 'using' otherlv_8= '{' ( (lv_value_9_0= ruleSelectStatement ) ) otherlv_10= '}' otherlv_11= 'on-condition' ( (lv_condition_12_0= ruleExpression ) ) )
            // InternalDsl.g:5481:3: otherlv_0= 'python' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'from' ( (lv_source_4_0= RULE_STRING ) ) otherlv_5= 'target' ( (lv_target_6_0= RULE_STRING ) ) otherlv_7= 'using' otherlv_8= '{' ( (lv_value_9_0= ruleSelectStatement ) ) otherlv_10= '}' otherlv_11= 'on-condition' ( (lv_condition_12_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,103,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getPythonAccess().getPythonKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getPythonAccess().getAsKeyword_1());
            		
            // InternalDsl.g:5489:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:5490:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:5490:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:5491:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_14); 

            					newLeafNode(lv_name_2_0, grammarAccess.getPythonAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getPythonRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,22,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getPythonAccess().getFromKeyword_3());
            		
            // InternalDsl.g:5511:3: ( (lv_source_4_0= RULE_STRING ) )
            // InternalDsl.g:5512:4: (lv_source_4_0= RULE_STRING )
            {
            // InternalDsl.g:5512:4: (lv_source_4_0= RULE_STRING )
            // InternalDsl.g:5513:5: lv_source_4_0= RULE_STRING
            {
            lv_source_4_0=(Token)match(input,RULE_STRING,FOLLOW_37); 

            					newLeafNode(lv_source_4_0, grammarAccess.getPythonAccess().getSourceSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getPythonRule());
            					}
            					setWithLastConsumed(
            						current,
            						"source",
            						lv_source_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,54,FOLLOW_3); 

            			newLeafNode(otherlv_5, grammarAccess.getPythonAccess().getTargetKeyword_5());
            		
            // InternalDsl.g:5533:3: ( (lv_target_6_0= RULE_STRING ) )
            // InternalDsl.g:5534:4: (lv_target_6_0= RULE_STRING )
            {
            // InternalDsl.g:5534:4: (lv_target_6_0= RULE_STRING )
            // InternalDsl.g:5535:5: lv_target_6_0= RULE_STRING
            {
            lv_target_6_0=(Token)match(input,RULE_STRING,FOLLOW_22); 

            					newLeafNode(lv_target_6_0, grammarAccess.getPythonAccess().getTargetSTRINGTerminalRuleCall_6_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getPythonRule());
            					}
            					setWithLastConsumed(
            						current,
            						"target",
            						lv_target_6_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_7=(Token)match(input,30,FOLLOW_4); 

            			newLeafNode(otherlv_7, grammarAccess.getPythonAccess().getUsingKeyword_7());
            		
            otherlv_8=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_8, grammarAccess.getPythonAccess().getLeftCurlyBracketKeyword_8());
            		
            // InternalDsl.g:5559:3: ( (lv_value_9_0= ruleSelectStatement ) )
            // InternalDsl.g:5560:4: (lv_value_9_0= ruleSelectStatement )
            {
            // InternalDsl.g:5560:4: (lv_value_9_0= ruleSelectStatement )
            // InternalDsl.g:5561:5: lv_value_9_0= ruleSelectStatement
            {

            					newCompositeNode(grammarAccess.getPythonAccess().getValueSelectStatementParserRuleCall_9_0());
            				
            pushFollow(FOLLOW_8);
            lv_value_9_0=ruleSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getPythonRule());
            					}
            					set(
            						current,
            						"value",
            						lv_value_9_0,
            						"in.handyman.Dsl.SelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_10=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_10, grammarAccess.getPythonAccess().getRightCurlyBracketKeyword_10());
            		
            otherlv_11=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_11, grammarAccess.getPythonAccess().getOnConditionKeyword_11());
            		
            // InternalDsl.g:5586:3: ( (lv_condition_12_0= ruleExpression ) )
            // InternalDsl.g:5587:4: (lv_condition_12_0= ruleExpression )
            {
            // InternalDsl.g:5587:4: (lv_condition_12_0= ruleExpression )
            // InternalDsl.g:5588:5: lv_condition_12_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getPythonAccess().getConditionExpressionParserRuleCall_12_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_12_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getPythonRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_12_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "rulePython"


    // $ANTLR start "entryRuleRest"
    // InternalDsl.g:5609:1: entryRuleRest returns [EObject current=null] : iv_ruleRest= ruleRest EOF ;
    public final EObject entryRuleRest() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleRest = null;


        try {
            // InternalDsl.g:5609:45: (iv_ruleRest= ruleRest EOF )
            // InternalDsl.g:5610:2: iv_ruleRest= ruleRest EOF
            {
             newCompositeNode(grammarAccess.getRestRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleRest=ruleRest();

            state._fsp--;

             current =iv_ruleRest; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleRest"


    // $ANTLR start "ruleRest"
    // InternalDsl.g:5616:1: ruleRest returns [EObject current=null] : (otherlv_0= 'rest' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'secured-by' ( (lv_authtoken_4_0= RULE_STRING ) ) otherlv_5= 'with-url' ( (lv_url_6_0= RULE_STRING ) ) otherlv_7= 'and-method-as' ( (lv_method_8_0= RULE_STRING ) ) otherlv_9= '{' otherlv_10= 'from' ( (lv_resourcedatafrom_11_0= RULE_STRING ) ) otherlv_12= 'update-url-with' otherlv_13= '{' ( (lv_urldata_14_0= ruleSelectStatement ) ) otherlv_15= '}' otherlv_16= 'from' ( (lv_headerdatafrom_17_0= RULE_STRING ) ) otherlv_18= 'update-header-with' otherlv_19= '{' ( (lv_headerdata_20_0= ruleSelectStatement ) ) otherlv_21= '}' otherlv_22= 'from' ( (lv_postdatafrom_23_0= RULE_STRING ) ) otherlv_24= 'update-body-with' otherlv_25= '{' otherlv_26= 'parent' otherlv_27= 'as' ( (lv_parentName_28_0= RULE_STRING ) ) ( (lv_parentdata_29_0= ruleSelectStatement ) ) ( (lv_parts_30_0= ruleRestPart ) )* otherlv_31= '}' otherlv_32= 'into' ( (lv_ackdatato_33_0= RULE_STRING ) ) otherlv_34= 'store-ack-at' otherlv_35= '{' ( (lv_ackdata_36_0= ruleSelectStatement ) ) otherlv_37= '}' otherlv_38= '}' otherlv_39= 'on-condition' ( (lv_condition_40_0= ruleExpression ) ) ) ;
    public final EObject ruleRest() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_name_2_0=null;
        Token otherlv_3=null;
        Token lv_authtoken_4_0=null;
        Token otherlv_5=null;
        Token lv_url_6_0=null;
        Token otherlv_7=null;
        Token lv_method_8_0=null;
        Token otherlv_9=null;
        Token otherlv_10=null;
        Token lv_resourcedatafrom_11_0=null;
        Token otherlv_12=null;
        Token otherlv_13=null;
        Token otherlv_15=null;
        Token otherlv_16=null;
        Token lv_headerdatafrom_17_0=null;
        Token otherlv_18=null;
        Token otherlv_19=null;
        Token otherlv_21=null;
        Token otherlv_22=null;
        Token lv_postdatafrom_23_0=null;
        Token otherlv_24=null;
        Token otherlv_25=null;
        Token otherlv_26=null;
        Token otherlv_27=null;
        Token lv_parentName_28_0=null;
        Token otherlv_31=null;
        Token otherlv_32=null;
        Token lv_ackdatato_33_0=null;
        Token otherlv_34=null;
        Token otherlv_35=null;
        Token otherlv_37=null;
        Token otherlv_38=null;
        Token otherlv_39=null;
        AntlrDatatypeRuleToken lv_urldata_14_0 = null;

        AntlrDatatypeRuleToken lv_headerdata_20_0 = null;

        AntlrDatatypeRuleToken lv_parentdata_29_0 = null;

        EObject lv_parts_30_0 = null;

        AntlrDatatypeRuleToken lv_ackdata_36_0 = null;

        EObject lv_condition_40_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:5622:2: ( (otherlv_0= 'rest' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'secured-by' ( (lv_authtoken_4_0= RULE_STRING ) ) otherlv_5= 'with-url' ( (lv_url_6_0= RULE_STRING ) ) otherlv_7= 'and-method-as' ( (lv_method_8_0= RULE_STRING ) ) otherlv_9= '{' otherlv_10= 'from' ( (lv_resourcedatafrom_11_0= RULE_STRING ) ) otherlv_12= 'update-url-with' otherlv_13= '{' ( (lv_urldata_14_0= ruleSelectStatement ) ) otherlv_15= '}' otherlv_16= 'from' ( (lv_headerdatafrom_17_0= RULE_STRING ) ) otherlv_18= 'update-header-with' otherlv_19= '{' ( (lv_headerdata_20_0= ruleSelectStatement ) ) otherlv_21= '}' otherlv_22= 'from' ( (lv_postdatafrom_23_0= RULE_STRING ) ) otherlv_24= 'update-body-with' otherlv_25= '{' otherlv_26= 'parent' otherlv_27= 'as' ( (lv_parentName_28_0= RULE_STRING ) ) ( (lv_parentdata_29_0= ruleSelectStatement ) ) ( (lv_parts_30_0= ruleRestPart ) )* otherlv_31= '}' otherlv_32= 'into' ( (lv_ackdatato_33_0= RULE_STRING ) ) otherlv_34= 'store-ack-at' otherlv_35= '{' ( (lv_ackdata_36_0= ruleSelectStatement ) ) otherlv_37= '}' otherlv_38= '}' otherlv_39= 'on-condition' ( (lv_condition_40_0= ruleExpression ) ) ) )
            // InternalDsl.g:5623:2: (otherlv_0= 'rest' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'secured-by' ( (lv_authtoken_4_0= RULE_STRING ) ) otherlv_5= 'with-url' ( (lv_url_6_0= RULE_STRING ) ) otherlv_7= 'and-method-as' ( (lv_method_8_0= RULE_STRING ) ) otherlv_9= '{' otherlv_10= 'from' ( (lv_resourcedatafrom_11_0= RULE_STRING ) ) otherlv_12= 'update-url-with' otherlv_13= '{' ( (lv_urldata_14_0= ruleSelectStatement ) ) otherlv_15= '}' otherlv_16= 'from' ( (lv_headerdatafrom_17_0= RULE_STRING ) ) otherlv_18= 'update-header-with' otherlv_19= '{' ( (lv_headerdata_20_0= ruleSelectStatement ) ) otherlv_21= '}' otherlv_22= 'from' ( (lv_postdatafrom_23_0= RULE_STRING ) ) otherlv_24= 'update-body-with' otherlv_25= '{' otherlv_26= 'parent' otherlv_27= 'as' ( (lv_parentName_28_0= RULE_STRING ) ) ( (lv_parentdata_29_0= ruleSelectStatement ) ) ( (lv_parts_30_0= ruleRestPart ) )* otherlv_31= '}' otherlv_32= 'into' ( (lv_ackdatato_33_0= RULE_STRING ) ) otherlv_34= 'store-ack-at' otherlv_35= '{' ( (lv_ackdata_36_0= ruleSelectStatement ) ) otherlv_37= '}' otherlv_38= '}' otherlv_39= 'on-condition' ( (lv_condition_40_0= ruleExpression ) ) )
            {
            // InternalDsl.g:5623:2: (otherlv_0= 'rest' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'secured-by' ( (lv_authtoken_4_0= RULE_STRING ) ) otherlv_5= 'with-url' ( (lv_url_6_0= RULE_STRING ) ) otherlv_7= 'and-method-as' ( (lv_method_8_0= RULE_STRING ) ) otherlv_9= '{' otherlv_10= 'from' ( (lv_resourcedatafrom_11_0= RULE_STRING ) ) otherlv_12= 'update-url-with' otherlv_13= '{' ( (lv_urldata_14_0= ruleSelectStatement ) ) otherlv_15= '}' otherlv_16= 'from' ( (lv_headerdatafrom_17_0= RULE_STRING ) ) otherlv_18= 'update-header-with' otherlv_19= '{' ( (lv_headerdata_20_0= ruleSelectStatement ) ) otherlv_21= '}' otherlv_22= 'from' ( (lv_postdatafrom_23_0= RULE_STRING ) ) otherlv_24= 'update-body-with' otherlv_25= '{' otherlv_26= 'parent' otherlv_27= 'as' ( (lv_parentName_28_0= RULE_STRING ) ) ( (lv_parentdata_29_0= ruleSelectStatement ) ) ( (lv_parts_30_0= ruleRestPart ) )* otherlv_31= '}' otherlv_32= 'into' ( (lv_ackdatato_33_0= RULE_STRING ) ) otherlv_34= 'store-ack-at' otherlv_35= '{' ( (lv_ackdata_36_0= ruleSelectStatement ) ) otherlv_37= '}' otherlv_38= '}' otherlv_39= 'on-condition' ( (lv_condition_40_0= ruleExpression ) ) )
            // InternalDsl.g:5624:3: otherlv_0= 'rest' otherlv_1= 'as' ( (lv_name_2_0= RULE_STRING ) ) otherlv_3= 'secured-by' ( (lv_authtoken_4_0= RULE_STRING ) ) otherlv_5= 'with-url' ( (lv_url_6_0= RULE_STRING ) ) otherlv_7= 'and-method-as' ( (lv_method_8_0= RULE_STRING ) ) otherlv_9= '{' otherlv_10= 'from' ( (lv_resourcedatafrom_11_0= RULE_STRING ) ) otherlv_12= 'update-url-with' otherlv_13= '{' ( (lv_urldata_14_0= ruleSelectStatement ) ) otherlv_15= '}' otherlv_16= 'from' ( (lv_headerdatafrom_17_0= RULE_STRING ) ) otherlv_18= 'update-header-with' otherlv_19= '{' ( (lv_headerdata_20_0= ruleSelectStatement ) ) otherlv_21= '}' otherlv_22= 'from' ( (lv_postdatafrom_23_0= RULE_STRING ) ) otherlv_24= 'update-body-with' otherlv_25= '{' otherlv_26= 'parent' otherlv_27= 'as' ( (lv_parentName_28_0= RULE_STRING ) ) ( (lv_parentdata_29_0= ruleSelectStatement ) ) ( (lv_parts_30_0= ruleRestPart ) )* otherlv_31= '}' otherlv_32= 'into' ( (lv_ackdatato_33_0= RULE_STRING ) ) otherlv_34= 'store-ack-at' otherlv_35= '{' ( (lv_ackdata_36_0= ruleSelectStatement ) ) otherlv_37= '}' otherlv_38= '}' otherlv_39= 'on-condition' ( (lv_condition_40_0= ruleExpression ) )
            {
            otherlv_0=(Token)match(input,104,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getRestAccess().getRestKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getRestAccess().getAsKeyword_1());
            		
            // InternalDsl.g:5632:3: ( (lv_name_2_0= RULE_STRING ) )
            // InternalDsl.g:5633:4: (lv_name_2_0= RULE_STRING )
            {
            // InternalDsl.g:5633:4: (lv_name_2_0= RULE_STRING )
            // InternalDsl.g:5634:5: lv_name_2_0= RULE_STRING
            {
            lv_name_2_0=(Token)match(input,RULE_STRING,FOLLOW_69); 

            					newLeafNode(lv_name_2_0, grammarAccess.getRestAccess().getNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getRestRule());
            					}
            					setWithLastConsumed(
            						current,
            						"name",
            						lv_name_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,105,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getRestAccess().getSecuredByKeyword_3());
            		
            // InternalDsl.g:5654:3: ( (lv_authtoken_4_0= RULE_STRING ) )
            // InternalDsl.g:5655:4: (lv_authtoken_4_0= RULE_STRING )
            {
            // InternalDsl.g:5655:4: (lv_authtoken_4_0= RULE_STRING )
            // InternalDsl.g:5656:5: lv_authtoken_4_0= RULE_STRING
            {
            lv_authtoken_4_0=(Token)match(input,RULE_STRING,FOLLOW_70); 

            					newLeafNode(lv_authtoken_4_0, grammarAccess.getRestAccess().getAuthtokenSTRINGTerminalRuleCall_4_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getRestRule());
            					}
            					setWithLastConsumed(
            						current,
            						"authtoken",
            						lv_authtoken_4_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_5=(Token)match(input,106,FOLLOW_3); 

            			newLeafNode(otherlv_5, grammarAccess.getRestAccess().getWithUrlKeyword_5());
            		
            // InternalDsl.g:5676:3: ( (lv_url_6_0= RULE_STRING ) )
            // InternalDsl.g:5677:4: (lv_url_6_0= RULE_STRING )
            {
            // InternalDsl.g:5677:4: (lv_url_6_0= RULE_STRING )
            // InternalDsl.g:5678:5: lv_url_6_0= RULE_STRING
            {
            lv_url_6_0=(Token)match(input,RULE_STRING,FOLLOW_71); 

            					newLeafNode(lv_url_6_0, grammarAccess.getRestAccess().getUrlSTRINGTerminalRuleCall_6_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getRestRule());
            					}
            					setWithLastConsumed(
            						current,
            						"url",
            						lv_url_6_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_7=(Token)match(input,107,FOLLOW_3); 

            			newLeafNode(otherlv_7, grammarAccess.getRestAccess().getAndMethodAsKeyword_7());
            		
            // InternalDsl.g:5698:3: ( (lv_method_8_0= RULE_STRING ) )
            // InternalDsl.g:5699:4: (lv_method_8_0= RULE_STRING )
            {
            // InternalDsl.g:5699:4: (lv_method_8_0= RULE_STRING )
            // InternalDsl.g:5700:5: lv_method_8_0= RULE_STRING
            {
            lv_method_8_0=(Token)match(input,RULE_STRING,FOLLOW_4); 

            					newLeafNode(lv_method_8_0, grammarAccess.getRestAccess().getMethodSTRINGTerminalRuleCall_8_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getRestRule());
            					}
            					setWithLastConsumed(
            						current,
            						"method",
            						lv_method_8_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_9=(Token)match(input,12,FOLLOW_14); 

            			newLeafNode(otherlv_9, grammarAccess.getRestAccess().getLeftCurlyBracketKeyword_9());
            		
            otherlv_10=(Token)match(input,22,FOLLOW_3); 

            			newLeafNode(otherlv_10, grammarAccess.getRestAccess().getFromKeyword_10());
            		
            // InternalDsl.g:5724:3: ( (lv_resourcedatafrom_11_0= RULE_STRING ) )
            // InternalDsl.g:5725:4: (lv_resourcedatafrom_11_0= RULE_STRING )
            {
            // InternalDsl.g:5725:4: (lv_resourcedatafrom_11_0= RULE_STRING )
            // InternalDsl.g:5726:5: lv_resourcedatafrom_11_0= RULE_STRING
            {
            lv_resourcedatafrom_11_0=(Token)match(input,RULE_STRING,FOLLOW_72); 

            					newLeafNode(lv_resourcedatafrom_11_0, grammarAccess.getRestAccess().getResourcedatafromSTRINGTerminalRuleCall_11_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getRestRule());
            					}
            					setWithLastConsumed(
            						current,
            						"resourcedatafrom",
            						lv_resourcedatafrom_11_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_12=(Token)match(input,108,FOLLOW_4); 

            			newLeafNode(otherlv_12, grammarAccess.getRestAccess().getUpdateUrlWithKeyword_12());
            		
            otherlv_13=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_13, grammarAccess.getRestAccess().getLeftCurlyBracketKeyword_13());
            		
            // InternalDsl.g:5750:3: ( (lv_urldata_14_0= ruleSelectStatement ) )
            // InternalDsl.g:5751:4: (lv_urldata_14_0= ruleSelectStatement )
            {
            // InternalDsl.g:5751:4: (lv_urldata_14_0= ruleSelectStatement )
            // InternalDsl.g:5752:5: lv_urldata_14_0= ruleSelectStatement
            {

            					newCompositeNode(grammarAccess.getRestAccess().getUrldataSelectStatementParserRuleCall_14_0());
            				
            pushFollow(FOLLOW_8);
            lv_urldata_14_0=ruleSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getRestRule());
            					}
            					set(
            						current,
            						"urldata",
            						lv_urldata_14_0,
            						"in.handyman.Dsl.SelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_15=(Token)match(input,13,FOLLOW_14); 

            			newLeafNode(otherlv_15, grammarAccess.getRestAccess().getRightCurlyBracketKeyword_15());
            		
            otherlv_16=(Token)match(input,22,FOLLOW_3); 

            			newLeafNode(otherlv_16, grammarAccess.getRestAccess().getFromKeyword_16());
            		
            // InternalDsl.g:5777:3: ( (lv_headerdatafrom_17_0= RULE_STRING ) )
            // InternalDsl.g:5778:4: (lv_headerdatafrom_17_0= RULE_STRING )
            {
            // InternalDsl.g:5778:4: (lv_headerdatafrom_17_0= RULE_STRING )
            // InternalDsl.g:5779:5: lv_headerdatafrom_17_0= RULE_STRING
            {
            lv_headerdatafrom_17_0=(Token)match(input,RULE_STRING,FOLLOW_73); 

            					newLeafNode(lv_headerdatafrom_17_0, grammarAccess.getRestAccess().getHeaderdatafromSTRINGTerminalRuleCall_17_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getRestRule());
            					}
            					setWithLastConsumed(
            						current,
            						"headerdatafrom",
            						lv_headerdatafrom_17_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_18=(Token)match(input,109,FOLLOW_4); 

            			newLeafNode(otherlv_18, grammarAccess.getRestAccess().getUpdateHeaderWithKeyword_18());
            		
            otherlv_19=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_19, grammarAccess.getRestAccess().getLeftCurlyBracketKeyword_19());
            		
            // InternalDsl.g:5803:3: ( (lv_headerdata_20_0= ruleSelectStatement ) )
            // InternalDsl.g:5804:4: (lv_headerdata_20_0= ruleSelectStatement )
            {
            // InternalDsl.g:5804:4: (lv_headerdata_20_0= ruleSelectStatement )
            // InternalDsl.g:5805:5: lv_headerdata_20_0= ruleSelectStatement
            {

            					newCompositeNode(grammarAccess.getRestAccess().getHeaderdataSelectStatementParserRuleCall_20_0());
            				
            pushFollow(FOLLOW_8);
            lv_headerdata_20_0=ruleSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getRestRule());
            					}
            					set(
            						current,
            						"headerdata",
            						lv_headerdata_20_0,
            						"in.handyman.Dsl.SelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_21=(Token)match(input,13,FOLLOW_14); 

            			newLeafNode(otherlv_21, grammarAccess.getRestAccess().getRightCurlyBracketKeyword_21());
            		
            otherlv_22=(Token)match(input,22,FOLLOW_3); 

            			newLeafNode(otherlv_22, grammarAccess.getRestAccess().getFromKeyword_22());
            		
            // InternalDsl.g:5830:3: ( (lv_postdatafrom_23_0= RULE_STRING ) )
            // InternalDsl.g:5831:4: (lv_postdatafrom_23_0= RULE_STRING )
            {
            // InternalDsl.g:5831:4: (lv_postdatafrom_23_0= RULE_STRING )
            // InternalDsl.g:5832:5: lv_postdatafrom_23_0= RULE_STRING
            {
            lv_postdatafrom_23_0=(Token)match(input,RULE_STRING,FOLLOW_74); 

            					newLeafNode(lv_postdatafrom_23_0, grammarAccess.getRestAccess().getPostdatafromSTRINGTerminalRuleCall_23_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getRestRule());
            					}
            					setWithLastConsumed(
            						current,
            						"postdatafrom",
            						lv_postdatafrom_23_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_24=(Token)match(input,110,FOLLOW_4); 

            			newLeafNode(otherlv_24, grammarAccess.getRestAccess().getUpdateBodyWithKeyword_24());
            		
            otherlv_25=(Token)match(input,12,FOLLOW_75); 

            			newLeafNode(otherlv_25, grammarAccess.getRestAccess().getLeftCurlyBracketKeyword_25());
            		
            otherlv_26=(Token)match(input,111,FOLLOW_10); 

            			newLeafNode(otherlv_26, grammarAccess.getRestAccess().getParentKeyword_26());
            		
            otherlv_27=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_27, grammarAccess.getRestAccess().getAsKeyword_27());
            		
            // InternalDsl.g:5864:3: ( (lv_parentName_28_0= RULE_STRING ) )
            // InternalDsl.g:5865:4: (lv_parentName_28_0= RULE_STRING )
            {
            // InternalDsl.g:5865:4: (lv_parentName_28_0= RULE_STRING )
            // InternalDsl.g:5866:5: lv_parentName_28_0= RULE_STRING
            {
            lv_parentName_28_0=(Token)match(input,RULE_STRING,FOLLOW_3); 

            					newLeafNode(lv_parentName_28_0, grammarAccess.getRestAccess().getParentNameSTRINGTerminalRuleCall_28_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getRestRule());
            					}
            					setWithLastConsumed(
            						current,
            						"parentName",
            						lv_parentName_28_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            // InternalDsl.g:5882:3: ( (lv_parentdata_29_0= ruleSelectStatement ) )
            // InternalDsl.g:5883:4: (lv_parentdata_29_0= ruleSelectStatement )
            {
            // InternalDsl.g:5883:4: (lv_parentdata_29_0= ruleSelectStatement )
            // InternalDsl.g:5884:5: lv_parentdata_29_0= ruleSelectStatement
            {

            					newCompositeNode(grammarAccess.getRestAccess().getParentdataSelectStatementParserRuleCall_29_0());
            				
            pushFollow(FOLLOW_76);
            lv_parentdata_29_0=ruleSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getRestRule());
            					}
            					set(
            						current,
            						"parentdata",
            						lv_parentdata_29_0,
            						"in.handyman.Dsl.SelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            // InternalDsl.g:5901:3: ( (lv_parts_30_0= ruleRestPart ) )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==114) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // InternalDsl.g:5902:4: (lv_parts_30_0= ruleRestPart )
            	    {
            	    // InternalDsl.g:5902:4: (lv_parts_30_0= ruleRestPart )
            	    // InternalDsl.g:5903:5: lv_parts_30_0= ruleRestPart
            	    {

            	    					newCompositeNode(grammarAccess.getRestAccess().getPartsRestPartParserRuleCall_30_0());
            	    				
            	    pushFollow(FOLLOW_76);
            	    lv_parts_30_0=ruleRestPart();

            	    state._fsp--;


            	    					if (current==null) {
            	    						current = createModelElementForParent(grammarAccess.getRestRule());
            	    					}
            	    					add(
            	    						current,
            	    						"parts",
            	    						lv_parts_30_0,
            	    						"in.handyman.Dsl.RestPart");
            	    					afterParserOrEnumRuleCall();
            	    				

            	    }


            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);

            otherlv_31=(Token)match(input,13,FOLLOW_77); 

            			newLeafNode(otherlv_31, grammarAccess.getRestAccess().getRightCurlyBracketKeyword_31());
            		
            otherlv_32=(Token)match(input,112,FOLLOW_3); 

            			newLeafNode(otherlv_32, grammarAccess.getRestAccess().getIntoKeyword_32());
            		
            // InternalDsl.g:5928:3: ( (lv_ackdatato_33_0= RULE_STRING ) )
            // InternalDsl.g:5929:4: (lv_ackdatato_33_0= RULE_STRING )
            {
            // InternalDsl.g:5929:4: (lv_ackdatato_33_0= RULE_STRING )
            // InternalDsl.g:5930:5: lv_ackdatato_33_0= RULE_STRING
            {
            lv_ackdatato_33_0=(Token)match(input,RULE_STRING,FOLLOW_78); 

            					newLeafNode(lv_ackdatato_33_0, grammarAccess.getRestAccess().getAckdatatoSTRINGTerminalRuleCall_33_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getRestRule());
            					}
            					setWithLastConsumed(
            						current,
            						"ackdatato",
            						lv_ackdatato_33_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_34=(Token)match(input,113,FOLLOW_4); 

            			newLeafNode(otherlv_34, grammarAccess.getRestAccess().getStoreAckAtKeyword_34());
            		
            otherlv_35=(Token)match(input,12,FOLLOW_3); 

            			newLeafNode(otherlv_35, grammarAccess.getRestAccess().getLeftCurlyBracketKeyword_35());
            		
            // InternalDsl.g:5954:3: ( (lv_ackdata_36_0= ruleSelectStatement ) )
            // InternalDsl.g:5955:4: (lv_ackdata_36_0= ruleSelectStatement )
            {
            // InternalDsl.g:5955:4: (lv_ackdata_36_0= ruleSelectStatement )
            // InternalDsl.g:5956:5: lv_ackdata_36_0= ruleSelectStatement
            {

            					newCompositeNode(grammarAccess.getRestAccess().getAckdataSelectStatementParserRuleCall_36_0());
            				
            pushFollow(FOLLOW_8);
            lv_ackdata_36_0=ruleSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getRestRule());
            					}
            					set(
            						current,
            						"ackdata",
            						lv_ackdata_36_0,
            						"in.handyman.Dsl.SelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            otherlv_37=(Token)match(input,13,FOLLOW_8); 

            			newLeafNode(otherlv_37, grammarAccess.getRestAccess().getRightCurlyBracketKeyword_37());
            		
            otherlv_38=(Token)match(input,13,FOLLOW_23); 

            			newLeafNode(otherlv_38, grammarAccess.getRestAccess().getRightCurlyBracketKeyword_38());
            		
            otherlv_39=(Token)match(input,31,FOLLOW_24); 

            			newLeafNode(otherlv_39, grammarAccess.getRestAccess().getOnConditionKeyword_39());
            		
            // InternalDsl.g:5985:3: ( (lv_condition_40_0= ruleExpression ) )
            // InternalDsl.g:5986:4: (lv_condition_40_0= ruleExpression )
            {
            // InternalDsl.g:5986:4: (lv_condition_40_0= ruleExpression )
            // InternalDsl.g:5987:5: lv_condition_40_0= ruleExpression
            {

            					newCompositeNode(grammarAccess.getRestAccess().getConditionExpressionParserRuleCall_40_0());
            				
            pushFollow(FOLLOW_2);
            lv_condition_40_0=ruleExpression();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getRestRule());
            					}
            					set(
            						current,
            						"condition",
            						lv_condition_40_0,
            						"in.handyman.Dsl.Expression");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleRest"


    // $ANTLR start "entryRuleRestPart"
    // InternalDsl.g:6008:1: entryRuleRestPart returns [EObject current=null] : iv_ruleRestPart= ruleRestPart EOF ;
    public final EObject entryRuleRestPart() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleRestPart = null;


        try {
            // InternalDsl.g:6008:49: (iv_ruleRestPart= ruleRestPart EOF )
            // InternalDsl.g:6009:2: iv_ruleRestPart= ruleRestPart EOF
            {
             newCompositeNode(grammarAccess.getRestPartRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleRestPart=ruleRestPart();

            state._fsp--;

             current =iv_ruleRestPart; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleRestPart"


    // $ANTLR start "ruleRestPart"
    // InternalDsl.g:6015:1: ruleRestPart returns [EObject current=null] : (otherlv_0= 'part' otherlv_1= 'as' ( (lv_partName_2_0= RULE_STRING ) ) otherlv_3= 'with' ( (lv_partData_4_0= ruleSelectStatement ) ) ) ;
    public final EObject ruleRestPart() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token otherlv_1=null;
        Token lv_partName_2_0=null;
        Token otherlv_3=null;
        AntlrDatatypeRuleToken lv_partData_4_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:6021:2: ( (otherlv_0= 'part' otherlv_1= 'as' ( (lv_partName_2_0= RULE_STRING ) ) otherlv_3= 'with' ( (lv_partData_4_0= ruleSelectStatement ) ) ) )
            // InternalDsl.g:6022:2: (otherlv_0= 'part' otherlv_1= 'as' ( (lv_partName_2_0= RULE_STRING ) ) otherlv_3= 'with' ( (lv_partData_4_0= ruleSelectStatement ) ) )
            {
            // InternalDsl.g:6022:2: (otherlv_0= 'part' otherlv_1= 'as' ( (lv_partName_2_0= RULE_STRING ) ) otherlv_3= 'with' ( (lv_partData_4_0= ruleSelectStatement ) ) )
            // InternalDsl.g:6023:3: otherlv_0= 'part' otherlv_1= 'as' ( (lv_partName_2_0= RULE_STRING ) ) otherlv_3= 'with' ( (lv_partData_4_0= ruleSelectStatement ) )
            {
            otherlv_0=(Token)match(input,114,FOLLOW_10); 

            			newLeafNode(otherlv_0, grammarAccess.getRestPartAccess().getPartKeyword_0());
            		
            otherlv_1=(Token)match(input,18,FOLLOW_3); 

            			newLeafNode(otherlv_1, grammarAccess.getRestPartAccess().getAsKeyword_1());
            		
            // InternalDsl.g:6031:3: ( (lv_partName_2_0= RULE_STRING ) )
            // InternalDsl.g:6032:4: (lv_partName_2_0= RULE_STRING )
            {
            // InternalDsl.g:6032:4: (lv_partName_2_0= RULE_STRING )
            // InternalDsl.g:6033:5: lv_partName_2_0= RULE_STRING
            {
            lv_partName_2_0=(Token)match(input,RULE_STRING,FOLLOW_39); 

            					newLeafNode(lv_partName_2_0, grammarAccess.getRestPartAccess().getPartNameSTRINGTerminalRuleCall_2_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getRestPartRule());
            					}
            					setWithLastConsumed(
            						current,
            						"partName",
            						lv_partName_2_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            otherlv_3=(Token)match(input,57,FOLLOW_3); 

            			newLeafNode(otherlv_3, grammarAccess.getRestPartAccess().getWithKeyword_3());
            		
            // InternalDsl.g:6053:3: ( (lv_partData_4_0= ruleSelectStatement ) )
            // InternalDsl.g:6054:4: (lv_partData_4_0= ruleSelectStatement )
            {
            // InternalDsl.g:6054:4: (lv_partData_4_0= ruleSelectStatement )
            // InternalDsl.g:6055:5: lv_partData_4_0= ruleSelectStatement
            {

            					newCompositeNode(grammarAccess.getRestPartAccess().getPartDataSelectStatementParserRuleCall_4_0());
            				
            pushFollow(FOLLOW_2);
            lv_partData_4_0=ruleSelectStatement();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getRestPartRule());
            					}
            					set(
            						current,
            						"partData",
            						lv_partData_4_0,
            						"in.handyman.Dsl.SelectStatement");
            					afterParserOrEnumRuleCall();
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleRestPart"


    // $ANTLR start "entryRuleSelectStatement"
    // InternalDsl.g:6076:1: entryRuleSelectStatement returns [String current=null] : iv_ruleSelectStatement= ruleSelectStatement EOF ;
    public final String entryRuleSelectStatement() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleSelectStatement = null;


        try {
            // InternalDsl.g:6076:55: (iv_ruleSelectStatement= ruleSelectStatement EOF )
            // InternalDsl.g:6077:2: iv_ruleSelectStatement= ruleSelectStatement EOF
            {
             newCompositeNode(grammarAccess.getSelectStatementRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleSelectStatement=ruleSelectStatement();

            state._fsp--;

             current =iv_ruleSelectStatement.getText(); 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleSelectStatement"


    // $ANTLR start "ruleSelectStatement"
    // InternalDsl.g:6083:1: ruleSelectStatement returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : this_STRING_0= RULE_STRING ;
    public final AntlrDatatypeRuleToken ruleSelectStatement() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_STRING_0=null;


        	enterRule();

        try {
            // InternalDsl.g:6089:2: (this_STRING_0= RULE_STRING )
            // InternalDsl.g:6090:2: this_STRING_0= RULE_STRING
            {
            this_STRING_0=(Token)match(input,RULE_STRING,FOLLOW_2); 

            		current.merge(this_STRING_0);
            	

            		newLeafNode(this_STRING_0, grammarAccess.getSelectStatementAccess().getSTRINGTerminalRuleCall());
            	

            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleSelectStatement"


    // $ANTLR start "entryRuleNonSelectStatement"
    // InternalDsl.g:6100:1: entryRuleNonSelectStatement returns [String current=null] : iv_ruleNonSelectStatement= ruleNonSelectStatement EOF ;
    public final String entryRuleNonSelectStatement() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleNonSelectStatement = null;


        try {
            // InternalDsl.g:6100:58: (iv_ruleNonSelectStatement= ruleNonSelectStatement EOF )
            // InternalDsl.g:6101:2: iv_ruleNonSelectStatement= ruleNonSelectStatement EOF
            {
             newCompositeNode(grammarAccess.getNonSelectStatementRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleNonSelectStatement=ruleNonSelectStatement();

            state._fsp--;

             current =iv_ruleNonSelectStatement.getText(); 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleNonSelectStatement"


    // $ANTLR start "ruleNonSelectStatement"
    // InternalDsl.g:6107:1: ruleNonSelectStatement returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : this_STRING_0= RULE_STRING ;
    public final AntlrDatatypeRuleToken ruleNonSelectStatement() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_STRING_0=null;


        	enterRule();

        try {
            // InternalDsl.g:6113:2: (this_STRING_0= RULE_STRING )
            // InternalDsl.g:6114:2: this_STRING_0= RULE_STRING
            {
            this_STRING_0=(Token)match(input,RULE_STRING,FOLLOW_2); 

            		current.merge(this_STRING_0);
            	

            		newLeafNode(this_STRING_0, grammarAccess.getNonSelectStatementAccess().getSTRINGTerminalRuleCall());
            	

            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleNonSelectStatement"


    // $ANTLR start "entryRuleCreateStatement"
    // InternalDsl.g:6124:1: entryRuleCreateStatement returns [String current=null] : iv_ruleCreateStatement= ruleCreateStatement EOF ;
    public final String entryRuleCreateStatement() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleCreateStatement = null;


        try {
            // InternalDsl.g:6124:55: (iv_ruleCreateStatement= ruleCreateStatement EOF )
            // InternalDsl.g:6125:2: iv_ruleCreateStatement= ruleCreateStatement EOF
            {
             newCompositeNode(grammarAccess.getCreateStatementRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleCreateStatement=ruleCreateStatement();

            state._fsp--;

             current =iv_ruleCreateStatement.getText(); 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleCreateStatement"


    // $ANTLR start "ruleCreateStatement"
    // InternalDsl.g:6131:1: ruleCreateStatement returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : this_STRING_0= RULE_STRING ;
    public final AntlrDatatypeRuleToken ruleCreateStatement() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token this_STRING_0=null;


        	enterRule();

        try {
            // InternalDsl.g:6137:2: (this_STRING_0= RULE_STRING )
            // InternalDsl.g:6138:2: this_STRING_0= RULE_STRING
            {
            this_STRING_0=(Token)match(input,RULE_STRING,FOLLOW_2); 

            		current.merge(this_STRING_0);
            	

            		newLeafNode(this_STRING_0, grammarAccess.getCreateStatementAccess().getSTRINGTerminalRuleCall());
            	

            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleCreateStatement"


    // $ANTLR start "entryRuleExpression"
    // InternalDsl.g:6148:1: entryRuleExpression returns [EObject current=null] : iv_ruleExpression= ruleExpression EOF ;
    public final EObject entryRuleExpression() throws RecognitionException {
        EObject current = null;

        EObject iv_ruleExpression = null;


        try {
            // InternalDsl.g:6148:51: (iv_ruleExpression= ruleExpression EOF )
            // InternalDsl.g:6149:2: iv_ruleExpression= ruleExpression EOF
            {
             newCompositeNode(grammarAccess.getExpressionRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleExpression=ruleExpression();

            state._fsp--;

             current =iv_ruleExpression; 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleExpression"


    // $ANTLR start "ruleExpression"
    // InternalDsl.g:6155:1: ruleExpression returns [EObject current=null] : (otherlv_0= 'if' ( (lv_lhs_1_0= RULE_STRING ) ) ( (lv_operator_2_0= ruleOperator ) ) ( (lv_rhs_3_0= RULE_STRING ) ) ) ;
    public final EObject ruleExpression() throws RecognitionException {
        EObject current = null;

        Token otherlv_0=null;
        Token lv_lhs_1_0=null;
        Token lv_rhs_3_0=null;
        AntlrDatatypeRuleToken lv_operator_2_0 = null;



        	enterRule();

        try {
            // InternalDsl.g:6161:2: ( (otherlv_0= 'if' ( (lv_lhs_1_0= RULE_STRING ) ) ( (lv_operator_2_0= ruleOperator ) ) ( (lv_rhs_3_0= RULE_STRING ) ) ) )
            // InternalDsl.g:6162:2: (otherlv_0= 'if' ( (lv_lhs_1_0= RULE_STRING ) ) ( (lv_operator_2_0= ruleOperator ) ) ( (lv_rhs_3_0= RULE_STRING ) ) )
            {
            // InternalDsl.g:6162:2: (otherlv_0= 'if' ( (lv_lhs_1_0= RULE_STRING ) ) ( (lv_operator_2_0= ruleOperator ) ) ( (lv_rhs_3_0= RULE_STRING ) ) )
            // InternalDsl.g:6163:3: otherlv_0= 'if' ( (lv_lhs_1_0= RULE_STRING ) ) ( (lv_operator_2_0= ruleOperator ) ) ( (lv_rhs_3_0= RULE_STRING ) )
            {
            otherlv_0=(Token)match(input,115,FOLLOW_3); 

            			newLeafNode(otherlv_0, grammarAccess.getExpressionAccess().getIfKeyword_0());
            		
            // InternalDsl.g:6167:3: ( (lv_lhs_1_0= RULE_STRING ) )
            // InternalDsl.g:6168:4: (lv_lhs_1_0= RULE_STRING )
            {
            // InternalDsl.g:6168:4: (lv_lhs_1_0= RULE_STRING )
            // InternalDsl.g:6169:5: lv_lhs_1_0= RULE_STRING
            {
            lv_lhs_1_0=(Token)match(input,RULE_STRING,FOLLOW_79); 

            					newLeafNode(lv_lhs_1_0, grammarAccess.getExpressionAccess().getLhsSTRINGTerminalRuleCall_1_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getExpressionRule());
            					}
            					setWithLastConsumed(
            						current,
            						"lhs",
            						lv_lhs_1_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }

            // InternalDsl.g:6185:3: ( (lv_operator_2_0= ruleOperator ) )
            // InternalDsl.g:6186:4: (lv_operator_2_0= ruleOperator )
            {
            // InternalDsl.g:6186:4: (lv_operator_2_0= ruleOperator )
            // InternalDsl.g:6187:5: lv_operator_2_0= ruleOperator
            {

            					newCompositeNode(grammarAccess.getExpressionAccess().getOperatorOperatorParserRuleCall_2_0());
            				
            pushFollow(FOLLOW_3);
            lv_operator_2_0=ruleOperator();

            state._fsp--;


            					if (current==null) {
            						current = createModelElementForParent(grammarAccess.getExpressionRule());
            					}
            					set(
            						current,
            						"operator",
            						lv_operator_2_0,
            						"in.handyman.Dsl.Operator");
            					afterParserOrEnumRuleCall();
            				

            }


            }

            // InternalDsl.g:6204:3: ( (lv_rhs_3_0= RULE_STRING ) )
            // InternalDsl.g:6205:4: (lv_rhs_3_0= RULE_STRING )
            {
            // InternalDsl.g:6205:4: (lv_rhs_3_0= RULE_STRING )
            // InternalDsl.g:6206:5: lv_rhs_3_0= RULE_STRING
            {
            lv_rhs_3_0=(Token)match(input,RULE_STRING,FOLLOW_2); 

            					newLeafNode(lv_rhs_3_0, grammarAccess.getExpressionAccess().getRhsSTRINGTerminalRuleCall_3_0());
            				

            					if (current==null) {
            						current = createModelElement(grammarAccess.getExpressionRule());
            					}
            					setWithLastConsumed(
            						current,
            						"rhs",
            						lv_rhs_3_0,
            						"org.eclipse.xtext.common.Terminals.STRING");
            				

            }


            }


            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleExpression"


    // $ANTLR start "entryRuleOperator"
    // InternalDsl.g:6226:1: entryRuleOperator returns [String current=null] : iv_ruleOperator= ruleOperator EOF ;
    public final String entryRuleOperator() throws RecognitionException {
        String current = null;

        AntlrDatatypeRuleToken iv_ruleOperator = null;


        try {
            // InternalDsl.g:6226:48: (iv_ruleOperator= ruleOperator EOF )
            // InternalDsl.g:6227:2: iv_ruleOperator= ruleOperator EOF
            {
             newCompositeNode(grammarAccess.getOperatorRule()); 
            pushFollow(FOLLOW_1);
            iv_ruleOperator=ruleOperator();

            state._fsp--;

             current =iv_ruleOperator.getText(); 
            match(input,EOF,FOLLOW_2); 

            }

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "entryRuleOperator"


    // $ANTLR start "ruleOperator"
    // InternalDsl.g:6233:1: ruleOperator returns [AntlrDatatypeRuleToken current=new AntlrDatatypeRuleToken()] : (kw= '<' | kw= '>' | kw= '==' | kw= 'contains' ) ;
    public final AntlrDatatypeRuleToken ruleOperator() throws RecognitionException {
        AntlrDatatypeRuleToken current = new AntlrDatatypeRuleToken();

        Token kw=null;


        	enterRule();

        try {
            // InternalDsl.g:6239:2: ( (kw= '<' | kw= '>' | kw= '==' | kw= 'contains' ) )
            // InternalDsl.g:6240:2: (kw= '<' | kw= '>' | kw= '==' | kw= 'contains' )
            {
            // InternalDsl.g:6240:2: (kw= '<' | kw= '>' | kw= '==' | kw= 'contains' )
            int alt6=4;
            switch ( input.LA(1) ) {
            case 116:
                {
                alt6=1;
                }
                break;
            case 117:
                {
                alt6=2;
                }
                break;
            case 118:
                {
                alt6=3;
                }
                break;
            case 119:
                {
                alt6=4;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }

            switch (alt6) {
                case 1 :
                    // InternalDsl.g:6241:3: kw= '<'
                    {
                    kw=(Token)match(input,116,FOLLOW_2); 

                    			current.merge(kw);
                    			newLeafNode(kw, grammarAccess.getOperatorAccess().getLessThanSignKeyword_0());
                    		

                    }
                    break;
                case 2 :
                    // InternalDsl.g:6247:3: kw= '>'
                    {
                    kw=(Token)match(input,117,FOLLOW_2); 

                    			current.merge(kw);
                    			newLeafNode(kw, grammarAccess.getOperatorAccess().getGreaterThanSignKeyword_1());
                    		

                    }
                    break;
                case 3 :
                    // InternalDsl.g:6253:3: kw= '=='
                    {
                    kw=(Token)match(input,118,FOLLOW_2); 

                    			current.merge(kw);
                    			newLeafNode(kw, grammarAccess.getOperatorAccess().getEqualsSignEqualsSignKeyword_2());
                    		

                    }
                    break;
                case 4 :
                    // InternalDsl.g:6259:3: kw= 'contains'
                    {
                    kw=(Token)match(input,119,FOLLOW_2); 

                    			current.merge(kw);
                    			newLeafNode(kw, grammarAccess.getOperatorAccess().getContainsKeyword_3());
                    		

                    }
                    break;

            }


            }


            	leaveRule();

        }

            catch (RecognitionException re) {
                recover(input,re);
                appendSkippedTokens();
            }
        finally {
        }
        return current;
    }
    // $ANTLR end "ruleOperator"

    // Delegated rules


 

    public static final BitSet FOLLOW_1 = new BitSet(new long[]{0x0000000000000000L});
    public static final BitSet FOLLOW_2 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_3 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_4 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_5 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_6 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_7 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_8 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_9 = new BitSet(new long[]{0xF528A5A900022000L,0x00000185D1004017L});
    public static final BitSet FOLLOW_10 = new BitSet(new long[]{0x0000000000040000L});
    public static final BitSet FOLLOW_11 = new BitSet(new long[]{0x0000000000080000L});
    public static final BitSet FOLLOW_12 = new BitSet(new long[]{0x0000000000100000L});
    public static final BitSet FOLLOW_13 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_14 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_15 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_16 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_17 = new BitSet(new long[]{0x0000000002000000L});
    public static final BitSet FOLLOW_18 = new BitSet(new long[]{0x0000000004000000L});
    public static final BitSet FOLLOW_19 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_20 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_21 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_22 = new BitSet(new long[]{0x0000000040000000L});
    public static final BitSet FOLLOW_23 = new BitSet(new long[]{0x0000000080000000L});
    public static final BitSet FOLLOW_24 = new BitSet(new long[]{0x0000000000000000L,0x0008000000000000L});
    public static final BitSet FOLLOW_25 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_26 = new BitSet(new long[]{0x0000000400000000L});
    public static final BitSet FOLLOW_27 = new BitSet(new long[]{0x0000001000000000L});
    public static final BitSet FOLLOW_28 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_29 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_30 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_31 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_32 = new BitSet(new long[]{0x0000400000000000L});
    public static final BitSet FOLLOW_33 = new BitSet(new long[]{0x0001000000000000L});
    public static final BitSet FOLLOW_34 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_35 = new BitSet(new long[]{0x0004000000000000L});
    public static final BitSet FOLLOW_36 = new BitSet(new long[]{0x0010000000000000L});
    public static final BitSet FOLLOW_37 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_38 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_39 = new BitSet(new long[]{0x0200000000000000L});
    public static final BitSet FOLLOW_40 = new BitSet(new long[]{0x0800000000000000L});
    public static final BitSet FOLLOW_41 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000008L});
    public static final BitSet FOLLOW_42 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_43 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_44 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000080L});
    public static final BitSet FOLLOW_45 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_46 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000200L});
    public static final BitSet FOLLOW_47 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_48 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_49 = new BitSet(new long[]{0x0000000000000000L,0x0000000000001000L});
    public static final BitSet FOLLOW_50 = new BitSet(new long[]{0x0000000000000000L,0x0000000000002000L});
    public static final BitSet FOLLOW_51 = new BitSet(new long[]{0x0000000000000000L,0x0000000000008000L});
    public static final BitSet FOLLOW_52 = new BitSet(new long[]{0x0000000000000000L,0x0000000000010000L});
    public static final BitSet FOLLOW_53 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020000L});
    public static final BitSet FOLLOW_54 = new BitSet(new long[]{0x0000000000000000L,0x0000000000040000L});
    public static final BitSet FOLLOW_55 = new BitSet(new long[]{0x0000000000000000L,0x0000000000080000L});
    public static final BitSet FOLLOW_56 = new BitSet(new long[]{0x0000000000000000L,0x0000000000100000L});
    public static final BitSet FOLLOW_57 = new BitSet(new long[]{0x0000000000000000L,0x0000000000200000L});
    public static final BitSet FOLLOW_58 = new BitSet(new long[]{0x0000000000000000L,0x0000000000400000L});
    public static final BitSet FOLLOW_59 = new BitSet(new long[]{0x0000000000000000L,0x0000000000800000L});
    public static final BitSet FOLLOW_60 = new BitSet(new long[]{0x0000000000000000L,0x0000000002000000L});
    public static final BitSet FOLLOW_61 = new BitSet(new long[]{0x0000000000000000L,0x0000000004000000L});
    public static final BitSet FOLLOW_62 = new BitSet(new long[]{0x0000000000000000L,0x0000000008000000L});
    public static final BitSet FOLLOW_63 = new BitSet(new long[]{0x0000000000000000L,0x0000000020000000L});
    public static final BitSet FOLLOW_64 = new BitSet(new long[]{0x0000000000000000L,0x0000000200000000L});
    public static final BitSet FOLLOW_65 = new BitSet(new long[]{0x0000000000000000L,0x0000000800000000L});
    public static final BitSet FOLLOW_66 = new BitSet(new long[]{0x0000000000000000L,0x0000001000000000L});
    public static final BitSet FOLLOW_67 = new BitSet(new long[]{0x0000000000000000L,0x0000002000000000L});
    public static final BitSet FOLLOW_68 = new BitSet(new long[]{0x0000000000000000L,0x0000004000000000L});
    public static final BitSet FOLLOW_69 = new BitSet(new long[]{0x0000000000000000L,0x0000020000000000L});
    public static final BitSet FOLLOW_70 = new BitSet(new long[]{0x0000000000000000L,0x0000040000000000L});
    public static final BitSet FOLLOW_71 = new BitSet(new long[]{0x0000000000000000L,0x0000080000000000L});
    public static final BitSet FOLLOW_72 = new BitSet(new long[]{0x0000000000000000L,0x0000100000000000L});
    public static final BitSet FOLLOW_73 = new BitSet(new long[]{0x0000000000000000L,0x0000200000000000L});
    public static final BitSet FOLLOW_74 = new BitSet(new long[]{0x0000000000000000L,0x0000400000000000L});
    public static final BitSet FOLLOW_75 = new BitSet(new long[]{0x0000000000000000L,0x0000800000000000L});
    public static final BitSet FOLLOW_76 = new BitSet(new long[]{0x0000000000002000L,0x0004000000000000L});
    public static final BitSet FOLLOW_77 = new BitSet(new long[]{0x0000000000000000L,0x0001000000000000L});
    public static final BitSet FOLLOW_78 = new BitSet(new long[]{0x0000000000000000L,0x0002000000000000L});
    public static final BitSet FOLLOW_79 = new BitSet(new long[]{0x0000000000000000L,0x00F0000000000000L});

}