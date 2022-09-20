/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2022 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.engine;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;

import java.util.Arrays;

/**
 * Fine-granular settings for performance control on {@link QueryEngine} instances.
 *
 * @author Markus Gärtner
 *
 */
public class EngineSettings implements Cloneable {

	private final int[] intValues = new int[IntField.values().length];

	public EngineSettings() {
		Arrays.fill(intValues, UNSET_INT);
	}

	public void setInt(IntField field, int value) {
		checkArgument(field+" must not be negative", value>=0);
		intValues[field.ordinal()] = value;
	}

	public int getInt(IntField field) {
		int value = intValues[field.ordinal()];
		if(value==UNSET_INT) {
			value = field.getDefaultValue();
		}
		return value;
	}

	@Override
	public EngineSettings clone() {
		try {
			return (EngineSettings) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new InternalError("not spossible", e);
		}
	}

	public enum IntField {

		/** Size of batches processed by a single thread */
		BATCH_SIZE(1<<7),
		/** Buffer size for the collector used by a single thread */
		COLLECTOR_BUFFER_SIZE(1<<10),
		/** Starting size of the shared main buffer that final result entries end up in */
		INITIAL_MAIN_BUFFER_SIZE(1<<14),
		/** Initial size of additional utility buffers, e.g. for sorting or value substitution */
		INITIAL_SECONDARY_BUFFER_SIZE(1<<12),
		/** maximum number of worker threads to use in parallel */
		WORKER_LIMIT(UNSET_INT),
		/** Timeout in seconds */
		TIMEOUT(UNSET_INT),
		;

		private final int defaultValue;

		private IntField(int defaultValue) { this.defaultValue = defaultValue; }

		public int getDefaultValue() { return defaultValue; }
	}
}
