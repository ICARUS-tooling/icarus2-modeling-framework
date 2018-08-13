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
/**
 *
 */
package de.ims.icarus2.test;

import java.util.Set;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * @author Markus Gärtner
 *
 */
public class TestSettings implements Cloneable {

	private Set<String> _features;

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

	public TestSettings withFeatures(TestFeature...features) {
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

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	public TestSettings clone() {
		try {
			TestSettings clone = (TestSettings) super.clone();

			if(_features!=null) {
				_features = new ObjectOpenHashSet<>(_features);
			}

			return clone;
		} catch (CloneNotSupportedException e) {
			throw new InternalError("Unexpected cloning issue", e);
		}
	}
}
