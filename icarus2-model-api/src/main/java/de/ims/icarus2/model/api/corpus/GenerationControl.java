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
package de.ims.icarus2.model.api.corpus;

import de.ims.icarus2.util.strings.StringResource;

/**
 * A simple "versioning" scheme for corpus objects.
 * It assumes that modifications transit the corpus from one generation stage into another,
 * either forward or backwards.
 * <p>
 * To ensure consistency over multiple subsequent undo-operations and intermediate
 * {@link #advance() forward steps}
 *
 * @author Markus Gärtner
 *
 */
public interface GenerationControl {

	Corpus getCorpus();

	/**
	 * Returns the current generation stage.
	 *
	 * @return
	 */
	Stage getStage();

	/**
	 * Advances into the next generation stage and returns the new marker.
	 *
	 * @return
	 */
	Stage advance();

	/**
	 * Attempts to set a new generation stage with the assumption that the current
	 * id equals {@code expectedStage}. If this check fails, no changes will be made
	 * and instead the method simply returns {@code false}.
	 * <p>
	 * Note that this method must ensure the corpus' {@link Corpus#getLock() lock}
	 * is held by the thread that attempts to change the generation stage.
	 *
	 * @param expectedStage
	 * @param newStage
	 * @return
	 */
	//FIXME not the most reliable strategy, since we can end up with the same "situation" stored in a previous edit after redoing and advancing
	boolean step(Stage expectedStage, Stage newStage);

	/**
	 * Maintenance method called when a corpus shuts down.
	 * Intended to persist the current generation stage by whatever method the
	 * implementation uses for storage.
	 */
	void close();

	/**
	 * Parse the given input string as a stage object belonging to this generation
	 * control instance.
	 *
	 * @param s
	 * @return
	 */
	Stage parseStage(String s);

	/**
	 * A marker for a certain stage in a corpus' modification life-cycle.
	 *
	 * @author Markus Gärtner
	 *
	 */
	public interface Stage extends Comparable<Stage>, StringResource {
		// marker interface
	}
}
