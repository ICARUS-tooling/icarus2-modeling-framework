/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.layer;

import static java.util.Objects.requireNonNull;

/**
 * @author Markus Gärtner
 *
 */
public final class Dependency<E extends Object> {

	private final E target;
	private final DependencyType type;

	public Dependency(E target, DependencyType type) {
		requireNonNull(target);
		requireNonNull(type);

		this.target = target;
		this.type = type;
	}

	public E getTarget() {
		return target;
	}

	public DependencyType getType() {
		return type;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return target.hashCode()*type.hashCode();
	}

	/**
	 * Since a {@code Dependency} instance can only be created for live
	 * objects of a corpus, this method performs identity checks on both the
	 * {@code target} and {@code type} between this dependency and the
	 * given {@code obj} parameter (only in case the parameter actually is
	 * another {@code Dependency}).
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this==obj) {
			return true;
		} if(obj instanceof Dependency) {
			Dependency<?> other = (Dependency<?>) obj;
			return target==other.target && type==other.type;
		}

		return false;
	}
}
