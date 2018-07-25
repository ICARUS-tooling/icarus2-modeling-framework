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
package de.ims.icarus2.util.mem;

import java.util.Set;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

/**
 * @author Markus Gärtner
 *
 */
public class ObjectCache {

	private final Set<Object> cache = new ReferenceOpenHashSet<>();

	public synchronized boolean contains(Object object) {
		if (object == null)
			throw new NullPointerException("Invalid object"); //$NON-NLS-1$

		return cache.contains(object);
	}

	public synchronized boolean addIfAbsent(Object object) {
		return cache.add(object);
	}
}
