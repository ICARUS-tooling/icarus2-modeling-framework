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
