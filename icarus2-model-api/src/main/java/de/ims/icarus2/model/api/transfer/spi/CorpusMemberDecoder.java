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
package de.ims.icarus2.model.api.transfer.spi;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.Reader;
import java.util.function.IntSupplier;

import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.api.path.CorpusPath;
import de.ims.icarus2.model.api.registry.LayerLookup;

/**
 * @author Markus Gärtner
 *
 */
public abstract class CorpusMemberDecoder {

	protected final LayerLookup config;

	protected CorpusMemberDecoder(LayerLookup config) {
		requireNonNull(config);

		this.config = config;
	}

	public final LayerLookup config() {
		return config;
	}

	public abstract CorpusPath readPath(CharSequence s);


	public abstract CorpusPath readPath(Reader in) throws IOException;


	public abstract CorpusPath readPath(IntSupplier in);


	public abstract Item readItem(CharSequence s);


	public abstract Item readItem(Reader in) throws IOException;


	public abstract Item readItem(IntSupplier in);
}
