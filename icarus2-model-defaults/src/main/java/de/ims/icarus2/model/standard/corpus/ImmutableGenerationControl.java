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
package de.ims.icarus2.model.standard.corpus;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.corpus.GenerationControl;
import de.ims.icarus2.util.annotations.TestableImplementation;

/**
 * Implements a generation control without any state.
 * The {@link #getStage()} method will always return {@link GenerationControl#INITIAL_STAGE}
 * and both modification methods ({@link #advance()} and {@link #step(long, long)}) will
 * throw a {@link ModelException} of code {@link GlobalErrorCode#ILLEGAL_STATE} when called.
 *
 * @author Markus Gärtner
 *
 */
@TestableImplementation(GenerationControl.class)
public class ImmutableGenerationControl implements GenerationControl {

	private final Corpus corpus;

	private final Stage stage = new EmptyStage();

	/**
	 * @param corpus
	 */
	public ImmutableGenerationControl(Corpus corpus) {
		requireNonNull(corpus);

		this.corpus = corpus;
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.GenerationControl#getCorpus()
	 */
	@Override
	public Corpus getCorpus() {
		return corpus;
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.GenerationControl#getStage()
	 */
	@Override
	public Stage getStage() {
		return stage;
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.GenerationControl#advance()
	 */
	@Override
	public Stage advance() {
		throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Corpus is immutable - cannot advance generation stage");
	}


	@Override
	public boolean step(Stage expectedStage, Stage newStage) {
		throw new ModelException(GlobalErrorCode.ILLEGAL_STATE, "Corpus is immutable - cannot alter generation stage");
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.GenerationControl#parseStage(java.lang.String)
	 */
	@Override
	public Stage parseStage(String s) {
		if(EmptyStage.LABEL.equals(s)) {
			return stage;
		}

		throw new ModelException(corpus, ModelErrorCode.MODEL_INVALID_REQUEST, "Not a valid serialized form of stage: "+s);
	}

	/**
	 * @see de.ims.icarus2.model.api.corpus.GenerationControl#close()
	 */
	@Override
	public void close() {
		// no-op
	}

	private static final class EmptyStage implements Stage {

		public static final String LABEL = "INITIAL_STAGE";

		/**
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(Stage o) {
			return equals(o) ? 0 : 1;
		}

		/**
		 * @see de.ims.icarus2.util.strings.StringResource#getStringValue()
		 */
		@Override
		public String getStringValue() {
			return LABEL;
		}

		//TODO hashCode, equals and toString methods!
	}
}
