# ICARUS 2 Query Language Specification

## Introduction

Queries in IQL are designed to be self-contained with logical sections for specifying all the information required to determine the target of a query and its granularity, resolve additional dependencies such as extensions or scripts, link and validate constraints to parts of the target corpus or corpora and finally optional pre- and post-processing steps. To achieve this complex task IQL uses a keyword-based syntax to drive declaration of all the aforementioned information. As a side effect queries can become quite verbose and potentially cumbersome to define manually. As a countermeasure the overall structure of a query is composed of blocks that can be glued together incrementally and that make it very easy for an application built on top of it to provision boilerplate query code based on settings or a GUI so that the user only needs to type the actual constraints used in the query. This document lists the basic building blocks of queries

## Literals

### String Literals

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

### Boolean Literals

Boolean literals are limited to either all lowercase or all uppercase version of the literals ``true`` and ``false``.

### Integer Literals

**Signed Integer Literals**
Integer literals consist of an optional initial sign (``+`` or ``-``) and the body consisting of digits or underscore (``_``) characters.
Underscore characters may only appear inside the integer literal, never at the beginning or end (not counting the sign symbol).

Examples for valid signed integer literals:

```
1
+123
-123
1_000_000
-99_000000_0
```

**Pure Integer Literals**
Some parts of the IQL syntax only allow unsigned "pure" integers and will explicitly state this fact. In those special cases integer literals may neither contain the initial sign symbol nor intermediate underscores.

### Floating Point Literals

## Identifiers

## Expressions

## Constraints

## Reserved Words