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
