/**
 *
 */
package de.ims.icarus2.model.standard.members.layers.annotation.packed;

import static de.ims.icarus2.util.IcarusUtils.UNSET_INT;
import static java.util.Objects.requireNonNull;

import java.io.Closeable;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;

import de.ims.icarus2.model.manifest.api.AnnotationLayerManifest;
import de.ims.icarus2.model.manifest.api.AnnotationManifest;
import de.ims.icarus2.model.manifest.api.ContextManifest;
import de.ims.icarus2.model.manifest.api.ItemLayerManifestBase;
import de.ims.icarus2.model.manifest.api.ManifestException;
import de.ims.icarus2.model.manifest.types.ValueType;
import de.ims.icarus2.model.manifest.util.ManifestUtils;
import de.ims.icarus2.util.collections.LazyCollection;
import de.ims.icarus2.util.collections.LookupList;

/**
 * @author Markus Gärtner
 *
 */
public class PackedDataUtils {

	/**
	 * Creates a set of {@link PackageHandle} instances describing all the
	 * {@link AnnotationManifest}s within the given {@link ContextManifest}
	 * that reference the context's {@link ContextManifest#getPrimaryLayerManifest() primary layer}.
	 *
	 * @param contextManifest
	 * @return
	 */
	public static Set<PackageHandle> createHandles(ContextManifest contextManifest,
			boolean allowBitPacking) {
		ItemLayerManifestBase<?> primaryLayer = (ItemLayerManifestBase<?>) contextManifest.getPrimaryLayerManifest()
				.orElseThrow(ManifestException.missing(contextManifest, "primary layer"))
				.getResolvedLayerManifest()
				.orElseThrow(ManifestException.missing(contextManifest, "resolved primary layer"));

		LazyCollection<PackageHandle> buffer = LazyCollection.lazySet();

		contextManifest.getLayerManifests().stream()
			.filter(ManifestUtils::isAnnotationLayerManifest)
			.map(AnnotationLayerManifest.class::cast)
			.forEach(manifest -> {
				if(manifest.isBaseLayerManifest(primaryLayer)) {
					manifest.forEachAnnotationManifest(annotationManifest ->
						buffer.add(createHandle(annotationManifest, allowBitPacking)));
				}
			});

		return buffer.getAsSet();
	}

	public static Set<PackageHandle> createHandles(AnnotationLayerManifest manifest,
			boolean allowBitPacking) {
		LazyCollection<PackageHandle> buffer = LazyCollection.lazySet();
		manifest.forEachAnnotationManifest(annotationManifest ->
			buffer.add(createHandle(annotationManifest, allowBitPacking)));

		return buffer.getAsSet();
	}

	public static PackageHandle createHandle(AnnotationManifest manifest, boolean allowBitPacking) {
		ValueType valueType = manifest.getValueType();

		// Easy mode for primitive values
		if(valueType.isPrimitiveType()) {
			return new PackageHandle(manifest, manifest.getNoEntryValue().orElseThrow(
					ManifestException.missing(manifest, "primitive noEntryValue")),
					BytePackConverter.forPrimitiveType(valueType, allowBitPacking));
		}

		// From here on only option is using substitutions
		return createDefaultSubstitutingHandler(manifest);
	}

	private static PackageHandle createDefaultSubstitutingHandler(AnnotationManifest manifest) {
		LookupList<Object> buffer = new LookupList<>(1000);
		BytePackConverter converter = new BytePackConverter.SubstitutingConverterInt<>(
				manifest.getValueType(), 4,
				new Substitutor<>(buffer, true),
				new Resubstitutor<>(buffer));

		return new PackageHandle(manifest, manifest.getNoEntryValue().orElse(null), converter);
	}

	public static class Substitutor<T> implements Closeable, ToIntFunction<T> {
		private final LookupList<T> buffer;
		private final boolean clearOnClose;

		public Substitutor(LookupList<T> buffer, boolean clearOnClose) {
			this.buffer = requireNonNull(buffer);
			this.clearOnClose = clearOnClose;
		}

		@Override
		public int applyAsInt(T value) {
			int index = buffer.indexOf(value);
			if(index==UNSET_INT) {
				index = buffer.size();
				buffer.add(index, value);
			}
			return index;
		}

		@Override
		public void close() {
			if(clearOnClose) {
				buffer.clear();
			}
		}
	}

	public static class Resubstitutor<T> implements IntFunction<T> {
		private final LookupList<T> buffer;

		public Resubstitutor(LookupList<T> buffer) {
			this.buffer = requireNonNull(buffer);
		}

		@Override
		public T apply(int value) {
			return buffer.get(value);
		}
	}
}
