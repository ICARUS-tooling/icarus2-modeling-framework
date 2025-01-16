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
/**
 *
 */
package de.ims.icarus2.query.api.engine.result;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

import com.google.common.annotations.VisibleForTesting;

/**
 * Models an entry within a complex search result. Each entry is composed
 * of a {@link Match} and a series of associated payload values. Internally
 * the payload is stored as a simple long array and other result-related
 * framework members will provide appropriate facilities to access that
 * payload data.
 *
 * @author Markus Gärtner
 *
 */
public final class ResultEntry {
	/** The match or MultiMatch assigned to this entry */
	private final Match match;
	/** Computed values for sorting, grouping and other forms of result processing */
	private final long[] payload;

	public ResultEntry(Match match, int payloadSize) {
		this.match = requireNonNull(match);
		this.payload = new long[payloadSize];
	}

	public Match getMatch() { return match; }

	@Override
	public boolean equals(Object obj) {
		if(obj==this) {
			 return true;
		} else if(obj instanceof ResultEntry) {
			ResultEntry other = (ResultEntry) obj;
			return Arrays.equals(payload, other.payload)
					&& match.equals(other.match);
		}
		return false;
	}

	long payloadAt(int index) { return payload[index]; }

	void setPayload(int index, long value) { payload[index] = value; }

	@VisibleForTesting
	void setPayload(long[] payload) {
		assert payload.length==this.payload.length;
		System.arraycopy(payload, 0, this.payload, 0, payload.length);
	}

	@VisibleForTesting
	long[] getPayload() { return payload; }

	@Override
	public int hashCode() {
		return match.hashCode() ^ Arrays.hashCode(payload);
	}

	@Override
	public String toString() { return String.format("Result@[%s, %s]", match, Arrays.toString(payload)); }
}
