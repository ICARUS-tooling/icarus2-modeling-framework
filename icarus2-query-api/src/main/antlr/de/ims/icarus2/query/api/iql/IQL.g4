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

//TODO wanna label it Query Programming 

//options {
//    tokenVocab=IQLLexer;
//}

@header {
package de.ims.icarus2.query.api.iql;
}

@lexer::members {
	public static final int WHITESPACE = 1;
	public static final int COMMENTS = 2;
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

standaloneExpression : expression EOF ;

standaloneVersionDeclaration : versionDeclaration EOF ;


/** Basic Rules */

query
	: preamble? statement EOF
	;
	
preamble
	: (DIALECT version=versionDeclaration)? importStatement+ setupStatement? // here goes configuration stuff, namespace declarations, etc...
	;
	
/** Import external extensions or script definitions, optionally renaming their namespace */
importStatement
	: IMPORT StringLiteral (AS Identifier)?
	;
	
/** Allows to do a general setup of query parameters */
setupStatement
	: SETUP property (COMMA property)*
	;
	
property
	: key=qualifiedIdentifier # flagProperty
	| key=qualifiedIdentifier ASSIGN value=integerLiteral # intProperty
	| key=qualifiedIdentifier ASSIGN value=floatingPointLiteral # floatProperty
	| key=qualifiedIdentifier ASSIGN value=booleanLiteral # booleanProperty
	| key=qualifiedIdentifier ASSIGN value=StringLiteral # stringProperty
	;

/** Core of every query: specify what to extract */
statement
 	: sourceStatement selectStatement constraintStatement
	//TODO add result statement
	//TODO add payload section to embed encoded binary data
 	;
 	
//TODO add hex literals for encoded data
 	
sourceStatement
	: FROM corpusSelector (COMMA corpusSelector)*
	;
	
//TODO needs support for namespaces since we can't assume all corpus ids to be unique
corpusSelector
	: id=qualifiedIdentifier AS renamed=name? # corpusIdReference
	| id=StringLiteral AS renamed=name # corpusNameReference
	;

selectStatement
	: SELECT layerSelector (COMMA layerSelector)*
	;
	
layerSelector 
	: id=qualifiedIdentifier (AS PRIMARY? layerAlias=name | AS PRIMARY? SCOPE scopeAlias=Identifier LPAREN scopeElementList RPAREN)?
	;

scopeElementList
	: qualifiedIdentifier STAR? (COMMA qualifiedIdentifier STAR?)*
	;
	
/** Addressing a layer/context or other embedded member via the identifiers of its environment */
qualifiedIdentifier
	: Identifier (DOUBLE_COLON Identifier)*
	;
	
constraintStatement
	: //TODO different types of query bodies: nodes, edges, alignments, embedded foreign QL, ...
	//TODO special markers for elements: DISTINCT to force (quantified) elements to be unique matches (e.g. when matching subgraphs)
	;
	
constraint
	: source=expression # predicate // must evaluate to boolean or equivalent
	| source=expression (GROUPING | GROUP) group=PureDigits (LABEL label=StringLiteral)? (DEFAULT defaultValue=StringLiteral)? # groupAssignment
	| FOREACH source=expression AS renamed=variableName loopControl counters=counterList? # loopConstraint
	;
	
loopControl
	: (EVEN | ODD)? (OMIT omit=constraint)? (RANGE range=boundedRange)? (STEP step=boundedRange)? (DO body=constraint)? END
	;
	
boundedRange
	: LPAREN from=expression COMMA to=expression RPAREN
	;
	
counterList
	: counter (AND counter)*
	;
	
counter
	: COUNT constraint AS variableName
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
	// function calls can only occur after direct references
	| expression {isAny(-1,Identifier)}? LPAREN expressionList? RPAREN 				# methodInvocation
	// array indices can only occur after direct references, function calls or annotations
	| expression {isAny(-1,Identifier,RPAREN,RBRACE,RBRACK)}? LBRACK expressionList RBRACK		# arrayAccess
	// annotation can only occur after direct references, function calls or annotations
	| expression {isAny(-1,Identifier,RPAREN,RBRACE,RBRACK)}? LBRACE expression RBRACE	# annotationAccess
	| LPAREN type RPAREN expression													# castExpression
	| LPAREN expression RPAREN 														# wrappingExpression
	| source=expression (NOT | EXMARK)? IN (STAR | ALL)? LBRACE set=expressionList RBRACE 	# setPredicate
	| (NOT | EXMARK | MINUS) expression 											# unaryOp
	| left=expression (STAR | SLASH | PERCENT) right=expression 					# multiplicativeOp
	| left=expression (PLUS | MINUS) right=expression 								# additiveOp
	| left=expression (SHIFT_LEFT | SHIFT_RIGHT | PIPE | AMP | CARET) right=expression 		# bitwiseOp
	| left=expression (LT | LT_EQ | GT | GT_EQ) right=expression 					# comparisonOp
	| left=expression (TILDE | NOT_MATCHES | HASH | NOT_CONTAINS) right=expression 	# stringOp
	| left=expression (EQ | NOT_EQ) right=expression 								# equalityCheck
	| left=expression (DOUBLE_AMP | AND) right=expression 							# conjunction
	| left=expression (DOUBLE_PIPE | OR) right=expression 							# disjunction
	| condition=expression QMARK optionTrue=expression COLON optionFalse=expression # ternaryOp
	;
	
primary
	: booleanLiteral
	| floatingPointLiteral
	| integerLiteral
	| StringLiteral
	| variableName
	| name	
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
	| unsignedSimpleQuantifier ( PIPE unsignedSimpleQuantifier )* 
	| LT unsignedSimpleQuantifier ( PIPE unsignedSimpleQuantifier )* GT
	;
	
unsignedSimpleQuantifier
	: PureDigits sign?
	| PureDigits DOUBLE_DOT PureDigits
	;
	
all 
	: STAR 
	| ALL
	;
	
variableName
	: AT name
	;
	
name
	: DOLLAR? Identifier
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
ARROW : '=>';
DOUBLE_COLON : '::';
DOUBLE_DOT : '..';
UNDERSCORE : '_';
//X : 'x' ;
GROUPING : '<*>';

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
WS : [ \t\r\n\u000C\u000B]+ -> skip;

SL_COMMENT : '//' .*? '\n' -> skip;

ErrorCharacter : . ;
