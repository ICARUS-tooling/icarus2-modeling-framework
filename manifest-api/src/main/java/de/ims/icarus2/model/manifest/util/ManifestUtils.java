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
package de.ims.icarus2.model.manifest.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestOwner;
import de.ims.icarus2.model.manifest.api.ManifestType;

/**
 * @author Markus Gärtner
 *
 */
public class ManifestUtils {

	private static final Pattern idPattern = Pattern.compile(
			"^\\p{Alpha}([:_\\-\\w]*[\\w])?$"); //$NON-NLS-1$

	private static Matcher idMatcher;

	/**
	 * Verifies the validity of the given {@code id} string.
	 * <p>
	 * Valid ids are defined as follows:
	 * <ul>
	 * <li>they have a minimum length of 3 characters</li>
	 * <li>they start with an alphabetic character (lower and upper case are allowed)</li>
	 * <li>subsequent characters may be alphabetic or digits</li>
	 * <li>no whitespaces, control characters or code points with 2 or more bytes are allowed</li>
	 * <li>no special characters are allowed besides the following 2: _- (underscore, hyphen)</li>
	 * </ul>
	 *
	 * Attempting to use any other string as an identifier for arbitrary members of a corpus will
	 * result in them being rejected by the registry.
	 */
	public static boolean isValidId(String id) {
		synchronized (idPattern) {
			if(idMatcher==null) {
				idMatcher = idPattern.matcher(id);
			} else {
				idMatcher.reset(id);
			}

			return idMatcher.matches();
		}
	}

	public static String getName(Object obj) {
		// IMPORTANT: should NEVER call Manifest.getName() to prevent loops!

		if(obj instanceof PrerequisiteManifest) {
			PrerequisiteManifest prerequisite = (PrerequisiteManifest)obj;
			String id = prerequisite.getLayerId();
			if(id!=null)
				return "Required layer-id: "+id; //$NON-NLS-1$

			String typeName = prerequisite.getTypeId();
			if(typeName!=null && !typeName.isEmpty())
				return "Required type-id: "+typeName; //$NON-NLS-1$

			return prerequisite.toString();
		} else if (obj instanceof ManifestOwner) {
			return ((ManifestOwner<?>)obj).getManifest().getName();
		} else {
			return obj.toString();
		}
	}

	public static boolean isItemLayerManifest(Manifest manifest) {
		return manifest.getManifestType()==ManifestType.ITEM_LAYER_MANIFEST;
	}

	//TODO methods for checking other types so we can use them as method references in lambdas
}
