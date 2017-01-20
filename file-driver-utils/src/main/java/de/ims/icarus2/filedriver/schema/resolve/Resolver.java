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
package de.ims.icarus2.filedriver.schema.resolve;

import java.util.function.Function;

import de.ims.icarus2.filedriver.Converter;
import de.ims.icarus2.filedriver.Converter.ReadMode;
import de.ims.icarus2.filedriver.schema.Schema;
import de.ims.icarus2.filedriver.schema.table.TableSchema;
import de.ims.icarus2.filedriver.schema.table.TableSchema.SubstituteType;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache;
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
	 * @param componentSuppliers a map-like lookup mechanism to fetch caches associated with
	 * @param options implementation specific options that have been parsed directly from the underlying {@link Schema}
	 *
	 */
	default void prepareForReading(Converter converter, ReadMode mode, Function<ItemLayer, InputCache> caches, Options options) {
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
	Item process(ResolverContext context);

	/**
	 * Optional method for subclasses to release internal resources.
	 */
	default void close() {
		// no-op
	}
}
