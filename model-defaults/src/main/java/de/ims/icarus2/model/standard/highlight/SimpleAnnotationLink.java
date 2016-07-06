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

import static de.ims.icarus2.util.Conditions.checkNotNull;
import de.ims.icarus2.model.api.highlight.HighlightInfo.AnnotationLink;
import de.ims.icarus2.model.api.layer.AnnotationLayer;

/**
 * Implements an immutable {@link AnnotationLink} that maintains
 * a strong reference to the respective {@link AnnotationLayer}.
 *
 * @author Markus Gärtner
 *
 */
public class SimpleAnnotationLink implements AnnotationLink {

	private final AnnotationLayer layer;
	private final String key;

	public SimpleAnnotationLink(AnnotationLayer layer, String key) {
		checkNotNull(layer);
		checkNotNull(key);

		this.layer = layer;
		this.key = key;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} else if(obj instanceof AnnotationLink) {
			AnnotationLink other = (AnnotationLink) obj;
			return getLayer()==other.getLayer() && getKey().equals(other.getKey());
		}

		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return getLayer().hashCode()*getKey().hashCode();
	}

	/**
	 * @see de.ims.icarus2.model.api.highlight.HighlightInfo.AnnotationLink#getLayer()
	 */
	@Override
	public AnnotationLayer getLayer() {
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
