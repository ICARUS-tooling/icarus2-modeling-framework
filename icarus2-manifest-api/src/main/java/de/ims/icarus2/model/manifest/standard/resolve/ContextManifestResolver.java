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
package de.ims.icarus2.model.manifest.standard.resolve;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Optional;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.Documentable;
import de.ims.icarus2.model.manifest.api.FragmentLayerManifest;
import de.ims.icarus2.model.manifest.api.Hierarchy;
import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.Manifest;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestFactory;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.manifest.api.MemberManifest.Property;
import de.ims.icarus2.model.manifest.api.ModifiableIdentity;
import de.ims.icarus2.model.manifest.api.OptionsManifest;
import de.ims.icarus2.model.manifest.api.RasterizerManifest;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.model.manifest.api.StructureManifest;
import de.ims.icarus2.model.manifest.api.ValueManifest;
import de.ims.icarus2.model.manifest.standard.ItemLayerManifestImpl;
import de.ims.icarus2.util.id.Identifiable;
import de.ims.icarus2.util.id.Identity;
import de.ims.icarus2.util.id.MutableIdentifiable;
import de.ims.icarus2.util.id.StaticIdentity;

/**
 * Utility class used to create <i>live</i> context manifests from templates.
 * Note that a {@link CorpusManifest corpus manifest} can only contain non-template
 * (i.e. <i>live</i>) contexts.
 * <p>
 * To provide proper linking of contained components, a <i>live</i> context is a complete
 * {@code deep clone} of a given context {@link Manifest#isTemplate() template}. The deep
 * cloning strategy works as follows:
 *
 * <ol>
 * <li>All manifests that contain an {@code uplink} such as {@link LayerManifest#getGroupManifest()} will be deep cloned with the actual new instance of their former host as environment</li>
 * <li>All templated manifests will have their template link removed and all derived content will be deep cloned and embedded into the new manifest</li>
 * <li>Each new manifest will have as its {@link Manifest#getId() identifier} assigned either the id of its former instance or the id of the referenced template, if present</li>
 * <li>Isolated manifests such as an {@link OptionsManifest} will be copied <i>as is</i>, meaning they preserve template references (this also will keep referenced template manifests {@link CorpusRegistry#isLocked(Manifest) locked})</li>
 * <li>Customizable isolated manifests such as an {@link AnnotationManifest} will be shallowly cloned, meaning that the new instance will only deep clone local data and just link to templates, if present</li>
 * <li>Non-manifest content (i.e. all complex objects not deriving from {@link Manifest}) such as a {@link ValueManifest} will be referenced <i>as is</i></li>
 * </ol>
 *
 * Note that this way a <i>live</i> context manifest (or any other manifest contained in it,
 * for that matter) will be decoupled from all template references.
 *
 * @author Markus Gärtner
 *
 */
public class ContextManifestResolver {

	private final ManifestFactory manifestFactory;

	public ContextManifestResolver(ManifestFactory manifestFactory) {
		requireNonNull(manifestFactory);

		this.manifestFactory = manifestFactory;
	}

	public ContextManifest toLiveContext(ContextManifest template, CorpusManifest host) {
		requireNonNull(template);
		requireNonNull(host);
		checkArgument("Provided context manifest must be a template", template.isTemplate());

		return cloneContextManifest(template, host);
	}

	protected void copyManifestFields(Manifest source, Manifest target) {
		if(source==null || target==null) {
			return;
		}

		source.getVersionManifest().ifPresent(target::setVersionManifest);
	}

	protected void copyIdentityFields(Identity source, ModifiableIdentity target) {
		if(source==null || target==null) {
			return;
		}

		source.getIcon().ifPresent(target::setIcon);
		source.getId().ifPresent(target::setId);
		source.getName().ifPresent(target::setName);
		source.getDescription().ifPresent(target::setDescription);
	}

