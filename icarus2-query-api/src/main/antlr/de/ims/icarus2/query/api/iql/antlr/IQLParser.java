// Generated from de/ims/icarus2/query/api/iql/antlr/IQL.g4 by ANTLR 4.13.2

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
public class IQLParser extends Parser {
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
		RULE_standaloneNodeStatement = 0, RULE_standaloneStructuralConstraint = 1, 
		RULE_standaloneSelectiveStatement = 2, RULE_standaloneExpression = 3, 
		RULE_payloadStatement = 4, RULE_bindingsList = 5, RULE_binding = 6, RULE_selectionStatement = 7, 
		RULE_laneStatementsList = 8, RULE_laneStatement = 9, RULE_structuralConstraint = 10, 
		RULE_hitsLimit = 11, RULE_matchFlag = 12, RULE_nodeStatement = 13, RULE_nodeArrangement = 14, 
		RULE_node = 15, RULE_memberLabel = 16, RULE_positionMarker = 17, RULE_positionArgument = 18, 
		RULE_element = 19, RULE_edge = 20, RULE_leftEdgePart = 21, RULE_rightEdgePart = 22, 
		RULE_directedEdgeLeft = 23, RULE_directedEdgeRight = 24, RULE_undirectedEdge = 25, 
		RULE_groupStatement = 26, RULE_groupExpression = 27, RULE_resultStatement = 28, 
		RULE_orderExpressionList = 29, RULE_orderExpression = 30, RULE_constraint = 31, 
		RULE_expressionList = 32, RULE_expression = 33, RULE_primary = 34, RULE_listStatement = 35, 
		RULE_reference = 36, RULE_qualifiedIdentifier = 37, RULE_loopExpresseion = 38, 
		RULE_loopControl = 39, RULE_boundedRange = 40, RULE_counterList = 41, 
		RULE_counter = 42, RULE_type = 43, RULE_quantifier = 44, RULE_simpleQuantifier = 45, 
		RULE_not = 46, RULE_all = 47, RULE_and = 48, RULE_or = 49, RULE_variable = 50, 
		RULE_member = 51, RULE_versionDeclaration = 52, RULE_nullLiteral = 53, 
		RULE_floatingPointLiteral = 54, RULE_signedFloatingPointLiteral = 55, 
		RULE_unsignedFloatingPointLiteral = 56, RULE_booleanLiteral = 57, RULE_integerLiteral = 58, 
		RULE_signedIntegerLiteral = 59, RULE_unsignedIntegerLiteral = 60, RULE_sign = 61;
	private static String[] makeRuleNames() {
		return new String[] {
			"standaloneNodeStatement", "standaloneStructuralConstraint", "standaloneSelectiveStatement", 
			"standaloneExpression", "payloadStatement", "bindingsList", "binding", 
			"selectionStatement", "laneStatementsList", "laneStatement", "structuralConstraint", 
			"hitsLimit", "matchFlag", "nodeStatement", "nodeArrangement", "node", 
			"memberLabel", "positionMarker", "positionArgument", "element", "edge", 
			"leftEdgePart", "rightEdgePart", "directedEdgeLeft", "directedEdgeRight", 
			"undirectedEdge", "groupStatement", "groupExpression", "resultStatement", 
			"orderExpressionList", "orderExpression", "constraint", "expressionList", 
			"expression", "primary", "listStatement", "reference", "qualifiedIdentifier", 
			"loopExpresseion", "loopControl", "boundedRange", "counterList", "counter", 
			"type", "quantifier", "simpleQuantifier", "not", "all", "and", "or", 
			"variable", "member", "versionDeclaration", "nullLiteral", "floatingPointLiteral", 
			"signedFloatingPointLiteral", "unsignedFloatingPointLiteral", "booleanLiteral", 
			"integerLiteral", "signedIntegerLiteral", "unsignedIntegerLiteral", "sign"
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
	public String getGrammarFileName() { return "IQL.g4"; }

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


	public IQLParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class StandaloneNodeStatementContext extends ParserRuleContext {
		public NodeStatementContext nodeStatement() {
			return getRuleContext(NodeStatementContext.class,0);
		}
		public TerminalNode EOF() { return getToken(IQLParser.EOF, 0); }
		public StandaloneNodeStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_standaloneNodeStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterStandaloneNodeStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitStandaloneNodeStatement(this);
		}
	}

	public final StandaloneNodeStatementContext standaloneNodeStatement() throws RecognitionException {
		StandaloneNodeStatementContext _localctx = new StandaloneNodeStatementContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_standaloneNodeStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(124);
			nodeStatement(0);
			setState(125);
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
		public TerminalNode EOF() { return getToken(IQLParser.EOF, 0); }
		public StandaloneStructuralConstraintContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_standaloneStructuralConstraint; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterStandaloneStructuralConstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitStandaloneStructuralConstraint(this);
		}
	}

