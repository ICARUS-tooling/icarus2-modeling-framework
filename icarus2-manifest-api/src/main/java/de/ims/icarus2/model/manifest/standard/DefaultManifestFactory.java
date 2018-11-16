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

import static java.util.Objects.requireNonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.FragmentLayerManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestFactory;
import de.ims.icarus2.model.manifest.api.ManifestFragment;
import de.ims.icarus2.model.manifest.api.ManifestLocation;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.model.manifest.api.TypedManifest;
import de.ims.icarus2.model.manifest.standard.DriverManifestImpl.ModuleManifestImpl;
import de.ims.icarus2.model.manifest.standard.DriverManifestImpl.ModuleSpecImpl;
import de.ims.icarus2.util.Options;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultManifestFactory implements ManifestFactory {

	private final ManifestLocation manifestLocation;
	private final ManifestRegistry registry;

	private static class ManifestFragmentInfo {
		public final boolean skipProvenanceInfo;

		public final Class<?> implementingClass;
		public final Class<?> hostClass;

		public final Constructor<?> baseConstructor;
		public final Constructor<?> hostConstructor;

		public ManifestFragmentInfo(Class<?> implementingClass,
				Class<?> hostClass, boolean skipProvenanceInfo) throws NoSuchMethodException, SecurityException {
			requireNonNull(implementingClass);

			this.implementingClass = implementingClass;
			this.hostClass = hostClass;
			this.skipProvenanceInfo = skipProvenanceInfo;

			Constructor<?> baseConstructor = null;

			try {
				baseConstructor = implementingClass.getConstructor(ManifestLocation.class, ManifestRegistry.class);
			} catch(NoSuchMethodException e) {
				baseConstructor = null;
			}

			this.baseConstructor = baseConstructor;

			if(hostClass==null) {
				hostConstructor = null;
			} else {
				if(skipProvenanceInfo) {
					hostConstructor = implementingClass.getConstructor(hostClass);
				} else {
					hostConstructor = implementingClass.getConstructor(ManifestLocation.class, ManifestRegistry.class, hostClass);
				}
			}
		}
	}

	private static final Map<ManifestType, ManifestFragmentInfo> _info = new Object2ObjectOpenHashMap<>();

	protected static void registerInfo(ManifestType manifestType, Class<?> implementingClass,
			Class<?> hostClass) {
		registerInfo(manifestType, implementingClass, hostClass, false);
	}

	protected static void registerInfo(ManifestType manifestType, Class<?> implementingClass,
			Class<?> hostClass, boolean skipProvenanceInfo) {

		if(_info.containsKey(manifestType))
			throw new IllegalStateException("Duplicate implementation for "+manifestType);

		try {
			_info.put(manifestType, new ManifestFragmentInfo(implementingClass, hostClass, skipProvenanceInfo));
		} catch (NoSuchMethodException | SecurityException e) {
			throw new ManifestException(ManifestErrorCode.IMPLEMENTATION_ERROR,
					"Failed to compute implementation info for type: "+manifestType, e);
		}
	}

	static {
		registerInfo(ManifestType.ANNOTATION_LAYER_MANIFEST,AnnotationLayerManifestImpl.class, LayerGroupManifest.class);
		registerInfo(ManifestType.ANNOTATION_MANIFEST, AnnotationManifestImpl.class, AnnotationLayerManifest.class);
		registerInfo(ManifestType.CONTAINER_MANIFEST, ContainerManifestImpl.class, ItemLayerManifest.class);
		registerInfo(ManifestType.CONTEXT_MANIFEST, ContextManifestImpl.class, CorpusManifest.class);
		registerInfo(ManifestType.CORPUS_MANIFEST, CorpusManifestImpl.class, null);
		registerInfo(ManifestType.DRIVER_MANIFEST, DriverManifestImpl.class, ContextManifest.class);
		registerInfo(ManifestType.FRAGMENT_LAYER_MANIFEST, FragmentLayerManifestImpl.class, LayerGroupManifest.class);
		registerInfo(ManifestType.HIGHLIGHT_LAYER_MANIFEST, HighlightLayerManifestImpl.class, LayerGroupManifest.class);
		registerInfo(ManifestType.IMPLEMENTATION_MANIFEST, ImplementationManifestImpl.class, MemberManifest.class);
		registerInfo(ManifestType.ITEM_LAYER_MANIFEST, ItemLayerManifestImpl.class, LayerGroupManifest.class);
		registerInfo(ManifestType.LAYER_GROUP_MANIFEST, LayerGroupManifestImpl.class, ContextManifest.class, true);
		registerInfo(ManifestType.LOCATION_MANIFEST, LocationManifestImpl.class, null);
		registerInfo(ManifestType.MODULE_MANIFEST, ModuleManifestImpl.class, DriverManifest.class);
		registerInfo(ManifestType.MODULE_SPEC, ModuleSpecImpl.class, DriverManifest.class, true);
		registerInfo(ManifestType.OPTIONS_MANIFEST, OptionsManifestImpl.class, MemberManifest.class);
		registerInfo(ManifestType.PATH_RESOLVER_MANIFEST, PathResolverManifestImpl.class, LocationManifest.class);
		registerInfo(ManifestType.RASTERIZER_MANIFEST, RasterizerManifestImpl.class, FragmentLayerManifest.class);
		registerInfo(ManifestType.STRUCTURE_LAYER_MANIFEST, StructureLayerManifestImpl.class, LayerGroupManifest.class);
		registerInfo(ManifestType.STRUCTURE_MANIFEST, StructureManifestImpl.class, StructureLayerManifest.class);
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ManifestFactory#getSupportedTypes()
	 */
	@Override
	public Set<ManifestType> getSupportedTypes() {
		return Collections.unmodifiableSet(_info.keySet());
	}

	protected static ManifestFragmentInfo getInfo(ManifestType type) {
		ManifestFragmentInfo info = _info.get(type);

		if(info==null)
			throw new ManifestException(GlobalErrorCode.UNSUPPORTED_OPERATION,
					"Cannot create manifest for type: "+type);

		return info;
	}

	public DefaultManifestFactory(ManifestLocation manifestLocation, ManifestRegistry registry) {
		requireNonNull(manifestLocation);
		requireNonNull(registry);

		this.manifestLocation = manifestLocation;
		this.registry = registry;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ManifestFactory#create(de.ims.icarus2.model.manifest.api.ManifestType, TypedManifest, de.ims.icarus2.util.Options)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <M extends ManifestFragment> M create(ManifestType type,
			TypedManifest host, Options options) {
		requireNonNull(type);

		ManifestFragmentInfo info = getInfo(type);

		ManifestFragment result = null;

		if(host==null) {
			if(info.baseConstructor==null)
				throw new ManifestException(ManifestErrorCode.IMPLEMENTATION_FACTORY,
						"Cannot instantiate manifest "+type+" without matching host environment: "+info.hostClass);

			try {
				result = (ManifestFragment) info.baseConstructor.newInstance(manifestLocation, registry);
			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				throw new ManifestException(ManifestErrorCode.IMPLEMENTATION_ERROR,
						"Failed to create new instance of type "+type+" via base constructor", e);
			}
		} else {
			if(info.hostConstructor==null)
				throw new ManifestException(GlobalErrorCode.INVALID_INPUT,
						"No host supported on implementation of type "+type+": "+info.implementingClass);
			if(!info.hostClass.isInstance(host))
				throw new ManifestException(ManifestErrorCode.MANIFEST_MISSING_ENVIRONMENT,
						"Missing environment of type "+info.hostClass.getName()+" for creation of new "+type); //$NON-NLS-1$

			try {
				if(info.skipProvenanceInfo) {
					result = (ManifestFragment) info.hostConstructor.newInstance(host);
				} else {
					result = (ManifestFragment) info.hostConstructor.newInstance(manifestLocation, registry, host);
				}

			} catch (InstantiationException | IllegalAccessException
					| IllegalArgumentException | InvocationTargetException e) {
				throw new ManifestException(ManifestErrorCode.IMPLEMENTATION_ERROR,
						"Failed to create new instance of type "+type+" via host constructor", e);
			}
		}

		return (M) result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ManifestFactory#getManifestLocation()
	 */
	@Override
	public ManifestLocation getManifestLocation() {
		return manifestLocation;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.ManifestFactory#getRegistry()
	 */
	@Override
	public ManifestRegistry getRegistry() {
		return registry;
	}
}
