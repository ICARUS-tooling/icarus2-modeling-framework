/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
grammar IQL;

//TODO wanna label it Query Programming 

//options {
//    tokenVocab=IQLLexer;
//}

@parser::header {
package de.ims.icarus2.query.api.iql.antlr;
}

@lexer::header {
package de.ims.icarus2.query.api.iql.antlr;
import org.antlr.v4.runtime.misc.Interval;	
}

@lexer::members {
public static final int WHITESPACE = 1;
public static final int COMMENTS = 2;

boolean ahead(String text) {
	//TODO debug this crap
    for (int i = 0; i < text.length(); i++) {
		if (text.charAt(i) != _input.LA(i + 1)) {
        	return false;
		}
    }
    return true;
}

boolean before(String text) {
	int len = text.length();
    for (int i = len-1; i > 0; i--) {
		if (text.charAt(i) != _input.LA(-len + i)) {
        	return false;
		}
    }
    return true;
}

boolean isDigit(int offset) {
	return Character.isDigit(_input.LA(offset));
}

}

@parser::members {
	
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

}

/**
 * Parser Rules
 */

/** Standalone rules */


// Standalone top-level parts
standalonePreamble : preamble EOF ;
standaloneStatement : statement EOF ;
standaloneResult : result EOF ;

// Standalone statement parts
standaloneFlatStatement : flatStatement EOF ;
standaloneTreeStatement : treeStatement EOF ;
standaloneGraphStatement : graphStatement EOF ;

// Standalone helpers
standaloneVersionDeclaration : versionDeclaration EOF ;
standaloneExpression : expression EOF ;


/** Basic Rules */

query
	: preamble statement (RETURN result) (APPENDIX appendix)? EOF
	;
	
// PREAMBLE BEGIN
	
preamble
	: (DIALECT versionDeclaration)? (IMPORT importTargetList)? (SETUP propertyList)? // here goes configuration stuff, namespace declarations, etc...
	;
	
/** Import external extensions or script definitions, optionally renaming their namespace */
importTargetList
	: importTarget (COMMA importTarget)*
	;
	
importTarget
	: StringLiteral (AS Identifier)?
	;
	
/** Allows to do a general setup of query parameters */
propertyList
	: property (COMMA property)*
	;
	
property
	: key=qualifiedIdentifier # switchProperty
	| key=qualifiedIdentifier ASSIGN value=integerLiteral # intProperty
	| key=qualifiedIdentifier ASSIGN value=floatingPointLiteral # floatProperty
	| key=qualifiedIdentifier ASSIGN value=booleanLiteral # booleanProperty
	| key=qualifiedIdentifier ASSIGN value=StringLiteral # stringProperty
	;
	
// PREAMBLE END


// STATEMENT BEGIN

/** Core of every query: specify what to extract from where */
statement
 	: FROM corpusSelectorList SELECT layerSelectorList constraintStatement groupStatement?
 	;
 	
corpusSelectorList
	: corpusSelector (COMMA corpusSelector)*
	;
	
corpusSelector
	: (id=qualifiedIdentifier | uri=StringLiteral) (AS renamed=Identifier)? 
	;

layerSelectorList
	: layerSelector (COMMA layerSelector)*
	;
	
layerSelector 
	: id=qualifiedIdentifier (AS PRIMARY? layerAlias=Identifier | AS PRIMARY? SCOPE scopeAlias=Identifier LPAREN scopeElementList RPAREN)?
	;

scopeElementList
	: qualifiedIdentifier STAR? (COMMA qualifiedIdentifier STAR?)*
	;
	
/** Addressing a layer/context or other embedded member via the identifiers of its environment */
qualifiedIdentifier
	: Identifier (DOUBLE_COLON Identifier)*
	;
	
/** 
 * Actual selector part of a query:
 * 
 * Bindings can be sued to simplify the subsequent part of the query.
 * Local constraints are the only obligatory part and define the basic
 * complexity for evaluating the query. Global constraints are optional and 
 * can cause unlimited increase in complexity!
 */
constraintStatement
	: (WITH bindingsList)? WHERE localConstraints (HAVING globalConstraints)?
	| ALL // special marker to return the entire corpus, with only the query scope as vertical filter
	;
	
