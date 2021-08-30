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
package de.ims.icarus2.query.api.iql;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Markus Gärtner
 *
 */
@Deprecated
public class IqlFragment extends AbstractIqlQueryElement {

	@JsonProperty(IqlTags.START)
	private int start;
	@JsonProperty(IqlTags.START)
	private int stop;

	/**
	 * @see de.ims.icarus2.query.api.iql.IqlQueryElement#getType()
	 */
	@Override
	public IqlType getType() {
//		return IqlType.FRAGMENT;
		return null; //TODO temporarily disabled
	}

	/**
	 * @see de.ims.icarus2.query.api.iql.AbstractIqlQueryElement#checkIntegrity()
	 */
	@Override
	public void checkIntegrity() {
		super.checkIntegrity();
		checkCondition(start>=0, "start", "start must not be negative");
		checkCondition(stop>=start, "stop", "stop must be equal or greater than start");
	}

	public int getStart() { return start; }

	public int getStop() { return stop; }

	public void setStart(int start) { this.start = start; }

	public void setStop(int stop) { this.stop = stop; }
}
