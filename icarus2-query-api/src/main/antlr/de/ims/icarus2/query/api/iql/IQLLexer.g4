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
lexer grammar IQLLexer;

@header {
	package de.ims.icarus2.query.api.iql;
}

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
	:	ExponentIndicator Sign? Digits
	;

fragment
ExponentIndicator
	:	[eE] 
	;
 
UnsignedIntegerLiteral
	:	Zero
	|	NonZeroDigit (Digits? | Underscores Digits)
	;
 
//IntegerLiteral
//	:	UnsignedIntegerLiteral
//	| 	SignedInteger
//	;

//fragment
//SignedInteger
//	:	Sign? Digits
//	;

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
