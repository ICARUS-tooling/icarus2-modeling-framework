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
package de.ims.icarus2.model.manifest.standard;

import java.util.Optional;

import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.LayerType;
import de.ims.icarus2.util.LazyStore;
import de.ims.icarus2.util.strings.StringResource;

/**
 * Defines constants to identify some common layer types.
 *
 * @author Markus Gärtner
 *
 */
public enum DefaultLayerType implements LayerType, StringResource {

	//TODO add actual descriptions to the layer types

	ITEM_TOKEN("item:token", "Token", ""),
	ITEM_SENTENCE("item:sentence", "Sentence", ""),
	ITEM_DOCUMENT("item:document", "Document", ""),
	ITEM_PARAGRAPH("item:paragraph", "Paragaraph", ""),
	ITEM_CHAPTER("item:chapter", "Chapter", ""),
	ITEM_COREFERENCE_CLUSTER("item:coreference-cluster", "Coreference Cluster", ""),
	ITEM_MENTION("item:mention", "Mention", ""),
	ITEM_PHRASE("item:phrase", "Phrase", ""),
	ITEM_LAYER_OVERLAY("item:layer-overlay", "Layer-Overlay", ""),

	STRUCT_DEPENDENCY("struct:dependency", "Dependency Syntax", ""),
	STRUCT_CONSTITUENT("struct:constituent", "Constituency Syntax", ""),
	STRUCT_COREFERENCE("struct:coreference", "Coreference Structure", ""),
	STRUCT_INFORMATION_STATUS("struct:information-status", "Information Status", ""),

	ANNO_FORM("anno:form", "Form", ""),
	ANNO_POS("anno:part-of-speech", "Part-of-Speech", ""),
	ANNO_LEMMA("anno:lemma", "Lemma", ""),
	ANNO_ROLE("anno:role", "Role", ""),
	ANNO_RELATION("anno:relation", "Relation", ""),
	ANNO_FEATURES("anno:features", "Features", ""),
	ANNO_PROPERTIES("anno:properties", "Properties", ""),
	ANNO_HEAD("anno:head", "Head", ""),
	ANNO_SPEAKER("anno:speaker", "Speaker", ""),
	;

	private final Optional<String> id, name, description;

	private DefaultLayerType(String id, String name, String description) {
		this.id = Optional.of(id);
		this.name = Optional.of(name);
		this.description = Optional.of(description);
	}

	private static final Optional<String> namespace = Optional.of("icarus2-commons");

	@Override
	public Optional<String> getNamespace() { return namespace; }

	@Override
	public Optional<String> getId() { return id; }

	@Override
	public Optional<String> getName() { return name; }

	@Override
	public Optional<String> getDescription() { return description; }

	@Override
	public Optional<LayerManifest<?>> getSharedManifest() { return Optional.empty(); }

	@Override
	public String getStringValue() { return id.get(); }

	private static final LazyStore<DefaultLayerType, String> _lookup =
			LazyStore.forStringResource(DefaultLayerType.class, true);

	public static Optional<? extends LayerType> forId(String id) {
		return _lookup.tryLookup(id);
	}
}
