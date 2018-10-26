/*
 * ICARUS2 Corpus Modeling Framework
 * Copyright (C) 2014-2018 Markus Gärtner <markus.gaertner@ims.uni-stuttgart.de>
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
package de.ims.icarus2.model.api.driver.mapping;

import java.util.EnumSet;

import de.ims.icarus2.util.Options;

/**
 * @author Markus Gärtner
 *
 */
public class RequestSettings extends Options {

	private static final long serialVersionUID = -5373096737649271204L;

	private EnumSet<RequestHint> hints;

	public static final RequestSettings emptySettings = new RequestSettings() {

		private static final long serialVersionUID = -929348039219943621L;

		@Override
		public Object put(String key, Object value) {
			return null;
		}

		@Override
		public Object remove(Object key) {
			return null;
		}

		@Override
		public void setHint(RequestHint hint) {
			// no-op
		}

		@Override
		public void removeHint(RequestHint hint) {
			// no-op
		}
	};

	public static RequestSettings none() {
		return emptySettings;
	}

	public void setHint(RequestHint hint) {
		if(hints==null) {
			hints = EnumSet.noneOf(RequestHint.class);
		}

		hints.add(hint);
	}

	public void removeHint(RequestHint hint) {
		if(hints!=null) {
			hints.remove(hint);
		}
	}

	public boolean isHintSet(RequestHint hint) {
		return hints!=null && hints.contains(hint);
	}
}
