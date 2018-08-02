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
package de.ims.icarus2.model.manifest.api;

import java.util.EnumSet;
import java.util.Set;

import de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec;

/**
 * @author Markus Gärtner
 *
 */
public enum ManifestType {
	CONTAINER_MANIFEST(ContainerManifest.class, true),
	STRUCTURE_MANIFEST(StructureManifest.class, true),
	ANNOTATION_MANIFEST(AnnotationManifest.class, true),

	ANNOTATION_LAYER_MANIFEST(AnnotationLayerManifest.class, true),
	ITEM_LAYER_MANIFEST(ItemLayerManifest.class, true),
	STRUCTURE_LAYER_MANIFEST(StructureLayerManifest.class, true),
	FRAGMENT_LAYER_MANIFEST(FragmentLayerManifest.class, true),
	HIGHLIGHT_LAYER_MANIFEST(HighlightLayerManifest.class, true),

	LOCATION_MANIFEST(LocationManifest.class, false),
	OPTIONS_MANIFEST(OptionsManifest.class, true),
	CONTEXT_MANIFEST(ContextManifest.class, true),
	CORPUS_MANIFEST(CorpusManifest.class, true),
	PATH_RESOLVER_MANIFEST(PathResolverManifest.class, true),
	RASTERIZER_MANIFEST(RasterizerManifest.class, true),
	DRIVER_MANIFEST(DriverManifest.class, true),
	IMPLEMENTATION_MANIFEST(ImplementationManifest.class, true),
	LAYER_GROUP_MANIFEST(LayerGroupManifest.class, false),

	MODULE_MANIFEST(ModuleManifest.class, true),
	MODULE_SPEC(ModuleSpec.class, false),
	MAPPING_MANIFEST(MappingManifest.class, false),
	OPTION(OptionsManifest.Option.class, false),

	VALUE_RANGE(ValueRange.class, false),
	VALUE_SET(ValueSet.class, false),
	VALUE_MANIFEST(ValueManifest.class, false),
	DOCUMENTATION(Documentation.class, false),
	VERSION(VersionManifest.class, false),

	/**
	 * Reserved manifest type for use in testing.
	 * Client code is free to throw an exception whenever this type is
	 * encountered during runtime.
	 */
	DUMMY_MANIFEST(null, false),
	;

	private static final EnumSet<ManifestType> memberTypes = EnumSet.of(
			ITEM_LAYER_MANIFEST, STRUCTURE_LAYER_MANIFEST, ANNOTATION_LAYER_MANIFEST,
			FRAGMENT_LAYER_MANIFEST, HIGHLIGHT_LAYER_MANIFEST,

			CONTAINER_MANIFEST, STRUCTURE_MANIFEST, ANNOTATION_MANIFEST,

			CONTEXT_MANIFEST, CORPUS_MANIFEST,

			DRIVER_MANIFEST, MODULE_MANIFEST, PATH_RESOLVER_MANIFEST, RASTERIZER_MANIFEST);

	public static Set<ManifestType> getMemberTypes() {
		return memberTypes.clone();
	}

	private static final EnumSet<ManifestType> layerTypes = EnumSet.of(
			ITEM_LAYER_MANIFEST, STRUCTURE_LAYER_MANIFEST, ANNOTATION_LAYER_MANIFEST,
			FRAGMENT_LAYER_MANIFEST, HIGHLIGHT_LAYER_MANIFEST);

	public static Set<ManifestType> getLayerTypes() {
		return layerTypes.clone();
	}

	static {
		ANNOTATION_LAYER_MANIFEST.require(LAYER_GROUP_MANIFEST);
		ITEM_LAYER_MANIFEST.require(LAYER_GROUP_MANIFEST);
		STRUCTURE_LAYER_MANIFEST.require(LAYER_GROUP_MANIFEST);
		FRAGMENT_LAYER_MANIFEST.require(LAYER_GROUP_MANIFEST);
		HIGHLIGHT_LAYER_MANIFEST.require(LAYER_GROUP_MANIFEST);

		LAYER_GROUP_MANIFEST.require(CONTEXT_MANIFEST);

		ANNOTATION_MANIFEST.require(ANNOTATION_LAYER_MANIFEST);

		CONTAINER_MANIFEST.require(ITEM_LAYER_MANIFEST, STRUCTURE_LAYER_MANIFEST);
		STRUCTURE_MANIFEST.require(STRUCTURE_LAYER_MANIFEST);

		CONTEXT_MANIFEST.require(CORPUS_MANIFEST);

		DRIVER_MANIFEST.require(CONTEXT_MANIFEST);

		MODULE_MANIFEST.require(DRIVER_MANIFEST);

		MODULE_SPEC.require(DRIVER_MANIFEST);

		OPTION.require(memberTypes);

		IMPLEMENTATION_MANIFEST.require(memberTypes);

		PATH_RESOLVER_MANIFEST.require(LOCATION_MANIFEST);

		RASTERIZER_MANIFEST.require(FRAGMENT_LAYER_MANIFEST);
	}

	private final boolean supportTemplating;

	private final Class<? extends TypedManifest> baseClass;

	private ManifestType[] requiredEnvironment;

	ManifestType(Class<? extends TypedManifest> baseClass, boolean supportTemplating) {
		this.supportTemplating = supportTemplating;
		this.baseClass = baseClass;
	}

	public boolean isSupportTemplating() {
		return supportTemplating;
	}

	private void require(ManifestType...required) {
		requiredEnvironment = required;
	}

	private void require(Set<ManifestType> required) {
		requiredEnvironment = required.toArray(new ManifestType[required.size()]);
	}

	public ManifestType[] getRequiredEnvironment() {
		return requiredEnvironment==null || requiredEnvironment.length==0 ? null : requiredEnvironment.clone();
	}

	public boolean requiresEnvironment() {
		return requiredEnvironment!=null && requiredEnvironment.length>0;
	}

	/**
	 * @return the baseClass
	 */
	public Class<? extends TypedManifest> getBaseClass() {
		return baseClass;
	}
}
