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
 */
package de.ims.icarus2.filedriver.schema.resolve.common;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.filedriver.Converter;
import de.ims.icarus2.filedriver.Converter.ReadMode;
import de.ims.icarus2.filedriver.schema.resolve.Resolver;
import de.ims.icarus2.filedriver.schema.resolve.ResolverContext;
import de.ims.icarus2.filedriver.schema.resolve.ResolverOptions;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.layer.AnnotationLayer;
import de.ims.icarus2.model.api.layer.ItemLayer;
import de.ims.icarus2.model.api.members.container.Container;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.api.AnnotationFlag;
import de.ims.icarus2.model.standard.driver.BufferedItemManager.InputCache;
import de.ims.icarus2.model.util.ModelUtils;
import de.ims.icarus2.util.Options;
import de.ims.icarus2.util.strings.FlexibleSubSequence;
import de.ims.icarus2.util.strings.StringUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

/**
 * A {@link Resolver} implementation that assumes raw values to be lists of
 * properties in the form of key-value assignments.
 *
 * @author Markus Gärtner
 *
 */
public class PropertyListResolver implements Resolver, ResolverOptions {

	public static final String OPTION_SEPARATOR = "separator";
	public static final String OPTION_ASSIGNMENT_SYMBOL = "assignmentSymbol";

	private AnnotationLayer annotationLayer;

	private Map<String, BasicAnnotationResolver<?>> fixedPropertyResolvers;

	// Configuration

	/**
	 * Used to split property assignments
	 */
	private String separator;

	/**
	 * Used to split assignments into key and value
	 */
	private char assignmentSymbol;

	private boolean allowUnknownKeys;


	/**
	 * @see de.ims.icarus2.filedriver.schema.resolve.Resolver#prepareForReading(de.ims.icarus2.filedriver.Converter, de.ims.icarus2.util.Options)
	 */
	@Override
	public void prepareForReading(Converter converter, ReadMode mode, Function<ItemLayer, InputCache> caches, Options options) {
		annotationLayer = (AnnotationLayer) options.get(LAYER);
		if(annotationLayer==null)
			throw new ModelException(GlobalErrorCode.INVALID_INPUT, "No layer assigned to this resolver "+getClass());

		Set<String> availableKeys = annotationLayer.getManifest().getAvailableKeys();
		if(!availableKeys.isEmpty()) {
			fixedPropertyResolvers = new Object2ObjectOpenHashMap<>(availableKeys.size());

			for(String annotationKey : availableKeys) {
				BasicAnnotationResolver<?> nestedResolver = BasicAnnotationResolver.forAnnotation(annotationLayer, annotationKey);
				if(nestedResolver!=null) {
					fixedPropertyResolvers.put(annotationKey, nestedResolver);
				}
			}
		}

		allowUnknownKeys = annotationLayer.getManifest().isAnnotationFlagSet(AnnotationFlag.UNKNOWN_KEYS);
	}

	/**
	 * Used to forward the "original" request with a replacement "data" value
	 */
	private final ProxyContext proxyContext = new ProxyContext();

	/**
	 * Tries to get a nested {@link Resolver} for the property specified by {@code key}
	 * and if one is found, forwards the processing to that resolver.
	 * Otherwise the {@code value} is assigned to the property as a plain {@code String}.
	 *
	 * @param context original context passed to the main {@link #process(ResolverContext)} method.
	 * @param key parsed key for the property assignment
	 * @param value parsed value for the property assignment
	 */
	private void saveAnnotation(ResolverContext context, String key, CharSequence value) {
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

	/**
	 * Used as cursor when parsing the raw data sequence
	 */
	private final FlexibleSubSequence subSequence = new FlexibleSubSequence();

	private void parseAssignment(ResolverContext context, CharSequence data, int begin, int end) {

		int assignmentIndex = -1;
		for(int i=begin+1; i<end;i++) {
			if(data.charAt(i)==assignmentSymbol) {
				assignmentIndex = i;
				break;
			}
		}

		if(assignmentIndex==-1)
			throw new ModelException(ModelErrorCode.DRIVER_INVALID_CONTENT,
					"Not a valid property assignment expression: "+data.toString());

		String key = StringUtil.toString(data, begin, assignmentIndex-1);
		subSequence.setSource(data);
		subSequence.setOffset(assignmentIndex+1);
		subSequence.setLength(data.length()-assignmentIndex-1);

		saveAnnotation(context, key, subSequence);
	}

	/**
	 * @see de.ims.icarus2.filedriver.schema.resolve.Resolver#process(de.ims.icarus2.filedriver.schema.resolve.ResolverContext)
	 */
	@Override
	public Item process(ResolverContext context) {
		CharSequence data = context.rawData();
		Item item = context.currentItem();

		int begin = 0;
		int end;
		while((end = StringUtil.indexOf(data, separator))!=-1) {
			parseAssignment(context, data, begin, end);
			begin = end+separator.length();
		}

		if(begin<data.length()-1) {
			parseAssignment(context, data, begin, data.length()-1);
		}

		return item;
	}

	/**
	 * @see de.ims.icarus2.filedriver.schema.resolve.Resolver#close()
	 */
	@Override
	public void close() {
		annotationLayer = null;
		separator = null;
		assignmentSymbol = '\0';
		allowUnknownKeys = false;

		proxyContext.reset(null, null);
		subSequence.close();

		fixedPropertyResolvers.values().forEach(Resolver::close);
		fixedPropertyResolvers.clear();
	}

	private static class ProxyContext implements ResolverContext {
		private ResolverContext source;
		private CharSequence data;
		@Override
		public Container currentContainer() {
			return source.currentContainer();
		}
		@Override
		public long currentIndex() {
			return source.currentIndex();
		}
		@Override
		public Item currentItem() {
			return source.currentItem();
		}
		@Override
		public CharSequence rawData() {
			return data;
		}
		public void reset(ResolverContext source, CharSequence data) {
			this.source = source;
			this.data = data;
		}
	}
}