	protected void copyIdentity(Identifiable source, MutableIdentifiable target) {
		if(source==null || target==null) {
			return;
		}

		Identity identity = source.getIdentity();
		if(identity!=null) {
			target.setIdentity(new StaticIdentity(identity));
		}
	}

	protected void copyDocumentableFields(Documentable<?> source, Documentable<?> target) {
		if(source==null || target==null) {
			return;
		}

		source.getDocumentation().ifPresent(target::setDocumentation);
	}

	/**
	 * Copies the {@link OptionsManifest} and all {@link MemberManifest#getProperties() properties}
	 * from {@code source} over to {@code target}. All property objects will be {@link Property#clone() cloned}!
	 * <p>
	 * Calls {@link #copyIdentityFields(Identity, ModifiableIdentity)} with the given arguments.
	 *
	 * @param source
	 * @param target
	 */
	protected void copyMemberFields(MemberManifest<?> source, MemberManifest<?> target) {
		if(source==null || target==null) {
			return;
		}

		copyManifestFields(source, target);
		copyIdentityFields(source, target);
		copyDocumentableFields(source, target);

		source.getOptionsManifest().ifPresent(target::setOptionsManifest);

		for(Property property : source.getProperties()) {
			// Actually the only place in the copy routines that uses regular cloning, but only since
			// Property implementations are already required to support cloning themselves!
			target.addProperty(property.clone());
		}
	}

	protected void copyImplementationFields(ImplementationManifest source, ImplementationManifest target) {
		if(source==null || target==null) {
			return;
		}

		copyMemberFields(source, target);

		source.getSourceType().ifPresent(target::setSourceType);
		target.setUseFactory(source.isUseFactory());
		source.getClassname().ifPresent(target::setClassname);
		source.getSource().ifPresent(target::setSource);
	}

	protected void copyLayerGroupFields(LayerGroupManifest source, LayerGroupManifest target) {
		if(source==null || target==null) {
			return;
		}

		copyIdentityFields(source, target);

		target.setIndependent(source.isIndependent());

		source.getPrimaryLayerManifest().ifPresent(m -> target.setPrimaryLayerId(
				m.getId().orElseThrow(Manifest.invalidId(
				"Primary layer does not declare valid identifier"))));
	}

	protected void copyLayerFields(LayerManifest<?> source, LayerManifest<?> target) {
		if(source==null || target==null) {
			return;
		}

		copyMemberFields(source, target);

		source.getLayerType().ifPresent(t -> target.setLayerTypeId(t.getId().orElseThrow(Manifest.invalidId(
				"Layer type does not declare valid identifier"))));

		source.forEachBaseLayerManifest(b -> target.addAndGetBaseLayer(b.getLayerId()));
	}

	protected void copyItemLayerFields(ItemLayerManifest source, ItemLayerManifest target) {
		if(source==null || target==null) {
			return;
		}

		copyLayerFields(source, target);

		source.getBoundaryLayerManifest().ifPresent(m -> target.setAndGetBoundaryLayer(m.getLayerId()));
		source.getFoundationLayerManifest().ifPresent(m -> target.setAndGetFoundationLayer(m.getLayerId()));

		// Containers
		Optional<Hierarchy<ContainerManifest>> containerHierarchy = source.getContainerHierarchy();
		if(containerHierarchy.isPresent()) {
			for(ContainerManifest containerManifest : containerHierarchy.get()) {
				ContainerManifest clonedManifest;
				if(containerManifest.getManifestType()==ManifestType.STRUCTURE_MANIFEST) {
					clonedManifest = cloneStructureManifest((StructureManifest) containerManifest,
							(StructureLayerManifest) target);
				} else {
					clonedManifest = cloneContainerManifest(containerManifest, target);
				}

				ItemLayerManifestImpl.getOrCreateLocalContainerhierarchy(target).add(clonedManifest);
			}
		}
	}

	protected void copyFragmentLayerFields(FragmentLayerManifest source, FragmentLayerManifest target) {
		if(source==null || target==null) {
			return;
		}

		copyItemLayerFields(source, target);

		source.getValueLayerManifest().ifPresent(m -> target.setAndGetValueLayer(m.getLayerId()));
		source.getAnnotationKey().ifPresent(target::setAnnotationKey);
	}

