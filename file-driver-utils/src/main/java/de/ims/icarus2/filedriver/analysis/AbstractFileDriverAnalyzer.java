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
package de.ims.icarus2.filedriver.analysis;

import static de.ims.icarus2.util.Conditions.checkState;
import static java.util.Objects.requireNonNull;

import de.ims.icarus2.Report.ReportItemCollector;
import de.ims.icarus2.filedriver.FileDataStates;

/**
 * Abstract analyzer implementation that is linked to an instance
 * of {@link FileDataStates} that is used to read and write actual
 * metadata.
 *
 * @author Markus Gärtner
 *
 */
public abstract class AbstractFileDriverAnalyzer implements Analyzer {

	/**
	 * The log to report problems to.
	 */
	private ReportItemCollector log;

	/**
	 * Storage to collect metadata in
	 */
	private final FileDataStates states;

	protected AbstractFileDriverAnalyzer(FileDataStates states) {
		this.states = requireNonNull(states);
	}

	/**
	 * @see de.ims.icarus2.filedriver.analysis.Analyzer#init(de.ims.icarus2.Report.ReportItemCollector)
	 */
	@Override
	public void init(ReportItemCollector log) {
		this.log = log;

		readStates(states);
	}

	protected ReportItemCollector getLog() {
		checkState("Log not set yet", log!=null);
		return log;
	}

	/**
	 * Read content from the given {@link FileDataStates states}
	 * to initialize internal statistics fields.
	 * <p>
	 * This is an optional method and only needs to be overridden
	 * if the implementation actually requires already computed data
	 * for the current layer or file.
	 *
	 * @param states
	 */
	protected void readStates(FileDataStates states) {
		// no-op
	}

	/**
	 * Write result of analysis from internal statistics fields
	 * into specified {@link FileDataStates states}.
	 *
	 * @param states
	 */
	protected abstract void writeStates(FileDataStates states);

	/**
	 * @see de.ims.icarus2.filedriver.analysis.Analyzer#finish()
	 */
	@Override
	public void finish() {
		writeStates(states);
	}
}
