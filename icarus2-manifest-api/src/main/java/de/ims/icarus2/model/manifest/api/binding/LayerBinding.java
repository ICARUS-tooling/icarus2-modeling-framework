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
package de.ims.icarus2.model.manifest.api.binding;

import static de.ims.icarus2.util.Conditions.checkArgument;
import static de.ims.icarus2.util.Conditions.checkState;
import static de.ims.icarus2.util.lang.Primitives._int;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.IcarusException;
import de.ims.icarus2.Report;
import de.ims.icarus2.Report.ReportItem;
import de.ims.icarus2.ReportBuilder;
import de.ims.icarus2.model.manifest.ManifestErrorCode;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.CorpusManifest;
import de.ims.icarus2.model.manifest.api.LayerManifest;
import de.ims.icarus2.model.manifest.api.LayerType;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.api.ManifestRegistry;
import de.ims.icarus2.util.Multiplicity;
import de.ims.icarus2.util.collections.CollectionUtils;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.id.Identity;
import de.ims.icarus2.util.id.StaticIdentity;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

/**
 * An almost stateless implementation for representing both requirements and
 * corresponding mappings for a binding process.
 * <p>
 * Binding in this context refers to the process of assigning {@link LayerPrerequisite prerequisites}
 * and {@link LayerPointer pointers} to a certain set of {@code aliases}. Normally a consuming entity
 * like a visualization will publish an incomplete {@link LayerBinding} that contains only prerequisites
 * to define what it can handle or what it needs as a minimum set of layers of which type to properly
 * do its job. An application can then use this information to either automatically or in cooperation
 * with the user assign real layer instances to those prerequisites.
 * <p>
 * Note that the basic mapping information in terms of prerequisites ad pointers to layers is stateless
 * and always the result of a build process using the available {@link Builder} class. For convenience
 * this implementation has built in support for resolving abstract {@link LayerPointer pointers} to actual
 * {@link LayerManifest manifest} instances. This resolution process can be initiated by calling the
 * {@link #resolve(ManifestRegistry)} manifest and supply it with the registry that should be used to
 * lookup the manifest objects. An internal flag keeps track of the success of attempted resolutions.
 * Once a resolution succeeded any subsequent call to the {@link #resolve(ManifestRegistry)} method will
 * yield an {@link IllegalStateException} to be thrown.
 * <p>
 * Since {@link LayerBinding} implements the {@link Bindable} interface it is possible to use an existing
 * binding to start over using the already defined prerequisites.
 * <p>
 * {@link LayerBinding} instances cannot be directly instantiated, but will always be the result of a
 * building process using the provided {@link Builder} facility.
 *
 * @author Markus Gärtner
 *
 */
public final class LayerBinding implements Bindable, Serializable {

	private static final long serialVersionUID = -6351446025754745168L;

	// Global access to builders

	public static Builder newBuilder() {
		return new Builder();
	}

	public static Builder newBuilder(String corpusId) {
		return new Builder().corpusId(corpusId);
	}

	/**
	 * Creates a new {@link Builder} that will contain all the {@link LayerPrerequisite}s
	 * from the given {@link Bindable}.
	 *
	 * @param source
	 * @return
	 */
	public static Builder fromBindable(Bindable source) {
		return new Builder(source);
	}

	/**
	 * Similar to {@link #fromBindable(Bindable)} but also copies over the
	 * {@link LayerBinding#getCorpusId() corpus-id} and {@link LayerBinding#getSource() source}
	 * attributes from the given {@link LayerBinding}, if present.
	 *
	 * @param source
	 * @return
	 */
	public static Builder fromBindable(LayerBinding source) {
		return new Builder(source);
	}

	public static Builder fromPrerequisites(Set<LayerPrerequisite> prerequisites) {
		return new Builder(prerequisites);
	}

	// Object fields

	private final transient Map<String, Entry> mappings = new Object2ObjectOpenHashMap<>();
	private final transient Identity source;
	private final String corpusId;

	/**
	 * Flag to keep track of (successful) resolution attempts
	 */
	private final AtomicBoolean isResolved = new AtomicBoolean(false);

	private LayerBinding(Builder builder) {
		requireNonNull(builder);

		mappings.putAll(builder.mappings);
		corpusId = builder.corpusId;
		source = builder.source;
	}

	/**
	 * @see de.ims.icarus2.model.manifest.api.binding.Bindable#getBindingEndpoints()
	 */
	@Override
	public Set<LayerPrerequisite> getBindingEndpoints() {
		LazyCollection<LayerPrerequisite> result = LazyCollection.lazySet();

		forEachPrerequisite(result);

		return result.getAsSet();
	}

