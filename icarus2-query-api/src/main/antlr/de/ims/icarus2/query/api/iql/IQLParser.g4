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
parser grammar IQLParser;

options {
    tokenVocab=IQLLexer;
}

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
	: pathBegin arrayRange? (DOT pathElement)*
	;
	
pathBegin
	: variable | anyName
	;

pathElement
	: name arrayRange?
	;
	
arrayRange
	: LBRACK (arrayIndex | integerFunction ) RBRACK
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
	: (NOT | EXMARK ) constraint # negation
	| predicate # logicalConstraint
	| groupAssignment # groupedConstraint
	| loopConstraint # forEachConstraint
	| LPAREN constraint RPAREN # wrappedConstraint
	| left=constraint OR right=constraint # logicalOr
	| left=constraint AND right=constraint # logicalAnd
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
	: BooleanLiteral
	| booleanFunction
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
	| GT_EQ # greaterOrEqual
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
	: GROUPING UnsignedIntegerLiteral
	| GROUP UnsignedIntegerLiteral
	;	
	
expression
	: valueSource
	| LPAREN expression RPAREN
	| expression expressionOperator expression
	;
	
expressionOperator
	: PLUS
	| MINUS
	| MOD
	| SLASH
	| PERCENT
	| STAR
	| AMP
	| CARET
	;
	
literal
	: Sign? UnsignedIntegerLiteral
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
	: Sign? UnsignedIntegerLiteral Sign?
	| Sign? UnsignedIntegerLiteral DOTDOT Sign? UnsignedIntegerLiteral
	;
	
versionDeclaration
	: major=UnsignedIntegerLiteral (DOT minor=UnsignedIntegerLiteral)? (DOT build=UnsignedIntegerLiteral)? suffix=Identifier? 
	;
