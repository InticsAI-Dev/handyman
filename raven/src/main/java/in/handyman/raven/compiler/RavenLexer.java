// Generated from Raven.g4 by ANTLR 4.9.2

package in.handyman.raven.compiler;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RuntimeMetaData;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class RavenLexer extends Lexer {
    public static final int
            T__0 = 1, T__1 = 2, T__2 = 3, T__3 = 4, T__4 = 5, T__5 = 6, T__6 = 7, T__7 = 8, T__8 = 9,
            T__9 = 10, T__10 = 11, T__11 = 12, T__12 = 13, T__13 = 14, T__14 = 15, T__15 = 16, T__16 = 17,
            T__17 = 18, T__18 = 19, T__19 = 20, T__20 = 21, T__21 = 22, T__22 = 23, T__23 = 24,
            T__24 = 25, T__25 = 26, T__26 = 27, T__27 = 28, T__28 = 29, T__29 = 30, T__30 = 31,
            T__31 = 32, T__32 = 33, T__33 = 34, T__34 = 35, T__35 = 36, T__36 = 37, T__37 = 38,
            T__38 = 39, T__39 = 40, T__40 = 41, T__41 = 42, T__42 = 43, T__43 = 44, T__44 = 45,
            T__45 = 46, T__46 = 47, NON_ZERO_DIGIT = 48, STRING = 49, CRLF = 50, Operator = 51,
            WS = 52, COMMENT = 53, LINE_COMMENT = 54, NUMBER = 55;
    public static final String[] ruleNames = makeRuleNames();
    /**
     * @deprecated Use {@link #VOCABULARY} instead.
     */
    @Deprecated
    public static final String[] tokenNames;
    public static final String _serializedATN =
            "\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\29\u0262\b\1\4\2\t" +
                    "\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13" +
                    "\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22" +
                    "\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31" +
                    "\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!" +
                    "\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4" +
                    ",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t" +
                    "\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t=" +
                    "\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\3\2\3\2\3\2\3\2\3\2\3\2\3\2" +
                    "\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3\6\3" +
                    "\7\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t" +
                    "\3\n\3\n\3\n\3\n\3\n\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3" +
                    "\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3" +
                    "\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3" +
                    "\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\20\3\20\3" +
                    "\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3" +
                    "\20\3\20\3\20\3\20\3\20\3\20\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3" +
                    "\21\3\21\3\22\3\22\3\22\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\23\3\24\3" +
                    "\24\3\24\3\24\3\25\3\25\3\25\3\25\3\25\3\26\3\26\3\26\3\26\3\26\3\26\3" +
                    "\26\3\26\3\26\3\27\3\27\3\27\3\27\3\27\3\27\3\30\3\30\3\30\3\30\3\30\3" +
                    "\30\3\30\3\30\3\30\3\30\3\30\3\30\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3" +
                    "\31\3\31\3\31\3\31\3\31\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3" +
                    "\32\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\34\3\34\3\34\3" +
                    "\34\3\34\3\34\3\34\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\36\3\36\3\36\3" +
                    "\36\3\36\3\36\3\36\3\36\3\36\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3\37\3" +
                    " \3 \3 \3 \3 \3 \3 \3 \3!\3!\3!\3!\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3#\3#\3" +
                    "#\3#\3#\3#\3#\3#\3#\3#\3#\3#\3#\3$\3$\3$\3$\3$\3$\3$\3$\3$\3$\3$\3$\3" +
                    "%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3%\3&\3&\3&\3&\3&\3&\3&\3\'\3" +
                    "\'\3\'\3\'\3\'\3\'\3\'\3\'\3(\3(\3(\3)\3)\3)\3*\3*\3+\3+\3,\3,\3-\3-\3" +
                    ".\3.\3.\3.\3.\3/\3/\3/\3/\3/\3/\3\60\3\60\3\60\3\60\3\60\3\61\3\61\3\62" +
                    "\3\62\3\63\3\63\3\63\3\64\3\64\3\65\6\65\u01ee\n\65\r\65\16\65\u01ef\3" +
                    "\66\3\66\3\67\6\67\u01f5\n\67\r\67\16\67\u01f6\38\38\58\u01fb\n8\38\3" +
                    "8\39\59\u0200\n9\39\39\59\u0204\n9\39\39\3:\3:\3:\3:\3:\3:\3:\3:\3:\3" +
                    ":\3:\5:\u0213\n:\3;\6;\u0216\n;\r;\16;\u0217\3;\3;\3<\3<\3<\3<\7<\u0220" +
                    "\n<\f<\16<\u0223\13<\3<\3<\3<\3<\3<\3=\3=\3=\3=\7=\u022e\n=\f=\16=\u0231" +
                    "\13=\3=\3=\3>\3>\3>\5>\u0238\n>\3?\3?\3?\3?\3?\3?\3@\3@\3A\3A\3B\5B\u0245" +
                    "\nB\3B\3B\3B\6B\u024a\nB\rB\16B\u024b\5B\u024e\nB\3B\5B\u0251\nB\3C\3" +
                    "C\3C\7C\u0256\nC\fC\16C\u0259\13C\5C\u025b\nC\3D\3D\5D\u025f\nD\3D\3D" +
                    "\3\u0221\2E\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33" +
                    "\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67" +
                    "\359\36;\37= ?!A\"C#E$G%I&K\'M(O)Q*S+U,W-Y.[/]\60_\61a\2c\2e\2g\2i\2k" +
                    "\2m\62o\63q\64s\65u\66w\67y8{\2}\2\177\2\u0081\2\u00839\u0085\2\u0087" +
                    "\2\3\2\16\3\2\62;\3\2\63;\4\2C\\c|\4\2$$^^\4\2>>@@\5\2\13\f\16\17\"\"" +
                    "\4\2\f\f\17\17\n\2$$\61\61^^ddhhppttvv\5\2\62;CHch\5\2\2!$$^^\4\2GGgg" +
                    "\4\2--//\2\u0267\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13" +
                    "\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2" +
                    "\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2" +
                    "!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3" +
                    "\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2" +
                    "\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E" +
                    "\3\2\2\2\2G\3\2\2\2\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\2Q\3\2" +
                    "\2\2\2S\3\2\2\2\2U\3\2\2\2\2W\3\2\2\2\2Y\3\2\2\2\2[\3\2\2\2\2]\3\2\2\2" +
                    "\2_\3\2\2\2\2m\3\2\2\2\2o\3\2\2\2\2q\3\2\2\2\2s\3\2\2\2\2u\3\2\2\2\2w" +
                    "\3\2\2\2\2y\3\2\2\2\2\u0083\3\2\2\2\3\u0089\3\2\2\2\5\u0091\3\2\2\2\7" +
                    "\u0093\3\2\2\2\t\u0095\3\2\2\2\13\u0099\3\2\2\2\r\u00a1\3\2\2\2\17\u00a7" +
                    "\3\2\2\2\21\u00b0\3\2\2\2\23\u00b3\3\2\2\2\25\u00b8\3\2\2\2\27\u00bb\3" +
                    "\2\2\2\31\u00c1\3\2\2\2\33\u00ce\3\2\2\2\35\u00d7\3\2\2\2\37\u00ed\3\2" +
                    "\2\2!\u0103\3\2\2\2#\u010d\3\2\2\2%\u0110\3\2\2\2\'\u0118\3\2\2\2)\u011c" +
                    "\3\2\2\2+\u0121\3\2\2\2-\u012a\3\2\2\2/\u0130\3\2\2\2\61\u013c\3\2\2\2" +
                    "\63\u0148\3\2\2\2\65\u0152\3\2\2\2\67\u015c\3\2\2\29\u0163\3\2\2\2;\u016a" +
                    "\3\2\2\2=\u0173\3\2\2\2?\u017b\3\2\2\2A\u0183\3\2\2\2C\u0187\3\2\2\2E" +
                    "\u018e\3\2\2\2G\u019b\3\2\2\2I\u01a7\3\2\2\2K\u01b6\3\2\2\2M\u01bd\3\2" +
                    "\2\2O\u01c5\3\2\2\2Q\u01c8\3\2\2\2S\u01cb\3\2\2\2U\u01cd\3\2\2\2W\u01cf" +
                    "\3\2\2\2Y\u01d1\3\2\2\2[\u01d3\3\2\2\2]\u01d8\3\2\2\2_\u01de\3\2\2\2a" +
                    "\u01e3\3\2\2\2c\u01e5\3\2\2\2e\u01e7\3\2\2\2g\u01ea\3\2\2\2i\u01ed\3\2" +
                    "\2\2k\u01f1\3\2\2\2m\u01f4\3\2\2\2o\u01f8\3\2\2\2q\u0203\3\2\2\2s\u0212" +
                    "\3\2\2\2u\u0215\3\2\2\2w\u021b\3\2\2\2y\u0229\3\2\2\2{\u0234\3\2\2\2}" +
                    "\u0239\3\2\2\2\177\u023f\3\2\2\2\u0081\u0241\3\2\2\2\u0083\u0244\3\2\2" +
                    "\2\u0085\u025a\3\2\2\2\u0087\u025c\3\2\2\2\u0089\u008a\7r\2\2\u008a\u008b" +
                    "\7t\2\2\u008b\u008c\7q\2\2\u008c\u008d\7e\2\2\u008d\u008e\7g\2\2\u008e" +
                    "\u008f\7u\2\2\u008f\u0090\7u\2\2\u0090\4\3\2\2\2\u0091\u0092\7}\2\2\u0092" +
                    "\6\3\2\2\2\u0093\u0094\7\177\2\2\u0094\b\3\2\2\2\u0095\u0096\7v\2\2\u0096" +
                    "\u0097\7t\2\2\u0097\u0098\7{\2\2\u0098\n\3\2\2\2\u0099\u009a\7h\2\2\u009a" +
                    "\u009b\7k\2\2\u009b\u009c\7p\2\2\u009c\u009d\7c\2\2\u009d\u009e\7n\2\2" +
                    "\u009e\u009f\7n\2\2\u009f\u00a0\7{\2\2\u00a0\f\3\2\2\2\u00a1\u00a2\7e" +
                    "\2\2\u00a2\u00a3\7c\2\2\u00a3\u00a4\7v\2\2\u00a4\u00a5\7e\2\2\u00a5\u00a6" +
                    "\7j\2\2\u00a6\16\3\2\2\2\u00a7\u00a8\7e\2\2\u00a8\u00a9\7q\2\2\u00a9\u00aa" +
                    "\7r\2\2\u00aa\u00ab\7{\2\2\u00ab\u00ac\7f\2\2\u00ac\u00ad\7c\2\2\u00ad" +
                    "\u00ae\7v\2\2\u00ae\u00af\7c\2\2\u00af\20\3\2\2\2\u00b0\u00b1\7c\2\2\u00b1" +
                    "\u00b2\7u\2\2\u00b2\22\3\2\2\2\u00b3\u00b4\7h\2\2\u00b4\u00b5\7t\2\2\u00b5" +
                    "\u00b6\7q\2\2\u00b6\u00b7\7o\2\2\u00b7\24\3\2\2\2\u00b8\u00b9\7v\2\2\u00b9" +
                    "\u00ba\7q\2\2\u00ba\26\3\2\2\2\u00bb\u00bc\7w\2\2\u00bc\u00bd\7u\2\2\u00bd" +
                    "\u00be\7k\2\2\u00be\u00bf\7p\2\2\u00bf\u00c0\7i\2\2\u00c0\30\3\2\2\2\u00c1" +
                    "\u00c2\7q\2\2\u00c2\u00c3\7p\2\2\u00c3\u00c4\7/\2\2\u00c4\u00c5\7e\2\2" +
                    "\u00c5\u00c6\7q\2\2\u00c6\u00c7\7p\2\2\u00c7\u00c8\7f\2\2\u00c8\u00c9" +
                    "\7k\2\2\u00c9\u00ca\7v\2\2\u00ca\u00cb\7k\2\2\u00cb\u00cc\7q\2\2\u00cc" +
                    "\u00cd\7p\2\2\u00cd\32\3\2\2\2\u00ce\u00cf\7h\2\2\u00cf\u00d0\7k\2\2\u00d0" +
                    "\u00d1\7g\2\2\u00d1\u00d2\7n\2\2\u00d2\u00d3\7f\2\2\u00d3\u00d4\7k\2\2" +
                    "\u00d4\u00d5\7p\2\2\u00d5\u00d6\7i\2\2\u00d6\34\3\2\2\2\u00d7\u00d8\7" +
                    "y\2\2\u00d8\u00d9\7k\2\2\u00d9\u00da\7v\2\2\u00da\u00db\7j\2\2\u00db\u00dc" +
                    "\7/\2\2\u00dc\u00dd\7h\2\2\u00dd\u00de\7g\2\2\u00de\u00df\7v\2\2\u00df" +
                    "\u00e0\7e\2\2\u00e0\u00e1\7j\2\2\u00e1\u00e2\7/\2\2\u00e2\u00e3\7d\2\2" +
                    "\u00e3\u00e4\7c\2\2\u00e4\u00e5\7v\2\2\u00e5\u00e6\7e\2\2\u00e6\u00e7" +
                    "\7j\2\2\u00e7\u00e8\7/\2\2\u00e8\u00e9\7u\2\2\u00e9\u00ea\7k\2\2\u00ea" +
                    "\u00eb\7|\2\2\u00eb\u00ec\7g\2\2\u00ec\36\3\2\2\2\u00ed\u00ee\7y\2\2\u00ee" +
                    "\u00ef\7k\2\2\u00ef\u00f0\7v\2\2\u00f0\u00f1\7j\2\2\u00f1\u00f2\7/\2\2" +
                    "\u00f2\u00f3\7y\2\2\u00f3\u00f4\7t\2\2\u00f4\u00f5\7k\2\2\u00f5\u00f6" +
                    "\7v\2\2\u00f6\u00f7\7g\2\2\u00f7\u00f8\7/\2\2\u00f8\u00f9\7d\2\2\u00f9" +
                    "\u00fa\7c\2\2\u00fa\u00fb\7v\2\2\u00fb\u00fc\7e\2\2\u00fc\u00fd\7j\2\2" +
                    "\u00fd\u00fe\7/\2\2\u00fe\u00ff\7u\2\2\u00ff\u0100\7k\2\2\u0100\u0101" +
                    "\7|\2\2\u0101\u0102\7g\2\2\u0102 \3\2\2\2\u0103\u0104\7v\2\2\u0104\u0105" +
                    "\7t\2\2\u0105\u0106\7c\2\2\u0106\u0107\7p\2\2\u0107\u0108\7u\2\2\u0108" +
                    "\u0109\7h\2\2\u0109\u010a\7q\2\2\u010a\u010b\7t\2\2\u010b\u010c\7o\2\2" +
                    "\u010c\"\3\2\2\2\u010d\u010e\7q\2\2\u010e\u010f\7p\2\2\u010f$\3\2\2\2" +
                    "\u0110\u0111\7n\2\2\u0111\u0112\7q\2\2\u0112\u0113\7c\2\2\u0113\u0114" +
                    "\7f\2\2\u0114\u0115\7e\2\2\u0115\u0116\7u\2\2\u0116\u0117\7x\2\2\u0117" +
                    "&\3\2\2\2\u0118\u0119\7r\2\2\u0119\u011a\7k\2\2\u011a\u011b\7f\2\2\u011b" +
                    "(\3\2\2\2\u011c\u011d\7y\2\2\u011d\u011e\7k\2\2\u011e\u011f\7v\2\2\u011f" +
                    "\u0120\7j\2\2\u0120*\3\2\2\2\u0121\u0122\7d\2\2\u0122\u0123\7{\2\2\u0123" +
                    "\u0124\7/\2\2\u0124\u0125\7d\2\2\u0125\u0126\7c\2\2\u0126\u0127\7v\2\2" +
                    "\u0127\u0128\7e\2\2\u0128\u0129\7j\2\2\u0129,\3\2\2\2\u012a\u012b\7c\2" +
                    "\2\u012b\u012c\7d\2\2\u012c\u012d\7q\2\2\u012d\u012e\7t\2\2\u012e\u012f" +
                    "\7v\2\2\u012f.\3\2\2\2\u0130\u0131\7e\2\2\u0131\u0132\7c\2\2\u0132\u0133" +
                    "\7n\2\2\u0133\u0134\7n\2\2\u0134\u0135\7r\2\2\u0135\u0136\7t\2\2\u0136" +
                    "\u0137\7q\2\2\u0137\u0138\7e\2\2\u0138\u0139\7g\2\2\u0139\u013a\7u\2\2" +
                    "\u013a\u013b\7u\2\2\u013b\60\3\2\2\2\u013c\u013d\7y\2\2\u013d\u013e\7" +
                    "k\2\2\u013e\u013f\7v\2\2\u013f\u0140\7j\2\2\u0140\u0141\7/\2\2\u0141\u0142" +
                    "\7v\2\2\u0142\u0143\7c\2\2\u0143\u0144\7t\2\2\u0144\u0145\7i\2\2\u0145" +
                    "\u0146\7g\2\2\u0146\u0147\7v\2\2\u0147\62\3\2\2\2\u0148\u0149\7h\2\2\u0149" +
                    "\u014a\7t\2\2\u014a\u014b\7q\2\2\u014b\u014c\7o\2\2\u014c\u014d\7/\2\2" +
                    "\u014d\u014e\7h\2\2\u014e\u014f\7k\2\2\u014f\u0150\7n\2\2\u0150\u0151" +
                    "\7g\2\2\u0151\64\3\2\2\2\u0152\u0153\7h\2\2\u0153\u0154\7q\2\2\u0154\u0155" +
                    "\7t\2\2\u0155\u0156\7/\2\2\u0156\u0157\7g\2\2\u0157\u0158\7x\2\2\u0158" +
                    "\u0159\7g\2\2\u0159\u015a\7t\2\2\u015a\u015b\7{\2\2\u015b\66\3\2\2\2\u015c" +
                    "\u015d\7c\2\2\u015d\u015e\7u\2\2\u015e\u015f\7u\2\2\u015f\u0160\7k\2\2" +
                    "\u0160\u0161\7i\2\2\u0161\u0162\7p\2\2\u01628\3\2\2\2\u0163\u0164\7u\2" +
                    "\2\u0164\u0165\7q\2\2\u0165\u0166\7w\2\2\u0166\u0167\7t\2\2\u0167\u0168" +
                    "\7e\2\2\u0168\u0169\7g\2\2\u0169:\3\2\2\2\u016a\u016b\7f\2\2\u016b\u016c" +
                    "\7t\2\2\u016c\u016d\7q\2\2\u016d\u016e\7r\2\2\u016e\u016f\7h\2\2\u016f" +
                    "\u0170\7k\2\2\u0170\u0171\7n\2\2\u0171\u0172\7g\2\2\u0172<\3\2\2\2\u0173" +
                    "\u0174\7k\2\2\u0174\u0175\7p\2\2\u0175\u0176\7/\2\2\u0176\u0177\7r\2\2" +
                    "\u0177\u0178\7c\2\2\u0178\u0179\7v\2\2\u0179\u017a\7j\2\2\u017a>\3\2\2" +
                    "\2\u017b\u017c\7t\2\2\u017c\u017d\7g\2\2\u017d\u017e\7u\2\2\u017e\u017f" +
                    "\7v\2\2\u017f\u0180\7c\2\2\u0180\u0181\7r\2\2\u0181\u0182\7k\2\2\u0182" +
                    "@\3\2\2\2\u0183\u0184\7w\2\2\u0184\u0185\7t\2\2\u0185\u0186\7n\2\2\u0186" +
                    "B\3\2\2\2\u0187\u0188\7o\2\2\u0188\u0189\7g\2\2\u0189\u018a\7v\2\2\u018a" +
                    "\u018b\7j\2\2\u018b\u018c\7q\2\2\u018c\u018d\7f\2\2\u018dD\3\2\2\2\u018e" +
                    "\u018f\7y\2\2\u018f\u0190\7k\2\2\u0190\u0191\7v\2\2\u0191\u0192\7j\2\2" +
                    "\u0192\u0193\7\"\2\2\u0193\u0194\7j\2\2\u0194\u0195\7g\2\2\u0195\u0196" +
                    "\7c\2\2\u0196\u0197\7f\2\2\u0197\u0198\7g\2\2\u0198\u0199\7t\2\2\u0199" +
                    "\u019a\7u\2\2\u019aF\3\2\2\2\u019b\u019c\7y\2\2\u019c\u019d\7k\2\2\u019d" +
                    "\u019e\7v\2\2\u019e\u019f\7j\2\2\u019f\u01a0\7\"\2\2\u01a0\u01a1\7r\2" +
                    "\2\u01a1\u01a2\7c\2\2\u01a2\u01a3\7t\2\2\u01a3\u01a4\7c\2\2\u01a4\u01a5" +
                    "\7o\2\2\u01a5\u01a6\7u\2\2\u01a6H\3\2\2\2\u01a7\u01a8\7y\2\2\u01a8\u01a9" +
                    "\7k\2\2\u01a9\u01aa\7v\2\2\u01aa\u01ab\7j\2\2\u01ab\u01ac\7\"\2\2\u01ac" +
                    "\u01ad\7d\2\2\u01ad\u01ae\7q\2\2\u01ae\u01af\7f\2\2\u01af\u01b0\7{\2\2" +
                    "\u01b0\u01b1\7\"\2\2\u01b1\u01b2\7v\2\2\u01b2\u01b3\7{\2\2\u01b3\u01b4" +
                    "\7r\2\2\u01b4\u01b5\7g\2\2\u01b5J\3\2\2\2\u01b6\u01b7\7}\2\2\u01b7\u01b8" +
                    "\7\"\2\2\u01b8\u01b9\7r\2\2\u01b9\u01ba\7c\2\2\u01ba\u01bb\7t\2\2\u01bb" +
                    "\u01bc\7v\2\2\u01bcL\3\2\2\2\u01bd\u01be\7v\2\2\u01be\u01bf\7{\2\2\u01bf" +
                    "\u01c0\7r\2\2\u01c0\u01c1\7g\2\2\u01c1\u01c2\7\"\2\2\u01c2\u01c3\7c\2" +
                    "\2\u01c3\u01c4\7u\2\2\u01c4N\3\2\2\2\u01c5\u01c6\7\"\2\2\u01c6\u01c7\7" +
                    "\177\2\2\u01c7P\3\2\2\2\u01c8\u01c9\7k\2\2\u01c9\u01ca\7h\2\2\u01caR\3" +
                    "\2\2\2\u01cb\u01cc\7.\2\2\u01ccT\3\2\2\2\u01cd\u01ce\7<\2\2\u01ceV\3\2" +
                    "\2\2\u01cf\u01d0\7]\2\2\u01d0X\3\2\2\2\u01d1\u01d2\7_\2\2\u01d2Z\3\2\2" +
                    "\2\u01d3\u01d4\7v\2\2\u01d4\u01d5\7t\2\2\u01d5\u01d6\7w\2\2\u01d6\u01d7" +
                    "\7g\2\2\u01d7\\\3\2\2\2\u01d8\u01d9\7h\2\2\u01d9\u01da\7c\2\2\u01da\u01db" +
                    "\7n\2\2\u01db\u01dc\7u\2\2\u01dc\u01dd\7g\2\2\u01dd^\3\2\2\2\u01de\u01df" +
                    "\7p\2\2\u01df\u01e0\7w\2\2\u01e0\u01e1\7n\2\2\u01e1\u01e2\7n\2\2\u01e2" +
                    "`\3\2\2\2\u01e3\u01e4\t\2\2\2\u01e4b\3\2\2\2\u01e5\u01e6\t\3\2\2\u01e6" +
                    "d\3\2\2\2\u01e7\u01e8\5a\61\2\u01e8\u01e9\5a\61\2\u01e9f\3\2\2\2\u01ea" +
                    "\u01eb\t\4\2\2\u01ebh\3\2\2\2\u01ec\u01ee\5k\66\2\u01ed\u01ec\3\2\2\2" +
                    "\u01ee\u01ef\3\2\2\2\u01ef\u01ed\3\2\2\2\u01ef\u01f0\3\2\2\2\u01f0j\3" +
                    "\2\2\2\u01f1\u01f2\n\5\2\2\u01f2l\3\2\2\2\u01f3\u01f5\5c\62\2\u01f4\u01f3" +
                    "\3\2\2\2\u01f5\u01f6\3\2\2\2\u01f6\u01f4\3\2\2\2\u01f6\u01f7\3\2\2\2\u01f7" +
                    "n\3\2\2\2\u01f8\u01fa\7$\2\2\u01f9\u01fb\5i\65\2\u01fa\u01f9\3\2\2\2\u01fa" +
                    "\u01fb\3\2\2\2\u01fb\u01fc\3\2\2\2\u01fc\u01fd\7$\2\2\u01fdp\3\2\2\2\u01fe" +
                    "\u0200\7\17\2\2\u01ff\u01fe\3\2\2\2\u01ff\u0200\3\2\2\2\u0200\u0201\3" +
                    "\2\2\2\u0201\u0204\7\f\2\2\u0202\u0204\7\17\2\2\u0203\u01ff\3\2\2\2\u0203" +
                    "\u0202\3\2\2\2\u0204\u0205\3\2\2\2\u0205\u0206\b9\2\2\u0206r\3\2\2\2\u0207" +
                    "\u0213\t\6\2\2\u0208\u0209\7?\2\2\u0209\u0213\7?\2\2\u020a\u020b\7e\2" +
                    "\2\u020b\u020c\7q\2\2\u020c\u020d\7p\2\2\u020d\u020e\7v\2\2\u020e\u020f" +
                    "\7c\2\2\u020f\u0210\7k\2\2\u0210\u0211\7p\2\2\u0211\u0213\7u\2\2\u0212" +
                    "\u0207\3\2\2\2\u0212\u0208\3\2\2\2\u0212\u020a\3\2\2\2\u0213t\3\2\2\2" +
                    "\u0214\u0216\t\7\2\2\u0215\u0214\3\2\2\2\u0216\u0217\3\2\2\2\u0217\u0215" +
                    "\3\2\2\2\u0217\u0218\3\2\2\2\u0218\u0219\3\2\2\2\u0219\u021a\b;\2\2\u021a" +
                    "v\3\2\2\2\u021b\u021c\7\61\2\2\u021c\u021d\7,\2\2\u021d\u0221\3\2\2\2" +
                    "\u021e\u0220\13\2\2\2\u021f\u021e\3\2\2\2\u0220\u0223\3\2\2\2\u0221\u0222" +
                    "\3\2\2\2\u0221\u021f\3\2\2\2\u0222\u0224\3\2\2\2\u0223\u0221\3\2\2\2\u0224" +
                    "\u0225\7,\2\2\u0225\u0226\7\61\2\2\u0226\u0227\3\2\2\2\u0227\u0228\b<" +
                    "\2\2\u0228x\3\2\2\2\u0229\u022a\7\61\2\2\u022a\u022b\7\61\2\2\u022b\u022f" +
                    "\3\2\2\2\u022c\u022e\n\b\2\2\u022d\u022c\3\2\2\2\u022e\u0231\3\2\2\2\u022f" +
                    "\u022d\3\2\2\2\u022f\u0230\3\2\2\2\u0230\u0232\3\2\2\2\u0231\u022f\3\2" +
                    "\2\2\u0232\u0233\b=\2\2\u0233z\3\2\2\2\u0234\u0237\7^\2\2\u0235\u0238" +
                    "\t\t\2\2\u0236\u0238\5}?\2\u0237\u0235\3\2\2\2\u0237\u0236\3\2\2\2\u0238" +
                    "|\3\2\2\2\u0239\u023a\7w\2\2\u023a\u023b\5\177@\2\u023b\u023c\5\177@\2" +
                    "\u023c\u023d\5\177@\2\u023d\u023e\5\177@\2\u023e~\3\2\2\2\u023f\u0240" +
                    "\t\n\2\2\u0240\u0080\3\2\2\2\u0241\u0242\n\13\2\2\u0242\u0082\3\2\2\2" +
                    "\u0243\u0245\7/\2\2\u0244\u0243\3\2\2\2\u0244\u0245\3\2\2\2\u0245\u0246" +
                    "\3\2\2\2\u0246\u024d\5\u0085C\2\u0247\u0249\7\60\2\2\u0248\u024a\t\2\2" +
                    "\2\u0249\u0248\3\2\2\2\u024a\u024b\3\2\2\2\u024b\u0249\3\2\2\2\u024b\u024c" +
                    "\3\2\2\2\u024c\u024e\3\2\2\2\u024d\u0247\3\2\2\2\u024d\u024e\3\2\2\2\u024e" +
                    "\u0250\3\2\2\2\u024f\u0251\5\u0087D\2\u0250\u024f\3\2\2\2\u0250\u0251" +
                    "\3\2\2\2\u0251\u0084\3\2\2\2\u0252\u025b\7\62\2\2\u0253\u0257\t\3\2\2" +
                    "\u0254\u0256\t\2\2\2\u0255\u0254\3\2\2\2\u0256\u0259\3\2\2\2\u0257\u0255" +
                    "\3\2\2\2\u0257\u0258\3\2\2\2\u0258\u025b\3\2\2\2\u0259\u0257\3\2\2\2\u025a" +
                    "\u0252\3\2\2\2\u025a\u0253\3\2\2\2\u025b\u0086\3\2\2\2\u025c\u025e\t\f" +
                    "\2\2\u025d\u025f\t\r\2\2\u025e\u025d\3\2\2\2\u025e\u025f\3\2\2\2\u025f" +
                    "\u0260\3\2\2\2\u0260\u0261\5\u0085C\2\u0261\u0088\3\2\2\2\24\2\u01ef\u01f6" +
                    "\u01fa\u01ff\u0203\u0212\u0217\u0221\u022f\u0237\u0244\u024b\u024d\u0250" +
                    "\u0257\u025a\u025e\3\2\3\2";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());
    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache =
            new PredictionContextCache();
    private static final String[] _LITERAL_NAMES = makeLiteralNames();
    private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
    public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);
    public static String[] channelNames = {
            "DEFAULT_TOKEN_CHANNEL", "HIDDEN"
    };
    public static String[] modeNames = {
            "DEFAULT_MODE"
    };

    static {
        RuntimeMetaData.checkVersion("4.9.2", RuntimeMetaData.VERSION);
    }

    static {
        tokenNames = new String[_SYMBOLIC_NAMES.length];
        for (int i = 0; i < tokenNames.length; i++) {
            tokenNames[i] = VOCABULARY.getLiteralName(i);
            if (tokenNames[i] == null) {
                tokenNames[i] = VOCABULARY.getSymbolicName(i);
            }

            if (tokenNames[i] == null) {
                tokenNames[i] = "<INVALID>";
            }
        }
    }

    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }

    public RavenLexer(CharStream input) {
        super(input);
        _interp = new LexerATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
    }

    private static String[] makeRuleNames() {
        return new String[]{
                "T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8",
                "T__9", "T__10", "T__11", "T__12", "T__13", "T__14", "T__15", "T__16",
                "T__17", "T__18", "T__19", "T__20", "T__21", "T__22", "T__23", "T__24",
                "T__25", "T__26", "T__27", "T__28", "T__29", "T__30", "T__31", "T__32",
                "T__33", "T__34", "T__35", "T__36", "T__37", "T__38", "T__39", "T__40",
                "T__41", "T__42", "T__43", "T__44", "T__45", "T__46", "DIGIT", "NON_Z_DIGIT",
                "TWODIGIT", "LETTER", "StringCharacters", "StringCharacter", "NON_ZERO_DIGIT",
                "STRING", "CRLF", "Operator", "WS", "COMMENT", "LINE_COMMENT", "ESC",
                "UNICODE", "HEX", "SAFECODEPOINT", "NUMBER", "INT", "EXP"
        };
    }

    private static String[] makeLiteralNames() {
        return new String[]{
                null, "'process'", "'{'", "'}'", "'try'", "'finally'", "'catch'", "'copydata'",
                "'as'", "'from'", "'to'", "'using'", "'on-condition'", "'fielding'",
                "'with-fetch-batch-size'", "'with-write-batch-size'", "'transform'",
                "'on'", "'loadcsv'", "'pid'", "'with'", "'by-batch'", "'abort'", "'callprocess'",
                "'with-target'", "'from-file'", "'for-every'", "'assign'", "'source'",
                "'dropfile'", "'in-path'", "'restapi'", "'url'", "'method'", "'with headers'",
                "'with params'", "'with body type'", "'{ part'", "'type as'", "' }'",
                "'if'", "','", "':'", "'['", "']'", "'true'", "'false'", "'null'"
        };
    }

    private static String[] makeSymbolicNames() {
        return new String[]{
                null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null,
                "NON_ZERO_DIGIT", "STRING", "CRLF", "Operator", "WS", "COMMENT", "LINE_COMMENT",
                "NUMBER"
        };
    }

    @Override
    @Deprecated
    public String[] getTokenNames() {
        return tokenNames;
    }

    @Override

    public Vocabulary getVocabulary() {
        return VOCABULARY;
    }

    @Override
    public String getGrammarFileName() {
        return "Raven.g4";
    }

    @Override
    public String[] getRuleNames() {
        return ruleNames;
    }

    @Override
    public String getSerializedATN() {
        return _serializedATN;
    }

    @Override
    public String[] getChannelNames() {
        return channelNames;
    }

    @Override
    public String[] getModeNames() {
        return modeNames;
    }

    @Override
    public ATN getATN() {
        return _ATN;
    }
}
