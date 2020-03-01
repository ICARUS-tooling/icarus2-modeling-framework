/*
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
/**
 *
 */
package de.ims.icarus2.query.api.eval.env;

import static de.ims.icarus2.query.api.eval.Expressions.wrapBool;

import de.ims.icarus2.query.api.eval.Expression;
import de.ims.icarus2.query.api.eval.Expressions;
import de.ims.icarus2.query.api.eval.TypeInfo;

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

		//TODO move those entries to an environment focused on java.lang.Object
		builder.method("toString", TypeInfo.TEXT)
			.noArgs()
			.instantiator((e, t, args) -> Expressions.<Object,String>wrapObj(e, Object::toString, t, args))
			.commitAndReset();

		builder.method("equals", TypeInfo.BOOLEAN)
			.argumentTypes(TypeInfo.GENERIC)
			.instantiator((e, t, args) -> {
				Expression<?> other = args[0];
				return wrapBool(e, obj -> obj.equals(other.compute()), t, args);
			})
			.commitAndReset();


		// TODO add more
	}

}