	protected void copyContainerFields(ContainerManifest source, ContainerManifest target) {
		if(source==null || target==null) {
			return;
		}

		copyMemberFields(source, target);

		target.setContainerType(source.getContainerType());

		source.forEachActiveContainerFlag(f -> target.setContainerFlag(f, true));
	}

	protected void copyStructureFields(StructureManifest source, StructureManifest target) {
		if(source==null || target==null) {
			return;
		}

		copyContainerFields(source, target);

		target.setStructureType(source.getStructureType());

		source.forEachActiveStructureFlag(f -> target.setStructureFlag(f, true));
	}

	protected void copyAnnotationFields(AnnotationManifest source, AnnotationManifest target) {
		if(source==null || target==null) {
			return;
		}

		copyMemberFields(source, target);

		source.getKey().ifPresent(target::setKey);

		source.forEachAlias(target::addAlias);

		Optional.ofNullable(source.getValueType()).ifPresent(target::setValueType);

		source.getNoEntryValue().ifPresent(target::setNoEntryValue);
		source.getContentType().ifPresent(target::setContentType);
		source.getValueRange().ifPresent(target::setValueRange);
		source.getValueSet().ifPresent(target::setValueSet);
	}

	protected void copyAnnotationLayerFields(AnnotationLayerManifest source, AnnotationLayerManifest target) {
		if(source==null || target==null) {
			return;
		}

		// Generic layer fields
		copyLayerFields(source, target);

		// Flags
		source.forEachActiveAnnotationFlag(f -> target.setAnnotationFlag(f, true));

		// Default key
		source.getDefaultKey().ifPresent(target::setDefaultKey);
	}

	protected void copyRasterizerFields(RasterizerManifest source, RasterizerManifest target) {
		if(source==null || target==null) {
			return;
		}

		copyMemberFields(source, target);
	}

	protected void copyContextFields(ContextManifest source, ContextManifest target) {
		if(source==null || target==null) {
			return;
		}

		copyMemberFields(source, target);
	}

	protected void copyPrerequisiteFields(PrerequisiteManifest source, PrerequisiteManifest target) {
		if(source==null || target==null) {
			return;
		}

		source.getContextId().ifPresent(target::setContextId);
		source.getDescription().ifPresent(target::setDescription);
		source.getLayerId().ifPresent(target::setLayerId);
		source.getTypeId().ifPresent(target::setTypeId);
	}

	public ImplementationManifest cloneImplementationManifest(ImplementationManifest source, MemberManifest<?> host) {
		ImplementationManifest target = manifestFactory.create(ManifestType.IMPLEMENTATION_MANIFEST, host, null);

		copyImplementationFields(source, target);

		return target;
	}

	public LayerGroupManifest cloneLayerGroupManifest(LayerGroupManifest source, ContextManifest contextManifest) {
		LayerGroupManifest target = manifestFactory.create(ManifestType.LAYER_GROUP_MANIFEST, contextManifest, null);

		copyLayerGroupFields(source, target);

		for(LayerManifest<?> layerManifest : source.getLayerManifests()) {
			target.addLayerManifest(cloneLayerManifest(layerManifest, target));
		}

		return target;
	}

	public LayerManifest<?> cloneLayerManifest(LayerManifest<?> source, LayerGroupManifest layerGroupManifest) {
		switch (source.getManifestType()) {
		case ANNOTATION_LAYER_MANIFEST: return cloneAnnotationLayerManifest((AnnotationLayerManifest) source, layerGroupManifest);
		case ITEM_LAYER_MANIFEST: return cloneItemLayerManifest((ItemLayerManifest) source, layerGroupManifest);
		case STRUCTURE_LAYER_MANIFEST: return cloneStructureLayerManifest((StructureLayerManifest) source, layerGroupManifest);
		case FRAGMENT_LAYER_MANIFEST: return cloneFragmentLayerManifest((FragmentLayerManifest) source, layerGroupManifest);

		//TODO remaining layer types

		default:
			throw new ManifestException(GlobalErrorCode.NOT_IMPLEMENTED,
					"Cloning not implemented for type: "+source.getManifestType());
		}
	}

