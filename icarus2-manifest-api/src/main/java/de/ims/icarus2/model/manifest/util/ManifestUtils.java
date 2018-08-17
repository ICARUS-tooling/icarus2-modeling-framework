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
package de.ims.icarus2.model.manifest.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest;
import de.ims.icarus2.model.manifest.api.Embedded;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestFragment;
import de.ims.icarus2.model.manifest.api.ManifestOwner;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.manifest.api.TypedManifest;

/**
 * @author Markus Gärtner
 *
 */
public class ManifestUtils {

	private static final Pattern idPattern = Pattern.compile(
			"^\\p{Alpha}[._\\-\\w]*[\\w]$"); //$NON-NLS-1$

	private static Matcher idMatcher;

	public static final char ID_SEPARATOR = '@';

	public static final int MIN_ID_LENGTH = 3;

	/**
	 * Verifies the validity of the given {@code id} string.
	 * <p>
	 * Valid ids are defined as follows:
	 * <ul>
	 * <li>they have a minimum length of 3 characters</li>
	 * <li>they start with an alphabetic character (lower and upper case are allowed)</li>
	 * <li>subsequent characters may be alphabetic or digits</li>
	 * <li>no whitespaces, control characters or code points with 2 or more bytes are allowed</li>
	 * <li>no special characters are allowed besides the following 3: _-. (underscore, hyphen, dot)</li>
	 * </ul>
	 *
	 * Attempting to use any other string as an identifier for arbitrary members of a corpus will
	 * result in them being rejected by the registry.
	 */
	public static boolean isValidId(String id) {
		if(id==null || id.length()<MIN_ID_LENGTH) {
			return false;
		}

		synchronized (idPattern) {
			if(idMatcher==null) {
				idMatcher = idPattern.matcher(id);
			} else {
				idMatcher.reset(id);
			}

			return idMatcher.matches();
		}
	}

	/**
	 * Calls {@link #isValidId(String)} with the given {@code id} and throws
	 * a {@link ManifestException} with error code {@link ManifestErrorCode#MANIFEST_INVALID_ID}
	 * if the id has been found invalid.
	 *
	 * @param id
	 */
	public static void checkId(String id) {
		if(!isValidId(id))
			throw new ManifestException(ManifestErrorCode.MANIFEST_INVALID_ID,
					"Id format not supported: "+id);

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
			MemberManifest manifest = ((ManifestOwner<?>)obj).getManifest();
			return manifest.getName();
		} else {
			return obj.toString();
		}
	}

	public static boolean isItemLayerManifest(Manifest manifest) {
		return manifest.getManifestType()==ManifestType.ITEM_LAYER_MANIFEST;
	}

	//TODO methods for checking other types so we can use them as method references in lambdas

	public static String getUniqueId(ManifestFragment manifest) {
		String id = manifest.getId();

		if(id==null) {
			return null;
		} else if(manifest instanceof Embedded) {
			StringBuilder sb = new StringBuilder();
			buildUniqueId(sb, manifest);
			return sb.toString();
		} else {
			return id;
		}
	}

	private static void buildUniqueId(StringBuilder sb, ManifestFragment manifest) {
		if(manifest instanceof Embedded) {
			TypedManifest host = ((Embedded)manifest).getHost();
			if(host!=null && host instanceof ManifestFragment) {
				buildUniqueId(sb, (ManifestFragment)host);
				sb.append(ID_SEPARATOR);
			}
		}
		sb.append(manifest.getId());
	}

	/**
	 * If the given {@code id} denotes a unique identifier with a host part
	 * (i.e. it is of the form {@code host_id@element_id}), this method
	 * returns the {@code host_id} section. Otherwise the returned value
	 * will be {@code null}.
	 *
	 * @param id
	 * @return
	 */
	public static String extractHostId(String id) {
		int idx = id.indexOf(ID_SEPARATOR);
		return idx==-1 ? null : id.substring(0, idx);
	}

	/**
	 * If the given {@code id} denotes a unique identifier with a host part
	 * (i.e. it is of the form {@code host_id@element_id}), this method
	 * returns the {@code element_id} section. Otherwise the entire
	 * {@code id} parameter is returned.
	 *
	 * @param id
	 * @return
	 */
	public static String extractElementId(String id) {
		int idx = id.indexOf(ID_SEPARATOR);
		return idx==-1 ? id : id.substring(idx+1);
	}

	/**
	 * Checks whether the given {@code manifest} is part of a template declaration
	 * or a {@link Manifest#isTemplate() template} itself.
	 *
	 * @param manifest
	 * @return
	 *
	 * @see Manifest#hasTemplateContext()
	 * @see Manifest#isValidTemplate()
	 * @see Embedded#getHost()
	 */
	public static boolean hasTemplateContext(TypedManifest manifest) {

		while(manifest!=null) {
			if(manifest instanceof Manifest
					&& ((Manifest)manifest).isValidTemplate()){
				return true;
			}

			manifest = (manifest instanceof Embedded) ?
					((Embedded)manifest).getHost() : null;
		}

		return false;
	}
}
