/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.filedriver.schema.resolve;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;

/**
 * Factory for creating {@link Resolver} instances from {@code String} values.
 * This base implementations only supports input {@code Strings} that denote
 * fully qualified class names of a {@code Resolver} implementation.
 *
 * @author Markus Gärtner
 *
 */
public class ResolverFactory {

	public static ResolverFactory newInstance() {
		return new ResolverFactory();
	}

	public Resolver createResolver(String s) {
		try {
			return (Resolver) Class.forName(s).newInstance();
		} catch(ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new ModelException(GlobalErrorCode.INSTANTIATION_ERROR,
					"Failed to instantiate requested resolver: "+s, e);
		}
	}
}