	/**
	 * Resolves all {@link LayerPointer pointers} for mappings in this binding
	 * to actual {@link LayerManifest layers}. In addition it resolves
	 * {@link LayerPrerequisite#getTypeId() type definitions} in prerequisites to
	 * actual {@link LayerType} instances.
	 * <p>
	 * The returned report will contain errors and informative entries.
	 *
	 * @param registry
	 */
	public Report<ReportItem> resolve(ManifestRegistry registry) {

		if(isResolved.compareAndSet(false, true)) {
			ReportBuilder<ReportItem> reportBuilder = ReportBuilder.newBuilder();
			if(source!=null) {
				reportBuilder.source(source);
			}

			CorpusManifest corpusManifest = registry.getCorpusManifest(corpusId)
					.orElseThrow(ManifestException.error(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
							"No corpus for id: "+corpusId));

			mappings.forEach((alias, entry) -> resolveEntry(alias, entry, corpusManifest, registry, reportBuilder));

			Report<ReportItem> report = reportBuilder.build();

			// Reset resolution flag in case part of the process failed!
			if(report.hasErrors()) {
				isResolved.set(false);
			}

			return report;
		} else
			throw new ManifestException(GlobalErrorCode.ILLEGAL_STATE, "Mapping content already resolved");
	}

	private void resolveEntry(String alias, Entry entry, CorpusManifest corpusManifest,
			ManifestRegistry registry, ReportBuilder<ReportItem> reportBuilder) {
		// Resolve layer manifests if required
		if(entry.layers==null && entry.targets!=null) {
			Set<LayerManifest> layers = new ReferenceOpenHashSet<>();
			int failedPointers = 0;
			for(LayerPointer layerPointer : entry.targets) {

				// Level-1 check
				Optional<ContextManifest> contextManifest = corpusManifest.getContextManifest(layerPointer.getContextId());
				if(!contextManifest.isPresent()) {
					failedPointers++;
					reportBuilder.addError(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
							"Alias '%s' - Missing context '%s' in target corpus '%s'",
							alias, layerPointer.getContextId(), corpusManifest.getId());
					continue;
				}

				// Level-2 check
				Optional<LayerManifest> layer = contextManifest.get()
						.getLayerManifest(layerPointer.getLayerId());
				if(!layer.isPresent()) {
					failedPointers++;
					reportBuilder.addError(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
							"Alias '%s' - Missing layer '%s' in target context '%s' in corpus '%s'",
							alias, layerPointer.getLayerId(), contextManifest.get().getId(), corpusManifest.getId());
					continue;
				}

				layers.add(layer.get());
			}

			// Delay actual assignment of resolved layers till we know that ALL pointers for the entry succeeded
			if(failedPointers==0) {
				entry.layers = layers;
			}

			reportBuilder.addInfo("Alias '%s' - Resolved %d layers, failed %d layers",
					alias, _int(layers.size()), _int(failedPointers));
		}

		// Resolve layer type if required
		if(entry.layerType==null && entry.prerequisite!=null && entry.prerequisite.getTypeId()!=null) {
			Optional<LayerType> layerType = entry.prerequisite.getTypeId()
					.flatMap(id -> registry.getLayerType(id));

			if(layerType.isPresent()) {
				entry.layerType = layerType.get();
			} else {
				reportBuilder.addError(ManifestErrorCode.MANIFEST_UNKNOWN_ID, "Alias '%s' - Unknown layer type id '%s'",
						alias, entry.prerequisite.getTypeId());
			}
		}
	}

	public String getCorpusId() {
		return corpusId;
	}

	public Identity getSource() {
		return source;
	}

	public void forEachAlias(Consumer<? super String> action) {
		mappings.keySet().forEach(action);
	}

	public void forEachPrerequisite(Consumer<? super LayerPrerequisite> action) {
		mappings.values().forEach(e -> {
			LayerPrerequisite prerequisite = e.prerequisite;
			if(prerequisite!=null) {
				action.accept(prerequisite);
			}
		});
	}

	public Set<String> getAliases() {
		return new ObjectOpenHashSet<>(mappings.keySet());
	}

	protected Entry getBinding(String alias) {
		requireNonNull(alias);

		Entry binding = mappings.get(alias);
		if(binding==null)
			throw new IcarusException(GlobalErrorCode.INVALID_INPUT, "No mappings defined for alias: "+alias);

		return binding;
	}

	public boolean isResolved() {
		return isResolved.get();
	}

