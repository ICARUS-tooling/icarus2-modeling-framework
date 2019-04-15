/**
 *
 */
package de.ims.icarus2.model.api.view.streamed;

import static de.ims.icarus2.util.IcarusUtils.UNSET_LONG;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.corpus.OwnableCorpusPartTest;
import de.ims.icarus2.model.api.view.CorpusViewTest;
import de.ims.icarus2.test.TestSettings;
import de.ims.icarus2.test.annotations.Provider;
import de.ims.icarus2.util.AccessMode;

/**
 * @author Markus Gärtner
 *
 */
public interface StreamedCorpusViewTest<V extends StreamedCorpusView>
		extends OwnableCorpusPartTest<V>, CorpusViewTest<V> {

	@Provider
	V createView(Corpus corpus, AccessMode accessMode, long size);

	/**
	 * @see de.ims.icarus2.util.PartTest#createPart()
	 */
	@Override
	default V createPart() {
		return createView(createEnvironment(), AccessMode.READ, UNSET_LONG);
	}

	/**
	 * @see de.ims.icarus2.model.api.view.CorpusViewTest#createForAccessMode(de.ims.icarus2.util.AccessMode)
	 */
	@Override
	default V createForAccessMode(AccessMode accessMode) {
		return createView(createEnvironment(), accessMode, UNSET_LONG);
	}

	/**
	 * @see de.ims.icarus2.model.api.view.CorpusViewTest#createForSize(long)
	 */
	@Override
	default V createForSize(long size) {
		return createView(createEnvironment(), AccessMode.READ, size);
	}

	/**
	 * @see de.ims.icarus2.test.Testable#createTestInstance(de.ims.icarus2.test.TestSettings)
	 */
	@Override
	default V createTestInstance(TestSettings settings) {
		return settings.process(createPart());
	}

	/**
	 * @see de.ims.icarus2.test.GenericTest#createNoArgs()
	 */
	@Override
	default V createNoArgs() {
		throw new UnsupportedOperationException("Views are supposed to be created via builders");
	}
}
