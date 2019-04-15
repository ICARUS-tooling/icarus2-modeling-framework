/**
 *
 */
package de.ims.icarus2.model.standard.view.streamed;

import static org.mockito.Mockito.mock;

import java.util.EnumSet;
import java.util.Set;

import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.view.streamed.StreamedCorpusViewTest;
import de.ims.icarus2.util.AccessMode;

/**
 * @author Markus Gärtner
 *
 */
class DefaultStreamedCorpusViewTest implements StreamedCorpusViewTest<DefaultStreamedCorpusView> {

	/**
	 * @see de.ims.icarus2.util.PartTest#createEnvironment()
	 */
	@Override
	public Corpus createEnvironment() {
		return mock(Corpus.class);
	}

	/**
	 * @see de.ims.icarus2.model.api.view.CorpusViewTest#getSupportedAccessModes()
	 */
	@Override
	public Set<AccessMode> getSupportedAccessModes() {
		return EnumSet.of(AccessMode.READ, AccessMode.READ_WRITE);
	}

	/**
	 * @see de.ims.icarus2.test.TargetedTest#getTestTargetClass()
	 */
	@Override
	public Class<? extends DefaultStreamedCorpusView> getTestTargetClass() {
		return DefaultStreamedCorpusView.class;
	}

	/**
	 * @see de.ims.icarus2.model.api.view.streamed.StreamedCorpusViewTest#createView(de.ims.icarus2.model.api.corpus.Corpus)
	 */
	@Override
	public DefaultStreamedCorpusView createView(Corpus corpus) {
		// TODO Auto-generated method stub
		return null;
	}

}
