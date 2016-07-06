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

import static de.ims.icarus2.util.Conditions.checkNotNull;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;


/**
 * Collection implementations for lazy linking of foreign objects.
 *
 * @author Markus Gärtner
 *
 */
public class Links {

	/**
	 * Implements a lazy link that stores the referent after resolution
	 * in a {@link WeakReference}.
	 * <p>
	 * The target is allowed
	 *
	 * @author Markus Gärtner
	 *
	 * @param <O> the type of object referenced by the link
	 */
	public static abstract class Link<O extends Object> {

		private final String id;

		transient Reference<O> target;

		public Link(String id) {
			checkNotNull(id);

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
