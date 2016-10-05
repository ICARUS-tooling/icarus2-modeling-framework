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
package de.ims.icarus2.model.api.registry;

import static de.ims.icarus2.util.Conditions.checkNotNull;
import static de.ims.icarus2.util.strings.StringUtil.getName;

import java.util.List;
import java.util.Set;
import java.util.Stack;

import de.ims.icarus2.ErrorCode;
import de.ims.icarus2.Report;
import de.ims.icarus2.Report.ReportItem;
import de.ims.icarus2.ReportBuilder;
import de.ims.icarus2.model.manifest.api.AnnotationFlag;
import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ContainerManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest.TargetLayerManifest;
import de.ims.icarus2.model.manifest.api.LocationManifest;
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.api.MemberManifest;
import de.ims.icarus2.model.manifest.api.OptionsManifest;
import de.ims.icarus2.model.manifest.api.OptionsManifest.Option;
import de.ims.icarus2.model.manifest.api.PathResolverManifest;
import de.ims.icarus2.model.manifest.api.StructureLayerManifest;
import de.ims.icarus2.model.manifest.types.ValueType;

/**
 * @author Markus Gärtner
 *
 */
public class CorpusVerifier {

	private final CorpusManifest corpusManifest;
	private final ReportBuilder<ReportItem> reportBuilder;

	private Stack<MemberManifest> stack = new Stack<>();

	private CorpusVerifier(CorpusManifest corpusManifest, ReportBuilder<ReportItem> reportBuilder) {
		checkNotNull(corpusManifest);
		checkNotNull(reportBuilder);

		this.corpusManifest = corpusManifest;
		this.reportBuilder = reportBuilder;
	}

	public static Report<ReportItem> checkManifest(CorpusManifest corpusManifest) {
		return checkManifest(corpusManifest, ReportBuilder.newBuilder(corpusManifest)).build();
	}

	public static ReportBuilder<ReportItem> checkManifest(CorpusManifest corpusManifest, ReportBuilder<ReportItem> reportBuilder) {
		CorpusVerifier verifier = new CorpusVerifier(corpusManifest, reportBuilder);

		verifier.check();

		return reportBuilder;
	}

	private void check() {

	}

	private void push(MemberManifest manifest) {
		stack.push(manifest);
	}

	private void pop() {
		stack.pop();
	}

