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

import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.filedriver.Converter;
import de.ims.icarus2.filedriver.Converter.ReadMode;
import de.ims.icarus2.filedriver.schema.Schema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema;
import de.ims.icarus2.filedriver.schema.tabular.TableSchema.SubstituteType;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.util.Options;

/**
 * @author Markus Gärtner
 *
 */
public interface Resolver {

	//TODO later define methods for the reverse process to actually write into a format!

	/**
	 *
	 * @param converter the {@link Converter} instance this resolver will be associated with
	 * @param options implementation specific options that have been parsed directly from the underlying {@link Schema}
	 *
	 */
	default void prepareForReading(Converter converter, ReadMode mode, ResolverContext context, Options options) {
		// no-op
	}

	/**
	 * Processes the given raw character information and associates it with
	 * the provided {@link Item}.
	 * <p>
	 * A resolver can provide candidates for {@link TableSchema.SubstituteSchema substitutes}
	 * during a conversion process. If a {@link TableSchema.ColumnSchema column} in a schema
	 * declares substitutes in either {@link SubstituteType#ADDITION} or {@link SubstituteType#REPLACEMENT}
	 * then it is usually the resolver's responsibility to provide the actual items to be used.
	 * If no substitution takes place during conversion, the return values of this method are
	 * ignored.
	 */
	Item process(ResolverContext context) throws IcarusApiException;

	/**
	 * Optional method for subclasses to release internal resources.
	 * <p>
	 * Called exactly once during a resolver's life cycle.
	 */
	default void close() {
		// no-op
	}
}