	public final StandaloneStructuralConstraintContext standaloneStructuralConstraint() throws RecognitionException {
		StandaloneStructuralConstraintContext _localctx = new StandaloneStructuralConstraintContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_standaloneStructuralConstraint);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(127);
			structuralConstraint();
			setState(128);
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
		public TerminalNode EOF() { return getToken(IQLParser.EOF, 0); }
		public StandaloneSelectiveStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_standaloneSelectiveStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterStandaloneSelectiveStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitStandaloneSelectiveStatement(this);
		}
	}

	public final StandaloneSelectiveStatementContext standaloneSelectiveStatement() throws RecognitionException {
		StandaloneSelectiveStatementContext _localctx = new StandaloneSelectiveStatementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_standaloneSelectiveStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(130);
			selectionStatement();
			setState(131);
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
		public TerminalNode EOF() { return getToken(IQLParser.EOF, 0); }
		public StandaloneExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_standaloneExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterStandaloneExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitStandaloneExpression(this);
		}
	}

	public final StandaloneExpressionContext standaloneExpression() throws RecognitionException {
		StandaloneExpressionContext _localctx = new StandaloneExpressionContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_standaloneExpression);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(133);
			expression(0);
			setState(134);
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
		public TerminalNode ALL() { return getToken(IQLParser.ALL, 0); }
		public TerminalNode EOF() { return getToken(IQLParser.EOF, 0); }
		public TerminalNode FIND() { return getToken(IQLParser.FIND, 0); }
		public SelectionStatementContext selectionStatement() {
			return getRuleContext(SelectionStatementContext.class,0);
		}
		public BindingsListContext bindingsList() {
			return getRuleContext(BindingsListContext.class,0);
		}
		public TerminalNode FILTER() { return getToken(IQLParser.FILTER, 0); }
		public TerminalNode BY() { return getToken(IQLParser.BY, 0); }
		public ConstraintContext constraint() {
			return getRuleContext(ConstraintContext.class,0);
		}
		public PayloadStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_payloadStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterPayloadStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitPayloadStatement(this);
		}
	}

	public final PayloadStatementContext payloadStatement() throws RecognitionException {
		PayloadStatementContext _localctx = new PayloadStatementContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_payloadStatement);
		int _la;
		try {
			setState(150);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ALL:
				enterOuterAlt(_localctx, 1);
				{
				setState(136);
				match(ALL);
				setState(137);
				match(EOF);
				}
				break;
			case FILTER:
			case FIND:
			case WITH:
				enterOuterAlt(_localctx, 2);
				{
				setState(139);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==WITH) {
					{
					setState(138);
					bindingsList();
					}
				}

				setState(144);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FILTER) {
					{
					setState(141);
					match(FILTER);
					setState(142);
					match(BY);
					setState(143);
					constraint();
					}
				}

				setState(146);
				match(FIND);
				setState(147);
				selectionStatement();
				setState(148);
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
		public TerminalNode WITH() { return getToken(IQLParser.WITH, 0); }
		public List<BindingContext> binding() {
			return getRuleContexts(BindingContext.class);
		}
		public BindingContext binding(int i) {
			return getRuleContext(BindingContext.class,i);
		}
		public List<TerminalNode> AND() { return getTokens(IQLParser.AND); }
		public TerminalNode AND(int i) {
			return getToken(IQLParser.AND, i);
		}
		public BindingsListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bindingsList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterBindingsList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitBindingsList(this);
		}
	}

	public final BindingsListContext bindingsList() throws RecognitionException {
		BindingsListContext _localctx = new BindingsListContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_bindingsList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(152);
			match(WITH);
			setState(153);
			binding();
			setState(158);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AND) {
				{
				{
				setState(154);
				match(AND);
				setState(155);
				binding();
				}
				}
				setState(160);
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
		public TerminalNode FROM() { return getToken(IQLParser.FROM, 0); }
		public TerminalNode Identifier() { return getToken(IQLParser.Identifier, 0); }
		public List<TerminalNode> COMMA() { return getTokens(IQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(IQLParser.COMMA, i);
		}
		public TerminalNode DISTINCT() { return getToken(IQLParser.DISTINCT, 0); }
		public TerminalNode EDGES() { return getToken(IQLParser.EDGES, 0); }
		public BindingContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_binding; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterBinding(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitBinding(this);
		}
	}

	public final BindingContext binding() throws RecognitionException {
		BindingContext _localctx = new BindingContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_binding);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(162);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DISTINCT || _la==EDGES) {
				{
				setState(161);
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

			setState(164);
			member();
			setState(169);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(165);
				match(COMMA);
				setState(166);
				member();
				}
				}
				setState(171);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(172);
			match(FROM);
			setState(173);
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
		public TerminalNode HAVING() { return getToken(IQLParser.HAVING, 0); }
		public SelectionStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_selectionStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterSelectionStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitSelectionStatement(this);
		}
	}

	public final SelectionStatementContext selectionStatement() throws RecognitionException {
		SelectionStatementContext _localctx = new SelectionStatementContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_selectionStatement);
		int _la;
		try {
			setState(184);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(175);
				constraint();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(178);
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
					setState(176);
					structuralConstraint();
					}
					break;
				case LANE:
					{
					setState(177);
					laneStatementsList();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(182);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==HAVING) {
					{
					setState(180);
					match(HAVING);
					setState(181);
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
		public List<TerminalNode> AND() { return getTokens(IQLParser.AND); }
		public TerminalNode AND(int i) {
			return getToken(IQLParser.AND, i);
		}
		public LaneStatementsListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_laneStatementsList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterLaneStatementsList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitLaneStatementsList(this);
		}
	}

	public final LaneStatementsListContext laneStatementsList() throws RecognitionException {
		LaneStatementsListContext _localctx = new LaneStatementsListContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_laneStatementsList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(186);
			laneStatement();
			setState(191);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==AND) {
				{
				{
				setState(187);
				match(AND);
				setState(188);
				laneStatement();
				}
				}
				setState(193);
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
		public TerminalNode LANE() { return getToken(IQLParser.LANE, 0); }
		public StructuralConstraintContext structuralConstraint() {
			return getRuleContext(StructuralConstraintContext.class,0);
		}
		public TerminalNode Identifier() { return getToken(IQLParser.Identifier, 0); }
		public TerminalNode AS() { return getToken(IQLParser.AS, 0); }
		public MemberContext member() {
			return getRuleContext(MemberContext.class,0);
		}
		public LaneStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_laneStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterLaneStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitLaneStatement(this);
		}
	}

	public final LaneStatementContext laneStatement() throws RecognitionException {
		LaneStatementContext _localctx = new LaneStatementContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_laneStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(194);
			match(LANE);
			setState(195);
			((LaneStatementContext)_localctx).name = match(Identifier);
			setState(198);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==AS) {
				{
				setState(196);
				match(AS);
				setState(197);
				member();
				}
			}

			setState(200);
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterStructuralConstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitStructuralConstraint(this);
		}
	}

	public final StructuralConstraintContext structuralConstraint() throws RecognitionException {
		StructuralConstraintContext _localctx = new StructuralConstraintContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_structuralConstraint);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(203);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				{
				setState(202);
				hitsLimit();
				}
				break;
			}
			setState(208);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3298534885504L) != 0)) {
				{
				{
				setState(205);
				matchFlag();
				}
				}
				setState(210);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(212); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(211);
				nodeStatement(0);
				}
				}
				setState(214); 
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
		public TerminalNode PureDigits() { return getToken(IQLParser.PureDigits, 0); }
		public TerminalNode HITS() { return getToken(IQLParser.HITS, 0); }
		public TerminalNode FIRST() { return getToken(IQLParser.FIRST, 0); }
		public HitsLimitContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hitsLimit; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterHitsLimit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitHitsLimit(this);
		}
	}

	public final HitsLimitContext hitsLimit() throws RecognitionException {
		HitsLimitContext _localctx = new HitsLimitContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_hitsLimit);
		try {
			setState(219);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PureDigits:
				enterOuterAlt(_localctx, 1);
				{
				setState(216);
				match(PureDigits);
				setState(217);
				match(HITS);
				}
				break;
			case FIRST:
				enterOuterAlt(_localctx, 2);
				{
				setState(218);
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
		public TerminalNode DISJOINT() { return getToken(IQLParser.DISJOINT, 0); }
		public TerminalNode CONSECUTIVE() { return getToken(IQLParser.CONSECUTIVE, 0); }
		public TerminalNode REVERSE() { return getToken(IQLParser.REVERSE, 0); }
		public TerminalNode ROOTED() { return getToken(IQLParser.ROOTED, 0); }
		public MatchFlagContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_matchFlag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterMatchFlag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitMatchFlag(this);
		}
	}

	public final MatchFlagContext matchFlag() throws RecognitionException {
		MatchFlagContext _localctx = new MatchFlagContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_matchFlag);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(221);
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterElementSequence(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitElementSequence(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ElementGroupingContext extends NodeStatementContext {
		public TerminalNode LBRACE() { return getToken(IQLParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(IQLParser.RBRACE, 0); }
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterElementGrouping(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitElementGrouping(this);
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterSingleNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitSingleNode(this);
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
		public List<TerminalNode> COMMA() { return getTokens(IQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(IQLParser.COMMA, i);
		}
		public GraphFragmentContext(NodeStatementContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterGraphFragment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitGraphFragment(this);
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterElementDisjunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitElementDisjunction(this);
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
		int _startState = 26;
		enterRecursionRule(_localctx, 26, RULE_nodeStatement, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(254);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
			case 1:
				{
				_localctx = new ElementGroupingContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(225);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ALL || _la==NOT || ((((_la - 71)) & ~0x3f) == 0 && ((1L << (_la - 71)) & 134250501L) != 0)) {
					{
					setState(224);
					quantifier();
					}
				}

				setState(227);
				match(LBRACE);
				setState(229); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(228);
					nodeStatement(0);
					}
					}
					setState(231); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 720593808516972550L) != 0) || ((((_la - 71)) & ~0x3f) == 0 && ((1L << (_la - 71)) & 134250501L) != 0) );
				setState(233);
				match(RBRACE);
				}
				break;
			case 2:
				{
				_localctx = new ElementSequenceContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(236); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(235);
						nodeArrangement();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(238); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				setState(241); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(240);
						nodeStatement(0);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(243); 
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
				setState(245);
				node();
				}
				break;
			case 4:
				{
				_localctx = new GraphFragmentContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(246);
				element();
				setState(251);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
				while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(247);
						match(COMMA);
						setState(248);
						element();
						}
						} 
					}
					setState(253);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
				}
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(262);
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
					setState(256);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(257);
					or();
					setState(258);
					((ElementDisjunctionContext)_localctx).right = nodeStatement(1);
					}
					} 
				}
				setState(264);
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
		public TerminalNode UNORDERED() { return getToken(IQLParser.UNORDERED, 0); }
		public TerminalNode ORDERED() { return getToken(IQLParser.ORDERED, 0); }
		public TerminalNode ADJACENT() { return getToken(IQLParser.ADJACENT, 0); }
		public NodeArrangementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nodeArrangement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterNodeArrangement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitNodeArrangement(this);
		}
	}

	public final NodeArrangementContext nodeArrangement() throws RecognitionException {
		NodeArrangementContext _localctx = new NodeArrangementContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_nodeArrangement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(265);
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
		public TerminalNode LBRACK() { return getToken(IQLParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(IQLParser.RBRACK, 0); }
		public TerminalNode PLUS() { return getToken(IQLParser.PLUS, 0); }
		public TerminalNode QMARK() { return getToken(IQLParser.QMARK, 0); }
		public TerminalNode STAR() { return getToken(IQLParser.STAR, 0); }
		public DummyNodeContext(NodeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterDummyNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitDummyNode(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ProperNodeContext extends NodeContext {
		public TerminalNode LBRACK() { return getToken(IQLParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(IQLParser.RBRACK, 0); }
		public QuantifierContext quantifier() {
			return getRuleContext(QuantifierContext.class,0);
		}
		public MemberLabelContext memberLabel() {
			return getRuleContext(MemberLabelContext.class,0);
		}
		public PositionMarkerContext positionMarker() {
			return getRuleContext(PositionMarkerContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(IQLParser.COMMA, 0); }
		public ConstraintContext constraint() {
			return getRuleContext(ConstraintContext.class,0);
		}
		public StructuralConstraintContext structuralConstraint() {
			return getRuleContext(StructuralConstraintContext.class,0);
		}
		public ProperNodeContext(NodeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterProperNode(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitProperNode(this);
		}
	}

	public final NodeContext node() throws RecognitionException {
		NodeContext _localctx = new NodeContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_node);
		int _la;
		try {
			setState(289);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
			case 1:
				_localctx = new DummyNodeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(267);
				match(LBRACK);
				setState(268);
				_la = _input.LA(1);
				if ( !(((((_la - 70)) & ~0x3f) == 0 && ((1L << (_la - 70)) & 25L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(269);
				match(RBRACK);
				}
				break;
			case 2:
				_localctx = new ProperNodeContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(271);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ALL || _la==NOT || ((((_la - 71)) & ~0x3f) == 0 && ((1L << (_la - 71)) & 134250501L) != 0)) {
					{
					setState(270);
					quantifier();
					}
				}

				setState(273);
				match(LBRACK);
				setState(275);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
				case 1:
					{
					setState(274);
					memberLabel();
					}
					break;
				}
				setState(280);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
				case 1:
					{
					setState(277);
					positionMarker(0);
					setState(278);
					match(COMMA);
					}
					break;
				}
				setState(283);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
				case 1:
					{
					setState(282);
					constraint();
					}
					break;
				}
				setState(286);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 720597107052906630L) != 0) || ((((_la - 71)) & ~0x3f) == 0 && ((1L << (_la - 71)) & 134250501L) != 0)) {
					{
					setState(285);
					structuralConstraint();
					}
				}

				setState(288);
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
		public TerminalNode COLON() { return getToken(IQLParser.COLON, 0); }
		public MemberLabelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_memberLabel; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterMemberLabel(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitMemberLabel(this);
		}
	}

	public final MemberLabelContext memberLabel() throws RecognitionException {
		MemberLabelContext _localctx = new MemberLabelContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_memberLabel);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(291);
			member();
			setState(292);
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterMarkerConjunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitMarkerConjunction(this);
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterMarkerDisjunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitMarkerDisjunction(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MarkerCallContext extends PositionMarkerContext {
		public Token name;
		public TerminalNode Identifier() { return getToken(IQLParser.Identifier, 0); }
		public TerminalNode LPAREN() { return getToken(IQLParser.LPAREN, 0); }
		public List<PositionArgumentContext> positionArgument() {
			return getRuleContexts(PositionArgumentContext.class);
		}
		public PositionArgumentContext positionArgument(int i) {
			return getRuleContext(PositionArgumentContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(IQLParser.RPAREN, 0); }
		public List<TerminalNode> COMMA() { return getTokens(IQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(IQLParser.COMMA, i);
		}
		public MarkerCallContext(PositionMarkerContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterMarkerCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitMarkerCall(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MarkerWrappingContext extends PositionMarkerContext {
		public TerminalNode LPAREN() { return getToken(IQLParser.LPAREN, 0); }
		public PositionMarkerContext positionMarker() {
			return getRuleContext(PositionMarkerContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(IQLParser.RPAREN, 0); }
		public MarkerWrappingContext(PositionMarkerContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterMarkerWrapping(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitMarkerWrapping(this);
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
		int _startState = 34;
		enterRecursionRule(_localctx, 34, RULE_positionMarker, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(313);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case Identifier:
				{
				_localctx = new MarkerCallContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(295);
				((MarkerCallContext)_localctx).name = match(Identifier);
				setState(307);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
				case 1:
					{
					setState(296);
					match(LPAREN);
					setState(297);
					positionArgument();
					setState(302);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while (_la==COMMA) {
						{
						{
						setState(298);
						match(COMMA);
						setState(299);
						positionArgument();
						}
						}
						setState(304);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(305);
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
				setState(309);
				match(LPAREN);
				setState(310);
				positionMarker(0);
				setState(311);
				match(RPAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(325);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,32,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(323);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
					case 1:
						{
						_localctx = new MarkerConjunctionContext(new PositionMarkerContext(_parentctx, _parentState));
						((MarkerConjunctionContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_positionMarker);
						setState(315);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(316);
						and();
						setState(317);
						((MarkerConjunctionContext)_localctx).right = positionMarker(2);
						}
						break;
					case 2:
						{
						_localctx = new MarkerDisjunctionContext(new PositionMarkerContext(_parentctx, _parentState));
						((MarkerDisjunctionContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_positionMarker);
						setState(319);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(320);
						or();
						setState(321);
						((MarkerDisjunctionContext)_localctx).right = positionMarker(1);
						}
						break;
					}
					} 
				}
				setState(327);
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterPositionArgument(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitPositionArgument(this);
		}
	}

	public final PositionArgumentContext positionArgument() throws RecognitionException {
		PositionArgumentContext _localctx = new PositionArgumentContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_positionArgument);
		try {
			setState(330);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,33,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(328);
				signedIntegerLiteral();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(329);
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterElement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitElement(this);
		}
	}

	public final ElementContext element() throws RecognitionException {
		ElementContext _localctx = new ElementContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_element);
		try {
			setState(337);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,34,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(332);
				((ElementContext)_localctx).content = node();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(333);
				((ElementContext)_localctx).source = node();
				setState(334);
				edge();
				setState(335);
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
		public TerminalNode EDGE_LEFT() { return getToken(IQLParser.EDGE_LEFT, 0); }
		public TerminalNode EDGE_RIGHT() { return getToken(IQLParser.EDGE_RIGHT, 0); }
		public TerminalNode EDGE_BIDIRECTIONAL() { return getToken(IQLParser.EDGE_BIDIRECTIONAL, 0); }
		public TerminalNode EDGE_UNDIRECTED() { return getToken(IQLParser.EDGE_UNDIRECTED, 0); }
		public EmptyEdgeContext(EdgeContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterEmptyEdge(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitEmptyEdge(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class FilledEdgeContext extends EdgeContext {
		public LeftEdgePartContext leftEdgePart() {
			return getRuleContext(LeftEdgePartContext.class,0);
		}
		public TerminalNode LBRACK() { return getToken(IQLParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(IQLParser.RBRACK, 0); }
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterFilledEdge(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitFilledEdge(this);
		}
	}

	public final EdgeContext edge() throws RecognitionException {
		EdgeContext _localctx = new EdgeContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_edge);
		int _la;
		try {
			setState(351);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case EDGE_LEFT:
			case EDGE_RIGHT:
			case EDGE_BIDIRECTIONAL:
			case EDGE_UNDIRECTED:
				_localctx = new EmptyEdgeContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(339);
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
				setState(340);
				leftEdgePart();
				setState(341);
				match(LBRACK);
				setState(343);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,35,_ctx) ) {
				case 1:
					{
					setState(342);
					memberLabel();
					}
					break;
				}
				setState(346);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 183882327852711936L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 32212255635L) != 0)) {
					{
					setState(345);
					constraint();
					}
				}

				setState(348);
				match(RBRACK);
				setState(349);
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterLeftEdgePart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitLeftEdgePart(this);
		}
	}

	public final LeftEdgePartContext leftEdgePart() throws RecognitionException {
		LeftEdgePartContext _localctx = new LeftEdgePartContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_leftEdgePart);
		try {
			setState(355);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LT:
				enterOuterAlt(_localctx, 1);
				{
				setState(353);
				directedEdgeLeft();
				}
				break;
			case MINUS:
				enterOuterAlt(_localctx, 2);
				{
				setState(354);
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterRightEdgePart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitRightEdgePart(this);
		}
	}

	public final RightEdgePartContext rightEdgePart() throws RecognitionException {
		RightEdgePartContext _localctx = new RightEdgePartContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_rightEdgePart);
		try {
			setState(359);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,39,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(357);
				directedEdgeRight();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(358);
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
		public TerminalNode LT() { return getToken(IQLParser.LT, 0); }
		public TerminalNode MINUS() { return getToken(IQLParser.MINUS, 0); }
		public DirectedEdgeLeftContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_directedEdgeLeft; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterDirectedEdgeLeft(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitDirectedEdgeLeft(this);
		}
	}

	public final DirectedEdgeLeftContext directedEdgeLeft() throws RecognitionException {
		DirectedEdgeLeftContext _localctx = new DirectedEdgeLeftContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_directedEdgeLeft);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(361);
			match(LT);
			setState(362);
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
		public TerminalNode MINUS() { return getToken(IQLParser.MINUS, 0); }
		public TerminalNode GT() { return getToken(IQLParser.GT, 0); }
		public DirectedEdgeRightContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_directedEdgeRight; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterDirectedEdgeRight(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitDirectedEdgeRight(this);
		}
	}

	public final DirectedEdgeRightContext directedEdgeRight() throws RecognitionException {
		DirectedEdgeRightContext _localctx = new DirectedEdgeRightContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_directedEdgeRight);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(364);
			match(MINUS);
			setState(365);
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
		public List<TerminalNode> MINUS() { return getTokens(IQLParser.MINUS); }
		public TerminalNode MINUS(int i) {
			return getToken(IQLParser.MINUS, i);
		}
		public UndirectedEdgeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_undirectedEdge; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterUndirectedEdge(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitUndirectedEdge(this);
		}
	}

	public final UndirectedEdgeContext undirectedEdge() throws RecognitionException {
		UndirectedEdgeContext _localctx = new UndirectedEdgeContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_undirectedEdge);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(367);
			match(MINUS);
			setState(368);
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
		public TerminalNode GROUP() { return getToken(IQLParser.GROUP, 0); }
		public List<GroupExpressionContext> groupExpression() {
			return getRuleContexts(GroupExpressionContext.class);
		}
		public GroupExpressionContext groupExpression(int i) {
			return getRuleContext(GroupExpressionContext.class,i);
		}
		public TerminalNode EOF() { return getToken(IQLParser.EOF, 0); }
		public List<TerminalNode> COMMA() { return getTokens(IQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(IQLParser.COMMA, i);
		}
		public GroupStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterGroupStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitGroupStatement(this);
		}
	}

	public final GroupStatementContext groupStatement() throws RecognitionException {
		GroupStatementContext _localctx = new GroupStatementContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_groupStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(370);
			match(GROUP);
			setState(371);
			groupExpression();
			setState(376);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(372);
				match(COMMA);
				setState(373);
				groupExpression();
				}
				}
				setState(378);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(379);
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
		public TerminalNode BY() { return getToken(IQLParser.BY, 0); }
		public TerminalNode LABEL() { return getToken(IQLParser.LABEL, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode StringLiteral() { return getToken(IQLParser.StringLiteral, 0); }
		public TerminalNode FILTER() { return getToken(IQLParser.FILTER, 0); }
		public TerminalNode ON() { return getToken(IQLParser.ON, 0); }
		public TerminalNode DEFAULT() { return getToken(IQLParser.DEFAULT, 0); }
		public GroupExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_groupExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterGroupExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitGroupExpression(this);
		}
	}

	public final GroupExpressionContext groupExpression() throws RecognitionException {
		GroupExpressionContext _localctx = new GroupExpressionContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_groupExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(381);
			match(BY);
			setState(382);
			((GroupExpressionContext)_localctx).selector = expression(0);
			setState(386);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FILTER) {
				{
				setState(383);
				match(FILTER);
				setState(384);
				match(ON);
				setState(385);
				((GroupExpressionContext)_localctx).filter = expression(0);
				}
			}

			setState(388);
			match(LABEL);
			setState(389);
			((GroupExpressionContext)_localctx).label = match(StringLiteral);
			setState(392);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DEFAULT) {
				{
				setState(390);
				match(DEFAULT);
				setState(391);
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
		public TerminalNode EOF() { return getToken(IQLParser.EOF, 0); }
		public TerminalNode LIMIT() { return getToken(IQLParser.LIMIT, 0); }
		public OrderExpressionListContext orderExpressionList() {
			return getRuleContext(OrderExpressionListContext.class,0);
		}
		public UnsignedIntegerLiteralContext unsignedIntegerLiteral() {
			return getRuleContext(UnsignedIntegerLiteralContext.class,0);
		}
		public TerminalNode FIRST() { return getToken(IQLParser.FIRST, 0); }
		public ResultStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_resultStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterResultStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitResultStatement(this);
		}
	}

	public final ResultStatementContext resultStatement() throws RecognitionException {
		ResultStatementContext _localctx = new ResultStatementContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_resultStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(399);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LIMIT) {
				{
				setState(394);
				match(LIMIT);
				setState(396);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==FIRST) {
					{
					setState(395);
					match(FIRST);
					}
				}

				setState(398);
				((ResultStatementContext)_localctx).limit = unsignedIntegerLiteral();
				}
			}

			setState(402);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ORDER) {
				{
				setState(401);
				orderExpressionList();
				}
			}

			setState(404);
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
		public TerminalNode ORDER() { return getToken(IQLParser.ORDER, 0); }
		public TerminalNode BY() { return getToken(IQLParser.BY, 0); }
		public List<OrderExpressionContext> orderExpression() {
			return getRuleContexts(OrderExpressionContext.class);
		}
		public OrderExpressionContext orderExpression(int i) {
			return getRuleContext(OrderExpressionContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(IQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(IQLParser.COMMA, i);
		}
		public OrderExpressionListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderExpressionList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterOrderExpressionList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitOrderExpressionList(this);
		}
	}

	public final OrderExpressionListContext orderExpressionList() throws RecognitionException {
		OrderExpressionListContext _localctx = new OrderExpressionListContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_orderExpressionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(406);
			match(ORDER);
			setState(407);
			match(BY);
			setState(408);
			orderExpression();
			setState(413);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(409);
				match(COMMA);
				setState(410);
				orderExpression();
				}
				}
				setState(415);
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
		public TerminalNode ASC() { return getToken(IQLParser.ASC, 0); }
		public TerminalNode DESC() { return getToken(IQLParser.DESC, 0); }
		public OrderExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderExpression; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterOrderExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitOrderExpression(this);
		}
	}

	public final OrderExpressionContext orderExpression() throws RecognitionException {
		OrderExpressionContext _localctx = new OrderExpressionContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_orderExpression);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(416);
			expression(0);
			setState(417);
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterConstraint(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitConstraint(this);
		}
	}

	public final ConstraintContext constraint() throws RecognitionException {
		ConstraintContext _localctx = new ConstraintContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_constraint);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(419);
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
		public List<TerminalNode> COMMA() { return getTokens(IQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(IQLParser.COMMA, i);
		}
		public ExpressionListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterExpressionList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitExpressionList(this);
		}
	}

	public final ExpressionListContext expressionList() throws RecognitionException {
		ExpressionListContext _localctx = new ExpressionListContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_expressionList);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(421);
			expression(0);
			setState(426);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==COMMA) {
				{
				{
				setState(422);
				match(COMMA);
				setState(423);
				expression(0);
				}
				}
				setState(428);
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterPrimaryExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitPrimaryExpression(this);
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterDisjunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitDisjunction(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PathAccessContext extends ExpressionContext {
		public ExpressionContext source;
		public TerminalNode DOT() { return getToken(IQLParser.DOT, 0); }
		public TerminalNode Identifier() { return getToken(IQLParser.Identifier, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public PathAccessContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterPathAccess(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitPathAccess(this);
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterForEach(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitForEach(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class MethodInvocationContext extends ExpressionContext {
		public ExpressionContext source;
		public ExpressionListContext arguments;
		public TerminalNode LPAREN() { return getToken(IQLParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(IQLParser.RPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public MethodInvocationContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterMethodInvocation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitMethodInvocation(this);
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
		public TerminalNode PLUS() { return getToken(IQLParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(IQLParser.MINUS, 0); }
		public AdditiveOpContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterAdditiveOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitAdditiveOp(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class UnaryOpContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode NOT() { return getToken(IQLParser.NOT, 0); }
		public TerminalNode EXMARK() { return getToken(IQLParser.EXMARK, 0); }
		public TerminalNode MINUS() { return getToken(IQLParser.MINUS, 0); }
		public TerminalNode TILDE() { return getToken(IQLParser.TILDE, 0); }
		public UnaryOpContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterUnaryOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitUnaryOp(this);
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
		public TerminalNode LT() { return getToken(IQLParser.LT, 0); }
		public TerminalNode LT_EQ() { return getToken(IQLParser.LT_EQ, 0); }
		public TerminalNode GT() { return getToken(IQLParser.GT, 0); }
		public TerminalNode GT_EQ() { return getToken(IQLParser.GT_EQ, 0); }
		public ComparisonOpContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterComparisonOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitComparisonOp(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AssignmentOpContext extends ExpressionContext {
		public ExpressionContext source;
		public TerminalNode AS() { return getToken(IQLParser.AS, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public MemberContext member() {
			return getRuleContext(MemberContext.class,0);
		}
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public TerminalNode OPTIONAL() { return getToken(IQLParser.OPTIONAL, 0); }
		public AssignmentOpContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterAssignmentOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitAssignmentOp(this);
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
		public TerminalNode MATCHES() { return getToken(IQLParser.MATCHES, 0); }
		public TerminalNode NOT_MATCHES() { return getToken(IQLParser.NOT_MATCHES, 0); }
		public TerminalNode CONTAINS() { return getToken(IQLParser.CONTAINS, 0); }
		public TerminalNode NOT_CONTAINS() { return getToken(IQLParser.NOT_CONTAINS, 0); }
		public StringOpContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterStringOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitStringOp(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class WrappingExpressionContext extends ExpressionContext {
		public TerminalNode LPAREN() { return getToken(IQLParser.LPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(IQLParser.RPAREN, 0); }
		public WrappingExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterWrappingExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitWrappingExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CastExpressionContext extends ExpressionContext {
		public TerminalNode LPAREN() { return getToken(IQLParser.LPAREN, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(IQLParser.RPAREN, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public CastExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterCastExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitCastExpression(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ListAccessContext extends ExpressionContext {
		public ExpressionContext source;
		public ExpressionListContext indices;
		public TerminalNode LBRACK() { return getToken(IQLParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(IQLParser.RBRACK, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public ListAccessContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterListAccess(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitListAccess(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class SetPredicateContext extends ExpressionContext {
		public ExpressionContext source;
		public ExpressionContext target;
		public TerminalNode IN() { return getToken(IQLParser.IN, 0); }
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterSetPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitSetPredicate(this);
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
		public TerminalNode EQ() { return getToken(IQLParser.EQ, 0); }
		public TerminalNode NOT_EQ() { return getToken(IQLParser.NOT_EQ, 0); }
		public EqualityCheckContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterEqualityCheck(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitEqualityCheck(this);
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterConjunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitConjunction(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class AnnotationAccessContext extends ExpressionContext {
		public ExpressionContext source;
		public ExpressionListContext keys;
		public TerminalNode LBRACE() { return getToken(IQLParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(IQLParser.RBRACE, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public AnnotationAccessContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterAnnotationAccess(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitAnnotationAccess(this);
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
		public TerminalNode STAR() { return getToken(IQLParser.STAR, 0); }
		public TerminalNode SLASH() { return getToken(IQLParser.SLASH, 0); }
		public TerminalNode PERCENT() { return getToken(IQLParser.PERCENT, 0); }
		public MultiplicativeOpContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterMultiplicativeOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitMultiplicativeOp(this);
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
		public TerminalNode SHIFT_LEFT() { return getToken(IQLParser.SHIFT_LEFT, 0); }
		public TerminalNode SHIFT_RIGHT() { return getToken(IQLParser.SHIFT_RIGHT, 0); }
		public TerminalNode PIPE() { return getToken(IQLParser.PIPE, 0); }
		public TerminalNode AMP() { return getToken(IQLParser.AMP, 0); }
		public TerminalNode CARET() { return getToken(IQLParser.CARET, 0); }
		public BitwiseOpContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterBitwiseOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitBitwiseOp(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class TernaryOpContext extends ExpressionContext {
		public ExpressionContext condition;
		public ExpressionContext optionTrue;
		public ExpressionContext optionFalse;
		public TerminalNode QMARK() { return getToken(IQLParser.QMARK, 0); }
		public TerminalNode COLON() { return getToken(IQLParser.COLON, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TernaryOpContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterTernaryOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitTernaryOp(this);
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
		int _startState = 66;
		enterRecursionRule(_localctx, 66, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(443);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,48,_ctx) ) {
			case 1:
				{
				_localctx = new PrimaryExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(430);
				primary();
				}
				break;
			case 2:
				{
				_localctx = new CastExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(431);
				match(LPAREN);
				setState(432);
				type();
				setState(433);
				match(RPAREN);
				setState(434);
				expression(15);
				}
				break;
			case 3:
				{
				_localctx = new WrappingExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(436);
				match(LPAREN);
				setState(437);
				expression(0);
				setState(438);
				match(RPAREN);
				}
				break;
			case 4:
				{
				_localctx = new UnaryOpContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(440);
				_la = _input.LA(1);
				if ( !(((((_la - 30)) & ~0x3f) == 0 && ((1L << (_la - 30)) & 107752139522049L) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(441);
				expression(13);
				}
				break;
			case 5:
				{
				_localctx = new ForEachContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(442);
				loopExpresseion();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(519);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,55,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(517);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,54,_ctx) ) {
					case 1:
						{
						_localctx = new MultiplicativeOpContext(new ExpressionContext(_parentctx, _parentState));
						((MultiplicativeOpContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(445);
						if (!(precpred(_ctx, 12))) throw new FailedPredicateException(this, "precpred(_ctx, 12)");
						setState(446);
						_la = _input.LA(1);
						if ( !(((((_la - 73)) & ~0x3f) == 0 && ((1L << (_la - 73)) & 49L) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(447);
						((MultiplicativeOpContext)_localctx).right = expression(13);
						}
						break;
					case 2:
						{
						_localctx = new AdditiveOpContext(new ExpressionContext(_parentctx, _parentState));
						((AdditiveOpContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(448);
						if (!(precpred(_ctx, 11))) throw new FailedPredicateException(this, "precpred(_ctx, 11)");
						setState(449);
						_la = _input.LA(1);
						if ( !(_la==PLUS || _la==MINUS) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(450);
						((AdditiveOpContext)_localctx).right = expression(12);
						}
						break;
					case 3:
						{
						_localctx = new BitwiseOpContext(new ExpressionContext(_parentctx, _parentState));
						((BitwiseOpContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(451);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(452);
						_la = _input.LA(1);
						if ( !(((((_la - 79)) & ~0x3f) == 0 && ((1L << (_la - 79)) & 109L) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(453);
						((BitwiseOpContext)_localctx).right = expression(11);
						}
						break;
					case 4:
						{
						_localctx = new ComparisonOpContext(new ExpressionContext(_parentctx, _parentState));
						((ComparisonOpContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(454);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(455);
						_la = _input.LA(1);
						if ( !(((((_la - 86)) & ~0x3f) == 0 && ((1L << (_la - 86)) & 15L) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(456);
						((ComparisonOpContext)_localctx).right = expression(10);
						}
						break;
					case 5:
						{
						_localctx = new StringOpContext(new ExpressionContext(_parentctx, _parentState));
						((StringOpContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(457);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(458);
						_la = _input.LA(1);
						if ( !(((((_la - 92)) & ~0x3f) == 0 && ((1L << (_la - 92)) & 15L) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(459);
						((StringOpContext)_localctx).right = expression(9);
						}
						break;
					case 6:
						{
						_localctx = new EqualityCheckContext(new ExpressionContext(_parentctx, _parentState));
						((EqualityCheckContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(460);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(461);
						_la = _input.LA(1);
						if ( !(_la==EQ || _la==NOT_EQ) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(462);
						((EqualityCheckContext)_localctx).right = expression(8);
						}
						break;
					case 7:
						{
						_localctx = new ConjunctionContext(new ExpressionContext(_parentctx, _parentState));
						((ConjunctionContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(463);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(464);
						and();
						setState(465);
						((ConjunctionContext)_localctx).right = expression(6);
						}
						break;
					case 8:
						{
						_localctx = new DisjunctionContext(new ExpressionContext(_parentctx, _parentState));
						((DisjunctionContext)_localctx).left = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(467);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(468);
						or();
						setState(469);
						((DisjunctionContext)_localctx).right = expression(5);
						}
						break;
					case 9:
						{
						_localctx = new SetPredicateContext(new ExpressionContext(_parentctx, _parentState));
						((SetPredicateContext)_localctx).source = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(471);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(473);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==ALL || _la==STAR) {
							{
							setState(472);
							all();
							}
						}

						setState(476);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==NOT || _la==EXMARK) {
							{
							setState(475);
							not();
							}
						}

						setState(478);
						match(IN);
						setState(479);
						((SetPredicateContext)_localctx).target = expression(4);
						}
						break;
					case 10:
						{
						_localctx = new TernaryOpContext(new ExpressionContext(_parentctx, _parentState));
						((TernaryOpContext)_localctx).condition = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(480);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(481);
						match(QMARK);
						setState(482);
						((TernaryOpContext)_localctx).optionTrue = expression(0);
						setState(483);
						match(COLON);
						setState(484);
						((TernaryOpContext)_localctx).optionFalse = expression(3);
						}
						break;
					case 11:
						{
						_localctx = new PathAccessContext(new ExpressionContext(_parentctx, _parentState));
						((PathAccessContext)_localctx).source = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(486);
						if (!(precpred(_ctx, 19))) throw new FailedPredicateException(this, "precpred(_ctx, 19)");
						setState(487);
						match(DOT);
						setState(488);
						match(Identifier);
						}
						break;
					case 12:
						{
						_localctx = new MethodInvocationContext(new ExpressionContext(_parentctx, _parentState));
						((MethodInvocationContext)_localctx).source = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(489);
						if (!(precpred(_ctx, 18))) throw new FailedPredicateException(this, "precpred(_ctx, 18)");
						setState(490);
						if (!(isAny(-1,Identifier))) throw new FailedPredicateException(this, "isAny(-1,Identifier)");
						setState(491);
						match(LPAREN);
						setState(493);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 183882327852711936L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 32212255635L) != 0)) {
							{
							setState(492);
							((MethodInvocationContext)_localctx).arguments = expressionList();
							}
						}

						setState(495);
						match(RPAREN);
						}
						break;
					case 13:
						{
						_localctx = new ListAccessContext(new ExpressionContext(_parentctx, _parentState));
						((ListAccessContext)_localctx).source = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(496);
						if (!(precpred(_ctx, 17))) throw new FailedPredicateException(this, "precpred(_ctx, 17)");
						setState(497);
						if (!(isAny(-1,Identifier,RPAREN,RBRACE,RBRACK))) throw new FailedPredicateException(this, "isAny(-1,Identifier,RPAREN,RBRACE,RBRACK)");
						setState(498);
						match(LBRACK);
						setState(499);
						((ListAccessContext)_localctx).indices = expressionList();
						setState(500);
						match(RBRACK);
						}
						break;
					case 14:
						{
						_localctx = new AnnotationAccessContext(new ExpressionContext(_parentctx, _parentState));
						((AnnotationAccessContext)_localctx).source = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(502);
						if (!(precpred(_ctx, 16))) throw new FailedPredicateException(this, "precpred(_ctx, 16)");
						setState(503);
						if (!(isAny(-1,Identifier,RPAREN,RBRACK))) throw new FailedPredicateException(this, "isAny(-1,Identifier,RPAREN,RBRACK)");
						setState(504);
						match(LBRACE);
						setState(505);
						((AnnotationAccessContext)_localctx).keys = expressionList();
						setState(506);
						match(RBRACE);
						}
						break;
					case 15:
						{
						_localctx = new AssignmentOpContext(new ExpressionContext(_parentctx, _parentState));
						((AssignmentOpContext)_localctx).source = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(508);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(509);
						match(AS);
						setState(511);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (_la==OPTIONAL) {
							{
							setState(510);
							match(OPTIONAL);
							}
						}

						setState(515);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case DOLLAR:
							{
							setState(513);
							member();
							}
							break;
						case AT:
							{
							setState(514);
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
				setState(521);
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
		public TerminalNode StringLiteral() { return getToken(IQLParser.StringLiteral, 0); }
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterPrimary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitPrimary(this);
		}
	}

	public final PrimaryContext primary() throws RecognitionException {
		PrimaryContext _localctx = new PrimaryContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_primary);
		try {
			setState(529);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,56,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(522);
				nullLiteral();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(523);
				booleanLiteral();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(524);
				floatingPointLiteral();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(525);
				integerLiteral();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(526);
				match(StringLiteral);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(527);
				listStatement();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(528);
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
		public TerminalNode LBRACE() { return getToken(IQLParser.LBRACE, 0); }
		public TerminalNode RBRACE() { return getToken(IQLParser.RBRACE, 0); }
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode LBRACK() { return getToken(IQLParser.LBRACK, 0); }
		public TerminalNode RBRACK() { return getToken(IQLParser.RBRACK, 0); }
		public ExpressionListContext expressionList() {
			return getRuleContext(ExpressionListContext.class,0);
		}
		public ListStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_listStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterListStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitListStatement(this);
		}
	}

	public final ListStatementContext listStatement() throws RecognitionException {
		ListStatementContext _localctx = new ListStatementContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_listStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(535);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 3729543441416192L) != 0)) {
				{
				setState(531);
				type();
				setState(532);
				match(LBRACK);
				setState(533);
				match(RBRACK);
				}
			}

			setState(537);
			match(LBRACE);
			setState(539);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & 183882327852711936L) != 0) || ((((_la - 67)) & ~0x3f) == 0 && ((1L << (_la - 67)) & 32212255635L) != 0)) {
				{
				setState(538);
				expressionList();
				}
			}

			setState(541);
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
		public TerminalNode Identifier() { return getToken(IQLParser.Identifier, 0); }
		public QualifiedIdentifierContext qualifiedIdentifier() {
			return getRuleContext(QualifiedIdentifierContext.class,0);
		}
		public ReferenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_reference; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitReference(this);
		}
	}

	public final ReferenceContext reference() throws RecognitionException {
		ReferenceContext _localctx = new ReferenceContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_reference);
		try {
			setState(547);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,59,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(543);
				variable();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(544);
				member();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(545);
				match(Identifier);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(546);
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
		public TerminalNode DOUBLE_COLON() { return getToken(IQLParser.DOUBLE_COLON, 0); }
		public List<TerminalNode> Identifier() { return getTokens(IQLParser.Identifier); }
		public TerminalNode Identifier(int i) {
			return getToken(IQLParser.Identifier, i);
		}
		public QualifiedIdentifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qualifiedIdentifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterQualifiedIdentifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitQualifiedIdentifier(this);
		}
	}

	public final QualifiedIdentifierContext qualifiedIdentifier() throws RecognitionException {
		QualifiedIdentifierContext _localctx = new QualifiedIdentifierContext(_ctx, getState());
		enterRule(_localctx, 74, RULE_qualifiedIdentifier);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(549);
			((QualifiedIdentifierContext)_localctx).hostId = match(Identifier);
			setState(550);
			match(DOUBLE_COLON);
			setState(551);
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
		public TerminalNode FOREACH() { return getToken(IQLParser.FOREACH, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode AS() { return getToken(IQLParser.AS, 0); }
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterLoopExpresseion(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitLoopExpresseion(this);
		}
	}

	public final LoopExpresseionContext loopExpresseion() throws RecognitionException {
		LoopExpresseionContext _localctx = new LoopExpresseionContext(_ctx, getState());
		enterRule(_localctx, 76, RULE_loopExpresseion);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(553);
			match(FOREACH);
			setState(554);
			expression(0);
			setState(555);
			match(AS);
			setState(556);
			variable();
			setState(557);
			loopControl();
			setState(559);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,60,_ctx) ) {
			case 1:
				{
				setState(558);
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
		public TerminalNode END() { return getToken(IQLParser.END, 0); }
		public TerminalNode OMIT() { return getToken(IQLParser.OMIT, 0); }
		public TerminalNode RANGE() { return getToken(IQLParser.RANGE, 0); }
		public TerminalNode STEP() { return getToken(IQLParser.STEP, 0); }
		public TerminalNode DO() { return getToken(IQLParser.DO, 0); }
		public TerminalNode EVEN() { return getToken(IQLParser.EVEN, 0); }
		public TerminalNode ODD() { return getToken(IQLParser.ODD, 0); }
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterLoopControl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitLoopControl(this);
		}
	}

	public final LoopControlContext loopControl() throws RecognitionException {
		LoopControlContext _localctx = new LoopControlContext(_ctx, getState());
		enterRule(_localctx, 78, RULE_loopControl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(562);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==EVEN || _la==ODD) {
				{
				setState(561);
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

			setState(566);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==OMIT) {
				{
				setState(564);
				match(OMIT);
				setState(565);
				((LoopControlContext)_localctx).omit = constraint();
				}
			}

			setState(570);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==RANGE) {
				{
				setState(568);
				match(RANGE);
				setState(569);
				((LoopControlContext)_localctx).range = boundedRange();
				}
			}

			setState(574);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==STEP) {
				{
				setState(572);
				match(STEP);
				setState(573);
				((LoopControlContext)_localctx).step = boundedRange();
				}
			}

			setState(578);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DO) {
				{
				setState(576);
				match(DO);
				setState(577);
				((LoopControlContext)_localctx).body = constraint();
				}
			}

			setState(580);
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
		public TerminalNode LPAREN() { return getToken(IQLParser.LPAREN, 0); }
		public TerminalNode COMMA() { return getToken(IQLParser.COMMA, 0); }
		public TerminalNode RPAREN() { return getToken(IQLParser.RPAREN, 0); }
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterBoundedRange(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitBoundedRange(this);
		}
	}

	public final BoundedRangeContext boundedRange() throws RecognitionException {
		BoundedRangeContext _localctx = new BoundedRangeContext(_ctx, getState());
		enterRule(_localctx, 80, RULE_boundedRange);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(582);
			match(LPAREN);
			setState(583);
			((BoundedRangeContext)_localctx).from = expression(0);
			setState(584);
			match(COMMA);
			setState(585);
			((BoundedRangeContext)_localctx).to = expression(0);
			setState(586);
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
		public TerminalNode COUNT() { return getToken(IQLParser.COUNT, 0); }
		public List<CounterContext> counter() {
			return getRuleContexts(CounterContext.class);
		}
		public CounterContext counter(int i) {
			return getRuleContext(CounterContext.class,i);
		}
		public List<TerminalNode> COMMA() { return getTokens(IQLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(IQLParser.COMMA, i);
		}
		public CounterListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_counterList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterCounterList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitCounterList(this);
		}
	}

	public final CounterListContext counterList() throws RecognitionException {
		CounterListContext _localctx = new CounterListContext(_ctx, getState());
		enterRule(_localctx, 82, RULE_counterList);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(588);
			match(COUNT);
			setState(589);
			counter();
			setState(594);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,66,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(590);
					match(COMMA);
					setState(591);
					counter();
					}
					} 
				}
				setState(596);
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
		public TerminalNode AS() { return getToken(IQLParser.AS, 0); }
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public CounterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_counter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterCounter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitCounter(this);
		}
	}

	public final CounterContext counter() throws RecognitionException {
		CounterContext _localctx = new CounterContext(_ctx, getState());
		enterRule(_localctx, 84, RULE_counter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(597);
			constraint();
			setState(598);
			match(AS);
			setState(599);
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
		public TerminalNode BOOLEAN() { return getToken(IQLParser.BOOLEAN, 0); }
		public TerminalNode STRING() { return getToken(IQLParser.STRING, 0); }
		public TerminalNode INT() { return getToken(IQLParser.INT, 0); }
		public TerminalNode FLOAT() { return getToken(IQLParser.FLOAT, 0); }
		public TypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitType(this);
		}
	}

	public final TypeContext type() throws RecognitionException {
		TypeContext _localctx = new TypeContext(_ctx, getState());
		enterRule(_localctx, 86, RULE_type);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(601);
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
		public List<TerminalNode> PIPE() { return getTokens(IQLParser.PIPE); }
		public TerminalNode PIPE(int i) {
			return getToken(IQLParser.PIPE, i);
		}
		public TerminalNode LT() { return getToken(IQLParser.LT, 0); }
		public TerminalNode GT() { return getToken(IQLParser.GT, 0); }
		public QuantifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_quantifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterQuantifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitQuantifier(this);
		}
	}

	public final QuantifierContext quantifier() throws RecognitionException {
		QuantifierContext _localctx = new QuantifierContext(_ctx, getState());
		enterRule(_localctx, 88, RULE_quantifier);
		int _la;
		try {
			setState(622);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ALL:
			case NOT:
			case EXMARK:
			case STAR:
			case PureDigits:
				enterOuterAlt(_localctx, 1);
				{
				setState(603);
				simpleQuantifier();
				setState(608);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==PIPE) {
					{
					{
					setState(604);
					match(PIPE);
					setState(605);
					simpleQuantifier();
					}
					}
					setState(610);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				}
				break;
			case LT:
				enterOuterAlt(_localctx, 2);
				{
				setState(611);
				match(LT);
				setState(612);
				simpleQuantifier();
				setState(617);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==PIPE) {
					{
					{
					setState(613);
					match(PIPE);
					setState(614);
					simpleQuantifier();
					}
					}
					setState(619);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(620);
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
		public TerminalNode QMARK() { return getToken(IQLParser.QMARK, 0); }
		public TerminalNode PLUS() { return getToken(IQLParser.PLUS, 0); }
		public List<TerminalNode> PureDigits() { return getTokens(IQLParser.PureDigits); }
		public TerminalNode PureDigits(int i) {
			return getToken(IQLParser.PureDigits, i);
		}
		public TerminalNode CARET() { return getToken(IQLParser.CARET, 0); }
		public TerminalNode MINUS() { return getToken(IQLParser.MINUS, 0); }
		public TerminalNode EXMARK() { return getToken(IQLParser.EXMARK, 0); }
		public TerminalNode DOUBLE_DOT() { return getToken(IQLParser.DOUBLE_DOT, 0); }
		public SimpleQuantifierContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_simpleQuantifier; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterSimpleQuantifier(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitSimpleQuantifier(this);
		}
	}

	public final SimpleQuantifierContext simpleQuantifier() throws RecognitionException {
		SimpleQuantifierContext _localctx = new SimpleQuantifierContext(_ctx, getState());
		enterRule(_localctx, 90, RULE_simpleQuantifier);
		int _la;
		try {
			setState(648);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,76,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(624);
				not();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(625);
				all();
				setState(627);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==QMARK || _la==PLUS) {
					{
					setState(626);
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
				setState(629);
				((SimpleQuantifierContext)_localctx).value = match(PureDigits);
				setState(631);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==PLUS || _la==MINUS) {
					{
					setState(630);
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

				setState(634);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==QMARK || _la==EXMARK) {
					{
					setState(633);
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

				setState(637);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CARET) {
					{
					setState(636);
					match(CARET);
					}
				}

				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(639);
				((SimpleQuantifierContext)_localctx).lowerBound = match(PureDigits);
				setState(640);
				match(DOUBLE_DOT);
				setState(641);
				((SimpleQuantifierContext)_localctx).upperBound = match(PureDigits);
				setState(643);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==QMARK || _la==EXMARK) {
					{
					setState(642);
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

				setState(646);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==CARET) {
					{
					setState(645);
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
		public TerminalNode NOT() { return getToken(IQLParser.NOT, 0); }
		public TerminalNode EXMARK() { return getToken(IQLParser.EXMARK, 0); }
		public NotContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_not; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterNot(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitNot(this);
		}
	}

	public final NotContext not() throws RecognitionException {
		NotContext _localctx = new NotContext(_ctx, getState());
		enterRule(_localctx, 92, RULE_not);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(650);
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
		public TerminalNode STAR() { return getToken(IQLParser.STAR, 0); }
		public TerminalNode ALL() { return getToken(IQLParser.ALL, 0); }
		public AllContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_all; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterAll(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitAll(this);
		}
	}

	public final AllContext all() throws RecognitionException {
		AllContext _localctx = new AllContext(_ctx, getState());
		enterRule(_localctx, 94, RULE_all);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(652);
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
		public TerminalNode AND() { return getToken(IQLParser.AND, 0); }
		public TerminalNode DOUBLE_AMP() { return getToken(IQLParser.DOUBLE_AMP, 0); }
		public AndContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_and; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterAnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitAnd(this);
		}
	}

	public final AndContext and() throws RecognitionException {
		AndContext _localctx = new AndContext(_ctx, getState());
		enterRule(_localctx, 96, RULE_and);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(654);
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
		public TerminalNode OR() { return getToken(IQLParser.OR, 0); }
		public TerminalNode DOUBLE_PIPE() { return getToken(IQLParser.DOUBLE_PIPE, 0); }
		public OrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_or; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterOr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitOr(this);
		}
	}

	public final OrContext or() throws RecognitionException {
		OrContext _localctx = new OrContext(_ctx, getState());
		enterRule(_localctx, 98, RULE_or);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(656);
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
		public TerminalNode AT() { return getToken(IQLParser.AT, 0); }
		public TerminalNode Identifier() { return getToken(IQLParser.Identifier, 0); }
		public VariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitVariable(this);
		}
	}

	public final VariableContext variable() throws RecognitionException {
		VariableContext _localctx = new VariableContext(_ctx, getState());
		enterRule(_localctx, 100, RULE_variable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(658);
			match(AT);
			setState(659);
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
		public TerminalNode DOLLAR() { return getToken(IQLParser.DOLLAR, 0); }
		public TerminalNode Identifier() { return getToken(IQLParser.Identifier, 0); }
		public MemberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_member; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterMember(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitMember(this);
		}
	}

	public final MemberContext member() throws RecognitionException {
		MemberContext _localctx = new MemberContext(_ctx, getState());
		enterRule(_localctx, 102, RULE_member);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(661);
			match(DOLLAR);
			setState(663);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,77,_ctx) ) {
			case 1:
				{
				setState(662);
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
		public List<TerminalNode> PureDigits() { return getTokens(IQLParser.PureDigits); }
		public TerminalNode PureDigits(int i) {
			return getToken(IQLParser.PureDigits, i);
		}
		public List<TerminalNode> DOT() { return getTokens(IQLParser.DOT); }
		public TerminalNode DOT(int i) {
			return getToken(IQLParser.DOT, i);
		}
		public TerminalNode MINUS() { return getToken(IQLParser.MINUS, 0); }
		public TerminalNode UNDERSCORE() { return getToken(IQLParser.UNDERSCORE, 0); }
		public TerminalNode COLON() { return getToken(IQLParser.COLON, 0); }
		public TerminalNode Identifier() { return getToken(IQLParser.Identifier, 0); }
		public VersionDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_versionDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterVersionDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitVersionDeclaration(this);
		}
	}

	public final VersionDeclarationContext versionDeclaration() throws RecognitionException {
		VersionDeclarationContext _localctx = new VersionDeclarationContext(_ctx, getState());
		enterRule(_localctx, 104, RULE_versionDeclaration);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(665);
			((VersionDeclarationContext)_localctx).major = match(PureDigits);
			setState(668);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,78,_ctx) ) {
			case 1:
				{
				setState(666);
				match(DOT);
				setState(667);
				((VersionDeclarationContext)_localctx).minor = match(PureDigits);
				}
				break;
			}
			setState(672);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DOT) {
				{
				setState(670);
				match(DOT);
				setState(671);
				((VersionDeclarationContext)_localctx).build = match(PureDigits);
				}
			}

			setState(675);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((((_la - 53)) & ~0x3f) == 0 && ((1L << (_la - 53)) & 4194817L) != 0)) {
				{
				setState(674);
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

			setState(678);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Identifier) {
				{
				setState(677);
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
		public TerminalNode NULL() { return getToken(IQLParser.NULL, 0); }
		public NullLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_nullLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterNullLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitNullLiteral(this);
		}
	}

	public final NullLiteralContext nullLiteral() throws RecognitionException {
		NullLiteralContext _localctx = new NullLiteralContext(_ctx, getState());
		enterRule(_localctx, 106, RULE_nullLiteral);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(680);
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterFloatingPointLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitFloatingPointLiteral(this);
		}
	}

	public final FloatingPointLiteralContext floatingPointLiteral() throws RecognitionException {
		FloatingPointLiteralContext _localctx = new FloatingPointLiteralContext(_ctx, getState());
		enterRule(_localctx, 108, RULE_floatingPointLiteral);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(682);
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterSignedFloatingPointLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitSignedFloatingPointLiteral(this);
		}
	}

	public final SignedFloatingPointLiteralContext signedFloatingPointLiteral() throws RecognitionException {
		SignedFloatingPointLiteralContext _localctx = new SignedFloatingPointLiteralContext(_ctx, getState());
		enterRule(_localctx, 110, RULE_signedFloatingPointLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(685);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PLUS || _la==MINUS) {
				{
				setState(684);
				sign();
				}
			}

			setState(687);
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
		public TerminalNode DOT() { return getToken(IQLParser.DOT, 0); }
		public UnsignedFloatingPointLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unsignedFloatingPointLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterUnsignedFloatingPointLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitUnsignedFloatingPointLiteral(this);
		}
	}

	public final UnsignedFloatingPointLiteralContext unsignedFloatingPointLiteral() throws RecognitionException {
		UnsignedFloatingPointLiteralContext _localctx = new UnsignedFloatingPointLiteralContext(_ctx, getState());
		enterRule(_localctx, 112, RULE_unsignedFloatingPointLiteral);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(689);
			unsignedIntegerLiteral();
			setState(690);
			match(DOT);
			setState(691);
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
		public TerminalNode TRUE() { return getToken(IQLParser.TRUE, 0); }
		public TerminalNode FALSE() { return getToken(IQLParser.FALSE, 0); }
		public BooleanLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_booleanLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterBooleanLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitBooleanLiteral(this);
		}
	}

	public final BooleanLiteralContext booleanLiteral() throws RecognitionException {
		BooleanLiteralContext _localctx = new BooleanLiteralContext(_ctx, getState());
		enterRule(_localctx, 114, RULE_booleanLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(693);
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterIntegerLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitIntegerLiteral(this);
		}
	}

	public final IntegerLiteralContext integerLiteral() throws RecognitionException {
		IntegerLiteralContext _localctx = new IntegerLiteralContext(_ctx, getState());
		enterRule(_localctx, 116, RULE_integerLiteral);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(695);
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
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterSignedIntegerLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitSignedIntegerLiteral(this);
		}
	}

	public final SignedIntegerLiteralContext signedIntegerLiteral() throws RecognitionException {
		SignedIntegerLiteralContext _localctx = new SignedIntegerLiteralContext(_ctx, getState());
		enterRule(_localctx, 118, RULE_signedIntegerLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(698);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==PLUS || _la==MINUS) {
				{
				setState(697);
				sign();
				}
			}

			setState(700);
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
		public TerminalNode Digits() { return getToken(IQLParser.Digits, 0); }
		public TerminalNode PureDigits() { return getToken(IQLParser.PureDigits, 0); }
		public UnsignedIntegerLiteralContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_unsignedIntegerLiteral; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterUnsignedIntegerLiteral(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitUnsignedIntegerLiteral(this);
		}
	}

	public final UnsignedIntegerLiteralContext unsignedIntegerLiteral() throws RecognitionException {
		UnsignedIntegerLiteralContext _localctx = new UnsignedIntegerLiteralContext(_ctx, getState());
		enterRule(_localctx, 120, RULE_unsignedIntegerLiteral);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(702);
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
		public TerminalNode PLUS() { return getToken(IQLParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(IQLParser.MINUS, 0); }
		public SignContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sign; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).enterSign(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof IQLListener ) ((IQLListener)listener).exitSign(this);
		}
	}

	public final SignContext sign() throws RecognitionException {
		SignContext _localctx = new SignContext(_ctx, getState());
		enterRule(_localctx, 122, RULE_sign);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(704);
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
		case 13:
			return nodeStatement_sempred((NodeStatementContext)_localctx, predIndex);
		case 17:
			return positionMarker_sempred((PositionMarkerContext)_localctx, predIndex);
		case 33:
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
		"\u0004\u0001h\u02c3\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
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
		"<\u0007<\u0002=\u0007=\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004"+
		"\u008c\b\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004\u0091\b"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0003\u0004\u0097"+
		"\b\u0004\u0001\u0005\u0001\u0005\u0001\u0005\u0001\u0005\u0005\u0005\u009d"+
		"\b\u0005\n\u0005\f\u0005\u00a0\t\u0005\u0001\u0006\u0003\u0006\u00a3\b"+
		"\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0005\u0006\u00a8\b\u0006\n"+
		"\u0006\f\u0006\u00ab\t\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001"+
		"\u0007\u0001\u0007\u0001\u0007\u0003\u0007\u00b3\b\u0007\u0001\u0007\u0001"+
		"\u0007\u0003\u0007\u00b7\b\u0007\u0003\u0007\u00b9\b\u0007\u0001\b\u0001"+
		"\b\u0001\b\u0005\b\u00be\b\b\n\b\f\b\u00c1\t\b\u0001\t\u0001\t\u0001\t"+
		"\u0001\t\u0003\t\u00c7\b\t\u0001\t\u0001\t\u0001\n\u0003\n\u00cc\b\n\u0001"+
		"\n\u0005\n\u00cf\b\n\n\n\f\n\u00d2\t\n\u0001\n\u0004\n\u00d5\b\n\u000b"+
		"\n\f\n\u00d6\u0001\u000b\u0001\u000b\u0001\u000b\u0003\u000b\u00dc\b\u000b"+
		"\u0001\f\u0001\f\u0001\r\u0001\r\u0003\r\u00e2\b\r\u0001\r\u0001\r\u0004"+
		"\r\u00e6\b\r\u000b\r\f\r\u00e7\u0001\r\u0001\r\u0001\r\u0004\r\u00ed\b"+
		"\r\u000b\r\f\r\u00ee\u0001\r\u0004\r\u00f2\b\r\u000b\r\f\r\u00f3\u0001"+
		"\r\u0001\r\u0001\r\u0001\r\u0005\r\u00fa\b\r\n\r\f\r\u00fd\t\r\u0003\r"+
		"\u00ff\b\r\u0001\r\u0001\r\u0001\r\u0001\r\u0005\r\u0105\b\r\n\r\f\r\u0108"+
		"\t\r\u0001\u000e\u0001\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0003\u000f\u0110\b\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u0114"+
		"\b\u000f\u0001\u000f\u0001\u000f\u0001\u000f\u0003\u000f\u0119\b\u000f"+
		"\u0001\u000f\u0003\u000f\u011c\b\u000f\u0001\u000f\u0003\u000f\u011f\b"+
		"\u000f\u0001\u000f\u0003\u000f\u0122\b\u000f\u0001\u0010\u0001\u0010\u0001"+
		"\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001"+
		"\u0011\u0005\u0011\u012d\b\u0011\n\u0011\f\u0011\u0130\t\u0011\u0001\u0011"+
		"\u0001\u0011\u0003\u0011\u0134\b\u0011\u0001\u0011\u0001\u0011\u0001\u0011"+
		"\u0001\u0011\u0003\u0011\u013a\b\u0011\u0001\u0011\u0001\u0011\u0001\u0011"+
		"\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0001\u0011\u0005\u0011"+
		"\u0144\b\u0011\n\u0011\f\u0011\u0147\t\u0011\u0001\u0012\u0001\u0012\u0003"+
		"\u0012\u014b\b\u0012\u0001\u0013\u0001\u0013\u0001\u0013\u0001\u0013\u0001"+
		"\u0013\u0003\u0013\u0152\b\u0013\u0001\u0014\u0001\u0014\u0001\u0014\u0001"+
		"\u0014\u0003\u0014\u0158\b\u0014\u0001\u0014\u0003\u0014\u015b\b\u0014"+
		"\u0001\u0014\u0001\u0014\u0001\u0014\u0003\u0014\u0160\b\u0014\u0001\u0015"+
		"\u0001\u0015\u0003\u0015\u0164\b\u0015\u0001\u0016\u0001\u0016\u0003\u0016"+
		"\u0168\b\u0016\u0001\u0017\u0001\u0017\u0001\u0017\u0001\u0018\u0001\u0018"+
		"\u0001\u0018\u0001\u0019\u0001\u0019\u0001\u0019\u0001\u001a\u0001\u001a"+
		"\u0001\u001a\u0001\u001a\u0005\u001a\u0177\b\u001a\n\u001a\f\u001a\u017a"+
		"\t\u001a\u0001\u001a\u0001\u001a\u0001\u001b\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0001\u001b\u0003\u001b\u0183\b\u001b\u0001\u001b\u0001\u001b\u0001"+
		"\u001b\u0001\u001b\u0003\u001b\u0189\b\u001b\u0001\u001c\u0001\u001c\u0003"+
		"\u001c\u018d\b\u001c\u0001\u001c\u0003\u001c\u0190\b\u001c\u0001\u001c"+
		"\u0003\u001c\u0193\b\u001c\u0001\u001c\u0001\u001c\u0001\u001d\u0001\u001d"+
		"\u0001\u001d\u0001\u001d\u0001\u001d\u0005\u001d\u019c\b\u001d\n\u001d"+
		"\f\u001d\u019f\t\u001d\u0001\u001e\u0001\u001e\u0001\u001e\u0001\u001f"+
		"\u0001\u001f\u0001 \u0001 \u0001 \u0005 \u01a9\b \n \f \u01ac\t \u0001"+
		"!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001"+
		"!\u0001!\u0001!\u0001!\u0003!\u01bc\b!\u0001!\u0001!\u0001!\u0001!\u0001"+
		"!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001"+
		"!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001"+
		"!\u0001!\u0001!\u0001!\u0003!\u01da\b!\u0001!\u0003!\u01dd\b!\u0001!\u0001"+
		"!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001"+
		"!\u0001!\u0001!\u0001!\u0003!\u01ee\b!\u0001!\u0001!\u0001!\u0001!\u0001"+
		"!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001!\u0001"+
		"!\u0001!\u0003!\u0200\b!\u0001!\u0001!\u0003!\u0204\b!\u0005!\u0206\b"+
		"!\n!\f!\u0209\t!\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001\"\u0001"+
		"\"\u0003\"\u0212\b\"\u0001#\u0001#\u0001#\u0001#\u0003#\u0218\b#\u0001"+
		"#\u0001#\u0003#\u021c\b#\u0001#\u0001#\u0001$\u0001$\u0001$\u0001$\u0003"+
		"$\u0224\b$\u0001%\u0001%\u0001%\u0001%\u0001&\u0001&\u0001&\u0001&\u0001"+
		"&\u0001&\u0003&\u0230\b&\u0001\'\u0003\'\u0233\b\'\u0001\'\u0001\'\u0003"+
		"\'\u0237\b\'\u0001\'\u0001\'\u0003\'\u023b\b\'\u0001\'\u0001\'\u0003\'"+
		"\u023f\b\'\u0001\'\u0001\'\u0003\'\u0243\b\'\u0001\'\u0001\'\u0001(\u0001"+
		"(\u0001(\u0001(\u0001(\u0001(\u0001)\u0001)\u0001)\u0001)\u0005)\u0251"+
		"\b)\n)\f)\u0254\t)\u0001*\u0001*\u0001*\u0001*\u0001+\u0001+\u0001,\u0001"+
		",\u0001,\u0005,\u025f\b,\n,\f,\u0262\t,\u0001,\u0001,\u0001,\u0001,\u0005"+
		",\u0268\b,\n,\f,\u026b\t,\u0001,\u0001,\u0003,\u026f\b,\u0001-\u0001-"+
		"\u0001-\u0003-\u0274\b-\u0001-\u0001-\u0003-\u0278\b-\u0001-\u0003-\u027b"+
		"\b-\u0001-\u0003-\u027e\b-\u0001-\u0001-\u0001-\u0001-\u0003-\u0284\b"+
		"-\u0001-\u0003-\u0287\b-\u0003-\u0289\b-\u0001.\u0001.\u0001/\u0001/\u0001"+
		"0\u00010\u00011\u00011\u00012\u00012\u00012\u00013\u00013\u00033\u0298"+
		"\b3\u00014\u00014\u00014\u00034\u029d\b4\u00014\u00014\u00034\u02a1\b"+
		"4\u00014\u00034\u02a4\b4\u00014\u00034\u02a7\b4\u00015\u00015\u00016\u0001"+
		"6\u00017\u00037\u02ae\b7\u00017\u00017\u00018\u00018\u00018\u00018\u0001"+
		"9\u00019\u0001:\u0001:\u0001;\u0003;\u02bb\b;\u0001;\u0001;\u0001<\u0001"+
		"<\u0001=\u0001=\u0001=\u0000\u0003\u001a\"B>\u0000\u0002\u0004\u0006\b"+
		"\n\f\u000e\u0010\u0012\u0014\u0016\u0018\u001a\u001c\u001e \"$&(*,.02"+
		"468:<>@BDFHJLNPRTVXZ\\^`bdfhjlnprtvxz\u0000\u0018\u0002\u0000\f\f\u000e"+
		"\u000e\u0003\u0000\u0007\u0007\u000b\u000b()\u0003\u0000\u0001\u0001&"+
		"&,,\u0002\u0000FFIJ\u0001\u0000?B\u0002\u0000\u0005\u0005\n\n\u0003\u0000"+
		"\u001e\u001eGGKL\u0002\u0000IIMN\u0001\u0000JK\u0003\u0000OOQRTU\u0001"+
		"\u0000VY\u0001\u0000\\_\u0001\u0000Z[\u0002\u0000\u0010\u0010  \u0003"+
		"\u0000..0023\u0002\u0000FFJJ\u0001\u0000FG\u0002\u0000\u001e\u001eGG\u0002"+
		"\u0000\u0002\u0002II\u0002\u0000\u0003\u0003PP\u0002\u0000$$SS\u0003\u0000"+
		"55>>KK\u0002\u0000\u0011\u0011++\u0001\u0000bc\u02f3\u0000|\u0001\u0000"+
		"\u0000\u0000\u0002\u007f\u0001\u0000\u0000\u0000\u0004\u0082\u0001\u0000"+
		"\u0000\u0000\u0006\u0085\u0001\u0000\u0000\u0000\b\u0096\u0001\u0000\u0000"+
		"\u0000\n\u0098\u0001\u0000\u0000\u0000\f\u00a2\u0001\u0000\u0000\u0000"+
		"\u000e\u00b8\u0001\u0000\u0000\u0000\u0010\u00ba\u0001\u0000\u0000\u0000"+
		"\u0012\u00c2\u0001\u0000\u0000\u0000\u0014\u00cb\u0001\u0000\u0000\u0000"+
		"\u0016\u00db\u0001\u0000\u0000\u0000\u0018\u00dd\u0001\u0000\u0000\u0000"+
		"\u001a\u00fe\u0001\u0000\u0000\u0000\u001c\u0109\u0001\u0000\u0000\u0000"+
		"\u001e\u0121\u0001\u0000\u0000\u0000 \u0123\u0001\u0000\u0000\u0000\""+
		"\u0139\u0001\u0000\u0000\u0000$\u014a\u0001\u0000\u0000\u0000&\u0151\u0001"+
		"\u0000\u0000\u0000(\u015f\u0001\u0000\u0000\u0000*\u0163\u0001\u0000\u0000"+
		"\u0000,\u0167\u0001\u0000\u0000\u0000.\u0169\u0001\u0000\u0000\u00000"+
		"\u016c\u0001\u0000\u0000\u00002\u016f\u0001\u0000\u0000\u00004\u0172\u0001"+
		"\u0000\u0000\u00006\u017d\u0001\u0000\u0000\u00008\u018f\u0001\u0000\u0000"+
		"\u0000:\u0196\u0001\u0000\u0000\u0000<\u01a0\u0001\u0000\u0000\u0000>"+
		"\u01a3\u0001\u0000\u0000\u0000@\u01a5\u0001\u0000\u0000\u0000B\u01bb\u0001"+
		"\u0000\u0000\u0000D\u0211\u0001\u0000\u0000\u0000F\u0217\u0001\u0000\u0000"+
		"\u0000H\u0223\u0001\u0000\u0000\u0000J\u0225\u0001\u0000\u0000\u0000L"+
		"\u0229\u0001\u0000\u0000\u0000N\u0232\u0001\u0000\u0000\u0000P\u0246\u0001"+
		"\u0000\u0000\u0000R\u024c\u0001\u0000\u0000\u0000T\u0255\u0001\u0000\u0000"+
		"\u0000V\u0259\u0001\u0000\u0000\u0000X\u026e\u0001\u0000\u0000\u0000Z"+
		"\u0288\u0001\u0000\u0000\u0000\\\u028a\u0001\u0000\u0000\u0000^\u028c"+
		"\u0001\u0000\u0000\u0000`\u028e\u0001\u0000\u0000\u0000b\u0290\u0001\u0000"+
		"\u0000\u0000d\u0292\u0001\u0000\u0000\u0000f\u0295\u0001\u0000\u0000\u0000"+
		"h\u0299\u0001\u0000\u0000\u0000j\u02a8\u0001\u0000\u0000\u0000l\u02aa"+
		"\u0001\u0000\u0000\u0000n\u02ad\u0001\u0000\u0000\u0000p\u02b1\u0001\u0000"+
		"\u0000\u0000r\u02b5\u0001\u0000\u0000\u0000t\u02b7\u0001\u0000\u0000\u0000"+
		"v\u02ba\u0001\u0000\u0000\u0000x\u02be\u0001\u0000\u0000\u0000z\u02c0"+
		"\u0001\u0000\u0000\u0000|}\u0003\u001a\r\u0000}~\u0005\u0000\u0000\u0001"+
		"~\u0001\u0001\u0000\u0000\u0000\u007f\u0080\u0003\u0014\n\u0000\u0080"+
		"\u0081\u0005\u0000\u0000\u0001\u0081\u0003\u0001\u0000\u0000\u0000\u0082"+
		"\u0083\u0003\u000e\u0007\u0000\u0083\u0084\u0005\u0000\u0000\u0001\u0084"+
		"\u0005\u0001\u0000\u0000\u0000\u0085\u0086\u0003B!\u0000\u0086\u0087\u0005"+
		"\u0000\u0000\u0001\u0087\u0007\u0001\u0000\u0000\u0000\u0088\u0089\u0005"+
		"\u0002\u0000\u0000\u0089\u0097\u0005\u0000\u0000\u0001\u008a\u008c\u0003"+
		"\n\u0005\u0000\u008b\u008a\u0001\u0000\u0000\u0000\u008b\u008c\u0001\u0000"+
		"\u0000\u0000\u008c\u0090\u0001\u0000\u0000\u0000\u008d\u008e\u0005\u0012"+
		"\u0000\u0000\u008e\u008f\u0005\u0006\u0000\u0000\u008f\u0091\u0003>\u001f"+
		"\u0000\u0090\u008d\u0001\u0000\u0000\u0000\u0090\u0091\u0001\u0000\u0000"+
		"\u0000\u0091\u0092\u0001\u0000\u0000\u0000\u0092\u0093\u0005\u0013\u0000"+
		"\u0000\u0093\u0094\u0003\u000e\u0007\u0000\u0094\u0095\u0005\u0000\u0000"+
		"\u0001\u0095\u0097\u0001\u0000\u0000\u0000\u0096\u0088\u0001\u0000\u0000"+
		"\u0000\u0096\u008b\u0001\u0000\u0000\u0000\u0097\t\u0001\u0000\u0000\u0000"+
		"\u0098\u0099\u0005-\u0000\u0000\u0099\u009e\u0003\f\u0006\u0000\u009a"+
		"\u009b\u0005\u0003\u0000\u0000\u009b\u009d\u0003\f\u0006\u0000\u009c\u009a"+
		"\u0001\u0000\u0000\u0000\u009d\u00a0\u0001\u0000\u0000\u0000\u009e\u009c"+
		"\u0001\u0000\u0000\u0000\u009e\u009f\u0001\u0000\u0000\u0000\u009f\u000b"+
		"\u0001\u0000\u0000\u0000\u00a0\u009e\u0001\u0000\u0000\u0000\u00a1\u00a3"+
		"\u0007\u0000\u0000\u0000\u00a2\u00a1\u0001\u0000\u0000\u0000\u00a2\u00a3"+
		"\u0001\u0000\u0000\u0000\u00a3\u00a4\u0001\u0000\u0000\u0000\u00a4\u00a9"+
		"\u0003f3\u0000\u00a5\u00a6\u0005=\u0000\u0000\u00a6\u00a8\u0003f3\u0000"+
		"\u00a7\u00a5\u0001\u0000\u0000\u0000\u00a8\u00ab\u0001\u0000\u0000\u0000"+
		"\u00a9\u00a7\u0001\u0000\u0000\u0000\u00a9\u00aa\u0001\u0000\u0000\u0000"+
		"\u00aa\u00ac\u0001\u0000\u0000\u0000\u00ab\u00a9\u0001\u0000\u0000\u0000"+
		"\u00ac\u00ad\u0005\u0016\u0000\u0000\u00ad\u00ae\u0005e\u0000\u0000\u00ae"+
		"\r\u0001\u0000\u0000\u0000\u00af\u00b9\u0003>\u001f\u0000\u00b0\u00b3"+
		"\u0003\u0014\n\u0000\u00b1\u00b3\u0003\u0010\b\u0000\u00b2\u00b0\u0001"+
		"\u0000\u0000\u0000\u00b2\u00b1\u0001\u0000\u0000\u0000\u00b3\u00b6\u0001"+
		"\u0000\u0000\u0000\u00b4\u00b5\u0005\u0018\u0000\u0000\u00b5\u00b7\u0003"+
		">\u001f\u0000\u00b6\u00b4\u0001\u0000\u0000\u0000\u00b6\u00b7\u0001\u0000"+
		"\u0000\u0000\u00b7\u00b9\u0001\u0000\u0000\u0000\u00b8\u00af\u0001\u0000"+
		"\u0000\u0000\u00b8\u00b2\u0001\u0000\u0000\u0000\u00b9\u000f\u0001\u0000"+
		"\u0000\u0000\u00ba\u00bf\u0003\u0012\t\u0000\u00bb\u00bc\u0005\u0003\u0000"+
		"\u0000\u00bc\u00be\u0003\u0012\t\u0000\u00bd\u00bb\u0001\u0000\u0000\u0000"+
		"\u00be\u00c1\u0001\u0000\u0000\u0000\u00bf\u00bd\u0001\u0000\u0000\u0000"+
		"\u00bf\u00c0\u0001\u0000\u0000\u0000\u00c0\u0011\u0001\u0000\u0000\u0000"+
		"\u00c1\u00bf\u0001\u0000\u0000\u0000\u00c2\u00c3\u0005\u001c\u0000\u0000"+
		"\u00c3\u00c6\u0005e\u0000\u0000\u00c4\u00c5\u0005\u0004\u0000\u0000\u00c5"+
		"\u00c7\u0003f3\u0000\u00c6\u00c4\u0001\u0000\u0000\u0000\u00c6\u00c7\u0001"+
		"\u0000\u0000\u0000\u00c7\u00c8\u0001\u0000\u0000\u0000\u00c8\u00c9\u0003"+
		"\u0014\n\u0000\u00c9\u0013\u0001\u0000\u0000\u0000\u00ca\u00cc\u0003\u0016"+
		"\u000b\u0000\u00cb\u00ca\u0001\u0000\u0000\u0000\u00cb\u00cc\u0001\u0000"+
		"\u0000\u0000\u00cc\u00d0\u0001\u0000\u0000\u0000\u00cd\u00cf\u0003\u0018"+
		"\f\u0000\u00ce\u00cd\u0001\u0000\u0000\u0000\u00cf\u00d2\u0001\u0000\u0000"+
		"\u0000\u00d0\u00ce\u0001\u0000\u0000\u0000\u00d0\u00d1\u0001\u0000\u0000"+
		"\u0000\u00d1\u00d4\u0001\u0000\u0000\u0000\u00d2\u00d0\u0001\u0000\u0000"+
		"\u0000\u00d3\u00d5\u0003\u001a\r\u0000\u00d4\u00d3\u0001\u0000\u0000\u0000"+
		"\u00d5\u00d6\u0001\u0000\u0000\u0000\u00d6\u00d4\u0001\u0000\u0000\u0000"+
		"\u00d6\u00d7\u0001\u0000\u0000\u0000\u00d7\u0015\u0001\u0000\u0000\u0000"+
		"\u00d8\u00d9\u0005b\u0000\u0000\u00d9\u00dc\u0005\u0019\u0000\u0000\u00da"+
		"\u00dc\u0005\u0014\u0000\u0000\u00db\u00d8\u0001\u0000\u0000\u0000\u00db"+
		"\u00da\u0001\u0000\u0000\u0000\u00dc\u0017\u0001\u0000\u0000\u0000\u00dd"+
		"\u00de\u0007\u0001\u0000\u0000\u00de\u0019\u0001\u0000\u0000\u0000\u00df"+
		"\u00e1\u0006\r\uffff\uffff\u0000\u00e0\u00e2\u0003X,\u0000\u00e1\u00e0"+
		"\u0001\u0000\u0000\u0000\u00e1\u00e2\u0001\u0000\u0000\u0000\u00e2\u00e3"+
		"\u0001\u0000\u0000\u0000\u00e3\u00e5\u00059\u0000\u0000\u00e4\u00e6\u0003"+
		"\u001a\r\u0000\u00e5\u00e4\u0001\u0000\u0000\u0000\u00e6\u00e7\u0001\u0000"+
		"\u0000\u0000\u00e7\u00e5\u0001\u0000\u0000\u0000\u00e7\u00e8\u0001\u0000"+
		"\u0000\u0000\u00e8\u00e9\u0001\u0000\u0000\u0000\u00e9\u00ea\u0005:\u0000"+
		"\u0000\u00ea\u00ff\u0001\u0000\u0000\u0000\u00eb\u00ed\u0003\u001c\u000e"+
		"\u0000\u00ec\u00eb\u0001\u0000\u0000\u0000\u00ed\u00ee\u0001\u0000\u0000"+
		"\u0000\u00ee\u00ec\u0001\u0000\u0000\u0000\u00ee\u00ef\u0001\u0000\u0000"+
		"\u0000\u00ef\u00f1\u0001\u0000\u0000\u0000\u00f0\u00f2\u0003\u001a\r\u0000"+
		"\u00f1\u00f0\u0001\u0000\u0000\u0000\u00f2\u00f3\u0001\u0000\u0000\u0000"+
		"\u00f3\u00f1\u0001\u0000\u0000\u0000\u00f3\u00f4\u0001\u0000\u0000\u0000"+
		"\u00f4\u00ff\u0001\u0000\u0000\u0000\u00f5\u00ff\u0003\u001e\u000f\u0000"+
		"\u00f6\u00fb\u0003&\u0013\u0000\u00f7\u00f8\u0005=\u0000\u0000\u00f8\u00fa"+
		"\u0003&\u0013\u0000\u00f9\u00f7\u0001\u0000\u0000\u0000\u00fa\u00fd\u0001"+
		"\u0000\u0000\u0000\u00fb\u00f9\u0001\u0000\u0000\u0000\u00fb\u00fc\u0001"+
		"\u0000\u0000\u0000\u00fc\u00ff\u0001\u0000\u0000\u0000\u00fd\u00fb\u0001"+
		"\u0000\u0000\u0000\u00fe\u00df\u0001\u0000\u0000\u0000\u00fe\u00ec\u0001"+
		"\u0000\u0000\u0000\u00fe\u00f5\u0001\u0000\u0000\u0000\u00fe\u00f6\u0001"+
		"\u0000\u0000\u0000\u00ff\u0106\u0001\u0000\u0000\u0000\u0100\u0101\n\u0001"+
		"\u0000\u0000\u0101\u0102\u0003b1\u0000\u0102\u0103\u0003\u001a\r\u0001"+
		"\u0103\u0105\u0001\u0000\u0000\u0000\u0104\u0100\u0001\u0000\u0000\u0000"+
		"\u0105\u0108\u0001\u0000\u0000\u0000\u0106\u0104\u0001\u0000\u0000\u0000"+
		"\u0106\u0107\u0001\u0000\u0000\u0000\u0107\u001b\u0001\u0000\u0000\u0000"+
		"\u0108\u0106\u0001\u0000\u0000\u0000\u0109\u010a\u0007\u0002\u0000\u0000"+
		"\u010a\u001d\u0001\u0000\u0000\u0000\u010b\u010c\u0005;\u0000\u0000\u010c"+
		"\u010d\u0007\u0003\u0000\u0000\u010d\u0122\u0005<\u0000\u0000\u010e\u0110"+
		"\u0003X,\u0000\u010f\u010e\u0001\u0000\u0000\u0000\u010f\u0110\u0001\u0000"+
		"\u0000\u0000\u0110\u0111\u0001\u0000\u0000\u0000\u0111\u0113\u0005;\u0000"+
		"\u0000\u0112\u0114\u0003 \u0010\u0000\u0113\u0112\u0001\u0000\u0000\u0000"+
		"\u0113\u0114\u0001\u0000\u0000\u0000\u0114\u0118\u0001\u0000\u0000\u0000"+
		"\u0115\u0116\u0003\"\u0011\u0000\u0116\u0117\u0005=\u0000\u0000\u0117"+
		"\u0119\u0001\u0000\u0000\u0000\u0118\u0115\u0001\u0000\u0000\u0000\u0118"+
		"\u0119\u0001\u0000\u0000\u0000\u0119\u011b\u0001\u0000\u0000\u0000\u011a"+
		"\u011c\u0003>\u001f\u0000\u011b\u011a\u0001\u0000\u0000\u0000\u011b\u011c"+
		"\u0001\u0000\u0000\u0000\u011c\u011e\u0001\u0000\u0000\u0000\u011d\u011f"+
		"\u0003\u0014\n\u0000\u011e\u011d\u0001\u0000\u0000\u0000\u011e\u011f\u0001"+
		"\u0000\u0000\u0000\u011f\u0120\u0001\u0000\u0000\u0000\u0120\u0122\u0005"+
		"<\u0000\u0000\u0121\u010b\u0001\u0000\u0000\u0000\u0121\u010f\u0001\u0000"+
		"\u0000\u0000\u0122\u001f\u0001\u0000\u0000\u0000\u0123\u0124\u0003f3\u0000"+
		"\u0124\u0125\u00055\u0000\u0000\u0125!\u0001\u0000\u0000\u0000\u0126\u0127"+
		"\u0006\u0011\uffff\uffff\u0000\u0127\u0133\u0005e\u0000\u0000\u0128\u0129"+
		"\u00057\u0000\u0000\u0129\u012e\u0003$\u0012\u0000\u012a\u012b\u0005="+
		"\u0000\u0000\u012b\u012d\u0003$\u0012\u0000\u012c\u012a\u0001\u0000\u0000"+
		"\u0000\u012d\u0130\u0001\u0000\u0000\u0000\u012e\u012c\u0001\u0000\u0000"+
		"\u0000\u012e\u012f\u0001\u0000\u0000\u0000\u012f\u0131\u0001\u0000\u0000"+
		"\u0000\u0130\u012e\u0001\u0000\u0000\u0000\u0131\u0132\u00058\u0000\u0000"+
		"\u0132\u0134\u0001\u0000\u0000\u0000\u0133\u0128\u0001\u0000\u0000\u0000"+
		"\u0133\u0134\u0001\u0000\u0000\u0000\u0134\u013a\u0001\u0000\u0000\u0000"+
		"\u0135\u0136\u00057\u0000\u0000\u0136\u0137\u0003\"\u0011\u0000\u0137"+
		"\u0138\u00058\u0000\u0000\u0138\u013a\u0001\u0000\u0000\u0000\u0139\u0126"+
		"\u0001\u0000\u0000\u0000\u0139\u0135\u0001\u0000\u0000\u0000\u013a\u0145"+
		"\u0001\u0000\u0000\u0000\u013b\u013c\n\u0002\u0000\u0000\u013c\u013d\u0003"+
		"`0\u0000\u013d\u013e\u0003\"\u0011\u0002\u013e\u0144\u0001\u0000\u0000"+
		"\u0000\u013f\u0140\n\u0001\u0000\u0000\u0140\u0141\u0003b1\u0000\u0141"+
		"\u0142\u0003\"\u0011\u0001\u0142\u0144\u0001\u0000\u0000\u0000\u0143\u013b"+
		"\u0001\u0000\u0000\u0000\u0143\u013f\u0001\u0000\u0000\u0000\u0144\u0147"+
		"\u0001\u0000\u0000\u0000\u0145\u0143\u0001\u0000\u0000\u0000\u0145\u0146"+
		"\u0001\u0000\u0000\u0000\u0146#\u0001\u0000\u0000\u0000\u0147\u0145\u0001"+
		"\u0000\u0000\u0000\u0148\u014b\u0003v;\u0000\u0149\u014b\u0003n7\u0000"+
		"\u014a\u0148\u0001\u0000\u0000\u0000\u014a\u0149\u0001\u0000\u0000\u0000"+
		"\u014b%\u0001\u0000\u0000\u0000\u014c\u0152\u0003\u001e\u000f\u0000\u014d"+
		"\u014e\u0003\u001e\u000f\u0000\u014e\u014f\u0003(\u0014\u0000\u014f\u0150"+
		"\u0003\u001e\u000f\u0000\u0150\u0152\u0001\u0000\u0000\u0000\u0151\u014c"+
		"\u0001\u0000\u0000\u0000\u0151\u014d\u0001\u0000\u0000\u0000\u0152\'\u0001"+
		"\u0000\u0000\u0000\u0153\u0160\u0007\u0004\u0000\u0000\u0154\u0155\u0003"+
		"*\u0015\u0000\u0155\u0157\u0005;\u0000\u0000\u0156\u0158\u0003 \u0010"+
		"\u0000\u0157\u0156\u0001\u0000\u0000\u0000\u0157\u0158\u0001\u0000\u0000"+
		"\u0000\u0158\u015a\u0001\u0000\u0000\u0000\u0159\u015b\u0003>\u001f\u0000"+
		"\u015a\u0159\u0001\u0000\u0000\u0000\u015a\u015b\u0001\u0000\u0000\u0000"+
		"\u015b\u015c\u0001\u0000\u0000\u0000\u015c\u015d\u0005<\u0000\u0000\u015d"+
		"\u015e\u0003,\u0016\u0000\u015e\u0160\u0001\u0000\u0000\u0000\u015f\u0153"+
		"\u0001\u0000\u0000\u0000\u015f\u0154\u0001\u0000\u0000\u0000\u0160)\u0001"+
		"\u0000\u0000\u0000\u0161\u0164\u0003.\u0017\u0000\u0162\u0164\u00032\u0019"+
		"\u0000\u0163\u0161\u0001\u0000\u0000\u0000\u0163\u0162\u0001\u0000\u0000"+
		"\u0000\u0164+\u0001\u0000\u0000\u0000\u0165\u0168\u00030\u0018\u0000\u0166"+
		"\u0168\u00032\u0019\u0000\u0167\u0165\u0001\u0000\u0000\u0000\u0167\u0166"+
		"\u0001\u0000\u0000\u0000\u0168-\u0001\u0000\u0000\u0000\u0169\u016a\u0005"+
		"V\u0000\u0000\u016a\u016b\u0005K\u0000\u0000\u016b/\u0001\u0000\u0000"+
		"\u0000\u016c\u016d\u0005K\u0000\u0000\u016d\u016e\u0005X\u0000\u0000\u016e"+
		"1\u0001\u0000\u0000\u0000\u016f\u0170\u0005K\u0000\u0000\u0170\u0171\u0005"+
		"K\u0000\u0000\u01713\u0001\u0000\u0000\u0000\u0172\u0173\u0005\u0017\u0000"+
		"\u0000\u0173\u0178\u00036\u001b\u0000\u0174\u0175\u0005=\u0000\u0000\u0175"+
		"\u0177\u00036\u001b\u0000\u0176\u0174\u0001\u0000\u0000\u0000\u0177\u017a"+
		"\u0001\u0000\u0000\u0000\u0178\u0176\u0001\u0000\u0000\u0000\u0178\u0179"+
		"\u0001\u0000\u0000\u0000\u0179\u017b\u0001\u0000\u0000\u0000\u017a\u0178"+
		"\u0001\u0000\u0000\u0000\u017b\u017c\u0005\u0000\u0000\u0001\u017c5\u0001"+
		"\u0000\u0000\u0000\u017d\u017e\u0005\u0006\u0000\u0000\u017e\u0182\u0003"+
		"B!\u0000\u017f\u0180\u0005\u0012\u0000\u0000\u0180\u0181\u0005\"\u0000"+
		"\u0000\u0181\u0183\u0003B!\u0000\u0182\u017f\u0001\u0000\u0000\u0000\u0182"+
		"\u0183\u0001\u0000\u0000\u0000\u0183\u0184\u0001\u0000\u0000\u0000\u0184"+
		"\u0185\u0005\u001b\u0000\u0000\u0185\u0188\u0005d\u0000\u0000\u0186\u0187"+
		"\u0005\t\u0000\u0000\u0187\u0189\u0003B!\u0000\u0188\u0186\u0001\u0000"+
		"\u0000\u0000\u0188\u0189\u0001\u0000\u0000\u0000\u01897\u0001\u0000\u0000"+
		"\u0000\u018a\u018c\u0005\u001d\u0000\u0000\u018b\u018d\u0005\u0014\u0000"+
		"\u0000\u018c\u018b\u0001\u0000\u0000\u0000\u018c\u018d\u0001\u0000\u0000"+
		"\u0000\u018d\u018e\u0001\u0000\u0000\u0000\u018e\u0190\u0003x<\u0000\u018f"+
		"\u018a\u0001\u0000\u0000\u0000\u018f\u0190\u0001\u0000\u0000\u0000\u0190"+
		"\u0192\u0001\u0000\u0000\u0000\u0191\u0193\u0003:\u001d\u0000\u0192\u0191"+
		"\u0001\u0000\u0000\u0000\u0192\u0193\u0001\u0000\u0000\u0000\u0193\u0194"+
		"\u0001\u0000\u0000\u0000\u0194\u0195\u0005\u0000\u0000\u0001\u01959\u0001"+
		"\u0000\u0000\u0000\u0196\u0197\u0005%\u0000\u0000\u0197\u0198\u0005\u0006"+
		"\u0000\u0000\u0198\u019d\u0003<\u001e\u0000\u0199\u019a\u0005=\u0000\u0000"+
		"\u019a\u019c\u0003<\u001e\u0000\u019b\u0199\u0001\u0000\u0000\u0000\u019c"+
		"\u019f\u0001\u0000\u0000\u0000\u019d\u019b\u0001\u0000\u0000\u0000\u019d"+
		"\u019e\u0001\u0000\u0000\u0000\u019e;\u0001\u0000\u0000\u0000\u019f\u019d"+
		"\u0001\u0000\u0000\u0000\u01a0\u01a1\u0003B!\u0000\u01a1\u01a2\u0007\u0005"+
		"\u0000\u0000\u01a2=\u0001\u0000\u0000\u0000\u01a3\u01a4\u0003B!\u0000"+
		"\u01a4?\u0001\u0000\u0000\u0000\u01a5\u01aa\u0003B!\u0000\u01a6\u01a7"+
		"\u0005=\u0000\u0000\u01a7\u01a9\u0003B!\u0000\u01a8\u01a6\u0001\u0000"+
		"\u0000\u0000\u01a9\u01ac\u0001\u0000\u0000\u0000\u01aa\u01a8\u0001\u0000"+
		"\u0000\u0000\u01aa\u01ab\u0001\u0000\u0000\u0000\u01abA\u0001\u0000\u0000"+
		"\u0000\u01ac\u01aa\u0001\u0000\u0000\u0000\u01ad\u01ae\u0006!\uffff\uffff"+
		"\u0000\u01ae\u01bc\u0003D\"\u0000\u01af\u01b0\u00057\u0000\u0000\u01b0"+
		"\u01b1\u0003V+\u0000\u01b1\u01b2\u00058\u0000\u0000\u01b2\u01b3\u0003"+
		"B!\u000f\u01b3\u01bc\u0001\u0000\u0000\u0000\u01b4\u01b5\u00057\u0000"+
		"\u0000\u01b5\u01b6\u0003B!\u0000\u01b6\u01b7\u00058\u0000\u0000\u01b7"+
		"\u01bc\u0001\u0000\u0000\u0000\u01b8\u01b9\u0007\u0006\u0000\u0000\u01b9"+
		"\u01bc\u0003B!\r\u01ba\u01bc\u0003L&\u0000\u01bb\u01ad\u0001\u0000\u0000"+
		"\u0000\u01bb\u01af\u0001\u0000\u0000\u0000\u01bb\u01b4\u0001\u0000\u0000"+
		"\u0000\u01bb\u01b8\u0001\u0000\u0000\u0000\u01bb\u01ba\u0001\u0000\u0000"+
		"\u0000\u01bc\u0207\u0001\u0000\u0000\u0000\u01bd\u01be\n\f\u0000\u0000"+
		"\u01be\u01bf\u0007\u0007\u0000\u0000\u01bf\u0206\u0003B!\r\u01c0\u01c1"+
		"\n\u000b\u0000\u0000\u01c1\u01c2\u0007\b\u0000\u0000\u01c2\u0206\u0003"+
		"B!\f\u01c3\u01c4\n\n\u0000\u0000\u01c4\u01c5\u0007\t\u0000\u0000\u01c5"+
		"\u0206\u0003B!\u000b\u01c6\u01c7\n\t\u0000\u0000\u01c7\u01c8\u0007\n\u0000"+
		"\u0000\u01c8\u0206\u0003B!\n\u01c9\u01ca\n\b\u0000\u0000\u01ca\u01cb\u0007"+
		"\u000b\u0000\u0000\u01cb\u0206\u0003B!\t\u01cc\u01cd\n\u0007\u0000\u0000"+
		"\u01cd\u01ce\u0007\f\u0000\u0000\u01ce\u0206\u0003B!\b\u01cf\u01d0\n\u0006"+
		"\u0000\u0000\u01d0\u01d1\u0003`0\u0000\u01d1\u01d2\u0003B!\u0006\u01d2"+
		"\u0206\u0001\u0000\u0000\u0000\u01d3\u01d4\n\u0005\u0000\u0000\u01d4\u01d5"+
		"\u0003b1\u0000\u01d5\u01d6\u0003B!\u0005\u01d6\u0206\u0001\u0000\u0000"+
		"\u0000\u01d7\u01d9\n\u0003\u0000\u0000\u01d8\u01da\u0003^/\u0000\u01d9"+
		"\u01d8\u0001\u0000\u0000\u0000\u01d9\u01da\u0001\u0000\u0000\u0000\u01da"+
		"\u01dc\u0001\u0000\u0000\u0000\u01db\u01dd\u0003\\.\u0000\u01dc\u01db"+
		"\u0001\u0000\u0000\u0000\u01dc\u01dd\u0001\u0000\u0000\u0000\u01dd\u01de"+
		"\u0001\u0000\u0000\u0000\u01de\u01df\u0005\u001a\u0000\u0000\u01df\u0206"+
		"\u0003B!\u0004\u01e0\u01e1\n\u0002\u0000\u0000\u01e1\u01e2\u0005F\u0000"+
		"\u0000\u01e2\u01e3\u0003B!\u0000\u01e3\u01e4\u00055\u0000\u0000\u01e4"+
		"\u01e5\u0003B!\u0003\u01e5\u0206\u0001\u0000\u0000\u0000\u01e6\u01e7\n"+
		"\u0013\u0000\u0000\u01e7\u01e8\u00056\u0000\u0000\u01e8\u0206\u0005e\u0000"+
		"\u0000\u01e9\u01ea\n\u0012\u0000\u0000\u01ea\u01eb\u0004!\u000f\u0000"+
		"\u01eb\u01ed\u00057\u0000\u0000\u01ec\u01ee\u0003@ \u0000\u01ed\u01ec"+
		"\u0001\u0000\u0000\u0000\u01ed\u01ee\u0001\u0000\u0000\u0000\u01ee\u01ef"+
		"\u0001\u0000\u0000\u0000\u01ef\u0206\u00058\u0000\u0000\u01f0\u01f1\n"+
		"\u0011\u0000\u0000\u01f1\u01f2\u0004!\u0011\u0000\u01f2\u01f3\u0005;\u0000"+
		"\u0000\u01f3\u01f4\u0003@ \u0000\u01f4\u01f5\u0005<\u0000\u0000\u01f5"+
		"\u0206\u0001\u0000\u0000\u0000\u01f6\u01f7\n\u0010\u0000\u0000\u01f7\u01f8"+
		"\u0004!\u0013\u0000\u01f8\u01f9\u00059\u0000\u0000\u01f9\u01fa\u0003@"+
		" \u0000\u01fa\u01fb\u0005:\u0000\u0000\u01fb\u0206\u0001\u0000\u0000\u0000"+
		"\u01fc\u01fd\n\u0004\u0000\u0000\u01fd\u01ff\u0005\u0004\u0000\u0000\u01fe"+
		"\u0200\u0005#\u0000\u0000\u01ff\u01fe\u0001\u0000\u0000\u0000\u01ff\u0200"+
		"\u0001\u0000\u0000\u0000\u0200\u0203\u0001\u0000\u0000\u0000\u0201\u0204"+
		"\u0003f3\u0000\u0202\u0204\u0003d2\u0000\u0203\u0201\u0001\u0000\u0000"+
		"\u0000\u0203\u0202\u0001\u0000\u0000\u0000\u0204\u0206\u0001\u0000\u0000"+
		"\u0000\u0205\u01bd\u0001\u0000\u0000\u0000\u0205\u01c0\u0001\u0000\u0000"+
		"\u0000\u0205\u01c3\u0001\u0000\u0000\u0000\u0205\u01c6\u0001\u0000\u0000"+
		"\u0000\u0205\u01c9\u0001\u0000\u0000\u0000\u0205\u01cc\u0001\u0000\u0000"+
		"\u0000\u0205\u01cf\u0001\u0000\u0000\u0000\u0205\u01d3\u0001\u0000\u0000"+
		"\u0000\u0205\u01d7\u0001\u0000\u0000\u0000\u0205\u01e0\u0001\u0000\u0000"+
		"\u0000\u0205\u01e6\u0001\u0000\u0000\u0000\u0205\u01e9\u0001\u0000\u0000"+
		"\u0000\u0205\u01f0\u0001\u0000\u0000\u0000\u0205\u01f6\u0001\u0000\u0000"+
		"\u0000\u0205\u01fc\u0001\u0000\u0000\u0000\u0206\u0209\u0001\u0000\u0000"+
		"\u0000\u0207\u0205\u0001\u0000\u0000\u0000\u0207\u0208\u0001\u0000\u0000"+
		"\u0000\u0208C\u0001\u0000\u0000\u0000\u0209\u0207\u0001\u0000\u0000\u0000"+
		"\u020a\u0212\u0003j5\u0000\u020b\u0212\u0003r9\u0000\u020c\u0212\u0003"+
		"l6\u0000\u020d\u0212\u0003t:\u0000\u020e\u0212\u0005d\u0000\u0000\u020f"+
		"\u0212\u0003F#\u0000\u0210\u0212\u0003H$\u0000\u0211\u020a\u0001\u0000"+
		"\u0000\u0000\u0211\u020b\u0001\u0000\u0000\u0000\u0211\u020c\u0001\u0000"+
		"\u0000\u0000\u0211\u020d\u0001\u0000\u0000\u0000\u0211\u020e\u0001\u0000"+
		"\u0000\u0000\u0211\u020f\u0001\u0000\u0000\u0000\u0211\u0210\u0001\u0000"+
		"\u0000\u0000\u0212E\u0001\u0000\u0000\u0000\u0213\u0214\u0003V+\u0000"+
		"\u0214\u0215\u0005;\u0000\u0000\u0215\u0216\u0005<\u0000\u0000\u0216\u0218"+
		"\u0001\u0000\u0000\u0000\u0217\u0213\u0001\u0000\u0000\u0000\u0217\u0218"+
		"\u0001\u0000\u0000\u0000\u0218\u0219\u0001\u0000\u0000\u0000\u0219\u021b"+
		"\u00059\u0000\u0000\u021a\u021c\u0003@ \u0000\u021b\u021a\u0001\u0000"+
		"\u0000\u0000\u021b\u021c\u0001\u0000\u0000\u0000\u021c\u021d\u0001\u0000"+
		"\u0000\u0000\u021d\u021e\u0005:\u0000\u0000\u021eG\u0001\u0000\u0000\u0000"+
		"\u021f\u0224\u0003d2\u0000\u0220\u0224\u0003f3\u0000\u0221\u0224\u0005"+
		"e\u0000\u0000\u0222\u0224\u0003J%\u0000\u0223\u021f\u0001\u0000\u0000"+
		"\u0000\u0223\u0220\u0001\u0000\u0000\u0000\u0223\u0221\u0001\u0000\u0000"+
		"\u0000\u0223\u0222\u0001\u0000\u0000\u0000\u0224I\u0001\u0000\u0000\u0000"+
		"\u0225\u0226\u0005e\u0000\u0000\u0226\u0227\u0005`\u0000\u0000\u0227\u0228"+
		"\u0005e\u0000\u0000\u0228K\u0001\u0000\u0000\u0000\u0229\u022a\u0005\u0015"+
		"\u0000\u0000\u022a\u022b\u0003B!\u0000\u022b\u022c\u0005\u0004\u0000\u0000"+
		"\u022c\u022d\u0003d2\u0000\u022d\u022f\u0003N\'\u0000\u022e\u0230\u0003"+
		"R)\u0000\u022f\u022e\u0001\u0000\u0000\u0000\u022f\u0230\u0001\u0000\u0000"+
		"\u0000\u0230M\u0001\u0000\u0000\u0000\u0231\u0233\u0007\r\u0000\u0000"+
		"\u0232\u0231\u0001\u0000\u0000\u0000\u0232\u0233\u0001\u0000\u0000\u0000"+
		"\u0233\u0236\u0001\u0000\u0000\u0000\u0234\u0235\u0005!\u0000\u0000\u0235"+
		"\u0237\u0003>\u001f\u0000\u0236\u0234\u0001\u0000\u0000\u0000\u0236\u0237"+
		"\u0001\u0000\u0000\u0000\u0237\u023a\u0001\u0000\u0000\u0000\u0238\u0239"+
		"\u0005\'\u0000\u0000\u0239\u023b\u0003P(\u0000\u023a\u0238\u0001\u0000"+
		"\u0000\u0000\u023a\u023b\u0001\u0000\u0000\u0000\u023b\u023e\u0001\u0000"+
		"\u0000\u0000\u023c\u023d\u0005*\u0000\u0000\u023d\u023f\u0003P(\u0000"+
		"\u023e\u023c\u0001\u0000\u0000\u0000\u023e\u023f\u0001\u0000\u0000\u0000"+
		"\u023f\u0242\u0001\u0000\u0000\u0000\u0240\u0241\u0005\r\u0000\u0000\u0241"+
		"\u0243\u0003>\u001f\u0000\u0242\u0240\u0001\u0000\u0000\u0000\u0242\u0243"+
		"\u0001\u0000\u0000\u0000\u0243\u0244\u0001\u0000\u0000\u0000\u0244\u0245"+
		"\u0005\u000f\u0000\u0000\u0245O\u0001\u0000\u0000\u0000\u0246\u0247\u0005"+
		"7\u0000\u0000\u0247\u0248\u0003B!\u0000\u0248\u0249\u0005=\u0000\u0000"+
		"\u0249\u024a\u0003B!\u0000\u024a\u024b\u00058\u0000\u0000\u024bQ\u0001"+
		"\u0000\u0000\u0000\u024c\u024d\u0005\b\u0000\u0000\u024d\u0252\u0003T"+
		"*\u0000\u024e\u024f\u0005=\u0000\u0000\u024f\u0251\u0003T*\u0000\u0250"+
		"\u024e\u0001\u0000\u0000\u0000\u0251\u0254\u0001\u0000\u0000\u0000\u0252"+
		"\u0250\u0001\u0000\u0000\u0000\u0252\u0253\u0001\u0000\u0000\u0000\u0253"+
		"S\u0001\u0000\u0000\u0000\u0254\u0252\u0001\u0000\u0000\u0000\u0255\u0256"+
		"\u0003>\u001f\u0000\u0256\u0257\u0005\u0004\u0000\u0000\u0257\u0258\u0003"+
		"d2\u0000\u0258U\u0001\u0000\u0000\u0000\u0259\u025a\u0007\u000e\u0000"+
		"\u0000\u025aW\u0001\u0000\u0000\u0000\u025b\u0260\u0003Z-\u0000\u025c"+
		"\u025d\u0005R\u0000\u0000\u025d\u025f\u0003Z-\u0000\u025e\u025c\u0001"+
		"\u0000\u0000\u0000\u025f\u0262\u0001\u0000\u0000\u0000\u0260\u025e\u0001"+
		"\u0000\u0000\u0000\u0260\u0261\u0001\u0000\u0000\u0000\u0261\u026f\u0001"+
		"\u0000\u0000\u0000\u0262\u0260\u0001\u0000\u0000\u0000\u0263\u0264\u0005"+
		"V\u0000\u0000\u0264\u0269\u0003Z-\u0000\u0265\u0266\u0005R\u0000\u0000"+
		"\u0266\u0268\u0003Z-\u0000\u0267\u0265\u0001\u0000\u0000\u0000\u0268\u026b"+
		"\u0001\u0000\u0000\u0000\u0269\u0267\u0001\u0000\u0000\u0000\u0269\u026a"+
		"\u0001\u0000\u0000\u0000\u026a\u026c\u0001\u0000\u0000\u0000\u026b\u0269"+
		"\u0001\u0000\u0000\u0000\u026c\u026d\u0005X\u0000\u0000\u026d\u026f\u0001"+
		"\u0000\u0000\u0000\u026e\u025b\u0001\u0000\u0000\u0000\u026e\u0263\u0001"+
		"\u0000\u0000\u0000\u026fY\u0001\u0000\u0000\u0000\u0270\u0289\u0003\\"+
		".\u0000\u0271\u0273\u0003^/\u0000\u0272\u0274\u0007\u000f\u0000\u0000"+
		"\u0273\u0272\u0001\u0000\u0000\u0000\u0273\u0274\u0001\u0000\u0000\u0000"+
		"\u0274\u0289\u0001\u0000\u0000\u0000\u0275\u0277\u0005b\u0000\u0000\u0276"+
		"\u0278\u0007\b\u0000\u0000\u0277\u0276\u0001\u0000\u0000\u0000\u0277\u0278"+
		"\u0001\u0000\u0000\u0000\u0278\u027a\u0001\u0000\u0000\u0000\u0279\u027b"+
		"\u0007\u0010\u0000\u0000\u027a\u0279\u0001\u0000\u0000\u0000\u027a\u027b"+
		"\u0001\u0000\u0000\u0000\u027b\u027d\u0001\u0000\u0000\u0000\u027c\u027e"+
		"\u0005Q\u0000\u0000\u027d\u027c\u0001\u0000\u0000\u0000\u027d\u027e\u0001"+
		"\u0000\u0000\u0000\u027e\u0289\u0001\u0000\u0000\u0000\u027f\u0280\u0005"+
		"b\u0000\u0000\u0280\u0281\u0005a\u0000\u0000\u0281\u0283\u0005b\u0000"+
		"\u0000\u0282\u0284\u0007\u0010\u0000\u0000\u0283\u0282\u0001\u0000\u0000"+
		"\u0000\u0283\u0284\u0001\u0000\u0000\u0000\u0284\u0286\u0001\u0000\u0000"+
		"\u0000\u0285\u0287\u0005Q\u0000\u0000\u0286\u0285\u0001\u0000\u0000\u0000"+
		"\u0286\u0287\u0001\u0000\u0000\u0000\u0287\u0289\u0001\u0000\u0000\u0000"+
		"\u0288\u0270\u0001\u0000\u0000\u0000\u0288\u0271\u0001\u0000\u0000\u0000"+
		"\u0288\u0275\u0001\u0000\u0000\u0000\u0288\u027f\u0001\u0000\u0000\u0000"+
		"\u0289[\u0001\u0000\u0000\u0000\u028a\u028b\u0007\u0011\u0000\u0000\u028b"+
		"]\u0001\u0000\u0000\u0000\u028c\u028d\u0007\u0012\u0000\u0000\u028d_\u0001"+
		"\u0000\u0000\u0000\u028e\u028f\u0007\u0013\u0000\u0000\u028fa\u0001\u0000"+
		"\u0000\u0000\u0290\u0291\u0007\u0014\u0000\u0000\u0291c\u0001\u0000\u0000"+
		"\u0000\u0292\u0293\u0005C\u0000\u0000\u0293\u0294\u0005e\u0000\u0000\u0294"+
		"e\u0001\u0000\u0000\u0000\u0295\u0297\u0005D\u0000\u0000\u0296\u0298\u0005"+
		"e\u0000\u0000\u0297\u0296\u0001\u0000\u0000\u0000\u0297\u0298\u0001\u0000"+
		"\u0000\u0000\u0298g\u0001\u0000\u0000\u0000\u0299\u029c\u0005b\u0000\u0000"+
		"\u029a\u029b\u00056\u0000\u0000\u029b\u029d\u0005b\u0000\u0000\u029c\u029a"+
		"\u0001\u0000\u0000\u0000\u029c\u029d\u0001\u0000\u0000\u0000\u029d\u02a0"+
		"\u0001\u0000\u0000\u0000\u029e\u029f\u00056\u0000\u0000\u029f\u02a1\u0005"+
		"b\u0000\u0000\u02a0\u029e\u0001\u0000\u0000\u0000\u02a0\u02a1\u0001\u0000"+
		"\u0000\u0000\u02a1\u02a3\u0001\u0000\u0000\u0000\u02a2\u02a4\u0007\u0015"+
		"\u0000\u0000\u02a3\u02a2\u0001\u0000\u0000\u0000\u02a3\u02a4\u0001\u0000"+
		"\u0000\u0000\u02a4\u02a6\u0001\u0000\u0000\u0000\u02a5\u02a7\u0005e\u0000"+
		"\u0000\u02a6\u02a5\u0001\u0000\u0000\u0000\u02a6\u02a7\u0001\u0000\u0000"+
		"\u0000\u02a7i\u0001\u0000\u0000\u0000\u02a8\u02a9\u0005\u001f\u0000\u0000"+
		"\u02a9k\u0001\u0000\u0000\u0000\u02aa\u02ab\u0003n7\u0000\u02abm\u0001"+
		"\u0000\u0000\u0000\u02ac\u02ae\u0003z=\u0000\u02ad\u02ac\u0001\u0000\u0000"+
		"\u0000\u02ad\u02ae\u0001\u0000\u0000\u0000\u02ae\u02af\u0001\u0000\u0000"+
		"\u0000\u02af\u02b0\u0003p8\u0000\u02b0o\u0001\u0000\u0000\u0000\u02b1"+
		"\u02b2\u0003x<\u0000\u02b2\u02b3\u00056\u0000\u0000\u02b3\u02b4\u0003"+
		"x<\u0000\u02b4q\u0001\u0000\u0000\u0000\u02b5\u02b6\u0007\u0016\u0000"+
		"\u0000\u02b6s\u0001\u0000\u0000\u0000\u02b7\u02b8\u0003v;\u0000\u02b8"+
		"u\u0001\u0000\u0000\u0000\u02b9\u02bb\u0003z=\u0000\u02ba\u02b9\u0001"+
		"\u0000\u0000\u0000\u02ba\u02bb\u0001\u0000\u0000\u0000\u02bb\u02bc\u0001"+
		"\u0000\u0000\u0000\u02bc\u02bd\u0003x<\u0000\u02bdw\u0001\u0000\u0000"+
		"\u0000\u02be\u02bf\u0007\u0017\u0000\u0000\u02bfy\u0001\u0000\u0000\u0000"+
		"\u02c0\u02c1\u0007\b\u0000\u0000\u02c1{\u0001\u0000\u0000\u0000T\u008b"+
		"\u0090\u0096\u009e\u00a2\u00a9\u00b2\u00b6\u00b8\u00bf\u00c6\u00cb\u00d0"+
		"\u00d6\u00db\u00e1\u00e7\u00ee\u00f3\u00fb\u00fe\u0106\u010f\u0113\u0118"+
		"\u011b\u011e\u0121\u012e\u0133\u0139\u0143\u0145\u014a\u0151\u0157\u015a"+
		"\u015f\u0163\u0167\u0178\u0182\u0188\u018c\u018f\u0192\u019d\u01aa\u01bb"+
		"\u01d9\u01dc\u01ed\u01ff\u0203\u0205\u0207\u0211\u0217\u021b\u0223\u022f"+
		"\u0232\u0236\u023a\u023e\u0242\u0252\u0260\u0269\u026e\u0273\u0277\u027a"+
		"\u027d\u0283\u0286\u0288\u0297\u029c\u02a0\u02a3\u02a6\u02ad\u02ba";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}