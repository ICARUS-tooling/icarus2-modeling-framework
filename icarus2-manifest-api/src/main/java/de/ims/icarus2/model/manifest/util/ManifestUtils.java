/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2019 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest;
import de.ims.icarus2.model.manifest.api.Embedded;
import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.ImplementationManifest.SourceType;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestFragment;
import de.ims.icarus2.model.manifest.api.ManifestOwner;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.manifest.api.TypedManifest;
import de.ims.icarus2.model.manifest.standard.ImplementationManifestImpl;

/**
 * @author Markus Gärtner
 *
 */
public class ManifestUtils {

	private static final Pattern idPattern = Pattern.compile(
			"^\\p{Alpha}[.:_\\-\\w]*[\\w]$"); //$NON-NLS-1$

	private static Matcher idMatcher;

	public static final String ID_SEPARATOR = "::";

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
	 * <li>no special characters are allowed besides the following 4: _-.: (underscore, hyphen, dot, colon)</li>
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
					"Id format not supported: "+String.valueOf(id));
	}

	public static String checkIdNotNull(String id) {
		if(id==null)
			throw new ManifestException(GlobalErrorCode.ILLEGAL_STATE,
					"Id is null");
		return id;
	}

	public static String getName(Object obj) {
		// IMPORTANT: should NEVER call Manifest.getName() to prevent loops!

		// Unwrap optionals
		if(obj instanceof Optional) {
			obj = ((Optional<?>)obj).orElse(null);
		}

		String result = null;

		if(obj instanceof PrerequisiteManifest) {
			PrerequisiteManifest prerequisite = (PrerequisiteManifest)obj;
			Optional<String> id = prerequisite.getLayerId();
			if(id.isPresent())
				return "Required layer-id: "+id.get();

			Optional<String> typeName = prerequisite.getTypeId();
			if(typeName.isPresent() && !typeName.get().isEmpty())
				return "Required type-id: "+typeName.get();

			result = prerequisite.toString();
		} else if (obj instanceof ManifestOwner) {
			result = ((ManifestOwner<?>)obj).getManifest()
					.getName()
					.orElse(null);
		}

		if(result==null) {
			result = obj.toString();
		}

		return result;
	}

	public static boolean isItemLayerManifest(Manifest manifest) {
		return manifest.getManifestType()==ManifestType.ITEM_LAYER_MANIFEST;
	}

	public static boolean isAnnotationLayerManifest(Manifest manifest) {
		return manifest.getManifestType()==ManifestType.ANNOTATION_LAYER_MANIFEST;
	}

	//TODO methods for checking other types so we can use them as method references in lambdas

	private static String getIdOrDefault(ManifestFragment manifest) {
		return manifest.getId().orElseGet(() -> defaultCreateUnnamedId(manifest));
	}

	private static ManifestFragment getNamespaceCarrier(ManifestFragment manifest) {
		while(manifest instanceof Embedded) {
			Optional<TypedManifest> optHost = ((Embedded)manifest).getHost();
			if(optHost.isPresent() && optHost.get() instanceof ManifestFragment) {
				manifest = (ManifestFragment) optHost.get();
				if(manifest instanceof ContextManifest) {
					return manifest;
				}
			}
		}
		return null;
	}

	public static String getUniqueId(ManifestFragment manifest) {
		String baseId = getIdOrDefault(manifest);

		if(manifest instanceof Embedded && !(manifest instanceof ContextManifest)) {
			ManifestFragment namespace = getNamespaceCarrier(manifest);
			if(namespace!=null) {
				String namespaceId = getIdOrDefault(namespace);
				return namespaceId+ID_SEPARATOR+baseId;
			}
		}

		return baseId;
	}

	private static String defaultCreateUnnamedId(ManifestFragment fragment) {
		return "unnamed@"+fragment.getClass().getName();
	}

	private static void buildUniqueId(StringBuilder sb, ManifestFragment manifest) {
		if(manifest instanceof Embedded) {
			Optional<TypedManifest> optHost = ((Embedded)manifest).getHost();
			if(optHost.isPresent() && optHost.get() instanceof ManifestFragment) {
				buildUniqueId(sb, (ManifestFragment)optHost.get());
				sb.append(ID_SEPARATOR);
			}
		}
		sb.append(manifest.getId().orElseGet(() -> defaultCreateUnnamedId(manifest)));
	}

	/**
	 * If the given {@code id} denotes a unique identifier with a host part
	 * (i.e. it is of the form {@code host_id::element_id}), this method
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
	 * (i.e. it is of the form {@code host_id::element_id}), this method
	 * returns the {@code element_id} section. Otherwise the entire
	 * {@code id} parameter is returned.
	 *
	 * @param id
	 * @return
	 */
	public static String extractElementId(String id) {
		int idx = id.indexOf(ID_SEPARATOR);
		return idx==-1 ? id : id.substring(idx+ID_SEPARATOR.length());
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

		Optional<TypedManifest> opt = Optional.ofNullable(manifest);

		while(opt.isPresent()) {
			if(opt.get() instanceof Manifest
					&& ((Manifest)opt.get()).isValidTemplate()){
				return true;
			}

			opt = (opt.get() instanceof Embedded) ?
					((Embedded)opt.get()).getHost() : Optional.empty();
		}

		return false;
	}

	public static <M extends ManifestFragment, T extends Object>
			T require(Optional<T> value, M fragment, String property) {
		requireNonNull(value);

		return value.orElseThrow(ManifestException.missing(fragment, property));
	}

	public static <M extends ManifestFragment, T extends Object>
			T require(M fragment, Function<M, Optional<T>> func, String property) {
		return func.apply(fragment).orElseThrow(ManifestException.missing(fragment, property));
	}

	public static <M extends ManifestFragment, T extends Object, U extends Object>
			U require(Optional<T> value, Function<T, Optional<U>> func, M fragment,
					String property1, String property2) {
		requireNonNull(value);

		T tmp = value.orElseThrow(ManifestException.missing(fragment, property1));

		return func.apply(tmp).orElseThrow(ManifestException.missing(fragment, property2));
	}

	@SuppressWarnings("unchecked")
	public static <M extends TypedManifest, E extends Embedded & ManifestFragment> M requireHost(E manifest) {
		return (M) Optional.of(manifest)
				.flatMap(Embedded::getHost)
				.orElseThrow(ManifestException.noHost(manifest));
	}

	@SuppressWarnings("unchecked")
	public static <M extends TypedManifest, E extends Embedded & ManifestFragment> M requireGrandHost(E manifest) {
		return (M) Optional.of(manifest)
				.flatMap(Embedded::getHost)
				.map(Embedded.class::cast)
				.flatMap(Embedded::getHost)
				.orElseThrow(ManifestException.noHost(manifest));
	}

	public static String requireId(ManifestFragment fragment) {
		return fragment.getId()
				.orElseThrow(ManifestException.missing(fragment, "id"));
	}

	public static String requireName(MemberManifest<?> manifest) {
		return manifest.getName()
				.orElseThrow(ManifestException.missing(manifest, "name"));
	}

	public static ImplementationManifest liveImplementation(
			MemberManifest<?> host, Class<?> clazz) {
		return new ImplementationManifestImpl(host)
				.setSourceType(SourceType.DEFAULT)
				.setClassname(clazz.getName());
	}
}
