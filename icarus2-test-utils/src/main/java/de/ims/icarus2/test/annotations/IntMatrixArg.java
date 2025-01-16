/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.test.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.params.converter.ConvertWith;

import de.ims.icarus2.test.util.convert.ArrayConverters;

/**
 * Allows conversion of arbitrarily deep matrix structures into
 * multidimensional int arrays.
 * <p>
 * Format of the String representation to be parsed can be controlled
 * via an explicit {@link ArrayFormat} annotation. If such an annotation
 * is not present, the converter will use the default values defined in
 * {@link ArrayFormat}.
 *
 * @author Markus Gärtner
 *
 */
@Documented
@Target({ ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@ConvertWith(ArrayConverters.IntegerMatrixConverter.class)
public @interface IntMatrixArg {
	// marker annotation
}
