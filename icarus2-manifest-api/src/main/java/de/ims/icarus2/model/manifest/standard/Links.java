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
package de.ims.icarus2.model.manifest.standard;

import static java.util.Objects.requireNonNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import de.ims.icarus2.model.manifest.util.ManifestUtils;


/**
 * Collection of implementations for lazy linking of foreign objects.
 *
 * @author Markus Gärtner
 *
 */
public class Links {

	/**
	 * Implements a lazy link that stores the referent after resolution
	 * in a {@link WeakReference}.
	 * <p>
	 * The target is allowed to be {@code null}.
	 *
	 * @author Markus Gärtner
	 *
	 * @param <O> the type of object referenced by the link
	 */
	public static abstract class Link<O extends Object> {

		private final String id;

		transient Reference<O> target;

		public Link(String id) {
			requireNonNull(id);

			ManifestUtils.checkId(id);

			this.id = id;
		}

		/**
		 * Resolves the actual target of this link.
		 * Note that the link will repeatedly try to resolve the target
		 * in case it is still {@code null}.
		 *
		 * @return
		 */
		protected abstract O resolve();

		public String getId() {
			return id;
		}

		protected Reference<O> wrap(O target) {
			return new WeakReference<>(target);
		}

		public O get() {
			if(target==null || target.get()==null) {
				target = wrap(resolve());
			}

			return target.get();
		}
	}

	/**
	 * Implements a link that remembers having tried to resolve
	 * the target and will never try to do it again after the
	 * first attempt.
	 *
	 * @author Markus Gärtner
	 *
	 * @param <O> the type of object referenced by the link
	 */
	public static abstract class MemoryLink<O extends Object> extends Link<O> {

		private volatile transient boolean resolved = false;

		public MemoryLink(String id) {
			super(id);
		}

		@Override
		public O get() {
			if(!resolved && (target==null || target.get()==null)) {
				target = wrap(resolve());
				resolved = true;
			}

			return target.get();
		}
	}
}
