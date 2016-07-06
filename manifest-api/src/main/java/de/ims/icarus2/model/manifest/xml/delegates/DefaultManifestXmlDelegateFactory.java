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
package de.ims.icarus2.model.manifest.xml.delegates;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.manifest.api.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestType;
import de.ims.icarus2.model.manifest.xml.ManifestXmlDelegate;
import de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateFactory;
import de.ims.icarus2.model.manifest.xml.delegates.DriverManifestXmlDelegate.ModuleManifestXmlDelegate;
import de.ims.icarus2.model.manifest.xml.delegates.DriverManifestXmlDelegate.ModuleSpecXmlDelegate;

/**
 * @author Markus Gärtner
 *
 */
public class DefaultManifestXmlDelegateFactory implements ManifestXmlDelegateFactory {

	/**
	 * Returns the matching {@link ManifestXmlDelegate} implementation available for the given type.
	 * <p>
	 * If the type provided is not supported a {@link ModelException} will be thrown.
	 *
	 * @param type
	 * @return
	 * @throws ModelException in case no matching delegate implementation could be found.
	 */
	@SuppressWarnings("unchecked")
	public static Class<ManifestXmlDelegate<?>> getDelegateClass(ManifestType type) {
		Class<?> result = null;

		switch (type) {
		case ANNOTATION_LAYER_MANIFEST: result = AnnotationLayerManifestXmlDelegate.class; break;
		case ANNOTATION_MANIFEST: result = AnnotationManifestXmlDelegate.class; break;
		case CONTAINER_MANIFEST: result = ContainerManifestXmlDelegate.class; break;
		case CONTEXT_MANIFEST: result = ContextManifestXmlDelegate.class; break;
		case CORPUS_MANIFEST: result = CorpusManifestXmlDelegate.class; break;
		case DRIVER_MANIFEST: result = DriverManifestXmlDelegate.class; break;
		case FRAGMENT_LAYER_MANIFEST: result = FragmentLayerManifestXmlDelegate.class; break;
		case HIGHLIGHT_LAYER_MANIFEST: result = HighlightLayerManifestXmlDelegate.class; break;
		case IMPLEMENTATION_MANIFEST: result = ImplementationManifestXmlDelegate.class; break;
		case ITEM_LAYER_MANIFEST: result = ItemLayerManifestXmlDelegate.class; break;
		case LAYER_GROUP_MANIFEST: result = LayerGroupManifestXmlHandler.class; break;
		case LOCATION_MANIFEST: result = LocationManifestXmlDelegate.class; break;
		case OPTIONS_MANIFEST: result = OptionsManifestXmlDelegate.class; break;
		case PATH_RESOLVER_MANIFEST: result = PathResolverManifestXmlDelegate.class; break;
		case RASTERIZER_MANIFEST: result = RasterizerManifestXmLDelegate.class; break;
		case STRUCTURE_LAYER_MANIFEST: result = StructureLayerManifestXmlDelegate.class; break;
		case STRUCTURE_MANIFEST: result = StructureManifestXmlDelegate.class; break;

		case DOCUMENTATION: result = DocumentationXmlDelegate.class; break;
		case VALUE_MANIFEST: result = ValueManifestXmlDelegate.class; break;
		case VALUE_RANGE: result = ValueRangeXmlDelegate.class; break;
		case VALUE_SET: result = ValueSetXmlDelegate.class; break;
		case VERSION: result = VersionManifestXmlDelegate.class; break;

		case MODULE_SPEC: result = ModuleSpecXmlDelegate.class; break;
		case MODULE_MANIFEST: result = ModuleManifestXmlDelegate.class; break;
		case MAPPING_MANIFEST: result = MappingManifestXmlDelegate.class; break;

		default:
			break;
		}

		if(result==null)
			throw new ManifestException(ManifestErrorCode.MANIFEST_ERROR,
					"Manifest type not supported by xml serialization framework: "+type);

		return (Class<ManifestXmlDelegate<?>>) result;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.xml.ManifestXmlDelegateFactory#newDelegate(de.ims.icarus2.model.manifest.api.ManifestType)
	 */
	@Override
	public ManifestXmlDelegate<?> newDelegate(ManifestType manifestType) {
		Class<ManifestXmlDelegate<?>> clazz = getDelegateClass(manifestType);

		try {
			return clazz.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ManifestException(GlobalErrorCode.INTERNAL_ERROR, "Error instatiating default manifest xml delegate for type: "+manifestType);
		}
	}
}
