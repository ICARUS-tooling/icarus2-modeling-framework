/*
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

import java.util.Collection;
import java.util.Optional;

import de.ims.icarus2.model.manifest.api.Lockable;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractLockable implements Lockable {

	private transient boolean locked = false;

	/**
	 * @see de.ims.icarus2.model.manifest.api.ManifestFragment#lock()
	 */
	@Override
	public final void lock() {
		locked = true;

		lockNested();
	}

	protected void lockNested() {
		// no-op
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ManifestFragment#isLocked()
	 */
	@Override
	public final boolean isLocked() {
		return locked || isNestedLocked();
	}

	protected boolean isNestedLocked() {
		return false;
	}

	protected final void lockNested(Lockable...items) {
		for(Lockable item : items) {
			if(item!=null) {
				item.lock();
			}
		}
	}

	@SafeVarargs
	protected final void lockNested(Optional<? extends Lockable>...items) {
		for(Optional<? extends Lockable> item : items) {
			if(item.isPresent()) {
				item.get().lock();
			}
		}
	}

	protected final void lockNested(Collection<? extends Lockable> items) {
		for(Lockable item : items) {
			if(item!=null) {
				item.lock();
			}
		}
	}
}
