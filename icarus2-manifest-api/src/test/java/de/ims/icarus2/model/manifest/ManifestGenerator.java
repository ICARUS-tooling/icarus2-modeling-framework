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
/**
 *
 */
package de.ims.icarus2.model.manifest;

import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockManifestLocation;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockManifestRegistry;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.mockTypedManifest;
import static de.ims.icarus2.model.manifest.ManifestTestUtils.stubId;
import static de.ims.icarus2.test.TestUtils.isMock;
import static de.ims.icarus2.test.TestUtils.random;
import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.doReturn;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import de.ims.icarus2.model.manifest.api.AnnotationFlag;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.Categorizable;
import de.ims.icarus2.model.manifest.api.Category;
import de.ims.icarus2.model.manifest.api.ContainerFlag;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContainerType;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest.Note;
import de.ims.icarus2.model.manifest.api.Documentable;
import de.ims.icarus2.model.manifest.api.Documentation;
import de.ims.icarus2.model.manifest.api.Documentation.Resource;
import de.ims.icarus2.model.manifest.api.DriverManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest.ModuleManifest;
import de.ims.icarus2.model.manifest.api.DriverManifest.ModuleSpec;
import de.ims.icarus2.model.manifest.api.Embedded;
import de.ims.icarus2.model.manifest.api.ForeignImplementationManifest;
import de.ims.icarus2.model.manifest.api.FragmentLayerManifest;
import de.ims.icarus2.model.manifest.api.Hierarchy;
import de.ims.icarus2.model.manifest.api.HighlightFlag;
import de.ims.icarus2.model.manifest.api.HighlightLayerManifest;
import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest.PathEntry;
import de.ims.icarus2.model.manifest.api.LocationManifest.PathType;
import de.ims.icarus2.model.manifest.api.LocationType;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestFactory;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.MappingManifest;
import de.ims.icarus2.model.manifest.api.MappingManifest.Coverage;
import de.ims.icarus2.model.manifest.api.MappingManifest.Relation;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.manifest.api.MemberManifest.Property;
import de.ims.icarus2.model.manifest.api.ModifiableIdentity;
import de.ims.icarus2.model.manifest.api.OptionsManifest;
import de.ims.icarus2.model.manifest.api.OptionsManifest.Option;
import de.ims.icarus2.model.manifest.api.PathResolverManifest;
import de.ims.icarus2.model.manifest.api.RasterizerManifest;
import de.ims.icarus2.model.manifest.api.StructureFlag;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.api.StructureType;
import de.ims.icarus2.model.manifest.api.TypedManifest;
import de.ims.icarus2.model.manifest.api.ValueManifest;
import de.ims.icarus2.model.manifest.api.ValueRange;
import de.ims.icarus2.model.manifest.api.ValueSet;
import de.ims.icarus2.model.manifest.api.VersionManifest;
import de.ims.icarus2.model.manifest.standard.AbstractMemberManifest.PropertyImpl;
import de.ims.icarus2.model.manifest.standard.CorpusManifestImpl.NoteImpl;
import de.ims.icarus2.model.manifest.standard.DefaultCategory;
import de.ims.icarus2.model.manifest.standard.DefaultManifestFactory;
import de.ims.icarus2.model.manifest.standard.DefaultModifiableIdentity;
import de.ims.icarus2.model.manifest.standard.DocumentationImpl;
import de.ims.icarus2.model.manifest.standard.DocumentationImpl.ResourceImpl;
import de.ims.icarus2.model.manifest.standard.HierarchyImpl;
import de.ims.icarus2.model.manifest.standard.LocationManifestImpl.PathEntryImpl;
import de.ims.icarus2.model.manifest.standard.MappingManifestImpl;
import de.ims.icarus2.model.manifest.standard.OptionsManifestImpl;
import de.ims.icarus2.model.manifest.standard.ValueManifestImpl;
import de.ims.icarus2.model.manifest.standard.ValueRangeImpl;
import de.ims.icarus2.model.manifest.standard.ValueSetImpl;
import de.ims.icarus2.model.manifest.standard.VersionManifestImpl;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.test.TestUtils;
import de.ims.icarus2.util.Multiplicity;
import de.ims.icarus2.util.data.ContentTypeRegistry;
import de.ims.icarus2.util.id.Identity;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * Factory to produce incremental constructions of real manifests
 * for testing. This class is intended for use cases where (partially)
 * filled manifest instances are needed.
 * <p>
 * For more basic functionality see the various mocking methods for
 * manifests in the {@link ManifestTestUtils} utility class (such as
 * {@link ManifestTestUtils#mockTypedManifest(ManifestType)}).
 *
 * @author Markus Gärtner
 *
 */
public class ManifestGenerator {

	public static ManifestGenerator forType(ManifestType type) {

		ManifestFactory factory = new DefaultManifestFactory(
				mockManifestLocation(type.isSupportTemplating()),
				mockManifestRegistry());

		return new ManifestGenerator(factory);
	}

	public static <M extends TypedManifest> IncrementalBuild<M> generateOnce(ManifestType type, Config config) {

		ManifestFactory factory = new DefaultManifestFactory(
				mockManifestLocation(type.isSupportTemplating()),
				mockManifestRegistry());

		return generateOnce(factory, type, config);
	}

	public static <M extends TypedManifest> IncrementalBuild<M> generateOnce(
			ManifestFactory factory, ManifestType type, Config config) {
		ManifestGenerator generator = new ManifestGenerator(factory);

		return generateOnce(generator, type, config);
	}

	public static <M extends TypedManifest> IncrementalBuild<M> generateOnce(
			ManifestGenerator generator, ManifestType type, Config config) {

		if(type.requiresEnvironment()) {
			ManifestType hostType = type.getRequiredEnvironment()[0];
			TypedManifest host = mockTypedManifest(hostType, true);

			return generator.build(type, host, config);
		} else {
			return generator.build(type, config);
		}
	}

	/**
	 * Creates manifests based on a {@link Config} instance.
	 */
	private static Map<ManifestType, Function<Config, TypedManifest>> fixedSimpleImplementations =
			new Object2ObjectOpenHashMap<>();
	static {
		fixedSimpleImplementations.put(ManifestType.VALUE_RANGE, config -> new ValueRangeImpl(config.getValueType()));
		fixedSimpleImplementations.put(ManifestType.VALUE_SET, config -> new ValueSetImpl(config.getValueType()));
		fixedSimpleImplementations.put(ManifestType.VALUE_MANIFEST, config -> new ValueManifestImpl(config.getValueType()));

		fixedSimpleImplementations.put(ManifestType.DOCUMENTATION, config -> new DocumentationImpl());
		fixedSimpleImplementations.put(ManifestType.VERSION, config -> new VersionManifestImpl());
		fixedSimpleImplementations.put(ManifestType.OPTION, config -> new OptionsManifestImpl.OptionImpl());
	}

	/**
	 * Creates manifests based on a host and {@link Config} instance.
	 */
	private static Map<ManifestType, BiFunction<Config, TypedManifest, TypedManifest>> fixedComplexImplementations =
			new Object2ObjectOpenHashMap<>();
	static {
		fixedComplexImplementations.put(ManifestType.MAPPING_MANIFEST,
				(config, host) -> new MappingManifestImpl((DriverManifest) host));
	}

	private final ManifestFactory factory;

	private final AtomicInteger counter = new AtomicInteger(0);

	public ManifestGenerator(ManifestFactory factory) {
		this.factory = requireNonNull(factory);
	}

	public <M extends TypedManifest> IncrementalBuild<M> build(ManifestType type) {
		requireNonNull(type);
		return generate0(type, config());
	}

	public <M extends TypedManifest> IncrementalBuild<M> build(ManifestType type, Config config) {
		requireNonNull(type);
		requireNonNull(config);
		return generate0(type, config);
	}

	public <M extends TypedManifest> IncrementalBuild<M> build(ManifestType type, TypedManifest host, Config config) {
		requireNonNull(type);
		requireNonNull(host);
		requireNonNull(config);
		return generate0(type, host, config);
	}

	public <M extends TypedManifest> M generatePlain(ManifestType type, Config config) {

		if(type.requiresEnvironment()) {
			ManifestType hostType = type.getRequiredEnvironment()[0];
			TypedManifest host = mockTypedManifest(hostType, true);

			return instantiate(type, host, config);
		} else {
			return instantiate(type, config);
		}
	}

	public <M extends TypedManifest> M generatePlainCopy(ManifestType type, M original, Config config) {

		if(type.requiresEnvironment()) {
			TypedManifest host = ((Embedded) original).getHost().get();

			return instantiate(type, host, config);
		} else {
			return instantiate(type, config);
		}
	}

	@SuppressWarnings("unchecked")
	private <M extends TypedManifest> M instantiate(ManifestType type, Config config) {
		return (M) Optional.ofNullable(fixedSimpleImplementations.get(type))
			.orElse(c -> factory.create(type))
			.apply(config);
	}

	@SuppressWarnings("unchecked")
	private <M extends TypedManifest> M instantiate(ManifestType type, TypedManifest host, Config config) {
		return (M) Optional.ofNullable(fixedComplexImplementations.get(type))
			.orElse((c,h) -> factory.create(type, h))
			.apply(config, host);
	}

	private <M extends TypedManifest> IncrementalBuild<M> generate0(ManifestType type, Config config) {
		M instance = instantiate(type, config);
		return prepare0(instance, config);
	}

	private <M extends TypedManifest> IncrementalBuild<M> generate0(ManifestType type, TypedManifest host, Config config) {
		M instance = instantiate(type, host, config);
		return prepare0(instance, config);
	}

	private <M extends TypedManifest> IncrementalBuild<M> prepare0(M manifest, Config config) {
		ManifestType type = manifest.getManifestType();
		IncrementalBuild<M> container = new IncrementalBuild<M>(manifest);

		config.preprocess(manifest);

		switch (type) {
		case ANNOTATION_LAYER_MANIFEST: prepareAnnotationLayerManifest((AnnotationLayerManifest) manifest, container, config); break;
		case ANNOTATION_MANIFEST: prepareAnnotationManifest((AnnotationManifest) manifest, container, config); break;
		case CONTAINER_MANIFEST: prepareContainerManifest((ContainerManifest) manifest, container, config); break;
		case CONTEXT_MANIFEST: prepareContextManifest((ContextManifest) manifest, container, config); break;
		case CORPUS_MANIFEST: prepareCorpusManifest((CorpusManifest) manifest, container, config); break;
		case DOCUMENTATION: prepareDocumentation((Documentation) manifest, container, config); break;
		case DRIVER_MANIFEST: prepareDriverManifest((DriverManifest) manifest, container, config); break;
		case DUMMY_MANIFEST: fail("not handled: "+type); break;
		case FRAGMENT_LAYER_MANIFEST: prepareFragmentLayerManifest((FragmentLayerManifest) manifest, container, config); break;
		case HIGHLIGHT_LAYER_MANIFEST: prepareHighlightLayerManifest((HighlightLayerManifest) manifest, container, config); break;
		case IMPLEMENTATION_MANIFEST: prepareImplementationManifest((ImplementationManifest) manifest, container, config); break;
		case ITEM_LAYER_MANIFEST: prepareItemLayerManifest((ItemLayerManifest) manifest, container, config); break;
		case LAYER_GROUP_MANIFEST: prepareLayerGroupManifest((LayerGroupManifest) manifest, container, config); break;
		case LOCATION_MANIFEST: prepareLocationManifest((LocationManifest) manifest, container, config); break;
		case MAPPING_MANIFEST: prepareMappingManifest((MappingManifest) manifest, container, config); break;
		case MODULE_MANIFEST: prepareModuleManifest((ModuleManifest) manifest, container, config); break;
		case MODULE_SPEC: prepareModuleSpec((ModuleSpec) manifest, container, config); break;
		case OPTION: prepareOption((Option) manifest, container, config); break;
		case OPTIONS_MANIFEST: prepareOptionsManifest((OptionsManifest) manifest, container, config); break;
		case PATH_RESOLVER_MANIFEST: preparePathResolverManifest((PathResolverManifest) manifest, container, config); break;
		case RASTERIZER_MANIFEST: prepareRasterizerManifest((RasterizerManifest) manifest, container, config); break;
		case STRUCTURE_LAYER_MANIFEST: prepareStructureLayerManifest((StructureLayerManifest) manifest, container, config); break;
		case STRUCTURE_MANIFEST: prepareStructureManifest((StructureManifest) manifest, container, config); break;
		case VALUE_MANIFEST: prepareValueManifest((ValueManifest) manifest, container, config); break;
		case VALUE_RANGE: prepareValueRange((ValueRange) manifest, container, config); break;
		case VALUE_SET: prepareValueSet((ValueSet) manifest, container, config); break;
		case VERSION: prepareVersionManifest((VersionManifest) manifest, container, config); break;

		default:
			fail("Type not handled: "+type);
		}

		config.postprocess(manifest);

		return container;
	}

	private int index() {
		return counter.getAndIncrement();
	}

	private String index(String base) {
		return base+index();
	}

	private Supplier<String> indexSupplier(String base) {
		return () -> index(base);
	}

	/**
	 * Wraps an entire (sub-)build into a single change by using its result as
	 * the input for the specified {@code setter} method.
	 * @param container
	 * @param setter
	 * @return
	 */
	private static <M extends TypedManifest> Change wrap(IncrementalBuild<M> container, Consumer<M> setter) {
		return () -> {
			container.applyAllChanges();
			setter.accept(container.getInstance());
		};
	}

	// Helpers for creating value type objects

	private void fillIdentity(ModifiableIdentity identity, int index) {
		identity.setName("cat"+index);
		identity.setDescription("desc"+index);
		identity.setId("catId"+index);
	}

	private Category createCategory(int index) {
		DefaultCategory category = new DefaultCategory();
		fillIdentity(category, index);
		category.setNamespace("catNamespace"+index);
		return category;
	}

	private PathEntry createPathEntry(PathType pathType) {
		return new PathEntryImpl(pathType, index("randomPath"));
	}

	private Resource createResource(int index) {
		ResourceImpl resource = new ResourceImpl();
		fillIdentity(resource, index);
		resource.setUri(URI.create("www.some.uri/"+index+"/whatever/#anchor"));
		return resource;
	}

	private Identity createIdentity(int index) {
		ModifiableIdentity identity = new DefaultModifiableIdentity();
		fillIdentity(identity, index);
		return identity;
	}

	private Property createProperty(ValueType valueType) {
		int index = index();
		PropertyImpl property = new PropertyImpl("property"+index, valueType);
		property.setValue(ManifestTestUtils.getTestValue(valueType));
		return property;
	}

	private Note createNote(int index) {
		NoteImpl note = new NoteImpl("note"+index);
		note.setModificationDate(LocalDateTime.now());
		note.changeContent(TestUtils.LOREM_IPSUM_ASCII);
		return note;
	}

	// Generic preparations

	private void prepareManifest(Manifest manifest, IncrementalBuild<?> container, Config config) {
		manifest.setId(index("manifestId"));

		//TODO currently we don't add any templating related fields
	}

	private <D extends Documentable & TypedManifest> void prepareDocumentable(
			D documentable, IncrementalBuild<?> container, Config config) {
		container.<Documentation>addManifestChange("documentation", ManifestType.DOCUMENTATION,
				config, documentable::setDocumentation);
	}

	private void prepareIdentity(ModifiableIdentity identity,
			IncrementalBuild<?> container, Config config) {

		// Only mess with id if not already set!!
		if(!identity.getId().isPresent())
			container.addFieldChange(identity::setId, "id", index("myId"));

		container.addFieldChange(identity::setDescription, "description", index("myDesc\nrandom stuff"));
		container.addFieldChange(identity::setName, "name", index("myName"));
		//TODO add Icon
	}

	private void prepareCategorizable(Categorizable categorizable,
			IncrementalBuild<?> container, Config config) {
		container.addFieldChange(categorizable::addCategory, "category", createCategory(0));
	}

	private void prepareMemberManifest(MemberManifest manifest,
			IncrementalBuild<?> container, Config config) {
		prepareManifest(manifest, container, config);
		prepareIdentity(manifest, container, config);
		prepareCategorizable(manifest, container, config);
		prepareDocumentable(manifest, container, config);

		container.addNestedManifestChange("options", ManifestType.OPTIONS_MANIFEST,
				manifest, config, manifest::setOptionsManifest);

		for(ValueType valueType : ValueType.simpleValueTypes()) {
			container.addLazyFieldChange(manifest::addProperty, "property"+valueType,
					() -> createProperty(valueType));
		}
	}

	private void prepareLayerManifest(LayerManifest manifest,
			IncrementalBuild<?> container, Config config) {
		prepareMemberManifest(manifest, container, config);

		for(int i=0; i<3; i++)
			container.addFieldChange(manifest::addBaseLayerId, "baseLayer", index("layer"));
	}

	private void prepareForeignImplementationManifest(ForeignImplementationManifest manifest,
			IncrementalBuild<?> container, Config config) {
		prepareMemberManifest(manifest, container, config);

		container.addNestedManifestChange("implementation", ManifestType.IMPLEMENTATION_MANIFEST, manifest,
				config, manifest::setImplementationManifest);
	}

	// Specific preparations

	private void prepareAnnotationLayerManifest(AnnotationLayerManifest manifest,
			IncrementalBuild<?> container, Config config) {
		prepareLayerManifest(manifest, container, config);

		container.addFieldChange(manifest::setDefaultKey, "defaultKey", index("defaultKey"));

		for(ValueType valueType : ValueType.simpleValueTypes()) {
			container.addManifestChange("annotationManifest"+valueType,
					ManifestType.ANNOTATION_MANIFEST, config.valueType(valueType), manifest::addAnnotationManifest);
		}

		for(int i=0; i<3; i++) {
			container.addFieldChange(manifest::addReferenceLayerId, "referenceLayer", index("layer"));
		}

		for(AnnotationFlag flag : AnnotationFlag.values()) {
			container.addFieldChange(f -> manifest.setAnnotationFlag(f, true), "annotationFlag", flag);
		}
	}

	private void prepareAnnotationManifest(AnnotationManifest manifest,
			IncrementalBuild<?> container, Config config) {
		prepareMemberManifest(manifest, container, config);

		ValueType valueType = config.getValueType();

		manifest.setValueType(valueType);

		container.addFieldChange(manifest::setAllowUnknownValues, "allowUnknownValues",
				Boolean.valueOf(!AnnotationManifest.DEFAULT_ALLOW_UNKNOWN_VALUES));
		container.addFieldChange(manifest::setKey, "key", index("key"));
		container.addFieldChange(manifest::setNoEntryValue, "noEntryValue",
				ManifestTestUtils.getTestValue(valueType));
		container.addFieldChange(manifest::setContentType, "contentType",
				ContentTypeRegistry.getInstance().getType("IntegerContentType"));

		for(int i=0; i<3; i++) {
			container.addFieldChange(manifest::addAlias, "alias", index("alias"));
		}

		container.addManifestChange("valueSet", ManifestType.VALUE_SET, config, manifest::setValueSet);

		if(ValueRange.SUPPORTED_VALUE_TYPES.contains(valueType)) {
			container.addManifestChange("valueRange", ManifestType.VALUE_RANGE, config, manifest::setValueRange);
		}
	}

	private void prepareContainerManifest(ContainerManifest manifest,
			IncrementalBuild<?> container, Config config) {
		prepareMemberManifest(manifest, container, config);

		for(ContainerFlag flag : ContainerFlag.values()) {
			container.addFieldChange(f -> manifest.setContainerFlag(f, true), "containerFlag", flag);
		}
	}

	private void prepareContextManifest(ContextManifest manifest,
			IncrementalBuild<?> container, Config config) {
		prepareMemberManifest(manifest, container, config);

		for(int i=0; i<3; i++) {
			container.addFieldChange(manifest::addPrerequisite, "prerequisite", index("alias"));
		}

		for(int i=0; i<3; i++) {
			container.addNestedManifestChange("layerGroup", ManifestType.LAYER_GROUP_MANIFEST,
					manifest, config, manifest::addLayerGroup);
		}

		for(int i=0; i<3; i++) {
			container.addManifestChange("location", ManifestType.LOCATION_MANIFEST,
					config, manifest::addLocationManifest);
		}
	}

	private void prepareCorpusManifest(CorpusManifest manifest,
			IncrementalBuild<?> container, Config config) {
		prepareMemberManifest(manifest, container, config);

		container.addNestedManifestChange("rootContext", ManifestType.CONTEXT_MANIFEST,
				manifest, config, manifest::addRootContextManifest);

		for(int i=0; i<3; i++) {
			container.addNestedManifestChange("customContext", ManifestType.CONTEXT_MANIFEST,
					manifest, config, manifest::addCustomContextManifest);
		}

		for(int i=0; i<3; i++) {
			container.addFieldChange(manifest::addNote, "note", createNote(index()));
		}

		container.addFieldChange(manifest::setEditable, "editable",
				Boolean.valueOf(!CorpusManifest.DEFAULT_EDITABLE_VALUE));

		container.addFieldChange(manifest::setParallel, "parallel",
				Boolean.valueOf(!CorpusManifest.DEFAULT_PARALLEL_VALUE));

		for(int i=0; i<2; i++) {
			container.addNestedManifestChange("rootContext", ManifestType.CONTEXT_MANIFEST,
					manifest, config, manifest::addRootContextManifest);
		}
	}

	private void prepareDocumentation(Documentation documentation,
			IncrementalBuild<?> container, Config config) {
		container.addFieldChange(documentation::setContent, "content", TestUtils.LOREM_IPSUM_CHINESE);

		for(int i=0; i<3; i++) {
			container.addFieldChange(documentation::addResource, "resource", createResource(index()));
		}
	}

	private void prepareDriverManifest(DriverManifest manifest,
			IncrementalBuild<?> container, Config config) {
		prepareForeignImplementationManifest(manifest, container, config);

		for(LocationType locationType : LocationType.values()) {
			container.addFieldChange(manifest::setLocationType, "locationType", locationType);
		}

		Queue<String> specIds = new LinkedList<>();

		for(int i=0; i<3; i++) {
			Consumer<ModuleSpec> action = manifest::addModuleSpec;
			action = action.andThen(spec -> specIds.add(ManifestUtils.requireId(spec)));
			container.addNestedManifestChange("spec", ManifestType.MODULE_SPEC,
					manifest, config, action);
		}

		for(int i=0; i<3; i++) {
			container.addLazyFieldChange(manifest::addModuleManifest, "module",
					() -> {
						ModuleManifest mod = (ModuleManifest) generate0(
								ManifestType.MODULE_MANIFEST, manifest, config)
								.applyAllAndGet();
						mod.setModuleSpecId(specIds.remove());
						return mod;
					});
		}

		for(int i=0; i<3; i++) {
			container.addNestedManifestChange("mapping", ManifestType.MAPPING_MANIFEST,
					manifest, config, manifest::addMappingManifest);
		}
	}

	private void prepareFragmentLayerManifest(FragmentLayerManifest manifest,
			IncrementalBuild<?> container, Config config) {
		prepareItemLayerManifest(manifest, container, config);

		container.addFieldChange(manifest::setAnnotationKey, "annotationKey", index("key"));
		container.addFieldChange(manifest::setValueLayerId, "valueLayer", index("valueLayer"));
		container.addNestedManifestChange("rasterizer", ManifestType.RASTERIZER_MANIFEST, manifest,
				config, manifest::setRasterizerManifest);
	}

	/**
	 * Makes the given {@code layerManifest} accessible from the specified {@code groupManifest}.
	 * Note that this accessibility only covers lookup operations, no bulk getters or
	 * {@code for-each} traversals!
	 *
	 * @param groupManifest
	 * @param layerManifest
	 */
	private void injectLayer(LayerGroupManifest groupManifest, LayerManifest layerManifest) {
		if(!isMock(groupManifest)) {
			groupManifest.addLayerManifest(layerManifest);
		} else {
			String id = ManifestUtils.requireId(layerManifest);

			doReturn(Optional.of(layerManifest)).when(groupManifest).getLayerManifest(id);

			ContextManifest contextManifest = groupManifest.getContextManifest().orElse(null);
			if(isMock(contextManifest)) {
				doReturn(Optional.of(layerManifest)).when(contextManifest).getLayerManifest(id);
			}
		}
	}

	private void prepareHighlightLayerManifest(HighlightLayerManifest manifest,
			IncrementalBuild<?> container, Config config) {
		prepareLayerManifest(manifest, container, config);

		LayerManifest layerManifest = manifest.getContextManifest()
				.map(c -> c.getLayerManifests(ManifestUtils::isItemLayerManifest))
				.filter(l -> !l.isEmpty())
				.map(l -> l.get(0))
				.orElse(null);

		if(layerManifest==null) {
			LayerGroupManifest groupManifest = manifest.getGroupManifest().get();
			layerManifest = instantiate(ManifestType.ITEM_LAYER_MANIFEST, groupManifest, config);
			layerManifest.setId(index("layer"));
			injectLayer(groupManifest, layerManifest);
		}

		String id = ManifestUtils.requireId(layerManifest);

		container.addFieldChange(manifest::setPrimaryLayerId, "primaryLayer", id);

		for(HighlightFlag flag : HighlightFlag.values()) {
			container.addFieldChange(f -> manifest.setHighlightFlag(f, true), "highLightFlag", flag);
		}
	}

	private void prepareImplementationManifest(ImplementationManifest manifest,
			IncrementalBuild<?> container, Config config) {
		manifest.setClassname(index("de.ims.test.FancyClass"));

		prepareMemberManifest(manifest, container, config);

		container.addFieldChange(manifest::setSource, "source", index("source"));
		container.addFieldChange(manifest::setUseFactory, "factory",
				Boolean.valueOf(!ImplementationManifest.DEFAULT_USE_FACTORY_VALUE));
	}

	private void prepareItemLayerManifest(ItemLayerManifest manifest,
			IncrementalBuild<?> container, Config config) {

		Hierarchy<ContainerManifest> hierarchy = new HierarchyImpl<>();
		hierarchy.add((ContainerManifest)generate0(ManifestType.CONTAINER_MANIFEST, manifest, config).applyAllAndGet());
		manifest.setContainerHierarchy(hierarchy);

		prepareLayerManifest(manifest, container, config);

		container.addFieldChange(manifest::setFoundationLayerId, "foundationLayer", index("layer"));
		container.addFieldChange(manifest::setBoundaryLayerId, "boundaryLayer", index("layer"));

		for(ContainerType containerType : ContainerType.values()) {
			if(containerType==ContainerType.PROXY) {
				continue;
			}
			container.addNestedManifestChange("container::"+containerType, ManifestType.CONTAINER_MANIFEST,
					manifest, config.preprocessor(ManifestType.CONTAINER_MANIFEST,
							c -> ((ContainerManifest)c).setContainerType(containerType))
					.label(containerType.toString()),
					cm -> manifest.getContainerHierarchy().get().add((ContainerManifest)cm));
		}
	}

	private Consumer<LayerManifest> createLayerLookup(ContextManifest contextManifest) {
		if(!isMock(contextManifest)) {
			return TestUtils.DO_NOTHING();
		}

		return layerManifest -> {
			doReturn(Optional.of(layerManifest)).when(contextManifest).getLayerManifest(layerManifest.getId()
					.orElseThrow(() -> new AssertionError("No layer id to mock lookup for")));
		};
	}

	private void prepareLayerGroupManifest(LayerGroupManifest manifest,
			IncrementalBuild<?> container, Config config) {
		prepareIdentity(manifest, container, config);

		/*
		 *  To be able to provide a "real" primary layer
		 *  we need to intercept all layers that get added to
		 *  this group during preparation phase and then pick
		 *  an item layer amongst them to be used as primary layer
		 */
		Consumer<LayerManifest> action = createLayerLookup(manifest.getContextManifest().orElse(null));
		List<LayerManifest> layers = new ArrayList<>();
		action = action.andThen(layers::add);
		action = action.andThen(manifest::addLayerManifest);

		for(ManifestType type : ManifestType.getLayerTypes()) {
			container.addNestedManifestChange("layer::"+type, type, manifest, config, action);
		}

		container.addFieldChange(manifest::setIndependent, "independent",
				Boolean.valueOf(!LayerGroupManifest.DEFAULT_INDEPENDENT_VALUE));

		/*
		 *  We added instances for all possible layer types already,
		 *  so we are guaranteed to have an item layer here!
		 */
		container.addLazyFieldChange(manifest::setPrimaryLayerId, "primaryLayer",
				() -> ManifestUtils.requireId(layers.stream()
						.filter(ManifestUtils::isItemLayerManifest)
						.findFirst()
						.get()));
	}

	private void prepareLocationManifest(LocationManifest manifest,
			IncrementalBuild<?> container, Config config) {

		prepareManifest(manifest, container, config);

		if(manifest.isInline()) {
			manifest.setInlineData(TestUtils.LOREM_IPSUM_ASCII);
			container.addValueChange("with unicode content", manifest::setInlineData,
					TestUtils.LOREM_IPSUM_ISO);
		} else {
			manifest.setRootPath("some/path/to/whatever/file");

			for(PathType pathType : PathType.values()) {
				PathEntry entry = createPathEntry(pathType);
				container.addFieldChange(manifest::addPathEntry, "pathEntry for "+pathType, entry);
			}

			container.addManifestChange("pathResolver", ManifestType.PATH_RESOLVER_MANIFEST,
					config, manifest::setPathResolverManifest);
		}
	}

	private void prepareMappingManifest(MappingManifest manifest,
			IncrementalBuild<?> container, Config config) {

		if(!manifest.getCoverage().isPresent())
			manifest.setCoverage(Coverage.TOTAL);
		if(!manifest.getRelation().isPresent())
			manifest.setRelation(Relation.ONE_TO_ONE);

		container.addFieldChange(manifest::setId, "id", index("id"));
		container.addFieldChange(manifest::setSourceLayerId, "sourceLayerId", index("sourceLayer"));
		container.addFieldChange(manifest::setTargetLayerId, "targetLayerId", index("targetLayer"));

		DriverManifest driverManifest = manifest.getDriverManifest().orElse(null);
		if(isMock(driverManifest)) {
			String id = index("inverse");
			MappingManifest inverse = stubId(mockTypedManifest(ManifestType.MAPPING_MANIFEST), id);
			doReturn(Optional.of(inverse)).when(driverManifest).getMappingManifest(id);

			container.addFieldChange(manifest::setInverseId, "inverse", id);
		}
	}

	private void prepareModuleManifest(ModuleManifest manifest,
			IncrementalBuild<?> container, Config config) {
		prepareForeignImplementationManifest(manifest, container, config);

		DriverManifest driverManifest = manifest.getDriverManifest().orElse(null);
		if(isMock(driverManifest)) {
			String id = index("spec");
			ModuleSpec spec = stubId(mockTypedManifest(ManifestType.MODULE_SPEC), id);
			doReturn(Optional.of(spec)).when(driverManifest).getModuleSpec(id);

			container.addFieldChange(manifest::setModuleSpecId, "moduleSpec", id);
		} else if(driverManifest!=null) {
			Set<ModuleSpec> specs = driverManifest.getModuleSpecs();
			ModuleSpec spec;
			if(specs.isEmpty()) {
				spec = instantiate(ManifestType.MODULE_SPEC, driverManifest, config);
				driverManifest.addModuleSpec(spec);
			} else {
				spec = random(specs);
			}

			container.addFieldChange(manifest::setModuleSpecId, "specId", ManifestUtils.requireId(spec));
		}
	}

	private void prepareModuleSpec(ModuleSpec spec,
			IncrementalBuild<?> container, Config config) {
		prepareIdentity(spec, container, config);
		prepareDocumentable(spec, container, config);
		prepareCategorizable(spec, container, config);

		for(Multiplicity multiplicity : Multiplicity.values()) {
			container.addFieldChange(spec::setMultiplicity, "multicplicity", multiplicity);
		}

		container.addFieldChange(spec::setCustomizable, "customizable",
				Boolean.valueOf(!ModuleSpec.DEFAULT_IS_CUSTOMIZABLE));
		container.addFieldChange(spec::setExtensionPointUid, "extensionPointUid", index("extension"));
	}

	private void prepareOption(Option option,
			IncrementalBuild<?> container, Config config) {

		ValueType valueType = config.getValueType();

		option.setValueType(valueType);

		prepareIdentity(option, container, config);

		if(option.isMultiValue()) {
			container.addFieldChange(option::setDefaultValue, "defaultValue",
					Arrays.asList(ManifestTestUtils.getTestValues(valueType)));
		} else {
			container.addFieldChange(option::setDefaultValue, "defaultValue", ManifestTestUtils.getTestValue(valueType));
		}

		container.addFieldChange(option::setAllowNull, "allowNull", Boolean.valueOf(!Option.DEFAULT_ALLOW_NULL));
		container.addFieldChange(option::setExtensionPointUid, "extensionPointUid", index("extension"));
		container.addFieldChange(option::setOptionGroup, "groupId", index("group"));
		container.addFieldChange(option::setPublished, "published", Boolean.valueOf(!Option.DEFAULT_PUBLISHED_VALUE));

		container.addManifestChange("valueSet", ManifestType.VALUE_SET, config, option::setSupportedValues);

		if(ValueRange.SUPPORTED_VALUE_TYPES.contains(valueType)) {
			container.addManifestChange("valueRange", ManifestType.VALUE_RANGE, config, option::setSupportedRange);
		}
	}

	private void prepareOptionsManifest(OptionsManifest manifest,
			IncrementalBuild<?> container, Config config) {

		for(ValueType valueType : ValueType.simpleValueTypes()) {
			container.addManifestChange("option"+valueType, ManifestType.OPTION, config.valueType(valueType), manifest::addOption);
		}

		for(int i=0; i<3; i++) {
			container.addFieldChange(manifest::addGroupIdentifier, "groupIdentifer", createIdentity(index()));
		}
	}

	private void preparePathResolverManifest(PathResolverManifest manifest,
			IncrementalBuild<?> container, Config config) {
		prepareForeignImplementationManifest(manifest, container, config);
	}

	private void prepareRasterizerManifest(RasterizerManifest manifest,
			IncrementalBuild<?> container, Config config) {
		prepareForeignImplementationManifest(manifest, container, config);
	}

	private void prepareStructureLayerManifest(StructureLayerManifest manifest,
			IncrementalBuild<?> container, Config config) {

		Hierarchy<ContainerManifest> hierarchy = new HierarchyImpl<>();
		hierarchy.add((ContainerManifest)generate0(ManifestType.CONTAINER_MANIFEST, manifest, config).applyAllAndGet());
		hierarchy.add((StructureManifest)generate0(ManifestType.STRUCTURE_MANIFEST, manifest, config).applyAllAndGet());
		manifest.setContainerHierarchy(hierarchy);

		prepareLayerManifest(manifest, container, config);

		container.addFieldChange(manifest::setFoundationLayerId, "foundationLayer", index("layer"));
		container.addFieldChange(manifest::setBoundaryLayerId, "boundaryLayer", index("layer"));

		for(StructureType structureType : StructureType.values()) {
			container.addNestedManifestChange("structure::"+structureType, ManifestType.STRUCTURE_MANIFEST,
					manifest, config.preprocessor(ManifestType.STRUCTURE_MANIFEST,
							c -> ((StructureManifest)c).setStructureType(structureType))
					.label(structureType.toString()),
					cm -> manifest.getContainerHierarchy().get().add((StructureManifest)cm));
		}

		for(ContainerType containerType : ContainerType.values()) {
			if(containerType==ContainerType.PROXY) {
				continue;
			}
			container.addNestedManifestChange("container::"+containerType, ManifestType.CONTAINER_MANIFEST,
					manifest, config.preprocessor(ManifestType.CONTAINER_MANIFEST,
							c -> ((ContainerManifest)c).setContainerType(containerType))
					.label(containerType.toString()),
					cm -> manifest.getContainerHierarchy().get().add((ContainerManifest)cm));
		}
	}

	private void prepareStructureManifest(StructureManifest manifest,
			IncrementalBuild<?> container, Config config) {

		prepareContainerManifest(manifest, container, config);

		for(StructureFlag flag : StructureFlag.values()) {
			container.addFieldChange(f -> manifest.setStructureFlag(f, true), "structureFlag", flag);
		}
	}

	private void prepareValueManifest(ValueManifest manifest,
			IncrementalBuild<?> container, Config config) {
		manifest.setValue(ManifestTestUtils.getTestValue(manifest.getValueType()));

		prepareIdentity(manifest, container, config);
		prepareDocumentable(manifest, container, config);
	}

	private void prepareValueRange(ValueRange valueRange,
			IncrementalBuild<?> container, Config config) {

		Object[] values = ManifestTestUtils.getTestValues(valueRange.getValueType());

		valueRange.setLowerBound((Comparable<?>) values[0]);

		container.addFieldChange(valueRange::setUpperBound, "upperBound", (Comparable<?>) values[1]);
		container.addFieldChange(valueRange::setStepSize, "stepSize", (Comparable<?>) values[2]);
		container.addFieldChange(valueRange::setLowerBoundInclusive, "lowerBoundInclusive", Boolean.TRUE);
		container.addFieldChange(valueRange::setUpperBoundInclusive, "upperBoundInclusive", Boolean.TRUE);
	}

	private void prepareValueSet(ValueSet valueSet,
			IncrementalBuild<?> container, Config config) {

		Object[] values = ManifestTestUtils.getTestValues(valueSet.getValueType());

		for(Object value : values) {
			valueSet.addValue(value);
		}
	}

	private void prepareVersionManifest(VersionManifest manifest,
			IncrementalBuild<?> container, Config config) {
		// All attributes obligatory
		manifest.setVersionString(index("v1.0."));
		manifest.setFormatId(index("randomFormatType"));
	}

	private static String with(String field, Object obj) {
		requireNonNull(obj);
		String s = (obj instanceof String) ? obj.toString() : obj.getClass().getName();

		return "with "+field+" "+s;
	}

	private static <T extends Enum<T>> String name(String field, T value) {
		return "with "+field+" "+value.getClass().getName()+"@"+value.name();
	}

	public static Config config() {
		return new Config();
	}

	private static class Step {
		final String label;
		final Change change;
		/**
		 * @param label
		 * @param change
		 */
		public Step(String label, Change change) {
			this.label = requireNonNull(label);
			this.change = requireNonNull(change);
		}
	}

	public static final class Config {

		// Configuration
		boolean attributes = true;
		boolean elements = true;
		boolean evals = false;
		int defaultCount = -1;
		Object2IntMap<ManifestType> specificCounts = new Object2IntOpenHashMap<>();
		ValueType valueType = ValueType.INTEGER;
		String label = "<unnamed>";

		Map<ManifestType, List<Consumer<? super TypedManifest>>> preprocessors = new Object2ObjectOpenHashMap<>();
		Map<ManifestType, List<Consumer<? super TypedManifest>>> postprocessors = new Object2ObjectOpenHashMap<>();

		// States
		int index = -1;

		Config() {
			specificCounts.defaultReturnValue(-1);
		}

		public Config mockAttributes(boolean value) {
			attributes = value;
			return this;
		}

		public Config mockElements(boolean value) {
			elements = value;
			return this;
		}

		public Config useEvals(boolean value) {
			evals = value;
			return this;
		}

		public Config defaultElementCount(int value) {
			checkArgument(value>=-1);
			defaultCount = value;
			return this;
		}

		public Config elementCount(ManifestType type, int value) {
			requireNonNull(type);
			checkArgument(value>=-1);
			specificCounts.put(type, value);
			return this;
		}

		public Config ignoreElement(ManifestType type) {
			requireNonNull(type);
			specificCounts.put(type, 0);
			return this;
		}

		int getCount(ManifestType type, int fallback) {
			int count = specificCounts.getInt(type);
			if(count==-1) {
				count = defaultCount;
			}
			if(count==-1) {
				count = fallback;
			}
			return count;
		}

		public Config valueType(ValueType valueType) {
			this.valueType = requireNonNull(valueType);
			return this;
		}

		ValueType getValueType() {
			return valueType;
		}

		public Config label(String label) {
			this.label = requireNonNull(label);
			return this;
		}

		public String getLabel() {
			return label;
		}

		private void addProcessor(Map<ManifestType, List<Consumer<? super TypedManifest>>> map,
				ManifestType type, Consumer<? super TypedManifest> processor) {
			requireNonNull(type);
			requireNonNull(processor);

			map.computeIfAbsent(type, t -> new ArrayList<>()).add(processor);
		}

		private void process(Map<ManifestType, List<Consumer<? super TypedManifest>>> map,
				TypedManifest manifest) {
			map.getOrDefault(manifest.getManifestType(), Collections.emptyList())
				.forEach(p -> p.accept(manifest));
		}

		public Config preprocessor(ManifestType type, Consumer<? super TypedManifest> processor) {
			addProcessor(preprocessors, type, processor);
			return this;
		}

		void preprocess(TypedManifest manifest) {
			process(preprocessors, manifest);
		}

		public Config postprocessor(ManifestType type, Consumer<? super TypedManifest> processor) {
			addProcessor(postprocessors, type, processor);
			return this;
		}

		void postprocess(TypedManifest manifest) {
			process(postprocessors, manifest);
		}

		private void clear(Map<ManifestType, List<Consumer<? super TypedManifest>>> map,
				ManifestType type) {
			map.getOrDefault(type, Collections.emptyList()).clear();
			map.remove(type);
		}

		void clearPreprocessors(ManifestType type) {
			clear(preprocessors, type);
		}

		void clearPostprocessors(ManifestType type) {
			clear(postprocessors, type);
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	@FunctionalInterface
	interface MockPreparation<M extends TypedManifest> {
		void prepare(M instance, IncrementalBuild<?> container, Config config);
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	@FunctionalInterface
	interface Change {

		void apply();
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 * @param <M> manifest type of the internal instance
	 */
	public final class IncrementalBuild<M extends TypedManifest> {

		private final M instance;

		private final Queue<Step> steps = new LinkedList<>();

		private Step lastStep = null;

		private int changeCount = 0;

		IncrementalBuild(M instance) {
			this.instance = requireNonNull(instance);
		}

		public M getInstance() {
			return instance;
		}

		public boolean applyNextChange() {
			lastStep = steps.poll();
			if(lastStep!=null) {
				lastStep.change.apply();
			}
			return lastStep!=null;
		}

		public String currentLabel() {
			return lastStep==null ? "-" : lastStep.label;
		}

		public boolean applyAllChanges() {
			boolean hasMoreChanges = !steps.isEmpty();
			while(!steps.isEmpty()) {
				steps.remove().change.apply();
			}
			return hasMoreChanges;
		}

		M applyAllAndGet() {
			applyAllChanges();
			return getInstance();
		}

		void addChange(String label, Change change) {
			steps.add(new Step(label, change));
			changeCount++;
		}

		<T extends Object> void addValueChange(String label, Consumer<? super T> setter, T value) {
			requireNonNull(setter);
			requireNonNull(value);

			addChange(label, () -> setter.accept(value));
		}

		<T extends Object> void addFieldChange(Consumer<? super T> setter, String field, T value) {
			requireNonNull(setter);
			requireNonNull(value);

			addChange(with(field, value), () -> setter.accept(value));
		}

		<T extends Object> void addLazyFieldChange(Consumer<? super T> setter, String field, Supplier<? extends T> source) {
			requireNonNull(setter);
			requireNonNull(source);

			addChange(with(field, "<delayed>"), () -> setter.accept(source.get()));
		}

		<T extends Enum<T>> void addEnumFieldChange(Consumer<? super T> setter, String field, T value) {
			requireNonNull(setter);
			requireNonNull(value);

			addChange(name(field, value), () -> setter.accept(value));
		}

		/**
		 * {@link ManifestGenerator#generate0(ManifestType, TypedManifest, Config) Generates} a new
		 * manifest, {@link ManifestGenerator#wrap(IncrementalBuild, Consumer) wrapping} its build into
		 * a single {@link Change} by applying the given {@code setter}. Finally adds the new change as
		 * as step using a label based on the given {@link ManifestType type}.
		 * @param type
		 * @param host
		 * @param config
		 * @param setter
		 */
		<M_sub extends TypedManifest> void addNestedManifestChange(String field, ManifestType type, TypedManifest host,
				Config config, Consumer<M_sub> setter) {
			addChange(name(field, type), wrap(generate0(type, host, config), setter));
		}

		<M_sub extends TypedManifest> void addManifestChange(String field, ManifestType type,
				Config config, Consumer<M_sub> setter) {
			addChange(name(field, type), wrap(generate0(type, config), setter));
		}

		public int getChangeCount() {
			return changeCount;
		}
	}
}
