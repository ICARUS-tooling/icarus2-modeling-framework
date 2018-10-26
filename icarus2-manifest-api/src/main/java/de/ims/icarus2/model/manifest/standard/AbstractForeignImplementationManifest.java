/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import java.util.Optional;
import java.util.function.Function;

import de.ims.icarus2.model.manifest.api.ForeignImplementationManifest;
import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.manifest.api.TypedManifest;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractForeignImplementationManifest<M extends MemberManifest & ForeignImplementationManifest, H extends TypedManifest>
	extends AbstractMemberManifest<M, H> implements ForeignImplementationManifest {

	private Optional<ImplementationManifest> implementationManifest = Optional.empty();

	/**
	 * {@inheritDoc}
	 */
	protected AbstractForeignImplementationManifest(
			ManifestLocation manifestLocation, ManifestRegistry registry) {
		super(manifestLocation, registry);
	}

	/**
	 * {@inheritDoc}
	 */
	protected AbstractForeignImplementationManifest(ManifestLocation manifestLocation, ManifestRegistry registry,
			H host, Class<? extends H> expectedHostClass) {
		super(manifestLocation, registry, host, expectedHostClass);
	}

	/**
	 * {@inheritDoc}
	 */
	protected AbstractForeignImplementationManifest(H host,
			Function<H, Manifest> properRegistrySource,
			Class<? extends H> expectedHostClass) {
		super(host, properRegistrySource, expectedHostClass);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.standard.AbstractManifest#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return super.isEmpty() && !implementationManifest.isPresent();
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ForeignImplementationManifest#isLocalImplementation()
	 */
	@Override
	public boolean isLocalImplementation() {
		return implementationManifest.isPresent();
	}

	/**
	 * @return the implementationManifest
	 */
	@Override
	public Optional<ImplementationManifest> getImplementationManifest() {
		return getDerivable(implementationManifest, t -> t.getImplementationManifest());
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
		this.implementationManifest = Optional.of(implementationManifest);
	}

	public void clearImplementationManifest() {
		implementationManifest = Optional.empty();
	}

	@Override
	protected void lockNested() {
		super.lockNested();

		lockNested(implementationManifest);
	}
}
