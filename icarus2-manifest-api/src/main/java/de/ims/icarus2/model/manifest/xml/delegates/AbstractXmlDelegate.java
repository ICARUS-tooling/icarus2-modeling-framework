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
package de.ims.icarus2.model.manifest.xml.delegates;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import de.ims.icarus2.model.manifest.xml.ManifestXmlDelegate;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractXmlDelegate<M extends Object> implements ManifestXmlDelegate<M> {

	private M instance;

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegate#setInstance(java.lang.Object)
	 */
	@Override
	public void setInstance(M instance) {
		requireNonNull(instance, "Invalid instance value");
		checkState("Instance already set", this.instance==null);

		this.instance = instance;
	}

	/**
	 * @return the instance
	 */
	@Override
	public M getInstance() {
		checkState("Instance not set", instance!=null);

		return instance;
	}

	/**
	 * Resets the internal {@code instance} reference back to {@code null}.
	 * Any subclass that overrides this method <b>must</b> make sure to
	 * call {@code super.reset()} to ensure a proper reset!
	 *
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegate#reset()
	 */
	@Override
	public void reset() {
		instance = null;
	}
}