	// Lookup methods to fetch parts of a binding

	public LayerPrerequisite getLayerPrerequisite(String alias) {
		return getBinding(alias).prerequisite;
	}

	public Set<LayerPointer> getLayerPointers(String alias) {
		return nonNullReadOnlySet(getBinding(alias).targets);
	}

	public Set<LayerManifest> getLayerManifests(String alias) {
		checkState("No manifests resolved yet", isResolved());
		return nonNullReadOnlySet(getBinding(alias).layers);
	}

	private static <E extends Object> Set<E> nonNullReadOnlySet(Set<E> set) {
		if(set==null) {
			set = Collections.emptySet();
		} else if(!set.isEmpty()) {
			set = CollectionUtils.getSetProxy(set);
		}

		return set;
	}

	public LayerType getLayerType(String alias) {
		checkState("No manifests resolved yet", isResolved());
		return getBinding(alias).layerType;
	}

	// Short-hand methods for checking

	public boolean containsAlias(String alias) {
		return mappings.containsKey(alias);
	}

	// Serialization

	/**
	 * Delegates serialization to a new {@link Builder} instance that contains all
	 * mapping information except for already resolved layer pointers or layer types.
	 *
	 * @return
	 * @throws ObjectStreamException
	 */
	private Object writeReplace() throws ObjectStreamException {
		Builder builder = fromBindable(this);

		mappings.forEach((alias, entry) -> {
			if(entry.targets!=null) {
				builder.addPointers(alias, entry.targets);
			}
		});

		return builder;
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class LayerPointerImpl implements LayerPointer {
		private final String contextId;
		private final String layerId;

		public LayerPointerImpl(String contextId, String layerId) {
			this.contextId = contextId;
			this.layerId = layerId;
		}

		@Override
		public String getContextId() {
			return contextId;
		}

		@Override
		public String getLayerId() {
			return layerId;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return new StringBuilder(100)
			.append("[").append(getClass()).append(":")
			.append(" contextId=").append(contextId)
			.append(" layerId=").append(layerId)
			.append("]")
			.toString();
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hash(layerId, contextId);
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} else if(obj instanceof LayerPointer) {
				LayerPointer other = (LayerPointer) obj;

				return LayerPointer.defaultEquals(this, other);
			}

			return false;
		}
	}

	/**
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class LayerPrerequisiteImpl implements LayerPrerequisite {

		private final Optional<String> layerId, contextId, typeId, description;
		private final String alias;
		private final Multiplicity multiplicity;

		/**
		 * Constructor reserved for serialization so we don't have to select any
		 * of the specialized ones.
		 */
		LayerPrerequisiteImpl(String layerId, String contextId,
				String typeId, String alias, String description,
				Multiplicity multiplicity) {
			this.layerId = Optional.ofNullable(layerId);
			this.contextId = Optional.ofNullable(contextId);
			this.typeId = Optional.ofNullable(typeId);
			this.alias = requireNonNull(alias);
			this.description = Optional.ofNullable(description);
			this.multiplicity = requireNonNull(multiplicity);
		}

		/**
		 * "Hard binding" constructor
		 *
		 * @param contextId
		 * @param layerId
		 * @param alias
		 * @param multiplicity
		 * @param description
		 */
		public LayerPrerequisiteImpl(String contextId, String layerId,
				String alias, Multiplicity multiplicity, String description) {

			this.contextId = Optional.ofNullable(contextId);
			this.layerId = Optional.ofNullable(layerId);
			this.typeId = Optional.empty();
			this.alias = requireNonNull(alias);
			this.multiplicity = requireNonNull(multiplicity);
			this.description = Optional.ofNullable(description);
		}

		/**
		 * "Soft binding" constructor
		 *
		 * @param typeId
		 * @param alias
		 * @param multiplicity
		 * @param description
		 */
		public LayerPrerequisiteImpl(String typeId, String alias,
				Multiplicity multiplicity, String description) {

			this.contextId = Optional.empty();
			this.layerId = Optional.empty();
			this.typeId = Optional.ofNullable(typeId);
			this.alias = requireNonNull(alias);
			this.multiplicity = multiplicity;
			this.description = Optional.ofNullable(description);
		}

		@Override
		public Optional<String> getLayerId() {
			return layerId;
		}

		@Override
		public Optional<String> getContextId() {
			return contextId;
		}

		@Override
		public Optional<String> getTypeId() {
			return typeId;
		}

		@Override
		public String getAlias() {
			return alias;
		}

		@Override
		public Optional<String> getDescription() {
			return description;
		}