/** Groups a non-empty sequence of member bindings */
bindingsList
	: binding (AND binding)* 
	;
	
/** 
 * Binds a series of member references to a layer source.
 * 
 * The 'DISTINCT' keyword enforces that the bound member references in this binding do
 * NOT match the same target. Depending on the localConstraint used in the query, this
 * might be redundant (e.g. when using the member references as identifiers for tree nodes
 * who already are structurally distinct), but can still be used to make that fact explicit.
 */
binding
	: member (COMMA member)* AS DISTINCT? qualifiedIdentifier
	;
	
localConstraints
	: flatStatement 
	| TREE treeStatement 
	| GRAPH graphStatement
	;
	
/** Text-corpus query with a flat sequential structure  */
flatStatement
	: constraint
	| flatNode+
	;
	
flatNode
	: quantifier? LBRACK memberLabel? constraint? RBRACK
	;
	
memberLabel
	: member COLON
	;
	
/**
 * Treebank query
 * 
 * Possible scenarios for tree (root) query composition:
 * []					singleton
 * [][]					siblings
 * [] or []				alternatives
 * {[][]} or [] 		grouping with alternative
 * {[][]} or {[] or {[][]}} complex alternatives
 * 
 */
treeStatement
	: LBRACE treeStatement RBRACE		#treeNodeGrouping
	| treeNode+							#treeNodeSiblings
	| treeStatement OR treeStatement	#treeNodeAlternatives
	;
	
/**
 * Node scenarios:
 * 
 * []						empty, existentially quantified node
 * <xy>[]					empty, explicitly quantified node
 * [...]					existentially quantified node with inner constraints
 * <xy>[...]				explicitly quantified node with inner constraints
 * [[][[]]]					existentially quantified tree
 * [...[...][[...]]]		existentially quantified tree with (multiple) inner constraints
 * <x>[[]<y>[<z>[]]]		tree with explicit and implicit quantification
 */
treeNode
	: quantifier? LBRACK memberLabel? constraint? treeStatement? RBRACK
	;

/**
 * Complex query over graph data
 * 
 * Possible scenarios for graph query composition:
 * []							singleton
 * [],[],[]---[],[]-->[]		siblings and edges
 * [] or []-->[]				alternatives
 * {[],[]} or []-->[]			grouping with alternative
 * {[],[]} or {[] or {[],[]}} 	complex alternatives
 * 
 */
graphStatement
	: LBRACE graphStatement RBRACE			#graphNodeGrouping
	| graphElement (COMMA graphElement)*	#graphNodeSiblings
	| graphStatement OR graphStatement		#graphNodeAlternatives
	;
	
/**
 * Possible graph element scenarios:
 * [$a]					singleton node
 * [$a]-->[$b]			right-directed edge (a to b)
 * [$a]<--[$b]			right-directed edge (a to b)
 * [$a]---[$b]			undirected edge
 * [$a]<->[$b]			bidirectional edge
 * 
 * Quantifiers on node actually count together with the respective edge:
 * <4+>[$a]-->[$b]		at least 4 nodes matching $a with an edge to $b
 * [$a]-->3-[$b]		node $a with no more than 3 edges to nodes matching $b
 * 
 * Note:
 *  Edges always use a total of 3 symbols to define, so they can't be accidentally
 *  mixed up with combinations of signs and comparison operators.
 */
graphElement
	: graphNode
	/*
	 * Models left-directed, undirected, bidirectional and right-directed edges
	 * with or without inner constraints.
	 */
	| graphNode graphEdge graphNode
	;
	
graphNode
	: member
	| quantifier? LBRACK memberLabel? constraint? RBRACK
	;
	
graphEdge
	: (EDGE_LEFT | EDGE_RIGHT | EDGE_BIDIRECTIONAL | EDGE_UNDIRECTED)	# emptyGraphEdge
	| (EDGE_LEFT_DIRECTED | EDGE_OUTER_UNDIRECTED) LBRACK memberLabel? constraint? RBRACK (EDGE_RIGHT_DIRECTED | EDGE_OUTER_UNDIRECTED) # filledGraphEdge
	;
	
//TODO add more statement variations based on established CQL families
	
globalConstraints
	: constraint
	;
	
