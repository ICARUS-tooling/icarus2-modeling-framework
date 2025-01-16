// Generated from de/ims/icarus2/query/api/iql/antlr/IQL_Test.g4 by ANTLR 4.13.2

package de.ims.icarus2.query.api.iql.antlr;
import org.antlr.v4.runtime.misc.Interval;	

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class IQL_TestParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		ADJACENT=1, ALL=2, AND=3, AS=4, ASC=5, BY=6, CONSECUTIVE=7, COUNT=8, DEFAULT=9, 
		DESC=10, DISJOINT=11, DISTINCT=12, DO=13, EDGES=14, END=15, EVEN=16, FALSE=17, 
		FILTER=18, FIND=19, FIRST=20, FOREACH=21, FROM=22, GROUP=23, HAVING=24, 
		HITS=25, IN=26, LABEL=27, LANE=28, LIMIT=29, NOT=30, NULL=31, ODD=32, 
		OMIT=33, ON=34, OPTIONAL=35, OR=36, ORDER=37, ORDERED=38, RANGE=39, REVERSE=40, 
		ROOTED=41, STEP=42, TRUE=43, UNORDERED=44, WITH=45, INT=46, LONG=47, FLOAT=48, 
		DOUBLE=49, BOOLEAN=50, STRING=51, SCOLON=52, COLON=53, DOT=54, LPAREN=55, 
		RPAREN=56, LBRACE=57, RBRACE=58, LBRACK=59, RBRACK=60, COMMA=61, UNDERSCORE=62, 
		EDGE_LEFT=63, EDGE_RIGHT=64, EDGE_BIDIRECTIONAL=65, EDGE_UNDIRECTED=66, 
		AT=67, DOLLAR=68, HASH=69, QMARK=70, EXMARK=71, ASSIGN=72, STAR=73, PLUS=74, 
		MINUS=75, TILDE=76, SLASH=77, PERCENT=78, AMP=79, DOUBLE_AMP=80, CARET=81, 
		PIPE=82, DOUBLE_PIPE=83, SHIFT_LEFT=84, SHIFT_RIGHT=85, LT=86, LT_EQ=87, 
		GT=88, GT_EQ=89, EQ=90, NOT_EQ=91, MATCHES=92, NOT_MATCHES=93, CONTAINS=94, 
		NOT_CONTAINS=95, DOUBLE_COLON=96, DOUBLE_DOT=97, PureDigits=98, Digits=99, 
		StringLiteral=100, Identifier=101, WS=102, SL_COMMENT=103, ErrorCharacter=104;
	public static final int
		RULE_versionDeclarationTest = 0, RULE_quantifierTest = 1, RULE_unsignedSimpleQuantifierTest = 2, 
		RULE_integerLiteralTest = 3, RULE_unsignedIntegerLiteralTest = 4, RULE_floatingPointLiteralTest = 5, 
		RULE_unsignedFloatingPointLiteralTest = 6, RULE_standaloneNodeStatement = 7, 
		RULE_standaloneStructuralConstraint = 8, RULE_standaloneSelectiveStatement = 9, 
		RULE_standaloneExpression = 10, RULE_payloadStatement = 11, RULE_bindingsList = 12, 
		RULE_binding = 13, RULE_selectionStatement = 14, RULE_laneStatementsList = 15, 
		RULE_laneStatement = 16, RULE_structuralConstraint = 17, RULE_hitsLimit = 18, 
		RULE_matchFlag = 19, RULE_nodeStatement = 20, RULE_nodeArrangement = 21, 
		RULE_node = 22, RULE_memberLabel = 23, RULE_positionMarker = 24, RULE_positionArgument = 25, 
		RULE_element = 26, RULE_edge = 27, RULE_leftEdgePart = 28, RULE_rightEdgePart = 29, 
		RULE_directedEdgeLeft = 30, RULE_directedEdgeRight = 31, RULE_undirectedEdge = 32, 
		RULE_groupStatement = 33, RULE_groupExpression = 34, RULE_resultStatement = 35, 
		RULE_orderExpressionList = 36, RULE_orderExpression = 37, RULE_constraint = 38, 
		RULE_expressionList = 39, RULE_expression = 40, RULE_primary = 41, RULE_listStatement = 42, 
		RULE_reference = 43, RULE_qualifiedIdentifier = 44, RULE_loopExpresseion = 45, 
		RULE_loopControl = 46, RULE_boundedRange = 47, RULE_counterList = 48, 
		RULE_counter = 49, RULE_type = 50, RULE_quantifier = 51, RULE_simpleQuantifier = 52, 
		RULE_not = 53, RULE_all = 54, RULE_and = 55, RULE_or = 56, RULE_variable = 57, 
		RULE_member = 58, RULE_versionDeclaration = 59, RULE_nullLiteral = 60, 
		RULE_floatingPointLiteral = 61, RULE_signedFloatingPointLiteral = 62, 
		RULE_unsignedFloatingPointLiteral = 63, RULE_booleanLiteral = 64, RULE_integerLiteral = 65, 
		RULE_signedIntegerLiteral = 66, RULE_unsignedIntegerLiteral = 67, RULE_sign = 68;
	private static String[] makeRuleNames() {
		return new String[] {
			"versionDeclarationTest", "quantifierTest", "unsignedSimpleQuantifierTest", 
			"integerLiteralTest", "unsignedIntegerLiteralTest", "floatingPointLiteralTest", 
			"unsignedFloatingPointLiteralTest", "standaloneNodeStatement", "standaloneStructuralConstraint", 
			"standaloneSelectiveStatement", "standaloneExpression", "payloadStatement", 
			"bindingsList", "binding", "selectionStatement", "laneStatementsList", 
			"laneStatement", "structuralConstraint", "hitsLimit", "matchFlag", "nodeStatement", 
			"nodeArrangement", "node", "memberLabel", "positionMarker", "positionArgument", 
			"element", "edge", "leftEdgePart", "rightEdgePart", "directedEdgeLeft", 
			"directedEdgeRight", "undirectedEdge", "groupStatement", "groupExpression", 
			"resultStatement", "orderExpressionList", "orderExpression", "constraint", 
			"expressionList", "expression", "primary", "listStatement", "reference", 
			"qualifiedIdentifier", "loopExpresseion", "loopControl", "boundedRange", 
			"counterList", "counter", "type", "quantifier", "simpleQuantifier", "not", 
			"all", "and", "or", "variable", "member", "versionDeclaration", "nullLiteral", 
			"floatingPointLiteral", "signedFloatingPointLiteral", "unsignedFloatingPointLiteral", 
			"booleanLiteral", "integerLiteral", "signedIntegerLiteral", "unsignedIntegerLiteral", 
			"sign"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, "'int'", 
			"'long'", "'float'", "'double'", "'boolean'", "'string'", "';'", "':'", 
			"'.'", "'('", "')'", "'{'", "'}'", "'['", "']'", "','", "'_'", "'<--'", 
			"'-->'", "'<->'", "'---'", "'@'", "'$'", "'#'", "'?'", "'!'", "'='", 
			"'*'", "'+'", "'-'", "'~'", "'/'", "'%'", "'&'", "'&&'", "'^'", "'|'", 
			"'||'", "'<<'", "'>>'", "'<'", "'<='", "'>'", "'>='", "'=='", "'!='", 
			"'=~'", "'!~'", "'=#'", "'!#'", "'::'", "'..'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "ADJACENT", "ALL", "AND", "AS", "ASC", "BY", "CONSECUTIVE", "COUNT", 
			"DEFAULT", "DESC", "DISJOINT", "DISTINCT", "DO", "EDGES", "END", "EVEN", 
			"FALSE", "FILTER", "FIND", "FIRST", "FOREACH", "FROM", "GROUP", "HAVING", 
			"HITS", "IN", "LABEL", "LANE", "LIMIT", "NOT", "NULL", "ODD", "OMIT", 
			"ON", "OPTIONAL", "OR", "ORDER", "ORDERED", "RANGE", "REVERSE", "ROOTED", 
			"STEP", "TRUE", "UNORDERED", "WITH", "INT", "LONG", "FLOAT", "DOUBLE", 
			"BOOLEAN", "STRING", "SCOLON", "COLON", "DOT", "LPAREN", "RPAREN", "LBRACE", 
			"RBRACE", "LBRACK", "RBRACK", "COMMA", "UNDERSCORE", "EDGE_LEFT", "EDGE_RIGHT", 
			"EDGE_BIDIRECTIONAL", "EDGE_UNDIRECTED", "AT", "DOLLAR", "HASH", "QMARK", 
			"EXMARK", "ASSIGN", "STAR", "PLUS", "MINUS", "TILDE", "SLASH", "PERCENT", 
			"AMP", "DOUBLE_AMP", "CARET", "PIPE", "DOUBLE_PIPE", "SHIFT_LEFT", "SHIFT_RIGHT", 
			"LT", "LT_EQ", "GT", "GT_EQ", "EQ", "NOT_EQ", "MATCHES", "NOT_MATCHES", 
			"CONTAINS", "NOT_CONTAINS", "DOUBLE_COLON", "DOUBLE_DOT", "PureDigits", 
			"Digits", "StringLiteral", "Identifier", "WS", "SL_COMMENT", "ErrorCharacter"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
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
	public String getGrammarFileName() { return "IQL_Test.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }


		
	/** Test that type of token at given lookahead position is IN the specified set */
	private boolean isAny(int pos, int...set) {
		Token t = _input.LT(pos);
		if(t==null) return false;
		int type = t.getType();
		for(int i=0; i<set.length; i++) {
			if(set[i]==type) {
				return true;
			}
		}
		return false;
	}

	/** Test that type of token at given lookahead position is NOT in the specified set */
	private boolean isNone(int pos, int...set) {
		Token t = _input.LT(pos);
		if(t==null) return true;
		int type = t.getType();
		for(int i=0; i<set.length; i++) {
			if(set[i]==type) {
				return false;
			}
		}
		return true;
	}

	private Token token(int pos) {
		return pos==0 ? getCurrentToken() : _input.LT(pos);
	}

	private boolean adjacent(int from, int to) {
		for(int i=from; i<to; i++) {
			Token t0 = token(i);
			Token t1 = token(i+1);
			int t0End = t0.getStopIndex();
			int t1Begin = t1.getStartIndex();
			if(t0End==-1 || t1Begin==-1 || t1Begin!=t0End+1) {
				return false;
			}
		}
		return true;
	}


	public IQL_TestParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VersionDeclarationTestContext extends ParserRuleContext {
		public VersionDeclarationContext versionDeclaration() {
			return getRuleContext(VersionDeclarationContext.class,0);
		}
		public TerminalNode EOF() { return getToken(IQL_TestParser.EOF, 0); }
		public VersionDeclarationTestContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_versionDeclarationTest; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterVersionDeclarationTest(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitVersionDeclarationTest(this);
		}
	}

	public final VersionDeclarationTestContext versionDeclarationTest() throws RecognitionException {
		VersionDeclarationTestContext _localctx = new VersionDeclarationTestContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_versionDeclarationTest);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(138);
			versionDeclaration();
			setState(139);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class QuantifierTestContext extends ParserRuleContext {
		public QuantifierContext quantifier() {
			return getRuleContext(QuantifierContext.class,0);
		}
		public TerminalNode EOF() { return getToken(IQL_TestParser.EOF, 0); }
		public QuantifierTestContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_quantifierTest; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterQuantifierTest(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitQuantifierTest(this);
		}
	}

	public final QuantifierTestContext quantifierTest() throws RecognitionException {
		QuantifierTestContext _localctx = new QuantifierTestContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_quantifierTest);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(141);
			quantifier();
			setState(142);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UnsignedSimpleQuantifierTestContext extends ParserRuleContext {
		public SimpleQuantifierContext simpleQuantifier() {
			return getRuleContext(SimpleQuantifierContext.class,0);
		}
		public TerminalNode EOF() { return getToken(IQL_TestParser.EOF, 0); }
		public UnsignedSimpleQuantifierTestContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unsignedSimpleQuantifierTest; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterUnsignedSimpleQuantifierTest(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitUnsignedSimpleQuantifierTest(this);
		}
	}

	public final UnsignedSimpleQuantifierTestContext unsignedSimpleQuantifierTest() throws RecognitionException {
		UnsignedSimpleQuantifierTestContext _localctx = new UnsignedSimpleQuantifierTestContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_unsignedSimpleQuantifierTest);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(144);
			simpleQuantifier();
			setState(145);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IntegerLiteralTestContext extends ParserRuleContext {
		public IntegerLiteralContext integerLiteral() {
			return getRuleContext(IntegerLiteralContext.class,0);
		}
		public TerminalNode EOF() { return getToken(IQL_TestParser.EOF, 0); }
		public IntegerLiteralTestContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integerLiteralTest; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterIntegerLiteralTest(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitIntegerLiteralTest(this);
		}
	}

	public final IntegerLiteralTestContext integerLiteralTest() throws RecognitionException {
		IntegerLiteralTestContext _localctx = new IntegerLiteralTestContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_integerLiteralTest);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(147);
			integerLiteral();
			setState(148);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UnsignedIntegerLiteralTestContext extends ParserRuleContext {
		public UnsignedIntegerLiteralContext unsignedIntegerLiteral() {
			return getRuleContext(UnsignedIntegerLiteralContext.class,0);
		}
		public TerminalNode EOF() { return getToken(IQL_TestParser.EOF, 0); }
		public UnsignedIntegerLiteralTestContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unsignedIntegerLiteralTest; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterUnsignedIntegerLiteralTest(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitUnsignedIntegerLiteralTest(this);
		}
	}

	public final UnsignedIntegerLiteralTestContext unsignedIntegerLiteralTest() throws RecognitionException {
		UnsignedIntegerLiteralTestContext _localctx = new UnsignedIntegerLiteralTestContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_unsignedIntegerLiteralTest);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(150);
			unsignedIntegerLiteral();
			setState(151);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FloatingPointLiteralTestContext extends ParserRuleContext {
		public FloatingPointLiteralContext floatingPointLiteral() {
			return getRuleContext(FloatingPointLiteralContext.class,0);
		}
		public TerminalNode EOF() { return getToken(IQL_TestParser.EOF, 0); }
		public FloatingPointLiteralTestContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_floatingPointLiteralTest; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterFloatingPointLiteralTest(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitFloatingPointLiteralTest(this);
		}
	}

	public final FloatingPointLiteralTestContext floatingPointLiteralTest() throws RecognitionException {
		FloatingPointLiteralTestContext _localctx = new FloatingPointLiteralTestContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_floatingPointLiteralTest);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(153);
			floatingPointLiteral();
			setState(154);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UnsignedFloatingPointLiteralTestContext extends ParserRuleContext {
		public UnsignedFloatingPointLiteralContext unsignedFloatingPointLiteral() {
			return getRuleContext(UnsignedFloatingPointLiteralContext.class,0);
		}
		public TerminalNode EOF() { return getToken(IQL_TestParser.EOF, 0); }
		public UnsignedFloatingPointLiteralTestContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unsignedFloatingPointLiteralTest; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterUnsignedFloatingPointLiteralTest(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitUnsignedFloatingPointLiteralTest(this);
		}
	}

	public final UnsignedFloatingPointLiteralTestContext unsignedFloatingPointLiteralTest() throws RecognitionException {
		UnsignedFloatingPointLiteralTestContext _localctx = new UnsignedFloatingPointLiteralTestContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_unsignedFloatingPointLiteralTest);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(156);
			unsignedFloatingPointLiteral();
			setState(157);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StandaloneNodeStatementContext extends ParserRuleContext {
		public NodeStatementContext nodeStatement() {
			return getRuleContext(NodeStatementContext.class,0);
		}
		public TerminalNode EOF() { return getToken(IQL_TestParser.EOF, 0); }
		public StandaloneNodeStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_standaloneNodeStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterStandaloneNodeStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitStandaloneNodeStatement(this);
		}
	}

	public final StandaloneNodeStatementContext standaloneNodeStatement() throws RecognitionException {
		StandaloneNodeStatementContext _localctx = new StandaloneNodeStatementContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_standaloneNodeStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(159);
			nodeStatement(0);
			setState(160);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StandaloneStructuralConstraintContext extends ParserRuleContext {
		public StructuralConstraintContext structuralConstraint() {
			return getRuleContext(StructuralConstraintContext.class,0);
		}
		public TerminalNode EOF() { return getToken(IQL_TestParser.EOF, 0); }
		public StandaloneStructuralConstraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_standaloneStructuralConstraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterStandaloneStructuralConstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitStandaloneStructuralConstraint(this);
		}
	}

	public final StandaloneStructuralConstraintContext standaloneStructuralConstraint() throws RecognitionException {
		StandaloneStructuralConstraintContext _localctx = new StandaloneStructuralConstraintContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_standaloneStructuralConstraint);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(162);
			structuralConstraint();
			setState(163);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StandaloneSelectiveStatementContext extends ParserRuleContext {
		public SelectionStatementContext selectionStatement() {
			return getRuleContext(SelectionStatementContext.class,0);
		}
		public TerminalNode EOF() { return getToken(IQL_TestParser.EOF, 0); }
		public StandaloneSelectiveStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_standaloneSelectiveStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterStandaloneSelectiveStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitStandaloneSelectiveStatement(this);
		}
	}

	public final StandaloneSelectiveStatementContext standaloneSelectiveStatement() throws RecognitionException {
		StandaloneSelectiveStatementContext _localctx = new StandaloneSelectiveStatementContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_standaloneSelectiveStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(165);
			selectionStatement();
			setState(166);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StandaloneExpressionContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode EOF() { return getToken(IQL_TestParser.EOF, 0); }
		public StandaloneExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_standaloneExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterStandaloneExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitStandaloneExpression(this);
		}
	}

	public final StandaloneExpressionContext standaloneExpression() throws RecognitionException {
		StandaloneExpressionContext _localctx = new StandaloneExpressionContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_standaloneExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(168);
			expression(0);
			setState(169);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PayloadStatementContext extends ParserRuleContext {
		public TerminalNode ALL() { return getToken(IQL_TestParser.ALL, 0); }
		public TerminalNode EOF() { return getToken(IQL_TestParser.EOF, 0); }
		public TerminalNode FIND() { return getToken(IQL_TestParser.FIND, 0); }
		public SelectionStatementContext selectionStatement() {
			return getRuleContext(SelectionStatementContext.class,0);
		}
		public BindingsListContext bindingsList() {
			return getRuleContext(BindingsListContext.class,0);
		}
		public TerminalNode FILTER() { return getToken(IQL_TestParser.FILTER, 0); }
		public TerminalNode BY() { return getToken(IQL_TestParser.BY, 0); }
		public ConstraintContext constraint() {
			return getRuleContext(ConstraintContext.class,0);
		}
		public PayloadStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_payloadStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterPayloadStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitPayloadStatement(this);
		}
	}

	public final PayloadStatementContext payloadStatement() throws RecognitionException {
		PayloadStatementContext _localctx = new PayloadStatementContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_payloadStatement);
		int _la;
		try {
			setState(185);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ALL:
				enterOuterAlt(_localctx, 1);
				{
				setState(171);
				match(ALL);
				setState(172);
				match(EOF);
				}
				break;
			case FILTER:
			case FIND:
			case WITH:
				enterOuterAlt(_localctx, 2);
				{
				setState(174);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WITH) {
					{
					setState(173);
					bindingsList();
					}
				}

				setState(179);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FILTER) {
					{
					setState(176);
					match(FILTER);
					setState(177);
					match(BY);
					setState(178);
					constraint();
					}
				}

				setState(181);
				match(FIND);
				setState(182);
				selectionStatement();
				setState(183);
				match(EOF);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BindingsListContext extends ParserRuleContext {
		public TerminalNode WITH() { return getToken(IQL_TestParser.WITH, 0); }
		public List<BindingContext> binding() {
			return getRuleContexts(BindingContext.class);
		}
		public BindingContext binding(int i) {
			return getRuleContext(BindingContext.class,i);
		}
		public List<TerminalNode> AND() { return getTokens(IQL_TestParser.AND); }
		public TerminalNode AND(int i) {
			return getToken(IQL_TestParser.AND, i);
		}
		public BindingsListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bindingsList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterBindingsList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitBindingsList(this);
		}
	}

	public final BindingsListContext bindingsList() throws RecognitionException {
		BindingsListContext _localctx = new BindingsListContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_bindingsList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(187);
			match(WITH);
			setState(188);
			binding();
			setState(193);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AND) {
				{
				{
				setState(189);
				match(AND);
				setState(190);
				binding();
				}
				}
				setState(195);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BindingContext extends ParserRuleContext {
		public List<MemberContext> member() {
			return getRuleContexts(MemberContext.class);
		}
		public MemberContext member(int i) {
			return getRuleContext(MemberContext.class,i);
		}
		public TerminalNode FROM() { return getToken(IQL_TestParser.FROM, 0); }
		public TerminalNode Identifier() { return getToken(IQL_TestParser.Identifier, 0); }
		public List<TerminalNode> COMMA() { return getTokens(IQL_TestParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(IQL_TestParser.COMMA, i);
		}
		public TerminalNode DISTINCT() { return getToken(IQL_TestParser.DISTINCT, 0); }
		public TerminalNode EDGES() { return getToken(IQL_TestParser.EDGES, 0); }
		public BindingContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_binding; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterBinding(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitBinding(this);
		}
	}

	public final BindingContext binding() throws RecognitionException {
		BindingContext _localctx = new BindingContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_binding);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(197);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DISTINCT || _la==EDGES) {
				{
				setState(196);
				_la = _input.LA(1);
				if ( !(_la==DISTINCT || _la==EDGES) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(199);
			member();
			setState(204);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(200);
				match(COMMA);
				setState(201);
				member();
				}
				}
				setState(206);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(207);
			match(FROM);
			setState(208);
			match(Identifier);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SelectionStatementContext extends ParserRuleContext {
		public ConstraintContext constraint() {
			return getRuleContext(ConstraintContext.class,0);
		}
		public StructuralConstraintContext structuralConstraint() {
			return getRuleContext(StructuralConstraintContext.class,0);
		}
		public LaneStatementsListContext laneStatementsList() {
			return getRuleContext(LaneStatementsListContext.class,0);
		}
		public TerminalNode HAVING() { return getToken(IQL_TestParser.HAVING, 0); }
		public SelectionStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectionStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterSelectionStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitSelectionStatement(this);
		}
	}

	public final SelectionStatementContext selectionStatement() throws RecognitionException {
		SelectionStatementContext _localctx = new SelectionStatementContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_selectionStatement);
		int _la;
		try {
			setState(219);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(210);
				constraint();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(213);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case ADJACENT:
				case ALL:
				case CONSECUTIVE:
				case DISJOINT:
				case FIRST:
				case NOT:
				case ORDERED:
				case REVERSE:
				case ROOTED:
				case UNORDERED:
				case LBRACE:
				case LBRACK:
				case EXMARK:
				case STAR:
				case LT:
				case PureDigits:
					{
					setState(211);
					structuralConstraint();
					}
					break;
				case LANE:
					{
					setState(212);
					laneStatementsList();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(217);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==HAVING) {
					{
					setState(215);
					match(HAVING);
					setState(216);
					constraint();
					}
				}

				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LaneStatementsListContext extends ParserRuleContext {
		public List<LaneStatementContext> laneStatement() {
			return getRuleContexts(LaneStatementContext.class);
		}
		public LaneStatementContext laneStatement(int i) {
			return getRuleContext(LaneStatementContext.class,i);
		}
		public List<TerminalNode> AND() { return getTokens(IQL_TestParser.AND); }
		public TerminalNode AND(int i) {
			return getToken(IQL_TestParser.AND, i);
		}
		public LaneStatementsListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_laneStatementsList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterLaneStatementsList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitLaneStatementsList(this);
		}
	}

	public final LaneStatementsListContext laneStatementsList() throws RecognitionException {
		LaneStatementsListContext _localctx = new LaneStatementsListContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_laneStatementsList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(221);
			laneStatement();
			setState(226);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AND) {
				{
				{
				setState(222);
				match(AND);
				setState(223);
				laneStatement();
				}
				}
				setState(228);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LaneStatementContext extends ParserRuleContext {
		public Token name;
		public TerminalNode LANE() { return getToken(IQL_TestParser.LANE, 0); }
		public StructuralConstraintContext structuralConstraint() {
			return getRuleContext(StructuralConstraintContext.class,0);
		}
		public TerminalNode Identifier() { return getToken(IQL_TestParser.Identifier, 0); }
		public TerminalNode AS() { return getToken(IQL_TestParser.AS, 0); }
		public MemberContext member() {
			return getRuleContext(MemberContext.class,0);
		}
		public LaneStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_laneStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterLaneStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitLaneStatement(this);
		}
	}

	public final LaneStatementContext laneStatement() throws RecognitionException {
		LaneStatementContext _localctx = new LaneStatementContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_laneStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(229);
			match(LANE);
			setState(230);
			((LaneStatementContext)_localctx).name = match(Identifier);
			setState(233);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS) {
				{
				setState(231);
				match(AS);
				setState(232);
				member();
				}
			}

			setState(235);
			structuralConstraint();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StructuralConstraintContext extends ParserRuleContext {
		public HitsLimitContext hitsLimit() {
			return getRuleContext(HitsLimitContext.class,0);
		}
		public List<MatchFlagContext> matchFlag() {
			return getRuleContexts(MatchFlagContext.class);
		}
		public MatchFlagContext matchFlag(int i) {
			return getRuleContext(MatchFlagContext.class,i);
		}
		public List<NodeStatementContext> nodeStatement() {
			return getRuleContexts(NodeStatementContext.class);
		}
		public NodeStatementContext nodeStatement(int i) {
			return getRuleContext(NodeStatementContext.class,i);
		}
		public StructuralConstraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_structuralConstraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterStructuralConstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitStructuralConstraint(this);
		}
	}

	public final StructuralConstraintContext structuralConstraint() throws RecognitionException {
		StructuralConstraintContext _localctx = new StructuralConstraintContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_structuralConstraint);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(238);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				{
				setState(237);
				hitsLimit();
				}
				break;
			}
			setState(243);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3298534885504L) != 0)) {
				{
				{
				setState(240);
				matchFlag();
				}
				}
				setState(245);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(247); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(246);
				nodeStatement(0);
				}
				}
				setState(249); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 720593808516972550L) != 0) || ((((_la - 71)) & ~0x3f) == 0 && ((1L << (_la - 71)) & 134250501L) != 0) );
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class HitsLimitContext extends ParserRuleContext {
		public TerminalNode PureDigits() { return getToken(IQL_TestParser.PureDigits, 0); }
		public TerminalNode HITS() { return getToken(IQL_TestParser.HITS, 0); }
		public TerminalNode FIRST() { return getToken(IQL_TestParser.FIRST, 0); }
		public HitsLimitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hitsLimit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterHitsLimit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitHitsLimit(this);
		}
	}

	public final HitsLimitContext hitsLimit() throws RecognitionException {
		HitsLimitContext _localctx = new HitsLimitContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_hitsLimit);
		try {
			setState(254);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PureDigits:
				enterOuterAlt(_localctx, 1);
				{
				setState(251);
				match(PureDigits);
				setState(252);
				match(HITS);
				}
				break;
			case FIRST:
				enterOuterAlt(_localctx, 2);
				{
				setState(253);
				match(FIRST);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MatchFlagContext extends ParserRuleContext {
		public TerminalNode DISJOINT() { return getToken(IQL_TestParser.DISJOINT, 0); }
		public TerminalNode CONSECUTIVE() { return getToken(IQL_TestParser.CONSECUTIVE, 0); }
		public TerminalNode REVERSE() { return getToken(IQL_TestParser.REVERSE, 0); }
		public TerminalNode ROOTED() { return getToken(IQL_TestParser.ROOTED, 0); }
		public MatchFlagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_matchFlag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterMatchFlag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitMatchFlag(this);
		}
	}

	public final MatchFlagContext matchFlag() throws RecognitionException {
		MatchFlagContext _localctx = new MatchFlagContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_matchFlag);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(256);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 3298534885504L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NodeStatementContext extends ParserRuleContext {
		public NodeStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nodeStatement; }
	 
		public NodeStatementContext() { }
		public void copyFrom(NodeStatementContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ElementSequenceContext extends NodeStatementContext {
		public List<NodeArrangementContext> nodeArrangement() {
			return getRuleContexts(NodeArrangementContext.class);
		}
		public NodeArrangementContext nodeArrangement(int i) {
			return getRuleContext(NodeArrangementContext.class,i);
		}
		public List<NodeStatementContext> nodeStatement() {
			return getRuleContexts(NodeStatementContext.class);
		}
		public NodeStatementContext nodeStatement(int i) {
			return getRuleContext(NodeStatementContext.class,i);
		}
		public ElementSequenceContext(NodeStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterElementSequence(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitElementSequence(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ElementGroupingContext extends NodeStatementContext {
		public TerminalNode LBRACE() { return getToken(IQL_TestParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(IQL_TestParser.RBRACE, 0); }
		public QuantifierContext quantifier() {
			return getRuleContext(QuantifierContext.class,0);
		}
		public List<NodeStatementContext> nodeStatement() {
			return getRuleContexts(NodeStatementContext.class);
		}
		public NodeStatementContext nodeStatement(int i) {
			return getRuleContext(NodeStatementContext.class,i);
		}
		public ElementGroupingContext(NodeStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterElementGrouping(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitElementGrouping(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SingleNodeContext extends NodeStatementContext {
		public NodeContext node() {
			return getRuleContext(NodeContext.class,0);
		}
		public SingleNodeContext(NodeStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterSingleNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitSingleNode(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class GraphFragmentContext extends NodeStatementContext {
		public List<ElementContext> element() {
			return getRuleContexts(ElementContext.class);
		}
		public ElementContext element(int i) {
			return getRuleContext(ElementContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(IQL_TestParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(IQL_TestParser.COMMA, i);
		}
		public GraphFragmentContext(NodeStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterGraphFragment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitGraphFragment(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ElementDisjunctionContext extends NodeStatementContext {
		public NodeStatementContext left;
		public NodeStatementContext right;
		public OrContext or() {
			return getRuleContext(OrContext.class,0);
		}
		public List<NodeStatementContext> nodeStatement() {
			return getRuleContexts(NodeStatementContext.class);
		}
		public NodeStatementContext nodeStatement(int i) {
			return getRuleContext(NodeStatementContext.class,i);
		}
		public ElementDisjunctionContext(NodeStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterElementDisjunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitElementDisjunction(this);
		}
	}

	public final NodeStatementContext nodeStatement() throws RecognitionException {
		return nodeStatement(0);
	}

	private NodeStatementContext nodeStatement(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		NodeStatementContext _localctx = new NodeStatementContext(_ctx, _parentState);
		NodeStatementContext _prevctx = _localctx;
		int _startState = 40;
		enterRecursionRule(_localctx, 40, RULE_nodeStatement, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(289);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
			case 1:
				{
				_localctx = new ElementGroupingContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(260);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ALL || _la==NOT || ((((_la - 71)) & ~0x3f) == 0 && ((1L << (_la - 71)) & 134250501L) != 0)) {
					{
					setState(259);
					quantifier();
					}
				}

				setState(262);
				match(LBRACE);
				setState(264); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(263);
					nodeStatement(0);
					}
					}
					setState(266); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 720593808516972550L) != 0) || ((((_la - 71)) & ~0x3f) == 0 && ((1L << (_la - 71)) & 134250501L) != 0) );
				setState(268);
				match(RBRACE);
				}
				break;
			case 2:
				{
				_localctx = new ElementSequenceContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(271); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(270);
						nodeArrangement();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(273); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(276); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(275);
						nodeStatement(0);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(278); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;
			case 3:
				{
				_localctx = new SingleNodeContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(280);
				node();
				}
				break;
			case 4:
				{
				_localctx = new GraphFragmentContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(281);
				element();
				setState(286);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(282);
						match(COMMA);
						setState(283);
						element();
						}
						} 
					}
					setState(288);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
				}
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(297);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ElementDisjunctionContext(new NodeStatementContext(_parentctx, _parentState));
					((ElementDisjunctionContext)_localctx).left = _prevctx;
					pushNewRecursionContext(_localctx, _startState, RULE_nodeStatement);
					setState(291);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(292);
					or();
					setState(293);
					((ElementDisjunctionContext)_localctx).right = nodeStatement(1);
					}
					} 
				}
				setState(299);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NodeArrangementContext extends ParserRuleContext {
		public TerminalNode UNORDERED() { return getToken(IQL_TestParser.UNORDERED, 0); }
		public TerminalNode ORDERED() { return getToken(IQL_TestParser.ORDERED, 0); }
		public TerminalNode ADJACENT() { return getToken(IQL_TestParser.ADJACENT, 0); }
		public NodeArrangementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nodeArrangement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterNodeArrangement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitNodeArrangement(this);
		}
	}

	public final NodeArrangementContext nodeArrangement() throws RecognitionException {
		NodeArrangementContext _localctx = new NodeArrangementContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_nodeArrangement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(300);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 17867063951362L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NodeContext extends ParserRuleContext {
		public NodeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_node; }
	 
		public NodeContext() { }
		public void copyFrom(NodeContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DummyNodeContext extends NodeContext {
		public TerminalNode LBRACK() { return getToken(IQL_TestParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(IQL_TestParser.RBRACK, 0); }
		public TerminalNode PLUS() { return getToken(IQL_TestParser.PLUS, 0); }
		public TerminalNode QMARK() { return getToken(IQL_TestParser.QMARK, 0); }
		public TerminalNode STAR() { return getToken(IQL_TestParser.STAR, 0); }
		public DummyNodeContext(NodeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterDummyNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitDummyNode(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ProperNodeContext extends NodeContext {
		public TerminalNode LBRACK() { return getToken(IQL_TestParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(IQL_TestParser.RBRACK, 0); }
		public QuantifierContext quantifier() {
			return getRuleContext(QuantifierContext.class,0);
		}
		public MemberLabelContext memberLabel() {
			return getRuleContext(MemberLabelContext.class,0);
		}
		public PositionMarkerContext positionMarker() {
			return getRuleContext(PositionMarkerContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(IQL_TestParser.COMMA, 0); }
		public ConstraintContext constraint() {
			return getRuleContext(ConstraintContext.class,0);
		}
		public StructuralConstraintContext structuralConstraint() {
			return getRuleContext(StructuralConstraintContext.class,0);
		}
		public ProperNodeContext(NodeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterProperNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitProperNode(this);
		}
	}

	public final NodeContext node() throws RecognitionException {
		NodeContext _localctx = new NodeContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_node);
		int _la;
		try {
			setState(324);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
			case 1:
				_localctx = new DummyNodeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(302);
				match(LBRACK);
				setState(303);
				_la = _input.LA(1);
				if ( !(((((_la - 70)) & ~0x3f) == 0 && ((1L << (_la - 70)) & 25L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(304);
				match(RBRACK);
				}
				break;
			case 2:
				_localctx = new ProperNodeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(306);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ALL || _la==NOT || ((((_la - 71)) & ~0x3f) == 0 && ((1L << (_la - 71)) & 134250501L) != 0)) {
					{
					setState(305);
					quantifier();
					}
				}

				setState(308);
				match(LBRACK);
				setState(310);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
				case 1:
					{
					setState(309);
					memberLabel();
					}
					break;
				}
				setState(315);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
				case 1:
					{
					setState(312);
					positionMarker(0);
					setState(313);
					match(COMMA);
					}
					break;
				}
				setState(318);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
				case 1:
					{
					setState(317);
					constraint();
					}
					break;
				}
				setState(321);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 720597107052906630L) != 0) || ((((_la - 71)) & ~0x3f) == 0 && ((1L << (_la - 71)) & 134250501L) != 0)) {
					{
					setState(320);
					structuralConstraint();
					}
				}

				setState(323);
				match(RBRACK);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MemberLabelContext extends ParserRuleContext {
		public MemberContext member() {
			return getRuleContext(MemberContext.class,0);
		}
		public TerminalNode COLON() { return getToken(IQL_TestParser.COLON, 0); }
		public MemberLabelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_memberLabel; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterMemberLabel(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitMemberLabel(this);
		}
	}

	public final MemberLabelContext memberLabel() throws RecognitionException {
		MemberLabelContext _localctx = new MemberLabelContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_memberLabel);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(326);
			member();
			setState(327);
			match(COLON);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PositionMarkerContext extends ParserRuleContext {
		public PositionMarkerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_positionMarker; }
	 
		public PositionMarkerContext() { }
		public void copyFrom(PositionMarkerContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MarkerConjunctionContext extends PositionMarkerContext {
		public PositionMarkerContext left;
		public PositionMarkerContext right;
		public AndContext and() {
			return getRuleContext(AndContext.class,0);
		}
		public List<PositionMarkerContext> positionMarker() {
			return getRuleContexts(PositionMarkerContext.class);
		}
		public PositionMarkerContext positionMarker(int i) {
			return getRuleContext(PositionMarkerContext.class,i);
		}
		public MarkerConjunctionContext(PositionMarkerContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterMarkerConjunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitMarkerConjunction(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MarkerDisjunctionContext extends PositionMarkerContext {
		public PositionMarkerContext left;
		public PositionMarkerContext right;
		public OrContext or() {
			return getRuleContext(OrContext.class,0);
		}
		public List<PositionMarkerContext> positionMarker() {
			return getRuleContexts(PositionMarkerContext.class);
		}
		public PositionMarkerContext positionMarker(int i) {
			return getRuleContext(PositionMarkerContext.class,i);
		}
		public MarkerDisjunctionContext(PositionMarkerContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterMarkerDisjunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitMarkerDisjunction(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MarkerCallContext extends PositionMarkerContext {
		public Token name;
		public TerminalNode Identifier() { return getToken(IQL_TestParser.Identifier, 0); }
		public TerminalNode LPAREN() { return getToken(IQL_TestParser.LPAREN, 0); }
		public List<PositionArgumentContext> positionArgument() {
			return getRuleContexts(PositionArgumentContext.class);
		}
		public PositionArgumentContext positionArgument(int i) {
			return getRuleContext(PositionArgumentContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(IQL_TestParser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(IQL_TestParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(IQL_TestParser.COMMA, i);
		}
		public MarkerCallContext(PositionMarkerContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterMarkerCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitMarkerCall(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MarkerWrappingContext extends PositionMarkerContext {
		public TerminalNode LPAREN() { return getToken(IQL_TestParser.LPAREN, 0); }
		public PositionMarkerContext positionMarker() {
			return getRuleContext(PositionMarkerContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(IQL_TestParser.RPAREN, 0); }
		public MarkerWrappingContext(PositionMarkerContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterMarkerWrapping(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitMarkerWrapping(this);
		}
	}

	public final PositionMarkerContext positionMarker() throws RecognitionException {
		return positionMarker(0);
	}

	private PositionMarkerContext positionMarker(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		PositionMarkerContext _localctx = new PositionMarkerContext(_ctx, _parentState);
		PositionMarkerContext _prevctx = _localctx;
		int _startState = 48;
		enterRecursionRule(_localctx, 48, RULE_positionMarker, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(348);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case Identifier:
				{
				_localctx = new MarkerCallContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(330);
				((MarkerCallContext)_localctx).name = match(Identifier);
				setState(342);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
				case 1:
					{
					setState(331);
					match(LPAREN);
					setState(332);
					positionArgument();
					setState(337);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(333);
						match(COMMA);
						setState(334);
						positionArgument();
						}
						}
						setState(339);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(340);
					match(RPAREN);
					}
					break;
				}
				}
				break;
			case LPAREN:
				{
				_localctx = new MarkerWrappingContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(344);
				match(LPAREN);
				setState(345);
				positionMarker(0);
				setState(346);
				match(RPAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(360);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(358);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
					case 1:
						{
						_localctx = new MarkerConjunctionContext(new PositionMarkerContext(_parentctx, _parentState));
						((MarkerConjunctionContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_positionMarker);
						setState(350);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(351);
						and();
						setState(352);
						((MarkerConjunctionContext)_localctx).right = positionMarker(2);
						}
						break;
					case 2:
						{
						_localctx = new MarkerDisjunctionContext(new PositionMarkerContext(_parentctx, _parentState));
						((MarkerDisjunctionContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_positionMarker);
						setState(354);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(355);
						or();
						setState(356);
						((MarkerDisjunctionContext)_localctx).right = positionMarker(1);
						}
						break;
					}
					} 
				}
				setState(362);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PositionArgumentContext extends ParserRuleContext {
		public SignedIntegerLiteralContext signedIntegerLiteral() {
			return getRuleContext(SignedIntegerLiteralContext.class,0);
		}
		public SignedFloatingPointLiteralContext signedFloatingPointLiteral() {
			return getRuleContext(SignedFloatingPointLiteralContext.class,0);
		}
		public PositionArgumentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_positionArgument; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterPositionArgument(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitPositionArgument(this);
		}
	}

	public final PositionArgumentContext positionArgument() throws RecognitionException {
		PositionArgumentContext _localctx = new PositionArgumentContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_positionArgument);
		try {
			setState(365);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,33,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(363);
				signedIntegerLiteral();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(364);
				signedFloatingPointLiteral();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ElementContext extends ParserRuleContext {
		public NodeContext content;
		public NodeContext source;
		public NodeContext target;
		public List<NodeContext> node() {
			return getRuleContexts(NodeContext.class);
		}
		public NodeContext node(int i) {
			return getRuleContext(NodeContext.class,i);
		}
		public EdgeContext edge() {
			return getRuleContext(EdgeContext.class,0);
		}
		public ElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_element; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterElement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitElement(this);
		}
	}

	public final ElementContext element() throws RecognitionException {
		ElementContext _localctx = new ElementContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_element);
		try {
			setState(372);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,34,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(367);
				((ElementContext)_localctx).content = node();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(368);
				((ElementContext)_localctx).source = node();
				setState(369);
				edge();
				setState(370);
				((ElementContext)_localctx).target = node();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class EdgeContext extends ParserRuleContext {
		public EdgeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_edge; }
	 
		public EdgeContext() { }
		public void copyFrom(EdgeContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class EmptyEdgeContext extends EdgeContext {
		public TerminalNode EDGE_LEFT() { return getToken(IQL_TestParser.EDGE_LEFT, 0); }
		public TerminalNode EDGE_RIGHT() { return getToken(IQL_TestParser.EDGE_RIGHT, 0); }
		public TerminalNode EDGE_BIDIRECTIONAL() { return getToken(IQL_TestParser.EDGE_BIDIRECTIONAL, 0); }
		public TerminalNode EDGE_UNDIRECTED() { return getToken(IQL_TestParser.EDGE_UNDIRECTED, 0); }
		public EmptyEdgeContext(EdgeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterEmptyEdge(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitEmptyEdge(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FilledEdgeContext extends EdgeContext {
		public LeftEdgePartContext leftEdgePart() {
			return getRuleContext(LeftEdgePartContext.class,0);
		}
		public TerminalNode LBRACK() { return getToken(IQL_TestParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(IQL_TestParser.RBRACK, 0); }
		public RightEdgePartContext rightEdgePart() {
			return getRuleContext(RightEdgePartContext.class,0);
		}
		public MemberLabelContext memberLabel() {
			return getRuleContext(MemberLabelContext.class,0);
		}
		public ConstraintContext constraint() {
			return getRuleContext(ConstraintContext.class,0);
		}
		public FilledEdgeContext(EdgeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterFilledEdge(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitFilledEdge(this);
		}
	}

	public final EdgeContext edge() throws RecognitionException {
		EdgeContext _localctx = new EdgeContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_edge);
		int _la;
		try {
			setState(386);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EDGE_LEFT:
			case EDGE_RIGHT:
			case EDGE_BIDIRECTIONAL:
			case EDGE_UNDIRECTED:
				_localctx = new EmptyEdgeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(374);
				_la = _input.LA(1);
				if ( !(((((_la - 63)) & ~0x3f) == 0 && ((1L << (_la - 63)) & 15L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case MINUS:
			case LT:
				_localctx = new FilledEdgeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(375);
				leftEdgePart();
				setState(376);
				match(LBRACK);
				setState(378);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,35,_ctx) ) {
				case 1:
					{
					setState(377);
					memberLabel();
					}
					break;
				}
				setState(381);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 183882327852711936L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 32212255635L) != 0)) {
					{
					setState(380);
					constraint();
					}
				}

				setState(383);
				match(RBRACK);
				setState(384);
				rightEdgePart();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LeftEdgePartContext extends ParserRuleContext {
		public DirectedEdgeLeftContext directedEdgeLeft() {
			return getRuleContext(DirectedEdgeLeftContext.class,0);
		}
		public UndirectedEdgeContext undirectedEdge() {
			return getRuleContext(UndirectedEdgeContext.class,0);
		}
		public LeftEdgePartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_leftEdgePart; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterLeftEdgePart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitLeftEdgePart(this);
		}
	}

	public final LeftEdgePartContext leftEdgePart() throws RecognitionException {
		LeftEdgePartContext _localctx = new LeftEdgePartContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_leftEdgePart);
		try {
			setState(390);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LT:
				enterOuterAlt(_localctx, 1);
				{
				setState(388);
				directedEdgeLeft();
				}
				break;
			case MINUS:
				enterOuterAlt(_localctx, 2);
				{
				setState(389);
				undirectedEdge();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class RightEdgePartContext extends ParserRuleContext {
		public DirectedEdgeRightContext directedEdgeRight() {
			return getRuleContext(DirectedEdgeRightContext.class,0);
		}
		public UndirectedEdgeContext undirectedEdge() {
			return getRuleContext(UndirectedEdgeContext.class,0);
		}
		public RightEdgePartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rightEdgePart; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterRightEdgePart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitRightEdgePart(this);
		}
	}

	public final RightEdgePartContext rightEdgePart() throws RecognitionException {
		RightEdgePartContext _localctx = new RightEdgePartContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_rightEdgePart);
		try {
			setState(394);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(392);
				directedEdgeRight();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(393);
				undirectedEdge();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DirectedEdgeLeftContext extends ParserRuleContext {
		public TerminalNode LT() { return getToken(IQL_TestParser.LT, 0); }
		public TerminalNode MINUS() { return getToken(IQL_TestParser.MINUS, 0); }
		public DirectedEdgeLeftContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_directedEdgeLeft; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterDirectedEdgeLeft(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitDirectedEdgeLeft(this);
		}
	}

	public final DirectedEdgeLeftContext directedEdgeLeft() throws RecognitionException {
		DirectedEdgeLeftContext _localctx = new DirectedEdgeLeftContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_directedEdgeLeft);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(396);
			match(LT);
			setState(397);
			match(MINUS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class DirectedEdgeRightContext extends ParserRuleContext {
		public TerminalNode MINUS() { return getToken(IQL_TestParser.MINUS, 0); }
		public TerminalNode GT() { return getToken(IQL_TestParser.GT, 0); }
		public DirectedEdgeRightContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_directedEdgeRight; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterDirectedEdgeRight(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitDirectedEdgeRight(this);
		}
	}

	public final DirectedEdgeRightContext directedEdgeRight() throws RecognitionException {
		DirectedEdgeRightContext _localctx = new DirectedEdgeRightContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_directedEdgeRight);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(399);
			match(MINUS);
			setState(400);
			match(GT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UndirectedEdgeContext extends ParserRuleContext {
		public List<TerminalNode> MINUS() { return getTokens(IQL_TestParser.MINUS); }
		public TerminalNode MINUS(int i) {
			return getToken(IQL_TestParser.MINUS, i);
		}
		public UndirectedEdgeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_undirectedEdge; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterUndirectedEdge(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitUndirectedEdge(this);
		}
	}

	public final UndirectedEdgeContext undirectedEdge() throws RecognitionException {
		UndirectedEdgeContext _localctx = new UndirectedEdgeContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_undirectedEdge);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(402);
			match(MINUS);
			setState(403);
			match(MINUS);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GroupStatementContext extends ParserRuleContext {
		public TerminalNode GROUP() { return getToken(IQL_TestParser.GROUP, 0); }
		public List<GroupExpressionContext> groupExpression() {
			return getRuleContexts(GroupExpressionContext.class);
		}
		public GroupExpressionContext groupExpression(int i) {
			return getRuleContext(GroupExpressionContext.class,i);
		}
		public TerminalNode EOF() { return getToken(IQL_TestParser.EOF, 0); }
		public List<TerminalNode> COMMA() { return getTokens(IQL_TestParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(IQL_TestParser.COMMA, i);
		}
		public GroupStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterGroupStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitGroupStatement(this);
		}
	}

	public final GroupStatementContext groupStatement() throws RecognitionException {
		GroupStatementContext _localctx = new GroupStatementContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_groupStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(405);
			match(GROUP);
			setState(406);
			groupExpression();
			setState(411);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(407);
				match(COMMA);
				setState(408);
				groupExpression();
				}
				}
				setState(413);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(414);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class GroupExpressionContext extends ParserRuleContext {
		public ExpressionContext selector;
		public ExpressionContext filter;
		public Token label;
		public ExpressionContext defaultValue;
		public TerminalNode BY() { return getToken(IQL_TestParser.BY, 0); }
		public TerminalNode LABEL() { return getToken(IQL_TestParser.LABEL, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode StringLiteral() { return getToken(IQL_TestParser.StringLiteral, 0); }
		public TerminalNode FILTER() { return getToken(IQL_TestParser.FILTER, 0); }
		public TerminalNode ON() { return getToken(IQL_TestParser.ON, 0); }
		public TerminalNode DEFAULT() { return getToken(IQL_TestParser.DEFAULT, 0); }
		public GroupExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterGroupExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitGroupExpression(this);
		}
	}

	public final GroupExpressionContext groupExpression() throws RecognitionException {
		GroupExpressionContext _localctx = new GroupExpressionContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_groupExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(416);
			match(BY);
			setState(417);
			((GroupExpressionContext)_localctx).selector = expression(0);
			setState(421);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FILTER) {
				{
				setState(418);
				match(FILTER);
				setState(419);
				match(ON);
				setState(420);
				((GroupExpressionContext)_localctx).filter = expression(0);
				}
			}

			setState(423);
			match(LABEL);
			setState(424);
			((GroupExpressionContext)_localctx).label = match(StringLiteral);
			setState(427);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DEFAULT) {
				{
				setState(425);
				match(DEFAULT);
				setState(426);
				((GroupExpressionContext)_localctx).defaultValue = expression(0);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ResultStatementContext extends ParserRuleContext {
		public UnsignedIntegerLiteralContext limit;
		public TerminalNode EOF() { return getToken(IQL_TestParser.EOF, 0); }
		public TerminalNode LIMIT() { return getToken(IQL_TestParser.LIMIT, 0); }
		public OrderExpressionListContext orderExpressionList() {
			return getRuleContext(OrderExpressionListContext.class,0);
		}
		public UnsignedIntegerLiteralContext unsignedIntegerLiteral() {
			return getRuleContext(UnsignedIntegerLiteralContext.class,0);
		}
		public TerminalNode FIRST() { return getToken(IQL_TestParser.FIRST, 0); }
		public ResultStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_resultStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterResultStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitResultStatement(this);
		}
	}

	public final ResultStatementContext resultStatement() throws RecognitionException {
		ResultStatementContext _localctx = new ResultStatementContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_resultStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(434);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LIMIT) {
				{
				setState(429);
				match(LIMIT);
				setState(431);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FIRST) {
					{
					setState(430);
					match(FIRST);
					}
				}

				setState(433);
				((ResultStatementContext)_localctx).limit = unsignedIntegerLiteral();
				}
			}

			setState(437);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ORDER) {
				{
				setState(436);
				orderExpressionList();
				}
			}

			setState(439);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OrderExpressionListContext extends ParserRuleContext {
		public TerminalNode ORDER() { return getToken(IQL_TestParser.ORDER, 0); }
		public TerminalNode BY() { return getToken(IQL_TestParser.BY, 0); }
		public List<OrderExpressionContext> orderExpression() {
			return getRuleContexts(OrderExpressionContext.class);
		}
		public OrderExpressionContext orderExpression(int i) {
			return getRuleContext(OrderExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(IQL_TestParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(IQL_TestParser.COMMA, i);
		}
		public OrderExpressionListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderExpressionList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterOrderExpressionList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitOrderExpressionList(this);
		}
	}

	public final OrderExpressionListContext orderExpressionList() throws RecognitionException {
		OrderExpressionListContext _localctx = new OrderExpressionListContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_orderExpressionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(441);
			match(ORDER);
			setState(442);
			match(BY);
			setState(443);
			orderExpression();
			setState(448);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(444);
				match(COMMA);
				setState(445);
				orderExpression();
				}
				}
				setState(450);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OrderExpressionContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode ASC() { return getToken(IQL_TestParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(IQL_TestParser.DESC, 0); }
		public OrderExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterOrderExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitOrderExpression(this);
		}
	}

	public final OrderExpressionContext orderExpression() throws RecognitionException {
		OrderExpressionContext _localctx = new OrderExpressionContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_orderExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(451);
			expression(0);
			setState(452);
			_la = _input.LA(1);
			if ( !(_la==ASC || _la==DESC) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ConstraintContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ConstraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_constraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterConstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitConstraint(this);
		}
	}

	public final ConstraintContext constraint() throws RecognitionException {
		ConstraintContext _localctx = new ConstraintContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_constraint);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(454);
			expression(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionListContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(IQL_TestParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(IQL_TestParser.COMMA, i);
		}
		public ExpressionListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterExpressionList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitExpressionList(this);
		}
	}

	public final ExpressionListContext expressionList() throws RecognitionException {
		ExpressionListContext _localctx = new ExpressionListContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_expressionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(456);
			expression(0);
			setState(461);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(457);
				match(COMMA);
				setState(458);
				expression(0);
				}
				}
				setState(463);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ExpressionContext extends ParserRuleContext {
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
	 
		public ExpressionContext() { }
		public void copyFrom(ExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryExpressionContext extends ExpressionContext {
		public PrimaryContext primary() {
			return getRuleContext(PrimaryContext.class,0);
		}
		public PrimaryExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterPrimaryExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitPrimaryExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class DisjunctionContext extends ExpressionContext {
		public ExpressionContext left;
		public ExpressionContext right;
		public OrContext or() {
			return getRuleContext(OrContext.class,0);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public DisjunctionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterDisjunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitDisjunction(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PathAccessContext extends ExpressionContext {
		public ExpressionContext source;
		public TerminalNode DOT() { return getToken(IQL_TestParser.DOT, 0); }
		public TerminalNode Identifier() { return getToken(IQL_TestParser.Identifier, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public PathAccessContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterPathAccess(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitPathAccess(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ForEachContext extends ExpressionContext {
		public LoopExpresseionContext loopExpresseion() {
			return getRuleContext(LoopExpresseionContext.class,0);
		}
		public ForEachContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterForEach(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitForEach(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MethodInvocationContext extends ExpressionContext {
		public ExpressionContext source;
		public ExpressionListContext arguments;
		public TerminalNode LPAREN() { return getToken(IQL_TestParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(IQL_TestParser.RPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public MethodInvocationContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterMethodInvocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitMethodInvocation(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AdditiveOpContext extends ExpressionContext {
		public ExpressionContext left;
		public ExpressionContext right;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode PLUS() { return getToken(IQL_TestParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(IQL_TestParser.MINUS, 0); }
		public AdditiveOpContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterAdditiveOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitAdditiveOp(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class UnaryOpContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode NOT() { return getToken(IQL_TestParser.NOT, 0); }
		public TerminalNode EXMARK() { return getToken(IQL_TestParser.EXMARK, 0); }
		public TerminalNode MINUS() { return getToken(IQL_TestParser.MINUS, 0); }
		public TerminalNode TILDE() { return getToken(IQL_TestParser.TILDE, 0); }
		public UnaryOpContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterUnaryOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitUnaryOp(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ComparisonOpContext extends ExpressionContext {
		public ExpressionContext left;
		public ExpressionContext right;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode LT() { return getToken(IQL_TestParser.LT, 0); }
		public TerminalNode LT_EQ() { return getToken(IQL_TestParser.LT_EQ, 0); }
		public TerminalNode GT() { return getToken(IQL_TestParser.GT, 0); }
		public TerminalNode GT_EQ() { return getToken(IQL_TestParser.GT_EQ, 0); }
		public ComparisonOpContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterComparisonOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitComparisonOp(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AssignmentOpContext extends ExpressionContext {
		public ExpressionContext source;
		public TerminalNode AS() { return getToken(IQL_TestParser.AS, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public MemberContext member() {
			return getRuleContext(MemberContext.class,0);
		}
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public TerminalNode OPTIONAL() { return getToken(IQL_TestParser.OPTIONAL, 0); }
		public AssignmentOpContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterAssignmentOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitAssignmentOp(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StringOpContext extends ExpressionContext {
		public ExpressionContext left;
		public ExpressionContext right;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode MATCHES() { return getToken(IQL_TestParser.MATCHES, 0); }
		public TerminalNode NOT_MATCHES() { return getToken(IQL_TestParser.NOT_MATCHES, 0); }
		public TerminalNode CONTAINS() { return getToken(IQL_TestParser.CONTAINS, 0); }
		public TerminalNode NOT_CONTAINS() { return getToken(IQL_TestParser.NOT_CONTAINS, 0); }
		public StringOpContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterStringOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitStringOp(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class WrappingExpressionContext extends ExpressionContext {
		public TerminalNode LPAREN() { return getToken(IQL_TestParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(IQL_TestParser.RPAREN, 0); }
		public WrappingExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterWrappingExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitWrappingExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CastExpressionContext extends ExpressionContext {
		public TerminalNode LPAREN() { return getToken(IQL_TestParser.LPAREN, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(IQL_TestParser.RPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public CastExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterCastExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitCastExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ListAccessContext extends ExpressionContext {
		public ExpressionContext source;
		public ExpressionListContext indices;
		public TerminalNode LBRACK() { return getToken(IQL_TestParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(IQL_TestParser.RBRACK, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public ListAccessContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterListAccess(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitListAccess(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SetPredicateContext extends ExpressionContext {
		public ExpressionContext source;
		public ExpressionContext target;
		public TerminalNode IN() { return getToken(IQL_TestParser.IN, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public AllContext all() {
			return getRuleContext(AllContext.class,0);
		}
		public NotContext not() {
			return getRuleContext(NotContext.class,0);
		}
		public SetPredicateContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterSetPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitSetPredicate(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class EqualityCheckContext extends ExpressionContext {
		public ExpressionContext left;
		public ExpressionContext right;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode EQ() { return getToken(IQL_TestParser.EQ, 0); }
		public TerminalNode NOT_EQ() { return getToken(IQL_TestParser.NOT_EQ, 0); }
		public EqualityCheckContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterEqualityCheck(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitEqualityCheck(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ConjunctionContext extends ExpressionContext {
		public ExpressionContext left;
		public ExpressionContext right;
		public AndContext and() {
			return getRuleContext(AndContext.class,0);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public ConjunctionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterConjunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitConjunction(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AnnotationAccessContext extends ExpressionContext {
		public ExpressionContext source;
		public ExpressionListContext keys;
		public TerminalNode LBRACE() { return getToken(IQL_TestParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(IQL_TestParser.RBRACE, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public AnnotationAccessContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterAnnotationAccess(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitAnnotationAccess(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MultiplicativeOpContext extends ExpressionContext {
		public ExpressionContext left;
		public ExpressionContext right;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode STAR() { return getToken(IQL_TestParser.STAR, 0); }
		public TerminalNode SLASH() { return getToken(IQL_TestParser.SLASH, 0); }
		public TerminalNode PERCENT() { return getToken(IQL_TestParser.PERCENT, 0); }
		public MultiplicativeOpContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterMultiplicativeOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitMultiplicativeOp(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class BitwiseOpContext extends ExpressionContext {
		public ExpressionContext left;
		public ExpressionContext right;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode SHIFT_LEFT() { return getToken(IQL_TestParser.SHIFT_LEFT, 0); }
		public TerminalNode SHIFT_RIGHT() { return getToken(IQL_TestParser.SHIFT_RIGHT, 0); }
		public TerminalNode PIPE() { return getToken(IQL_TestParser.PIPE, 0); }
		public TerminalNode AMP() { return getToken(IQL_TestParser.AMP, 0); }
		public TerminalNode CARET() { return getToken(IQL_TestParser.CARET, 0); }
		public BitwiseOpContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterBitwiseOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitBitwiseOp(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TernaryOpContext extends ExpressionContext {
		public ExpressionContext condition;
		public ExpressionContext optionTrue;
		public ExpressionContext optionFalse;
		public TerminalNode QMARK() { return getToken(IQL_TestParser.QMARK, 0); }
		public TerminalNode COLON() { return getToken(IQL_TestParser.COLON, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TernaryOpContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterTernaryOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitTernaryOp(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		return expression(0);
	}

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 80;
		enterRecursionRule(_localctx, 80, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(478);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
			case 1:
				{
				_localctx = new PrimaryExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(465);
				primary();
				}
				break;
			case 2:
				{
				_localctx = new CastExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(466);
				match(LPAREN);
				setState(467);
				type();
				setState(468);
				match(RPAREN);
				setState(469);
				expression(15);
				}
				break;
			case 3:
				{
				_localctx = new WrappingExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(471);
				match(LPAREN);
				setState(472);
				expression(0);
				setState(473);
				match(RPAREN);
				}
				break;
			case 4:
				{
				_localctx = new UnaryOpContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(475);
				_la = _input.LA(1);
				if ( !(((((_la - 30)) & ~0x3f) == 0 && ((1L << (_la - 30)) & 107752139522049L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(476);
				expression(13);
				}
				break;
			case 5:
				{
				_localctx = new ForEachContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(477);
				loopExpresseion();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(554);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,55,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(552);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,54,_ctx) ) {
					case 1:
						{
						_localctx = new MultiplicativeOpContext(new ExpressionContext(_parentctx, _parentState));
						((MultiplicativeOpContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(480);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(481);
						_la = _input.LA(1);
						if ( !(((((_la - 73)) & ~0x3f) == 0 && ((1L << (_la - 73)) & 49L) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(482);
						((MultiplicativeOpContext)_localctx).right = expression(13);
						}
						break;
					case 2:
						{
						_localctx = new AdditiveOpContext(new ExpressionContext(_parentctx, _parentState));
						((AdditiveOpContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(483);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(484);
						_la = _input.LA(1);
						if ( !(_la==PLUS || _la==MINUS) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(485);
						((AdditiveOpContext)_localctx).right = expression(12);
						}
						break;
					case 3:
						{
						_localctx = new BitwiseOpContext(new ExpressionContext(_parentctx, _parentState));
						((BitwiseOpContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(486);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(487);
						_la = _input.LA(1);
						if ( !(((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 109L) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(488);
						((BitwiseOpContext)_localctx).right = expression(11);
						}
						break;
					case 4:
						{
						_localctx = new ComparisonOpContext(new ExpressionContext(_parentctx, _parentState));
						((ComparisonOpContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(489);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(490);
						_la = _input.LA(1);
						if ( !(((((_la - 86)) & ~0x3f) == 0 && ((1L << (_la - 86)) & 15L) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(491);
						((ComparisonOpContext)_localctx).right = expression(10);
						}
						break;
					case 5:
						{
						_localctx = new StringOpContext(new ExpressionContext(_parentctx, _parentState));
						((StringOpContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(492);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(493);
						_la = _input.LA(1);
						if ( !(((((_la - 92)) & ~0x3f) == 0 && ((1L << (_la - 92)) & 15L) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(494);
						((StringOpContext)_localctx).right = expression(9);
						}
						break;
					case 6:
						{
						_localctx = new EqualityCheckContext(new ExpressionContext(_parentctx, _parentState));
						((EqualityCheckContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(495);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(496);
						_la = _input.LA(1);
						if ( !(_la==EQ || _la==NOT_EQ) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(497);
						((EqualityCheckContext)_localctx).right = expression(8);
						}
						break;
					case 7:
						{
						_localctx = new ConjunctionContext(new ExpressionContext(_parentctx, _parentState));
						((ConjunctionContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(498);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(499);
						and();
						setState(500);
						((ConjunctionContext)_localctx).right = expression(6);
						}
						break;
					case 8:
						{
						_localctx = new DisjunctionContext(new ExpressionContext(_parentctx, _parentState));
						((DisjunctionContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(502);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(503);
						or();
						setState(504);
						((DisjunctionContext)_localctx).right = expression(5);
						}
						break;
					case 9:
						{
						_localctx = new SetPredicateContext(new ExpressionContext(_parentctx, _parentState));
						((SetPredicateContext)_localctx).source = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(506);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(508);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==ALL || _la==STAR) {
							{
							setState(507);
							all();
							}
						}

						setState(511);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==NOT || _la==EXMARK) {
							{
							setState(510);
							not();
							}
						}

						setState(513);
						match(IN);
						setState(514);
						((SetPredicateContext)_localctx).target = expression(4);
						}
						break;
					case 10:
						{
						_localctx = new TernaryOpContext(new ExpressionContext(_parentctx, _parentState));
						((TernaryOpContext)_localctx).condition = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(515);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(516);
						match(QMARK);
						setState(517);
						((TernaryOpContext)_localctx).optionTrue = expression(0);
						setState(518);
						match(COLON);
						setState(519);
						((TernaryOpContext)_localctx).optionFalse = expression(3);
						}
						break;
					case 11:
						{
						_localctx = new PathAccessContext(new ExpressionContext(_parentctx, _parentState));
						((PathAccessContext)_localctx).source = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(521);
						if (!(precpred(_ctx, 19))) throw new FailedPredicateException(this, "precpred(_ctx, 19)");
						setState(522);
						match(DOT);
						setState(523);
						match(Identifier);
						}
						break;
					case 12:
						{
						_localctx = new MethodInvocationContext(new ExpressionContext(_parentctx, _parentState));
						((MethodInvocationContext)_localctx).source = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(524);
						if (!(precpred(_ctx, 18))) throw new FailedPredicateException(this, "precpred(_ctx, 18)");
						setState(525);
						if (!(isAny(-1,Identifier))) throw new FailedPredicateException(this, "isAny(-1,Identifier)");
						setState(526);
						match(LPAREN);
						setState(528);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 183882327852711936L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 32212255635L) != 0)) {
							{
							setState(527);
							((MethodInvocationContext)_localctx).arguments = expressionList();
							}
						}

						setState(530);
						match(RPAREN);
						}
						break;
					case 13:
						{
						_localctx = new ListAccessContext(new ExpressionContext(_parentctx, _parentState));
						((ListAccessContext)_localctx).source = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(531);
						if (!(precpred(_ctx, 17))) throw new FailedPredicateException(this, "precpred(_ctx, 17)");
						setState(532);
						if (!(isAny(-1,Identifier,RPAREN,RBRACE,RBRACK))) throw new FailedPredicateException(this, "isAny(-1,Identifier,RPAREN,RBRACE,RBRACK)");
						setState(533);
						match(LBRACK);
						setState(534);
						((ListAccessContext)_localctx).indices = expressionList();
						setState(535);
						match(RBRACK);
						}
						break;
					case 14:
						{
						_localctx = new AnnotationAccessContext(new ExpressionContext(_parentctx, _parentState));
						((AnnotationAccessContext)_localctx).source = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(537);
						if (!(precpred(_ctx, 16))) throw new FailedPredicateException(this, "precpred(_ctx, 16)");
						setState(538);
						if (!(isAny(-1,Identifier,RPAREN,RBRACK))) throw new FailedPredicateException(this, "isAny(-1,Identifier,RPAREN,RBRACK)");
						setState(539);
						match(LBRACE);
						setState(540);
						((AnnotationAccessContext)_localctx).keys = expressionList();
						setState(541);
						match(RBRACE);
						}
						break;
					case 15:
						{
						_localctx = new AssignmentOpContext(new ExpressionContext(_parentctx, _parentState));
						((AssignmentOpContext)_localctx).source = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(543);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(544);
						match(AS);
						setState(546);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==OPTIONAL) {
							{
							setState(545);
							match(OPTIONAL);
							}
						}

						setState(550);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case DOLLAR:
							{
							setState(548);
							member();
							}
							break;
						case AT:
							{
							setState(549);
							variable();
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						}
						break;
					}
					} 
				}
				setState(556);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,55,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryContext extends ParserRuleContext {
		public NullLiteralContext nullLiteral() {
			return getRuleContext(NullLiteralContext.class,0);
		}
		public BooleanLiteralContext booleanLiteral() {
			return getRuleContext(BooleanLiteralContext.class,0);
		}
		public FloatingPointLiteralContext floatingPointLiteral() {
			return getRuleContext(FloatingPointLiteralContext.class,0);
		}
		public IntegerLiteralContext integerLiteral() {
			return getRuleContext(IntegerLiteralContext.class,0);
		}
		public TerminalNode StringLiteral() { return getToken(IQL_TestParser.StringLiteral, 0); }
		public ListStatementContext listStatement() {
			return getRuleContext(ListStatementContext.class,0);
		}
		public ReferenceContext reference() {
			return getRuleContext(ReferenceContext.class,0);
		}
		public PrimaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primary; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterPrimary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitPrimary(this);
		}
	}

	public final PrimaryContext primary() throws RecognitionException {
		PrimaryContext _localctx = new PrimaryContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_primary);
		try {
			setState(564);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,56,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(557);
				nullLiteral();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(558);
				booleanLiteral();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(559);
				floatingPointLiteral();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(560);
				integerLiteral();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(561);
				match(StringLiteral);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(562);
				listStatement();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(563);
				reference();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ListStatementContext extends ParserRuleContext {
		public TerminalNode LBRACE() { return getToken(IQL_TestParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(IQL_TestParser.RBRACE, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode LBRACK() { return getToken(IQL_TestParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(IQL_TestParser.RBRACK, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public ListStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_listStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterListStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitListStatement(this);
		}
	}

	public final ListStatementContext listStatement() throws RecognitionException {
		ListStatementContext _localctx = new ListStatementContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_listStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(570);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3729543441416192L) != 0)) {
				{
				setState(566);
				type();
				setState(567);
				match(LBRACK);
				setState(568);
				match(RBRACK);
				}
			}

			setState(572);
			match(LBRACE);
			setState(574);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 183882327852711936L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 32212255635L) != 0)) {
				{
				setState(573);
				expressionList();
				}
			}

			setState(576);
			match(RBRACE);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ReferenceContext extends ParserRuleContext {
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public MemberContext member() {
			return getRuleContext(MemberContext.class,0);
		}
		public TerminalNode Identifier() { return getToken(IQL_TestParser.Identifier, 0); }
		public QualifiedIdentifierContext qualifiedIdentifier() {
			return getRuleContext(QualifiedIdentifierContext.class,0);
		}
		public ReferenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_reference; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitReference(this);
		}
	}

	public final ReferenceContext reference() throws RecognitionException {
		ReferenceContext _localctx = new ReferenceContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_reference);
		try {
			setState(582);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,59,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(578);
				variable();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(579);
				member();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(580);
				match(Identifier);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(581);
				qualifiedIdentifier();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class QualifiedIdentifierContext extends ParserRuleContext {
		public Token hostId;
		public Token elementId;
		public TerminalNode DOUBLE_COLON() { return getToken(IQL_TestParser.DOUBLE_COLON, 0); }
		public List<TerminalNode> Identifier() { return getTokens(IQL_TestParser.Identifier); }
		public TerminalNode Identifier(int i) {
			return getToken(IQL_TestParser.Identifier, i);
		}
		public QualifiedIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterQualifiedIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitQualifiedIdentifier(this);
		}
	}

	public final QualifiedIdentifierContext qualifiedIdentifier() throws RecognitionException {
		QualifiedIdentifierContext _localctx = new QualifiedIdentifierContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_qualifiedIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(584);
			((QualifiedIdentifierContext)_localctx).hostId = match(Identifier);
			setState(585);
			match(DOUBLE_COLON);
			setState(586);
			((QualifiedIdentifierContext)_localctx).elementId = match(Identifier);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LoopExpresseionContext extends ParserRuleContext {
		public TerminalNode FOREACH() { return getToken(IQL_TestParser.FOREACH, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode AS() { return getToken(IQL_TestParser.AS, 0); }
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public LoopControlContext loopControl() {
			return getRuleContext(LoopControlContext.class,0);
		}
		public CounterListContext counterList() {
			return getRuleContext(CounterListContext.class,0);
		}
		public LoopExpresseionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_loopExpresseion; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterLoopExpresseion(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitLoopExpresseion(this);
		}
	}

	public final LoopExpresseionContext loopExpresseion() throws RecognitionException {
		LoopExpresseionContext _localctx = new LoopExpresseionContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_loopExpresseion);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(588);
			match(FOREACH);
			setState(589);
			expression(0);
			setState(590);
			match(AS);
			setState(591);
			variable();
			setState(592);
			loopControl();
			setState(594);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,60,_ctx) ) {
			case 1:
				{
				setState(593);
				counterList();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class LoopControlContext extends ParserRuleContext {
		public ConstraintContext omit;
		public BoundedRangeContext range;
		public BoundedRangeContext step;
		public ConstraintContext body;
		public TerminalNode END() { return getToken(IQL_TestParser.END, 0); }
		public TerminalNode OMIT() { return getToken(IQL_TestParser.OMIT, 0); }
		public TerminalNode RANGE() { return getToken(IQL_TestParser.RANGE, 0); }
		public TerminalNode STEP() { return getToken(IQL_TestParser.STEP, 0); }
		public TerminalNode DO() { return getToken(IQL_TestParser.DO, 0); }
		public TerminalNode EVEN() { return getToken(IQL_TestParser.EVEN, 0); }
		public TerminalNode ODD() { return getToken(IQL_TestParser.ODD, 0); }
		public List<ConstraintContext> constraint() {
			return getRuleContexts(ConstraintContext.class);
		}
		public ConstraintContext constraint(int i) {
			return getRuleContext(ConstraintContext.class,i);
		}
		public List<BoundedRangeContext> boundedRange() {
			return getRuleContexts(BoundedRangeContext.class);
		}
		public BoundedRangeContext boundedRange(int i) {
			return getRuleContext(BoundedRangeContext.class,i);
		}
		public LoopControlContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_loopControl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterLoopControl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitLoopControl(this);
		}
	}

	public final LoopControlContext loopControl() throws RecognitionException {
		LoopControlContext _localctx = new LoopControlContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_loopControl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(597);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EVEN || _la==ODD) {
				{
				setState(596);
				_la = _input.LA(1);
				if ( !(_la==EVEN || _la==ODD) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(601);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==OMIT) {
				{
				setState(599);
				match(OMIT);
				setState(600);
				((LoopControlContext)_localctx).omit = constraint();
				}
			}

			setState(605);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RANGE) {
				{
				setState(603);
				match(RANGE);
				setState(604);
				((LoopControlContext)_localctx).range = boundedRange();
				}
			}

			setState(609);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==STEP) {
				{
				setState(607);
				match(STEP);
				setState(608);
				((LoopControlContext)_localctx).step = boundedRange();
				}
			}

			setState(613);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DO) {
				{
				setState(611);
				match(DO);
				setState(612);
				((LoopControlContext)_localctx).body = constraint();
				}
			}

			setState(615);
			match(END);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BoundedRangeContext extends ParserRuleContext {
		public ExpressionContext from;
		public ExpressionContext to;
		public TerminalNode LPAREN() { return getToken(IQL_TestParser.LPAREN, 0); }
		public TerminalNode COMMA() { return getToken(IQL_TestParser.COMMA, 0); }
		public TerminalNode RPAREN() { return getToken(IQL_TestParser.RPAREN, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public BoundedRangeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_boundedRange; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterBoundedRange(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitBoundedRange(this);
		}
	}

	public final BoundedRangeContext boundedRange() throws RecognitionException {
		BoundedRangeContext _localctx = new BoundedRangeContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_boundedRange);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(617);
			match(LPAREN);
			setState(618);
			((BoundedRangeContext)_localctx).from = expression(0);
			setState(619);
			match(COMMA);
			setState(620);
			((BoundedRangeContext)_localctx).to = expression(0);
			setState(621);
			match(RPAREN);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CounterListContext extends ParserRuleContext {
		public TerminalNode COUNT() { return getToken(IQL_TestParser.COUNT, 0); }
		public List<CounterContext> counter() {
			return getRuleContexts(CounterContext.class);
		}
		public CounterContext counter(int i) {
			return getRuleContext(CounterContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(IQL_TestParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(IQL_TestParser.COMMA, i);
		}
		public CounterListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_counterList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterCounterList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitCounterList(this);
		}
	}

	public final CounterListContext counterList() throws RecognitionException {
		CounterListContext _localctx = new CounterListContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_counterList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(623);
			match(COUNT);
			setState(624);
			counter();
			setState(629);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,66,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(625);
					match(COMMA);
					setState(626);
					counter();
					}
					} 
				}
				setState(631);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,66,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class CounterContext extends ParserRuleContext {
		public ConstraintContext constraint() {
			return getRuleContext(ConstraintContext.class,0);
		}
		public TerminalNode AS() { return getToken(IQL_TestParser.AS, 0); }
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public CounterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_counter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterCounter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitCounter(this);
		}
	}

	public final CounterContext counter() throws RecognitionException {
		CounterContext _localctx = new CounterContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_counter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(632);
			constraint();
			setState(633);
			match(AS);
			setState(634);
			variable();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class TypeContext extends ParserRuleContext {
		public TerminalNode BOOLEAN() { return getToken(IQL_TestParser.BOOLEAN, 0); }
		public TerminalNode STRING() { return getToken(IQL_TestParser.STRING, 0); }
		public TerminalNode INT() { return getToken(IQL_TestParser.INT, 0); }
		public TerminalNode FLOAT() { return getToken(IQL_TestParser.FLOAT, 0); }
		public TypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitType(this);
		}
	}

	public final TypeContext type() throws RecognitionException {
		TypeContext _localctx = new TypeContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_type);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(636);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 3729543441416192L) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class QuantifierContext extends ParserRuleContext {
		public List<SimpleQuantifierContext> simpleQuantifier() {
			return getRuleContexts(SimpleQuantifierContext.class);
		}
		public SimpleQuantifierContext simpleQuantifier(int i) {
			return getRuleContext(SimpleQuantifierContext.class,i);
		}
		public List<TerminalNode> PIPE() { return getTokens(IQL_TestParser.PIPE); }
		public TerminalNode PIPE(int i) {
			return getToken(IQL_TestParser.PIPE, i);
		}
		public TerminalNode LT() { return getToken(IQL_TestParser.LT, 0); }
		public TerminalNode GT() { return getToken(IQL_TestParser.GT, 0); }
		public QuantifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_quantifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterQuantifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitQuantifier(this);
		}
	}

	public final QuantifierContext quantifier() throws RecognitionException {
		QuantifierContext _localctx = new QuantifierContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_quantifier);
		int _la;
		try {
			setState(657);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ALL:
			case NOT:
			case EXMARK:
			case STAR:
			case PureDigits:
				enterOuterAlt(_localctx, 1);
				{
				setState(638);
				simpleQuantifier();
				setState(643);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==PIPE) {
					{
					{
					setState(639);
					match(PIPE);
					setState(640);
					simpleQuantifier();
					}
					}
					setState(645);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case LT:
				enterOuterAlt(_localctx, 2);
				{
				setState(646);
				match(LT);
				setState(647);
				simpleQuantifier();
				setState(652);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==PIPE) {
					{
					{
					setState(648);
					match(PIPE);
					setState(649);
					simpleQuantifier();
					}
					}
					setState(654);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(655);
				match(GT);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SimpleQuantifierContext extends ParserRuleContext {
		public Token value;
		public Token lowerBound;
		public Token upperBound;
		public NotContext not() {
			return getRuleContext(NotContext.class,0);
		}
		public AllContext all() {
			return getRuleContext(AllContext.class,0);
		}
		public TerminalNode QMARK() { return getToken(IQL_TestParser.QMARK, 0); }
		public TerminalNode PLUS() { return getToken(IQL_TestParser.PLUS, 0); }
		public List<TerminalNode> PureDigits() { return getTokens(IQL_TestParser.PureDigits); }
		public TerminalNode PureDigits(int i) {
			return getToken(IQL_TestParser.PureDigits, i);
		}
		public TerminalNode CARET() { return getToken(IQL_TestParser.CARET, 0); }
		public TerminalNode MINUS() { return getToken(IQL_TestParser.MINUS, 0); }
		public TerminalNode EXMARK() { return getToken(IQL_TestParser.EXMARK, 0); }
		public TerminalNode DOUBLE_DOT() { return getToken(IQL_TestParser.DOUBLE_DOT, 0); }
		public SimpleQuantifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleQuantifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterSimpleQuantifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitSimpleQuantifier(this);
		}
	}

	public final SimpleQuantifierContext simpleQuantifier() throws RecognitionException {
		SimpleQuantifierContext _localctx = new SimpleQuantifierContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_simpleQuantifier);
		int _la;
		try {
			setState(683);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,76,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(659);
				not();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(660);
				all();
				setState(662);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==QMARK || _la==PLUS) {
					{
					setState(661);
					_la = _input.LA(1);
					if ( !(_la==QMARK || _la==PLUS) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(664);
				((SimpleQuantifierContext)_localctx).value = match(PureDigits);
				setState(666);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PLUS || _la==MINUS) {
					{
					setState(665);
					_la = _input.LA(1);
					if ( !(_la==PLUS || _la==MINUS) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(669);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==QMARK || _la==EXMARK) {
					{
					setState(668);
					_la = _input.LA(1);
					if ( !(_la==QMARK || _la==EXMARK) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(672);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CARET) {
					{
					setState(671);
					match(CARET);
					}
				}

				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(674);
				((SimpleQuantifierContext)_localctx).lowerBound = match(PureDigits);
				setState(675);
				match(DOUBLE_DOT);
				setState(676);
				((SimpleQuantifierContext)_localctx).upperBound = match(PureDigits);
				setState(678);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==QMARK || _la==EXMARK) {
					{
					setState(677);
					_la = _input.LA(1);
					if ( !(_la==QMARK || _la==EXMARK) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					}
				}

				setState(681);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CARET) {
					{
					setState(680);
					match(CARET);
					}
				}

				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NotContext extends ParserRuleContext {
		public TerminalNode NOT() { return getToken(IQL_TestParser.NOT, 0); }
		public TerminalNode EXMARK() { return getToken(IQL_TestParser.EXMARK, 0); }
		public NotContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_not; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterNot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitNot(this);
		}
	}

	public final NotContext not() throws RecognitionException {
		NotContext _localctx = new NotContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_not);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(685);
			_la = _input.LA(1);
			if ( !(_la==NOT || _la==EXMARK) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AllContext extends ParserRuleContext {
		public TerminalNode STAR() { return getToken(IQL_TestParser.STAR, 0); }
		public TerminalNode ALL() { return getToken(IQL_TestParser.ALL, 0); }
		public AllContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_all; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterAll(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitAll(this);
		}
	}

	public final AllContext all() throws RecognitionException {
		AllContext _localctx = new AllContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_all);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(687);
			_la = _input.LA(1);
			if ( !(_la==ALL || _la==STAR) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class AndContext extends ParserRuleContext {
		public TerminalNode AND() { return getToken(IQL_TestParser.AND, 0); }
		public TerminalNode DOUBLE_AMP() { return getToken(IQL_TestParser.DOUBLE_AMP, 0); }
		public AndContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_and; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterAnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitAnd(this);
		}
	}

	public final AndContext and() throws RecognitionException {
		AndContext _localctx = new AndContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_and);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(689);
			_la = _input.LA(1);
			if ( !(_la==AND || _la==DOUBLE_AMP) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class OrContext extends ParserRuleContext {
		public TerminalNode OR() { return getToken(IQL_TestParser.OR, 0); }
		public TerminalNode DOUBLE_PIPE() { return getToken(IQL_TestParser.DOUBLE_PIPE, 0); }
		public OrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_or; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterOr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitOr(this);
		}
	}

	public final OrContext or() throws RecognitionException {
		OrContext _localctx = new OrContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_or);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(691);
			_la = _input.LA(1);
			if ( !(_la==OR || _la==DOUBLE_PIPE) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VariableContext extends ParserRuleContext {
		public TerminalNode AT() { return getToken(IQL_TestParser.AT, 0); }
		public TerminalNode Identifier() { return getToken(IQL_TestParser.Identifier, 0); }
		public VariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitVariable(this);
		}
	}

	public final VariableContext variable() throws RecognitionException {
		VariableContext _localctx = new VariableContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_variable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(693);
			match(AT);
			setState(694);
			match(Identifier);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class MemberContext extends ParserRuleContext {
		public TerminalNode DOLLAR() { return getToken(IQL_TestParser.DOLLAR, 0); }
		public TerminalNode Identifier() { return getToken(IQL_TestParser.Identifier, 0); }
		public MemberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_member; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterMember(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitMember(this);
		}
	}

	public final MemberContext member() throws RecognitionException {
		MemberContext _localctx = new MemberContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_member);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(696);
			match(DOLLAR);
			setState(698);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,77,_ctx) ) {
			case 1:
				{
				setState(697);
				match(Identifier);
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class VersionDeclarationContext extends ParserRuleContext {
		public Token major;
		public Token minor;
		public Token build;
		public Token suffix;
		public List<TerminalNode> PureDigits() { return getTokens(IQL_TestParser.PureDigits); }
		public TerminalNode PureDigits(int i) {
			return getToken(IQL_TestParser.PureDigits, i);
		}
		public List<TerminalNode> DOT() { return getTokens(IQL_TestParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(IQL_TestParser.DOT, i);
		}
		public TerminalNode MINUS() { return getToken(IQL_TestParser.MINUS, 0); }
		public TerminalNode UNDERSCORE() { return getToken(IQL_TestParser.UNDERSCORE, 0); }
		public TerminalNode COLON() { return getToken(IQL_TestParser.COLON, 0); }
		public TerminalNode Identifier() { return getToken(IQL_TestParser.Identifier, 0); }
		public VersionDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_versionDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterVersionDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitVersionDeclaration(this);
		}
	}

	public final VersionDeclarationContext versionDeclaration() throws RecognitionException {
		VersionDeclarationContext _localctx = new VersionDeclarationContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_versionDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(700);
			((VersionDeclarationContext)_localctx).major = match(PureDigits);
			setState(703);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,78,_ctx) ) {
			case 1:
				{
				setState(701);
				match(DOT);
				setState(702);
				((VersionDeclarationContext)_localctx).minor = match(PureDigits);
				}
				break;
			}
			setState(707);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DOT) {
				{
				setState(705);
				match(DOT);
				setState(706);
				((VersionDeclarationContext)_localctx).build = match(PureDigits);
				}
			}

			setState(710);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 53)) & ~0x3f) == 0 && ((1L << (_la - 53)) & 4194817L) != 0)) {
				{
				setState(709);
				_la = _input.LA(1);
				if ( !(((((_la - 53)) & ~0x3f) == 0 && ((1L << (_la - 53)) & 4194817L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(713);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Identifier) {
				{
				setState(712);
				((VersionDeclarationContext)_localctx).suffix = match(Identifier);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class NullLiteralContext extends ParserRuleContext {
		public TerminalNode NULL() { return getToken(IQL_TestParser.NULL, 0); }
		public NullLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nullLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterNullLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitNullLiteral(this);
		}
	}

	public final NullLiteralContext nullLiteral() throws RecognitionException {
		NullLiteralContext _localctx = new NullLiteralContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_nullLiteral);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(715);
			match(NULL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FloatingPointLiteralContext extends ParserRuleContext {
		public SignedFloatingPointLiteralContext signedFloatingPointLiteral() {
			return getRuleContext(SignedFloatingPointLiteralContext.class,0);
		}
		public FloatingPointLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_floatingPointLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterFloatingPointLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitFloatingPointLiteral(this);
		}
	}

	public final FloatingPointLiteralContext floatingPointLiteral() throws RecognitionException {
		FloatingPointLiteralContext _localctx = new FloatingPointLiteralContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_floatingPointLiteral);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(717);
			signedFloatingPointLiteral();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SignedFloatingPointLiteralContext extends ParserRuleContext {
		public UnsignedFloatingPointLiteralContext unsignedFloatingPointLiteral() {
			return getRuleContext(UnsignedFloatingPointLiteralContext.class,0);
		}
		public SignContext sign() {
			return getRuleContext(SignContext.class,0);
		}
		public SignedFloatingPointLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_signedFloatingPointLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterSignedFloatingPointLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitSignedFloatingPointLiteral(this);
		}
	}

	public final SignedFloatingPointLiteralContext signedFloatingPointLiteral() throws RecognitionException {
		SignedFloatingPointLiteralContext _localctx = new SignedFloatingPointLiteralContext(_ctx, getState());
		enterRule(_localctx, 124, RULE_signedFloatingPointLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(720);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PLUS || _la==MINUS) {
				{
				setState(719);
				sign();
				}
			}

			setState(722);
			unsignedFloatingPointLiteral();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UnsignedFloatingPointLiteralContext extends ParserRuleContext {
		public List<UnsignedIntegerLiteralContext> unsignedIntegerLiteral() {
			return getRuleContexts(UnsignedIntegerLiteralContext.class);
		}
		public UnsignedIntegerLiteralContext unsignedIntegerLiteral(int i) {
			return getRuleContext(UnsignedIntegerLiteralContext.class,i);
		}
		public TerminalNode DOT() { return getToken(IQL_TestParser.DOT, 0); }
		public UnsignedFloatingPointLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unsignedFloatingPointLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterUnsignedFloatingPointLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitUnsignedFloatingPointLiteral(this);
		}
	}

	public final UnsignedFloatingPointLiteralContext unsignedFloatingPointLiteral() throws RecognitionException {
		UnsignedFloatingPointLiteralContext _localctx = new UnsignedFloatingPointLiteralContext(_ctx, getState());
		enterRule(_localctx, 126, RULE_unsignedFloatingPointLiteral);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(724);
			unsignedIntegerLiteral();
			setState(725);
			match(DOT);
			setState(726);
			unsignedIntegerLiteral();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class BooleanLiteralContext extends ParserRuleContext {
		public TerminalNode TRUE() { return getToken(IQL_TestParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(IQL_TestParser.FALSE, 0); }
		public BooleanLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterBooleanLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitBooleanLiteral(this);
		}
	}

	public final BooleanLiteralContext booleanLiteral() throws RecognitionException {
		BooleanLiteralContext _localctx = new BooleanLiteralContext(_ctx, getState());
		enterRule(_localctx, 128, RULE_booleanLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(728);
			_la = _input.LA(1);
			if ( !(_la==FALSE || _la==TRUE) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class IntegerLiteralContext extends ParserRuleContext {
		public SignedIntegerLiteralContext signedIntegerLiteral() {
			return getRuleContext(SignedIntegerLiteralContext.class,0);
		}
		public IntegerLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integerLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterIntegerLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitIntegerLiteral(this);
		}
	}

	public final IntegerLiteralContext integerLiteral() throws RecognitionException {
		IntegerLiteralContext _localctx = new IntegerLiteralContext(_ctx, getState());
		enterRule(_localctx, 130, RULE_integerLiteral);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(730);
			signedIntegerLiteral();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SignedIntegerLiteralContext extends ParserRuleContext {
		public UnsignedIntegerLiteralContext unsignedIntegerLiteral() {
			return getRuleContext(UnsignedIntegerLiteralContext.class,0);
		}
		public SignContext sign() {
			return getRuleContext(SignContext.class,0);
		}
		public SignedIntegerLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_signedIntegerLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterSignedIntegerLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitSignedIntegerLiteral(this);
		}
	}

	public final SignedIntegerLiteralContext signedIntegerLiteral() throws RecognitionException {
		SignedIntegerLiteralContext _localctx = new SignedIntegerLiteralContext(_ctx, getState());
		enterRule(_localctx, 132, RULE_signedIntegerLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(733);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PLUS || _la==MINUS) {
				{
				setState(732);
				sign();
				}
			}

			setState(735);
			unsignedIntegerLiteral();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class UnsignedIntegerLiteralContext extends ParserRuleContext {
		public TerminalNode Digits() { return getToken(IQL_TestParser.Digits, 0); }
		public TerminalNode PureDigits() { return getToken(IQL_TestParser.PureDigits, 0); }
		public UnsignedIntegerLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unsignedIntegerLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterUnsignedIntegerLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitUnsignedIntegerLiteral(this);
		}
	}

	public final UnsignedIntegerLiteralContext unsignedIntegerLiteral() throws RecognitionException {
		UnsignedIntegerLiteralContext _localctx = new UnsignedIntegerLiteralContext(_ctx, getState());
		enterRule(_localctx, 134, RULE_unsignedIntegerLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(737);
			_la = _input.LA(1);
			if ( !(_la==PureDigits || _la==Digits) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class SignContext extends ParserRuleContext {
		public TerminalNode PLUS() { return getToken(IQL_TestParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(IQL_TestParser.MINUS, 0); }
		public SignContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sign; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).enterSign(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQL_TestListener ) ((IQL_TestListener)listener).exitSign(this);
		}
	}

	public final SignContext sign() throws RecognitionException {
		SignContext _localctx = new SignContext(_ctx, getState());
		enterRule(_localctx, 136, RULE_sign);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(739);
			_la = _input.LA(1);
			if ( !(_la==PLUS || _la==MINUS) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 20:
			return nodeStatement_sempred((NodeStatementContext)_localctx, predIndex);
		case 24:
			return positionMarker_sempred((PositionMarkerContext)_localctx, predIndex);
		case 40:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean nodeStatement_sempred(NodeStatementContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean positionMarker_sempred(PositionMarkerContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return precpred(_ctx, 2);
		case 2:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 3:
			return precpred(_ctx, 12);
		case 4:
			return precpred(_ctx, 11);
		case 5:
			return precpred(_ctx, 10);
		case 6:
			return precpred(_ctx, 9);
		case 7:
			return precpred(_ctx, 8);
		case 8:
			return precpred(_ctx, 7);
		case 9:
			return precpred(_ctx, 6);
		case 10:
			return precpred(_ctx, 5);
		case 11:
			return precpred(_ctx, 3);
		case 12:
			return precpred(_ctx, 2);
		case 13:
			return precpred(_ctx, 19);
		case 14:
			return precpred(_ctx, 18);
		case 15:
			return isAny(-1,Identifier);
		case 16:
			return precpred(_ctx, 17);
		case 17:
			return isAny(-1,Identifier,RPAREN,RBRACE,RBRACK);
		case 18:
			return precpred(_ctx, 16);
		case 19:
			return isAny(-1,Identifier,RPAREN,RBRACK);
		case 20:
			return precpred(_ctx, 4);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001h\u02e6\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002\u000b\u0007\u000b\u0002"+
		"\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e\u0002\u000f\u0007\u000f"+
		"\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011\u0002\u0012\u0007\u0012"+
		"\u0002\u0013\u0007\u0013\u0002\u0014\u0007\u0014\u0002\u0015\u0007\u0015"+
		"\u0002\u0016\u0007\u0016\u0002\u0017\u0007\u0017\u0002\u0018\u0007\u0018"+
		"\u0002\u0019\u0007\u0019\u0002\u001a\u0007\u001a\u0002\u001b\u0007\u001b"+
		"\u0002\u001c\u0007\u001c\u0002\u001d\u0007\u001d\u0002\u001e\u0007\u001e"+
		"\u0002\u001f\u0007\u001f\u0002 \u0007 \u0002!\u0007!\u0002\"\u0007\"\u0002"+
		"#\u0007#\u0002$\u0007$\u0002%\u0007%\u0002&\u0007&\u0002\'\u0007\'\u0002"+
		"(\u0007(\u0002)\u0007)\u0002*\u0007*\u0002+\u0007+\u0002,\u0007,\u0002"+
		"-\u0007-\u0002.\u0007.\u0002/\u0007/\u00020\u00070\u00021\u00071\u0002"+
		"2\u00072\u00023\u00073\u00024\u00074\u00025\u00075\u00026\u00076\u0002"+
		"7\u00077\u00028\u00078\u00029\u00079\u0002:\u0007:\u0002;\u0007;\u0002"+
		"<\u0007<\u0002=\u0007=\u0002>\u0007>\u0002?\u0007?\u0002@\u0007@\u0002"+
		"A\u0007A\u0002B\u0007B\u0002C\u0007C\u0002D\u0007D\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001"+
		"\b\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\n\u0001\u000b\u0001\u000b"+
		"\u0001\u000b\u0003\u000b\u00af\b\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0003\u000b\u00b4\b\u000b\u0001\u000b\u0001\u000b\u0001\u000b\u0001\u000b"+
		"\u0003\u000b\u00ba\b\u000b\u0001\f\u0001\f\u0001\f\u0001\f\u0005\f\u00c0"+
		"\b\f\n\f\f\f\u00c3\t\f\u0001\r\u0003\r\u00c6\b\r\u0001\r\u0001\r\u0001"+
		"\r\u0005\r\u00cb\b\r\n\r\f\r\u00ce\t\r\u0001\r\u0001\r\u0001\r\u0001\u000e"+
		"\u0001\u000e\u0001\u000e\u0003\u000e\u00d6\b\u000e\u0001\u000e\u0001\u000e"+
		"\u0003\u000e\u00da\b\u000e\u0003\u000e\u00dc\b\u000e\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0005\u000f\u00e1\b\u000f\n\u000f\f\u000f\u00e4\t\u000f"+
		"\u0001\u0010\u0001\u0010\u0001\u0010\u0001\u0010\u0003\u0010\u00ea\b\u0010"+
		"\u0001\u0010\u0001\u0010\u0001\u0011\u0003\u0011\u00ef\b\u0011\u0001\u0011"+
		"\u0005\u0011\u00f2\b\u0011\n\u0011\f\u0011\u00f5\t\u0011\u0001\u0011\u0004"+
		"\u0011\u00f8\b\u0011\u000b\u0011\f\u0011\u00f9\u0001\u0012\u0001\u0012"+
		"\u0001\u0012\u0003\u0012\u00ff\b\u0012\u0001\u0013\u0001\u0013\u0001\u0014"+
		"\u0001\u0014\u0003\u0014\u0105\b\u0014\u0001\u0014\u0001\u0014\u0004\u0014"+
		"\u0109\b\u0014\u000b\u0014\f\u0014\u010a\u0001\u0014\u0001\u0014\u0001"+
		"\u0014\u0004\u0014\u0110\b\u0014\u000b\u0014\f\u0014\u0111\u0001\u0014"+
		"\u0004\u0014\u0115\b\u0014\u000b\u0014\f\u0014\u0116\u0001\u0014\u0001"+
		"\u0014\u0001\u0014\u0001\u0014\u0005\u0014\u011d\b\u0014\n\u0014\f\u0014"+
		"\u0120\t\u0014\u0003\u0014\u0122\b\u0014\u0001\u0014\u0001\u0014\u0001"+
		"\u0014\u0001\u0014\u0005\u0014\u0128\b\u0014\n\u0014\f\u0014\u012b\t\u0014"+
		"\u0001\u0015\u0001\u0015\u0001\u0016\u0001\u0016\u0001\u0016\u0001\u0016"+
		"\u0003\u0016\u0133\b\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u0137\b"+
		"\u0016\u0001\u0016\u0001\u0016\u0001\u0016\u0003\u0016\u013c\b\u0016\u0001"+
		"\u0016\u0003\u0016\u013f\b\u0016\u0001\u0016\u0003\u0016\u0142\b\u0016"+
		"\u0001\u0016\u0003\u0016\u0145\b\u0016\u0001\u0017\u0001\u0017\u0001\u0017"+
		"\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018"+
		"\u0005\u0018\u0150\b\u0018\n\u0018\f\u0018\u0153\t\u0018\u0001\u0018\u0001"+
		"\u0018\u0003\u0018\u0157\b\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0003\u0018\u015d\b\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001"+
		"\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0001\u0018\u0005\u0018\u0167"+
		"\b\u0018\n\u0018\f\u0018\u016a\t\u0018\u0001\u0019\u0001\u0019\u0003\u0019"+
		"\u016e\b\u0019\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a\u0001\u001a"+
		"\u0003\u001a\u0175\b\u001a\u0001\u001b\u0001\u001b\u0001\u001b\u0001\u001b"+
		"\u0003\u001b\u017b\b\u001b\u0001\u001b\u0003\u001b\u017e\b\u001b\u0001"+
		"\u001b\u0001\u001b\u0001\u001b\u0003\u001b\u0183\b\u001b\u0001\u001c\u0001"+
		"\u001c\u0003\u001c\u0187\b\u001c\u0001\u001d\u0001\u001d\u0003\u001d\u018b"+
		"\b\u001d\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001f\u0001\u001f\u0001"+
		"\u001f\u0001 \u0001 \u0001 \u0001!\u0001!\u0001!\u0001!\u0005!\u019a\b"+
		"!\n!\f!\u019d\t!\u0001!\u0001!\u0001\"\u0001\"\u0001\"\u0001\"\u0001\""+
		"\u0003\"\u01a6\b\"\u0001\"\u0001\"\u0001\"\u0001\"\u0003\"\u01ac\b\"\u0001"+
		"#\u0001#\u0003#\u01b0\b#\u0001#\u0003#\u01b3\b#\u0001#\u0003#\u01b6\b"+
		"#\u0001#\u0001#\u0001$\u0001$\u0001$\u0001$\u0001$\u0005$\u01bf\b$\n$"+
		"\f$\u01c2\t$\u0001%\u0001%\u0001%\u0001&\u0001&\u0001\'\u0001\'\u0001"+
		"\'\u0005\'\u01cc\b\'\n\'\f\'\u01cf\t\'\u0001(\u0001(\u0001(\u0001(\u0001"+
		"(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0003"+
		"(\u01df\b(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001"+
		"(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001"+
		"(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0003"+
		"(\u01fd\b(\u0001(\u0003(\u0200\b(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001"+
		"(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0003"+
		"(\u0211\b(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001"+
		"(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0001(\u0003(\u0223\b(\u0001"+
		"(\u0001(\u0003(\u0227\b(\u0005(\u0229\b(\n(\f(\u022c\t(\u0001)\u0001)"+
		"\u0001)\u0001)\u0001)\u0001)\u0001)\u0003)\u0235\b)\u0001*\u0001*\u0001"+
		"*\u0001*\u0003*\u023b\b*\u0001*\u0001*\u0003*\u023f\b*\u0001*\u0001*\u0001"+
		"+\u0001+\u0001+\u0001+\u0003+\u0247\b+\u0001,\u0001,\u0001,\u0001,\u0001"+
		"-\u0001-\u0001-\u0001-\u0001-\u0001-\u0003-\u0253\b-\u0001.\u0003.\u0256"+
		"\b.\u0001.\u0001.\u0003.\u025a\b.\u0001.\u0001.\u0003.\u025e\b.\u0001"+
		".\u0001.\u0003.\u0262\b.\u0001.\u0001.\u0003.\u0266\b.\u0001.\u0001.\u0001"+
		"/\u0001/\u0001/\u0001/\u0001/\u0001/\u00010\u00010\u00010\u00010\u0005"+
		"0\u0274\b0\n0\f0\u0277\t0\u00011\u00011\u00011\u00011\u00012\u00012\u0001"+
		"3\u00013\u00013\u00053\u0282\b3\n3\f3\u0285\t3\u00013\u00013\u00013\u0001"+
		"3\u00053\u028b\b3\n3\f3\u028e\t3\u00013\u00013\u00033\u0292\b3\u00014"+
		"\u00014\u00014\u00034\u0297\b4\u00014\u00014\u00034\u029b\b4\u00014\u0003"+
		"4\u029e\b4\u00014\u00034\u02a1\b4\u00014\u00014\u00014\u00014\u00034\u02a7"+
		"\b4\u00014\u00034\u02aa\b4\u00034\u02ac\b4\u00015\u00015\u00016\u0001"+
		"6\u00017\u00017\u00018\u00018\u00019\u00019\u00019\u0001:\u0001:\u0003"+
		":\u02bb\b:\u0001;\u0001;\u0001;\u0003;\u02c0\b;\u0001;\u0001;\u0003;\u02c4"+
		"\b;\u0001;\u0003;\u02c7\b;\u0001;\u0003;\u02ca\b;\u0001<\u0001<\u0001"+
		"=\u0001=\u0001>\u0003>\u02d1\b>\u0001>\u0001>\u0001?\u0001?\u0001?\u0001"+
		"?\u0001@\u0001@\u0001A\u0001A\u0001B\u0003B\u02de\bB\u0001B\u0001B\u0001"+
		"C\u0001C\u0001D\u0001D\u0001D\u0000\u0003(0PE\u0000\u0002\u0004\u0006"+
		"\b\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,."+
		"02468:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvxz|~\u0080\u0082\u0084\u0086\u0088"+
		"\u0000\u0018\u0002\u0000\f\f\u000e\u000e\u0003\u0000\u0007\u0007\u000b"+
		"\u000b()\u0003\u0000\u0001\u0001&&,,\u0002\u0000FFIJ\u0001\u0000?B\u0002"+
		"\u0000\u0005\u0005\n\n\u0003\u0000\u001e\u001eGGKL\u0002\u0000IIMN\u0001"+
		"\u0000JK\u0003\u0000OOQRTU\u0001\u0000VY\u0001\u0000\\_\u0001\u0000Z["+
		"\u0002\u0000\u0010\u0010  \u0003\u0000..0023\u0002\u0000FFJJ\u0001\u0000"+
		"FG\u0002\u0000\u001e\u001eGG\u0002\u0000\u0002\u0002II\u0002\u0000\u0003"+
		"\u0003PP\u0002\u0000$$SS\u0003\u000055>>KK\u0002\u0000\u0011\u0011++\u0001"+
		"\u0000bc\u030f\u0000\u008a\u0001\u0000\u0000\u0000\u0002\u008d\u0001\u0000"+
		"\u0000\u0000\u0004\u0090\u0001\u0000\u0000\u0000\u0006\u0093\u0001\u0000"+
		"\u0000\u0000\b\u0096\u0001\u0000\u0000\u0000\n\u0099\u0001\u0000\u0000"+
		"\u0000\f\u009c\u0001\u0000\u0000\u0000\u000e\u009f\u0001\u0000\u0000\u0000"+
		"\u0010\u00a2\u0001\u0000\u0000\u0000\u0012\u00a5\u0001\u0000\u0000\u0000"+
		"\u0014\u00a8\u0001\u0000\u0000\u0000\u0016\u00b9\u0001\u0000\u0000\u0000"+
		"\u0018\u00bb\u0001\u0000\u0000\u0000\u001a\u00c5\u0001\u0000\u0000\u0000"+
		"\u001c\u00db\u0001\u0000\u0000\u0000\u001e\u00dd\u0001\u0000\u0000\u0000"+
		" \u00e5\u0001\u0000\u0000\u0000\"\u00ee\u0001\u0000\u0000\u0000$\u00fe"+
		"\u0001\u0000\u0000\u0000&\u0100\u0001\u0000\u0000\u0000(\u0121\u0001\u0000"+
		"\u0000\u0000*\u012c\u0001\u0000\u0000\u0000,\u0144\u0001\u0000\u0000\u0000"+
		".\u0146\u0001\u0000\u0000\u00000\u015c\u0001\u0000\u0000\u00002\u016d"+
		"\u0001\u0000\u0000\u00004\u0174\u0001\u0000\u0000\u00006\u0182\u0001\u0000"+
		"\u0000\u00008\u0186\u0001\u0000\u0000\u0000:\u018a\u0001\u0000\u0000\u0000"+
		"<\u018c\u0001\u0000\u0000\u0000>\u018f\u0001\u0000\u0000\u0000@\u0192"+
		"\u0001\u0000\u0000\u0000B\u0195\u0001\u0000\u0000\u0000D\u01a0\u0001\u0000"+
		"\u0000\u0000F\u01b2\u0001\u0000\u0000\u0000H\u01b9\u0001\u0000\u0000\u0000"+
		"J\u01c3\u0001\u0000\u0000\u0000L\u01c6\u0001\u0000\u0000\u0000N\u01c8"+
		"\u0001\u0000\u0000\u0000P\u01de\u0001\u0000\u0000\u0000R\u0234\u0001\u0000"+
		"\u0000\u0000T\u023a\u0001\u0000\u0000\u0000V\u0246\u0001\u0000\u0000\u0000"+
		"X\u0248\u0001\u0000\u0000\u0000Z\u024c\u0001\u0000\u0000\u0000\\\u0255"+
		"\u0001\u0000\u0000\u0000^\u0269\u0001\u0000\u0000\u0000`\u026f\u0001\u0000"+
		"\u0000\u0000b\u0278\u0001\u0000\u0000\u0000d\u027c\u0001\u0000\u0000\u0000"+
		"f\u0291\u0001\u0000\u0000\u0000h\u02ab\u0001\u0000\u0000\u0000j\u02ad"+
		"\u0001\u0000\u0000\u0000l\u02af\u0001\u0000\u0000\u0000n\u02b1\u0001\u0000"+
		"\u0000\u0000p\u02b3\u0001\u0000\u0000\u0000r\u02b5\u0001\u0000\u0000\u0000"+
		"t\u02b8\u0001\u0000\u0000\u0000v\u02bc\u0001\u0000\u0000\u0000x\u02cb"+
		"\u0001\u0000\u0000\u0000z\u02cd\u0001\u0000\u0000\u0000|\u02d0\u0001\u0000"+
		"\u0000\u0000~\u02d4\u0001\u0000\u0000\u0000\u0080\u02d8\u0001\u0000\u0000"+
		"\u0000\u0082\u02da\u0001\u0000\u0000\u0000\u0084\u02dd\u0001\u0000\u0000"+
		"\u0000\u0086\u02e1\u0001\u0000\u0000\u0000\u0088\u02e3\u0001\u0000\u0000"+
		"\u0000\u008a\u008b\u0003v;\u0000\u008b\u008c\u0005\u0000\u0000\u0001\u008c"+
		"\u0001\u0001\u0000\u0000\u0000\u008d\u008e\u0003f3\u0000\u008e\u008f\u0005"+
		"\u0000\u0000\u0001\u008f\u0003\u0001\u0000\u0000\u0000\u0090\u0091\u0003"+
		"h4\u0000\u0091\u0092\u0005\u0000\u0000\u0001\u0092\u0005\u0001\u0000\u0000"+
		"\u0000\u0093\u0094\u0003\u0082A\u0000\u0094\u0095\u0005\u0000\u0000\u0001"+
		"\u0095\u0007\u0001\u0000\u0000\u0000\u0096\u0097\u0003\u0086C\u0000\u0097"+
		"\u0098\u0005\u0000\u0000\u0001\u0098\t\u0001\u0000\u0000\u0000\u0099\u009a"+
		"\u0003z=\u0000\u009a\u009b\u0005\u0000\u0000\u0001\u009b\u000b\u0001\u0000"+
		"\u0000\u0000\u009c\u009d\u0003~?\u0000\u009d\u009e\u0005\u0000\u0000\u0001"+
		"\u009e\r\u0001\u0000\u0000\u0000\u009f\u00a0\u0003(\u0014\u0000\u00a0"+
		"\u00a1\u0005\u0000\u0000\u0001\u00a1\u000f\u0001\u0000\u0000\u0000\u00a2"+
		"\u00a3\u0003\"\u0011\u0000\u00a3\u00a4\u0005\u0000\u0000\u0001\u00a4\u0011"+
		"\u0001\u0000\u0000\u0000\u00a5\u00a6\u0003\u001c\u000e\u0000\u00a6\u00a7"+
		"\u0005\u0000\u0000\u0001\u00a7\u0013\u0001\u0000\u0000\u0000\u00a8\u00a9"+
		"\u0003P(\u0000\u00a9\u00aa\u0005\u0000\u0000\u0001\u00aa\u0015\u0001\u0000"+
		"\u0000\u0000\u00ab\u00ac\u0005\u0002\u0000\u0000\u00ac\u00ba\u0005\u0000"+
		"\u0000\u0001\u00ad\u00af\u0003\u0018\f\u0000\u00ae\u00ad\u0001\u0000\u0000"+
		"\u0000\u00ae\u00af\u0001\u0000\u0000\u0000\u00af\u00b3\u0001\u0000\u0000"+
		"\u0000\u00b0\u00b1\u0005\u0012\u0000\u0000\u00b1\u00b2\u0005\u0006\u0000"+
		"\u0000\u00b2\u00b4\u0003L&\u0000\u00b3\u00b0\u0001\u0000\u0000\u0000\u00b3"+
		"\u00b4\u0001\u0000\u0000\u0000\u00b4\u00b5\u0001\u0000\u0000\u0000\u00b5"+
		"\u00b6\u0005\u0013\u0000\u0000\u00b6\u00b7\u0003\u001c\u000e\u0000\u00b7"+
		"\u00b8\u0005\u0000\u0000\u0001\u00b8\u00ba\u0001\u0000\u0000\u0000\u00b9"+
		"\u00ab\u0001\u0000\u0000\u0000\u00b9\u00ae\u0001\u0000\u0000\u0000\u00ba"+
		"\u0017\u0001\u0000\u0000\u0000\u00bb\u00bc\u0005-\u0000\u0000\u00bc\u00c1"+
		"\u0003\u001a\r\u0000\u00bd\u00be\u0005\u0003\u0000\u0000\u00be\u00c0\u0003"+
		"\u001a\r\u0000\u00bf\u00bd\u0001\u0000\u0000\u0000\u00c0\u00c3\u0001\u0000"+
		"\u0000\u0000\u00c1\u00bf\u0001\u0000\u0000\u0000\u00c1\u00c2\u0001\u0000"+
		"\u0000\u0000\u00c2\u0019\u0001\u0000\u0000\u0000\u00c3\u00c1\u0001\u0000"+
		"\u0000\u0000\u00c4\u00c6\u0007\u0000\u0000\u0000\u00c5\u00c4\u0001\u0000"+
		"\u0000\u0000\u00c5\u00c6\u0001\u0000\u0000\u0000\u00c6\u00c7\u0001\u0000"+
		"\u0000\u0000\u00c7\u00cc\u0003t:\u0000\u00c8\u00c9\u0005=\u0000\u0000"+
		"\u00c9\u00cb\u0003t:\u0000\u00ca\u00c8\u0001\u0000\u0000\u0000\u00cb\u00ce"+
		"\u0001\u0000\u0000\u0000\u00cc\u00ca\u0001\u0000\u0000\u0000\u00cc\u00cd"+
		"\u0001\u0000\u0000\u0000\u00cd\u00cf\u0001\u0000\u0000\u0000\u00ce\u00cc"+
		"\u0001\u0000\u0000\u0000\u00cf\u00d0\u0005\u0016\u0000\u0000\u00d0\u00d1"+
		"\u0005e\u0000\u0000\u00d1\u001b\u0001\u0000\u0000\u0000\u00d2\u00dc\u0003"+
		"L&\u0000\u00d3\u00d6\u0003\"\u0011\u0000\u00d4\u00d6\u0003\u001e\u000f"+
		"\u0000\u00d5\u00d3\u0001\u0000\u0000\u0000\u00d5\u00d4\u0001\u0000\u0000"+
		"\u0000\u00d6\u00d9\u0001\u0000\u0000\u0000\u00d7\u00d8\u0005\u0018\u0000"+
		"\u0000\u00d8\u00da\u0003L&\u0000\u00d9\u00d7\u0001\u0000\u0000\u0000\u00d9"+
		"\u00da\u0001\u0000\u0000\u0000\u00da\u00dc\u0001\u0000\u0000\u0000\u00db"+
		"\u00d2\u0001\u0000\u0000\u0000\u00db\u00d5\u0001\u0000\u0000\u0000\u00dc"+
		"\u001d\u0001\u0000\u0000\u0000\u00dd\u00e2\u0003 \u0010\u0000\u00de\u00df"+
		"\u0005\u0003\u0000\u0000\u00df\u00e1\u0003 \u0010\u0000\u00e0\u00de\u0001"+
		"\u0000\u0000\u0000\u00e1\u00e4\u0001\u0000\u0000\u0000\u00e2\u00e0\u0001"+
		"\u0000\u0000\u0000\u00e2\u00e3\u0001\u0000\u0000\u0000\u00e3\u001f\u0001"+
		"\u0000\u0000\u0000\u00e4\u00e2\u0001\u0000\u0000\u0000\u00e5\u00e6\u0005"+
		"\u001c\u0000\u0000\u00e6\u00e9\u0005e\u0000\u0000\u00e7\u00e8\u0005\u0004"+
		"\u0000\u0000\u00e8\u00ea\u0003t:\u0000\u00e9\u00e7\u0001\u0000\u0000\u0000"+
		"\u00e9\u00ea\u0001\u0000\u0000\u0000\u00ea\u00eb\u0001\u0000\u0000\u0000"+
		"\u00eb\u00ec\u0003\"\u0011\u0000\u00ec!\u0001\u0000\u0000\u0000\u00ed"+
		"\u00ef\u0003$\u0012\u0000\u00ee\u00ed\u0001\u0000\u0000\u0000\u00ee\u00ef"+
		"\u0001\u0000\u0000\u0000\u00ef\u00f3\u0001\u0000\u0000\u0000\u00f0\u00f2"+
		"\u0003&\u0013\u0000\u00f1\u00f0\u0001\u0000\u0000\u0000\u00f2\u00f5\u0001"+
		"\u0000\u0000\u0000\u00f3\u00f1\u0001\u0000\u0000\u0000\u00f3\u00f4\u0001"+
		"\u0000\u0000\u0000\u00f4\u00f7\u0001\u0000\u0000\u0000\u00f5\u00f3\u0001"+
		"\u0000\u0000\u0000\u00f6\u00f8\u0003(\u0014\u0000\u00f7\u00f6\u0001\u0000"+
		"\u0000\u0000\u00f8\u00f9\u0001\u0000\u0000\u0000\u00f9\u00f7\u0001\u0000"+
		"\u0000\u0000\u00f9\u00fa\u0001\u0000\u0000\u0000\u00fa#\u0001\u0000\u0000"+
		"\u0000\u00fb\u00fc\u0005b\u0000\u0000\u00fc\u00ff\u0005\u0019\u0000\u0000"+
		"\u00fd\u00ff\u0005\u0014\u0000\u0000\u00fe\u00fb\u0001\u0000\u0000\u0000"+
		"\u00fe\u00fd\u0001\u0000\u0000\u0000\u00ff%\u0001\u0000\u0000\u0000\u0100"+
		"\u0101\u0007\u0001\u0000\u0000\u0101\'\u0001\u0000\u0000\u0000\u0102\u0104"+
		"\u0006\u0014\uffff\uffff\u0000\u0103\u0105\u0003f3\u0000\u0104\u0103\u0001"+
		"\u0000\u0000\u0000\u0104\u0105\u0001\u0000\u0000\u0000\u0105\u0106\u0001"+
		"\u0000\u0000\u0000\u0106\u0108\u00059\u0000\u0000\u0107\u0109\u0003(\u0014"+
		"\u0000\u0108\u0107\u0001\u0000\u0000\u0000\u0109\u010a\u0001\u0000\u0000"+
		"\u0000\u010a\u0108\u0001\u0000\u0000\u0000\u010a\u010b\u0001\u0000\u0000"+
		"\u0000\u010b\u010c\u0001\u0000\u0000\u0000\u010c\u010d\u0005:\u0000\u0000"+
		"\u010d\u0122\u0001\u0000\u0000\u0000\u010e\u0110\u0003*\u0015\u0000\u010f"+
		"\u010e\u0001\u0000\u0000\u0000\u0110\u0111\u0001\u0000\u0000\u0000\u0111"+
		"\u010f\u0001\u0000\u0000\u0000\u0111\u0112\u0001\u0000\u0000\u0000\u0112"+
		"\u0114\u0001\u0000\u0000\u0000\u0113\u0115\u0003(\u0014\u0000\u0114\u0113"+
		"\u0001\u0000\u0000\u0000\u0115\u0116\u0001\u0000\u0000\u0000\u0116\u0114"+
		"\u0001\u0000\u0000\u0000\u0116\u0117\u0001\u0000\u0000\u0000\u0117\u0122"+
		"\u0001\u0000\u0000\u0000\u0118\u0122\u0003,\u0016\u0000\u0119\u011e\u0003"+
		"4\u001a\u0000\u011a\u011b\u0005=\u0000\u0000\u011b\u011d\u00034\u001a"+
		"\u0000\u011c\u011a\u0001\u0000\u0000\u0000\u011d\u0120\u0001\u0000\u0000"+
		"\u0000\u011e\u011c\u0001\u0000\u0000\u0000\u011e\u011f\u0001\u0000\u0000"+
		"\u0000\u011f\u0122\u0001\u0000\u0000\u0000\u0120\u011e\u0001\u0000\u0000"+
		"\u0000\u0121\u0102\u0001\u0000\u0000\u0000\u0121\u010f\u0001\u0000\u0000"+
		"\u0000\u0121\u0118\u0001\u0000\u0000\u0000\u0121\u0119\u0001\u0000\u0000"+
		"\u0000\u0122\u0129\u0001\u0000\u0000\u0000\u0123\u0124\n\u0001\u0000\u0000"+
		"\u0124\u0125\u0003p8\u0000\u0125\u0126\u0003(\u0014\u0001\u0126\u0128"+
		"\u0001\u0000\u0000\u0000\u0127\u0123\u0001\u0000\u0000\u0000\u0128\u012b"+
		"\u0001\u0000\u0000\u0000\u0129\u0127\u0001\u0000\u0000\u0000\u0129\u012a"+
		"\u0001\u0000\u0000\u0000\u012a)\u0001\u0000\u0000\u0000\u012b\u0129\u0001"+
		"\u0000\u0000\u0000\u012c\u012d\u0007\u0002\u0000\u0000\u012d+\u0001\u0000"+
		"\u0000\u0000\u012e\u012f\u0005;\u0000\u0000\u012f\u0130\u0007\u0003\u0000"+
		"\u0000\u0130\u0145\u0005<\u0000\u0000\u0131\u0133\u0003f3\u0000\u0132"+
		"\u0131\u0001\u0000\u0000\u0000\u0132\u0133\u0001\u0000\u0000\u0000\u0133"+
		"\u0134\u0001\u0000\u0000\u0000\u0134\u0136\u0005;\u0000\u0000\u0135\u0137"+
		"\u0003.\u0017\u0000\u0136\u0135\u0001\u0000\u0000\u0000\u0136\u0137\u0001"+
		"\u0000\u0000\u0000\u0137\u013b\u0001\u0000\u0000\u0000\u0138\u0139\u0003"+
		"0\u0018\u0000\u0139\u013a\u0005=\u0000\u0000\u013a\u013c\u0001\u0000\u0000"+
		"\u0000\u013b\u0138\u0001\u0000\u0000\u0000\u013b\u013c\u0001\u0000\u0000"+
		"\u0000\u013c\u013e\u0001\u0000\u0000\u0000\u013d\u013f\u0003L&\u0000\u013e"+
		"\u013d\u0001\u0000\u0000\u0000\u013e\u013f\u0001\u0000\u0000\u0000\u013f"+
		"\u0141\u0001\u0000\u0000\u0000\u0140\u0142\u0003\"\u0011\u0000\u0141\u0140"+
		"\u0001\u0000\u0000\u0000\u0141\u0142\u0001\u0000\u0000\u0000\u0142\u0143"+
		"\u0001\u0000\u0000\u0000\u0143\u0145\u0005<\u0000\u0000\u0144\u012e\u0001"+
		"\u0000\u0000\u0000\u0144\u0132\u0001\u0000\u0000\u0000\u0145-\u0001\u0000"+
		"\u0000\u0000\u0146\u0147\u0003t:\u0000\u0147\u0148\u00055\u0000\u0000"+
		"\u0148/\u0001\u0000\u0000\u0000\u0149\u014a\u0006\u0018\uffff\uffff\u0000"+
		"\u014a\u0156\u0005e\u0000\u0000\u014b\u014c\u00057\u0000\u0000\u014c\u0151"+
		"\u00032\u0019\u0000\u014d\u014e\u0005=\u0000\u0000\u014e\u0150\u00032"+
		"\u0019\u0000\u014f\u014d\u0001\u0000\u0000\u0000\u0150\u0153\u0001\u0000"+
		"\u0000\u0000\u0151\u014f\u0001\u0000\u0000\u0000\u0151\u0152\u0001\u0000"+
		"\u0000\u0000\u0152\u0154\u0001\u0000\u0000\u0000\u0153\u0151\u0001\u0000"+
		"\u0000\u0000\u0154\u0155\u00058\u0000\u0000\u0155\u0157\u0001\u0000\u0000"+
		"\u0000\u0156\u014b\u0001\u0000\u0000\u0000\u0156\u0157\u0001\u0000\u0000"+
		"\u0000\u0157\u015d\u0001\u0000\u0000\u0000\u0158\u0159\u00057\u0000\u0000"+
		"\u0159\u015a\u00030\u0018\u0000\u015a\u015b\u00058\u0000\u0000\u015b\u015d"+
		"\u0001\u0000\u0000\u0000\u015c\u0149\u0001\u0000\u0000\u0000\u015c\u0158"+
		"\u0001\u0000\u0000\u0000\u015d\u0168\u0001\u0000\u0000\u0000\u015e\u015f"+
		"\n\u0002\u0000\u0000\u015f\u0160\u0003n7\u0000\u0160\u0161\u00030\u0018"+
		"\u0002\u0161\u0167\u0001\u0000\u0000\u0000\u0162\u0163\n\u0001\u0000\u0000"+
		"\u0163\u0164\u0003p8\u0000\u0164\u0165\u00030\u0018\u0001\u0165\u0167"+
		"\u0001\u0000\u0000\u0000\u0166\u015e\u0001\u0000\u0000\u0000\u0166\u0162"+
		"\u0001\u0000\u0000\u0000\u0167\u016a\u0001\u0000\u0000\u0000\u0168\u0166"+
		"\u0001\u0000\u0000\u0000\u0168\u0169\u0001\u0000\u0000\u0000\u01691\u0001"+
		"\u0000\u0000\u0000\u016a\u0168\u0001\u0000\u0000\u0000\u016b\u016e\u0003"+
		"\u0084B\u0000\u016c\u016e\u0003|>\u0000\u016d\u016b\u0001\u0000\u0000"+
		"\u0000\u016d\u016c\u0001\u0000\u0000\u0000\u016e3\u0001\u0000\u0000\u0000"+
		"\u016f\u0175\u0003,\u0016\u0000\u0170\u0171\u0003,\u0016\u0000\u0171\u0172"+
		"\u00036\u001b\u0000\u0172\u0173\u0003,\u0016\u0000\u0173\u0175\u0001\u0000"+
		"\u0000\u0000\u0174\u016f\u0001\u0000\u0000\u0000\u0174\u0170\u0001\u0000"+
		"\u0000\u0000\u01755\u0001\u0000\u0000\u0000\u0176\u0183\u0007\u0004\u0000"+
		"\u0000\u0177\u0178\u00038\u001c\u0000\u0178\u017a\u0005;\u0000\u0000\u0179"+
		"\u017b\u0003.\u0017\u0000\u017a\u0179\u0001\u0000\u0000\u0000\u017a\u017b"+
		"\u0001\u0000\u0000\u0000\u017b\u017d\u0001\u0000\u0000\u0000\u017c\u017e"+
		"\u0003L&\u0000\u017d\u017c\u0001\u0000\u0000\u0000\u017d\u017e\u0001\u0000"+
		"\u0000\u0000\u017e\u017f\u0001\u0000\u0000\u0000\u017f\u0180\u0005<\u0000"+
		"\u0000\u0180\u0181\u0003:\u001d\u0000\u0181\u0183\u0001\u0000\u0000\u0000"+
		"\u0182\u0176\u0001\u0000\u0000\u0000\u0182\u0177\u0001\u0000\u0000\u0000"+
		"\u01837\u0001\u0000\u0000\u0000\u0184\u0187\u0003<\u001e\u0000\u0185\u0187"+
		"\u0003@ \u0000\u0186\u0184\u0001\u0000\u0000\u0000\u0186\u0185\u0001\u0000"+
		"\u0000\u0000\u01879\u0001\u0000\u0000\u0000\u0188\u018b\u0003>\u001f\u0000"+
		"\u0189\u018b\u0003@ \u0000\u018a\u0188\u0001\u0000\u0000\u0000\u018a\u0189"+
		"\u0001\u0000\u0000\u0000\u018b;\u0001\u0000\u0000\u0000\u018c\u018d\u0005"+
		"V\u0000\u0000\u018d\u018e\u0005K\u0000\u0000\u018e=\u0001\u0000\u0000"+
		"\u0000\u018f\u0190\u0005K\u0000\u0000\u0190\u0191\u0005X\u0000\u0000\u0191"+
		"?\u0001\u0000\u0000\u0000\u0192\u0193\u0005K\u0000\u0000\u0193\u0194\u0005"+
		"K\u0000\u0000\u0194A\u0001\u0000\u0000\u0000\u0195\u0196\u0005\u0017\u0000"+
		"\u0000\u0196\u019b\u0003D\"\u0000\u0197\u0198\u0005=\u0000\u0000\u0198"+
		"\u019a\u0003D\"\u0000\u0199\u0197\u0001\u0000\u0000\u0000\u019a\u019d"+
		"\u0001\u0000\u0000\u0000\u019b\u0199\u0001\u0000\u0000\u0000\u019b\u019c"+
		"\u0001\u0000\u0000\u0000\u019c\u019e\u0001\u0000\u0000\u0000\u019d\u019b"+
		"\u0001\u0000\u0000\u0000\u019e\u019f\u0005\u0000\u0000\u0001\u019fC\u0001"+
		"\u0000\u0000\u0000\u01a0\u01a1\u0005\u0006\u0000\u0000\u01a1\u01a5\u0003"+
		"P(\u0000\u01a2\u01a3\u0005\u0012\u0000\u0000\u01a3\u01a4\u0005\"\u0000"+
		"\u0000\u01a4\u01a6\u0003P(\u0000\u01a5\u01a2\u0001\u0000\u0000\u0000\u01a5"+
		"\u01a6\u0001\u0000\u0000\u0000\u01a6\u01a7\u0001\u0000\u0000\u0000\u01a7"+
		"\u01a8\u0005\u001b\u0000\u0000\u01a8\u01ab\u0005d\u0000\u0000\u01a9\u01aa"+
		"\u0005\t\u0000\u0000\u01aa\u01ac\u0003P(\u0000\u01ab\u01a9\u0001\u0000"+
		"\u0000\u0000\u01ab\u01ac\u0001\u0000\u0000\u0000\u01acE\u0001\u0000\u0000"+
		"\u0000\u01ad\u01af\u0005\u001d\u0000\u0000\u01ae\u01b0\u0005\u0014\u0000"+
		"\u0000\u01af\u01ae\u0001\u0000\u0000\u0000\u01af\u01b0\u0001\u0000\u0000"+
		"\u0000\u01b0\u01b1\u0001\u0000\u0000\u0000\u01b1\u01b3\u0003\u0086C\u0000"+
		"\u01b2\u01ad\u0001\u0000\u0000\u0000\u01b2\u01b3\u0001\u0000\u0000\u0000"+
		"\u01b3\u01b5\u0001\u0000\u0000\u0000\u01b4\u01b6\u0003H$\u0000\u01b5\u01b4"+
		"\u0001\u0000\u0000\u0000\u01b5\u01b6\u0001\u0000\u0000\u0000\u01b6\u01b7"+
		"\u0001\u0000\u0000\u0000\u01b7\u01b8\u0005\u0000\u0000\u0001\u01b8G\u0001"+
		"\u0000\u0000\u0000\u01b9\u01ba\u0005%\u0000\u0000\u01ba\u01bb\u0005\u0006"+
		"\u0000\u0000\u01bb\u01c0\u0003J%\u0000\u01bc\u01bd\u0005=\u0000\u0000"+
		"\u01bd\u01bf\u0003J%\u0000\u01be\u01bc\u0001\u0000\u0000\u0000\u01bf\u01c2"+
		"\u0001\u0000\u0000\u0000\u01c0\u01be\u0001\u0000\u0000\u0000\u01c0\u01c1"+
		"\u0001\u0000\u0000\u0000\u01c1I\u0001\u0000\u0000\u0000\u01c2\u01c0\u0001"+
		"\u0000\u0000\u0000\u01c3\u01c4\u0003P(\u0000\u01c4\u01c5\u0007\u0005\u0000"+
		"\u0000\u01c5K\u0001\u0000\u0000\u0000\u01c6\u01c7\u0003P(\u0000\u01c7"+
		"M\u0001\u0000\u0000\u0000\u01c8\u01cd\u0003P(\u0000\u01c9\u01ca\u0005"+
		"=\u0000\u0000\u01ca\u01cc\u0003P(\u0000\u01cb\u01c9\u0001\u0000\u0000"+
		"\u0000\u01cc\u01cf\u0001\u0000\u0000\u0000\u01cd\u01cb\u0001\u0000\u0000"+
		"\u0000\u01cd\u01ce\u0001\u0000\u0000\u0000\u01ceO\u0001\u0000\u0000\u0000"+
		"\u01cf\u01cd\u0001\u0000\u0000\u0000\u01d0\u01d1\u0006(\uffff\uffff\u0000"+
		"\u01d1\u01df\u0003R)\u0000\u01d2\u01d3\u00057\u0000\u0000\u01d3\u01d4"+
		"\u0003d2\u0000\u01d4\u01d5\u00058\u0000\u0000\u01d5\u01d6\u0003P(\u000f"+
		"\u01d6\u01df\u0001\u0000\u0000\u0000\u01d7\u01d8\u00057\u0000\u0000\u01d8"+
		"\u01d9\u0003P(\u0000\u01d9\u01da\u00058\u0000\u0000\u01da\u01df\u0001"+
		"\u0000\u0000\u0000\u01db\u01dc\u0007\u0006\u0000\u0000\u01dc\u01df\u0003"+
		"P(\r\u01dd\u01df\u0003Z-\u0000\u01de\u01d0\u0001\u0000\u0000\u0000\u01de"+
		"\u01d2\u0001\u0000\u0000\u0000\u01de\u01d7\u0001\u0000\u0000\u0000\u01de"+
		"\u01db\u0001\u0000\u0000\u0000\u01de\u01dd\u0001\u0000\u0000\u0000\u01df"+
		"\u022a\u0001\u0000\u0000\u0000\u01e0\u01e1\n\f\u0000\u0000\u01e1\u01e2"+
		"\u0007\u0007\u0000\u0000\u01e2\u0229\u0003P(\r\u01e3\u01e4\n\u000b\u0000"+
		"\u0000\u01e4\u01e5\u0007\b\u0000\u0000\u01e5\u0229\u0003P(\f\u01e6\u01e7"+
		"\n\n\u0000\u0000\u01e7\u01e8\u0007\t\u0000\u0000\u01e8\u0229\u0003P(\u000b"+
		"\u01e9\u01ea\n\t\u0000\u0000\u01ea\u01eb\u0007\n\u0000\u0000\u01eb\u0229"+
		"\u0003P(\n\u01ec\u01ed\n\b\u0000\u0000\u01ed\u01ee\u0007\u000b\u0000\u0000"+
		"\u01ee\u0229\u0003P(\t\u01ef\u01f0\n\u0007\u0000\u0000\u01f0\u01f1\u0007"+
		"\f\u0000\u0000\u01f1\u0229\u0003P(\b\u01f2\u01f3\n\u0006\u0000\u0000\u01f3"+
		"\u01f4\u0003n7\u0000\u01f4\u01f5\u0003P(\u0006\u01f5\u0229\u0001\u0000"+
		"\u0000\u0000\u01f6\u01f7\n\u0005\u0000\u0000\u01f7\u01f8\u0003p8\u0000"+
		"\u01f8\u01f9\u0003P(\u0005\u01f9\u0229\u0001\u0000\u0000\u0000\u01fa\u01fc"+
		"\n\u0003\u0000\u0000\u01fb\u01fd\u0003l6\u0000\u01fc\u01fb\u0001\u0000"+
		"\u0000\u0000\u01fc\u01fd\u0001\u0000\u0000\u0000\u01fd\u01ff\u0001\u0000"+
		"\u0000\u0000\u01fe\u0200\u0003j5\u0000\u01ff\u01fe\u0001\u0000\u0000\u0000"+
		"\u01ff\u0200\u0001\u0000\u0000\u0000\u0200\u0201\u0001\u0000\u0000\u0000"+
		"\u0201\u0202\u0005\u001a\u0000\u0000\u0202\u0229\u0003P(\u0004\u0203\u0204"+
		"\n\u0002\u0000\u0000\u0204\u0205\u0005F\u0000\u0000\u0205\u0206\u0003"+
		"P(\u0000\u0206\u0207\u00055\u0000\u0000\u0207\u0208\u0003P(\u0003\u0208"+
		"\u0229\u0001\u0000\u0000\u0000\u0209\u020a\n\u0013\u0000\u0000\u020a\u020b"+
		"\u00056\u0000\u0000\u020b\u0229\u0005e\u0000\u0000\u020c\u020d\n\u0012"+
		"\u0000\u0000\u020d\u020e\u0004(\u000f\u0000\u020e\u0210\u00057\u0000\u0000"+
		"\u020f\u0211\u0003N\'\u0000\u0210\u020f\u0001\u0000\u0000\u0000\u0210"+
		"\u0211\u0001\u0000\u0000\u0000\u0211\u0212\u0001\u0000\u0000\u0000\u0212"+
		"\u0229\u00058\u0000\u0000\u0213\u0214\n\u0011\u0000\u0000\u0214\u0215"+
		"\u0004(\u0011\u0000\u0215\u0216\u0005;\u0000\u0000\u0216\u0217\u0003N"+
		"\'\u0000\u0217\u0218\u0005<\u0000\u0000\u0218\u0229\u0001\u0000\u0000"+
		"\u0000\u0219\u021a\n\u0010\u0000\u0000\u021a\u021b\u0004(\u0013\u0000"+
		"\u021b\u021c\u00059\u0000\u0000\u021c\u021d\u0003N\'\u0000\u021d\u021e"+
		"\u0005:\u0000\u0000\u021e\u0229\u0001\u0000\u0000\u0000\u021f\u0220\n"+
		"\u0004\u0000\u0000\u0220\u0222\u0005\u0004\u0000\u0000\u0221\u0223\u0005"+
		"#\u0000\u0000\u0222\u0221\u0001\u0000\u0000\u0000\u0222\u0223\u0001\u0000"+
		"\u0000\u0000\u0223\u0226\u0001\u0000\u0000\u0000\u0224\u0227\u0003t:\u0000"+
		"\u0225\u0227\u0003r9\u0000\u0226\u0224\u0001\u0000\u0000\u0000\u0226\u0225"+
		"\u0001\u0000\u0000\u0000\u0227\u0229\u0001\u0000\u0000\u0000\u0228\u01e0"+
		"\u0001\u0000\u0000\u0000\u0228\u01e3\u0001\u0000\u0000\u0000\u0228\u01e6"+
		"\u0001\u0000\u0000\u0000\u0228\u01e9\u0001\u0000\u0000\u0000\u0228\u01ec"+
		"\u0001\u0000\u0000\u0000\u0228\u01ef\u0001\u0000\u0000\u0000\u0228\u01f2"+
		"\u0001\u0000\u0000\u0000\u0228\u01f6\u0001\u0000\u0000\u0000\u0228\u01fa"+
		"\u0001\u0000\u0000\u0000\u0228\u0203\u0001\u0000\u0000\u0000\u0228\u0209"+
		"\u0001\u0000\u0000\u0000\u0228\u020c\u0001\u0000\u0000\u0000\u0228\u0213"+
		"\u0001\u0000\u0000\u0000\u0228\u0219\u0001\u0000\u0000\u0000\u0228\u021f"+
		"\u0001\u0000\u0000\u0000\u0229\u022c\u0001\u0000\u0000\u0000\u022a\u0228"+
		"\u0001\u0000\u0000\u0000\u022a\u022b\u0001\u0000\u0000\u0000\u022bQ\u0001"+
		"\u0000\u0000\u0000\u022c\u022a\u0001\u0000\u0000\u0000\u022d\u0235\u0003"+
		"x<\u0000\u022e\u0235\u0003\u0080@\u0000\u022f\u0235\u0003z=\u0000\u0230"+
		"\u0235\u0003\u0082A\u0000\u0231\u0235\u0005d\u0000\u0000\u0232\u0235\u0003"+
		"T*\u0000\u0233\u0235\u0003V+\u0000\u0234\u022d\u0001\u0000\u0000\u0000"+
		"\u0234\u022e\u0001\u0000\u0000\u0000\u0234\u022f\u0001\u0000\u0000\u0000"+
		"\u0234\u0230\u0001\u0000\u0000\u0000\u0234\u0231\u0001\u0000\u0000\u0000"+
		"\u0234\u0232\u0001\u0000\u0000\u0000\u0234\u0233\u0001\u0000\u0000\u0000"+
		"\u0235S\u0001\u0000\u0000\u0000\u0236\u0237\u0003d2\u0000\u0237\u0238"+
		"\u0005;\u0000\u0000\u0238\u0239\u0005<\u0000\u0000\u0239\u023b\u0001\u0000"+
		"\u0000\u0000\u023a\u0236\u0001\u0000\u0000\u0000\u023a\u023b\u0001\u0000"+
		"\u0000\u0000\u023b\u023c\u0001\u0000\u0000\u0000\u023c\u023e\u00059\u0000"+
		"\u0000\u023d\u023f\u0003N\'\u0000\u023e\u023d\u0001\u0000\u0000\u0000"+
		"\u023e\u023f\u0001\u0000\u0000\u0000\u023f\u0240\u0001\u0000\u0000\u0000"+
		"\u0240\u0241\u0005:\u0000\u0000\u0241U\u0001\u0000\u0000\u0000\u0242\u0247"+
		"\u0003r9\u0000\u0243\u0247\u0003t:\u0000\u0244\u0247\u0005e\u0000\u0000"+
		"\u0245\u0247\u0003X,\u0000\u0246\u0242\u0001\u0000\u0000\u0000\u0246\u0243"+
		"\u0001\u0000\u0000\u0000\u0246\u0244\u0001\u0000\u0000\u0000\u0246\u0245"+
		"\u0001\u0000\u0000\u0000\u0247W\u0001\u0000\u0000\u0000\u0248\u0249\u0005"+
		"e\u0000\u0000\u0249\u024a\u0005`\u0000\u0000\u024a\u024b\u0005e\u0000"+
		"\u0000\u024bY\u0001\u0000\u0000\u0000\u024c\u024d\u0005\u0015\u0000\u0000"+
		"\u024d\u024e\u0003P(\u0000\u024e\u024f\u0005\u0004\u0000\u0000\u024f\u0250"+
		"\u0003r9\u0000\u0250\u0252\u0003\\.\u0000\u0251\u0253\u0003`0\u0000\u0252"+
		"\u0251\u0001\u0000\u0000\u0000\u0252\u0253\u0001\u0000\u0000\u0000\u0253"+
		"[\u0001\u0000\u0000\u0000\u0254\u0256\u0007\r\u0000\u0000\u0255\u0254"+
		"\u0001\u0000\u0000\u0000\u0255\u0256\u0001\u0000\u0000\u0000\u0256\u0259"+
		"\u0001\u0000\u0000\u0000\u0257\u0258\u0005!\u0000\u0000\u0258\u025a\u0003"+
		"L&\u0000\u0259\u0257\u0001\u0000\u0000\u0000\u0259\u025a\u0001\u0000\u0000"+
		"\u0000\u025a\u025d\u0001\u0000\u0000\u0000\u025b\u025c\u0005\'\u0000\u0000"+
		"\u025c\u025e\u0003^/\u0000\u025d\u025b\u0001\u0000\u0000\u0000\u025d\u025e"+
		"\u0001\u0000\u0000\u0000\u025e\u0261\u0001\u0000\u0000\u0000\u025f\u0260"+
		"\u0005*\u0000\u0000\u0260\u0262\u0003^/\u0000\u0261\u025f\u0001\u0000"+
		"\u0000\u0000\u0261\u0262\u0001\u0000\u0000\u0000\u0262\u0265\u0001\u0000"+
		"\u0000\u0000\u0263\u0264\u0005\r\u0000\u0000\u0264\u0266\u0003L&\u0000"+
		"\u0265\u0263\u0001\u0000\u0000\u0000\u0265\u0266\u0001\u0000\u0000\u0000"+
		"\u0266\u0267\u0001\u0000\u0000\u0000\u0267\u0268\u0005\u000f\u0000\u0000"+
		"\u0268]\u0001\u0000\u0000\u0000\u0269\u026a\u00057\u0000\u0000\u026a\u026b"+
		"\u0003P(\u0000\u026b\u026c\u0005=\u0000\u0000\u026c\u026d\u0003P(\u0000"+
		"\u026d\u026e\u00058\u0000\u0000\u026e_\u0001\u0000\u0000\u0000\u026f\u0270"+
		"\u0005\b\u0000\u0000\u0270\u0275\u0003b1\u0000\u0271\u0272\u0005=\u0000"+
		"\u0000\u0272\u0274\u0003b1\u0000\u0273\u0271\u0001\u0000\u0000\u0000\u0274"+
		"\u0277\u0001\u0000\u0000\u0000\u0275\u0273\u0001\u0000\u0000\u0000\u0275"+
		"\u0276\u0001\u0000\u0000\u0000\u0276a\u0001\u0000\u0000\u0000\u0277\u0275"+
		"\u0001\u0000\u0000\u0000\u0278\u0279\u0003L&\u0000\u0279\u027a\u0005\u0004"+
		"\u0000\u0000\u027a\u027b\u0003r9\u0000\u027bc\u0001\u0000\u0000\u0000"+
		"\u027c\u027d\u0007\u000e\u0000\u0000\u027de\u0001\u0000\u0000\u0000\u027e"+
		"\u0283\u0003h4\u0000\u027f\u0280\u0005R\u0000\u0000\u0280\u0282\u0003"+
		"h4\u0000\u0281\u027f\u0001\u0000\u0000\u0000\u0282\u0285\u0001\u0000\u0000"+
		"\u0000\u0283\u0281\u0001\u0000\u0000\u0000\u0283\u0284\u0001\u0000\u0000"+
		"\u0000\u0284\u0292\u0001\u0000\u0000\u0000\u0285\u0283\u0001\u0000\u0000"+
		"\u0000\u0286\u0287\u0005V\u0000\u0000\u0287\u028c\u0003h4\u0000\u0288"+
		"\u0289\u0005R\u0000\u0000\u0289\u028b\u0003h4\u0000\u028a\u0288\u0001"+
		"\u0000\u0000\u0000\u028b\u028e\u0001\u0000\u0000\u0000\u028c\u028a\u0001"+
		"\u0000\u0000\u0000\u028c\u028d\u0001\u0000\u0000\u0000\u028d\u028f\u0001"+
		"\u0000\u0000\u0000\u028e\u028c\u0001\u0000\u0000\u0000\u028f\u0290\u0005"+
		"X\u0000\u0000\u0290\u0292\u0001\u0000\u0000\u0000\u0291\u027e\u0001\u0000"+
		"\u0000\u0000\u0291\u0286\u0001\u0000\u0000\u0000\u0292g\u0001\u0000\u0000"+
		"\u0000\u0293\u02ac\u0003j5\u0000\u0294\u0296\u0003l6\u0000\u0295\u0297"+
		"\u0007\u000f\u0000\u0000\u0296\u0295\u0001\u0000\u0000\u0000\u0296\u0297"+
		"\u0001\u0000\u0000\u0000\u0297\u02ac\u0001\u0000\u0000\u0000\u0298\u029a"+
		"\u0005b\u0000\u0000\u0299\u029b\u0007\b\u0000\u0000\u029a\u0299\u0001"+
		"\u0000\u0000\u0000\u029a\u029b\u0001\u0000\u0000\u0000\u029b\u029d\u0001"+
		"\u0000\u0000\u0000\u029c\u029e\u0007\u0010\u0000\u0000\u029d\u029c\u0001"+
		"\u0000\u0000\u0000\u029d\u029e\u0001\u0000\u0000\u0000\u029e\u02a0\u0001"+
		"\u0000\u0000\u0000\u029f\u02a1\u0005Q\u0000\u0000\u02a0\u029f\u0001\u0000"+
		"\u0000\u0000\u02a0\u02a1\u0001\u0000\u0000\u0000\u02a1\u02ac\u0001\u0000"+
		"\u0000\u0000\u02a2\u02a3\u0005b\u0000\u0000\u02a3\u02a4\u0005a\u0000\u0000"+
		"\u02a4\u02a6\u0005b\u0000\u0000\u02a5\u02a7\u0007\u0010\u0000\u0000\u02a6"+
		"\u02a5\u0001\u0000\u0000\u0000\u02a6\u02a7\u0001\u0000\u0000\u0000\u02a7"+
		"\u02a9\u0001\u0000\u0000\u0000\u02a8\u02aa\u0005Q\u0000\u0000\u02a9\u02a8"+
		"\u0001\u0000\u0000\u0000\u02a9\u02aa\u0001\u0000\u0000\u0000\u02aa\u02ac"+
		"\u0001\u0000\u0000\u0000\u02ab\u0293\u0001\u0000\u0000\u0000\u02ab\u0294"+
		"\u0001\u0000\u0000\u0000\u02ab\u0298\u0001\u0000\u0000\u0000\u02ab\u02a2"+
		"\u0001\u0000\u0000\u0000\u02aci\u0001\u0000\u0000\u0000\u02ad\u02ae\u0007"+
		"\u0011\u0000\u0000\u02aek\u0001\u0000\u0000\u0000\u02af\u02b0\u0007\u0012"+
		"\u0000\u0000\u02b0m\u0001\u0000\u0000\u0000\u02b1\u02b2\u0007\u0013\u0000"+
		"\u0000\u02b2o\u0001\u0000\u0000\u0000\u02b3\u02b4\u0007\u0014\u0000\u0000"+
		"\u02b4q\u0001\u0000\u0000\u0000\u02b5\u02b6\u0005C\u0000\u0000\u02b6\u02b7"+
		"\u0005e\u0000\u0000\u02b7s\u0001\u0000\u0000\u0000\u02b8\u02ba\u0005D"+
		"\u0000\u0000\u02b9\u02bb\u0005e\u0000\u0000\u02ba\u02b9\u0001\u0000\u0000"+
		"\u0000\u02ba\u02bb\u0001\u0000\u0000\u0000\u02bbu\u0001\u0000\u0000\u0000"+
		"\u02bc\u02bf\u0005b\u0000\u0000\u02bd\u02be\u00056\u0000\u0000\u02be\u02c0"+
		"\u0005b\u0000\u0000\u02bf\u02bd\u0001\u0000\u0000\u0000\u02bf\u02c0\u0001"+
		"\u0000\u0000\u0000\u02c0\u02c3\u0001\u0000\u0000\u0000\u02c1\u02c2\u0005"+
		"6\u0000\u0000\u02c2\u02c4\u0005b\u0000\u0000\u02c3\u02c1\u0001\u0000\u0000"+
		"\u0000\u02c3\u02c4\u0001\u0000\u0000\u0000\u02c4\u02c6\u0001\u0000\u0000"+
		"\u0000\u02c5\u02c7\u0007\u0015\u0000\u0000\u02c6\u02c5\u0001\u0000\u0000"+
		"\u0000\u02c6\u02c7\u0001\u0000\u0000\u0000\u02c7\u02c9\u0001\u0000\u0000"+
		"\u0000\u02c8\u02ca\u0005e\u0000\u0000\u02c9\u02c8\u0001\u0000\u0000\u0000"+
		"\u02c9\u02ca\u0001\u0000\u0000\u0000\u02caw\u0001\u0000\u0000\u0000\u02cb"+
		"\u02cc\u0005\u001f\u0000\u0000\u02ccy\u0001\u0000\u0000\u0000\u02cd\u02ce"+
		"\u0003|>\u0000\u02ce{\u0001\u0000\u0000\u0000\u02cf\u02d1\u0003\u0088"+
		"D\u0000\u02d0\u02cf\u0001\u0000\u0000\u0000\u02d0\u02d1\u0001\u0000\u0000"+
		"\u0000\u02d1\u02d2\u0001\u0000\u0000\u0000\u02d2\u02d3\u0003~?\u0000\u02d3"+
		"}\u0001\u0000\u0000\u0000\u02d4\u02d5\u0003\u0086C\u0000\u02d5\u02d6\u0005"+
		"6\u0000\u0000\u02d6\u02d7\u0003\u0086C\u0000\u02d7\u007f\u0001\u0000\u0000"+
		"\u0000\u02d8\u02d9\u0007\u0016\u0000\u0000\u02d9\u0081\u0001\u0000\u0000"+
		"\u0000\u02da\u02db\u0003\u0084B\u0000\u02db\u0083\u0001\u0000\u0000\u0000"+
		"\u02dc\u02de\u0003\u0088D\u0000\u02dd\u02dc\u0001\u0000\u0000\u0000\u02dd"+
		"\u02de\u0001\u0000\u0000\u0000\u02de\u02df\u0001\u0000\u0000\u0000\u02df"+
		"\u02e0\u0003\u0086C\u0000\u02e0\u0085\u0001\u0000\u0000\u0000\u02e1\u02e2"+
		"\u0007\u0017\u0000\u0000\u02e2\u0087\u0001\u0000\u0000\u0000\u02e3\u02e4"+
		"\u0007\b\u0000\u0000\u02e4\u0089\u0001\u0000\u0000\u0000T\u00ae\u00b3"+
		"\u00b9\u00c1\u00c5\u00cc\u00d5\u00d9\u00db\u00e2\u00e9\u00ee\u00f3\u00f9"+
		"\u00fe\u0104\u010a\u0111\u0116\u011e\u0121\u0129\u0132\u0136\u013b\u013e"+
		"\u0141\u0144\u0151\u0156\u015c\u0166\u0168\u016d\u0174\u017a\u017d\u0182"+
		"\u0186\u018a\u019b\u01a5\u01ab\u01af\u01b2\u01b5\u01c0\u01cd\u01de\u01fc"+
		"\u01ff\u0210\u0222\u0226\u0228\u022a\u0234\u023a\u023e\u0246\u0252\u0255"+
		"\u0259\u025d\u0261\u0265\u0275\u0283\u028c\u0291\u0296\u029a\u029d\u02a0"+
		"\u02a6\u02a9\u02ab\u02ba\u02bf\u02c3\u02c6\u02c9\u02d0\u02dd";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}