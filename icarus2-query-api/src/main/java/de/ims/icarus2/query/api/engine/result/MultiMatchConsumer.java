/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2021 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
/**
 *
 */
package de.ims.icarus2.query.api.engine.result;

import de.ims.icarus2.query.api.engine.LaneConfig;

/**
 * Models the transmission of a single-stream match result that can stretch
 * across multiple lanes.
 *
 * @author Markus Gärtner
 *
 */
public interface MultiMatchConsumer {

	/** Called at the beginning of an entire match */
	void resultBegin();
	/** Called at the end of an entire match */
	void resultEnd();

	/** Called at the beginning of a single-lane match */
	void matchBegin(LaneConfig lane, int size);
	/** Called at the end of a single-lane match */
	void matchEnd(LaneConfig lane);
	/** Called for each individual mapping inside a single-lane match */
	void mapping(LaneConfig lane, int mappingId, int index);
}
