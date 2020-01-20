/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.iql;

import java.util.Collection;
import java.util.Optional;

import de.ims.icarus2.IcarusRuntimeException;
import de.ims.icarus2.query.api.QueryErrorCode;

/**
 * @author Markus Gärtner
 *
 */
abstract class AbstractIqlQueryElement implements IqlQueryElement {

	// Helper methods for integrity checking

	/**
	 * Implementation note:
	 * Subclasses should always include a call to {@code super.checkIntegrity()}
	 * as the first statement when overriding this method!
	 *
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#checkIntegrity()
	 */
	@Override
	public void checkIntegrity() {
		// no-op
	}

	private IcarusRuntimeException forCorruption(String label, String reason) {
		return new IcarusRuntimeException(QueryErrorCode.CORRUPTED_QUERY,
				String.format("Field '%s' corrupted: %s", label, reason));
	}

	protected void checkNotNull(Object field, String label) {
		if(field==null)
			throw forCorruption(label, "value is null");
	}

	protected void checkCondition(boolean condition, String label, String message) {
		if(!condition)
			throw forCorruption(label, message);
	}

	protected void checkStringNotEmpty(String field, String label) {
		checkNotNull(field, label);
		if(field.trim().isEmpty())
			throw forCorruption(label, "string is empty");
	}

	protected void checkOptionalStringNotEmpty(Optional<String> field, String label) {
		if(field.isPresent() && field.get().trim().isEmpty())
			throw forCorruption(label, "optional string is empty");
	}

	protected void checkNestedNotNull(IqlQueryElement element, String label) {
		checkNotNull(element, label);
		element.checkIntegrity();
	}

	protected <E extends IqlQueryElement> void checkOptionalNested(Optional<E> element) {
		if(element.isPresent()) {
			element.get().checkIntegrity();
		}
	}

	protected void checkCollection(Collection<? extends IqlQueryElement> elements) {
		for (IqlQueryElement element : elements) {
			element.checkIntegrity();
		}
	}

	protected void checkCollectionNotEmpty(Collection<? extends IqlQueryElement> elements, String label) {
		checkNotNull(elements, label);
		if(elements.isEmpty())
			throw forCorruption(label, "collection is empty");

		for (IqlQueryElement element : elements) {
			element.checkIntegrity();
		}
	}

	// Helper methods for field access

	protected boolean getOrFallback(Boolean value, boolean fallback) {
		return value==null ? fallback : value.booleanValue();
	}

	protected Boolean setOrFallback(boolean value, boolean fallback) {
		return value==fallback ? null : Boolean.valueOf(value);
	}
}