		/**
		 * @see de.ims.icarus2.model.manifest.api.binding.LayerPrerequisite#getMultiplicity()
		 */
		@Override
		public Multiplicity getMultiplicity() {
			return multiplicity;
		}

		/**
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return new StringBuilder(100)
			.append("[").append(getClass()).append(":")
			.append(" alias=").append(alias)
			.append(" contextId=").append(contextId)
			.append(" layerId=").append(layerId)
			.append(" typeId=").append(typeId)
			.append(" description=").append(description)
			.append(" multiplicity=").append(multiplicity)
			.append("]")
			.toString();
		}

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return Objects.hash(alias, contextId, layerId, typeId, description, multiplicity);
		}

		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if(this==obj) {
				return true;
			} else if(obj instanceof LayerPrerequisite) {
				LayerPrerequisite other = (LayerPrerequisite) obj;

				return LayerPrerequisite.defaultEquals(this, other);
			}

			return false;
		}
	}

	private static class Entry {

		/**
		 * Source of the binding which specifies what kind of layers can be bound to it.
		 */
		public LayerPrerequisite prerequisite;

		/**
		 * Resolved type definition for supported layers from this binding's
		 * {@link LayerPrerequisite prerequisite}.
		 */
		public LayerType layerType;

		/**
		 * Link to target of the binding.
		 */
		public Set<LayerPointer> targets;

