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

options {
//    tokenVocab=IQLLexer;
	language = Java;
}

@parser::header {
package de.ims.icarus2.query.api.iql.antlr;
}

@lexer::header {
package de.ims.icarus2.query.api.iql.antlr;
import org.antlr.v4.runtime.misc.Interval;	
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

}

@lexer::members {
public static final int WHITESPACE = 1;
public static final int COMMENTS = 2;
}

/**
 * Parser Rules
 */

/** Standalone rules */


// Standalone statement parts
standaloneNodeStatement : nodeStatement EOF ;
standaloneSelectiveStatement : selectionStatement EOF ;

// Standalone helpers
standaloneExpression : expression EOF ;

/** 
 * Actual selector part of a query:
 * 
 * Bindings can (and must!!) be used to simplify the subsequent part of the query.
 * Local constraints are the only obligatory part and define the basic
 * complexity for evaluating the query. Global constraints are optional and 
 * can cause unlimited increase in complexity!
 */
payloadStatement
	: ALL EOF// special marker to return the entire corpus, with only the query scope as vertical filter
	| bindingsList? (FILTER BY constraint)? FIND (FIRST | LAST | ANY)? selectionStatement EOF
	;
	
/** Groups a non-empty sequence of member bindings */
bindingsList
	: WITH binding (AND binding)* 
	;
	
/** 
 * Binds a series of member references to a layer source.
 * 
 * The 'DISTINCT' keyword enforces that the bound member references in this binding do
 * NOT match the same target. Depending on the localConstraint used in the query, this
 * might be redundant (e.g. when using the member references as identifiers for tree nodes
 * who already are structurally distinct), but can still be used to make that fact explicit.
 * 
 * The 'EDGES' keyword declares the binding to target edges in the specified layer instead of
 * fragments or other 'first class' item derivatives. Note that edges are always distinct and
 * as such a combination of the 'EDGES' and 'DISTINCT' keywords is not possible!
 */
binding
	: (DISTINCT | EDGES)? member (COMMA member)* FROM Identifier
	;

selectionStatement
	: constraint // plain
	| (nodeStatement | laneStatementsList) (HAVING constraint)? //structural constraints
	;	

laneStatementsList
	: laneStatement (AND laneStatement)*
	;
	
laneStatement
	: LANE name=Identifier (AS member)? nodeStatement
	;
	
/**
 * Possible scenarios for node composition:
 * []					singleton
 * [][]					siblings
 * [] or []				alternatives
 * {[][]} or [] 		grouping with alternative
 * {[][]} or {[] or {[][]}} complex alternatives
 * 
 * For Graphs:
 * [],[],[]---[],[]-->[]		unconnected nodes and edges
 * [] or []-->[]				alternatives
 * {[],[]} or []-->[]			grouping with alternative
 */
nodeStatement
	: LBRACE nodeStatement RBRACE				#nodeGrouping
	| nodeArrangement? node+										#nodeSequence
	| element (COMMA element)*					#elementSequence	
	| <assoc=right> left=nodeStatement or right=nodeStatement			#nodeAlternatives
	;
	
nodeArrangement
	: ORDERED
	| ADJACENT
	;
	
/**
 * Node scenarios:
 * 
 * []						empty, existentially quantified node
 * <xy>[]					empty, explicitly quantified node
 * [...]					existentially quantified node with inner constraints
 * <xy>[...]				explicitly quantified node with inner constraints
 * 
 * For trees:
 * [[][[]]]					existentially quantified tree
 * [...[...][[...]]]		existentially quantified tree with (multiple) inner constraints
 * <x>[[]<y>[<z>[]]]		tree with explicit and implicit quantification
 */
 // The grammar overgenerates here due to 'nodeStatement' allowing graph structures, but we handle that later 
node
	: quantifier? LBRACK memberLabel? constraint? nodeStatement? RBRACK
	;
	
memberLabel
	: member COLON
	;
	
/**
 * Possible graph element scenarios:
 * [$a]					singleton node
 * [$a]-->[$b]			right-directed edge (a to b)
 * [$a]<--[$b]			right-directed edge (a to b)
 * [$a]---[$b]			undirected edge
 * [$a]<->[$b]			bidirectional edge
 * [$a]--[$e]--[$b]		undirected edge $e with internal constraints between $a and $b
 * 
 * Quantifiers on node actually count together with the respective edge:
 * <4+>[$a]-->[$b]		at least 4 nodes matching $a with an edge to $b
 * [$a]-->3-[$b]		node $a with no more than 3 edges to nodes matching $b
 * 
 * Notes:
 * 
 *  Edges always use a total of 3 symbols to define, so they can't be accidentally
 *  mixed up with combinations of signs and comparison operators.
 * 
 *  Member labels can only be used once for a full grown node. If a node is required
 *  for more than one graphElement expression, additional statements must use the simple
 *  member reference instead!
 */
