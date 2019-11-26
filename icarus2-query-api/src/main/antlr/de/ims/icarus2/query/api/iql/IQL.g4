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

/**
 * Parser Rules
 */

query
	: preambel? statement EOF
	;
	
preambel
	: (DIALECT version=versionDeclaration)? importStatement+ // here goes configuration stuff, namespace declarations, etc...
	;
	
/** Import external extensions or script definitions, optionally renaming their namespace */
importStatement
	: IMPORT StringLiteral (AS Identifier)?
	;

/** Core of every query: specify what to extract */
statement
 	: sourceStatement selectStatement;
 	
sourceStatement
	: FROM corpusSelector (COMMA corpusSelector)*
	;
	
//TODO needs support for namespaces since we can't assume all corpus ids to be unique
corpusSelector
	: Identifier identifierAssignment?
	| StringLiteral identifierAssignment
	;

selectStatement
	: SELECT layerSelector (COMMA layerSelector)*
	;
	
layerSelector 
	: qualifiedIdentifier (identifierAssignment | scopeDefinition)? //TODO AS PRIMARY assignment?
	;
	
scopeDefinition
	: AS SCOPE id=Identifier LPAREN qualifiedIdentifier (COMMA qualifiedIdentifier)* RPAREN
	;

/** Allows to reassign a previously resolved (qualified) identifier to a new (shorter?) identifier */
identifierAssignment
	: AS Identifier
	;
	
/** Addressing a layer/context or other embedded member via the identifiers of its environment */
qualifiedIdentifier
	: Identifier (DOUBLE_COLON Identifier)*
	;
	
variableName
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
	
constraint
	: (NOT | EXMARK) constraint # negation
	| expression # predicate
	| left=expression EQ all? right=expression # equals
	| left=expression LT all? right=expression # less
	| left=expression LT_EQ all? right=expression # lessOrEqual
	| left=expression GT STAR? right=expression # greater
	| left=expression GT_EQ all? right=expression # greaterOrEqual
	| left=expression NOT_EQ all? right=expression # equalsNot
	| left=expression TILDE all? right=expression # matches
	| left=expression NOT_MATCHES all? right=expression # matchesNot
	| left=expression HASH all? right=expression # contains
	| left=expression NOT_CONTAINS all? right=expression # containsNot
	| source=expression (NOT | EXMARK)? IN STAR? LBRACE expressionList RBRACE # setPredicate
	| source=expression (GROUPING | GROUP) group=PureDigits (LABEL label=StringLiteral)? (DEFAULT defaultValue=StringLiteral)? # groupAssignment
	| FOREACH source=expression AS variableName loopControl counterList? # loopConstraint
	| LPAREN source=constraint RPAREN # wrappedConstraint
	| left=constraint (AND | DOUBLE_AMP) right=constraint # logicalAnd
	| left=constraint (OR | DOUBLE_PIPE) right=constraint # logicalOr
	;
	
loopControl
	: ( EVEN | ODD )? ( OMIT omit=constraint )? ( RANGE range=boundedRange )? ( STEP step=boundedRange )? loopBody
	;

loopBody
	: DO constraint END
	| END
	;
	
boundedRange
	: LPAREN from=expression COMMA to=expression RPAREN
	;
	
counterList
	: counter ( AND counter )*
	;
	
counter
	: COUNT constraint AS variableName
	;
	
expressionList
	: expression (COMMA expression)*
	;
	
expression
	: MINUS expression # unaryMinus
	| PLUS expression # unaryPlus
	| source=expression LPAREN arguments=expressionList RPAREN # call
	| source=expression LBRACK index=expression RBRACK # array
	| source=expression LBRACE key=StringLiteral RBRACE # annotation
	| expression (DOT expression)+ # path
	| LPAREN source=expression RPAREN # wrapping
	| integerLiteral # intLiteral
	| floatingPointLiteral # floatLiteral
	| StringLiteral # stringLliteral
	| BooleanLiteral # booleanLiteral
	| variableName # variable
	| anyName # reference
	| left=expression PLUS right=expression # binaryAdd
	| left=expression MINUS right=expression # binaryMinus
	| left=expression (MOD | PERCENT) right=expression # binaryMod
	| left=expression SLASH right=expression # binaryDiv
	| left=expression STAR right=expression # binaryMult
	| left=expression AMP right=expression # binaryAnd
	| left=expression CARET right=expression # binaryPow
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
	
//signedSimpleQuantifier
//	: sign? PureDigits sign?
//	| sign? PureDigits DOUBLE_DOT sign? PureDigits
//	;
	
versionDeclaration
	: major=PureDigits (DOT minor=PureDigits)? (DOT build=PureDigits)? (MINUS | UNDERSCORE)? suffix=Identifier? 
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
 	: unsignedIntegerLiteral DOT decimalPart
 	;
 	
decimalPart
	:	unsignedIntegerLiteral exponentPart?
	|	exponentPart
	;

exponentPart
	:	Identifier sign? unsignedIntegerLiteral
	;
 
integerLiteral
 	: signedIntegerLiteral
 	;
 
signedIntegerLiteral
	: sign? unsignedIntegerLiteral
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
//DIV : 'DIV' | 'div' ;
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
SLASH : '/';
PERCENT : '%';
AMP : '&';
DOUBLE_AMP : '&&';
CARET : '^';
PIPE : '|';
DOUBLE_PIPE : '||';
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

//UnsignedFloatingPointLiteral
//	:	Digits '.' DecimalPart 
//	;
//
//FloatingPointLiteral
//	:	Sign? UnsignedFloatingPointLiteral
//	;
//
//fragment
//DecimalPart
//	:	Digits
//	|	Digits ExponentPart
//	|	ExponentPart
//	;
//
//fragment
//ExponentPart
//	:	ExponentIndicator Sign? Digits
//	;

ExponentPart
	:	[eE] [+-]? (Digits | PureDigits) 
	;
 
//UnsignedIntegerLiteral
//	:	Zero
//	|	NonZeroDigit (Digits? | Underscores Digits)
//	;
 
//IntegerLiteral
//	:	UnsignedIntegerLiteral
//	| 	SignedInteger
//	;

//fragment
//SignedInteger
//	:	Sign? Digits
//	;

//Sign
//	:	[+-]
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

SL_COMMENT : '//' .*? '\n' -> skip;

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

ErrorCharacter : . ;

