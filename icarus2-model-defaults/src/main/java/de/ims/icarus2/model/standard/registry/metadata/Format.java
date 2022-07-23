/**
 *
 */
package de.ims.icarus2.model.standard.registry.metadata;

import java.util.Optional;

import de.ims.icarus2.util.LazyStore;

public enum Format {
	XML(JAXBMetadataRegistry.DEFAULT_FILE_ENDING),
	PLAIN(PlainMetadataRegistry.DEFAULT_FILE_ENDING),
	;

	private final String fileSuffix;

	private Format(String fileSuffix) {
		this.fileSuffix = fileSuffix;
	}

	public String getFileSuffix() {
		return fileSuffix;
	}

	private static LazyStore<Format, String> _store = new LazyStore<>(Format.class, f -> f.fileSuffix.toLowerCase());

	/** Looks up the format that uses the given file suffix. This lookup ignores case. */
	public static Optional<Format> forFileSuffix(String fileSuffix) {
		return _store.tryLookup(fileSuffix.toLowerCase());
	}
}