groupStatement
	: GROUP groupExpression (COMMA)
	;
	
groupExpression
	: BY expression (FILTER ON expression)? (LABEL StringLiteral)? (DEFAULT StringLiteral)?
	;
	
// STATEMENT END


// RESULT BEGIN

/** Post-processing directives for generating textual/statistical results */
result
	:
	;

// RESULT END


// APPENDIX BEGIN

/** Currently appendix of a query can only hold binary payloads as hex strings */
appendix
	: binaryPayload (COMMA binaryPayload)*
	;
	
binaryPayload
	: id=variableName ASSIGN HexLiteral
	;

// APPENDIX END

// GENERAL HELPERS
	
/** 
 * We keep this rule as an explicit marker for situation where the expression
 * is required to evaluate to a boolean type.
 */
constraint
	: expression 
	;
	
expressionList
	: expression (COMMA expression)*
	;
	
/*
    IQL binary operators, in order from highest to lowest precedence:
    *    /    %
    +    -
    <<   >>   &    |   ^
    <    <=   >    >=   
    ~   !~   #   !#
    ==   != 
    &&   AND
    ||   OR
*/
expression
	: primary																		# primaryExpression
	| expression DOT Identifier														# pathAccess
	/*
	 * Function calls can only occur after direct references.
	 * Arguments may evaluate to any type (a cast exception may be thrown).
	 */
	| expression {isAny(-1,Identifier)}? LPAREN expressionList? RPAREN 				# methodInvocation
	/*
	 * Array indices can only occur after direct references, array access, function calls or annotations.
	 * Arguments must evaluate to an 'int' type.
	 */
	| expression {isAny(-1,Identifier,RPAREN,RBRACE,RBRACK)}? LBRACK expressionList RBRACK		# arrayAccess
	/*
	 * Annotation can only occur after direct references, function calls or arrays.
	 * Arguments must evaluate to string values.
	 */
	| expression {isAny(-1,Identifier,RPAREN,RBRACK)}? LBRACE expressionList RBRACE	# annotationAccess
	| LPAREN type RPAREN expression													# castExpression
	| LPAREN expression RPAREN 														# wrappingExpression
	| source=expression (NOT | EXMARK)? IN all? LBRACE set=expressionList RBRACE 	# setPredicate
	| (NOT | EXMARK | MINUS) expression 											# unaryOp
	| left=expression (STAR | SLASH | PERCENT) right=expression 					# multiplicativeOp
	| left=expression (PLUS | MINUS) right=expression 								# additiveOp
	| left=expression (SHIFT_LEFT | SHIFT_RIGHT | PIPE | AMP | CARET) right=expression 		# bitwiseOp
	| left=expression (LT | LT_EQ | GT | GT_EQ) right=expression 					# comparisonOp
	| left=expression (TILDE | NOT_MATCHES | HASH | NOT_CONTAINS) right=expression 	# stringOp
	| left=expression (EQ | NOT_EQ) right=expression 								# equalityCheck
	| left=expression and right=expression 											# conjunction
	| left=expression or right=expression 											# disjunction
	| condition=expression QMARK optionTrue=expression COLON optionFalse=expression # ternaryOp
	| loopExpresseion																# forEach
	;
	
primary
	: nullLiteral
	| booleanLiteral
	| floatingPointLiteral
	| integerLiteral
	| StringLiteral
	| reference	
	;
	
reference
	: variableName
	| member
	| Identifier
	;
	
loopExpresseion
	// expression source must be a reference to an iterable object or collection
	: FOREACH expression AS variableName loopControl (COUNT counterList)?
	;
	
loopControl
	: (EVEN | ODD)? (OMIT omit=constraint)? (RANGE range=boundedRange)? (STEP step=boundedRange)? (DO body=constraint)? END
	;
	
boundedRange
	: LPAREN from=expression COMMA to=expression RPAREN
	;
	
counterList
	: counter (COMMA counter)*
	;
	
counter
	: constraint AS variableName
	;
	
type
	: BOOLEAN
	| STRING
	| INT
	| LONG
	| FLOAT
	| DOUBLE
	;
	
