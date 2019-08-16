/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.view.paged.PagedCorpusView;

/**
 * Collection of commonly used options available for corpora.
 *
 * @author Markus Gärtner
 *
 */
public class CorpusOption {


	/**
	 * The prefix common to all corpus-related options defied in this class.
	 */
	public static final String PARAM_PREFIX = "icarus2.model.api.corpus.";

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
	 * Implementations are encouraged to use this value if client code did
	 * not explicitly specify one.
	 *
	 * @see #PARAM_VIEW_PAGE_SIZE
	 */
	public static final int DEFAULT_VIEW_PAGE_SIZE = 1000;

	/**
	 * Key for signaling that {@link AnnotationLayer} instances in a corpus
	 * are encouraged to use storage implementations optimized for memory
	 * efficiency.
	 * <p>
	 * The type of this property is {@code boolean}.
	 */
	public static final String PARAM_ANNOTATION_PACKAGING = PARAM_PREFIX+"annotationPackaging";
}
