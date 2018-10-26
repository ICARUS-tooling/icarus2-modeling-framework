/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.util.eval.jdk;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import de.ims.icarus2.util.eval.ExpressionFactory;
import de.ims.icarus2.util.eval.spi.ExpressionFactoryProvider;

/**
 * @author Markus Gärtner
 *
 */
public class JDKExpressionFactoryProvider extends ExpressionFactoryProvider {

	public static final String PROVIDER_NAME = "java.jdk";

	/**
	 * @see de.ims.icarus2.util.eval.spi.ExpressionFactoryProvider#getName()
	 */
	@Override
	public String getName() {
		return PROVIDER_NAME;
	}

	/**
	 * This implementation relies on a {@link JavaCompiler} being available under
	 * the current JVM. This is checked by {@link ToolProvider#getSystemJavaCompiler()}
	 * returning a {@code non-null} value.
	 *
	 * @see de.ims.icarus2.util.eval.spi.ExpressionFactoryProvider#isSupported()
	 */
	@Override
	public boolean isSupported() {
		return ToolProvider.getSystemJavaCompiler()!=null;
	}

	/**
	 * @see de.ims.icarus2.util.eval.spi.ExpressionFactoryProvider#newFactory()
	 */
	@Override
	public ExpressionFactory newFactory() {
		return new JDKExpressionFactory();
	}

}