		/**
		 * Resolved target layer, must satisfy specification from this binding's
		 * {@link LayerPrerequisite prerequisite}.
		 */
		public Set<LayerManifest> layers;
	}

	/**
	 * Construction facility for creating {@link LayerBinding}s.
	 * Unless otherwise noted all builder methods will throw {@link NullPointerException}
	 * if provided with {@code null} arguments or {@link IllegalStateException} in case the
	 * builder field that is being attempted to change has already been set.
	 * <p>
	 * Note that a builder instance can be used to instantiate multiple binding objects,
	 * but there is no automatic purge of data happening after a call to {@link Builder#build()}!
	 * If client code wishes to clear data in a builder, it should call {@link Builder#reset()}
	 * which will erase all previously set fields.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public static class Builder implements Serializable {

		private static final long serialVersionUID = 3632658096612104387L;

		/**
		 * Maps aliases to Binding objects
		 */
		private final transient Map<String, Entry> mappings = new Object2ObjectOpenHashMap<>();

		private String corpusId;

		private transient Identity source;

		private transient boolean requiresCorpusId = false;

		Builder() {
			// no-op
		}

		Builder(Set<LayerPrerequisite> prerequisites) {
			prerequisites.forEach(this::addPrerequisite);
		}

		Builder(Bindable bindable) {
			bindable.getBindingEndpoints().forEach(this::addPrerequisite);
		}

		Builder(LayerBinding binding) {
			binding.getBindingEndpoints().forEach(this::addPrerequisite);

			String corpusId = binding.getCorpusId();
			if(corpusId!=null) {
				corpusId(corpusId);
			}

			Identity source = binding.getSource();
			if(source!=null) {
				source(source);
			}
		}

		protected Entry getEntry(String alias, boolean createIfMissing) {
			Entry entry = mappings.get(alias);

			if(entry==null) {
				if(createIfMissing) {
					entry = new Entry();
					mappings.put(alias, entry);
				} else
					throw new ManifestException(ManifestErrorCode.MANIFEST_UNKNOWN_ID,
							"No prerequisite defined for alias yet: "+alias);
			}

			return entry;
		}

		public Builder addPrerequisite(String alias, String contextId, String layerId, String description) {
			return addPrerequisite(new LayerPrerequisiteImpl(contextId, layerId, alias, Multiplicity.ONE, description));
		}

		public Builder addPrerequisite(String alias, String contextId, String layerId, Multiplicity multiplicity, String description) {
			return addPrerequisite(new LayerPrerequisiteImpl(contextId, layerId, alias, multiplicity, description));
		}

		public Builder addPrerequisite(String alias, String typeId, String description) {
			return addPrerequisite(new LayerPrerequisiteImpl(typeId, alias, Multiplicity.ONE, description));
		}

		public Builder addPrerequisite(String alias, String typeId, Multiplicity multiplicity, String description) {
			return addPrerequisite(new LayerPrerequisiteImpl(typeId, alias, multiplicity, description));
		}

		public Builder addPrerequisite(LayerPrerequisite prerequisite) {
			requireNonNull(prerequisite);

			getEntry(prerequisite.getAlias(), true).prerequisite = prerequisite;

			return this;
		}

		public Builder addPointer(String alias, String contextId, String layerId) {
			return addPointer(alias, new LayerPointerImpl(contextId, layerId));
		}

		public Builder addPointer(String alias, LayerPointer pointer) {
			requireNonNull(alias);

			Entry entry = getEntry(alias, true);

			if(entry.targets==null) {
				entry.targets = new ObjectOpenHashSet<>();
			}

			entry.targets.add(pointer);

			requiresCorpusId = true;

			return this;
		}

		public Builder addPointers(String alias, Collection<? extends LayerPointer> pointers) {
			requireNonNull(alias);
			checkArgument("List of pointers is empty", !pointers.isEmpty());

			Entry entry = getEntry(alias, true);

			if(entry.targets==null) {
				entry.targets = new ObjectOpenHashSet<>();
			}

			entry.targets.addAll(pointers);

			requiresCorpusId = true;

			return this;
		}

		public Builder corpusId(String corpusId) {
			requireNonNull(corpusId);
			checkState("Corpus id already set", this.corpusId==null);

			this.corpusId = corpusId;

			return this;
		}

		public Builder source(Identity source) {
			requireNonNull(source);
			checkState("Source already set", this.source==null);

			this.source = source;

			return this;
		}

		protected void checkInternals() {
//			checkState("No mappings defined", !mappings.isEmpty());

			checkState("Missing corpus-id - must have a valid corpus-id defined when using layer pointers!",
					!requiresCorpusId || corpusId!=null);
		}

		public void reset() {
			mappings.clear();
			corpusId = null;
			source = null;
			requiresCorpusId = false;
		}

		public LayerBinding build() {
			checkInternals();

			return new LayerBinding(this);
		}

		// Serialization

		private void writeObject(ObjectOutputStream out)
		        throws IOException {

			out.defaultWriteObject();

			// Source
			out.writeObject(source==null ? null : source.getId());
			out.writeObject(source==null ? null : source.getName());
			out.writeObject(source==null ? null : source.getDescription());

			// Mappings
			out.writeInt(mappings.size());

			for(Map.Entry<String, Entry> mapEntry : mappings.entrySet()) {
				String alias = mapEntry.getKey();
				Entry entry = mapEntry.getValue();

				out.writeObject(alias);

				LayerPrerequisite prerequisite = entry.prerequisite;
				out.writeBoolean(prerequisite!=null);
				if(prerequisite!=null) {
					out.writeObject(prerequisite.getContextId());
					out.writeObject(prerequisite.getLayerId());
					out.writeObject(prerequisite.getTypeId());
					out.writeObject(prerequisite.getMultiplicity());
					out.writeObject(prerequisite.getDescription());
				}

				Set<LayerPointer> layerPointers = entry.targets;
				if(layerPointers==null || layerPointers.isEmpty()) {
					out.writeInt(0);
				} else {
					out.writeInt(layerPointers.size());

					for(LayerPointer layerPointer : layerPointers) {
						out.writeObject(layerPointer.getContextId());
						out.writeObject(layerPointer.getLayerId());
					}
				}
			}
		}

		private void readObject(ObjectInputStream in)
		        throws IOException, ClassNotFoundException {

			in.defaultReadObject();

			// Read source
			String sourceId = (String)in.readObject();
			String sourceName = (String)in.readObject();
			String sourceDesc = (String)in.readObject();

			if(sourceId!=null) {
				source(new StaticIdentity(sourceId, sourceName, sourceDesc, null));
			}

			int expectedMappings = in.readInt();

			while(expectedMappings-->0) {
				String alias = (String) in.readObject();

				Entry entry = getEntry(alias, true);

				boolean hasPrerequisite = in.readBoolean();
				if(hasPrerequisite) {
					String contextId = (String)in.readObject();
					String layerId = (String)in.readObject();
					String typeId = (String)in.readObject();
					Multiplicity multiplicity = (Multiplicity)in.readObject();
					String description = (String)in.readObject();

					entry.prerequisite = new LayerPrerequisiteImpl(
							layerId, contextId, typeId, alias, description, multiplicity);
				}

				int expectedTargets = in.readInt();
				if(expectedTargets>0) {
					Set<LayerPointer> targets = new ObjectOpenHashSet<>();
					while(expectedTargets-->0) {
						String contextId = (String)in.readObject();
						String layerId = (String)in.readObject();

						targets.add(new LayerPointerImpl(contextId, layerId));
					}
					entry.targets = targets;
				}
			}
		}

		private Object readResolve()
                throws ObjectStreamException {

			LayerBinding result = build();
			reset();

			return result;
		}
	}
}
