# ICARUS 2 Query Language Specification

## 1. Introduction

Queries in IQL are designed to be self-contained with logical sections for specifying all the information required to determine the target of a query and its granularity, resolve additional dependencies such as extensions or scripts, link and validate constraints to parts of the target corpus or corpora and finally optional pre- and post-processing steps. To achieve this complex task IQL uses a keyword-based syntax to drive declaration of all the aforementioned information. As a side effect queries can become quite verbose and potentially cumbersome to define manually. As a countermeasure the overall structure of a query is composed of blocks that can be glued together incrementally and that make it very easy for an application built on top of it to provision boilerplate query code based on settings or a GUI so that the user only needs to type the actual constraints used in the query. This document lists the basic building blocks of queries and their compositions.

## 2. Reserved Words

The following list of keywords is reserved and any of the words may not be used as identifier strings in a query (they are reserved in both all lowercase and all uppercase):

```
NULL
TRUE
FALSE
FROM
SETUP
SELECT
AS
SCOPE
NODES
EDGES
OR
AND
ELEMENTS
HAVING
LOCAL
RANGE
LIMIT
ALL
NOT
IMPORT
LABEL
DEFAULT
GROUP
IN
FOREACH
EVEN
ODD
OMIT
STEP
DO
END
COUNT
DIALECT
PRIMARY
```

In addition the following strictly lowercase words are reserved as type identifiers and may not be used otherwise:

```
int
long
float
double
string
boolean
```

## 3. Comments

IQL supports single-line comments, indicated by ``//``. All remaining content in a line after the comment indicator will be ignored when parsing and evaluating a query.

## 4. Literals

### 4.1. String Literals

IQL uses double quotes to define string literals. String literals may not contain any of the following symbols directly:

```
\n line break
\r carriage return
\f form feed
\b backspace
\t tab
\ backslash
" nested quotation mark
```

Any of those symbols listed above can be embedded into a string literal as part of an escape sequence with a preceding backslash.

Examples for valid string literals:

```
"string"
"123"
"some fancy number (123.456e-789) and emoji 👍"
"a more complex string!"
"a\n multiline\n string..."
```

### 4.2. Boolean Literals

Boolean literals are limited to either all lowercase or all uppercase versions of the literals ``true`` and ``false``.

### 4.3. Integer Literals

**Signed Integer Literals**
Literals representing regular (32bit) or long (64bit) integers consist of an optional initial sign (``+`` or ``-``) and the body consisting of digits or underscore (``_``) characters.
Underscore characters may only appear inside the integer literal, never at the beginning or end (not counting the sign symbol).

Examples for valid (signed) integer literals:

```
1
+123
-123
1_000_000
-99_000000_0
```

**Pure Integer Literals**
Some parts of the IQL syntax only allow unsigned "pure" integers and will explicitly state this fact. In those special cases integer literals may neither contain the initial sign symbol nor intermediate underscores.

### 4.4. Floating Point Literals

Floating point literals are constructed by using a (signed) integer literal for the pre-decimal part, a dot '``.``' as delimiter and a decimal part made up by a unsigned integer literal. They represent either single-precision ``float`` (32bit) or double-precision ``long`` (64bit) values.

Examples for valid (signed) floating point literals:

```
1.0
+123.456
-123.456
1_000_000.999
-99_000000_0.000_000_001
```

While many languages offer to express floating point literals in the scientific notation with explicit exponent declaration, we do not include this in the initial draft of IQL.

## 5. Identifiers

Identifiers in IQL are combinations of lowercase or uppercase alphabetic ``[a-zA-Z]`` characters that may contain underscore symbols ``_`` between the first and last position and may also contain digits ``[0-9]`` on any position except as initial symbol.

Examples for valid identifiers:

```
x
myIdentifier
x1
x321
some_random_id
random_2_4
notTheBest______________example
```

Identifiers are limited in length by the engine to a total of 255 characters. 

## 6. Variables and References

In IQL all top-level (i.e. not part of the tail expression in a hierarchical path) identifiers are expected to reference 'something' from the global namespace available to the query. This namespae is populated with all the globally available constants, methods and helper objects from the IQL core and any imported extensions, as well as all the corpus members defined in the scoping part of the query. Outside this global namespace any dynamically created identifiers from within a query reside in the variable namespace and are marked with a preceding ``@`` (e.g. ``@myVariable``). They can be used the same way as any regular identifier, with the exception of allowing assignment expressions when inside script blocks.

