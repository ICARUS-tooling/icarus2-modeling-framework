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
package de.ims.icarus2.model.standard.highlight;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import java.lang.ref.WeakReference;

import de.ims.icarus2.model.api.highlight.HighlightInfo.AnnotationLink;
import de.ims.icarus2.model.api.layer.AnnotationLayer;

/**
 * @author Markus Gärtner
 *
 */
public class WeakAnnotationLink extends WeakReference<AnnotationLayer> implements AnnotationLink {

	private final String key;

	/**
	 * @param layer
	 */
	public WeakAnnotationLink(AnnotationLayer layer, String key) {
		super(layer);

		requireNonNull(key);

		this.key = key;
	}

	/**
	 * @throws IllegalStateException in case the {@link AnnotationLayer layer} has already been
	 * gc'd.
	 *
	 * @see de.ims.icarus2.model.api.highlight.HighlightInfo.AnnotationLink#getLayer()
	 */
	@Override
	public AnnotationLayer getLayer() {
		AnnotationLayer layer = get();

		checkState("Layer invalid", layer!=null);

		return layer;
	}

	/**
	 * @see de.ims.icarus2.model.api.highlight.HighlightInfo.AnnotationLink#getKey()
	 */
	@Override
	public String getKey() {
		return key;
	}

}
