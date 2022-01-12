/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.members;


/**
 * A temporary utility object that can be used to query whether or not
 * certain actions are currently being possible for an associated object.
 * <p>
 * Note that client code should make sure to always {@link #close() close}
 * a verifier instance to make sure there are no dangling references to
 * the associated object.
 *
 * @author Markus Gärtner
 *
 */
public interface EditVerifier<E extends Object> extends AutoCloseable {

	E getSource();

	/**
	 * General hint on whether this verifier allows any kind of edits at all.
	 * <p>
	 * The default implementation always returns {@code true}.
	 *
	 * @return
	 */
	default boolean isAllowEdits() {
		return true;
	}

	@Override
	void close();
}
