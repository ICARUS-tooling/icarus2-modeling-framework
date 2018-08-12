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