	public AnnotationLayerManifest cloneAnnotationLayerManifest(AnnotationLayerManifest source, LayerGroupManifest layerGroupManifest) {
		AnnotationLayerManifest target = manifestFactory.create(ManifestType.ANNOTATION_LAYER_MANIFEST, layerGroupManifest, null);

		copyAnnotationLayerFields(source, target);

		// Annotation manifests
		source.forEachAnnotationManifest(m -> target.addAnnotationManifest(cloneAnnotationManifest(m)));

		return target;
	}

	public AnnotationManifest cloneAnnotationManifest(AnnotationManifest source) {
		AnnotationManifest target = manifestFactory.create(ManifestType.ANNOTATION_MANIFEST);

		copyAnnotationFields(source, target);

		return target;
	}

	public ItemLayerManifest cloneItemLayerManifest(ItemLayerManifest source, LayerGroupManifest layerGroupManifest) {
		ItemLayerManifest target = manifestFactory.create(ManifestType.ITEM_LAYER_MANIFEST, layerGroupManifest, null);

		// Generic item layer fields
		copyItemLayerFields(source, target);

		return target;
	}

	public ContainerManifest cloneContainerManifest(ContainerManifest source, ItemLayerManifest itemLayerManifest) {
		ContainerManifest target = manifestFactory.create(ManifestType.CONTAINER_MANIFEST, itemLayerManifest, null);

		copyContainerFields(source, target);

		return target;
	}

	public StructureLayerManifest cloneStructureLayerManifest(StructureLayerManifest source, LayerGroupManifest layerGroupManifest) {
		StructureLayerManifest target = manifestFactory.create(ManifestType.STRUCTURE_LAYER_MANIFEST, layerGroupManifest, null);

		copyItemLayerFields(source, target);

		return target;
	}

	public StructureManifest cloneStructureManifest(StructureManifest source, StructureLayerManifest structureLayerManifest) {
		StructureManifest target = manifestFactory.create(ManifestType.STRUCTURE_MANIFEST, structureLayerManifest, null);

		copyStructureFields(source, target);

		return target;
	}

	public FragmentLayerManifest cloneFragmentLayerManifest(FragmentLayerManifest source, LayerGroupManifest layerGroupManifest) {
		FragmentLayerManifest target = manifestFactory.create(ManifestType.FRAGMENT_LAYER_MANIFEST, layerGroupManifest, null);

		copyFragmentLayerFields(source, target);

		source.getRasterizerManifest().ifPresent(
				m -> target.setRasterizerManifest(cloneRasterizerManifest(m, target)));

		return target;
	}

	public RasterizerManifest cloneRasterizerManifest(RasterizerManifest source, FragmentLayerManifest fragmentLayerManifest) {
		RasterizerManifest target = manifestFactory.create(ManifestType.RASTERIZER_MANIFEST, fragmentLayerManifest, null);

		copyRasterizerFields(source, target);

		source.getImplementationManifest().ifPresent(target::setImplementationManifest);

		return target;
	}

	public ContextManifest cloneContextManifest(ContextManifest source, CorpusManifest corpusManifest) {
		ContextManifest target = manifestFactory.create(ManifestType.CONTEXT_MANIFEST, corpusManifest, null);

		copyContextFields(source, target);

		source.forEachGroupManifest(g -> target.addLayerGroup(cloneLayerGroupManifest(g, target)));

		source.forEachPrerequisite(p -> {
			PrerequisiteManifest pNew = target.addAndGetPrerequisite(p.getAlias());
			copyPrerequisiteFields(p, pNew);
		});

		return target;
	}
}