quantifier
	: all
	| EXMARK
	| unsignedSimpleQuantifier ( PIPE unsignedSimpleQuantifier )*  
	| LT unsignedSimpleQuantifier ( PIPE unsignedSimpleQuantifier )* GT
	;
	
unsignedSimpleQuantifier
	: PureDigits 						# exactQuantifier
	| PureDigits PLUS					# atLeastQuantifier
	| PureDigits MINUS					# atMostQuantifier
	| PureDigits DOUBLE_DOT PureDigits	# rangeQuantifier
	;
	
all 
	: STAR 
	| ALL
	;
	
and
	: AND
	| DOUBLE_AMP
	;
	
or
	: OR
	| DOUBLE_PIPE
	;
	
variableName
	: AT Identifier
	;

/** 
 * Models the actual member variables for a matching.
 * The optional index suffix
 */
member
	: DOLLAR Identifier
	;
	
//signedSimpleQuantifier
//	: sign? PureDigits sign?
//	| sign? PureDigits DOUBLE_DOT sign? PureDigits
//	;
	
versionDeclaration
	: major=PureDigits (DOT minor=PureDigits)? (DOT build=PureDigits)? (MINUS | UNDERSCORE | COLON)? suffix=Identifier? 
	;

/**
 * Low-level literals 
 */	
 
nullLiteral
	: NULL
	;
 
floatingPointLiteral
 	: signedFloatingPointLiteral
 	;
 
signedFloatingPointLiteral
 	: sign? unsignedFloatingPointLiteral
 	;
 
unsignedFloatingPointLiteral
 	: unsignedIntegerLiteral DOT unsignedIntegerLiteral
 	;
 
booleanLiteral
	:	TRUE
	|	FALSE
	;
 
integerLiteral
 	: signedIntegerLiteral
 	;
 
signedIntegerLiteral
	: sign? unsignedIntegerLiteral //{isNone(-1,PLUS,MINUS,Digits,PureDigits)}?
	;

unsignedIntegerLiteral
	: Digits
	| PureDigits
	;

sign 
	: PLUS
	| MINUS
	;


/** 
 * Lexer Rules
 */

// Keywords
NULL : 'NULL' | 'null' ;
TRUE : 'TRUE' | 'true' ;
FALSE : 'FALSE' | 'false' ;
FROM : 'FROM' | 'from' ;
SETUP : 'SETUP' | 'setup' ;
SELECT : 'SELECT' | 'select' ;
AS : 'AS' | 'as' ;
SCOPE : 'SCOPE' | 'scope' ;
WITH : 'WITH' | 'with' ;
DISTINCT : 'DISTINCT' | 'distinct' ;
WHERE : 'WHERE' | 'where' ;
TREE : 'TREE' | 'tree' ;
GRAPH : 'GRAPH' | 'graph' ;
OR : 'OR' | 'or' ;
AND : 'AND' | 'and' ;
ELEMENTS : 'ELEMENTS' | 'elements' ;
HAVING : 'HAVING' | 'having' ;
LOCAL : 'LOCAL' | 'local' ;
RANGE : 'RANGE' | 'range' ;
LIMIT : 'LIMIT' | 'limit' ;
ALL : 'ALL' | 'all' ;
NOT : 'NOT' | 'not' ;
IMPORT : 'IMPORT' | 'import' ;
LABEL : 'LABEL' | 'label' ;
DEFAULT : 'DEFAULT' | 'default' ;
GROUP : 'GROUP' | 'group' ;
IN : 'IN' | 'in' ;
FOREACH : 'FOREACH' | 'foreach' ;
EVEN : 'EVEN' | 'even' ;
ODD : 'ODD' | 'odd' ;
OMIT : 'OMIT' | 'omit' ;
STEP : 'STEP' | 'step' ;
DO : 'DO' | 'do' ;
END : 'END' | 'end' ;
COUNT : 'COUNT' | 'count' ;
DIALECT : 'DIALECT' | 'dialect' ;
PRIMARY : 'PRIMARY' | 'primary' ;
APPEND : 'APPEND' | 'append' ;
BY : 'BY' | 'by' ;
FILTER : 'FILTER' | 'filter' ;
ON : 'ON' | 'on' ;
RETURN : 'RETURN' | 'return' ;
APPENDIX : 'APPENDIX' | 'appendix' ;

