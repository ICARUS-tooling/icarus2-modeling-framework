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

/**
 *
 *
 * @author Markus Gärtner
 *
 */
public interface CorpusGeneration {

	Corpus getCorpus();

	/**
	 * Returns the current generation id.
	 *
	 * @return
	 */
	long getStage();

	/**
	 * Advances into the next generation and returns the new id.
	 *
	 * @return
	 */
	long advance();

	/**
	 * Attempts to set a new generation id with the assumption that the current
	 * id equals {@code expectedId}. If this check fails, no changes will be made
	 * and instead the method simply returns {@code false}.
	 *
	 * @param expectedId
	 * @param newId
	 * @return
	 */
	boolean step(long expectedId, long newId);
}
