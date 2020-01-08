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
grammar IQL_Test;

import IQL;

/**
 * Parser Rules
 * 
 * Existing rules from IQL are extended for testing purposes to force matching 
 * of the entire input sequence so we can properly test them in isolation.
 */

versionDeclarationTest : versionDeclaration EOF ;

quantifierTest : quantifier EOF ;

unsignedSimpleQuantifierTest : unsignedSimpleQuantifier EOF ;

integerLiteralTest : integerLiteral EOF ;
unsignedIntegerLiteralTest : unsignedIntegerLiteral EOF ;

floatingPointLiteralTest : floatingPointLiteral EOF ; 
unsignedFloatingPointLiteralTest : unsignedFloatingPointLiteral EOF ;