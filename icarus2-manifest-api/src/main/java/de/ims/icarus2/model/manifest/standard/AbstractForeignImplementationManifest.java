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

import de.ims.icarus2.model.manifest.api.ForeignImplementationManifest;
import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.MemberManifest;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractForeignImplementationManifest<M extends MemberManifest> extends AbstractMemberManifest<M> implements ForeignImplementationManifest {

	private ImplementationManifest implementationManifest;

	/**
	 * @param manifestLocation
	 * @param registry
	 */
	protected AbstractForeignImplementationManifest(
			ManifestLocation manifestLocation, ManifestRegistry registry) {
		super(manifestLocation, registry);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && implementationManifest==null;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ForeignImplementationManifest#isLocalImplementation()
	 */
	@Override
	public boolean isLocalImplementation() {
		return implementationManifest!=null;
	}

	/**
	 * @return the implementationManifest
	 */
	@Override
	public ImplementationManifest getImplementationManifest() {
		return implementationManifest;
	}

	/**
	 * @param implementationManifest the implementationManifest to set
	 */
	@Override
	public void setImplementationManifest(
			ImplementationManifest implementationManifest) {
		checkNotLocked();

		setImplementationManifest0(implementationManifest);
	}

	protected void setImplementationManifest0(
			ImplementationManifest implementationManifest) {
		requireNonNull(implementationManifest);

		this.implementationManifest = implementationManifest;
	}

	public void clearImplementationManifest() {
		implementationManifest = null;
	}

	@Override
	public void lock() {
		super.lock();

		if(implementationManifest!=null) {
			implementationManifest.lock();
		}
	}
}
