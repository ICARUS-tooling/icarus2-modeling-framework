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
package de.ims.icarus2.model.manifest.standard.resolve;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static java.util.Objects.requireNonNull;

import javax.swing.Icon;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest.PrerequisiteManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.Documentable;
import de.ims.icarus2.model.manifest.api.Documentation;
import de.ims.icarus2.model.manifest.api.FragmentLayerManifest;
import de.ims.icarus2.model.manifest.api.ImplementationManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerGroupManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerType;
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
import de.ims.icarus2.model.manifest.api.ValueRange;
import de.ims.icarus2.model.manifest.api.ValueSet;
import de.ims.icarus2.model.manifest.api.VersionManifest;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.util.data.ContentType;
import de.ims.icarus2.util.id.Identity;

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

		VersionManifest versionManifest = source.getVersionManifest();
		if(versionManifest!=null) {
			target.setVersionManifest(versionManifest);
		}
	}

	protected void copyIdentityFields(Identity source, ModifiableIdentity target) {
		if(source==null || target==null) {
			return;
		}

		Icon icon = source.getIcon();
		if(icon!=null) {
			target.setIcon(icon);
		}

		String id = source.getId();
		if(id!=null) {
			target.setId(id);
		}

		String name = source.getName();
		if(name!=null) {
			target.setName(name);
		}

		String description = source.getDescription();
		if(description!=null) {
			target.setDescription(description);
		}
	}

	protected void copyDocumentableFields(Documentable source, Documentable target) {
		if(source==null || target==null) {
			return;
		}

		Documentation documentation = source.getDocumentation();
		if(documentation!=null) {
			target.setDocumentation(documentation);
		}
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
	protected void copyMemberFields(MemberManifest source, MemberManifest target) {
		if(source==null || target==null) {
			return;
		}

		copyManifestFields(source, target);
		copyIdentityFields(source, target);
		copyDocumentableFields(source, target);

		OptionsManifest optionsManifest = source.getOptionsManifest();
		if(optionsManifest!=null) {
			target.setOptionsManifest(optionsManifest);
		}

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

		target.setSourceType(source.getSourceType());
		target.setSource(source.getSource());
		target.setClassname(source.getClassname());
		target.setUseFactory(source.isUseFactory());
	}

	protected void copyLayerGroupFields(LayerGroupManifest source, LayerGroupManifest target) {
		if(source==null || target==null) {
			return;
		}

		copyIdentityFields(source, target);

		target.setIndependent(source.isIndependent());

		ItemLayerManifest primaryLayerManifest = source.getPrimaryLayerManifest();
		if(primaryLayerManifest!=null) {
			target.setPrimaryLayerId(primaryLayerManifest.getId());
		}
	}

	protected void copyLayerFields(LayerManifest source, LayerManifest target) {
		if(source==null || target==null) {
			return;
		}

		copyMemberFields(source, target);

		LayerType layerType = source.getLayerType();
		if(layerType!=null) {
			target.setLayerTypeId(layerType.getId());
		}

		source.forEachBaseLayerManifest(b -> target.addBaseLayerId(b.getLayerId()));
	}

	protected void copyItemLayerFields(ItemLayerManifest source, ItemLayerManifest target) {
		if(source==null || target==null) {
			return;
		}

		copyLayerFields(source, target);

		TargetLayerManifest boundaryLayerManifest = source.getBoundaryLayerManifest();
		if(boundaryLayerManifest!=null) {
			target.setBoundaryLayerId(boundaryLayerManifest.getLayerId());
		}

		TargetLayerManifest foundationLayerManifest = source.getFoundationLayerManifest();
		if(foundationLayerManifest!=null) {
			target.setFoundationLayerId(foundationLayerManifest.getLayerId());
		}
	}

	protected void copyFragmentLayerFields(FragmentLayerManifest source, FragmentLayerManifest target) {
		if(source==null || target==null) {
			return;
		}

		copyItemLayerFields(source, target);

		TargetLayerManifest valueManifest = source.getValueLayerManifest();
		if(valueManifest!=null) {
			target.setValueLayerId(valueManifest.getLayerId());
		}

		String annotationKey = source.getAnnotationKey();
		if(annotationKey!=null) {
			target.setAnnotationKey(annotationKey);
		}
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

		target.setKey(source.getKey());

		source.forEachAlias(target::addAlias);

		ValueType valueType = source.getValueType();
		if(valueType!=null) {
			target.setValueType(valueType);
		}

		Object noEntryValue = source.getNoEntryValue();
		if(noEntryValue!=null) {
			target.setNoEntryValue(noEntryValue);
		}

		ContentType contentType = source.getContentType();
		if(contentType!=null) {
			target.setContentType(contentType);
		}

		ValueRange valueRange = source.getValueRange();
		if(valueRange!=null) {
			target.setValueRange(valueRange);
		}

		ValueSet valueSet = source.getValueSet();
		if(valueSet!=null) {
			target.setValueSet(valueSet);
		}
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
		String defaultKey = source.getDefaultKey();
		if(defaultKey!=null) {
			target.setDefaultKey(defaultKey);
		}
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

		String contextId = source.getContextId();
		if(contextId!=null) {
			target.setContextId(contextId);
		}

		String description = source.getDescription();
		if(description!=null) {
			target.setDescription(description);
		}

		String layerId = source.getLayerId();
		if(layerId!=null) {
			target.setLayerId(layerId);
		}

		String typeId = source.getTypeId();
		if(typeId!=null) {
			target.setTypeId(typeId);
		}
	}

	public ImplementationManifest cloneImplementationManifest(ImplementationManifest source, MemberManifest host) {
		ImplementationManifest target = manifestFactory.create(ManifestType.IMPLEMENTATION_MANIFEST, host, null);

		copyImplementationFields(source, target);

		return target;
	}

	public LayerGroupManifest cloneLayerGroupManifest(LayerGroupManifest source, ContextManifest contextManifest) {
		LayerGroupManifest target = manifestFactory.create(ManifestType.LAYER_GROUP_MANIFEST, contextManifest, null);

		copyLayerGroupFields(source, target);

		for(LayerManifest layerManifest : source.getLayerManifests()) {
			target.addLayerManifest(cloneLayerManifest(layerManifest, target));
		}

		return target;
	}

	public LayerManifest cloneLayerManifest(LayerManifest source, LayerGroupManifest layerGroupManifest) {
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

		// Containers
		for(int level = 0 ; level <source.getContainerDepth(); level++) {
			target.addContainerManifest(cloneContainerManifest(source.getContainerManifest(level), target));
		}

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

		// Containers AND structures
		for(int level = 0 ; level <source.getContainerDepth(); level++) {
			ContainerManifest containerManifest = source.getContainerManifest(level);

			if(containerManifest.getManifestType()==ManifestType.STRUCTURE_MANIFEST) {
				target.addStructureManifest(cloneStructureManifest((StructureManifest) containerManifest, target));
			} else {
				target.addContainerManifest(cloneContainerManifest(containerManifest, target));
			}
		}

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

		RasterizerManifest rasterizerManifest = source.getRasterizerManifest();
		if(rasterizerManifest!=null) {
			target.setRasterizerManifest(cloneRasterizerManifest(rasterizerManifest, target));
		}

		return target;
	}

	public RasterizerManifest cloneRasterizerManifest(RasterizerManifest source, FragmentLayerManifest fragmentLayerManifest) {
		RasterizerManifest target = manifestFactory.create(ManifestType.RASTERIZER_MANIFEST, fragmentLayerManifest, null);

		copyRasterizerFields(source, target);

		ImplementationManifest implementationManifest = source.getImplementationManifest();
		if(implementationManifest!=null) {
			target.setImplementationManifest(implementationManifest);
		}

		return target;
	}

	public ContextManifest cloneContextManifest(ContextManifest source, CorpusManifest corpusManifest) {
		ContextManifest target = manifestFactory.create(ManifestType.CONTEXT_MANIFEST, corpusManifest, null);

		copyContextFields(source, target);

		source.forEachGroupManifest(g -> target.addLayerGroup(cloneLayerGroupManifest(g, target)));

		source.forEachPrerequisite(p -> {
			PrerequisiteManifest pNew = target.addPrerequisite(p.getAlias());
			copyPrerequisiteFields(p, pNew);
		});

		return target;
	}
}
