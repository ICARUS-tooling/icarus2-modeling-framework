/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

@header {
	package de.ims.icarus2.query.api.iql;
}

/**
 * Parser Rules
 */

query
	: preambel? statement 
	;
	
preambel
	: dialectDefinition? importStatement+ // here goes configuration stuff, namespace declarations, etc...
	;
	
/** Primarily for future use: specify the exact IQL dialect to use */
dialectDefinition
	: DIALECT versionDeclaration
	;
	
/** Import external extensions or script definitions, optionally renaming their namespace */
importStatement
	: IMPORT StringLiteral (AS Identifier)?
	;

/** Core of every query: specify what to extract */
statement
 	: sourceStatement selectStatement;
 	
sourceStatement
	: FROM corpusSelection
	;

selectStatement
	: SELECT layerSelection
	;

corpusSelection
	: corpusSelector (COMMA corpusSelector)*
	;
	
//TODO needs support for namespaces since we can't assume all corpus ids to be unique
corpusSelector
	: Identifier identifierAssignment?
	| StringLiteral identifierAssignment
	;
	
layerSelection
	: layerSelector (COMMA layerSelector)*
	;
	
layerSelector 
	: qualifiedIdentifier (identifierAssignment | scopeDefinition)? //TODO AS PRIMARY assignment?
	;
	
scopeDefinition
	: AS SCOPE Identifier //TODO
	;

/** Allows to reassign a previously resolved (qualified) identifier to a new (shorter?) identifier */
identifierAssignment
	: AS Identifier
	;
	
/** Addressing a layer/context or other embedded member via the identifiers of its environment */
qualifiedIdentifier
	: Identifier (COLONCOLON Identifier)*
	;
	
/** dot-style path definition for referencing methods or attributes */
path
	: (variable | anyName) (DOT pathElement)*
	;

pathElement
	: name
	| arrayElement
	;
	
arrayElement
	: name LBRACK (arrayIndex | integerFunction ) RBRACK
	;
	
variable
	: AT name
	;

anyName
	: prefixedName
	| name
	;
	
prefixedName
	: DOLLAR name
	;
	
name
	: Identifier
	;
	
call
	: path LPAREN arguments? RPAREN
	;
	
arguments
	: expression ( COMMA expression )*
	;
	
annotation
	: path LBRACK StringLiteral RBRACK
	;
	
valueSource
	: literal
	| call
	| path
	| annotation
	;
	
relativeExpression
	: UnsignedIntegerLiteral
	| UnsignedFloatingPointLiteral
	| numericalFunction
	;
	
omitExpression
	: UnsignedIntegerLiteral
	| UnsignedFloatingPointLiteral
	| numericalFunction
	| StringLiteral // denoting the identifier of a random number list, to be resolved by the environment
	;
	
numericalFunction
	: call //TODO needs some tagging to ensure numerical return value
	;
	
integerFunction
	: call //TODO needs some tagging to ensure integer return value
	;
	
booleanFunction
	: call //TODO needs some tagging to ensure boolean return value
	;
	
iteratorFunction
	: call //TODO needs some tagging to ensure boolean return value
	;
	
constraint
	: (NOT | EXMARK ) constraint
	| predicate
	| groupAssignment
	| loopConstraint
	| LPAREN constraint RPAREN
	| constraint OR constraint
	| constraint AND constraint
	;
	
loopConstraint
	: FOREACH iterator AS variable loopControl counterList? 
	;
	
loopControl
	: ( EVEN | ODD )? ( OMIT constraint )? ( RANGE boundedRange )? ( STEP boundedRange )? loopBody
	;

loopBody
	: DO constraint END
	| END
	;
	
boundedRange
	: LPAREN relativeExpression COMMA relativeExpression RPAREN
	;
	
counterList
	: counter ( AND counter )*
	;
	
counter
	: COUNT constraint AS variable
	;
	
iterator
	: iteratorFunction
	| LPAREN iteratorFunction ( COMMA iteratorFunction )* RPAREN
	;
	
predicate
	: booleanFunction
	| binaryComparison
	| setPredicate
	;
	
binaryComparison
	: expression binaryComparator expression
	;
	
binaryComparator
	: EQ # equals
	| LT # less
	| LT_EQ # lessOrEqual
	| GT # greater
	| GT_EQ # greatherOrEqual
	| quantifiedBinaryComparator # quantified
	| quantifiedBinaryComparator STAR # universal
	;
	
quantifiedBinaryComparator
	: NOT_EQ # equalsNot
	| TILDE # matches
	| NOT_MATCHES # matchesNot
	| HASH # contains
	| NOT_CONTAINS # containsNot
	;
	
setPredicate
	: expression ( NOT | EXMARK ) IN STAR? LBRACE arguments RBRACE
	;

groupAssignment
	: expression groupSelector (LABEL StringLiteral)? (DEFAULT StringLiteral)?
	;
	
groupSelector
	: GROUPING IntegerLiteral
	| GROUP IntegerLiteral
	;	
	
expression
	: valueSource
	| LPAREN expression RPAREN
	| expression expressionOperator expression
	;
	
expressionOperator
	: PLUS
	| MINUS
	| DIV
	| MOD
	| SLASH
	| PERCENT
	| STAR
	| AMP
	| CARET
	;
	
