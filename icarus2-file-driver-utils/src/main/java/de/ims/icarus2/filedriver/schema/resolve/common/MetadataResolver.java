/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2023 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.filedriver.schema.resolve.common;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Set;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusApiException;
import de.ims.icarus2.filedriver.Converter;
import de.ims.icarus2.filedriver.Converter.ReadMode;
import de.ims.icarus2.filedriver.schema.resolve.Resolver;
import de.ims.icarus2.filedriver.schema.resolve.ResolverContext;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationFlag;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.Options;
import de.ims.icarus2.util.strings.FlexibleSubSequence;
import de.ims.icarus2.util.strings.StringUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * @author Markus Gärtner
 *
 */
public class MetadataResolver implements Resolver {

	//FIXME for now we only provide a single format "x=y", but in the future we want to expand that

	public static final String OPTION_PREFIX = "prefix";
	public static final String OPTION_LAYER_ID = "layerId";
	public static final String OPTION_LAYER = "layer";

	private String prefix;

	private AnnotationLayer annotationLayer;

	private Map<String, BasicAnnotationResolver<?>> fixedPropertyResolvers;

	private boolean allowUnknownKeys;


	/**
	 * Used to forward the "original" request with a replacement "data" value
	 */
	private final ProxyContext proxyContext = new ProxyContext();


	/**
	 * Used as cursor when parsing the raw data sequence
	 */
	private final FlexibleSubSequence subSequence = new FlexibleSubSequence();

	@Override
	public void prepareForReading(Converter converter, ReadMode mode, ResolverContext context, Options options) {
		requireNonNull(converter);
		requireNonNull(mode);
		requireNonNull(context);
		requireNonNull(options);

		String layerId = options.getString(OPTION_LAYER_ID);
		if(layerId!=null) {
			annotationLayer = converter.getDriver().getContext().getLayer(layerId);
		}

		if(annotationLayer==null) {
			annotationLayer = (AnnotationLayer) options.get(OPTION_LAYER);
		}

		if(annotationLayer==null)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT, String.format(
					"No annotation layer defined. Use '%s' or '%s' option to provide a layer id or direct layer instance.", OPTION_LAYER_ID, OPTION_PREFIX));

		prefix = options.getString(OPTION_PREFIX);

		Set<String> availableKeys = annotationLayer.getManifest().getAvailableKeys();
		if(!availableKeys.isEmpty()) {
			fixedPropertyResolvers = new Object2ObjectOpenHashMap<>(availableKeys.size());

			for(String annotationKey : availableKeys) {
				BasicAnnotationResolver<?> nestedResolver =
						BasicAnnotationResolver.forAnnotation(annotationLayer, annotationKey);
				if(nestedResolver!=null) {
					fixedPropertyResolvers.put(annotationKey, nestedResolver);
				}
			}
		}

		allowUnknownKeys = annotationLayer.getManifest().isAnnotationFlagSet(AnnotationFlag.UNKNOWN_KEYS);
	}

	/**
	 * Tries to get a nested {@link Resolver} for the property specified by {@code key}
	 * and if one is found, forwards the processing to that resolver.
	 * Otherwise the {@code value} is assigned to the property as a plain {@code String}.
	 *
	 * @param context original context passed to the main {@link #process(ResolverContext)} method.
	 * @param key parsed key for the property assignment
	 * @param value parsed value for the property assignment
	 * @throws IcarusApiException
	 */
	private void saveAnnotation(ResolverContext context, String key, CharSequence value) throws IcarusApiException {
		BasicAnnotationResolver<?> resolver = (fixedPropertyResolvers==null) ? null : fixedPropertyResolvers.get(key);
		if(resolver!=null) {
			// In case of nested resolver we delegate the work
			proxyContext.reset(context, value);
			resolver.process(proxyContext);
		} else if(allowUnknownKeys){
			// If allowed we simply store the property as String type
			annotationLayer.getAnnotationStorage().setValue(context.currentItem(), key, value.toString());
		} else
			throw new ModelException(ModelErrorCode.DRIVER_INVALID_CONTENT,
					"Unknown key '"+key+"' for layer "+ModelUtils.getUniqueId(annotationLayer));
	}

	@Override
	public Item process(ResolverContext context) throws IcarusApiException {
		CharSequence rawData = context.rawData();
		int start = 0;
		if(prefix!=null) {
			start += prefix.length();
		}
		int sep = StringUtil.indexOf(rawData, '=', start);
		assert sep!=-1 : "couldn't find assignment symbol in: "+rawData;
		String key = rawData.subSequence(start, sep).toString();

		Item item = context.currentItem();
		subSequence.setSource(rawData);
		subSequence.setRange(sep+1, rawData.length()-1);

		saveAnnotation(context, key, subSequence);

		return item;
	}

}
