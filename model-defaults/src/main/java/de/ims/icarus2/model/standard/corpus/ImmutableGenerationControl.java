/*
 *  ICARUS 2 -  Interactive platform for Corpus Analysis and Research tools, University of Stuttgart
 *  Copyright (C) 2015-2016 Markus Gärtner
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses.
 */
package de.ims.icarus2.model.standard.corpus;

import static java.util.Objects.requireNonNull;

import de.ims.icarus2.GlobalErrorCode;
import de.ims.icarus2.model.api.ModelErrorCode;
import de.ims.icarus2.model.api.ModelException;
import de.ims.icarus2.model.api.corpus.Corpus;
import de.ims.icarus2.model.api.corpus.GenerationControl;

/**
 * Implements a generation control without any state.
 * The {@link #getStage()} method will always return {@link GenerationControl#INITIAL_STAGE}
 * and both modification methods ({@link #advance()} and {@link #step(long, long)}) will
 * throw a {@link ModelException} of code {@link GlobalErrorCode#ILLEGAL_STATE} when called.
 *
 * @author Markus Gärtner
 *
 */
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
		} else
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
