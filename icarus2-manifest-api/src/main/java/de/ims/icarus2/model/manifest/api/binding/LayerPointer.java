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
package de.ims.icarus2.model.manifest.api.binding;

import de.ims.icarus2.util.lang.ClassUtils;

/**
 * @author Markus Gärtner
 *
 */
public interface LayerPointer {

	String getContextId();

	String getLayerId();

	public static boolean defaultEquals(LayerPointer p1, LayerPointer p2) {
		return ClassUtils.equals(p1.getContextId(), p2.getContextId())
				&& ClassUtils.equals(p1.getLayerId(), p2.getLayerId());

	}
}