element
	: content=node
	/*
	 * Models left-directed, undirected, bidirectional and right-directed edges
	 * with or without inner constraints.
	 */
	| source=node edge target=node
	;

edge
	: (EDGE_LEFT | EDGE_RIGHT | EDGE_BIDIRECTIONAL | EDGE_UNDIRECTED)	# emptyEdge
	| leftEdgePart LBRACK memberLabel? constraint? RBRACK rightEdgePart # filledEdge
	;
	
leftEdgePart
	: directedEdgeLeft | undirectedEdge
	;
	
rightEdgePart
	: directedEdgeRight | undirectedEdge
	;
	
directedEdgeLeft
	: LT MINUS //check-adjacent
	;
	
directedEdgeRight
	: MINUS GT //check-adjacent
	;
	
undirectedEdge
	: MINUS MINUS //check-adjacent
	;
	
//TODO add more statement variations based on established CQL families
	
// STATEMENT END


// GROUPING BEGIN
	
groupStatement
	: GROUP groupExpression (COMMA groupExpression)* EOF
	;
	
groupExpression
	: BY selector=expression (FILTER ON filter=expression)? LABEL label=StringLiteral (DEFAULT defaultValue=expression)?
	;

// GROUPING END
	
// RESULT BEGIN

/** 
 * Post-processing directives for generating textual/statistical results:
 * 
 * LIMIT:
 * Limit the result to the first N reported matches
 * 
 * ORDER BY:
 * General result reordering. Potentially expensive
 * 
 * BEST N (LIMIT+ORDER BY):
 * Find the N biggest or smallest results according to the specified order
 * criteria. Note that if a tie between two results cannot be resolved by
 * using the provided order expressions, it is undefined which the evaluation
 * engine will pick.
 */
resultStatement
	: (LIMIT limit=unsignedIntegerLiteral PERCENT?)? orderExpressionList? EOF
	;
	
orderExpressionList
	: ORDER BY orderExpression (COMMA orderExpression)*
	;
	
orderExpression
	: expression (ASC | DESC)
	;
	
// RESULT END	

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
	
/**
 * Core rule to express complex formulas/references/constraints, basically everything...
 * 
    IQL binary operators, in order from highest to lowest precedence:
    *    /    %
    +    -
    <<   >>   &    |   ^
    <    <=   >    >=   
    =~   !~   =#   !#
    ==   != 
    &&   AND
    ||   OR
*/
expression
	: primary																		# primaryExpression
	| source=expression DOT Identifier														# pathAccess
	/*
	 * Function calls can only occur after direct references.
	 * Arguments may evaluate to any type (a cast exception may be thrown).
	 */
	| source=expression {isAny(-1,Identifier)}? LPAREN arguments=expressionList? RPAREN 				# methodInvocation
	/*
	 * List indices can only occur after direct references, list access, function calls or annotations.
	 * Arguments must evaluate to an 'int' type.
	 */
	| source=expression {isAny(-1,Identifier,RPAREN,RBRACE,RBRACK)}? LBRACK indices=expressionList RBRACK		# listAccess
	/*
	 * Annotation can only occur after direct references, function calls or lists.
	 * Arguments must evaluate to string values.
	 */
	| source=expression {isAny(-1,Identifier,RPAREN,RBRACK)}? LBRACE keys=expressionList RBRACE	# annotationAccess
	| LPAREN type RPAREN expression													# castExpression
	| LPAREN expression RPAREN 														# wrappingExpression
	| (NOT | EXMARK | MINUS | TILDE) expression 									# unaryOp
	| left=expression (STAR | SLASH | PERCENT) right=expression 					# multiplicativeOp
	| left=expression (PLUS | MINUS) right=expression 								# additiveOp
	| left=expression (SHIFT_LEFT | SHIFT_RIGHT | PIPE | AMP | CARET) right=expression 		# bitwiseOp
	| left=expression (LT | LT_EQ | GT | GT_EQ) right=expression 					# comparisonOp
	| left=expression (MATCHES | NOT_MATCHES | CONTAINS | NOT_CONTAINS) right=expression 	# stringOp
	| left=expression (EQ | NOT_EQ) right=expression 								# equalityCheck
	| <assoc=right> left=expression and right=expression 											# conjunction
	| <assoc=right> left=expression or right=expression 											# disjunction
	// Optional extra expressions that will be supported fully in a later IQL iteration
	| source=expression AS (member | variableName)											# assignmentOp
	| source=expression all? not? IN target=expression 	# setPredicate
	| condition=expression QMARK optionTrue=expression COLON optionFalse=expression # ternaryOp
	| loopExpresseion																# forEach
	;
	
