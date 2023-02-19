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
package de.ims.icarus2.model.manifest.api;

import javax.swing.text.Highlighter.Highlight;

import de.ims.icarus2.util.Flag;
import de.ims.icarus2.util.LazyStore;
import de.ims.icarus2.util.strings.StringResource;

/**
 * @author Markus Gärtner
 *
 */
public enum HighlightFlag implements StringResource, Flag {

	/**
	 * Signals that a {@link Highlight} object may affect not only the top most
	 * items in a target container, but also deeply nested members.
	 */
	DEEP_HIGHLIGHTING("deep-highlighting"),

//	/**
//	 * Signals that cursors for a given {@link HighlightLayer} do not support
//	 * scrolling through concurrent highlight data and will throw an exception
//	 * for each of the scrolling related methods in the {@link HighlightCursor cursor} interface.
//	 */
//	CURSOR_NO_SCROLL("cursor-no-scroll"),
//
//	/**
//	 * Signals that
//	 */
//	CURSOR_FORWARD_ONLY("cursor-forward-only"),

	/**
	 * Signals that the user or other client code is allowed to make modifications to existing
	 * highlight information, remove it or add new data to it.
	 */
	EDITABLE("editable"),
	;

	private final String xmlForm;

	private HighlightFlag(String xmlForm) {
		this.xmlForm = xmlForm;
	}

	/**
	 * @see de.ims.icarus2.model.util.StringResource.XmlResource#getStringValue()
	 */
	@Override
	public String getStringValue() {
		return xmlForm;
	}

	private static LazyStore<HighlightFlag,String> store = LazyStore.forStringResource(
			HighlightFlag.class, true);

	public static HighlightFlag parseHighlightFlag(String s) {
		return store.lookup(s);
	}
}
