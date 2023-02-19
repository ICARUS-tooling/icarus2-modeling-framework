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
package de.ims.icarus2.model.standard.members.layer.annotation.single;

import static java.util.Objects.requireNonNull;

import java.util.function.Consumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.members.item.Item;
import de.ims.icarus2.model.manifest.util.Messages;
import de.ims.icarus2.model.standard.members.layer.annotation.AbstractManagedAnnotationStorage;

/**
 * @author Markus Gärtner
 *
 */
public abstract class AbstractSingleKeyStorage extends AbstractManagedAnnotationStorage {

	private final String annotationKey;

	/**
	 * @param weakKeys
	 * @param initialCapacity
	 */
	public AbstractSingleKeyStorage(String annotationKey, boolean weakKeys, int initialCapacity) {
		super(weakKeys, initialCapacity);

		this.annotationKey = requireNonNull(annotationKey);
	}

	public String getAnnotationKey() {
		return annotationKey;
	}

	/**
	 * Special consideration of single key storages: We allow {@code null}
	 * keys which default to the one single key defined for this annotation!
	 */
	protected void checkKey(String key) {
		requireNonNull(key);
		if(!annotationKey.equals(key))
			throw new ModelException(GlobalErrorCode.INVALID_INPUT,
					Messages.mismatch("Unknown key", annotationKey, key));
	}

	@Override
	public boolean collectKeys(Item item, Consumer<String> action) {
		requireNonNull(item);
		requireNonNull(action);

		boolean result = false;

		if(hasAnnotations(item)) {
			action.accept(annotationKey);

			result = true;
		}

		return result;
	}

	@Override
	public boolean containsItem(Item item) {
		requireNonNull(item);
		return hasAnnotations(item);
	}

}
