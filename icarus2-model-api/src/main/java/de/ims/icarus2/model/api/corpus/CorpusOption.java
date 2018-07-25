/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@uni-stuttgart.de>
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
package de.ims.icarus2.model.api.corpus;

import de.ims.icarus2.model.api.view.paged.PagedCorpusView;

/**
 *
 * @author Markus Gärtner
 *
 */
public class CorpusOption {


	public static final String PARAM_PREFIX = "icarus2.model.api.";

	/**
	 * Key for fetching the {@code page size} to be used when creating a new {@link PagedCorpusView}.
	 * <p>
	 * The type of this property is {@code int}.
	 *
	 * @see #DEFAULT_VIEW_PAGE_SIZE
	 */
	public static final String PARAM_VIEW_PAGE_SIZE = PARAM_PREFIX+"viewPageSize";

	/**
	 * Default value for the page size of a new {@link PagedCorpusView}.
	 * Implementations are encouraged to use this value if client code did not specify another one.
	 *
	 * @see #PARAM_VIEW_PAGE_SIZE
	 */
	public static final int DEFAULT_VIEW_PAGE_SIZE = 1000;
}