literal
	: IntegerLiteral
	| UnsignedIntegerLiteral
	| FloatingPointLiteral
	| UnsignedFloatingPointLiteral
	| StringLiteral
	| BooleanLiteral
	;
	
arrayIndex
	: STAR
	| signedQuantifier
	;
	
quantifier
	: STAR
	| unsignedQuantifier ( PIPE unsignedQuantifier )*
	| LT unsignedQuantifier ( PIPE unsignedQuantifier )* GT
	;
	
unsignedQuantifier
	: UnsignedIntegerLiteral Sign?
	| UnsignedIntegerLiteral DOTDOT UnsignedIntegerLiteral
	;
	
signedQuantifier
	: IntegerLiteral Sign?
	| IntegerLiteral DOTDOT IntegerLiteral
	;
	
versionDeclaration
	: 'v'? UnsignedIntegerLiteral (DOT UnsignedIntegerLiteral)? (DOT UnsignedIntegerLiteral)? Identifier? 
	;

/** 
 * Lexer Rules
 */

// Keywords
FROM : 'FROM' | 'from' ;
SELECT : 'SELECT' | 'select' ;
AS : 'AS' | 'as' ;
SCOPE : 'SCOPE' | 'scope' ;
NODES : 'NODES' | 'nodes' ;
EDGES : 'EDGES' | 'edges' ;
OR : 'OR' | 'or' ;
AND : 'AND' | 'and' ;
ELEMENTS : 'ELEMENTS' | 'elements' ;
HAVING : 'HAVING' | 'having' ;
LOCAL : 'LOCAL' | 'local' ;
RANGE : 'RANGE' | 'range' ;
LIMIT : 'LIMIT' | 'limit' ;
ALL : 'ALL' | 'all' ;
NOT : 'NOT' | 'not' ;
MOD : 'MOD' | 'mod' ;
DIV : 'DIV' | 'div' ;
IMPORT : 'IMPORT' | 'import' ;
LABEL : 'LABEL' | 'label' ;
DEFAULT : 'DEFAULT' | 'default' ;
GROUP : 'GROUP' | 'group' ;
IN : 'IN' | 'in' ;
FOREACH : 'FOREACH' | 'foreach' ;
EVEN : 'EVEN' | 'even' ;
ODD : 'ODD' | 'odd' ;
OMIT : 'OMI' | 'omit' ;
STEP : 'STEP' | 'step' ;
DO : 'DO' | 'do' ;
END : 'END' | 'end' ;
COUNT : 'COUNT' | 'count' ;
DIALECT : 'DIALECT' | 'dialect' ;

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

// Literals

UnsignedFloatingPointLiteral
	:	Digits '.' DecimalPart 
	;

FloatingPointLiteral
	:	Sign? UnsignedFloatingPointLiteral
	;

fragment
DecimalPart
	:	Digits
	|	Digits ExponentPart
	|	ExponentPart
	;

fragment
ExponentPart
	:	ExponentIndicator SignedInteger
	;

fragment
ExponentIndicator
	:	[eE] 
	;
 
UnsignedIntegerLiteral
	:	Zero
	|	NonZeroDigit (Digits? | Underscores Digits)
	;
 
IntegerLiteral
	:	UnsignedIntegerLiteral
	| 	SignedInteger
	;

fragment
SignedInteger
	:	Sign? Digits
	;

Sign
	:	[+-]
	;

fragment
Digits
	:	Digit (DigitsAndUnderscores? Digit)?
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
 
BooleanLiteral
	:	'true'
	|	'false'
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
	:	~["\\\r\n]
	|	EscapeSequence
	;
	
fragment
EscapeSequence
	:	'\\' [btnfr"\\]
	;
	

// Ignored content
WS : [ \t\r\n\u000C\u000B]+ -> skip;

// Separators
SCOL : ';';
COL : ':';
DOT : '.';
LPAREN : '(';
RPAREN : ')';
LBRACE : '{';
RBRACE : '}';
LBRACK : '[';
RBRACK : ']';
COMMA : ',';
 
// Operator symbols
ASSIGN : '=';
STAR : '*';
PLUS : '+';
MINUS : '-';
TILDE : '~';
PIPE2 : '||';
SLASH : '/';
PERCENT : '%';
AMP : '&';
CARET : '^';
PIPE : '|';
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
ARROW : '=>';
COLONCOLON : '::';
DOTDOT : '..';
UNDERSCORE : '_';
//X : 'x' ;
GROUPING : '<*>';

// Basic alnum symbols
fragment LOWERCASE  : [a-z] ;
fragment UPPERCASE  : [A-Z] ;

//fragment A : [aA];
//fragment B : [bB];
//fragment C : [cC];
//fragment D : [dD];
//fragment E : [eE];
//fragment F : [fF];
//fragment G : [gG];
//fragment H : [hH];
//fragment I : [iI];
//fragment J : [jJ];
//fragment K : [kK];
//fragment L : [lL];
//fragment M : [mM];
//fragment N : [nN];
//fragment O : [oO];
//fragment P : [pP];
//fragment Q : [qQ];
//fragment R : [rR];
//fragment S : [sS];
//fragment T : [tT];
//fragment U : [uU];
//fragment V : [vV];
//fragment W : [wW];
//fragment X : [xX];
//fragment Y : [yY];
//fragment Z : [zZ];
