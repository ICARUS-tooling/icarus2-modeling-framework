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
package de.ims.icarus2.model.api.members;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.util.LazyStore;
import de.ims.icarus2.util.strings.StringResource;

/**
 * Defines the possibles types a {@link CorpusMember} can declare to represent by
 * its {@link CorpusMember#getMemberType()} method. Note although that a class can implement
 * multiple interfaces of the corpus framework, it can only ever be assigned to exactly one
 * <i>member role</i> specified by its {@code MemberType}.
 *
 * @author Markus Gärtner
 *
 */
public enum MemberType implements StringResource {
	FRAGMENT("fragment"),
	ITEM("item"),
	EDGE("edge"),
	CONTAINER("container"),
	STRUCTURE("structure"),
	LAYER("layer"), // No distinction between different layer types. they are defined by the manifest type
	CONTEXT("context"),
	;

	private final String xmlForm;

	private MemberType(String xmlForm) {
		this.xmlForm = requireNonNull(xmlForm);
	}

	/**
	 * @see de.ims.icarus2.model.util.StringResource.XmlResource#getStringValue()
	 */
	@Override
	public String getStringValue() {
		return xmlForm;
	}

	private static LazyStore<MemberType,String> store = LazyStore.forStringResource(MemberType.class);

	public static MemberType parseMemberType(String s) {
		return store.lookup(s);
	}
}
