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
 *
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
	 * Advances into the next generation stage and returns the new id.
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
	 * @author Markus
	 *
	 */
	public interface Stage extends Comparable<Stage>, StringResource {

	}
}
