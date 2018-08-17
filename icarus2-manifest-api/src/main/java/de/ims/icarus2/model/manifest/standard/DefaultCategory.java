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

import de.ims.icarus2.model.manifest.api.Category;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultCategory extends DefaultModifiableIdentity implements Category {

	private String namespace;

	/**
	 * @see de.ims.icarus2.model.manifest.api.Category#getNamespace()
	 */
	@Override
	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		checkNotLocked();

		setNamespace0(namespace);
	}

	protected void setNamespace0(String namespace) {
		requireNonNull(namespace);
		this.namespace = namespace;
	}

}
