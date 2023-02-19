/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.exp.env;

import de.ims.icarus2.query.api.exp.Expressions;
import de.ims.icarus2.query.api.exp.TypeInfo;
import de.ims.icarus2.util.MutablePrimitives.Primitive;

/**
 * @author Markus Gärtner
 *
 */
public class SharedGlobalEnvironment extends AbstractEnvironment {

	private volatile static SharedGlobalEnvironment instance;

	public static SharedGlobalEnvironment getInstance() {
		SharedGlobalEnvironment result = instance;

		if (result == null) {
			synchronized (SharedGlobalEnvironment.class) {
				result = instance;

				if (result == null) {
					instance = new SharedGlobalEnvironment();
					result = instance;
				}
			}
		}

		return result;
	}

	private SharedGlobalEnvironment() {
		super(null, null);
	}

	@Override
	protected void createEntries() {
		EntryBuilder builder = entryBuilder();

		// Math utils

		builder.method("abs", TypeInfo.INTEGER, TypeInfo.INTEGER)
			.instantiator((e, ctx, t, args) -> Expressions.<Primitive<Long>>wrapInt(e,
					p -> Math.abs(p.longValue()), t, args))
			.commitAndReset();

		// TODO add more entries
	}

}
