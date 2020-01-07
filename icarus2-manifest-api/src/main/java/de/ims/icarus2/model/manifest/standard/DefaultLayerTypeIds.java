/**
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
package de.ims.icarus2.model.manifest.standard;

/**
 * Defines constants to identify some common layer types.
 *
 * @author Markus Gärtner
 *
 */
public interface DefaultLayerTypeIds {

	public static final String ITEM_TOKENS = "item:tokens"; //$NON-NLS-1$
	public static final String ITEM_SENTENCES = "item:sentences"; //$NON-NLS-1$
	public static final String ITEM_DOCUMENTS = "item:documents"; //$NON-NLS-1$
	public static final String ITEM_PARAGRAPHS = "item:paragraphs"; //$NON-NLS-1$
	public static final String ITEM_CHAPTERS = "item:chapters"; //$NON-NLS-1$
	public static final String ITEM_COREFERENCE_CLUSTER = "item:coreference-cluster"; //$NON-NLS-1$
	public static final String ITEM_MENTIONS = "item:mentions"; //$NON-NLS-1$
	public static final String ITEM_PHRASES = "item:phrases"; //$NON-NLS-1$
	public static final String ITEM_LAYER_OVERLAY = "item:layer-overlay"; //$NON-NLS-1$

	public static final String STRUCT_DEPENDENCY = "struct:dependency"; //$NON-NLS-1$
	public static final String STRUCT_CONSTITUENT = "struct:constituent"; //$NON-NLS-1$
	public static final String STRUCT_LFG = "struct:lfg"; //$NON-NLS-1$
	public static final String STRUCT_COREFERENCE = "struct:coreference"; //$NON-NLS-1$
	public static final String STRUCT_INFORMATION_STATUS = "struct:information-status"; //$NON-NLS-1$

	public static final String ANNO_FORM = "anno:form"; //$NON-NLS-1$
	public static final String ANNO_POS = "anno:part-of-speech"; //$NON-NLS-1$
	public static final String ANNO_LEMMA = "anno:lemma"; //$NON-NLS-1$
	public static final String ANNO_ROLE = "anno:role"; //$NON-NLS-1$
	public static final String ANNO_RELATION = "anno:relation"; //$NON-NLS-1$
	public static final String ANNO_FEATURES = "anno:features"; //$NON-NLS-1$
	public static final String ANNO_PROPERTIES = "anno:properties"; //$NON-NLS-1$
	public static final String ANNO_HEAD = "anno:head"; //$NON-NLS-1$
	public static final String ANNO_SPEAKER = "anno:speaker"; //$NON-NLS-1$
}
