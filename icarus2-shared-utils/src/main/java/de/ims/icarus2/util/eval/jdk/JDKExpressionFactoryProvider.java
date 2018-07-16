/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
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