	private void checkManifest0(MemberManifest manifest) {
		if(manifest.getId()==null) {
			error(ManifestErrorCode.MANIFEST_CORRUPTED_STATE, "Missing id"); //$NON-NLS-1$
		}

		if(manifest.isTemplate()) {
			error(ManifestErrorCode.MANIFEST_CORRUPTED_STATE, "Illegal template declaration"); //$NON-NLS-1$
		}

		// Verify options/properties
		OptionsManifest optionsManifest = manifest.getOptionsManifest();
		Set<String> properties = manifest.getPropertyNames();
		Set<String> options = optionsManifest==null ? null : optionsManifest.getOptionIds();
		for(String property : properties) {
			if(optionsManifest==null) {
				// Check that options manifest is defined
				error(ManifestErrorCode.MANIFEST_CORRUPTED_STATE, "No options manifest present (property: "+property+")"); //$NON-NLS-1$ //$NON-NLS-2$
			} else if(!options.contains(property)) {
				// Check that property is defined as an option
				error(ManifestErrorCode.MANIFEST_CORRUPTED_STATE, "Missing options declaration in manifest for property:"+property); //$NON-NLS-1$
			} else {
				// Check for correct value type
				Option option = optionsManifest.getOption(property);
				Object value = manifest.getProperty(property);
				ValueType valueType = option.getValueType();

				if(valueType==null) {
					error(ManifestErrorCode.MANIFEST_CORRUPTED_STATE, "Missing value type declaration in options manifest for property: "+property); //$NON-NLS-1$
				} else if(!valueType.isValidValue(value)) {
					error(ManifestErrorCode.MANIFEST_CORRUPTED_STATE, "Incompatible property value '"+value+"' for property "+property+" - Expected "+valueType.getStringValue()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
		}
	}

	private void checkPathResolver(PathResolverManifest manifest) {
		push(manifest);

		checkType(manifest, ManifestType.PATH_RESOLVER_MANIFEST);
		checkManifest0(manifest);

		pop();
	}

	private void checkContext(ContextManifest manifest) {
		push(manifest);

		checkType(manifest, ManifestType.CONTEXT_MANIFEST);
		checkManifest0(manifest);

		if(manifest.getCorpusManifest()!=corpusManifest) {
			error(ManifestErrorCode.MANIFEST_CORRUPTED_STATE, "Corrupted hierarchy - foreign corpus manifest ancestor"); //$NON-NLS-1$
		}

		// Check location
		List<LocationManifest> locationManifests = manifest.getLocationManifests();
		if(locationManifests==null || locationManifests.isEmpty()) {
			error(ManifestErrorCode.MANIFEST_CORRUPTED_STATE, "Missing location declarations list"); //$NON-NLS-1$
		} else {
			for(LocationManifest locationManifest : locationManifests) {

				if(locationManifest==null) {
					error(ManifestErrorCode.MANIFEST_CORRUPTED_STATE, "List of locations contains null manifest"); //$NON-NLS-1$
				} else {
					if(locationManifest.getRootPath()==null) {
						error(ManifestErrorCode.MANIFEST_CORRUPTED_STATE, "Missing location path declaration: "+getName(locationManifest)); //$NON-NLS-1$
					}

					PathResolverManifest pathResolverManifest = locationManifest.getPathResolverManifest();
					if(pathResolverManifest!=null) {
						checkPathResolver(pathResolverManifest);
					}
				}
			}
		}

		// Check layers
		for(LayerManifest layerManifest : manifest.getLayerManifests()) {
			checkLayer(layerManifest, manifest);
		}

		pop();
	}

	private void checkLayer(LayerManifest manifest, ContextManifest contextManifest) {
		push(manifest);

		checkManifest0(manifest);

		// Check integrity
		if(manifest.getContextManifest()!=contextManifest) {
			error(ManifestErrorCode.MANIFEST_CORRUPTED_STATE, "Corrupted hierarchy - foreign context manifest ancestor"); //$NON-NLS-1$
		}

		//TODO this part needs some fixing to bring it in line with the layer linking contract
		for(TargetLayerManifest baseLayer : manifest.getBaseLayerManifests()) {
			ItemLayerManifest baseLayerManifest = (ItemLayerManifest) baseLayer.getResolvedLayerManifest();
			if(baseLayerManifest!=null) {
				CorpusManifest root = getRoot(baseLayerManifest);
				if(root!=corpusManifest) {
					error(ManifestErrorCode.MANIFEST_CORRUPTED_STATE, "Corrupted hierarchy - foreign corpus manifest ancestor for base layer"); //$NON-NLS-1$
				}
			}
		}

		switch (manifest.getManifestType()) {
		case ANNOTATION_LAYER_MANIFEST: {
			checkAnnotationLayer((AnnotationLayerManifest) manifest);
		} break;

		case ITEM_LAYER_MANIFEST: {
			checkItemLayer((ItemLayerManifest) manifest);
		} break;

		case STRUCTURE_LAYER_MANIFEST: {
			checkStructureLayer((StructureLayerManifest) manifest);
		} break;

		default:
			break;
		}

		pop();
	}

	private void checkAnnotationLayer(AnnotationLayerManifest manifest) {
		Set<String> keys = manifest.getAvailableKeys();
		String defaultKey = manifest.getDefaultKey();

		if(keys.isEmpty() && defaultKey==null && !manifest.isAnnotationFlagSet(AnnotationFlag.UNKNOWN_KEYS)) {
			error(ManifestErrorCode.MANIFEST_CORRUPTED_STATE, "No annotation manifests defined for bounded annotation layer. "
					+ "Need at least a default annotation when no foreign keys are allowed");
		}

		AnnotationManifest defaultAnnotationManifest = defaultKey==null ? null : manifest.getAnnotationManifest(defaultKey);

		if(defaultAnnotationManifest!=null) {
			checkAnnotation(defaultAnnotationManifest);
		}

		for(String key : keys) {

			// Skip optional default key since we had to check it separately anyway
			if(key.equals(defaultKey)) {
				continue;
			}
			checkAnnotation(manifest.getAnnotationManifest(key));
		}
	}

	private void checkAnnotation(AnnotationManifest manifest) {
		push(manifest);

		checkType(manifest, ManifestType.ANNOTATION_MANIFEST);
		checkManifest0(manifest);

		pop();
	}

	private void checkItemLayer(ItemLayerManifest manifest) {
		ContainerManifest rootContainerManifest = manifest.getRootContainerManifest();
		if(rootContainerManifest==null) {
			error(ManifestErrorCode.MANIFEST_CORRUPTED_STATE, "Missing root container manifest");
		} else {
			chechContainer(rootContainerManifest, manifest);
		}

		ContainerManifest parent = rootContainerManifest;
		for(int i=0; i<manifest.getContainerDepth(); i++) {
			ContainerManifest containerManifest = manifest.getContainerManifest(i);

			if(containerManifest.getParentManifest()!=parent) {
				error(ManifestErrorCode.MANIFEST_CORRUPTED_STATE, "Corrupted hierarchy - foreign parent container at level "+i); //$NON-NLS-1$
			}

			chechContainer(containerManifest, manifest);

			parent = containerManifest;
		}
	}

	private void chechContainer(ContainerManifest manifest, ItemLayerManifest itemLayerManifest) {

	}

	private void checkStructureLayer(StructureLayerManifest manifest) {

	}

	private void checkType(MemberManifest manifest, ManifestType type) {

		if(manifest.getManifestType()==null) {
			error(ManifestErrorCode.MANIFEST_MISSING_TYPE, "Missing manifest type"); //$NON-NLS-1$
		} else if(manifest.getManifestType()!=type) {
			error(ManifestErrorCode.MANIFEST_TYPE_CAST, "Type mismatch in manifest - expected "+type+", got "+manifest.getManifestType()); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private CorpusManifest getRoot(LayerManifest manifest) {
		return manifest.getContextManifest().getCorpusManifest();
	}

	private CorpusManifest getRoot(ContainerManifest manifest) {
		return getRoot(manifest.getLayerManifest());
	}

	private String trace(String msg) {
		if(stack.isEmpty()) {
			return ""; //$NON-NLS-1$
		}

		StringBuilder sb = new StringBuilder();

		for(int i=stack.size()-1; i>-1; i++) {
			String id = stack.get(i).getId();
			if(id==null) {
				id = "<unknown>"; //$NON-NLS-1$
			}

			if(i>0) {
				sb.append('.');
			}

			sb.append(id);
		}

		if(sb.length()>0) {
			sb.append(": "); //$NON-NLS-1$
		}

		sb.append(msg);

		return sb.toString();
	}

	private void info(String msg, Object... data) {
		reportBuilder.addInfo(trace(msg), data);
	}

	private void warning(ErrorCode code, String msg, Object...data) {
		reportBuilder.addWarning(code, trace(msg), data);
	}

	private void error(ErrorCode code, String msg, Object...data) {
		reportBuilder.addError(code, trace(msg), data);
	}
}
