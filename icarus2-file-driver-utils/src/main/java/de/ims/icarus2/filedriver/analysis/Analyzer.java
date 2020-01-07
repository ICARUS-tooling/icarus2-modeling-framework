/**
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2020 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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

import de.ims.icarus2.Report.ReportItemCollector;

/**
 * @author Markus Gärtner
 *
 */
public interface Analyzer {

	/**
	 * Initializes this analyzer to use the given
	 * {@link ReportItemCollector} as logging facility
	 * to report to.
	 *
	 * @param log
	 */
	default void init(ReportItemCollector log) {
		// no-op
	}

	/**
	 * Publishes the analysis results as metadata entries
	 * and resets this analyzer.
	 */
	default void finish() {
		// no-op
	}
}
