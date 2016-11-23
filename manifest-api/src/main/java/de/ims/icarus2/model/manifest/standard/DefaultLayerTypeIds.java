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
 *
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
