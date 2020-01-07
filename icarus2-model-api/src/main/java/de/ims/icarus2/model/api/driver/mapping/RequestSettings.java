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
package de.ims.icarus2.model.api.driver.mapping;

import java.util.Collections;
import java.util.EnumSet;

import javax.annotation.Nullable;

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
		public RequestSettings setHint(RequestHint hint) {
			return this;
		}

		@Override
		public RequestSettings removeHint(RequestHint hint) {
			return this;
		}
	};

	public static RequestSettings none() {
		return emptySettings;
	}

	public static RequestSettings fallback(@Nullable RequestSettings settings) {
		return settings==null ? emptySettings : settings;
	}

	public static RequestSettings settings() {
		return new RequestSettings();
	}

	public static RequestSettings withHints(RequestHint...hints) {
		return settings().setHints(hints);
	}

	public RequestSettings setHint(RequestHint hint) {
		if(hints==null) {
			hints = EnumSet.noneOf(RequestHint.class);
		}

		hints.add(hint);

		return this;
	}

	public RequestSettings setHints(RequestHint...newHints) {
		if(hints==null) {
			hints = EnumSet.noneOf(RequestHint.class);
		}

		Collections.addAll(hints, newHints);

		return this;
	}

	public RequestSettings removeHint(RequestHint hint) {
		if(hints!=null) {
			hints.remove(hint);
		}
		return this;
	}

	public boolean isHintSet(RequestHint hint) {
		return hints!=null && hints.contains(hint);
	}
}
