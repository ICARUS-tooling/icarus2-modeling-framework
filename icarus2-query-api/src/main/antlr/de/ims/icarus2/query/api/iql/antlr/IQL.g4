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
	
public static final int WHITESPACE = 1;
public static final int COMMENTS = 2;
	
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

/**
 * Parser Rules
 */

/** Standalone rules */


// Standalone statement parts
standaloneNodeStatement : nodeStatement EOF ;
standaloneSelectiveStatement : selectiveStatement EOF ;

// Standalone helpers
standaloneExpression : expression EOF ;

/** 
 * Actual selector part of a query:
 * 
 * Bindings can be sued to simplify the subsequent part of the query.
 * Local constraints are the only obligatory part and define the basic
 * complexity for evaluating the query. Global constraints are optional and 
 * can cause unlimited increase in complexity!
 */
constraintStatement
	: ALL EOF// special marker to return the entire corpus, with only the query scope as vertical filter
	| (WITH bindingsList FIND)? selectiveStatement EOF
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

selectiveStatement
	: constraint												# plainStatement
	| nodeStatement (HAVING globalConstraints)? 				# sequenceStatement
	| ALIGNED? TREE nodeStatement (HAVING globalConstraints)?	# treeStatement
	| ALIGNED? GRAPH nodeStatement (HAVING globalConstraints)? 	# graphStatement
	;	
	
/** Addressing a layer/context or other embedded member via the identifiers of its environment */
qualifiedIdentifier
	: Identifier (DOUBLE_COLON Identifier)*
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
 * [],[],[]---[],[]-->[]		siblings and edges
 * [] or []-->[]				alternatives
 * {[],[]} or []-->[]			grouping with alternative
 */
nodeStatement
	: LBRACE nodeStatement RBRACE				#nodeGrouping
	| node+										#nodeSequence
	| element (COMMA element)*					#elementSequence	
	| nodeStatement OR nodeStatement			#nodeAlternatives
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
	: node
	/*
	 * Models left-directed, undirected, bidirectional and right-directed edges
	 * with or without inner constraints.
	 */
	| node edge node
	;

edge
	: (EDGE_LEFT | EDGE_RIGHT | EDGE_BIDIRECTIONAL | EDGE_UNDIRECTED)	# emptyEdge
	| (directedEdgeLeft | undirectedEdge) LBRACK memberLabel? constraint? RBRACK (directedEdgeRight | undirectedEdge) # filledEdge
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
	
globalConstraints
	: constraint
	;
	
// STATEMENT END


// GROUPING BEGIN
	
groupStatement
	: GROUP groupExpression (COMMA groupExpression) EOF
	;
	
groupExpression
	: BY selector=expression (FILTER ON filter=expression)? (LABEL label=StringLiteral)? (DEFAULT defaultValue=StringLiteral)?
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
 * criteria. Note that it a tie between two results cannot be resolved by
 * using the provided order expressions, it is undefined which the evaluation
 * engine will pick.
 */
resultStatement
	: (LIMIT limit=unsignedIntegerLiteral)? (ORDER BY orderExpressionList)? EOF
	;
	
orderExpressionList
	: orderExpression (COMMA orderExpression)*
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

EVEN : 'EVEN' | 'even' ;
ODD : 'ODD' | 'odd' ;
OMIT : 'OMIT' | 'omit' ;
STEP : 'STEP' | 'step' ;
DO : 'DO' | 'do' ;
END : 'END' | 'end' ;
COUNT : 'COUNT' | 'count' ;
FOREACH : 'FOREACH' | 'foreach' ;
NULL : 'NULL' | 'null' ;
TRUE : 'TRUE' | 'true' ;
FALSE : 'FALSE' | 'false' ;
TREE : 'TREE' | 'tree' ;
OR : 'OR' | 'or' ;
AND : 'AND' | 'and' ;
AS : 'AS' | 'as' ;
ALL : 'ALL' | 'all' ;
NOT : 'NOT' | 'not' ;
IN : 'IN' | 'in' ;
ALIGNED : 'ALIGNED' | 'aligned';
GRAPH : 'GRAPH' | 'graph' ;
HAVING : 'HAVING' | 'having' ;
RANGE : 'RANGE' | 'range' ;
DISTINCT : 'DISTINCT' | 'distinct' ;
WITH : 'WITH' | 'with' ;
FIND : 'FIND' | 'find' ;
GROUP : 'GROUP' | 'group' ;
BY : 'BY' | 'by' ;
LABEL : 'LABEL' | 'label' ;
DEFAULT : 'DEFAULT' | 'default' ;
FILTER : 'FILTER' | 'filter' ;
ON : 'ON' | 'on' ;
LIMIT : 'LIMIT' | 'limit' ;
ORDER : 'ORDER' | 'order' ;
ASC : 'ASC' | 'asc' ;
DESC : 'DESC' | 'desc' ;

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

// Basic alnum symbols
fragment LOWERCASE  : [a-z] ;
fragment UPPERCASE  : [A-Z] ;

// Ignored content
WS : [ \t\r\n\u000C\u000B]+ -> channel(1);

SL_COMMENT : '//' .*? '\n' -> channel(2);

ErrorCharacter : . ;
