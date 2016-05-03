/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015 Markus Gärtner
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

 * $Revision$
 * $Date$
 * $URL$
 *
 * $LastChangedDate$
 * $LastChangedRevision$
 * $LastChangedBy$
 */
package de.ims.icarus2.model.manifest.xml;

import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.TypedManifest;

/**
 * @author Markus Gärtner
 * @version $Id$
 *
 */
public interface ManifestXmlDelegateFactory {

	ManifestXmlDelegate<?> newDelegate(ManifestType manifestType);

	@SuppressWarnings("unchecked")
	default <M extends TypedManifest> ManifestXmlDelegate<M> newDelegate(M manifest) {
		return (ManifestXmlDelegate<M>) newDelegate(manifest.getManifestType());
	}
}
