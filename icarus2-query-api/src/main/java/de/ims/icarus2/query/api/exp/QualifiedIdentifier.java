/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2025 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.query.api.exp;

import static de.ims.icarus2.util.Conditions.checkNotEmpty;

import java.util.Optional;

import de.ims.icarus2.model.manifest.util.ManifestUtils;

/**
 * Models a qualified identifier according to the IQL specification.
 *
 * A qualified identifier consists of a non-empty sequence of individual
 * identifier elements, in raw form separated by '::'. The semantics and
 * maximum number of elements is context dependent:
 * <p>
 * Element description if targeting a layer:
 * <table border="1">
 * <tr><th># Elements</th><th>1<sup>st</sup></th><th>2<sup>nd</sup></th><th>3<sup>rd</supS</th></tr>
 * <tr><td>1</td><td>layer id</td><td>-</td><td>-</td></tr>
 * <tr><td>2</td><td>corpus id</td><td>layer id</td><td>-</td></tr>
 * <tr><td>3</td><td>corpus id</td><td>context id</td><td>layer id</td></tr>
 * </table>
 * <p>
 * Element description if targeting an annotation:
 * <table border="1">
 * <tr><th># Elements</th><th>1<sup>st</sup></th><th>2<sup>nd</sup></th><th>3<sup>rd</sup></th><th>4<sup>th</sup></th></tr>
 * <tr><td>1</td><td>annotation id/layer id*</td><td>-</td><td>-</td><td>-</td></tr>
 * <tr><td>2</td><td>layer id</td><td>annotation id</td><td>-</td><td>-</td></tr>
 * <tr><td>3</td><td>corpus id</td><td>layer id</td><td>annotation id</td><td>-</td></tr>
 * <tr><td>3</td><td>corpus id</td><td>context id</td><td>layer id</td><td>annotation id</td></tr>
 *
 * * a sole element present in the context of an annotation resolution can reference
 * either directly a uniquely identified annotation manifest or an annotation layer
 * with a default annotation key.
 * </table>
 *
 * TODO fix explanation
 *
 * @author Markus Gärtner
 *
 */
public final class QualifiedIdentifier {

	public static QualifiedIdentifier parseIdentifier(String identifier) {
		checkNotEmpty(identifier);
		String host = ManifestUtils.extractHostId(identifier);
		String element = ManifestUtils.extractElementId(identifier);
		return new QualifiedIdentifier(identifier, host, element);
	}

	public static QualifiedIdentifier of(String rawText, String host, String element) {
		checkNotEmpty(rawText);
		checkNotEmpty(host);
		checkNotEmpty(element);
		return new QualifiedIdentifier(rawText, host, element);
	}

	public static QualifiedIdentifier of(String element) {
		checkNotEmpty(element);
		return new QualifiedIdentifier(element, null, element);
	}

	private final String rawText;
	private final String host, element;

	private QualifiedIdentifier(String rawText, String host, String element) {
		this.rawText = rawText;
		this.host = host;
		this.element = element;
	}

	public String getRawText() { return rawText; }

	public Optional<String> getHost() { return Optional.of(host); }
	public String getElement() { return element; }

	public boolean hasHost() { return host!=null; }

	@Override
	public int hashCode() { return rawText.hashCode(); }

	@Override
	public boolean equals(Object obj) {
		if(obj==this) {
			return true;
		} else if(obj instanceof QualifiedIdentifier) {
			return rawText.equals(((QualifiedIdentifier)obj).rawText);
		}
		return false;
	}

	/** Returns the raw string from which this identifier was parsed. */
	@Override
	public String toString() { return rawText; }
}

