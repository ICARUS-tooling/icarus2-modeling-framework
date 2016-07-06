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
package de.ims.icarus2.model.manifest.xml;

import de.ims.icarus2.util.xml.XmlSerializer;


/**
 * @author Markus Gärtner
 *
 */
public interface ManifestXmlDelegate<M extends Object> extends ManifestXmlHandler {

	/**
	 * Intended for use in constructors, this method should check
	 * for the internal reference to the manifest framework member to be {@code null}
	 * and change it to the provided one if it is so. In case it is non-{@code null}
	 * an exception is thrown.
	 *
	 * @throws IllegalStateException if the internal reference to the
	 * manifest framework member has already been set.
	 */
	void setInstance(M instance);

	M getInstance();

	/**
	 * Erases any internal state and resets the reference to the manifest framework member
	 * managed by this delegate back to {@code null}. This method is mainly intended for
	 * delegates of commonly used primitive framework members.
	 */
	void reset();

	default ManifestXmlDelegate<M> reset(M instance) {
		reset();
		setInstance(instance);

		return this;
	}

	void writeXml(XmlSerializer serializer) throws Exception;
}