primary
	: nullLiteral
	| booleanLiteral
	| floatingPointLiteral
	| integerLiteral
	| StringLiteral
	| setStatement
	| reference	
	;
	
setStatement
	: (type LBRACK RBRACK)? LBRACE expressionList? RBRACE
	;
	
reference
	: variableName
	| member
	| Identifier
	| qualifiedIdentifier // always points to annotations
	;
	
/** Addressing an annotation via layer::key */
qualifiedIdentifier
	: hostId=Identifier DOUBLE_COLON elementId=Identifier
	;
	
loopExpresseion
	// expression source must be a reference to an iterable object or collection
	: FOREACH expression AS variableName loopControl counterList?
	;
	
loopControl
	: (EVEN | ODD)? (OMIT omit=constraint)? (RANGE range=boundedRange)? (STEP step=boundedRange)? (DO body=constraint)? END
	;
	
boundedRange
	: LPAREN from=expression COMMA to=expression RPAREN
	;
	
counterList
	: COUNT counter (COMMA counter)*
	;
	
counter
	: constraint AS variableName
	;
	
type
	: BOOLEAN
	| STRING
	| INT // all kinds of integer types (up to 64bit signed integer)
	| FLOAT // all kinds of floating point types (up to 64bit double precision)
	;
	
quantifier
	: simpleQuantifier ( PIPE simpleQuantifier )*  
	| LT simpleQuantifier ( PIPE simpleQuantifier )* GT
	;
	
simpleQuantifier
	: all
	| not
	| value=PureDigits 						
	| value=PureDigits PLUS					
	| value=PureDigits MINUS					
	| lowerBound=PureDigits DOUBLE_DOT upperBound=PureDigits
	;
	
not
	: NOT
	| EXMARK
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

//TODO change all keywords to use character fragments to make them resistant against camel casing abuse <- actually intended?

//TODO add specification about unicode codepoints for all special characters we use here

ADJACENT : 'ADJACENT' | 'adjacent';
ALL : 'ALL' | 'all' ;
AND : 'AND' | 'and' ;
ANY : 'ANY' | 'any' ;
AS : 'AS' | 'as' ;
ASC : 'ASC' | 'asc' ;
BY : 'BY' | 'by' ;
COUNT : 'COUNT' | 'count' ;
DEFAULT : 'DEFAULT' | 'default' ;
DESC : 'DESC' | 'desc' ;
DISTINCT : 'DISTINCT' | 'distinct' ;
DO : 'DO' | 'do' ;
EDGES : 'EDGES' | 'edges' ;
END : 'END' | 'end' ;
EVEN : 'EVEN' | 'even' ;
FALSE : 'FALSE' | 'false' ;
FILTER : 'FILTER' | 'filter' ;
FIND : 'FIND' | 'find' ;
FIRST : 'FIRST' | 'first' ;
FOREACH : 'FOREACH' | 'foreach' ;
FROM : 'FROM' | 'from' ;
GROUP : 'GROUP' | 'group' ;
HAVING : 'HAVING' | 'having' ;
IN : 'IN' | 'in' ;
LABEL : 'LABEL' | 'label' ;
LANE : 'LANE' | 'lane' ;
LAST : 'LAST' | 'last' ;
LIMIT : 'LIMIT' | 'limit' ;
NOT : 'NOT' | 'not' ;
NULL : 'NULL' | 'null' ;
ODD : 'ODD' | 'odd' ;
OMIT : 'OMIT' | 'omit' ;
ON : 'ON' | 'on' ;
OR : 'OR' | 'or' ;
ORDER : 'ORDER' | 'order' ;
ORDERED : 'ORDERED' | 'ordered';
RANGE : 'RANGE' | 'range' ;
STEP : 'STEP' | 'step' ;
TRUE : 'TRUE' | 'true' ;
WITH : 'WITH' | 'with' ;

// Keywords

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
EDGE_UNDIRECTED: '---';
 

// Helper symbols
AT : '@';
DOLLAR : '$';
HASH : '#';
QMARK : '?';
EXMARK : '!';
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
MATCHES : '=~';
NOT_MATCHES : '!~';
CONTAINS : '=#';
NOT_CONTAINS : '!#';
// Range and linking punctuation
DOUBLE_COLON : '::';
DOUBLE_DOT : '..';

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
	:	~["\\\r\n\t]
	|	EscapeSequence
	;
	
fragment
EscapeSequence
	:	'\\' [rnt"\\]
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

// Basic alnum symbols
fragment LOWERCASE  : [a-z] ;
fragment UPPERCASE  : [A-Z] ;

// Ignored content
WS : [ \t\r\n\u000C\u000B]+ -> skip;

SL_COMMENT : '//' .*? '\n' -> skip;

ErrorCharacter : . ;
