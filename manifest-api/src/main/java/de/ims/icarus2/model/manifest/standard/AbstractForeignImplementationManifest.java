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
		checkNotNull(implementationManifest);

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