## 7. Expressions

Expressions are the foundation of every query and can take any of the following forms:

### 7.1. Primary Expressions

Any literal (boolean, string, integer or floating point) can serve as a primary expression.

### 7.2. Path Expressions

For navigating hierarchically structured object graphs or namespaces, expressions can take the form of paths:

```
<expression> '.' <identifier>
```

Examples:

```
someObejct.someProperty
some.really.long.winded.path
```

### 7.3. Method Invocation

Method invocations consist of an expression that points to the actual method (such as an identifier in the global namespace) and round brackets for the invocation with an optional argument list:

```
<expression> '(' <expression>? (',' <expression>)* ')'
```

Examples:

```
myFunction()
myNamespace.someFunction(someArgument, anotherArgument)
min(123, 456, dynamicContent())
```

### 7.4. Array Access

Arrays are accessed by an expression pointing to the array itself and an index expression in square brackets indicating the position of the desired element within the array. Note that the index expression must evaluate to an integer value within ``int`` space. Positive values indicate the position beginning from the start of the array (with ``0`` being the first position), whereas negative values allow backwards referencing of elements with ``-1`` pointing to the last array element and ``-2`` to the second to last one. For multidimensional arrays several index statements can be chained or even combined in a single comma-separated list.

```
<expression> '[' <expression> (',' <expression>)* ']'
```

Examples:

```
myArray[1]
complexArray[1][2][3]
complexArray[1, 2][3]
complexArray[1, 2, 3]
```

### 7.5. Annotation Access

### 7.6. Cast

Expressions in IQL are automatically cast to matching types according to the actual consumer's needs. Explicit casts can be performed by preceding an expression with one of the type keywords listed above in round brackets.

Examples:

```
(int) myValue
(long) 12345.678
(float) average(myVector)
```

### 7.7. Wrapping

Expression hierarchy and evaluation order follows the order the different types of expressions are listed here. To dictate another order, expressions can be wrapped into round brackets. This will cause the inner expression to be evaluated independent of potential hierarchical rules from outside.

Examples:

```
6 + 4 * 2   // multiplication is evaluated first -> result 14
(6 + 4) * 2 // addition is forced to be evaluated first -> result 20
```

### 7.8. Set Predicate

Also called 'containment predicate', this expression allows to check if a given value is a member of a specified set (or generally speaking 'collection'). The basic form of a set predicate looks as follows:

```
<expression> 'IN' '{' <expression> (',' <expression>)* '}' 
```

The entire expression evaluates to a boolean value and will be ``true`` iff the input expression (left-most one) evaluates to the same value as any of the expressions inside the curly brackets. See the section about equality operators in 7.10.

### 7.9. Unary Operation

### 7.10. Binary Operation

### 7.11. Ternary Operation

### 7.12. Value Expansion

IQL supports expansion of arrays, lists and array-like method return values for situations where an immediate consumer supports lists of values as input. Assuming the method ``randomPoint()`` returns an array of 3 integer values and another method ``invertPoint(int, int, int)`` takes 3 integer arguments, then the invocation of ``invertPoint(randomPoint())`` is legal and the array from the inner method call will be automatically expanded into the separate 3 values. This is especially handy when dealing with multidimensional arrays, as regular indexing would require manual extraction of method return values into variables to then be used in accessing the different array dimensions. With automatic expansion, a three-dimensional array could directly be accessed with aforementioned method via ``array[randomPoint()]``.


## 8. Switches

For increased flexibility, IQL supports a collection of switches to turn certain optional features on when needed. Switches are part of a query's preamble and cannot be changed after their initial declaration. 

## 9. Constraints

Simply put, constraints are expressions that evaluate to a boolean result. Apart from native boolean expressions (such as comparisons, boolean literals or boolean functions), IQL allows the following evaluations as syntactic sugar:

- any ``string`` object evaluate to ``false`` when they are empty or null
- any ``int`` or ``long`` value evaluates to ``false`` when it is ``0``
- 
- any object evaluates to ``false`` if it is null