// Types
INT : 'int' ;
LONG : 'long' ;
FLOAT : 'float' ;
DOUBLE : 'double' ;
BOOLEAN : 'boolean' ;
STRING : 'string' ;

// Separators
SCOLON : ';';
COLON : ':';
DOT : '.';
LPAREN : '(';
RPAREN : ')';
LBRACE : '{';
RBRACE : '}';
LBRACK : '[';
RBRACK : ']';
COMMA : ',';
UNDERSCORE : '_';

// Edge definitions without inner constraints
EDGE_LEFT : '<--';
EDGE_RIGHT : '-->';
EDGE_BIDIRECTIONAL :'<->';
EDGE_UNDIRECTED: {before("]")}? '---' {ahead("[")}?;
// Edge definitions with inner constraints
EDGE_LEFT_DIRECTED : {before("]")}? '<-' {ahead("[")}?;
EDGE_OUTER_UNDIRECTED: {before("]")}? '--' {ahead("[")}?;
EDGE_RIGHT_DIRECTED : {before("]")}? '->' {ahead("[")}?;
 
// Operator symbols
ASSIGN : '=';
STAR : '*';
PLUS : '+';
MINUS : '-';
TILDE : '~';
SLASH : '/';
PERCENT : '%';
AMP : '&';
DOUBLE_AMP : '&&';
CARET : '^';
PIPE : '|';
DOUBLE_PIPE : '||';
SHIFT_LEFT : '<<';
SHIFT_RIGHT : '>>';
LT : '<';
LT_EQ : '<=';
GT : '>';
GT_EQ : '>=';
EQ : '==';
NOT_EQ : '!=';
NOT_MATCHES : '!~';
NOT_CONTAINS : '!#';

// Helper symbols
AT : '@';
DOLLAR : '$';
HASH : '#';
QMARK : '?';
EXMARK : '!';
// Range and linking punctuation
DOUBLE_COLON : '::';
DOUBLE_DOT : '..';

VersionPrefix
	: 'v'
	;

// Literals

//ExponentPart
//	:	[eE] [+-]? (Digits | PureDigits) 
//	;

PureDigits
	: Digit+
	;

Digits
	: Digit (DigitsAndUnderscores? Digit)?
	;

fragment
DigitsAndUnderscores
	:	DigitOrUnderscore+
	;

fragment
DigitOrUnderscore
	:	Digit
	|	UNDERSCORE
	;

fragment Digit 
 	: 	Zero
	| 	NonZeroDigit
 	;
 
fragment Zero : '0';

fragment NonZeroDigit : [1-9];

fragment
Underscores
	:	UNDERSCORE+
	;
	
StringLiteral
	:	'"' StringCharacters? '"'
	;
	
fragment
StringCharacters
	:	StringCharacter+
	;
	
fragment
StringCharacter
	:	~["\\\r\n\f\b\t]
	|	EscapeSequence
	;
	
fragment
EscapeSequence
	:	'\\' [rnfbt"\\]
	;

// Identifiers
Identifier
 	: 	IdentifierBegin IdentifierPart* IdentifierEnd?
 	;

fragment
IdentifierPart
 	: 	IdentifierEnd
 	| 	UNDERSCORE
	;

fragment
IdentifierEnd
 	: 	IdentifierBegin
 	| 	Digit
 	;

fragment
IdentifierBegin
 	: 	LOWERCASE 
 	| 	UPPERCASE
 	;

HexLiteral
	:	'0' [xX] HexDigits
	;

fragment
HexDigits
	:	HexDigit (HexDigitsAndUnderscores? HexDigit)?
	;

fragment
HexDigit
	:	[0-9a-fA-F]
	;

fragment
HexDigitsAndUnderscores
	:	HexDigitOrUnderscore+
	;

fragment
HexDigitOrUnderscore
	:	HexDigit
	|	'_'
	;	

// Basic alnum symbols
fragment LOWERCASE  : [a-z] ;
fragment UPPERCASE  : [A-Z] ;

// Ignored content
WS : [ \t\r\n\u000C\u000B]+ -> skip;

SL_COMMENT : '//' .*? '\n' -> skip;

ErrorCharacter : . ;
