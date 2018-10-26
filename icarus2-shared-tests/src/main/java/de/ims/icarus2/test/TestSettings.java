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
package de.ims.icarus2.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * @author Markus Gärtner
 *
 */
public class TestSettings implements Cloneable {

	private Set<String> _features;

	private List<BiConsumer<TestSettings,?>> _processors;

	private String message;

	private Map<String, Object> _properties;

	//TODO add locking mechanism to block modifications during process() calls

	TestSettings() {
		// no-op
	}

	public boolean hasFeature(TestFeature feature) {
		return _features!=null && _features.contains(feature.name());
	}

	public boolean hasFeatures(TestFeature...features) {
		if(_features==null || _features.size()<features.length) {
			return false;
		}

		for(TestFeature feature : features) {
			if(!_features.contains(feature.name())) {
				return false;
			}
		}

		return true;
	}

	public TestSettings features(TestFeature...features) {
		if(_features==null) {
			_features = new ObjectOpenHashSet<>();
		}

		for(TestFeature feature : features) {
			_features.add(feature.name());
		}

		return this;
	}

	public TestSettings withoutFeatures(TestFeature...features) {
		if(_features!=null) {
			for(TestFeature feature : features) {
				_features.remove(feature.name());
			}
		}

		return this;
	}

	public TestSettings message(String message) {
		this.message = message;

		return this;
	}

	public String getMessage() {
		return getMessage((String)null);
	}

	public String getMessage(String fallback) {
		return getMessage(() -> fallback);
	}

	public String getMessage(Supplier<? extends String> message) {

		String msg = this.message;
		if(msg == null && message!=null) {
			msg = message.get();
		}

		return msg;
	}

	public TestSettings property(String key, Object value) {
		if(_properties==null) {
			_properties = new Object2ObjectOpenHashMap<>();
		}

		_properties.put(key, value);

		return this;
	}

	@SuppressWarnings("unchecked")
	public <T extends Object> T getProperty(String key) {
		return (T) (_properties==null ? null : _properties.get(key));
	}

	public <T extends Object> T getProperty(String key, T defaultValue) {
		return getProperty(key, () -> defaultValue);
	}

	@SuppressWarnings("unchecked")
	public <T extends Object> T getProperty(String key, Supplier<? extends T> defaultValue) {
		T result = null;
		if(_properties!=null) {
			result = (T) _properties.get(key);
		}

		if(result==null && defaultValue!=null) {
			result = defaultValue.get();
		}

		return result;
	}

	/**
	 * Adds a processor to this settings object that will be used to configure
	 * test instances.
	 * Note that the supplied processor should <b>not</b> modify the {@link TestSettings}
	 * instance supplied to its {@link BiConsumer#accept(Object, Object)} method!
	 * <p>
	 * Processors will be applied in the order they have been registered with
	 * a {@link TestSettings} instance.
	 *
	 * @param clazz
	 * @param processor
	 * @return
	 */
	public <T extends Object> TestSettings processor(BiConsumer<TestSettings, ? super T> processor) {
		if(_processors==null) {
			_processors = new ArrayList<>();
		}

		_processors.add(processor);

		return this;
	}

	@SuppressWarnings("unchecked")
	public <T extends Object> T process(T target) {

		if(_processors!=null) {
			for(BiConsumer<TestSettings,?> processor : _processors) {
				((BiConsumer<TestSettings,? super T>)processor).accept(this, target);
			}
		}

		return target;
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public TestSettings clone() {
		try {
			TestSettings clone = (TestSettings) super.clone();

			if(_features!=null) {
				clone._features = new ObjectOpenHashSet<>(_features);
			}

			if(_properties!=null) {
				clone._properties = new Object2ObjectOpenHashMap<>(_properties);
			}

			if(_processors!=null) {
				clone._processors = new ArrayList<>(_processors);
			}

			return clone;
		} catch (CloneNotSupportedException e) {
			throw new InternalError("Unexpected cloning issue", e);
		}
	}

//	private static class Pair<K extends Object, V extends Object> {
//		private final K first;
//		private final V second;
//		/**
//		 * @param first
//		 * @param second
//		 */
//		public Pair(K first, V second) {
//			this.first = requireNonNull(first);
//			this.second = requireNonNull(second);
//		}
//
//		public K getFirst() {
//			return first;
//		}
//
//		public V getSecond() {
//			return second;
//		}
//	}
}
